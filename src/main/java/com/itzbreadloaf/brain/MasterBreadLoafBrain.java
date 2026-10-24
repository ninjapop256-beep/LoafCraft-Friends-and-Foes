package com.itzbreadloaf.brain;

import com.itzbreadloaf.ModAdvancements;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.LoafCharacter;
import com.itzbreadloaf.character.LoafIds;
import com.itzbreadloaf.config.ModConfig;
import com.itzbreadloaf.item.ModItems;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Master BreadLoaf: a boss who fights like a PvP mace player, entirely with real player inputs. Nerfed hard (AI
 * overhaul part 2) so an average player in full diamond can beat him on Normal; see AI_DESIGN.md section 6.
 *
 * Kit (real hotbar, Normal): 1 diamond sword (his hits do 55% damage on Normal), 2 Wind Burst I mace, 3 wind charges, 4 firework rockets,
 * 5 ender pearls, 6 one golden apple, 9 elytra; one Speed I potion (Hard adds Strength I); diamond armor with
 * Protection II. The Easy/Normal/Hard numbers live in {@link ModConfig.BossPreset}.
 *
 * Limits: no hit of his deals more than 16 damage (LivingEntityMixin), at least 8 seconds between mace smashes,
 * Wind Burst chains of at most 2 hits, elytra dives at most every 20 seconds (and some fail on purpose), a
 * 1-second wind-up with a loud sound and particles before every smash and every dive, a 300-400 ms reaction delay
 * to new targets, and some sword swings that miss.
 *
 * Phase 1 (over half health): sword on the ground, a warned smash every 8+ seconds.
 * Phase 2 (half health and below): a little faster. Nothing else changes, and nobody is summoned.
 */
public class MasterBreadLoafBrain extends LoafBrain {
	public static final int SLOT_SWORD = 0;
	public static final int SLOT_MACE = 1;
	public static final int SLOT_WIND_CHARGE = 2;
	public static final int SLOT_ROCKET = 3;
	public static final int SLOT_PEARL = 4;
	public static final int SLOT_GOLDEN_APPLE = 5;
	public static final int SLOT_ELYTRA = 8;
	/** How high above the target he climbs before a dive. */
	public static final double DIVE_HEIGHT = 18.0;
	public static final double BOSS_BAR_RANGE = 64.0;
	public static final double HUNT_RANGE = 48.0;
	/** Ticks of warning (charge-up sound and particles, standing still or hanging in the air) before a smash or dive. */
	public static final int WINDUP_TICKS = 20;
	/** Smashes in one Wind Burst chain. */
	public static final int MAX_CHAIN = 2;
	/** Bumped when his kit changes, so Master BreadLoafs saved in old worlds get the new kit. */
	private static final int KIT_VERSION = 2;

	private static final Identifier PHASE2_SPEED_ID = Identifier.fromNamespaceAndPath("itzbreadloaf", "boss_phase3_speed");
	private static final Identifier DAMAGE_PRESET_ID = Identifier.fromNamespaceAndPath("itzbreadloaf", "boss_damage_preset");

	private enum State { GROUND, DRINK, WIND_JUMP, TAKEOFF, CLIMB, CRUISE, TELEGRAPH, DROP, RECOVER, TECHNIQUE }

	private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
			net.minecraft.network.chat.Component.literal("Master BreadLoaf"),
			BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS).setDarkenScreen(true);
	private final Set<UUID> tookFallDamage = new HashSet<>();

	private State state = State.GROUND;
	private int stateTicks;
	/** Ticks until the next mace smash is allowed (8+ seconds after the last one ends). */
	private int smashCooldown = 100;
	/** Ticks until the next elytra dive or pearl catch (20+ seconds). */
	private int techniqueCooldown = 200;
	private int pearlCooldown;
	private int lastRocketTick = -100;
	private int buffStep;
	private boolean drinkStarted;
	private int smashChain;
	private boolean telegraphed;
	private double diveTopY;
	/** This dive is one he fails on purpose: he comes down next to the target instead of on them. */
	private boolean diveMisses;
	private Vec3 missOffset = Vec3.ZERO;
	private int kitVersion = KIT_VERSION;
	private LivingEntity reactingTo;
	private int reactionTicks;
	private int idleTicks;
	/** His pearl catches (the shared mace moves, held to his limits). */
	private final MaceTechniques techniques;

	public MasterBreadLoafBrain(LoafCharacter character) {
		super(character);
		this.combat.useShield = false;
		this.techniques = new MaceTechniques(this.player, this.controls, this.combat, this.random);
	}

	/** The shared mace moves, always held to his nerf: the warning first, his fall cap, 2-hit chains, failures. */
	@Override
	public MaceTechniques techniques() {
		ModConfig.BossPreset preset = preset();
		this.techniques.limits.maxFall = preset.maxSmashFall;
		this.techniques.limits.maxChain = MAX_CHAIN;
		this.techniques.limits.warnTicks = WINDUP_TICKS;
		this.techniques.limits.warn = this::windupWarning;
		this.techniques.limits.failChance = preset.techniqueFailChance;
		return this.techniques;
	}

	// ---------------------------------------------------------------- stats and kit

	private static ModConfig.BossPreset preset() {
		return ModConfig.get().bossPreset();
	}

	@Override
	protected double baseMaxHealth() {
		return preset().health;
	}

	@Override
	public void applyAttributes() {
		super.applyAttributes();
		this.setBase(Attributes.KNOCKBACK_RESISTANCE, 0.3);
		AttributeInstance damage = this.player.getAttribute(Attributes.ATTACK_DAMAGE);
		if (damage != null) {
			damage.addOrReplacePermanentModifier(new AttributeModifier(DAMAGE_PRESET_ID, preset().damageMultiplier - 1.0,
					AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
		this.combat.whiffChance = preset().whiffChance;
		this.combat.critChance = preset().critChance;
	}

	@Override
	protected void rollGear() {
		ServerLevel level = this.player.level();
		ModConfig.BossPreset preset = preset();
		var inventory = this.player.getInventory();
		inventory.clearContent();
		this.player.setItemSlot(EquipmentSlot.HEAD, Gear.enchanted(level, Items.DIAMOND_HELMET, Enchantments.PROTECTION, preset.protection));
		this.player.setItemSlot(EquipmentSlot.CHEST, Gear.enchanted(level, Items.DIAMOND_CHESTPLATE, Enchantments.PROTECTION, preset.protection));
		this.player.setItemSlot(EquipmentSlot.LEGS, Gear.enchanted(level, Items.DIAMOND_LEGGINGS, Enchantments.PROTECTION, preset.protection));
		this.player.setItemSlot(EquipmentSlot.FEET, Gear.enchanted(level, Items.DIAMOND_BOOTS, Enchantments.PROTECTION, preset.protection));
		this.player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
		inventory.setItem(SLOT_SWORD, preset.sharpness > 0
				? Gear.enchanted(level, Items.DIAMOND_SWORD, Enchantments.SHARPNESS, preset.sharpness) : new ItemStack(Items.DIAMOND_SWORD));
		inventory.setItem(SLOT_MACE, Gear.enchanted(level, Items.MACE, Enchantments.WIND_BURST, 1));
		inventory.setItem(SLOT_WIND_CHARGE, new ItemStack(Items.WIND_CHARGE, 32));
		inventory.setItem(SLOT_ROCKET, new ItemStack(Items.FIREWORK_ROCKET, 32));
		inventory.setItem(SLOT_PEARL, new ItemStack(Items.ENDER_PEARL, 8));
		inventory.setItem(SLOT_ELYTRA, new ItemStack(Items.ELYTRA));
		this.refillForNextFight();
		inventory.setSelectedSlot(SLOT_SWORD);
		this.kitVersion = KIT_VERSION;
	}

	/** One golden apple and the start-of-fight potions, given again once a fight is over. */
	private void refillForNextFight() {
		var inventory = this.player.getInventory();
		if (Inv.count(this.player, stack -> stack.is(Items.GOLDEN_APPLE)) == 0) {
			inventory.setItem(SLOT_GOLDEN_APPLE, new ItemStack(Items.GOLDEN_APPLE, 1));
		}
		if (Inv.count(this.player, stack -> stack.is(Items.POTION) && hasPotion(stack, Potions.SWIFTNESS)) == 0) {
			Inv.give(this.player, PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS));
		}
		if (preset().strength > 0 && Inv.count(this.player, stack -> stack.is(Items.POTION) && hasPotion(stack, Potions.STRENGTH)) == 0) {
			Inv.give(this.player, PotionContents.createItemStack(Items.POTION, Potions.STRENGTH));
		}
		this.buffStep = 0;
	}

	@Override
	protected void restock() {
		// Food so he can sprint and regenerate between fights.
		if (Inv.count(this.player, stack -> stack.is(Items.COOKED_BEEF)) < 8) {
			Inv.give(this.player, new ItemStack(Items.COOKED_BEEF, 16));
		}
	}

	@Override
	protected SoundSource soundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	public SoundEvent deathSound() {
		return SoundEvents.WITHER_DEATH;
	}

	@Override
	public int experienceReward() {
		return 200;
	}

	/** His rockets and wind charges are his own; he doesn't pick things up. */
	@Override
	public boolean wantsToPickUp(ItemStack stack) {
		return false;
	}

	/** 1 over half health, 2 at half health and below. */
	public int phase() {
		return this.player.getHealth() > this.player.getMaxHealth() * 0.5F ? 1 : 2;
	}

	private static boolean isChestplate(ItemStack stack) {
		return stack.is(Items.DIAMOND_CHESTPLATE) || stack.is(Items.NETHERITE_CHESTPLATE);
	}

	// ---------------------------------------------------------------- lifecycle

	@Override
	public void onActivated() {
		super.onActivated();
		// Despawned mid-dive: put the chestplate back on.
		if (this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
			int chest = Inv.find(this.player, MasterBreadLoafBrain::isChestplate);
			if (chest >= 0) {
				ItemStack chestplate = this.player.getInventory().getItem(chest);
				this.player.getInventory().setItem(chest, this.player.getItemBySlot(EquipmentSlot.CHEST));
				this.player.setItemSlot(EquipmentSlot.CHEST, chestplate);
			}
		}
		// Saved before the nerf: netherite, three maces and 200 health. New kit, new health.
		if (this.kitVersion < KIT_VERSION) {
			this.rollGear();
			this.restock();
			this.applyAttributes();
		}
		this.bossEvent.setName(this.player.getDisplayName());
	}

	@Override
	public void onDeactivated() {
		super.onDeactivated();
		this.bossEvent.removeAllPlayers();
	}

	@Override
	public boolean allowDamage(DamageSource source, float amount) {
		// No fall damage (mace smashes, missed dives) and no elytra wall crashes.
		return !source.is(DamageTypeTags.IS_FALL) && !source.is(DamageTypes.FLY_INTO_WALL);
	}

	/** Mace Master: players who took fall damage during the fight don't get it. */
	public void markFallDamage(UUID player) {
		this.tookFallDamage.add(player);
	}

	@Override
	public boolean onDeath(DamageSource source) {
		ServerLevel level = this.player.level();
		this.bossEvent.removeAllPlayers();
		if (source.getEntity() instanceof ServerPlayer killer && LoafIds.isRealPlayer(killer)) {
			ModAdvancements.award(killer, ModAdvancements.GROUNDED);
			if (!this.tookFallDamage.contains(killer.getUUID())) {
				ModAdvancements.award(killer, ModAdvancements.MACE_MASTER);
			}
		}
		// Guaranteed: the trophy. His Wind Burst mace only super rarely (maceDropChance, 0.8%). Plus his drop table:
		// extra Epic and Legendary rolls.
		if (com.itzbreadloaf.loot.LootRarity.maceDrops(this.random)) {
			this.player.spawnAtLocation(level, Gear.enchanted(level, Items.MACE, Enchantments.WIND_BURST, 2));
		}
		this.player.spawnAtLocation(level, new ItemStack(ModItems.MASTER_LOAF_TROPHY));
		com.itzbreadloaf.loot.LootRarity.dropCharacterLoot(this, this.player, source);
		this.player.getInventory().clearContent();
		return false;
	}

	// ---------------------------------------------------------------- ticking

	@Override
	protected void tickBehavior() {
		if (this.ticks % 10 == 0) {
			this.updateBossBar();
		}
		if (this.smashCooldown > 0) {
			this.smashCooldown--;
		}
		if (this.techniqueCooldown > 0) {
			this.techniqueCooldown--;
		}
		if (this.pearlCooldown > 0) {
			this.pearlCooldown--;
		}
		if (this.ticks % 10 == 0 && (this.target == null || this.state == State.GROUND)) {
			this.chooseTarget();
		}
		LivingEntity target = this.target;
		if (target == null) {
			this.reactingTo = null;
			if (this.state != State.GROUND && this.state != State.RECOVER) {
				this.setState(State.RECOVER);
			}
			if (this.state == State.RECOVER) {
				this.recover();
				return;
			}
			// A fight is over after 30 quiet seconds: one golden apple and the potions again for the next one.
			if (++this.idleTicks == 600) {
				this.refillForNextFight();
			}
			this.controls.selectSlot(SLOT_SWORD);
			if (!this.tryEat()) {
				this.wander(10.0);
			}
			return;
		}
		this.idleTicks = 0;
		// Reaction time: a new target is noticed 300-400 ms later (Normal). Until then he only turns toward them.
		if (target != this.reactingTo) {
			this.reactingTo = target;
			ModConfig.BossPreset preset = preset();
			this.reactionTicks = preset.reactionMin + this.random.nextInt(Math.max(1, preset.reactionMax - preset.reactionMin + 1));
		}
		if (this.reactionTicks > 0 && this.state == State.GROUND) {
			this.reactionTicks--;
			this.controls.lookAt(target.getEyePosition());
			return;
		}
		this.phaseEffects();
		this.stateTicks++;
		switch (this.state) {
			case GROUND -> this.ground(target);
			case DRINK -> this.drink();
			case WIND_JUMP -> this.windJump(target);
			case TAKEOFF -> this.takeoff(target);
			case CLIMB -> this.climb(target);
			case CRUISE -> this.cruise(target);
			case TELEGRAPH -> this.telegraphOver(target);
			case DROP -> this.drop(target);
			case RECOVER -> this.recover();
			case TECHNIQUE -> {
				if (!this.techniques.tick(target)) {
					this.setState(State.RECOVER);
				}
			}
		}
	}

	private void setState(State state) {
		this.state = state;
		this.stateTicks = 0;
		this.telegraphed = false;
	}

	private void chooseTarget() {
		if (this.lastAttacker != null && this.lastAttacker.isAlive() && this.ticks - this.lastHurtTick < 200
				&& this.lastAttacker.distanceTo(this.player) < HUNT_RANGE && CharacterManager.brain(this.lastAttacker) == null) {
			if (!(this.lastAttacker instanceof Player p) || !p.isCreative() && !p.isSpectator()) {
				this.setTarget(this.lastAttacker);
				return;
			}
		}
		if (this.target != null) {
			return;
		}
		Player nearest = this.player.level().getNearestPlayer(this.player.getX(), this.player.getY(), this.player.getZ(), HUNT_RANGE,
				p -> p != this.player && p instanceof Player pl && LoafIds.isRealPlayer(pl) && !pl.isSpectator() && !pl.isCreative());
		if (nearest != null) {
			this.setTarget(nearest);
		}
	}

	private void updateBossBar() {
		this.bossEvent.setProgress(this.player.getHealth() / this.player.getMaxHealth());
		for (ServerPlayer viewer : Set.copyOf(this.bossEvent.getPlayers())) {
			if (viewer.level() != this.player.level() || viewer.distanceTo(this.player) > BOSS_BAR_RANGE + 16.0 || viewer.isRemoved()) {
				this.bossEvent.removePlayer(viewer);
			}
		}
		for (ServerPlayer viewer : this.player.level().players()) {
			if (LoafIds.isRealPlayer(viewer) && viewer.distanceTo(this.player) <= BOSS_BAR_RANGE) {
				this.bossEvent.addPlayer(viewer);
			}
		}
	}

	/** Phase 2: a little faster. That's all (no summons, no extra damage). */
	private void phaseEffects() {
		AttributeInstance speed = this.player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}
		if (this.phase() == 2) {
			speed.addOrReplacePermanentModifier(new AttributeModifier(PHASE2_SPEED_ID, preset().phase2Speed,
					AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		} else if (speed.hasModifier(PHASE2_SPEED_ID)) {
			speed.removeModifier(PHASE2_SPEED_ID);
		}
	}

	// ---------------------------------------------------------------- ground fighting

	private void ground(LivingEntity target) {
		double distance = this.player.distanceTo(target);

		// Start of the fight: Speed I (and Strength I on Hard), drunk at a distance.
		int buffs = preset().strength > 0 ? 2 : 1;
		if (this.buffStep < buffs && distance > 5.0) {
			if (this.startDrinking(this.buffPotion(this.buffStep))) {
				this.buffStep++;
				return;
			}
			this.buffStep++;
		}
		// Low health: his one golden apple, eaten at a distance. No healing potions.
		if (this.player.getHealth() < this.player.getMaxHealth() * 0.35F && this.stateTicks > 40 && distance > 6.0
				&& this.startDrinking(stack -> stack.is(Items.GOLDEN_APPLE))) {
			return;
		}
		// Far away (or out of walking reach): ender pearl.
		boolean unreachable = this.nav.isAtDeadEnd() || this.nav.failed();
		if ((distance > 24.0 || unreachable && distance > 6.0) && this.pearlCooldown <= 0 && this.throwPearl(target)) {
			return;
		}
		// A mace smash every 8+ seconds: an elytra dive (every 20+ seconds) or a wind-charge jump.
		if (this.smashCooldown <= 0 && this.player.onGround()) {
			boolean dive = this.techniqueCooldown <= 0 && this.random.nextFloat() < 0.5F && this.hasRoomAbove(6) && distance < 40.0;
			boolean pearlCatch = !dive && this.techniqueCooldown <= 0 && distance > 3.0 && distance < 10.0
					&& this.random.nextFloat() < 0.6F && this.techniques().canStart(MaceTechniques.Technique.PEARL_CATCH, target);
			if (dive) {
				this.setState(State.TAKEOFF);
				this.diveTopY = target.getY() + DIVE_HEIGHT;
				this.techniqueCooldown = preset().techniqueCooldown;
				this.diveMisses = this.random.nextFloat() < preset().techniqueFailChance;
				double angle = this.random.nextDouble() * Math.PI * 2.0;
				this.missOffset = this.diveMisses ? new Vec3(Math.cos(angle) * 4.5, 0.0, Math.sin(angle) * 4.5) : Vec3.ZERO;
			} else if (pearlCatch) {
				// A pearl catch, at most every 20 seconds (it shares the dive's cooldown), warned like everything else.
				this.techniqueCooldown = preset().techniqueCooldown;
				this.techniques().start(MaceTechniques.Technique.PEARL_CATCH, target, 0);
				this.setState(State.TECHNIQUE);
			} else {
				this.setState(State.WIND_JUMP);
			}
			this.smashChain = 0;
			this.smashCooldown = preset().smashCooldown;
			return;
		}
		this.controls.selectSlot(SLOT_SWORD);
		this.combat.fight(target, this.nav);
	}

	private Predicate<ItemStack> buffPotion(int step) {
		return step == 0
				? stack -> stack.is(Items.POTION) && hasPotion(stack, Potions.SWIFTNESS)
				: stack -> stack.is(Items.POTION) && hasPotion(stack, Potions.STRENGTH);
	}

	private static boolean hasPotion(ItemStack stack, net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion) {
		PotionContents contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
		return contents != null && contents.is(potion);
	}

	/** Moves a drink (or food) into the golden apple slot and holds right-click until it's finished. */
	private boolean startDrinking(Predicate<ItemStack> what) {
		int slot = Inv.ready(this.player, what, SLOT_GOLDEN_APPLE);
		if (slot < 0) {
			return false;
		}
		this.controls.selectSlot(slot);
		this.controls.holdUse(true);
		this.drinkStarted = false;
		this.setState(State.DRINK);
		return true;
	}

	private void drink() {
		// Back off while drinking.
		if (this.target != null && this.player.distanceTo(this.target) < 8.0) {
			this.controls.walk(this.player.position().subtract(this.target.position()));
			this.controls.setSprinting(false);
		}
		this.controls.look(this.player.getYRot(), -30.0F);
		if (this.player.isUsingItem()) {
			this.drinkStarted = true;
		}
		if (this.drinkStarted && !this.player.isUsingItem() || this.stateTicks > 50) {
			this.controls.holdUse(false);
			this.setState(State.GROUND);
		}
	}

	private boolean throwPearl(LivingEntity target) {
		if (this.player.getInventory().getItem(SLOT_PEARL).isEmpty()) {
			return false;
		}
		this.controls.selectSlot(SLOT_PEARL);
		// Not a perfect throw: aimed at a spot up to 3 blocks off.
		this.combat.aimProjectile(target, 1.5, 0.03);
		this.controls.lookInstantly(this.player.getYRot() + (this.random.nextFloat() - 0.5F) * 8.0F, this.player.getXRot());
		this.controls.useOnce();
		this.pearlCooldown = 160;
		this.playSound(SoundEvents.ENDER_PEARL_THROW, 1.0F, 0.6F);
		return true;
	}

	// ---------------------------------------------------------------- wind charge mace jump

	private void windJump(LivingEntity target) {
		// 1 second of warning first: he stops, the charge-up sound plays and sparks fly.
		if (this.stateTicks <= WINDUP_TICKS) {
			this.controls.selectSlot(SLOT_WIND_CHARGE);
			this.controls.lookAt(target.getEyePosition());
			if (this.stateTicks == 1) {
				this.windupWarning(true);
			} else if (this.stateTicks % 4 == 0) {
				this.windupWarning(false);
			}
			return;
		}
		int t = this.stateTicks - WINDUP_TICKS;
		if (t <= 6) {
			// Look down, jump, throw the wind charge at our feet: up we go.
			this.controls.lookInstantly(Controls.yawTo(this.player.position(), target.position()), 90.0F);
			if (this.player.onGround() && t <= 2) {
				this.controls.jumpOnce();
			} else if (!this.player.onGround() && t >= 3 && t <= 5) {
				this.controls.useOnce();
				this.stateTicks = WINDUP_TICKS + 6;
			}
			return;
		}
		this.airSmash(target, t > 12 && this.player.onGround());
	}

	/**
	 * In the air above or near the target: mace out, a warning as the fall starts, drift toward them and smash on the
	 * way down. Wind Burst throws him back up after a hit, but he smashes at most twice per chain.
	 */
	private void airSmash(LivingEntity target, boolean landed) {
		Vec3 aim = this.combat.aimPoint(target).add(this.missOffset);
		double distance = this.player.getEyePosition().distanceTo(aim);
		boolean falling = this.player.getDeltaMovement().y < 0.0 && !this.player.onGround();
		boolean canSmash = this.smashChain < MAX_CHAIN;
		if (canSmash && !this.telegraphed && falling) {
			this.windupWarning(true);
		}
		this.controls.selectSlot(canSmash ? SLOT_MACE : SLOT_SWORD);
		this.controls.aimAt(aim);
		this.controls.walkToward(target.position().add(this.missOffset));
		this.controls.setSprinting(false);
		// Fairness: cap how far a smash counts as a fall (mace damage grows with the fall). The 16-damage cap is on top.
		if (this.player.fallDistance > preset().maxSmashFall) {
			this.player.fallDistance = preset().maxSmashFall;
		}
		// No full-charge wait: the smash bonus isn't scaled by the attack charge (only the mace's base damage is).
		if (canSmash && falling && distance <= this.combat.reach() - 0.1 && this.player.fallDistance > 1.5) {
			this.controls.attackOnce();
			this.smashChain++;
			this.telegraphed = false;
		}
		if (landed || this.stateTicks > 160) {
			this.setState(State.RECOVER);
		}
	}

	/** The warning before every smash and dive: a loud charge-up sound, sparks and portal particles. */
	private void windupWarning(boolean withSound) {
		this.telegraphed = true;
		ServerLevel level = this.player.level();
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.player.getX(), this.player.getY(1.0), this.player.getZ(), 16, 0.6, 0.7, 0.6, 0.08);
		level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.player.getX(), this.player.getY(0.5), this.player.getZ(), 12, 0.5, 0.6, 0.5, 0.04);
		if (withSound) {
			this.playSound(SoundEvents.BREEZE_CHARGE, 3.0F, 0.6F);
			this.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 2.0F, 1.4F);
		}
	}

	// ---------------------------------------------------------------- elytra mace dive (rockets)

	/** Swap the chestplate for the elytra (right-click it in the hotbar), jump, and start gliding. */
	private void takeoff(LivingEntity target) {
		boolean wearingElytra = this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
		if (!wearingElytra) {
			int elytra = Inv.ready(this.player, stack -> stack.is(Items.ELYTRA), SLOT_ELYTRA);
			if (elytra < 0 || this.stateTicks > 10) {
				this.setState(State.WIND_JUMP);
				return;
			}
			this.controls.selectSlot(elytra);
			this.controls.look(this.player.getYRot(), -60.0F);
			if (this.stateTicks % 3 == 1) {
				this.controls.useOnce();
			}
			return;
		}
		this.controls.selectSlot(SLOT_ROCKET);
		this.controls.look(Controls.yawTo(this.player.position(), target.position()), -60.0F);
		if (this.player.isFallFlying()) {
			this.useRocket();
			this.setState(State.CLIMB);
			return;
		}
		if (this.player.onGround()) {
			this.controls.jumpOnce();
		} else if (this.player.getDeltaMovement().y < 0.1) {
			// The jump key in mid-air: start gliding.
			this.player.tryToStartFallFlying();
		}
		if (this.stateTicks > 40) {
			this.setState(State.RECOVER);
		}
	}

	private void useRocket() {
		if (this.ticks - this.lastRocketTick < 18 || this.player.getInventory().getItem(SLOT_ROCKET).isEmpty()) {
			return;
		}
		this.controls.selectSlot(SLOT_ROCKET);
		this.controls.useOnce();
		this.lastRocketTick = this.ticks;
	}

	/** Rocket up at a steep angle until well above the target (or a ceiling). */
	private void climb(LivingEntity target) {
		if (!this.player.isFallFlying()) {
			this.setState(State.DROP);
			return;
		}
		this.controls.look(Controls.yawTo(this.player.position(), target.position()), -55.0F);
		this.useRocket();
		if (this.player.getY() >= this.diveTopY || !this.hasRoomAbove(4) || this.stateTicks > 160) {
			this.diveTopY = Math.max(target.getY() + 8.0, Math.min(this.diveTopY, this.player.getY()));
			this.setState(State.CRUISE);
		}
	}

	/** Glide to a point above the target (or beside them, on a dive he fails on purpose). */
	private void cruise(LivingEntity target) {
		if (!this.player.isFallFlying()) {
			this.setState(State.DROP);
			return;
		}
		Vec3 spot = target.position().add(this.missOffset);
		Vec3 over = new Vec3(spot.x, this.diveTopY, spot.z);
		double horizontal = Math.sqrt(Math.pow(spot.x - this.player.getX(), 2) + Math.pow(spot.z - this.player.getZ(), 2));
		float pitch = this.player.getY() < this.diveTopY - 3.0 ? -25.0F : horizontal > 20.0 ? 5.0F : 15.0F;
		this.controls.look(Controls.yawTo(this.player.position(), over), pitch);
		double speed = this.player.getDeltaMovement().horizontalDistance();
		if ((speed < 0.6 || this.player.getY() < this.diveTopY - 5.0) && horizontal > 8.0) {
			this.useRocket();
		}
		if (horizontal < 5.0 || this.stateTicks > 200) {
			this.setState(State.TELEGRAPH);
		}
	}

	/** Over the target: pull up to bleed speed, 1 second of sparks and the charge-up sound, then drop. */
	private void telegraphOver(LivingEntity target) {
		this.controls.look(Controls.yawTo(this.player.position(), target.position()), -35.0F);
		if (this.stateTicks == 1) {
			this.windupWarning(true);
		} else if (this.stateTicks % 4 == 0) {
			this.windupWarning(false);
		}
		if (this.stateTicks >= WINDUP_TICKS || !this.player.isFallFlying()) {
			this.setState(State.DROP);
		}
	}

	/** Chestplate back on (stops the glide, so the fall starts counting), mace out, smash. */
	private void drop(LivingEntity target) {
		if (this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
			int chestplate = Inv.ready(this.player, MasterBreadLoafBrain::isChestplate, SLOT_ELYTRA);
			if (chestplate >= 0 && this.stateTicks <= 6) {
				this.controls.selectSlot(chestplate);
				this.controls.look(this.player.getYRot(), -60.0F);
				if (this.stateTicks % 2 == 1) {
					this.controls.useOnce();
				}
				return;
			}
		}
		this.telegraphed = true;
		this.airSmash(target, this.player.onGround() && this.stateTicks > 4);
	}

	/** After a smash or dive: back to the sword (and the chestplate, if the elytra is still on); the 8-second wait restarts. */
	private void recover() {
		if (this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA) && this.player.onGround()) {
			int chestplate = Inv.ready(this.player, MasterBreadLoafBrain::isChestplate, SLOT_ELYTRA);
			if (chestplate >= 0 && this.stateTicks < 10) {
				this.controls.selectSlot(chestplate);
				this.controls.look(this.player.getYRot(), -60.0F);
				if (this.stateTicks % 3 == 1) {
					this.controls.useOnce();
				}
				return;
			}
		}
		this.missOffset = Vec3.ZERO;
		this.diveMisses = false;
		this.techniques.cancel();
		this.smashCooldown = Math.max(this.smashCooldown, preset().smashCooldown);
		this.controls.selectSlot(SLOT_SWORD);
		this.setState(State.GROUND);
	}

	/** Open sky (no blocks) for {@code blocks} blocks above his head. */
	private boolean hasRoomAbove(int blocks) {
		AABB above = this.player.getBoundingBox().move(0.0, 1.0, 0.0).expandTowards(0.0, blocks, 0.0);
		return this.player.level().noCollision(this.player, above);
	}

	// ---------------------------------------------------------------- saving

	@Override
	public void save(CompoundTag tag) {
		super.save(tag);
		tag.putInt("BuffStep", this.buffStep);
		tag.putInt("KitVersion", this.kitVersion);
		tag.store("FallDamaged", UUIDUtil.CODEC_SET, Set.copyOf(this.tookFallDamage));
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.buffStep = tag.getIntOr("BuffStep", 0);
		// No KitVersion tag: saved before the nerf.
		this.kitVersion = tag.getIntOr("KitVersion", 1);
		this.tookFallDamage.clear();
		tag.read("FallDamaged", UUIDUtil.CODEC_SET).ifPresent(this.tookFallDamage::addAll);
	}

	// ---------------------------------------------------------------- personality (AI overhaul part 6)

	/** Master BreadLoaf: a proud showman of a boss. */
	@Override
	protected String[] lines(com.itzbreadloaf.brain.ai.Situation situation) {
		return switch (situation) {
			case GREET_PLAYER -> new String[]{"..."};
			case GREET_FRIEND -> new String[]{"..."};
			case FIGHT_START -> new String[]{"so, you've come to challenge me.", "show me what you've got.", "let's see if you're ready."};
			case RIVAL -> new String[]{"..."};
			case FIGHT_WON -> new String[]{"train harder. come back stronger.", "not today."};
			case LOW_HEALTH -> new String[]{"impressive... but not enough!", "you're stronger than you look."};
			case DEFEND_FRIEND -> new String[]{"..."};
			case SCARED_FOR_FRIEND -> new String[]{"..."};
			case REST -> new String[]{"..."};
			case MORNING -> new String[]{"..."};
			case NIGHT -> new String[]{"..."};
			case RAIN -> new String[]{"..."};
			case ITEM -> new String[]{"..."};
			case WATCH -> new String[]{"..."};
		};
	}

	@Override
	protected String[] moodLines(com.itzbreadloaf.brain.ai.Mood mood) {
		return switch (mood) {
			case HAPPY -> new String[]{"heh."};
			case BORED -> new String[]{"is anyone strong enough?"};
			case TIRED -> new String[]{"..."};
			case CURIOUS -> new String[]{"hm. a challenger?"};
			case SCARED -> new String[]{"..."};
			case ANGRY -> new String[]{"now you'll see my real strength!"};
		};
	}
}
