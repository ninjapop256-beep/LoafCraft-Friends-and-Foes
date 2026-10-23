package com.itzbreadloaf.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** Every LoafCraft structure. Width x depth x height is the piece's bounding box (local coordinates). */
public enum HomeKind implements StringRepresentable {
	/** ItzBreadLoaf's 7x7 cabin, one resident. */
	HOUSE("house", 7, 7, 7),
	/** ItzBreadLoaf group tent (9 wide) with a campfire in front, 2-4 residents. */
	TENT("tent", 9, 10, 6),
	/** ImLilyLol's 7x9 cherry house with a petal-covered yard around it (Lily, Gardener or Witchy Lily). */
	LILY_HOUSE("lily_house", 9, 11, 7),
	/** Madison's 9x9 birch and white concrete cottage with a crop garden and beehives (Madison, Farmer or Beekeeper). */
	MADISON_HOUSE("madison_house", 11, 14, 7),
	/** LoafCraft Village: three cabins, a well, a farm and a fishing pond. Village ItzBreadLoafs, Archer Lily, Farmer and Fisher Madison. */
	VILLAGE("village", 31, 31, 10),
	/** Bread Fort: stone walls, corner towers, barracks and a treasure room. Three Knights and a Captain. */
	FORT("fort", 19, 19, 9),
	/** Watchtower: a tall wooden tower with a ladder and a lookout platform. One Archer ItzBreadLoaf on top. */
	WATCHTOWER("watchtower", 7, 7, 20),
	/** Bandit Camp: dark tents around a campfire and a loot chest. 3-4 Bandits who raid villages at night. */
	BANDIT_CAMP("bandit_camp", 15, 15, 6),
	/** Lily's Treehouse: a library on a platform up a big tree, with stairs. Bookworm Lily. */
	TREEHOUSE("treehouse", 13, 13, 16),
	/** Madison's Bakery: counter, ovens and a bread chest. Baker Madison. */
	BAKERY("bakery", 11, 12, 8),
	/** Master's Sky Arena: a round platform on a tall pillar (ladder up). Master BreadLoaf is waiting. */
	SKY_ARENA("sky_arena", 21, 21, 38),
	/** Pirate Ship: floating on the ocean, with a mast, sails and a captain's cabin. 2-3 Pirate ItzBreadLoafs. */
	PIRATE_SHIP("pirate_ship", 9, 21, 12),

	// ---- Lily in more biomes (same personality everywhere)
	/** Meadow: small stone-and-birch cottage with a flower garden. */
	LILY_MEADOW_COTTAGE("lily_meadow_cottage", 9, 11, 7),
	/** Flower Forest: mossy cottage covered in flowers and vines. */
	LILY_FLOWER_COTTAGE("lily_flower_cottage", 9, 11, 7),
	/** Birch Forest: cozy birch reading cabin full of bookshelves. */
	LILY_READING_CABIN("lily_reading_cabin", 9, 11, 7),
	/** Snowy Taiga: spruce cabin with a fireplace and lanterns. */
	LILY_SNOWY_CABIN("lily_snowy_cabin", 9, 11, 8),
	/** Mushroom Fields (rare): a mushroom-shaped house. */
	LILY_MUSHROOM_HOUSE("lily_mushroom_house", 11, 11, 9),

	// ---- ItzBreadLoaf structures
	/** Bread bakery: his bread-themed home with furnaces, smokers, hay and a chest of bread. */
	BREAD_BAKERY("bread_bakery", 11, 11, 8),
	/** Lookout tower: tall stone-and-wood tower with an Archer ItzBreadLoaf on top. */
	LOOKOUT_TOWER("lookout_tower", 7, 7, 22),
	/** Mining camp: tent, campfire, and rails running into a small tunnel with ore. Miners. */
	MINING_CAMP("mining_camp", 15, 13, 7),
	/** Jungle treehouse: a hut in a big jungle tree with a ladder up the trunk. */
	JUNGLE_TREEHOUSE("jungle_treehouse", 13, 13, 18),
	/** Desert bunker: half-buried sandstone bunker with an arrow trap and loot. */
	DESERT_BUNKER("desert_bunker", 11, 13, 6),
	/** Igloo hideout: a snow igloo with a hidden basement storage room under a carpet. */
	IGLOO_HIDEOUT("igloo_hideout", 9, 9, 6),
	/** Fishing dock: a pier and a shed. A fisherman ItzBreadLoaf. */
	FISHING_DOCK("fishing_dock", 7, 15, 6),
	/** Ruined Loaf Castle (rare): crumbling walls, towers and a keep. 3-5 ItzBreadLoafs and great loot. */
	RUINED_LOAF_CASTLE("ruined_loaf_castle", 27, 27, 14),

	// ---- Lily, Madison, shared and special
	/** Glass greenhouse full of flowers and saplings. Gardener Lily. */
	GREENHOUSE("greenhouse", 9, 11, 7),
	/** Lakeside gazebo by a little pond, where Lily sits and reads. */
	LAKESIDE_GAZEBO("lakeside_gazebo", 13, 13, 7),
	/** Farmstead: barn, animal pens, crop fields and a big pantry. Farmer Madison. */
	FARMSTEAD("farmstead", 25, 21, 11),
	/** Orchard and beehive farm: blossoming trees and hives over campfires. Beekeeper Madison. */
	ORCHARD("orchard", 19, 19, 9),
	/** Market stall: Merchant Madison trades food for emeralds. */
	MARKET_STALL("market_stall", 9, 7, 6),
	/** Cousins' Camp (rare): Lily and Madison camping together. */
	COUSINS_CAMP("cousins_camp", 13, 11, 5),
	/** PvP Arena (rare): a fenced arena where ItzBreadLoaf and FelixSyn fight. */
	PVP_ARENA("pvp_arena", 23, 23, 6),
	/** Giant Loaf (very rare): a huge bread-shaped building with a treasure room. */
	GIANT_LOAF("giant_loaf", 27, 15, 12),
	/** Nether Mace Training Ground: tall pillars for mace drops. Master BreadLoaf. */
	NETHER_TRAINING_GROUND("nether_training_ground", 21, 21, 22),
	/** End Mace Tower: a tall purpur tower on the outer islands. Master BreadLoaf. */
	END_MACE_TOWER("end_mace_tower", 9, 9, 32),
	/** Bread Mill (id "windmill"): ItzBreadLoaf's windmill with a big wheat farm, Farmer Madison helps. */
	WINDMILL("windmill", 15, 15, 17),
	/** Sweaty Dojo (rare): where a Sweaty ItzBreadLoaf trains. */
	SWEATY_DOJO("sweaty_dojo", 13, 13, 7),
	/** Flower Shrine: a little shrine of lanterns and flower beds that Gardener Lily tends. */
	FLOWER_SHRINE("flower_shrine", 9, 9, 7),
	/** Smugglers' Cove: a beach shack and a dock. A Pirate and a Bandit. */
	SMUGGLERS_COVE("smugglers_cove", 13, 15, 7),
	/** Pumpkin Patch Farm: a pumpkin field with a scarecrow and a farm shed. Farmer Madison. */
	PUMPKIN_PATCH("pumpkin_patch", 15, 13, 7),

	// ---- the remake's new structures
	/** Swamp Hut on stilts over the water. ItzBreadLoaf. */
	SWAMP_HUT("swamp_hut", 11, 11, 10),
	/** Mountain Outpost on the peaks: a stone lookout tower. Archer and Knight ItzBreadLoaf. */
	MOUNTAIN_OUTPOST("mountain_outpost", 9, 9, 12),
	/** Badlands Mineshaft Saloon: a saloon with a mine shaft under the back room. Miner and ItzBreadLoaf. */
	BADLANDS_SALOON("badlands_saloon", 15, 13, 9),
	/** Cliffside Cave Base: an underground room behind a hidden trapdoor. ItzBreadLoaf. */
	CLIFFSIDE_BASE("cliffside_base", 13, 13, 8),
	/** Floating Island Cottage: a sky island over a Cherry Grove. ImLilyLol. */
	FLOATING_ISLAND_COTTAGE("floating_island_cottage", 15, 15, 30),
	/** Lantern Garden: a night-lit garden with benches and a pond. Gardener Lily. */
	LANTERN_GARDEN("lantern_garden", 13, 13, 6),
	/** Treetop Library: a bookshelf room high in tall birch trees. Bookworm Lily. */
	TREETOP_LIBRARY("treetop_library", 13, 13, 22),
	/** Riverside Mill with a water wheel. Madison. */
	RIVERSIDE_MILL("riverside_mill", 13, 11, 10),
	/** Bee Sanctuary: lots of beehives, flowers and honey. Beekeeper Madison. */
	BEE_SANCTUARY("bee_sanctuary", 17, 17, 8),
	/** Ruined Portal Hideout: FelixSyn and Harley's camp behind a broken portal. */
	RUINED_PORTAL_HIDEOUT("ruined_portal_hideout", 13, 11, 10),
	/** Village Campsite: built at a village's edge when FelixSyn and Harley show up (not world generation). */
	VILLAGE_CAMPSITE("village_campsite", 11, 9, 5),
	/** Bastion Throne Room: built inside a bastion the first time a player gets there (not world generation). Master. */
	BASTION_THRONE_ROOM("bastion_throne_room", 11, 11, 8),
	/** Nether Mace Arena: a lava-ringed arena with tall pillars. Master BreadLoaf. */
	NETHER_MACE_ARENA("nether_mace_arena", 25, 25, 26),
	/** End Sky Platform: a purpur deck high over an outer End island. Master BreadLoaf. */
	END_SKY_PLATFORM("end_sky_platform", 15, 15, 20),
	/** Friendship Festival grounds: tables, food, banners and a stage. ItzBreadLoaf, Lily and Madison. */
	FRIENDSHIP_FESTIVAL("friendship_festival", 21, 17, 7),
	/** Loaf Lighthouse (my idea): a striped lighthouse on the shore. Fisherman ItzBreadLoaf. */
	LOAF_LIGHTHOUSE("loaf_lighthouse", 9, 9, 22),
	/** Starwatch Observatory (my idea): a glass-domed stargazing room. Bookworm Lily. */
	STARWATCH_OBSERVATORY("starwatch_observatory", 11, 11, 12),
	/** Sweet Berry Cabin (my idea): a spruce cabin with berry bushes. Madison. */
	BERRY_CABIN("berry_cabin", 11, 13, 8),
	/** Shipwreck Camp (my idea): Pirates camping by half a ship. */
	SHIPWRECK_CAMP("shipwreck_camp", 17, 11, 8),
	/** Snowball Fort (my idea): two snow forts and a snowman. Two ItzBreadLoafs. */
	SNOWBALL_FORT("snowball_fort", 19, 13, 4),

	// ---- update 2: wild FelixSyn's homes
	/** Mace Workshop: stone-and-dark-oak workshop with an anvil, smithing table and mace racks. Wild FelixSyn. */
	MACE_WORKSHOP("mace_workshop", 13, 11, 8),
	/** Cliff Hut: a little hut with a ledge for mace drops, on windswept hills and mountains. Wild FelixSyn. */
	CLIFF_HUT("cliff_hut", 11, 11, 8),
	/** Wind Tower: a tall thin tower with a ladder all the way up, for jump training. Wild FelixSyn. */
	WIND_TOWER("wind_tower", 9, 9, 27),
	/** Rival Camp: always within sight of an ItzBreadLoaf house or tent, so they fight. Wild FelixSyn. */
	RIVAL_CAMP("rival_camp", 11, 9, 5);

	public static final Codec<HomeKind> CODEC = StringRepresentable.fromEnum(HomeKind::values);

	private final String name;
	public final int width;
	public final int depth;
	public final int height;

	HomeKind(String name, int width, int depth, int height) {
		this.name = name;
		this.width = width;
		this.depth = depth;
		this.height = height;
	}

	/** Built on land or water, wherever it lands (docks, stilts, mills and lighthouses on shores). */
	public boolean anySurface() {
		return this == FISHING_DOCK || this == SMUGGLERS_COVE || this == SWAMP_HUT || this == RIVERSIDE_MILL
				|| this == LOAF_LIGHTHOUSE || this == SHIPWRECK_CAMP;
	}

	/** Built by the mod while the game runs (not by world generation), so /locate can't find it. */
	public boolean runtimeOnly() {
		return this == VILLAGE_CAMPSITE || this == BASTION_THRONE_ROOM;
	}

	/** Built on the Nether floor (found by searching down, not from the top of the world). */
	public boolean inNether() {
		return this == NETHER_TRAINING_GROUND || this == NETHER_MACE_ARENA;
	}

	/** Needs land under it (the End has void between islands). */
	public boolean needsSolidGround() {
		return this == END_MACE_TOWER || this == END_SKY_PLATFORM;
	}

	/** Only generates near an ItzBreadLoaf house or tent (the Rival Camp). */
	public boolean needsRivalNearby() {
		return this == RIVAL_CAMP;
	}

	/** Built on the water surface instead of on land. */
	public boolean onWater() {
		return this == PIRATE_SHIP;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public static HomeKind byName(String name) {
		for (HomeKind kind : values()) {
			if (kind.name.equals(name)) {
				return kind;
			}
		}
		return HOUSE;
	}
}
