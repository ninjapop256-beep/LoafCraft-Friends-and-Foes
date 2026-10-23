package com.itzbreadloaf.worldgen.build;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Walks a blueprint like a player would, starting from the ground all around it: walking, stepping up one block,
 * dropping up to three, climbing ladders, and going through doors, gates and trapdoors (they open). Then it reports
 * every declared floor, chest, barrel, bed and resident spot it couldn't get to. Unset cells count as ground at
 * y <= 0 and air above, which is what the leveling leaves.
 */
public final class Reachability {
	private static final int[][] SIDES = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

	private Reachability() {
	}

	public static List<String> check(Blueprint b) {
		Set<Long> reached = walk(b);
		List<String> problems = new ArrayList<>();
		for (Blueprint.Floor floor : b.floors) {
			boolean ok = false;
			for (int x = floor.x0(); x <= floor.x1() && !ok; x++) {
				for (int z = floor.z0(); z <= floor.z1() && !ok; z++) {
					ok = reached.contains(Blueprint.key(x, floor.y(), z));
				}
			}
			if (!ok) {
				problems.add("floor '" + floor.name() + "' (y " + floor.y() + ") can't be reached");
			}
		}
		for (Blueprint.Container c : b.containers) {
			if (!nextTo(reached, c.x(), c.y(), c.z())) {
				problems.add((c.barrel() ? "barrel" : "chest") + " at " + c.x() + " " + c.y() + " " + c.z() + " can't be reached");
			}
		}
		for (Blueprint.Bed bed : b.beds) {
			if (!nextTo(reached, bed.x(), bed.y(), bed.z()) && !nextTo(reached, bed.x(), bed.y(), bed.z() + 1)) {
				problems.add("bed at " + bed.x() + " " + bed.y() + " " + bed.z() + " can't be reached");
			}
		}
		for (Blueprint.Resident r : b.residents) {
			if (!reached.contains(Blueprint.key(r.x(), r.y(), r.z())) && !nextTo(reached, r.x(), r.y(), r.z())) {
				problems.add(r.variant().displayName() + "'s spawn at " + r.x() + " " + r.y() + " " + r.z() + " can't be reached");
			}
		}
		return problems;
	}

	/** Something you can use from any reachable spot right beside it (or above/below by one). */
	private static boolean nextTo(Set<Long> reached, int x, int y, int z) {
		for (int dy = -1; dy <= 1; dy++) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					if (reached.contains(Blueprint.key(x + dx, y + dy, z + dz))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static Set<Long> walk(Blueprint b) {
		Set<Long> seen = new HashSet<>();
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		// Start on the ground all around the outside.
		for (int x = -2; x <= b.width + 1; x++) {
			for (int z = -2; z <= b.depth + 1; z++) {
				boolean outside = x < 0 || z < 0 || x >= b.width || z >= b.depth;
				if (outside && walkable(b, x, 1, z) && seen.add(Blueprint.key(x, 1, z))) {
					queue.add(new int[]{x, 1, z});
				}
			}
		}
		int minY = b.lowestY() - 1;
		while (!queue.isEmpty()) {
			int[] at = queue.poll();
			int x = at[0];
			int y = at[1];
			int z = at[2];
			for (int[] side : SIDES) {
				int nx = x + side[0];
				int nz = z + side[1];
				if (nx < -3 || nz < -3 || nx > b.width + 2 || nz > b.depth + 2) {
					continue;
				}
				if (walkable(b, nx, y, nz)) {
					visit(seen, queue, nx, y, nz);
				} else if (walkable(b, nx, y + 1, nz) && passable(b, x, y + 2, z)) {
					visit(seen, queue, nx, y + 1, nz);
				} else if (passable(b, nx, y, nz) && passable(b, nx, y + 1, nz)) {
					for (int drop = 1; drop <= 3 && y - drop >= minY; drop++) {
						if (walkable(b, nx, y - drop, nz)) {
							visit(seen, queue, nx, y - drop, nz);
							break;
						}
						if (!passable(b, nx, y - drop, nz)) {
							break;
						}
					}
				}
			}
			// Ladders (and trapdoor hatches on top of them): up and down.
			if (climbable(b, x, y, z)) {
				if (y + 1 < b.height + 2 && passable(b, x, y + 1, z) && passable(b, x, y + 2, z)) {
					visit(seen, queue, x, y + 1, z);
				}
			}
			if (climbable(b, x, y - 1, z) && y - 1 >= minY) {
				visit(seen, queue, x, y - 1, z);
			}
		}
		return seen;
	}

	private static void visit(Set<Long> seen, ArrayDeque<int[]> queue, int x, int y, int z) {
		if (seen.add(Blueprint.key(x, y, z))) {
			queue.add(new int[]{x, y, z});
		}
	}

	/** Feet and head fit, and there's something to stand on (or a ladder to hold). */
	static boolean walkable(Blueprint b, int x, int y, int z) {
		if (!passable(b, x, y, z) || !passable(b, x, y + 1, z)) {
			return false;
		}
		return ladder(b, x, y, z) || standOn(b, x, y - 1, z);
	}

	private static boolean climbable(Blueprint b, int x, int y, int z) {
		BlockState state = b.get(x, y, z);
		return state != null && (state.getBlock() instanceof LadderBlock || state.getBlock() instanceof TrapDoorBlock);
	}

	private static boolean ladder(Blueprint b, int x, int y, int z) {
		BlockState state = b.get(x, y, z);
		return state != null && state.getBlock() instanceof LadderBlock;
	}

	static boolean passable(Blueprint b, int x, int y, int z) {
		BlockState state = b.get(x, y, z);
		if (state == null) {
			return y >= 1;
		}
		if (state.isAir() || state.getBlock() instanceof DoorBlock || state.getBlock() instanceof FenceGateBlock
				|| state.getBlock() instanceof TrapDoorBlock || state.getBlock() instanceof LadderBlock) {
			return true;
		}
		// Low things you walk over: carpets, pressure plates, a thin snow layer.
		VoxelShape shape = shape(state);
		return shape.isEmpty() || shape.max(Direction.Axis.Y) <= 0.25;
	}

	private static boolean standOn(Blueprint b, int x, int y, int z) {
		BlockState state = b.get(x, y, z);
		if (state == null) {
			return y <= 0;
		}
		if (state.getBlock() instanceof LadderBlock || state.getBlock() instanceof TrapDoorBlock) {
			return true;
		}
		VoxelShape shape = shape(state);
		// Fences and walls are 1.5 tall: you can't stand on them in a normal way.
		return !shape.isEmpty() && shape.max(Direction.Axis.Y) <= 1.0;
	}

	private static VoxelShape shape(BlockState state) {
		return state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
	}
}
