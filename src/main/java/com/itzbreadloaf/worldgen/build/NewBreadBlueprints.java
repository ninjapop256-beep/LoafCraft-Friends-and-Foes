package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import java.util.ArrayDeque;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.RailShape;

/** Update 2's new ItzBreadLoaf structures (part 8, batch 1). */
final class NewBreadBlueprints {
	private NewBreadBlueprints() {
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

	/** Roadside Bread Stand: a little stall under a bread-colored awning, hay and cakes on the counter. */
	static void breadStand(Blueprint b) {
		Palette p = b.palette;
		b.fill(1, 0, 1, 7, 0, 5, p.planks());
		for (int[] c : new int[][]{{1, 1}, {7, 1}, {1, 5}, {7, 5}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.fence());
		}
		for (int x = 0; x <= 8; x++) {
			b.fill(x, 4, 0, x, 4, 6, x % 2 == 0 ? Blocks.YELLOW_WOOL : Blocks.BROWN_WOOL);
		}
		b.set(2, 1, 2, Blocks.HAY_BLOCK);
		b.set(6, 1, 2, Blocks.HAY_BLOCK);
		for (int x = 3; x <= 5; x++) {
			b.set(x, 1, 2, Pieces.topSlab(p.slab()));
		}
		b.set(4, 2, 2, Blocks.CAKE);
		b.barrel(3, 1, 4, "chests/bread_stand");
		b.set(5, 1, 4, Blocks.CRAFTING_TABLE);
		b.set(6, 1, 4, Blocks.SMOKER);
		Pieces.lantern(b, 2, 3, 3, true);
		Pieces.lantern(b, 6, 3, 3, true);
		b.floor(2, 3, 6, 4, 1, "behind the counter");
		b.resident(CharacterVariant.BREADLOAF, 4, 1, 3, 4, 3, 8);
	}

	/** Toaster House: a giant toaster (two slices of toast sticking out of the top, a lever on the side) you can live in. */
	static void toasterHouse(Blueprint b) {
		Palette p = b.palette;
		b.fill(1, 0, 1, 9, 0, 7, p.planks());
		b.walls(1, 1, 9, 7, 1, 5, Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());
		b.clear(2, 1, 2, 8, 5, 6);
		for (int[] c : new int[][]{{1, 1}, {9, 1}, {1, 7}, {9, 7}}) {
			b.fill(c[0], 1, c[1], c[0], 5, c[1], Blocks.IRON_BLOCK);
		}
		b.fill(1, 6, 1, 9, 6, 7, Blocks.SMOOTH_STONE_SLAB);
		b.floor(2, 2, 8, 6, 1, "toaster");
		// Two slices of toast in the slots on top.
		for (int z : new int[]{3, 5}) {
			b.fill(3, 6, z, 7, 7, z, Blocks.ORANGE_TERRACOTTA);
			b.fill(3, 8, z, 7, 8, z, Blocks.BROWN_TERRACOTTA);
		}
		b.set(10, 3, 4, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.WALL).setValue(LeverBlock.FACING, Direction.EAST));
		Pieces.door(b, 5, 1, 1, Direction.NORTH);
		Pieces.window(b, 3, 3, 1);
		Pieces.window(b, 7, 3, 1);
		Pieces.window(b, 1, 3, 4);
		Pieces.window(b, 9, 3, 4);
		b.bed(2, 1, 5, p.bed());
		b.chest(3, 1, 6, "chests/toaster_house");
		b.set(7, 1, 6, Blocks.FURNACE);
		b.set(8, 1, 6, Blocks.SMOKER);
		Pieces.table(b, 6, 1, 3, false, false);
		Pieces.lantern(b, 8, 1, 2, false);
		Pieces.path(b, 5, 0, 5, 0, 0);
		b.resident(CharacterVariant.BREADLOAF, 5, 1, 0, 5, 4, 10, at(2, 1, 5), null, false);
	}

	/** Baguette Tower: a tall golden tower with diagonal score marks, a ladder through four floors, a lookout in its tip. */
	static void baguetteTower(Blueprint b) {
		int top = 18;
		Pieces.tower(b, 2, 2, 5, 0, top, 6, true);
		Pieces.door(b, 4, 1, 2, Direction.NORTH);
		// Bake it: terracotta walls with diagonal scores, darker corners.
		for (int y = 1; y <= top; y++) {
			for (int x = 2; x <= 6; x++) {
				for (int z = 2; z <= 6; z++) {
					boolean wall = x == 2 || x == 6 || z == 2 || z == 6;
					BlockState s = b.get(x, y, z);
					if (!wall || s == null || s.isAir() || s.getBlock() == Blocks.GLASS_PANE || s.getBlock() instanceof net.minecraft.world.level.block.DoorBlock) {
						continue;
					}
					boolean corner = (x == 2 || x == 6) && (z == 2 || z == 6);
					b.set(x, y, z, corner ? Blocks.BROWN_TERRACOTTA : (x + y + z) % 5 == 0 ? Blocks.YELLOW_TERRACOTTA : Blocks.ORANGE_TERRACOTTA);
				}
			}
		}
		// The tip: two more rows of wall with open slits, a crusty cap.
		for (int y = top + 1; y <= top + 2; y++) {
			b.walls(2, 2, 6, 6, y, y, Blocks.ORANGE_TERRACOTTA.defaultBlockState());
			b.clear(4, y, 2, 4, y, 2);
			b.clear(4, y, 6, 4, y, 6);
			b.clear(2, y, 4, 2, y, 4);
			b.clear(6, y, 4, 6, y, 4);
		}
		b.fill(2, top + 3, 2, 6, top + 3, 6, Blocks.BROWN_TERRACOTTA);
		b.fill(3, top + 4, 3, 5, top + 4, 5, Blocks.BROWN_TERRACOTTA);
		b.chest(5, 13, 3, "chests/baguette_tower");
		b.resident(CharacterVariant.ARCHER, 3, top + 1, 3, 4, 4, 4);
	}

	/** Crumb Trail Camp: a trail of hay "crumbs" winding to a walk-in tent, a campfire and a picnic blanket. */
	static void crumbTrailCamp(Blueprint b) {
		Palette p = b.palette;
		int[][] trail = {{0, 5}, {1, 5}, {2, 4}, {3, 4}, {4, 5}, {5, 6}, {6, 6}, {7, 5}, {8, 4}};
		for (int i = 0; i < trail.length; i++) {
			b.set(trail[i][0], 0, trail[i][1], i % 2 == 0 ? Blocks.HAY_BLOCK : Blocks.COARSE_DIRT);
		}
		Pieces.tent(b, 8, 3, 7, Blocks.ORANGE_WOOL.defaultBlockState());
		b.bed(9, 1, 4, p.bed());
		b.bed(11, 1, 4, p.bed());
		b.chest(11, 1, 8, "chests/crumb_trail_camp");
		Pieces.lantern(b, 10, 1, 8, false);
		Pieces.campfire(b, 4, 1, 9);
		for (int x = 1; x <= 3; x++) {
			for (int z = 1; z <= 2; z++) {
				b.set(x, 1, z, (x + z) % 2 == 0 ? Blocks.RED_CARPET : Blocks.WHITE_CARPET);
			}
		}
		b.set(5, 1, 1, Blocks.CAKE);
		for (int i = 0; i < 2; i++) {
			b.resident(CharacterVariant.BREADLOAF, 6, 1, 2, 8, 5, 10, i == 0 ? at(9, 1, 4) : at(11, 1, 4), null, true);
		}
	}

	/** Knight Training Yard: a fenced yard with training dummies, targets and banners, and the knights' armory shed. */
	static void knightTrainingYard(Blueprint b) {
		Palette p = b.palette;
		Pieces.fenceRing(b, 0, 0, 16, 14, 1, 8, 0);
		int door = Pieces.cottage(b, 10, 8, 15, 13, 0, 3, Pieces.Roof.GABLE_X, "armory");
		b.bed(11, 1, 11, p.bed());
		b.bed(14, 1, 11, p.bed());
		b.chest(12, 1, 12, "chests/knight_training_yard");
		b.set(13, 1, 12, Blocks.ANVIL);
		for (int[] d : new int[][]{{3, 3}, {6, 3}, {3, 6}}) {
			b.mob(d[0], 1, d[1], "dummy");
		}
		for (int x = 2; x <= 6; x += 2) {
			b.set(x, 1, 12, Blocks.HAY_BLOCK);
			b.set(x, 2, 12, Blocks.TARGET);
		}
		b.fill(1, 1, 1, 1, 3, 1, p.fence());
		b.set(1, 4, 1, Blocks.RED_BANNER);
		b.fill(15, 1, 1, 15, 3, 1, p.fence());
		b.set(15, 4, 1, Blocks.BLUE_BANNER);
		b.set(12, 1, 6, Blocks.GRINDSTONE);
		lampPost(b, 8, 7);
		b.floor(2, 2, 9, 6, 1, "yard");
		for (int i = 0; i < 2; i++) {
			b.resident(CharacterVariant.KNIGHT, door, 1, 7, 8, 7, 10, i == 0 ? at(11, 1, 11) : at(14, 1, 11), null, true);
		}
	}

	/**
	 * Bread Bank: a stone bank with pillars out front, a teller counter with bars, and a vault behind an iron door
	 * (the button is on the wall beside it). A Captain guards it.
	 */
	static void breadBank(Blueprint b) {
		Palette p = b.palette;
		Pieces.stoneRoom(b, 1, 1, 11, 9, 0, 4, "bank hall");
		b.fill(1, 5, 1, 11, 5, 9, p.bricks());
		b.walls(1, 1, 11, 9, 6, 6, p.stoneSlab());
		// Portico: pillars and a roof over the steps.
		for (int x : new int[]{3, 9}) {
			b.fill(x, 1, 0, x, 4, 0, Blocks.QUARTZ_PILLAR);
		}
		b.fill(2, 5, 0, 10, 5, 0, p.stoneSlab());
		Pieces.door(b, 6, 1, 1, Direction.NORTH);
		b.set(4, 2, 1, Blocks.IRON_BARS);
		b.set(8, 2, 1, Blocks.IRON_BARS);
		// Teller counter, open at the east end.
		for (int x = 2; x <= 8; x++) {
			b.set(x, 1, 4, Pieces.topSlab(p.stoneSlab()));
			if (x != 5) {
				b.set(x, 2, 4, Blocks.IRON_BARS);
			}
		}
		// The vault: a wall with an iron door and a button beside it.
		b.fill(2, 1, 6, 10, 4, 6, p.bricks());
		b.set(6, 1, 6, Blocks.IRON_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.NORTH));
		b.set(6, 2, 6, Blocks.IRON_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.NORTH)
				.setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
		b.set(7, 2, 5, Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACE, AttachFace.WALL).setValue(ButtonBlock.FACING, Direction.NORTH));
		for (int[] g : new int[][]{{2, 8}, {10, 8}, {2, 7}, {10, 7}}) {
			b.set(g[0], 1, g[1], Blocks.GOLD_BLOCK);
		}
		b.chest(4, 1, 8, "chests/bread_bank");
		b.chest(8, 1, 8, "chests/bread_bank");
		b.set(6, 1, 8, Blocks.EMERALD_BLOCK);
		b.floor(3, 7, 9, 8, 1, "vault");
		Pieces.lantern(b, 4, 4, 3, true);
		Pieces.lantern(b, 8, 4, 3, true);
		Pieces.lantern(b, 6, 4, 7, true);
		b.resident(CharacterVariant.CAPTAIN, 9, 1, 3, 6, 4, 6);
	}

	/** Sweaty Gym: an outdoor gym with a parkour course, anvil "weights", punching dummies and a locker room. */
	static void sweatyGym(Blueprint b) {
		Palette p = b.palette;
		Pieces.fenceRing(b, 0, 0, 14, 12, 1, 7, 0);
		int[] heights = {1, 2, 3, 2, 3, 4};
		for (int i = 0; i < heights.length; i++) {
			b.fill(2 + i * 2, 1, 3, 2 + i * 2, heights[i], 3, p.bricks());
		}
		b.set(3, 1, 10, Blocks.ANVIL);
		b.set(4, 1, 10, Blocks.ANVIL);
		b.set(2, 1, 8, Pieces.stairs(p.stairs(), Direction.WEST));
		b.mob(6, 1, 7, "dummy");
		b.mob(8, 1, 7, "dummy");
		b.set(1, 1, 6, Blocks.WATER_CAULDRON);
		Pieces.cottage(b, 9, 7, 13, 11, 0, 3, Pieces.Roof.GABLE_X, "locker room");
		b.bed(10, 1, 9, p.bed());
		b.chest(12, 1, 10, "chests/sweaty_gym");
		b.floor(1, 5, 8, 6, 1, "gym");
		b.resident(CharacterVariant.SWEATY, 7, 1, 6, 7, 6, 10, at(10, 1, 9), null, false);
	}

	/** Hay Bale Maze: a real maze of hay two blocks high (a different one each time), the prize chest in a dead end. */
	static void hayMaze(Blueprint b) {
		int cells = 8;
		b.fill(0, 1, 0, 16, 2, 16, Blocks.HAY_BLOCK);
		boolean[][] seen = new boolean[cells][cells];
		ArrayDeque<int[]> stack = new ArrayDeque<>();
		stack.push(new int[]{0, 0});
		seen[0][0] = true;
		b.clear(1, 1, 1, 1, 2, 1);
		int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
		while (!stack.isEmpty()) {
			int[] cell = stack.peek();
			int[] order = {0, 1, 2, 3};
			for (int i = 3; i > 0; i--) {
				int j = b.random.nextInt(i + 1);
				int t = order[i];
				order[i] = order[j];
				order[j] = t;
			}
			boolean moved = false;
			for (int o : order) {
				int nx = cell[0] + dirs[o][0];
				int nz = cell[1] + dirs[o][1];
				if (nx < 0 || nz < 0 || nx >= cells || nz >= cells || seen[nx][nz]) {
					continue;
				}
				seen[nx][nz] = true;
				int x = 1 + 2 * cell[0];
				int z = 1 + 2 * cell[1];
				b.clear(Math.min(x, x + 2 * dirs[o][0]), 1, Math.min(z, z + 2 * dirs[o][1]),
						Math.max(x, x + 2 * dirs[o][0]), 2, Math.max(z, z + 2 * dirs[o][1]));
				stack.push(new int[]{nx, nz});
				moved = true;
				break;
			}
			if (!moved) {
				stack.pop();
			}
		}
		// The way in, the prize in the far corner, lanterns on top of some walls.
		b.clear(1, 1, 0, 1, 2, 0);
		b.chest(15, 1, 15, "chests/hay_maze");
		for (int[] l : new int[][]{{0, 0}, {16, 0}, {0, 16}, {16, 16}, {8, 8}}) {
			Pieces.lantern(b, l[0], 3, l[1], false);
		}
		b.resident(CharacterVariant.BREADLOAF, 1, 1, 1, 8, 8, 10);
	}

	/** Miner's Rest Stop: a little cabin, ore piles and a rail track into a short tunnel in a stone mound. */
	static void minersRest(Blueprint b) {
		Palette p = b.palette;
		int door = Pieces.cottage(b, 1, 2, 7, 8, 0, 3, Pieces.Roof.GABLE_X, "miner's rest");
		b.bed(2, 1, 6, p.bed());
		b.chest(3, 1, 7, "chests/miners_rest");
		b.set(6, 1, 7, Blocks.FURNACE);
		b.set(6, 1, 6, Blocks.CRAFTING_TABLE);
		Pieces.path(b, door, 0, door, 1, 0);
		// The mound and its tunnel.
		b.fill(9, 1, 3, 12, 4, 9, p.stoneBase());
		b.clear(9, 1, 5, 11, 2, 6);
		b.set(12, 1, 5, Blocks.COAL_ORE);
		b.set(12, 2, 6, Blocks.IRON_ORE);
		b.set(10, 3, 5, Blocks.COPPER_ORE);
		Pieces.lantern(b, 11, 2, 6, true);
		BlockState rail = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST);
		b.fill(8, 1, 5, 11, 1, 5, rail);
		b.set(8, 1, 9, Blocks.RAW_IRON_BLOCK);
		b.set(9, 1, 10, Blocks.COAL_BLOCK);
		b.set(8, 1, 10, Blocks.RAW_COPPER_BLOCK);
		b.floor(9, 6, 11, 6, 1, "tunnel");
		b.resident(CharacterVariant.MINER, 8, 1, 1, 6, 5, 10, at(2, 1, 6), null, false);
	}

	/** Bandit Lookout: a hut on a stilted platform in the woods, up a ladder, with stolen goods stashed below. */
	static void banditLookout(Blueprint b) {
		Palette p = b.palette;
		int deck = 6;
		b.fill(2, deck, 2, 8, deck, 8, p.planks());
		for (int[] c : new int[][]{{2, 2}, {8, 2}, {2, 8}, {8, 8}}) {
			b.fill(c[0], 1, c[1], c[0], deck - 1, c[1], p.log());
		}
		b.fill(5, 1, 2, 5, deck - 1, 2, p.log());
		Pieces.ladder(b, 5, 1, deck, 1, Direction.NORTH);
		b.walls(2, 2, 8, 8, deck + 1, deck + 1, p.fence());
		b.clear(5, deck + 1, 2, 5, deck + 1, 2);
		Pieces.room(b, 3, 4, 7, 8, deck, 3, "lookout hut");
		Pieces.door(b, 5, deck + 1, 4, Direction.NORTH);
		Pieces.roof(b, Pieces.Roof.GABLE_X, 3, 4, 7, 8, deck + 3);
		b.bed(4, deck + 1, 6, p.bed());
		b.bed(6, deck + 1, 6, p.bed());
		b.chest(5, deck + 1, 7, "chests/bandit_lookout");
		Pieces.lantern(b, 8, deck + 2, 2, false);
		b.floor(3, 2, 7, 3, deck + 1, "porch");
		b.barrel(9, 1, 9, "chests/barrel_supplies");
		b.barrel(9, 2, 9, "chests/barrel_supplies");
		for (int i = 0; i < 2; i++) {
			b.resident(CharacterVariant.BANDIT, 5, 1, 4, 5, 5, 12, i == 0 ? at(4, deck + 1, 6) : at(6, deck + 1, 6), null, true);
		}
	}

	/** Statue of ItzBreadLoaf: a big loaf with a face, arms and a raised sword on a stone plinth, in a garden. */
	static void loafStatue(Blueprint b) {
		Palette p = b.palette;
		Pieces.garden(b, 0, 0, 10, 8, 0);
		b.fill(2, 0, 2, 8, 2, 6, p.bricks());
		b.fill(3, 3, 3, 7, 7, 5, Blocks.ORANGE_TERRACOTTA);
		b.fill(3, 8, 3, 7, 8, 5, Blocks.BROWN_TERRACOTTA);
		b.fill(4, 9, 3, 6, 9, 5, Blocks.BROWN_TERRACOTTA);
		b.set(4, 6, 3, Blocks.BLACK_CONCRETE);
		b.set(6, 6, 3, Blocks.BLACK_CONCRETE);
		for (int x = 4; x <= 6; x++) {
			b.set(x, 4, 3, Blocks.BLACK_CONCRETE);
		}
		b.set(2, 5, 4, Pieces.logAlong(p.log(), Direction.Axis.X));
		b.set(8, 5, 4, Pieces.logAlong(p.log(), Direction.Axis.X));
		b.set(9, 5, 4, Blocks.GOLD_BLOCK);
		b.fill(9, 6, 3, 9, 6, 5, Blocks.GOLD_BLOCK);
		b.fill(9, 7, 4, 9, 10, 4, Blocks.IRON_BLOCK);
		for (int[] c : new int[][]{{1, 1}, {9, 1}, {1, 7}, {9, 7}}) {
			lampPost(b, c[0], c[1]);
		}
		b.chest(5, 1, 7, "chests/loaf_statue");
		b.resident(CharacterVariant.VILLAGER, 5, 1, 1, 5, 4, 8);
	}

	/** Archery Range: a covered shooting line behind a rail, lanes down to targets on hay bales. */
	static void archeryRange(Blueprint b) {
		Palette p = b.palette;
		for (int x : new int[]{0, 8, 16}) {
			b.fill(x, 1, 1, x, 3, 1, p.fence());
		}
		b.fill(0, 4, 0, 16, 4, 2, p.slab());
		for (int x = 0; x <= 16; x++) {
			if (x != 8) {
				b.set(x, 1, 2, p.fence());
			}
		}
		for (int x : new int[]{2, 6, 10, 14}) {
			Pieces.path(b, x, 3, x, 8, 0);
			b.set(x, 1, 9, Blocks.HAY_BLOCK);
			b.set(x, 2, 9, Blocks.TARGET);
		}
		b.set(1, 1, 1, Blocks.FLETCHING_TABLE);
		b.barrel(2, 1, 1, "chests/barrel_supplies");
		b.chest(3, 1, 1, "chests/archery_range");
		Pieces.lantern(b, 4, 3, 1, true);
		Pieces.lantern(b, 12, 3, 1, true);
		b.floor(2, 3, 14, 8, 1, "range");
		b.resident(CharacterVariant.ARCHER, 5, 1, 1, 8, 4, 8);
	}

	/** A palm tree: a jungle-log trunk and a cross of leaves at the top. */
	private static void palm(Blueprint b, int x, int z) {
		b.fill(x, 1, z, x, 5, z, Blocks.JUNGLE_LOG);
		BlockState leaves = LilyBlueprints.leaves(Blocks.JUNGLE_LEAVES);
		for (int d = -2; d <= 2; d++) {
			b.set(x + d, 6, z, leaves);
			b.set(x, 6, z + d, leaves);
		}
		b.set(x, 7, z, leaves);
	}

	/** Desert Oasis Camp: a pond with reeds under two palms, a walk-in tent and a campfire in the sand. */
	static void desertOasisCamp(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 7, 7);
				if (d <= 3.0) {
					b.set(x, 0, z, Blocks.WATER);
					b.set(x, -1, z, Blocks.WATER);
				} else if (d <= 3.9 && b.random.nextInt(3) == 0 && (dist(x - 1, z, 7, 7) <= 3.0 || dist(x + 1, z, 7, 7) <= 3.0
						|| dist(x, z - 1, 7, 7) <= 3.0 || dist(x, z + 1, 7, 7) <= 3.0)) {
					// Reeds only where they touch the water (or they'd pop off).
					b.set(x, 0, z, Blocks.SAND);
					b.set(x, 1, z, Blocks.SUGAR_CANE);
				}
			}
		}
		palm(b, 2, 2);
		palm(b, 12, 12);
		Pieces.tent(b, 1, 8, 6, Blocks.WHITE_WOOL.defaultBlockState());
		b.bed(2, 1, 9, p.bed());
		b.bed(4, 1, 9, p.bed());
		b.chest(4, 1, 12, "chests/desert_oasis_camp");
		Pieces.lantern(b, 3, 1, 12, false);
		Pieces.campfire(b, 11, 1, 3);
		b.resident(CharacterVariant.BREADLOAF, 7, 1, 12, 7, 7, 10, at(2, 1, 9), null, true);
		b.resident(CharacterVariant.VILLAGER, 7, 1, 12, 7, 7, 10, at(4, 1, 9), null, true);
	}

	/** Snowy Bakehouse: a stone bakehouse with a brick oven and a smoking chimney, flour sacks, hay and a woodpile. */
	static void snowyBakehouse(Blueprint b) {
		Palette p = b.palette;
		Pieces.stoneRoom(b, 1, 1, 10, 8, 0, 4, "bakehouse");
		Pieces.roof(b, Pieces.Roof.GABLE_X, 1, 1, 10, 8, 4);
		Pieces.windowsAround(b, 1, 1, 10, 8, 2, 5);
		Pieces.door(b, 5, 1, 1, Direction.NORTH);
		b.fill(6, 1, 7, 8, 2, 7, Blocks.BRICKS);
		b.set(7, 1, 7, Blocks.FURNACE);
		b.fill(7, 1, 8, 7, 9, 8, Blocks.BRICKS);
		b.set(7, 10, 8, Blocks.CAMPFIRE);
		b.set(2, 1, 7, Blocks.SMOKER);
		b.chest(3, 1, 7, "chests/snowy_bakehouse");
		b.set(9, 1, 7, Blocks.HAY_BLOCK);
		b.set(9, 1, 6, Blocks.HAY_BLOCK);
		b.set(9, 1, 5, Blocks.WHITE_WOOL);
		for (int x = 3; x <= 6; x++) {
			b.set(x, 1, 4, Pieces.topSlab(p.slab()));
		}
		b.bed(2, 1, 2, p.bed());
		Pieces.lantern(b, 8, 1, 2, false);
		Pieces.lantern(b, 4, 2, 4, false);
		b.fill(8, 1, 10, 11, 2, 10, Pieces.logAlong(p.log(), Direction.Axis.X));
		lampPost(b, 3, 0);
		b.resident(CharacterVariant.BREADLOAF, 6, 1, 0, 5, 4, 8, at(2, 1, 2), null, false);
	}

	/**
	 * Captain's Manor: a two-floor manor. Downstairs a hall with a long table, a fireplace and armor dummies;
	 * stairs (railed at the top) up to the bedrooms and the captain's study.
	 */
	static void captainsManor(Blueprint b) {
		Palette p = b.palette;
		Pieces.room(b, 1, 2, 15, 12, 0, 4, "hall");
		Pieces.room(b, 1, 2, 15, 12, 4, 3, "upstairs");
		Pieces.roof(b, Pieces.Roof.GABLE_X, 1, 2, 15, 12, 7);
		Pieces.windowsAround(b, 1, 2, 15, 12, 2, 8);
		Pieces.windowsAround(b, 1, 2, 15, 12, 6, -1);
		Pieces.door(b, 8, 1, 2, Direction.NORTH);
		// Stairs up the east side, with a rail around the stairwell upstairs.
		Pieces.stairsRun(b, 13, 1, 10, Direction.NORTH, 4);
		for (int[] f : new int[][]{{12, 8}, {12, 9}, {12, 10}, {14, 8}, {14, 9}, {14, 10}, {13, 11}}) {
			b.set(f[0], 5, f[1], p.fence());
		}
		// Fireplace in the west wall, chimney up past the roof.
		b.fill(1, 1, 6, 1, 3, 8, Blocks.BRICKS);
		b.set(1, 1, 7, Blocks.CAMPFIRE);
		b.fill(0, 1, 7, 0, 13, 7, Blocks.BRICKS);
		// The hall.
		for (int x = 4; x <= 9; x++) {
			b.set(x, 1, 7, Pieces.topSlab(p.slab()));
			b.set(x, 1, 6, Pieces.stairs(p.stairs(), Direction.NORTH));
			b.set(x, 1, 8, Pieces.stairs(p.stairs(), Direction.SOUTH));
		}
		b.mob(3, 1, 3, "dummy");
		b.mob(5, 1, 3, "dummy");
		b.chest(14, 1, 3, "chests/captains_manor");
		Pieces.lantern(b, 11, 1, 3, false);
		Pieces.lantern(b, 3, 1, 11, false);
		// Upstairs: two bedrooms and the captain's study.
		b.bed(3, 5, 10, p.bed());
		b.bed(6, 5, 10, p.bed());
		b.chest(9, 5, 11, "chests/captains_manor");
		b.fill(2, 5, 3, 6, 6, 3, Blocks.BOOKSHELF);
		b.set(4, 5, 4, Blocks.LECTERN);
		b.set(8, 5, 3, Blocks.CARTOGRAPHY_TABLE);
		Pieces.lantern(b, 10, 5, 4, false);
		b.resident(CharacterVariant.CAPTAIN, 8, 1, 1, 8, 7, 10, at(3, 5, 10), null, true);
		b.resident(CharacterVariant.KNIGHT, 8, 1, 1, 8, 7, 10, at(6, 5, 10), null, true);
	}
}
