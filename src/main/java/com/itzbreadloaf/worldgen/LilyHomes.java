package com.itzbreadloaf.worldgen;

import com.itzbreadloaf.brain.LilyBrain;
import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * ImLilyLol's homes outside the Cherry Grove. Same shy Lily everywhere (sometimes a Gardener, Witchy or Bookworm Lily),
 * each house built from its biome's materials, with a chest of cozy loot that fits the biome.
 */
final class LilyHomes {
	private static final Block[] FLOWERS = {Blocks.POPPY, Blocks.CORNFLOWER, Blocks.ALLIUM, Blocks.OXEYE_DAISY,
			Blocks.AZURE_BLUET, Blocks.LILY_OF_THE_VALLEY, Blocks.PINK_TULIP, Blocks.DANDELION, Blocks.BLUE_ORCHID};

	private LilyHomes() {
	}

	/** Meadow: birch walls on stone-brick corners, and a flower garden out front. */
	static void meadow(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		cottage(p, level, box, random, Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.BIRCH_PLANKS.defaultBlockState(),
				Blocks.BIRCH_PLANKS.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(),
				Blocks.BIRCH_SLAB.defaultBlockState(), Blocks.BIRCH_DOOR, Blocks.LIGHT_BLUE_BED, ModStructures.lootTable("chests/lily_meadow_cottage"));
		garden(p, level, box, random, 0.8F);
		p.put(level, box, Blocks.POTTED_CORNFLOWER.defaultBlockState(), 6, 1, 3);
		resident(p, level, box, random, CharacterVariant.LILY, CharacterVariant.GARDENER, CharacterVariant.GARDENER);
	}

	/** Flower Forest: mossy walls, a moss roof with flowering azalea, vines climbing the sides, flowers everywhere. */
	static void flowerForest(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		cottage(p, level, box, random, Blocks.MOSS_BLOCK.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(),
				Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), Blocks.MOSS_BLOCK.defaultBlockState(),
				Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), Blocks.OAK_DOOR,
				Blocks.LIME_BED, ModStructures.lootTable("chests/lily_flower_cottage"));
		garden(p, level, box, random, 1.0F);
		for (int z = 3; z <= 8; z++) {
			for (int y = 1; y <= 3; y++) {
				if (random.nextInt(3) != 0) {
					p.put(level, box, Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, true), 0, y, z);
				}
				if (random.nextInt(3) != 0) {
					p.put(level, box, Blocks.VINE.defaultBlockState().setValue(VineBlock.WEST, true), 8, y, z);
				}
			}
		}
		p.put(level, box, Blocks.FLOWERING_AZALEA.defaultBlockState(), 0, 1, 1);
		p.put(level, box, Blocks.FLOWERING_AZALEA.defaultBlockState(), 8, 1, 1);
		p.put(level, box, Blocks.POTTED_FLOWERING_AZALEA.defaultBlockState(), 6, 1, 3);
		resident(p, level, box, random, CharacterVariant.LILY, CharacterVariant.GARDENER, CharacterVariant.WITCHY);
	}

	/** Birch Forest: a cozy reading cabin with bookshelves along the walls and a lectern. */
	static void readingCabin(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		cottage(p, level, box, random, Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.BIRCH_PLANKS.defaultBlockState(),
				Blocks.BIRCH_PLANKS.defaultBlockState(), Blocks.BIRCH_LOG.defaultBlockState(), Blocks.BIRCH_PLANKS.defaultBlockState(),
				Blocks.BIRCH_SLAB.defaultBlockState(), Blocks.BIRCH_DOOR, Blocks.YELLOW_BED, ModStructures.lootTable("chests/lily_reading_cabin"));
		for (int z = 3; z <= 6; z++) {
			p.fill(level, box, 6, 1, z, 6, 2, z, Blocks.BOOKSHELF.defaultBlockState());
		}
		for (int z = 3; z <= 5; z++) {
			p.fill(level, box, 2, 1, z, 2, 2, z, Blocks.BOOKSHELF.defaultBlockState());
		}
		p.put(level, box, Blocks.LECTERN.defaultBlockState(), 4, 1, 8);
		p.put(level, box, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, false), 6, 3, 3);
		resident(p, level, box, random, CharacterVariant.BOOKWORM, CharacterVariant.BOOKWORM, CharacterVariant.LILY);
	}

	/** Snowy Taiga: a spruce cabin with a stone fireplace (and chimney), lanterns, snow all around. */
	static void snowyCabin(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		cottage(p, level, box, random, Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(),
				Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_LOG.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(),
				Blocks.SPRUCE_SLAB.defaultBlockState(), Blocks.SPRUCE_DOOR, Blocks.WHITE_BED, ModStructures.lootTable("chests/lily_snowy_cabin"));
		// Fireplace in the back wall, with a chimney through the roof.
		BlockState stone = Blocks.COBBLESTONE.defaultBlockState();
		p.fill(level, box, 3, 1, 9, 3, 3, 9, stone);
		p.fill(level, box, 5, 1, 9, 5, 3, 9, stone);
		p.fill(level, box, 4, 2, 9, 4, 3, 9, Blocks.BRICKS.defaultBlockState());
		p.put(level, box, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 4, 1, 9);
		p.put(level, box, stone, 4, 1, 10);
		p.fill(level, box, 4, 4, 10, 4, 7, 10, stone);
		BlockState lantern = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
		p.put(level, box, lantern, 2, 3, 4);
		p.put(level, box, lantern, 6, 3, 4);
		p.put(level, box, Blocks.LANTERN.defaultBlockState(), 0, 1, 1);
		p.put(level, box, Blocks.LANTERN.defaultBlockState(), 8, 1, 1);
		p.put(level, box, Blocks.WHITE_CARPET.defaultBlockState(), 4, 1, 6);
		for (int x = 0; x <= 8; x++) {
			for (int z = 0; z <= 10; z++) {
				boolean outside = x == 0 || x == 8 || z <= 1 || z == 10;
				if (outside && random.nextInt(3) != 0 && level.getBlockState(p.pos(x, 1, z)).isAir()) {
					p.put(level, box, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + random.nextInt(2)), x, 1, z);
				}
			}
		}
		p.fill(level, box, 2, 6, 3, 6, 6, 8, Blocks.SNOW.defaultBlockState());
		resident(p, level, box, random, CharacterVariant.LILY, CharacterVariant.WITCHY, CharacterVariant.BOOKWORM);
	}

	/** Mushroom Fields (rare): mushroom-stem walls under a big red mushroom cap. */
	static void mushroomHouse(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState stem = Blocks.MUSHROOM_STEM.defaultBlockState();
		BlockState cap = Blocks.RED_MUSHROOM_BLOCK.defaultBlockState();
		p.fill(level, box, 0, 1, 0, 10, 8, 10, Blocks.AIR.defaultBlockState());
		p.fill(level, box, 0, 0, 0, 10, 0, 10, Blocks.MYCELIUM.defaultBlockState());
		p.foundation(level, box, 0, 0, 10, 10, Blocks.DIRT.defaultBlockState());
		p.fill(level, box, 3, 0, 3, 7, 0, 7, Blocks.SPRUCE_PLANKS.defaultBlockState());
		p.walls(level, box, 2, 2, 8, 8, 1, 4, stem);
		p.door(level, box, 5, 1, 2, Blocks.SPRUCE_DOOR);
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 2, 2, 5);
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 8, 2, 5);
		p.fill(level, box, 1, 5, 1, 9, 5, 9, cap);
		p.fill(level, box, 2, 6, 2, 8, 6, 8, cap);
		p.fill(level, box, 3, 7, 3, 7, 7, 7, cap);
		p.fill(level, box, 3, 5, 3, 7, 5, 7, stem);
		p.bed(level, box, 3, 1, 6, Blocks.RED_BED);
		p.chest(level, box, random, 7, 1, 7, ModStructures.lootTable("chests/lily_mushroom_house"));
		p.put(level, box, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 5, 4, 5);
		for (int i = 0; i < 8; i++) {
			int x = random.nextInt(11);
			int z = random.nextInt(11);
			if (x < 1 || x > 9 || z < 1 || z > 9) {
				p.put(level, box, (random.nextBoolean() ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM).defaultBlockState(), x, 1, z);
			}
		}
		BlockPos spawn = p.pos(5, 1, 4);
		if (p.claimSpawn(spawn, box)) {
			BlockPos home = p.pos(5, 1, 5);
			BlockPos bed = p.pos(3, 1, 7);
			CharacterVariant type = new CharacterVariant[]{CharacterVariant.LILY, CharacterVariant.WITCHY}[random.nextInt(2)];
			p.placeMarker(level, spawn, marker -> marker.addResident(type, spawn, home, LilyBrain.HOME_RADIUS, bed, null, null));
		}
	}

	// ------------------------------------------------------------ shared

	/**
	 * Lily's cottage shape (9x11, the same layout as her cherry house): door at the front, bed in the back left,
	 * chest in the back right, a lantern in the middle. Materials vary by biome.
	 */
	static void cottage(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random, BlockState ground, BlockState floor,
			BlockState wall, BlockState corner, BlockState roof, BlockState roofTop, Block door, Block bed, ResourceKey<LootTable> loot) {
		p.fill(level, box, 0, 1, 0, 8, 7, 10, Blocks.AIR.defaultBlockState());
		p.fill(level, box, 0, 0, 0, 8, 0, 10, ground);
		p.foundation(level, box, 0, 0, 8, 10, Blocks.DIRT.defaultBlockState());
		p.fill(level, box, 1, 0, 2, 7, 0, 9, floor);
		p.walls(level, box, 1, 2, 7, 9, 1, 3, wall);
		for (int[] c : new int[][]{{1, 2}, {7, 2}, {1, 9}, {7, 9}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 4, c[1], corner);
		}
		p.fill(level, box, 1, 4, 2, 7, 4, 9, roof);
		p.fill(level, box, 2, 5, 3, 6, 5, 8, roofTop);
		p.door(level, box, 4, 1, 2, door);
		BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
		for (int[] w : new int[][]{{2, 2}, {6, 2}, {1, 5}, {7, 5}, {1, 7}, {7, 7}}) {
			p.put(level, box, glass, w[0], 2, w[1]);
		}
		p.bed(level, box, 2, 1, 7, bed);
		p.chest(level, box, random, 6, 1, 8, loot);
		p.put(level, box, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 4, 3, 5);
	}

	/** Flowers in the front yard (keeping the path to the door clear). */
	static void garden(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random, float density) {
		for (int x = 0; x <= 8; x++) {
			for (int z = 0; z <= 1; z++) {
				if (x != 4 && random.nextFloat() < density) {
					p.put(level, box, FLOWERS[random.nextInt(FLOWERS.length)].defaultBlockState(), x, 1, z);
				}
			}
		}
		p.put(level, box, Blocks.DIRT_PATH.defaultBlockState(), 4, 0, 0);
		p.put(level, box, Blocks.DIRT_PATH.defaultBlockState(), 4, 0, 1);
	}

	/** One Lily (of one of the given types) who lives here: spawn by the door, bed in the back. */
	static void resident(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random, CharacterVariant... types) {
		BlockPos spawn = p.pos(4, 1, 4);
		if (!p.claimSpawn(spawn, box)) {
			return;
		}
		CharacterVariant type = types[random.nextInt(types.length)];
		BlockPos home = p.pos(4, 1, 5);
		BlockPos bed = p.pos(2, 1, 8);
		p.placeMarker(level, spawn, marker -> marker.addResident(type, spawn, home, LilyBrain.HOME_RADIUS, bed, null, null));
	}

}
