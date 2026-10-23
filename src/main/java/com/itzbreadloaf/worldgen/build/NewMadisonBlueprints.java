package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Update 2's new Madison structures (part 8, batch 4). */
final class NewMadisonBlueprints {
	private NewMadisonBlueprints() {
	}

	private static BlockPos at(int x, int y, int z) {
		return new BlockPos(x, y, z);
	}

	/** A fence of fence blocks along x at height y, with gates at the given x's. */
	private static void fenceRow(Blueprint b, int x0, int x1, int y, int z, int... gates) {
		for (int x = x0; x <= x1; x++) {
			b.set(x, y, z, b.palette.fence());
		}
		for (int g : gates) {
			b.set(g, y, z, b.palette.wood().gate());
		}
	}

	/** Dairy Barn: a barn with three cow stalls behind gated fences, "milk" cauldrons, hay and the farmer's bed. */
	static void dairyBarn(Blueprint b) {
		Palette p = b.palette;
		Pieces.room(b, 1, 1, 13, 9, 0, 4, "dairy barn");
		Pieces.windowsAround(b, 1, 1, 13, 9, 3, 7);
		b.clear(6, 1, 1, 8, 3, 1);
		Pieces.roof(b, Pieces.Roof.GABLE_X, 1, 1, 13, 9, 4);
		fenceRow(b, 2, 12, 1, 5, 3, 7, 11);
		for (int x : new int[]{5, 9}) {
			b.fill(x, 1, 6, x, 1, 8, p.fence());
		}
		for (int x : new int[]{3, 7, 11}) {
			b.mob(x, 1, 7, "cow");
		}
		b.set(2, 1, 8, Blocks.HAY_BLOCK);
		b.set(12, 1, 8, Blocks.HAY_BLOCK);
		b.set(2, 1, 2, Blocks.POWDER_SNOW_CAULDRON);
		b.set(3, 1, 2, Blocks.POWDER_SNOW_CAULDRON);
		b.chest(4, 1, 2, "chests/dairy_barn");
		b.barrel(10, 1, 2, "chests/barrel_supplies");
		b.set(11, 1, 2, Blocks.HAY_BLOCK);
		b.set(12, 1, 2, Blocks.HAY_BLOCK);
		b.bed(12, 1, 3, p.bed());
		Pieces.lantern(b, 5, 1, 2, false);
		Pieces.lantern(b, 9, 1, 2, false);
		b.resident(CharacterVariant.FARMER, 7, 1, 0, 7, 5, 10, at(12, 1, 3), null, false);
	}

	/** Madison's Pie Shop: a big display window, a counter of cakes, smokers and a furnace, stacks of pumpkins. */
	static void pieShop(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 1, 1, 11, 8, 0, 4, Pieces.Roof.GABLE_X, "pie shop");
		for (int x = 2; x <= 10; x++) {
			if (x != door) {
				b.fill(x, 2, 1, x, 3, 1, Blocks.GLASS_PANE);
			}
		}
		for (int x = 2; x <= 7; x++) {
			b.set(x, 1, 4, Pieces.topSlab(p.slab()));
			if (x % 2 == 0) {
				b.set(x, 2, 4, Blocks.CAKE);
			}
		}
		Pieces.table(b, 3, 1, 2, true, false);
		b.set(2, 1, 7, Blocks.SMOKER);
		b.set(3, 1, 7, Blocks.SMOKER);
		b.set(4, 1, 7, Blocks.FURNACE);
		b.chest(6, 1, 7, "chests/pie_shop");
		b.set(9, 1, 7, Blocks.PUMPKIN);
		b.set(10, 1, 7, Blocks.PUMPKIN);
		b.set(10, 2, 7, Blocks.PUMPKIN);
		b.bed(10, 1, 5, p.bed());
		b.set(0, 1, 0, Blocks.PUMPKIN);
		b.set(12, 1, 0, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.NORTH));
		Pieces.path(b, door, 0, door, 0, 0);
		b.floor(8, 5, 9, 6, 1, "kitchen");
		b.resident(CharacterVariant.BAKER, door, 1, 0, door, 4, 10, at(10, 1, 5), null, false);
	}

	/** Fish Smokehouse: smokers along the back, a smoking chimney, dried kelp racks outside, a little fishing pond. */
	static void fishSmokehouse(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 1, 1, 8, 7, 0, 3, Pieces.Roof.GABLE_X, "smokehouse");
		Pieces.path(b, door, 0, door, 0, 0);
		for (int x = 2; x <= 4; x++) {
			b.set(x, 1, 6, Blocks.SMOKER);
		}
		b.chest(5, 1, 6, "chests/fish_smokehouse");
		b.barrel(6, 1, 6, "chests/fishing_barrel");
		b.bed(7, 1, 4, p.bed());
		b.fill(3, 1, 7, 3, 8, 7, p.bricks());
		b.set(3, 9, 7, Blocks.CAMPFIRE);
		for (int x = 9; x <= 11; x++) {
			b.set(x, 1, 2, p.fence());
			b.set(x, 2, 2, Blocks.DRIED_KELP_BLOCK);
		}
		b.fill(9, -1, 6, 12, 0, 9, Blocks.WATER);
		b.fill(9, -2, 6, 12, -2, 9, Blocks.DIRT);
		b.set(10, 1, 8, Blocks.LILY_PAD);
		b.barrel(11, 1, 4, "chests/fishing_barrel");
		b.resident(CharacterVariant.FISHER, door, 1, 0, 6, 5, 10, at(7, 1, 4), null, false);
	}

	/** Chicken Coop Farm: a fenced run with chickens, a raised coop with a ramp and hay nests, the farmer's hut. */
	static void chickenCoop(Blueprint b) {
		Palette p = b.palette;
		Pieces.fenceRing(b, 0, 0, 14, 11, 1, 7, 0);
		Pieces.room(b, 2, 6, 6, 10, 1, 2, "coop");
		Pieces.roof(b, Pieces.Roof.FLAT, 2, 6, 6, 10, 3);
		b.clear(4, 2, 6, 4, 3, 6);
		b.set(4, 1, 5, Pieces.stairs(p.stairs(), Direction.SOUTH));
		b.set(3, 2, 9, Blocks.HAY_BLOCK);
		b.set(5, 2, 9, Blocks.HAY_BLOCK);
		b.mob(4, 2, 8, "chicken");
		b.mob(3, 2, 7, "chicken");
		for (int[] c : new int[][]{{8, 3}, {11, 3}, {6, 3}}) {
			b.mob(c[0], 1, c[1], "chicken");
		}
		b.set(13, 1, 2, Blocks.WATER_CAULDRON);
		b.set(13, 1, 4, Blocks.HAY_BLOCK);
		b.set(1, 1, 1, Blocks.COMPOSTER);
		int door = Pieces.cottage(b, 9, 6, 13, 10, 0, 3, Pieces.Roof.GABLE_X, "farm hut");
		b.bed(10, 1, 8, p.bed());
		b.chest(12, 1, 9, "chests/chicken_coop");
		b.floor(2, 1, 12, 4, 1, "run");
		b.resident(CharacterVariant.FARMER, 7, 1, 2, 7, 6, 10, at(10, 1, 8), null, false);
	}

	/** Beekeeper's Cottage: hives over campfires along the side wall, a flower garden, honeycomb shelves and beeswax candles. */
	static void beekeepersCottage(Blueprint b) {
		Palette p = b.palette;
		LilyBlueprints.flowerBed(b, 0, 0, 12, 0, 0.6F);
		LilyBlueprints.flowerBed(b, 10, 1, 12, 9, 0.5F);
		int door = Pieces.cottage(b, 1, 1, 9, 7, 0, 3, Pieces.Roof.GABLE_X, "beekeeper's cottage");
		Pieces.path(b, door, 0, door, 0, 0);
		b.clear(door, 1, 0, door, 1, 0);
		for (int z : new int[]{2, 4, 6}) {
			b.beehive(11, 1, z);
		}
		b.bed(2, 1, 5, p.bed());
		b.chest(3, 1, 6, "chests/beekeepers_cottage");
		b.set(6, 1, 6, Blocks.HONEYCOMB_BLOCK);
		b.set(7, 1, 6, Blocks.HONEYCOMB_BLOCK);
		b.set(8, 1, 6, Blocks.HONEY_BLOCK);
		b.set(5, 1, 4, Pieces.topSlab(p.slab()));
		b.set(5, 2, 4, Blocks.YELLOW_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
		b.resident(CharacterVariant.BEEKEEPER, door, 1, 0, door, 4, 10, at(2, 1, 5), null, false);
	}

	/** Horse Stables: three gated stalls with horses, hay and water, a tack corner, and a fenced paddock out front. */
	static void horseStables(Blueprint b) {
		Palette p = b.palette;
		Pieces.room(b, 1, 5, 15, 11, 0, 4, "stables");
		Pieces.windowsAround(b, 1, 5, 15, 11, 3, 8);
		b.clear(7, 1, 5, 9, 3, 5);
		Pieces.roof(b, Pieces.Roof.GABLE_X, 1, 5, 15, 11, 4);
		fenceRow(b, 2, 14, 1, 7, 3, 8, 12);
		for (int x : new int[]{6, 10}) {
			b.fill(x, 1, 8, x, 1, 10, p.fence());
			Pieces.lantern(b, x, 2, 8, false);
		}
		for (int x : new int[]{3, 8, 12}) {
			b.mob(x, 1, 9, "horse");
		}
		b.set(2, 1, 10, Blocks.HAY_BLOCK);
		b.set(14, 1, 10, Blocks.HAY_BLOCK);
		b.set(9, 1, 10, Blocks.WATER_CAULDRON);
		b.chest(2, 1, 6, "chests/horse_stables");
		b.barrel(3, 1, 6, "chests/barrel_supplies");
		Pieces.fenceRing(b, 0, 0, 16, 4, 1, 8, 4);
		b.set(8, 1, 0, p.wood().gate());
		b.mob(4, 1, 2, "horse");
		b.floor(1, 1, 15, 3, 1, "paddock");
		b.resident(CharacterVariant.FARMER, 8, 1, 2, 8, 6, 12);
	}

	/** A market stall: posts, a striped awning, a counter at the front with goods on it. */
	private static void stall(Blueprint b, int x0, int z0, Block stripeA, Block stripeB) {
		for (int[] c : new int[][]{{x0, z0}, {x0 + 4, z0}, {x0, z0 + 3}, {x0 + 4, z0 + 3}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], b.palette.fence());
		}
		for (int x = x0; x <= x0 + 4; x++) {
			b.fill(x, 4, z0, x, 4, z0 + 3, x % 2 == 0 ? stripeA : stripeB);
		}
		Pieces.lantern(b, x0 + 2, 3, z0 + 2, true);
	}

	/** Farmers' Market: three stalls (produce, cakes, flowers) under striped awnings, a little fountain and benches. */
	static void farmersMarket(Blueprint b) {
		Palette p = b.palette;
		stall(b, 1, 6, Blocks.GREEN_WOOL, Blocks.WHITE_WOOL);
		b.set(2, 1, 7, Blocks.PUMPKIN);
		b.set(3, 1, 7, Blocks.MELON);
		b.set(4, 1, 7, Blocks.HAY_BLOCK);
		b.barrel(2, 1, 9, "chests/barrel_supplies");
		stall(b, 7, 6, Blocks.RED_WOOL, Blocks.WHITE_WOOL);
		for (int x = 8; x <= 10; x++) {
			b.set(x, 1, 7, Pieces.topSlab(p.slab()));
		}
		b.set(8, 2, 7, Blocks.CAKE);
		b.set(10, 2, 7, Blocks.CAKE);
		b.chest(9, 1, 9, "chests/farmers_market");
		stall(b, 13, 6, Blocks.PINK_WOOL, Blocks.WHITE_WOOL);
		Block[] pots = {Blocks.POTTED_POPPY, Blocks.POTTED_DANDELION, Blocks.POTTED_BLUE_ORCHID};
		for (int x = 14; x <= 16; x++) {
			b.set(x, 1, 7, Pieces.topSlab(p.slab()));
			b.set(x, 2, 7, pots[x - 14]);
		}
		b.barrel(16, 1, 9, "chests/barrel_supplies");
		// The fountain: a shallow basin, too shallow to get stuck in.
		b.fill(8, 0, 1, 10, 0, 3, p.stoneBase());
		b.set(9, 0, 2, Blocks.WATER);
		b.set(9, 1, 2, p.stoneWall());
		for (int x : new int[]{5, 13}) {
			b.set(x, 1, 2, Pieces.stairs(p.stairs(), x < 9 ? Direction.WEST : Direction.EAST));
		}
		b.floor(2, 8, 16, 8, 1, "behind the stalls");
		b.resident(CharacterVariant.MERCHANT, 9, 1, 8, 9, 7, 8);
		b.resident(CharacterVariant.FARMER, 3, 1, 8, 3, 7, 8);
	}

	/** Grain Silo: a round stone silo half full of hay "grain", a ladder up to a lookout room under its cap, a wheat strip. */
	static void grainSilo(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x <= 9; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = Math.sqrt((x - 5) * (x - 5) + (z - 5) * (z - 5));
				if (d <= 3.4) {
					b.set(x, 0, z, p.stoneFloor());
					b.set(x, 16, z, p.stoneSlab());
				}
				if (d > 2.4 && d <= 3.4) {
					b.fill(x, 1, z, x, 15, z, p.bricks());
				} else if (d <= 2.4) {
					b.clear(x, 1, z, x, 15, z);
					b.set(x, 13, z, p.planks());
					if (z >= 5 && !(x == 5 && z == 3)) {
						b.fill(x, 1, z, x, 4, z, Blocks.HAY_BLOCK);
					}
				}
			}
		}
		Pieces.door(b, 2, 1, 4, Direction.WEST);
		Pieces.ladder(b, 5, 1, 13, 3, Direction.SOUTH);
		for (int[] w : new int[][]{{2, 5}, {8, 5}, {5, 8}}) {
			Pieces.window(b, w[0], 14, w[1]);
		}
		b.chest(6, 14, 6, "chests/grain_silo");
		Pieces.lantern(b, 4, 14, 6, false);
		b.floor(3, 3, 7, 4, 1, "silo floor");
		b.floor(4, 4, 6, 6, 14, "lookout");
		Pieces.field(b, 10, 1, 12, 11, 0, Blocks.WHEAT);
		b.barrel(1, 1, 10, "chests/barrel_supplies");
		b.set(1, 1, 11, Blocks.COMPOSTER);
		b.resident(CharacterVariant.FARMER, 1, 1, 1, 5, 5, 10);
	}

	/** Melon & Berry Farm: rows of melons on their stems, sweet berry bushes, a scarecrow and a farm stand, all fenced. */
	static void melonFarm(Blueprint b) {
		Palette p = b.palette;
		Pieces.fenceRing(b, 0, 0, 16, 12, 1, 8, 0);
		BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);
		BlockState stem = Blocks.ATTACHED_MELON_STEM.defaultBlockState().setValue(AttachedStemBlock.FACING, Direction.SOUTH);
		for (int x = 1; x <= 9; x++) {
			for (int z : new int[]{2, 5}) {
				b.set(x, 0, z, farmland);
				b.set(x, 1, z, stem);
				b.set(x, 1, z + 1, Blocks.MELON);
			}
			b.set(x, 0, 4, Blocks.WATER);
		}
		BlockState berries = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3);
		for (int x = 11; x <= 15; x++) {
			for (int z = 2; z <= 6; z++) {
				if ((x + z) % 2 == 0) {
					b.set(x, 1, z, berries);
				}
			}
		}
		b.set(10, 1, 8, p.fence());
		b.set(10, 2, 8, Blocks.HAY_BLOCK);
		b.set(10, 3, 8, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.NORTH));
		for (int[] c : new int[][]{{2, 9}, {6, 9}, {2, 11}, {6, 11}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.fence());
		}
		b.fill(2, 4, 9, 6, 4, 11, p.slab());
		b.set(3, 1, 9, Blocks.MELON);
		b.set(4, 1, 9, Pieces.topSlab(p.slab()));
		b.set(5, 1, 9, Blocks.MELON);
		b.chest(4, 1, 11, "chests/melon_farm");
		Pieces.lantern(b, 4, 3, 10, true);
		b.floor(3, 10, 5, 10, 1, "farm stand");
		b.resident(CharacterVariant.FARMER, 8, 1, 8, 8, 6, 10);
	}

	/** Cookie Cottage: walls of cookie dough with chocolate chips, ovens and cookie jars inside, cocoa growing on a jungle log. */
	static void cookieCottage(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 2, 2, 10, 8, 0, 3, Pieces.Roof.GABLE_X, "cookie cottage");
		for (int y = 1; y <= 3; y++) {
			for (int x = 2; x <= 10; x++) {
				for (int z = 2; z <= 8; z++) {
					if ((x == 2 || x == 10 || z == 2 || z == 8) && b.get(x, y, z) == p.planks()) {
						b.set(x, y, z, b.random.nextInt(5) == 0 ? Blocks.BROWN_CONCRETE : Blocks.SMOOTH_SANDSTONE);
					}
				}
			}
		}
		Pieces.path(b, door, 0, door, 1, 0);
		b.set(3, 1, 7, Blocks.SMOKER);
		b.set(4, 1, 7, Blocks.SMOKER);
		b.chest(5, 1, 7, "chests/cookie_cottage");
		b.set(6, 1, 7, Blocks.DECORATED_POT);
		b.set(7, 1, 7, Blocks.DECORATED_POT);
		b.bed(9, 1, 5, p.bed());
		b.set(6, 1, 4, Pieces.topSlab(p.slab()));
		b.set(6, 2, 4, Blocks.CAKE);
		// Cocoa pods on a jungle log beside the cottage.
		b.fill(11, 1, 6, 11, 3, 6, Blocks.JUNGLE_LOG);
		b.set(11, 2, 5, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.FACING, Direction.SOUTH).setValue(CocoaBlock.AGE, 2));
		b.set(11, 2, 7, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.FACING, Direction.NORTH).setValue(CocoaBlock.AGE, 2));
		b.set(12, 3, 6, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.FACING, Direction.WEST).setValue(CocoaBlock.AGE, 1));
		b.resident(CharacterVariant.BAKER, door, 1, 1, door, 5, 10, at(9, 1, 5), null, false);
	}

	/** Sheep Ranch: a fenced pasture with sheep, a water trough and hay, and a shearing shed with looms and wool. */
	static void sheepRanch(Blueprint b) {
		Palette p = b.palette;
		Pieces.fenceRing(b, 0, 0, 18, 14, 1, 9, 0);
		for (int[] s : new int[][]{{3, 3}, {6, 6}, {9, 4}, {5, 10}}) {
			b.mob(s[0], 1, s[1], "sheep");
		}
		b.set(2, 1, 12, Blocks.WATER_CAULDRON);
		b.set(3, 1, 12, Blocks.WATER_CAULDRON);
		b.set(7, 1, 12, Blocks.HAY_BLOCK);
		b.set(8, 1, 12, Blocks.HAY_BLOCK);
		Pieces.cottage(b, 12, 8, 17, 13, 0, 3, Pieces.Roof.GABLE_X, "shearing shed");
		b.set(13, 1, 12, Blocks.LOOM);
		b.set(14, 1, 12, Blocks.LOOM);
		b.chest(15, 1, 12, "chests/sheep_ranch");
		Block[] wool = {Blocks.WHITE_WOOL, Blocks.LIGHT_GRAY_WOOL, Blocks.BROWN_WOOL, Blocks.BLACK_WOOL, Blocks.PINK_WOOL};
		b.set(16, 1, 11, wool[0]);
		b.set(16, 1, 12, wool[2]);
		b.set(16, 2, 12, wool[4]);
		b.bed(13, 1, 9, p.bed());
		b.floor(2, 2, 10, 10, 1, "pasture");
		b.resident(CharacterVariant.FARMER, 9, 1, 2, 9, 7, 12, at(13, 1, 9), null, false);
	}

	/** Merchant Caravan: a covered wagon (wheels, a wool canopy, cargo inside, a step up the back), a hitched horse, a campfire. */
	static void merchantCaravan(Blueprint b) {
		Palette p = b.palette;
		BlockState wheel = Pieces.logAlong(Blocks.DARK_OAK_LOG.defaultBlockState(), Direction.Axis.Z);
		for (int[] w : new int[][]{{5, 3}, {9, 3}, {5, 7}, {9, 7}}) {
			b.set(w[0], 1, w[1], wheel);
		}
		b.fill(4, 1, 4, 10, 1, 6, p.log());
		b.fill(4, 2, 4, 10, 2, 6, p.planks());
		for (int x = 4; x <= 9; x++) {
			b.set(x, 3, 4, Blocks.WHITE_WOOL);
			b.set(x, 3, 6, Blocks.WHITE_WOOL);
			b.fill(x, 4, 4, x, 4, 6, Blocks.WHITE_WOOL);
		}
		b.set(4, 3, 5, Blocks.WHITE_WOOL);
		// Cargo stacked deep in the wagon (just barrels; the loot is in the chest at the open back).
		for (int x = 5; x <= 8; x++) {
			b.set(x, 3, 5, Blocks.BARREL);
		}
		b.chest(9, 3, 5, "chests/merchant_caravan");
		b.set(11, 1, 5, Pieces.stairs(p.stairs(), Direction.WEST));
		b.floor(10, 5, 10, 5, 3, "wagon");
		b.set(1, 1, 5, p.fence());
		b.mob(2, 1, 5, "horse");
		Pieces.campfire(b, 12, 1, 2);
		b.bed(13, 1, 7, p.bed());
		for (int x = 4; x <= 8; x++) {
			b.set(x, 1, 9, (x % 2 == 0) ? Blocks.ORANGE_CARPET : Blocks.YELLOW_CARPET);
		}
		b.barrel(9, 1, 9, "chests/barrel_supplies");
		b.resident(CharacterVariant.MERCHANT, 7, 1, 8, 7, 6, 10, at(13, 1, 7), null, false);
	}
}
