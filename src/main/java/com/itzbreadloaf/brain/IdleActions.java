package com.itzbreadloaf.brain;

import com.itzbreadloaf.brain.ai.Mood;
import com.itzbreadloaf.brain.ai.Routine;
import com.itzbreadloaf.brain.ai.Situation;
import com.itzbreadloaf.character.CharacterKind;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.LoafCharacter;
import com.itzbreadloaf.character.LoafIds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The little human things characters do when nothing else is going on (AI overhaul part 6), all with real inputs:
 * greeting trusted players with a crouch-spam and a wave, greeting friend characters, happy jumps, looking at what a
 * player is holding, walking over to look at dropped items, sorting through the hotbar, fiddling with it when bored,
 * sitting on a chair (stairs), stretching in the morning, stargazing, watching a player build or mine, tinkering
 * (placing a couple of blocks and taking them back), mining a bit of natural stone, fishing, getting out of the rain,
 * catching their breath after a close fight, celebrating a win, glancing at a fight or a hit nearby, and stepping
 * aside when someone stands right on top of them.
 *
 * What they pick depends on their mood, the time, who's around and who they are (Lily stargazes, Madison sorts,
 * ItzBreadLoaf jumps and tinkers, miners mine). Master BreadLoaf doesn't do any of this: he's a boss.
 */
final class IdleActions {
	enum Action {
		NONE, GREET, GREET_FRIEND, HAPPY_JUMP, LOOK_AT_ITEM, INSPECT_DROPS, ORGANIZE, FIDDLE, SIT, STRETCH, STARGAZE, WATCH_WORK,
		TINKER, CLEANUP, MINE, FISH, SHELTER, REST, CELEBRATE, GLANCE
	}

	private static final int GREET_PLAYER_GAP = 3600;
	private static final int GREET_FRIEND_GAP = 6000;

	private final LoafBrain brain;
	private Action action = Action.NONE;
	private int ticks;
	private int duration;
	private @Nullable Entity focus;
	private @Nullable BlockPos spot;
	private @Nullable Vec3 lookPoint;
	private int savedSlot = -1;
	private int scanTimer;
	private int stepTicks;
	private @Nullable Vec3 stepDir;
	private final Map<UUID, Long> lastGreeted = new HashMap<>();
	private final List<BlockPos> placed = new ArrayList<>();
	private @Nullable Fishing fishing;

	IdleActions(LoafBrain brain) {
		this.brain = brain;
	}

	Action current() {
		return this.action;
	}

	private long now() {
		return this.brain.player.level().getGameTime();
	}

	// ---------------------------------------------------------------- the main entry points

	/**
	 * One idle tick. {@code tame}: they're with their owner (only the actions that keep them near). True while busy
	 * with something, so the brain's own idle (wandering, looking around) waits.
	 */
	boolean tick(boolean tame) {
		if (this.brain.kind == CharacterKind.MASTER || this.brain.baby && this.action != Action.HAPPY_JUMP) {
			return false;
		}
		if (this.personalSpace()) {
			return true;
		}
		if (--this.scanTimer <= 0) {
			this.scanTimer = 20;
			this.scan(tame);
		}
		if (this.action == Action.NONE) {
			if (!this.placed.isEmpty() && !tame) {
				this.begin(Action.CLEANUP, 200);
			} else if (this.brain.random.nextInt(tame ? 220 : 150) == 0) {
				this.choose(tame);
			}
		}
		if (this.action == Action.NONE) {
			return false;
		}
		this.ticks++;
		boolean busy = this.run();
		if (!busy || this.ticks >= this.duration) {
			this.stop();
		}
		return busy;
	}

	/** Drops whatever they were doing (a fight started, they're going to bed...). */
	void stop() {
		if (this.action == Action.FISH && this.fishing != null) {
			this.fishing.stop();
		}
		if (this.savedSlot >= 0) {
			this.brain.controls.selectSlot(this.savedSlot);
			this.savedSlot = -1;
		}
		this.brain.controls.setSneaking(false);
		this.brain.builder.stopDigging();
		this.action = Action.NONE;
		this.focus = null;
		this.spot = null;
		this.lookPoint = null;
	}

	private void begin(Action action, int duration) {
		this.stop();
		this.action = action;
		this.duration = duration;
		this.ticks = 0;
	}

	/** Won a fight: a couple of jumps and a swing of the arm. */
	void celebrate() {
		this.begin(Action.CELEBRATE, 30);
	}

	/** After a close fight: crouch down and catch their breath for a few seconds. */
	void rest() {
		this.begin(Action.REST, 80 + this.brain.random.nextInt(40));
	}

	/** Something got hit nearby: turn to look (unless busy with something they'd rather not drop). */
	void glance(Vec3 where) {
		if (this.action == Action.NONE || this.action == Action.FIDDLE || this.action == Action.ORGANIZE || this.action == Action.STARGAZE) {
			this.begin(Action.GLANCE, 20 + this.brain.random.nextInt(15));
			this.lookPoint = where;
		}
	}

	// ---------------------------------------------------------------- noticing people

	/** Every second: trusted players and friends to greet, rain to get out of. */
	private void scan(boolean tame) {
		if (this.action == Action.GREET || this.action == Action.GREET_FRIEND || this.action == Action.CELEBRATE) {
			return;
		}
		long now = this.now();
		for (ServerPlayer viewer : this.brain.player.level().players()) {
			if (!LoafIds.isRealPlayer(viewer) || viewer.isSpectator() || viewer.distanceTo(this.brain.player) > 7.0
					|| !viewer.hasLineOfSight(this.brain.player) || !this.brain.likesPlayer(viewer)) {
				continue;
			}
			Long last = this.lastGreeted.get(viewer.getUUID());
			this.lastGreeted.put(viewer.getUUID(), now);
			if (last == null || now - last > GREET_PLAYER_GAP) {
				this.begin(Action.GREET, 34);
				this.focus = viewer;
				return;
			}
		}
		if (!tame) {
			for (LoafCharacter other : CharacterManager.active()) {
				LoafBrain o = other.brain;
				if (o == null || o == this.brain || other.player.level() != this.brain.player.level() || o.target != null
						|| other.player.isSleeping() || other.player.distanceTo(this.brain.player) > 10.0 || !this.brain.isFriendCharacter(o)) {
					continue;
				}
				Long last = this.lastGreeted.get(other.player.getUUID());
				this.lastGreeted.put(other.player.getUUID(), now);
				if (last == null || now - last > GREET_FRIEND_GAP) {
					this.begin(Action.GREET_FRIEND, 40);
					this.focus = other.player;
					return;
				}
			}
			if (this.action != Action.SHELTER && this.brain.player.level().isRainingAt(this.brain.player.blockPosition().above())
					&& (this.brain.home != null || this.brain.bed != null)) {
				this.begin(Action.SHELTER, 400);
			}
		}
	}

	/** Someone standing right inside them: step aside. True while stepping. */
	private boolean personalSpace() {
		if (this.stepTicks > 0) {
			this.stepTicks--;
			if (this.stepDir != null) {
				this.brain.controls.walk(this.stepDir, 0.6F);
			}
			return true;
		}
		if (this.brain.ticks % 5 != 0 || this.brain.nav.isMoving() || this.action == Action.SIT) {
			return false;
		}
		for (Entity other : this.brain.player.level().getEntities(this.brain.player, this.brain.player.getBoundingBox().inflate(0.1),
				e -> e instanceof LivingEntity && !e.isSpectator())) {
			Vec3 away = this.brain.player.position().subtract(other.position()).multiply(1.0, 0.0, 1.0);
			if (away.lengthSqr() < 0.36) {
				this.stepDir = away.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
				this.stepTicks = 6;
				return true;
			}
		}
		return false;
	}

	// ---------------------------------------------------------------- choosing

	private void add(List<Action> options, List<Float> weights, Action action, float weight) {
		if (weight > 0.0F) {
			options.add(action);
			weights.add(weight);
		}
	}

	private @Nullable Player playerHoldingSomething() {
		return this.brain.player.level().getNearestPlayer(this.brain.player.getX(), this.brain.player.getY(), this.brain.player.getZ(), 5.0,
				p -> p != this.brain.player && p instanceof Player pl && LoafIds.isRealPlayer(pl) && !pl.isSpectator()
						&& !pl.getMainHandItem().isEmpty() && pl.hasLineOfSight(this.brain.player));
	}

	private @Nullable Player playerAtWork() {
		return this.brain.player.level().getNearestPlayer(this.brain.player.getX(), this.brain.player.getY(), this.brain.player.getZ(), 10.0,
				p -> p != this.brain.player && p instanceof Player pl && LoafIds.isRealPlayer(pl) && !pl.isSpectator() && pl.swinging);
	}

	private @Nullable ItemEntity droppedItem() {
		List<ItemEntity> items = this.brain.player.level().getEntitiesOfClass(ItemEntity.class, this.brain.player.getBoundingBox().inflate(6.0, 2.0, 6.0));
		return items.isEmpty() ? null : items.get(this.brain.random.nextInt(items.size()));
	}

	private boolean night() {
		long time = Math.floorMod(this.brain.player.level().getDayTime(), 24000L);
		return time >= 13000 && time < 23000;
	}

	private boolean underSky() {
		return this.brain.player.level().canSeeSky(this.brain.player.blockPosition().above());
	}

	/** Picks something to do from the mood, the time, who's around and who they are. */
	private void choose(boolean tame) {
		Mood mood = this.brain.mood.mood();
		if (mood == Mood.SCARED || mood == Mood.ANGRY || !this.brain.player.onGround()) {
			return;
		}
		CharacterKind kind = this.brain.kind;
		boolean lily = kind == CharacterKind.LILY;
		boolean bread = kind == CharacterKind.BREADLOAF;
		List<Action> options = new ArrayList<>();
		List<Float> weights = new ArrayList<>();
		Player holder = this.playerHoldingSomething();
		Player worker = this.playerAtWork();
		ItemEntity dropped = tame ? null : this.droppedItem();
		boolean happy = mood == Mood.HAPPY;
		boolean bored = mood == Mood.BORED;
		boolean curious = mood == Mood.CURIOUS;
		boolean tired = mood == Mood.TIRED;
		this.add(options, weights, Action.HAPPY_JUMP, happy ? (bread || kind == CharacterKind.FELIXSYN ? 3.0F : 2.0F) : 0.3F);
		if (holder != null) {
			this.add(options, weights, Action.LOOK_AT_ITEM, curious ? 4.0F : happy ? 2.0F : 1.0F);
		}
		if (worker != null && !tame) {
			this.add(options, weights, Action.WATCH_WORK, curious ? 3.0F : 1.0F);
		}
		if (dropped != null) {
			this.add(options, weights, Action.INSPECT_DROPS, curious ? 3.0F : 1.0F);
		}
		this.add(options, weights, Action.ORGANIZE, (kind == CharacterKind.MADISON ? 2.0F : 1.0F) * (bored ? 2.0F : 0.7F));
		this.add(options, weights, Action.FIDDLE, bored ? (kind == CharacterKind.FELIXSYN ? 5.0F : 4.0F) : 0.4F);
		if (this.night() && this.underSky() && !this.brain.player.isSleeping()) {
			this.add(options, weights, Action.STARGAZE, lily ? 5.0F : 1.5F);
		}
		if (this.brain.routineActivity() == Routine.Activity.WAKE_UP) {
			this.add(options, weights, Action.STRETCH, 4.0F);
		}
		if (!tame) {
			this.add(options, weights, Action.SIT, tired ? 4.0F : 1.2F);
			if (tired) {
				this.add(options, weights, Action.REST, 1.5F);
			}
			if (Builder.allowed(this.brain.player.level()) && Inv.find(this.brain.player, IdleActions::tinkerBlock) >= 0) {
				this.add(options, weights, Action.TINKER, (bread ? 1.5F : lily ? 0.4F : 1.0F) * (bored ? 2.0F : 0.6F));
			}
			if (Builder.allowed(this.brain.player.level()) && Inv.find(this.brain.player, stack -> stack.is(ItemTags.PICKAXES)) >= 0) {
				this.add(options, weights, Action.MINE, (this.brain.variant == com.itzbreadloaf.character.CharacterVariant.MINER ? 3.0F : 1.0F)
						* (bored ? 2.0F : 0.6F));
			}
			if (Inv.find(this.brain.player, stack -> stack.is(Items.FISHING_ROD)) >= 0) {
				this.add(options, weights, Action.FISH, happy || bored ? 2.0F : 1.0F);
			}
		}
		float total = 0.0F;
		for (float w : weights) {
			total += w;
		}
		if (total <= 0.0F) {
			return;
		}
		float roll = this.brain.random.nextFloat() * total;
		Action picked = options.get(options.size() - 1);
		for (int i = 0; i < options.size(); i++) {
			roll -= weights.get(i);
			if (roll <= 0.0F) {
				picked = options.get(i);
				break;
			}
		}
		int length = switch (picked) {
			case HAPPY_JUMP -> 30;
			case LOOK_AT_ITEM -> 50;
			case WATCH_WORK -> 80;
			case INSPECT_DROPS -> 100;
			case ORGANIZE, FIDDLE -> 45;
			case STARGAZE -> 140;
			case STRETCH -> 40;
			case SIT -> 160 + this.brain.random.nextInt(160);
			case REST -> 100;
			case TINKER -> 220;
			case MINE -> 160;
			case FISH -> 500 + this.brain.random.nextInt(400);
			default -> 40;
		};
		this.begin(picked, length);
		this.focus = picked == Action.LOOK_AT_ITEM ? holder : picked == Action.WATCH_WORK ? worker : picked == Action.INSPECT_DROPS ? dropped : null;
	}

	// ---------------------------------------------------------------- doing

	private boolean run() {
		return switch (this.action) {
			case GREET -> this.greet();
			case GREET_FRIEND -> this.greetFriend();
			case HAPPY_JUMP -> this.happyJump();
			case LOOK_AT_ITEM -> this.lookAtItem();
			case INSPECT_DROPS -> this.inspectDrops();
			case ORGANIZE -> this.scrollHotbar(true);
			case FIDDLE -> this.scrollHotbar(false);
			case SIT -> this.sit();
			case STRETCH -> this.stretch();
			case STARGAZE -> this.stargaze();
			case WATCH_WORK -> this.watchWork();
			case TINKER -> this.tinker();
			case CLEANUP -> this.cleanup();
			case MINE -> this.mine();
			case FISH -> this.fish();
			case SHELTER -> this.shelter();
			case REST -> this.restTick();
			case CELEBRATE -> this.celebrateTick();
			case GLANCE -> this.glanceTick();
			case NONE -> false;
		};
	}

	/** A trusted player is back: turn to them, crouch-spam, wave, say hi. */
	private boolean greet() {
		if (!(this.focus instanceof Player p) || !p.isAlive()) {
			return false;
		}
		if (this.ticks == 1) {
			this.brain.speak(Situation.GREET_PLAYER, null);
		}
		this.brain.nav.stop();
		this.brain.controls.lookAt(p.getEyePosition());
		this.brain.controls.setSneaking(this.ticks > 4 && this.ticks < 22 && this.ticks % 4 < 2);
		if (this.ticks == 24 || this.ticks == 28) {
			this.brain.player.swing(InteractionHand.MAIN_HAND);
		}
		return true;
	}

	/** A friend character is around again: walk over a bit, crouch twice, wave, and call their name. */
	private boolean greetFriend() {
		if (this.focus == null || !this.focus.isAlive()) {
			return false;
		}
		if (this.ticks == 1) {
			this.brain.speak(Situation.GREET_FRIEND, this.focus.getName().getString());
		}
		double distance = this.brain.player.distanceTo(this.focus);
		this.brain.controls.lookAt(this.focus.getEyePosition());
		if (distance > 4.0 && this.ticks < 20) {
			this.brain.nav.follow(this.focus, 3.0, Navigator.Speed.WALK);
		} else {
			this.brain.nav.stop();
			this.brain.controls.setSneaking(this.ticks % 10 < 3 && this.ticks < 30);
			if (this.ticks == 32) {
				this.brain.player.swing(InteractionHand.MAIN_HAND);
			}
		}
		return true;
	}

	private boolean happyJump() {
		if ((this.ticks == 2 || this.ticks == 14 || this.ticks == 26) && this.brain.player.onGround()) {
			this.brain.controls.jumpOnce();
		}
		this.brain.lookAround(8.0);
		return true;
	}

	/** Looks at the item in a nearby player's hand (and sometimes says something about it). */
	private boolean lookAtItem() {
		if (!(this.focus instanceof Player p) || !p.isAlive() || p.getMainHandItem().isEmpty() || p.distanceTo(this.brain.player) > 7.0) {
			return false;
		}
		Vec3 right = p.getViewVector(1.0F).cross(new Vec3(0.0, 1.0, 0.0)).normalize();
		Vec3 hand = p.getEyePosition().add(0.0, -0.6, 0.0).add(right.scale(0.35));
		this.brain.nav.stop();
		this.brain.controls.lookAt(hand);
		if (this.ticks == 15 && this.brain.random.nextInt(3) == 0) {
			ItemStack held = p.getMainHandItem();
			this.brain.speak(Situation.ITEM, held.getHoverName().getString().toLowerCase(java.util.Locale.ROOT));
		}
		return true;
	}

	/** Walks over to an item on the ground and looks at it (players pick up what they walk over). */
	private boolean inspectDrops() {
		if (!(this.focus instanceof ItemEntity item) || item.isRemoved()) {
			return false;
		}
		this.brain.controls.lookAt(item.position());
		if (this.brain.player.distanceTo(item) > 1.2) {
			this.brain.nav.moveTo(item.position(), 0.8, Navigator.Speed.WALK);
		} else {
			this.brain.nav.stop();
		}
		return true;
	}

	/**
	 * Sorting (looking down at the hotbar, going through it slot by slot) or, bored, fiddling (flicking between random
	 * slots). The slot they had is put back at the end.
	 */
	private boolean scrollHotbar(boolean sorting) {
		if (this.savedSlot < 0) {
			this.savedSlot = this.brain.player.getInventory().getSelectedSlot();
		}
		this.brain.nav.stop();
		if (sorting) {
			this.brain.controls.look(this.brain.player.getYRot(), 50.0F);
			this.brain.controls.setSneaking(true);
			if (this.ticks % 5 == 0) {
				this.brain.controls.selectSlot((this.ticks / 5) % 9);
			}
		} else {
			this.brain.lookAround(6.0);
			if (this.ticks % (4 + this.brain.random.nextInt(5)) == 0) {
				this.brain.controls.selectSlot(this.brain.random.nextInt(9));
			}
		}
		return true;
	}

	/** Sits on a chair: a stairs block near home with room above it (crouched on it, looking around). */
	private boolean sit() {
		if (this.spot == null) {
			BlockPos center = this.brain.home != null ? this.brain.home : this.brain.player.blockPosition();
			this.spot = this.brain.findBlock(center, 8, 3, state -> state.is(BlockTags.STAIRS));
			if (this.spot == null || !this.brain.player.level().getBlockState(this.spot.above()).isAir()
					|| !this.brain.player.level().getBlockState(this.spot.above(2)).isAir()) {
				// No chair around: sit down where they are.
				this.spot = this.brain.player.blockPosition().below();
			}
		}
		Vec3 seat = Vec3.atBottomCenterOf(this.spot.above());
		if (this.brain.player.position().distanceTo(seat) > 0.8 && this.ticks < 120) {
			this.brain.nav.moveTo(seat, 0.4, Navigator.Speed.WALK);
			return true;
		}
		this.brain.nav.stop();
		this.brain.controls.setSneaking(true);
		this.brain.lookAround(8.0);
		return true;
	}

	private boolean stretch() {
		this.brain.nav.stop();
		if (this.ticks == 1) {
			this.brain.speak(Situation.MORNING, null);
		}
		if (this.ticks < 15) {
			this.brain.controls.look(this.brain.player.getYRot(), -55.0F);
		} else {
			this.brain.controls.setSneaking(this.ticks < 31 && this.ticks % 8 < 4);
			if (this.ticks == 34) {
				this.brain.player.swing(InteractionHand.MAIN_HAND);
			}
		}
		return true;
	}

	private boolean stargaze() {
		if (!this.night() || this.brain.player.isSleeping()) {
			return false;
		}
		if (this.ticks == 10) {
			this.brain.speak(Situation.NIGHT, null);
		}
		this.brain.nav.stop();
		this.brain.controls.look(this.brain.player.getYRot() + 0.6F, -65.0F);
		return true;
	}

	/** Watches what a nearby player is working on: looks at the block they're looking at. */
	private boolean watchWork() {
		if (!(this.focus instanceof Player p) || !p.isAlive() || p.distanceTo(this.brain.player) > 12.0) {
			return false;
		}
		Vec3 eyes = p.getEyePosition();
		BlockHitResult hit = p.level().clip(new ClipContext(eyes, eyes.add(p.getViewVector(1.0F).scale(5.0)),
				ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, p));
		this.brain.controls.lookAt(hit.getType() == HitResult.Type.BLOCK ? Vec3.atCenterOf(hit.getBlockPos()) : p.getEyePosition());
		this.brain.nav.stop();
		if (this.ticks == 30 && this.brain.random.nextInt(3) == 0) {
			this.brain.speak(Situation.WATCH, null);
		}
		return true;
	}

	/** Tinkering: puts down a block and one on top, looks at it a while, then takes both back (no mess left). */
	private boolean tinker() {
		BlockPos base = this.spot;
		if (base == null) {
			Direction facing = this.brain.player.getDirection();
			base = this.brain.player.blockPosition().relative(facing, 2);
			var level = this.brain.player.level();
			if (!level.getBlockState(base).isAir() || !level.getBlockState(base.above()).isAir()
					|| !level.getBlockState(base.below()).isFaceSturdy(level, base.below(), Direction.UP)) {
				return false;
			}
			this.spot = base;
		}
		this.brain.nav.stop();
		var level = this.brain.player.level();
		if (this.ticks < 60) {
			for (BlockPos pos : new BlockPos[]{base, base.above()}) {
				if (level.getBlockState(pos).isAir()) {
					int slot = Inv.ready(this.brain.player, IdleActions::tinkerBlock, 8);
					if (slot < 0) {
						return false;
					}
					if (this.savedSlot < 0) {
						this.savedSlot = this.brain.player.getInventory().getSelectedSlot();
					}
					this.brain.controls.selectSlot(slot);
					this.brain.builder.place(pos);
					return true;
				}
				if (!this.placed.contains(pos)) {
					this.placed.add(pos.immutable());
				}
			}
			this.brain.controls.lookAt(Vec3.atCenterOf(base.above()));
			return true;
		}
		if (this.ticks < 120) {
			this.brain.controls.lookAt(Vec3.atCenterOf(base.above()));
			if (this.ticks % 25 == 0) {
				this.brain.player.swing(InteractionHand.MAIN_HAND);
			}
			return true;
		}
		return this.cleanup();
	}

	/** Takes back blocks they put down while tinkering. */
	private boolean cleanup() {
		while (!this.placed.isEmpty()) {
			BlockPos pos = this.placed.get(this.placed.size() - 1);
			if (this.brain.player.level().getBlockState(pos).isAir() || this.brain.player.position().distanceTo(Vec3.atCenterOf(pos)) > 8.0) {
				this.placed.remove(this.placed.size() - 1);
				continue;
			}
			if (!this.brain.canReach(pos)) {
				this.brain.nav.moveTo(pos, 2.0, Navigator.Speed.WALK);
				return true;
			}
			this.brain.nav.stop();
			if (!this.brain.builder.dig(pos)) {
				this.placed.remove(this.placed.size() - 1);
			}
			return true;
		}
		this.brain.builder.stopDigging();
		return false;
	}

	/** Building blocks that break fast by hand (dirt, planks...), so tinkering can always be taken back. */
	private static boolean tinkerBlock(ItemStack stack) {
		return Inv.isBuildingBlock(stack) && stack.getItem() instanceof net.minecraft.world.item.BlockItem block
				&& !block.getBlock().defaultBlockState().requiresCorrectToolForDrops()
				&& block.getBlock().defaultDestroyTime() <= 2.0F;
	}

	private static boolean naturalStone(BlockState state) {
		return state.is(Blocks.STONE) || state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) || state.is(Blocks.ANDESITE)
				|| state.is(Blocks.TUFF) || state.is(Blocks.DEEPSLATE);
	}

	/** Mines one block of natural stone close by (never near home, so they don't dig into anything built). */
	private boolean mine() {
		if (this.spot == null) {
			BlockPos found = this.brain.findBlock(this.brain.player.blockPosition(), 3, 2, IdleActions::naturalStone);
			if (found == null || this.brain.home != null && found.closerThan(this.brain.home, this.brain.homeRadius + 4)) {
				return false;
			}
			this.spot = found;
			int pick = Inv.ready(this.brain.player, stack -> stack.is(ItemTags.PICKAXES), 3);
			if (pick < 0) {
				return false;
			}
			this.savedSlot = this.brain.player.getInventory().getSelectedSlot();
			this.brain.controls.selectSlot(pick);
		}
		if (!naturalStone(this.brain.player.level().getBlockState(this.spot))) {
			this.brain.builder.stopDigging();
			return false;
		}
		if (!this.brain.canReach(this.spot)) {
			this.brain.nav.moveTo(this.spot, 2.0, Navigator.Speed.WALK);
			return true;
		}
		this.brain.nav.stop();
		return this.brain.builder.dig(this.spot);
	}

	private boolean fish() {
		if (this.fishing == null) {
			this.fishing = new Fishing(this.brain);
		}
		int rod = Inv.ready(this.brain.player, stack -> stack.is(Items.FISHING_ROD), 8);
		if (rod < 0) {
			return false;
		}
		BlockPos center = this.brain.home != null ? this.brain.home : this.brain.player.blockPosition();
		return this.fishing.tick(center, rod);
	}

	/** Raining and out under the sky: back home (or under the bed's roof) until it stops or they're inside. */
	private boolean shelter() {
		if (this.ticks == 1) {
			this.brain.speak(Situation.RAIN, null);
		}
		if (!this.brain.player.level().isRainingAt(this.brain.player.blockPosition().above())) {
			return false;
		}
		BlockPos spot = this.brain.bed != null ? this.brain.bed : this.brain.home;
		if (spot == null || this.brain.player.position().distanceTo(Vec3.atBottomCenterOf(spot)) < 2.5) {
			return false;
		}
		this.brain.nav.moveTo(spot, 2.0, Navigator.Speed.SPRINT);
		return true;
	}

	private boolean restTick() {
		if (this.ticks == 1) {
			this.brain.speak(Situation.REST, null);
		}
		this.brain.nav.stop();
		this.brain.controls.setSneaking(true);
		this.brain.controls.look(this.brain.player.getYRot(), 30.0F);
		return true;
	}

	private boolean celebrateTick() {
		if ((this.ticks == 2 || this.ticks == 14) && this.brain.player.onGround()) {
			this.brain.controls.jumpOnce();
		}
		if (this.ticks == 8 || this.ticks == 20) {
			this.brain.player.swing(InteractionHand.MAIN_HAND);
		}
		return true;
	}

	private boolean glanceTick() {
		if (this.lookPoint == null) {
			return false;
		}
		this.brain.controls.lookAt(this.lookPoint);
		return true;
	}
}
