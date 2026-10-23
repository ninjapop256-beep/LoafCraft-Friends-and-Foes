package com.itzbreadloaf.worldgen.build;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The materials a structure is built from, picked from the biome so the same layout looks right anywhere: a wood
 * set (planks, logs, stairs, slabs, fences, doors...), a stone set (bricks, stairs, slabs, walls), and the ground,
 * path, light and flowers around it. Pieces always ask the palette instead of naming blocks.
 */
public record Palette(Wood wood, Stone stone, BlockState ground, BlockState path, BlockState light, Block bed, Block[] flowers) {

	/** A wood family. */
	public record Wood(Block planks, Block log, Block strippedLog, Block slab, Block stairs, Block fence, Block gate,
			Block door, Block trapdoor, Block leaves) {
	}

	/** A stone family. */
	public record Stone(Block base, Block bricks, Block stairs, Block slab, Block wall, Block floor) {
	}

	public static final Wood OAK = new Wood(Blocks.OAK_PLANKS, Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG, Blocks.OAK_SLAB,
			Blocks.OAK_STAIRS, Blocks.OAK_FENCE, Blocks.OAK_FENCE_GATE, Blocks.OAK_DOOR, Blocks.OAK_TRAPDOOR, Blocks.OAK_LEAVES);
	public static final Wood SPRUCE = new Wood(Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_SLAB,
			Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_FENCE_GATE, Blocks.SPRUCE_DOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.SPRUCE_LEAVES);
	public static final Wood BIRCH = new Wood(Blocks.BIRCH_PLANKS, Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_SLAB,
			Blocks.BIRCH_STAIRS, Blocks.BIRCH_FENCE, Blocks.BIRCH_FENCE_GATE, Blocks.BIRCH_DOOR, Blocks.BIRCH_TRAPDOOR, Blocks.BIRCH_LEAVES);
	public static final Wood DARK_OAK = new Wood(Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_SLAB,
			Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_FENCE_GATE, Blocks.DARK_OAK_DOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.DARK_OAK_LEAVES);
	public static final Wood ACACIA = new Wood(Blocks.ACACIA_PLANKS, Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_SLAB,
			Blocks.ACACIA_STAIRS, Blocks.ACACIA_FENCE, Blocks.ACACIA_FENCE_GATE, Blocks.ACACIA_DOOR, Blocks.ACACIA_TRAPDOOR, Blocks.ACACIA_LEAVES);
	public static final Wood JUNGLE = new Wood(Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_SLAB,
			Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_FENCE, Blocks.JUNGLE_FENCE_GATE, Blocks.JUNGLE_DOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.JUNGLE_LEAVES);
	public static final Wood MANGROVE = new Wood(Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_SLAB,
			Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_FENCE, Blocks.MANGROVE_FENCE_GATE, Blocks.MANGROVE_DOOR, Blocks.MANGROVE_TRAPDOOR, Blocks.MANGROVE_LEAVES);
	public static final Wood CHERRY = new Wood(Blocks.CHERRY_PLANKS, Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_SLAB,
			Blocks.CHERRY_STAIRS, Blocks.CHERRY_FENCE, Blocks.CHERRY_FENCE_GATE, Blocks.CHERRY_DOOR, Blocks.CHERRY_TRAPDOOR, Blocks.CHERRY_LEAVES);
	public static final Wood BAMBOO = new Wood(Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.BAMBOO_SLAB,
			Blocks.BAMBOO_STAIRS, Blocks.BAMBOO_FENCE, Blocks.BAMBOO_FENCE_GATE, Blocks.BAMBOO_DOOR, Blocks.BAMBOO_TRAPDOOR, Blocks.JUNGLE_LEAVES);
	public static final Wood CRIMSON = new Wood(Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_SLAB,
			Blocks.CRIMSON_STAIRS, Blocks.CRIMSON_FENCE, Blocks.CRIMSON_FENCE_GATE, Blocks.CRIMSON_DOOR, Blocks.CRIMSON_TRAPDOOR, Blocks.NETHER_WART_BLOCK);
	public static final Wood WARPED = new Wood(Blocks.WARPED_PLANKS, Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_SLAB,
			Blocks.WARPED_STAIRS, Blocks.WARPED_FENCE, Blocks.WARPED_FENCE_GATE, Blocks.WARPED_DOOR, Blocks.WARPED_TRAPDOOR, Blocks.WARPED_WART_BLOCK);

	public static final Stone COBBLE = new Stone(Blocks.COBBLESTONE, Blocks.STONE_BRICKS, Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_SLAB,
			Blocks.COBBLESTONE_WALL, Blocks.POLISHED_ANDESITE);
	public static final Stone MOSSY = new Stone(Blocks.MOSSY_COBBLESTONE, Blocks.MOSSY_STONE_BRICKS, Blocks.MOSSY_STONE_BRICK_STAIRS,
			Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.MOSSY_COBBLESTONE_WALL, Blocks.COBBLESTONE);
	public static final Stone SANDSTONE = new Stone(Blocks.SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.SANDSTONE_STAIRS, Blocks.SANDSTONE_SLAB,
			Blocks.SANDSTONE_WALL, Blocks.SMOOTH_SANDSTONE);
	public static final Stone RED_SANDSTONE = new Stone(Blocks.RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.RED_SANDSTONE_STAIRS,
			Blocks.RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE_WALL, Blocks.SMOOTH_RED_SANDSTONE);
	public static final Stone DEEPSLATE = new Stone(Blocks.COBBLED_DEEPSLATE, Blocks.DEEPSLATE_BRICKS, Blocks.DEEPSLATE_BRICK_STAIRS,
			Blocks.DEEPSLATE_BRICK_SLAB, Blocks.COBBLED_DEEPSLATE_WALL, Blocks.POLISHED_DEEPSLATE);
	public static final Stone MUD = new Stone(Blocks.PACKED_MUD, Blocks.MUD_BRICKS, Blocks.MUD_BRICK_STAIRS, Blocks.MUD_BRICK_SLAB,
			Blocks.MUD_BRICK_WALL, Blocks.PACKED_MUD);
	public static final Stone BLACKSTONE = new Stone(Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
			Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE);
	public static final Stone END = new Stone(Blocks.END_STONE, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICK_STAIRS, Blocks.END_STONE_BRICK_SLAB,
			Blocks.END_STONE_BRICK_WALL, Blocks.PURPUR_BLOCK);

	private static final Block[] MEADOW_FLOWERS = {Blocks.POPPY, Blocks.DANDELION, Blocks.CORNFLOWER, Blocks.OXEYE_DAISY, Blocks.AZURE_BLUET, Blocks.ALLIUM};
	private static final Block[] CHERRY_FLOWERS = {Blocks.PINK_TULIP, Blocks.ALLIUM, Blocks.LILY_OF_THE_VALLEY, Blocks.PINK_TULIP};
	private static final Block[] DRY_FLOWERS = {Blocks.DEAD_BUSH, Blocks.DEAD_BUSH};
	private static final Block[] COLD_FLOWERS = {Blocks.SWEET_BERRY_BUSH, Blocks.FERN, Blocks.LILY_OF_THE_VALLEY};
	private static final Block[] JUNGLE_FLOWERS = {Blocks.FERN, Blocks.LARGE_FERN, Blocks.POPPY};
	private static final Block[] NETHER_FLOWERS = {Blocks.CRIMSON_FUNGUS, Blocks.WARPED_FUNGUS, Blocks.CRIMSON_ROOTS};
	private static final Block[] END_FLOWERS = {Blocks.AIR};

	/** The palette for a biome. Anything not listed is built like the plains: oak and cobblestone on grass. */
	public static Palette forBiome(Holder<Biome> biome) {
		BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
		BlockState dirtPath = Blocks.DIRT_PATH.defaultBlockState();
		BlockState lantern = Blocks.LANTERN.defaultBlockState();
		if (biome.is(BiomeTags.IS_NETHER)) {
			Wood wood = biome.is(Biomes.WARPED_FOREST) ? WARPED : CRIMSON;
			BlockState ground = biome.is(Biomes.WARPED_FOREST) ? Blocks.WARPED_NYLIUM.defaultBlockState()
					: biome.is(Biomes.CRIMSON_FOREST) ? Blocks.CRIMSON_NYLIUM.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState();
			return new Palette(wood, BLACKSTONE, ground, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), Blocks.SOUL_LANTERN.defaultBlockState(),
					Blocks.RED_BED, NETHER_FLOWERS);
		}
		if (biome.is(BiomeTags.IS_END)) {
			return new Palette(BIRCH, END, Blocks.END_STONE.defaultBlockState(), Blocks.END_STONE_BRICKS.defaultBlockState(),
					Blocks.END_ROD.defaultBlockState(), Blocks.PURPLE_BED, END_FLOWERS);
		}
		if (biome.is(Biomes.DESERT)) {
			return new Palette(BIRCH, SANDSTONE, Blocks.SAND.defaultBlockState(), Blocks.SMOOTH_SANDSTONE.defaultBlockState(), lantern,
					Blocks.YELLOW_BED, DRY_FLOWERS);
		}
		if (biome.is(BiomeTags.IS_BADLANDS)) {
			return new Palette(DARK_OAK, RED_SANDSTONE, Blocks.RED_SAND.defaultBlockState(), Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState(), lantern,
					Blocks.ORANGE_BED, DRY_FLOWERS);
		}
		if (biome.is(Biomes.MANGROVE_SWAMP)) {
			return new Palette(MANGROVE, MUD, Blocks.MUD.defaultBlockState(), Blocks.PACKED_MUD.defaultBlockState(), lantern, Blocks.GREEN_BED, JUNGLE_FLOWERS);
		}
		if (biome.is(Biomes.SWAMP)) {
			return new Palette(OAK, MOSSY, grass, dirtPath, lantern, Blocks.GREEN_BED, JUNGLE_FLOWERS);
		}
		if (biome.is(Biomes.CHERRY_GROVE)) {
			return new Palette(CHERRY, COBBLE, grass, dirtPath, lantern, Blocks.PINK_BED, CHERRY_FLOWERS);
		}
		if (biome.is(Biomes.BAMBOO_JUNGLE)) {
			return new Palette(BAMBOO, MOSSY, grass, dirtPath, lantern, Blocks.LIME_BED, JUNGLE_FLOWERS);
		}
		if (biome.is(BiomeTags.IS_JUNGLE)) {
			return new Palette(JUNGLE, MOSSY, grass, dirtPath, lantern, Blocks.LIME_BED, JUNGLE_FLOWERS);
		}
		if (biome.is(Biomes.DARK_FOREST) || biome.is(Biomes.PALE_GARDEN)) {
			return new Palette(DARK_OAK, MOSSY, grass, dirtPath, lantern, Blocks.BROWN_BED, MEADOW_FLOWERS);
		}
		if (biome.is(Biomes.BIRCH_FOREST) || biome.is(Biomes.OLD_GROWTH_BIRCH_FOREST)) {
			return new Palette(BIRCH, COBBLE, grass, dirtPath, lantern, Blocks.LIGHT_BLUE_BED, MEADOW_FLOWERS);
		}
		if (biome.is(BiomeTags.IS_SAVANNA)) {
			return new Palette(ACACIA, COBBLE, grass, dirtPath, lantern, Blocks.ORANGE_BED, DRY_FLOWERS);
		}
		if (biome.is(Biomes.MUSHROOM_FIELDS)) {
			return new Palette(OAK, COBBLE, Blocks.MYCELIUM.defaultBlockState(), dirtPath, lantern, Blocks.RED_BED, MEADOW_FLOWERS);
		}
		if (biome.value().getBaseTemperature() < 0.15F) {
			// Snowy and frozen biomes: spruce and stone on snow.
			return new Palette(SPRUCE, COBBLE, Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.PACKED_ICE.defaultBlockState(), lantern,
					Blocks.WHITE_BED, COLD_FLOWERS);
		}
		if (biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(BiomeTags.IS_HILL)) {
			return new Palette(SPRUCE, DEEPSLATE, grass, Blocks.GRAVEL.defaultBlockState(), lantern, Blocks.GRAY_BED, COLD_FLOWERS);
		}
		if (biome.is(BiomeTags.IS_TAIGA)) {
			return new Palette(SPRUCE, COBBLE, Blocks.PODZOL.defaultBlockState(), dirtPath, lantern, Blocks.BROWN_BED, COLD_FLOWERS);
		}
		if (biome.is(BiomeTags.IS_BEACH) || biome.is(BiomeTags.IS_OCEAN)) {
			return new Palette(BIRCH, SANDSTONE, Blocks.SAND.defaultBlockState(), Blocks.SMOOTH_SANDSTONE.defaultBlockState(), lantern,
					Blocks.LIGHT_BLUE_BED, MEADOW_FLOWERS);
		}
		return new Palette(OAK, COBBLE, grass, dirtPath, lantern, Blocks.RED_BED, MEADOW_FLOWERS);
	}

	// ---------------------------------------------------------------- shortcuts used by the pieces

	public BlockState planks() {
		return this.wood.planks().defaultBlockState();
	}

	public BlockState log() {
		return this.wood.log().defaultBlockState();
	}

	public BlockState strippedLog() {
		return this.wood.strippedLog().defaultBlockState();
	}

	public BlockState slab() {
		return this.wood.slab().defaultBlockState();
	}

	public BlockState stairs() {
		return this.wood.stairs().defaultBlockState();
	}

	public BlockState fence() {
		return this.wood.fence().defaultBlockState();
	}

	public BlockState bricks() {
		return this.stone.bricks().defaultBlockState();
	}

	public BlockState stoneBase() {
		return this.stone.base().defaultBlockState();
	}

	public BlockState stoneFloor() {
		return this.stone.floor().defaultBlockState();
	}

	public BlockState stoneStairs() {
		return this.stone.stairs().defaultBlockState();
	}

	public BlockState stoneSlab() {
		return this.stone.slab().defaultBlockState();
	}

	public BlockState stoneWall() {
		return this.stone.wall().defaultBlockState();
	}

	/** What fills the gap under a structure: dirt under soil, the stone otherwise (sand would fall). */
	public BlockState foundation() {
		Block g = this.ground.getBlock();
		return g == Blocks.GRASS_BLOCK || g == Blocks.PODZOL || g == Blocks.MYCELIUM || g == Blocks.SNOW_BLOCK || g == Blocks.MUD
				? Blocks.DIRT.defaultBlockState() : this.stoneBase();
	}

	/** A flower that fits the biome (air in the End). */
	public Block flower(int index) {
		return this.flowers[Math.floorMod(index, this.flowers.length)];
	}
}
