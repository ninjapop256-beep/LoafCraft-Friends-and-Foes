package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Update 2's new FelixSyn structures (part 8, batch 2). Wild FelixSyns live in most of them (Harley sometimes). */
final class NewFelixBlueprints {
	private static final Block[] RAINBOW = {Blocks.RED_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE,
			Blocks.LIGHT_BLUE_CONCRETE, Blocks.BLUE_CONCRETE, Blocks.PURPLE_CONCRETE, Blocks.MAGENTA_CONCRETE};

	private NewFelixBlueprints() {
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

	/** A mace on display built from blocks (no free maces). */
	private static void maceRack(Blueprint b, int x, int y, int z) {
		b.set(x, y, z, Blocks.DARK_OAK_FENCE);
		b.set(x, y + 1, z, Blocks.POLISHED_DEEPSLATE_WALL);
	}

	/** Wind Charge Launch Pad: a bullseye pad to land wind-charge jumps on, targets up on posts, a gear booth. */
	static void windLaunchPad(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 7, 7);
				if (d <= 6.0) {
					b.set(x, 0, z, ((int) Math.round(d)) % 2 == 0 ? Blocks.RED_CONCRETE : Blocks.WHITE_CONCRETE);
				}
			}
		}
		for (int[] t : new int[][]{{1, 1}, {13, 1}, {1, 13}}) {
			b.fill(t[0], 1, t[1], t[0], 5, t[1], p.fence());
			b.set(t[0], 6, t[1], Blocks.TARGET);
		}
		for (int[] c : new int[][]{{11, 11}, {14, 11}, {11, 14}, {14, 14}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.fence());
		}
		b.fill(11, 4, 11, 14, 4, 14, p.slab());
		b.bed(13, 1, 12, Blocks.BLACK_BED);
		b.chest(12, 1, 14, "chests/wind_launch_pad");
		Pieces.lantern(b, 12, 3, 12, true);
		b.floor(5, 5, 9, 9, 1, "pad");
		b.resident(CharacterVariant.WILD_FELIX, 7, 1, 12, 7, 7, 12, at(13, 1, 12), null, false);
	}

	/** Smash Crater: a crater where a mace smash landed (cracked stone in the middle), debris all around, a lean-to tent. */
	static void smashCrater(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 8, 8);
				int depth = (int) Math.round((6.0 - d) / 1.5);
				if (d <= 6.0 && depth > 0) {
					b.clear(x, 1 - depth, z, x, 0, z);
					b.set(x, -depth, z, d < 1.6 ? Blocks.CRACKED_STONE_BRICKS : b.random.nextBoolean() ? Blocks.GRAVEL : p.stoneBase().getBlock());
				} else if (d <= 7.5 && b.random.nextInt(4) == 0) {
					b.set(x, 1, z, b.random.nextBoolean() ? Blocks.COBBLESTONE : Blocks.GRAVEL);
				}
			}
		}
		b.mob(8, -2, 9, "dummy");
		b.floor(8, 8, 8, 8, -3, "crater");
		Pieces.tent(b, 0, 12, 5, Blocks.BLACK_WOOL.defaultBlockState());
		b.bed(1, 1, 13, Blocks.BLACK_BED);
		b.chest(3, 1, 15, "chests/smash_crater");
		b.resident(CharacterVariant.WILD_FELIX, 8, 1, 0, 8, 8, 12, at(1, 1, 13), null, false);
	}

	/** FelixSyn's Trophy Hall: trophies on pedestals down both sides, a red-vs-blue banner scoreboard, a gold podium. */
	static void trophyHall(Blueprint b) {
		Palette p = b.palette;
		Pieces.room(b, 1, 1, 13, 9, 0, 5, "trophy hall");
		b.walls(1, 1, 13, 9, 1, 1, p.bricks());
		Pieces.windowsAround(b, 1, 1, 13, 9, 3, 7);
		Pieces.roof(b, Pieces.Roof.GABLE_X, 1, 1, 13, 9, 5);
		Pieces.door(b, 7, 1, 1, Direction.NORTH);
		Pieces.path(b, 7, 0, 7, 0, 0);
		b.fill(7, 1, 2, 7, 1, 7, Blocks.RED_CARPET);
		Block[] west = {Blocks.GOLD_BLOCK, Blocks.EMERALD_BLOCK, Blocks.COPPER_BLOCK};
		int[] zs = {3, 5, 7};
		for (int i = 0; i < 3; i++) {
			b.set(3, 1, zs[i], p.stoneWall());
			b.set(3, 2, zs[i], west[i]);
			b.set(11, 1, zs[i], p.stoneWall());
		}
		b.set(11, 2, 3, Blocks.SKELETON_SKULL);
		maceRack(b, 11, 2, 5);
		b.set(11, 2, 7, Blocks.LAPIS_BLOCK);
		b.set(7, 1, 8, Blocks.GOLD_BLOCK);
		b.set(7, 2, 8, Blocks.END_ROD);
		for (int x : new int[]{4, 5, 9, 10}) {
			b.set(x, 1, 8, x < 7 ? Blocks.RED_BANNER : Blocks.BLUE_BANNER);
		}
		b.set(6, 1, 8, Blocks.RED_BANNER);
		b.set(8, 1, 8, Blocks.BLUE_BANNER);
		b.bed(2, 1, 7, Blocks.BLACK_BED);
		b.chest(12, 1, 8, "chests/felix_trophy_hall");
		Pieces.lantern(b, 2, 1, 2, false);
		Pieces.lantern(b, 12, 1, 2, false);
		b.resident(CharacterVariant.WILD_FELIX, 7, 1, 3, 7, 5, 10, at(2, 1, 7), null, false);
	}

	/** FelixSyn & Harley's Cabin: a cozy cabin with a black bed and a pink bed, a mace rack, a porch with two chairs. */
	static void duoCabin(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 2, 2, 10, 8, 0, 3, Pieces.Roof.GABLE_X, "duo cabin");
		b.fill(2, 0, 0, 10, 0, 1, p.planks());
		for (int x = 2; x <= 10; x++) {
			if (x != door) {
				b.set(x, 1, 0, p.fence());
			}
		}
		b.set(4, 1, 1, Pieces.stairs(p.stairs(), Direction.SOUTH));
		b.set(8, 1, 1, Pieces.stairs(p.stairs(), Direction.SOUTH));
		b.bed(3, 1, 6, Blocks.BLACK_BED);
		b.bed(5, 1, 6, Blocks.PINK_BED);
		b.chest(7, 1, 7, "chests/duo_cabin");
		b.barrel(8, 1, 7, "chests/barrel_supplies");
		maceRack(b, 9, 1, 7);
		Pieces.table(b, 7, 1, 4, true, false);
		b.resident(CharacterVariant.FELIXSYN, door, 1, 1, door, 5, 16, at(3, 1, 6), null, false);
	}

	/** FelixSyn's Parkour Tower: a stone tower with a ladder to the top, a rainbow parkour course around it, hay to land on. */
	static void parkourTower(Blueprint b) {
		int top = 18;
		for (int x = 1; x <= 11; x++) {
			for (int z = 1; z <= 11; z++) {
				b.set(x, 0, z, Blocks.HAY_BLOCK);
			}
		}
		Pieces.tower(b, 4, 4, 5, 0, top, 6, true);
		Pieces.door(b, 6, 1, 4, Direction.NORTH);
		b.walls(4, 4, 8, 8, top + 1, top + 1, b.palette.fence());
		Pieces.lantern(b, 4, top + 2, 4, false);
		b.chest(5, top + 1, 5, "chests/parkour_tower");
		// The course: one block every other cell around the square, each one higher.
		int step = 0;
		for (int[] c : ring(2, 2, 10, 10)) {
			if ((c[0] + c[1]) % 2 == 0 && step < 16) {
				b.set(c[0], 1 + step, c[1], RAINBOW[step % RAINBOW.length]);
				step++;
			}
		}
		b.bed(5, 1, 6, Blocks.BLACK_BED);
		b.resident(CharacterVariant.WILD_FELIX, 6, 1, 2, 6, 6, 12, at(5, 1, 6), null, false);
	}

	/** The cells around a square's edge, clockwise from its north-west corner. */
	private static java.util.List<int[]> ring(int x0, int z0, int x1, int z1) {
		java.util.List<int[]> cells = new java.util.ArrayList<>();
		for (int x = x0; x < x1; x++) {
			cells.add(new int[]{x, z0});
		}
		for (int z = z0; z < z1; z++) {
			cells.add(new int[]{x1, z});
		}
		for (int x = x1; x > x0; x--) {
			cells.add(new int[]{x, z1});
		}
		for (int z = z1; z > z0; z--) {
			cells.add(new int[]{x0, z});
		}
		return cells;
	}

	/** Sparring Ring: a raised ring with rope fences and red and blue corner posts, steps up, benches for the crowd. */
	static void sparringRing(Blueprint b) {
		Palette p = b.palette;
		b.fill(3, 1, 3, 11, 1, 11, Blocks.WHITE_WOOL);
		b.fill(4, 1, 4, 6, 1, 6, Blocks.RED_WOOL);
		b.fill(8, 1, 8, 10, 1, 10, Blocks.BLUE_WOOL);
		b.walls(3, 3, 11, 11, 2, 3, p.fence());
		b.clear(7, 2, 3, 7, 3, 3);
		b.set(7, 1, 2, Pieces.stairs(p.stairs(), Direction.SOUTH));
		b.fill(3, 1, 3, 3, 4, 3, Blocks.RED_CONCRETE);
		b.fill(11, 1, 11, 11, 4, 11, Blocks.RED_CONCRETE);
		b.fill(11, 1, 3, 11, 4, 3, Blocks.BLUE_CONCRETE);
		b.fill(3, 1, 11, 3, 4, 11, Blocks.BLUE_CONCRETE);
		b.mob(7, 2, 8, "dummy");
		for (int x = 4; x <= 10; x++) {
			b.set(x, 1, 13, Pieces.stairs(p.stairs(), Direction.SOUTH));
		}
		b.chest(1, 1, 13, "chests/sparring_ring");
		lampPost(b, 1, 1);
		lampPost(b, 13, 1);
		lampPost(b, 13, 13);
		b.floor(4, 4, 10, 10, 2, "ring");
		b.resident(CharacterVariant.WILD_FELIX, 7, 2, 6, 7, 7, 10);
	}

	/**
	 * Mace Drop Tower: an open log tower with a ladder up its middle post to three platforms, each with a jump board
	 * out over a deep water pool for practice drops.
	 */
	static void maceDropTower(Blueprint b) {
		Palette p = b.palette;
		BlockState log = p.log();
		for (int[] c : new int[][]{{4, 4}, {6, 4}, {4, 6}, {6, 6}}) {
			b.fill(c[0], 1, c[1], c[0], 21, c[1], log);
		}
		b.fill(5, 1, 5, 5, 21, 5, log);
		// Platforms wider than the posts, so there's room to step off the ladder; a jump board out over the pool.
		for (int h : new int[]{7, 14, 21}) {
			b.fill(3, h, 3, 7, h, 7, p.planks());
			b.fill(5, h, 1, 5, h, 2, p.planks());
			b.set(4, h + 1, 2, p.fence());
			b.set(6, h + 1, 2, p.fence());
			b.floor(3, 3, 7, 7, h + 1, "platform");
		}
		Pieces.ladder(b, 5, 1, 21, 4, Direction.NORTH);
		b.walls(3, 3, 7, 7, 22, 22, p.fence());
		b.clear(5, 22, 3, 5, 22, 3);
		b.chest(6, 22, 6, "chests/mace_drop_tower");
		// The pool under the boards.
		b.fill(3, -2, 0, 7, -2, 1, p.stoneBase());
		b.fill(3, -1, 0, 7, 0, 1, Blocks.WATER);
		b.bed(1, 1, 8, Blocks.BLACK_BED);
		Pieces.campfire(b, 8, 1, 9);
		b.resident(CharacterVariant.WILD_FELIX, 8, 1, 5, 5, 5, 10, at(1, 1, 8), null, false);
	}

	private static final String[] LETTER_F = {"#####", "#....", "#....", "####.", "#....", "#....", "#...."};
	private static final String[] LETTER_B = {"####.", "#...#", "#...#", "####.", "#...#", "#...#", "####."};
	private static final String[] LETTER_V = {"#.#", "#.#", "#.#", "#.#", ".#."};
	private static final String[] LETTER_S = {"###", "#..", "###", "..#", "###"};

	/** Draws a letter on the wall at z, its top row at yTop. */
	private static void letter(Blueprint b, String[] rows, int x0, int yTop, int z, Block block) {
		for (int r = 0; r < rows.length; r++) {
			for (int c = 0; c < rows[r].length(); c++) {
				if (rows[r].charAt(c) == '#') {
					b.set(x0 + c, yTop - r, z, block);
				}
			}
		}
	}

	/** The Rivalry Wall: a giant "F vs B" wall with a win tally (FelixSyn is winning), and FelixSyn's camp in front. */
	static void rivalryWall(Blueprint b) {
		Palette p = b.palette;
		b.fill(0, 1, 4, 20, 10, 4, Blocks.WHITE_CONCRETE);
		b.fill(0, 1, 4, 20, 1, 4, p.bricks());
		b.fill(0, 11, 4, 20, 11, 4, p.stoneSlab());
		for (int x : new int[]{1, 10, 19}) {
			b.fill(x, 1, 5, x, 9, 5, p.log());
		}
		letter(b, LETTER_F, 2, 9, 4, Blocks.RED_CONCRETE);
		letter(b, LETTER_V, 8, 8, 4, Blocks.BLACK_CONCRETE);
		letter(b, LETTER_S, 11, 8, 4, Blocks.BLACK_CONCRETE);
		letter(b, LETTER_B, 15, 9, 4, Blocks.BLUE_CONCRETE);
		for (int x = 2; x <= 4; x++) {
			b.set(x, 2, 4, Blocks.RED_CONCRETE);
		}
		for (int x = 15; x <= 16; x++) {
			b.set(x, 2, 4, Blocks.BLUE_CONCRETE);
		}
		Pieces.tent(b, 15, 0, 4, Blocks.BLACK_WOOL.defaultBlockState());
		b.bed(16, 1, 1, Blocks.BLACK_BED);
		Pieces.campfire(b, 10, 1, 2);
		b.chest(3, 1, 1, "chests/rivalry_wall");
		b.resident(CharacterVariant.WILD_FELIX, 7, 1, 1, 10, 2, 12, at(16, 1, 1), null, false);
	}

	/** Breeze Hunter's Camp: a tent, a drying rack of breeze "rods", targets on posts and a map table. */
	static void breezeHunterCamp(Blueprint b) {
		Palette p = b.palette;
		Pieces.tent(b, 1, 6, 6, Blocks.LIGHT_GRAY_WOOL.defaultBlockState());
		b.bed(2, 1, 7, Blocks.BLACK_BED);
		b.chest(4, 1, 10, "chests/breeze_hunter_camp");
		Pieces.lantern(b, 3, 1, 10, false);
		for (int x = 8; x <= 12; x++) {
			b.set(x, 1, 9, p.fence());
			b.set(x, 2, 9, Blocks.END_ROD);
		}
		Pieces.campfire(b, 10, 1, 4);
		for (int x : new int[]{1, 13}) {
			b.fill(x, 1, 1, x, 2, 1, p.fence());
			b.set(x, 3, 1, Blocks.TARGET);
		}
		b.set(7, 1, 11, Blocks.CARTOGRAPHY_TABLE);
		b.resident(CharacterVariant.WILD_FELIX, 7, 1, 7, 7, 6, 12, at(2, 1, 7), null, false);
	}

	/** Harley's Training Grove: a clearing of pink petals ringed by blossom trees, dummies and a target, her pink tent. */
	static void harleysGrove(Blueprint b) {
		BlockState leaves = LilyBlueprints.leaves(Blocks.CHERRY_LEAVES);
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				if (dist(x, z, 7, 7) <= 5.5 && b.random.nextInt(3) == 0) {
					b.set(x, 1, z, Blocks.PINK_PETALS);
				}
			}
		}
		for (int[] t : new int[][]{{2, 2}, {12, 2}, {2, 12}, {12, 12}}) {
			b.fill(t[0], 1, t[1], t[0], 4, t[1], Blocks.CHERRY_LOG);
			for (int y = 3; y <= 5; y++) {
				int r = y == 5 ? 1 : 2;
				for (int dx = -r; dx <= r; dx++) {
					for (int dz = -r; dz <= r; dz++) {
						int x = t[0] + dx;
						int z = t[1] + dz;
						if (x >= 0 && z >= 0 && x < b.width && z < b.depth && !(Math.abs(dx) == 2 && Math.abs(dz) == 2) && !b.isSet(x, y, z)) {
							b.set(x, y, z, leaves);
						}
					}
				}
			}
		}
		b.mob(5, 1, 7, "dummy");
		b.mob(9, 1, 7, "dummy");
		b.set(7, 1, 3, b.palette.fence());
		b.set(7, 2, 3, Blocks.TARGET);
		Pieces.tent(b, 5, 10, 4, Blocks.PINK_WOOL.defaultBlockState());
		b.bed(6, 1, 11, Blocks.PINK_BED);
		b.chest(8, 1, 12, "chests/harleys_training_grove");
		b.floor(6, 5, 8, 8, 1, "clearing");
		b.resident(CharacterVariant.HARLEY, 7, 1, 6, 7, 7, 10, at(6, 1, 11), null, false);
	}
}
