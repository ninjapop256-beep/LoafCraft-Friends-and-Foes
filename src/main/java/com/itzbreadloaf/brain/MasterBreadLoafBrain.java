package com.itzbreadloaf.brain;

import com.itzbreadloaf.ModAdvancements;
import com.itzbreadloaf.character.CharacterKind;
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
import org.jetbrains.annotations.Nullable;

/**
 * Master BreadLoaf: a boss who fights like a PvP mace player, entirely with real player inputs.
 *
 * Kit (real hotbar): 1 Sharpness V netherite sword, 2 Wind Burst II mace, 3 Density V mace, 4 Breach IV mace,
 * 5 wind charges, 6 firework rockets, 7 ender pearls, 8 golden apples, 9 elytra; real potions in the inventory
 * (Strength II, Swiftness II, Fire Resistance, Splash Healing II); full netherite with Protection IV.
 *
 * Elytra mace dive: right-click the elytra (it swaps with the chestplate), jump, start gliding, climb on firework
 * rockets until well above the target, glide over them, warn (charge-up sound and sparks), right-click the
 * chestplate back on (the glide stops), pick a mace for the fall and the target's armor, and smash on the way down.
 * Wind-charge jumps: look down, jump, throw a wind charge at his feet, smash. Pearls to close long gaps.
 *
 * Phase 1 (100-60%): sword on the ground, an occasional dive and wind jump.
 * Phase 2 (60-25%): dives and wind jumps much more often.
 * Phase 3 (under 25%): faster, constant mace pressure, calls in 2 ItzBreadLoafs.
 */
public class MasterBreadLoafBrain extends LoafBrain {
	public static final int SLOT_SWORD = 0;
	public static final int SLOT_WIND_BURST_MACE = 1;
	public static final int SLOT_DENSITY_MACE = 2;
	public static final int SLOT_BREACH_MACE = 3;
	public static final int SLOT_WIND_CHARGE = 4;
	public static final int SLOT_ROCKET = 5;
	public static final int SLOT_PEARL = 6;
	public static final int SLOT_GOLDEN_APPLE = 7;
	public static final int SLOT_ELYTRA = 8;
	/** How high above the target he climbs before a dive. */
	public static final double DIVE_HEIGHT = 18.0;
	public static final double BOSS_BAR_RANGE = 64.0;
	public static final double HUNT_RANGE = 48.0;

	private static final Identifier PHASE3_SPEED_ID = Identifier.fromNamespaceAndPath("itzbreadloaf", "boss_phase3_speed");
	private static final Identifier DAMAGE_PRESET_ID = Identifier.fromNamespaceAndPath("itzbreadloaf", "boss_damage_preset");

	private enum State { GROUND, DRINK, WIND_JUMP, TAKEOFF, CLIMB, CRUISE, TELEGRAPH, DROP, RECOVER }

	private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
			net.minecraft.network.chat.Component.literal("Master BreadLoaf"),
			BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS).setDarkenScreen(true);
	private final Set<UUID> tookFallDamage = new HashSet<>();

	private State state = State.GROUND;
	private int stateTicks;
	private int specialCooldown = 100;
	private int pearlCooldown;
	private int lastRocketTick = -100;
	private int buffStep;
	private boolean summonedHelpers;
	private boolean drinkStarted;
	private int smashChain;
	private boolean telegraphed;
	private double diveTopY;

	public MasterBreadLoafBrain(LoafCharacter character) {
		super(character);
		this.combat.useShield = false;
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
		this.setBase(Attributes.KNOCKBACK_RESISTANCE, 0.5);
		AttributeInstance damage = this.player.getAttribute(Attributes.ATTACK_DAMAGE);
		if (damage != null) {
			damage.addOrReplacePermanentModifier(new AttributeModifier(DAMAGE_PRESET_ID, preset().damageMultiplier - 1.0,
					AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
	}

	@Override
	protected void rollGear() {
		ServerLevel level = this.player.level();
		this.player.setItemSlot(EquipmentSlot.HEAD, Gear.enchanted(level, Items.NETHERITE_HELMET, Enchantments.PROTECTION, 4));
		this.player.setItemSlot(EquipmentSlot.CHEST, Gear.enchanted(level, Items.NETHERITE_CHESTPLATE, Enchantments.PROTECTION, 4));
		this.player.setItemSlot(EquipmentSlot.LEGS, Gear.enchanted(level, Items.NETHERITE_LEGGINGS, Enchantments.PROTECTION, 4));
		this.player.setItemSlot(EquipmentSlot.FEET, Gear.enchanted(level, Items.NETHERITE_BOOTS, Enchantments.PROTECTION, 4));
		this.player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
		var inventory = this.player.getInventory();
		inventory.setItem(SLOT_SWORD, Gear.enchanted(level, Items.NETHERITE_SWORD, Enchantments.SHARPNESS, 5));
		inventory.setItem(SLOT_WIND_BURST_MACE, Gear.enchanted(level, Items.MACE, Enchantments.WIND_BURST, 2));
		inventory.setItem(SLOT_DENSITY_MACE, Gear.enchanted(level, Items.MACE, Enchantments.DENSITY, 5));
		inventory.setItem(SLOT_BREACH_MACE, Gear.enchanted(level, Items.MACE, Enchantments.BREACH, 4));
		inventory.setItem(SLOT_WIND_CHARGE, new ItemStack(Items.WIND_CHARGE, 64));
		inventory.setItem(SLOT_ROCKET, new ItemStack(Items.FIREWORK_ROCKET, 64));
		inventory.setItem(SLOT_PEARL, new ItemStack(Items.ENDER_PEARL, 16));
		inventory.setItem(SLOT_GOLDEN_APPLE, new ItemStack(Items.GOLDEN_APPLE, 3));
		inventory.setItem(SLOT_ELYTRA, new ItemStack(Items.ELYTRA));
		inventory.setItem(9, PotionContents.createItemStack(Items.POTION, Potions.STRONG_STRENGTH));
		inventory.setItem(10, PotionContents.createItemStack(Items.POTION, Potions.STRONG_SWIFTNESS));
		inventory.setItem(11, PotionContents.createItemStack(Items.POTION, Potions.LONG_FIRE_RESISTANCE));
		inventory.setItem(12, PotionContents.createItemStack(Items.SPLASH_POTION, Potions.STRONG_HEALING));
		inventory.setItem(13, PotionContents.createItemStack(Items.SPLASH_POTION, Potions.STRONG_HEALING));
		inventory.setSelectedSlot(SLOT_SWORD);
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

	/** 1, 2 or 3, from his health. */
	public int phase() {
		float fraction = this.player.getHealth() / this.player.getMaxHealth();
		return fraction > 0.6F ? 1 : fraction > 0.25F ? 2 : 3;
	}

	// ---------------------------------------------------------------- lifecycle

	@Override
	public void onActivated() {
		super.onActivated();
		// Despawned mid-dive: put the chestplate back on.
		if (this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
			int chest = Inv.find(this.player, stack -> stack.is(Items.NETHERITE_CHESTPLATE));
			if (chest >= 0) {
				ItemStack chestplate = this.player.getInventory().getItem(chest);
				this.player.getInventory().setItem(chest, this.player.getItemBySlot(EquipmentSlot.CHEST));
				this.player.setItemSlot(EquipmentSlot.CHEST, chestplate);
			}
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
		// Guaranteed: his Wind Burst mace and the trophy. Plus his drop table: extra Epic and Legendary rolls.
		this.player.spawnAtLocation(level, Gear.enchanted(level, Items.MACE, Enchantments.WIND_BURST, 2));
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
		if (this.specialCooldown > 0) {
			this.specialCooldown--;
		}
		if (this.pearlCooldown > 0) {
			this.pearlCooldown--;
		}
		if (this.ticks % 10 == 0 && (this.target == null || this.state == State.GROUND)) {
			this.chooseTarget();
		}
		LivingEntity target = this.target;
		if (target == null) {
			if (this.state != State.GROUND && this.state != State.RECOVER) {
				this.setState(State.RECOVER);
			}
			if (this.state == State.RECOVER) {
				this.recover();
				return;
			}
			this.controls.selectSlot(SLOT_SWORD);
			if (!this.tryEat()) {
				this.wander(10.0);
			}
			return;
		}
		this.phaseEffects(target);
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

	/** Buffs at the start of a fight, healing when low, phase 3 speed and helpers. */
	private void phaseEffects(LivingEntity target) {
		if (this.phase() == 3) {
			AttributeInstance speed = this.player.getAttribute(Attributes.MOVEMENT_SPEED);
			if (speed != null && !speed.hasModifier(PHASE3_SPEED_ID)) {
				speed.addPermanentModifier(new AttributeModifier(PHASE3_SPEED_ID, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
			}
			if (!this.summonedHelpers) {
				this.summonedHelpers = true;
				this.summonHelpers(target);
			}
		}
	}

	private void summonHelpers(LivingEntity target) {
		this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.5F, 0.8F);
		ServerLevel level = this.player.level();
		for (int i = 0; i < 2; i++) {
			Vec3 spot = this.findSpotNear(this.player.blockPosition().offset(i == 0 ? 2 : -2, 0, 1), 1);
			if (spot == null) {
				spot = this.player.position();
			}
			LoafCharacter helper = CharacterManager.create(level, CharacterKind.BREADLOAF, spot, this.player.getYRot(), brain -> brain.setWild(true));
			if (helper != null) {
				helper.brain.setTarget(target);
			}
		}
	}

	// ---------------------------------------------------------------- ground fighting

	private void ground(LivingEntity target) {
		double distance = this.player.distanceTo(target);
		int phase = this.phase();

		// Drink buffs (Strength II, Swiftness II, Fire Resistance) at the start of the fight.
		if (this.buffStep < 3 && distance > 5.0) {
			if (this.startDrinking(this.buffPotion(this.buffStep))) {
				this.buffStep++;
				return;
			}
			this.buffStep++;
		}
		// Low health: Splash Healing II at his feet, then golden apples.
		if (this.player.getHealth() < this.player.getMaxHealth() * 0.4F && this.stateTicks > 40) {
			if (this.throwHealing() || distance > 6.0 && this.startDrinking(stack -> stack.is(Items.GOLDEN_APPLE))) {
				return;
			}
		}
		// Far away (or out of walking reach): ender pearl.
		boolean unreachable = this.nav.isAtDeadEnd() || this.nav.failed();
		if ((distance > 24.0 || unreachable && distance > 6.0) && this.pearlCooldown <= 0 && this.throwPearl(target)) {
			return;
		}
		// Special moves.
		if (this.specialCooldown <= 0 && this.player.onGround()) {
			boolean dive = this.random.nextFloat() < (phase == 1 ? 0.5F : 0.65F) && this.hasRoomAbove(6) && distance < 40.0;
			if (dive) {
				this.setState(State.TAKEOFF);
				this.diveTopY = target.getY() + DIVE_HEIGHT;
			} else {
				this.setState(State.WIND_JUMP);
				this.smashChain = 0;
			}
			this.specialCooldown = phase == 1 ? 260 : phase == 2 ? 140 : 90;
			return;
		}
		this.controls.selectSlot(SLOT_SWORD);
		this.combat.fight(target, this.nav);
	}

	private Predicate<ItemStack> buffPotion(int step) {
		return switch (step) {
			case 0 -> stack -> stack.is(Items.POTION) && hasPotion(stack, Potions.STRONG_STRENGTH);
			case 1 -> stack -> stack.is(Items.POTION) && hasPotion(stack, Potions.STRONG_SWIFTNESS);
			default -> stack -> stack.is(Items.POTION) && hasPotion(stack, Potions.LONG_FIRE_RESISTANCE);
		};
	}

	private static boolean hasPotion(ItemStack stack, net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion) {
		PotionContents contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
		return contents != null && contents.is(potion);
	}

	/** Moves a drink (or food) into hotbar slot 8 and holds right-click until it's finished. */
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

	/** Splash Healing II thrown straight down at his own feet. */
	private boolean throwHealing() {
		int slot = Inv.ready(this.player, stack -> stack.is(Items.SPLASH_POTION), SLOT_GOLDEN_APPLE);
		if (slot < 0) {
			return false;
		}
		this.controls.selectSlot(slot);
		this.controls.lookInstantly(this.player.getYRot(), 90.0F);
		this.controls.useOnce();
		this.stateTicks = 0;
		this.particles(ParticleTypes.HEART, 5);
		return true;
	}

	private boolean throwPearl(LivingEntity target) {
		if (this.player.getInventory().getItem(SLOT_PEARL).isEmpty()) {
			return false;
		}
		this.controls.selectSlot(SLOT_PEARL);
		this.combat.aimProjectile(target, 1.5, 0.03);
		this.controls.useOnce();
		this.pearlCooldown = 120;
		this.playSound(SoundEvents.ENDER_PEARL_THROW, 1.0F, 0.6F);
		return true;
	}

	// ---------------------------------------------------------------- wind charge mace jump

	private void windJump(LivingEntity target) {
		if (this.stateTicks == 1) {
			this.controls.selectSlot(SLOT_WIND_CHARGE);
		}
		if (this.stateTicks <= 6) {
			// Look down, jump, throw the wind charge at our feet: up we go.
			this.controls.lookInstantly(Controls.yawTo(this.player.position(), target.position()), 90.0F);
			if (this.player.onGround() && this.stateTicks <= 2) {
				this.controls.jumpOnce();
			} else if (!this.player.onGround() && this.stateTicks >= 3 && this.stateTicks <= 5) {
				this.controls.useOnce();
				this.stateTicks = 6;
			}
			return;
		}
		this.airSmash(target, this.stateTicks > 12 && this.player.onGround());
	}

	/**
	 * In the air above or near the target: pick a mace for the fall, warn once at the top, drift toward them and
	 * smash on the way down. Wind Burst throws him back up after a hit, so he can chain smashes.
	 */
	private void airSmash(LivingEntity target, boolean landed) {
		Vec3 aim = this.combat.aimPoint(target);
		double distance = this.player.getEyePosition().distanceTo(aim);
		boolean falling = this.player.getDeltaMovement().y < 0.0 && !this.player.onGround();
		if (!this.telegraphed && falling) {
			this.telegraph(true);
		}
		this.selectMaceFor(target, this.player.getY() - target.getY());
		this.controls.aimAt(aim);
		this.controls.walkToward(target.position());
		this.controls.setSprinting(false);
		// Fairness: cap how far a smash counts as a fall (mace damage grows with the fall).
		if (this.player.fallDistance > preset().maxSmashFall) {
			this.player.fallDistance = preset().maxSmashFall;
		}
		if (falling && distance <= this.combat.reach() - 0.1 && this.player.fallDistance > 1.5
				&& this.player.getAttackStrengthScale(0.5F) >= 0.9F) {
			this.controls.attackOnce();
			this.smashChain++;
			this.telegraphed = false;
		}
		if (landed || this.stateTicks > 160) {
			this.setState(State.RECOVER);
		}
	}

	/** Breach against heavy armor, Density for big drops, Wind Burst otherwise. */
	public void selectMaceFor(LivingEntity target, double dropHeight) {
		int slot = target.getArmorValue() >= 15 ? SLOT_BREACH_MACE : dropHeight >= 6.0 ? SLOT_DENSITY_MACE : SLOT_WIND_BURST_MACE;
		if (this.player.getInventory().getItem(slot).isEmpty()) {
			slot = Inv.find(this.player, stack -> stack.is(Items.MACE));
			slot = Inv.toHotbar(this.player, slot, SLOT_WIND_BURST_MACE);
		}
		if (slot >= 0) {
			this.controls.selectSlot(slot);
		}
	}

	/** Warning before every smash so players can dodge: a charge-up sound and sparks. */
	private void telegraph(boolean withSound) {
		this.telegraphed = true;
		ServerLevel level = this.player.level();
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.player.getX(), this.player.getY(1.0), this.player.getZ(), 10, 0.5, 0.6, 0.5, 0.05);
		level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.player.getX(), this.player.getY(0.5), this.player.getZ(), 6, 0.4, 0.5, 0.4, 0.02);
		if (withSound) {
			this.playSound(SoundEvents.BREEZE_CHARGE, 1.5F, 0.7F);
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

	/** Glide to a point above the target, rocketing when slow or too low. */
	private void cruise(LivingEntity target) {
		if (!this.player.isFallFlying()) {
			this.setState(State.DROP);
			return;
		}
		Vec3 over = new Vec3(target.getX(), this.diveTopY, target.getZ());
		double horizontal = Math.sqrt(Math.pow(target.getX() - this.player.getX(), 2) + Math.pow(target.getZ() - this.player.getZ(), 2));
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

	/** Over the target: pull up to bleed speed, sparks and the charge-up sound, then drop. */
	private void telegraphOver(LivingEntity target) {
		this.controls.look(Controls.yawTo(this.player.position(), target.position()), -35.0F);
		if (this.stateTicks == 1) {
			this.telegraph(true);
		} else if (this.stateTicks % 4 == 0) {
			this.telegraph(false);
		}
		if (this.stateTicks >= 10 || !this.player.isFallFlying()) {
			this.setState(State.DROP);
		}
	}

	/** Chestplate back on (stops the glide), mace out, smash. */
	private void drop(LivingEntity target) {
		if (this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
			int chestplate = Inv.ready(this.player, stack -> stack.is(Items.NETHERITE_CHESTPLATE), SLOT_ELYTRA);
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

	/** After a special move: back to the sword (and the chestplate, if the elytra is still on). */
	private void recover() {
		if (this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA) && this.player.onGround()) {
			int chestplate = Inv.ready(this.player, stack -> stack.is(Items.NETHERITE_CHESTPLATE), SLOT_ELYTRA);
			if (chestplate >= 0 && this.stateTicks < 10) {
				this.controls.selectSlot(chestplate);
				this.controls.look(this.player.getYRot(), -60.0F);
				if (this.stateTicks % 3 == 1) {
					this.controls.useOnce();
				}
				return;
			}
		}
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
		tag.putBoolean("SummonedHelpers", this.summonedHelpers);
		tag.store("FallDamaged", UUIDUtil.CODEC_SET, Set.copyOf(this.tookFallDamage));
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.buffStep = tag.getIntOr("BuffStep", 0);
		this.summonedHelpers = tag.getBooleanOr("SummonedHelpers", false);
		this.tookFallDamage.clear();
		tag.read("FallDamaged", UUIDUtil.CODEC_SET).ifPresent(this.tookFallDamage::addAll);
	}
}
