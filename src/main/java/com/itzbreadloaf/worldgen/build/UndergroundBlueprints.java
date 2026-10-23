package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;

/**
 * Update 2's underground structures (part 8, batch 7): a little something at the surface (a stairway mouth, a
 * headframe, a hidden hatch) and rooms carved deep under it. Stairways and shafts are lined with stone so caves,
 * gravel and water stay out.
 */
final class UndergroundBlueprints {
	private UndergroundBlueprints() {
	}

	private static BlockPos at(int x, int y, int z) {
		return new BlockPos(x, y, z);
	}

	/**
	 * A stone-lined stairway from a room floor at yFloor (negative) up to the surface. The first step is at
	 * (x, yFloor + 1, z), each next one a block up and a block toward {@code up}; the last is at ground level (y 0).
	 * Underground, the steps get stone walls on both sides and a stone ceiling.
	 */
	static void stairway(Blueprint b, int x, int z, int yFloor, Direction up) {
		BlockState wall = b.palette.bricks();
		int steps = -yFloor;
		int px = up.getStepZ() != 0 ? 1 : 0;
		int pz = up.getStepX() != 0 ? 1 : 0;
		for (int i = 0; i < steps; i++) {
			int sx = x + up.getStepX() * i;
			int sz = z + up.getStepZ() * i;
			int y = yFloor + 1 + i;
			for (int dy = -1; dy <= 4; dy++) {
				int wy = y + dy;
				if (wy > 0) {
					continue;
				}
				for (int side : new int[]{-1, 1}) {
					if (!b.isSet(sx + px * side, wy, sz + pz * side)) {
						b.set(sx + px * side, wy, sz + pz * side, wall);
					}
				}
			}
			if (!b.isSet(sx, y - 1, sz)) {
				b.set(sx, y - 1, sz, wall);
			}
			b.set(sx, y, sz, Pieces.stairs(b.palette.stoneStairs(), up));
			b.clear(sx, y + 1, sz, sx, y + 3, sz);
			if (y + 4 <= 0) {
				b.set(sx, y + 4, sz, wall);
			}
		}
	}

	/**
	 * A stone-lined ladder shaft at (x, z) from yBottom up to y top (inclusive), the ladder facing {@code facing}
	 * (hanging on the block behind it). With {@code hatch}, a trapdoor flush with the ground closes the top.
	 */
	static void ladderShaft(Blueprint b, int x, int z, int yBottom, int top, Direction facing, boolean hatch) {
		BlockState wall = b.palette.bricks();
		for (int y = yBottom; y <= top; y++) {
			for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
				if (!b.isSet(x + d[0], y, z + d[1])) {
					b.set(x + d[0], y, z + d[1], wall);
				}
			}
		}
		Pieces.ladder(b, x, yBottom, top, z, facing);
		if (hatch) {
			b.set(x, 0, z, b.palette.wood().trapdoor().defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP));
		}
	}

	/** A stone room with a stone ceiling: floor at yFloor, {@code height} blocks of air inside. */
	static void vault(Blueprint b, int x0, int z0, int x1, int z1, int yFloor, int height, String name) {
		Pieces.stoneRoom(b, x0, z0, x1, z1, yFloor, height, name);
		b.fill(x0, yFloor + height + 1, z0, x1, yFloor + height + 1, z1, b.palette.bricks());
	}

	/** A log door frame around the mouth of a stairway at (x, 0, z), facing along x. */
	private static void doorFrame(Blueprint b, int x, int z) {
		BlockState log = b.palette.log();
		b.fill(x - 1, 1, z, x - 1, 2, z, log);
		b.fill(x + 1, 1, z, x + 1, 2, z, log);
		b.fill(x - 1, 3, z, x + 1, 3, z, Pieces.logAlong(log, Direction.Axis.X));
	}

	// ================================================================ the structures

	/** ItzBreadLoaf's Bread Cellar: a stairway down to a vaulted cellar of bread barrels, hay and a long table. */
	static void breadCellar(Blueprint b) {
		Palette p = b.palette;
		int floor = -6;
		vault(b, 1, 6, 9, 12, floor, 4, "bread cellar");
		b.clear(5, floor + 1, 6, 5, floor + 3, 6);
		stairway(b, 5, 5, floor, Direction.NORTH);
		doorFrame(b, 5, 0);
		int y = floor + 1;
		for (int z = 7; z <= 11; z++) {
			b.set(2, y, z, Blocks.BARREL);
			b.set(2, y + 1, z, Blocks.BARREL);
			b.set(8, y, z, Blocks.BARREL);
		}
		b.barrel(2, y, 9, "chests/bread_cellar");
		b.barrel(8, y, 9, "chests/bread_cellar");
		b.set(8, y + 1, 11, Blocks.HAY_BLOCK);
		b.set(8, y + 1, 10, Blocks.HAY_BLOCK);
		for (int z = 8; z <= 10; z++) {
			b.set(5, y, z, Pieces.topSlab(p.slab()));
		}
		b.set(5, y + 1, 9, Blocks.CAKE);
		b.chest(4, y, 11, "chests/bread_cellar");
		Pieces.lantern(b, 4, floor + 4, 8, true);
		Pieces.lantern(b, 6, floor + 4, 10, true);
		b.set(7, 1, 2, p.fence());
		Pieces.lantern(b, 7, 2, 2, false);
		b.resident(CharacterVariant.BREADLOAF, 6, y, 8, 5, 9, 10);
	}

	/** Miner's Shaft: a log headframe over a ladder shaft 14 deep, a miner's camp at the bottom with rails and ore in the walls. */
	static void minersShaft(Blueprint b) {
		Palette p = b.palette;
		int floor = -14;
		vault(b, 2, 2, 9, 8, floor, 3, "mine camp");
		ladderShaft(b, 5, 3, floor + 1, 0, Direction.SOUTH, false);
		// Headframe.
		for (int[] c : new int[][]{{3, 2}, {7, 2}, {3, 4}, {7, 4}}) {
			b.fill(c[0], 1, c[1], c[0], 5, c[1], p.log());
		}
		b.fill(3, 6, 3, 7, 6, 3, Pieces.logAlong(p.log(), Direction.Axis.X));
		b.set(5, 5, 3, p.fence());
		b.set(5, 4, 3, p.fence());
		int y = floor + 1;
		b.bed(3, y, 5, p.bed());
		b.chest(3, y, 7, "chests/miners_shaft");
		b.set(7, y, 3, Blocks.FURNACE);
		b.set(8, y, 3, Blocks.CRAFTING_TABLE);
		b.fill(5, y, 7, 8, y, 7, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST));
		for (int[] o : new int[][]{{9, y + 1, 4}, {9, y, 6}, {2, y + 1, 6}, {6, y + 2, 8}}) {
			b.set(o[0], o[1], o[2], Blocks.IRON_ORE);
		}
		b.set(9, y + 1, 7, Blocks.COAL_ORE);
		b.set(4, y + 1, 8, Blocks.COPPER_ORE);
		Pieces.lantern(b, 6, floor + 3, 5, true);
		b.resident(CharacterVariant.MINER, 6, y, 5, 6, 5, 8, at(3, y, 5), null, false);
	}

	/** Witchy Lily's Secret Lab: a hatch hidden in a bush, a ladder down to a lab of brewing stands and an enchanting table. */
	static void secretLab(Blueprint b) {
		Palette p = b.palette;
		int floor = -9;
		vault(b, 1, 3, 9, 9, floor, 3, "secret lab");
		ladderShaft(b, 5, 4, floor + 1, -1, Direction.SOUTH, true);
		BlockState leaves = LilyBlueprints.leaves(p.wood().leaves());
		for (int x = 4; x <= 6; x++) {
			for (int z = 3; z <= 5; z++) {
				if (!(x == 5 && (z == 4 || z == 3))) {
					b.set(x, 1, z, leaves);
				}
			}
		}
		int y = floor + 1;
		b.fill(2, y, 5, 2, y + 1, 8, Blocks.BOOKSHELF);
		b.set(4, y, 8, Blocks.BREWING_STAND);
		b.set(6, y, 8, Blocks.BREWING_STAND);
		b.set(3, y, 8, Blocks.WATER_CAULDRON);
		b.set(7, y, 8, Blocks.WATER_CAULDRON);
		b.set(5, y, 7, Blocks.ENCHANTING_TABLE);
		b.chest(8, y, 5, "chests/secret_lab");
		b.set(8, y, 6, Blocks.POTTED_RED_MUSHROOM);
		b.set(5, y, 8, Blocks.PURPLE_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 4).setValue(CandleBlock.LIT, true));
		Pieces.lantern(b, 3, floor + 3, 6, true);
		Pieces.lantern(b, 7, floor + 3, 6, true);
		b.resident(CharacterVariant.WITCHY, 6, y, 5, 5, 6, 8);
	}

	/** FelixSyn's Secret Bunker: a hatch by a boulder, a ladder down to a training bunker with dummies, targets and mace racks. */
	static void felixBunker(Blueprint b) {
		Palette p = b.palette;
		int floor = -8;
		vault(b, 1, 1, 11, 9, floor, 4, "bunker");
		ladderShaft(b, 6, 2, floor + 1, -1, Direction.SOUTH, true);
		b.fill(8, 1, 1, 9, 1, 2, Blocks.MOSSY_COBBLESTONE);
		b.set(8, 2, 1, Blocks.MOSSY_COBBLESTONE);
		int y = floor + 1;
		b.mob(4, y, 5, "dummy");
		b.mob(8, y, 5, "dummy");
		b.set(11, y + 1, 4, Blocks.TARGET);
		b.set(11, y + 1, 6, Blocks.TARGET);
		for (int z = 4; z <= 6; z++) {
			b.set(2, y, z, Blocks.DARK_OAK_FENCE);
			b.set(2, y + 1, z, Blocks.POLISHED_DEEPSLATE_WALL);
		}
		b.bed(9, y, 7, Blocks.BLACK_BED);
		b.chest(3, y, 8, "chests/felix_bunker");
		b.barrel(4, y, 8, "chests/barrel_supplies");
		Pieces.lantern(b, 6, floor + 4, 5, true);
		Pieces.lantern(b, 4, floor + 4, 3, true);
		b.resident(CharacterVariant.WILD_FELIX, 6, y, 4, 6, 5, 8, at(9, y, 7), null, false);
	}

	/** Glowing Mushroom Grotto: a stairway down into a cave with a mycelium floor, giant mushrooms, shroomlights and a pool. */
	static void mushroomGrotto(Blueprint b) {
		Palette p = b.palette;
		int floor = -7;
		// The cave: rough stone walls and ceiling, mycelium floor.
		Block[] rock = {Blocks.STONE, Blocks.ANDESITE, Blocks.MOSSY_COBBLESTONE, Blocks.TUFF};
		for (int x = 2; x <= 12; x++) {
			for (int z = 8; z <= 17; z++) {
				boolean edge = x == 2 || x == 12 || z == 8 || z == 17;
				for (int y = floor; y <= floor + 6; y++) {
					if (edge || y == floor + 6) {
						b.set(x, y, z, rock[b.random.nextInt(rock.length)]);
					} else if (y == floor) {
						b.set(x, y, z, Blocks.MYCELIUM);
					} else {
						b.set(x, y, z, Blocks.AIR);
					}
				}
				if (!edge && b.random.nextInt(5) == 0) {
					b.set(x, floor + 5, z, rock[b.random.nextInt(rock.length)]);
				}
			}
		}
		b.clear(7, floor + 1, 8, 7, floor + 3, 8);
		stairway(b, 7, 7, floor, Direction.NORTH);
		doorFrame(b, 7, 1);
		b.floor(5, 10, 9, 12, floor + 1, "grotto");
		// Giant mushrooms.
		int y = floor + 1;
		b.fill(4, y, 14, 4, y + 2, 14, Blocks.MUSHROOM_STEM);
		b.fill(3, y + 3, 13, 5, y + 3, 15, Blocks.RED_MUSHROOM_BLOCK);
		b.set(4, y + 3, 14, Blocks.SHROOMLIGHT);
		b.fill(10, y, 11, 10, y + 1, 11, Blocks.MUSHROOM_STEM);
		b.fill(9, y + 2, 10, 11, y + 2, 12, Blocks.BROWN_MUSHROOM_BLOCK);
		b.set(10, y + 2, 11, Blocks.SHROOMLIGHT);
		for (int[] m : new int[][]{{6, 15}, {8, 14}, {3, 10}, {11, 15}}) {
			b.set(m[0], y, m[1], b.random.nextBoolean() ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM);
		}
		b.fill(7, floor - 1, 15, 9, floor - 1, 16, Blocks.STONE);
		b.fill(7, floor, 15, 9, floor, 16, Blocks.WATER);
		b.set(7, floor + 5, 12, Blocks.SHROOMLIGHT);
		b.chest(11, y, 16, "chests/mushroom_grotto");
		b.resident(CharacterVariant.GARDENER, 7, y, 11, 7, 12, 8);
	}

	/** The Deep Forge: a stone hut over a ladder shaft 16 deep, a forge hall with anvils, blast furnaces and a lava trough behind bars. */
	static void deepForge(Blueprint b) {
		Palette p = b.palette;
		int floor = -16;
		vault(b, 1, 3, 11, 11, floor, 5, "forge hall");
		ladderShaft(b, 6, 4, floor + 1, 0, Direction.SOUTH, false);
		Pieces.stoneRoom(b, 4, 2, 8, 6, 0, 3, "forge entrance");
		Pieces.roof(b, Pieces.Roof.FLAT, 4, 2, 8, 6, 3);
		Pieces.door(b, 6, 1, 2, Direction.NORTH);
		b.set(6, 0, 4, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.SOUTH));
		int y = floor + 1;
		// Lava trough along the back wall, behind iron bars.
		b.fill(3, floor, 10, 9, floor, 10, p.bricks());
		b.fill(3, y, 10, 9, y, 10, Blocks.LAVA);
		b.fill(3, y, 9, 9, y + 1, 9, Blocks.IRON_BARS);
		b.set(2, y, 10, p.bricks());
		b.set(10, y, 10, p.bricks());
		b.set(3, y, 4, Blocks.ANVIL);
		b.set(9, y, 4, Blocks.ANVIL);
		b.set(2, y, 6, Blocks.BLAST_FURNACE);
		b.set(2, y, 7, Blocks.BLAST_FURNACE);
		b.set(10, y, 6, Blocks.SMITHING_TABLE);
		b.set(10, y, 7, Blocks.GRINDSTONE);
		b.chest(10, y, 8, "chests/deep_forge");
		b.barrel(2, y, 8, "chests/barrel_supplies");
		b.bed(4, y, 6, p.bed());
		for (int x : new int[]{4, 8}) {
			b.fill(x, floor + 4, 7, x, floor + 5, 7, Blocks.IRON_BARS);
			Pieces.lantern(b, x, floor + 3, 7, true);
		}
		b.floor(5, 5, 9, 8, y, "forge hall");
		b.resident(CharacterVariant.MINER, 6, y, 6, 6, 7, 8, at(4, y, 6), null, false);
	}

	/** The Buried Library: a stairway down to a hidden hall of bookshelves, lecterns and a candle-lit reading table. */
	static void buriedLibrary(Blueprint b) {
		Palette p = b.palette;
		int floor = -7;
		vault(b, 1, 7, 11, 17, floor, 4, "buried library");
		b.clear(6, floor + 1, 7, 6, floor + 3, 7);
		stairway(b, 6, 6, floor, Direction.NORTH);
		doorFrame(b, 6, 0);
		int y = floor + 1;
		b.fill(2, y, 8, 2, y + 1, 16, Blocks.BOOKSHELF);
		b.fill(10, y, 8, 10, y + 1, 16, Blocks.BOOKSHELF);
		b.fill(3, y, 16, 9, y + 1, 16, Blocks.BOOKSHELF);
		for (int x : new int[]{4, 8}) {
			b.fill(x, y, 10, x, y + 1, 13, Blocks.BOOKSHELF);
		}
		b.set(6, y, 15, Blocks.LECTERN);
		b.set(3, y, 9, Blocks.LECTERN);
		for (int z = 11; z <= 12; z++) {
			b.set(6, y, z, Pieces.topSlab(p.slab()));
		}
		b.set(6, y + 1, 11, Blocks.WHITE_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
		b.set(5, y, 11, Pieces.stairs(p.stairs(), Direction.WEST));
		b.set(7, y, 12, Pieces.stairs(p.stairs(), Direction.EAST));
		b.chest(9, y, 15, "chests/buried_library");
		for (int z : new int[]{9, 14}) {
			Pieces.lantern(b, 6, floor + 4, z, true);
		}
		b.resident(CharacterVariant.BOOKWORM, 6, y, 9, 6, 12, 8);
	}

	/** Madison's Root Cellar: a log doorway at the top of a short stairway, a cool cellar of crop barrels, roots and candles. */
	static void rootCellar(Blueprint b) {
		Palette p = b.palette;
		int floor = -5;
		vault(b, 1, 5, 9, 10, floor, 3, "root cellar");
		b.clear(5, floor + 1, 5, 5, floor + 3, 5);
		stairway(b, 5, 4, floor, Direction.NORTH);
		doorFrame(b, 5, 0);
		int y = floor + 1;
		b.barrel(2, y, 9, "chests/root_cellar");
		b.barrel(8, y, 9, "chests/root_cellar");
		for (int x : new int[]{3, 7}) {
			b.set(x, y, 9, Blocks.BARREL);
			b.set(x, y + 1, 9, Blocks.BARREL);
		}
		b.set(2, y, 6, Blocks.PUMPKIN);
		b.set(8, y, 6, Blocks.HAY_BLOCK);
		b.set(2, floor + 3, 9, Blocks.COBWEB);
		b.set(8, floor + 3, 6, Blocks.COBWEB);
		for (int x = 3; x <= 7; x += 2) {
			b.set(x, floor + 3, 7, Blocks.HANGING_ROOTS);
		}
		b.set(5, y, 9, Blocks.YELLOW_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 2).setValue(CandleBlock.LIT, true));
		Pieces.lantern(b, 5, floor + 3, 8, true);
		b.resident(CharacterVariant.FARMER, 5, y, 7, 5, 8, 8);
	}
}
