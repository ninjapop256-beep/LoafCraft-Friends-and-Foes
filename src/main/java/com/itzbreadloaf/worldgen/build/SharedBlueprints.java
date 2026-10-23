package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import com.itzbreadloaf.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

/** Places everyone shares (the LoafCraft Village, camps, the arena, the festival) and the Giant Loaf. */
final class SharedBlueprints {
	private SharedBlueprints() {
	}

	private static BlockPos at(int x, int y, int z) {
		return new BlockPos(x, y, z);
	}

	private static double dist(int x, int z, double cx, double cz) {
		return Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
	}

	private static void lampPost(Blueprint b, int x, int z) {
		b.set(x, 1, z, b.palette.fence());
		Pieces.lantern(b, x, 2, z, false);
	}

	/** A cabin (cottage, gable roof) with its door in the front wall, or in the back wall when {@code doorSouth}. */
	private static int cabin(Blueprint b, int x0, int z0, int x1, int z1, boolean doorSouth, String name) {
		int door = Pieces.cottage(b, x0, z0, x1, z1, 0, 3, Pieces.Roof.GABLE_X, name);
		if (doorSouth) {
			b.set(door, 1, z0, b.palette.planks());
			Pieces.window(b, door, 2, z0);
			Pieces.door(b, door, 1, z1, Direction.SOUTH);
		}
		return door;
	}

	// ================================================================ LoafCraft Village

	/**
	 * LoafCraft Village: paths crossing at a fenced, roofed well (nobody can fall in), a bell, two ItzBreadLoaf
	 * cabins, Archer Lily's cabin with targets, a fishing pond with a dock, Baker Madison's booth, a market corner and
	 * a fenced farm. FelixSyn drifts in sometimes (felixVillageChance).
	 */
	static void village(Blueprint b) {
		Palette p = b.palette;
		Pieces.path(b, 14, 0, 16, 30, 0);
		Pieces.path(b, 0, 14, 30, 16, 0);
		// The well: two blocks of water under a fence ring and a roof, so nobody falls in.
		b.fill(13, 0, 13, 17, 0, 17, p.stoneBase());
		b.fill(14, -2, 14, 16, -2, 16, p.stoneBase());
		b.fill(14, -1, 14, 16, 0, 16, Blocks.WATER);
		b.walls(13, 13, 17, 17, 1, 1, p.fence());
		for (int[] c : new int[][]{{13, 13}, {17, 13}, {13, 17}, {17, 17}}) {
			b.fill(c[0], 2, c[1], c[0], 3, c[1], p.fence());
		}
		b.fill(13, 4, 13, 17, 4, 17, p.slab());
		b.set(18, 1, 13, Blocks.BELL);
		for (int[] c : new int[][]{{12, 12}, {18, 12}, {12, 18}, {18, 18}}) {
			lampPost(b, c[0], c[1]);
		}
		// Two ItzBreadLoaf cabins in the north, doors toward the village.
		int doorA = cabin(b, 1, 1, 8, 7, true, "cabin");
		b.bed(2, 1, 2, Blocks.RED_BED);
		b.barrel(3, 1, 2, "chests/barrel_supplies");
		b.set(7, 1, 5, Blocks.CRAFTING_TABLE);
		Pieces.table(b, 6, 1, 4, false, false);
		Pieces.path(b, doorA, 8, doorA, 13, 0);
		int doorB = cabin(b, 22, 1, 29, 7, true, "cabin");
		b.bed(23, 1, 2, Blocks.BLUE_BED);
		b.barrel(24, 1, 2, "chests/barrel_supplies");
		b.set(28, 1, 5, Blocks.CRAFTING_TABLE);
		Pieces.table(b, 27, 1, 4, false, false);
		Pieces.path(b, doorB, 8, doorB, 13, 0);
		// Fishing pond with a dock, between the cabins.
		b.fill(18, -2, 9, 22, -2, 12, Blocks.DIRT);
		b.fill(18, -1, 9, 22, 0, 12, Blocks.WATER);
		b.fill(20, 0, 10, 20, 0, 12, p.planks());
		b.set(19, 1, 9, Blocks.LILY_PAD);
		b.set(22, 1, 11, Blocks.LILY_PAD);
		b.barrel(21, 1, 13, "chests/fishing_barrel");
		// A little garden in the north-west corner.
		Pieces.garden(b, 9, 9, 12, 12, 0);
		// Archer Lily's cabin in the south-west, facing the village, with targets.
		int doorL = cabin(b, 1, 24, 8, 30, false, "lily's cabin");
		b.bed(2, 1, 28, Blocks.PINK_BED);
		b.barrel(3, 1, 29, "chests/barrel_supplies");
		b.set(7, 1, 29, Blocks.FLETCHING_TABLE);
		Pieces.table(b, 6, 1, 27, false, false);
		Pieces.path(b, doorL, 23, 13, 23, 0);
		b.set(11, 1, 26, Blocks.TARGET);
		b.set(11, 1, 29, Blocks.TARGET);
		// Baker Madison's booth: a counter, smoker and bread barrels under a striped awning.
		b.fill(1, 0, 17, 7, 0, 21, p.planks());
		for (int[] c : new int[][]{{1, 17}, {7, 17}, {1, 21}, {7, 21}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.fence());
		}
		for (int x = 1; x <= 7; x++) {
			b.fill(x, 4, 17, x, 4, 21, x % 2 == 0 ? Blocks.WHITE_WOOL : Blocks.ORANGE_WOOL);
		}
		for (int x = 2; x <= 6; x++) {
			b.set(x, 1, 18, Pieces.topSlab(p.slab()));
		}
		b.set(2, 1, 20, Blocks.SMOKER);
		b.set(3, 1, 20, Blocks.HAY_BLOCK);
		b.barrel(5, 1, 20, "chests/bakery");
		b.barrel(6, 1, 20, "chests/barrel_supplies");
		Pieces.lantern(b, 4, 3, 19, true);
		// Market corner with the village chest.
		b.set(10, 1, 18, Blocks.HAY_BLOCK);
		b.set(10, 1, 19, Blocks.HAY_BLOCK);
		b.set(11, 1, 21, Blocks.CRAFTING_TABLE);
		b.chest(12, 1, 18, "chests/village");
		b.floor(10, 18, 12, 21, 1, "market");
		// The farm: mixed crops around a water channel, fenced.
		Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS};
		BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);
		for (int x = 20; x <= 29; x++) {
			for (int z = 20; z <= 29; z++) {
				if (z == 24) {
					b.set(x, 0, z, Blocks.WATER);
					continue;
				}
				b.set(x, 0, z, farmland);
				CropBlock crop = (CropBlock) crops[(x / 3) % crops.length];
				b.set(x, 1, z, crop.getStateForAge(b.random.nextInt(crop.getMaxAge() + 1)));
			}
		}
		Pieces.fenceRing(b, 19, 19, 30, 30, 1, 24, 19);
		// Who lives here.
		BlockPos center = at(15, 1, 15);
		b.resident(CharacterVariant.VILLAGER, 15, 1, 20, center.getX(), center.getZ(), 14, at(2, 1, 2), null, true);
		b.resident(CharacterVariant.VILLAGER, 15, 1, 20, center.getX(), center.getZ(), 14, at(23, 1, 2), null, true);
		b.resident(CharacterVariant.VILLAGER, 15, 1, 20, center.getX(), center.getZ(), 14, null, null, true);
		b.resident(CharacterVariant.ARCHER_LILY, 5, 1, 23, 4, 27, 8, at(2, 1, 28), null, false);
		b.resident(CharacterVariant.FARMER, 24, 1, 18, 25, 24, 6);
		b.resident(CharacterVariant.FISHER, 19, 1, 13, 20, 12, 4);
		b.resident(CharacterVariant.BAKER, 4, 1, 17, 4, 19, 4);
		if (b.random.nextDouble() < ModConfig.get().felixVillageChance) {
			b.resident(CharacterVariant.FELIXSYN, 15, 1, 20, center.getX(), center.getZ(), 24);
		}
	}

	// ================================================================ camps and get-togethers

	/** Cousins' Camp: Lily's pink tent and Madison's yellow tent around a campfire, a picnic table with a cake. */
	static void cousinsCamp(Blueprint b) {
		Palette p = b.palette;
		Pieces.tent(b, 1, 4, 7, Blocks.PINK_WOOL.defaultBlockState());
		b.bed(2, 1, 5, Blocks.PINK_BED);
		b.chest(4, 1, 9, "chests/cousins_camp");
		Pieces.lantern(b, 3, 1, 9, false);
		Pieces.tent(b, 9, 4, 7, Blocks.YELLOW_WOOL.defaultBlockState());
		b.bed(12, 1, 5, Blocks.YELLOW_BED);
		b.barrel(10, 1, 9, "chests/madison_pantry");
		Pieces.lantern(b, 11, 1, 9, false);
		b.fill(5, 0, 0, 9, 0, 3, Blocks.COARSE_DIRT);
		Pieces.campfire(b, 7, 1, 2);
		Pieces.table(b, 7, 1, 6, false, true);
		b.set(7, 2, 6, Blocks.CAKE);
		Pieces.garden(b, 6, 9, 8, 11, 0);
		b.resident(CharacterVariant.LILY, 6, 1, 3, 3, 7, 10, at(2, 1, 5), null, false);
		b.resident(CharacterVariant.MADISON, 8, 1, 3, 11, 7, 10, at(12, 1, 5), at(10, 1, 9), false);
	}

	/**
	 * PvP Arena: a round arena with a low wall, two rows of stands facing in and entrances east and west. A Sweaty
	 * ItzBreadLoaf (geared up, so the rivalry is on) and FelixSyn start at opposite ends.
	 */
	static void pvpArena(Blueprint b) {
		Palette p = b.palette;
		int c = 12;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, c, c);
				int dx = x - c;
				int dz = z - c;
				Direction out = Math.abs(dx) >= Math.abs(dz) ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
				if (d <= 9.5) {
					b.set(x, 0, z, (x + z) % 2 == 0 ? p.stoneFloor() : p.bricks());
				} else if (d <= 10.5) {
					b.set(x, 0, z, p.bricks());
					b.set(x, 1, z, p.stoneWall());
					if (Math.abs(Math.abs(dx) - Math.abs(dz)) <= 1) {
						Pieces.lantern(b, x, 2, z, false);
					}
				} else if (d <= 11.5) {
					b.set(x, 0, z, p.bricks());
					b.set(x, 1, z, Pieces.stairs(p.stoneStairs(), out));
				} else if (d <= 12.5) {
					b.fill(x, 0, z, x, 1, z, p.bricks());
					b.set(x, 2, z, Pieces.stairs(p.stoneStairs(), out));
				}
			}
		}
		// Entrances: gaps through the wall and the stands, east and west.
		for (int z = 11; z <= 13; z++) {
			for (int x : new int[]{0, 1, 2, 3, 21, 22, 23, 24}) {
				b.clear(x, 1, z, x, 3, z);
				b.set(x, 0, z, p.path());
			}
		}
		b.set(c, 0, c, Blocks.GOLD_BLOCK);
		b.chest(c, 1, 3, "chests/pvp_arena");
		b.floor(6, 6, 18, 18, 1, "arena");
		b.resident(CharacterVariant.SWEATY, 5, 1, c, c, c, 10);
		b.resident(CharacterVariant.FELIXSYN, 19, 1, c, c, c, 10);
	}

	/**
	 * Friendship Festival: two long tables with chairs and cakes, a raised stage with bunting, banner poles and lamp
	 * posts. ItzBreadLoaf, Lily and Madison.
	 */
	static void friendshipFestival(Blueprint b) {
		Palette p = b.palette;
		Pieces.garden(b, 0, 0, 22, 0, 0);
		// Stage at the back, steps up the front, bunting between two posts.
		b.fill(6, 1, 14, 16, 1, 17, p.planks());
		for (int x = 10; x <= 12; x++) {
			b.set(x, 1, 13, Pieces.stairs(p.stairs(), Direction.SOUTH));
		}
		b.fill(6, 2, 17, 6, 4, 17, p.fence());
		b.fill(16, 2, 17, 16, 4, 17, p.fence());
		Block[] bunting = {Blocks.RED_WOOL, Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.LIGHT_BLUE_WOOL, Blocks.PINK_WOOL};
		for (int x = 6; x <= 16; x++) {
			b.set(x, 5, 17, bunting[x % bunting.length]);
		}
		b.floor(7, 14, 15, 16, 2, "stage");
		// Two long tables with chairs on both sides (gaps to walk through) and cakes.
		for (int z : new int[]{5, 9}) {
			for (int x = 3; x <= 19; x++) {
				if (x == 8 || x == 14) {
					continue;
				}
				b.set(x, 1, z, Pieces.topSlab(p.slab()));
				b.set(x, 1, z - 1, Pieces.stairs(p.stairs(), Direction.NORTH));
				b.set(x, 1, z + 1, Pieces.stairs(p.stairs(), Direction.SOUTH));
				if (x % 4 == 1) {
					b.set(x, 2, z, Blocks.CAKE);
				}
			}
		}
		b.floor(3, 7, 19, 7, 1, "between the tables");
		Block[] banners = {Blocks.RED_BANNER, Blocks.YELLOW_BANNER, Blocks.LIME_BANNER, Blocks.PINK_BANNER};
		int i = 0;
		for (int[] c : new int[][]{{1, 1}, {21, 1}, {1, 17}, {21, 17}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.fence());
			b.set(c[0], 4, c[1], banners[i++]);
		}
		lampPost(b, 5, 11);
		lampPost(b, 17, 11);
		b.chest(20, 1, 9, "chests/friendship_festival");
		b.barrel(2, 1, 9, "chests/barrel_supplies");
		b.resident(CharacterVariant.BREADLOAF, 2, 1, 7, 11, 9, 10);
		b.resident(CharacterVariant.LILY, 11, 1, 11, 11, 9, 10);
		b.resident(CharacterVariant.MADISON, 20, 1, 7, 11, 9, 10);
	}

	// ================================================================ FelixSyn and Harley's hideout

	/** Ruined Portal Hideout: a broken obsidian portal on netherrack, and FelixSyn and Harley's camp behind it. */
	static void ruinedPortalHideout(Blueprint b) {
		for (int x = 2; x <= 11; x++) {
			for (int z = 0; z <= 4; z++) {
				if (b.random.nextBoolean()) {
					b.set(x, 0, z, Blocks.NETHERRACK);
				}
			}
		}
		for (int x = 4; x <= 9; x++) {
			for (int y = 0; y <= 6; y++) {
				boolean frame = y == 0 || y == 6 || x == 4 || x == 9;
				if (!frame) {
					continue;
				}
				// Ruined: some of the frame is gone, some of it is crying.
				if (y > 0 && b.random.nextInt(4) == 0) {
					continue;
				}
				b.set(x, y, 2, b.random.nextInt(5) == 0 ? Blocks.CRYING_OBSIDIAN : Blocks.OBSIDIAN);
			}
		}
		b.set(3, 1, 1, Blocks.GOLD_BLOCK);
		Pieces.tent(b, 1, 6, 6, Blocks.BLACK_WOOL.defaultBlockState());
		b.bed(2, 1, 7, Blocks.BLACK_BED);
		b.bed(4, 1, 7, Blocks.PINK_BED);
		b.chest(4, 1, 10, "chests/ruined_portal_hideout");
		Pieces.lantern(b, 3, 1, 10, false);
		Pieces.campfire(b, 9, 1, 8);
		b.set(12, 1, 10, Blocks.GOLD_BLOCK);
		b.set(13, 1, 9, Blocks.TARGET);
		b.mob(12, 1, 5, "dummy");
		b.resident(CharacterVariant.FELIXSYN, 9, 1, 10, 7, 8, 16, at(2, 1, 7), null, false);
	}

	// ================================================================ easter egg

	/**
	 * Giant Loaf: a huge loaf of bread (crust outside, crumb inside, score marks on top) with a hall inside. A carpet
	 * down the middle hides a trapdoor; a ladder under it goes down to the treasure room.
	 */
	static void giantLoaf(Blueprint b) {
		Palette p = b.palette;
		double cx = 13;
		double cz = 8;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				for (int y = 0; y <= 9; y++) {
					if (loaf(x, y, z, cx, cz, 0) > 1.0) {
						continue;
					}
					boolean outer = loaf(x, y, z, cx, cz, 1) > 1.0;
					boolean crumb = !outer && loaf(x, y, z, cx, cz, 2) > 1.0;
					if (y == 0) {
						b.set(x, 0, z, outer ? Blocks.TERRACOTTA.defaultBlockState() : p.planks());
					} else if (outer) {
						boolean score = y >= 7 && (x == 7 || x == 13 || x == 19);
						b.set(x, y, z, score ? Blocks.YELLOW_TERRACOTTA : y >= 5 ? Blocks.BROWN_TERRACOTTA : Blocks.TERRACOTTA);
					} else if (crumb) {
						b.set(x, y, z, Blocks.WHITE_TERRACOTTA);
					} else {
						b.set(x, y, z, Blocks.AIR);
					}
				}
			}
		}
		// The way in: a tunnel through the west end.
		b.clear(0, 1, 7, 3, 3, 9);
		Pieces.path(b, 0, 7, 3, 9, 0);
		b.floor(5, 6, 21, 10, 1, "inside the loaf");
		for (int[] c : new int[][]{{7, 6}, {19, 10}, {7, 10}, {19, 6}}) {
			Pieces.lantern(b, c[0], 1, c[1], false);
		}
		b.fill(5, 1, 5, 5, 2, 5, Blocks.HAY_BLOCK);
		b.fill(21, 1, 11, 21, 2, 11, Blocks.HAY_BLOCK);
		// Treasure room under the floor, down a ladder from a trapdoor hidden under the carpet.
		b.fill(9, -5, 4, 17, -1, 12, p.bricks());
		b.clear(10, -4, 5, 16, -2, 11);
		Pieces.ladder(b, 13, -4, -1, 8, Direction.NORTH);
		b.set(13, 0, 8, p.wood().trapdoor().defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP));
		b.fill(4, 1, 8, 22, 1, 8, Blocks.RED_CARPET);
		b.chest(10, -4, 8, "chests/giant_loaf");
		b.chest(16, -4, 8, "chests/giant_loaf");
		b.chest(13, -4, 11, "chests/giant_loaf");
		for (int[] c : new int[][]{{10, 5}, {16, 5}, {10, 11}, {16, 11}}) {
			b.set(c[0], -4, c[1], Blocks.GOLD_BLOCK);
		}
		Pieces.lantern(b, 11, -2, 6, true);
		Pieces.lantern(b, 15, -2, 10, true);
		b.floor(10, 5, 16, 11, -4, "treasure room");
	}

	/** The loaf's shape: below 1 is inside, {@code inset} blocks in from the crust. */
	private static double loaf(int x, int y, int z, double cx, double cz, int inset) {
		double ax = (x - cx) / (12.5 - inset);
		double az = (z - cz) / (7.5 - inset);
		double ay = y / (9.0 - inset);
		return ax * ax + az * az + ay * ay;
	}
}
