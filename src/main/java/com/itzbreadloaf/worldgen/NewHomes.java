package com.itzbreadloaf.worldgen;

import static com.itzbreadloaf.worldgen.SpecialHomes.FLOWERS;
import static com.itzbreadloaf.worldgen.SpecialHomes.air;
import static com.itzbreadloaf.worldgen.SpecialHomes.hanging;
import static com.itzbreadloaf.worldgen.SpecialHomes.one;
import static com.itzbreadloaf.worldgen.SpecialHomes.site;

import com.itzbreadloaf.character.CharacterVariant;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * The remake's new structures. Local coordinates: y = 0 is the ground layer, +z is "back" (doors face -z).
 * A ladder FACING NORTH at z hangs on the block at z + 1.
 */
final class NewHomes {
	private NewHomes() {
	}

	// ------------------------------------------------------------ small helpers

	private static BlockState ladder() {
		return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
	}

	private static BlockState stair(Block block, Direction facing) {
		return block.defaultBlockState().setValue(StairBlock.FACING, facing);
	}

	private static BlockState topSlab(Block block) {
		return block.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
	}

	private static BlockState leaves(Block block) {
		return block.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
	}

	/** A gable-free roof: a slab layer over the walls and a smaller one on top. */
	private static void roof(HomePiece p, WorldGenLevel level, BoundingBox box, int x0, int z0, int x1, int z1, int y, Block slab) {
		p.fill(level, box, x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, slab.defaultBlockState());
		if (x1 - x0 > 2 && z1 - z0 > 2) {
			p.fill(level, box, x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, slab.defaultBlockState());
		}
	}

	/** Two residents (or more) sharing one marker, all in one group so they defend each other. */
	private static void group(HomePiece p, WorldGenLevel level, BoundingBox box, int x, int y, int z, int homeX, int homeZ,
			int radius, CharacterVariant... types) {
		BlockPos spawn = p.pos(x, y, z);
		if (!p.claimSpawn(spawn, box)) {
			return;
		}
		UUID id = p.group();
		BlockPos home = p.pos(homeX, y, homeZ);
		p.placeMarker(level, spawn, m -> {
			for (CharacterVariant type : types) {
				m.addResident(type, spawn, home, radius, null, id, null);
			}
		});
	}

	// ============================================================ ItzBreadLoaf

	/** Swamp Hut on stilts: a mangrove shack over the water with a porch and a ladder. */
	static void swampHut(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		p.fill(level, box, 0, 3, 0, 10, 9, 10, air());
		BlockState log = Blocks.MANGROVE_LOG.defaultBlockState();
		for (int[] c : new int[][]{{2, 2}, {8, 2}, {2, 8}, {8, 8}, {5, 2}}) {
			p.column(level, box, log, c[0], 3, c[1]);
		}
		p.fill(level, box, 2, 3, 1, 8, 3, 8, Blocks.MANGROVE_PLANKS.defaultBlockState());
		p.walls(level, box, 2, 2, 8, 8, 4, 6, Blocks.MANGROVE_PLANKS.defaultBlockState());
		for (int[] c : new int[][]{{2, 2}, {8, 2}, {2, 8}, {8, 8}}) {
			p.fill(level, box, c[0], 4, c[1], c[0], 6, c[1], log);
		}
		roof(p, level, box, 2, 2, 8, 8, 7, Blocks.MANGROVE_SLAB);
		p.door(level, box, 5, 4, 2, Blocks.MANGROVE_DOOR);
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 2, 5, 5);
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 8, 5, 5);
		// Porch rail and the ladder down to the water.
		p.fill(level, box, 2, 4, 1, 4, 4, 1, Blocks.MANGROVE_FENCE.defaultBlockState());
		p.fill(level, box, 6, 4, 1, 8, 4, 1, Blocks.MANGROVE_FENCE.defaultBlockState());
		p.column(level, box, log, 5, 2, 2);
		for (int y = 0; y <= 3; y++) {
			p.put(level, box, ladder(), 5, y, 1);
		}
		p.put(level, box, air(), 5, 4, 1);
		// Inside: bed, cauldron, crafting, loot.
		p.bed(level, box, 3, 4, 6, Blocks.GREEN_BED);
		p.put(level, box, Blocks.WATER_CAULDRON.defaultBlockState(), 7, 4, 7);
		p.put(level, box, Blocks.CRAFTING_TABLE.defaultBlockState(), 7, 4, 3);
		p.chest(level, box, random, 7, 4, 5, ModStructures.lootTable("chests/swamp_hut"));
		p.barrel(level, box, random, 3, 4, 3, ModStructures.BARREL_LOOT);
		p.put(level, box, hanging(), 5, 6, 5);
		p.put(level, box, Blocks.LILY_PAD.defaultBlockState(), 1, 1, 4);
		one(p, level, box, CharacterVariant.BREADLOAF, 5, 4, 4, 5, 5, 10, p.pos(3, 4, 6), null);
	}

	/** Mountain Outpost on the peaks: a stone tower with a lookout and a campfire on top. Archer and Knight. */
	static void mountainOutpost(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		p.fill(level, box, 0, 1, 0, 8, 11, 8, air());
		p.fill(level, box, 0, 0, 0, 8, 0, 8, Blocks.STONE_BRICKS.defaultBlockState());
		p.foundation(level, box, 0, 0, 8, 8, Blocks.STONE.defaultBlockState());
		p.walls(level, box, 1, 1, 7, 7, 1, 8, Blocks.STONE_BRICKS.defaultBlockState());
		for (int[] c : new int[][]{{1, 1}, {7, 1}, {1, 7}, {7, 7}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 8, c[1], Blocks.POLISHED_ANDESITE.defaultBlockState());
		}
		p.fill(level, box, 1, 4, 1, 7, 4, 7, Blocks.SPRUCE_PLANKS.defaultBlockState());
		p.fill(level, box, 1, 9, 1, 7, 9, 7, Blocks.STONE_BRICKS.defaultBlockState());
		p.door(level, box, 4, 1, 1, Blocks.SPRUCE_DOOR);
		for (int y : new int[]{2, 6}) {
			p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 1, y, 4);
			p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 7, y, 4);
		}
		// Ladder up through both floors.
		p.fill(level, box, 4, 1, 7, 4, 9, 7, Blocks.STONE_BRICKS.defaultBlockState());
		for (int y = 1; y <= 9; y++) {
			p.put(level, box, ladder(), 4, y, 6);
		}
		// Crenellations and a campfire on the roof.
		for (int i = 1; i <= 7; i += 2) {
			for (int[] c : new int[][]{{i, 1}, {i, 7}, {1, i}, {7, i}}) {
				p.put(level, box, Blocks.STONE_BRICK_WALL.defaultBlockState(), c[0], 10, c[1]);
			}
		}
		p.put(level, box, Blocks.CAMPFIRE.defaultBlockState(), 3, 10, 3);
		p.bed(level, box, 2, 5, 5, Blocks.GRAY_BED);
		p.bed(level, box, 6, 1, 5, Blocks.GRAY_BED);
		p.put(level, box, Blocks.FURNACE.defaultBlockState(), 2, 1, 6);
		p.chest(level, box, random, 2, 1, 2, ModStructures.lootTable("chests/mountain_outpost"));
		p.barrel(level, box, random, 6, 5, 2, ModStructures.BARREL_LOOT);
		p.put(level, box, Blocks.LANTERN.defaultBlockState(), 6, 1, 2);
		p.put(level, box, Blocks.LANTERN.defaultBlockState(), 2, 5, 2);
		group(p, level, box, 3, 1, 4, 4, 4, 10, CharacterVariant.ARCHER, CharacterVariant.KNIGHT);
	}

	/** Badlands Mineshaft Saloon: a terracotta saloon with a bar, tables, and a mine shaft going down in the back. */
	static void badlandsSaloon(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 14, 8, 12, Blocks.RED_SAND.defaultBlockState());
		p.fill(level, box, 1, 0, 1, 13, 0, 11, Blocks.DARK_OAK_PLANKS.defaultBlockState());
		p.walls(level, box, 1, 1, 13, 11, 1, 5, Blocks.ORANGE_TERRACOTTA.defaultBlockState());
		for (int[] c : new int[][]{{1, 1}, {13, 1}, {1, 11}, {13, 11}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 5, c[1], Blocks.DARK_OAK_LOG.defaultBlockState());
		}
		p.fill(level, box, 1, 6, 1, 13, 6, 11, Blocks.DARK_OAK_SLAB.defaultBlockState());
		// The tall false front with a "sign" stripe.
		p.fill(level, box, 1, 6, 1, 13, 8, 1, Blocks.DARK_OAK_PLANKS.defaultBlockState());
		p.fill(level, box, 4, 7, 1, 10, 7, 1, Blocks.YELLOW_TERRACOTTA.defaultBlockState());
		// Swinging-door gap and windows.
		p.fill(level, box, 6, 1, 1, 8, 2, 1, air());
		p.put(level, box, Blocks.OAK_FENCE_GATE.defaultBlockState(), 6, 1, 1);
		p.put(level, box, Blocks.OAK_FENCE_GATE.defaultBlockState(), 8, 1, 1);
		for (int x : new int[]{3, 11}) {
			p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), x, 2, 1);
		}
		// Bar along the back: upside-down stairs as the counter, stools in front.
		for (int x = 2; x <= 8; x++) {
			p.put(level, box, stair(Blocks.DARK_OAK_STAIRS, Direction.NORTH).setValue(StairBlock.HALF, Half.TOP), x, 1, 9);
			p.put(level, box, Blocks.BARREL.defaultBlockState(), x, 1, 10);
		}
		for (int x = 3; x <= 7; x += 2) {
			p.put(level, box, Blocks.OAK_FENCE.defaultBlockState(), x, 1, 7);
			p.put(level, box, Blocks.BROWN_CARPET.defaultBlockState(), x, 2, 7);
		}
		// Tables.
		for (int[] t : new int[][]{{3, 4}, {10, 4}}) {
			p.put(level, box, topSlab(Blocks.DARK_OAK_SLAB), t[0], 1, t[1]);
			p.put(level, box, stair(Blocks.DARK_OAK_STAIRS, Direction.EAST), t[0] - 1, 1, t[1]);
			p.put(level, box, stair(Blocks.DARK_OAK_STAIRS, Direction.WEST), t[0] + 1, 1, t[1]);
		}
		p.chest(level, box, random, 2, 1, 6, ModStructures.lootTable("chests/badlands_saloon"));
		p.barrel(level, box, random, 9, 1, 10, ModStructures.BARREL_LOOT);
		p.put(level, box, hanging(), 5, 5, 5);
		p.put(level, box, hanging(), 10, 5, 7);
		// Mine shaft in the back corner: a ladder down to a little rail room with the real loot.
		p.fill(level, box, 11, -8, 8, 12, 0, 10, Blocks.OAK_PLANKS.defaultBlockState());
		p.fill(level, box, 11, -7, 9, 11, 0, 9, air());
		for (int y = -7; y <= 0; y++) {
			p.put(level, box, ladder(), 11, y, 9);
		}
		p.fill(level, box, 4, -8, 6, 12, -8, 10, Blocks.OAK_PLANKS.defaultBlockState());
		p.fill(level, box, 5, -7, 7, 10, -5, 9, air());
		p.fill(level, box, 5, -7, 8, 10, -7, 8, Blocks.RAIL.defaultBlockState());
		p.fill(level, box, 5, -4, 7, 10, -4, 9, Blocks.OAK_PLANKS.defaultBlockState());
		p.chest(level, box, random, 5, -7, 9, ModStructures.lootTable("chests/badlands_saloon"));
		p.put(level, box, Blocks.LANTERN.defaultBlockState(), 7, -7, 7);
		group(p, level, box, 7, 1, 5, 7, 6, 10, CharacterVariant.MINER, CharacterVariant.BREADLOAF);
	}

	/** Cliffside Cave Base: a trapdoor hidden among bushes leads down a ladder to a lit stone room. */
	static void cliffsideBase(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState wall = Blocks.STONE_BRICKS.defaultBlockState();
		p.fill(level, box, 1, -8, 1, 11, -2, 11, wall);
		p.fill(level, box, 2, -7, 2, 10, -3, 10, air());
		p.fill(level, box, 2, -8, 2, 10, -8, 10, Blocks.POLISHED_ANDESITE.defaultBlockState());
		// The hidden entrance: a closed trapdoor in the grass, a ladder down, bushes all around it.
		p.fill(level, box, 6, -7, 7, 6, -1, 7, wall);
		for (int y = -7; y <= -1; y++) {
			p.put(level, box, ladder(), 6, y, 6);
		}
		p.put(level, box, Blocks.SPRUCE_TRAPDOOR.defaultBlockState(), 6, 0, 6);
		for (int[] b : new int[][]{{5, 5}, {6, 5}, {7, 5}, {5, 6}, {7, 6}, {5, 7}, {7, 7}}) {
			p.put(level, box, leaves(Blocks.OAK_LEAVES), b[0], 1, b[1]);
		}
		p.put(level, box, leaves(Blocks.AZALEA_LEAVES), 6, 2, 5);
		// Inside: bed, crafting, furnace, armor stand, lots of light.
		p.bed(level, box, 3, -7, 8, Blocks.CYAN_BED);
		p.put(level, box, Blocks.CRAFTING_TABLE.defaultBlockState(), 9, -7, 9);
		p.put(level, box, Blocks.FURNACE.defaultBlockState(), 9, -7, 8);
		p.put(level, box, Blocks.SMITHING_TABLE.defaultBlockState(), 9, -7, 3);
		p.chest(level, box, random, 3, -7, 3, ModStructures.lootTable("chests/cliffside_base"));
		p.chest(level, box, random, 4, -7, 3, ModStructures.lootTable("chests/cliffside_base"));
		p.barrel(level, box, random, 9, -7, 5, ModStructures.BARREL_LOOT);
		for (int[] l : new int[][]{{4, 6}, {8, 4}, {8, 9}}) {
			p.put(level, box, hanging(), l[0], -3, l[1]);
		}
		p.put(level, box, Blocks.RED_CARPET.defaultBlockState(), 5, -7, 5);
		p.put(level, box, Blocks.RED_CARPET.defaultBlockState(), 6, -7, 5);
		BlockPos bed = p.pos(3, -7, 8);
		one(p, level, box, CharacterVariant.BREADLOAF, 6, -7, 4, 6, 6, 4, bed, null);
	}

	// ============================================================ ImLilyLol

	/** Floating Island Cottage: a little sky island with a cherry cottage, held up by a rope-ladder pillar. */
	static void floatingIsland(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		int top = 22;
		for (int y = 14; y <= top; y++) {
			double radius = Math.min(7.0, (y - 13) * 0.85);
			for (int x = 0; x <= 14; x++) {
				for (int z = 0; z <= 14; z++) {
					double d = Math.sqrt((x - 7) * (x - 7) + (z - 7) * (z - 7));
					if (d > radius + random.nextFloat() * 0.6) {
						continue;
					}
					BlockState state = y == top ? Blocks.GRASS_BLOCK.defaultBlockState()
							: y >= top - 2 ? Blocks.DIRT.defaultBlockState()
							: random.nextInt(5) == 0 ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
					p.put(level, box, state, x, y, z);
				}
			}
		}
		p.fill(level, box, 0, top + 1, 0, 14, top + 7, 14, air());
		// Cottage.
		p.fill(level, box, 4, top, 5, 10, top, 10, Blocks.CHERRY_PLANKS.defaultBlockState());
		p.walls(level, box, 4, 5, 10, 10, top + 1, top + 3, Blocks.CHERRY_PLANKS.defaultBlockState());
		for (int[] c : new int[][]{{4, 5}, {10, 5}, {4, 10}, {10, 10}}) {
			p.fill(level, box, c[0], top + 1, c[1], c[0], top + 3, c[1], Blocks.CHERRY_LOG.defaultBlockState());
		}
		roof(p, level, box, 4, 5, 10, 10, top + 4, Blocks.CHERRY_SLAB);
		p.door(level, box, 7, top + 1, 5, Blocks.CHERRY_DOOR);
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 4, top + 2, 7);
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 10, top + 2, 8);
		p.bed(level, box, 5, top + 1, 8, Blocks.PINK_BED);
		p.chest(level, box, random, 9, top + 1, 9, ModStructures.lootTable("chests/floating_island_cottage"));
		p.put(level, box, Blocks.BOOKSHELF.defaultBlockState(), 9, top + 1, 6);
		p.put(level, box, hanging(), 7, top + 3, 8);
		p.put(level, box, Blocks.POTTED_CHERRY_SAPLING.defaultBlockState(), 5, top + 1, 6);
		// A small cherry tree and petals.
		p.fill(level, box, 11, top + 1, 3, 11, top + 4, 3, Blocks.CHERRY_LOG.defaultBlockState());
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (Math.abs(dx) + Math.abs(dz) <= 3) {
					p.put(level, box, leaves(Blocks.CHERRY_LEAVES), 11 + dx, top + 5, 3 + dz);
				}
			}
		}
		for (int i = 0; i < 8; i++) {
			p.put(level, box, Blocks.PINK_PETALS.defaultBlockState(), 2 + random.nextInt(11), top + 1, 1 + random.nextInt(4));
		}
		// The way up: a log pillar from the ground with a ladder all the way through the island.
		for (int y = 1; y <= top; y++) {
			p.put(level, box, Blocks.SPRUCE_LOG.defaultBlockState(), 7, y, 3);
			p.put(level, box, ladder(), 7, y, 2);
		}
		p.column(level, box, Blocks.SPRUCE_LOG.defaultBlockState(), 7, 0, 3);
		p.put(level, box, air(), 7, top + 1, 2);
		BlockPos bed = p.pos(5, top + 1, 8);
		one(p, level, box, CharacterVariant.LILY, 7, top + 1, 7, 7, 7, 6, bed, null);
	}

	/** Lantern Garden: lantern posts, benches and a lily pond, lovely at night. Gardener Lily. */
	static void lanternGarden(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 12, 5, 12, Blocks.GRASS_BLOCK.defaultBlockState());
		p.fill(level, box, 6, 0, 0, 6, 0, 12, Blocks.MOSS_BLOCK.defaultBlockState());
		p.fill(level, box, 0, 0, 6, 12, 0, 6, Blocks.MOSS_BLOCK.defaultBlockState());
		p.fill(level, box, 5, -1, 5, 7, -1, 7, Blocks.CLAY.defaultBlockState());
		p.fill(level, box, 5, 0, 5, 7, 0, 7, Blocks.WATER.defaultBlockState());
		p.put(level, box, Blocks.LILY_PAD.defaultBlockState(), 5, 1, 7);
		p.put(level, box, Blocks.LILY_PAD.defaultBlockState(), 7, 1, 5);
		for (int[] c : new int[][]{{2, 2}, {10, 2}, {2, 10}, {10, 10}, {6, 1}, {6, 11}, {1, 6}, {11, 6}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 2, c[1], Blocks.DARK_OAK_FENCE.defaultBlockState());
			p.put(level, box, Blocks.LANTERN.defaultBlockState(), c[0], 3, c[1]);
		}
		// Benches facing the pond.
		p.fill(level, box, 4, 1, 3, 8, 1, 3, stair(Blocks.SPRUCE_STAIRS, Direction.NORTH));
		p.fill(level, box, 4, 1, 9, 8, 1, 9, stair(Blocks.SPRUCE_STAIRS, Direction.SOUTH));
		p.put(level, box, air(), 6, 1, 3);
		p.put(level, box, air(), 6, 1, 9);
		for (int x = 0; x <= 12; x++) {
			for (int z = 0; z <= 12; z++) {
				boolean free = x != 6 && z != 6 && (x < 4 || x > 8 || z < 3 || z > 9);
				if (free && random.nextInt(3) == 0) {
					p.put(level, box, FLOWERS[random.nextInt(FLOWERS.length)].defaultBlockState(), x, 1, z);
				}
			}
		}
		p.put(level, box, Blocks.FLOWERING_AZALEA.defaultBlockState(), 0, 1, 0);
		p.put(level, box, Blocks.FLOWERING_AZALEA.defaultBlockState(), 12, 1, 12);
		p.chest(level, box, random, 0, 1, 12, ModStructures.lootTable("chests/lantern_garden"));
		one(p, level, box, CharacterVariant.GARDENER, 6, 1, 2, 6, 6, 8, null, null);
	}

	/** Treetop Library: a bookshelf room on a platform between four tall birch trunks. Bookworm Lily. */
	static void treetopLibrary(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 12, 21, 12, Blocks.GRASS_BLOCK.defaultBlockState());
		BlockState birch = Blocks.BIRCH_LOG.defaultBlockState();
		int floor = 12;
		for (int[] t : new int[][]{{1, 1}, {11, 1}, {1, 11}, {11, 11}}) {
			p.fill(level, box, t[0], 1, t[1], t[0], 18, t[1], birch);
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					for (int y = 17; y <= 20; y++) {
						if (Math.abs(dx) + Math.abs(dz) + Math.abs(y - 18) <= 3) {
							p.put(level, box, leaves(Blocks.BIRCH_LEAVES), t[0] + dx, y, t[1] + dz);
						}
					}
				}
			}
		}
		p.fill(level, box, 1, floor, 1, 11, floor, 11, Blocks.BIRCH_PLANKS.defaultBlockState());
		p.walls(level, box, 2, 2, 10, 10, floor + 1, floor + 3, Blocks.BOOKSHELF.defaultBlockState());
		p.fill(level, box, 2, floor + 4, 2, 10, floor + 4, 10, Blocks.BIRCH_SLAB.defaultBlockState());
		for (int[] w : new int[][]{{2, 6}, {10, 6}, {6, 10}}) {
			p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), w[0], floor + 2, w[1]);
		}
		p.door(level, box, 6, floor + 1, 2, Blocks.BIRCH_DOOR);
		p.fill(level, box, 1, floor + 1, 1, 11, floor + 1, 1, Blocks.BIRCH_FENCE.defaultBlockState());
		p.put(level, box, air(), 6, floor + 1, 1);
		// The ladder: a birch post in the middle of the porch, ladder on its front.
		p.fill(level, box, 6, 1, 0, 6, floor, 0, Blocks.BIRCH_LOG.defaultBlockState());
		p.fill(level, box, 6, 1, 1, 6, floor + 1, 1, air());
		for (int y = 1; y <= floor; y++) {
			p.put(level, box, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH), 6, y, 1);
		}
		// Reading room.
		p.put(level, box, Blocks.LECTERN.defaultBlockState(), 6, floor + 1, 8);
		p.put(level, box, Blocks.ENCHANTING_TABLE.defaultBlockState(), 4, floor + 1, 8);
		p.put(level, box, stair(Blocks.BIRCH_STAIRS, Direction.SOUTH), 6, floor + 1, 6);
		p.bed(level, box, 8, floor + 1, 7, Blocks.LIGHT_BLUE_BED);
		p.chest(level, box, random, 3, floor + 1, 3, ModStructures.lootTable("chests/treetop_library"));
		p.put(level, box, hanging(), 6, floor + 3, 6);
		BlockPos bed = p.pos(8, floor + 1, 7);
		one(p, level, box, CharacterVariant.BOOKWORM, 5, floor + 1, 5, 6, 6, 4, bed, null);
	}

	/** Starwatch Observatory (my idea): a stone tower room with a glass dome and a copper telescope. Bookworm Lily. */
	static void observatory(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 10, 11, 10, Blocks.GRASS_BLOCK.defaultBlockState());
		p.fill(level, box, 1, 0, 1, 9, 0, 9, Blocks.STONE_BRICKS.defaultBlockState());
		p.walls(level, box, 1, 1, 9, 9, 1, 4, Blocks.STONE_BRICKS.defaultBlockState());
		for (int x = 1; x <= 9; x++) {
			for (int z = 1; z <= 9; z++) {
				for (int y = 5; y <= 9; y++) {
					double d = Math.sqrt((x - 5) * (x - 5) + (y - 4) * (y - 4) * 1.2 + (z - 5) * (z - 5));
					if (d > 3.6 && d <= 4.6) {
						p.put(level, box, Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), x, y, z);
					}
				}
			}
		}
		p.door(level, box, 5, 1, 1, Blocks.SPRUCE_DOOR);
		// Telescope: copper on a stand, pointing up through the dome.
		p.put(level, box, Blocks.LIGHTNING_ROD.defaultBlockState(), 5, 1, 6);
		p.put(level, box, Blocks.COPPER_BLOCK.defaultBlockState(), 5, 2, 6);
		p.put(level, box, Blocks.LIGHTNING_ROD.defaultBlockState(), 5, 3, 6);
		p.put(level, box, Blocks.LECTERN.defaultBlockState(), 3, 1, 7);
		p.fill(level, box, 2, 1, 8, 4, 2, 8, Blocks.BOOKSHELF.defaultBlockState());
		p.bed(level, box, 7, 1, 6, Blocks.BLUE_BED);
		p.chest(level, box, random, 8, 1, 3, ModStructures.lootTable("chests/starwatch_observatory"));
		p.put(level, box, Blocks.LANTERN.defaultBlockState(), 2, 1, 2);
		BlockPos bed = p.pos(7, 1, 6);
		one(p, level, box, CharacterVariant.BOOKWORM, 4, 1, 4, 5, 5, 8, bed, null);
	}

	// ============================================================ Madison

	/** Riverside Mill: a stone-and-oak mill on the river with a big water wheel. Madison. */
	static void riversideMill(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		p.fill(level, box, 0, 1, 0, 12, 9, 10, air());
		p.fill(level, box, 1, 0, 1, 9, 0, 9, Blocks.STONE_BRICKS.defaultBlockState());
		p.foundation(level, box, 1, 1, 9, 9, Blocks.COBBLESTONE.defaultBlockState());
		p.walls(level, box, 1, 1, 9, 9, 1, 2, Blocks.COBBLESTONE.defaultBlockState());
		p.walls(level, box, 1, 1, 9, 9, 3, 5, Blocks.OAK_PLANKS.defaultBlockState());
		for (int[] c : new int[][]{{1, 1}, {9, 1}, {1, 9}, {9, 9}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 5, c[1], Blocks.STRIPPED_OAK_LOG.defaultBlockState());
		}
		roof(p, level, box, 1, 1, 9, 9, 6, Blocks.SPRUCE_SLAB);
		p.door(level, box, 5, 1, 1, Blocks.OAK_DOOR);
		for (int z : new int[]{3, 7}) {
			p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 1, 3, z);
		}
		// Water wheel on the east side: an axle through the wall and a ring of planks with spokes.
		p.fill(level, box, 9, 3, 5, 11, 3, 5, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
		for (int dy = -3; dy <= 3; dy++) {
			for (int dz = -3; dz <= 3; dz++) {
				double r = Math.sqrt(dy * dy + dz * dz);
				if (r > 2.5 && r < 3.5) {
					p.put(level, box, Blocks.SPRUCE_PLANKS.defaultBlockState(), 11, 3 + dy, 5 + dz);
				} else if ((dy == 0 || dz == 0) && r > 0.5 && r < 2.6) {
					p.put(level, box, Blocks.SPRUCE_FENCE.defaultBlockState(), 11, 3 + dy, 5 + dz);
				}
			}
		}
		// Inside: millstone, grain, pantry.
		p.put(level, box, Blocks.GRINDSTONE.defaultBlockState(), 7, 1, 5);
		p.fill(level, box, 2, 1, 7, 3, 2, 8, Blocks.HAY_BLOCK.defaultBlockState());
		p.chest(level, box, random, 7, 1, 8, ModStructures.lootTable("chests/riverside_mill"));
		p.chest(level, box, random, 8, 1, 8, ModStructures.MADISON_PANTRY);
		p.barrel(level, box, random, 8, 1, 2, ModStructures.BARREL_LOOT);
		p.put(level, box, Blocks.SMOKER.defaultBlockState(), 2, 1, 3);
		p.bed(level, box, 4, 1, 7, Blocks.YELLOW_BED);
		p.put(level, box, hanging(), 5, 4, 5);
		one(p, level, box, CharacterVariant.MADISON, 5, 1, 4, 5, 5, 8, p.pos(4, 1, 7), p.pos(8, 1, 8));
	}

	/** Bee Sanctuary: a wildflower meadow full of beehives and a little honey hut. Beekeeper Madison. */
	static void beeSanctuary(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 16, 7, 16, Blocks.GRASS_BLOCK.defaultBlockState());
		for (int x = 0; x <= 16; x++) {
			for (int z = 0; z <= 16; z++) {
				if (random.nextInt(3) != 0) {
					p.put(level, box, FLOWERS[random.nextInt(FLOWERS.length)].defaultBlockState(), x, 1, z);
				}
			}
		}
		p.fill(level, box, 8, 0, 0, 8, 0, 16, Blocks.DIRT_PATH.defaultBlockState());
		p.fill(level, box, 8, 1, 0, 8, 1, 16, air());
		for (int[] h : new int[][]{{3, 3}, {13, 3}, {3, 8}, {13, 8}, {3, 13}, {13, 13}, {6, 15}, {10, 15}, {5, 1}, {11, 1}}) {
			p.beehive(level, box, random, h[0], 1, h[1]);
		}
		// Honey hut.
		p.fill(level, box, 6, 0, 6, 10, 0, 10, Blocks.BIRCH_PLANKS.defaultBlockState());
		p.walls(level, box, 6, 6, 10, 10, 1, 3, Blocks.HONEYCOMB_BLOCK.defaultBlockState());
		for (int[] c : new int[][]{{6, 6}, {10, 6}, {6, 10}, {10, 10}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 3, c[1], Blocks.BIRCH_LOG.defaultBlockState());
		}
		roof(p, level, box, 6, 6, 10, 10, 4, Blocks.BIRCH_SLAB);
		p.door(level, box, 8, 1, 6, Blocks.BIRCH_DOOR);
		p.put(level, box, Blocks.HONEY_BLOCK.defaultBlockState(), 9, 1, 9);
		p.chest(level, box, random, 7, 1, 9, ModStructures.lootTable("chests/bee_sanctuary"));
		p.put(level, box, hanging(), 8, 3, 8);
		one(p, level, box, CharacterVariant.BEEKEEPER, 8, 1, 4, 8, 8, 10, null, null);
	}

	/** Sweet Berry Cabin (my idea): a spruce cabin with a chimney and rows of berry bushes. Madison. */
	static void berryCabin(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 10, 7, 12, Blocks.PODZOL.defaultBlockState());
		p.fill(level, box, 2, 0, 1, 8, 0, 7, Blocks.SPRUCE_PLANKS.defaultBlockState());
		p.walls(level, box, 2, 1, 8, 7, 1, 3, Blocks.SPRUCE_PLANKS.defaultBlockState());
		for (int[] c : new int[][]{{2, 1}, {8, 1}, {2, 7}, {8, 7}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 3, c[1], Blocks.SPRUCE_LOG.defaultBlockState());
		}
		roof(p, level, box, 2, 1, 8, 7, 4, Blocks.SPRUCE_SLAB);
		p.fill(level, box, 8, 1, 4, 8, 6, 4, Blocks.COBBLESTONE.defaultBlockState());
		p.put(level, box, Blocks.CAMPFIRE.defaultBlockState(), 8, 7, 4);
		p.door(level, box, 5, 1, 1, Blocks.SPRUCE_DOOR);
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 2, 2, 4);
		p.bed(level, box, 3, 1, 5, Blocks.RED_BED);
		p.put(level, box, Blocks.FURNACE.defaultBlockState(), 7, 1, 4);
		p.put(level, box, Blocks.COMPOSTER.defaultBlockState(), 9, 1, 8);
		p.chest(level, box, random, 7, 1, 6, ModStructures.MADISON_PANTRY);
		p.barrel(level, box, random, 7, 1, 2, ModStructures.lootTable("chests/berry_cabin"));
		p.put(level, box, Blocks.LANTERN.defaultBlockState(), 3, 1, 2);
		BlockState bush = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3);
		for (int x = 1; x <= 9; x += 2) {
			for (int z = 9; z <= 12; z++) {
				p.put(level, box, Blocks.GRASS_BLOCK.defaultBlockState(), x, 0, z);
				p.put(level, box, bush, x, 1, z);
			}
		}
		one(p, level, box, CharacterVariant.MADISON, 5, 1, 4, 5, 8, 10, p.pos(3, 1, 5), p.pos(7, 1, 6));
	}

	// ============================================================ FelixSyn and Harley

	/** Ruined Portal Hideout: a broken nether portal with a camp tucked behind it. FelixSyn (and Harley). */
	static void ruinedPortalHideout(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 12, 9, 10, Blocks.COARSE_DIRT.defaultBlockState());
		for (int x = 2; x <= 10; x++) {
			for (int z = 1; z <= 5; z++) {
				int roll = random.nextInt(6);
				p.put(level, box, roll == 0 ? Blocks.MAGMA_BLOCK.defaultBlockState() : roll < 3 ? Blocks.NETHERRACK.defaultBlockState()
						: Blocks.COARSE_DIRT.defaultBlockState(), x, 0, z);
			}
		}
		// The frame: 4 wide, 5 tall, with gaps and crying obsidian.
		for (int y = 1; y <= 5; y++) {
			for (int x = 4; x <= 7; x++) {
				boolean edge = y == 1 || y == 5 || x == 4 || x == 7;
				if (edge && random.nextInt(5) != 0) {
					p.put(level, box, random.nextInt(4) == 0 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState(), x, y, 3);
				}
			}
		}
		p.put(level, box, Blocks.GOLD_BLOCK.defaultBlockState(), 3, 1, 4);
		p.put(level, box, Blocks.IRON_BARS.defaultBlockState(), 8, 1, 2);
		// Camp behind the portal.
		p.bed(level, box, 3, 1, 7, Blocks.BLACK_BED);
		p.bed(level, box, 8, 1, 7, Blocks.PINK_BED);
		p.put(level, box, Blocks.CAMPFIRE.defaultBlockState(), 6, 1, 8);
		p.put(level, box, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), 5, 1, 9);
		p.put(level, box, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), 6, 1, 9);
		p.chest(level, box, random, 10, 1, 8, ModStructures.lootTable("chests/ruined_portal_hideout"));
		p.barrel(level, box, random, 10, 1, 7, ModStructures.BARREL_LOOT);
		p.put(level, box, Blocks.TARGET.defaultBlockState(), 1, 1, 8);
		one(p, level, box, CharacterVariant.FELIXSYN, 6, 1, 6, 6, 6, 12, null, null);
	}

	/**
	 * Village Campsite (built at the edge of a village when FelixSyn and Harley arrive): bedrolls, a campfire and
	 * two mace training dummies (armor stands in old armor).
	 */
	static void villageCampsite(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 10, 4, 8, Blocks.GRASS_BLOCK.defaultBlockState());
		p.fill(level, box, 1, 0, 1, 9, 0, 7, Blocks.COARSE_DIRT.defaultBlockState());
		p.bed(level, box, 2, 1, 5, Blocks.BLACK_BED);
		p.bed(level, box, 4, 1, 5, Blocks.PINK_BED);
		p.put(level, box, Blocks.CAMPFIRE.defaultBlockState(), 5, 1, 3);
		p.put(level, box, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), 4, 1, 1);
		p.put(level, box, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), 5, 1, 1);
		p.put(level, box, Blocks.HAY_BLOCK.defaultBlockState(), 9, 1, 7);
		p.put(level, box, Blocks.TARGET.defaultBlockState(), 9, 1, 6);
		p.chest(level, box, random, 8, 1, 7, ModStructures.lootTable("chests/village_campsite"));
		dummy(p, level, box, 8, 1, 2, Items.IRON_CHESTPLATE, Items.IRON_HELMET);
		dummy(p, level, box, 8, 1, 4, Items.CHAINMAIL_CHESTPLATE, Items.LEATHER_HELMET);
		one(p, level, box, CharacterVariant.FELIXSYN, 5, 1, 4, 5, 4, 24, null, null);
	}

	/** An armor stand wearing old armor: a training dummy for mace smashes. */
	private static void dummy(HomePiece p, WorldGenLevel level, BoundingBox box, int x, int y, int z,
			net.minecraft.world.item.Item chest, net.minecraft.world.item.Item head) {
		BlockPos at = p.pos(x, y, z);
		if (!box.isInside(at)) {
			return;
		}
		ArmorStand stand = EntityType.ARMOR_STAND.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
		if (stand != null) {
			stand.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
			stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chest));
			stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(head));
			level.addFreshEntityWithPassengers(stand);
		}
	}

	// ============================================================ Master BreadLoaf

	/** Bastion Throne Room (built inside a bastion the first time a player gets there): a closed gold-and-blackstone hall. */
	static void bastionThroneRoom(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState bricks = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
		p.fill(level, box, 0, 0, 0, 10, 7, 10, bricks);
		p.fill(level, box, 1, 1, 1, 9, 6, 9, air());
		p.fill(level, box, 1, 0, 1, 9, 0, 9, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
		p.fill(level, box, 5, 0, 1, 5, 0, 7, Blocks.GILDED_BLACKSTONE.defaultBlockState());
		p.fill(level, box, 5, 1, 1, 5, 1, 7, Blocks.RED_CARPET.defaultBlockState());
		p.fill(level, box, 4, 1, 0, 6, 3, 0, air());
		// The throne.
		p.put(level, box, Blocks.GOLD_BLOCK.defaultBlockState(), 5, 1, 8);
		p.put(level, box, stair(Blocks.BLACKSTONE_STAIRS, Direction.NORTH), 5, 2, 8);
		p.put(level, box, stair(Blocks.BLACKSTONE_STAIRS, Direction.EAST), 4, 2, 8);
		p.put(level, box, stair(Blocks.BLACKSTONE_STAIRS, Direction.WEST), 6, 2, 8);
		p.fill(level, box, 4, 1, 8, 4, 1, 8, Blocks.GOLD_BLOCK.defaultBlockState());
		p.fill(level, box, 6, 1, 8, 6, 1, 8, Blocks.GOLD_BLOCK.defaultBlockState());
		p.fill(level, box, 5, 3, 9, 5, 5, 9, Blocks.GOLD_BLOCK.defaultBlockState());
		for (int[] c : new int[][]{{2, 2}, {8, 2}, {2, 6}, {8, 6}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 4, c[1], Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
			p.put(level, box, Blocks.SOUL_LANTERN.defaultBlockState(), c[0], 5, c[1]);
		}
		p.chest(level, box, random, 2, 1, 8, ModStructures.lootTable("chests/bastion_throne_room"));
		p.chest(level, box, random, 8, 1, 8, ModStructures.lootTable("chests/bastion_throne_room"));
		BlockPos spawn = p.pos(5, 1, 6);
		if (p.claimSpawn(spawn, box)) {
			p.placeMarker(level, spawn, m -> m.addResident(CharacterVariant.MASTER, spawn, spawn, 5, null, null, null));
		}
	}

	/** Nether Mace Arena: a round blackstone floor ringed by lava, with tall pillars of different heights. */
	static void netherMaceArena(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		p.fill(level, box, 0, 1, 0, 24, 25, 24, air());
		for (int x = 0; x <= 24; x++) {
			for (int z = 0; z <= 24; z++) {
				double d = Math.sqrt((x - 12) * (x - 12) + (z - 12) * (z - 12));
				if (d <= 10.5) {
					p.put(level, box, d > 9.5 ? Blocks.POLISHED_BLACKSTONE.defaultBlockState()
							: (x + z) % 2 == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), x, 0, z);
					p.column(level, box, Blocks.BASALT.defaultBlockState(), x, -1, z);
				} else if (d <= 12.0) {
					p.put(level, box, Blocks.LAVA.defaultBlockState(), x, 0, z);
					p.put(level, box, Blocks.BLACKSTONE.defaultBlockState(), x, -1, z);
				}
			}
		}
		p.put(level, box, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 12, 0, 12);
		int[] heights = {10, 16, 22, 16, 10, 16, 22, 16};
		for (int i = 0; i < 8; i++) {
			double angle = Math.PI * 2 * i / 8;
			int px = 12 + (int) Math.round(Math.cos(angle) * 8);
			int pz = 12 + (int) Math.round(Math.sin(angle) * 8);
			int h = heights[i];
			p.fill(level, box, px, 1, pz, px, h, pz, Blocks.POLISHED_BASALT.defaultBlockState());
			p.fill(level, box, px - 1, h, pz - 1, px + 1, h, pz + 1, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
			for (int y = 1; y < h; y++) {
				p.put(level, box, ladder(), px, y, pz - 1);
			}
			p.put(level, box, Blocks.SOUL_LANTERN.defaultBlockState(), px, h + 1, pz);
		}
		p.chest(level, box, random, 12, 1, 14, ModStructures.lootTable("chests/nether_mace_arena"));
		BlockPos spawn = p.pos(12, 1, 10);
		if (p.claimSpawn(spawn, box)) {
			p.placeMarker(level, spawn, m -> m.addResident(CharacterVariant.MASTER, spawn, spawn, 9, null, null, null));
		}
	}

	/** End Sky Platform: a round purpur platform high on an obsidian column over an outer End island. */
	static void endSkyPlatform(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		int deck = 14;
		p.fill(level, box, 0, 1, 0, 14, 19, 14, air());
		p.fill(level, box, 5, 0, 5, 9, 0, 9, Blocks.END_STONE_BRICKS.defaultBlockState());
		p.foundation(level, box, 5, 5, 9, 9, Blocks.END_STONE.defaultBlockState());
		p.fill(level, box, 7, 1, 7, 7, deck - 1, 7, Blocks.OBSIDIAN.defaultBlockState());
		for (int y = 1; y <= deck; y++) {
			p.put(level, box, ladder(), 7, y, 6);
		}
		for (int x = 0; x <= 14; x++) {
			for (int z = 0; z <= 14; z++) {
				double d = Math.sqrt((x - 7) * (x - 7) + (z - 7) * (z - 7));
				if (d <= 7.0 && !(x == 7 && z == 6)) {
					p.put(level, box, d > 6.0 ? Blocks.PURPUR_PILLAR.defaultBlockState() : Blocks.PURPUR_BLOCK.defaultBlockState(), x, deck, z);
				}
			}
		}
		for (int i = 0; i < 8; i++) {
			double angle = Math.PI * 2 * i / 8;
			int x = 7 + (int) Math.round(Math.cos(angle) * 6);
			int z = 7 + (int) Math.round(Math.sin(angle) * 6);
			p.put(level, box, Blocks.END_ROD.defaultBlockState(), x, deck + 1, z);
		}
		p.chest(level, box, random, 9, deck + 1, 9, ModStructures.lootTable("chests/end_sky_platform"));
		BlockPos spawn = p.pos(7, deck + 1, 9);
		if (p.claimSpawn(spawn, box)) {
			p.placeMarker(level, spawn, m -> m.addResident(CharacterVariant.MASTER, spawn, spawn, 5, null, null, null));
		}
	}

	// ============================================================ shared

	/** Friendship Festival: long tables with food, banners, lantern lines and a little stage. Bread, Lily and Madison. */
	static void friendshipFestival(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 20, 6, 16, Blocks.GRASS_BLOCK.defaultBlockState());
		p.fill(level, box, 9, 0, 0, 11, 0, 16, Blocks.DIRT_PATH.defaultBlockState());
		// Two long tables (top slabs with a tablecloth) with stair benches.
		for (int tz : new int[]{5, 11}) {
			for (int x = 2; x <= 7; x++) {
				p.put(level, box, topSlab(Blocks.SPRUCE_SLAB), x, 1, tz);
				p.put(level, box, Blocks.WHITE_CARPET.defaultBlockState(), x, 2, tz);
				p.put(level, box, stair(Blocks.SPRUCE_STAIRS, Direction.SOUTH), x, 1, tz - 1);
				p.put(level, box, stair(Blocks.SPRUCE_STAIRS, Direction.NORTH), x, 1, tz + 1);
			}
			p.put(level, box, Blocks.CAKE.defaultBlockState(), 4, 2, tz);
			p.put(level, box, Blocks.CANDLE_CAKE.defaultBlockState(), 6, 2, tz);
		}
		// Picnic blankets on the other side.
		for (int[] b : new int[][]{{13, 3, 0}, {16, 9, 1}, {13, 13, 2}}) {
			Block carpet = new Block[]{Blocks.RED_CARPET, Blocks.YELLOW_CARPET, Blocks.PINK_CARPET}[b[2]];
			p.fill(level, box, b[0], 1, b[1], b[0] + 2, 1, b[1] + 2, carpet.defaultBlockState());
		}
		// Little stage with a jukebox and note blocks.
		p.fill(level, box, 16, 0, 0, 20, 0, 3, Blocks.OAK_PLANKS.defaultBlockState());
		p.put(level, box, Blocks.JUKEBOX.defaultBlockState(), 18, 1, 1);
		p.put(level, box, Blocks.NOTE_BLOCK.defaultBlockState(), 17, 1, 1);
		p.put(level, box, Blocks.NOTE_BLOCK.defaultBlockState(), 19, 1, 1);
		// Banner poles and lantern lines around the edge.
		Block[] banners = {Blocks.RED_BANNER, Blocks.YELLOW_BANNER, Blocks.PINK_BANNER, Blocks.LIME_BANNER, Blocks.LIGHT_BLUE_BANNER};
		int i = 0;
		for (int x = 0; x <= 20; x += 5) {
			for (int z : new int[]{0, 16}) {
				p.fill(level, box, x, 1, z, x, 3, z, Blocks.OAK_FENCE.defaultBlockState());
				p.put(level, box, Blocks.LANTERN.defaultBlockState(), x, 4, z);
				p.put(level, box, banners[i++ % banners.length].defaultBlockState(), x + 1 > 20 ? x - 1 : x + 1, 1, z);
			}
		}
		p.chest(level, box, random, 1, 1, 8, ModStructures.lootTable("chests/friendship_festival"));
		p.chest(level, box, random, 1, 1, 9, ModStructures.lootTable("chests/friendship_festival"));
		p.barrel(level, box, random, 8, 1, 8, ModStructures.BARREL_LOOT);
		BlockPos spawn = p.pos(10, 1, 8);
		if (p.claimSpawn(spawn, box)) {
			BlockPos center = p.pos(10, 1, 8);
			p.placeMarker(level, spawn, m -> {
				m.addResident(CharacterVariant.BREADLOAF, spawn, center, 10, null, null, null);
				m.addResident(CharacterVariant.LILY, spawn, center, 10, null, null, null);
				m.addResident(CharacterVariant.MADISON, spawn, center, 10, null, null, null);
			});
		}
	}

	/** Loaf Lighthouse (my idea): a striped tower on the shore with a glowing lamp room. Fisherman ItzBreadLoaf. */
	static void lighthouse(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		p.fill(level, box, 0, 1, 0, 8, 21, 8, air());
		p.fill(level, box, 0, 0, 0, 8, 0, 8, Blocks.STONE_BRICKS.defaultBlockState());
		p.foundation(level, box, 0, 0, 8, 8, Blocks.STONE_BRICKS.defaultBlockState());
		for (int y = 1; y <= 16; y++) {
			BlockState stripe = (y - 1) / 3 % 2 == 0 ? Blocks.WHITE_CONCRETE.defaultBlockState() : Blocks.RED_CONCRETE.defaultBlockState();
			p.walls(level, box, 2, 2, 6, 6, y, y, stripe);
		}
		p.door(level, box, 4, 1, 2, Blocks.SPRUCE_DOOR);
		for (int y = 1; y <= 17; y++) {
			p.put(level, box, ladder(), 4, y, 5);
		}
		for (int y : new int[]{6, 12}) {
			p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 2, y, 4);
			p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 6, y, 4);
		}
		p.fill(level, box, 1, 17, 1, 7, 17, 7, Blocks.STONE_BRICKS.defaultBlockState());
		p.put(level, box, ladder(), 4, 17, 5);
		p.walls(level, box, 1, 1, 7, 7, 18, 18, Blocks.IRON_BARS.defaultBlockState());
		p.walls(level, box, 3, 3, 5, 5, 18, 19, Blocks.GLASS.defaultBlockState());
		p.put(level, box, air(), 4, 18, 5);
		p.put(level, box, air(), 4, 19, 5);
		p.put(level, box, Blocks.SEA_LANTERN.defaultBlockState(), 4, 18, 4);
		p.put(level, box, Blocks.GLOWSTONE.defaultBlockState(), 4, 19, 4);
		p.fill(level, box, 2, 20, 2, 6, 20, 6, Blocks.STONE_BRICK_SLAB.defaultBlockState());
		// Ground floor: bed, fish barrel, chest.
		p.bed(level, box, 3, 1, 3, Blocks.BLUE_BED);
		p.barrel(level, box, random, 5, 1, 3, ModStructures.FISH_BARREL);
		p.chest(level, box, random, 5, 1, 4, ModStructures.lootTable("chests/loaf_lighthouse"));
		p.put(level, box, Blocks.LANTERN.defaultBlockState(), 3, 1, 5);
		one(p, level, box, CharacterVariant.FISHERMAN, 4, 1, 1, 4, 4, 10, p.pos(3, 1, 3), null);
	}

	/** Shipwreck Camp (my idea): Pirates camping beside the broken half of their ship. */
	static void shipwreckCamp(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		p.fill(level, box, 0, 1, 0, 16, 7, 10, air());
		p.fill(level, box, 0, 0, 0, 16, 0, 10, Blocks.SAND.defaultBlockState());
		p.foundation(level, box, 0, 0, 16, 10, Blocks.SANDSTONE.defaultBlockState());
		// Broken hull: dark oak walls with missing chunks, tilted mast.
		for (int x = 2; x <= 11; x++) {
			for (int y = 0; y <= 3; y++) {
				for (int z : new int[]{2, 7}) {
					if (random.nextInt(5) != 0 || y == 0) {
						p.put(level, box, Blocks.DARK_OAK_PLANKS.defaultBlockState(), x, y, z);
					}
				}
			}
		}
		p.fill(level, box, 2, 0, 3, 2, 3, 6, Blocks.DARK_OAK_PLANKS.defaultBlockState());
		p.fill(level, box, 3, 0, 3, 11, 0, 6, Blocks.SPRUCE_PLANKS.defaultBlockState());
		p.fill(level, box, 6, 1, 4, 6, 6, 4, Blocks.DARK_OAK_LOG.defaultBlockState());
		p.put(level, box, Blocks.WHITE_WOOL.defaultBlockState(), 7, 5, 4);
		p.put(level, box, Blocks.WHITE_WOOL.defaultBlockState(), 7, 4, 4);
		p.chest(level, box, random, 4, 1, 5, ModStructures.lootTable("chests/shipwreck_camp"));
		p.chest(level, box, random, 9, 1, 5, ModStructures.lootTable("chests/shipwreck_camp"));
		p.barrel(level, box, random, 10, 1, 3, ModStructures.FISH_BARREL);
		// Camp.
		p.smallTent(level, box, 13, 5, Blocks.BLACK_WOOL.defaultBlockState());
		p.put(level, box, Blocks.CAMPFIRE.defaultBlockState(), 14, 1, 2);
		p.put(level, box, Blocks.BLACK_BANNER.defaultBlockState(), 16, 1, 0);
		group(p, level, box, 13, 1, 2, 10, 4, 10, CharacterVariant.PIRATE, CharacterVariant.PIRATE);
	}

	/** Snowball Fort (my idea): two snow forts facing each other, with snowball stashes. Two ItzBreadLoafs. */
	static void snowballFort(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 18, 3, 12, Blocks.SNOW_BLOCK.defaultBlockState());
		BlockState snow = Blocks.SNOW_BLOCK.defaultBlockState();
		for (int[] fort : new int[][]{{1, 6, 1}, {12, 17, -1}}) {
			int x0 = fort[0];
			int x1 = fort[1];
			int front = fort[2] > 0 ? x1 : x0;
			p.fill(level, box, front, 1, 3, front, 2, 9, snow);
			p.fill(level, box, x0, 1, 3, x1, 1, 3, snow);
			p.fill(level, box, x0, 1, 9, x1, 1, 9, snow);
			for (int z = 3; z <= 9; z += 2) {
				p.put(level, box, snow, front, 3, z);
			}
			p.put(level, box, Blocks.SNOW.defaultBlockState(), (x0 + x1) / 2, 1, 6);
		}
		p.put(level, box, snow, 9, 1, 6);
		p.put(level, box, snow, 9, 2, 6);
		p.put(level, box, Blocks.CARVED_PUMPKIN.defaultBlockState(), 9, 3, 6);
		p.chest(level, box, random, 2, 1, 5, ModStructures.lootTable("chests/snowball_fort"));
		p.chest(level, box, random, 16, 1, 7, ModStructures.lootTable("chests/snowball_fort"));
		group(p, level, box, 9, 1, 4, 9, 6, 10, CharacterVariant.BREADLOAF, CharacterVariant.VILLAGER);
	}
}
