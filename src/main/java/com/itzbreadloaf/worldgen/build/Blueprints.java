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
		LAYOUTS.put("lily_house", LilyBlueprints::lilyHouse);
		LAYOUTS.put("lily_meadow_cottage", LilyBlueprints::meadowCottage);
		LAYOUTS.put("lily_flower_cottage", LilyBlueprints::flowerCottage);
		LAYOUTS.put("lily_reading_cabin", LilyBlueprints::readingCabin);
		LAYOUTS.put("lily_snowy_cabin", LilyBlueprints::snowyCabin);
		LAYOUTS.put("lily_mushroom_house", LilyBlueprints::mushroomHouse);
		LAYOUTS.put("treehouse", LilyBlueprints::treehouse);
		LAYOUTS.put("greenhouse", LilyBlueprints::greenhouse);
		LAYOUTS.put("lakeside_gazebo", LilyBlueprints::lakesideGazebo);
		LAYOUTS.put("flower_shrine", LilyBlueprints::flowerShrine);
		LAYOUTS.put("floating_island_cottage", LilyBlueprints::floatingIsland);
		LAYOUTS.put("lantern_garden", LilyBlueprints::lanternGarden);
		LAYOUTS.put("treetop_library", LilyBlueprints::treetopLibrary);
		LAYOUTS.put("starwatch_observatory", LilyBlueprints::starwatchObservatory);
		LAYOUTS.put("madison_house", MadisonBlueprints::madisonHouse);
		LAYOUTS.put("bakery", MadisonBlueprints::bakery);
		LAYOUTS.put("farmstead", MadisonBlueprints::farmstead);
		LAYOUTS.put("orchard", MadisonBlueprints::orchard);
		LAYOUTS.put("market_stall", MadisonBlueprints::marketStall);
		LAYOUTS.put("pumpkin_patch", MadisonBlueprints::pumpkinPatch);
		LAYOUTS.put("riverside_mill", MadisonBlueprints::riversideMill);
		LAYOUTS.put("bee_sanctuary", MadisonBlueprints::beeSanctuary);
		LAYOUTS.put("berry_cabin", MadisonBlueprints::berryCabin);
		LAYOUTS.put("village", SharedBlueprints::village);
		LAYOUTS.put("cousins_camp", SharedBlueprints::cousinsCamp);
		LAYOUTS.put("pvp_arena", SharedBlueprints::pvpArena);
		LAYOUTS.put("friendship_festival", SharedBlueprints::friendshipFestival);
		LAYOUTS.put("giant_loaf", SharedBlueprints::giantLoaf);
		LAYOUTS.put("ruined_portal_hideout", SharedBlueprints::ruinedPortalHideout);
		LAYOUTS.put("mace_workshop", FelixBlueprints::maceWorkshop);
		LAYOUTS.put("cliff_hut", FelixBlueprints::cliffHut);
		LAYOUTS.put("wind_tower", FelixBlueprints::windTower);
		LAYOUTS.put("rival_camp", FelixBlueprints::rivalCamp);
		LAYOUTS.put("sky_arena", MasterBlueprints::skyArena);
		LAYOUTS.put("nether_training_ground", MasterBlueprints::netherTrainingGround);
		LAYOUTS.put("nether_mace_arena", MasterBlueprints::netherMaceArena);
		LAYOUTS.put("end_mace_tower", MasterBlueprints::endMaceTower);
		LAYOUTS.put("end_sky_platform", MasterBlueprints::endSkyPlatform);
		LAYOUTS.put("village_campsite", BreadBlueprints::villageCampsite);
		LAYOUTS.put("bastion_throne_room", BreadBlueprints::bastionThroneRoom);
	}

	// Update 2's new structures (after the remake's block, which tools/update2_p7_catalog.py rewrites).
	static {
		NewLayouts.register(LAYOUTS);
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
