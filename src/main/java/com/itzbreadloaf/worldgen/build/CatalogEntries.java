package com.itzbreadloaf.worldgen.build;

import java.util.List;

/**
 * The remade structures' catalog entries (ids, rarity, salts and biomes kept from before the remake, so worlds keep
 * finding them in the same places). Written by tools/update2_p7_catalog.py.
 */
final class CatalogEntries {
	private CatalogEntries() {
	}

	static void addAll() {
		StructureCatalog.add(StructureCatalog.def("house", "House", "ItzBreadLoaf", 1, 40, 14, 1022001, List.of("minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow", "#minecraft:is_forest", "#minecraft:is_taiga", "#minecraft:is_savanna", "minecraft:snowy_plains", "minecraft:snowy_taiga", "minecraft:desert"), "ItzBreadLoaf", 9, 9, 12));
		StructureCatalog.add(StructureCatalog.def("tent", "Tent", "ItzBreadLoaf", 1, 50, 16, 1022002, List.of("minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow", "#minecraft:is_forest", "#minecraft:is_taiga", "#minecraft:is_savanna", "minecraft:snowy_plains", "minecraft:snowy_taiga", "minecraft:desert"), "2-4 ItzBreadLoafs", 11, 11, 7));
		StructureCatalog.add(StructureCatalog.def("fort", "Bread Fort", "ItzBreadLoaf", 3, 68, 22, 1022006, List.of("minecraft:plains", "minecraft:meadow", "#minecraft:is_forest", "#minecraft:is_taiga", "#minecraft:is_savanna", "minecraft:windswept_hills", "minecraft:snowy_plains"), "3 Knights + Captain", 21, 21, 14));
		StructureCatalog.add(StructureCatalog.def("watchtower", "Watchtower", "ItzBreadLoaf", 2, 48, 16, 1022007, List.of("minecraft:plains", "minecraft:sunflower_plains", "#minecraft:is_forest", "#minecraft:is_taiga", "#minecraft:is_savanna", "minecraft:desert", "minecraft:snowy_plains", "minecraft:meadow"), "Archer", 7, 7, 22));
		StructureCatalog.add(StructureCatalog.def("bandit_camp", "Bandit Camp", "ItzBreadLoaf", 2, 60, 18, 1022008, List.of("#minecraft:is_forest", "#minecraft:is_taiga", "minecraft:dark_forest", "#minecraft:is_badlands", "#minecraft:is_savanna"), "3-4 Bandits", 15, 15, 7));
		StructureCatalog.add(StructureCatalog.def("bread_bakery", "Bread Bakery", "ItzBreadLoaf", 1, 52, 16, 1022201, List.of("minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow"), "ItzBreadLoaf", 11, 11, 12));
		StructureCatalog.add(StructureCatalog.def("mining_camp", "Mining Camp", "ItzBreadLoaf", 2, 64, 20, 1022203, List.of("#minecraft:is_overworld"), "Miner", 13, 13, 8));
		StructureCatalog.add(StructureCatalog.def("jungle_treehouse", "Jungle Treehouse", "ItzBreadLoaf", 2, 48, 16, 1022204, List.of("#minecraft:is_jungle", "minecraft:dark_forest"), "ItzBreadLoaf", 13, 13, 24));
		StructureCatalog.add(StructureCatalog.def("desert_bunker", "Desert Bunker", "ItzBreadLoaf", 2, 52, 16, 1022205, List.of("minecraft:desert", "#minecraft:is_badlands"), "ItzBreadLoaf", 11, 11, 6));
		StructureCatalog.add(StructureCatalog.def("igloo_hideout", "Igloo Hideout", "ItzBreadLoaf", 1, 48, 16, 1022206, List.of("minecraft:snowy_plains", "minecraft:snowy_taiga", "minecraft:ice_spikes", "minecraft:snowy_slopes"), "ItzBreadLoaf", 9, 9, 7));
		StructureCatalog.add(StructureCatalog.def("ruined_loaf_castle", "Ruined Loaf Castle", "ItzBreadLoaf", 3, 72, 24, 1022208, List.of("minecraft:plains", "minecraft:sunflower_plains", "#minecraft:is_savanna"), "Captain, Knights, ItzBreadLoafs (3-5)", 27, 27, 16));
		StructureCatalog.add(StructureCatalog.def("sweaty_dojo", "Sweaty Dojo", "ItzBreadLoaf", 2, 64, 20, 1022314, List.of("minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:cherry_grove", "minecraft:savanna", "minecraft:plains"), "Sweaty", 13, 13, 10));
		StructureCatalog.add(StructureCatalog.def("windmill", "Bread Mill (id windmill)", "ItzBreadLoaf", 1, 56, 18, 1022307, List.of("minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow"), "ItzBreadLoaf + Farmer Madison", 15, 15, 18));
		StructureCatalog.add(StructureCatalog.def("swamp_hut", "Swamp Hut", "ItzBreadLoaf", 1, 48, 16, 1022401, List.of("minecraft:swamp", "minecraft:mangrove_swamp"), "ItzBreadLoaf", 11, 11, 12).placement(StructureCatalog.Placement.ANY_SURFACE));
		StructureCatalog.add(StructureCatalog.def("mountain_outpost", "Mountain Outpost", "ItzBreadLoaf", 2, 48, 16, 1022402, List.of("minecraft:jagged_peaks", "minecraft:frozen_peaks", "minecraft:stony_peaks", "minecraft:snowy_slopes"), "Archer + Knight", 9, 9, 14));
		StructureCatalog.add(StructureCatalog.def("badlands_saloon", "Badlands Mineshaft Saloon", "ItzBreadLoaf", 2, 52, 16, 1022403, List.of("#minecraft:is_badlands"), "Miner + ItzBreadLoaf", 15, 13, 10));
		StructureCatalog.add(StructureCatalog.def("cliffside_base", "Cliffside Cave Base", "ItzBreadLoaf", 2, 52, 16, 1022404, List.of("minecraft:windswept_hills", "minecraft:windswept_gravelly_hills", "minecraft:windswept_forest", "minecraft:meadow", "minecraft:stony_shore"), "ItzBreadLoaf", 13, 13, 4));
		StructureCatalog.add(StructureCatalog.def("snowball_fort", "Snowball Fort (my idea)", "ItzBreadLoaf", 1, 52, 16, 1022418, List.of("minecraft:snowy_plains", "minecraft:ice_spikes", "minecraft:snowy_taiga", "minecraft:grove"), "ItzBreadLoaf + Village Bread", 19, 13, 5));
		StructureCatalog.add(StructureCatalog.def("lookout_tower", "Lookout Tower", "ItzBreadLoaf", 2, 48, 16, 1022202, List.of("#minecraft:is_forest", "#minecraft:is_taiga", "minecraft:dark_forest", "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga"), "Archer", 7, 7, 23));
		StructureCatalog.add(StructureCatalog.def("pirate_ship", "Pirate Ship", "ItzBreadLoaf", 2, 56, 16, 1022012, List.of("#minecraft:is_ocean"), "2-3 Pirates", 9, 23, 15).placement(StructureCatalog.Placement.WATER));
		StructureCatalog.add(StructureCatalog.def("fishing_dock", "Fishing Dock", "ItzBreadLoaf", 1, 40, 12, 1022207, List.of("minecraft:river", "minecraft:beach", "minecraft:snowy_beach"), "Fisherman", 7, 17, 7).placement(StructureCatalog.Placement.ANY_SURFACE));
		StructureCatalog.add(StructureCatalog.def("loaf_lighthouse", "Loaf Lighthouse (my idea)", "ItzBreadLoaf", 1, 52, 16, 1022414, List.of("#minecraft:is_beach", "minecraft:stony_shore"), "Fisherman", 9, 9, 23).placement(StructureCatalog.Placement.ANY_SURFACE));
		StructureCatalog.add(StructureCatalog.def("smugglers_cove", "Smugglers' Cove", "Ocean/beach", 2, 60, 20, 1022315, List.of("#minecraft:is_beach", "minecraft:stony_shore"), "Pirate + Bandit", 13, 17, 8).placement(StructureCatalog.Placement.ANY_SURFACE));
		StructureCatalog.add(StructureCatalog.def("shipwreck_camp", "Shipwreck Camp (my idea)", "Ocean/beach", 2, 56, 18, 1022417, List.of("#minecraft:is_beach"), "2 Pirates", 17, 11, 8).placement(StructureCatalog.Placement.ANY_SURFACE));
		StructureCatalog.add(StructureCatalog.def("village_campsite", "Village Campsite", "FelixSyn", 2, 2, 1, 0, List.of(), "FelixSyn & Harley", 13, 11, 7).placement(StructureCatalog.Placement.RUNTIME));
		StructureCatalog.add(StructureCatalog.def("bastion_throne_room", "Bastion Throne Room", "Nether", 4, 2, 1, 0, List.of(), "Master BreadLoaf", 13, 13, 9).placement(StructureCatalog.Placement.RUNTIME));
	}
}
