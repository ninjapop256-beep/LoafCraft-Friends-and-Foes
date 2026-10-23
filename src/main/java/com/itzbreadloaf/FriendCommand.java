package com.itzbreadloaf;

import com.itzbreadloaf.character.LoafIds;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

/**
 * Friend list (anyone can use it, no operator needed):
 * <pre>
 *   /friend add &lt;player&gt;      your tamed characters (Breads, Lily, Madison, Harley) never attack them or their pets
 *   /friend remove &lt;name&gt;
 *   /friend list
 * </pre>
 * It is one-way: your friends' tamed characters still follow their own owner's list. Saved with the world.
 */
public final class FriendCommand {
	private static final SimpleCommandExceptionType NOT_A_PLAYER = new SimpleCommandExceptionType(
			Component.literal("You can only friend real players, not LoafCraft characters."));
	private static final SimpleCommandExceptionType SELF = new SimpleCommandExceptionType(
			Component.literal("You're already your own best friend."));

	private static final SuggestionProvider<CommandSourceStack> MY_FRIENDS = (context, builder) -> {
		ServerPlayer self = context.getSource().getPlayer();
		if (self == null) {
			return builder.buildFuture();
		}
		return SharedSuggestionProvider.suggest(Data.get(context.getSource().getServer()).friendsOf(self.getUUID()).values(), builder);
	};

	private FriendCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				Commands.literal("friend")
						.then(Commands.literal("add").then(Commands.argument("player", EntityArgument.player()).executes(FriendCommand::add)))
						.then(Commands.literal("remove").then(Commands.argument("name", StringArgumentType.word()).suggests(MY_FRIENDS)
								.executes(FriendCommand::remove)))
						.then(Commands.literal("list").executes(FriendCommand::list))));
	}

	/** True if {@code friend} is on {@code owner}'s friend list. */
	public static boolean isFriend(@Nullable MinecraftServer server, UUID owner, UUID friend) {
		if (server == null) {
			return false;
		}
		Map<UUID, String> friends = Data.get(server).friends.get(owner);
		return friends != null && friends.containsKey(friend);
	}

	private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer self = context.getSource().getPlayerOrException();
		ServerPlayer friend = EntityArgument.getPlayer(context, "player");
		if (!LoafIds.isRealPlayer(friend)) {
			throw NOT_A_PLAYER.create();
		}
		if (friend == self) {
			throw SELF.create();
		}
		Data data = Data.get(context.getSource().getServer());
		data.friends.computeIfAbsent(self.getUUID(), id -> new LinkedHashMap<>()).put(friend.getUUID(), friend.getGameProfile().name());
		data.setDirty();
		context.getSource().sendSuccess(() -> Component.literal(friend.getGameProfile().name() + " is now your friend. Your tamed characters won't attack them.")
				.withStyle(ChatFormatting.GREEN), false);
		friend.sendSystemMessage(Component.literal(self.getGameProfile().name() + " added you as a friend. Their tamed characters won't attack you.")
				.withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer self = context.getSource().getPlayerOrException();
		String name = StringArgumentType.getString(context, "name");
		Data data = Data.get(context.getSource().getServer());
		Map<UUID, String> friends = data.friendsOf(self.getUUID());
		boolean removed = friends.values().removeIf(friendName -> friendName.equalsIgnoreCase(name));
		if (!removed) {
			context.getSource().sendFailure(Component.literal(name + " isn't on your friend list."));
			return 0;
		}
		data.setDirty();
		context.getSource().sendSuccess(() -> Component.literal(name + " is no longer your friend.").withStyle(ChatFormatting.YELLOW), false);
		return 1;
	}

	private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer self = context.getSource().getPlayerOrException();
		Map<UUID, String> friends = Data.get(context.getSource().getServer()).friendsOf(self.getUUID());
		if (friends.isEmpty()) {
			context.getSource().sendSuccess(() -> Component.literal("You have no friends on your list yet. Use /friend add <player>."), false);
			return 0;
		}
		context.getSource().sendSuccess(() -> Component.literal("Friends (" + friends.size() + "): " + String.join(", ", friends.values())), false);
		return friends.size();
	}

	/** owner UUID -> (friend UUID -> friend name). Stored as "owner" -> ["friendUuid|name", ...]. */
	public static class Data extends SavedData {
		public static final Codec<Data> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()).fieldOf("friends").codec()
				.xmap(Data::new, Data::encode);
		public static final SavedDataType<Data> TYPE = new SavedDataType<>("itzbreadloaf_friends", Data::new, CODEC, null);

		final Map<UUID, Map<UUID, String>> friends = new HashMap<>();

		public Data() {
		}

		public Data(Map<String, List<String>> stored) {
			stored.forEach((owner, entries) -> {
				Map<UUID, String> map = new LinkedHashMap<>();
				for (String entry : entries) {
					int bar = entry.indexOf('|');
					try {
						map.put(UUID.fromString(bar < 0 ? entry : entry.substring(0, bar)), bar < 0 ? "?" : entry.substring(bar + 1));
					} catch (IllegalArgumentException ignored) {
						// Skip broken entries.
					}
				}
				try {
					this.friends.put(UUID.fromString(owner), map);
				} catch (IllegalArgumentException ignored) {
					// Skip broken entries.
				}
			});
		}

		static Data get(MinecraftServer server) {
			return server.overworld().getDataStorage().computeIfAbsent(TYPE);
		}

		Map<UUID, String> friendsOf(UUID owner) {
			return this.friends.computeIfAbsent(owner, id -> new LinkedHashMap<>());
		}

		private Map<String, List<String>> encode() {
			Map<String, List<String>> out = new HashMap<>();
			this.friends.forEach((owner, map) -> {
				if (!map.isEmpty()) {
					List<String> entries = new ArrayList<>();
					map.forEach((id, name) -> entries.add(id + "|" + name));
					out.put(owner.toString(), entries);
				}
			});
			return out;
		}
	}
}
