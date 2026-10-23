package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Update 2's new End structures (part 8, batch 6), on the outer islands. Purpur and end stone; no beds (they explode
 * in the End).
 */
final class EndBlueprints {
	private EndBlueprints() {
	}

	private static double dist(int x, int z, double cx, double cz) {
		return Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
	}

	/** Void Observatory: a round purpur tower, bookshelves, a ladder to a loft with a telescope under a glass skylight. */
	static void voidObservatory(Blueprint b) {
		BlockState purpur = Blocks.PURPUR_BLOCK.defaultBlockState();
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 5, 5);
				if (d <= 3.5) {
					b.set(x, 0, z, Blocks.END_STONE_BRICKS);
					b.set(x, 9, z, d > 2.5 ? Blocks.PURPUR_SLAB.defaultBlockState() : Blocks.GLASS.defaultBlockState());
				}
				if (d > 2.5 && d <= 3.5) {
					b.fill(x, 1, z, x, 8, z, purpur);
				} else if (d <= 2.5) {
					b.clear(x, 1, z, x, 8, z);
					b.set(x, 5, z, Blocks.PURPUR_BLOCK);
				}
			}
		}
		b.clear(5, 1, 2, 5, 2, 2);
		for (int y : new int[]{3, 7}) {
			b.set(2, y, 5, Blocks.GLASS_PANE);
			b.set(8, y, 5, Blocks.GLASS_PANE);
		}
		Pieces.ladder(b, 5, 1, 5, 7, Direction.NORTH);
		b.fill(3, 1, 4, 3, 2, 6, Blocks.BOOKSHELF);
		b.fill(7, 1, 4, 7, 2, 6, Blocks.BOOKSHELF);
		b.set(5, 1, 5, Blocks.LECTERN);
		b.set(4, 1, 6, Blocks.END_ROD);
		b.set(5, 6, 4, Blocks.PURPUR_PILLAR);
		b.set(5, 7, 4, Blocks.END_ROD);
		b.chest(4, 6, 6, "chests/void_observatory");
		b.floor(4, 3, 6, 4, 1, "observatory");
		b.floor(4, 5, 6, 6, 6, "loft");
		b.resident(CharacterVariant.BOOKWORM, 5, 1, 0, 5, 5, 8);
	}

	/** A chorus stalk: chorus plant blocks up to height h, a chorus flower on top. */
	private static void chorus(Blueprint b, int x, int z, int h) {
		BlockState plant = Blocks.CHORUS_PLANT.defaultBlockState().setValue(PipeBlock.DOWN, true).setValue(PipeBlock.UP, true);
		for (int y = 1; y <= h; y++) {
			b.set(x, y, z, plant);
		}
		b.set(x, h + 1, z, Blocks.CHORUS_FLOWER.defaultBlockState().setValue(ChorusFlowerBlock.AGE, 0));
	}

	/** Chorus Garden: purpur paths, chorus stalks in each corner, wilted chorus flowers, a purpur gazebo in the middle. */
	static void chorusGarden(Blueprint b) {
		for (int i = 0; i < b.width; i++) {
			b.set(7, 0, i, Blocks.PURPUR_BLOCK);
			b.set(i, 0, 7, Blocks.PURPUR_BLOCK);
		}
		chorus(b, 3, 3, 3);
		chorus(b, 11, 3, 2);
		chorus(b, 3, 11, 2);
		chorus(b, 11, 11, 3);
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				if (x != 7 && z != 7 && !b.isSet(x, 1, z) && b.random.nextInt(9) == 0) {
					b.set(x, 1, z, Blocks.CHORUS_FLOWER.defaultBlockState().setValue(ChorusFlowerBlock.AGE, 5));
				}
			}
		}
		for (int[] c : new int[][]{{5, 5}, {9, 5}, {5, 9}, {9, 9}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], Blocks.PURPUR_PILLAR);
		}
		b.fill(5, 4, 5, 9, 4, 9, Blocks.PURPUR_SLAB);
		b.clear(6, 1, 5, 8, 3, 9);
		b.clear(5, 1, 6, 9, 3, 8);
		b.chest(7, 1, 7, "chests/chorus_garden");
		b.set(7, 3, 7, Blocks.END_ROD.defaultBlockState().setValue(net.minecraft.world.level.block.EndRodBlock.FACING, Direction.DOWN));
		b.floor(6, 6, 8, 6, 1, "gazebo");
		b.resident(CharacterVariant.GARDENER, 7, 1, 2, 7, 7, 8);
	}

	/** The Last Loaf: a golden loaf on a pedestal under a purpur arch, end rods all around. Master BreadLoaf meditates here. */
	static void lastLoafShrine(Blueprint b) {
		b.fill(1, 0, 1, 9, 0, 9, Blocks.END_STONE_BRICKS);
		b.fill(4, 1, 4, 6, 1, 6, Blocks.END_STONE_BRICKS);
		b.fill(4, 2, 5, 6, 2, 5, Blocks.GOLD_BLOCK);
		b.set(5, 3, 5, Blocks.HAY_BLOCK);
		for (int x : new int[]{3, 7}) {
			b.fill(x, 1, 5, x, 5, 5, Blocks.PURPUR_PILLAR);
		}
		b.fill(3, 6, 5, 7, 6, 5, Blocks.PURPUR_BLOCK);
		for (int[] c : new int[][]{{1, 1}, {9, 1}, {1, 9}, {9, 9}, {5, 1}, {5, 9}, {1, 5}, {9, 5}}) {
			b.set(c[0], 1, c[1], Blocks.END_ROD);
		}
		b.chest(5, 1, 8, "chests/last_loaf_shrine");
		b.floor(4, 2, 6, 3, 1, "shrine");
		b.resident(CharacterVariant.MASTER, 5, 1, 2, 5, 5, 6);
	}

	/** Purpur Vault: a sealed purpur vault with end rod lights, shulker boxes of loot on the back wall, a dragon head. */
	static void purpurVault(Blueprint b) {
		BlockState purpur = Blocks.PURPUR_BLOCK.defaultBlockState();
		b.fill(1, 0, 1, 11, 0, 11, Blocks.END_STONE_BRICKS);
		b.walls(1, 1, 11, 11, 1, 5, purpur);
		for (int[] c : new int[][]{{1, 1}, {11, 1}, {1, 11}, {11, 11}}) {
			b.fill(c[0], 1, c[1], c[0], 5, c[1], Blocks.PURPUR_PILLAR);
		}
		b.fill(1, 6, 1, 11, 6, 11, Blocks.PURPUR_SLAB);
		b.clear(2, 1, 2, 10, 5, 10);
		b.clear(6, 1, 1, 6, 2, 1);
		b.floor(2, 2, 10, 9, 1, "vault");
		BlockState box = Blocks.PURPLE_SHULKER_BOX.defaultBlockState();
		for (int x = 3; x <= 9; x += 2) {
			b.container(x, 1, 10, box, "chests/purpur_vault");
		}
		b.set(6, 1, 6, Blocks.PURPUR_PILLAR);
		b.set(6, 2, 6, Blocks.DRAGON_HEAD);
		for (int[] c : new int[][]{{2, 2}, {10, 2}, {2, 10}, {10, 10}}) {
			b.set(c[0], 1, c[1], Blocks.END_ROD);
		}
	}

	/** Dragon Lookout: a tall end stone tower with purpur corners and a ladder to the top, a dragon head keeping watch. */
	static void dragonLookout(Blueprint b) {
		int top = 20;
		Pieces.tower(b, 2, 2, 5, 0, top, 5, true);
		for (int y = 1; y <= top; y++) {
			for (int[] c : new int[][]{{2, 2}, {6, 2}, {2, 6}, {6, 6}}) {
				b.set(c[0], y, c[1], Blocks.PURPUR_PILLAR);
			}
			for (int x = 3; x <= 5; x++) {
				for (int z = 3; z <= 5; z++) {
					if (y % 5 == 0 && b.get(x, y, z) == b.palette.planks()) {
						b.set(x, y, z, Blocks.PURPUR_BLOCK);
					}
				}
			}
		}
		b.clear(4, 1, 2, 4, 2, 2);
		b.walls(2, 2, 6, 6, top + 1, top + 1, Blocks.END_STONE_BRICK_WALL.defaultBlockState());
		b.set(3, top + 1, 3, Blocks.DRAGON_HEAD);
		b.set(6, top + 2, 6, Blocks.END_ROD);
		b.set(2, top + 2, 2, Blocks.END_ROD);
		b.chest(5, top + 1, 3, "chests/dragon_lookout");
	}

	/** Void Fishing Platform: a railed wooden deck on posts with a bench along the back for fishing into the void. */
	static void voidFishingPlatform(Blueprint b) {
		Palette p = b.palette;
		b.foundation = false;
		b.groundLayer = false;
		b.skirt = false;
		b.fill(2, 0, 2, 10, 0, 8, p.planks());
		for (int[] c : new int[][]{{2, 2}, {10, 2}, {2, 8}, {10, 8}, {6, 8}}) {
			b.pillar(c[0], -1, c[1], p.log());
		}
		b.walls(2, 2, 10, 8, 1, 1, p.fence());
		b.clear(6, 1, 2, 6, 1, 2);
		for (int x = 4; x <= 8; x++) {
			b.set(x, 1, 7, Pieces.stairs(p.stairs(), Direction.NORTH));
		}
		b.fill(3, 1, 4, 3, 2, 4, p.fence());
		b.barrel(9, 1, 3, "chests/fishing_barrel");
		b.chest(9, 1, 6, "chests/void_fishing_platform");
		b.set(2, 2, 2, Blocks.END_ROD);
		b.set(10, 2, 8, Blocks.END_ROD);
		b.floor(4, 3, 8, 5, 1, "deck");
		b.resident(CharacterVariant.FISHERMAN, 6, 1, 5, 6, 5, 6);
	}
}
