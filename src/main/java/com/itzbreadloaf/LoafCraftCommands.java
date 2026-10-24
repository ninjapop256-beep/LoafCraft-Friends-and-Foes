package com.itzbreadloaf;

import carpet.helpers.EntityPlayerActionPack;
import carpet.helpers.EntityPlayerActionPack.Action;
import carpet.helpers.EntityPlayerActionPack.ActionType;
import com.itzbreadloaf.boss.BossSpawner;
import com.itzbreadloaf.brain.LoafBrain;
import com.itzbreadloaf.character.CharacterKind;
import com.itzbreadloaf.character.CharacterVariant;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.CharacterRecord;
import com.itzbreadloaf.character.LoafCharacter;
import com.itzbreadloaf.character.LoafWorldData;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Locale;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * LoafCraft commands (operators only). {@code <name>} is a character's player name: ItzBreadLoaf, ItzBreadLoaf_2,
 * ImLilyLol, Madison, MasterBreadLoaf...
 * <pre>
 *   /loafcraft spawn &lt;type&gt; [pos]   (itzbreadloaf, knight, archer, miner, pirate, nether, sweaty, captain, bandit, villager,
 *                                       imlilylol, bookworm, gardener, witchy, archer_lily,
 *                                       madison, farmer, baker, fisher, beekeeper, master_breadloaf)
 *   /loafcraft list
 *   /loafcraft despawn &lt;name&gt;                    (removes them for good)
 *   /loafcraft brain &lt;name&gt; pause|resume
 *   /loafcraft action &lt;name&gt; use|attack|jump|sneak|unsneak|sprint|walk|swap|stop   (pauses the brain)
 *   /loafcraft action &lt;name&gt; slot &lt;1-9&gt;
 *   /loafcraft boss [pos]
 *   /loafcraft inventory &lt;name&gt; list
 *   /loafcraft inventory &lt;name&gt; give &lt;item&gt; [count]
 *   /loafcraft inventory &lt;name&gt; set &lt;slot&gt; &lt;item&gt; [count]   (slots like /item: hotbar.0, armor.head, weapon.offhand)
 *   /loafcraft inventory &lt;name&gt; clear [slot]
 *   /loafcraft data &lt;name&gt; health|food|saturation &lt;value&gt;
 * </pre>
 * Characters are real players, so vanilla /item, /effect, /attribute, /give and /tp also work on them while spawned.
 */
public final class LoafCraftCommands {
	private static final SimpleCommandExceptionType NOT_SPAWNED = new SimpleCommandExceptionType(
			Component.literal("That character isn't spawned right now (no real player within ~4 chunks), or doesn't exist."));
	private static final SimpleCommandExceptionType NO_SUCH_KIND = new SimpleCommandExceptionType(
			Component.literal("Unknown character type. Try itzbreadloaf, knight, archer, miner, pirate, nether, sweaty, captain, bandit, villager, imlilylol, bookworm, gardener, witchy, archer_lily, madison, farmer, baker, fisher, beekeeper or master_breadloaf."));

	private static final SuggestionProvider<CommandSourceStack> NAMES = (context, builder) -> SharedSuggestionProvider.suggest(
			LoafWorldData.get(context.getSource().getServer()).records().stream().map(CharacterRecord::profileName), builder);
	private static final SuggestionProvider<CommandSourceStack> KINDS = (context, builder) -> SharedSuggestionProvider.suggest(
			java.util.Arrays.stream(CharacterVariant.values()).map(variant -> variant.id), builder);

	private LoafCraftCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				Commands.literal("loafcraft").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.then(Commands.literal("spawn").then(Commands.argument("character", StringArgumentType.word()).suggests(KINDS)
								.executes(c -> spawn(c, c.getSource().getPosition()))
								.then(Commands.argument("pos", Vec3Argument.vec3()).executes(c -> spawn(c, Vec3Argument.getVec3(c, "pos"))))))
						.then(Commands.literal("list").executes(LoafCraftCommands::list))
						.then(Commands.literal("despawn").then(name().executes(LoafCraftCommands::despawn)))
						.then(Commands.literal("brain").then(name()
								.then(Commands.literal("pause").executes(c -> pause(c, true)))
								.then(Commands.literal("resume").executes(c -> pause(c, false)))))
						.then(Commands.literal("action").then(actions(name())))
						.then(Commands.literal("checkstructures").executes(c -> {
							for (String line : com.itzbreadloaf.worldgen.build.StructureCheck.run(c.getSource().getServer())) {
								c.getSource().sendSuccess(() -> Component.literal(line), false);
							}
							return 1;
						}))
						.then(Commands.literal("boss")
								.executes(c -> boss(c, c.getSource().getPosition()))
								.then(Commands.argument("pos", Vec3Argument.vec3()).executes(c -> boss(c, Vec3Argument.getVec3(c, "pos")))))
						.then(Commands.literal("inventory").then(inventory(name(), registryAccess)))
						.then(Commands.literal("data").then(name()
								.then(Commands.literal("health").then(Commands.argument("value", FloatArgumentType.floatArg(0.5F, 1024.0F))
										.executes(c -> setData(c, "health"))))
								.then(Commands.literal("food").then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, 20.0F))
										.executes(c -> setData(c, "food"))))
								.then(Commands.literal("saturation").then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, 20.0F))
										.executes(c -> setData(c, "saturation"))))))
						.then(Commands.literal("mood").then(name()
								.executes(LoafCraftCommands::showMind)
								.then(Commands.argument("mood", StringArgumentType.word()).suggests(MOODS)
										.executes(LoafCraftCommands::setMood))))
						.then(Commands.literal("technique").then(name()
								.then(Commands.argument("technique", StringArgumentType.word()).suggests(TECHNIQUES)
										.executes(c -> technique(c, null))
										.then(Commands.argument("target", net.minecraft.commands.arguments.EntityArgument.entity())
												.executes(c -> technique(c, net.minecraft.commands.arguments.EntityArgument.getEntity(c, "target")))))))
						.then(Commands.literal("debug").then(Commands.literal("ai").executes(LoafCraftCommands::debugAi)))
						.then(Commands.literal("duel").then(Commands.argument("first", StringArgumentType.word()).suggests(NAMES)
								.then(Commands.argument("second", StringArgumentType.word()).suggests(NAMES)
										.executes(LoafCraftCommands::duel))))));
	}

	/**
	 * /loafcraft debug ai: every active character's goal, mood, detail level and AI time per tick, plus totals and the
	 * path searches and cache hits since the last time it was run.
	 */
	private static int debugAi(CommandContext<CommandSourceStack> context) {
		var active = CharacterManager.active();
		if (active.isEmpty()) {
			reply(context, "No characters are active right now.");
			return 0;
		}
		double total = 0.0;
		int full = 0;
		int simple = 0;
		int paused = 0;
		for (LoafCharacter character : active) {
			var brain = character.brain;
			if (brain == null) {
				continue;
			}
			total += brain.aiMicros();
			switch (brain.detail()) {
				case FULL -> full++;
				case SIMPLE -> simple++;
				case PAUSED -> paused++;
			}
			String line = String.format(java.util.Locale.ROOT, "%s [%s] %s, %s, %.0f us/tick", character.record.profileName(),
					brain.detail().name().toLowerCase(java.util.Locale.ROOT), brain.describeGoal(), brain.mood().mood().id(), brain.aiMicros());
			context.getSource().sendSuccess(() -> Component.literal(line), false);
		}
		int searches = com.itzbreadloaf.brain.Navigator.searchesCount;
		int hits = com.itzbreadloaf.brain.Navigator.cacheHits;
		com.itzbreadloaf.brain.Navigator.searchesCount = 0;
		com.itzbreadloaf.brain.Navigator.cacheHits = 0;
		String summary = String.format(java.util.Locale.ROOT, "%d characters (%d full, %d simple, %d paused): %.2f ms of AI per tick. Paths since last check: %d searched, %d from cache.",
				active.size(), full, simple, paused, total / 1000.0, searches, hits);
		context.getSource().sendSuccess(() -> Component.literal(summary).withStyle(net.minecraft.ChatFormatting.GOLD), false);
		return active.size();
	}

	/** /loafcraft duel <first> <second>: makes two characters fight each other (up to 2 minutes, or until one goes down). */
	private static int duel(CommandContext<CommandSourceStack> context) {
		LoafCharacter first = CharacterManager.byName(StringArgumentType.getString(context, "first"));
		LoafCharacter second = CharacterManager.byName(StringArgumentType.getString(context, "second"));
		if (first == null || second == null || first.brain == null || second.brain == null) {
			context.getSource().sendFailure(Component.literal("Both characters need to be spawned right now (a real player within ~4 chunks)."));
			return 0;
		}
		if (first == second) {
			context.getSource().sendFailure(Component.literal("A character can't duel itself."));
			return 0;
		}
		if (first.player.level() != second.player.level() || first.player.distanceTo(second.player) > 48.0) {
			context.getSource().sendFailure(Component.literal("They need to be in the same dimension and within 48 blocks of each other."));
			return 0;
		}
		first.brain.startDuel(second.player);
		second.brain.startDuel(first.player);
		reply(context, first.record.profileName() + " and " + second.record.profileName() + " are dueling!");
		return 1;
	}

	private static final SuggestionProvider<CommandSourceStack> TECHNIQUES = (context, builder) -> SharedSuggestionProvider.suggest(
			java.util.Arrays.stream(com.itzbreadloaf.brain.MaceTechniques.Technique.values()).map(com.itzbreadloaf.brain.MaceTechniques.Technique::id), builder);

	/**
	 * /loafcraft technique <name> <technique> [target]: makes FelixSyn, Harley or Master BreadLoaf do a mace move now
	 * (testing). Without a target, the nearest living thing within 24 blocks. Hands them anything the move needs that
	 * they don't have (a mace, wind charges, pearls, rockets, an elytra).
	 */
	private static int technique(CommandContext<CommandSourceStack> context, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.Entity targetArg)
			throws CommandSyntaxException {
		LoafCharacter character = character(context);
		var brain = character.brain;
		String id = StringArgumentType.getString(context, "technique");
		var technique = com.itzbreadloaf.brain.MaceTechniques.Technique.byId(id);
		if (technique == null) {
			context.getSource().sendFailure(Component.literal("Unknown technique. Try pearl_catch, diagonal_pearl, elytra_mace, wind_chain or wind_jump."));
			return 0;
		}
		if (brain.techniques() == null) {
			context.getSource().sendFailure(Component.literal(character.record.profileName() + " doesn't do mace moves (FelixSyn, Harley and Master BreadLoaf do)."));
			return 0;
		}
		var player = character.player;
		net.minecraft.world.entity.LivingEntity target = targetArg instanceof net.minecraft.world.entity.LivingEntity living ? living : null;
		if (target == null) {
			double best = 24.0;
			for (var entity : player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(24.0),
					e -> e != player && e.isAlive() && !e.isSpectator())) {
				double d = entity.distanceTo(player);
				if (d < best) {
					best = d;
					target = entity;
				}
			}
		}
		if (target == null) {
			context.getSource().sendFailure(Component.literal("Nothing to aim at within 24 blocks. Give a target, e.g. @p or an armor stand."));
			return 0;
		}
		java.util.List<String> given = new java.util.ArrayList<>();
		java.util.function.BiConsumer<net.minecraft.world.item.Item, Integer> ensure = (item, count) -> {
			if (com.itzbreadloaf.brain.Inv.count(player, stack -> stack.is(item)) < count) {
				com.itzbreadloaf.brain.Inv.give(player, new net.minecraft.world.item.ItemStack(item, count));
				given.add(count + " " + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath());
			}
		};
		ensure.accept(net.minecraft.world.item.Items.MACE, 1);
		ensure.accept(net.minecraft.world.item.Items.WIND_CHARGE, 4);
		if (technique == com.itzbreadloaf.brain.MaceTechniques.Technique.PEARL_CATCH
				|| technique == com.itzbreadloaf.brain.MaceTechniques.Technique.DIAGONAL_PEARL) {
			ensure.accept(net.minecraft.world.item.Items.ENDER_PEARL, 2);
		}
		if (technique == com.itzbreadloaf.brain.MaceTechniques.Technique.ELYTRA_MACE) {
			ensure.accept(net.minecraft.world.item.Items.FIREWORK_ROCKET, 8);
			if (!player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).is(net.minecraft.world.item.Items.ELYTRA)) {
				ensure.accept(net.minecraft.world.item.Items.ELYTRA, 1);
			}
			if (player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEmpty()) {
				player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_CHESTPLATE));
				given.add("an iron chestplate (worn)");
			}
		}
		brain.forceTechnique(technique, target);
		String note = given.isEmpty() ? "" : " (gave them " + String.join(", ", given) + ")";
		String targetName = target.getName().getString();
		reply(context, character.record.profileName() + ": " + technique.id() + " at " + targetName + note);
		return 1;
	}

	private static final SuggestionProvider<CommandSourceStack> MOODS = (context, builder) -> SharedSuggestionProvider.suggest(
			java.util.Arrays.stream(com.itzbreadloaf.brain.ai.Mood.values()).map(com.itzbreadloaf.brain.ai.Mood::id), builder);

	/** /loafcraft mood <name>: mood, routine, skill preset, play style and how many players they remember. */
	private static int showMind(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		LoafCharacter character = character(context);
		var brain = character.brain;
		reply(context, character.record.profileName() + ": " + brain.mood().mood().id() + " (" + Math.round(brain.mood().strength() * 100)
				+ "%), routine " + brain.routineActivity().id() + ", skill " + com.itzbreadloaf.config.ModConfig.get().aiPresetFor(character.kind())
				+ ", play style " + brain.playStyle().id());
		return 1;
	}

	/** /loafcraft mood <name> <mood>: holds that mood for 2 minutes (testing). */
	private static int setMood(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		LoafCharacter character = character(context);
		String id = StringArgumentType.getString(context, "mood");
		com.itzbreadloaf.brain.ai.Mood mood = com.itzbreadloaf.brain.ai.Mood.byId(id);
		if (!mood.id().equalsIgnoreCase(id)) {
			context.getSource().sendFailure(Component.literal("Unknown mood. Try happy, bored, tired, scared, angry or curious."));
			return 0;
		}
		character.brain.setMood(mood, 2400);
		reply(context, character.record.profileName() + " is now " + mood.id() + " (for 2 minutes)");
		return 1;
	}

	private static RequiredArgumentBuilder<CommandSourceStack, String> name() {
		return Commands.argument("name", StringArgumentType.word()).suggests(NAMES);
	}

	private static LoafCharacter character(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		LoafCharacter character = CharacterManager.byName(StringArgumentType.getString(context, "name"));
		if (character == null) {
			throw NOT_SPAWNED.create();
		}
		return character;
	}

	private static void reply(CommandContext<CommandSourceStack> context, String text) {
		context.getSource().sendSuccess(() -> Component.literal(text), false);
	}

	// ---------------------------------------------------------------- spawn, list, despawn, brain

	private static int spawn(CommandContext<CommandSourceStack> context, Vec3 pos) throws CommandSyntaxException {
		CharacterVariant kind = CharacterVariant.byId(StringArgumentType.getString(context, "character"));
		if (kind == null) {
			throw NO_SUCH_KIND.create();
		}
		CommandSourceStack source = context.getSource();
		LoafCharacter character = kind.kind == CharacterKind.MASTER
				? BossSpawner.spawnBoss(source.getLevel(), BlockPos.containing(pos))
				: kind == CharacterVariant.WILD_FELIX
				? com.itzbreadloaf.brain.FelixBrain.spawnWild(source.getLevel(), pos, BlockPos.containing(pos), 12, null)
				: kind.kind == CharacterKind.FELIXSYN
				? com.itzbreadloaf.brain.FelixBrain.spawnDuo(source.getLevel(), pos, BlockPos.containing(pos))
				: CharacterManager.create(source.getLevel(), kind, pos, source.getRotation().y, null);
		if (character == null) {
			source.sendFailure(Component.literal("Could not spawn " + kind.displayName() + " (see the log)."));
			return 0;
		}
		source.sendSuccess(() -> Component.literal("Spawned " + character.record.profileName()), true);
		return 1;
	}

	private static int list(CommandContext<CommandSourceStack> context) {
		LoafWorldData data = LoafWorldData.get(context.getSource().getServer());
		if (data.records().isEmpty()) {
			reply(context, "No LoafCraft characters in this world yet.");
			return 0;
		}
		for (CharacterRecord record : data.records()) {
			LoafCharacter active = CharacterManager.get(record.id);
			Vec3 pos = active != null ? active.player.position() : record.pos;
			String where = String.format(Locale.ROOT, "%s %d %d %d", (active != null ? active.player.level().dimension() : record.dimension).identifier().getPath(),
					(int) pos.x, (int) pos.y, (int) pos.z);
			String state = active == null ? "saved" : active.brain.isPaused() ? "active, paused" : "active";
			float health = active != null ? active.player.getHealth() : record.health;
			String owner = "";
			if (active != null && active.brain.getOwner() != null) {
				ServerPlayer ownerPlayer = active.brain.getOwnerPlayer();
				owner = ", owner " + (ownerPlayer != null ? ownerPlayer.getGameProfile().name() : active.brain.getOwner().toString().substring(0, 8));
			}
			String line = record.profileName() + " (" + record.variant().displayName() + ") - " + state + ", " + where
					+ String.format(Locale.ROOT, ", %.0f HP", health) + owner;
			context.getSource().sendSuccess(() -> Component.literal(line).withStyle(active != null ? ChatFormatting.GREEN : ChatFormatting.GRAY), false);
		}
		reply(context, CharacterManager.active().size() + " active of " + data.records().size() + " (max active "
				+ com.itzbreadloaf.config.ModConfig.get().maxActiveCharacters + ")");
		return data.records().size();
	}

	private static int despawn(CommandContext<CommandSourceStack> context) {
		String name = StringArgumentType.getString(context, "name");
		LoafCharacter character = CharacterManager.byName(name);
		if (character != null) {
			CharacterManager.delete(character);
		} else {
			LoafWorldData data = LoafWorldData.get(context.getSource().getServer());
			CharacterRecord record = data.byName(name);
			if (record == null) {
				context.getSource().sendFailure(Component.literal("No character named " + name));
				return 0;
			}
			data.remove(record.id);
		}
		context.getSource().sendSuccess(() -> Component.literal("Removed " + name + " for good"), true);
		return 1;
	}

	private static int pause(CommandContext<CommandSourceStack> context, boolean paused) throws CommandSyntaxException {
		LoafCharacter character = character(context);
		character.brain.setPaused(paused);
		reply(context, character.record.profileName() + "'s brain " + (paused ? "paused (they stand still; /loafcraft action works)" : "resumed"));
		return 1;
	}

	// ---------------------------------------------------------------- actions (Carpet action pack)

	private static RequiredArgumentBuilder<CommandSourceStack, String> actions(RequiredArgumentBuilder<CommandSourceStack, String> name) {
		for (String action : new String[]{"use", "attack", "jump", "sneak", "unsneak", "sprint", "walk", "swap", "stop"}) {
			name.then(Commands.literal(action).executes(c -> action(c, action)));
		}
		return name.then(Commands.literal("slot").then(Commands.argument("slot", IntegerArgumentType.integer(1, 9))
				.executes(c -> action(c, "slot"))));
	}

	private static int action(CommandContext<CommandSourceStack> context, String action) throws CommandSyntaxException {
		LoafCharacter character = character(context);
		LoafBrain brain = character.brain;
		if (!brain.isPaused()) {
			brain.setPaused(true);
		}
		EntityPlayerActionPack pack = character.actions();
		switch (action) {
			case "use" -> pack.start(ActionType.USE, Action.once());
			case "attack" -> pack.start(ActionType.ATTACK, Action.once());
			case "jump" -> pack.start(ActionType.JUMP, Action.once());
			case "sneak" -> pack.setSneaking(true);
			case "unsneak" -> pack.setSneaking(false);
			case "sprint" -> pack.setSprinting(true);
			case "walk" -> pack.setSprinting(false);
			case "swap" -> pack.start(ActionType.SWAP_HANDS, Action.once());
			case "stop" -> pack.stopAll();
			case "slot" -> pack.setSlot(IntegerArgumentType.getInteger(context, "slot"));
			default -> {
			}
		}
		reply(context, character.record.profileName() + ": " + action + " (holding " + character.player.getMainHandItem().getHoverName().getString()
				+ "). Brain is paused: /loafcraft brain " + character.record.profileName() + " resume");
		return 1;
	}

	private static int boss(CommandContext<CommandSourceStack> context, Vec3 pos) {
		return BossSpawner.spawnBoss(context.getSource().getLevel(), BlockPos.containing(pos)) != null ? 1 : 0;
	}

	// ---------------------------------------------------------------- inventory and player data

	private static RequiredArgumentBuilder<CommandSourceStack, String> inventory(RequiredArgumentBuilder<CommandSourceStack, String> name,
			CommandBuildContext registryAccess) {
		return name
				.then(Commands.literal("list").executes(LoafCraftCommands::listInventory))
				.then(Commands.literal("give").then(Commands.argument("item", ItemArgument.item(registryAccess))
						.executes(c -> give(c, 1))
						.then(Commands.argument("count", IntegerArgumentType.integer(1, 6400))
								.executes(c -> give(c, IntegerArgumentType.getInteger(c, "count"))))))
				.then(Commands.literal("set").then(Commands.argument("slot", SlotArgument.slot())
						.then(Commands.argument("item", ItemArgument.item(registryAccess))
								.executes(c -> setSlot(c, 1))
								.then(Commands.argument("count", IntegerArgumentType.integer(1, 99))
										.executes(c -> setSlot(c, IntegerArgumentType.getInteger(c, "count")))))))
				.then(Commands.literal("clear")
						.executes(c -> clear(c, false))
						.then(Commands.argument("slot", SlotArgument.slot()).executes(c -> clear(c, true))));
	}

	private static int listInventory(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		LoafCharacter character = character(context);
		Inventory inventory = character.player.getInventory();
		reply(context, character.record.profileName() + " (hotbar slot " + (inventory.getSelectedSlot() + 1) + " selected, "
				+ String.format(Locale.ROOT, "%.1f", character.player.getHealth()) + " HP, food " + character.player.getFoodData().getFoodLevel() + "):");
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty()) {
				String slot = i < 9 ? "hotbar " + (i + 1) : i < 36 ? "inventory " + (i - 9) : i == 40 ? "offhand"
						: new String[]{"feet", "legs", "chest", "head"}[i - 36];
				reply(context, "  " + slot + ": " + stack.getCount() + "x " + stack.getHoverName().getString());
			}
		}
		return 1;
	}

	private static int give(CommandContext<CommandSourceStack> context, int count) throws CommandSyntaxException {
		LoafCharacter character = character(context);
		ItemStack stack = ItemArgument.getItem(context, "item").createItemStack(count, false);
		String what = stack.getCount() + "x " + stack.getHoverName().getString();
		boolean added = character.player.getInventory().add(stack);
		reply(context, (added ? "Gave " : "Inventory full, gave part of ") + what + " to " + character.record.profileName());
		return added ? 1 : 0;
	}

	private static int setSlot(CommandContext<CommandSourceStack> context, int count) throws CommandSyntaxException {
		LoafCharacter character = character(context);
		int slot = SlotArgument.getSlot(context, "slot");
		ItemStack stack = ItemArgument.getItem(context, "item").createItemStack(count, false);
		SlotAccess access = character.player.getSlot(slot);
		if (access == null || !access.set(stack)) {
			context.getSource().sendFailure(Component.literal("Can't put that in that slot."));
			return 0;
		}
		reply(context, "Set " + character.record.profileName() + "'s slot to " + stack.getCount() + "x " + stack.getHoverName().getString());
		return 1;
	}

	private static int clear(CommandContext<CommandSourceStack> context, boolean oneSlot) throws CommandSyntaxException {
		LoafCharacter character = character(context);
		if (oneSlot) {
			SlotAccess access = character.player.getSlot(SlotArgument.getSlot(context, "slot"));
			if (access != null) {
				access.set(ItemStack.EMPTY);
			}
		} else {
			character.player.getInventory().clearContent();
		}
		reply(context, "Cleared " + character.record.profileName() + (oneSlot ? "'s slot" : "'s inventory"));
		return 1;
	}

	private static int setData(CommandContext<CommandSourceStack> context, String what) throws CommandSyntaxException {
		LoafCharacter character = character(context);
		float value = FloatArgumentType.getFloat(context, "value");
		switch (what) {
			case "health" -> character.player.setHealth(Math.min(value, character.player.getMaxHealth()));
			case "food" -> character.player.getFoodData().setFoodLevel((int) value);
			default -> character.player.getFoodData().setSaturation(value);
		}
		CharacterManager.snapshot(character);
		reply(context, character.record.profileName() + " " + what + " = " + value);
		return 1;
	}

}
