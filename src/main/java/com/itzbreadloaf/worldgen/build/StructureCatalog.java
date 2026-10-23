package com.itzbreadloaf.worldgen.build;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every structure built with the blueprint system, in one place: id, name, category, biomes, rarity (spacing and
 * separation in chunks), loot level, who lives there, and an optional lore book. This class is plain Java on purpose
 * (no Minecraft classes), so {@code ./gradlew exportStructures} can run it outside the game to write the worldgen
 * JSON (structure, structure set, biome tag) for each entry. The layout itself is in {@link Blueprints}.
 */
public final class StructureCatalog {
	/** How it's placed in the world. */
	public enum Placement {
		/** On dry land. */
		LAND,
		/** Floating on water (pirate ships). */
		WATER,
		/** Land or water, wherever it lands (docks, stilts). */
		ANY_SURFACE,
		/** On the Nether floor, found by searching down under the ceiling. */
		NETHER_FLOOR,
		/** Needs ground under it (the End's islands). */
		ISLAND,
		/** Built by the mod while the game runs (campsites, throne rooms): no worldgen files. */
		RUNTIME
	}

	/**
	 * One structure. Size is the footprint (width x depth) and height; the piece adds a leveling margin around it.
	 * {@code biomes} are biome ids or #tags. {@code loreBook} is a lore book id or null.
	 */
	public record Def(String id, String name, String category, int lootLevel, int spacing, int separation, int salt,
			List<String> biomes, String residents, String loreBook, int width, int depth, int height,
			Placement placement, String step, List<String> near) {
		public int footprint() {
			return Math.max(this.width, this.depth);
		}

		public Def placement(Placement value) {
			return new Def(this.id, this.name, this.category, this.lootLevel, this.spacing, this.separation, this.salt, this.biomes,
					this.residents, this.loreBook, this.width, this.depth, this.height, value, this.step, this.near);
		}

		public Def lore(String book) {
			return new Def(this.id, this.name, this.category, this.lootLevel, this.spacing, this.separation, this.salt, this.biomes,
					this.residents, book, this.width, this.depth, this.height, this.placement, this.step, this.near);
		}

		public Def step(String value) {
			return new Def(this.id, this.name, this.category, this.lootLevel, this.spacing, this.separation, this.salt, this.biomes,
					this.residents, this.loreBook, this.width, this.depth, this.height, this.placement, value, this.near);
		}

		/** Only generates 24-96 blocks from one of these structures (Rival Camp: near ItzBreadLoaf's houses). */
		public Def near(String... ids) {
			return new Def(this.id, this.name, this.category, this.lootLevel, this.spacing, this.separation, this.salt, this.biomes,
					this.residents, this.loreBook, this.width, this.depth, this.height, this.placement, this.step, List.of(ids));
		}
	}

	/** A structure on land, on the surface, with no lore book (change those with placement(), step(), lore(), near()). */
	static Def def(String id, String name, String category, int lootLevel, int spacing, int separation, int salt,
			List<String> biomes, String residents, int width, int depth, int height) {
		return new Def(id, name, category, lootLevel, spacing, separation, salt, biomes, residents, null, width, depth, height,
				Placement.LAND, "surface_structures", List.of());
	}

	private static final Map<String, Def> DEFS = new LinkedHashMap<>();

	static {
		// ---- remade with the blueprint system (ids kept, so old worlds keep their structures)
		CatalogEntries.addAll();
	}

	private StructureCatalog() {
	}

	static void add(Def def) {
		if (DEFS.put(def.id(), def) != null) {
			throw new IllegalStateException("Two structures with the id " + def.id());
		}
		if (def.separation() >= def.spacing()) {
			throw new IllegalStateException(def.id() + ": separation must be smaller than spacing");
		}
	}

	public static Def get(String id) {
		return DEFS.get(id);
	}

	public static java.util.Collection<Def> all() {
		return DEFS.values();
	}
}
