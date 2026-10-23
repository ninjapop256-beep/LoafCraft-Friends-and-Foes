package com.itzbreadloaf.worldgen.build;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Writes the worldgen JSON for every {@link StructureCatalog} entry: worldgen/structure/<id>.json (type
 * itzbreadloaf:blueprint), worldgen/structure_set/<id>.json (random spread, kept out of vanilla villages in the
 * Overworld) and tags/worldgen/biome/has_structure/<id>.json. Run with {@code ./gradlew exportStructures}.
 * Plain Java only: it runs outside the game.
 */
public final class StructureCatalogExport {
	/** Structure sets whose file had another name before the remake (kept, so there is one set per structure). */
	private static final Map<String, String> OLD_SET_NAMES = Map.ofEntries(
			Map.entry("bakery", "bakeries"),
			Map.entry("bandit_camp", "bandit_camps"),
			Map.entry("fort", "forts"),
			Map.entry("house", "houses"),
			Map.entry("lily_house", "lily_houses"),
			Map.entry("madison_house", "madison_houses"),
			Map.entry("pirate_ship", "pirate_ships"),
			Map.entry("sky_arena", "sky_arenas"),
			Map.entry("tent", "tents"),
			Map.entry("treehouse", "treehouses"),
			Map.entry("village", "villages"),
			Map.entry("watchtower", "watchtowers"));

	private StructureCatalogExport() {
	}

	public static void main(String[] args) throws IOException {
		Path data = Path.of(args.length > 0 ? args[0] : "src/main/resources").resolve("data/itzbreadloaf");
		int count = 0;
		for (StructureCatalog.Def def : StructureCatalog.all()) {
			if (def.placement() == StructureCatalog.Placement.RUNTIME) {
				continue;
			}
			write(data.resolve("worldgen/structure/" + def.id() + ".json"), """
					{
					  "type": "itzbreadloaf:blueprint",
					  "id": "%s",
					  "biomes": "#itzbreadloaf:has_structure/%s",
					  "step": "%s",
					  "spawn_overrides": {},
					  "terrain_adaptation": "none"
					}
					""".formatted(def.id(), def.id(), def.step()));
			boolean overworld = def.placement() != StructureCatalog.Placement.NETHER_FLOOR && def.placement() != StructureCatalog.Placement.ISLAND;
			String exclusion = overworld ? """
					,
					    "exclusion_zone": {
					      "other_set": "minecraft:villages",
					      "chunk_count": 4
					    }""" : "";
			write(data.resolve("worldgen/structure_set/" + OLD_SET_NAMES.getOrDefault(def.id(), def.id()) + ".json"), """
					{
					  "structures": [
					    {
					      "structure": "itzbreadloaf:%s",
					      "weight": 1
					    }
					  ],
					  "placement": {
					    "type": "minecraft:random_spread",
					    "spacing": %d,
					    "separation": %d,
					    "salt": %d%s
					  }
					}
					""".formatted(def.id(), def.spacing(), def.separation(), def.salt(), exclusion));
			String biomes = def.biomes().stream().map(b -> "    \"" + b + "\"").collect(Collectors.joining(",\n"));
			write(data.resolve("tags/worldgen/biome/has_structure/" + def.id() + ".json"), "{\n  \"values\": [\n" + biomes + "\n  ]\n}\n");
			count++;
		}
		System.out.println("Exported " + count + " structures to " + data);
	}

	private static void write(Path path, String text) throws IOException {
		Files.createDirectories(path.getParent());
		Files.writeString(path, text, StandardCharsets.UTF_8);
	}
}
