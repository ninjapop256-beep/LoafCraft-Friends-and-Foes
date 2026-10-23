package com.itzbreadloaf.worldgen.build;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

/** Update 2's easter eggs (part 8, batch 2): very rare, a little silly, nobody home. */
final class EasterEggBlueprints {
	private EasterEggBlueprints() {
	}

	private static double dist(int x, int z, double cx, double cz) {
		return Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
	}

	/** The Giant Mace: a huge mace head half-buried in a cracked crater, its handle sticking up into the sky. */
	static void giantMace(Blueprint b) {
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				if (dist(x, z, 4, 4) <= 4.2 && b.random.nextInt(3) != 0) {
					b.set(x, 0, z, b.random.nextBoolean() ? Blocks.CRACKED_DEEPSLATE_TILES : Blocks.GRAVEL);
				}
			}
		}
		b.fill(2, -2, 2, 6, 2, 6, Blocks.POLISHED_DEEPSLATE);
		for (int[] c : new int[][]{{2, 2}, {6, 2}, {2, 6}, {6, 6}}) {
			b.fill(c[0], -2, c[1], c[0], 2, c[1], Blocks.CHISELED_DEEPSLATE);
		}
		b.walls(2, 2, 6, 6, 0, 0, Blocks.GOLD_BLOCK.defaultBlockState());
		b.fill(4, 3, 4, 4, 13, 4, Blocks.STRIPPED_DARK_OAK_LOG);
		b.set(4, 14, 4, Blocks.POLISHED_DEEPSLATE);
		b.set(4, 15, 4, Blocks.HEAVY_CORE);
		b.chest(4, 1, 0, "chests/giant_mace");
	}

	/** The Tiny House: a little house with a two-block room (a chest and a flower pot), a tiny fenced garden. */
	static void tinyHouse(Blueprint b) {
		Palette p = b.palette;
		Pieces.room(b, 1, 1, 4, 4, 0, 2, "tiny house");
		b.fill(0, 3, 0, 5, 3, 5, p.slab());
		Pieces.door(b, 2, 1, 1, Direction.NORTH);
		Pieces.window(b, 4, 2, 2);
		b.chest(3, 1, 3, "chests/tiny_house");
		b.set(2, 1, 3, Blocks.POTTED_POPPY);
		Pieces.fenceRing(b, 0, 0, 6, 6, 1, 2, 0);
		// A second gate into the garden (the first one leads straight to the door).
		b.set(5, 1, 0, p.wood().gate());
		b.set(5, 1, 5, Blocks.DANDELION);
		b.set(5, 1, 1, Blocks.POPPY);
		b.set(1, 1, 5, Blocks.CORNFLOWER);
		b.set(5, 1, 3, p.fence());
		b.barrel(5, 2, 3, "chests/barrel_supplies");
	}

	/**
	 * The Upside-Down House: a house standing on the tip of its own roof, its garden on top. Inside, the furniture
	 * hangs from the ceiling. A ladder up the end wall to the door.
	 */
	static void upsideDownHouse(Blueprint b) {
		Palette p = b.palette;
		// The roof, upside down, from the eaves at y 4 to the tip on the ground.
		for (int i = 0; i <= 4; i++) {
			int front = i;
			int back = 8 - i;
			int y = 4 - i;
			if (front == back) {
				b.fill(0, y, front, 10, y, front, p.planks());
				break;
			}
			b.fill(0, y, front, 10, y, front, Pieces.upsideDown(p.stairs(), Direction.SOUTH));
			b.fill(0, y, back, 10, y, back, Pieces.upsideDown(p.stairs(), Direction.NORTH));
			for (int z = front + 1; i > 0 && z <= back - 1; z++) {
				b.set(1, y, z, p.planks());
				b.set(9, y, z, p.planks());
			}
		}
		// The house on top of it, and the garden on top of the house.
		Pieces.room(b, 1, 1, 9, 7, 5, 3, "upside-down house");
		b.fill(1, 9, 1, 9, 9, 7, p.planks());
		b.fill(1, 10, 1, 9, 10, 7, Blocks.GRASS_BLOCK);
		for (int x = 2; x <= 8; x += 2) {
			b.set(x, 11, 3, LilyBlueprints.FLOWERS[x % LilyBlueprints.FLOWERS.length]);
			b.set(x, 11, 5, LilyBlueprints.FLOWERS[(x + 3) % LilyBlueprints.FLOWERS.length]);
		}
		Pieces.windowsAround(b, 1, 1, 9, 7, 7, -1);
		Pieces.door(b, 1, 6, 4, Direction.WEST);
		Pieces.ladder(b, 0, 1, 5, 4, Direction.WEST);
		// Furniture on the ceiling: a table and chairs, a crafting table, bookshelves, hanging lanterns.
		b.set(5, 8, 4, Pieces.topSlab(p.slab()));
		b.set(4, 8, 4, Pieces.upsideDown(p.stairs(), Direction.WEST));
		b.set(6, 8, 4, Pieces.upsideDown(p.stairs(), Direction.EAST));
		b.set(8, 8, 6, Blocks.CRAFTING_TABLE);
		b.set(2, 8, 2, Blocks.BOOKSHELF);
		b.set(8, 8, 2, Blocks.BOOKSHELF);
		Pieces.lantern(b, 3, 8, 6, true);
		Pieces.lantern(b, 7, 8, 3, true);
		b.chest(2, 6, 6, "chests/upside_down_house");
	}

	/** Portal to the Bread Dimension: a hay-bale "portal" glowing orange, candles on top, wheat all around. */
	static void breadPortal(Blueprint b) {
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				if (b.random.nextInt(3) == 0) {
					b.set(x, 0, z, Blocks.COARSE_DIRT);
				}
			}
		}
		b.fill(2, 0, 2, 6, 6, 2, Blocks.HAY_BLOCK);
		b.fill(3, 1, 2, 5, 5, 2, Blocks.ORANGE_STAINED_GLASS);
		BlockState candle = Blocks.YELLOW_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 2).setValue(CandleBlock.LIT, true);
		b.set(2, 7, 2, candle);
		b.set(6, 7, 2, candle);
		b.set(4, 7, 2, Blocks.LIGHTNING_ROD);
		b.chest(7, 1, 3, "chests/bread_portal");
		b.set(1, 1, 3, Blocks.HAY_BLOCK);
		b.set(1, 2, 3, Blocks.HAY_BLOCK);
	}

	/** The Developer's Desk: a little study with a huge monitor, keyboard and mouse, a whiteboard and a messy bin. */
	static void developersDesk(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 1, 1, 7, 7, 0, 3, Pieces.Roof.GABLE_X, "developer's desk");
		for (int x = 2; x <= 5; x++) {
			b.set(x, 1, 5, Pieces.topSlab(p.slab()));
			b.set(x, 1, 6, Pieces.topSlab(p.slab()));
		}
		b.fill(3, 2, 6, 4, 3, 6, Blocks.BLACK_CONCRETE);
		b.set(3, 3, 6, Blocks.LIGHT_BLUE_CONCRETE);
		b.set(3, 2, 5, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
		b.set(4, 2, 5, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
		b.set(5, 2, 5, Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACE, AttachFace.FLOOR));
		b.set(2, 2, 5, Blocks.DECORATED_POT);
		b.set(4, 1, 4, Pieces.stairs(p.stairs(), Direction.NORTH));
		// Whiteboard on the west wall, with "diagrams".
		b.fill(1, 2, 3, 1, 3, 5, Blocks.WHITE_CONCRETE);
		b.set(1, 3, 3, Blocks.BLACK_CONCRETE);
		b.set(1, 2, 4, Blocks.BLACK_CONCRETE);
		b.set(1, 3, 5, Blocks.RED_CONCRETE);
		b.set(6, 1, 6, Blocks.COMPOSTER);
		b.chest(6, 1, 3, "chests/developers_desk");
		Pieces.path(b, door, 0, door, 0, 0);
	}
}
