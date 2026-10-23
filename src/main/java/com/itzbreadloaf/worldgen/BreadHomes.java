package com.itzbreadloaf.worldgen;

import com.itzbreadloaf.character.CharacterVariant;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** ItzBreadLoaf's new homes: bakery, lookout tower, mining camp, jungle treehouse, desert bunker, igloo, dock, castle. */
final class BreadHomes {
	private BreadHomes() {
	}

	private static BlockState air() {
		return Blocks.AIR.defaultBlockState();
	}

	private static BlockState lantern(boolean hanging) {
		return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, hanging);
	}

	// ------------------------------------------------------------ Bread bakery

	static void bakery(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
		p.fill(level, box, 0, 1, 0, 10, 7, 10, air());
		p.fill(level, box, 0, 0, 0, 10, 0, 10, Blocks.GRASS_BLOCK.defaultBlockState());
		p.foundation(level, box, 0, 0, 10, 10, Blocks.COBBLESTONE.defaultBlockState());
		p.fill(level, box, 1, 0, 1, 9, 0, 9, Blocks.COBBLESTONE.defaultBlockState());
		p.walls(level, box, 1, 1, 9, 9, 1, 4, planks);
		for (int[] c : new int[][]{{1, 1}, {9, 1}, {1, 9}, {9, 9}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 5, c[1], Blocks.STRIPPED_OAK_LOG.defaultBlockState());
		}
		p.fill(level, box, 1, 5, 1, 9, 5, 9, planks);
		p.fill(level, box, 2, 6, 2, 8, 6, 8, Blocks.OAK_SLAB.defaultBlockState());
		p.door(level, box, 5, 1, 1, Blocks.OAK_DOOR);
		for (int[] w : new int[][]{{3, 1}, {7, 1}, {1, 5}, {9, 5}}) {
			p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), w[0], 2, w[1]);
		}
		// Ovens along the right wall, with a chimney.
		BlockState furnace = Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.WEST);
		BlockState smoker = Blocks.SMOKER.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.WEST);
		p.put(level, box, furnace, 8, 1, 8);
		p.put(level, box, furnace, 8, 1, 7);
		p.put(level, box, smoker, 8, 1, 6);
		p.put(level, box, smoker, 8, 1, 5);
		p.fill(level, box, 8, 6, 8, 8, 8, 8, Blocks.COBBLESTONE.defaultBlockState());
		// Hay for flour, a work table, his bed, the bread chest and a supply barrel.
		p.put(level, box, Blocks.HAY_BLOCK.defaultBlockState(), 2, 1, 2);
		p.put(level, box, Blocks.HAY_BLOCK.defaultBlockState(), 3, 1, 2);
		p.put(level, box, Blocks.HAY_BLOCK.defaultBlockState(), 2, 2, 2);
		p.put(level, box, Blocks.CRAFTING_TABLE.defaultBlockState(), 8, 1, 3);
		p.bed(level, box, 2, 1, 7, Blocks.ORANGE_BED);
		p.chest(level, box, random, 6, 1, 8, ModStructures.lootTable("chests/bread_bakery"));
		p.barrel(level, box, random, 2, 1, 4, ModStructures.BARREL_LOOT);
		p.put(level, box, lantern(true), 5, 4, 5);
		// Hay bales outside too.
		p.put(level, box, Blocks.HAY_BLOCK.defaultBlockState(), 0, 1, 3);
		p.put(level, box, Blocks.HAY_BLOCK.defaultBlockState(), 0, 1, 4);

		BlockPos spawn = p.pos(5, 1, 4);
		if (p.claimSpawn(spawn, box)) {
			BlockPos bed = p.pos(2, 1, 8);
			p.placeMarker(level, spawn, m -> m.addResident(CharacterVariant.BREADLOAF, spawn, spawn, 10, bed, null, null));
		}
	}

	// ------------------------------------------------------------ Lookout tower

	static void lookoutTower(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
		BlockState planks = Blocks.DARK_OAK_PLANKS.defaultBlockState();
		BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState();
		p.fill(level, box, 0, 1, 0, 6, 21, 6, air());
		p.fill(level, box, 0, 0, 0, 6, 0, 6, stone);
		p.foundation(level, box, 0, 0, 6, 6, stone);
		// Stone lower half, dark wood upper half.
		p.walls(level, box, 1, 1, 5, 5, 1, 8, stone);
		p.walls(level, box, 1, 1, 5, 5, 9, 16, planks);
		for (int[] c : new int[][]{{1, 1}, {5, 1}, {1, 5}, {5, 5}}) {
			p.fill(level, box, c[0], 9, c[1], c[0], 16, c[1], log);
		}
		p.fill(level, box, 3, 1, 1, 3, 2, 1, air());
		for (int y = 1; y <= 17; y++) {
			p.put(level, box, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH), 3, y, 4);
		}
		for (int x = 0; x <= 6; x++) {
			for (int z = 0; z <= 6; z++) {
				if (x != 3 || z != 4) {
					p.put(level, box, planks, x, 17, z);
				}
			}
		}
		for (int i = 1; i <= 5; i++) {
			for (int[] c : new int[][]{{i, 0}, {i, 6}, {0, i}, {6, i}}) {
				p.put(level, box, Blocks.DARK_OAK_FENCE.defaultBlockState(), c[0], 18, c[1]);
			}
		}
		for (int[] c : new int[][]{{0, 0}, {6, 0}, {0, 6}, {6, 6}}) {
			p.fill(level, box, c[0], 18, c[1], c[0], 20, c[1], log);
		}
		p.fill(level, box, 0, 21, 0, 6, 21, 6, Blocks.DARK_OAK_SLAB.defaultBlockState());
		p.chest(level, box, random, 5, 18, 1, ModStructures.lootTable("chests/lookout_tower"));
		p.put(level, box, lantern(true), 3, 20, 3);
		p.put(level, box, Blocks.WALL_TORCH.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), 3, 6, 2);

		BlockPos spawn = p.pos(2, 18, 2);
		if (p.claimSpawn(spawn, box)) {
			BlockPos post = p.pos(3, 18, 2);
			p.placeMarker(level, spawn, m -> m.addResident(CharacterVariant.ARCHER, spawn, post, 2, null, null, null));
		}
	}

	// ------------------------------------------------------------ Mining camp

	static void miningCamp(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		p.fill(level, box, 0, 1, 0, 14, 6, 12, air());
		p.fill(level, box, 0, 0, 0, 14, 0, 12, Blocks.COARSE_DIRT.defaultBlockState());
		p.foundation(level, box, 0, 0, 14, 12, Blocks.STONE.defaultBlockState());
		// A rocky hill with a tunnel cut into it.
		for (int x = 9; x <= 14; x++) {
			for (int z = 2; z <= 10; z++) {
				int height = 5 - Math.max(0, Math.abs(z - 6) - 1) - (x == 9 ? 1 : 0);
				for (int y = 1; y <= height; y++) {
					BlockState rock = random.nextInt(12) == 0 ? Blocks.COAL_ORE.defaultBlockState()
							: random.nextInt(20) == 0 ? Blocks.IRON_ORE.defaultBlockState()
							: random.nextInt(4) == 0 ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
					p.put(level, box, rock, x, y, z);
				}
			}
		}
		p.fill(level, box, 9, 1, 6, 14, 2, 6, air());
		// Rails from the camp into the tunnel.
		BlockState rail = Blocks.RAIL.defaultBlockState().setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);
		for (int x = 6; x <= 13; x++) {
			p.put(level, box, rail, x, 1, 6);
		}
		p.put(level, box, Blocks.OAK_FENCE.defaultBlockState(), 10, 1, 5);
		p.put(level, box, Blocks.OAK_FENCE.defaultBlockState(), 10, 1, 7);
		p.put(level, box, Blocks.OAK_PLANKS.defaultBlockState(), 10, 3, 6);
		p.put(level, box, Blocks.WALL_TORCH.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH), 12, 2, 7);
		p.chest(level, box, random, 14, 1, 6, ModStructures.lootTable("chests/mining_camp"));
		// Camp: a tent, a campfire, a barrel of supplies.
		p.smallTent(level, box, 1, 1, Blocks.BROWN_WOOL.defaultBlockState());
		p.smallTent(level, box, 1, 7, Blocks.GRAY_WOOL.defaultBlockState());
		p.put(level, box, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 6, 1, 3);
		p.barrel(level, box, random, 7, 1, 9, ModStructures.BARREL_LOOT);
		p.put(level, box, Blocks.CRAFTING_TABLE.defaultBlockState(), 7, 1, 10);

		BlockPos spawn = p.pos(6, 1, 5);
		if (p.claimSpawn(spawn, box)) {
			UUID group = p.group();
			BlockPos[] beds = {p.pos(2, 1, 3), p.pos(2, 1, 9)};
			p.placeMarker(level, spawn, m -> {
				for (BlockPos bed : beds) {
					m.addResident(CharacterVariant.MINER, spawn, spawn, 10, bed, group, null);
				}
			});
		}
	}

	// ------------------------------------------------------------ Jungle treehouse

	static void jungleTreehouse(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState log = Blocks.JUNGLE_LOG.defaultBlockState();
		BlockState planks = Blocks.JUNGLE_PLANKS.defaultBlockState();
		BlockState leaves = Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
		p.fill(level, box, 0, 1, 0, 12, 17, 12, air());
		p.fill(level, box, 0, 0, 0, 12, 0, 12, Blocks.GRASS_BLOCK.defaultBlockState());
		p.foundation(level, box, 0, 0, 12, 12, Blocks.DIRT.defaultBlockState());
		p.fill(level, box, 2, 10, 2, 10, 10, 10, planks);
		p.fill(level, box, 5, 1, 5, 6, 15, 6, log);
		// Hut on the platform.
		p.walls(level, box, 3, 3, 9, 9, 11, 13, Blocks.BAMBOO_PLANKS.defaultBlockState());
		p.fill(level, box, 3, 14, 3, 9, 14, 9, Blocks.JUNGLE_SLAB.defaultBlockState());
		p.door(level, box, 7, 11, 3, Blocks.JUNGLE_DOOR);
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 3, 12, 6);
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 9, 12, 6);
		p.bed(level, box, 8, 11, 6, Blocks.GREEN_BED);
		p.chest(level, box, random, 4, 11, 8, ModStructures.lootTable("chests/jungle_treehouse"));
		p.put(level, box, lantern(true), 6, 13, 7);
		// Rope ladder: ladders up the front of the trunk, through the platform into the hut.
		for (int y = 1; y <= 11; y++) {
			p.put(level, box, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH), 5, y, 4);
		}
		// Canopy and vines.
		for (int x = 0; x <= 12; x++) {
			for (int z = 0; z <= 12; z++) {
				for (int y = 15; y <= 16; y++) {
					if (random.nextInt(5) != 0 && !(y == 15 && x >= 3 && x <= 9 && z >= 3 && z <= 9 && x != 5 && x != 6)) {
						p.put(level, box, leaves, x, y, z);
					}
				}
				if ((x == 2 || x == 10 || z == 2 || z == 10) && random.nextInt(3) == 0) {
					int length = 2 + random.nextInt(5);
					for (int y = 9; y > 9 - length; y--) {
						p.put(level, box, Blocks.VINE.defaultBlockState().setValue(VineBlock.UP, true), x, y, z);
					}
				}
			}
		}

		BlockPos spawn = p.pos(5, 1, 2);
		if (p.claimSpawn(spawn, box)) {
			p.placeMarker(level, spawn, m -> m.addResident(CharacterVariant.BREADLOAF, spawn, spawn, 10, null, null, null));
		}
	}

	// ------------------------------------------------------------ Desert bunker

	static void desertBunker(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState wall = Blocks.CUT_SANDSTONE.defaultBlockState();
		BlockState sand = Blocks.SAND.defaultBlockState();
		// Dig it in: walls and floor three blocks down, the roof just above the sand.
		p.fill(level, box, 0, -4, 2, 10, 1, 12, wall);
		p.fill(level, box, 1, -3, 3, 9, 0, 11, air());
		p.fill(level, box, 0, 1, 2, 10, 1, 12, Blocks.SMOOTH_SANDSTONE.defaultBlockState());
		p.fill(level, box, 0, 2, 2, 10, 2, 12, air());
		p.fill(level, box, 0, 0, 0, 10, 0, 1, sand);
		// Entrance stairs going down from the front.
		BlockState stairs = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
		for (int i = 0; i < 3; i++) {
			int y = -i;
			int z = 1 + i;
			p.put(level, box, stairs, 5, y, z);
			p.fill(level, box, 5, y + 1, z, 5, y + 3, z, air());
		}
		p.fill(level, box, 5, 1, 2, 5, 1, 3, air());
		// Arrow trap: a narrow hall with a pressure plate and a dispenser in the wall beside it.
		p.fill(level, box, 4, -3, 4, 4, 0, 6, wall);
		p.fill(level, box, 6, -3, 4, 6, 0, 6, wall);
		p.put(level, box, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST), 4, -2, 5);
		BlockPos dispenser = p.pos(4, -2, 5);
		if (box.isInside(dispenser) && level.getBlockEntity(dispenser) instanceof DispenserBlockEntity trap) {
			trap.setItem(0, new ItemStack(Items.ARROW, 16));
		}
		p.put(level, box, Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), 5, -2, 5);
		// Back rooms: loot, his bed, a lamp.
		p.chest(level, box, random, 2, -2, 10, ModStructures.lootTable("chests/desert_bunker"));
		p.chest(level, box, random, 8, -2, 10, ModStructures.lootTable("chests/desert_bunker"));
		p.barrel(level, box, random, 8, -2, 8, ModStructures.BARREL_LOOT);
		p.bed(level, box, 2, -2, 7, Blocks.YELLOW_BED);
		p.put(level, box, lantern(true), 5, 0, 9);
		// An air vent sticking out of the sand.
		p.put(level, box, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 8, 2, 5);

		BlockPos spawn = p.pos(5, -2, 9);
		if (p.claimSpawn(spawn, box)) {
			BlockPos bed = p.pos(2, -2, 8);
			p.placeMarker(level, spawn, m -> m.addResident(CharacterVariant.BREADLOAF, spawn, spawn, 10, bed, null, null));
		}
	}

	// ------------------------------------------------------------ Igloo hideout

	static void igloo(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState snow = Blocks.SNOW_BLOCK.defaultBlockState();
		p.fill(level, box, 0, 1, 0, 8, 5, 8, air());
		p.fill(level, box, 0, 0, 0, 8, 0, 8, snow);
		p.foundation(level, box, 0, 0, 8, 8, Blocks.DIRT.defaultBlockState());
		// Dome.
		for (int x = 0; x <= 8; x++) {
			for (int z = 0; z <= 8; z++) {
				double d = Math.sqrt((x - 4) * (x - 4) + (z - 4) * (z - 4));
				if (d <= 3.6 && d >= 2.6) {
					p.fill(level, box, x, 1, z, x, 2, z, snow);
				}
				if (d <= 2.7 && d >= 1.6) {
					p.put(level, box, snow, x, 3, z);
				}
				if (d < 1.7) {
					p.put(level, box, snow, x, 4, z);
				}
			}
		}
		p.fill(level, box, 4, 1, 0, 4, 2, 1, air());
		p.put(level, box, Blocks.ICE.defaultBlockState(), 1, 2, 4);
		p.put(level, box, Blocks.ICE.defaultBlockState(), 7, 2, 4);
		p.bed(level, box, 2, 1, 4, Blocks.WHITE_BED);
		p.put(level, box, Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.WEST), 6, 1, 5);
		p.put(level, box, Blocks.REDSTONE_TORCH.defaultBlockState(), 6, 2, 3);
		// The secret: a trapdoor under the carpet, a ladder down, a storage room.
		p.put(level, box, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP), 4, 0, 4);
		p.put(level, box, Blocks.WHITE_CARPET.defaultBlockState(), 4, 1, 4);
		p.fill(level, box, 1, -7, 1, 7, -1, 7, Blocks.STONE_BRICKS.defaultBlockState());
		p.fill(level, box, 2, -6, 2, 6, -3, 6, air());
		for (int y = -6; y <= -1; y++) {
			p.put(level, box, air(), 4, y, 4);
			p.put(level, box, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH), 4, y, 4);
		}
		p.chest(level, box, random, 2, -6, 6, ModStructures.lootTable("chests/igloo_hideout"));
		p.chest(level, box, random, 6, -6, 6, ModStructures.lootTable("chests/igloo_hideout"));
		p.barrel(level, box, random, 2, -6, 2, ModStructures.BARREL_LOOT);
		p.put(level, box, Blocks.BREWING_STAND.defaultBlockState(), 6, -6, 2);
		p.put(level, box, lantern(true), 4, -3, 2);

		BlockPos spawn = p.pos(4, 1, 2);
		if (p.claimSpawn(spawn, box)) {
			BlockPos bed = p.pos(2, 1, 5);
			p.placeMarker(level, spawn, m -> m.addResident(CharacterVariant.BREADLOAF, spawn, spawn, 10, bed, null, null));
		}
	}

	// ------------------------------------------------------------ Fishing dock

	static void fishingDock(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
		p.fill(level, box, 0, 1, 0, 6, 5, 14, air());
		// Shed on the shore.
		p.fill(level, box, 0, 0, 0, 6, 0, 4, planks);
		p.foundation(level, box, 0, 0, 6, 4, Blocks.SAND.defaultBlockState());
		p.walls(level, box, 1, 0, 5, 3, 1, 3, Blocks.SPRUCE_PLANKS.defaultBlockState());
		p.fill(level, box, 1, 4, 0, 5, 4, 3, Blocks.SPRUCE_SLAB.defaultBlockState());
		p.door(level, box, 3, 1, 3, Blocks.SPRUCE_DOOR);
		p.chest(level, box, random, 2, 1, 1, ModStructures.lootTable("chests/fishing_dock"));
		p.barrel(level, box, random, 4, 1, 1, ModStructures.FISH_BARREL);
		// Pier out over the water, on log posts.
		p.fill(level, box, 2, 0, 4, 4, 0, 14, planks);
		for (int z = 5; z <= 14; z += 3) {
			p.column(level, box, Blocks.SPRUCE_LOG.defaultBlockState(), 2, -1, z);
			p.column(level, box, Blocks.SPRUCE_LOG.defaultBlockState(), 4, -1, z);
			p.put(level, box, Blocks.SPRUCE_FENCE.defaultBlockState(), 2, 1, z);
			p.put(level, box, Blocks.SPRUCE_FENCE.defaultBlockState(), 4, 1, z);
		}
		p.put(level, box, lantern(false), 2, 2, 14);
		p.barrel(level, box, random, 4, 1, 13, ModStructures.FISH_BARREL);

		BlockPos spawn = p.pos(3, 1, 8);
		if (p.claimSpawn(spawn, box)) {
			BlockPos end = p.pos(3, 1, 12);
			p.placeMarker(level, spawn, m -> m.addResident(CharacterVariant.FISHERMAN, spawn, end, 5, null, null, null));
		}
	}

	// ------------------------------------------------------------ Ruined Loaf Castle

	static void ruinedCastle(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		p.fill(level, box, 0, 1, 0, 26, 13, 26, air());
		p.fill(level, box, 0, 0, 0, 26, 0, 26, Blocks.GRASS_BLOCK.defaultBlockState());
		p.foundation(level, box, 0, 0, 26, 26, Blocks.COBBLESTONE.defaultBlockState());
		// Outer walls, crumbling: higher in places, gaps in others.
		for (int i = 0; i <= 26; i++) {
			for (int[] c : new int[][]{{i, 0}, {i, 26}, {0, i}, {26, i}}) {
				int height = 3 + random.nextInt(4);
				for (int y = 1; y <= height; y++) {
					if (random.nextInt(8) != 0) {
						p.put(level, box, ruinStone(random), c[0], y, c[1]);
					}
				}
			}
		}
		// Gate.
		p.fill(level, box, 12, 1, 0, 14, 4, 0, air());
		// Corner towers, broken at different heights.
		for (int[] c : new int[][]{{0, 0}, {22, 0}, {0, 22}, {22, 22}}) {
			int top = 7 + random.nextInt(4);
			for (int y = 1; y <= top; y++) {
				for (int x = 0; x <= 4; x++) {
					for (int z = 0; z <= 4; z++) {
						boolean edge = x == 0 || x == 4 || z == 0 || z == 4;
						if (edge && (y < top - 1 || random.nextBoolean())) {
							p.put(level, box, ruinStone(random), c[0] + x, y, c[1] + z);
						}
					}
				}
			}
		}
		// The keep: half its roof fell in.
		p.walls(level, box, 9, 13, 17, 21, 1, 7, Blocks.STONE_BRICKS.defaultBlockState());
		for (int x = 9; x <= 17; x++) {
			for (int z = 13; z <= 21; z++) {
				if (random.nextInt(3) != 0) {
					p.put(level, box, Blocks.STONE_BRICK_SLAB.defaultBlockState(), x, 8, z);
				}
				if (random.nextInt(10) == 0) {
					p.put(level, box, random.nextBoolean() ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.GRAVEL.defaultBlockState(), x, 1, z);
				}
			}
		}
		p.fill(level, box, 12, 1, 13, 14, 3, 13, air());
		p.fill(level, box, 10, 0, 14, 16, 0, 20, Blocks.POLISHED_ANDESITE.defaultBlockState());
		p.put(level, box, Blocks.RED_CARPET.defaultBlockState(), 13, 1, 17);
		p.put(level, box, Blocks.RED_CARPET.defaultBlockState(), 13, 1, 18);
		p.chest(level, box, random, 10, 1, 20, ModStructures.lootTable("chests/ruined_loaf_castle"));
		p.chest(level, box, random, 16, 1, 20, ModStructures.lootTable("chests/ruined_loaf_castle"));
		p.chest(level, box, random, 13, 1, 20, ModStructures.lootTable("chests/ruined_loaf_castle"));
		p.barrel(level, box, random, 16, 1, 14, ModStructures.BARREL_LOOT);
		p.put(level, box, Blocks.LANTERN.defaultBlockState(), 10, 1, 14);
		// Rubble in the courtyard.
		for (int i = 0; i < 25; i++) {
			int x = 2 + random.nextInt(23);
			int z = 2 + random.nextInt(10);
			p.put(level, box, random.nextBoolean() ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState(), x, 1, z);
		}
		p.put(level, box, Blocks.RED_BANNER.defaultBlockState(), 11, 5, 0);

		BlockPos spawn = p.pos(13, 1, 8);
		if (p.claimSpawn(spawn, box)) {
			UUID group = p.group();
			BlockPos home = p.pos(13, 1, 10);
			int residents = 3 + random.nextInt(3);
			p.placeMarker(level, spawn, m -> {
				for (int i = 0; i < residents; i++) {
					CharacterVariant type = i == 0 ? CharacterVariant.CAPTAIN : i % 2 == 0 ? CharacterVariant.BREADLOAF : CharacterVariant.KNIGHT;
					m.addResident(type, spawn, home, 12, null, group, null);
				}
			});
		}
	}

	private static BlockState ruinStone(RandomSource random) {
		int roll = random.nextInt(10);
		Block block = roll < 5 ? Blocks.STONE_BRICKS : roll < 7 ? Blocks.CRACKED_STONE_BRICKS : roll < 9 ? Blocks.MOSSY_STONE_BRICKS : Blocks.COBBLESTONE;
		return block.defaultBlockState();
	}
}
