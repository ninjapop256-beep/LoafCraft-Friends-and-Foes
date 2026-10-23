package com.itzbreadloaf.character;

import com.itzbreadloaf.ItzBreadLoaf;
import com.itzbreadloaf.ModAdvancements;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Remembers which kinds of LoafCraft structure each player has stood inside. Awards "Structure Hunter" at 10
 * different kinds and "Lily's Travels" at 3 different Lily houses. Saved with the world.
 */
public final class StructureTracker {
	public static final int HUNTER_GOAL = 10;
	public static final int LILY_GOAL = 3;
	/** Lily's houses, one per biome style. */
	public static final Set<String> LILY_HOMES = Set.of("lily_house", "lily_meadow_cottage", "lily_flower_cottage",
			"lily_reading_cabin", "lily_snowy_cabin", "lily_mushroom_house", "floating_island_cottage", "treetop_library");

	private StructureTracker() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(StructureTracker::tick);
	}

	private static void tick(MinecraftServer server) {
		if (server.getTickCount() % 40 != 13) {
			return;
		}
		for (ServerPlayer player : CharacterManager.realPlayers(server)) {
			ServerLevel level = player.level();
			String kind = null;
			StructureStart start = level.structureManager().getStructureWithPieceAt(player.blockPosition(),
					holder -> holder.is(key -> key.identifier().getNamespace().equals(ItzBreadLoaf.MOD_ID)));
			if (start.isValid()) {
				Identifier id = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(start.getStructure());
				kind = id == null ? null : id.getPath();
			}
			if (kind == null) {
				// Structures built while the game runs (campsites, throne rooms).
				String dimension = level.dimension().identifier().toString();
				for (com.itzbreadloaf.worldgen.RuntimeSites.Site site : com.itzbreadloaf.worldgen.RuntimeSites.all(server)) {
					if (site.dimension().equals(dimension) && site.box().inflatedBy(1).isInside(player.blockPosition())) {
						kind = site.kind();
						break;
					}
				}
			}
			if (kind == null) {
				continue;
			}
			awardFor(player, kind);
			Data data = server.overworld().getDataStorage().computeIfAbsent(Data.TYPE);
			Set<String> found = data.found.computeIfAbsent(player.getUUID(), uuid -> new LinkedHashSet<>());
			if (!found.add(kind)) {
				continue;
			}
			data.setDirty();
			long lilyHomes = found.stream().filter(LILY_HOMES::contains).count();
			player.displayClientMessage(Component.literal("Found a new LoafCraft structure! (" + Math.min(found.size(), HUNTER_GOAL) + "/" + HUNTER_GOAL + ")")
					.withStyle(ChatFormatting.GOLD), true);
			if (found.size() >= HUNTER_GOAL) {
				ModAdvancements.award(player, ModAdvancements.STRUCTURE_HUNTER);
			}
			if (lilyHomes >= LILY_GOAL) {
				ModAdvancements.award(player, ModAdvancements.LILYS_TRAVELS);
			}
		}
	}

	/** Advancements for walking into one particular structure. */
	private static void awardFor(ServerPlayer player, String kind) {
		Identifier advancement = switch (kind) {
			case "windmill", "riverside_mill" -> ModAdvancements.MILL_WORKER;
			case "floating_island_cottage" -> ModAdvancements.SKY_COTTAGE;
			case "friendship_festival" -> ModAdvancements.FESTIVAL_GOER;
			case "bastion_throne_room" -> ModAdvancements.THRONE_ROOM_CRASHER;
			case "mountain_outpost" -> ModAdvancements.PEAK_PERFORMANCE;
			case "village_campsite" -> ModAdvancements.CAMPFIRE_STORIES;
			default -> null;
		};
		if (advancement != null) {
			ModAdvancements.award(player, advancement);
		}
	}

	/** player UUID -> structure ids found (paths, like "bread_bakery"). */
	public static class Data extends SavedData {
		public static final Codec<Data> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()).fieldOf("found").codec()
				.xmap(Data::new, Data::encode);
		public static final SavedDataType<Data> TYPE = new SavedDataType<>("itzbreadloaf_structures_found", Data::new, CODEC, null);

		final Map<UUID, Set<String>> found = new HashMap<>();

		public Data() {
		}

		public Data(Map<String, List<String>> stored) {
			stored.forEach((player, ids) -> {
				try {
					this.found.put(UUID.fromString(player), new LinkedHashSet<>(ids));
				} catch (IllegalArgumentException ignored) {
					// Skip broken entries.
				}
			});
		}

		private Map<String, List<String>> encode() {
			Map<String, List<String>> out = new HashMap<>();
			this.found.forEach((player, ids) -> out.put(player.toString(), new ArrayList<>(ids)));
			return out;
		}
	}
}
