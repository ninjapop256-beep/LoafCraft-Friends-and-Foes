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

	public static final int BOOKWORM_GOAL = 5;
	static final String BOOK_PREFIX = "book:";
	/** The 15 lore places Lily's books talk about ("Lore Keeper" needs all of them). */
	public static final Set<String> LORE_PLACES = Set.of("lily_writing_cabin", "first_bakery_ruins", "memorial_grove", "rivalry_battle_site",
			"sealed_library", "cartographers_tent", "masters_old_home", "old_loaf_archive", "cousins_old_treehouse", "drifters_rest",
			"naturalist_camp", "masters_departure", "whispering_stones", "crossroads_signpost", "lost_loaf_wagon");

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
			Data data = server.overworld().getDataStorage().computeIfAbsent(Data.TYPE);
			Set<String> found = data.found.computeIfAbsent(player.getUUID(), uuid -> new LinkedHashSet<>());
			trackBooks(player, data, found);
			if (kind == null) {
				continue;
			}
			awardFor(player, kind);
			if (kind.equals("wind_tower") && start.isValid() && player.getY() >= start.getBoundingBox().maxY() - 6) {
				ModAdvancements.award(player, ModAdvancements.WIND_TOWER_CLIMBER);
			}
			if (!found.add(kind)) {
				continue;
			}
			data.setDirty();
			long lilyHomes = found.stream().filter(LILY_HOMES::contains).count();
			long structures = found.stream().filter(k -> !k.startsWith(BOOK_PREFIX)).count();
			long lore = found.stream().filter(LORE_PLACES::contains).count();
			if (LORE_PLACES.contains(kind)) {
				player.displayClientMessage(Component.translatable("message.itzbreadloaf.lore_found", lore, LORE_PLACES.size())
						.withStyle(ChatFormatting.LIGHT_PURPLE), true);
			} else {
				player.displayClientMessage(Component.translatable("message.itzbreadloaf.structures_found", structures).withStyle(ChatFormatting.GOLD), true);
			}
			if (structures >= HUNTER_GOAL) {
				ModAdvancements.award(player, ModAdvancements.STRUCTURE_HUNTER);
			}
			if (structures >= 25) {
				ModAdvancements.award(player, ModAdvancements.EXPLORER_I);
			}
			if (structures >= 50) {
				ModAdvancements.award(player, ModAdvancements.EXPLORER_II);
			}
			if (structures >= 100) {
				ModAdvancements.award(player, ModAdvancements.EXPLORER_III);
			}
			if (lore >= LORE_PLACES.size()) {
				ModAdvancements.award(player, ModAdvancements.LORE_KEEPER);
			}
			if (lilyHomes >= LILY_GOAL) {
				ModAdvancements.award(player, ModAdvancements.LILYS_TRAVELS);
			}
		}
	}

	/**
	 * Lily's books count as read when the player holds one (in the inventory) or has a lectern open with one nearby.
	 * Read books are kept in the same saved set as "book:<id>".
	 */
	private static void trackBooks(ServerPlayer player, Data data, Set<String> found) {
		List<String> books = new ArrayList<>();
		for (net.minecraft.world.item.ItemStack stack : player.getInventory()) {
			String id = bookId(stack);
			if (id != null) {
				books.add(id);
			}
		}
		if (player.containerMenu instanceof net.minecraft.world.inventory.LecternMenu) {
			net.minecraft.core.BlockPos center = player.blockPosition();
			for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(center.offset(-5, -3, -5), center.offset(5, 3, 5))) {
				if (player.level().getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity lectern) {
					String id = bookId(lectern.getBook());
					if (id != null) {
						books.add(id);
					}
				}
			}
		}
		boolean added = false;
		for (String id : books) {
			added |= found.add(BOOK_PREFIX + id);
		}
		if (!added) {
			return;
		}
		data.setDirty();
		long read = found.stream().filter(k -> k.startsWith(BOOK_PREFIX)).count();
		player.displayClientMessage(Component.translatable("message.itzbreadloaf.lily_book_read", read, com.itzbreadloaf.item.LilyBooks.all().size())
				.withStyle(ChatFormatting.LIGHT_PURPLE), true);
		if (read >= BOOKWORM_GOAL) {
			ModAdvancements.award(player, ModAdvancements.BOOKWORM);
		}
	}

	private static String bookId(net.minecraft.world.item.ItemStack stack) {
		net.minecraft.world.item.component.CustomData custom = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
		if (custom == null) {
			return null;
		}
		String id = custom.copyTag().getStringOr("lily_book", "");
		return id.isEmpty() ? null : id;
	}

	/** Advancements for walking into one particular structure. */
	private static void awardFor(ServerPlayer player, String kind) {
		Identifier advancement = switch (kind) {
			case "first_bakery_ruins" -> ModAdvancements.THE_FIRST_BAKERY;
			case "rivalry_battle_site" -> ModAdvancements.WHERE_IT_ALL_BEGAN;
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
