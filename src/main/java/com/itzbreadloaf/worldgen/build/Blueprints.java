package com.itzbreadloaf.worldgen.build;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** The layout for each catalog id. Layouts live in the *Blueprints classes, grouped by who builds there. */
public final class Blueprints {
	/** Draws a structure into a blueprint. */
	@FunctionalInterface
	public interface Layout {
		void build(Blueprint b);
	}

	private static final Map<String, Layout> LAYOUTS = new HashMap<>();

	static {
		LAYOUTS.put("house", BreadBlueprints::house);
		LAYOUTS.put("tent", BreadBlueprints::tent);
		LAYOUTS.put("fort", BreadBlueprints::fort);
		LAYOUTS.put("watchtower", BreadBlueprints::watchtower);
		LAYOUTS.put("bandit_camp", BreadBlueprints::banditCamp);
		LAYOUTS.put("bread_bakery", BreadBlueprints::breadBakery);
		LAYOUTS.put("mining_camp", BreadBlueprints::miningCamp);
		LAYOUTS.put("jungle_treehouse", BreadBlueprints::jungleTreehouse);
		LAYOUTS.put("desert_bunker", BreadBlueprints::desertBunker);
		LAYOUTS.put("igloo_hideout", BreadBlueprints::igloo);
		LAYOUTS.put("ruined_loaf_castle", BreadBlueprints::ruinedCastle);
		LAYOUTS.put("sweaty_dojo", BreadBlueprints::sweatyDojo);
		LAYOUTS.put("windmill", BreadBlueprints::breadMill);
		LAYOUTS.put("swamp_hut", BreadBlueprints::swampHut);
		LAYOUTS.put("mountain_outpost", BreadBlueprints::mountainOutpost);
		LAYOUTS.put("badlands_saloon", BreadBlueprints::badlandsSaloon);
		LAYOUTS.put("cliffside_base", BreadBlueprints::cliffsideBase);
		LAYOUTS.put("snowball_fort", BreadBlueprints::snowballFort);
		LAYOUTS.put("lookout_tower", BreadBlueprints::lookoutTower);
		LAYOUTS.put("pirate_ship", BreadBlueprints::pirateShip);
		LAYOUTS.put("fishing_dock", BreadBlueprints::fishingDock);
		LAYOUTS.put("loaf_lighthouse", BreadBlueprints::lighthouse);
		LAYOUTS.put("smugglers_cove", BreadBlueprints::smugglersCove);
		LAYOUTS.put("shipwreck_camp", BreadBlueprints::shipwreckCamp);
		LAYOUTS.put("village_campsite", BreadBlueprints::villageCampsite);
		LAYOUTS.put("bastion_throne_room", BreadBlueprints::bastionThroneRoom);
	}

	private Blueprints() {
	}

	public static boolean has(String id) {
		return LAYOUTS.containsKey(id);
	}

	public static void build(String id, Blueprint blueprint) {
		LAYOUTS.get(id).build(blueprint);
	}

	public static Set<String> ids() {
		return LAYOUTS.keySet();
	}
}
