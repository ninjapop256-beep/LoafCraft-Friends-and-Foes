package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.worldgen.ModStructures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jetbrains.annotations.Nullable;

/**
 * A structure built from a blueprint. Its worldgen JSON ("type": "itzbreadloaf:blueprint", "id": ...) is written by
 * {@link StructureCatalogExport}; everything else about it (size, placement, layout) comes from the catalog and
 * {@link Blueprints} by that id.
 */
public class BlueprintStructure extends Structure {
	public static final MapCodec<BlueprintStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			settingsCodec(instance),
			Codec.STRING.fieldOf("id").forGetter(structure -> structure.id)
	).apply(instance, BlueprintStructure::new));

	private final String id;

	public BlueprintStructure(StructureSettings settings, String id) {
		super(settings);
		this.id = id;
	}

	public String id() {
		return this.id;
	}

	public @Nullable StructureCatalog.Def def() {
		return StructureCatalog.get(this.id);
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		StructureCatalog.Def def = this.def();
		if (def == null || def.placement() == StructureCatalog.Placement.RUNTIME || !Blueprints.has(this.id)
				|| PlacementRules.yields(context, this) || !def.near().isEmpty() && !PlacementRules.nearAny(context, def.near(), 24, 96)) {
			return Optional.empty();
		}
		ChunkPos chunk = context.chunkPos();
		int x = chunk.getMiddleBlockX();
		int z = chunk.getMiddleBlockZ();
		NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
		if (def.placement() == StructureCatalog.Placement.NETHER_FLOOR) {
			for (int y = 100; y > 32; y--) {
				if (column.getBlock(y).isAir() && column.getBlock(y + 1).isAir() && !column.getBlock(y - 1).isAir()
						&& column.getBlock(y - 1).getFluidState().isEmpty()) {
					return Optional.of(new GenerationStub(new BlockPos(x, y, z), builder ->
							builder.addPiece(new BlueprintPiece(this.id, context.random(), chunk.getMinBlockX(), chunk.getMinBlockZ()))));
				}
			}
			return Optional.empty();
		}
		int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
		if (def.placement() == StructureCatalog.Placement.ISLAND && surface <= context.heightAccessor().getMinY() + 8) {
			return Optional.empty();
		}
		boolean water = !column.getBlock(surface - 1).getFluidState().isEmpty();
		if (def.placement() == StructureCatalog.Placement.LAND && water || def.placement() == StructureCatalog.Placement.WATER && !water) {
			return Optional.empty();
		}
		return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder ->
				builder.addPiece(new BlueprintPiece(this.id, context.random(), chunk.getMinBlockX(), chunk.getMinBlockZ())));
	}

	@Override
	public StructureType<?> type() {
		return ModStructures.BLUEPRINT;
	}
}
