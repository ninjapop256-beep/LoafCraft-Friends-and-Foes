package com.itzbreadloaf.brain;

import com.itzbreadloaf.ModAdvancements;
import com.itzbreadloaf.character.CharacterKind;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.CharacterVariant;
import com.itzbreadloaf.character.LoafCharacter;
import com.itzbreadloaf.character.LoafIds;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * FelixSyn: a homeless mace fighter who drifts around villages with his partner Harley. He hangs out at the bell and
 * the well, curls up on a hay bale (or the ground) at night, and trades nothing.
 *
 * Neutral to players: he only goes after whoever attacks him or Harley. He never starts a fight with an ItzBreadLoaf,
 * but geared ItzBreadLoafs (armor or a weapon) still pick fights with him outside villages, and then he fights back.
 * Tameable with cooked porkchops (1 in felixTameChance): then he follows and defends his owner like the others, and
 * ItzBreadLoafs leave him alone.
 *
 * Fights like a PvP mace player: wind charge at his feet, Density + Wind Burst mace smash on the way down, a sword
 * on the ground in between, golden apples when low.
 */
public class FelixBrain extends LoafBrain {
	public static final double RIVAL_RANGE = 24.0;
	public static final double HOME_RADIUS = 24.0;

	private final MaceTechniques techniques;
	private @Nullable BlockPos hangoutSpot;
	private int hangoutTicks;
	private int nextRivalMessage;
	private int nextDrifterCheck;
	// Voice lines (see FelixVoices for which recording plays when).
	private int nextIdleVoice = 1200;
	/** No line of any kind before this tick (the gap between lines). */
	private int nextVoiceTick;
	private int nextSpotVoice;
	private int nextVillageVoice;
	private int harleyApartSince;
	private boolean wasRetreating;
	private boolean sleepVoiced;
	/** The ItzBreadLoaf he's fighting, to celebrate when it goes down. */
	private @Nullable LivingEntity rivalTarget;

	public FelixBrain(LoafCharacter character) {
		super(character);
		this.techniques = new MaceTechniques(this.player, this.controls, this.combat, this.random);
		this.combat.critChance = 0.6F;
		this.combat.retreatBelow = (float) com.itzbreadloaf.config.ModConfig.get().felixRetreatHealth;
	}

	/**
	 * A wild FelixSyn (natural spawns and his own homes): rolled gear, and Harley only comes along sometimes
	 * (wildFelixWithHarleyChance, 20%).
	 */
	public static @Nullable LoafCharacter spawnWild(ServerLevel level, Vec3 pos, @Nullable BlockPos home, int radius, @Nullable BlockPos bed) {
		LoafCharacter felix = CharacterManager.create(level, CharacterVariant.WILD_FELIX, pos, level.getRandom().nextFloat() * 360.0F,
				brain -> {
					if (home != null) {
						brain.setupHome(home, radius, bed);
					}
				});
		if (felix != null && level.getRandom().nextFloat() < com.itzbreadloaf.config.ModConfig.get().wildFelixWithHarleyChance) {
			UUID felixId = felix.player.getUUID();
			CharacterManager.create(level, CharacterVariant.HARLEY, pos.add(1.0, 0.0, 0.0), level.getRandom().nextFloat() * 360.0F, brain -> {
				if (home != null) {
					brain.setupHome(home, radius, null);
				}
				if (brain instanceof HarleyBrain harley) {
					harley.setPartner(felixId);
				}
			});
		}
		return felix;
	}

	public boolean isWild() {
		return this.variant == CharacterVariant.WILD_FELIX;
	}

	/** FelixSyn with Harley at his side, always (the spawn egg, /loafcraft spawn felixsyn). */
	public static @Nullable LoafCharacter spawnDuo(ServerLevel level, Vec3 pos, @Nullable BlockPos home) {
		return spawnDuo(level, pos, home, false);
	}

	/**
	 * FelixSyn, and Harley with him: always, or for natural spawns (villages, structures) only felixHarleyChance of
	 * the time (20%), so most FelixSyns you meet are on their own.
	 */
	public static @Nullable LoafCharacter spawnDuo(ServerLevel level, Vec3 pos, @Nullable BlockPos home, boolean natural) {
		BlockPos homePos = home != null ? home : BlockPos.containing(pos);
		LoafCharacter felix = CharacterManager.create(level, CharacterVariant.FELIXSYN, pos, level.getRandom().nextFloat() * 360.0F,
				brain -> brain.setupHome(homePos, (int) HOME_RADIUS, null));
		if (felix == null) {
			return null;
		}
		if (natural && level.getRandom().nextDouble() >= com.itzbreadloaf.config.ModConfig.get().felixHarleyChance) {
			return felix;
		}
		UUID felixId = felix.player.getUUID();
		CharacterManager.create(level, CharacterVariant.HARLEY, pos.add(1.0, 0.0, 0.0), level.getRandom().nextFloat() * 360.0F, brain -> {
			brain.setupHome(homePos, (int) HOME_RADIUS, null);
			if (brain instanceof HarleyBrain harley) {
				harley.setPartner(felixId);
			}
		});
		return felix;
	}

	@Override
	protected String[] chatterLines() {
		return new String[]{"this mace never misses", "wind charges: check", "the village bell is my alarm clock",
				"where's Harley?", "ItzBreadLoaf better watch out", "*bounces on a wind charge*"};
	}

	@Override
	protected @Nullable String[] chatWith(LoafBrain other) {
		return other.kind() == com.itzbreadloaf.character.CharacterKind.HARLEY
				? new String[]{"ready to roam, partner?", "Harley, watch my back", "let's check the well"} : null;
	}

	@Override
	protected double baseMaxHealth() {
		return com.itzbreadloaf.config.ModConfig.get().felixHealth;
	}

	@Override
	protected void rollGear() {
		if (this.isWild()) {
			Gear.rollWildFelix(this.player, this.player.level(), this.random,
					this.player.level().getCurrentDifficultyAt(this.player.blockPosition()));
		} else {
			Kits.felixSyn(this.player, this.player.level(), this.random);
		}
	}

	@Override
	protected void restock() {
		if (Inv.count(this.player, Inv::isFood) < 4) {
			Inv.give(this.player, new ItemStack(Items.COOKED_PORKCHOP, 8));
		}
		if (Inv.count(this.player, stack -> stack.is(Items.WIND_CHARGE)) < 8) {
			Inv.give(this.player, new ItemStack(Items.WIND_CHARGE, 16));
		}
		// FelixSyn himself (not wild ones, and not once tamed, so nobody can farm them) keeps pearls and rockets topped up.
		if (this.variant == CharacterVariant.FELIXSYN && !this.isTame()) {
			if (Inv.count(this.player, stack -> stack.is(Items.ENDER_PEARL)) < 4) {
				Inv.give(this.player, new ItemStack(Items.ENDER_PEARL, 8));
			}
			if (Inv.count(this.player, stack -> stack.is(Items.FIREWORK_ROCKET)) < 8) {
				Inv.give(this.player, new ItemStack(Items.FIREWORK_ROCKET, 16));
			}
		}
	}

	/** His limits: 6 blocks of fall counted per smash (as before), chains of up to 3, failures by skill. */
	private void configureTechniques() {
		this.techniques.limits.maxFall = 6.0F;
		this.techniques.limits.maxChain = 3;
		this.techniques.limits.warnTicks = 0;
		this.techniques.limits.warn = null;
		this.techniques.limits.failChance = this.skill().techniqueFailChance;
	}

	// ---------------------------------------------------------------- voice

	/** Plays a line for the moment, unless he spoke in the last felixVoiceGapSeconds (so lines never pile up). */
	private void voice(com.itzbreadloaf.sound.FelixVoices.Moment moment) {
		if (this.ticks < this.nextVoiceTick) {
			return;
		}
		net.minecraft.sounds.SoundEvent sound = com.itzbreadloaf.sound.FelixVoices.random(moment, this.random);
		if (sound != null) {
			this.playSound(sound, (float) com.itzbreadloaf.config.ModConfig.get().felixVoiceVolume, 1.0F);
			this.nextVoiceTick = this.ticks + 20 * Math.max(1, com.itzbreadloaf.config.ModConfig.get().felixVoiceGapSeconds);
		}
	}

	@Override
	public @Nullable net.minecraft.sounds.SoundEvent hurtSound() {
		// Only now and then, and not right after another line; otherwise the normal hurt sound.
		if (this.ticks < this.nextVoiceTick || this.random.nextDouble() >= com.itzbreadloaf.config.ModConfig.get().felixHurtVoiceChance) {
			return null;
		}
		net.minecraft.sounds.SoundEvent sound = com.itzbreadloaf.sound.FelixVoices.random(com.itzbreadloaf.sound.FelixVoices.Moment.HURT, this.random);
		if (sound != null) {
			this.nextVoiceTick = this.ticks + 20 * Math.max(1, com.itzbreadloaf.config.ModConfig.get().felixVoiceGapSeconds);
		}
		return sound;
	}

	@Override
	public net.minecraft.sounds.SoundEvent deathSound() {
		net.minecraft.sounds.SoundEvent sound = com.itzbreadloaf.sound.FelixVoices.random(com.itzbreadloaf.sound.FelixVoices.Moment.DEATH, this.random);
		return sound != null ? sound : super.deathSound();
	}

	/** Lines that react to what's going on: spotting a player, Harley showing up, backing off, beating a rival. */
	private void tickVoice() {
		boolean retreating = this.combat.isRetreating();
		if (retreating && !this.wasRetreating) {
			this.voice(com.itzbreadloaf.sound.FelixVoices.Moment.RETREAT);
		}
		this.wasRetreating = retreating;
		if (this.rivalTarget != null && (!this.rivalTarget.isAlive() || this.rivalTarget.isRemoved())) {
			if (this.rivalTarget.isDeadOrDying() && this.rivalTarget.distanceTo(this.player) < 32.0) {
				this.voice(com.itzbreadloaf.sound.FelixVoices.Moment.WIN);
			}
			this.rivalTarget = null;
		}
		if (this.ticks % 20 != 0) {
			return;
		}
		HarleyBrain harley = this.partnerBrain();
		if (harley != null && harley.player.level() == this.player.level()) {
			double apart = harley.player.distanceTo(this.player);
			if (apart > 16.0) {
				this.harleyApartSince = this.harleyApartSince == 0 ? this.ticks : this.harleyApartSince;
			} else if (apart < 6.0 && this.harleyApartSince != 0 && this.ticks - this.harleyApartSince > 1200) {
				this.voice(com.itzbreadloaf.sound.FelixVoices.Moment.SEE_HARLEY);
				this.harleyApartSince = 0;
			} else if (apart < 6.0) {
				this.harleyApartSince = 0;
			}
		}
		if (this.target == null && this.ticks >= this.nextSpotVoice) {
			for (ServerPlayer viewer : this.player.level().players()) {
				if (LoafIds.isRealPlayer(viewer) && viewer.distanceTo(this.player) < 10.0 && this.player.hasLineOfSight(viewer)) {
					this.voice(com.itzbreadloaf.sound.FelixVoices.Moment.SPOT_PLAYER);
					this.nextSpotVoice = this.ticks + 2400;
					this.nextIdleVoice = Math.max(this.nextIdleVoice, this.ticks + 200);
					break;
				}
			}
		}
	}

	/** Targets someone and says the line for it (only when it's a new target). */
	private void targetWithVoice(LivingEntity target, com.itzbreadloaf.sound.FelixVoices.Moment moment) {
		if (target == this.target) {
			return;
		}
		this.setTarget(target);
		if (this.target == target) {
			this.voice(moment);
		}
	}

	@Override
	protected SoundSource soundSource() {
		return SoundSource.NEUTRAL;
	}

	@Override
	public int experienceReward() {
		return 20;
	}

	@Override
	public boolean allowDamage(DamageSource source, float amount) {
		// His own mace moves don't give him fall damage (his pearls still cost him 5, like anyone's).
		return !(this.techniques.isActive() && source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)
				&& !source.is(net.minecraft.world.damagesource.DamageTypes.ENDER_PEARL));
	}

	@Override
	public MaceTechniques techniques() {
		return this.techniques;
	}

	// ---------------------------------------------------------------- behavior

	@Override
	protected void tickBehavior() {
		this.techniques.tickCooldown();
		this.tickVoice();
		if (!this.isWild() && this.ticks >= this.nextDrifterCheck) {
			this.nextDrifterCheck = this.ticks + 40;
			for (ServerPlayer viewer : this.player.level().players()) {
				if (LoafIds.isRealPlayer(viewer) && viewer.distanceTo(this.player) < 12.0) {
					ModAdvancements.award(viewer, ModAdvancements.VILLAGE_DRIFTER);
				}
			}
		}
		if (this.ticks % 10 == 0) {
			this.chooseTarget();
		}
		if (this.target != null) {
			this.fight(this.target);
			return;
		}
		this.techniques.cancel();
		if (this.tryEat() || this.tickLove()) {
			return;
		}
		if (this.isTame()) {
			// Tamed: he goes where his owner goes (an empty-hand right-click makes him sit or stand).
			this.idleVoice();
			if (!this.followOwner()) {
				this.idleWithOwner();
			}
			return;
		}
		if (this.player.level().isDarkOutside()) {
			// Wild ones have a bed at home; the drifter sleeps rough.
			if (this.goToBed()) {
				return;
			}
			this.sleepRough();
			return;
		}
		this.controls.setSneaking(false);
		this.sleepVoiced = false;
		this.idleVoice();
		if (this.returnHome() || this.tickRoutine() || this.tickIdleActions()) {
			return;
		}
		this.hangOut();
	}

	/** Work hours: mace practice (wind-charge jumps out in the open, shadow-boxing in between). */
	@Override
	protected boolean doWork() {
		return this.activities.practiceMaceJump();
	}

	/** Friends time is his hangout (the village bell, the water): Harley comes along anyway. */
	@Override
	protected boolean socialize() {
		return false;
	}

	/** Now and then, one of his idle lines. */
	private void idleVoice() {
		if (this.ticks >= this.nextIdleVoice) {
			int min = Math.max(5, com.itzbreadloaf.config.ModConfig.get().felixIdleLineMinSeconds);
			int max = Math.max(min, com.itzbreadloaf.config.ModConfig.get().felixIdleLineMaxSeconds);
			this.nextIdleVoice = this.ticks + 20 * (min + this.random.nextInt(max - min + 1));
			this.voice(com.itzbreadloaf.sound.FelixVoices.Moment.IDLE);
		}
	}

	private void chooseTarget() {
		// Whoever hit him (not Harley, and not his owner or the owner's friends if he's tamed).
		LivingEntity attacker = this.lastAttacker;
		if (attacker != null && attacker.isAlive() && this.ticks - this.lastHurtTick < 200 && !this.isPartner(attacker)
				&& !this.isFriendlyWith(attacker) && !(attacker instanceof Player p && (p.isCreative() || p.isSpectator()))) {
			this.setTarget(attacker);
			if (this.target == attacker && LoafIds.kind(attacker.getUUID()) == CharacterKind.BREADLOAF) {
				this.rivalTarget = attacker;
			}
			return;
		}
		if (this.target != null) {
			return;
		}
		// Whoever attacks Harley.
		HarleyBrain harley = this.partnerBrain();
		if (harley != null && harley.lastAttacker != null && harley.lastAttacker.isAlive() && harley.ticks - harley.lastHurtTick < 200
				&& harley.lastAttacker != this.player) {
			this.targetWithVoice(harley.lastAttacker, com.itzbreadloaf.sound.FelixVoices.Moment.DEFEND);
			return;
		}
		// He never starts a fight with an ItzBreadLoaf: only when one attacks him (above) or Harley. Tamed, he also
		// stands up for his owner.
		if (this.isTame()) {
			this.protectOwner(true);
		}
	}

	/**
	 * The closest rival of that kind within 24 blocks in sight (skipping babies and ones ordered to sit). The rivalry
	 * is only between FelixSyn and ItzBreadLoafs that have gear, and never inside a village.
	 */
	public static @Nullable LivingEntity findRival(Player self, CharacterKind kind) {
		if (inVillage(self) || kind == CharacterKind.FELIXSYN && !hasGear(self)) {
			return null;
		}
		LivingEntity best = null;
		for (LoafCharacter other : CharacterManager.active()) {
			if (other.kind() != kind || other.brain == null || other.player.level() != self.level() || other.brain.isBaby()
					|| other.brain.isSitting() || other.player.distanceTo(self) > RIVAL_RANGE || !self.hasLineOfSight(other.player)
					|| kind == CharacterKind.BREADLOAF && !hasGear(other.player) || inVillage(other.player)
					|| kind == CharacterKind.FELIXSYN && other.brain.isTame()) {
				continue;
			}
			if (best == null || other.player.distanceTo(self) < best.distanceTo(self)) {
				best = other.player;
			}
		}
		return best;
	}

	/** In or right next to a village: vanilla villages (bells, beds, workstations nearby) or a LoafCraft Village. */
	public static boolean inVillage(LivingEntity entity) {
		if (!(entity.level() instanceof ServerLevel level)) {
			return false;
		}
		BlockPos pos = entity.blockPosition();
		return level.isVillage(pos) || level.structureManager().getStructureWithPieceAt(pos, holder -> holder.is(net.minecraft.tags.StructureTags.VILLAGE)
				|| holder.unwrapKey().map(key -> key.identifier().getNamespace().equals("itzbreadloaf")
						&& key.identifier().getPath().equals("village")).orElse(false)).isValid();
	}

	/** Wearing any armor, or holding a weapon (sword, axe, mace, trident, spear, bow or crossbow). */
	public static boolean hasGear(LivingEntity entity) {
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			if (!entity.getItemBySlot(slot).isEmpty()) {
				return true;
			}
		}
		ItemStack hand = entity.getMainHandItem();
		return hand.has(net.minecraft.core.component.DataComponents.WEAPON) || hand.is(Items.BOW) || hand.is(Items.CROSSBOW);
	}

	/**
	 * Whether a rival fight between FelixSyn and an ItzBreadLoaf may go on: not in a village, and the ItzBreadLoaf
	 * has gear. (Fighting back after being hit is always allowed; callers check that.)
	 */
	public static boolean rivalryAllowed(LivingEntity felix, LivingEntity bread) {
		return hasGear(bread) && !inVillage(felix) && !inVillage(bread);
	}

	/** "FelixSyn and ItzBreadLoaf are fighting!" for everyone watching (and the Rival Showdown advancement). */
	public static void announceRivalry(ServerLevel level, Vec3 where) {
		for (ServerPlayer viewer : level.players()) {
			if (LoafIds.isRealPlayer(viewer) && viewer.position().distanceTo(where) < 40.0) {
				viewer.displayClientMessage(Component.literal("FelixSyn and ItzBreadLoaf are fighting!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
				ModAdvancements.award(viewer, ModAdvancements.RIVAL_SHOWDOWN);
			}
		}
	}

	private void fight(LivingEntity target) {
		// A rival fight stops once it moves into a village (or the ItzBreadLoaf has no gear), unless it hit him just now.
		boolean selfDefense = target == this.lastAttacker && this.ticks - this.lastHurtTick < 200;
		if (LoafIds.kind(target.getUUID()) == CharacterKind.BREADLOAF && !selfDefense && !rivalryAllowed(this.player, target)) {
			this.setTarget(null);
			this.rivalTarget = null;
			return;
		}
		if (LoafIds.kind(target.getUUID()) == CharacterKind.BREADLOAF && this.ticks >= this.nextRivalMessage) {
			this.nextRivalMessage = this.ticks + 200;
			announceRivalry(this.player.level(), this.player.position());
		}
		if (this.player.distanceTo(target) > 40.0) {
			this.setTarget(null);
			return;
		}
		if (this.techniques.isActive()) {
			this.techniques.tick(target);
			return;
		}
		if (this.player.getHealth() < this.player.getMaxHealth() * 0.4F && this.eatNow(stack -> stack.is(Items.GOLDEN_APPLE))) {
			return;
		}
		if (this.player.getHealth() < this.player.getMaxHealth() * 0.3F && this.throwSplash(this.player, net.minecraft.world.item.alchemy.Potions.STRONG_HEALING)) {
			return;
		}
		double distance = this.player.distanceTo(target);
		// The aerialist: wind jumps and chains up close, pearl catches, diagonal pearls from range, elytra dives from far.
		if (this.techniques.isReady() && this.random.nextInt(4) == 0) {
			MaceTechniques.Technique move = this.techniques.choose(target, distance, this.playStyle());
			if (move != null) {
				this.configureTechniques();
				this.techniques.start(move, target, 100 + this.random.nextInt(60));
				this.voice(com.itzbreadloaf.sound.FelixVoices.Moment.MACE);
				this.techniques.tick(target);
				return;
			}
		}
		// On the ground: his sword.
		int sword = Inv.ready(this.player, stack -> stack.is(net.minecraft.tags.ItemTags.SWORDS), 0);
		if (sword >= 0) {
			this.controls.switchSlot(sword, this.skill().hotbarSwitchTicks);
		}
		this.combat.fight(target, this.nav);
	}

	/** Bell and well: he wanders between them (and around the village) during the day. */
	private void hangOut() {
		if (this.hangoutSpot == null || --this.hangoutTicks <= 0) {
			this.hangoutTicks = 300 + this.random.nextInt(400);
			BlockPos center = this.home != null ? this.home : this.player.blockPosition();
			this.hangoutSpot = switch (this.random.nextInt(3)) {
				case 0 -> this.findBlock(center, 24, 8, state -> state.is(Blocks.BELL));
				case 1 -> this.dryBesideWater(center);
				default -> null;
			};
		}
		// Greets the village when he gets to the bell.
		if (this.hangoutSpot != null && this.ticks >= this.nextVillageVoice
				&& this.player.level().getBlockState(this.hangoutSpot).is(Blocks.BELL)
				&& this.player.position().distanceTo(Vec3.atCenterOf(this.hangoutSpot)) <= 3.5) {
			this.voice(com.itzbreadloaf.sound.FelixVoices.Moment.VILLAGE);
			this.nextVillageVoice = this.ticks + 3600;
		}
		if (this.hangoutSpot != null && this.player.position().distanceTo(Vec3.atCenterOf(this.hangoutSpot)) > 3.0) {
			this.nav.moveTo(this.hangoutSpot, 2.5, Navigator.Speed.WALK);
			return;
		}
		this.wander(HOME_RADIUS * 0.6);
	}

	/**
	 * A dry spot right beside water (the well, a pond) to hang out at. It used to walk to the water block itself,
	 * which is how FelixSyn and Harley ended up stuck in village wells.
	 */
	private @Nullable BlockPos dryBesideWater(BlockPos center) {
		BlockPos water = this.findBlock(center, 24, 6, state -> state.getFluidState().isSource() && state.is(Blocks.WATER));
		if (water == null) {
			return null;
		}
		ServerLevel level = this.player.level();
		for (int radius = 2; radius <= 4; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
						continue;
					}
					for (int dy = -1; dy <= 2; dy++) {
						BlockPos feet = water.offset(dx, dy, dz);
						BlockPos below = feet.below();
						if (level.getFluidState(feet).isEmpty() && level.getFluidState(below).isEmpty()
								&& level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir()
								&& level.getBlockState(below).isFaceSturdy(level, below, net.minecraft.core.Direction.UP)) {
							return feet;
						}
					}
				}
			}
		}
		return null;
	}

	/** Night: no bed, so he curls up on a hay bale if there is one nearby, otherwise right where he is. */
	private void sleepRough() {
		if (!this.sleepVoiced) {
			this.sleepVoiced = true;
			this.voice(com.itzbreadloaf.sound.FelixVoices.Moment.SLEEP);
		}
		BlockPos center = this.home != null ? this.home : this.player.blockPosition();
		BlockPos hay = this.findBlock(center, 20, 6, state -> state.is(Blocks.HAY_BLOCK));
		if (hay != null && this.player.position().distanceTo(Vec3.atBottomCenterOf(hay.above())) > 1.2) {
			this.nav.moveTo(hay.above(), 0.8, Navigator.Speed.WALK);
			return;
		}
		this.nav.stop();
		this.controls.setSneaking(true);
		this.controls.look(this.player.getYRot(), 40.0F);
	}

	// ---------------------------------------------------------------- Harley

	public boolean isPartner(@Nullable LivingEntity entity) {
		LoafBrain brain = CharacterManager.brain(entity);
		return brain instanceof HarleyBrain harley && this.player.getUUID().equals(harley.partner());
	}

	private @Nullable HarleyBrain partnerBrain() {
		for (LoafCharacter other : CharacterManager.active()) {
			if (other.brain instanceof HarleyBrain harley && this.player.getUUID().equals(harley.partner())) {
				return harley;
			}
		}
		return null;
	}

	// ---------------------------------------------------------------- death and drops

	/**
	 * Taming: cooked porkchops (his favorite), 1 in felixTameChance, only while he isn't fighting. Tamed, an
	 * empty-hand right-click from his owner makes him sit or stand.
	 */
	@Override
	protected @Nullable net.minecraft.world.InteractionResult interact(ServerPlayer clicker, ItemStack stack) {
		if (!this.isTame()) {
			if (stack.is(Items.COOKED_PORKCHOP) && this.target == null && !this.baby) {
				ItemStack food = stack.copyWithCount(1);
				stack.consume(1, clicker);
				Inv.give(this.player, food);
				if (this.random.nextInt(Math.max(1, com.itzbreadloaf.config.ModConfig.get().felixTameChance)) == 0) {
					this.setOwner(clicker);
					ModAdvancements.award(clicker, ModAdvancements.WILD_CARD);
					this.setTarget(null);
					this.particles(net.minecraft.core.particles.ParticleTypes.HEART, 8);
					this.say(clicker, "alright, alright. you're cool. I'm with you now");
				} else {
					this.particles(net.minecraft.core.particles.ParticleTypes.SMOKE, 6);
				}
				return net.minecraft.world.InteractionResult.SUCCESS;
			}
			return null;
		}
		if (this.isOwnedBy(clicker) && stack.isEmpty()) {
			this.setSitting(!this.sitting);
			this.setTarget(null);
			return net.minecraft.world.InteractionResult.SUCCESS;
		}
		return null;
	}

	/** Drops some of his loot (not his whole kit), and lets Harley know who did it. Tamed, he drops his inventory. */
	@Override
	public boolean onDeath(DamageSource source) {
		HarleyBrain harley = this.partnerBrain();
		if (harley != null && source.getEntity() instanceof LivingEntity killer) {
			harley.onPartnerKilled(killer);
		}
		if (this.isTame()) {
			return super.onDeath(source);
		}
		ServerLevel level = this.player.level();
		// Loot rarity system: his drop table + tier rolls, armor 8.5% each. Sometimes some of his wind charges
		// (25%), and super rarely his own mace (maceDropChance, 0.8%).
		com.itzbreadloaf.loot.LootRarity.dropCharacterLoot(this, this.player, source);
		com.itzbreadloaf.loot.LootRarity.dropGear(this.player, this.random, false);
		boolean windCharges = this.random.nextFloat() < 0.25F;
		boolean mace = com.itzbreadloaf.loot.LootRarity.maceDrops(this.random);
		for (int i = 0; i < 36; i++) {
			ItemStack stack = this.player.getInventory().getItem(i);
			if (windCharges && stack.is(Items.WIND_CHARGE)) {
				this.player.spawnAtLocation(level, stack.copyWithCount(Math.max(1, stack.getCount() / 2)));
				windCharges = false;
			} else if (mace && stack.is(Items.MACE)) {
				this.player.spawnAtLocation(level, stack.copy());
				mace = false;
			}
		}
		this.player.getInventory().clearContent();
		return false;
	}

	@Override
	public void save(CompoundTag tag) {
		super.save(tag);
		if (this.hangoutSpot != null) {
			tag.store("Hangout", BlockPos.CODEC, this.hangoutSpot);
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.hangoutSpot = tag.read("Hangout", BlockPos.CODEC).orElse(null);
	}


	// ---------------------------------------------------------------- personality (AI overhaul part 6)

	/** FelixSyn: a cool show-off who lives for mace jumps. */
	@Override
	protected String[] lines(com.itzbreadloaf.brain.ai.Situation situation) {
		return switch (situation) {
			case GREET_PLAYER -> new String[]{"yo, what's good?", "ayy, look who it is", "sup!", "hey hey!"};
			case GREET_FRIEND -> new String[]{"there you are, %s", "sup %s", "%s! ready to roll?"};
			case FIGHT_START -> new String[]{"let's dance!", "you sure about this?", "alright, let's go"};
			case RIVAL -> new String[]{"ready for round two, BreadLoaf?", "watch the skies, toast boy!", "I'll land on you like that loaf!", "say goodbye to your crumbs!"};
			case FIGHT_WON -> new String[]{"too easy!", "that's how it's done!", "gg, better luck next time"};
			case LOW_HEALTH -> new String[]{"okay, okay, you're good!", "need a sec...", "whoa, not bad!"};
			case DEFEND_FRIEND -> new String[]{"hey! nobody messes with Harley!", "you wanna go? come on then!"};
			case SCARED_FOR_FRIEND -> new String[]{"you good?!", "hey, hang in there!"};
			case REST -> new String[]{"that was close...", "*stretches* alright, alright"};
			case MORNING -> new String[]{"morning! perfect wind today", "time to fly"};
			case NIGHT -> new String[]{"stars are out", "good night for a jump"};
			case RAIN -> new String[]{"rain? perfect for a smash", "ugh, slippery"};
			case ITEM -> new String[]{"nice %s!", "where'd you get that %s?", "is that a %s? cool"};
			case WATCH -> new String[]{"need some help with that?", "looking good", "not bad, not bad"};
		};
	}

	@Override
	protected String[] moodLines(com.itzbreadloaf.brain.ai.Mood mood) {
		return switch (mood) {
			case HAPPY -> new String[]{"feeling light today!", "*bounces on his toes*"};
			case BORED -> new String[]{"anyone up for some mace practice?", "*spins his mace*", "so... quiet..."};
			case TIRED -> new String[]{"*yawns*", "long day"};
			case CURIOUS -> new String[]{"hm? what's that?", "huh, interesting"};
			case SCARED -> new String[]{"whoa, whoa!", "okay, that's a lot"};
			case ANGRY -> new String[]{"alright, now I'm annoyed", "you asked for it!"};
		};
	}
}
