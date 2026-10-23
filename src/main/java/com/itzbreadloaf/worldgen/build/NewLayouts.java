package com.itzbreadloaf.worldgen.build;

import java.util.Map;

/** The layouts of update 2's new structures (part 8). Written by tools/update2_p8.py. */
final class NewLayouts {
	private NewLayouts() {
	}

	static void register(Map<String, Blueprints.Layout> layouts) {
		layouts.put("bread_stand", NewBreadBlueprints::breadStand);
		layouts.put("toaster_house", NewBreadBlueprints::toasterHouse);
		layouts.put("baguette_tower", NewBreadBlueprints::baguetteTower);
		layouts.put("crumb_trail_camp", NewBreadBlueprints::crumbTrailCamp);
		layouts.put("knight_training_yard", NewBreadBlueprints::knightTrainingYard);
		layouts.put("bread_bank", NewBreadBlueprints::breadBank);
		layouts.put("sweaty_gym", NewBreadBlueprints::sweatyGym);
		layouts.put("hay_maze", NewBreadBlueprints::hayMaze);
		layouts.put("miners_rest", NewBreadBlueprints::minersRest);
		layouts.put("bandit_lookout", NewBreadBlueprints::banditLookout);
		layouts.put("loaf_statue", NewBreadBlueprints::loafStatue);
		layouts.put("archery_range", NewBreadBlueprints::archeryRange);
		layouts.put("desert_oasis_camp", NewBreadBlueprints::desertOasisCamp);
		layouts.put("snowy_bakehouse", NewBreadBlueprints::snowyBakehouse);
		layouts.put("captains_manor", NewBreadBlueprints::captainsManor);
		layouts.put("wind_launch_pad", NewFelixBlueprints::windLaunchPad);
		layouts.put("smash_crater", NewFelixBlueprints::smashCrater);
		layouts.put("felix_trophy_hall", NewFelixBlueprints::trophyHall);
		layouts.put("duo_cabin", NewFelixBlueprints::duoCabin);
		layouts.put("parkour_tower", NewFelixBlueprints::parkourTower);
		layouts.put("sparring_ring", NewFelixBlueprints::sparringRing);
		layouts.put("mace_drop_tower", NewFelixBlueprints::maceDropTower);
		layouts.put("rivalry_wall", NewFelixBlueprints::rivalryWall);
		layouts.put("breeze_hunter_camp", NewFelixBlueprints::breezeHunterCamp);
		layouts.put("harleys_training_grove", NewFelixBlueprints::harleysGrove);
		layouts.put("giant_mace", EasterEggBlueprints::giantMace);
		layouts.put("tiny_house", EasterEggBlueprints::tinyHouse);
		layouts.put("upside_down_house", EasterEggBlueprints::upsideDownHouse);
		layouts.put("bread_portal", EasterEggBlueprints::breadPortal);
		layouts.put("developers_desk", EasterEggBlueprints::developersDesk);
		layouts.put("witchy_potion_shop", NewLilyBlueprints::witchyPotionShop);
		layouts.put("moonlit_reading_nook", NewLilyBlueprints::moonlitReadingNook);
		layouts.put("tulip_terrace", NewLilyBlueprints::tulipTerrace);
		layouts.put("willow_swing", NewLilyBlueprints::willowSwing);
		layouts.put("archers_glade", NewLilyBlueprints::archersGlade);
		layouts.put("flower_studio", NewLilyBlueprints::flowerStudio);
		layouts.put("herbalist_hut", NewLilyBlueprints::herbalistHut);
		layouts.put("wishing_hill", NewLilyBlueprints::wishingHill);
		layouts.put("amethyst_garden", NewLilyBlueprints::amethystGarden);
		layouts.put("lily_letter_post", NewLilyBlueprints::lilyLetterPost);
		layouts.put("fairy_ring", NewLilyBlueprints::fairyRing);
		layouts.put("winter_dome_garden", NewLilyBlueprints::winterDomeGarden);
		layouts.put("cherry_tea_house", NewLilyBlueprints::cherryTeaHouse);
		layouts.put("bookbinders_workshop", NewLilyBlueprints::bookbindersWorkshop);
		layouts.put("rose_arch_garden", NewLilyBlueprints::roseArchGarden);
		layouts.put("dairy_barn", NewMadisonBlueprints::dairyBarn);
		layouts.put("pie_shop", NewMadisonBlueprints::pieShop);
		layouts.put("fish_smokehouse", NewMadisonBlueprints::fishSmokehouse);
		layouts.put("chicken_coop", NewMadisonBlueprints::chickenCoop);
		layouts.put("beekeepers_cottage", NewMadisonBlueprints::beekeepersCottage);
		layouts.put("horse_stables", NewMadisonBlueprints::horseStables);
		layouts.put("farmers_market", NewMadisonBlueprints::farmersMarket);
		layouts.put("grain_silo", NewMadisonBlueprints::grainSilo);
		layouts.put("melon_farm", NewMadisonBlueprints::melonFarm);
		layouts.put("cookie_cottage", NewMadisonBlueprints::cookieCottage);
		layouts.put("sheep_ranch", NewMadisonBlueprints::sheepRanch);
		layouts.put("merchant_caravan", NewMadisonBlueprints::merchantCaravan);
		layouts.put("friends_picnic", NewSharedBlueprints::friendsPicnic);
		layouts.put("story_circle", NewSharedBlueprints::storyCircle);
		layouts.put("movie_night", NewSharedBlueprints::movieNight);
		layouts.put("winter_lodge", NewSharedBlueprints::winterLodge);
		layouts.put("obstacle_course", NewSharedBlueprints::obstacleCourse);
		layouts.put("clubhouse_tree", NewSharedBlueprints::clubhouseTree);
		layouts.put("harvest_fair", NewSharedBlueprints::harvestFair);
		layouts.put("community_garden", NewSharedBlueprints::communityGarden);
		layouts.put("mini_golf", NewSharedBlueprints::miniGolf);
		layouts.put("truce_pavilion", NewSharedBlueprints::trucePavilion);
		layouts.put("beach_snack_hut", OceanBlueprints::beachSnackHut);
		layouts.put("lifeguard_tower", OceanBlueprints::lifeguardTower);
		layouts.put("ferry_landing", OceanBlueprints::ferryLanding);
		layouts.put("pearl_diver_raft", OceanBlueprints::pearlDiverRaft);
		layouts.put("sea_glass_cove", OceanBlueprints::seaGlassCove);
		layouts.put("fishermens_wharf", OceanBlueprints::fishermensWharf);
		layouts.put("hellfire_bakery", NetherBlueprints::hellfireBakery);
		layouts.put("crimson_outpost", NetherBlueprints::crimsonOutpost);
		layouts.put("nether_wart_garden", NetherBlueprints::netherWartGarden);
		layouts.put("lava_fishing_spot", NetherBlueprints::lavaFishingSpot);
		layouts.put("basalt_watchtower", NetherBlueprints::basaltWatchtower);
		layouts.put("soul_camp", NetherBlueprints::soulCamp);
		layouts.put("gold_trading_post", NetherBlueprints::goldTradingPost);
		layouts.put("strider_stables", NetherBlueprints::striderStables);
		layouts.put("void_observatory", EndBlueprints::voidObservatory);
		layouts.put("chorus_garden", EndBlueprints::chorusGarden);
		layouts.put("last_loaf_shrine", EndBlueprints::lastLoafShrine);
		layouts.put("purpur_vault", EndBlueprints::purpurVault);
		layouts.put("dragon_lookout", EndBlueprints::dragonLookout);
		layouts.put("void_fishing_platform", EndBlueprints::voidFishingPlatform);
	}
}
