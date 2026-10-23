package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;

/** FelixSyn's own homes, where wild FelixSyns live (Harley sometimes), remade on the blueprint system. */
final class FelixBlueprints {
	private FelixBlueprints() {
	}

	private static BlockPos at(int x, int y, int z) {
		return new BlockPos(x, y, z);
	}

	/**
	 * Mace racks: maces on display built from blocks (a fence handle under a heavy-looking head), so they look the
	 * part without handing out free maces (real ones are super rare).
	 */
	private static void maceRack(Blueprint b, int x, int z) {
		b.set(x, 1, z, Blocks.DARK_OAK_FENCE);
		b.set(x, 2, z, Blocks.POLISHED_DEEPSLATE_WALL);
	}

	/** Mace Workshop: a stone-and-wood workshop with an anvil, smithing table, grindstone, blast furnace and mace racks. */
	static void maceWorkshop(Blueprint b) {
		Palette p = b.palette;
		b.fill(1, 0, 1, 11, 0, 9, p.stoneFloor());
		b.walls(1, 1, 11, 9, 1, 1, p.bricks());
		b.walls(1, 1, 11, 9, 2, 4, p.planks());
		b.clear(2, 1, 2, 10, 4, 8);
		for (int[] c : new int[][]{{1, 1}, {11, 1}, {1, 9}, {11, 9}, {6, 9}}) {
			b.fill(c[0], 1, c[1], c[0], 4, c[1], p.log());
		}
		b.floor(2, 2, 10, 8, 1, "workshop");
		Pieces.windowsAround(b, 1, 1, 11, 9, 2, 6);
		Pieces.roof(b, Pieces.Roof.GABLE_X, 1, 1, 11, 9, 4);
		Pieces.door(b, 6, 1, 1, Direction.NORTH);
		Pieces.path(b, 5, 0, 7, 0, 0);
		b.set(3, 1, 8, Blocks.ANVIL);
		b.set(4, 1, 8, Blocks.SMITHING_TABLE);
		b.set(5, 1, 8, Blocks.GRINDSTONE);
		b.set(7, 1, 8, Blocks.BLAST_FURNACE);
		b.chest(8, 1, 8, "chests/mace_workshop");
		b.barrel(9, 1, 8, "chests/barrel_supplies");
		for (int z : new int[]{3, 5, 7}) {
			maceRack(b, 2, z);
		}
		b.mob(9, 1, 3, "dummy");
		b.bed(9, 1, 5, Blocks.BLACK_BED);
		Pieces.lantern(b, 10, 1, 2, false);
		Pieces.lantern(b, 3, 1, 6, false);
		b.resident(CharacterVariant.WILD_FELIX, 6, 1, 4, 6, 5, 12, at(9, 1, 5), null, false);
	}

	/**
	 * Cliff Hut: a little hip-roofed hut and a diving board for mace drops: a ladder up a log post to a railed
	 * board, with hay bales underneath its end to land on.
	 */
	static void cliffHut(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 1, 1, 6, 6, 0, 3, Pieces.Roof.HIP, "cliff hut");
		Pieces.path(b, door, 0, door, 0, 0);
		b.bed(2, 1, 4, Blocks.BLACK_BED);
		b.chest(4, 1, 5, "chests/cliff_hut");
		// The diving board.
		b.fill(8, 1, 5, 8, 5, 5, p.log());
		Pieces.ladder(b, 7, 1, 6, 5, Direction.WEST);
		b.fill(8, 6, 5, 9, 6, 5, p.planks());
		b.fill(8, 7, 4, 9, 7, 4, p.fence());
		b.fill(8, 7, 6, 9, 7, 6, p.fence());
		b.floor(8, 5, 9, 5, 7, "diving board");
		b.fill(10, 0, 4, 10, 0, 6, Blocks.HAY_BLOCK);
		b.set(9, 1, 1, Blocks.TARGET);
		b.resident(CharacterVariant.WILD_FELIX, door, 1, 0, 4, 4, 10, at(2, 1, 4), null, false);
	}

	/**
	 * Wind Tower: a tall, thin stone tower he jumps off to train. A ladder runs all the way up the inside, past
	 * windows and torches, to a wide fenced lookout with a gap to jump from; hay bales at the bottom to land on.
	 */
	static void windTower(Blueprint b) {
		Palette p = b.palette;
		int top = 22;
		// The lookout floor first (wider than the tower); the tower fills its middle and the ladder.
		b.fill(1, top, 1, 7, top, 7, p.planks());
		Pieces.tower(b, 2, 2, 5, 0, top, top, true);
		Pieces.door(b, 4, 1, 2, Direction.NORTH);
		for (int y = 6; y < top; y += 5) {
			Pieces.window(b, 2, y, 4);
			Pieces.window(b, 6, y, 4);
			b.set(3, y, 3, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
		}
		b.walls(1, 1, 7, 7, top + 1, top + 1, p.fence());
		b.clear(4, top + 1, 1, 4, top + 1, 1);
		for (int[] c : new int[][]{{1, 1}, {7, 1}, {1, 7}, {7, 7}}) {
			Pieces.lantern(b, c[0], top + 2, c[1], false);
		}
		b.chest(2, top + 1, 6, "chests/wind_tower");
		b.floor(1, 1, 7, 7, top + 1, "lookout");
		// Landing pad in front.
		b.fill(0, 0, 0, 8, 0, 1, Blocks.HAY_BLOCK);
		b.bed(3, 1, 3, Blocks.BLACK_BED);
		b.barrel(5, 1, 3, "chests/barrel_supplies");
		b.resident(CharacterVariant.WILD_FELIX, 4, 1, 0, 4, 4, 12, at(3, 1, 3), null, false);
	}

	/**
	 * Rival Camp: set up within sight of an ItzBreadLoaf house or tent, so the two of them fight all the time. A
	 * black tent, a campfire, targets, a training dummy and a red-vs-blue banner "scoreboard".
	 */
	static void rivalCamp(Blueprint b) {
		b.fill(1, 0, 1, 9, 0, 7, Blocks.COARSE_DIRT);
		Pieces.tent(b, 1, 2, 5, Blocks.BLACK_WOOL.defaultBlockState());
		b.bed(2, 1, 3, Blocks.BLACK_BED);
		b.chest(4, 1, 5, "chests/rival_camp");
		Pieces.campfire(b, 8, 1, 5);
		b.set(10, 1, 1, Blocks.TARGET);
		b.set(10, 1, 8, Blocks.TARGET);
		b.mob(9, 1, 7, "dummy");
		b.set(6, 1, 1, Blocks.RED_BANNER);
		b.set(8, 1, 1, Blocks.BLUE_BANNER);
		b.resident(CharacterVariant.WILD_FELIX, 7, 1, 2, 6, 4, 14, at(2, 1, 3), null, false);
	}
}
