package com.itzbreadloaf.brain;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A* over block positions (the block a player's feet are in).
 *
 * Moves: walk to any of the 8 neighbours (diagonals only if both sides are open), jump up 1 block, drop down up to
 * 3 blocks (or further into water), and swim up/down in water. Avoids lava, fire, magma, cactus, berry bushes,
 * cobwebs and powder snow. Wooden doors and fence gates count as open (the brain opens them on the way).
 * Gives up after {@link #MAX_NODES} nodes and returns the path to the closest point it found instead.
 */
public final class Pathfinder {
	public static final int MAX_NODES = 2500;
	public static final int MAX_DROP = 3;
	public static final int MAX_WATER_DROP = 12;

	private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

	private final ServerLevel level;
	/** 0 = unknown, 1 = open (feet/head can be here), 2 = blocked, 3 = dangerous. */
	private final Long2ByteOpenHashMap openCache = new Long2ByteOpenHashMap();
	/** 0 = unknown, 1 = standable, 2 = not, 3 = swim. */
	private final Long2ByteOpenHashMap standCache = new Long2ByteOpenHashMap();
	/** Water spots already checked for being a trap (1 = trap, 2 = fine). */
	private final Long2ByteOpenHashMap trapCache = new Long2ByteOpenHashMap();
	private BlockPos goal = BlockPos.ZERO;

	private Pathfinder(ServerLevel level) {
		this.level = level;
	}

	private static final class Node implements Comparable<Node> {
		final long pos;
		final double g;
		final double f;
		final @Nullable Node parent;

		Node(long pos, double g, double f, @Nullable Node parent) {
			this.pos = pos;
			this.g = g;
			this.f = f;
			this.parent = parent;
		}

		@Override
		public int compareTo(Node other) {
			return Double.compare(this.f, other.f);
		}
	}

	/**
	 * Path from {@code start} to within {@code reach} blocks of {@code goal}. Never longer than {@code maxRange}
	 * blocks from the start. Returns a partial path toward the closest reachable point when the goal can't be
	 * reached, or null if not even one step is possible.
	 */
	public static @Nullable Path find(ServerLevel level, BlockPos start, BlockPos goal, double reach, int maxRange) {
		return new Pathfinder(level).search(start, goal, reach, maxRange);
	}

	public record Path(List<BlockPos> nodes, boolean complete) {
	}

	private @Nullable Path search(BlockPos start, BlockPos goal, double reach, int maxRange) {
		this.goal = goal;
		BlockPos begin = this.settle(start);
		PriorityQueue<Node> open = new PriorityQueue<>();
		Long2ObjectOpenHashMap<Node> best = new Long2ObjectOpenHashMap<>();
		Node first = new Node(begin.asLong(), 0.0, heuristic(begin, goal), null);
		open.add(first);
		best.put(first.pos, first);
		Node closest = first;
		double closestDistance = distance(begin, goal);
		int expanded = 0;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		List<long[]> neighbours = new ArrayList<>(16);

		while (!open.isEmpty() && expanded < MAX_NODES) {
			Node node = open.poll();
			Node known = best.get(node.pos);
			if (known != null && known.g < node.g) {
				continue;
			}
			expanded++;
			cursor.set(BlockPos.getX(node.pos), BlockPos.getY(node.pos), BlockPos.getZ(node.pos));
			double toGoal = distance(cursor, goal);
			if (toGoal <= reach) {
				return new Path(this.build(node), true);
			}
			if (toGoal < closestDistance) {
				closestDistance = toGoal;
				closest = node;
			}
			neighbours.clear();
			this.neighbours(cursor.immutable(), neighbours);
			for (long[] next : neighbours) {
				long pos = next[0];
				int x = BlockPos.getX(pos);
				int z = BlockPos.getZ(pos);
				if (Math.abs(x - begin.getX()) > maxRange || Math.abs(z - begin.getZ()) > maxRange) {
					continue;
				}
				double g = node.g + Double.longBitsToDouble(next[1]);
				Node old = best.get(pos);
				if (old != null && old.g <= g) {
					continue;
				}
				BlockPos nextPos = BlockPos.of(pos);
				Node child = new Node(pos, g, g + heuristic(nextPos, goal), node);
				best.put(pos, child);
				open.add(child);
			}
		}
		if (closest == first) {
			return null;
		}
		return new Path(this.build(closest), false);
	}

	private List<BlockPos> build(Node end) {
		List<BlockPos> path = new ArrayList<>();
		for (Node node = end; node != null; node = node.parent) {
			path.add(BlockPos.of(node.pos));
		}
		Collections.reverse(path);
		return path;
	}

	private static double heuristic(BlockPos a, BlockPos b) {
		return distance(a, b) * 1.1;
	}

	private static double distance(BlockPos a, BlockPos b) {
		double dx = a.getX() - b.getX();
		double dy = a.getY() - b.getY();
		double dz = a.getZ() - b.getZ();
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/** Mid-jump or standing on a slab: use the first standable block at or below the start. */
	private BlockPos settle(BlockPos start) {
		BlockPos pos = start;
		for (int i = 0; i < 4; i++) {
			if (this.standable(pos) != 2) {
				return pos;
			}
			pos = pos.below();
		}
		return start;
	}

	// ---------------------------------------------------------------- moves

	private void neighbours(BlockPos pos, List<long[]> out) {
		boolean swimming = this.standable(pos) == 3;
		for (int[] dir : DIRECTIONS) {
			boolean diagonal = dir[0] != 0 && dir[1] != 0;
			BlockPos side = pos.offset(dir[0], 0, dir[1]);
			if (diagonal) {
				// No cutting corners.
				if (!this.bodyFits(pos.offset(dir[0], 0, 0)) || !this.bodyFits(pos.offset(0, 0, dir[1]))) {
					continue;
				}
			}
			double stepCost = diagonal ? 1.414 : 1.0;
			int stand = this.standable(side);
			if (stand == 1 || stand == 3) {
				out.add(move(side, stepCost * (stand == 3 ? 2.0 : 1.0) + this.dangerNear(side)));
				continue;
			}
			// Jump up one block (straight moves only): needs room above our head.
			if (!diagonal) {
				BlockPos up = side.above();
				if (this.open(pos.above(2)) == 1 && this.standable(up) == 1) {
					out.add(move(up, 2.0 + this.dangerNear(up)));
					continue;
				}
			}
			// Drop down: the side must be open all the way to the landing spot. At most 3 blocks onto ground
			// (no fall damage), further only into water.
			if (this.bodyFits(side)) {
				for (int drop = 1; drop <= MAX_WATER_DROP; drop++) {
					BlockPos below = side.below(drop);
					int landing = this.standable(below);
					if (landing == 1) {
						if (drop <= MAX_DROP) {
							out.add(move(below, stepCost + drop * 0.5 + this.dangerNear(below)));
						}
						break;
					}
					if (landing == 3) {
						out.add(move(below, stepCost + drop * 0.3));
						break;
					}
					if (this.open(below) != 1) {
						break;
					}
				}
			}
		}
		// Swim straight up or down.
		if (swimming) {
			BlockPos up = pos.above();
			int stand = this.standable(up);
			if (stand == 1 || stand == 3) {
				out.add(move(up, 1.5));
			}
			BlockPos down = pos.below();
			if (this.standable(down) == 3) {
				out.add(move(down, 1.5));
			}
		}
		// Never step or drop from land into a water trap (a village well, a flooded pit): a small pool with no
		// edge low enough to climb back out. Already swimming is fine, so is a trap the goal itself is in.
		if (!swimming) {
			out.removeIf(next -> {
				BlockPos to = BlockPos.of(next[0]);
				return this.standable(to) == 3 && this.isWaterTrap(to);
			});
		}
	}

	/**
	 * True for water you can't get out of: floods the pool's surface (up to 4 blocks out) looking for a bank you
	 * could swim to and step onto (land no higher than one block above the water). A pool that runs past 4 blocks
	 * is open water (a lake, a river), not a trap.
	 */
	private boolean isWaterTrap(BlockPos water) {
		if (water.distManhattan(this.goal) <= 4) {
			return false;
		}
		BlockPos surface = water;
		for (int i = 0; i < 8 && this.isWater(surface.above()); i++) {
			surface = surface.above();
		}
		long key = surface.asLong();
		byte cached = this.trapCache.get(key);
		if (cached != 0) {
			return cached == 1;
		}
		boolean trap = true;
		java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
		java.util.Set<BlockPos> seen = new java.util.HashSet<>();
		queue.add(surface);
		seen.add(surface);
		search:
		while (!queue.isEmpty()) {
			BlockPos at = queue.poll();
			if (Math.abs(at.getX() - surface.getX()) > 4 || Math.abs(at.getZ() - surface.getZ()) > 4) {
				trap = false;
				break;
			}
			for (int[] dir : DIRECTIONS) {
				if (dir[0] != 0 && dir[1] != 0) {
					continue;
				}
				BlockPos side = at.offset(dir[0], 0, dir[1]);
				if (this.isWater(side)) {
					if (seen.add(side)) {
						queue.add(side);
					}
					continue;
				}
				// A bank: stand at water level, or one block up (the jump you can make out of water).
				if (this.standable(side) == 1 || this.standable(side.above()) == 1 && this.open(at.above()) == 1) {
					trap = false;
					break search;
				}
			}
		}
		this.trapCache.put(key, trap ? (byte) 1 : (byte) 2);
		return trap;
	}

	private static long[] move(BlockPos pos, double cost) {
		return new long[]{pos.asLong(), Double.doubleToRawLongBits(cost)};
	}

	/** Extra cost for walking right next to cactus, fire or lava. */
	private double dangerNear(BlockPos pos) {
		for (int[] dir : DIRECTIONS) {
			if (dir[0] != 0 && dir[1] != 0) {
				continue;
			}
			if (this.open(pos.offset(dir[0], 0, dir[1])) == 3) {
				return 4.0;
			}
		}
		return 0.0;
	}

	// ---------------------------------------------------------------- block checks

	private boolean bodyFits(BlockPos feet) {
		return this.open(feet) == 1 && this.open(feet.above()) == 1;
	}

	private boolean isWater(BlockPos pos) {
		return this.level.getFluidState(pos).is(FluidTags.WATER);
	}

	/** 1: standable, 2: no, 3: swimming spot. */
	private int standable(BlockPos feet) {
		long key = feet.asLong();
		byte cached = this.standCache.get(key);
		if (cached != 0) {
			return cached;
		}
		byte result = this.computeStandable(feet);
		this.standCache.put(key, result);
		return result;
	}

	private byte computeStandable(BlockPos feet) {
		if (!this.level.isLoaded(feet) || feet.getY() <= this.level.getMinY() || feet.getY() >= this.level.getMaxY()) {
			return 2;
		}
		BlockState feetState = this.level.getBlockState(feet);
		int feetOpen = this.open(feet);
		int headOpen = this.open(feet.above());
		if (headOpen != 1) {
			return 2;
		}
		if (feetOpen == 1) {
			if (this.isWater(feet)) {
				return 3;
			}
			BlockPos below = feet.below();
			BlockState belowState = this.level.getBlockState(below);
			if (this.isHazard(belowState) || belowState.getFluidState().is(FluidTags.LAVA)) {
				return 2;
			}
			if (this.isWater(below)) {
				return 3;
			}
			VoxelShape shape = belowState.getCollisionShape(this.level, below);
			if (shape.isEmpty() || belowState.getBlock() instanceof DoorBlock || belowState.getBlock() instanceof FenceGateBlock) {
				return 2;
			}
			double top = shape.max(net.minecraft.core.Direction.Axis.Y);
			// Fences and walls are 1.5 tall: you can't stand on them.
			return top >= 0.5 && top <= 1.0 ? (byte) 1 : (byte) 2;
		}
		// A low block in the feet space (slab, carpet, snow layer, path): you stand on top of it, inside this space.
		if (feetOpen == 2 && !feetState.isAir()) {
			VoxelShape shape = feetState.getCollisionShape(this.level, feet);
			if (!shape.isEmpty() && shape.max(net.minecraft.core.Direction.Axis.Y) <= 0.6 && !this.isHazard(feetState)
					&& this.open(feet.above(2)) == 1) {
				return 1;
			}
		}
		return 2;
	}

	/** 1: a player's body can be here, 2: solid, 3: dangerous (lava, fire, cactus...). */
	private int open(BlockPos pos) {
		long key = pos.asLong();
		byte cached = this.openCache.get(key);
		if (cached != 0) {
			return cached;
		}
		byte result = this.computeOpen(pos);
		this.openCache.put(key, result);
		return result;
	}

	private byte computeOpen(BlockPos pos) {
		if (!this.level.isLoaded(pos)) {
			return 2;
		}
		BlockState state = this.level.getBlockState(pos);
		FluidState fluid = state.getFluidState();
		if (fluid.is(FluidTags.LAVA) || this.isHazard(state)) {
			return 3;
		}
		if (state.is(Blocks.COBWEB) || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SWEET_BERRY_BUSH)) {
			return 3;
		}
		if (state.getBlock() instanceof DoorBlock door) {
			return door.type().canOpenByHand() ? (byte) 1 : (byte) 2;
		}
		if (state.getBlock() instanceof FenceGateBlock) {
			return 1;
		}
		return state.getCollisionShape(this.level, pos).isEmpty() ? (byte) 1 : (byte) 2;
	}

	private boolean isHazard(BlockState state) {
		return state.is(BlockTags.FIRE) || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CACTUS)
				|| state.is(BlockTags.CAMPFIRES) || state.is(Blocks.LAVA) || state.is(Blocks.WITHER_ROSE);
	}
}
