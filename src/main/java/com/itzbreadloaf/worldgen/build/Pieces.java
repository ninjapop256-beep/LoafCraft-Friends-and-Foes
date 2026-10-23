package com.itzbreadloaf.worldgen.build;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Reusable building pieces for blueprints: rooms, doors, windows, roofs, stairs, ladders, towers, furniture,
 * gardens, fields, paths and lights. All of them take their materials from the blueprint's palette.
 * Directions are in local coordinates (north = towards z 0, the front).
 */
public final class Pieces {
	private Pieces() {
	}

	// ---------------------------------------------------------------- block states

	public static BlockState stairs(BlockState stairs, Direction facing) {
		return stairs.setValue(StairBlock.FACING, facing);
	}

	public static BlockState upsideDown(BlockState stairs, Direction facing) {
		return stairs.setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, Half.TOP);
	}

	public static BlockState topSlab(BlockState slab) {
		return slab.setValue(SlabBlock.TYPE, SlabType.TOP);
	}

	public static BlockState logAlong(BlockState log, Direction.Axis axis) {
		return log.hasProperty(RotatedPillarBlock.AXIS) ? log.setValue(RotatedPillarBlock.AXIS, axis) : log;
	}

	// ---------------------------------------------------------------- rooms, doors, windows

	/**
	 * A room: floor at y (the whole rectangle, walls included), walls from y + 1 to y + wallHeight with logs in the
	 * corners. The inside is cleared, and its floor is declared so the reachability check looks for it.
	 */
	public static void room(Blueprint b, int x0, int z0, int x1, int z1, int y, int wallHeight, String name) {
		Palette p = b.palette;
		b.fill(x0, y, z0, x1, y, z1, p.planks());
		b.clear(x0 + 1, y + 1, z0 + 1, x1 - 1, y + wallHeight, z1 - 1);
		b.walls(x0, z0, x1, z1, y + 1, y + wallHeight, p.planks());
		for (int[] c : new int[][]{{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}}) {
			b.fill(c[0], y + 1, c[1], c[0], y + wallHeight, c[1], p.log());
		}
		b.floor(x0 + 1, z0 + 1, x1 - 1, z1 - 1, y + 1, name);
	}

	/** A stone room (cellars, workshops, towers): same as {@link #room} in the palette's stone bricks. */
	public static void stoneRoom(Blueprint b, int x0, int z0, int x1, int z1, int y, int wallHeight, String name) {
		Palette p = b.palette;
		b.fill(x0, y, z0, x1, y, z1, p.stoneFloor());
		b.clear(x0 + 1, y + 1, z0 + 1, x1 - 1, y + wallHeight, z1 - 1);
		b.walls(x0, z0, x1, z1, y + 1, y + wallHeight, p.bricks());
		b.floor(x0 + 1, z0 + 1, x1 - 1, z1 - 1, y + 1, name);
	}

	/** A door (two blocks) in a wall, opening outward toward {@code facing}; clears a step in front of it. */
	public static void door(Blueprint b, int x, int y, int z, Direction facing) {
		BlockState lower = b.palette.wood().door().defaultBlockState().setValue(DoorBlock.FACING, facing)
				.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
		b.set(x, y, z, lower);
		b.set(x, y + 1, z, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
	}

	public static void window(Blueprint b, int x, int y, int z) {
		b.set(x, y, z, Blocks.GLASS_PANE);
	}

	// ---------------------------------------------------------------- roofs

	/**
	 * A gable roof over walls x0..x1, z0..z1 with its ridge running along x. {@code y} is the height of the walls' top
	 * row: the eaves overhang one block out at that height, then each row steps up and in, so the roof sits right on
	 * the walls with no gap. The gable ends are filled with planks; an odd depth gets a slab ridge.
	 */
	public static void gableRoof(Blueprint b, int x0, int z0, int x1, int z1, int y) {
		Palette p = b.palette;
		for (int i = 0; ; i++) {
			int front = z0 - 1 + i;
			int back = z1 + 1 - i;
			int level = y + i;
			if (front > back) {
				break;
			}
			if (front == back) {
				b.fill(x0 - 1, level, front, x1 + 1, level, front, p.slab());
				break;
			}
			b.fill(x0 - 1, level, front, x1 + 1, level, front, stairs(p.stairs(), Direction.SOUTH));
			b.fill(x0 - 1, level, back, x1 + 1, level, back, stairs(p.stairs(), Direction.NORTH));
			// Close the triangle at both gable ends (above the walls).
			for (int z = Math.max(front + 1, z0); i > 0 && z <= Math.min(back - 1, z1); z++) {
				b.set(x0, level, z, p.planks());
				b.set(x1, level, z, p.planks());
			}
		}
	}

	/** A hip roof: stairs on all four sides stepping in, a slab cap on top. */
	public static void hipRoof(Blueprint b, int x0, int z0, int x1, int z1, int y) {
		Palette p = b.palette;
		for (int i = 0; ; i++) {
			int ax = x0 - 1 + i;
			int bx = x1 + 1 - i;
			int az = z0 - 1 + i;
			int bz = z1 + 1 - i;
			int level = y + i;
			if (ax > bx || az > bz) {
				break;
			}
			if (ax >= bx - 1 || az >= bz - 1) {
				b.fill(ax, level, az, bx, level, bz, p.slab());
				break;
			}
			b.fill(ax, level, az, bx, level, az, stairs(p.stairs(), Direction.SOUTH));
			b.fill(ax, level, bz, bx, level, bz, stairs(p.stairs(), Direction.NORTH));
			b.fill(ax, level, az + 1, ax, level, bz - 1, stairs(p.stairs(), Direction.EAST));
			b.fill(bx, level, az + 1, bx, level, bz - 1, stairs(p.stairs(), Direction.WEST));
		}
	}

	/** A gable roof with its ridge running along z (same rules as {@link #gableRoof}). */
	public static void gableRoofZ(Blueprint b, int x0, int z0, int x1, int z1, int y) {
		Palette p = b.palette;
		for (int i = 0; ; i++) {
			int left = x0 - 1 + i;
			int right = x1 + 1 - i;
			int level = y + i;
			if (left > right) {
				break;
			}
			if (left == right) {
				b.fill(left, level, z0 - 1, left, level, z1 + 1, p.slab());
				break;
			}
			b.fill(left, level, z0 - 1, left, level, z1 + 1, stairs(p.stairs(), Direction.EAST));
			b.fill(right, level, z0 - 1, right, level, z1 + 1, stairs(p.stairs(), Direction.WEST));
			for (int x = Math.max(left + 1, x0); i > 0 && x <= Math.min(right - 1, x1); x++) {
				b.set(x, level, z0, p.planks());
				b.set(x, level, z1, p.planks());
			}
		}
	}

	public static void flatRoof(Blueprint b, int x0, int z0, int x1, int z1, int y) {
		b.fill(x0, y, z0, x1, y, z1, b.palette.slab());
	}

	public enum Roof { GABLE_X, GABLE_Z, HIP, FLAT }

	/** A roof of the given shape over walls x0..x1, z0..z1 whose top row is at y. */
	public static void roof(Blueprint b, Roof roof, int x0, int z0, int x1, int z1, int y) {
		switch (roof) {
			case GABLE_X -> gableRoof(b, x0, z0, x1, z1, y);
			case GABLE_Z -> gableRoofZ(b, x0, z0, x1, z1, y);
			case HIP -> hipRoof(b, x0, z0, x1, z1, y);
			case FLAT -> {
				b.fill(x0, y + 1, z0, x1, y + 1, z1, b.palette.planks());
				b.walls(x0, z0, x1, z1, y + 2, y + 2, b.palette.slab());
			}
		}
	}

	/** Glass panes every other block along all four walls at height y (not in the corners, not over the door). */
	public static void windowsAround(Blueprint b, int x0, int z0, int x1, int z1, int y, int doorX) {
		for (int x = x0 + 2; x <= x1 - 2; x += 2) {
			if (x != doorX) {
				window(b, x, y, z0);
			}
			window(b, x, y, z1);
		}
		for (int z = z0 + 2; z <= z1 - 2; z += 2) {
			window(b, x0, y, z);
			window(b, x1, y, z);
		}
	}

	/**
	 * A whole little house: a room (floor at y, walls wallHeight tall), a door in the middle of the front wall, windows
	 * all around, a roof, and lanterns inside (two in bigger rooms). Returns the door's x.
	 */
	public static int cottage(Blueprint b, int x0, int z0, int x1, int z1, int y, int wallHeight, Roof roof, String name) {
		room(b, x0, z0, x1, z1, y, wallHeight, name);
		int doorX = (x0 + x1) / 2;
		door(b, doorX, y + 1, z0, Direction.NORTH);
		windowsAround(b, x0, z0, x1, z1, y + 2, doorX);
		roof(b, roof, x0, z0, x1, z1, y + wallHeight);
		lantern(b, x1 - 1, y + 1, z0 + 1, false);
		if (x1 - x0 >= 8 || z1 - z0 >= 8) {
			lantern(b, x0 + 1, y + 1, z1 - 1, false);
		}
		return doorX;
	}

	/**
	 * A walk-in tent, 5 wide and 3 tall, open at the front (z = oz) and closed at the back. Sleeping spots are the
	 * two side rows (x = ox + 1 and ox + 3); the middle row is the aisle.
	 */
	public static void tent(Blueprint b, int ox, int oz, int length, BlockState wool) {
		for (int z = oz; z < oz + length; z++) {
			b.set(ox, 1, z, wool);
			b.set(ox + 4, 1, z, wool);
			b.set(ox + 1, 2, z, wool);
			b.set(ox + 3, 2, z, wool);
			b.set(ox + 2, 3, z, wool);
			b.clear(ox + 1, 1, z, ox + 3, 1, z);
			b.clear(ox + 2, 2, z, ox + 2, 2, z);
		}
		b.fill(ox + 1, 1, oz + length - 1, ox + 3, 2, oz + length - 1, wool);
		b.floor(ox + 2, oz, ox + 2, oz + length - 2, 1, "tent");
	}

	/** A campfire with log benches on the left, right and front (low z) sides; the back is left open. */
	public static void campfire(Blueprint b, int x, int y, int z) {
		b.set(x, y, z, Blocks.CAMPFIRE);
		BlockState bench = logAlong(b.palette.log(), Direction.Axis.Z);
		b.set(x - 2, y, z, bench);
		b.set(x + 2, y, z, bench);
		b.set(x, y, z - 2, logAlong(b.palette.log(), Direction.Axis.X));
	}

	/** A plank dock (deck at y) on log posts down to the ground or the bottom of the water, with a rail on both sides. */
	public static void dock(Blueprint b, int x0, int z0, int x1, int z1, int y) {
		Palette p = b.palette;
		b.fill(x0, y, z0, x1, y, z1, p.planks());
		for (int z = z0; z <= z1; z += 3) {
			b.pillar(x0, y - 1, z, p.log());
			b.pillar(x1, y - 1, z, p.log());
		}
		for (int z = z0 + 1; z <= z1; z++) {
			if (z % 4 != 0) {
				b.set(x0, y + 1, z, p.fence());
				b.set(x1, y + 1, z, p.fence());
			}
		}
	}

	// ---------------------------------------------------------------- getting up

	/**
	 * A ladder from y0 to y1 at (x, z), facing away from the wall it hangs on. Puts a wall block behind any rung
	 * that has nothing to hang on, and clears the rungs' space.
	 */
	public static void ladder(Blueprint b, int x, int y0, int y1, int z, Direction facing) {
		int sx = x - facing.getStepX();
		int sz = z - facing.getStepZ();
		BlockState rung = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
		for (int y = y0; y <= y1; y++) {
			BlockState behind = b.get(sx, y, sz);
			if (behind == null || behind.isAir()) {
				b.set(sx, y, sz, b.palette.planks());
			}
			b.set(x, y, z, rung);
		}
	}

	/** A straight staircase going {@code dir}, one block up per step, with headroom cleared. Returns the top step's y. */
	public static int stairsRun(Blueprint b, int x, int y, int z, Direction dir, int steps) {
		for (int i = 0; i < steps; i++) {
			int sx = x + dir.getStepX() * i;
			int sz = z + dir.getStepZ() * i;
			b.set(sx, y + i, sz, stairs(b.palette.stairs(), dir));
			b.clear(sx, y + i + 1, sz, sx, y + i + 3, sz);
		}
		return y + steps - 1;
	}

	/**
	 * Spiral stairs around a log post at (cx, cz): eight steps per full turn, one block up each, from y0 up to y1.
	 * Floors the stairs pass through must leave the 3x3 around the post open (the caller clears them).
	 */
	public static void spiralStairs(Blueprint b, int cx, int cz, int y0, int y1) {
		int[][] ring = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
		b.fill(cx, y0, cz, cx, y1, cz, b.palette.log());
		for (int k = 0; y0 + k <= y1; k++) {
			int[] cell = ring[k % 8];
			int[] next = ring[(k + 1) % 8];
			int dx = next[0] - cell[0];
			int dz = next[1] - cell[1];
			Direction dir = dx > 0 ? Direction.EAST : dx < 0 ? Direction.WEST : dz > 0 ? Direction.SOUTH : Direction.NORTH;
			int y = y0 + k;
			b.set(cx + cell[0], y, cz + cell[1], stairs(b.palette.stairs(), dir));
			b.clear(cx + cell[0], y + 1, cz + cell[1], cx + cell[0], y + 3, cz + cell[1]);
		}
	}

	/**
	 * A square tower with walls x0..x0+size-1, z0..z0+size-1 from y0 up to the top floor at y0 + height. Floors every
	 * {@code floorEvery} blocks, a ladder up the inside of the back wall through every floor, a window on each side of
	 * every floor, a lantern on each floor. Every floor and the top are declared for the reachability check.
	 */
	public static void tower(Blueprint b, int x0, int z0, int size, int y0, int height, int floorEvery, boolean stone) {
		Palette p = b.palette;
		int x1 = x0 + size - 1;
		int z1 = z0 + size - 1;
		int top = y0 + height;
		BlockState wall = stone ? p.bricks() : p.planks();
		b.fill(x0, y0, z0, x1, y0, z1, stone ? p.stoneFloor() : p.planks());
		b.clear(x0 + 1, y0 + 1, z0 + 1, x1 - 1, top - 1, z1 - 1);
		// Walls go up to the top floor's level, so anything built on the rim (parapets, crenellations) sits on them.
		b.walls(x0, z0, x1, z1, y0 + 1, top, wall);
		for (int[] c : new int[][]{{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}}) {
			b.fill(c[0], y0 + 1, c[1], c[0], top, c[1], p.log());
		}
		int lx = x0 + size / 2;
		int lz = z1 - 1;
		int floorNumber = 0;
		for (int f = y0; f <= top; f += floorEvery) {
			int floorY = Math.min(f, top);
			if (floorY != y0) {
				b.fill(x0 + 1, floorY, z0 + 1, x1 - 1, floorY, z1 - 1, p.planks());
			}
			b.floor(x0 + 1, z0 + 1, x1 - 1, z1 - 1, floorY + 1, floorY == top ? "top" : "floor " + floorNumber++);
			if (floorY < top) {
				int windowY = floorY + 2;
				window(b, x0, windowY, z0 + size / 2);
				window(b, x1, windowY, z0 + size / 2);
				window(b, x0 + 1, windowY, z1);
				b.set(x0 + 1, floorY + 1, z0 + 1, Blocks.LANTERN);
			}
			if (floorY == top) {
				break;
			}
		}
		if ((top - y0) % floorEvery != 0) {
			b.fill(x0 + 1, top, z0 + 1, x1 - 1, top, z1 - 1, p.planks());
			b.floor(x0 + 1, z0 + 1, x1 - 1, z1 - 1, top + 1, "top");
		}
		ladder(b, lx, y0 + 1, top, lz, Direction.NORTH);
	}

	// ---------------------------------------------------------------- furniture

	/** A table (top slab) with stair chairs facing it on the given sides. */
	public static void table(Blueprint b, int x, int y, int z, boolean chairsX, boolean chairsZ) {
		Palette p = b.palette;
		b.set(x, y, z, topSlab(p.slab()));
		if (chairsX) {
			b.set(x - 1, y, z, stairs(p.stairs(), Direction.WEST));
			b.set(x + 1, y, z, stairs(p.stairs(), Direction.EAST));
		}
		if (chairsZ) {
			b.set(x, y, z - 1, stairs(p.stairs(), Direction.NORTH));
			b.set(x, y, z + 1, stairs(p.stairs(), Direction.SOUTH));
		}
	}

	/** A bed with a chest at its foot side and a lantern beside it. */
	public static void bedroom(Blueprint b, int x, int y, int z, String loot) {
		b.bed(x, y, z, b.palette.bed());
		b.chest(x + 1, y, z + 1, loot);
		b.set(x + 1, y, z, Blocks.LANTERN);
	}

	/** Smoker, barrel and a cauldron in a row along x. */
	public static void kitchen(Blueprint b, int x, int y, int z, String barrelLoot) {
		b.set(x, y, z, Blocks.SMOKER);
		b.barrel(x + 1, y, z, barrelLoot);
		b.set(x + 2, y, z, Blocks.WATER_CAULDRON);
	}

	/** A row of bookshelves two high along x, with a lectern in front of the middle. */
	public static void library(Blueprint b, int x, int y, int z, int length) {
		b.fill(x, y, z, x + length - 1, y + 1, z, Blocks.BOOKSHELF);
		b.set(x + length / 2, y, z - 1, Blocks.LECTERN);
	}

	/** Crafting table, furnace and anvil in a row along x. */
	public static void workshop(Blueprint b, int x, int y, int z) {
		b.set(x, y, z, Blocks.CRAFTING_TABLE);
		b.set(x + 1, y, z, Blocks.FURNACE);
		b.set(x + 2, y, z, Blocks.ANVIL);
	}

	public static void lantern(Blueprint b, int x, int y, int z, boolean hanging) {
		b.set(x, y, z, b.palette.light().hasProperty(LanternBlock.HANGING)
				? b.palette.light().setValue(LanternBlock.HANGING, hanging) : b.palette.light());
	}

	// ---------------------------------------------------------------- outside

	/** Flowers (and grass under them) scattered over a rectangle, about one in three spots. */
	public static void garden(Blueprint b, int x0, int z0, int x1, int z1, int y) {
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				b.set(x, y, z, Blocks.GRASS_BLOCK);
				if (b.random.nextInt(3) == 0) {
					Block flower = b.palette.flower(b.random.nextInt(16));
					if (flower != Blocks.AIR) {
						b.set(x, y + 1, z, flower);
					}
				}
			}
		}
	}

	/** Wet farmland with a crop on it and a water channel down the middle (along x). */
	public static void field(Blueprint b, int x0, int z0, int x1, int z1, int y, Block crop) {
		BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);
		int channel = (z0 + z1) / 2;
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				if (z == channel) {
					b.set(x, y, z, Blocks.WATER);
					continue;
				}
				b.set(x, y, z, farmland);
				if (crop instanceof CropBlock c) {
					b.set(x, y + 1, z, c.getStateForAge(b.random.nextInt(c.getMaxAge() + 1)));
				}
			}
		}
	}

	public static void path(Blueprint b, int x0, int z0, int x1, int z1, int y) {
		b.fill(x0, y, z0, x1, y, z1, b.palette.path());
	}

	/** A fence around a rectangle with a gate at (gateX, gateZ). */
	public static void fenceRing(Blueprint b, int x0, int z0, int x1, int z1, int y, int gateX, int gateZ) {
		b.walls(x0, z0, x1, z1, y, y, b.palette.fence());
		b.set(gateX, y, gateZ, b.palette.wood().gate());
	}
}
