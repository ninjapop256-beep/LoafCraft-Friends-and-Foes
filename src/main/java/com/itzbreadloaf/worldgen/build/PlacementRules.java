package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.ItzBreadLoaf;
import com.itzbreadloaf.worldgen.HomeStructure;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

/**
 * Separation: keeps every LoafCraft structure off other structures. Before one generates, it works out (from the
 * world seed, the way the game does) where every other random-spread structure set will try to place a structure
 * nearby, and checks the biome there. If one would overlap, this one doesn't generate:
 * <ul>
 *   <li>other LoafCraft structures: the bigger one wins (equal sizes: the one whose id sorts first);</li>
 *   <li>vanilla and other mods' structures (temples, outposts, ruined portals, other mods' buildings): they always
 *   win, and are assumed to be {@link #OTHER_SIZE} blocks across. Vanilla villages are also kept away by the
 *   "exclusion_zone" in each structure set.</li>
 * </ul>
 */
public final class PlacementRules {
	/** Assumed size of a structure we don't know the size of. */
	public static final int OTHER_SIZE = 24;
	/** Sets this dense are tiny scattered things (buried treasure...), not worth avoiding. */
	private static final int MIN_OTHER_SPACING = 8;

	private PlacementRules() {
	}

	/** Size used for overlap checks, or -1 for structures that aren't ours. */
	public static int footprint(Structure structure) {
		if (structure instanceof HomeStructure home) {
			return Math.max(home.kind().width, home.kind().depth);
		}
		if (structure instanceof BlueprintStructure blueprint && blueprint.def() != null) {
			return blueprint.def().footprint();
		}
		return -1;
	}

	private static String rankName(Structure structure) {
		if (structure instanceof HomeStructure home) {
			return home.kind().getSerializedName();
		}
		if (structure instanceof BlueprintStructure blueprint) {
			return blueprint.id();
		}
		return "";
	}

	/** True if {@code self} should not generate here because another structure would overlap it. */
	public static boolean yields(Structure.GenerationContext context, Structure self) {
		ChunkPos here = context.chunkPos();
		int mySize = footprint(self);
		String myName = rankName(self);
		for (StructureSet set : context.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET)) {
			if (!(set.placement() instanceof RandomSpreadStructurePlacement spread)) {
				continue;
			}
			for (StructureSet.StructureSelectionEntry entry : set.structures()) {
				Structure other = entry.structure().value();
				if (other == self) {
					continue;
				}
				int otherSize = footprint(other);
				if (otherSize < 0) {
					boolean ours = entry.structure().unwrapKey().map(k -> k.identifier().getNamespace().equals(ItzBreadLoaf.MOD_ID)).orElse(false);
					if (ours || spread.spacing() < MIN_OTHER_SPACING) {
						continue;
					}
					otherSize = OTHER_SIZE;
				} else if (!outranks(otherSize, rankName(other), mySize, myName)) {
					continue;
				}
				int reach = (mySize + otherSize) / 2 + 6;
				if (startsNear(context, spread, other, here, reach, 0)) {
					return true;
				}
			}
		}
		return false;
	}

	/** True if one of the LoafCraft structures with these ids will start between minBlocks and maxBlocks away. */
	public static boolean nearAny(Structure.GenerationContext context, java.util.List<String> ids, int minBlocks, int maxBlocks) {
		for (StructureSet set : context.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET)) {
			if (!(set.placement() instanceof RandomSpreadStructurePlacement spread)) {
				continue;
			}
			for (StructureSet.StructureSelectionEntry entry : set.structures()) {
				boolean wanted = entry.structure().unwrapKey().map(k -> k.identifier().getNamespace().equals(ItzBreadLoaf.MOD_ID)
						&& ids.contains(k.identifier().getPath())).orElse(false);
				if (wanted && startsNear(context, spread, entry.structure().value(), context.chunkPos(), maxBlocks, minBlocks)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean outranks(int size, String name, int otherSize, String otherName) {
		return size != otherSize ? size > otherSize : name.compareTo(otherName) < 0;
	}

	/**
	 * Whether {@code structure}'s set places a start (in a biome it can use) between minBlocks and reach blocks
	 * (box distance) from this chunk.
	 */
	public static boolean startsNear(Structure.GenerationContext context, RandomSpreadStructurePlacement spread, Structure structure,
			ChunkPos here, int reach, int minBlocks) {
		int reachChunks = reach / 16 + 1;
		int spacing = spread.spacing();
		for (int cx = Math.floorDiv(here.x - reachChunks, spacing); cx <= Math.floorDiv(here.x + reachChunks, spacing); cx++) {
			for (int cz = Math.floorDiv(here.z - reachChunks, spacing); cz <= Math.floorDiv(here.z + reachChunks, spacing); cz++) {
				ChunkPos start = spread.getPotentialStructureChunk(context.seed(), cx * spacing, cz * spacing);
				int dx = Math.abs(start.x - here.x) * 16;
				int dz = Math.abs(start.z - here.z) * 16;
				if (dx > reach || dz > reach || Math.max(dx, dz) < minBlocks) {
					continue;
				}
				Holder<Biome> biome = context.biomeSource().getNoiseBiome(QuartPos.fromBlock(start.getMiddleBlockX()),
						QuartPos.fromBlock(64), QuartPos.fromBlock(start.getMiddleBlockZ()), context.randomState().sampler());
				if (structure.biomes().contains(biome)) {
					return true;
				}
			}
		}
		return false;
	}
}
