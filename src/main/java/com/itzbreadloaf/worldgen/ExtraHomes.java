package com.itzbreadloaf.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Builds every structure that isn't one of the original twelve (those are in {@link HomePiece} itself). */
final class ExtraHomes {
	private ExtraHomes() {
	}

	static void build(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random, Holder<Biome> biome) {
		switch (p.kind()) {
			case LILY_MEADOW_COTTAGE -> LilyHomes.meadow(p, level, box, random);
			case LILY_FLOWER_COTTAGE -> LilyHomes.flowerForest(p, level, box, random);
			case LILY_READING_CABIN -> LilyHomes.readingCabin(p, level, box, random);
			case LILY_SNOWY_CABIN -> LilyHomes.snowyCabin(p, level, box, random);
			case LILY_MUSHROOM_HOUSE -> LilyHomes.mushroomHouse(p, level, box, random);
			case BREAD_BAKERY -> BreadHomes.bakery(p, level, box, random);
			case LOOKOUT_TOWER -> BreadHomes.lookoutTower(p, level, box, random);
			case MINING_CAMP -> BreadHomes.miningCamp(p, level, box, random);
			case JUNGLE_TREEHOUSE -> BreadHomes.jungleTreehouse(p, level, box, random);
			case DESERT_BUNKER -> BreadHomes.desertBunker(p, level, box, random);
			case IGLOO_HIDEOUT -> BreadHomes.igloo(p, level, box, random);
			case FISHING_DOCK -> BreadHomes.fishingDock(p, level, box, random);
			case RUINED_LOAF_CASTLE -> BreadHomes.ruinedCastle(p, level, box, random);
			case GREENHOUSE -> SpecialHomes.greenhouse(p, level, box, random);
			case LAKESIDE_GAZEBO -> SpecialHomes.gazebo(p, level, box, random);
			case FARMSTEAD -> SpecialHomes.farmstead(p, level, box, random);
			case ORCHARD -> SpecialHomes.orchard(p, level, box, random);
			case MARKET_STALL -> SpecialHomes.marketStall(p, level, box, random);
			case COUSINS_CAMP -> SpecialHomes.cousinsCamp(p, level, box, random);
			case PVP_ARENA -> SpecialHomes.pvpArena(p, level, box, random);
			case GIANT_LOAF -> SpecialHomes.giantLoaf(p, level, box, random);
			case NETHER_TRAINING_GROUND -> SpecialHomes.netherTrainingGround(p, level, box, random);
			case END_MACE_TOWER -> SpecialHomes.endMaceTower(p, level, box, random);
			case WINDMILL -> SpecialHomes.windmill(p, level, box, random);
			case SWEATY_DOJO -> SpecialHomes.sweatyDojo(p, level, box, random);
			case FLOWER_SHRINE -> SpecialHomes.flowerShrine(p, level, box, random);
			case SMUGGLERS_COVE -> SpecialHomes.smugglersCove(p, level, box, random);
			case PUMPKIN_PATCH -> SpecialHomes.pumpkinPatch(p, level, box, random);
			case SWAMP_HUT -> NewHomes.swampHut(p, level, box, random);
			case MOUNTAIN_OUTPOST -> NewHomes.mountainOutpost(p, level, box, random);
			case BADLANDS_SALOON -> NewHomes.badlandsSaloon(p, level, box, random);
			case CLIFFSIDE_BASE -> NewHomes.cliffsideBase(p, level, box, random);
			case FLOATING_ISLAND_COTTAGE -> NewHomes.floatingIsland(p, level, box, random);
			case LANTERN_GARDEN -> NewHomes.lanternGarden(p, level, box, random);
			case TREETOP_LIBRARY -> NewHomes.treetopLibrary(p, level, box, random);
			case RIVERSIDE_MILL -> NewHomes.riversideMill(p, level, box, random);
			case BEE_SANCTUARY -> NewHomes.beeSanctuary(p, level, box, random);
			case RUINED_PORTAL_HIDEOUT -> NewHomes.ruinedPortalHideout(p, level, box, random);
			case VILLAGE_CAMPSITE -> NewHomes.villageCampsite(p, level, box, random);
			case BASTION_THRONE_ROOM -> NewHomes.bastionThroneRoom(p, level, box, random);
			case NETHER_MACE_ARENA -> NewHomes.netherMaceArena(p, level, box, random);
			case END_SKY_PLATFORM -> NewHomes.endSkyPlatform(p, level, box, random);
			case FRIENDSHIP_FESTIVAL -> NewHomes.friendshipFestival(p, level, box, random);
			case LOAF_LIGHTHOUSE -> NewHomes.lighthouse(p, level, box, random);
			case STARWATCH_OBSERVATORY -> NewHomes.observatory(p, level, box, random);
			case BERRY_CABIN -> NewHomes.berryCabin(p, level, box, random);
			case SHIPWRECK_CAMP -> NewHomes.shipwreckCamp(p, level, box, random);
			case SNOWBALL_FORT -> NewHomes.snowballFort(p, level, box, random);
			case MACE_WORKSHOP -> FelixHomes.maceWorkshop(p, level, box, random, HomePiece.Palette.forBiome(biome));
			case CLIFF_HUT -> FelixHomes.cliffHut(p, level, box, random, HomePiece.Palette.forBiome(biome));
			case WIND_TOWER -> FelixHomes.windTower(p, level, box, random, HomePiece.Palette.forBiome(biome));
			case RIVAL_CAMP -> FelixHomes.rivalCamp(p, level, box, random, HomePiece.Palette.forBiome(biome));
			default -> {
			}
		}
	}
}
