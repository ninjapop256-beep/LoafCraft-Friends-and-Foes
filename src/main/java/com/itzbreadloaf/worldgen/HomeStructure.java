package com.itzbreadloaf.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * One structure type for all three homes. The worldgen JSON picks which one with "kind":
 * "house", "tent" or "lily_house". Biomes, spacing and rarity are set in the JSON files.
 */
public class HomeStructure extends Structure {
	public static final MapCodec<HomeStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			settingsCodec(instance),
			HomeKind.CODEC.fieldOf("kind").forGetter(structure -> structure.kind)
	).apply(instance, HomeStructure::new));

	private final HomeKind kind;

	public HomeStructure(StructureSettings settings, HomeKind kind) {
		super(settings);
		this.kind = kind;
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunk = context.chunkPos();
		if (this.yieldsToNeighbor(context) || this.kind.needsRivalNearby() && !this.rivalHomeNearby(context)) {
			return Optional.empty();
		}
		int x = chunk.getMiddleBlockX();
		int z = chunk.getMiddleBlockZ();
		// Homes never on water (lakes, rivers, ocean edges); the pirate ship only on water.
		NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
		if (this.kind.inNether()) {
			// The Nether: search down for an open floor (not lava) under the ceiling.
			for (int y = 100; y > 32; y--) {
				if (column.getBlock(y).isAir() && column.getBlock(y + 1).isAir() && !column.getBlock(y - 1).isAir()
						&& column.getBlock(y - 1).getFluidState().isEmpty()) {
					return Optional.of(new GenerationStub(new BlockPos(x, y, z), builder ->
							builder.addPiece(new HomePiece(this.kind, context.random(), chunk.getMinBlockX(), chunk.getMinBlockZ()))));
				}
			}
			return Optional.empty();
		}
		int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
				context.heightAccessor(), context.randomState());
		if (this.kind.needsSolidGround() && surface <= context.heightAccessor().getMinY() + 8) {
			return Optional.empty();
		}
		boolean water = !column.getBlock(surface - 1).getFluidState().isEmpty();
		if (water != this.kind.onWater() && !this.kind.anySurface()) {
			return Optional.empty();
		}
		return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder ->
				builder.addPiece(new HomePiece(this.kind, context.random(), chunk.getMinBlockX(), chunk.getMinBlockZ())));
	}

	/** Separation from other structures (ours, vanilla's and other mods'): see {@link com.itzbreadloaf.worldgen.build.PlacementRules}. */
	private boolean yieldsToNeighbor(GenerationContext context) {
		return com.itzbreadloaf.worldgen.build.PlacementRules.yields(context, this);
	}

	public HomeKind kind() {
		return this.kind;
	}

	/**
	 * Rival Camp: true if an ItzBreadLoaf house or tent will start 24-96 blocks away (close enough to see, far enough
	 * not to overlap), worked out from the world seed like {@link #yieldsToNeighbor}.
	 */
	private boolean rivalHomeNearby(GenerationContext context) {
		ChunkPos here = context.chunkPos();
		for (StructureSet set : context.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET)) {
			if (!(set.placement() instanceof RandomSpreadStructurePlacement spread)) {
				continue;
			}
			for (StructureSet.StructureSelectionEntry entry : set.structures()) {
				if (!(entry.structure().value() instanceof HomeStructure other)
						|| other.kind != HomeKind.HOUSE && other.kind != HomeKind.TENT) {
					continue;
				}
				int spacing = spread.spacing();
				for (int cx = Math.floorDiv(here.x - 6, spacing); cx <= Math.floorDiv(here.x + 6, spacing); cx++) {
					for (int cz = Math.floorDiv(here.z - 6, spacing); cz <= Math.floorDiv(here.z + 6, spacing); cz++) {
						ChunkPos start = spread.getPotentialStructureChunk(context.seed(), cx * spacing, cz * spacing);
						double blocks = Math.sqrt(Math.pow((start.x - here.x) * 16.0, 2) + Math.pow((start.z - here.z) * 16.0, 2));
						if (blocks < 24.0 || blocks > 96.0) {
							continue;
						}
						Holder<Biome> biome = context.biomeSource().getNoiseBiome(QuartPos.fromBlock(start.getMiddleBlockX()),
								QuartPos.fromBlock(64), QuartPos.fromBlock(start.getMiddleBlockZ()), context.randomState().sampler());
						if (other.biomes().contains(biome)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public StructureType<?> type() {
		return ModStructures.HOME;
	}
}
