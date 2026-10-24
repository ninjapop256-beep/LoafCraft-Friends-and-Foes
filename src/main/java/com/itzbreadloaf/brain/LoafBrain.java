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
import net.minecraft.world.effect.MobEffects;
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
	/** Stuck-in-water rescue: where it was and for how long it hasn't got anywhere. */
	private @Nullable Vec3 waterStuckFrom;
	private int waterStuckTicks;
	/** How long the way home has been blocked (to retry, and teleport home if nobody is watching). */
	private int homeFailTicks;
	private int eatTicks;
	private boolean eatStarted;
	private int eatCooldown;
	private int wanderTimer;
	private int lastNearOwnerTick = -1000;
	private int lookTimer;
	private @Nullable Entity lookTarget;

	// ---- mind (AI overhaul part 3): mood, memory of players, daily routine, skill and play style
	protected final com.itzbreadloaf.brain.ai.MoodState mood = new com.itzbreadloaf.brain.ai.MoodState();
	protected final com.itzbreadloaf.brain.ai.PlayerMemory memory = new com.itzbreadloaf.brain.ai.PlayerMemory();
	final Activities activities;
	/** Each character thinks on its own tick of the second, so they don't all think at once. */
	private final int mindOffset;
	/** Last time something happened (a fight, a gift, a player nearby): long quiet stretches make them bored. */
	private int lastEventTick;
	private float baseRetreatBelow = -1.0F;
	private @Nullable com.itzbreadloaf.brain.ai.Mood speedMood;
	private int nextMoodLineTick;
	private static final Identifier MOOD_SPEED_ID = Identifier.fromNamespaceAndPath("itzbreadloaf", "mood_speed");
	/** Idle actions and the little human touches (part 6). */
	final IdleActions idleActions;
	private int nextLineTick;
	private @Nullable com.itzbreadloaf.brain.ai.Situation queuedLine;
	private @Nullable String queuedArg;
	private int queuedLineTick;
	private @Nullable LivingEntity lastVictim;
	private int lastDealtTick = -1000;
	private boolean saidLowHealth;
	private boolean wasFighting;
	/** How much thinking this character gets (part 7): full near players, simple farther out, paused far away. */
	public enum Detail { FULL, SIMPLE, PAUSED }

	private Detail detail = Detail.FULL;
	private boolean released;
	/** Average AI time per server tick (nanoseconds, smoothed), for /loafcraft debug ai. */
	private double aiNanos;
	/** A fight forced with /loafcraft duel, and until when (brain ticks). */
	private @Nullable LivingEntity duelOpponent;
	private int duelUntil;
	/** A mace technique forced with /loafcraft technique, and who it's aimed at. */
	private @Nullable LivingEntity forcedTechniqueTarget;

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
		this.activities = new Activities(this);
		this.idleActions = new IdleActions(this);
		this.mindOffset = Math.floorMod(this.player.getUUID().hashCode(), 20);
		// Every "every N ticks" check lands on a different tick for each character, so they don't all think at once.
		this.ticks = this.mindOffset;
		if (this.kind != CharacterKind.MASTER) {
			this.combat.healHook = this::combatHeal;
			this.combat.builder = this.builder;
		} else {
			this.combat.useReaction = false;
		}
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
		this.applySkill();
	}

	/**
	 * The skill profile's numbers that the current combat code uses (crits, deliberate misses). Harley's crit chance
	 * and all of Master BreadLoaf's numbers come from their own presets instead.
	 */
	protected void applySkill() {
		com.itzbreadloaf.brain.ai.SkillProfile skill = this.skill();
		this.combat.skill = skill;
		this.combat.style = this.playStyle();
		if (this.kind != CharacterKind.HARLEY && this.kind != CharacterKind.MASTER) {
			this.combat.critChance = skill.critChance;
		}
		if (this.kind != CharacterKind.MASTER) {
			// Misses now come from the mouse-like aim (and the skill's mistake chance), not a forced mouse jerk.
			this.combat.whiffChance = 0.0F;
		}
	}

	/**
	 * Healing while backing off from a fight: a golden apple, a healing or regeneration potion, or food if hungry.
	 * True if they started eating or drinking.
	 */
	protected boolean combatHeal() {
		return this.eatNow(stack -> stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE))
				|| this.eatNow(stack -> drinkableWith(stack, MobEffects.INSTANT_HEALTH, MobEffects.REGENERATION))
				|| this.isHungry() && this.eatNow(Inv::isFood);
	}

	/** A drinkable potion with any of these effects. */
	@SafeVarargs
	protected static boolean drinkableWith(ItemStack stack, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>... effects) {
		if (!stack.is(Items.POTION)) {
			return false;
		}
		net.minecraft.world.item.alchemy.PotionContents contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
		if (contents == null) {
			return false;
		}
		for (net.minecraft.world.effect.MobEffectInstance effect : contents.getAllEffects()) {
			for (var wanted : effects) {
				if (effect.getEffect().equals(wanted)) {
					return true;
				}
			}
		}
		return false;
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
		this.activities.stopAll();
		this.idleActions.stop();
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
			boolean won = !this.target.isAlive() && this.target == this.lastVictim && this.ticks - this.lastDealtTick < 100;
			this.setTarget(null);
			if (won) {
				this.onFightWon();
			}
		}
		this.tickQueuedLine();
		this.tickDuel();

		if (this.forcedTechniqueTarget != null) {
			// Testing a mace technique (/loafcraft technique): it runs on its own until it's done.
			MaceTechniques techniques = this.techniques();
			if (techniques == null || !this.forcedTechniqueTarget.isAlive() || !techniques.tick(this.forcedTechniqueTarget)) {
				this.forcedTechniqueTarget = null;
			}
			this.nav.stop();
			this.controls.apply();
			return;
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
			this.tickWaterRescue();
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
		if ((this.ticks + this.mindOffset) % 20 == 0) {
			this.tickMind();
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
		this.idleActions.stop();
		this.activities.stopAll();
		this.saidLowHealth = false;
		// ItzBreadLoaf and FelixSyn trash-talk before a fight; others sometimes say something. Whoever answers waits a moment.
		CharacterKind other = LoafIds.kind(target.getUUID());
		boolean rival = this.kind == CharacterKind.BREADLOAF && other == CharacterKind.FELIXSYN
				|| this.kind == CharacterKind.FELIXSYN && other == CharacterKind.BREADLOAF;
		LoafBrain answering = CharacterManager.brain(target);
		int delay = answering != null && answering.target == this.player ? 30 : 0;
		if (rival || this.random.nextFloat() < 0.4F) {
			this.queueLine(rival ? com.itzbreadloaf.brain.ai.Situation.RIVAL : com.itzbreadloaf.brain.ai.Situation.FIGHT_START, null, delay);
		}
		// A fight is starting and there's room: drink a Strength, Swiftness or Fire Resistance potion first, if they carry one.
		if (this.kind != CharacterKind.HARLEY && this.kind != CharacterKind.MASTER && !this.baby && this.player.distanceTo(target) > 6.0) {
			this.eatNow(stack -> drinkableWith(stack, MobEffects.STRENGTH, MobEffects.SPEED, MobEffects.FIRE_RESISTANCE));
		}
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
		LivingEntity attacker = null;
		if (source.getEntity() instanceof LivingEntity living && living != this.player) {
			this.lastAttacker = living;
			attacker = living;
		}
		this.sitting = false;
		if (this.player.isSleeping()) {
			this.player.stopSleeping();
		}
		this.lastEventTick = this.ticks;
		if (this.target != null && !blocked) {
			this.combat.onHurt(attacker);
		}
		long gameTime = this.player.level().getGameTime();
		if (attacker instanceof Player p && LoafIds.isRealPlayer(p) && !this.isOwnedBy(p) && damage > 0.0F) {
			this.memory.hurt(p.getUUID(), damage, gameTime);
		}
		if (attacker != null && damage > 0.0F) {
			boolean low = this.player.getHealth() < this.player.getMaxHealth() * 0.5F;
			com.itzbreadloaf.brain.ai.Mood felt = this.isFighter() ? (low && this.skill().panicHealth > 0.0F
					&& this.player.getHealth() < this.player.getMaxHealth() * this.skill().panicHealth
					? com.itzbreadloaf.brain.ai.Mood.SCARED : com.itzbreadloaf.brain.ai.Mood.ANGRY)
					: low || this.kind == CharacterKind.LILY ? com.itzbreadloaf.brain.ai.Mood.SCARED : com.itzbreadloaf.brain.ai.Mood.ANGRY;
			this.pushMood(felt, 0.6F);
			// Friends nearby notice.
			for (LoafCharacter other : CharacterManager.active()) {
				if (other.brain != null && other.brain != this && other.player.level() == this.player.level()
						&& other.player.distanceTo(this.player) < 16.0 && other.brain.isFriendCharacter(this)) {
					other.brain.onFriendHurt(this, attacker);
				}
			}
		}
	}

	/** A friend nearby was just hurt. Defenders get angry, Lily gets scared (part 6 adds what they do about it). */
	protected void onFriendHurt(LoafBrain friend, LivingEntity attacker) {
		if (attacker == this.player || this.isFriendlyWith(attacker)) {
			return;
		}
		LoafBrain attackerBrain = CharacterManager.brain(attacker);
		if (attackerBrain != null && this.isFriendCharacter(attackerBrain)) {
			return;
		}
		this.lastEventTick = this.ticks;
		boolean untouchable = attacker instanceof Player p && (p.isCreative() || p.isSpectator());
		boolean defender = this.isFighter() || this.kind == CharacterKind.MADISON
				|| this.kind == CharacterKind.LILY && this.mood.mood() == com.itzbreadloaf.brain.ai.Mood.ANGRY;
		if (defender && !untouchable && !this.baby && this.kind != CharacterKind.MASTER && this.target == null
				&& attacker.distanceTo(this.player) < 16.0) {
			this.pushMood(com.itzbreadloaf.brain.ai.Mood.ANGRY, 0.6F);
			this.speak(com.itzbreadloaf.brain.ai.Situation.DEFEND_FRIEND, null);
			this.defendAgainst(attacker);
			return;
		}
		this.pushMood(this.kind == CharacterKind.LILY ? com.itzbreadloaf.brain.ai.Mood.SCARED : com.itzbreadloaf.brain.ai.Mood.ANGRY, 0.5F);
		this.speak(com.itzbreadloaf.brain.ai.Situation.SCARED_FOR_FRIEND, null);
	}

	/** Steps in for a friend: fights whoever hurt them. */
	protected void defendAgainst(LivingEntity attacker) {
		this.setTarget(attacker);
	}

	public void onDealtDamage(LivingEntity victim, float damage) {
		this.lastVictim = victim;
		this.lastDealtTick = this.ticks;
	}

	/** Their target just died to them: celebrate (jumps, a swing) and say something. */
	protected void onFightWon() {
		this.speak(com.itzbreadloaf.brain.ai.Situation.FIGHT_WON, null);
		this.idleActions.celebrate();
	}

	/** Something living got hurt near them (a fight, a hit): turn and look, if they're not busy. */
	public void noticeCommotion(LivingEntity hurt) {
		if (this.target == null && !this.player.isSleeping() && !this.baby && hurt != this.player && this.kind != CharacterKind.MASTER) {
			this.idleActions.glance(hurt.getEyePosition());
		}
	}

	/** Players they like: their owner, anyone they've grown to trust (fed them, helped them). */
	public boolean likesPlayer(Player player) {
		return this.isOwnedBy(player) || this.memory.isLiked(player.getUUID());
	}

	/** Idle actions (greetings, jumps, sitting...). True while busy with one. */
	protected boolean tickIdleActions() {
		return this.idleActions.tick(false);
	}

	/** Near their owner with nothing to do: owner-friendly idle actions, otherwise looking around. */
	protected void idleWithOwner() {
		if (!this.idleActions.tick(true)) {
			this.lookAround(8.0);
		}
	}

	// ---------------------------------------------------------------- lines

	/** Says a line for the moment to players nearby (each character has its own lines; at most one every 8 seconds). */
	public void speak(com.itzbreadloaf.brain.ai.Situation situation, @Nullable String arg) {
		if (!com.itzbreadloaf.config.ModConfig.get().personalityLines || this.baby || this.player.isSleeping()) {
			return;
		}
		boolean urgent = situation == com.itzbreadloaf.brain.ai.Situation.RIVAL || situation == com.itzbreadloaf.brain.ai.Situation.FIGHT_WON
				|| situation == com.itzbreadloaf.brain.ai.Situation.DEFEND_FRIEND;
		if (this.ticks < this.nextLineTick && !urgent) {
			return;
		}
		String[] lines = this.lines(situation);
		if (lines.length == 0) {
			return;
		}
		String line = lines[this.random.nextInt(lines.length)];
		if (line.equals("...")) {
			// Nothing to say about this one.
			return;
		}
		if (line.contains("%s")) {
			line = String.format(line, arg == null ? "you" : arg);
		}
		boolean said = false;
		for (ServerPlayer viewer : this.player.level().players()) {
			if (LoafIds.isRealPlayer(viewer) && viewer.distanceTo(this.player) < 12.0) {
				this.say(viewer, line);
				said = true;
			}
		}
		if (said) {
			this.nextLineTick = this.ticks + 160;
		}
	}

	/** A line said after a short delay (so two characters don't talk over each other). */
	protected void queueLine(com.itzbreadloaf.brain.ai.Situation situation, @Nullable String arg, int delay) {
		if (delay <= 0) {
			this.speak(situation, arg);
			return;
		}
		this.queuedLine = situation;
		this.queuedArg = arg;
		this.queuedLineTick = this.ticks + delay;
	}

	private void tickQueuedLine() {
		if (this.queuedLine != null && this.ticks >= this.queuedLineTick) {
			com.itzbreadloaf.brain.ai.Situation line = this.queuedLine;
			this.queuedLine = null;
			this.speak(line, this.queuedArg);
		}
	}

	/** This character's lines for a moment. Each character overrides this with its own. */
	protected String[] lines(com.itzbreadloaf.brain.ai.Situation situation) {
		return switch (situation) {
			case GREET_PLAYER -> new String[]{"hey!", "oh, hi!"};
			case GREET_FRIEND -> new String[]{"hey %s!"};
			case FIGHT_START -> new String[]{"bring it!"};
			case RIVAL -> new String[]{"you again?!"};
			case FIGHT_WON -> new String[]{"good fight!"};
			case LOW_HEALTH -> new String[]{"ouch!"};
			case DEFEND_FRIEND -> new String[]{"leave them alone!"};
			case SCARED_FOR_FRIEND -> new String[]{"oh no!"};
			case REST -> new String[]{"phew..."};
			case MORNING -> new String[]{"good morning!"};
			case NIGHT -> new String[]{"nice night"};
			case RAIN -> new String[]{"ugh, rain"};
			case ITEM -> new String[]{"nice %s!"};
			case WATCH -> new String[]{"looking good"};
		};
	}

	public boolean allowDamage(DamageSource source, float amount) {
		return true;
	}

	/** Called when they die. Return true to drop the whole inventory like a player. */
	public boolean onDeath(DamageSource source) {
		if (this.baby) {
			return false;
		}
		if (this.isTame()) {
			// A tamed character's inventory is mostly what its owner gave it: all of it drops, except maces (super
			// rare: each one only with maceDropChance).
			com.itzbreadloaf.loot.LootRarity.stripMaces(this.player, this.random);
			return true;
		}
		// Wild: its own drop table with tier rolls (loot rarity system), plus armor and weapon like a normal mob.
		com.itzbreadloaf.loot.LootRarity.dropCharacterLoot(this, this.player, source);
		com.itzbreadloaf.loot.LootRarity.dropGear(this.player, this.random, true);
		this.player.getInventory().clearContent();
		return false;
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
			this.memory.helped(newOwner.getUUID(), 30, this.player.level().getGameTime());
			this.pushMood(com.itzbreadloaf.brain.ai.Mood.HAPPY, 0.7F);
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

	// ================================================================ water rescue

	/**
	 * Fell into a well or a pit full of water: swim up the whole time, and after 5 seconds without getting anywhere,
	 * step out onto the nearest dry block.
	 */
	private void tickWaterRescue() {
		if (!this.player.isInWater()) {
			this.waterStuckFrom = null;
			this.waterStuckTicks = 0;
			return;
		}
		if (this.waterStuckFrom == null || this.player.position().distanceToSqr(this.waterStuckFrom) > 4.0) {
			this.waterStuckFrom = this.player.position();
			this.waterStuckTicks = 0;
			return;
		}
		if (!this.nav.isMoving()) {
			this.controls.holdJump(true);
		}
		if (++this.waterStuckTicks < 100) {
			return;
		}
		this.waterStuckTicks = 0;
		this.waterStuckFrom = null;
		BlockPos dry = this.findDrySpot(this.player.blockPosition(), 6);
		if (dry != null) {
			this.player.teleportTo(dry.getX() + 0.5, dry.getY(), dry.getZ() + 0.5);
			this.nav.stop();
		}
	}

	/** The closest spot within the radius with solid ground, room for a player, and no water. */
	private @Nullable BlockPos findDrySpot(BlockPos center, int radius) {
		ServerLevel level = this.player.level();
		BlockPos best = null;
		double bestDistance = Double.MAX_VALUE;
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				for (int dy = -2; dy <= 4; dy++) {
					BlockPos feet = center.offset(dx, dy, dz);
					BlockPos below = feet.below();
					if (!level.getFluidState(feet).isEmpty() || !level.getFluidState(below).isEmpty()
							|| !level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
							|| !level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
							|| !level.getBlockState(below).isFaceSturdy(level, below, net.minecraft.core.Direction.UP)) {
						continue;
					}
					double distance = feet.distSqr(center);
					if (distance < bestDistance) {
						bestDistance = distance;
						best = feet;
					}
				}
			}
		}
		return best;
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
		int before = stack.getCount();
		net.minecraft.world.item.Item item = stack.getItem();
		InteractionResult result = this.interact(clicker, stack);
		boolean taken = !stack.is(item) || stack.getCount() < before;
		if (taken && LoafIds.isRealPlayer(clicker)) {
			this.memory.fed(clicker.getUUID(), this.player.level().getGameTime());
			this.lastEventTick = this.ticks;
			this.pushMood(com.itzbreadloaf.brain.ai.Mood.HAPPY, 0.5F);
		}
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

	// ================================================================ detail levels, timing, debug (part 7)

	public Detail detail() {
		return this.detail;
	}

	public double aiMicros() {
		return this.aiNanos / 1000.0;
	}

	/**
	 * Once a second: how much thinking this character needs. Full within aiFullDetailRange of a real player (or while
	 * fighting, tamed or forced into something); simple out to aiSimpleDetailRange (thinks every 4th tick, keeps
	 * walking in between); paused beyond (stands still, doesn't think) until a player comes closer.
	 */
	public void updateDetail() {
		com.itzbreadloaf.config.ModConfig config = com.itzbreadloaf.config.ModConfig.get();
		if (this.target != null || this.isTame() || this.keepActive() || this.forcedTechniqueTarget != null || this.duelOpponent != null
				|| this.kind == CharacterKind.MASTER) {
			this.detail = Detail.FULL;
			return;
		}
		double nearest = Double.MAX_VALUE;
		for (ServerPlayer viewer : this.player.level().players()) {
			if (LoafIds.isRealPlayer(viewer) && !viewer.isSpectator()) {
				nearest = Math.min(nearest, viewer.distanceToSqr(this.player));
			}
		}
		double full = config.aiFullDetailRange;
		double simple = Math.max(full, config.aiSimpleDetailRange);
		this.detail = nearest < full * full ? Detail.FULL : nearest < simple * simple ? Detail.SIMPLE : Detail.PAUSED;
	}

	/**
	 * Called every server tick by the character manager: thinks (or not) for this tick's detail level and measures how
	 * long it took. Hurt or attacked characters always think.
	 */
	public void serverTick(int serverTick) {
		if ((serverTick + this.mindOffset) % 20 == 0) {
			this.updateDetail();
		}
		boolean think = switch (this.detail) {
			case FULL -> true;
			case SIMPLE -> (serverTick + this.mindOffset) % 4 == 0 || this.ticks - this.lastHurtTick < 40;
			case PAUSED -> this.ticks - this.lastHurtTick < 40;
		};
		long start = System.nanoTime();
		if (think) {
			this.released = false;
			this.tick();
		} else if (this.detail == Detail.PAUSED && !this.released) {
			this.released = true;
			this.nav.stop();
			this.controls.releaseAll();
		}
		long spent = System.nanoTime() - start;
		this.aiNanos = this.aiNanos * 0.95 + spent * 0.05;
	}

	/** What they're doing right now, in a few words (for /loafcraft debug ai). */
	public String describeGoal() {
		if (this.paused) {
			return "paused (command)";
		}
		if (this.forcedTechniqueTarget != null && this.techniques() != null && this.techniques().current() != null) {
			return "test move " + this.techniques().current().id();
		}
		if (this.duelOpponent != null) {
			return "duel " + this.duelOpponent.getName().getString();
		}
		MaceTechniques techniques = this.techniques();
		if (techniques != null && techniques.isActive() && techniques.current() != null) {
			return "mace " + techniques.current().id();
		}
		if (this.target != null) {
			return (this.combat.isRetreating() ? "backing off from " : "fighting ") + this.target.getName().getString();
		}
		if (this.player.isSleeping()) {
			return "sleeping";
		}
		if (this.isEating()) {
			return "eating";
		}
		if (this.idleActions.current() != IdleActions.Action.NONE) {
			return "idle " + this.idleActions.current().name().toLowerCase(java.util.Locale.ROOT);
		}
		if (this.isTame()) {
			return this.sitting ? "sitting (tamed)" : "with owner";
		}
		return "routine " + this.routineActivity().id();
	}

	/**
	 * Forces a fight with another character (/loafcraft duel), for up to 2 minutes or until one of them goes down,
	 * overriding friendships and the usual rules about who fights whom.
	 */
	public void startDuel(LivingEntity opponent) {
		this.duelOpponent = opponent;
		this.duelUntil = this.ticks + 2400;
		if (this.player.isSleeping()) {
			this.player.stopSleeping();
		}
		this.sitting = false;
		this.target = opponent;
		this.onNewTarget(opponent);
	}

	/** Keeps a forced duel going: the opponent stays the target, and counts as having just attacked them. */
	private void tickDuel() {
		if (this.duelOpponent == null) {
			return;
		}
		if (!this.duelOpponent.isAlive() || this.duelOpponent.isRemoved() || this.ticks > this.duelUntil
				|| this.duelOpponent.level() != this.player.level() || this.duelOpponent.distanceTo(this.player) > 48.0) {
			this.duelOpponent = null;
			this.setTarget(null);
			return;
		}
		this.target = this.duelOpponent;
		this.lastAttacker = this.duelOpponent;
		this.lastHurtTick = this.ticks;
		if (this instanceof NeutralBrain neutral) {
			neutral.anger = Math.max(neutral.anger, 200);
		}
	}

	// ================================================================ mace techniques

	/** The mace moves this character knows (FelixSyn, Harley, Master BreadLoaf), or null. */
	public @Nullable MaceTechniques techniques() {
		return null;
	}

	/** Forces a mace technique at a target, for testing. False if this character doesn't do mace moves. */
	public boolean forceTechnique(MaceTechniques.Technique technique, LivingEntity at) {
		MaceTechniques techniques = this.techniques();
		if (techniques == null) {
			return false;
		}
		if (this.player.isSleeping()) {
			this.player.stopSleeping();
		}
		this.combat.stop();
		techniques.limits.failChance = this.kind == CharacterKind.MASTER ? techniques.limits.failChance : this.skill().techniqueFailChance;
		techniques.start(technique, at, 0);
		this.forcedTechniqueTarget = at;
		return true;
	}

	// ================================================================ mind: mood, memory, routine, skill, play style

	public com.itzbreadloaf.brain.ai.MoodState mood() {
		return this.mood;
	}

	public com.itzbreadloaf.brain.ai.PlayerMemory memory() {
		return this.memory;
	}

	/** The skill numbers this character fights with (config "skillProfiles", by its preset). */
	public com.itzbreadloaf.brain.ai.SkillProfile skill() {
		return com.itzbreadloaf.config.ModConfig.get().skillFor(this.kind);
	}

	/** How this character likes to fight (config "playStyleOverrides", or picked from who they are). */
	public com.itzbreadloaf.brain.ai.PlayStyle playStyle() {
		var overrides = com.itzbreadloaf.config.ModConfig.get().playStyleOverrides;
		String id = overrides == null ? null : overrides.get(this.kind.id);
		com.itzbreadloaf.brain.ai.PlayStyle style = id == null ? null : com.itzbreadloaf.brain.ai.PlayStyle.byId(id);
		return style != null ? style : com.itzbreadloaf.brain.ai.PlayStyle.forCharacter(this.kind, this.variant);
	}

	/** Characters who start fights (the others only defend themselves and their friends). */
	public boolean isFighter() {
		return this.kind == CharacterKind.BREADLOAF || this.kind == CharacterKind.FELIXSYN || this.kind == CharacterKind.HARLEY
				|| this.kind == CharacterKind.MASTER;
	}

	/**
	 * Friends among the characters: the same owner, cousins Lily and Madison, FelixSyn and Harley, ItzBreadLoafs with
	 * each other, and ItzBreadLoaf with Lily or Madison (the couples). FelixSyn and ItzBreadLoaf are rivals, not friends.
	 */
	public boolean isFriendCharacter(LoafBrain other) {
		if (this.owner != null && this.owner.equals(other.owner)) {
			return true;
		}
		if (this.kind == CharacterKind.MASTER || other.kind == CharacterKind.MASTER) {
			return false;
		}
		CharacterKind a = this.kind;
		CharacterKind b = other.kind;
		return a == b && a != CharacterKind.FELIXSYN
				|| pair(a, b, CharacterKind.LILY, CharacterKind.MADISON)
				|| pair(a, b, CharacterKind.FELIXSYN, CharacterKind.HARLEY)
				|| isCouple(a, b);
	}

	private static boolean pair(CharacterKind a, CharacterKind b, CharacterKind x, CharacterKind y) {
		return a == x && b == y || a == y && b == x;
	}

	/** Feel this way (if the push is strong enough). Shows the change with a line now and then. */
	public void pushMood(com.itzbreadloaf.brain.ai.Mood mood, float amount) {
		if (!com.itzbreadloaf.config.ModConfig.get().moods) {
			return;
		}
		com.itzbreadloaf.brain.ai.Mood old = this.mood.mood();
		this.mood.push(mood, amount, this.player.level().getGameTime());
		if (this.mood.mood() != old) {
			this.onMoodChanged(old, this.mood.mood());
		}
	}

	/** Forces a mood for a while (/loafcraft mood). */
	public void setMood(com.itzbreadloaf.brain.ai.Mood mood, int ticks) {
		com.itzbreadloaf.brain.ai.Mood old = this.mood.mood();
		this.mood.set(mood, this.player.level().getGameTime(), ticks);
		this.applyMoodEffects();
		if (old != mood) {
			this.onMoodChanged(old, mood);
		}
	}

	/** The everyday mood when nothing in particular is going on: tired at night, bored after 3 quiet minutes, else happy. */
	protected com.itzbreadloaf.brain.ai.Mood everydayMood() {
		long time = Math.floorMod(this.player.level().getDayTime(), 24000L);
		if (time >= 12500 && time < 23500) {
			return com.itzbreadloaf.brain.ai.Mood.TIRED;
		}
		if (this.target == null && this.ticks - this.lastEventTick > 3600) {
			return com.itzbreadloaf.brain.ai.Mood.BORED;
		}
		return com.itzbreadloaf.brain.ai.Mood.HAPPY;
	}

	/** Once a second: notice players, feel the fight, fade toward the everyday mood, apply the mood. */
	private void tickMind() {
		this.combat.skill = this.skill();
		this.combat.style = this.playStyle();
		// Fighters always fight with full skill; Lily and Madison only once they're angry.
		this.combat.focused = this.isFighter() || this.isAngry() || this.mood.mood() == com.itzbreadloaf.brain.ai.Mood.ANGRY;
		float healthShare = this.player.getHealth() / this.player.getMaxHealth();
		if (this.target != null) {
			this.wasFighting = true;
			if (healthShare < 0.3F && !this.saidLowHealth) {
				this.saidLowHealth = true;
				this.speak(com.itzbreadloaf.brain.ai.Situation.LOW_HEALTH, null);
			}
		} else if (this.wasFighting) {
			// The fight's over: after a close one, catch their breath for a moment.
			this.wasFighting = false;
			if (healthShare < 0.5F && this.kind != CharacterKind.MASTER) {
				this.idleActions.rest();
			}
		}
		if (!com.itzbreadloaf.config.ModConfig.get().moods) {
			this.removeMoodSpeed();
			return;
		}
		long gameTime = this.player.level().getGameTime();
		com.itzbreadloaf.brain.ai.Mood old = this.mood.mood();
		if ((this.ticks + this.mindOffset) % 40 == 0) {
			this.noticePlayersNearby(gameTime);
		}
		if (this.target != null) {
			this.lastEventTick = this.ticks;
			float health = this.player.getHealth() / this.player.getMaxHealth();
			if (health < this.skill().panicHealth) {
				this.mood.push(com.itzbreadloaf.brain.ai.Mood.SCARED, 0.7F, gameTime);
			} else if (this.isFighter() || this.isAngry()) {
				this.mood.push(com.itzbreadloaf.brain.ai.Mood.ANGRY, 0.3F, gameTime);
			}
		}
		this.mood.tickSecond(this.everydayMood(), gameTime);
		this.applyMoodEffects();
		if (this.mood.mood() != old) {
			this.onMoodChanged(old, this.mood.mood());
		}
	}

	/** Players nearby: new faces make them curious, feared ones scare (or anger) them, liked ones cheer them up. */
	private void noticePlayersNearby(long gameTime) {
		for (ServerPlayer viewer : this.player.level().players()) {
			if (!LoafIds.isRealPlayer(viewer) || viewer.isSpectator() || viewer.distanceTo(this.player) > 12.0
					|| !viewer.hasLineOfSight(this.player)) {
				continue;
			}
			this.lastEventTick = this.ticks;
			boolean first = this.memory.seen(viewer.getUUID(), gameTime);
			if (this.memory.isFeared(viewer.getUUID(), gameTime)) {
				this.mood.push(this.isFighter() || this.kind == CharacterKind.MADISON
						? com.itzbreadloaf.brain.ai.Mood.ANGRY : com.itzbreadloaf.brain.ai.Mood.SCARED, 0.5F, gameTime);
			} else if (this.memory.isLiked(viewer.getUUID()) || this.isOwnedBy(viewer)) {
				this.mood.push(com.itzbreadloaf.brain.ai.Mood.HAPPY, 0.3F, gameTime);
			} else if (first) {
				this.mood.push(com.itzbreadloaf.brain.ai.Mood.CURIOUS, 0.5F, gameTime);
			}
		}
	}

	/**
	 * What the mood does right now: a little faster or slower (not Master BreadLoaf, whose speed is part of his
	 * balance), and backing off from a fight sooner or later. Lily and Madison back off sooner still from a player
	 * who hurt them before.
	 */
	protected void applyMoodEffects() {
		com.itzbreadloaf.brain.ai.Mood now = this.mood.mood();
		if (this.kind != CharacterKind.MASTER && this.speedMood != now) {
			this.speedMood = now;
			AttributeInstance speed = this.player.getAttribute(Attributes.MOVEMENT_SPEED);
			if (speed != null) {
				if (now.speedBonus == 0.0) {
					speed.removeModifier(MOOD_SPEED_ID);
				} else {
					speed.addOrReplacePermanentModifier(new AttributeModifier(MOOD_SPEED_ID, now.speedBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
				}
			}
		}
		if (this.baseRetreatBelow < 0.0F) {
			this.baseRetreatBelow = this.combat.retreatBelow;
		}
		if (this.baseRetreatBelow > 0.0F) {
			float shift = now.retreatShift;
			if (!this.isFighter() && this.target instanceof Player p
					&& this.memory.isFeared(p.getUUID(), this.player.level().getGameTime())) {
				shift += 0.15F;
			}
			this.combat.retreatBelow = Math.max(0.05F, Math.min(0.8F, this.baseRetreatBelow + shift));
		}
	}

	private void removeMoodSpeed() {
		if (this.speedMood != null) {
			this.speedMood = null;
			AttributeInstance speed = this.player.getAttribute(Attributes.MOVEMENT_SPEED);
			if (speed != null) {
				speed.removeModifier(MOOD_SPEED_ID);
			}
		}
	}

	/** A short line when the mood changes (at most every 30 seconds), to a player watching. */
	protected void onMoodChanged(com.itzbreadloaf.brain.ai.Mood old, com.itzbreadloaf.brain.ai.Mood now) {
		if (this.ticks < this.nextMoodLineTick || this.baby || this.player.isSleeping()
				|| !com.itzbreadloaf.config.ModConfig.get().personalityLines) {
			return;
		}
		String[] lines = this.moodLines(now);
		if (lines.length == 0) {
			return;
		}
		String line = lines[this.random.nextInt(lines.length)];
		if (line.equals("...")) {
			return;
		}
		for (ServerPlayer viewer : this.player.level().players()) {
			if (LoafIds.isRealPlayer(viewer) && viewer.distanceTo(this.player) < 8.0 && viewer.hasLineOfSight(this.player)) {
				this.say(viewer, line);
				this.nextMoodLineTick = this.ticks + 600;
				return;
			}
		}
	}

	/** Lines for a new mood. Each character has its own (part 6 adds more). */
	protected String[] moodLines(com.itzbreadloaf.brain.ai.Mood mood) {
		return switch (mood) {
			case TIRED -> new String[]{"*yawns*", "getting sleepy..."};
			case BORED -> new String[]{"*sighs*", "nothing to do..."};
			case HAPPY -> new String[]{"*hums*", "nice day!"};
			case CURIOUS -> new String[]{"hm? who's that?", "oh, hello there"};
			case SCARED -> new String[]{"eek!", "uh oh..."};
			case ANGRY -> new String[]{"hey!", "that's it!"};
		};
	}

	/** What the routine says they should be doing right now (fixed per character, day and hour). */
	public com.itzbreadloaf.brain.ai.Routine.Activity routineActivity() {
		return com.itzbreadloaf.brain.ai.Routine.at(this.player.level().getDayTime(), this.mood.mood(),
				this.player.getUUID().getLeastSignificantBits());
	}

	/**
	 * The daily routine, for wild and resident characters with nothing better to do (never while fighting, tamed,
	 * as a baby, or for Master BreadLoaf). True while busy with it; otherwise the character's own idle behavior runs.
	 */
	protected boolean tickRoutine() {
		if (!com.itzbreadloaf.config.ModConfig.get().routines || this.target != null || this.isTame() || this.baby
				|| this.kind == CharacterKind.MASTER) {
			this.activities.stopAll();
			return false;
		}
		com.itzbreadloaf.brain.ai.Routine.Activity activity = this.routineActivity();
		if (activity != com.itzbreadloaf.brain.ai.Routine.Activity.WORK) {
			this.activities.stopWork();
		}
		if (activity != com.itzbreadloaf.brain.ai.Routine.Activity.FRIENDS) {
			this.activities.stopVisit();
		}
		return switch (activity) {
			case WORK -> this.doWork();
			case FRIENDS -> this.socialize();
			case HEAD_HOME -> this.activities.stayNearHome();
			default -> false;
		};
	}

	/** Each character's work (bake, train, read, garden, farm, practice...). True while busy. */
	protected boolean doWork() {
		return false;
	}

	/** Friends time: visit a friend character nearby. */
	protected boolean socialize() {
		return this.activities.visitFriend();
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
		this.mood.save(tag);
		this.memory.save(tag);
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
		this.mood.load(tag);
		this.memory.load(tag);
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
