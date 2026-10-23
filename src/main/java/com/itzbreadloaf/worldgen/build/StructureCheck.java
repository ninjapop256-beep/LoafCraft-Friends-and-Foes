package com.itzbreadloaf.worldgen.build;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * {@code /loafcraft checkstructures}: builds every blueprint in memory (no world needed) in several biomes' palettes
 * and with several seeds, and runs the reachability check on each. Lists every floor, chest, bed or resident spot that
 * can't be reached, or says they're all fine.
 */
public final class StructureCheck {
	private static final List<ResourceKey<Biome>> OVERWORLD = List.of(Biomes.PLAINS, Biomes.DESERT, Biomes.SNOWY_TAIGA, Biomes.CHERRY_GROVE,
			Biomes.BADLANDS, Biomes.JUNGLE, Biomes.MANGROVE_SWAMP);
	private static final int SEEDS = 4;

	private StructureCheck() {
	}

	public static List<String> run(MinecraftServer server) {
		var biomes = server.registryAccess().lookupOrThrow(Registries.BIOME);
		List<String> report = new ArrayList<>();
		int built = 0;
		for (StructureCatalog.Def def : StructureCatalog.all()) {
			if (!Blueprints.has(def.id())) {
				report.add(def.id() + ": no layout");
				continue;
			}
			List<ResourceKey<Biome>> keys = switch (def.placement()) {
				case NETHER_FLOOR -> List.of(Biomes.NETHER_WASTES, Biomes.CRIMSON_FOREST, Biomes.WARPED_FOREST);
				case ISLAND -> List.of(Biomes.END_HIGHLANDS);
				default -> OVERWORLD;
			};
			for (ResourceKey<Biome> key : keys) {
				Holder<Biome> biome = biomes.getOrThrow(key);
				for (int seed = 0; seed < SEEDS; seed++) {
					Blueprint b = new Blueprint(def.width(), def.depth(), def.height(), Palette.forBiome(biome), RandomSource.create(seed));
					Blueprints.build(def.id(), b);
					built++;
					for (String problem : Reachability.check(b)) {
						String line = def.id() + " (" + key.identifier().getPath() + "): " + problem;
						if (!report.contains(line)) {
							report.add(line);
						}
					}
				}
			}
		}
		report.add(0, report.isEmpty() ? "All " + built + " test builds passed: every floor, chest, bed and resident is reachable."
				: report.size() + " problem(s) in " + built + " test builds:");
		return report;
	}
}
