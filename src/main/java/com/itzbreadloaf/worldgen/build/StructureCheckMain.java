package com.itzbreadloaf.worldgen.build;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;

/**
 * {@code ./gradlew checkStructures}: the reachability check without starting the game. Boots just enough of
 * Minecraft to know its blocks, builds every blueprint with several wood/stone palettes and seeds, and fails the
 * task if any floor, chest, bed or resident can't be reached. ({@code /loafcraft checkstructures} does the same
 * in game with the real biome palettes.)
 */
public final class StructureCheckMain {
	private StructureCheckMain() {
	}

	public static void main(String[] args) {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		List<Palette> palettes = List.of(
				new Palette(Palette.OAK, Palette.COBBLE, net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState(),
						net.minecraft.world.level.block.Blocks.DIRT_PATH.defaultBlockState(), net.minecraft.world.level.block.Blocks.LANTERN.defaultBlockState(),
						net.minecraft.world.level.block.Blocks.RED_BED, new net.minecraft.world.level.block.Block[]{net.minecraft.world.level.block.Blocks.POPPY}),
				new Palette(Palette.SPRUCE, Palette.DEEPSLATE, net.minecraft.world.level.block.Blocks.SNOW_BLOCK.defaultBlockState(),
						net.minecraft.world.level.block.Blocks.GRAVEL.defaultBlockState(), net.minecraft.world.level.block.Blocks.LANTERN.defaultBlockState(),
						net.minecraft.world.level.block.Blocks.WHITE_BED, new net.minecraft.world.level.block.Block[]{net.minecraft.world.level.block.Blocks.FERN}),
				new Palette(Palette.CRIMSON, Palette.BLACKSTONE, net.minecraft.world.level.block.Blocks.NETHERRACK.defaultBlockState(),
						net.minecraft.world.level.block.Blocks.POLISHED_BLACKSTONE.defaultBlockState(), net.minecraft.world.level.block.Blocks.SOUL_LANTERN.defaultBlockState(),
						net.minecraft.world.level.block.Blocks.RED_BED, new net.minecraft.world.level.block.Block[]{net.minecraft.world.level.block.Blocks.CRIMSON_ROOTS}));
		selfTest(palettes.get(0));
		List<String> problems = new ArrayList<>();
		int built = 0;
		for (StructureCatalog.Def def : StructureCatalog.all()) {
			if (!Blueprints.has(def.id())) {
				problems.add(def.id() + ": no layout");
				continue;
			}
			for (Palette palette : palettes) {
				for (int seed = 0; seed < 4; seed++) {
					Blueprint b = new Blueprint(def.width(), def.depth(), def.height(), palette, RandomSource.create(seed));
					Blueprints.build(def.id(), b);
					built++;
					for (String problem : Reachability.check(b)) {
						String line = def.id() + ": " + problem;
						if (!problems.contains(line)) {
							problems.add(line);
						}
					}
				}
			}
		}
		int tables = checkLootItems(java.nio.file.Path.of("src/main/resources/data/itzbreadloaf/loot_table"), problems);
		if (problems.isEmpty()) {
			System.out.println("Structure check: all " + built + " test builds passed (" + StructureCatalog.all().size() + " structures), "
					+ tables + " loot tables use only real items.");
			return;
		}
		problems.forEach(p -> System.out.println("Structure check: " + p));
		throw new IllegalStateException(problems.size() + " structure problem(s)");
	}

	/**
	 * Every minecraft: item named in the mod's loot tables must exist in this game version (item ids get renamed
	 * between versions, and a table with an unknown item fails to load). The mod's own items aren't registered in this
	 * offline run, so those are skipped. Returns how many tables it read.
	 */
	private static int checkLootItems(java.nio.file.Path root, List<String> problems) {
		if (!java.nio.file.Files.isDirectory(root)) {
			return 0;
		}
		int count = 0;
		try (var files = java.nio.file.Files.walk(root)) {
			for (java.nio.file.Path file : files.filter(f -> f.toString().endsWith(".json")).toList()) {
				count++;
				com.google.gson.JsonElement json = com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(file));
				collectItems(json, name -> {
					net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.tryParse(name);
					if (id == null || id.getNamespace().equals("minecraft")
							&& !net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(id)) {
						problems.add("loot table " + root.relativize(file) + ": unknown item " + name);
					}
				});
			}
		} catch (java.io.IOException e) {
			problems.add("loot tables: " + e);
		}
		return count;
	}

	private static void collectItems(com.google.gson.JsonElement element, java.util.function.Consumer<String> out) {
		if (element.isJsonObject()) {
			com.google.gson.JsonObject object = element.getAsJsonObject();
			com.google.gson.JsonElement type = object.get("type");
			if (type != null && type.isJsonPrimitive() && "minecraft:item".equals(type.getAsString()) && object.has("name")) {
				out.accept(object.get("name").getAsString());
			}
			for (var entry : object.entrySet()) {
				collectItems(entry.getValue(), out);
			}
		} else if (element.isJsonArray()) {
			for (com.google.gson.JsonElement child : element.getAsJsonArray()) {
				collectItems(child, out);
			}
		}
	}

	/** Makes sure the check can fail: a sealed room's chest, and a tower top with no ladder, must be reported. */
	private static void selfTest(Palette palette) {
		Blueprint sealed = new Blueprint(5, 5, 6, palette, RandomSource.create(0));
		Pieces.room(sealed, 0, 0, 4, 4, 0, 3, "sealed room");
		sealed.fill(0, 4, 0, 4, 4, 4, palette.planks());
		sealed.chest(2, 1, 2, "chests/house");
		Blueprint noLadder = new Blueprint(5, 5, 12, palette, RandomSource.create(0));
		Pieces.tower(noLadder, 0, 0, 5, 0, 10, 5, true);
		noLadder.fill(2, 1, 3, 2, 10, 3, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
		Pieces.door(noLadder, 2, 1, 0, net.minecraft.core.Direction.NORTH);
		if (Reachability.check(sealed).size() < 2 || Reachability.check(noLadder).isEmpty()) {
			throw new IllegalStateException("The structure check didn't catch a sealed room or a missing ladder: it's broken");
		}
	}
}
