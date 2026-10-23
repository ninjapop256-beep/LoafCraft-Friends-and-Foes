package com.itzbreadloaf.brain;

import carpet.patches.EntityPlayerMPFake;
import com.itzbreadloaf.ModAdvancements;
import com.itzbreadloaf.ModMessages;
import com.itzbreadloaf.character.CharacterKind;
import com.itzbreadloaf.character.CharacterVariant;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.LoafCharacter;
import com.itzbreadloaf.character.LoafIds;
import com.itzbreadloaf.item.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The mind of one character fake player. Ticked every server tick by {@link CharacterManager}; it never moves the
 * player directly, it only presses keys through {@link Controls} (Carpet's action pack).
 *
 * This base class has what every character shares: owner and sitting, home and bed, eating real food from the
 * inventory, wandering, following the owner (through portals too), taming,
 * babies and breeding. Each character's personality lives in its subclass.
 */
public abstract class LoafBrain {
	/** Babies grow up after 20 minutes. Feeding them their parent's favorite food takes 10% off. */
	public static final int GROW_UP_TICKS = 24000;
	public static final int BREED_COOLDOWN_TICKS = 6000;
	public static final int LOVE_TICKS = 600;
	private static final Identifier BABY_SCALE_ID = Identifier.fromNamespaceAndPath("itzbreadloaf", "baby_scale");
	private static final Identifier BABY_HEALTH_ID = Identifier.fromNamespaceAndPath("itzbreadloaf", "baby_health");
	private static final Identifier BABY_SPEED_ID = Identifier.fromNamespaceAndPath("itzbreadloaf", "baby_speed");

	protected final LoafCharacter character;
	protected final EntityPlayerMPFake player;
	protected final CharacterKind kind;
	protected final Controls controls;
	protected final Navigator nav;
	protected final Combat combat;
	protected final Builder builder;
	protected final RandomSource random;
	protected final CharacterVariant variant;
	protected int ticks;

	// ---- saved
	protected @Nullable UUID owner;
	protected boolean sitting;
	protected @Nullable BlockPos home;
	protected int homeRadius = 10;
	protected @Nullable BlockPos bed;
	/** Spawned naturally: forgotten (not saved) when despawned, unless someone befriended it. */
	protected boolean wild;
	private boolean paused;
	protected boolean baby;
	protected int babyAge;
	protected int love;
	protected int breedCooldown;
	protected @Nullable UUID parentA;
	protected @Nullable UUID parentB;

	// ---- runtime
	protected @Nullable LivingEntity target;
	protected int lastHurtTick = -1000;
	protected @Nullable LivingEntity lastAttacker;
	private int nextChatterTick = 600;
	/** How long the way home has been blocked (to retry, and teleport home if nobody is watching). */
	private int homeFailTicks;
	private int eatTicks;
	private boolean eatStarted;
	private int eatCooldown;
	private int wanderTimer;
	private int lastNearOwnerTick = -1000;
	private int lookTimer;
	private @Nullable Entity lookTarget;

	protected LoafBrain(LoafCharacter character) {
		this.character = character;
		this.player = character.player;
		this.kind = character.kind();
		this.random = this.player.getRandom();
		this.controls = new Controls(this.player, character.actions());
		this.nav = new Navigator(this.player, this.controls);
		this.combat = new Combat(this.player, this.controls, this.random);
		this.builder = new Builder(this.player, this.controls);
		this.variant = character.record.variant();
	}

	public static LoafBrain create(LoafCharacter character) {
		return switch (character.kind()) {
			case BREADLOAF -> new BreadLoafBrain(character);
			case LILY -> new LilyBrain(character);
			case MADISON -> new MadisonBrain(character);
			case MASTER -> new MasterBreadLoafBrain(character);
			case FELIXSYN -> new FelixBrain(character);
			case HARLEY -> new HarleyBrain(character);
		};
	}

	// ================================================================ hooks for each character

	/** Fills the inventory with this character's gear (weapon, armor, shield, tools...) for an adult. */
	protected abstract void rollGear();

	/** Puts food and other supplies in the inventory (when new, and as they run low). */
	protected abstract void restock();

	/** The personality: called every tick unless asleep, sitting or eating. */
	protected abstract void tickBehavior();

	protected double baseMaxHealth() {
		return 20.0;
	}

	protected double baseAttackDamage() {
		return 1.0;
	}

	protected double baseSpeed() {
		return 0.1;
	}

	/** Food that puts a tamed adult in love mode and makes babies of this kind grow faster. */
	public Item breedFood() {
		return Items.AIR;
	}

	public boolean isFriendlyToMobs() {
		return false;
	}

	/** Neutral characters (Lily, Madison) and tamed ones are never "hostile" for sounds. */
	public boolean isAngry() {
		return false;
	}

	public SoundEvent deathSound() {
		return SoundEvents.PLAYER_DEATH;
	}

	public @Nullable SoundEvent hurtSound() {
		return null;
	}

	public int experienceReward() {
		return this.baby ? 0 : 5;
	}


	// ================================================================ lifecycle

	public void onFirstSpawn() {
		if (!this.baby) {
			this.rollGear();
		}
		this.restock();
	}

	/** Health, damage, speed and baby size, written into the player's attributes. */
	public void applyAttributes() {
		setBase(Attributes.MAX_HEALTH, this.baseMaxHealth());
		setBase(Attributes.ATTACK_DAMAGE, this.baseAttackDamage());
		setBase(Attributes.MOVEMENT_SPEED, this.baseSpeed());
		this.applyBabyModifiers();
		if (this.player.getHealth() > this.player.getMaxHealth()) {
			this.player.setHealth(this.player.getMaxHealth());
		}
	}

	protected void setBase(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value) {
		AttributeInstance instance = this.player.getAttribute(attribute);
		if (instance != null) {
			instance.setBaseValue(value);
		}
	}

	/** Babies: 0.5 scale growing to 1.0, half health, a little faster. */
	protected void applyBabyModifiers() {
		AttributeInstance scale = this.player.getAttribute(Attributes.SCALE);
		AttributeInstance health = this.player.getAttribute(Attributes.MAX_HEALTH);
		AttributeInstance speed = this.player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (scale == null || health == null || speed == null) {
			return;
		}
		if (this.baby) {
			double grown = Math.min(1.0, this.babyAge / (double) GROW_UP_TICKS);
			// 0.5 -> 1.0 as they grow (scale attribute base is 1.0).
			scale.addOrReplacePermanentModifier(new AttributeModifier(BABY_SCALE_ID, -0.5 * (1.0 - grown), AttributeModifier.Operation.ADD_VALUE));
			health.addOrReplacePermanentModifier(new AttributeModifier(BABY_HEALTH_ID, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
			speed.addOrReplacePermanentModifier(new AttributeModifier(BABY_SPEED_ID, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		} else {
			scale.removeModifier(BABY_SCALE_ID);
			health.removeModifier(BABY_HEALTH_ID);
			speed.removeModifier(BABY_SPEED_ID);
		}
	}

	public void onActivated() {
		this.lastNearOwnerTick = 0;
	}

	public void onDeactivated() {
		if (this.player.isSleeping()) {
			this.player.stopSleeping();
		}
		this.combat.stop();
		this.builder.stopDigging();
		this.controls.releaseAll();
	}

	/** Natural spawns nobody cares about are forgotten when despawned. */
	public boolean isDisposable() {
		return this.wild && !this.isTame();
	}

	/** Stay spawned even when no real player is near (nothing needs this by default). */
	public boolean keepActive() {
		return false;
	}

	// ================================================================ ticking

	public final void tick() {
		this.ticks++;
		if (this.paused) {
			// Hands off: whatever /loafcraft action (or Carpet's /player) set up keeps running.
			return;
		}
		this.controls.begin();
		this.builder.tick();
		this.upkeep();
		if (this.target != null && (!this.target.isAlive() || this.target.isRemoved() || this.target.level() != this.player.level()
				|| this.target instanceof Player p && (p.isCreative() || p.isSpectator()))) {
			this.setTarget(null);
		}

		if (this.player.isSleeping()) {
			if (this.target != null || this.player.level().isBrightOutside()) {
				this.player.stopSleeping();
			}
			this.controls.releaseAll();
		} else if (this.tickEating()) {
			this.nav.tick();
		} else if (this.sitting && this.target == null) {
			this.nav.stop();
			this.controls.setSneaking(true);
			this.lookAround(8.0);
		} else {
			this.controls.setSneaking(false);
			this.tickChatter();
			this.tickBehavior();
			this.nav.tick();
		}
		this.controls.apply();
	}

	/** Every-tick and every-second housekeeping shared by all characters. */
	private void upkeep() {
		if (this.eatCooldown > 0) {
			this.eatCooldown--;
		}
		if (this.love > 0) {
			this.love--;
			if (this.ticks % 10 == 0) {
				this.particles(ParticleTypes.HEART, 1);
			}
		}
		if (this.breedCooldown > 0) {
			this.breedCooldown--;
		}
		if (this.baby) {
			this.babyAge++;
			if (this.ticks % 200 == 0) {
				this.applyBabyModifiers();
			}
			if (this.babyAge >= GROW_UP_TICKS) {
				this.growUp();
			}
		}
		if (this.ticks % 20 == 0) {
			// Characters never need to rest: no phantoms.
			this.player.getStats().setValue(this.player, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 0);
		}
		if (this.ticks % 10 == 0) {
			this.followOwnerThroughPortals();
		}
		if (this.ticks % 6000 == 0) {
			this.restock();
		}
	}

	// ================================================================ targets

	public @Nullable LivingEntity getTarget() {
		return this.target;
	}

	public void setTarget(@Nullable LivingEntity target) {
		if (target != null && target != this.target && this.owner != null && this.isFriendlyWith(target)) {
			// Tamed characters never pick a fight with their owner, the owner's friends, or anyone's pets on that list.
			return;
		}
		LivingEntity old = this.target;
		this.target = target;
		if (target == null) {
			this.combat.stop();
			this.builder.stopDigging();
		} else if (target != old) {
			this.onNewTarget(target);
		}
	}

	protected void onNewTarget(LivingEntity target) {
		if (this.player.isSleeping()) {
			this.player.stopSleeping();
		}
		this.sitting = false;
	}

	/** Fights the current target with the best weapon for the distance. */
	protected void fightTarget(boolean allowRanged) {
		if (this.target == null) {
			return;
		}
		double distance = this.player.distanceTo(this.target);
		if (this.ticks % 10 == 0 || this.combat.held() == Combat.Weapon.FISTS) {
			Combat.Weapon current = this.combat.held();
			boolean busy = this.player.isUsingItem() && current.isRanged();
			if (!busy) {
				this.combat.equipBest(distance, allowRanged);
			}
		}
		this.combat.fight(this.target, this.nav);
	}

	public void onHurt(DamageSource source, float damage, boolean blocked) {
		this.lastHurtTick = this.ticks;
		if (source.getEntity() instanceof LivingEntity attacker && attacker != this.player) {
			this.lastAttacker = attacker;
		}
		this.sitting = false;
		if (this.player.isSleeping()) {
			this.player.stopSleeping();
		}
	}

	public void onDealtDamage(LivingEntity victim, float damage) {
	}

	public boolean allowDamage(DamageSource source, float amount) {
		return true;
	}

	/** Called when they die. Return true to drop the whole inventory like a player. */
	public boolean onDeath(DamageSource source) {
		return !this.baby;
	}

	// ================================================================ owner and sitting

	public boolean isTame() {
		return this.owner != null;
	}

	public @Nullable UUID getOwner() {
		return this.owner;
	}

	public boolean isOwnedBy(@Nullable Entity entity) {
		return entity != null && this.owner != null && this.owner.equals(entity.getUUID());
	}

	public @Nullable ServerPlayer getOwnerPlayer() {
		return this.owner == null ? null : this.player.level().getServer().getPlayerList().getPlayer(this.owner);
	}

	public void setOwner(@Nullable Player newOwner) {
		this.owner = newOwner == null ? null : newOwner.getUUID();
		this.sitting = false;
		this.wild = false;
		if (newOwner != null) {
			this.setTarget(null);
			this.home = null;
			this.bed = null;
		}
	}

	public void adoptByOwner(UUID ownerId) {
		this.owner = ownerId;
		this.wild = false;
	}

	public boolean isSitting() {
		return this.sitting;
	}

	public void setSitting(boolean sitting) {
		this.sitting = sitting;
		if (sitting) {
			this.nav.stop();
		}
	}

	/**
	 * The owner, characters with the same owner, the owner's pets, and (from /friend) the owner's friends plus their
	 * tamed characters and pets.
	 */
	public boolean isFriendlyWith(@Nullable Entity entity) {
		if (entity == null || this.owner == null) {
			return false;
		}
		if (this.isOwnedBy(entity)) {
			return true;
		}
		UUID theirs;
		LoafBrain other = CharacterManager.brain(entity);
		if (other != null) {
			theirs = other.owner;
		} else if (entity instanceof OwnableEntity ownable && ownable.getOwnerReference() != null) {
			theirs = ownable.getOwnerReference().getUUID();
		} else if (entity instanceof net.minecraft.world.entity.player.Player playerEntity) {
			theirs = playerEntity.getUUID();
		} else {
			return false;
		}
		return theirs != null && (this.owner.equals(theirs)
				|| com.itzbreadloaf.FriendCommand.isFriend(this.player.level().getServer(), this.owner, theirs));
	}

	/** Walks after the owner; teleports to them when far, like a tamed wolf. True if it did something. */
	protected boolean followOwner() {
		ServerPlayer ownerPlayer = this.getOwnerPlayer();
		if (ownerPlayer == null || ownerPlayer.isSpectator() || ownerPlayer.level() != this.player.level()) {
			return false;
		}
		double distance = this.player.distanceTo(ownerPlayer);
		if (distance > 24.0 && ownerPlayer.onGround() && !ownerPlayer.isFallFlying()) {
			Vec3 spot = this.findSpotNear(ownerPlayer.blockPosition(), 3);
			if (spot != null) {
				this.player.teleportTo(spot.x, spot.y, spot.z);
				this.nav.stop();
				return true;
			}
		}
		if (distance > 5.0) {
			this.nav.follow(ownerPlayer, 2.5, distance > 10.0 ? Navigator.Speed.SPRINT : Navigator.Speed.WALK);
			return true;
		}
		if (distance < 3.0) {
			this.nav.stop();
		}
		return false;
	}

	/** Tamed characters follow their owner anywhere, including through Nether and End portals. */
	private void followOwnerThroughPortals() {
		if (this.owner == null || this.sitting || this.baby) {
			return;
		}
		ServerPlayer ownerPlayer = this.getOwnerPlayer();
		if (ownerPlayer == null || ownerPlayer.isSpectator()) {
			return;
		}
		if (ownerPlayer.level() == this.player.level()) {
			if (this.player.distanceTo(ownerPlayer) < 16.0) {
				this.lastNearOwnerTick = this.ticks;
			}
		} else if (this.ticks - this.lastNearOwnerTick < 100) {
			this.player.teleport(new TeleportTransition(ownerPlayer.level(), ownerPlayer.position(), Vec3.ZERO,
					this.player.getYRot(), this.player.getXRot(), TeleportTransition.DO_NOTHING));
			this.lastNearOwnerTick = this.ticks;
		}
	}

	/** Pets defend their owner: whoever hurts the owner, and (if {@code alsoAttack}) whatever the owner attacks. */
	protected void protectOwner(boolean alsoAttack) {
		ServerPlayer ownerPlayer = this.getOwnerPlayer();
		if (ownerPlayer == null || ownerPlayer.level() != this.player.level() || this.baby) {
			return;
		}
		LivingEntity attacker = ownerPlayer.getLastHurtByMob();
		if (attacker != null && attacker.isAlive() && ownerPlayer.tickCount - ownerPlayer.getLastHurtByMobTimestamp() < 100
				&& !this.isFriendlyWith(attacker) && attacker != this.player) {
			this.setTarget(attacker);
			return;
		}
		if (alsoAttack) {
			LivingEntity victim = ownerPlayer.getLastHurtMob();
			if (victim != null && victim.isAlive() && ownerPlayer.tickCount - ownerPlayer.getLastHurtMobTimestamp() < 100
					&& !this.isFriendlyWith(victim) && victim != this.player && !(victim instanceof OwnableEntity o && o.getOwnerReference() != null)) {
				this.setTarget(victim);
			}
		}
	}

	// ================================================================ home, bed, wandering

	public void setupHome(BlockPos home, int radius, @Nullable BlockPos bed) {
		this.home = home.immutable();
		this.homeRadius = radius;
		this.bed = bed == null ? null : bed.immutable();
	}

	public @Nullable BlockPos getHome() {
		return this.home;
	}

	public void leaveHome() {
		this.home = null;
		this.bed = null;
	}

	public boolean isResident() {
		return this.home != null;
	}

	/** Night: walk to the bed and lie down. True while doing that. */
	protected boolean goToBed() {
		if (this.bed == null || this.isTame() || this.target != null || this.player.level().isBrightOutside()) {
			return false;
		}
		ServerLevel level = this.player.level();
		if (!(level.getBlockState(this.bed).getBlock() instanceof BedBlock)) {
			this.bed = null;
			return false;
		}
		if (level.getBlockState(this.bed).getValue(BedBlock.OCCUPIED)) {
			return false;
		}
		if (this.player.position().distanceTo(Vec3.atBottomCenterOf(this.bed)) > 2.0) {
			this.walkHome(this.bed, 1.5);
			return true;
		}
		this.nav.stop();
		this.controls.releaseAll();
		this.player.startSleeping(this.bed);
		return true;
	}

	/** Back inside the home radius. True while walking home. */
	protected boolean returnHome() {
		if (this.home == null) {
			return false;
		}
		// At night they stay closer to home.
		int radius = this.player.level().isBrightOutside() ? this.homeRadius : Math.max(4, this.homeRadius / 2);
		if (this.player.position().distanceTo(Vec3.atBottomCenterOf(this.home)) > radius + 3) {
			this.walkHome(this.home, 2.0);
			return true;
		}
		this.homeFailTicks = 0;
		return false;
	}

	/**
	 * Walks to home or bed. If the way is blocked it retries every 5 seconds, and after 20 seconds it simply
	 * teleports there, but only when no real player can see it happen.
	 */
	private void walkHome(BlockPos destination, double reach) {
		this.nav.moveTo(destination, reach, Navigator.Speed.WALK);
		if (!this.nav.failed()) {
			this.homeFailTicks = Math.max(0, this.homeFailTicks - 1);
			return;
		}
		this.homeFailTicks++;
		if (this.homeFailTicks % 100 == 0) {
			this.nav.retry();
		}
		if (this.homeFailTicks >= 400 && !this.isWatched(32.0)) {
			Vec3 spot = this.findSpotNear(destination, 2);
			if (spot != null) {
				this.player.teleportTo(spot.x, spot.y, spot.z);
				this.nav.stop();
			}
			this.homeFailTicks = 0;
		}
	}

	/** A real player within range who can see this character. */
	protected boolean isWatched(double range) {
		for (ServerPlayer viewer : this.player.level().players()) {
			if (LoafIds.isRealPlayer(viewer) && viewer.distanceTo(this.player) < range && viewer.hasLineOfSight(this.player)) {
				return true;
			}
		}
		return false;
	}

	// ================================================================ idle chatter

	/** Random things this character says when a player is nearby (shown in the action bar). */
	protected String[] chatterLines() {
		return new String[0];
	}

	/** What this character says to another character standing nearby, or null to say nothing to them. */
	protected @Nullable String[] chatWith(LoafBrain other) {
		return null;
	}

	/** Every 1-3 minutes: a line to a nearby player, or (if another character is close) to that character. */
	private void tickChatter() {
		if (this.ticks < this.nextChatterTick) {
			return;
		}
		com.itzbreadloaf.config.ModConfig config = com.itzbreadloaf.config.ModConfig.get();
		int min = Math.max(5, config.chatterMinSeconds);
		int max = Math.max(min, config.chatterMaxSeconds);
		this.nextChatterTick = this.ticks + 20 * (min + this.random.nextInt(max - min + 1));
		if (!config.idleChatter || this.target != null || this.baby || this.player.isSleeping()) {
			return;
		}
		ServerPlayer listener = null;
		for (ServerPlayer viewer : this.player.level().players()) {
			if (LoafIds.isRealPlayer(viewer) && viewer.distanceTo(this.player) < 10.0 && viewer.hasLineOfSight(this.player)) {
				listener = viewer;
				break;
			}
		}
		if (listener == null) {
			return;
		}
		for (LoafCharacter other : CharacterManager.active()) {
			if (other.brain == null || other.brain == this || other.player.level() != this.player.level()
					|| other.player.distanceTo(this.player) > 6.0) {
				continue;
			}
			String[] lines = this.chatWith(other.brain);
			if (lines != null && lines.length > 0) {
				this.controls.lookAt(other.player.getEyePosition());
				this.say(listener, lines[this.random.nextInt(lines.length)]);
				return;
			}
		}
		String[] lines = this.chatterLines();
		if (lines.length > 0) {
			this.say(listener, lines[this.random.nextInt(lines.length)]);
		}
	}

	/** Strolls to a random nearby spot now and then (around home if they have one). */
	protected void wander(double radius) {
		if (this.nav.isMoving()) {
			return;
		}
		if (--this.wanderTimer > 0) {
			this.lookAround(8.0);
			return;
		}
		this.wanderTimer = 100 + this.random.nextInt(200);
		BlockPos center = this.home != null ? this.home : this.player.blockPosition();
		int range = (int) Math.max(3, radius);
		BlockPos spot = center.offset(this.random.nextInt(range * 2 + 1) - range, 0, this.random.nextInt(range * 2 + 1) - range);
		Vec3 standable = this.findSpotNear(spot, 0);
		if (standable != null) {
			this.nav.moveTo(standable, 1.0, Navigator.Speed.WALK);
		}
	}

	/** Looks at a nearby real player now and then, otherwise around. */
	protected void lookAround(double range) {
		if (--this.lookTimer <= 0) {
			this.lookTimer = 40 + this.random.nextInt(60);
			Player nearest = this.player.level().getNearestPlayer(this.player.getX(), this.player.getY(), this.player.getZ(), range,
					p -> p != this.player && !p.isSpectator() && LoafIds.isRealPlayer(p));
			this.lookTarget = nearest != null && this.random.nextFloat() < 0.7F ? nearest : null;
			if (this.lookTarget == null && !this.controls.hasLook()) {
				this.controls.look(this.player.getYRot() + (this.random.nextFloat() - 0.5F) * 120.0F, (this.random.nextFloat() - 0.3F) * 30.0F);
			}
		}
		if (this.lookTarget != null && this.lookTarget.isAlive() && !this.controls.hasLook()) {
			this.controls.lookAt(this.lookTarget.getEyePosition());
		}
	}

	/** A standable spot at or near {@code pos} (same column, a few blocks up or down). */
	protected @Nullable Vec3 findSpotNear(BlockPos pos, int spread) {
		ServerLevel level = this.player.level();
		for (int attempt = 0; attempt < 8; attempt++) {
			BlockPos column = spread > 0 ? pos.offset(this.random.nextInt(spread * 2 + 1) - spread, 0, this.random.nextInt(spread * 2 + 1) - spread) : pos;
			if (!level.isLoaded(column)) {
				continue;
			}
			for (int dy = 4; dy >= -6; dy--) {
				BlockPos feet = column.above(dy);
				if (level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
						&& level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
						&& level.getFluidState(feet).isEmpty()
						&& level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP)) {
					return Vec3.atBottomCenterOf(feet);
				}
			}
			if (spread == 0) {
				int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
				if (Math.abs(y - pos.getY()) < 8) {
					return Vec3.atBottomCenterOf(new BlockPos(column.getX(), y, column.getZ()));
				}
				return null;
			}
		}
		return null;
	}

	// ================================================================ eating (real food from the inventory)

	protected boolean isHungry() {
		int food = this.player.getFoodData().getFoodLevel();
		return food <= 14 || food < 20 && this.player.getHealth() < this.player.getMaxHealth() * 0.7F;
	}

	/** Starts eating if hungry and there's food. Returns true if they started. */
	protected boolean tryEat() {
		if (this.eatTicks > 0 || this.eatCooldown > 0 || !this.isHungry() || this.player.isUsingItem() && !this.combat.isShieldUp()) {
			return false;
		}
		int slot = Inv.ready(this.player, Inv::isFood, 7);
		if (slot < 0) {
			return false;
		}
		this.combat.stop();
		this.controls.selectSlot(slot);
		this.controls.holdUse(true);
		this.eatTicks = 50;
		this.eatStarted = false;
		return true;
	}

	/** Eats (or drinks) something right now, hungry or not: golden apples in a fight. True if they started. */
	protected boolean eatNow(java.util.function.Predicate<ItemStack> what) {
		if (this.eatTicks > 0 || this.eatCooldown > 0) {
			return false;
		}
		int slot = Inv.ready(this.player, what, 7);
		if (slot < 0) {
			return false;
		}
		this.combat.stop();
		this.controls.selectSlot(slot);
		this.controls.holdUse(true);
		this.eatTicks = 50;
		this.eatStarted = false;
		return true;
	}

	/** While eating: hold right-click with the food (looking up, so the click doesn't hit a door or a bed). */
	private boolean tickEating() {
		if (this.eatTicks <= 0) {
			return false;
		}
		this.eatTicks--;
		if (this.player.isUsingItem()) {
			this.eatStarted = true;
		}
		boolean done = this.eatStarted && !this.player.isUsingItem() || this.eatTicks <= 0
				|| !this.player.getMainHandItem().has(net.minecraft.core.component.DataComponents.CONSUMABLE) && !this.player.isUsingItem();
		if (done) {
			this.controls.holdUse(false);
			this.eatTicks = 0;
			this.eatCooldown = 30;
			return false;
		}
		this.controls.holdUse(true);
		if (!this.controls.hasLook()) {
			this.controls.look(this.player.getYRot(), -35.0F);
		}
		// Keep backing off from a target while eating.
		if (this.target != null && this.player.distanceTo(this.target) < 6.0) {
			this.controls.walk(this.player.position().subtract(this.target.position()));
		}
		return true;
	}

	public boolean isEating() {
		return this.eatTicks > 0;
	}

	// ================================================================ babies and love

	public boolean isBaby() {
		return this.baby;
	}

	public void setBaby(boolean baby) {
		this.baby = baby;
		this.babyAge = 0;
		if (baby) {
			// Babies carry nothing: no weapon, no armor.
			this.player.getInventory().clearContent();
		}
		this.applyAttributes();
		this.player.setHealth(this.player.getMaxHealth());
	}

	public int getBabyAge() {
		return this.babyAge;
	}

	/** Feeding a baby its parent's food: 10% of the growing time off. */
	public void ageUp(int amount) {
		this.babyAge = Math.min(GROW_UP_TICKS, this.babyAge + amount);
		this.applyBabyModifiers();
	}

	protected void growUp() {
		this.baby = false;
		this.applyAttributes();
		this.rollGear();
		this.restock();
		this.player.setHealth(this.player.getMaxHealth());
		this.particles(ParticleTypes.HAPPY_VILLAGER, 10);
	}

	public void setParents(UUID a, UUID b) {
		this.parentA = a;
		this.parentB = b;
	}

	public boolean canFallInLove() {
		return this.isTame() && !this.baby && this.breedCooldown <= 0 && this.love <= 0;
	}

	public void setInLove() {
		this.love = LOVE_TICKS;
		this.particles(ParticleTypes.HEART, 7);
	}

	public boolean isInLove() {
		return this.love > 0;
	}

	public void startBreedCooldown() {
		this.breedCooldown = BREED_COOLDOWN_TICKS;
		this.love = 0;
	}

	/** Babies stay near a parent (or the owner). True while walking to them. */
	protected boolean followParent() {
		if (!this.baby) {
			return false;
		}
		Entity parent = null;
		double best = Double.MAX_VALUE;
		for (UUID id : new UUID[]{this.parentA, this.parentB}) {
			if (id == null) {
				continue;
			}
			LoafCharacter other = CharacterManager.get(id);
			if (other != null && other.player.level() == this.player.level()) {
				double distance = other.player.distanceTo(this.player);
				if (distance < best) {
					best = distance;
					parent = other.player;
				}
			}
		}
		if (parent == null || best < 4.0 || best > 24.0) {
			return false;
		}
		this.nav.follow(parent, 2.5, Navigator.Speed.WALK);
		return true;
	}

	/**
	 * Love mode: walk to a partner in love (ItzBreadLoaf + ImLilyLol or ItzBreadLoaf + Madison, same owner,
	 * within 8 blocks). ItzBreadLoaf's side makes the baby. True while busy with that.
	 */
	protected boolean tickLove() {
		if (!this.isInLove() || this.baby || this.owner == null) {
			return false;
		}
		LoafBrain partner = null;
		double best = 8.0;
		for (LoafCharacter other : CharacterManager.active()) {
			LoafBrain brain = other.brain;
			if (brain == null || brain == this || !brain.isInLove() || brain.baby || !this.owner.equals(brain.owner)
					|| !isCouple(this.kind, brain.kind) || other.player.level() != this.player.level()) {
				continue;
			}
			double distance = other.player.distanceTo(this.player);
			if (distance < best) {
				best = distance;
				partner = brain;
			}
		}
		if (partner == null) {
			return false;
		}
		this.controls.lookAt(partner.player.getEyePosition());
		if (best > 2.0) {
			this.nav.follow(partner.player, 1.5, Navigator.Speed.WALK);
		} else if (this.kind == CharacterKind.BREADLOAF && this.love < LOVE_TICKS - 60) {
			this.breedWith(partner);
		}
		return true;
	}

	public static boolean isCouple(CharacterKind a, CharacterKind b) {
		return a == CharacterKind.BREADLOAF && (b == CharacterKind.LILY || b == CharacterKind.MADISON)
				|| b == CharacterKind.BREADLOAF && (a == CharacterKind.LILY || a == CharacterKind.MADISON);
	}

	private void breedWith(LoafBrain partner) {
		ServerLevel level = this.player.level();
		UUID ownerId = this.owner;
		boolean withMadison = partner.kind == CharacterKind.MADISON;
		CharacterKind babyKind = this.random.nextBoolean() ? CharacterKind.BREADLOAF : partner.kind;
		Vec3 middle = this.player.position().add(partner.player.position()).scale(0.5);
		this.startBreedCooldown();
		partner.startBreedCooldown();
		UUID mom = partner.player.getUUID();
		UUID dad = this.player.getUUID();
		LoafCharacter child = CharacterManager.create(level, babyKind, middle, this.player.getYRot(), brain -> {
			brain.setBaby(true);
			brain.adoptByOwner(ownerId);
			brain.setParents(dad, mom);
			brain.onAdopted(ownerId);
		});
		if (child == null) {
			return;
		}
		if (withMadison) {
			// Orange and yellow sparkles for Madison's babies.
			child.brain.particles(new DustParticleOptions(0xFFA500, 1.4F), 10);
			child.brain.particles(new DustParticleOptions(0xFFE040, 1.2F), 10);
		} else {
			child.brain.particles(ParticleTypes.HEART, 7);
		}
		net.minecraft.world.entity.ExperienceOrb.award(level, middle, 1 + this.random.nextInt(7));
		ServerPlayer ownerPlayer = this.getOwnerPlayer();
		if (ownerPlayer != null) {
			if (withMadison) {
				ownerPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("A new little loaf has arrived!")
						.withStyle(net.minecraft.ChatFormatting.GOLD));
				ModAdvancements.award(ownerPlayer, ModAdvancements.LITTLE_LOAF);
			} else {
				ownerPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("A little one joined the family!")
						.withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
				ModAdvancements.award(ownerPlayer, ModAdvancements.FAMILY_REUNION);
			}
		}
	}

	/** A baby trusts the player who bred them from the start (Lily and Madison use this). */
	public void onAdopted(UUID ownerId) {
	}

	// ================================================================ interaction

	public InteractionResult onInteract(ServerPlayer clicker, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		ItemStack stack = clicker.getItemInHand(hand);
		InteractionResult result = this.interact(clicker, stack);
		return result != null ? result : InteractionResult.PASS;
	}

	/** Each character's own right-click handling (taming, trust gifts, sit/stand...). Null if not handled. */
	protected @Nullable InteractionResult interact(ServerPlayer clicker, ItemStack stack) {
		return null;
	}

	/** Should this item be picked up off the ground? (Players pick up anything they touch.) */
	public boolean wantsToPickUp(ItemStack stack) {
		return false;
	}

	// ================================================================ effects and helpers

	public EntityPlayerMPFake player() {
		return this.player;
	}

	public CharacterKind kind() {
		return this.kind;
	}

	public CharacterVariant variant() {
		return this.variant;
	}

	public Navigator nav() {
		return this.nav;
	}

	public Controls controls() {
		return this.controls;
	}

	public Builder builder() {
		return this.builder;
	}

	public RandomSource random() {
		return this.random;
	}

	public int ticks() {
		return this.ticks;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
		if (paused) {
			this.nav.stop();
			this.combat.stop();
			this.controls.releaseAll();
		}
	}

	public boolean isPaused() {
		return this.paused;
	}

	public void setWild(boolean wild) {
		this.wild = wild;
	}

	public void particles(ParticleOptions particle, int count) {
		this.player.level().sendParticles(particle, this.player.getX(), this.player.getY(0.6), this.player.getZ(), count,
				0.4, 0.5, 0.4, 0.02);
	}

	public void playSound(SoundEvent sound, float volume, float pitch) {
		this.player.level().playSound(null, this.player.getX(), this.player.getY(), this.player.getZ(), sound,
				this.soundSource(), volume, pitch);
	}

	protected SoundSource soundSource() {
		return SoundSource.PLAYERS;
	}

	public void say(ServerPlayer to, String text) {
		ModMessages.say(to, this.player, text);
	}

	/** Tosses an item to a player; only that player can pick it up. */
	public void giveTo(ServerPlayer receiver, ItemStack stack) {
		ItemEntity item = new ItemEntity(this.player.level(), this.player.getX(), this.player.getEyeY() - 0.3, this.player.getZ(), stack);
		Vec3 toss = receiver.position().subtract(this.player.position()).normalize().scale(0.25);
		item.setDeltaMovement(toss.x, 0.2, toss.z);
		item.setPickUpDelay(10);
		item.setTarget(receiver.getUUID());
		this.player.level().addFreshEntity(item);
	}

	// ================================================================ hands-on helpers (used by the character types)

	/** A potion item stack (drinkable, splash or lingering) of one potion type. */
	public static ItemStack potion(Item item, net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion) {
		return net.minecraft.world.item.alchemy.PotionContents.createItemStack(item, potion);
	}

	public static boolean isPotion(ItemStack stack, Item item, net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion) {
		net.minecraft.world.item.alchemy.PotionContents contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
		return stack.is(item) && contents != null && contents.is(potion);
	}

	/** Throws a splash potion of this type at an entity (or at our own feet if it's us). True if one was thrown. */
	protected boolean throwSplash(LivingEntity at, net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion) {
		int slot = Inv.ready(this.player, stack -> isPotion(stack, Items.SPLASH_POTION, potion), 7);
		if (slot < 0) {
			return false;
		}
		this.controls.selectSlot(slot);
		if (at == this.player) {
			this.controls.lookInstantly(this.player.getYRot(), 90.0F);
		} else {
			this.combat.aimProjectile(at, 0.5, 0.05);
		}
		this.controls.useOnce();
		return true;
	}

	/** Right-clicks the given face of a block with an item from the inventory (planting, bone meal, bottles...). */
	protected boolean useItemOn(BlockPos pos, net.minecraft.core.Direction face, java.util.function.Predicate<ItemStack> item) {
		int slot = Inv.ready(this.player, item, 6);
		if (slot < 0) {
			return false;
		}
		this.controls.selectSlot(slot);
		this.controls.aimAt(Vec3.atCenterOf(pos).add(face.getStepX() * 0.499, face.getStepY() * 0.499, face.getStepZ() * 0.499));
		this.controls.useOnce();
		return true;
	}

	/** One left-click on a block (breaks crops and other instant blocks). */
	protected void hitBlock(BlockPos pos) {
		this.controls.aimAt(Vec3.atCenterOf(pos));
		this.controls.attackOnce();
	}

	/** The closest block within {@code radius} (and {@code height} up or down) matching the test, or null. */
	protected @Nullable BlockPos findBlock(BlockPos center, int radius, int height,
			java.util.function.Predicate<net.minecraft.world.level.block.state.BlockState> test) {
		ServerLevel level = this.player.level();
		return BlockPos.findClosestMatch(center, radius, height, pos -> level.isLoaded(pos) && test.test(level.getBlockState(pos)))
				.map(BlockPos::immutable).orElse(null);
	}

	/** Within arm's reach (eyes to block center). */
	protected boolean canReach(BlockPos pos) {
		return this.player.getEyePosition().distanceTo(Vec3.atCenterOf(pos)) < 4.2;
	}

	// ================================================================ saving

	public void save(CompoundTag tag) {
		if (this.owner != null) {
			tag.store("Owner", UUIDUtil.CODEC, this.owner);
		}
		tag.putBoolean("Sitting", this.sitting);
		if (this.home != null) {
			tag.store("Home", BlockPos.CODEC, this.home);
			tag.putInt("HomeRadius", this.homeRadius);
		}
		if (this.bed != null) {
			tag.store("Bed", BlockPos.CODEC, this.bed);
		}
		tag.putBoolean("Wild", this.wild);
		tag.putBoolean("Paused", this.paused);
		tag.putBoolean("Baby", this.baby);
		tag.putInt("BabyAge", this.babyAge);
		tag.putInt("BreedCooldown", this.breedCooldown);
		if (this.parentA != null && this.parentB != null) {
			tag.store("ParentA", UUIDUtil.CODEC, this.parentA);
			tag.store("ParentB", UUIDUtil.CODEC, this.parentB);
		}
	}

	public void load(CompoundTag tag) {
		this.owner = tag.read("Owner", UUIDUtil.CODEC).orElse(null);
		this.sitting = tag.getBooleanOr("Sitting", false);
		this.home = tag.read("Home", BlockPos.CODEC).orElse(null);
		this.homeRadius = tag.getIntOr("HomeRadius", 10);
		this.bed = tag.read("Bed", BlockPos.CODEC).orElse(null);
		this.wild = tag.getBooleanOr("Wild", false);
		this.paused = tag.getBooleanOr("Paused", false);
		this.baby = tag.getBooleanOr("Baby", false);
		this.babyAge = tag.getIntOr("BabyAge", 0);
		this.breedCooldown = tag.getIntOr("BreedCooldown", 0);
		this.parentA = tag.read("ParentA", UUIDUtil.CODEC).orElse(null);
		this.parentB = tag.read("ParentB", UUIDUtil.CODEC).orElse(null);
	}

	protected static List<UUID> uuids(@Nullable UUID... ids) {
		List<UUID> list = new ArrayList<>();
		for (UUID id : ids) {
			if (id != null) {
				list.add(id);
			}
		}
		return list;
	}
}
