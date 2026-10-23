package com.itzbreadloaf.brain;

import com.itzbreadloaf.ModAdvancements;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.LoafCharacter;
import com.itzbreadloaf.character.LoafIds;
import com.itzbreadloaf.config.ModConfig;
import com.itzbreadloaf.item.ModItems;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.Nullable;

/**
 * Harley: FelixSyn's duo partner and the best fighter in the mod outside Master BreadLoaf.
 *
 * Rebalanced: full diamond (Protection II), Harley's Blade is a diamond sword with Sharpness II, 26 health on Normal,
 * good timing and some strafing but no ender pearls and only one golden apple per fight.
 *
 * Wild: follows FelixSyn everywhere and fights whatever he fights. If he's killed she goes after his killer for
 * 60 seconds, then wanders off alone. Tame her with golden apples (1 in 5, only while she isn't fighting): then she
 * follows you (through portals too), fights whatever attacks you or whatever you attack, and sits/follows on an
 * empty-hand right-click.
 *
 * Fighting: perfect attack-cooldown timing, a jump crit on (nearly) every hit, strafing and circling, a shield up
 * between hits, ender pearls to close distance, Harley's Blade on the ground and a mace for wind-charge smashes,
 * Strength and Swiftness potions at the start of a fight, golden apples when low. Difficulty presets in the config.
 *
 * Best friend: she adores one real player, "FelixSyn" by default (harleyBestFriend in the config). She never attacks
 * them (even if they hit her), greets them, stays close with hearts, defends them from anything that hurts them,
 * tames on the first golden apple from them, and every few minutes gives them a gift. Nobody else gets gifts.
 */
public class HarleyBrain extends LoafBrain {
	public static final int VENGEANCE_TICKS = 1200;
	public static final int TAME_CHANCE = 5;
	private static final Identifier DAMAGE_PRESET_ID = Identifier.fromNamespaceAndPath("itzbreadloaf", "harley_damage_preset");

	private final MaceJump maceJump;
	private @Nullable UUID partner;
	private @Nullable UUID vengeance;
	private int vengeanceTicks;
	/** Only one golden apple per fight. */
	private boolean ateApple;
	private boolean buffed;
	private int fightTicks;
	private int nextGiftTick = 200;
	private int lastSawFriendTick = -100000;
	private int nextOuchTick;
	private static final double FRIEND_RANGE = 16.0;
	private static final double GIFT_RANGE = 10.0;

	public HarleyBrain(LoafCharacter character) {
		super(character);
		this.maceJump = new MaceJump(this.player, this.controls, this.combat);
		// The best duelist: almost never backs off.
		this.combat.retreatBelow = (float) com.itzbreadloaf.config.ModConfig.get().harleyRetreatHealth;
	}

	private static ModConfig.HarleyPreset preset() {
		return ModConfig.get().harleyPreset();
	}

	@Override
	protected double baseMaxHealth() {
		return preset().health;
	}

	@Override
	protected double baseSpeed() {
		return preset().speed;
	}

	@Override
	public void applyAttributes() {
		super.applyAttributes();
		this.combat.critChance = preset().critChance;
		AttributeInstance damage = this.player.getAttribute(Attributes.ATTACK_DAMAGE);
		if (damage != null) {
			damage.addOrReplacePermanentModifier(new AttributeModifier(DAMAGE_PRESET_ID, preset().damageMultiplier - 1.0,
					AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
	}

	@Override
	protected void rollGear() {
		Kits.harley(this.player, this.player.level(), this.random);
	}

	@Override
	protected void restock() {
		if (Inv.count(this.player, Inv::isFood) < 4) {
			Inv.give(this.player, new ItemStack(Items.COOKED_BEEF, 8));
		}
		if (Inv.count(this.player, stack -> stack.is(Items.WIND_CHARGE)) < 4) {
			Inv.give(this.player, new ItemStack(Items.WIND_CHARGE, 8));
		}
		if (Inv.count(this.player, stack -> stack.is(Items.GOLDEN_APPLE)) < 1) {
			Inv.give(this.player, new ItemStack(Items.GOLDEN_APPLE, 1));
		}
	}

	@Override
	public void setTarget(@Nullable LivingEntity target) {
		if (target != null && isBestFriend(target)) {
			// Never fights her best friend, whatever happens.
			return;
		}
		super.setTarget(target);
	}

	@Override
	public boolean isFriendlyWith(@Nullable Entity entity) {
		return isBestFriend(entity) || super.isFriendlyWith(entity);
	}

	@Override
	public boolean isFriendlyToMobs() {
		return this.isTame();
	}

	@Override
	protected SoundSource soundSource() {
		return SoundSource.NEUTRAL;
	}

	@Override
	public int experienceReward() {
		return 40;
	}

	@Override
	public boolean allowDamage(DamageSource source, float amount) {
		return !(this.maceJump.isActive() && source.is(net.minecraft.tags.DamageTypeTags.IS_FALL));
	}

	// ---------------------------------------------------------------- partner

	public @Nullable UUID partner() {
		return this.partner;
	}

	public void setPartner(@Nullable UUID partner) {
		this.partner = partner;
	}

	private @Nullable LoafCharacter partnerCharacter() {
		return this.partner == null ? null : CharacterManager.get(this.partner);
	}

	/** FelixSyn was killed: 60 seconds of fury at whoever did it, then she goes her own way. */
	public void onPartnerKilled(LivingEntity killer) {
		this.partner = null;
		if (this.isTame()) {
			return;
		}
		this.vengeance = killer.getUUID();
		this.vengeanceTicks = VENGEANCE_TICKS;
		this.setTarget(killer);
		this.particles(ParticleTypes.ANGRY_VILLAGER, 8);
		if (killer instanceof ServerPlayer player) {
			this.say(player, "you'll pay for that!");
		}
	}

	// ---------------------------------------------------------------- behavior

	@Override
	protected void tickBehavior() {
		this.maceJump.tickCooldown();
		if (this.vengeanceTicks > 0 && --this.vengeanceTicks == 0) {
			this.vengeance = null;
			this.setTarget(null);
		}
		if (this.ticks % 5 == 0) {
			this.chooseTarget();
		}
		if (this.target != null) {
			this.fight(this.target);
			return;
		}
		this.fightTicks = 0;
		this.buffed = false;
		this.ateApple = false;
		this.maceJump.cancel();
		if (this.tryEat() || this.tickLove()) {
			return;
		}
		if (this.tickBestFriend()) {
			return;
		}
		if (this.isTame()) {
			if (!this.followOwner()) {
				this.lookAround(8.0);
			}
			return;
		}
		LoafCharacter felix = this.partnerCharacter();
		if (felix != null && felix.player.level() == this.player.level()) {
			double distance = this.player.distanceTo(felix.player);
			if (distance > 4.0) {
				this.nav.follow(felix.player, 2.5, distance > 10.0 ? Navigator.Speed.SPRINT : Navigator.Speed.WALK);
			} else {
				this.nav.stop();
				this.controls.setSneaking(felix.player.isShiftKeyDown());
				this.lookAround(6.0);
			}
			return;
		}
		if (this.returnHome()) {
			return;
		}
		this.wander(16.0);
	}

	private void chooseTarget() {
		if (this.vengeance != null && this.vengeanceTicks > 0) {
			Player killer = this.player.level().getPlayerByUUID(this.vengeance);
			if (killer instanceof LivingEntity living && living.isAlive() && !killer.isCreative() && !killer.isSpectator()) {
				this.setTarget(living);
				return;
			}
		}
		// Anyone who hurts her best friend has to deal with her.
		ServerPlayer friend = this.findBestFriend();
		if (friend != null) {
			LivingEntity bully = friend.getLastHurtByMob();
			if (bully != null && bully.isAlive() && bully != this.player && friend.tickCount - friend.getLastHurtByMobTimestamp() < 100
					&& !this.isOwnedBy(bully) && (this.partner == null || !this.partner.equals(bully.getUUID()))
					&& !(bully instanceof Player p && (p.isCreative() || p.isSpectator()))) {
				this.setTarget(bully);
				if (this.target == bully && this.ticks >= this.nextOuchTick) {
					this.say(friend, "hey! leave them alone!");
					this.nextOuchTick = this.ticks + 200;
				}
				return;
			}
		}
		// Her best friend hit her: no fighting back, just a sad look.
		LivingEntity hitter = this.lastAttacker;
		if (hitter instanceof ServerPlayer hitBy && isBestFriend(hitBy) && this.ticks - this.lastHurtTick < 20 && this.ticks >= this.nextOuchTick) {
			this.say(hitBy, Lines.OUCH[this.random.nextInt(Lines.OUCH.length)]);
			this.particles(ParticleTypes.DAMAGE_INDICATOR, 3);
			this.nextOuchTick = this.ticks + 200;
		}
		if (this.isTame()) {
			this.protectOwner(true);
		} else {
			LoafCharacter felix = this.partnerCharacter();
			if (felix != null && felix.brain != null && felix.brain.getTarget() != null && felix.brain.getTarget() != this.player) {
				this.setTarget(felix.brain.getTarget());
				return;
			}
		}
		LivingEntity attacker = this.lastAttacker;
		if (attacker != null && attacker.isAlive() && this.ticks - this.lastHurtTick < 200 && !this.isFriendlyWith(attacker)
				&& (this.partner == null || !this.partner.equals(attacker.getUUID()))
				&& !(attacker instanceof Player p && (p.isCreative() || p.isSpectator()))) {
			this.setTarget(attacker);
		}
	}

	private void fight(LivingEntity target) {
		this.fightTicks++;
		double distance = this.player.distanceTo(target);
		if (distance > 48.0 || this.isTame() && this.isFriendlyWith(target)) {
			this.setTarget(null);
			return;
		}
		if (this.maceJump.isActive()) {
			this.maceJump.tick(target, 6.0F);
			return;
		}
		// Strength and Swiftness at the start of a fight (if there's a moment to drink).
		if (!this.buffed && distance > 5.0) {
			boolean strength = this.player.hasEffect(MobEffects.STRENGTH);
			boolean speed = this.player.hasEffect(MobEffects.SPEED);
			if (!strength && this.eatNow(stack -> isPotion(stack, Items.POTION, Potions.STRONG_STRENGTH))
					|| !speed && this.eatNow(stack -> isPotion(stack, Items.POTION, Potions.STRONG_SWIFTNESS))) {
				return;
			}
			this.buffed = true;
		}
		if (!this.ateApple && this.player.getHealth() < this.player.getMaxHealth() * 0.4F && distance > 3.0
				&& this.eatNow(stack -> stack.is(Items.GOLDEN_APPLE))) {
			this.ateApple = true;
			return;
		}
		// Mace smash when they're close and armored, or now and then to mix it up.
		boolean armored = target.getArmorValue() >= 15;
		if (distance < 5.5 && distance > 1.5 && this.maceJump.canStart() && (armored || this.random.nextInt(10) == 0)) {
			this.maceJump.start(armored ? 60 : 120);
			this.maceJump.tick(target, 6.0F);
			return;
		}
		int blade = Inv.ready(this.player, stack -> stack.is(ModItems.HARLEYS_BLADE) || stack.is(net.minecraft.tags.ItemTags.SWORDS), 0);
		if (blade >= 0) {
			this.controls.selectSlot(blade);
		}
		this.combat.fight(target, this.nav);
	}

	// ---------------------------------------------------------------- best friend

	/** The real player she adores (harleyBestFriend in the config, "FelixSyn" by default). */
	public static boolean isBestFriend(@Nullable Entity entity) {
		String name = ModConfig.get().harleyBestFriend;
		return entity instanceof ServerPlayer player && LoafIds.isRealPlayer(player) && name != null && !name.isBlank()
				&& player.getGameProfile().name().equalsIgnoreCase(name.trim());
	}

	private @Nullable ServerPlayer findBestFriend() {
		for (ServerPlayer candidate : this.player.level().players()) {
			if (isBestFriend(candidate) && candidate.isAlive() && !candidate.isSpectator()
					&& candidate.distanceTo(this.player) < FRIEND_RANGE) {
				return candidate;
			}
		}
		return null;
	}

	/** Greets, stays close and gives gifts while her best friend is around. True if she spent the tick on them. */
	private boolean tickBestFriend() {
		ServerPlayer friend = this.findBestFriend();
		if (friend == null) {
			return false;
		}
		double distance = this.player.distanceTo(friend);
		boolean sees = this.player.hasLineOfSight(friend);
		if (sees && this.ticks - this.lastSawFriendTick > 6000) {
			this.say(friend, Lines.HELLO[this.random.nextInt(Lines.HELLO.length)]);
			this.particles(ParticleTypes.HEART, 6);
		}
		if (sees) {
			this.lastSawFriendTick = this.ticks;
		}
		if (this.ticks % 50 == 0 && this.random.nextInt(3) == 0) {
			this.particles(ParticleTypes.HEART, 1);
		}
		if (sees && distance < GIFT_RANGE && this.ticks >= this.nextGiftTick) {
			this.giveGift(friend);
			int min = Math.max(10, com.itzbreadloaf.config.ModConfig.get().harleyGiftMinSeconds);
			int max = Math.max(min, com.itzbreadloaf.config.ModConfig.get().harleyGiftMaxSeconds);
			this.nextGiftTick = this.ticks + 20 * (min + this.random.nextInt(max - min + 1));
		}
		// Tamed by someone else: she still loves them, but she goes with her owner.
		if (this.sitting || this.isTame() && !this.isOwnedBy(friend)) {
			return false;
		}
		if (distance > 3.5) {
			this.nav.follow(friend, 2.5, distance > 10.0 ? Navigator.Speed.SPRINT : Navigator.Speed.WALK);
		} else {
			this.nav.stop();
			this.controls.setSneaking(friend.isShiftKeyDown());
			this.controls.lookAt(friend.getEyePosition());
		}
		return true;
	}

	/** A gift only her best friend can pick up. Every 3-5 minutes while they're close. */
	private void giveGift(ServerPlayer friend) {
		int roll = this.random.nextInt(100);
		ItemStack gift;
		if (roll < 18) {
			gift = new ItemStack(this.random.nextBoolean() ? Items.POPPY : this.random.nextBoolean() ? Items.PINK_TULIP : Items.CORNFLOWER,
					1 + this.random.nextInt(3));
		} else if (roll < 32) {
			gift = new ItemStack(Items.COOKIE, 4 + this.random.nextInt(5));
		} else if (roll < 42) {
			gift = new ItemStack(Items.GOLDEN_CARROT, 3 + this.random.nextInt(4));
		} else if (roll < 54) {
			gift = new ItemStack(Items.GOLDEN_APPLE, 1 + this.random.nextInt(2));
		} else if (roll < 64) {
			gift = new ItemStack(Items.ENDER_PEARL, 2 + this.random.nextInt(4));
		} else if (roll < 74) {
			gift = new ItemStack(Items.WIND_CHARGE, 6 + this.random.nextInt(7));
		} else if (roll < 82) {
			gift = new ItemStack(Items.CAKE);
		} else if (roll < 90) {
			gift = new ItemStack(Items.EXPERIENCE_BOTTLE, 4 + this.random.nextInt(5));
		} else if (roll < 97) {
			gift = new ItemStack(Items.DIAMOND, 1 + this.random.nextInt(2));
		} else {
			gift = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
		}
		ItemEntity item = new ItemEntity(this.player.level(), this.player.getX(), this.player.getEyeY() - 0.3, this.player.getZ(), gift);
		Vec3 toward = friend.position().subtract(this.player.position());
		Vec3 motion = toward.lengthSqr() > 0.01 ? toward.normalize().scale(0.3) : Vec3.ZERO;
		item.setDeltaMovement(motion.x, 0.25, motion.z);
		item.setPickUpDelay(10);
		item.setTarget(friend.getUUID());
		this.player.level().addFreshEntity(item);
		this.controls.lookAt(friend.getEyePosition());
		this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.2F);
		this.particles(ParticleTypes.HEART, 6);
		this.say(friend, Lines.GIFT[this.random.nextInt(Lines.GIFT.length)]);
	}

	@Override
	protected String[] chatterLines() {
		return new String[]{"*spins her blade*", "stay sharp", "I don't lose duels", "where'd FelixSyn wander off to?", "*checks her pearls*"};
	}

	@Override
	protected @Nullable String[] chatWith(LoafBrain other) {
		return other.kind() == com.itzbreadloaf.character.CharacterKind.FELIXSYN
				? new String[]{"lead the way", "always got your back", "ready when you are"} : null;
	}

	/** Harley's lines for her best friend. */
	private static final class Lines {
		static final String[] HELLO = {"hiii! you're here!", "you're back!! I missed you", "there you are <3", "yay, my favorite person!"};
		static final String[] GIFT = {"I got you something <3", "here, this is for you!", "found this and thought of you",
				"a little present, just for you", "don't tell anyone I only give these to you"};
		static final String[] OUCH = {"ow!! why would you hit me :(", "hey! that hurt...", "I'd never hit YOU", "rude :("};
	}

	// ---------------------------------------------------------------- taming

	@Override
	protected @Nullable InteractionResult interact(ServerPlayer clicker, ItemStack stack) {
		if (!this.isTame()) {
			if (stack.is(Items.GOLDEN_APPLE) && this.target == null && !this.baby) {
				ItemStack apple = stack.copyWithCount(1);
				stack.consume(1, clicker);
				Inv.give(this.player, apple);
				// Her best friend only needs one apple.
				if (isBestFriend(clicker) || this.random.nextInt(Math.max(1, com.itzbreadloaf.config.ModConfig.get().harleyTameChance)) == 0) {
					this.setOwner(clicker);
					this.partner = null;
					this.vengeance = null;
					this.particles(ParticleTypes.HEART, 8);
					this.say(clicker, "...fine. I'm with you now.");
					ModAdvancements.award(clicker, ModAdvancements.TAKEN_IN);
				} else {
					this.particles(ParticleTypes.SMOKE, 6);
				}
				return InteractionResult.SUCCESS;
			}
			return null;
		}
		if (this.isOwnedBy(clicker) && stack.isEmpty()) {
			this.setSitting(!this.sitting);
			this.setTarget(null);
			return InteractionResult.SUCCESS;
		}
		return null;
	}

	// ---------------------------------------------------------------- death and loot

	/** Each armor piece 8.5% (like normal mobs), Harley's Blade 25% when she was wild. Untouchable for whoever beat her. */
	@Override
	public boolean onDeath(DamageSource source) {
		ServerLevel level = this.player.level();
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			if (this.random.nextFloat() < com.itzbreadloaf.config.ModConfig.get().characterArmorDropChance) {
				this.player.spawnAtLocation(level, this.player.getItemBySlot(slot).copy());
			}
		}
		if (!this.isTame()) {
			if (this.random.nextFloat() < 0.25F) {
				this.player.spawnAtLocation(level, ModItems.harleysBlade(level.registryAccess()));
			}
			if (source.getEntity() instanceof ServerPlayer killer && LoafIds.isRealPlayer(killer)) {
				ModAdvancements.award(killer, ModAdvancements.UNTOUCHABLE);
			}
		}
		// Everything else comes from her drop table and its tier rolls (loot rarity system).
		com.itzbreadloaf.loot.LootRarity.dropCharacterLoot(this, this.player, source);
		this.player.getInventory().clearContent();
		return false;
	}

	@Override
	public void save(CompoundTag tag) {
		super.save(tag);
		if (this.partner != null) {
			tag.store("Partner", UUIDUtil.CODEC, this.partner);
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.partner = tag.read("Partner", UUIDUtil.CODEC).orElse(null);
	}

}
