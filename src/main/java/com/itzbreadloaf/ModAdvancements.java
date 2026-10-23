package com.itzbreadloaf;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * The "LoafCraft" advancement tab (data/itzbreadloaf/advancement/*.json).
 * Most use the "impossible" trigger and are granted from code here (including "Grounded", since Master BreadLoaf is
 * a Carpet fake player now). "Tent Raider" (standing in a tent) uses a vanilla trigger and needs no code.
 */
public final class ModAdvancements {
	public static final Identifier BREAD_WINNER = ItzBreadLoaf.id("bread_winner");
	public static final Identifier EARNED_HER_TRUST = ItzBreadLoaf.id("earned_her_trust");
	public static final Identifier HANDS_OFF_THE_PANTRY = ItzBreadLoaf.id("hands_off_the_pantry");
	public static final Identifier PEACE_OFFERING = ItzBreadLoaf.id("peace_offering");
	/** Criteria: "lily", "madison". */
	public static final Identifier COUSIN_CLUB = ItzBreadLoaf.id("cousin_club");
	public static final Identifier FAMILY_REUNION = ItzBreadLoaf.id("family_reunion");
	public static final Identifier LITTLE_LOAF = ItzBreadLoaf.id("little_loaf");
	public static final Identifier MACE_MASTER = ItzBreadLoaf.id("mace_master");
	/** Defeat Master BreadLoaf (granted from code: he is a fake player, not an entity type). */
	public static final Identifier GROUNDED = ItzBreadLoaf.id("grounded");
	/** Defeat a Captain ItzBreadLoaf (the Bread Fort mini-boss). */
	public static final Identifier STORM_THE_FORT = ItzBreadLoaf.id("storm_the_fort");
	/** Buy something from Baker Madison. */
	public static final Identifier FRESH_FROM_THE_OVEN = ItzBreadLoaf.id("fresh_from_the_oven");
	/** Watch FelixSyn and ItzBreadLoaf fight. */
	public static final Identifier RIVAL_SHOWDOWN = ItzBreadLoaf.id("rival_showdown");
	/** Find FelixSyn in a village. */
	public static final Identifier VILLAGE_DRIFTER = ItzBreadLoaf.id("village_drifter");
	/** Tame Harley. */
	public static final Identifier TAKEN_IN = ItzBreadLoaf.id("taken_in");
	/** Defeat a wild Harley. */
	public static final Identifier UNTOUCHABLE = ItzBreadLoaf.id("untouchable");
	/** Find 10 different LoafCraft structures. */
	public static final Identifier STRUCTURE_HUNTER = ItzBreadLoaf.id("structure_hunter");
	/** Find Lily's houses in 3 different biomes. */
	public static final Identifier LILYS_TRAVELS = ItzBreadLoaf.id("lilys_travels");
	/** Visit the Bread Mill or the Riverside Mill. */
	public static final Identifier MILL_WORKER = ItzBreadLoaf.id("mill_worker");
	/** Reach a Floating Island Cottage. */
	public static final Identifier SKY_COTTAGE = ItzBreadLoaf.id("sky_cottage");
	/** Find the Friendship Festival grounds. */
	public static final Identifier FESTIVAL_GOER = ItzBreadLoaf.id("festival_goer");
	/** Walk into Master BreadLoaf's throne room in a bastion. */
	public static final Identifier THRONE_ROOM_CRASHER = ItzBreadLoaf.id("throne_room_crasher");
	/** Find a Mountain Outpost. */
	public static final Identifier PEAK_PERFORMANCE = ItzBreadLoaf.id("peak_performance");
	/** Find FelixSyn and Harley's campsite at a village. */
	public static final Identifier CAMPFIRE_STORIES = ItzBreadLoaf.id("campfire_stories");
	// ---- update 2
	/** Tame a wild FelixSyn with cooked porkchops. */
	public static final Identifier WILD_CARD = ItzBreadLoaf.id("wild_card");
	/** Climb to the top of a Wind Tower. */
	public static final Identifier WIND_TOWER_CLIMBER = ItzBreadLoaf.id("wind_tower_climber");
	/** Read 5 of Lily's books. */
	public static final Identifier BOOKWORM = ItzBreadLoaf.id("bookworm");
	/** Visit all 15 lore places from Lily's books. */
	public static final Identifier LORE_KEEPER = ItzBreadLoaf.id("lore_keeper");
	/** Find the Ruins of the First Bakery. */
	public static final Identifier THE_FIRST_BAKERY = ItzBreadLoaf.id("the_first_bakery");
	/** Find the Old Battle Site where the rivalry started. */
	public static final Identifier WHERE_IT_ALL_BEGAN = ItzBreadLoaf.id("where_it_all_began");
	/** Find 25 different LoafCraft structures. */
	public static final Identifier EXPLORER_I = ItzBreadLoaf.id("explorer_i");
	/** Find 50 different LoafCraft structures. */
	public static final Identifier EXPLORER_II = ItzBreadLoaf.id("explorer_ii");
	/** Find 100 different LoafCraft structures. */
	public static final Identifier EXPLORER_III = ItzBreadLoaf.id("explorer_iii");
	/** Find a Legendary item in LoafCraft loot. */
	public static final Identifier LUCKY_FIND = ItzBreadLoaf.id("lucky_find");

	private ModAdvancements() {
	}

	public static void award(ServerPlayer player, Identifier id) {
		award(player, id, "done");
	}

	public static void award(ServerPlayer player, Identifier id, String criterion) {
		AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
		if (advancement != null) {
			player.getAdvancements().award(advancement, criterion);
		}
	}
}
