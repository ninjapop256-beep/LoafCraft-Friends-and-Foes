package com.itzbreadloaf.worldgen.build;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Update 2's lore structures (part 8, batch 8): quiet, mostly abandoned places from the characters' past. Each has a
 * lectern and a chest where Lily's books go (part 9), and the books point the way to the others.
 */
final class LoreBlueprints {
	private LoreBlueprints() {
	}

	private static double dist(int x, int z, double cx, double cz) {
		return Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
	}

	private static BlockState coldCampfire() {
		return Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false);
	}

	private static BlockState candles(Block candle, int count) {
		return candle.defaultBlockState().setValue(CandleBlock.CANDLES, count).setValue(CandleBlock.LIT, true);
	}

	private static void lampPost(Blueprint b, int x, int z) {
		b.set(x, 1, z, b.palette.fence());
		Pieces.lantern(b, x, 2, z, false);
	}

	/** A small blossoming tree. */
	private static void blossom(Blueprint b, int x, int z) {
		b.fill(x, 1, z, x, 3, z, b.palette.log());
		BlockState leaves = LilyBlueprints.leaves(Blocks.FLOWERING_AZALEA_LEAVES);
		for (int y = 3; y <= 4; y++) {
			int r = y == 4 ? 1 : 2;
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					int lx = x + dx;
					int lz = z + dz;
					if (Math.abs(dx) + Math.abs(dz) <= r + 1 && lx >= 0 && lz >= 0 && lx < b.width && lz < b.depth && !b.isSet(lx, y, lz)) {
						b.set(lx, y, lz, leaves);
					}
				}
			}
		}
	}

	/** Lily's Abandoned Writing Cabin: her old cabin, door gone, windows broken, moss and cobwebs, her desk and lectern. */
	static void lilyWritingCabin(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 2, 2, 8, 8, 0, 3, Pieces.Roof.GABLE_X, "writing cabin");
		b.clear(door, 1, 2, door, 2, 2);
		b.clear(4, 2, 2, 4, 2, 2);
		b.clear(8, 2, 6, 8, 2, 6);
		for (int y = 1; y <= 3; y++) {
			for (int i = 3; i <= 7; i++) {
				for (int[] c : new int[][]{{i, 2}, {i, 8}, {2, i}, {8, i}}) {
					if (b.get(c[0], y, c[1]) == p.planks() && b.random.nextInt(5) == 0) {
						b.set(c[0], y, c[1], Blocks.MOSSY_COBBLESTONE);
					}
				}
			}
		}
		for (int z = 3; z <= 7; z += 2) {
			b.set(1, 2, z, Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, true));
		}
		b.set(3, 3, 3, Blocks.COBWEB);
		b.set(7, 3, 7, Blocks.COBWEB);
		b.set(4, 1, 7, Blocks.LECTERN);
		b.set(5, 1, 7, Pieces.topSlab(p.slab()));
		b.set(6, 1, 7, Pieces.topSlab(p.slab()));
		b.set(5, 2, 7, candles(Blocks.WHITE_CANDLE, 1));
		b.set(5, 1, 5, Blocks.WHITE_CARPET);
		b.set(6, 1, 4, Blocks.WHITE_CARPET);
		b.set(4, 1, 4, Blocks.MOSS_CARPET);
		b.chest(7, 1, 6, "chests/lily_writing_cabin");
		b.bed(3, 1, 5, p.bed());
		Pieces.path(b, door, 0, door, 1, 0);
	}

	/** Ruins of the First Bakery: crumbling brick walls, a cold oven, spilled hay, the counter's remains. */
	static void firstBakeryRuins(Blueprint b) {
		Palette p = b.palette;
		for (int x = 1; x <= 11; x++) {
			for (int z = 1; z <= 9; z++) {
				b.set(x, 0, z, b.random.nextInt(4) == 0 ? Blocks.COARSE_DIRT.defaultBlockState() : p.stoneFloor());
				boolean wall = x == 1 || x == 11 || z == 1 || z == 9;
				if (wall && !(x == 6 && z == 1)) {
					int h = z == 1 ? b.random.nextInt(2) : b.random.nextInt(4);
					for (int y = 1; y <= h; y++) {
						b.set(x, y, z, b.random.nextInt(3) == 0 ? Blocks.CRACKED_STONE_BRICKS : Blocks.BRICKS);
					}
				}
			}
		}
		// The old oven against the back wall, long cold.
		b.fill(5, 1, 8, 7, 2, 8, Blocks.BRICKS);
		b.set(6, 1, 8, coldCampfire());
		b.set(6, 3, 8, Blocks.BRICKS);
		b.set(3, 1, 4, Pieces.topSlab(p.slab()));
		b.set(4, 1, 4, Pieces.topSlab(p.slab()));
		b.set(9, 1, 3, Blocks.HAY_BLOCK);
		b.set(9, 2, 3, Blocks.ORANGE_TERRACOTTA);
		b.set(8, 1, 2, Blocks.HAY_BLOCK);
		b.set(4, 1, 7, Blocks.LECTERN);
		b.chest(9, 1, 7, "chests/first_bakery_ruins");
		b.floor(4, 5, 8, 6, 1, "ruins");
	}

	/** The Memorial Grove: blossom trees in a ring, lily of the valley, a stone stele with candles, benches, a lectern. */
	static void memorialGrove(Blueprint b) {
		Palette p = b.palette;
		for (int i = 0; i < 6; i++) {
			double a = Math.PI * 2 * i / 6;
			blossom(b, (int) Math.round(7 + 5 * Math.cos(a)), (int) Math.round(7 + 5 * Math.sin(a)));
		}
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 7, 7);
				if (d >= 2.6 && d <= 3.5 && !b.isSet(x, 1, z)) {
					b.set(x, 1, z, Blocks.LILY_OF_THE_VALLEY);
				}
			}
		}
		b.fill(6, 0, 6, 8, 0, 8, Blocks.POLISHED_ANDESITE);
		b.fill(7, 1, 8, 7, 2, 8, Blocks.POLISHED_ANDESITE);
		b.set(7, 3, 8, candles(Blocks.WHITE_CANDLE, 3));
		b.set(7, 1, 6, Blocks.LECTERN);
		b.chest(8, 1, 8, "chests/memorial_grove");
		b.set(4, 1, 7, Pieces.stairs(p.stairs(), Direction.WEST));
		b.set(10, 1, 7, Pieces.stairs(p.stairs(), Direction.EAST));
		b.clear(7, 1, 1, 7, 1, 5);
		Pieces.path(b, 7, 0, 7, 5, 0);
		b.floor(6, 4, 8, 5, 1, "grove");
	}

	/** The Old Battle Site: where the rivalry began. Scorched ground, craters, broken maces, swords stuck in the ground, the two banners. */
	static void rivalryBattleSite(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				int roll = b.random.nextInt(5);
				if (roll == 0) {
					b.set(x, 0, z, Blocks.COARSE_DIRT);
				} else if (roll == 1) {
					b.set(x, 0, z, Blocks.GRAVEL);
				}
			}
		}
		for (int[] c : new int[][]{{4, 3}, {12, 8}, {7, 9}}) {
			b.clear(c[0], 0, c[1], c[0] + 1, 0, c[1] + 1);
			b.fill(c[0], -1, c[1], c[0] + 1, -1, c[1] + 1, Blocks.CRACKED_STONE_BRICKS);
		}
		// Broken maces lying on the ground, swords stuck in it.
		for (int[] m : new int[][]{{3, 9}, {13, 3}}) {
			b.set(m[0], 1, m[1], Blocks.POLISHED_DEEPSLATE);
			b.set(m[0] + 1, 1, m[1], Pieces.logAlong(Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), Direction.Axis.X));
		}
		for (int[] s : new int[][]{{6, 4}, {10, 6}, {9, 2}, {14, 10}}) {
			b.set(s[0], 1, s[1], Blocks.IRON_BARS);
			b.set(s[0], 2, s[1], Blocks.DARK_OAK_FENCE);
		}
		b.fill(2, 1, 6, 2, 3, 6, p.fence());
		b.set(2, 4, 6, Blocks.RED_BANNER);
		b.fill(14, 1, 6, 14, 3, 6, p.fence());
		b.set(14, 4, 6, Blocks.BLUE_BANNER);
		b.set(8, 1, 11, Blocks.CRACKED_STONE_BRICKS);
		b.set(8, 1, 10, Blocks.LECTERN);
		b.chest(9, 1, 11, "chests/rivalry_battle_site");
		b.floor(6, 6, 8, 7, 1, "battlefield");
	}

	/**
	 * The Sealed Library: a stone library with no door and barred windows. The way in is hidden: a hatch in a bush
	 * out front, a ladder down, a tunnel under the wall and a ladder up through a trapdoor in the library floor.
	 */
	static void sealedLibrary(Blueprint b) {
		Palette p = b.palette;
		BlockState wall = p.bricks();
		b.fill(2, 0, 3, 10, 0, 11, p.stoneFloor());
		b.walls(2, 3, 10, 11, 1, 6, wall);
		b.fill(2, 7, 3, 10, 7, 11, wall);
		b.clear(3, 1, 4, 9, 6, 10);
		for (int[] w : new int[][]{{2, 3, 7}, {10, 3, 7}, {6, 3, 11}, {4, 3, 3}, {8, 3, 3}}) {
			b.set(w[0], w[1], w[2], Blocks.IRON_BARS);
		}
		// The secret way in.
		BlockState leaves = LilyBlueprints.leaves(p.wood().leaves());
		for (int[] l : new int[][]{{5, 0}, {7, 0}, {5, 1}, {7, 1}, {5, 2}, {7, 2}, {6, 2}}) {
			b.set(l[0], 1, l[1], leaves);
		}
		b.fill(5, -4, 0, 7, -1, 7, wall);
		b.clear(6, -3, 1, 6, -2, 6);
		Pieces.ladder(b, 6, -3, -1, 1, Direction.SOUTH);
		b.set(6, 0, 1, p.wood().trapdoor().defaultBlockState().setValue(net.minecraft.world.level.block.TrapDoorBlock.HALF,
				net.minecraft.world.level.block.state.properties.Half.TOP));
		Pieces.ladder(b, 6, -3, -1, 6, Direction.NORTH);
		b.set(6, 0, 6, p.wood().trapdoor().defaultBlockState().setValue(net.minecraft.world.level.block.TrapDoorBlock.HALF,
				net.minecraft.world.level.block.state.properties.Half.TOP));
		// The library.
		b.fill(3, 1, 4, 3, 3, 10, Blocks.BOOKSHELF);
		b.fill(9, 1, 4, 9, 3, 10, Blocks.BOOKSHELF);
		b.fill(4, 1, 10, 8, 2, 10, Blocks.BOOKSHELF);
		b.set(5, 1, 9, Blocks.LECTERN);
		b.set(7, 1, 9, Blocks.LECTERN);
		b.chest(6, 1, 9, "chests/sealed_library");
		b.set(6, 1, 4, candles(Blocks.WHITE_CANDLE, 2));
		Pieces.lantern(b, 6, 6, 7, true);
		b.floor(4, 5, 8, 8, 1, "library");
	}

	/** The Cartographer's Tent: a striped pavilion over a floor "map" of colored carpets, a map table, rolled maps in barrels. */
	static void cartographersTent(Blueprint b) {
		Palette p = b.palette;
		for (int[] c : new int[][]{{1, 1}, {11, 1}, {1, 9}, {11, 9}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.fence());
		}
		for (int x = 1; x <= 11; x++) {
			b.fill(x, 4, 1, x, 4, 9, x % 2 == 0 ? Blocks.WHITE_WOOL : Blocks.LIGHT_BLUE_WOOL);
		}
		b.fill(1, 5, 5, 11, 5, 5, Blocks.WHITE_WOOL);
		b.fill(2, 1, 9, 10, 3, 9, Blocks.WHITE_WOOL);
		b.fill(1, 1, 2, 1, 3, 8, Blocks.WHITE_WOOL);
		b.fill(11, 1, 2, 11, 3, 8, Blocks.WHITE_WOOL);
		Block[] land = {Blocks.GREEN_CARPET, Blocks.LIME_CARPET, Blocks.BLUE_CARPET, Blocks.YELLOW_CARPET, Blocks.BROWN_CARPET};
		for (int x = 4; x <= 8; x++) {
			for (int z = 4; z <= 7; z++) {
				b.set(x, 1, z, land[(x * 3 + z * 5 + b.random.nextInt(2)) % land.length]);
			}
		}
		b.set(3, 1, 7, Blocks.CARTOGRAPHY_TABLE);
		b.set(9, 1, 7, Blocks.LECTERN);
		b.set(2, 1, 8, Blocks.BARREL);
		b.set(10, 1, 8, Blocks.BARREL);
		b.chest(9, 1, 3, "chests/cartographers_tent");
		Pieces.lantern(b, 6, 3, 5, true);
		b.floor(3, 2, 9, 3, 1, "tent");
	}

	/** Master BreadLoaf's Old Home: a dusty cottage with his training dummy, a gold-trimmed chair, his diary, sketches of the portals. */
	static void mastersOldHome(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 2, 2, 10, 8, 0, 3, Pieces.Roof.GABLE_X, "master's old home");
		b.walls(2, 2, 10, 8, 1, 1, p.bricks());
		Pieces.door(b, door, 1, 2, Direction.NORTH);
		b.mob(8, 1, 4, "dummy");
		b.set(6, 1, 7, Pieces.stairs(p.stairs(), Direction.SOUTH));
		b.set(5, 1, 7, Blocks.GOLD_BLOCK);
		b.set(7, 1, 7, Blocks.GOLD_BLOCK);
		b.set(3, 1, 4, Blocks.LECTERN);
		b.fill(3, 1, 6, 3, 2, 7, Blocks.BOOKSHELF);
		b.chest(9, 1, 7, "chests/masters_old_home");
		// Sketches on the back wall: a Nether portal and the End.
		b.set(4, 2, 8, Blocks.OBSIDIAN);
		b.set(4, 3, 8, Blocks.PURPLE_STAINED_GLASS);
		b.set(8, 2, 8, Blocks.END_STONE);
		b.set(8, 3, 8, Blocks.PURPUR_BLOCK);
		b.set(4, 3, 3, Blocks.COBWEB);
		b.set(9, 3, 6, Blocks.COBWEB);
		Pieces.path(b, door, 0, door, 1, 0);
	}

	/** The loaf's shape: below 1 is inside, {@code inset} blocks in from the crust. */
	private static double loaf(int x, int y, int z, double cx, double cz, int inset) {
		double ax = (x - cx) / (6.5 - inset);
		double az = (z - cz) / (4.8 - inset);
		double ay = y / (5.5 - inset);
		return ax * ax + az * az + ay * ay;
	}

	/**
	 * The Archive Under the Old Loaf: the crumbling crust of an older, smaller giant loaf, and under it (down a ladder from
	 * a trapdoor hidden under a carpet) a hidden archive of bookshelves, lecterns and chests.
	 */
	static void oldLoafArchive(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				for (int y = 1; y <= 5; y++) {
					double e = loaf(x, y, z, 7, 6, 0);
					if (e > 1.0) {
						continue;
					}
					boolean crust = loaf(x, y, z, 7, 6, 1) > 1.0;
					if (crust && b.random.nextInt(10) < 7 && !(x == 7 && z <= 2)) {
						b.set(x, y, z, y >= 4 ? Blocks.BROWN_TERRACOTTA : Blocks.TERRACOTTA);
					} else {
						b.set(x, y, z, Blocks.AIR);
					}
				}
			}
		}
		b.clear(6, 1, 0, 8, 3, 2);
		int floor = -7;
		UndergroundBlueprints.vault(b, 3, 3, 11, 9, floor, 3, "archive");
		UndergroundBlueprints.ladderShaft(b, 7, 4, floor + 1, -1, Direction.SOUTH, true);
		b.set(7, 1, 4, Blocks.RED_CARPET);
		int y = floor + 1;
		b.fill(4, y, 5, 4, y + 1, 8, Blocks.BOOKSHELF);
		b.fill(10, y, 5, 10, y + 1, 8, Blocks.BOOKSHELF);
		b.set(6, y, 8, Blocks.LECTERN);
		b.set(8, y, 8, Blocks.LECTERN);
		b.chest(7, y, 8, "chests/old_loaf_archive");
		b.chest(5, y, 5, "chests/old_loaf_archive");
		b.set(9, y, 5, candles(Blocks.YELLOW_CANDLE, 3));
		Pieces.lantern(b, 7, floor + 3, 6, true);
		b.floor(7, 3, 7, 4, 1, "inside the old loaf");
	}

	/** The Cousins' Old Treehouse: where Lily and Madison played as kids. A hut up a big tree, two little beds, their banners, a note block. */
	static void cousinsOldTreehouse(Blueprint b) {
		Palette p = b.palette;
		int deck = 7;
		Pieces.room(b, 4, 4, 9, 9, deck, 3, "cousins' treehouse");
		Pieces.windowsAround(b, 4, 4, 9, 9, deck + 2, -1);
		Pieces.roof(b, Pieces.Roof.FLAT, 4, 4, 9, 9, deck + 3);
		b.fill(6, 1, 6, 7, deck + 5, 7, p.log());
		Pieces.ladder(b, 6, 1, deck, 5, Direction.NORTH);
		BlockState leaves = LilyBlueprints.leaves(p.wood().leaves());
		for (int x = 1; x <= 12; x++) {
			for (int z = 1; z <= 12; z++) {
				for (int y = deck + 5; y <= deck + 7; y++) {
					double d = dist(x, z, 6.5, 6.5);
					if (d <= (y == deck + 7 ? 3.0 : 5.5) && !b.isSet(x, y, z)) {
						b.set(x, y, z, leaves);
					}
				}
			}
		}
		int y = deck + 1;
		b.bed(5, y, 6, Blocks.PINK_BED);
		b.bed(8, y, 6, Blocks.YELLOW_BED);
		b.chest(5, y, 5, "chests/cousins_old_treehouse");
		b.set(8, y, 5, Blocks.NOTE_BLOCK);
		b.set(6, y, 8, Blocks.PINK_BANNER);
		b.set(7, y, 8, Blocks.YELLOW_BANNER);
		// Height marks on the trunk: one candle for each of them, at different heights.
		b.set(8, 2, 6, Blocks.PINK_WOOL);
		b.set(8, 3, 7, Blocks.YELLOW_WOOL);
	}

	/** The Drifters' Rest: FelixSyn and Harley's old roadside camp. Two bedrolls, a cold campfire, their banners on a stump, scorch marks. */
	static void driftersRest(Blueprint b) {
		Palette p = b.palette;
		Pieces.path(b, 0, 5, 12, 5, 0);
		b.bed(3, 1, 7, Blocks.BLACK_BED);
		b.bed(5, 1, 7, Blocks.PINK_BED);
		b.set(4, 1, 3, coldCampfire());
		b.set(2, 1, 3, Pieces.logAlong(p.log(), Direction.Axis.Z));
		b.set(6, 1, 3, Pieces.logAlong(p.log(), Direction.Axis.Z));
		b.set(9, 1, 8, p.log());
		b.set(9, 2, 8, Blocks.BLACK_BANNER);
		b.set(10, 1, 8, Blocks.PINK_BANNER);
		for (int[] s : new int[][]{{10, 2}, {11, 3}, {9, 1}}) {
			b.set(s[0], 0, s[1], Blocks.COAL_BLOCK);
		}
		b.chest(7, 1, 9, "chests/drifters_rest");
		b.set(8, 1, 9, Blocks.LECTERN);
		b.floor(2, 6, 8, 6, 1, "camp");
	}

	/** Lily's Naturalist Camp: her field tent and a shelf of specimens under an awning, a lectern with her notes. */
	static void naturalistCamp(Blueprint b) {
		Palette p = b.palette;
		Pieces.tent(b, 1, 5, 6, Blocks.GREEN_WOOL.defaultBlockState());
		b.chest(4, 1, 9, "chests/naturalist_camp");
		for (int[] c : new int[][]{{7, 2}, {11, 2}, {7, 4}, {11, 4}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.fence());
		}
		b.fill(7, 4, 2, 11, 4, 4, p.slab());
		for (int x = 8; x <= 10; x++) {
			b.set(x, 1, 4, Pieces.topSlab(p.slab()));
		}
		b.set(8, 2, 4, Blocks.POTTED_CACTUS);
		b.set(9, 2, 4, Blocks.CREEPER_HEAD);
		b.set(10, 2, 4, Blocks.POTTED_FERN);
		b.set(9, 1, 6, Blocks.LECTERN);
		b.set(12, 1, 7, p.fence());
		b.set(12, 2, 7, Blocks.WHITE_WOOL);
		b.set(8, 1, 8, coldCampfire());
		b.floor(8, 2, 10, 3, 1, "under the awning");
	}

	/**
	 * Master's Departure Point: where Master BreadLoaf left for the Nether and the End. A ruined Nether portal, and a
	 * broken ring of End portal frames (too broken to ever light), a lectern with his last note.
	 */
	static void mastersDeparture(Blueprint b) {
		Palette p = b.palette;
		b.fill(2, 0, 2, 12, 0, 12, p.stoneFloor());
		for (int x = 4; x <= 8; x++) {
			for (int y = 0; y <= 5; y++) {
				boolean frame = x == 4 || x == 8 || y == 0 || y == 5;
				if (frame && (y == 0 || b.random.nextInt(4) != 0)) {
					b.set(x, y, 3, b.random.nextInt(4) == 0 ? Blocks.CRYING_OBSIDIAN : Blocks.OBSIDIAN);
				}
			}
		}
		// A broken ring of End portal frames facing in: three are missing, so it can never become a portal.
		int[][] frames = {{6, 7, 0}, {7, 7, 0}, {8, 7, 0}, {6, 11, 1}, {8, 11, 1}, {5, 8, 2}, {5, 10, 2}, {9, 9, 3}, {9, 10, 3}};
		Direction[] facing = {Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST};
		for (int[] f : frames) {
			b.set(f[0], 1, f[1], Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, facing[f[2]])
					.setValue(EndPortalFrameBlock.HAS_EYE, f[0] == 7 && f[1] == 7));
		}
		for (int z = 4; z <= 6; z++) {
			b.set(6, 0, z, Blocks.CRACKED_STONE_BRICKS);
		}
		b.set(11, 1, 5, Blocks.LECTERN);
		b.chest(3, 1, 11, "chests/masters_departure");
		b.floor(10, 6, 11, 8, 1, "platform");
	}

	/** The Whispering Stones: a circle of mossy standing stones around a candle-lit altar, a lectern of strange notes. */
	static void whisperingStones(Blueprint b) {
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				if (b.random.nextInt(4) == 0) {
					b.set(x, 0, z, Blocks.MOSS_BLOCK);
				}
			}
		}
		for (int i = 0; i < 8; i++) {
			double a = Math.PI * 2 * i / 8;
			int x = (int) Math.round(6 + 5 * Math.cos(a));
			int z = (int) Math.round(6 + 5 * Math.sin(a));
			int h = 2 + (i % 2);
			b.fill(x, 1, z, x, h, z, Blocks.MOSSY_STONE_BRICKS);
		}
		b.set(6, 1, 6, Blocks.CHISELED_STONE_BRICKS);
		b.set(6, 2, 6, candles(Blocks.WHITE_CANDLE, 4));
		b.set(6, 1, 4, Blocks.LECTERN);
		b.chest(6, 1, 8, "chests/whispering_stones");
		b.floor(4, 5, 5, 7, 1, "circle");
	}

	/** The Crossroads Signpost: paths crossing at an old signpost with colored banners pointing four ways, a box of notes. */
	static void crossroadsSignpost(Blueprint b) {
		Palette p = b.palette;
		Pieces.path(b, 5, 0, 5, 10, 0);
		Pieces.path(b, 0, 5, 10, 5, 0);
		b.fill(5, 1, 5, 5, 3, 5, p.fence());
		b.set(4, 3, 5, p.fence());
		b.set(6, 3, 5, p.fence());
		b.set(5, 3, 4, p.fence());
		b.set(5, 3, 6, p.fence());
		b.set(4, 4, 5, Blocks.RED_BANNER);
		b.set(6, 4, 5, Blocks.BLUE_BANNER);
		b.set(5, 4, 4, Blocks.PINK_BANNER);
		b.set(5, 4, 6, Blocks.YELLOW_BANNER);
		b.barrel(8, 1, 8, "chests/crossroads_signpost");
		b.set(8, 1, 2, Pieces.stairs(p.stairs(), Direction.EAST));
		lampPost(b, 3, 3);
		lampPost(b, 7, 7);
		b.set(2, 1, 8, Blocks.LECTERN);
	}

	/** The Lost Loaf Wagon: the giant wagon that once hauled the Giant Loaf, broken down, its cargo spilled. */
	static void lostLoafWagon(Blueprint b) {
		Palette p = b.palette;
		BlockState wheel = Pieces.logAlong(Blocks.DARK_OAK_LOG.defaultBlockState(), Direction.Axis.Z);
		for (int[] w : new int[][]{{4, 2}, {4, 8}, {9, 2}}) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = 0; dy <= 2; dy++) {
					if (dx != 0 || dy != 1) {
						b.set(w[0] + dx, 1 + dy, w[1], wheel);
					}
				}
			}
			b.set(w[0], 2, w[1], Blocks.STRIPPED_DARK_OAK_LOG);
		}
		// The fourth wheel fell off and lies flat.
		b.fill(11, 1, 9, 13, 1, 9, Pieces.logAlong(Blocks.DARK_OAK_LOG.defaultBlockState(), Direction.Axis.X));
		b.fill(11, 1, 10, 13, 1, 10, Pieces.logAlong(Blocks.DARK_OAK_LOG.defaultBlockState(), Direction.Axis.X));
		// The bed sags down where the wheel is missing.
		b.fill(3, 2, 3, 8, 2, 7, p.planks());
		b.fill(9, 1, 3, 11, 1, 7, p.planks());
		b.fill(5, 3, 4, 7, 4, 6, Blocks.ORANGE_TERRACOTTA);
		b.fill(5, 5, 4, 7, 5, 6, Blocks.BROWN_TERRACOTTA);
		for (int[] h : new int[][]{{12, 4}, {13, 5}, {12, 6}, {1, 8}}) {
			b.set(h[0], 1, h[1], Blocks.HAY_BLOCK);
		}
		b.set(3, 3, 3, Blocks.BARREL);
		b.chest(2, 1, 9, "chests/lost_loaf_wagon");
		b.set(1, 1, 9, Blocks.LECTERN);
		b.floor(1, 1, 13, 1, 1, "road");
	}
}
