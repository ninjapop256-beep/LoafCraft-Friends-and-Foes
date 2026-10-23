package com.itzbreadloaf.brain;

import com.itzbreadloaf.ModAdvancements;
import com.itzbreadloaf.ModMessages;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.CharacterVariant;
import com.itzbreadloaf.character.LoafCharacter;
import com.itzbreadloaf.character.LoafIds;
import com.itzbreadloaf.sound.ModSounds;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * ItzBreadLoaf and his types. Hostile until tamed (steak, 1 in 3): hunts real players within 32 blocks, fights like
 * a PvP player, pillars and bridges toward targets he can't walk to and digs through soft walls (mobGriefing), lives
 * in his home and sleeps in his bed at night. Group mates (tents, forts, camps, ships) defend each other.
 *
 * Types:
 * - Knight: iron armor, sword and shield, patrols close to the fort. Captain: the fort's mini-boss (40 health,
 *   netherite chestplate, diamond axe, golden apples, a banner on his head).
 * - Archer: bow and arrows, keeps his distance, stone sword up close. Lives in a watchtower.
 * - Miner: pickaxe, places torches in the dark, tunnels through stone toward his target. Found underground.
 * - Pirate: trident, swims fast and breathes underwater. Lives on shipwrecks.
 * - Nether: gold armor (piglins leave him alone), never burns. Found in the Nether.
 * - Sweaty: rare. Diamond gear, throws pearls to catch you and eats golden apples when low.
 * - Bandit: camps in the woods; at night the camp raids villages (villagers and golems too).
 * - Village: lives in a LoafCraft village, never hunts players, fights off monsters near the village.
 */
public class BreadLoafBrain extends LoafBrain {
	public static final float STEAK_FEED_HEAL = 4.0F;
	public static final double HUNT_RANGE = 32.0;

	/** Shared by everyone living in the same tent, fort, camp or ship. They defend each other. */
	private @Nullable UUID groupId;
	private int nextTauntTick;
	private int nextAmbientTick = 200;
	private int nextCritVoiceTick;
	private final DailyDuties duties = new DailyDuties(this);
	/** The player (or character) he's fighting, to cheer when they go down. */
	private @Nullable LivingEntity duelTarget;
	private int buildTicks;
	private int lostSightTicks;
	private int pearlCooldown;
	private int torchCooldown;
	private final Fishing fishing = new Fishing(this);

	public BreadLoafBrain(LoafCharacter character) {
		super(character);
		// Scrappy: only backs off when it's really bad.
		this.combat.retreatBelow = (float) com.itzbreadloaf.config.ModConfig.get().breadRetreatHealth;
	}

	// ---------------------------------------------------------------- gear and supplies

	@Override
	protected double baseMaxHealth() {
		return this.variantHealth() * com.itzbreadloaf.config.ModConfig.get().breadHealthMultiplier;
	}

	private double variantHealth() {
		return switch (this.variant) {
			case CAPTAIN -> 40.0;
			case KNIGHT, SWEATY -> 24.0;
			default -> 20.0;
		};
	}

	@Override
	protected void rollGear() {
		ServerLevel level = this.player.level();
		if (this.variant == CharacterVariant.BREADLOAF) {
			Gear.rollBreadLoaf(this.player, level, this.random, level.getCurrentDifficultyAt(this.player.blockPosition()));
		} else {
			Kits.breadLoaf(this.player, level, this.variant, this.random);
		}
	}

	@Override
	protected void restock() {
		if (this.baby) {
			return;
		}
		Kits.passiveEffects(this.player, this.variant);
		if (Inv.count(this.player, Inv::isFood) < 4) {
			Inv.give(this.player, new ItemStack(this.variant == CharacterVariant.PIRATE ? Items.COOKED_COD
					: this.variant == CharacterVariant.NETHER ? Items.COOKED_PORKCHOP : Items.BREAD, 8));
		}
		// Building blocks for pillars and bridges.
		if (Inv.count(this.player, Inv::isBuildingBlock) < 16) {
			Item block = switch (this.variant) {
				case NETHER -> Items.NETHERRACK;
				case PIRATE -> Items.OAK_PLANKS;
				case MINER -> Items.COBBLED_DEEPSLATE;
				default -> this.random.nextBoolean() ? Items.COBBLESTONE : Items.DIRT;
			};
			Inv.give(this.player, new ItemStack(block, 32));
		}
		if (Inv.find(this.player, s -> s.is(Items.BOW) || s.is(Items.CROSSBOW)) >= 0 && Inv.count(this.player, s -> s.is(ItemTags.ARROWS)) < 16) {
			Inv.give(this.player, new ItemStack(Items.ARROW, this.variant == CharacterVariant.ARCHER ? 64 : 16));
		}
		if (this.variant == CharacterVariant.MINER && Inv.count(this.player, s -> s.is(Items.TORCH)) < 8) {
			Inv.give(this.player, new ItemStack(Items.TORCH, 16));
		}
	}

	@Override
	public Item breedFood() {
		return Items.COOKED_BEEF;
	}

	@Override
	public boolean isFriendlyToMobs() {
		return this.isTame();
	}

	@Override
	public boolean isAngry() {
		return this.target != null;
	}

	@Override
	protected SoundSource soundSource() {
		return this.isTame() || this.variant == CharacterVariant.VILLAGER || this.variant == CharacterVariant.FISHERMAN ? SoundSource.NEUTRAL : SoundSource.HOSTILE;
	}

	@Override
	public SoundEvent deathSound() {
		return ModSounds.DEATH;
	}

	@Override
	public @Nullable SoundEvent hurtSound() {
		return ModSounds.HURT;
	}

	@Override
	public int experienceReward() {
		if (this.baby) {
			return 0;
		}
		return switch (this.variant) {
			case CAPTAIN -> 40;
			case SWEATY -> 25;
			default -> 5 + this.random.nextInt(4);
		};
	}

	/** Better weapons and armor, food, arrows, building blocks and his own thrown trident. */
	@Override
	public boolean wantsToPickUp(ItemStack stack) {
		if (this.baby || this.isEating()) {
			return false;
		}
		return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(Items.TRIDENT) || stack.is(Items.SHIELD)
				|| stack.has(DataComponents.EQUIPPABLE) && stack.get(DataComponents.EQUIPPABLE).slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR
				|| Inv.isFood(stack) || stack.is(ItemTags.ARROWS)
				|| Inv.isBuildingBlock(stack) && Inv.count(this.player, Inv::isBuildingBlock) < 64
				|| this.variant == CharacterVariant.MINER && (stack.is(ItemTags.PICKAXES) || stack.is(Items.RAW_IRON) || stack.is(Items.COAL))
				|| this.variant == CharacterVariant.FISHERMAN && (this.fishing.isCollecting() || stack.is(ItemTags.FISHES));
	}

	@Override
	public boolean onDeath(DamageSource source) {
		if (this.variant == CharacterVariant.CAPTAIN && source.getEntity() instanceof ServerPlayer killer && LoafIds.isRealPlayer(killer)) {
			ModAdvancements.award(killer, ModAdvancements.STORM_THE_FORT);
		}
		return super.onDeath(source);
	}

	// ---------------------------------------------------------------- groups

	public void setGroupId(@Nullable UUID groupId) {
		this.groupId = groupId;
	}

	public boolean isTentMate(LoafBrain other) {
		return this.groupId != null && other instanceof BreadLoafBrain bread && this.groupId.equals(bread.groupId);
	}

	// ---------------------------------------------------------------- behavior

	@Override
	protected void tickBehavior() {
		if (this.pearlCooldown > 0) {
			this.pearlCooldown--;
		}
		if (this.torchCooldown > 0) {
			this.torchCooldown--;
		}
		if (this.ticks % 40 == 0 && !this.baby) {
			this.tidyInventory();
		}
		if (this.ticks >= this.nextAmbientTick) {
			// Voice lines, so less often than the old meow (every 20-45 seconds), and only when not fighting.
			this.nextAmbientTick = this.ticks + 400 + this.random.nextInt(500);
			if (!this.player.isSleeping() && this.target == null) {
				this.playSound(ModSounds.AMBIENT, 1.0F, 1.0F);
			}
		}
		if (this.isTame() && !this.baby && this.target == null) {
			this.duties.tick();
		}
		if (this.combat.onCrit == null) {
			this.combat.onCrit = () -> {
				if (this.ticks >= this.nextCritVoiceTick && this.random.nextInt(3) == 0) {
					this.playSound(ModSounds.CRIT, 1.0F, 1.0F);
					this.nextCritVoiceTick = this.ticks + 100;
				}
			};
		}
		if (this.duelTarget != null && (!this.duelTarget.isAlive() || this.duelTarget.isRemoved())) {
			if (this.duelTarget.isDeadOrDying() && this.duelTarget.distanceTo(this.player) < 32.0 && !this.baby) {
				this.playSound(ModSounds.END_DUEL, 1.0F, 1.0F);
			}
			this.duelTarget = null;
		}
		if (this.ticks % 10 == 0) {
			this.chooseTarget();
		}
		if (this.target != null) {
			this.fight();
			return;
		}
		this.builder.stopDigging();
		this.buildTicks = 0;
		if (this.tryEat()) {
			return;
		}
		if (this.tickLove() || this.followParent()) {
			return;
		}
		if (this.fetchTrident()) {
			return;
		}
		if (this.variant == CharacterVariant.MINER && this.placeTorch()) {
			return;
		}
		if (this.variant == CharacterVariant.FISHERMAN && !this.isTame()
				&& this.fishing.tick(this.home != null ? this.home : this.player.blockPosition(), 1)) {
			return;
		}
		if (this.isTame()) {
			if (!this.followOwner()) {
				this.lookAround(8.0);
			}
			return;
		}
		if (this.goToBed() || this.returnHome()) {
			return;
		}
		double radius = switch (this.variant) {
			case KNIGHT, CAPTAIN -> 6.0;
			case ARCHER -> 2.0;
			default -> this.home != null ? this.homeRadius : 12.0;
		};
		this.wander(radius);
		if (this.ticks % 40 == 0) {
			this.provokeGolems();
		}
	}

	/** Puts on better armor, keeps a shield in the offhand (swap-hands key). */
	private void tidyInventory() {
		Gear.equipBestArmor(this.player);
		if (this.player.getOffhandItem().isEmpty() && !this.player.isUsingItem()) {
			int shield = Inv.ready(this.player, stack -> stack.is(Items.SHIELD), 6);
			if (shield >= 0) {
				this.controls.selectSlot(shield);
				this.controls.swapHands();
			}
		}
	}

	private void chooseTarget() {
		if (this.baby) {
			this.setTarget(null);
			return;
		}
		if (this.isTame()) {
			this.protectOwner(true);
			this.retaliate();
			this.fightRival();
			return;
		}
		this.retaliate();
		if (this.target != null) {
			return;
		}
		if (this.fightRival()) {
			return;
		}
		ServerLevel level = this.player.level();
		if (this.variant == CharacterVariant.VILLAGER || this.variant == CharacterVariant.FISHERMAN) {
			// Peaceful workers: they never hunt players. Village ones fight off monsters.
			if (this.variant == CharacterVariant.VILLAGER) {
				this.defendVillage();
			}
			return;
		}
		if (level.getDifficulty() == Difficulty.PEACEFUL || this.player.isSleeping()) {
			return;
		}
		Player nearest = level.getNearestPlayer(this.player.getX(), this.player.getY(), this.player.getZ(), HUNT_RANGE, p ->
				p != this.player && p instanceof Player pl && LoafIds.isRealPlayer(pl) && !pl.isSpectator() && !pl.isCreative()
						&& (this.player.hasLineOfSight(pl) || this.player.distanceTo(pl) < 12.0));
		if (nearest != null) {
			this.setTarget(nearest);
			return;
		}
		if (this.variant == CharacterVariant.BANDIT) {
			this.raid();
		}
	}

	/** FelixSyn within 24 blocks in sight: they always fight (even tamed ones, unless told to sit). */
	private boolean fightRival() {
		if (this.target != null) {
			return false;
		}
		LivingEntity rival = FelixBrain.findRival(this.player, com.itzbreadloaf.character.CharacterKind.FELIXSYN);
		if (rival == null) {
			return false;
		}
		this.setTarget(rival);
		if (this.target == rival) {
			this.playSound(ModSounds.START_DUEL, 1.0F, 1.0F);
		}
		FelixBrain.announceRivalry(this.player.level(), this.player.position());
		return true;
	}

	/** Village ItzBreadLoafs fight monsters that come near (not creepers, he's not silly). */
	private void defendVillage() {
		List<LivingEntity> monsters = this.player.level().getEntitiesOfClass(LivingEntity.class, this.player.getBoundingBox().inflate(16.0),
				entity -> entity instanceof Enemy && !(entity instanceof Creeper) && entity.isAlive() && CharacterManager.brain(entity) == null
						&& this.player.hasLineOfSight(entity));
		LivingEntity closest = null;
		for (LivingEntity monster : monsters) {
			if (closest == null || monster.distanceTo(this.player) < closest.distanceTo(this.player)) {
				closest = monster;
			}
		}
		if (closest != null) {
			this.setTarget(closest);
		}
	}

	/** Bandits go after villagers and golems too: far at night (raids), close during the day. */
	private void raid() {
		boolean night = !this.player.level().isBrightOutside();
		double range = night ? 64.0 : 20.0;
		List<LivingEntity> victims = this.player.level().getEntitiesOfClass(LivingEntity.class, this.player.getBoundingBox().inflate(range),
				entity -> (entity instanceof AbstractVillager || entity instanceof IronGolem) && entity.isAlive());
		LivingEntity closest = null;
		for (LivingEntity victim : victims) {
			if (closest == null || victim.distanceTo(this.player) < closest.distanceTo(this.player)) {
				closest = victim;
			}
		}
		if (closest != null) {
			this.setTarget(closest);
		}
	}

	/** Fights back against whoever hit him in the last 10 seconds. */
	private void retaliate() {
		LivingEntity attacker = this.lastAttacker;
		if (attacker == null || !attacker.isAlive() || this.ticks - this.lastHurtTick > 200 || attacker == this.target
				|| this.isFriendlyWith(attacker) || attacker.distanceTo(this.player) > HUNT_RANGE
				|| attacker instanceof Player p && (p.isCreative() || p.isSpectator())) {
			return;
		}
		LoafBrain other = CharacterManager.brain(attacker);
		if (other != null && this.isTentMate(other)) {
			return;
		}
		this.setTarget(attacker);
	}

	@Override
	protected void onNewTarget(LivingEntity target) {
		super.onNewTarget(target);
		this.fishing.stop();
		this.duelTarget = target instanceof Player ? target : null;
		// Taunt when he spots a new player.
		if (target instanceof ServerPlayer player && LoafIds.isRealPlayer(player) && this.ticks >= this.nextTauntTick && !this.isTame()) {
			this.playSound(ModSounds.TAUNT, 1.0F, 1.0F);
			this.nextTauntTick = this.ticks + 200;
			String line = switch (this.variant) {
				case KNIGHT, CAPTAIN -> ModMessages.pick(this.random, "halt! intruder!", "for the fort!", "you shall not pass!");
				case PIRATE -> ModMessages.pick(this.random, "arr, a landlubber!", "walk the plank!", "yer treasure is mine!");
				case BANDIT -> ModMessages.pick(this.random, "hand over the loot!", "nice stuff you got there...", "get 'em!");
				case SWEATY -> ModMessages.pick(this.random, "1v1 me", "ez", "you're not ready");
				default -> ModMessages.pick(this.random, "get over here!", "found you!", "you're mine!", "there you are!");
			};
			ModMessages.say(player, this.player, line);
		}
	}

	private void fight() {
		LivingEntity target = this.target;
		if (target == null) {
			return;
		}
		double distance = this.player.distanceTo(target);
		if (this.player.hasLineOfSight(target)) {
			this.lostSightTicks = 0;
		} else if (++this.lostSightTicks > 300 || distance > 48.0) {
			this.setTarget(null);
			return;
		}
		if (this.isTame() && this.isOwnedBy(target)) {
			this.setTarget(null);
			return;
		}
		// Golden apples when low (Sweaty, Captain); everyone else eats normal food with some room.
		if (this.player.getHealth() < this.player.getMaxHealth() * 0.5F
				&& this.eatNow(stack -> stack.is(Items.GOLDEN_APPLE))) {
			return;
		}
		if (this.player.getHealth() < this.player.getMaxHealth() * 0.35F && distance > 4.0 && this.tryEat()) {
			return;
		}
		// Sweaty: pearl in when they run.
		if (this.variant == CharacterVariant.SWEATY && distance > 14.0 && this.pearlCooldown <= 0 && this.throwPearl(target)) {
			return;
		}
		if (this.buildOrDigToward(target)) {
			return;
		}
		this.builder.stopDigging();
		this.fightTarget(true);
	}

	private boolean throwPearl(LivingEntity target) {
		int slot = Inv.ready(this.player, stack -> stack.is(Items.ENDER_PEARL), 5);
		if (slot < 0) {
			return false;
		}
		this.combat.stop();
		this.controls.selectSlot(slot);
		this.combat.aimProjectile(target, 1.5, 0.03);
		this.controls.useOnce();
		this.pearlCooldown = 100;
		return true;
	}

	/**
	 * When walking can't reach the target: pillar up to them, bridge across a gap, or dig through a soft wall.
	 * Real blocks from his inventory, real placing and mining. Only with mobGriefing on.
	 * Miners dig through stone too, and dig as soon as the path is blocked.
	 */
	private boolean buildOrDigToward(LivingEntity target) {
		boolean stuck = this.nav.isAtDeadEnd() || this.nav.failed() || this.variant == CharacterVariant.MINER && this.nav.isPartial();
		if (!stuck || !Builder.allowed(this.player.level())) {
			this.buildTicks = 0;
			return false;
		}
		if (++this.buildTicks > 400) {
			this.buildTicks = 0;
			this.nav.retry();
			return false;
		}
		Vec3 pos = this.player.position();
		double dy = target.getY() - pos.y;
		double horizontal = Math.sqrt(Math.pow(target.getX() - pos.x, 2) + Math.pow(target.getZ() - pos.z, 2));
		Direction toward = Direction.getApproximateNearest(target.getX() - pos.x, 0.0, target.getZ() - pos.z);
		this.controls.lookAt(target.getEyePosition());

		if (dy >= 1.5 && horizontal < 4.0 && this.builder.hasBlocks()) {
			this.combat.stop();
			if (this.builder.pillar()) {
				if (this.player.getY() >= target.getY() - 0.5) {
					this.nav.retry();
				}
				return true;
			}
		}
		BlockPos feet = this.player.blockPosition();
		BlockPos ahead = feet.relative(toward);
		ServerLevel level = this.player.level();
		boolean gap = level.getBlockState(ahead.below()).canBeReplaced() && level.getBlockState(ahead).canBeReplaced();
		if (gap && dy > -3.0 && this.builder.hasBlocks() && this.player.onGround()) {
			this.combat.stop();
			if (this.builder.bridge(toward)) {
				return true;
			}
		}
		// Miners hold their pickaxe to dig.
		if (this.variant == CharacterVariant.MINER) {
			int pickaxe = Inv.ready(this.player, stack -> stack.is(ItemTags.PICKAXES), 0);
			if (pickaxe >= 0) {
				this.controls.selectSlot(pickaxe);
			}
		}
		for (BlockPos block : new BlockPos[]{ahead.above(), ahead}) {
			BlockState state = level.getBlockState(block);
			if (!state.canBeReplaced() && !state.getCollisionShape(level, block).isEmpty()) {
				this.combat.stop();
				if (this.builder.dig(block)) {
					return true;
				}
			}
		}
		if (this.builder.isDigging()) {
			this.builder.stopDigging();
			this.nav.retry();
		}
		return false;
	}

	/** Miners light up dark spots: a torch on the floor (right-click the top of the block below). */
	private boolean placeTorch() {
		if (this.torchCooldown > 0) {
			return false;
		}
		this.torchCooldown = 60;
		ServerLevel level = this.player.level();
		BlockPos feet = this.player.blockPosition();
		if (level.getBrightness(LightLayer.BLOCK, feet) > 4 || level.getBrightness(LightLayer.SKY, feet) > 6
				|| !level.getBlockState(feet).isAir() || !level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)) {
			return false;
		}
		return this.useItemOn(feet.below(), Direction.UP, stack -> stack.is(Items.TORCH));
	}

	/** Walks over to a trident he threw and picks it up. */
	private boolean fetchTrident() {
		if (Inv.find(this.player, stack -> stack.is(Items.TRIDENT)) >= 0) {
			return false;
		}
		ThrownTrident trident = this.combat.findThrownTrident();
		if (trident == null || trident.distanceTo(this.player) > 40.0) {
			return false;
		}
		this.nav.moveTo(trident.position(), 0.5, Navigator.Speed.SPRINT);
		return true;
	}

	/** Iron golems go after untamed hostile ItzBreadLoafs near them. */
	private void provokeGolems() {
		if (this.isTame() || this.baby || this.variant == CharacterVariant.VILLAGER) {
			return;
		}
		for (IronGolem golem : this.player.level().getEntitiesOfClass(IronGolem.class, this.player.getBoundingBox().inflate(16.0),
				golem -> golem.getTarget() == null && !golem.isPlayerCreated())) {
			golem.setTarget(this.player);
		}
	}

	@Override
	public void onHurt(DamageSource source, float damage, boolean blocked) {
		super.onHurt(source, damage, blocked);
		// Group mates come to help.
		if (this.groupId != null && !this.isTame() && source.getEntity() instanceof LivingEntity attacker && attacker != this.player) {
			LoafBrain attackerBrain = CharacterManager.brain(attacker);
			if (attackerBrain != null && this.isTentMate(attackerBrain)) {
				return;
			}
			for (LoafCharacter character : CharacterManager.active()) {
				if (character.brain instanceof BreadLoafBrain mate && mate != this && this.isTentMate(mate) && !mate.isTame()
						&& character.player.distanceTo(this.player) < 32.0 && (mate.target == null || !mate.target.isAlive())) {
					mate.setTarget(attacker);
				}
			}
		}
	}

	// ---------------------------------------------------------------- chatter

	@Override
	protected String[] chatterLines() {
		return switch (this.variant) {
			case KNIGHT, CAPTAIN -> new String[]{"*polishes armor*", "the fort is safe with me", "stand tall, soldier!", "shields up, always"};
			case PIRATE -> new String[]{"yo ho ho!", "smell that sea breeze?", "where'd I bury that chest...", "arr, land is boring"};
			case MINER -> new String[]{"diamonds are down there somewhere", "*taps the stone*", "need more torches", "I can hear lava..."};
			case FISHERMAN -> new String[]{"they're biting today!", "caught one THIS big", "shh, don't scare the fish", "nothing beats a quiet dock"};
			case VILLAGER -> new String[]{"nice day in the village", "the farm's looking good", "anyone seen the iron golem?", "*waves*"};
			case SWEATY -> new String[]{"1v1 me later", "my cps is insane", "practicing my combos", "ez"};
			case BANDIT -> new String[]{"nothing to see here...", "*counts emeralds*", "that loot looks unguarded", "shh"};
			default -> new String[]{"anyone up for a spar?", "I could eat like five loaves rn", "*practices sword swings*",
					"where'd I put my steak...", "FelixSyn better not show up here", "bread is life", "I'm basically unbeatable"};
		};
	}

	@Override
	protected @Nullable String[] chatWith(LoafBrain other) {
		return switch (other.kind()) {
			case LILY -> new String[]{"hey Lily! still reading that book?", "Lily, look at this sword!", "nice flowers, Lily"};
			case MADISON -> new String[]{"Madison, got any bread?", "I'm not touching your pantry, promise", "hey Madison!"};
			case BREADLOAF -> new String[]{"race you to that tree!", "bet I can out-crit you", "loaf squad!"};
			default -> null;
		};
	}

	// ---------------------------------------------------------------- taming

	public void tame(ServerPlayer tamer) {
		this.setOwner(tamer);
		this.groupId = null;
		this.playSound(ModSounds.TAMED, 1.0F, 1.0F);
		this.particles(ParticleTypes.HEART, 7);
		ModAdvancements.award(tamer, ModAdvancements.BREAD_WINNER);
	}

	@Override
	protected @Nullable InteractionResult interact(ServerPlayer clicker, ItemStack stack) {
		if (!this.isTame()) {
			if (stack.is(Items.COOKED_BEEF) && !this.baby) {
				// Can't be tamed by the player he is currently fighting.
				if (this.target == clicker) {
					this.particles(ParticleTypes.SMOKE, 7);
					return InteractionResult.FAIL;
				}
				ItemStack steak = stack.copyWithCount(1);
				stack.consume(1, clicker);
				Inv.give(this.player, steak);
				this.playSound(ModSounds.GIVEN_GIFT, 1.0F, 1.0F);
				if (this.random.nextInt(Math.max(1, com.itzbreadloaf.config.ModConfig.get().breadTameChance)) == 0) {
					this.tame(clicker);
				} else {
					this.particles(ParticleTypes.SMOKE, 7);
				}
				return InteractionResult.SUCCESS;
			}
			return null;
		}
		if (!this.isOwnedBy(clicker)) {
			return null;
		}
		if (!this.baby && this.duties.tryComplete(clicker, stack)) {
			return InteractionResult.SUCCESS;
		}
		if (stack.isEmpty() && clicker.isShiftKeyDown() && !this.baby && this.duties.remind(clicker)) {
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.COOKED_BEEF)) {
			if (this.baby) {
				// Feeding a baby its parent's food makes it grow up faster.
				stack.consume(1, clicker);
				this.ageUp(GROW_UP_TICKS / 10);
				this.particles(ParticleTypes.HAPPY_VILLAGER, 7);
				return InteractionResult.SUCCESS;
			}
			if (this.player.getHealth() < this.player.getMaxHealth()) {
				stack.consume(1, clicker);
				this.player.heal(STEAK_FEED_HEAL);
				this.playSound(SoundEvents.GENERIC_EAT.value(), 1.0F, 1.0F);
				this.playSound(ModSounds.GIVEN_GIFT, 1.0F, 1.0F);
				this.particles(ParticleTypes.HEART, 7);
				return InteractionResult.SUCCESS;
			}
			if (this.canFallInLove()) {
				stack.consume(1, clicker);
				this.setInLove();
				return InteractionResult.SUCCESS;
			}
		}
		if (stack.isEmpty()) {
			this.setSitting(!this.sitting);
			this.setTarget(null);
			return InteractionResult.SUCCESS;
		}
		return null;
	}

	// ---------------------------------------------------------------- saving

	@Override
	public void save(CompoundTag tag) {
		super.save(tag);
		this.duties.save(tag);
		if (this.groupId != null) {
			tag.store("Group", UUIDUtil.CODEC, this.groupId);
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.duties.load(tag);
		this.groupId = tag.read("Group", UUIDUtil.CODEC).orElse(null);
	}
}
