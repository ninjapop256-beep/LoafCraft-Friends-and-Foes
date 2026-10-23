package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Master BreadLoaf's arenas: the Sky Arena, his Nether training grounds and his End tower and platform. */
final class MasterBlueprints {
	private MasterBlueprints() {
	}

	private static double dist(int x, int z, double cx, double cz) {
		return Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
	}

	/**
	 * A round deck high up on a 3x3 pillar, with stairs winding up around the pillar and through the deck. The deck
	 * has a rim all around and a rail around the stairwell (every side except the way out of the top step).
	 */
	private static void skyDeck(Blueprint b, int c, int deckY, double radius, BlockState floor, BlockState rim,
			BlockState pillar, BlockState stair, BlockState light) {
		b.fill(c - 1, 1, c - 1, c + 1, deckY - 1, c + 1, pillar);
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, c, c);
				if (d > radius) {
					continue;
				}
				b.set(x, deckY, z, floor);
				if (d > radius - 1.0) {
					b.set(x, deckY + 1, z, rim);
					if (x == c || z == c) {
						b.set(x, deckY + 2, z, light);
					}
				}
			}
		}
		List<int[]> ring = Pieces.ringStairs(b, c - 1, c - 1, c + 1, c + 1, 1, deckY, stair);
		int n = ring.size();
		Set<Long> keep = new HashSet<>();
		Set<Long> holes = new HashSet<>();
		for (int k = deckY - 4; k <= deckY - 2; k++) {
			int[] cell = ring.get(k % n);
			holes.add(Blueprint.key(cell[0], 0, cell[1]));
		}
		int[] top = ring.get((deckY - 1) % n);
		int[] exit = ring.get(deckY % n);
		keep.add(Blueprint.key(top[0], 0, top[1]));
		keep.add(Blueprint.key(exit[0], 0, exit[1]));
		for (int k = deckY - 4; k <= deckY - 2; k++) {
			int[] cell = ring.get(k % n);
			for (int[] side : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
				int x = cell[0] + side[0];
				int z = cell[1] + side[1];
				long key = Blueprint.key(x, 0, z);
				boolean pillarCell = Math.abs(x - c) <= 1 && Math.abs(z - c) <= 1;
				if (holes.contains(key) || keep.contains(key) || pillarCell) {
					continue;
				}
				b.set(x, deckY + 1, z, rim);
			}
		}
		b.set(c, deckY, c, Blocks.GOLD_BLOCK);
	}

	/** Sky Arena: Master BreadLoaf's round stone arena high on a pillar, stairs winding up around it. */
	static void skyArena(Blueprint b) {
		Palette p = b.palette;
		int c = 10;
		int deck = 30;
		skyDeck(b, c, deck, 10.0, p.stoneFloor(), p.stoneWall(), p.bricks(), p.stoneStairs(), p.light());
		b.chest(c, deck + 1, 17, "chests/sky_arena");
		b.chest(c, deck + 1, 3, "chests/sky_arena");
		b.floor(4, 4, 16, 16, deck + 1, "arena");
		b.resident(CharacterVariant.MASTER, c, deck + 1, 6, c, c, 8);
	}

	/** End Sky Platform: a purpur deck high over an outer End island, purpur stairs winding up its pillar. */
	static void endSkyPlatform(Blueprint b) {
		int c = 7;
		int deck = 16;
		skyDeck(b, c, deck, 7.0, Blocks.PURPUR_BLOCK.defaultBlockState(), Blocks.END_STONE_BRICK_WALL.defaultBlockState(),
				Blocks.PURPUR_PILLAR.defaultBlockState(), Blocks.PURPUR_STAIRS.defaultBlockState(), Blocks.END_ROD.defaultBlockState());
		b.chest(c, deck + 1, 11, "chests/end_sky_platform");
		b.chest(11, deck + 1, c, "chests/end_sky_platform");
		b.floor(3, 3, 11, 11, deck + 1, "platform");
		b.resident(CharacterVariant.MASTER, c, deck + 1, 3, c, c, 6);
	}

	/**
	 * A 3x3 training pillar from the floor up to {@code h}, with a ladder on the side facing the middle and a light
	 * on one top corner. Its top is declared as a floor (the reachability check makes sure the ladder gets there).
	 */
	private static void pillar(Blueprint b, int x0, int z0, int h, boolean ladderSouth, BlockState block) {
		b.fill(x0, 1, z0, x0 + 2, h, z0 + 2, block);
		if (ladderSouth) {
			Pieces.ladder(b, x0 + 1, 1, h, z0 + 3, Direction.SOUTH);
		} else {
			Pieces.ladder(b, x0 + 1, 1, h, z0 - 1, Direction.NORTH);
		}
		Pieces.lantern(b, x0, h + 1, ladderSouth ? z0 : z0 + 2, false);
		b.floor(x0, z0, x0 + 2, z0 + 2, h + 1, "pillar top");
	}

	/**
	 * Nether Mace Training Ground: a round blackstone floor with four pillars of different heights (ladders up),
	 * hay to land on beside each, training dummies, soul lanterns. Master BreadLoaf trains here.
	 */
	static void netherTrainingGround(Blueprint b) {
		Palette p = b.palette;
		int c = 10;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				if (dist(x, z, c, c) <= 10.0) {
					b.set(x, 0, z, (x + z) % 2 == 0 ? p.stoneBase() : p.bricks());
				}
			}
		}
		BlockState pillarBlock = p.bricks();
		pillar(b, 3, 3, 8, true, pillarBlock);
		pillar(b, 15, 3, 12, true, pillarBlock);
		pillar(b, 3, 15, 16, false, pillarBlock);
		pillar(b, 15, 15, 20, false, pillarBlock);
		for (int[] pad : new int[][]{{6, 6}, {12, 6}, {6, 12}, {12, 12}}) {
			b.fill(pad[0], 0, pad[1], pad[0] + 2, 0, pad[1] + 2, Blocks.HAY_BLOCK);
		}
		for (int[] d : new int[][]{{10, 4}, {10, 16}, {4, 10}, {16, 10}}) {
			b.mob(d[0], 1, d[1], "dummy");
		}
		for (int[] l : new int[][]{{10, 1}, {10, 19}, {1, 10}, {19, 10}}) {
			b.set(l[0], 1, l[1], p.stoneWall());
			Pieces.lantern(b, l[0], 2, l[1], false);
		}
		b.chest(13, 1, 10, "chests/nether_training_ground");
		b.floor(9, 9, 11, 11, 1, "training ground");
		b.resident(CharacterVariant.MASTER, c, 1, c, c, c, 10);
	}

	/**
	 * Nether Mace Arena: a round floor inside a ring of lava (four bridges across), a low rim beyond it, and four
	 * tall pillars with ladders for mace drops. Master BreadLoaf waits in the middle.
	 */
	static void netherMaceArena(Blueprint b) {
		Palette p = b.palette;
		int c = 12;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, c, c);
				boolean bridge = Math.abs(x - c) <= 1 || Math.abs(z - c) <= 1;
				if (d <= 9.5) {
					b.set(x, 0, z, (x + z) % 2 == 0 ? p.stoneBase() : p.bricks());
				} else if (d <= 11.0) {
					b.set(x, -1, z, p.stoneBase());
					b.set(x, 0, z, bridge ? p.stoneFloor() : Blocks.LAVA.defaultBlockState());
				} else if (d <= 12.5) {
					b.set(x, 0, z, p.stoneBase());
					if (!bridge) {
						b.set(x, 1, z, p.stoneWall());
					} else if (Math.abs(x - c) == 1 || Math.abs(z - c) == 1) {
						Pieces.lantern(b, x, 1, z, false);
					}
				}
			}
		}
		b.set(c, 0, c, Blocks.GILDED_BLACKSTONE);
		BlockState pillarBlock = p.bricks();
		pillar(b, 6, 6, 10, true, pillarBlock);
		pillar(b, 16, 6, 14, true, pillarBlock);
		pillar(b, 6, 16, 18, false, pillarBlock);
		pillar(b, 16, 16, 22, false, pillarBlock);
		b.chest(c, 1, 9, "chests/nether_mace_arena");
		b.floor(10, 10, 14, 14, 1, "arena");
		b.resident(CharacterVariant.MASTER, c, 1, c, c, c, 10);
	}

	/**
	 * End Mace Tower: a tall End-stone tower with purpur pillars and floors, a ladder up through five floors to a
	 * crenellated top with end rods. Master BreadLoaf waits on top.
	 */
	static void endMaceTower(Blueprint b) {
		Palette p = b.palette;
		int top = 28;
		Pieces.tower(b, 2, 2, 7, 0, top, 7, true);
		b.clear(5, 1, 2, 5, 2, 2);
		BlockState purpur = Blocks.PURPUR_BLOCK.defaultBlockState();
		for (int y = 1; y <= top; y++) {
			for (int[] c : new int[][]{{2, 2}, {8, 2}, {2, 8}, {8, 8}}) {
				b.set(c[0], y, c[1], Blocks.PURPUR_PILLAR);
			}
			if (y % 7 == 0) {
				for (int x = 3; x <= 7; x++) {
					for (int z = 3; z <= 7; z++) {
						if (b.get(x, y, z) == p.planks()) {
							b.set(x, y, z, purpur);
						}
					}
				}
			}
		}
		for (int i = 2; i <= 8; i += 2) {
			for (int[] c : new int[][]{{i, 2}, {i, 8}, {2, i}, {8, i}}) {
				b.set(c[0], top + 1, c[1], purpur);
			}
		}
		for (int[] c : new int[][]{{2, 2}, {8, 2}, {2, 8}, {8, 8}}) {
			b.set(c[0], top + 2, c[1], Blocks.END_ROD);
		}
		b.chest(7, 8, 3, "chests/end_mace_tower");
		b.chest(3, top + 1, 3, "chests/end_mace_tower");
		b.resident(CharacterVariant.MASTER, 4, top + 1, 4, 5, 5, 6);
	}
}
