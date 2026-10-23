package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Madison's structures (her house, bakery, farms, market and mill), remade on the blueprint system. */
final class MadisonBlueprints {
	private MadisonBlueprints() {
	}

	private static BlockPos at(int x, int y, int z) {
		return new BlockPos(x, y, z);
	}

	private static CharacterVariant pick(Blueprint b, CharacterVariant... variants) {
		return variants[b.random.nextInt(variants.length)];
	}

	/** Madison's House: a white-walled cottage with a kitchen and pantry, a fenced carrot garden and beehives. */
	static void madisonHouse(Blueprint b) {
		Palette p = b.palette;
		BlockState white = Blocks.WHITE_CONCRETE.defaultBlockState();
		Pieces.garden(b, 0, 0, 12, 0, 0);
		Pieces.garden(b, 11, 1, 12, 8, 0);
		int door = Pieces.cottage(b, 2, 1, 10, 7, 0, 3, Pieces.Roof.GABLE_X, "madison's house");
		for (int y = 1; y <= 3; y++) {
			for (int x = 2; x <= 10; x++) {
				for (int z = 1; z <= 7; z++) {
					if ((x == 2 || x == 10 || z == 1 || z == 7) && b.get(x, y, z) == p.planks()) {
						b.set(x, y, z, white);
					}
				}
			}
		}
		Pieces.path(b, door, 0, door, 0, 0);
		b.clear(door, 1, 0, door, 1, 0);
		b.bed(3, 1, 5, p.bed());
		b.chest(4, 1, 6, "chests/madison_pantry");
		Pieces.kitchen(b, 7, 1, 6, "chests/barrel_supplies");
		Pieces.table(b, 5, 1, 3, true, false);
		// Carrot garden behind the house, fenced, the gate facing the back door path.
		Pieces.fenceRing(b, 1, 9, 11, 14, 1, door, 9);
		Pieces.field(b, 2, 10, 10, 13, 0, Blocks.CARROTS);
		b.beehive(12, 1, 2);
		b.beehive(12, 1, 5);
		b.resident(pick(b, CharacterVariant.MADISON, CharacterVariant.FARMER, CharacterVariant.BEEKEEPER), door, 1, 8, door, 5, 12,
				at(3, 1, 5), at(4, 1, 6), false);
	}

	/** Madison's Bakery: a shop counter with a cake, smokers and a brick oven with a chimney, the baker's bed. */
	static void bakery(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 1, 1, 11, 9, 0, 4, Pieces.Roof.GABLE_X, "bakery");
		Pieces.path(b, door, 0, door, 0, 0);
		// Counter across the shop, open at the east end into the kitchen.
		for (int x = 2; x <= 8; x++) {
			b.set(x, 1, 4, Pieces.topSlab(p.slab()));
		}
		b.set(3, 2, 4, Blocks.CAKE);
		Pieces.table(b, 4, 1, 2, true, false);
		// Kitchen: smokers, a furnace, a brick oven with a chimney through the roof (smoke from the top).
		b.set(2, 1, 8, Blocks.SMOKER);
		b.set(3, 1, 8, Blocks.SMOKER);
		b.set(4, 1, 8, Blocks.FURNACE);
		b.fill(7, 1, 8, 9, 2, 8, Blocks.BRICKS);
		b.set(8, 1, 8, Blocks.FURNACE);
		b.fill(8, 1, 9, 8, 8, 9, Blocks.BRICKS);
		b.set(8, 9, 9, Blocks.CAMPFIRE);
		b.set(10, 1, 8, Blocks.HAY_BLOCK);
		b.set(10, 1, 7, Blocks.HAY_BLOCK);
		b.set(2, 1, 5, Blocks.CRAFTING_TABLE);
		b.chest(2, 1, 6, "chests/bakery");
		Pieces.lantern(b, 6, 1, 7, false);
		b.bed(10, 1, 5, p.bed());
		b.resident(CharacterVariant.BAKER, door + 1, 1, 0, door, 5, 10, at(10, 1, 5), null, false);
	}

	/**
	 * Farmstead: a big barn (hay, the pantry and the farmer's bed), a fenced pen with cows, sheep, a pig and
	 * chickens, wheat and potato fields behind, paths between everything.
	 */
	static void farmstead(Blueprint b) {
		Palette p = b.palette;
		Pieces.room(b, 1, 1, 11, 9, 0, 5, "barn");
		Pieces.windowsAround(b, 1, 1, 11, 9, 3, 6);
		b.clear(5, 1, 1, 7, 3, 1);
		Pieces.roof(b, Pieces.Roof.GABLE_X, 1, 1, 11, 9, 5);
		b.fill(2, 1, 7, 3, 2, 8, Blocks.HAY_BLOCK);
		b.fill(2, 1, 2, 2, 1, 4, Blocks.HAY_BLOCK);
		b.bed(10, 1, 7, p.bed());
		b.chest(9, 1, 8, "chests/madison_pantry");
		b.barrel(8, 1, 8, "chests/barrel_supplies");
		b.set(10, 1, 2, Blocks.COMPOSTER);
		Pieces.lantern(b, 4, 1, 8, false);
		Pieces.lantern(b, 10, 1, 4, false);
		// The animal pen.
		Pieces.fenceRing(b, 13, 1, 23, 9, 1, 18, 1);
		b.mob(15, 1, 3, "cow");
		b.mob(17, 1, 6, "cow");
		b.mob(20, 1, 3, "sheep");
		b.mob(21, 1, 7, "sheep");
		b.mob(15, 1, 7, "pig");
		b.mob(19, 1, 5, "chicken");
		b.mob(22, 1, 4, "chicken");
		b.set(22, 0, 8, Blocks.WATER);
		b.set(14, 1, 8, Blocks.HAY_BLOCK);
		b.floor(14, 2, 22, 8, 1, "pen");
		// Fields and paths.
		Pieces.field(b, 1, 12, 11, 18, 0, Blocks.WHEAT);
		Pieces.field(b, 13, 12, 23, 18, 0, Blocks.POTATOES);
		Pieces.path(b, 12, 0, 12, 19, 0);
		Pieces.path(b, 0, 10, 24, 10, 0);
		b.resident(CharacterVariant.FARMER, 12, 1, 0, 12, 10, 16, at(10, 1, 7), at(9, 1, 8), false);
	}

	/** A blossoming fruit tree: a short trunk under a round crown of leaves and flowering azalea leaves. */
	private static void blossomTree(Blueprint b, int x, int z) {
		b.fill(x, 1, z, x, 4, z, b.palette.log());
		BlockState leaves = LilyBlueprints.leaves(b.palette.wood().leaves());
		BlockState blossom = LilyBlueprints.leaves(Blocks.FLOWERING_AZALEA_LEAVES);
		for (int y = 3; y <= 5; y++) {
			int r = y == 5 ? 1 : 2;
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.abs(dx) == 2 && Math.abs(dz) == 2 || b.isSet(x + dx, y, z + dz)) {
						continue;
					}
					b.set(x + dx, y, z + dz, b.random.nextInt(9) < 4 ? blossom : leaves);
				}
			}
		}
	}

	/** Orchard: rows of blossoming trees in a fenced meadow, hives over campfires, the beekeeper's honey shed. */
	static void orchard(Blueprint b) {
		Pieces.garden(b, 0, 0, 18, 18, 0);
		Pieces.fenceRing(b, 0, 0, 18, 18, 1, 9, 0);
		for (int tx : new int[]{3, 9, 15}) {
			for (int tz : new int[]{3, 9, 15}) {
				if (tx == 9 && tz != 15) {
					continue;
				}
				blossomTree(b, tx, tz);
			}
		}
		int door = Pieces.cottage(b, 7, 7, 11, 11, 0, 3, Pieces.Roof.HIP, "honey shed");
		b.bed(8, 1, 9, b.palette.bed());
		b.chest(10, 1, 10, "chests/orchard");
		b.barrel(10, 1, 9, "chests/barrel_supplies");
		for (int[] c : new int[][]{{6, 6}, {12, 6}, {6, 12}, {12, 12}}) {
			b.beehive(c[0], 1, c[1]);
		}
		Pieces.path(b, door, 0, door, 6, 0);
		b.clear(door, 1, 1, door, 1, 6);
		b.resident(CharacterVariant.BEEKEEPER, door, 1, 5, 9, 9, 12, at(8, 1, 9), null, false);
	}

	/** Market Stall: a striped awning on posts over a counter of produce, the merchant's chest, benches out front. */
	static void marketStall(Blueprint b) {
		Palette p = b.palette;
		b.fill(2, 0, 2, 8, 0, 6, p.planks());
		for (int[] c : new int[][]{{2, 2}, {8, 2}, {2, 6}, {8, 6}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.fence());
		}
		for (int x = 1; x <= 9; x++) {
			for (int z = 1; z <= 7; z++) {
				b.set(x, 4, z, x % 2 == 0 ? Blocks.RED_WOOL : Blocks.WHITE_WOOL);
			}
		}
		b.set(3, 1, 3, Blocks.PUMPKIN);
		b.set(4, 1, 3, Blocks.MELON);
		b.set(5, 1, 3, Pieces.topSlab(p.slab()));
		b.set(5, 2, 3, Blocks.CAKE);
		b.set(6, 1, 3, Blocks.HAY_BLOCK);
		b.barrel(7, 1, 3, "chests/barrel_supplies");
		b.chest(4, 1, 6, "chests/market_stall");
		Pieces.lantern(b, 3, 3, 4, true);
		Pieces.lantern(b, 7, 3, 4, true);
		Pieces.path(b, 0, 0, 10, 1, 0);
		b.set(3, 1, 0, Pieces.stairs(p.stairs(), Direction.NORTH));
		b.set(7, 1, 0, Pieces.stairs(p.stairs(), Direction.NORTH));
		b.floor(3, 4, 7, 5, 1, "behind the counter");
		b.resident(CharacterVariant.MERCHANT, 5, 1, 5, 5, 4, 6);
	}

	/** Pumpkin Patch Farm: fenced rows of pumpkins with a scarecrow in the middle, and a farm shed. */
	static void pumpkinPatch(Blueprint b) {
		Palette p = b.palette;
		Pieces.fenceRing(b, 0, 1, 10, 14, 1, 5, 1);
		for (int z = 2; z <= 13; z++) {
			if (z % 2 != 0) {
				continue;
			}
			for (int x = 1; x <= 9; x++) {
				b.set(x, 0, z, Blocks.COARSE_DIRT);
				if (x % 2 == 1 && b.random.nextInt(5) != 0) {
					b.set(x, 1, z, Blocks.PUMPKIN);
				}
			}
		}
		// Scarecrow: a fence post body, hay chest, fence arms and a carved pumpkin head.
		b.set(5, 1, 7, p.fence());
		b.set(5, 2, 7, Blocks.HAY_BLOCK);
		b.set(4, 2, 7, p.fence());
		b.set(6, 2, 7, p.fence());
		b.set(5, 3, 7, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.NORTH));
		Pieces.path(b, 5, 0, 5, 0, 0);
		// The farm shed.
		int door = Pieces.cottage(b, 11, 2, 15, 8, 0, 3, Pieces.Roof.GABLE_Z, "farm shed");
		Pieces.path(b, door, 0, door, 1, 0);
		b.bed(12, 1, 6, p.bed());
		b.chest(14, 1, 7, "chests/pumpkin_patch");
		b.set(14, 1, 6, Blocks.COMPOSTER);
		b.resident(CharacterVariant.FARMER, door, 1, 0, 8, 7, 12, at(12, 1, 6), null, false);
	}

	/**
	 * Riverside Mill: a stone mill floor (grindstone, flour sacks, the mill's chest) under a wooden loft with Madison's
	 * bed, a ladder between them, and a water wheel on an axle turning in its own mill race.
	 */
	static void riversideMill(Blueprint b) {
		Palette p = b.palette;
		for (int x = 11; x <= 13; x++) {
			for (int z = 0; z < b.depth; z++) {
				b.set(x, 0, z, Blocks.WATER);
				b.set(x, -1, z, Blocks.WATER);
			}
		}
		Pieces.stoneRoom(b, 1, 2, 9, 10, 0, 4, "mill floor");
		Pieces.room(b, 1, 2, 9, 10, 4, 3, "mill loft");
		Pieces.roof(b, Pieces.Roof.GABLE_X, 1, 2, 9, 10, 7);
		Pieces.door(b, 5, 1, 2, Direction.NORTH);
		Pieces.windowsAround(b, 1, 2, 9, 10, 2, 5);
		Pieces.windowsAround(b, 1, 2, 9, 10, 6, -1);
		Pieces.ladder(b, 8, 1, 4, 9, Direction.NORTH);
		b.set(5, 1, 6, Blocks.GRINDSTONE);
		b.set(3, 1, 6, Blocks.STONECUTTER);
		b.fill(2, 1, 9, 2, 2, 9, Blocks.WHITE_WOOL);
		b.set(3, 1, 9, Blocks.WHITE_WOOL);
		b.chest(4, 1, 9, "chests/riverside_mill");
		b.barrel(2, 1, 8, "chests/barrel_supplies");
		Pieces.lantern(b, 8, 1, 3, false);
		b.bed(2, 5, 8, p.bed());
		b.set(5, 5, 9, Blocks.CRAFTING_TABLE);
		Pieces.lantern(b, 2, 5, 3, false);
		// The wheel: a ring of planks with log spokes in the x = 12 plane, on an axle through the east wall.
		b.fill(10, 3, 6, 11, 3, 6, Pieces.logAlong(p.log(), Direction.Axis.X));
		for (int y = 0; y <= 6; y++) {
			for (int z = 3; z <= 9; z++) {
				double d = Math.sqrt((y - 3) * (y - 3) + (z - 6) * (z - 6));
				if (Math.abs(d - 3.0) < 0.55) {
					b.set(12, y, z, p.planks());
				} else if (d < 2.6 && (y == 3 || z == 6)) {
					b.set(12, y, z, Pieces.logAlong(p.strippedLog(), y == 3 && z != 6 ? Direction.Axis.Z : z == 6 && y != 3 ? Direction.Axis.Y : Direction.Axis.X));
				}
			}
		}
		Pieces.path(b, 5, 0, 5, 1, 0);
		b.resident(CharacterVariant.MADISON, 6, 1, 1, 5, 6, 12, at(2, 5, 8), null, false);
	}

	/** Bee Sanctuary: a fenced meadow thick with flowers, eight hives over campfires, and the beekeeper's apiary hut. */
	static void beeSanctuary(Blueprint b) {
		Pieces.garden(b, 0, 0, 16, 16, 0);
		for (int x = 1; x <= 15; x++) {
			for (int z = 1; z <= 15; z++) {
				if (!b.isSet(x, 1, z) && b.random.nextBoolean()) {
					b.set(x, 1, z, LilyBlueprints.flower(b));
				}
			}
		}
		Pieces.fenceRing(b, 0, 0, 16, 16, 1, 8, 0);
		int door = Pieces.cottage(b, 6, 6, 10, 10, 0, 3, Pieces.Roof.HIP, "apiary");
		b.bed(7, 1, 8, b.palette.bed());
		b.chest(9, 1, 9, "chests/bee_sanctuary");
		b.set(9, 1, 8, Blocks.HONEY_BLOCK);
		for (int[] c : new int[][]{{3, 4}, {13, 4}, {3, 8}, {13, 8}, {3, 12}, {13, 12}, {6, 14}, {10, 14}}) {
			b.beehive(c[0], 1, c[1]);
		}
		Pieces.path(b, door, 0, door, 5, 0);
		b.clear(door, 1, 1, door, 1, 5);
		b.resident(CharacterVariant.BEEKEEPER, door, 1, 4, 8, 8, 12, at(7, 1, 8), null, false);
	}

	/** Sweet Berry Cabin: a cozy cabin with a kitchen, a fenced patch of ripe sweet berries, a woodpile. */
	static void berryCabin(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 2, 1, 10, 7, 0, 3, Pieces.Roof.GABLE_X, "berry cabin");
		Pieces.path(b, door, 0, door, 0, 0);
		b.bed(3, 1, 5, p.bed());
		b.chest(4, 1, 6, "chests/berry_cabin");
		Pieces.kitchen(b, 7, 1, 6, "chests/barrel_supplies");
		Pieces.table(b, 6, 1, 3, true, false);
		Pieces.fenceRing(b, 1, 9, 11, 12, 1, door, 9);
		BlockState berries = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3);
		for (int x = 2; x <= 10; x++) {
			for (int z = 10; z <= 11; z++) {
				b.set(x, 0, z, Blocks.PODZOL);
				if (b.random.nextInt(10) < 7) {
					b.set(x, 1, z, berries);
				}
			}
		}
		b.fill(11, 1, 2, 12, 2, 4, Pieces.logAlong(p.log(), Direction.Axis.Z));
		b.set(4, 1, 0, p.fence());
		Pieces.lantern(b, 4, 2, 0, false);
		b.resident(CharacterVariant.MADISON, door, 1, 8, door, 5, 12, at(3, 1, 5), null, false);
	}
}
