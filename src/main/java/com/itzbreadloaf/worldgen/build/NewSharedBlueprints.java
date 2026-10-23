package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Update 2's new shared structures (part 8, batch 5): places the friends spend time together. */
final class NewSharedBlueprints {
	private NewSharedBlueprints() {
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

	/** A round tree: a log trunk up to {@code top} and a ball of leaves around its top. */
	private static void tree(Blueprint b, int x, int z, int top, int radius) {
		b.fill(x, 1, z, x, top, z, b.palette.log());
		BlockState leaves = LilyBlueprints.leaves(b.palette.wood().leaves());
		for (int dy = -1; dy <= 2; dy++) {
			int r = dy == 2 ? radius - 1 : radius;
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					int lx = x + dx;
					int lz = z + dz;
					if (dx * dx + dz * dz <= r * r + 1 && lx >= 0 && lz >= 0 && lx < b.width && lz < b.depth && !b.isSet(lx, top + dy, lz)) {
						b.set(lx, top + dy, lz, leaves);
					}
				}
			}
		}
	}

	/** Friends' Picnic: three picnic blankets in their colors with cakes and baskets, a shade tree, a cooler. */
	static void friendsPicnic(Blueprint b) {
		Block[][] blankets = {{Blocks.RED_CARPET, Blocks.WHITE_CARPET}, {Blocks.PINK_CARPET, Blocks.WHITE_CARPET}, {Blocks.YELLOW_CARPET, Blocks.WHITE_CARPET}};
		int[] xs = {1, 6, 11};
		for (int i = 0; i < 3; i++) {
			for (int x = xs[i]; x <= xs[i] + 2; x++) {
				for (int z = 1; z <= 3; z++) {
					b.set(x, 1, z, blankets[i][(x + z) % 2]);
				}
			}
			b.set(xs[i] + 1, 1, 2, Blocks.CAKE);
			b.barrel(xs[i] + 3, 1, 1, "chests/barrel_supplies");
		}
		tree(b, 8, 9, 4, 3);
		b.chest(7, 1, 11, "chests/friends_picnic");
		lampPost(b, 0, 7);
		lampPost(b, 16, 7);
		b.floor(1, 5, 15, 6, 1, "meadow");
		b.resident(CharacterVariant.BREADLOAF, 2, 1, 5, 8, 4, 10);
		b.resident(CharacterVariant.LILY, 7, 1, 5, 8, 4, 10);
		b.resident(CharacterVariant.MADISON, 12, 1, 5, 8, 4, 10);
	}

	/** Campfire Story Circle: a big campfire ringed by log benches, three tents facing it, lamp posts. */
	static void storyCircle(Blueprint b) {
		Palette p = b.palette;
		b.set(7, 1, 6, Blocks.CAMPFIRE);
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z <= 10; z++) {
				double d = dist(x, z, 7, 6);
				if (d >= 2.6 && d <= 3.4 && x != 7 && z != 6) {
					b.set(x, 1, z, Pieces.logAlong(p.log(), Math.abs(x - 7) > Math.abs(z - 6) ? Direction.Axis.Z : Direction.Axis.X));
				}
			}
		}
		Block[] wool = {Blocks.RED_WOOL, Blocks.PINK_WOOL, Blocks.YELLOW_WOOL};
		CharacterVariant[] who = {CharacterVariant.BREADLOAF, CharacterVariant.LILY, CharacterVariant.MADISON};
		for (int i = 0; i < 3; i++) {
			int ox = i * 5;
			Pieces.tent(b, ox, 10, 5, wool[i].defaultBlockState());
			b.bed(ox + 1, 1, 11, p.bed());
			b.resident(who[i], 7, 1, 2, 7, 7, 10, at(ox + 1, 1, 11), null, false);
		}
		b.chest(8, 1, 13, "chests/story_circle");
		lampPost(b, 0, 0);
		lampPost(b, 14, 0);
		b.floor(6, 3, 8, 4, 1, "circle");
	}

	/** Movie Night Screen: a big framed screen, rows of seats with an aisle, a projector booth and a popcorn stand. */
	static void movieNight(Blueprint b) {
		Palette p = b.palette;
		b.fill(2, 1, 14, 14, 8, 14, Blocks.BLACK_CONCRETE);
		b.fill(3, 2, 14, 13, 7, 14, Blocks.WHITE_CONCRETE);
		for (int x : new int[]{3, 8, 13}) {
			b.fill(x, 1, 15, x, 7, 15, p.log());
		}
		for (int z : new int[]{6, 8, 10}) {
			for (int x = 3; x <= 13; x++) {
				if (x != 8) {
					b.set(x, 1, z, Pieces.stairs(p.stairs(), Direction.NORTH));
				}
			}
		}
		for (int[] c : new int[][]{{6, 0}, {10, 0}, {6, 2}, {10, 2}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.fence());
		}
		b.fill(6, 4, 0, 10, 4, 2, p.slab());
		b.set(8, 1, 1, p.fence());
		b.set(8, 2, 1, Blocks.OBSERVER.defaultBlockState().setValue(ObserverBlock.FACING, Direction.NORTH));
		b.set(1, 1, 2, Blocks.HAY_BLOCK);
		b.set(1, 1, 3, Blocks.YELLOW_CONCRETE_POWDER);
		b.chest(2, 1, 2, "chests/movie_night");
		lampPost(b, 0, 12);
		lampPost(b, 16, 12);
		b.floor(8, 5, 8, 11, 1, "aisle");
		b.resident(CharacterVariant.BREADLOAF, 5, 1, 7, 8, 8, 10);
		b.resident(CharacterVariant.LILY, 11, 1, 7, 8, 8, 10);
		b.resident(CharacterVariant.MADISON, 8, 1, 4, 8, 8, 10);
	}

	/** Friends' Winter Lodge: a big lodge with three beds, a long table, a stone fireplace and chimney, a snowman outside. */
	static void winterLodge(Blueprint b) {
		Palette p = b.palette;
		Pieces.room(b, 1, 1, 15, 10, 0, 4, "lodge");
		b.walls(1, 1, 15, 10, 1, 1, p.bricks());
		Pieces.windowsAround(b, 1, 1, 15, 10, 3, 8);
		Pieces.roof(b, Pieces.Roof.GABLE_X, 1, 1, 15, 10, 4);
		Pieces.door(b, 8, 1, 1, Direction.NORTH);
		b.fill(9, 1, 10, 11, 3, 10, Blocks.BRICKS);
		b.set(10, 1, 10, Blocks.CAMPFIRE);
		b.fill(10, 1, 11, 10, 10, 11, Blocks.BRICKS);
		b.set(10, 11, 11, Blocks.CAMPFIRE);
		Block[] beds = {Blocks.RED_BED, Blocks.PINK_BED, Blocks.YELLOW_BED};
		CharacterVariant[] who = {CharacterVariant.BREADLOAF, CharacterVariant.LILY, CharacterVariant.MADISON};
		for (int i = 0; i < 3; i++) {
			b.bed(2 + i * 2, 1, 7, beds[i]);
			b.resident(who[i], 8, 1, 0, 8, 5, 10, at(2 + i * 2, 1, 7), null, false);
		}
		for (int x = 9; x <= 12; x++) {
			b.set(x, 1, 5, Pieces.topSlab(p.slab()));
			b.set(x, 1, 4, Pieces.stairs(p.stairs(), Direction.NORTH));
			b.set(x, 1, 6, Pieces.stairs(p.stairs(), Direction.SOUTH));
		}
		b.fill(3, 1, 3, 5, 1, 4, Blocks.RED_CARPET);
		b.chest(14, 1, 9, "chests/winter_lodge");
		Pieces.lantern(b, 14, 1, 2, false);
		Pieces.lantern(b, 2, 1, 2, false);
		b.fill(15, 1, 12, 15, 2, 12, Blocks.SNOW_BLOCK);
		b.set(15, 3, 12, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.NORTH));
	}

	/** Obstacle Course: hay hurdles, a balance beam, a climbing wall with a ladder, the finish line and a winners' podium. */
	static void obstacleCourse(Blueprint b) {
		Palette p = b.palette;
		Pieces.path(b, 1, 3, 19, 7, 0);
		for (int x : new int[]{4, 8}) {
			b.fill(x, 1, 3, x, 1, 7, Blocks.HAY_BLOCK);
		}
		b.fill(10, 1, 5, 13, 1, 5, Pieces.logAlong(p.log(), Direction.Axis.X));
		b.fill(15, 1, 3, 15, 2, 7, p.bricks());
		Pieces.ladder(b, 14, 1, 2, 5, Direction.WEST);
		for (int z = 3; z <= 7; z++) {
			b.set(17, 0, z, (z % 2 == 0) ? Blocks.WHITE_CONCRETE : Blocks.BLACK_CONCRETE);
		}
		b.set(1, 1, 2, Blocks.RED_BANNER);
		b.set(1, 1, 8, Blocks.BLUE_BANNER);
		b.fill(19, 1, 1, 19, 3, 1, Blocks.GOLD_BLOCK);
		b.fill(18, 1, 1, 18, 2, 1, Blocks.IRON_BLOCK);
		b.set(20, 1, 1, Blocks.COPPER_BLOCK);
		for (int x = 4; x <= 12; x++) {
			b.set(x, 1, 9, Pieces.stairs(p.stairs(), Direction.SOUTH));
		}
		b.chest(20, 1, 9, "chests/obstacle_course");
		b.floor(17, 3, 19, 7, 1, "finish");
		b.resident(CharacterVariant.BREADLOAF, 2, 1, 5, 10, 5, 12);
		b.resident(CharacterVariant.LILY, 2, 1, 6, 10, 5, 12);
	}

	/** Treehouse Clubhouse: a big tree with the friends' clubhouse around its trunk, a ladder up, three beds and banners. */
	static void clubhouseTree(Blueprint b) {
		Palette p = b.palette;
		int floorY = 8;
		Pieces.room(b, 3, 3, 11, 11, floorY, 3, "clubhouse");
		Pieces.windowsAround(b, 3, 3, 11, 11, floorY + 2, 7);
		Pieces.roof(b, Pieces.Roof.FLAT, 3, 3, 11, 11, floorY + 3);
		b.fill(6, 1, 6, 8, floorY + 5, 8, p.log());
		Pieces.ladder(b, 7, 1, floorY, 5, Direction.NORTH);
		BlockState leaves = LilyBlueprints.leaves(p.wood().leaves());
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 7, 7);
				for (int y = floorY + 5; y <= floorY + 7; y++) {
					double r = y == floorY + 7 ? 3.5 : 6.2;
					if (d <= r && !b.isSet(x, y, z)) {
						b.set(x, y, z, leaves);
					}
				}
			}
		}
		Block[] beds = {Blocks.RED_BED, Blocks.PINK_BED, Blocks.YELLOW_BED};
		CharacterVariant[] who = {CharacterVariant.BREADLOAF, CharacterVariant.LILY, CharacterVariant.MADISON};
		int[][] bedAt = {{4, 9}, {10, 9}, {10, 4}};
		for (int i = 0; i < 3; i++) {
			b.bed(bedAt[i][0], floorY + 1, bedAt[i][1], beds[i]);
			b.resident(who[i], 7, 1, 3, 7, 7, 10, at(bedAt[i][0], floorY + 1, bedAt[i][1]), null, false);
		}
		b.chest(4, floorY + 1, 4, "chests/clubhouse_tree");
		b.set(5, floorY + 1, 10, Blocks.RED_BANNER);
		b.set(9, floorY + 1, 10, Blocks.YELLOW_BANNER);
		b.set(9, floorY + 1, 6, Blocks.PINK_BANNER);
		Pieces.lantern(b, 5, floorY + 1, 4, false);
	}

	/** A market stall with a striped awning on four posts. */
	private static void stall(Blueprint b, int x0, int z0, Block stripeA, Block stripeB) {
		for (int[] c : new int[][]{{x0, z0}, {x0 + 4, z0}, {x0, z0 + 3}, {x0 + 4, z0 + 3}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], b.palette.fence());
		}
		for (int x = x0; x <= x0 + 4; x++) {
			b.fill(x, 4, z0, x, 4, z0 + 3, x % 2 == 0 ? stripeA : stripeB);
		}
	}

	/** Harvest Fair: a prize-winning giant pumpkin with a blue ribbon, two stalls, a hay-ride wagon and a scarecrow. */
	static void harvestFair(Blueprint b) {
		Palette p = b.palette;
		for (int x = 1; x <= 7; x++) {
			for (int z = 1; z <= 7; z++) {
				for (int y = 1; y <= 5; y++) {
					double r = Math.sqrt((x - 4) * (x - 4) + (y - 3) * (y - 3) * 1.4 + (z - 4) * (z - 4));
					if (r <= 2.7) {
						b.set(x, y, z, Blocks.PUMPKIN);
					}
				}
			}
		}
		b.set(4, 6, 4, Blocks.OAK_LOG);
		b.set(4, 3, 1, Blocks.BLUE_WOOL);
		stall(b, 9, 1, Blocks.ORANGE_WOOL, Blocks.WHITE_WOOL);
		b.set(10, 1, 2, Blocks.PUMPKIN);
		b.set(11, 1, 2, Pieces.topSlab(p.slab()));
		b.set(11, 2, 2, Blocks.CAKE);
		b.set(12, 1, 2, Blocks.HAY_BLOCK);
		stall(b, 14, 1, Blocks.RED_WOOL, Blocks.WHITE_WOOL);
		for (int x = 15; x <= 17; x++) {
			b.set(x, 1, 2, Pieces.topSlab(p.slab()));
		}
		b.set(16, 2, 2, Blocks.CAKE);
		b.chest(16, 1, 4, "chests/harvest_fair");
		// Hay-ride wagon.
		BlockState wheel = Pieces.logAlong(Blocks.DARK_OAK_LOG.defaultBlockState(), Direction.Axis.Z);
		for (int[] w : new int[][]{{14, 8}, {17, 8}, {14, 12}, {17, 12}}) {
			b.set(w[0], 1, w[1], wheel);
		}
		b.fill(13, 2, 9, 18, 2, 11, p.planks());
		b.fill(14, 3, 9, 17, 3, 9, Blocks.HAY_BLOCK);
		b.fill(14, 3, 11, 17, 3, 11, Blocks.HAY_BLOCK);
		// Scarecrow.
		b.set(3, 1, 11, p.fence());
		b.set(3, 2, 11, Blocks.HAY_BLOCK);
		b.set(2, 2, 11, p.fence());
		b.set(4, 2, 11, p.fence());
		b.set(3, 3, 11, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.NORTH));
		b.floor(8, 6, 12, 8, 1, "fairground");
		b.resident(CharacterVariant.MADISON, 9, 1, 7, 9, 7, 12);
		b.resident(CharacterVariant.FARMER, 10, 1, 7, 9, 7, 12);
		b.resident(CharacterVariant.BREADLOAF, 11, 1, 7, 9, 7, 12);
	}

	/** Community Garden: fenced plots of different crops, a flower plot, a safe well and an open tool shed. */
	static void communityGarden(Blueprint b) {
		Palette p = b.palette;
		Pieces.fenceRing(b, 0, 0, 16, 16, 1, 8, 0);
		Pieces.path(b, 1, 1, 15, 15, 0);
		Pieces.field(b, 1, 2, 4, 5, 0, Blocks.WHEAT);
		Pieces.field(b, 6, 2, 9, 5, 0, Blocks.CARROTS);
		Pieces.field(b, 11, 2, 14, 5, 0, Blocks.POTATOES);
		Pieces.field(b, 1, 8, 4, 11, 0, Blocks.BEETROOTS);
		LilyBlueprints.flowerBed(b, 11, 8, 14, 11, 0.8F);
		// The well: fenced and roofed so nobody falls in.
		b.fill(6, 0, 8, 8, 0, 10, p.stoneBase());
		b.set(7, 0, 9, Blocks.WATER);
		b.set(7, -1, 9, Blocks.WATER);
		b.walls(6, 8, 8, 10, 1, 1, p.fence());
		for (int[] c : new int[][]{{6, 8}, {8, 8}, {6, 10}, {8, 10}}) {
			b.fill(c[0], 2, c[1], c[0], 3, c[1], p.fence());
		}
		b.fill(6, 4, 8, 8, 4, 10, p.slab());
		// Tool shed.
		for (int[] c : new int[][]{{11, 13}, {15, 13}, {11, 15}, {15, 15}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.fence());
		}
		b.fill(11, 4, 13, 15, 4, 15, p.slab());
		b.set(12, 1, 15, Blocks.COMPOSTER);
		b.set(13, 1, 15, Blocks.CRAFTING_TABLE);
		b.chest(14, 1, 15, "chests/community_garden");
		b.barrel(12, 1, 14, "chests/barrel_supplies");
		b.set(3, 1, 14, Pieces.stairs(p.stairs(), Direction.SOUTH));
		b.set(4, 1, 14, Pieces.stairs(p.stairs(), Direction.SOUTH));
		b.floor(1, 13, 9, 15, 1, "garden paths");
		b.resident(CharacterVariant.GARDENER, 8, 1, 1, 8, 8, 10);
		b.resident(CharacterVariant.FARMER, 8, 1, 1, 8, 8, 10);
		b.resident(CharacterVariant.BREADLOAF, 8, 1, 1, 8, 8, 10);
	}

	/** Mini Golf Course: three holes with fairways, flags and obstacles (a windmill, a ramp, slime bumpers), a little counter. */
	static void miniGolf(Blueprint b) {
		Palette p = b.palette;
		int[] lanes = {1, 7, 13};
		for (int i = 0; i < 3; i++) {
			int x0 = lanes[i];
			b.fill(x0, 0, 1, x0 + 3, 0, 12, Blocks.GREEN_CONCRETE);
			b.set(x0 + 1, 0, 1, Blocks.WHITE_CONCRETE);
			b.clear(x0 + 1, 0, 11, x0 + 1, 0, 11);
			b.set(x0 + 1, -1, 11, Blocks.GREEN_CONCRETE);
			b.fill(x0 + 2, 1, 11, x0 + 2, 3, 11, p.fence());
			b.set(x0 + 2, 4, 11, i == 0 ? Blocks.RED_WOOL : i == 1 ? Blocks.YELLOW_WOOL : Blocks.LIGHT_BLUE_WOOL);
		}
		// Hole 2: a windmill. Hole 3: a ramp and two bumpers.
		b.fill(8, 1, 6, 8, 3, 6, p.fence());
		b.set(7, 3, 6, Blocks.WHITE_WOOL);
		b.set(9, 3, 6, Blocks.WHITE_WOOL);
		b.set(8, 4, 6, Blocks.RED_WOOL);
		b.set(14, 1, 5, Pieces.stairs(p.stoneStairs(), Direction.SOUTH));
		b.set(15, 1, 5, Pieces.stairs(p.stoneStairs(), Direction.SOUTH));
		b.set(14, 1, 6, p.stoneSlab());
		b.set(15, 1, 6, p.stoneSlab());
		b.set(13, 1, 9, Blocks.SLIME_BLOCK);
		b.set(16, 1, 9, Blocks.SLIME_BLOCK);
		b.set(18, 1, 2, Pieces.topSlab(p.slab()));
		b.chest(18, 1, 3, "chests/mini_golf");
		lampPost(b, 18, 0);
		b.floor(2, 2, 3, 9, 1, "hole 1");
		b.resident(CharacterVariant.BREADLOAF, 5, 1, 1, 9, 7, 10);
		b.resident(CharacterVariant.MADISON, 11, 1, 1, 9, 7, 10);
	}

	/**
	 * The Truce Pavilion: a round stone pavilion where FelixSyn and ItzBreadLoaf once signed a truce (Lily and Madison
	 * set it up). A long table half red and half blue, a chair at each end, the treaty on a lectern, their banners.
	 */
	static void trucePavilion(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 7, 6);
				if (d <= 5.5) {
					b.set(x, 0, z, p.stoneFloor());
					b.set(x, 5, z, p.stoneSlab());
				}
				if (d <= 3.6) {
					b.set(x, 6, z, p.stoneSlab());
				}
				if (d <= 1.6) {
					b.set(x, 7, z, p.stoneSlab());
				}
			}
		}
		for (int i = 0; i < 8; i++) {
			double a = Math.PI * 2 * i / 8 + Math.PI / 8;
			int x = (int) Math.round(7 + 5 * Math.cos(a));
			int z = (int) Math.round(6 + 5 * Math.sin(a));
			b.fill(x, 1, z, x, 4, z, Blocks.QUARTZ_PILLAR);
		}
		for (int x = 5; x <= 9; x++) {
			b.set(x, 1, 6, Pieces.topSlab(p.slab()));
		}
		b.fill(3, 0, 5, 6, 0, 7, Blocks.RED_WOOL);
		b.fill(8, 0, 5, 11, 0, 7, Blocks.BLUE_WOOL);
		b.set(4, 1, 6, Pieces.stairs(p.stairs(), Direction.WEST));
		b.set(10, 1, 6, Pieces.stairs(p.stairs(), Direction.EAST));
		b.set(7, 1, 4, Blocks.LECTERN);
		b.set(3, 1, 3, Blocks.RED_BANNER);
		b.set(11, 1, 3, Blocks.BLUE_BANNER);
		b.chest(7, 1, 9, "chests/truce_pavilion");
		Pieces.path(b, 7, 0, 7, 1, 0);
		LilyBlueprints.flowerBed(b, 0, 12, 14, 12, 0.5F);
		b.floor(5, 3, 9, 4, 1, "pavilion");
		b.resident(CharacterVariant.LILY, 6, 1, 3, 7, 6, 10);
		b.resident(CharacterVariant.MADISON, 8, 1, 3, 7, 6, 10);
		b.resident(CharacterVariant.FELIXSYN, 7, 1, 8, 7, 6, 10);
	}
}
