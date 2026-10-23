package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Update 2's new ImLilyLol structures (part 8, batch 3). */
final class NewLilyBlueprints {
	private static final Block[] TULIPS = {Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP};

	private NewLilyBlueprints() {
	}

	private static BlockPos at(int x, int y, int z) {
		return new BlockPos(x, y, z);
	}

	private static double dist(int x, int z, double cx, double cz) {
		return Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
	}

	private static void lampPost(Blueprint b, int x, int y, int z) {
		b.set(x, y, z, b.palette.fence());
		Pieces.lantern(b, x, y + 1, z, false);
	}

	private static BlockState litCandles(Block candle, int count) {
		return candle.defaultBlockState().setValue(CandleBlock.CANDLES, count).setValue(CandleBlock.LIT, true);
	}

	/** Witchy Potion Shop: brewing stands on the counter, shelves of potted mushrooms, candles, a bubbling cauldron outside. */
	static void witchyPotionShop(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 1, 1, 8, 8, 0, 4, Pieces.Roof.GABLE_Z, "potion shop");
		Pieces.path(b, door, 0, door, 0, 0);
		for (int x = 3; x <= 6; x++) {
			b.set(x, 1, 4, Pieces.topSlab(p.slab()));
		}
		b.set(3, 2, 4, Blocks.BREWING_STAND);
		b.set(5, 2, 4, Blocks.BREWING_STAND);
		b.set(4, 2, 4, litCandles(Blocks.PURPLE_CANDLE, 3));
		b.set(6, 2, 4, litCandles(Blocks.PURPLE_CANDLE, 2));
		b.set(2, 1, 7, Blocks.WATER_CAULDRON);
		b.set(3, 1, 7, Blocks.BOOKSHELF);
		b.chest(4, 1, 7, "chests/witchy_potion_shop");
		b.set(5, 1, 7, Blocks.BOOKSHELF);
		b.barrel(6, 1, 7, "chests/barrel_supplies");
		b.set(3, 2, 7, Blocks.POTTED_RED_MUSHROOM);
		b.set(5, 2, 7, Blocks.POTTED_BROWN_MUSHROOM);
		b.bed(7, 1, 5, p.bed());
		b.set(9, 1, 9, Blocks.CAMPFIRE);
		b.set(9, 2, 9, Blocks.WATER_CAULDRON);
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				if (!b.isSet(x, 1, z) && (x == 0 || z >= 9 || x >= 9) && b.random.nextInt(6) == 0) {
					b.set(x, 1, z, b.random.nextBoolean() ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM);
				}
			}
		}
		b.resident(CharacterVariant.WITCHY, door, 1, 0, door, 4, 10, at(7, 1, 5), null, false);
	}

	/** Moonlit Reading Nook: a round stone tower with bookshelves, a ladder up to a loft bed and a telescope, a glass skylight. */
	static void moonlitReadingNook(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 4, 4);
				if (d <= 3.5) {
					b.set(x, 0, z, p.planks());
					b.set(x, 9, z, d > 2.5 ? p.bricks() : Blocks.GLASS.defaultBlockState());
				}
				if (d > 2.5 && d <= 3.5) {
					b.fill(x, 1, z, x, 8, z, p.bricks());
				} else if (d <= 2.5) {
					b.clear(x, 1, z, x, 8, z);
					b.set(x, 5, z, p.planks());
				}
			}
		}
		Pieces.door(b, 4, 1, 1, Direction.NORTH);
		for (int y : new int[]{3, 7}) {
			Pieces.window(b, 1, y, 4);
			Pieces.window(b, 7, y, 4);
			Pieces.window(b, 4, y, 7);
		}
		Pieces.ladder(b, 4, 1, 5, 6, Direction.NORTH);
		b.floor(3, 3, 5, 5, 1, "reading room");
		b.floor(3, 3, 5, 5, 6, "loft");
		b.fill(2, 1, 3, 2, 2, 5, Blocks.BOOKSHELF);
		b.fill(6, 1, 3, 6, 2, 5, Blocks.BOOKSHELF);
		b.set(4, 1, 4, Blocks.LECTERN);
		Pieces.lantern(b, 3, 1, 5, false);
		b.bed(5, 6, 3, p.bed());
		b.chest(3, 6, 4, "chests/moonlit_reading_nook");
		b.set(4, 6, 3, p.fence());
		b.set(4, 7, 3, Blocks.END_ROD);
		b.set(3, 6, 5, litCandles(Blocks.WHITE_CANDLE, 3));
		b.resident(CharacterVariant.BOOKWORM, 4, 1, 0, 4, 4, 8, at(5, 6, 3), null, false);
	}

	/** Tulip Terrace: three stepped terraces of tulips behind stone retaining walls, stairs up the middle, a gazebo on top. */
	static void tulipTerrace(Blueprint b) {
		Palette p = b.palette;
		int[][] levels = {{5, 8}, {9, 11}, {12, 14}};
		for (int k = 1; k <= 3; k++) {
			int z0 = levels[k - 1][0];
			int z1 = levels[k - 1][1];
			for (int x = 0; x < b.width; x++) {
				for (int z = z0; z <= z1; z++) {
					for (int y = 1; y <= k; y++) {
						b.set(x, y, z, z == z0 ? p.bricks() : y == k ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.DIRT.defaultBlockState());
					}
					if (z > z0 && x != 7 && b.random.nextInt(4) != 0) {
						b.set(x, k + 1, z, TULIPS[(x + z) % TULIPS.length]);
					}
				}
			}
		}
		// Stairs up the middle.
		b.set(7, 1, 4, Pieces.stairs(p.stoneStairs(), Direction.SOUTH));
		b.set(7, 1, 5, Pieces.stairs(p.stoneStairs(), Direction.SOUTH));
		b.set(7, 2, 9, Pieces.stairs(p.stoneStairs(), Direction.SOUTH));
		b.set(7, 3, 12, Pieces.stairs(p.stoneStairs(), Direction.SOUTH));
		// Gazebo on the top terrace.
		for (int[] c : new int[][]{{5, 12}, {9, 12}, {5, 14}, {9, 14}}) {
			b.fill(c[0], 4, c[1], c[0], 6, c[1], p.fence());
		}
		b.fill(4, 7, 11, 10, 7, 14, p.slab());
		b.clear(6, 4, 12, 8, 6, 14);
		b.set(7, 4, 14, Pieces.stairs(p.stairs(), Direction.SOUTH));
		b.chest(6, 4, 14, "chests/tulip_terrace");
		Pieces.lantern(b, 7, 6, 13, true);
		b.floor(6, 12, 8, 13, 4, "gazebo");
		// The bottom garden.
		LilyBlueprints.flowerBed(b, 0, 0, 5, 3, 0.4F);
		LilyBlueprints.flowerBed(b, 9, 0, 14, 3, 0.4F);
		Pieces.path(b, 6, 0, 8, 4, 0);
		b.clear(6, 1, 0, 8, 1, 3);
		b.resident(CharacterVariant.GARDENER, 7, 1, 1, 7, 7, 10);
	}

	/** Willow Swing Pond: a big willow with hanging vines leaning over a lily pond, a swing on a frame, a picnic blanket. */
	static void willowSwing(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				if (dist(x, z, 10, 10) <= 3.2) {
					b.set(x, 0, z, Blocks.WATER);
					b.set(x, -1, z, Blocks.WATER);
					if (b.random.nextInt(6) == 0) {
						b.set(x, 1, z, Blocks.LILY_PAD);
					}
				}
			}
		}
		// The willow.
		b.fill(4, 1, 5, 4, 6, 5, p.log());
		BlockState leaves = LilyBlueprints.leaves(p.wood().leaves());
		BlockState vine = Blocks.VINE.defaultBlockState().setValue(VineBlock.UP, true);
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 4, 5);
				if (d <= 4.2) {
					b.set(x, 7, z, leaves);
					if (d > 3.0 && b.random.nextInt(3) != 0) {
						b.set(x, 6, z, vine);
						if (b.random.nextBoolean()) {
							b.set(x, 5, z, vine);
						}
					}
				}
				if (d <= 2.6) {
					b.set(x, 8, z, leaves);
				}
			}
		}
		// The swing frame.
		b.fill(10, 1, 2, 10, 4, 2, p.fence());
		b.fill(14, 1, 2, 14, 4, 2, p.fence());
		b.fill(10, 5, 2, 14, 5, 2, Pieces.logAlong(p.log(), Direction.Axis.X));
		b.fill(11, 3, 2, 11, 4, 2, p.fence());
		b.fill(13, 3, 2, 13, 4, 2, p.fence());
		b.fill(11, 2, 2, 13, 2, 2, p.slab());
		// Picnic.
		for (int x = 1; x <= 3; x++) {
			for (int z = 10; z <= 12; z++) {
				b.set(x, 1, z, (x + z) % 2 == 0 ? Blocks.PINK_CARPET : Blocks.WHITE_CARPET);
			}
		}
		b.chest(2, 1, 13, "chests/willow_swing");
		b.set(3, 1, 13, Blocks.CAKE);
		b.resident(CharacterVariant.LILY, 3, 1, 9, 7, 7, 12);
	}

	/** Archer Lily's Glade: trees with targets on their trunks, a raised tree stand up a ladder, dummies, her green tent. */
	static void archersGlade(Blueprint b) {
		Palette p = b.palette;
		BlockState leaves = LilyBlueprints.leaves(p.wood().leaves());
		for (int[] t : new int[][]{{2, 2}, {14, 2}, {2, 14}, {14, 14}}) {
			b.fill(t[0], 1, t[1], t[0], 6, t[1], p.log());
			for (int y = 5; y <= 8; y++) {
				int r = y == 8 ? 1 : 2;
				for (int dx = -r; dx <= r; dx++) {
					for (int dz = -r; dz <= r; dz++) {
						int x = t[0] + dx;
						int z = t[1] + dz;
						if (x >= 0 && z >= 0 && x < b.width && z < b.depth && !b.isSet(x, y, z)) {
							b.set(x, y, z, leaves);
						}
					}
				}
			}
			int tx = t[0] == 2 ? 3 : 13;
			b.set(tx, 3, t[1], Blocks.TARGET);
		}
		// The tree stand.
		int deck = 5;
		for (int[] c : new int[][]{{7, 12}, {9, 12}, {7, 14}, {9, 14}}) {
			b.fill(c[0], 1, c[1], c[0], deck - 1, c[1], p.log());
		}
		b.fill(7, deck, 12, 9, deck, 14, p.planks());
		Pieces.ladder(b, 8, 1, deck, 11, Direction.NORTH);
		b.walls(7, 12, 9, 14, deck + 1, deck + 1, p.fence());
		b.clear(8, deck + 1, 12, 8, deck + 1, 12);
		b.floor(8, 13, 8, 13, deck + 1, "tree stand");
		b.mob(8, 1, 4, "dummy");
		b.mob(5, 1, 8, "dummy");
		Pieces.tent(b, 11, 4, 5, Blocks.GREEN_WOOL.defaultBlockState());
		b.bed(12, 1, 5, p.bed());
		b.chest(14, 1, 7, "chests/archers_glade");
		Pieces.lantern(b, 13, 1, 7, false);
		b.resident(CharacterVariant.ARCHER_LILY, 8, 1, 8, 8, 8, 12, at(12, 1, 5), null, false);
	}

	/** Pressed Flower Studio: an art studio with looms, an easel, flower "paintings" in the back wall, a cutting garden. */
	static void flowerStudio(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 1, 1, 9, 8, 0, 4, Pieces.Roof.GABLE_X, "flower studio");
		b.set(3, 2, 8, Blocks.PINK_GLAZED_TERRACOTTA);
		b.set(5, 2, 8, Blocks.YELLOW_GLAZED_TERRACOTTA);
		b.set(7, 2, 8, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
		b.set(5, 3, 8, Blocks.MAGENTA_GLAZED_TERRACOTTA);
		b.set(2, 1, 3, Blocks.LOOM);
		b.set(2, 1, 5, Blocks.LOOM);
		b.set(5, 1, 5, p.fence());
		b.set(5, 2, 5, Blocks.PINK_WOOL);
		b.set(5, 3, 5, Blocks.LIME_WOOL);
		b.set(6, 1, 4, Pieces.stairs(p.stairs(), Direction.EAST));
		b.set(7, 1, 3, Blocks.CARTOGRAPHY_TABLE);
		b.bed(8, 1, 6, p.bed());
		b.chest(2, 1, 7, "chests/flower_studio");
		b.set(4, 1, 7, Blocks.POTTED_ALLIUM);
		b.set(5, 1, 7, Blocks.POTTED_BLUE_ORCHID);
		LilyBlueprints.flowerBed(b, 0, 0, door - 1, 0, 0.8F);
		LilyBlueprints.flowerBed(b, door + 1, 0, 10, 0, 0.8F);
		Pieces.path(b, door, 0, door, 0, 0);
		b.resident(CharacterVariant.LILY, door, 1, 0, door, 4, 10, at(8, 1, 6), null, false);
	}

	/** Herbalist's Stilt Hut: a hut on a stilted deck over the swamp, a ladder up, a cauldron, brewing stand and composter. */
	static void herbalistHut(Blueprint b) {
		Palette p = b.palette;
		b.groundLayer = false;
		b.foundation = false;
		b.skirt = false;
		int deck = 3;
		b.fill(2, deck, 2, 10, deck, 10, p.planks());
		for (int[] c : new int[][]{{2, 2}, {10, 2}, {2, 10}, {10, 10}, {6, 10}, {2, 6}, {10, 6}}) {
			b.pillar(c[0], deck - 1, c[1], p.log());
		}
		for (int x = 3; x <= 9; x += 3) {
			b.set(x, deck - 1, 3, Blocks.HANGING_ROOTS);
		}
		Pieces.ladder(b, 6, 1, deck, 1, Direction.NORTH);
		b.walls(2, 2, 10, 10, deck + 1, deck + 1, p.fence());
		b.clear(6, deck + 1, 2, 6, deck + 1, 2);
		Pieces.room(b, 3, 4, 9, 9, deck, 3, "herbalist hut");
		Pieces.door(b, 6, deck + 1, 4, Direction.NORTH);
		Pieces.roof(b, Pieces.Roof.GABLE_X, 3, 4, 9, 9, deck + 3);
		b.set(4, deck + 1, 8, Blocks.WATER_CAULDRON);
		b.set(5, deck + 1, 8, Blocks.BREWING_STAND);
		b.chest(6, deck + 1, 8, "chests/herbalist_hut");
		b.set(8, deck + 1, 8, Blocks.COMPOSTER);
		b.bed(8, deck + 1, 6, p.bed());
		b.set(4, deck + 1, 5, Blocks.POTTED_FERN);
		Pieces.lantern(b, 4, deck + 1, 6, false);
		lampPost(b, 9, deck + 1, 3);
		b.floor(4, 2, 8, 3, deck + 1, "porch");
		b.resident(CharacterVariant.WITCHY, 6, deck + 1, 3, 6, 6, 10, at(8, deck + 1, 6), null, false);
	}

	/** Stargazing Hill: a grassy mound with a picnic blanket, a telescope and lanterns on top, star stones around its foot. */
	static void wishingHill(Blueprint b) {
		Palette p = b.palette;
		int[][] height = new int[b.width][b.depth];
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				int h = (int) Math.max(0, Math.round((6.5 - dist(x, z, 7, 7)) / 1.8));
				height[x][z] = h;
				for (int y = 1; y <= h; y++) {
					b.set(x, y, z, y == h ? Blocks.GRASS_BLOCK : Blocks.DIRT);
				}
			}
		}
		int top = height[7][7];
		b.set(7, top + 1, 7, Blocks.BLUE_CARPET);
		b.set(6, height[6][7] + 1, 7, Blocks.WHITE_CARPET);
		b.set(8, height[8][7] + 1, 7, Blocks.BLUE_CARPET);
		b.set(7, height[7][8] + 1, 8, p.fence());
		b.set(7, height[7][8] + 2, 8, Blocks.END_ROD);
		b.chest(7, height[7][6] + 1, 6, "chests/wishing_hill");
		lampPost(b, 5, height[5][5] + 1, 5);
		lampPost(b, 9, height[9][9] + 1, 9);
		// Star stones: the Big Dipper in calcite around the foot of the hill.
		int[][] stars = {{1, 2}, {3, 1}, {5, 0}, {8, 0}, {12, 1}, {13, 4}, {11, 4}};
		for (int[] s : stars) {
			b.set(s[0], height[s[0]][s[1]] + 1, s[1], Blocks.CALCITE);
		}
		b.floor(6, 7, 8, 7, top + 1, "hilltop");
		b.resident(CharacterVariant.BOOKWORM, 7, height[7][13] + 1, 13, 7, 7, 10);
	}

	/** Amethyst Garden: a calcite path around a pool, amethyst clusters growing in the beds, a tinted-glass pavilion. */
	static void amethystGarden(Blueprint b) {
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 6, 6);
				if (d <= 1.5) {
					b.set(x, 0, z, Blocks.WATER);
					b.set(x, -1, z, Blocks.WATER);
				} else if (d <= 2.5) {
					b.set(x, 0, z, Blocks.CALCITE);
				} else if (d <= 5.2) {
					int roll = b.random.nextInt(6);
					if (roll == 0) {
						b.set(x, 0, z, Blocks.AMETHYST_BLOCK);
						b.set(x, 1, z, Blocks.AMETHYST_CLUSTER);
					} else if (roll == 1) {
						b.set(x, 1, z, Blocks.ALLIUM);
					} else if (roll == 2) {
						b.set(x, 1, z, Blocks.LILAC.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
						b.set(x, 2, z, Blocks.LILAC.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
					}
				}
			}
		}
		Pieces.path(b, 6, 0, 6, 3, 0);
		b.clear(6, 1, 0, 6, 2, 3);
		for (int x = 6; x <= 6; x++) {
			b.set(x, 0, 0, Blocks.CALCITE);
		}
		for (int[] c : new int[][]{{9, 9}, {11, 9}, {9, 11}, {11, 11}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], Blocks.AMETHYST_BLOCK);
		}
		b.fill(9, 4, 9, 11, 4, 11, Blocks.TINTED_GLASS);
		b.clear(10, 1, 9, 10, 3, 11);
		b.clear(9, 1, 10, 11, 3, 10);
		b.chest(10, 1, 10, "chests/amethyst_garden");
		b.set(10, 1, 12, Pieces.stairs(b.palette.stairs(), Direction.SOUTH));
		b.resident(CharacterVariant.WITCHY, 6, 1, 1, 6, 6, 8);
	}

	/** Lily's Letter Post: a little post office with a wall of mailbox barrels, a counter with a bell, a mailbox out front. */
	static void lilyLetterPost(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 1, 1, 9, 6, 0, 3, Pieces.Roof.GABLE_X, "letter post");
		BlockState mailbox = Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.NORTH);
		for (int x = 2; x <= 8; x++) {
			for (int y = 1; y <= 2; y++) {
				b.set(x, y, 6, mailbox);
			}
		}
		b.barrel(4, 1, 6, "chests/lily_letter_post");
		b.barrel(6, 2, 6, "chests/lily_letter_post");
		for (int x = 3; x <= 7; x++) {
			b.set(x, 1, 3, Pieces.topSlab(p.slab()));
		}
		b.set(7, 2, 3, Blocks.BELL);
		b.set(5, 1, 4, Blocks.LECTERN);
		b.set(3, 1, 5, Blocks.CARTOGRAPHY_TABLE);
		b.set(10, 1, 0, p.fence());
		b.set(10, 2, 0, Blocks.RED_WOOL);
		Pieces.path(b, door, 0, door, 0, 0);
		b.floor(3, 4, 7, 5, 1, "behind the counter");
		b.resident(CharacterVariant.LILY, door - 1, 1, 0, door, 3, 8);
	}

	/** Fairy Ring: a ring of mushrooms on podzol in a mossy clearing, standing stones, a candle-lit pedestal and two mushroom stools. */
	static void fairyRing(Blueprint b) {
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 6, 6);
				if (d >= 3.6 && d <= 4.6) {
					b.set(x, 0, z, Blocks.PODZOL);
					b.set(x, 1, z, (x + z) % 2 == 0 ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM);
				} else if (d < 3.6 && b.random.nextInt(3) == 0) {
					b.set(x, 1, z, Blocks.MOSS_CARPET);
				}
			}
		}
		for (int[] s : new int[][]{{6, 0}, {12, 6}, {6, 12}, {0, 6}, {2, 2}, {10, 10}}) {
			b.fill(s[0], 1, s[1], s[0], 2, s[1], Blocks.MOSSY_COBBLESTONE_WALL);
		}
		b.clear(6, 1, 2, 6, 1, 2);
		b.set(6, 0, 2, Blocks.MOSS_BLOCK);
		b.set(6, 1, 6, Blocks.CHISELED_STONE_BRICKS);
		b.set(6, 2, 6, litCandles(Blocks.PURPLE_CANDLE, 4));
		b.chest(6, 1, 7, "chests/fairy_ring");
		b.set(4, 1, 6, Blocks.BROWN_MUSHROOM_BLOCK);
		b.set(8, 1, 6, Blocks.BROWN_MUSHROOM_BLOCK);
		b.floor(5, 4, 7, 5, 1, "inside the ring");
		b.resident(CharacterVariant.WITCHY, 6, 1, 1, 6, 6, 8);
	}

	/** Winter Dome Garden: a glass dome over a warm garden in the snow: berry bushes, potted spruces, a bench and a lamp. */
	static void winterDomeGarden(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				for (int y = 0; y <= 6; y++) {
					double r = Math.sqrt((x - 6) * (x - 6) + y * y + (z - 6) * (z - 6));
					if (y == 0 && r <= 6.4) {
						b.set(x, 0, z, Blocks.PODZOL);
					} else if (y > 0 && Math.abs(r - 6.0) < 0.6) {
						b.set(x, y, z, Blocks.GLASS);
					} else if (y > 0 && r < 5.4) {
						b.set(x, y, z, Blocks.AIR);
					}
				}
			}
		}
		b.clear(6, 1, 0, 6, 2, 1);
		Pieces.path(b, 6, 0, 6, 3, 0);
		BlockState berries = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3);
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 6, 6);
				if (d >= 3.5 && d <= 4.4 && z > 4 && b.random.nextInt(2) == 0) {
					b.set(x, 1, z, berries);
				}
			}
		}
		for (int[] s : new int[][]{{3, 8}, {9, 8}, {6, 10}}) {
			b.set(s[0], 1, s[1], Pieces.logAlong(Blocks.SPRUCE_LOG.defaultBlockState(), Direction.Axis.Y));
			b.set(s[0], 2, s[1], Blocks.POTTED_SPRUCE_SAPLING);
		}
		lampPost(b, 6, 1, 6);
		b.set(4, 1, 5, Pieces.stairs(p.stairs(), Direction.WEST));
		b.chest(8, 1, 5, "chests/winter_dome_garden");
		b.floor(5, 3, 7, 5, 1, "dome");
		b.resident(CharacterVariant.GARDENER, 6, 1, 3, 6, 6, 6);
	}

	/**
	 * Cherry Tea House: a raised tea room with log frames and paper walls under a wide hip roof, a low tea table with
	 * cushions, a koi pond with stepping stones, a stone lantern.
	 */
	static void cherryTeaHouse(Blueprint b) {
		Palette p = b.palette;
		b.fill(2, 0, 3, 12, 0, 10, p.stoneBase());
		b.fill(2, 1, 3, 12, 1, 10, p.planks());
		b.walls(2, 3, 12, 10, 2, 4, Blocks.WHITE_WOOL.defaultBlockState());
		b.clear(3, 2, 4, 11, 4, 9);
		for (int x = 2; x <= 12; x += 2) {
			b.fill(x, 2, 3, x, 4, 3, p.log());
			b.fill(x, 2, 10, x, 4, 10, p.log());
		}
		for (int z = 3; z <= 10; z += 7) {
			b.fill(2, 2, z, 2, 4, z, p.log());
			b.fill(12, 2, z, 12, 4, z, p.log());
		}
		b.clear(6, 2, 3, 8, 3, 3);
		b.set(7, 1, 2, Pieces.stairs(p.stairs(), Direction.SOUTH));
		Pieces.hipRoof(b, 2, 3, 12, 10, 5);
		b.floor(3, 4, 11, 9, 2, "tea room");
		b.set(7, 2, 6, Pieces.topSlab(p.slab()));
		for (int[] c : new int[][]{{6, 6}, {8, 6}, {7, 5}, {7, 7}}) {
			b.set(c[0], 2, c[1], Blocks.PINK_CARPET);
		}
		b.set(3, 2, 9, Blocks.DECORATED_POT);
		Pieces.lantern(b, 4, 2, 4, false);
		Pieces.lantern(b, 10, 2, 4, false);
		b.bed(11, 2, 8, p.bed());
		b.chest(3, 2, 8, "chests/cherry_tea_house");
		// Koi pond and stepping stones.
		for (int x = 9; x <= 13; x++) {
			for (int z = 0; z <= 1; z++) {
				b.set(x, 0, z, Blocks.WATER);
				b.set(x, -1, z, Blocks.WATER);
			}
		}
		b.set(11, 1, 0, Blocks.LILY_PAD);
		for (int z = 0; z <= 1; z++) {
			b.set(7, 0, z, Blocks.SMOOTH_STONE);
		}
		b.set(3, 1, 1, p.stoneWall());
		Pieces.lantern(b, 3, 2, 1, false);
		b.resident(CharacterVariant.LILY, 7, 1, 1, 7, 6, 10, at(11, 2, 8), null, false);
	}

	/** Bookbinder's Workshop: a wall of bookshelves, two lecterns, an anvil "press", map and crafting tables, a bed. */
	static void bookbindersWorkshop(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 1, 1, 11, 8, 0, 4, Pieces.Roof.GABLE_X, "bookbinder's workshop");
		Pieces.path(b, door, 0, door, 0, 0);
		Pieces.library(b, 2, 1, 7, 7);
		b.set(7, 1, 4, Blocks.LECTERN);
		b.set(9, 1, 7, Blocks.ANVIL);
		b.set(10, 1, 7, Blocks.CARTOGRAPHY_TABLE);
		b.set(10, 1, 3, Blocks.CRAFTING_TABLE);
		b.set(10, 1, 4, Blocks.LOOM);
		b.bed(2, 1, 3, p.bed());
		b.chest(3, 1, 3, "chests/bookbinders_workshop");
		b.fill(2, 1, 5, 2, 2, 5, Blocks.BOOKSHELF);
		b.resident(CharacterVariant.BOOKWORM, door, 1, 0, door, 4, 10, at(2, 1, 3), null, false);
	}

	/** Rose Arch Garden: a tunnel of leafy arches lined with rose bushes, leading to a round garden with a safe wishing well. */
	static void roseArchGarden(Blueprint b) {
		Palette p = b.palette;
		BlockState leaves = LilyBlueprints.leaves(p.wood().leaves());
		BlockState roseLower = Blocks.ROSE_BUSH.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
		BlockState roseUpper = Blocks.ROSE_BUSH.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
		Pieces.path(b, 5, 0, 7, 9, 0);
		for (int z = 0; z <= 8; z += 2) {
			b.fill(4, 1, z, 4, 3, z, p.fence());
			b.fill(8, 1, z, 8, 3, z, p.fence());
			b.fill(4, 4, z, 8, 4, z, leaves);
		}
		for (int z = 1; z <= 7; z += 2) {
			b.set(5, 4, z, leaves);
			b.set(7, 4, z, leaves);
		}
		for (int z = 0; z <= 8; z++) {
			for (int x : new int[]{3, 9}) {
				b.set(x, 1, z, roseLower);
				b.set(x, 2, z, roseUpper);
			}
		}
		// The round garden and the well (fenced and roofed, so nobody falls in).
		for (int x = 0; x < b.width; x++) {
			for (int z = 10; z < b.depth; z++) {
				double d = dist(x, z, 6, 12);
				if (d >= 2.5 && d <= 4.5 && b.random.nextInt(3) != 0) {
					b.set(x, 0, z, Blocks.GRASS_BLOCK);
					b.set(x, 1, z, LilyBlueprints.flower(b));
				}
			}
		}
		b.fill(5, 0, 11, 7, 0, 13, p.stoneBase());
		b.set(6, 0, 12, Blocks.WATER);
		b.set(6, -1, 12, Blocks.WATER);
		b.walls(5, 11, 7, 13, 1, 1, p.fence());
		for (int[] c : new int[][]{{5, 11}, {7, 11}, {5, 13}, {7, 13}}) {
			b.fill(c[0], 2, c[1], c[0], 3, c[1], p.fence());
		}
		b.fill(5, 4, 11, 7, 4, 13, p.slab());
		b.set(6, 1, 16, Pieces.stairs(p.stairs(), Direction.SOUTH));
		b.chest(9, 1, 15, "chests/rose_arch_garden");
		b.floor(5, 3, 7, 7, 1, "rose arch");
		b.resident(CharacterVariant.GARDENER, 6, 1, 1, 6, 10, 10);
	}
}
