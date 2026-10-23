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
		if (this.yieldsToNeighbor(context)) {
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

	/**
	 * Keeps LoafCraft structures off each other: if a bigger LoafCraft structure (from any of our structure sets) would
	 * start close enough to overlap this one, this one doesn't generate. Bigger wins; equal sizes go to the earlier kind.
	 * It works out where the other sets place their structures from the world seed, the same way the game does, and
	 * checks the biome there. (Vanilla villages are kept away with "exclusion_zone" in each structure_set file.)
	 */
	private boolean yieldsToNeighbor(GenerationContext context) {
		ChunkPos here = context.chunkPos();
		int mySize = Math.max(this.kind.width, this.kind.depth);
		for (StructureSet set : context.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET)) {
			if (!(set.placement() instanceof RandomSpreadStructurePlacement spread)) {
				continue;
			}
			for (StructureSet.StructureSelectionEntry entry : set.structures()) {
				if (!(entry.structure().value() instanceof HomeStructure other) || other.kind == this.kind || !other.outranks(this)) {
					continue;
				}
				int reach = (mySize + Math.max(other.kind.width, other.kind.depth)) / 2 + 6;
				int reachChunks = reach / 16 + 1;
				int spacing = spread.spacing();
				for (int cx = Math.floorDiv(here.x - reachChunks, spacing); cx <= Math.floorDiv(here.x + reachChunks, spacing); cx++) {
					for (int cz = Math.floorDiv(here.z - reachChunks, spacing); cz <= Math.floorDiv(here.z + reachChunks, spacing); cz++) {
						ChunkPos start = spread.getPotentialStructureChunk(context.seed(), cx * spacing, cz * spacing);
						if (Math.abs(start.x - here.x) * 16 > reach || Math.abs(start.z - here.z) * 16 > reach) {
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

	private boolean outranks(HomeStructure other) {
		int mine = Math.max(this.kind.width, this.kind.depth);
		int theirs = Math.max(other.kind.width, other.kind.depth);
		return mine != theirs ? mine > theirs : this.kind.ordinal() < other.kind.ordinal();
	}

	@Override
	public StructureType<?> type() {
		return ModStructures.HOME;
	}
}
