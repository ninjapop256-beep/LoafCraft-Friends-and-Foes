package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** ImLilyLol's structures (her biome cottages, gardens, libraries and lookouts), remade on the blueprint system. */
final class LilyBlueprints {
	/** Flowers Lily grows anywhere: her gardens don't depend on the biome. */
	static final Block[] FLOWERS = {Blocks.POPPY, Blocks.DANDELION, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET,
			Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER,
			Blocks.LILY_OF_THE_VALLEY, Blocks.TORCHFLOWER};
	private static final Block[] POTTED_SAPLINGS = {Blocks.POTTED_OAK_SAPLING, Blocks.POTTED_BIRCH_SAPLING, Blocks.POTTED_CHERRY_SAPLING,
			Blocks.POTTED_SPRUCE_SAPLING, Blocks.POTTED_AZALEA, Blocks.POTTED_FLOWERING_AZALEA};

	private LilyBlueprints() {
	}

	static BlockPos at(int x, int y, int z) {
		return new BlockPos(x, y, z);
	}

	/** One of these variants, picked from the structure's own random. */
	static CharacterVariant pick(Blueprint b, CharacterVariant... variants) {
		return variants[b.random.nextInt(variants.length)];
	}

	/** Leaves that never decay. */
	static BlockState leaves(Block block) {
		BlockState state = block.defaultBlockState();
		return state.hasProperty(LeavesBlock.PERSISTENT) ? state.setValue(LeavesBlock.PERSISTENT, true) : state;
	}

	static Block flower(Blueprint b) {
		return FLOWERS[b.random.nextInt(FLOWERS.length)];
	}

	/** Grass with Lily's flowers on it ({@code chance} of the cells). */
	static void flowerBed(Blueprint b, int x0, int z0, int x1, int z1, float chance) {
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				b.set(x, 0, z, Blocks.GRASS_BLOCK);
				if (b.random.nextFloat() < chance) {
					b.set(x, 1, z, flower(b));
				}
			}
		}
	}

	/** A path on the ground with nothing growing on it. */
	private static void clearPath(Blueprint b, int x0, int z0, int x1, int z1) {
		Pieces.path(b, x0, z0, x1, z1, 0);
		b.clear(x0, 1, z0, x1, 1, z1);
	}

	/** A fence post with a lantern on top. */
	private static void lampPost(Blueprint b, int x, int z) {
		b.set(x, 1, z, b.palette.fence());
		Pieces.lantern(b, x, 2, z, false);
	}

	private static double dist(int x, int z, double cx, double cz) {
		return Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
	}

	// ================================================================ Lily's homes

	/** ImLilyLol's House: a cherry cottage with a reading corner, in a yard covered in pink petals. */
	static void lilyHouse(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				if (b.random.nextInt(5) < 2) {
					b.set(x, 1, z, Blocks.PINK_PETALS);
				}
			}
		}
		int door = Pieces.cottage(b, 2, 2, 8, 10, 0, 4, Pieces.Roof.GABLE_Z, "lily's house");
		clearPath(b, door, 0, door, 1);
		b.bed(3, 1, 6, p.bed());
		b.chest(3, 1, 8, "chests/lily_house");
		Pieces.library(b, 5, 1, 9, 3);
		Pieces.table(b, 6, 1, 5, true, false);
		b.set(3, 1, 3, Blocks.CRAFTING_TABLE);
		for (int[] c : new int[][]{{0, 0}, {10, 0}, {0, 12}, {10, 12}}) {
			b.set(c[0], 1, c[1], Blocks.FLOWERING_AZALEA);
		}
		b.resident(pick(b, CharacterVariant.LILY, CharacterVariant.GARDENER, CharacterVariant.WITCHY), 5, 1, 4, 5, 6, 12,
				at(3, 1, 6), null, false);
	}

	/** Meadow Cottage: a little stone-and-wood cottage in a fenced flower garden, with a beehive out back. */
	static void meadowCottage(Blueprint b) {
		Palette p = b.palette;
		Pieces.garden(b, 0, 0, 10, 10, 0);
		int door = Pieces.cottage(b, 2, 2, 8, 7, 0, 3, Pieces.Roof.GABLE_X, "meadow cottage");
		// Stone along the bottom of the walls.
		for (int x = 2; x <= 8; x++) {
			b.set(x, 1, 2, p.bricks());
			b.set(x, 1, 7, p.bricks());
		}
		for (int z = 3; z <= 6; z++) {
			b.set(2, 1, z, p.bricks());
			b.set(8, 1, z, p.bricks());
		}
		Pieces.door(b, door, 1, 2, Direction.NORTH);
		clearPath(b, door, 0, door, 1);
		Pieces.fenceRing(b, 0, 0, 10, 10, 1, door, 0);
		b.bed(3, 1, 5, p.bed());
		b.chest(4, 1, 6, "chests/lily_meadow_cottage");
		b.set(7, 1, 6, Blocks.CRAFTING_TABLE);
		b.set(6, 1, 6, Blocks.FURNACE);
		Pieces.table(b, 6, 1, 4, false, false);
		b.set(3, 1, 3, Blocks.POTTED_CORNFLOWER);
		b.beehive(9, 1, 9);
		b.resident(pick(b, CharacterVariant.LILY, CharacterVariant.GARDENER), 5, 1, 1, 5, 5, 12, at(3, 1, 5), null, false);
	}

	/** Swaps some of a wall's planks for mossy cobblestone. */
	private static void mossy(Blueprint b, int x, int y, int z) {
		if (b.get(x, y, z) == b.palette.planks() && b.random.nextInt(5) < 2) {
			b.set(x, y, z, Blocks.MOSSY_COBBLESTONE);
		}
	}

	/** A vine at (x, y, z) growing on the wall on its {@code side} (if the spot is free). */
	private static void vine(Blueprint b, int x, int y, int z, Direction side) {
		if (b.isSet(x, y, z) || b.random.nextInt(2) == 0) {
			return;
		}
		BooleanProperty face = side == Direction.NORTH ? VineBlock.NORTH : side == Direction.SOUTH ? VineBlock.SOUTH
				: side == Direction.EAST ? VineBlock.EAST : VineBlock.WEST;
		b.set(x, y, z, Blocks.VINE.defaultBlockState().setValue(face, true));
	}

	/** Flower Forest Cottage: a mossy cottage overgrown with vines, flowers all around, a little potion corner. */
	static void flowerCottage(Blueprint b) {
		Palette p = b.palette;
		Pieces.garden(b, 0, 0, 10, 10, 0);
		int door = Pieces.cottage(b, 2, 2, 8, 8, 0, 3, Pieces.Roof.HIP, "flower cottage");
		for (int y = 1; y <= 3; y++) {
			for (int i = 3; i <= 7; i++) {
				mossy(b, i, y, 2);
				mossy(b, i, y, 8);
				mossy(b, 2, y, i);
				mossy(b, 8, y, i);
			}
		}
		for (int i = 3; i <= 7; i++) {
			if (i != door) {
				vine(b, i, 2, 1, Direction.SOUTH);
			}
			vine(b, i, 2, 9, Direction.NORTH);
			vine(b, 1, 2, i, Direction.EAST);
			vine(b, 9, 2, i, Direction.WEST);
		}
		clearPath(b, door, 0, door, 1);
		b.bed(3, 1, 6, p.bed());
		b.chest(4, 1, 7, "chests/lily_flower_cottage");
		b.set(7, 1, 7, Blocks.BREWING_STAND);
		b.set(7, 1, 6, Blocks.CAULDRON);
		b.set(6, 1, 7, Blocks.POTTED_ALLIUM);
		Pieces.table(b, 6, 1, 4, false, false);
		b.resident(pick(b, CharacterVariant.LILY, CharacterVariant.GARDENER, CharacterVariant.WITCHY), 5, 1, 1, 5, 5, 12,
				at(3, 1, 6), null, false);
	}

	/** Reading Cabin: bookshelves along the walls, a reading table, and Lily's bed in a loft up a ladder. */
	static void readingCabin(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 1, 2, 9, 8, 0, 4, Pieces.Roof.GABLE_X, "reading cabin");
		Pieces.path(b, door, 0, door, 1, 0);
		Pieces.library(b, 2, 1, 7, 7);
		b.fill(2, 1, 4, 2, 2, 5, Blocks.BOOKSHELF);
		Pieces.table(b, 6, 1, 5, true, false);
		b.chest(3, 1, 6, "chests/lily_reading_cabin");
		// The loft: a floor under the roof, reached by a ladder on the east wall.
		b.fill(2, 4, 3, 8, 4, 7, p.planks());
		Pieces.ladder(b, 8, 1, 4, 5, Direction.WEST);
		b.floor(2, 4, 8, 6, 5, "loft");
		b.bed(4, 5, 4, p.bed());
		Pieces.lantern(b, 2, 5, 5, false);
		b.resident(pick(b, CharacterVariant.BOOKWORM, CharacterVariant.LILY), door, 1, 1, 5, 5, 10, at(4, 5, 4), null, false);
	}

	/** Snowy Spruce Cabin: a stone fireplace with a chimney, a rug, a kitchen corner and lamp posts by the door. */
	static void snowyCabin(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 1, 1, 9, 8, 0, 3, Pieces.Roof.GABLE_X, "snowy cabin");
		Pieces.path(b, door, 0, door, 0, 0);
		// Fireplace in the back wall, chimney up past the roof.
		b.fill(4, 1, 8, 6, 3, 8, p.bricks());
		b.set(5, 1, 8, Blocks.CAMPFIRE);
		b.fill(5, 1, 9, 5, 8, 9, p.bricks());
		b.fill(4, 1, 4, 6, 1, 6, Blocks.RED_CARPET);
		b.bed(2, 1, 5, p.bed());
		b.chest(3, 1, 7, "chests/lily_snowy_cabin");
		Pieces.kitchen(b, 6, 1, 7, "chests/barrel_supplies");
		Pieces.table(b, 7, 1, 4, false, true);
		lampPost(b, 3, 0);
		lampPost(b, 7, 0);
		b.fill(7, 1, 10, 9, 2, 10, Pieces.logAlong(p.log(), Direction.Axis.X));
		b.resident(pick(b, CharacterVariant.LILY, CharacterVariant.WITCHY, CharacterVariant.BOOKWORM), 6, 1, 0, 5, 5, 10,
				at(2, 1, 5), null, false);
	}

	/** Mushroom House: a round mushroom-stem room under a big red cap, little mushrooms all around. */
	static void mushroomHouse(Blueprint b) {
		Palette p = b.palette;
		BlockState stem = Blocks.MUSHROOM_STEM.defaultBlockState();
		b.fill(3, 0, 3, 9, 0, 9, p.planks());
		for (int y = 1; y <= 4; y++) {
			for (int x = 3; x <= 9; x++) {
				for (int z = 3; z <= 9; z++) {
					boolean edgeX = x == 3 || x == 9;
					boolean edgeZ = z == 3 || z == 9;
					if (edgeX && edgeZ) {
						continue;
					}
					if (edgeX || edgeZ) {
						b.set(x, y, z, stem);
					} else {
						b.set(x, y, z, Blocks.AIR);
					}
				}
			}
		}
		b.floor(4, 4, 8, 8, 1, "mushroom house");
		Pieces.door(b, 6, 1, 3, Direction.NORTH);
		Pieces.window(b, 3, 2, 6);
		Pieces.window(b, 9, 2, 6);
		Pieces.window(b, 6, 2, 9);
		double[] radius = {5.5, 4.5, 2.5};
		for (int i = 0; i < 3; i++) {
			for (int x = 0; x < b.width; x++) {
				for (int z = 0; z < b.depth; z++) {
					if (dist(x, z, 6, 6) <= radius[i]) {
						b.set(x, 5 + i, z, Blocks.RED_MUSHROOM_BLOCK);
					}
				}
			}
		}
		b.bed(4, 1, 7, p.bed());
		b.chest(5, 1, 8, "chests/lily_mushroom_house");
		b.set(8, 1, 8, Blocks.BREWING_STAND);
		b.set(8, 1, 7, Blocks.CRAFTING_TABLE);
		Pieces.lantern(b, 8, 1, 4, false);
		b.set(4, 1, 4, Blocks.POTTED_RED_MUSHROOM);
		Pieces.path(b, 6, 0, 6, 2, 0);
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				if (dist(x, z, 6, 6) > 3.6 && !b.isSet(x, 1, z) && x != 6 && b.random.nextInt(9) == 0) {
					b.set(x, 1, z, b.random.nextBoolean() ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM);
				}
			}
		}
		b.resident(pick(b, CharacterVariant.LILY, CharacterVariant.WITCHY), 6, 1, 5, 6, 6, 10, at(4, 1, 7), null, false);
	}

	// ================================================================ up in the trees

	/**
	 * Lily's Treehouse: a library room on a platform around a big trunk, stairs winding up the trunk into the room,
	 * a balcony all around and the tree's canopy on top. Bookworm Lily.
	 */
	static void treehouse(Blueprint b) {
		Palette p = b.palette;
		BlockState log = p.log();
		int floorY = 10;
		b.fill(0, floorY, 0, 14, floorY, 14, p.planks());
		for (int[] c : new int[][]{{0, 0}, {14, 0}, {0, 14}, {14, 14}}) {
			b.fill(c[0], 1, c[1], c[0], floorY - 1, c[1], log);
		}
		Pieces.room(b, 2, 2, 12, 12, floorY, 4, "library");
		Pieces.windowsAround(b, 2, 2, 12, 12, floorY + 2, 7);
		Pieces.door(b, 7, floorY + 1, 2, Direction.NORTH);
		b.walls(0, 0, 14, 14, floorY + 1, floorY + 1, p.fence());
		b.floor(1, 1, 13, 1, floorY + 1, "balcony");
		Pieces.roof(b, Pieces.Roof.FLAT, 2, 2, 12, 12, floorY + 4);
		// The trunk, through the floor and the roof.
		b.fill(6, 1, 6, 8, floorY + 6, 8, log);
		BlockState leaves = leaves(p.wood().leaves());
		double[] radius = {6.2, 5.0, 3.0};
		for (int i = 0; i < 3; i++) {
			for (int x = 0; x < b.width; x++) {
				for (int z = 0; z < b.depth; z++) {
					if (dist(x, z, 7, 7) <= radius[i] && !b.isSet(x, floorY + 6 + i, z)) {
						b.set(x, floorY + 6 + i, z, leaves);
					}
				}
			}
		}
		// Stairs up around the trunk, coming out through the floor; a rail around the stairwell.
		Pieces.ringStairs(b, 6, 6, 8, 8, 1, floorY);
		for (int[] f : new int[][]{{9, 6}, {10, 7}, {10, 8}, {10, 9}, {10, 10}, {9, 10}}) {
			b.set(f[0], floorY + 1, f[1], p.fence());
		}
		// The library.
		Pieces.library(b, 3, floorY + 1, 11, 9);
		b.fill(3, floorY + 1, 4, 3, floorY + 2, 8, Blocks.BOOKSHELF);
		b.bed(4, floorY + 1, 3, p.bed());
		b.chest(11, floorY + 1, 3, "chests/treehouse");
		Pieces.lantern(b, 5, floorY + 4, 9, true);
		Pieces.lantern(b, 10, floorY + 4, 4, true);
		b.resident(CharacterVariant.BOOKWORM, 7, 1, 3, 7, 7, 12, at(4, floorY + 1, 3), null, false);
	}

	/**
	 * Treetop Library: a bookshelf room on a platform held up by four tall birch trees, spiral stairs up a post in
	 * the middle, a railed balcony all around. Bookworm Lily.
	 */
	static void treetopLibrary(Blueprint b) {
		Palette p = b.palette;
		BlockState birch = Blocks.BIRCH_LOG.defaultBlockState();
		BlockState birchLeaves = leaves(Blocks.BIRCH_LEAVES);
		int floorY = 14;
		b.fill(1, floorY, 1, 13, floorY, 13, p.planks());
		Pieces.room(b, 3, 3, 11, 11, floorY, 4, "library");
		Pieces.roof(b, Pieces.Roof.GABLE_X, 3, 3, 11, 11, floorY + 4);
		Pieces.windowsAround(b, 3, 3, 11, 11, floorY + 2, 7);
		Pieces.door(b, 7, floorY + 1, 3, Direction.NORTH);
		b.walls(1, 1, 13, 13, floorY + 1, floorY + 1, p.fence());
		b.floor(2, 2, 12, 2, floorY + 1, "balcony");
		// Four tall birches at the platform's corners, their crowns above it.
		for (int[] c : new int[][]{{1, 1}, {13, 1}, {1, 13}, {13, 13}}) {
			b.fill(c[0], 1, c[1], c[0], floorY + 5, c[1], birch);
			for (int y = floorY + 3; y <= floorY + 6; y++) {
				int r = y == floorY + 6 ? 0 : 1;
				for (int dx = -r; dx <= r; dx++) {
					for (int dz = -r; dz <= r; dz++) {
						int x = c[0] + dx;
						int z = c[1] + dz;
						if (x >= 0 && z >= 0 && x < b.width && z < b.depth && !b.isSet(x, y, z)) {
							b.set(x, y, z, birchLeaves);
						}
					}
				}
			}
		}
		// Spiral stairs up a post in the middle, into the library; a rail around the stairwell.
		Pieces.spiralStairs(b, 7, 7, 1, floorY);
		for (int[] f : new int[][]{{8, 6}, {9, 7}, {9, 8}, {9, 9}, {8, 9}, {7, 9}}) {
			b.set(f[0], floorY + 1, f[1], p.fence());
		}
		b.set(7, floorY + 1, 7, Blocks.LECTERN);
		b.fill(4, floorY + 1, 10, 10, floorY + 2, 10, Blocks.BOOKSHELF);
		b.fill(4, floorY + 1, 6, 4, floorY + 2, 8, Blocks.BOOKSHELF);
		b.fill(10, floorY + 1, 6, 10, floorY + 2, 8, Blocks.BOOKSHELF);
		Pieces.lantern(b, 4, floorY + 3, 8, false);
		Pieces.lantern(b, 10, floorY + 3, 8, false);
		b.bed(10, floorY + 1, 4, p.bed());
		b.chest(4, floorY + 1, 4, "chests/treetop_library");
		b.resident(CharacterVariant.BOOKWORM, 7, 1, 3, 7, 7, 12, at(10, floorY + 1, 4), null, false);
	}

	/**
	 * Floating Island Cottage: a sky island over a cherry grove with Lily's cottage and a little cherry tree on it,
	 * roots hanging underneath, and spiral stairs up a cherry post from the ground to the island's edge.
	 */
	static void floatingIsland(Blueprint b) {
		Palette p = b.palette;
		int top = 20;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 7, 7);
				if (d > 6.5) {
					continue;
				}
				int depth = 1 + (int) ((6.5 - d) * 0.9);
				b.set(x, top, z, Blocks.GRASS_BLOCK);
				for (int y = top - depth; y < top; y++) {
					b.set(x, y, z, y == top - depth && depth > 2 ? Blocks.STONE : Blocks.DIRT);
				}
				if (depth >= 2 && b.random.nextInt(4) == 0) {
					b.set(x, top - depth - 1, z, Blocks.HANGING_ROOTS);
				}
			}
		}
		int door = Pieces.cottage(b, 4, 4, 10, 9, top, 3, Pieces.Roof.GABLE_X, "island cottage");
		b.bed(5, top + 1, 7, p.bed());
		b.chest(6, top + 1, 8, "chests/floating_island_cottage");
		b.set(9, top + 1, 8, Blocks.CRAFTING_TABLE);
		b.set(8, top + 1, 8, Blocks.POTTED_PINK_TULIP);
		Pieces.table(b, 8, top + 1, 6, false, false);
		// Spiral stairs up a cherry post on the east side, from the ground to the island's edge.
		Pieces.spiralStairs(b, 13, 7, 1, top);
		Pieces.lantern(b, 13, top + 1, 7, false);
		// A little cherry tree beside the cottage.
		BlockState cherryLeaves = leaves(Blocks.CHERRY_LEAVES);
		b.fill(3, top + 1, 11, 3, top + 4, 11, Blocks.CHERRY_LOG);
		for (int y = top + 4; y <= top + 6; y++) {
			int r = y == top + 4 ? 2 : y == top + 5 ? 1 : 0;
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.abs(dx) + Math.abs(dz) <= r + 1 && !b.isSet(3 + dx, y, 11 + dz)) {
						b.set(3 + dx, y, 11 + dz, cherryLeaves);
					}
				}
			}
		}
		// Petals on the open grass.
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				if (b.get(x, top, z) == Blocks.GRASS_BLOCK.defaultBlockState() && !b.isSet(x, top + 1, z) && b.random.nextInt(4) == 0) {
					b.set(x, top + 1, z, Blocks.PINK_PETALS);
				}
			}
		}
		b.clear(door - 1, top + 1, 1, door + 1, top + 1, 3);
		b.floor(door - 1, 1, door + 1, 3, top + 1, "island front");
		b.resident(CharacterVariant.LILY, door, top + 1, 2, 7, 7, 10, at(5, top + 1, 7), null, false);
	}

	/**
	 * Starwatch Observatory: a stone tower with a ladder through three floors (a map room, Lily's bedroom with
	 * bookshelves) up to a glass dome with a telescope. Bookworm Lily.
	 */
	static void starwatchObservatory(Blueprint b) {
		Palette p = b.palette;
		BlockState glass = Blocks.GLASS.defaultBlockState();
		Pieces.tower(b, 2, 2, 7, 0, 8, 4, true);
		Pieces.door(b, 5, 1, 2, Direction.NORTH);
		Pieces.path(b, 5, 0, 5, 1, 0);
		// The glass dome.
		b.walls(2, 2, 8, 8, 9, 10, glass);
		b.fill(2, 11, 2, 8, 11, 8, glass);
		b.clear(4, 11, 4, 6, 11, 6);
		b.fill(3, 12, 3, 7, 12, 7, glass);
		b.set(5, 9, 5, p.fence());
		b.set(5, 10, 5, Blocks.END_ROD);
		b.set(3, 9, 7, Blocks.LECTERN);
		b.chest(7, 9, 3, "chests/starwatch_observatory");
		// Middle floor: Lily's bedroom.
		b.bed(3, 5, 5, p.bed());
		b.fill(7, 5, 3, 7, 6, 6, Blocks.BOOKSHELF);
		// Ground floor: the map room.
		b.set(7, 1, 3, Blocks.CARTOGRAPHY_TABLE);
		b.barrel(7, 1, 6, "chests/barrel_supplies");
		b.resident(CharacterVariant.BOOKWORM, 5, 1, 1, 5, 5, 10, at(3, 5, 5), null, false);
	}

	// ================================================================ gardens

	/** Greenhouse: glass walls and a raised glass roof, flower beds either side of a path, potted saplings, a composter. */
	static void greenhouse(Blueprint b) {
		Palette p = b.palette;
		BlockState glass = Blocks.GLASS.defaultBlockState();
		b.fill(1, 0, 1, 9, 0, 11, Blocks.GRASS_BLOCK);
		b.walls(1, 1, 9, 11, 1, 1, p.bricks());
		b.walls(1, 1, 9, 11, 2, 4, glass);
		for (int[] c : new int[][]{{1, 1}, {9, 1}, {1, 11}, {9, 11}, {1, 6}, {9, 6}}) {
			b.fill(c[0], 1, c[1], c[0], 4, c[1], p.log());
		}
		b.fill(1, 5, 1, 9, 5, 11, glass);
		b.clear(3, 5, 3, 7, 5, 9);
		b.fill(3, 6, 3, 7, 6, 9, glass);
		b.clear(2, 1, 2, 8, 4, 10);
		Pieces.door(b, 5, 1, 1, Direction.NORTH);
		Pieces.path(b, 5, 0, 5, 1, 0);
		Pieces.path(b, 5, 2, 5, 10, 0);
		for (int x : new int[]{2, 3, 4, 6, 7, 8}) {
			for (int z = 2; z <= 9; z++) {
				if (b.random.nextInt(4) != 0) {
					b.set(x, 1, z, flower(b));
				}
			}
		}
		for (int x : new int[]{3, 7}) {
			b.set(x, 0, 6, Blocks.WATER);
			b.clear(x, 1, 6, x, 1, 6);
		}
		for (int x = 2; x <= 8; x++) {
			if (x != 5) {
				b.set(x, 1, 10, p.planks());
				if (x % 2 == 0) {
					b.set(x, 2, 10, POTTED_SAPLINGS[b.random.nextInt(POTTED_SAPLINGS.length)]);
				}
			}
		}
		Pieces.lantern(b, 5, 1, 10, false);
		Pieces.lantern(b, 5, 5, 6, true);
		b.chest(8, 1, 2, "chests/greenhouse");
		b.set(2, 1, 2, Blocks.COMPOSTER);
		b.floor(2, 2, 8, 10, 1, "greenhouse");
		b.resident(CharacterVariant.GARDENER, 5, 1, 3, 5, 6, 10);
	}

	/** Lakeside Gazebo: a roofed gazebo with benches facing a little pond with lily pads and reeds, and a small dock. */
	static void lakesideGazebo(Blueprint b) {
		Palette p = b.palette;
		Pieces.garden(b, 0, 0, 14, 12, 0);
		boolean[][] water = new boolean[b.width][b.depth];
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double dx = (x - 10.5) / 3.6;
				double dz = (z - 6.0) / 4.2;
				double d = dx * dx + dz * dz;
				if (d <= 1.0) {
					water[x][z] = true;
					b.set(x, 0, z, Blocks.WATER);
					if (d <= 0.55) {
						b.set(x, -1, z, Blocks.WATER);
					}
					if (b.random.nextInt(7) == 0) {
						b.set(x, 1, z, Blocks.LILY_PAD);
					} else {
						b.clear(x, 1, z, x, 1, z);
					}
				}
			}
		}
		// Reeds where the bank touches the water.
		for (int x = 1; x < b.width - 1; x++) {
			for (int z = 1; z < b.depth - 1; z++) {
				boolean bank = !water[x][z] && (water[x - 1][z] || water[x + 1][z] || water[x][z - 1] || water[x][z + 1]);
				if (bank && x > 7 && b.random.nextInt(3) == 0) {
					b.set(x, 1, z, Blocks.SUGAR_CANE);
				}
			}
		}
		// The gazebo: a plank floor, four posts and a hip roof.
		b.fill(2, 0, 4, 6, 0, 8, p.planks());
		b.clear(2, 1, 4, 6, 4, 8);
		for (int[] c : new int[][]{{2, 4}, {6, 4}, {2, 8}, {6, 8}}) {
			b.fill(c[0], 1, c[1], c[0], 4, c[1], p.fence());
		}
		Pieces.hipRoof(b, 2, 4, 6, 8, 4);
		Pieces.lantern(b, 4, 6, 6, true);
		for (int z = 5; z <= 7; z++) {
			b.set(3, 1, z, Pieces.stairs(p.stairs(), Direction.WEST));
		}
		b.chest(5, 1, 5, "chests/lakeside_gazebo");
		b.floor(4, 5, 5, 7, 1, "gazebo");
		b.fill(7, 0, 6, 8, 0, 6, p.planks());
		b.clear(7, 1, 6, 8, 1, 6);
		Pieces.path(b, 4, 0, 4, 3, 0);
		b.clear(4, 1, 0, 4, 1, 3);
		b.resident(pick(b, CharacterVariant.BOOKWORM, CharacterVariant.LILY), 4, 1, 4, 5, 6, 12);
	}

	/** Flower Shrine: a little stone shrine with a candle-lit altar, flower rings, four paths and lamp posts. */
	static void flowerShrine(Blueprint b) {
		Palette p = b.palette;
		Pieces.garden(b, 0, 0, 10, 10, 0);
		clearPath(b, 5, 0, 5, 10);
		clearPath(b, 0, 5, 10, 5);
		b.fill(3, 0, 3, 7, 0, 7, p.stoneFloor());
		b.clear(3, 1, 3, 7, 3, 7);
		for (int[] c : new int[][]{{3, 3}, {7, 3}, {3, 7}, {7, 7}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.stoneWall());
		}
		b.fill(2, 4, 2, 8, 4, 8, p.stoneSlab());
		b.fill(4, 5, 4, 6, 5, 6, p.stoneSlab());
		b.set(5, 5, 5, p.bricks());
		Pieces.lantern(b, 5, 6, 5, false);
		Pieces.lantern(b, 5, 3, 5, true);
		b.set(5, 1, 6, Blocks.CHISELED_STONE_BRICKS);
		b.set(5, 2, 6, Blocks.PINK_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
		b.chest(5, 1, 7, "chests/flower_shrine");
		// A ring of flowers around the shrine.
		for (int i = 2; i <= 8; i++) {
			for (int[] c : new int[][]{{i, 2}, {i, 8}, {2, i}, {8, i}}) {
				if (c[0] != 5 && c[1] != 5) {
					b.set(c[0], 0, c[1], Blocks.GRASS_BLOCK);
					b.set(c[0], 1, c[1], FLOWERS[(c[0] + c[1]) % FLOWERS.length]);
				}
			}
		}
		for (int[] c : new int[][]{{1, 1}, {9, 1}, {1, 9}, {9, 9}}) {
			lampPost(b, c[0], c[1]);
		}
		b.floor(4, 4, 6, 6, 1, "shrine");
		b.resident(CharacterVariant.GARDENER, 5, 1, 1, 5, 5, 10);
	}

	/** Lantern Garden: a hedged garden lit by lamp posts, cross paths to a lily pond in the middle, benches. */
	static void lanternGarden(Blueprint b) {
		Palette p = b.palette;
		flowerBed(b, 0, 0, 12, 12, 0.35F);
		b.walls(0, 0, 12, 12, 1, 1, leaves(p.wood().leaves()));
		clearPath(b, 6, 0, 6, 12);
		clearPath(b, 0, 6, 12, 6);
		for (int x = 5; x <= 7; x++) {
			for (int z = 5; z <= 7; z++) {
				b.set(x, 0, z, Blocks.WATER);
				b.set(x, -1, z, Blocks.WATER);
				b.clear(x, 1, z, x, 1, z);
			}
		}
		b.set(5, 1, 7, Blocks.LILY_PAD);
		b.set(7, 1, 5, Blocks.LILY_PAD);
		for (int[] c : new int[][]{{4, 2}, {8, 2}, {2, 4}, {2, 8}, {10, 4}, {10, 8}, {4, 10}, {8, 10}}) {
			lampPost(b, c[0], c[1]);
		}
		for (int z : new int[]{2, 10}) {
			b.set(5, 1, z, Pieces.stairs(p.stairs(), Direction.WEST));
			b.set(7, 1, z, Pieces.stairs(p.stairs(), Direction.EAST));
		}
		b.chest(11, 1, 11, "chests/lantern_garden");
		b.floor(1, 1, 11, 11, 1, "garden");
		b.resident(CharacterVariant.GARDENER, 6, 1, 1, 6, 6, 10);
	}
}
