package com.itzbreadloaf.worldgen;

import com.itzbreadloaf.block.ModBlocks;
import com.itzbreadloaf.block.ResidentMarkerBlockEntity;
import com.itzbreadloaf.brain.LilyBrain;
import com.itzbreadloaf.brain.MadisonBrain;
import com.itzbreadloaf.character.CharacterVariant;
import java.util.UUID;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Builds a house, tent, Lily's house or Madison's house block by block (no .nbt files). Residents are not spawned
 * here: an invisible resident marker block remembers who lives here (home, bed, tent group, pantry), and creates
 * them as Carpet fake players the first time a real player comes near.
 * Local coordinates: x across, z from the front (z = 0) to the back, y = 0 is the ground/floor layer.
 * placeBlock() turns and mirrors everything to match the piece's random orientation.
 */
public class HomePiece extends ScatteredFeaturePiece {
	private static final Block[] WOOL = {
			Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL, Blocks.LIGHT_BLUE_WOOL, Blocks.YELLOW_WOOL,
			Blocks.LIME_WOOL, Blocks.PINK_WOOL, Blocks.GRAY_WOOL, Blocks.LIGHT_GRAY_WOOL, Blocks.CYAN_WOOL,
			Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL, Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.RED_WOOL, Blocks.BLACK_WOOL
	};
	private static final Block[] BEDS = {Blocks.RED_BED, Blocks.BLUE_BED, Blocks.GREEN_BED, Blocks.YELLOW_BED};

	private final HomeKind kind;
	private final int woolColor;
	private final int members;
	private final long groupSeed;
	private boolean spawned;

	public HomePiece(HomeKind kind, RandomSource random, int x, int z) {
		super(ModStructures.HOME_PIECE, x, 64, z, kind.width, kind.height, kind.depth, Direction.Plane.HORIZONTAL.getRandomDirection(random));
		this.kind = kind;
		this.woolColor = random.nextInt(WOOL.length);
		this.members = 2 + random.nextInt(3);
		this.groupSeed = random.nextLong();
	}

	public HomePiece(CompoundTag tag) {
		super(ModStructures.HOME_PIECE, tag);
		this.kind = HomeKind.byName(tag.getStringOr("Kind", "house"));
		this.woolColor = tag.getIntOr("Wool", 0);
		this.members = tag.getIntOr("Members", 2);
		this.groupSeed = tag.getLongOr("GroupSeed", 0L);
		this.spawned = tag.getBooleanOr("Spawned", false);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		super.addAdditionalSaveData(context, tag);
		tag.putString("Kind", this.kind.getSerializedName());
		tag.putInt("Wool", this.woolColor);
		tag.putInt("Members", this.members);
		tag.putLong("GroupSeed", this.groupSeed);
		tag.putBoolean("Spawned", this.spawned);
	}

	// ------------------------------------------------------------ materials

	/** Wood type (or sandstone) matching the biome. */
	record Palette(BlockState planks, BlockState corner, BlockState slab, Block door, BlockState foundation, BlockState ground) {
		static Palette forBiome(Holder<Biome> biome) {
			if (biome.is(ConventionalBiomeTags.IS_DESERT) || biome.is(BiomeTags.IS_BADLANDS)) {
				return new Palette(Blocks.SANDSTONE.defaultBlockState(), Blocks.SMOOTH_SANDSTONE.defaultBlockState(),
						Blocks.SANDSTONE_SLAB.defaultBlockState(), Blocks.BIRCH_DOOR, Blocks.SANDSTONE.defaultBlockState(),
						Blocks.SAND.defaultBlockState());
			}
			if (biome.is(BiomeTags.IS_TAIGA) || biome.is(ConventionalBiomeTags.IS_SNOWY)) {
				return new Palette(Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_LOG.defaultBlockState(),
						Blocks.SPRUCE_SLAB.defaultBlockState(), Blocks.SPRUCE_DOOR, Blocks.COBBLESTONE.defaultBlockState(),
						biome.is(ConventionalBiomeTags.IS_SNOWY) ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.PODZOL.defaultBlockState());
			}
			if (biome.is(BiomeTags.IS_SAVANNA)) {
				return new Palette(Blocks.ACACIA_PLANKS.defaultBlockState(), Blocks.ACACIA_LOG.defaultBlockState(),
						Blocks.ACACIA_SLAB.defaultBlockState(), Blocks.ACACIA_DOOR, Blocks.COBBLESTONE.defaultBlockState(),
						Blocks.GRASS_BLOCK.defaultBlockState());
			}
			return new Palette(Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(),
					Blocks.OAK_SLAB.defaultBlockState(), Blocks.OAK_DOOR, Blocks.COBBLESTONE.defaultBlockState(),
					Blocks.GRASS_BLOCK.defaultBlockState());
		}

		static Palette cherry() {
			return new Palette(Blocks.CHERRY_PLANKS.defaultBlockState(), Blocks.CHERRY_LOG.defaultBlockState(),
					Blocks.CHERRY_SLAB.defaultBlockState(), Blocks.CHERRY_DOOR, Blocks.COBBLESTONE.defaultBlockState(),
					Blocks.GRASS_BLOCK.defaultBlockState());
		}
	}

	// ------------------------------------------------------------ building

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
			BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
		// Sink the piece one block so y = 0 replaces the top ground layer.
		if (this.kind.inNether()) {
			this.settleOnNetherFloor(level);
		} else if (!this.updateAverageGroundHeight(level, chunkBox, -1)) {
			return;
		}
		Holder<Biome> biome = level.getBiome(this.getWorldPos(this.kind.width / 2, 1, this.kind.depth / 2));
		switch (this.kind) {
			case HOUSE -> this.buildHouse(level, chunkBox, random, Palette.forBiome(biome));
			case TENT -> this.buildTent(level, chunkBox, random, Palette.forBiome(biome));
			case LILY_HOUSE -> this.buildLilyHouse(level, chunkBox, random, Palette.cherry());
			case MADISON_HOUSE -> this.buildMadisonHouse(level, chunkBox, random);
			case VILLAGE -> this.buildVillage(level, chunkBox, random, Palette.forBiome(biome));
			case FORT -> this.buildFort(level, chunkBox, random);
			case WATCHTOWER -> this.buildWatchtower(level, chunkBox, random);
			case BANDIT_CAMP -> this.buildBanditCamp(level, chunkBox, random);
			case TREEHOUSE -> this.buildTreehouse(level, chunkBox, random);
			case BAKERY -> this.buildBakery(level, chunkBox, random);
			case SKY_ARENA -> this.buildSkyArena(level, chunkBox, random);
			case PIRATE_SHIP -> this.buildPirateShip(level, chunkBox, random);
			default -> ExtraHomes.build(this, level, chunkBox, random, biome);
		}
	}

	// ------------------------------------------------------------ LoafCraft Village

	private void buildVillage(WorldGenLevel level, BoundingBox box, RandomSource random, Palette p) {
		this.fill(level, box, 0, 1, 0, 30, 9, 30, Blocks.AIR.defaultBlockState());
		this.fill(level, box, 0, 0, 0, 30, 0, 30, p.ground());
		this.foundation(level, box, 0, 0, 30, 30, Blocks.DIRT.defaultBlockState());
		// Main paths crossing at the well.
		this.fill(level, box, 14, 0, 0, 16, 0, 30, Blocks.DIRT_PATH.defaultBlockState());
		this.fill(level, box, 0, 0, 14, 30, 0, 16, Blocks.DIRT_PATH.defaultBlockState());
		// Well.
		this.fill(level, box, 13, 0, 13, 17, 0, 17, Blocks.COBBLESTONE.defaultBlockState());
		this.fill(level, box, 14, -1, 14, 16, -1, 16, Blocks.COBBLESTONE.defaultBlockState());
		this.fill(level, box, 14, 0, 14, 16, 0, 16, Blocks.WATER.defaultBlockState());
		this.walls(level, box, 13, 13, 17, 17, 1, 1, Blocks.COBBLESTONE.defaultBlockState());
		for (int[] c : new int[][]{{13, 13}, {17, 13}, {13, 17}, {17, 17}}) {
			this.fill(level, box, c[0], 2, c[1], c[0], 3, c[1], Blocks.OAK_FENCE.defaultBlockState());
		}
		this.fill(level, box, 13, 4, 13, 17, 4, 17, Blocks.OAK_SLAB.defaultBlockState());
		this.placeBlock(level, Blocks.BELL.defaultBlockState(), 18, 1, 13, box);
		// Lamp posts.
		for (int[] c : new int[][]{{12, 12}, {18, 12}, {12, 18}, {18, 18}}) {
			this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), c[0], 1, c[1], box);
			this.placeBlock(level, Blocks.LANTERN.defaultBlockState(), c[0], 2, c[1], box);
		}
		// Three cabins.
		this.cabin(level, box, 1, 1, p, Blocks.RED_BED);
		this.cabin(level, box, 23, 1, p, Blocks.BLUE_BED);
		this.cabin(level, box, 1, 23, p, Blocks.PINK_BED);
		// Farm: crop rows around a water channel, fenced.
		BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7);
		Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS};
		for (int x = 20; x <= 29; x++) {
			for (int z = 20; z <= 29; z++) {
				if (z == 24) {
					this.placeBlock(level, Blocks.WATER.defaultBlockState(), x, 0, z, box);
					continue;
				}
				this.placeBlock(level, farmland, x, 0, z, box);
				net.minecraft.world.level.block.CropBlock crop = (net.minecraft.world.level.block.CropBlock) crops[(x / 3) % crops.length];
				this.placeBlock(level, crop.getStateForAge(random.nextInt(crop.getMaxAge() + 1)), x, 1, z, box);
			}
		}
		this.walls(level, box, 19, 19, 30, 30, 1, 1, Blocks.OAK_FENCE.defaultBlockState());
		this.placeBlock(level, Blocks.OAK_FENCE_GATE.defaultBlockState(), 24, 1, 19, box);
		// Fishing pond with a little dock.
		this.fill(level, box, 3, -1, 9, 10, -1, 12, Blocks.DIRT.defaultBlockState());
		this.fill(level, box, 3, 0, 9, 10, 0, 12, Blocks.WATER.defaultBlockState());
		this.fill(level, box, 6, 0, 10, 7, 0, 13, Blocks.SPRUCE_PLANKS.defaultBlockState());
		this.barrel(level, box, random, 8, 1, 13, ModStructures.FISH_BARREL);
		// Market corner: hay, a crafting table and the village chest.
		this.placeBlock(level, Blocks.HAY_BLOCK.defaultBlockState(), 10, 1, 20, box);
		this.placeBlock(level, Blocks.HAY_BLOCK.defaultBlockState(), 10, 1, 21, box);
		this.placeBlock(level, Blocks.CRAFTING_TABLE.defaultBlockState(), 11, 1, 22, box);
		this.createChest(level, box, random, 12, 1, 20, ModStructures.VILLAGE_CHEST);

		// Bakery booth (Baker Madison): a counter, smoker and bread barrels under a striped awning.
		this.fill(level, box, 1, 0, 17, 7, 0, 21, p.planks());
		for (int[] c : new int[][]{{1, 17}, {7, 17}, {1, 21}, {7, 21}}) {
			this.fill(level, box, c[0], 1, c[1], c[0], 3, c[1], Blocks.OAK_FENCE.defaultBlockState());
		}
		for (int x = 1; x <= 7; x++) {
			Block wool = x % 2 == 0 ? Blocks.WHITE_WOOL : Blocks.ORANGE_WOOL;
			this.fill(level, box, x, 4, 17, x, 4, 21, wool.defaultBlockState());
		}
		this.fill(level, box, 2, 1, 18, 6, 1, 18, Blocks.SPRUCE_SLAB.defaultBlockState()
				.setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.TOP));
		this.placeBlock(level, Blocks.SMOKER.defaultBlockState(), 2, 1, 20, box);
		this.placeBlock(level, Blocks.HAY_BLOCK.defaultBlockState(), 3, 1, 20, box);
		this.barrel(level, box, random, 5, 1, 20, ModStructures.BAKERY_CHEST);
		this.barrel(level, box, random, 6, 1, 20, ModStructures.BARREL_LOOT);
		this.placeBlock(level, Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true), 4, 3, 19, box);

		BlockPos spawn = this.getWorldPos(15, 1, 20);
		if (!this.spawned && box.isInside(spawn)) {
			this.spawned = true;
			UUID group = Mth.createInsecureUUID(RandomSource.create(this.groupSeed));
			BlockPos center = this.getWorldPos(15, 1, 15);
			BlockPos[] beds = {this.getWorldPos(2, 1, 6), this.getWorldPos(24, 1, 6), null};
			BlockPos lilyHome = this.getWorldPos(4, 1, 26);
			BlockPos lilyBed = this.getWorldPos(2, 1, 28);
			BlockPos farm = this.getWorldPos(25, 1, 22);
			BlockPos dock = this.getWorldPos(7, 1, 13);
			BlockPos bakery = this.getWorldPos(4, 1, 19);
			boolean felixHere = random.nextDouble() < com.itzbreadloaf.config.ModConfig.get().felixVillageChance;
			this.placeMarker(level, spawn, marker -> {
				for (BlockPos bed : beds) {
					marker.addResident(CharacterVariant.VILLAGER, spawn, center, 14, bed, group, null);
				}
				marker.addResident(CharacterVariant.ARCHER_LILY, spawn, lilyHome, 8, lilyBed, null, null);
				marker.addResident(CharacterVariant.FARMER, spawn, farm, 6, null, null, null);
				marker.addResident(CharacterVariant.FISHER, spawn, dock, 4, null, null, null);
				marker.addResident(CharacterVariant.BAKER, spawn, bakery, 4, null, null, null);
				if (felixHere) {
					// FelixSyn drifts into LoafCraft villages too (Harley comes with him).
					marker.addResident(CharacterVariant.FELIXSYN, spawn, center, 24, null, null, null);
				}
			});
		}
	}

	/** A 7x7 cabin (like ItzBreadLoaf's house) with its corner at (ox, oz): door at the front, bed at the back. */
	void cabin(WorldGenLevel level, BoundingBox box, int ox, int oz, Palette p, Block bed) {
		this.fill(level, box, ox, 0, oz, ox + 6, 0, oz + 6, p.planks());
		this.walls(level, box, ox, oz, ox + 6, oz + 6, 1, 3, p.planks());
		for (int[] c : new int[][]{{0, 0}, {6, 0}, {0, 6}, {6, 6}}) {
			this.fill(level, box, ox + c[0], 1, oz + c[1], ox + c[0], 4, oz + c[1], p.corner());
		}
		this.fill(level, box, ox, 4, oz, ox + 6, 4, oz + 6, p.planks());
		this.fill(level, box, ox + 1, 5, oz + 1, ox + 5, 5, oz + 5, p.slab());
		this.door(level, box, ox + 3, 1, oz, p.door());
		BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
		this.placeBlock(level, glass, ox + 1, 2, oz, box);
		this.placeBlock(level, glass, ox + 5, 2, oz, box);
		this.bed(level, box, ox + 1, 1, oz + 4, bed);
		this.placeBlock(level, Blocks.CRAFTING_TABLE.defaultBlockState(), ox + 5, 1, oz + 5, box);
		this.placeBlock(level, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH), ox + 3, 3, oz + 5, box);
	}

	// ------------------------------------------------------------ Bread Fort

	private void buildFort(WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
		this.fill(level, box, 0, 1, 0, 18, 8, 18, Blocks.AIR.defaultBlockState());
		this.fill(level, box, 0, 0, 0, 18, 0, 18, Blocks.COBBLESTONE.defaultBlockState());
		this.foundation(level, box, 0, 0, 18, 18, Blocks.COBBLESTONE.defaultBlockState());
		// Walls, some mossy, with battlements on top.
		for (int y = 1; y <= 4; y++) {
			for (int i = 0; i <= 18; i++) {
				for (int[] c : new int[][]{{i, 0}, {i, 18}, {0, i}, {18, i}}) {
					this.placeBlock(level, random.nextInt(5) == 0 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : bricks, c[0], y, c[1], box);
				}
			}
		}
		for (int i = 0; i <= 18; i += 2) {
			for (int[] c : new int[][]{{i, 0}, {i, 18}, {0, i}, {18, i}}) {
				this.placeBlock(level, bricks, c[0], 5, c[1], box);
			}
		}
		// Corner towers.
		for (int[] c : new int[][]{{0, 0}, {16, 0}, {0, 16}, {16, 16}}) {
			this.fill(level, box, c[0], 1, c[1], c[0] + 2, 6, c[1] + 2, bricks);
			this.placeBlock(level, Blocks.LANTERN.defaultBlockState(), c[0] + 1, 7, c[1] + 1, box);
		}
		// Gate.
		this.fill(level, box, 8, 1, 0, 10, 3, 0, Blocks.AIR.defaultBlockState());
		this.placeBlock(level, Blocks.RED_BANNER.defaultBlockState(), 7, 5, 0, box);
		this.placeBlock(level, Blocks.RED_BANNER.defaultBlockState(), 11, 5, 0, box);
		// Training yard: hay dummies and a target.
		this.placeBlock(level, Blocks.HAY_BLOCK.defaultBlockState(), 4, 1, 4, box);
		this.placeBlock(level, Blocks.CARVED_PUMPKIN.defaultBlockState(), 4, 2, 4, box);
		this.placeBlock(level, Blocks.HAY_BLOCK.defaultBlockState(), 14, 1, 4, box);
		this.placeBlock(level, Blocks.TARGET.defaultBlockState(), 14, 2, 4, box);
		// Barracks along the back wall, the captain's corner and the treasure chest.
		Block[] beds = {Blocks.RED_BED, Blocks.RED_BED, Blocks.RED_BED};
		for (int i = 0; i < 3; i++) {
			this.bed(level, box, 4 + i * 2, 1, 15, beds[i]);
		}
		this.bed(level, box, 13, 1, 15, Blocks.BLACK_BED);
		this.createChest(level, box, random, 15, 1, 16, ModStructures.FORT_CHEST);
		this.placeBlock(level, Blocks.ANVIL.defaultBlockState(), 15, 1, 13, box);
		for (int x : new int[]{3, 9, 15}) {
			this.placeBlock(level, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH), x, 3, 17, box);
		}

		BlockPos spawn = this.getWorldPos(9, 1, 9);
		if (!this.spawned && box.isInside(spawn)) {
			this.spawned = true;
			UUID group = Mth.createInsecureUUID(RandomSource.create(this.groupSeed));
			BlockPos yard = this.getWorldPos(9, 1, 8);
			BlockPos captainHome = this.getWorldPos(12, 1, 12);
			BlockPos[] knightBeds = {this.getWorldPos(4, 1, 16), this.getWorldPos(6, 1, 16), this.getWorldPos(8, 1, 16)};
			BlockPos captainBed = this.getWorldPos(13, 1, 16);
			this.placeMarker(level, spawn, marker -> {
				for (BlockPos bed : knightBeds) {
					marker.addResident(CharacterVariant.KNIGHT, spawn, yard, 6, bed, group, null);
				}
				marker.addResident(CharacterVariant.CAPTAIN, spawn, captainHome, 4, captainBed, group, null);
			});
		}
	}

	// ------------------------------------------------------------ Watchtower

	private void buildWatchtower(WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
		BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
		this.fill(level, box, 0, 1, 0, 6, 19, 6, Blocks.AIR.defaultBlockState());
		this.fill(level, box, 0, 0, 0, 6, 0, 6, Blocks.COBBLESTONE.defaultBlockState());
		this.foundation(level, box, 0, 0, 6, 6, Blocks.COBBLESTONE.defaultBlockState());
		this.walls(level, box, 1, 1, 5, 5, 1, 14, planks);
		for (int[] c : new int[][]{{1, 1}, {5, 1}, {1, 5}, {5, 5}}) {
			this.fill(level, box, c[0], 1, c[1], c[0], 14, c[1], log);
		}
		// Doorway, and a ladder up the inside of the back wall.
		this.fill(level, box, 3, 1, 1, 3, 2, 1, Blocks.AIR.defaultBlockState());
		for (int y = 1; y <= 15; y++) {
			this.placeBlock(level, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.NORTH), 3, y, 4, box);
		}
		// Lookout platform with a railing and a roof on posts.
		for (int x = 0; x <= 6; x++) {
			for (int z = 0; z <= 6; z++) {
				if (x != 3 || z != 4) {
					this.placeBlock(level, planks, x, 15, z, box);
				}
			}
		}
		for (int i = 1; i <= 5; i++) {
			for (int[] c : new int[][]{{i, 0}, {i, 6}, {0, i}, {6, i}}) {
				this.placeBlock(level, Blocks.SPRUCE_FENCE.defaultBlockState(), c[0], 16, c[1], box);
			}
		}
		for (int[] c : new int[][]{{0, 0}, {6, 0}, {0, 6}, {6, 6}}) {
			this.fill(level, box, c[0], 16, c[1], c[0], 18, c[1], log);
		}
		this.fill(level, box, 0, 19, 0, 6, 19, 6, Blocks.SPRUCE_SLAB.defaultBlockState());
		this.createChest(level, box, random, 5, 16, 1, ModStructures.WATCHTOWER_CHEST);
		this.placeBlock(level, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 3, 18, 3, box);

		BlockPos spawn = this.getWorldPos(2, 16, 2);
		if (!this.spawned && box.isInside(spawn)) {
			this.spawned = true;
			BlockPos post = this.getWorldPos(3, 16, 2);
			this.placeMarker(level, spawn, marker -> marker.addResident(CharacterVariant.ARCHER, spawn, post, 2, null, null, null));
		}
	}

	// ------------------------------------------------------------ Bandit Camp

	private void buildBanditCamp(WorldGenLevel level, BoundingBox box, RandomSource random) {
		this.fill(level, box, 0, 1, 0, 14, 5, 14, Blocks.AIR.defaultBlockState());
		for (int x = 0; x <= 14; x++) {
			for (int z = 0; z <= 14; z++) {
				this.placeBlock(level, random.nextInt(3) == 0 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.PODZOL.defaultBlockState(), x, 0, z, box);
			}
		}
		this.foundation(level, box, 0, 0, 14, 14, Blocks.DIRT.defaultBlockState());
		this.placeBlock(level, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 7, 1, 7, box);
		BlockState bench = Blocks.DARK_OAK_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Z);
		this.placeBlock(level, bench, 5, 1, 7, box);
		this.placeBlock(level, bench, 9, 1, 7, box);
		Block[] wool = {Blocks.BLACK_WOOL, Blocks.GRAY_WOOL, Blocks.BROWN_WOOL, Blocks.BLACK_WOOL};
		int[][] tents = {{1, 1}, {11, 1}, {1, 10}, {11, 10}};
		int bandits = Math.min(4, this.members + 1);
		for (int i = 0; i < bandits; i++) {
			this.smallTent(level, box, tents[i][0], tents[i][1], wool[i].defaultBlockState());
		}
		this.createChest(level, box, random, 7, 1, 12, ModStructures.BANDIT_CHEST);
		this.placeBlock(level, Blocks.BLACK_BANNER.defaultBlockState(), 7, 1, 13, box);
		this.placeBlock(level, Blocks.HAY_BLOCK.defaultBlockState(), 12, 1, 7, box);
		this.barrel(level, box, random, 2, 1, 7, ModStructures.BARREL_LOOT);

		BlockPos spawn = this.getWorldPos(7, 1, 9);
		if (!this.spawned && box.isInside(spawn)) {
			this.spawned = true;
			UUID group = Mth.createInsecureUUID(RandomSource.create(this.groupSeed));
			BlockPos campfire = this.getWorldPos(7, 1, 8);
			BlockPos[] beds = new BlockPos[bandits];
			for (int i = 0; i < bandits; i++) {
				beds[i] = this.getWorldPos(tents[i][0] + 1, 1, tents[i][1] + 2);
			}
			this.placeMarker(level, spawn, marker -> {
				for (BlockPos bed : beds) {
					marker.addResident(CharacterVariant.BANDIT, spawn, campfire, 8, bed, group, null);
				}
			});
		}
	}

	/** A 3x4 A-frame tent with its corner at (ox, oz), entrance at the front, one bed inside. */
	void smallTent(WorldGenLevel level, BoundingBox box, int ox, int oz, BlockState wool) {
		for (int z = oz; z <= oz + 3; z++) {
			this.placeBlock(level, wool, ox, 1, z, box);
			this.placeBlock(level, wool, ox + 2, 1, z, box);
			this.placeBlock(level, wool, ox + 1, 2, z, box);
		}
		this.placeBlock(level, wool, ox + 1, 1, oz + 3, box);
		this.bed(level, box, ox + 1, 1, oz + 1, Blocks.BROWN_BED);
	}

	// ------------------------------------------------------------ Lily's Treehouse

	private void buildTreehouse(WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState log = Blocks.OAK_LOG.defaultBlockState();
		BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
		BlockState leaves = Blocks.CHERRY_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true);
		this.fill(level, box, 0, 1, 0, 12, 15, 12, Blocks.AIR.defaultBlockState());
		this.fill(level, box, 0, 0, 0, 12, 0, 12, Blocks.GRASS_BLOCK.defaultBlockState());
		this.foundation(level, box, 0, 0, 12, 12, Blocks.DIRT.defaultBlockState());
		// Platform, then the trunk through it.
		this.fill(level, box, 2, 8, 2, 10, 8, 10, planks);
		this.fill(level, box, 5, 1, 5, 6, 13, 6, log);
		// Little house on the platform.
		this.walls(level, box, 3, 3, 9, 9, 9, 11, Blocks.BIRCH_PLANKS.defaultBlockState());
		this.fill(level, box, 3, 12, 3, 9, 12, 9, Blocks.CHERRY_SLAB.defaultBlockState());
		this.door(level, box, 8, 9, 3, Blocks.CHERRY_DOOR);
		BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
		this.placeBlock(level, glass, 3, 10, 6, box);
		this.placeBlock(level, glass, 9, 10, 6, box);
		this.placeBlock(level, glass, 5, 10, 9, box);
		// Library inside.
		for (int x = 4; x <= 8; x++) {
			this.fill(level, box, x, 9, 8, x, 10, 8, Blocks.BOOKSHELF.defaultBlockState());
		}
		this.bed(level, box, 4, 9, 6, Blocks.PINK_BED);
		this.placeBlock(level, Blocks.LECTERN.defaultBlockState(), 8, 9, 7, box);
		this.createChest(level, box, random, 8, 9, 5, ModStructures.TREEHOUSE_CHEST);
		this.placeBlock(level, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 6, 11, 7, box);
		// Railing around the platform, open at the top of the stairs.
		for (int i = 2; i <= 10; i++) {
			for (int[] c : new int[][]{{i, 2}, {i, 10}, {2, i}, {10, i}}) {
				if (!(c[0] == 10 && (c[1] == 4 || c[1] == 5))) {
					this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), c[0], 9, c[1], box);
				}
			}
		}
		// Canopy of cherry leaves.
		for (int x = 1; x <= 11; x++) {
			for (int z = 1; z <= 11; z++) {
				for (int y = 13; y <= 14; y++) {
					boolean edge = x == 1 || x == 11 || z == 1 || z == 11;
					if (y == 13 && !edge && x >= 3 && x <= 9 && z >= 3 && z <= 9) {
						continue;
					}
					if (random.nextInt(6) != 0) {
						this.placeBlock(level, leaves, x, y, z, box);
					}
				}
			}
		}
		// Stairs up the east side (walking north), on fence posts.
		for (int y = 1; y <= 7; y++) {
			int z = 13 - y;
			this.placeBlock(level, Blocks.OAK_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH), 11, y, z, box);
			for (int below = 1; below < y; below++) {
				this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), 11, below, z, box);
			}
		}
		this.fill(level, box, 11, 8, 4, 11, 8, 5, planks);

		BlockPos spawn = this.getWorldPos(7, 9, 4);
		if (!this.spawned && box.isInside(spawn)) {
			this.spawned = true;
			BlockPos home = this.getWorldPos(7, 9, 6);
			BlockPos bed = this.getWorldPos(4, 9, 7);
			this.placeMarker(level, spawn, marker -> marker.addResident(CharacterVariant.BOOKWORM, spawn, home, 6, bed, null, null));
		}
	}

	// ------------------------------------------------------------ Madison's Bakery

	private void buildBakery(WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
		BlockState log = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
		this.fill(level, box, 0, 1, 0, 10, 7, 11, Blocks.AIR.defaultBlockState());
		this.fill(level, box, 0, 0, 0, 10, 0, 11, Blocks.STONE_BRICKS.defaultBlockState());
		this.foundation(level, box, 0, 0, 10, 11, Blocks.COBBLESTONE.defaultBlockState());
		this.walls(level, box, 0, 0, 10, 11, 1, 4, planks);
		for (int[] c : new int[][]{{0, 0}, {10, 0}, {0, 11}, {10, 11}}) {
			this.fill(level, box, c[0], 1, c[1], c[0], 5, c[1], log);
		}
		this.fill(level, box, 0, 5, 0, 10, 5, 11, planks);
		this.fill(level, box, 1, 6, 1, 9, 6, 10, Blocks.SPRUCE_SLAB.defaultBlockState());
		this.door(level, box, 5, 1, 0, Blocks.SPRUCE_DOOR);
		BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
		for (int x : new int[]{2, 3, 7, 8}) {
			this.placeBlock(level, glass, x, 2, 0, box);
		}
		this.placeBlock(level, Blocks.POTTED_RED_TULIP.defaultBlockState(), 2, 1, 1, box);
		this.placeBlock(level, Blocks.POTTED_DANDELION.defaultBlockState(), 8, 1, 1, box);
		// Counter with cakes on it (gap on the right to walk behind).
		this.fill(level, box, 2, 1, 5, 7, 1, 5, Blocks.SMOOTH_STONE.defaultBlockState());
		this.placeBlock(level, Blocks.CAKE.defaultBlockState(), 3, 2, 5, box);
		this.placeBlock(level, Blocks.CAKE.defaultBlockState(), 6, 2, 5, box);
		// Ovens and the bread chest at the back, her bed in the corner.
		this.placeBlock(level, Blocks.SMOKER.defaultBlockState().setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING, Direction.NORTH), 3, 1, 10, box);
		this.placeBlock(level, Blocks.FURNACE.defaultBlockState().setValue(net.minecraft.world.level.block.FurnaceBlock.FACING, Direction.NORTH), 4, 1, 10, box);
		this.placeBlock(level, Blocks.CRAFTING_TABLE.defaultBlockState(), 5, 1, 10, box);
		this.placeBlock(level, Blocks.HAY_BLOCK.defaultBlockState(), 6, 1, 10, box);
		this.createChest(level, box, random, 8, 1, 10, ModStructures.BAKERY_CHEST);
		this.bed(level, box, 1, 1, 8, Blocks.YELLOW_BED);
		this.placeBlock(level, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 5, 4, 6, box);

		BlockPos spawn = this.getWorldPos(5, 1, 7);
		if (!this.spawned && box.isInside(spawn)) {
			this.spawned = true;
			BlockPos bed = this.getWorldPos(1, 1, 9);
			// She keeps an eye on her bread chest like Madison watches her pantry.
			BlockPos chest = this.getWorldPos(8, 1, 10);
			this.placeMarker(level, spawn, marker -> marker.addResident(CharacterVariant.BAKER, spawn, spawn, 3, bed, null, chest));
		}
	}

	// ------------------------------------------------------------ Master's Sky Arena

	private void buildSkyArena(WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
		int top = 30;
		this.fill(level, box, 7, 0, 7, 13, 0, 13, bricks);
		this.foundation(level, box, 7, 7, 13, 13, bricks);
		// The pillar, with a ladder up its south face.
		this.fill(level, box, 9, 1, 9, 11, top - 1, 11, bricks);
		for (int y = 1; y <= top; y++) {
			this.placeBlock(level, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.SOUTH), 10, y, 12, box);
		}
		// Round platform: polished blackstone with an obsidian rim, open sky above.
		for (int x = 0; x <= 20; x++) {
			for (int z = 0; z <= 20; z++) {
				int dx = x - 10;
				int dz = z - 10;
				int d2 = dx * dx + dz * dz;
				if (d2 > 100 || x == 10 && z == 12) {
					continue;
				}
				this.fill(level, box, x, top + 1, z, x, top + 7, z, Blocks.AIR.defaultBlockState());
				this.placeBlock(level, d2 >= 81 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.POLISHED_BLACKSTONE.defaultBlockState(), x, top, z, box);
			}
		}
		for (int[] c : new int[][]{{2, 10}, {18, 10}, {10, 2}, {10, 18}}) {
			this.fill(level, box, c[0], top + 1, c[1], c[0], top + 2, c[1], Blocks.CRYING_OBSIDIAN.defaultBlockState());
			this.placeBlock(level, Blocks.SOUL_LANTERN.defaultBlockState(), c[0], top + 3, c[1], box);
		}
		this.createChest(level, box, random, 10, top + 1, 17, ModStructures.ARENA_CHEST);

		BlockPos spawn = this.getWorldPos(10, top + 1, 10);
		if (!this.spawned && box.isInside(spawn)) {
			this.spawned = true;
			this.placeMarker(level, spawn, marker -> marker.addResident(CharacterVariant.MASTER, spawn, spawn, 8, null, null, null));
		}
	}

	// ------------------------------------------------------------ Pirate Ship

	private void buildPirateShip(WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState hull = Blocks.DARK_OAK_PLANKS.defaultBlockState();
		BlockState deck = Blocks.SPRUCE_PLANKS.defaultBlockState();
		// Hull (solid, so the sea stays out) and deck; pointed at the bow (z = 20), narrower at the stern.
		for (int z = 0; z <= 20; z++) {
			int half = z <= 2 ? z + 1 : z >= 17 ? Math.max(0, 20 - z) : 3;
			for (int x = 4 - half; x <= 4 + half; x++) {
				for (int y = -2; y <= 0; y++) {
					if (y == -2 && (x == 4 - half || x == 4 + half) && half > 0) {
						continue;
					}
					this.placeBlock(level, hull, x, y, z, box);
				}
				this.placeBlock(level, deck, x, 1, z, box);
				this.fill(level, box, x, 2, z, x, 11, z, Blocks.AIR.defaultBlockState());
			}
			this.placeBlock(level, Blocks.DARK_OAK_FENCE.defaultBlockState(), 4 - half, 2, z, box);
			this.placeBlock(level, Blocks.DARK_OAK_FENCE.defaultBlockState(), 4 + half, 2, z, box);
		}
		// Masts with sails and the black flag.
		this.fill(level, box, 4, 2, 10, 4, 10, 10, Blocks.DARK_OAK_LOG.defaultBlockState());
		this.fill(level, box, 2, 5, 11, 6, 9, 11, Blocks.WHITE_WOOL.defaultBlockState());
		this.placeBlock(level, Blocks.BLACK_BANNER.defaultBlockState(), 4, 11, 10, box);
		this.fill(level, box, 4, 2, 15, 4, 7, 15, Blocks.DARK_OAK_LOG.defaultBlockState());
		this.fill(level, box, 3, 4, 16, 5, 6, 16, Blocks.WHITE_WOOL.defaultBlockState());
		// Captain's cabin at the stern.
		this.walls(level, box, 2, 2, 6, 6, 2, 4, hull);
		this.fill(level, box, 2, 5, 2, 6, 5, 6, Blocks.DARK_OAK_SLAB.defaultBlockState());
		this.fill(level, box, 4, 2, 6, 4, 3, 6, Blocks.AIR.defaultBlockState());
		this.createChest(level, box, random, 3, 2, 3, ModStructures.PIRATE_CHEST);
		this.bed(level, box, 5, 2, 3, Blocks.RED_BED);
		this.placeBlock(level, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 4, 4, 4, box);
		// Cargo.
		this.barrel(level, box, random, 2, 2, 13, ModStructures.BARREL_LOOT);
		this.barrel(level, box, random, 6, 2, 13, ModStructures.BARREL_LOOT);
		this.barrel(level, box, random, 6, 2, 12, ModStructures.BARREL_LOOT);

		BlockPos spawn = this.getWorldPos(4, 2, 12);
		if (!this.spawned && box.isInside(spawn)) {
			this.spawned = true;
			UUID group = Mth.createInsecureUUID(RandomSource.create(this.groupSeed));
			int pirates = 2 + (this.members > 2 ? 1 : 0);
			BlockPos captainBed = this.getWorldPos(5, 2, 4);
			this.placeMarker(level, spawn, marker -> {
				for (int i = 0; i < pirates; i++) {
					marker.addResident(CharacterVariant.PIRATE, spawn, spawn, 6, i == 0 ? captainBed : null, group, null);
				}
			});
		}
	}

	/** A beehive full of honey (with two bees inside) on top of a lit campfire. */
	void beehive(WorldGenLevel level, BoundingBox box, RandomSource random, int x, int y, int z) {
		this.placeBlock(level, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), x, y, z, box);
		this.placeBlock(level, Blocks.BEEHIVE.defaultBlockState()
				.setValue(net.minecraft.world.level.block.BeehiveBlock.FACING, Direction.SOUTH)
				.setValue(net.minecraft.world.level.block.BeehiveBlock.HONEY_LEVEL, net.minecraft.world.level.block.BeehiveBlock.MAX_HONEY_LEVELS), x, y + 1, z, box);
		BlockPos pos = this.getWorldPos(x, y + 1, z);
		if (box.isInside(pos) && level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.BeehiveBlockEntity hive) {
			for (int i = 0; i < 2; i++) {
				hive.storeBee(net.minecraft.world.level.block.entity.BeehiveBlockEntity.Occupant.create(random.nextInt(599)));
			}
		}
	}

	private void buildHouse(WorldGenLevel level, BoundingBox box, RandomSource random, Palette p) {
		this.fill(level, box, 0, 1, 0, 6, 6, 6, Blocks.AIR.defaultBlockState());
		this.fill(level, box, 0, 0, 0, 6, 0, 6, p.planks());
		this.foundation(level, box, 0, 0, 6, 6, p.foundation());
		// Walls with log corners.
		this.walls(level, box, 0, 0, 6, 6, 1, 3, p.planks());
		for (int[] c : new int[][]{{0, 0}, {6, 0}, {0, 6}, {6, 6}}) {
			this.fill(level, box, c[0], 1, c[1], c[0], 4, c[1], p.corner());
		}
		// Roof: a plank layer and a slab layer on top.
		this.fill(level, box, 0, 4, 0, 6, 4, 6, p.planks());
		this.fill(level, box, 1, 5, 1, 5, 5, 5, p.slab());
		// Door and windows.
		this.door(level, box, 3, 1, 0, p.door());
		BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
		this.placeBlock(level, glass, 1, 2, 0, box);
		this.placeBlock(level, glass, 5, 2, 0, box);
		this.placeBlock(level, glass, 0, 2, 3, box);
		this.placeBlock(level, glass, 6, 2, 3, box);
		// Furniture.
		this.bed(level, box, 1, 1, 4, Blocks.RED_BED);
		this.placeBlock(level, Blocks.CRAFTING_TABLE.defaultBlockState(), 5, 1, 5, box);
		this.placeBlock(level, Blocks.FURNACE.defaultBlockState().setValue(net.minecraft.world.level.block.FurnaceBlock.FACING, Direction.NORTH), 4, 1, 5, box);
		this.createChest(level, box, random, 5, 1, 3, ModStructures.HOUSE_CHEST);
		this.placeBlock(level, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH), 3, 3, 5, box);

		BlockPos spawn = this.getWorldPos(3, 1, 3);
		if (!this.spawned && box.isInside(spawn)) {
			this.spawned = true;
			BlockPos bed = this.getWorldPos(1, 1, 5);
			this.placeMarker(level, spawn, marker -> marker.addResident(CharacterVariant.BREADLOAF, spawn, spawn, 10, bed, null, null));
		}
	}

	private void buildTent(WorldGenLevel level, BoundingBox box, RandomSource random, Palette p) {
		BlockState wool = WOOL[this.woolColor].defaultBlockState();
		this.fill(level, box, 0, 1, 0, 8, 5, 9, Blocks.AIR.defaultBlockState());
		this.fill(level, box, 0, 0, 0, 8, 0, 9, p.ground());
		this.foundation(level, box, 0, 0, 8, 9, Blocks.DIRT.defaultBlockState());
		// A-frame from z = 3 (front) to z = 9 (back).
		for (int z = 3; z <= 9; z++) {
			this.placeBlock(level, wool, 1, 1, z, box);
			this.placeBlock(level, wool, 7, 1, z, box);
			this.placeBlock(level, wool, 2, 2, z, box);
			this.placeBlock(level, wool, 6, 2, z, box);
			this.placeBlock(level, wool, 3, 3, z, box);
			this.placeBlock(level, wool, 5, 3, z, box);
			this.placeBlock(level, wool, 4, 4, z, box);
		}
		// Closed back, front with a 1x2 entrance in the middle.
		this.fill(level, box, 2, 1, 9, 6, 1, 9, wool);
		this.fill(level, box, 3, 2, 9, 5, 2, 9, wool);
		this.placeBlock(level, wool, 4, 3, 9, box);
		for (int[] w : new int[][]{{2, 1}, {3, 1}, {5, 1}, {6, 1}, {3, 2}, {5, 2}, {4, 3}}) {
			this.placeBlock(level, wool, w[0], w[1], 3, box);
		}
		// One bed per member, a chest at the back, and a lantern hanging from the ridge.
		int[][] beds = {{2, 7}, {6, 7}, {2, 4}, {6, 4}};
		for (int i = 0; i < this.members; i++) {
			this.bed(level, box, beds[i][0], 1, beds[i][1], BEDS[i]);
		}
		this.createChest(level, box, random, 4, 1, 8, ModStructures.TENT_CHEST);
		this.placeBlock(level, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 4, 3, 6, box);
		// Campfire with two log benches in front of the tent.
		this.placeBlock(level, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 4, 1, 1, box);
		this.placeBlock(level, Blocks.OAK_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Z), 2, 1, 1, box);
		this.placeBlock(level, Blocks.OAK_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Z), 6, 1, 1, box);

		BlockPos spawn = this.getWorldPos(4, 1, 6);
		if (!this.spawned && box.isInside(spawn)) {
			this.spawned = true;
			UUID group = Mth.createInsecureUUID(RandomSource.create(this.groupSeed));
			BlockPos campfire = this.getWorldPos(4, 1, 1);
			this.placeMarker(level, spawn, marker -> {
				for (int i = 0; i < this.members; i++) {
					BlockPos bedHead = this.getWorldPos(beds[i][0], 1, beds[i][1] + 1);
					marker.addResident(CharacterVariant.BREADLOAF, spawn, campfire, 6, bedHead, group, null);
				}
			});
		}
	}

	private void buildLilyHouse(WorldGenLevel level, BoundingBox box, RandomSource random, Palette p) {
		this.fill(level, box, 0, 1, 0, 8, 6, 10, Blocks.AIR.defaultBlockState());
		// Grassy yard around the house, cherry floor inside.
		this.fill(level, box, 0, 0, 0, 8, 0, 10, Blocks.GRASS_BLOCK.defaultBlockState());
		this.foundation(level, box, 0, 0, 8, 10, Blocks.DIRT.defaultBlockState());
		this.fill(level, box, 1, 0, 1, 7, 0, 9, p.planks());
		// 7x9 cherry walls with log corners.
		this.walls(level, box, 1, 1, 7, 9, 1, 3, p.planks());
		for (int[] c : new int[][]{{1, 1}, {7, 1}, {1, 9}, {7, 9}}) {
			this.fill(level, box, c[0], 1, c[1], c[0], 4, c[1], p.corner());
		}
		this.fill(level, box, 1, 4, 1, 7, 4, 9, p.planks());
		this.fill(level, box, 2, 5, 2, 6, 5, 8, p.slab());
		this.door(level, box, 4, 1, 1, p.door());
		BlockState pinkGlass = Blocks.PINK_STAINED_GLASS_PANE.defaultBlockState();
		this.placeBlock(level, pinkGlass, 2, 2, 1, box);
		this.placeBlock(level, pinkGlass, 6, 2, 1, box);
		this.placeBlock(level, pinkGlass, 1, 2, 5, box);
		this.placeBlock(level, pinkGlass, 7, 2, 5, box);
		// Cozy inside: pink bed, bookshelves with a lantern, a potted flower, a rug and a chest.
		this.bed(level, box, 2, 1, 7, Blocks.PINK_BED);
		this.fill(level, box, 4, 1, 8, 6, 2, 8, Blocks.BOOKSHELF.defaultBlockState());
		this.placeBlock(level, Blocks.LANTERN.defaultBlockState(), 5, 3, 8, box);
		this.placeBlock(level, Blocks.POTTED_PINK_TULIP.defaultBlockState(), 6, 1, 2, box);
		this.fill(level, box, 3, 1, 4, 4, 1, 5, Blocks.PINK_CARPET.defaultBlockState());
		this.createChest(level, box, random, 6, 1, 5, ModStructures.LILY_HOUSE_CHEST);
		// Pink petals scattered around the yard.
		for (int x = 0; x <= 8; x++) {
			for (int z = 0; z <= 10; z++) {
				boolean yard = x == 0 || x == 8 || z == 0 || z == 10;
				if (yard && random.nextFloat() < 0.6F) {
					BlockState petals = Blocks.PINK_PETALS.defaultBlockState()
							.setValue(FlowerBedBlock.AMOUNT, 1 + random.nextInt(4))
							.setValue(FlowerBedBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
					this.placeBlock(level, petals, x, 1, z, box);
				}
			}
		}

		BlockPos spawn = this.getWorldPos(4, 1, 4);
		if (!this.spawned && box.isInside(spawn)) {
			this.spawned = true;
			BlockPos home = this.getWorldPos(4, 1, 5);
			BlockPos bed = this.getWorldPos(2, 1, 8);
			CharacterVariant lilyType = new CharacterVariant[]{CharacterVariant.LILY, CharacterVariant.GARDENER, CharacterVariant.WITCHY}[random.nextInt(3)];
			this.placeMarker(level, spawn, marker -> marker.addResident(lilyType, spawn, home, LilyBrain.HOME_RADIUS, bed, null, null));
		}
	}

	private void buildMadisonHouse(WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState planks = Blocks.BIRCH_PLANKS.defaultBlockState();
		BlockState wall = Blocks.WHITE_CONCRETE.defaultBlockState();
		BlockState log = Blocks.BIRCH_LOG.defaultBlockState();
		this.fill(level, box, 0, 1, 0, 10, 6, 13, Blocks.AIR.defaultBlockState());
		this.fill(level, box, 0, 0, 0, 10, 0, 13, Blocks.GRASS_BLOCK.defaultBlockState());
		this.foundation(level, box, 0, 0, 10, 13, Blocks.DIRT.defaultBlockState());
		// Garden out front: three rows of crops on farmland, two water blocks, and a path to the door.
		BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7);
		Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES};
		for (int z = 1; z <= 3; z++) {
			for (int x = 1; x <= 9; x++) {
				if (x == 5) {
					this.placeBlock(level, Blocks.DIRT_PATH.defaultBlockState(), x, 0, z, box);
				} else if (z == 2 && (x == 3 || x == 7)) {
					this.placeBlock(level, Blocks.WATER.defaultBlockState(), x, 0, z, box);
				} else {
					this.placeBlock(level, farmland, x, 0, z, box);
					net.minecraft.world.level.block.CropBlock crop = (net.minecraft.world.level.block.CropBlock) crops[z - 1];
					this.placeBlock(level, crop.getStateForAge(3 + random.nextInt(crop.getMaxAge() - 2)), x, 1, z, box);
				}
			}
		}
		this.placeBlock(level, Blocks.DIRT_PATH.defaultBlockState(), 5, 0, 0, box);
		// 9x9 cottage: birch floor, white concrete walls, birch log corners.
		this.fill(level, box, 1, 0, 4, 9, 0, 12, planks);
		this.walls(level, box, 1, 4, 9, 12, 1, 3, wall);
		for (int[] c : new int[][]{{1, 4}, {9, 4}, {1, 12}, {9, 12}}) {
			this.fill(level, box, c[0], 1, c[1], c[0], 4, c[1], log);
		}
		this.fill(level, box, 1, 4, 4, 9, 4, 12, planks);
		this.fill(level, box, 2, 5, 5, 8, 5, 11, Blocks.BIRCH_SLAB.defaultBlockState());
		this.door(level, box, 5, 1, 4, Blocks.BIRCH_DOOR);
		BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
		this.placeBlock(level, glass, 3, 2, 4, box);
		this.placeBlock(level, glass, 7, 2, 4, box);
		this.placeBlock(level, glass, 1, 2, 8, box);
		this.placeBlock(level, glass, 9, 2, 8, box);
		this.placeBlock(level, glass, 5, 2, 12, box);
		// Inside: bed, barrel, flower pots, a torch and the pantry chest.
		this.bed(level, box, 2, 1, 10, Blocks.YELLOW_BED);
		this.barrel(level, box, random, 8, 1, 11, ModStructures.BARREL_LOOT);
		this.placeBlock(level, Blocks.POTTED_DANDELION.defaultBlockState(), 2, 1, 6, box);
		this.placeBlock(level, Blocks.POTTED_OXEYE_DAISY.defaultBlockState(), 8, 1, 6, box);
		this.placeBlock(level, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH), 5, 3, 11, box);
		this.createChest(level, box, random, 8, 1, 9, ModStructures.MADISON_PANTRY);
		// Two beehives over lit campfires (the smoke keeps the bees calm when honey is collected).
		this.beehive(level, box, random, 0, 1, 2);
		this.beehive(level, box, random, 10, 1, 2);

		BlockPos spawn = this.getWorldPos(5, 1, 8);
		if (!this.spawned && box.isInside(spawn)) {
			this.spawned = true;
			BlockPos bed = this.getWorldPos(2, 1, 11);
			// She watches this exact chest.
			BlockPos pantry = this.getWorldPos(8, 1, 9);
			CharacterVariant madisonType = new CharacterVariant[]{CharacterVariant.MADISON, CharacterVariant.FARMER, CharacterVariant.BEEKEEPER}[random.nextInt(3)];
			this.placeMarker(level, spawn, marker -> marker.addResident(madisonType, spawn, spawn, MadisonBrain.HOME_RADIUS, bed, null, pantry));
		}
	}

	/** An invisible marker where the residents will appear; it holds their details until a player comes near. */
	/** Nether pieces: drop the piece onto the floor under its center (the heightmap there is the ceiling). */
	/**
	 * Builds a structure right now in a loaded world (Village Campsites and Bastion Throne Rooms, which the mod adds while
	 * the game runs). {@code floorY} is the height of the ground layer (local y = 0). Returns the area it covers.
	 */
	public static BoundingBox buildNow(net.minecraft.server.level.ServerLevel level, HomeKind kind, int x, int z, int floorY, RandomSource random) {
		HomePiece piece = new HomePiece(kind, random, x, z);
		// A fixed height: postProcess then skips the ground averaging that world generation uses.
		piece.heightPosition = floorY;
		piece.boundingBox.move(0, floorY - piece.boundingBox.minY(), 0);
		BoundingBox area = piece.getBoundingBox();
		piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), random, area,
				new ChunkPos(area.getCenter()), area.getCenter());
		return area;
	}

	private void settleOnNetherFloor(WorldGenLevel level) {
		if (this.heightPosition >= 0) {
			return;
		}
		BlockPos center = this.getWorldPos(this.kind.width / 2, 0, this.kind.depth / 2);
		int floor = 64;
		for (int y = 100; y > 32; y--) {
			BlockPos at = new BlockPos(center.getX(), y, center.getZ());
			if (level.getBlockState(at).isAir() && level.getBlockState(at.above()).isAir()
					&& !level.getBlockState(at.below()).isAir() && level.getFluidState(at.below()).isEmpty()) {
				floor = y - 1;
				break;
			}
		}
		this.heightPosition = floor;
		this.boundingBox.move(0, floor - this.boundingBox.minY(), 0);
	}

	// ------------------------------------------------------------ helpers for the builder classes (LilyHomes, BreadHomes, ...)

	/** One block at local coordinates (rotated and mirrored with the piece). */
	void put(WorldGenLevel level, BoundingBox box, BlockState state, int x, int y, int z) {
		this.placeBlock(level, state, x, y, z, box);
	}

	/** World position of local coordinates. */
	BlockPos pos(int x, int y, int z) {
		return this.getWorldPos(x, y, z);
	}

	/** A loot chest at local coordinates. */
	void chest(WorldGenLevel level, BoundingBox box, RandomSource random, int x, int y, int z,
			net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot) {
		this.createChest(level, box, random, x, y, z, loot);
	}

	/** A barrel filled from a loot table (barrels everywhere have loot now, not just chests). */
	void barrel(WorldGenLevel level, BoundingBox box, RandomSource random, int x, int y, int z,
			net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot) {
		BlockPos at = this.getWorldPos(x, y, z);
		if (!box.isInside(at)) {
			return;
		}
		level.setBlock(at, Blocks.BARREL.defaultBlockState(), 2);
		net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, at, loot);
	}

	/** Fills downward from local y to the ground (pillars under raised floors). */
	void column(WorldGenLevel level, BoundingBox box, BlockState state, int x, int y, int z) {
		this.fillColumnDown(level, state, x, y, z, box);
	}

	/** True once, for the chunk that holds the residents' spawn spot. */
	boolean claimSpawn(BlockPos spawn, BoundingBox box) {
		if (this.spawned || !box.isInside(spawn)) {
			return false;
		}
		this.spawned = true;
		return true;
	}

	/** Shared group id for everyone living in this structure (they defend each other). */
	UUID group() {
		return Mth.createInsecureUUID(RandomSource.create(this.groupSeed));
	}

	/** 2-4, rolled once per structure. */
	int members() {
		return this.members;
	}

	HomeKind kind() {
		return this.kind;
	}

	void placeMarker(WorldGenLevel level, BlockPos pos, java.util.function.Consumer<ResidentMarkerBlockEntity> residents) {
		level.setBlock(pos, ModBlocks.RESIDENT_MARKER.defaultBlockState(), 2);
		if (level.getBlockEntity(pos) instanceof ResidentMarkerBlockEntity marker) {
			residents.accept(marker);
			marker.setChanged();
		}
	}

	// ------------------------------------------------------------ helpers (local coordinates)

	void fill(WorldGenLevel level, BoundingBox box, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				for (int z = z0; z <= z1; z++) {
					this.placeBlock(level, state, x, y, z, box);
				}
			}
		}
	}

	/** Only the outer ring of the rectangle, from y0 to y1. */
	void walls(WorldGenLevel level, BoundingBox box, int x0, int z0, int x1, int z1, int y0, int y1, BlockState state) {
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				this.placeBlock(level, state, x, y, z0, box);
				this.placeBlock(level, state, x, y, z1, box);
			}
			for (int z = z0; z <= z1; z++) {
				this.placeBlock(level, state, x0, y, z, box);
				this.placeBlock(level, state, x1, y, z, box);
			}
		}
	}

	/** Fills any gap under the floor down to the ground, so nothing floats on slopes. */
	void foundation(WorldGenLevel level, BoundingBox box, int x0, int z0, int x1, int z1, BlockState state) {
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				this.fillColumnDown(level, state, x, -1, z, box);
			}
		}
	}

	void door(WorldGenLevel level, BoundingBox box, int x, int y, int z, Block door) {
		BlockState lower = door.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
		this.placeBlock(level, lower, x, y, z, box);
		this.placeBlock(level, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), x, y + 1, z, box);
	}

	/** A bed with its foot at z and its head at z + 1 (toward the back). */
	void bed(WorldGenLevel level, BoundingBox box, int x, int y, int z, Block bed) {
		// Work out the real world positions first and point the bed from foot to head, so both halves always match
		// no matter how the structure is rotated or mirrored (a mismatched half breaks itself).
		BlockPos footPos = this.getWorldPos(x, y, z);
		BlockPos headPos = this.getWorldPos(x, y, z + 1);
		if (!box.isInside(footPos) || !box.isInside(headPos)) {
			return;
		}
		Direction facing = Direction.getApproximateNearest(headPos.getX() - footPos.getX(), 0, headPos.getZ() - footPos.getZ());
		BlockState foot = bed.defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, BedPart.FOOT);
		level.setBlock(footPos, foot, 2);
		level.setBlock(headPos, foot.setValue(BedBlock.PART, BedPart.HEAD), 2);
	}
}
