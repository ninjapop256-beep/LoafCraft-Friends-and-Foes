package com.itzbreadloaf.worldgen;

import com.itzbreadloaf.ItzBreadLoaf;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Registers the home structure type and its piece. The actual structures (house, tent, lily_house),
 * their biomes and spacing are data files in data/itzbreadloaf/worldgen/.
 */
public final class ModStructures {
	public static StructureType<HomeStructure> HOME;
	public static StructurePieceType HOME_PIECE;
	/** Structures built from blueprints (see worldgen/build). */
	public static StructureType<com.itzbreadloaf.worldgen.build.BlueprintStructure> BLUEPRINT;
	public static StructurePieceType BLUEPRINT_PIECE;

	public static final ResourceKey<LootTable> HOUSE_CHEST = lootTable("chests/house");
	public static final ResourceKey<LootTable> TENT_CHEST = lootTable("chests/tent");
	public static final ResourceKey<LootTable> LILY_HOUSE_CHEST = lootTable("chests/lily_house");
	public static final ResourceKey<LootTable> MADISON_PANTRY = lootTable("chests/madison_pantry");
	public static final ResourceKey<LootTable> VILLAGE_CHEST = lootTable("chests/village");
	public static final ResourceKey<LootTable> FORT_CHEST = lootTable("chests/fort");
	public static final ResourceKey<LootTable> WATCHTOWER_CHEST = lootTable("chests/watchtower");
	public static final ResourceKey<LootTable> BANDIT_CHEST = lootTable("chests/bandit_camp");
	public static final ResourceKey<LootTable> TREEHOUSE_CHEST = lootTable("chests/treehouse");
	public static final ResourceKey<LootTable> BAKERY_CHEST = lootTable("chests/bakery");
	public static final ResourceKey<LootTable> ARENA_CHEST = lootTable("chests/sky_arena");
	public static final ResourceKey<LootTable> PIRATE_CHEST = lootTable("chests/pirate_ship");
	/** Barrels: general supplies, or fish for barrels by the water. */
	public static final ResourceKey<LootTable> BARREL_LOOT = lootTable("chests/barrel_supplies");
	public static final ResourceKey<LootTable> FISH_BARREL = lootTable("chests/fishing_barrel");

	private ModStructures() {
	}

	public static ResourceKey<LootTable> lootTable(String path) {
		return ResourceKey.create(Registries.LOOT_TABLE, ItzBreadLoaf.id(path));
	}

	public static void register() {
		HOME = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, ItzBreadLoaf.id("home"), () -> HomeStructure.CODEC);
		HOME_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE, ItzBreadLoaf.id("home_piece"),
				(StructurePieceType.ContextlessType) HomePiece::new);
		BLUEPRINT = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, ItzBreadLoaf.id("blueprint"),
				() -> com.itzbreadloaf.worldgen.build.BlueprintStructure.CODEC);
		BLUEPRINT_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE, ItzBreadLoaf.id("blueprint_piece"),
				(StructurePieceType.ContextlessType) com.itzbreadloaf.worldgen.build.BlueprintPiece::new);
	}
}
