package com.itzbreadloaf.brain;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Walks a character along an A* path with real player inputs: it looks along the way (unless the brain is
 * looking at something else, then it strafes), presses forward/strafe, jumps up blocks and out of water, sprints,
 * and opens wooden doors and gates in the way.
 *
 * Repaths every second when following something that moves, and right away when stuck. After a few failed tries
 * it reports {@link #failed()} so the brain can build, dig or give up.
 */
public final class Navigator {
	public enum Speed { WALK, SPRINT, SNEAK }

	public static final int REPATH_TICKS = 20;
	public static final int MAX_FAILURES = 4;
	public static final int MAX_RANGE = 48;

	private final ServerPlayer player;
	private final Controls controls;

	private @Nullable Vec3 targetPos;
	private @Nullable Entity targetEntity;
	private double reach = 1.0;
	private Speed speed = Speed.WALK;

	private @Nullable List<BlockPos> path;
	private boolean partial;
	private int index;
	private int repathTimer;
	private @Nullable Vec3 pathGoal;
	private int stuckTimer;
	private Vec3 lastCheck = Vec3.ZERO;
	private int failures;
	private boolean failed;
	private boolean arrived;
	private int jumpCooldown;
	private int doorCooldown;
	/** The door or gate this character opened last, so it can close it again behind itself. */
	private @Nullable BlockPos openedDoor;
	private int openedDoorTicks;
	/** Path search budget shared by every character (server thread only). */
	private static long budgetTick = -1;
	private static int searchesThisTick;
	/**
	 * Recent paths, shared by every character (server thread only): the same start and goal block within 5 seconds
	 * reuses the path instead of searching again (characters walking to the same bell, bed or friend).
	 */
	private static final java.util.LinkedHashMap<Long, CachedPath> PATH_CACHE = new java.util.LinkedHashMap<>(64, 0.75F, true) {
		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<Long, CachedPath> eldest) {
			return this.size() > 256;
		}
	};
	private static final int CACHE_TICKS = 100;
	/** Searches and cache hits, for /loafcraft debug ai. */
	public static int searchesCount;
	public static int cacheHits;

	private record CachedPath(Pathfinder.Path path, long time) {
	}

	private static long cacheKey(BlockPos start, BlockPos goal, double reach) {
		return start.asLong() * 31L ^ goal.asLong() * 17L ^ Math.round(reach * 4.0);
	}

	public Navigator(ServerPlayer player, Controls controls) {
		this.player = player;
		this.controls = controls;
	}

	// ---------------------------------------------------------------- goals

	public void moveTo(Vec3 pos, double reach, Speed speed) {
		boolean same = this.targetEntity == null && this.targetPos != null && this.targetPos.distanceToSqr(pos) < 1.0;
		this.targetEntity = null;
		this.targetPos = pos;
		this.reach = reach;
		this.speed = speed;
		if (!same) {
			this.reset();
		}
	}

	public void moveTo(BlockPos pos, double reach, Speed speed) {
		this.moveTo(Vec3.atBottomCenterOf(pos), reach, speed);
	}

	public void follow(Entity entity, double reach, Speed speed) {
		boolean same = this.targetEntity == entity;
		this.targetEntity = entity;
		this.targetPos = null;
		this.reach = reach;
		this.speed = speed;
		if (!same) {
			this.reset();
		}
	}

	public void stop() {
		this.targetEntity = null;
		this.targetPos = null;
		this.path = null;
		this.arrived = false;
	}

	private void reset() {
		this.path = null;
		this.failures = 0;
		this.failed = false;
		this.arrived = false;
		this.repathTimer = 0;
		this.stuckTimer = 0;
		this.lastCheck = this.player.position();
	}

	/** Try again after a failure (for example after building a pillar or digging through a wall). */
	public void retry() {
		this.reset();
	}

	public boolean isMoving() {
		return (this.targetPos != null || this.targetEntity != null) && !this.arrived;
	}

	public boolean hasGoal() {
		return this.targetPos != null || this.targetEntity != null;
	}

	public boolean hasArrived() {
		return this.arrived;
	}

	/** True once it tried several times and can't get closer. */
	public boolean failed() {
		return this.failed;
	}

	/** The path only gets partway (the goal is up a cliff, across a gap...). */
	public boolean isPartial() {
		return this.path != null && this.partial;
	}

	/** Reached the end of a partial path: as close as walking gets. */
	public boolean isAtDeadEnd() {
		return this.failed || this.partial && this.path != null && this.index >= this.path.size();
	}

	public @Nullable Vec3 goal() {
		if (this.targetEntity != null) {
			return this.targetEntity.isRemoved() ? null : this.targetEntity.position();
		}
		return this.targetPos;
	}

	// ---------------------------------------------------------------- ticking

	public void tick() {
		if (this.jumpCooldown > 0) {
			this.jumpCooldown--;
		}
		if (this.doorCooldown > 0) {
			this.doorCooldown--;
		}
		this.closeDoorBehind();
		Vec3 goal = this.goal();
		if (goal == null) {
			this.stop();
			return;
		}
		Vec3 pos = this.player.position();
		double horizontal = Math.sqrt(sqr(goal.x - pos.x) + sqr(goal.z - pos.z));
		double dy = goal.y - pos.y;
		if (horizontal <= this.reach && Math.abs(dy) <= Math.max(1.5, this.reach)) {
			this.arrived = true;
			this.path = null;
			this.controls.setSprinting(false);
			this.swimUp();
			return;
		}
		this.arrived = false;

		// Close, same height and nothing in the way: just walk straight there.
		if (horizontal < 3.0 && Math.abs(dy) < 1.0 && this.clearLine(pos, goal)) {
			this.steer(goal, false);
			this.checkStuck(pos);
			return;
		}

		// The path is kept until the goal has moved 2 blocks (a target strafing around doesn't cost a new search).
		boolean goalMoved = this.pathGoal != null && this.pathGoal.distanceToSqr(goal) > 4.0;
		this.repathTimer--;
		if ((this.path == null || goalMoved || this.partial) && this.repathTimer <= 0 && !this.failed) {
			this.repath(goal);
		}
		if (this.path == null) {
			if (!this.failed) {
				this.steer(goal, false);
			}
			this.checkStuck(pos);
			return;
		}

		// Next waypoint: skip the ones we've reached.
		while (this.index < this.path.size()) {
			BlockPos node = this.path.get(this.index);
			double ndx = node.getX() + 0.5 - pos.x;
			double ndz = node.getZ() + 0.5 - pos.z;
			double ndy = node.getY() - pos.y;
			if (ndx * ndx + ndz * ndz < 0.35 * 0.35 && Math.abs(ndy) < 1.2
					|| ndx * ndx + ndz * ndz < 0.7 * 0.7 && Math.abs(ndy) < 0.6 && this.index + 1 < this.path.size()) {
				this.index++;
			} else {
				break;
			}
		}
		if (this.index >= this.path.size()) {
			// End of the path: the goal itself, if it's close; otherwise this is as far as we get.
			if (this.partial) {
				this.controls.setSprinting(false);
				this.checkStuck(pos);
				return;
			}
			this.steer(goal, false);
		} else {
			BlockPos node = this.path.get(this.index);
			boolean up = node.getY() > Math.floor(pos.y + 0.2);
			this.openDoorIfNeeded(node);
			this.steer(Vec3.atBottomCenterOf(node), up);
		}
		this.checkStuck(pos);
	}

	private void repath(Vec3 goal) {
		ServerLevel level = this.player.level();
		long now = level.getGameTime();
		BlockPos start = this.player.blockPosition();
		BlockPos end = BlockPos.containing(goal);
		double searchReach = Math.max(0.9, this.reach);
		long key = cacheKey(start, end, searchReach) ^ level.dimension().identifier().hashCode();
		CachedPath cached = PATH_CACHE.get(key);
		if (cached != null && now - cached.time() <= CACHE_TICKS) {
			cacheHits++;
			this.repathTimer = REPATH_TICKS + (this.player.getId() & 7);
			this.pathGoal = goal;
			this.path = cached.path().nodes();
			this.partial = !cached.path().complete();
			this.index = 0;
			return;
		}
		// Spread path searches out: only a few per tick for all characters together, and each one on its own beat.
		if (now != budgetTick) {
			budgetTick = now;
			searchesThisTick = 0;
		}
		if (searchesThisTick >= Math.max(1, com.itzbreadloaf.config.ModConfig.get().maxPathSearchesPerTick)) {
			this.repathTimer = 1 + (this.player.getId() & 3);
			return;
		}
		searchesThisTick++;
		this.repathTimer = REPATH_TICKS + (this.player.getId() & 7);
		Pathfinder.Path found = Pathfinder.find(level, start, end, searchReach, MAX_RANGE);
		searchesCount++;
		if (found != null) {
			PATH_CACHE.put(key, new CachedPath(found, now));
		}
		this.pathGoal = goal;
		if (found == null) {
			this.path = null;
			this.partial = true;
			this.failures++;
			if (this.failures >= MAX_FAILURES) {
				this.failed = true;
			}
			return;
		}
		this.path = found.nodes();
		this.partial = !found.complete();
		this.index = 0;
	}

	/** Sets the movement input toward a point, jumping where needed. */
	private void steer(Vec3 point, boolean jumpUp) {
		Vec3 pos = this.player.position();
		this.controls.walkToward(point);
		if (!this.controls.hasLook()) {
			float yaw = Controls.yawTo(pos, point);
			this.controls.look(yaw, 8.0F);
		}
		boolean inWater = this.player.isInWater();
		this.controls.setSprinting(this.speed == Speed.SPRINT && !this.player.isUnderWater());
		this.controls.setSneaking(this.speed == Speed.SNEAK);
		if (inWater) {
			// Swim: hold jump to stay up (and to climb out onto the bank).
			this.controls.holdJump(point.y > pos.y - 0.6 || this.player.isUnderWater());
			return;
		}
		this.controls.holdJump(false);
		double horizontal = Math.sqrt(sqr(point.x - pos.x) + sqr(point.z - pos.z));
		boolean bump = this.player.horizontalCollision && this.player.onGround();
		if ((jumpUp && horizontal < 1.6 || bump) && this.player.onGround() && this.jumpCooldown <= 0) {
			this.controls.jumpOnce();
			this.jumpCooldown = 8;
		}
	}

	private void swimUp() {
		this.controls.holdJump(this.player.isInWater());
	}

	/** Not getting anywhere for a second: try a new path; after a few tries, give up. */
	private void checkStuck(Vec3 pos) {
		if (++this.stuckTimer < 20) {
			return;
		}
		this.stuckTimer = 0;
		if (pos.distanceToSqr(this.lastCheck) < 0.25 * 0.25) {
			this.failures++;
			this.path = null;
			this.repathTimer = 0;
			if (this.failures >= MAX_FAILURES) {
				this.failed = true;
			}
		} else if (this.failures > 0 && pos.distanceToSqr(this.lastCheck) > 1.0) {
			this.failures = Math.max(0, this.failures - 1);
			this.failed = false;
		}
		this.lastCheck = pos;
	}

	/** Right-clicks a closed wooden door or fence gate that's in the way. */
	private void openDoorIfNeeded(BlockPos node) {
		if (this.doorCooldown > 0) {
			return;
		}
		for (BlockPos pos : new BlockPos[]{node, node.above()}) {
			BlockState state = this.player.level().getBlockState(pos);
			boolean closedDoor = state.getBlock() instanceof DoorBlock door && door.type().canOpenByHand() && !door.isOpen(state);
			boolean closedGate = state.getBlock() instanceof FenceGateBlock && !state.getValue(BlockStateProperties.OPEN);
			if ((closedDoor || closedGate) && this.player.position().distanceTo(Vec3.atCenterOf(pos)) < 3.0) {
				this.controls.aimAt(Vec3.atCenterOf(pos));
				this.controls.useOnce();
				this.doorCooldown = 20;
				this.openedDoor = pos.immutable();
				this.openedDoorTicks = 0;
				return;
			}
		}
	}

	/** Once through a door or gate it opened, it closes it again (unless someone is standing in it). */
	private void closeDoorBehind() {
		if (this.openedDoor == null) {
			return;
		}
		BlockPos door = this.openedDoor;
		double distance = this.player.position().distanceTo(Vec3.atCenterOf(door));
		if (++this.openedDoorTicks > 200 || distance > 5.0) {
			this.openedDoor = null;
			return;
		}
		if (distance < 1.8 || this.openedDoorTicks < 10 || this.doorCooldown > 0) {
			return;
		}
		this.openedDoor = null;
		BlockState state = this.player.level().getBlockState(door);
		boolean open = state.getBlock() instanceof DoorBlock doorBlock && doorBlock.isOpen(state)
				|| state.getBlock() instanceof FenceGateBlock && state.getValue(BlockStateProperties.OPEN);
		if (open && this.player.level().getEntities(null, new net.minecraft.world.phys.AABB(door)).isEmpty()) {
			this.controls.aimAt(Vec3.atCenterOf(door));
			this.controls.useOnce();
			this.doorCooldown = 10;
		}
	}

	private boolean clearLine(Vec3 from, Vec3 to) {
		Vec3 start = from.add(0.0, 0.5, 0.0);
		Vec3 end = to.add(0.0, 0.5, 0.0);
		HitResult hit = this.player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.player));
		if (hit.getType() != HitResult.Type.MISS) {
			return false;
		}
		// No hole in the floor along the way.
		Vec3 mid = start.add(end).scale(0.5);
		BlockPos below = BlockPos.containing(mid.x, from.y - 0.5, mid.z);
		return !this.player.level().getBlockState(below).getCollisionShape(this.player.level(), below).isEmpty()
				|| this.player.level().getFluidState(below).isSource();
	}

	private static double sqr(double value) {
		return value * value;
	}
}
