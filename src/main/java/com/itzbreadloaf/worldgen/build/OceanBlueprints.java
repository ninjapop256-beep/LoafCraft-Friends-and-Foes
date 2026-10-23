package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Update 2's new ocean, river and beach structures (part 8, batch 5). Things on water put their deck at y 0, which
 * is the water's surface; posts go down to the bottom.
 */
final class OceanBlueprints {
	private OceanBlueprints() {
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

	/** A beach umbrella: a fence pole and a 3x3 striped canopy. */
	private static void umbrella(Blueprint b, int x, int z, Block a, Block c) {
		b.fill(x, 1, z, x, 3, z, b.palette.fence());
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				b.set(x + dx, 4, z + dz, (dx + dz) % 2 == 0 ? a : c);
			}
		}
	}

	/** Beach Snack Hut: a counter of snacks under a slab roof, striped umbrellas with towels, and a sand castle. */
	static void beachSnackHut(Blueprint b) {
		Palette p = b.palette;
		for (int[] c : new int[][]{{1, 1}, {5, 1}, {1, 4}, {5, 4}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.fence());
		}
		b.fill(0, 4, 0, 6, 4, 5, p.slab());
		for (int x = 2; x <= 4; x++) {
			b.set(x, 1, 2, Pieces.topSlab(p.slab()));
		}
		b.set(3, 2, 2, Blocks.CAKE);
		b.set(2, 2, 2, Blocks.MELON);
		b.chest(2, 1, 3, "chests/beach_snack_hut");
		b.set(4, 1, 3, Blocks.SMOKER);
		b.set(2, 1, 0, Pieces.stairs(p.stairs(), Direction.NORTH));
		b.set(4, 1, 0, Pieces.stairs(p.stairs(), Direction.NORTH));
		Pieces.lantern(b, 3, 3, 3, true);
		umbrella(b, 3, 8, Blocks.RED_WOOL, Blocks.WHITE_WOOL);
		umbrella(b, 9, 8, Blocks.LIGHT_BLUE_WOOL, Blocks.WHITE_WOOL);
		b.fill(2, 1, 7, 2, 1, 9, Blocks.ORANGE_CARPET);
		b.fill(10, 1, 7, 10, 1, 9, Blocks.YELLOW_CARPET);
		// Sand castle.
		b.fill(9, 1, 2, 11, 1, 4, Blocks.SANDSTONE);
		for (int[] c : new int[][]{{9, 2}, {11, 2}, {9, 4}, {11, 4}}) {
			b.set(c[0], 2, c[1], Blocks.CHISELED_SANDSTONE);
		}
		b.set(10, 2, 3, Blocks.CUT_SANDSTONE);
		b.floor(3, 3, 3, 3, 1, "behind the counter");
		b.resident(CharacterVariant.BREADLOAF, 3, 1, 6, 6, 5, 10);
		b.resident(CharacterVariant.VILLAGER, 4, 1, 6, 6, 5, 10);
	}

	/** Lifeguard Tower: a red-and-white lookout on stilts, a ladder up the front, a flag and a chest of beach gear. */
	static void lifeguardTower(Blueprint b) {
		Palette p = b.palette;
		int deck = 4;
		b.fill(2, deck, 2, 6, deck, 6, p.planks());
		for (int[] c : new int[][]{{2, 2}, {6, 2}, {2, 6}, {6, 6}}) {
			b.fill(c[0], 1, c[1], c[0], deck - 1, c[1], p.log());
		}
		Pieces.ladder(b, 4, 1, deck, 1, Direction.NORTH);
		b.fill(2, deck + 1, 6, 6, deck + 2, 6, Blocks.RED_CONCRETE);
		b.fill(2, deck + 1, 3, 2, deck + 2, 5, Blocks.WHITE_CONCRETE);
		b.fill(6, deck + 1, 3, 6, deck + 2, 5, Blocks.WHITE_CONCRETE);
		b.fill(2, deck + 3, 2, 6, deck + 3, 6, Blocks.RED_CONCRETE);
		for (int x = 2; x <= 6; x++) {
			if (x != 4) {
				b.set(x, deck + 1, 2, p.fence());
			}
		}
		b.fill(7, 1, 7, 7, deck + 4, 7, p.fence());
		b.set(7, deck + 4, 8, Blocks.RED_WOOL);
		b.chest(3, deck + 1, 5, "chests/lifeguard_tower");
		Pieces.lantern(b, 5, deck + 1, 5, false);
		b.floor(3, 3, 5, 4, deck + 1, "lookout");
		b.resident(CharacterVariant.FISHERMAN, 4, deck + 1, 4, 4, 4, 12);
	}

	/** River Ferry Landing: a dock out into the river with a booth and a bell to call the ferry, the ferry raft at the end. */
	static void ferryLanding(Blueprint b) {
		Palette p = b.palette;
		b.foundation = false;
		b.groundLayer = false;
		b.skirt = false;
		Pieces.dock(b, 4, 0, 8, 12, 0);
		for (int[] c : new int[][]{{5, 1}, {7, 1}, {5, 3}, {7, 3}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.fence());
		}
		b.fill(4, 4, 0, 8, 4, 4, p.slab());
		b.set(5, 1, 2, Blocks.BELL);
		b.barrel(7, 1, 2, "chests/barrel_supplies");
		// The ferry: a log-edged raft tied up at the end of the dock.
		b.fill(3, 0, 13, 9, 0, 16, p.planks());
		b.walls(3, 13, 9, 16, 0, 0, p.log());
		b.fill(4, 0, 14, 8, 0, 15, p.planks());
		for (int[] c : new int[][]{{3, 16}, {9, 16}}) {
			lampPost(b, c[0], 1, c[1]);
		}
		b.set(3, 1, 13, p.fence());
		b.set(9, 1, 13, p.fence());
		b.chest(8, 1, 15, "chests/ferry_landing");
		b.barrel(4, 1, 15, "chests/barrel_supplies");
		b.floor(5, 14, 7, 15, 1, "ferry");
		b.resident(CharacterVariant.FISHER, 6, 1, 6, 6, 8, 10);
	}

	/** Pearl Diver's Raft: a raft floating on a warm sea, a lean-to with a bed, a ladder down into the water, fishing gear. */
	static void pearlDiverRaft(Blueprint b) {
		Palette p = b.palette;
		b.foundation = false;
		b.groundLayer = false;
		b.skirt = false;
		b.fill(2, 0, 2, 8, 0, 8, p.planks());
		b.walls(2, 2, 8, 8, 0, 0, p.log());
		Pieces.ladder(b, 5, -3, -1, 1, Direction.NORTH);
		b.set(5, 0, 1, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.NORTH));
		for (int[] c : new int[][]{{3, 6}, {7, 6}}) {
			b.fill(c[0], 1, c[1], c[0], 2, c[1], p.fence());
		}
		b.fill(3, 3, 6, 7, 3, 8, Blocks.LIGHT_BLUE_WOOL);
		b.bed(4, 1, 7, p.bed());
		b.chest(6, 1, 8, "chests/pearl_diver_raft");
		b.barrel(7, 1, 7, "chests/fishing_barrel");
		b.fill(8, 1, 3, 8, 2, 3, p.fence());
		lampPost(b, 2, 1, 2);
		b.floor(3, 3, 7, 5, 1, "raft");
		b.resident(CharacterVariant.FISHERMAN, 5, 1, 4, 5, 5, 8, at(4, 1, 7), null, false);
	}

	/** Sea Glass Cove: a rock pool with sea glass at the bottom and on the sand, a reading spot under an umbrella. */
	static void seaGlassCove(Blueprint b) {
		Block[] glass = {Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS};
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 4, 5);
				if (d <= 2.5) {
					b.set(x, 0, z, Blocks.WATER);
					b.set(x, -1, z, glass[(x + z) % glass.length]);
				} else if (d <= 3.4) {
					b.set(x, 0, z, Blocks.STONE);
					if (b.random.nextBoolean()) {
						b.set(x, 1, z, b.random.nextBoolean() ? Blocks.COBBLESTONE : Blocks.ANDESITE);
					}
				} else if (b.random.nextInt(14) == 0) {
					b.set(x, 0, z, glass[b.random.nextInt(glass.length)]);
				}
			}
		}
		umbrella(b, 9, 5, Blocks.CYAN_WOOL, Blocks.WHITE_WOOL);
		b.fill(8, 1, 7, 10, 1, 7, Blocks.PINK_CARPET);
		b.set(8, 1, 4, Blocks.LECTERN);
		b.chest(10, 1, 3, "chests/sea_glass_cove");
		b.resident(CharacterVariant.LILY, 6, 1, 9, 6, 5, 10);
	}

	/**
	 * Fishermen's Wharf: a long pier on posts with a net shed (two beds, the wharf's chest) at the shore end, fishing
	 * spots with barrels along it, nets drying on the rails, and a wide platform with a bell at the far end.
	 */
	static void fishermensWharf(Blueprint b) {
		Palette p = b.palette;
		b.foundation = false;
		b.groundLayer = false;
		b.skirt = false;
		for (int[] c : new int[][]{{4, 1}, {10, 1}, {4, 6}, {10, 6}}) {
			b.pillar(c[0], -1, c[1], p.log());
		}
		Pieces.room(b, 4, 1, 10, 6, 0, 3, "net shed");
		Pieces.roof(b, Pieces.Roof.GABLE_X, 4, 1, 10, 6, 3);
		Pieces.door(b, 7, 1, 6, Direction.SOUTH);
		Pieces.window(b, 4, 2, 3);
		Pieces.window(b, 10, 2, 3);
		b.bed(5, 1, 2, p.bed());
		b.bed(9, 1, 2, p.bed());
		b.chest(7, 1, 2, "chests/fishermens_wharf");
		b.barrel(6, 1, 2, "chests/fishing_barrel");
		Pieces.lantern(b, 8, 1, 5, false);
		Pieces.dock(b, 5, 7, 9, 18, 0);
		b.fill(3, 0, 15, 11, 0, 18, p.planks());
		b.clear(5, 1, 15, 9, 1, 18);
		for (int[] c : new int[][]{{3, 15}, {11, 15}, {3, 18}, {11, 18}}) {
			b.pillar(c[0], -1, c[1], p.log());
		}
		for (int z = 9; z <= 13; z += 2) {
			b.set(5, 2, z, Blocks.COBWEB);
			b.set(9, 2, z, Blocks.COBWEB);
		}
		b.barrel(6, 1, 10, "chests/fishing_barrel");
		b.barrel(8, 1, 13, "chests/fishing_barrel");
		b.set(7, 1, 18, Blocks.BELL);
		lampPost(b, 3, 1, 18);
		lampPost(b, 11, 1, 18);
		b.floor(4, 16, 10, 17, 1, "pier end");
		b.resident(CharacterVariant.FISHER, 7, 1, 9, 7, 10, 12, at(5, 1, 2), null, false);
		b.resident(CharacterVariant.FISHERMAN, 7, 1, 9, 7, 10, 12, at(9, 1, 2), null, false);
	}
}
