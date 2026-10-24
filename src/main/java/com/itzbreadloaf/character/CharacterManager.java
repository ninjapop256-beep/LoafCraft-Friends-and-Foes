package com.itzbreadloaf.character;

import carpet.patches.EntityPlayerMPFake;
import com.itzbreadloaf.ItzBreadLoaf;
import com.itzbreadloaf.brain.LoafBrain;
import com.itzbreadloaf.config.ModConfig;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Spawns, ticks, saves and despawns the character fake players.
 *
 * - Activation: a character exists as a fake player only while a real player is within about 4 chunks (64 blocks).
 *   Farther away it is saved to {@link LoafWorldData} and removed; it comes back when someone is near again.
 *   At most {@code maxActiveCharacters} (config, default 20) are active at once, closest first.
 * - Every server tick each brain runs before the world ticks, then Carpet's action pack plays its inputs.
 * - Death is handled here (Fabric ALLOW_DEATH), not by Carpet: custom drops and a short message, then the
 *   character is gone for good.
 */
public final class CharacterManager {
	public static final double DEACTIVATION_MARGIN = 16.0;

	private static final Map<UUID, LoafCharacter> ACTIVE = new LinkedHashMap<>();
	private static @Nullable MinecraftServer server;

	private CharacterManager() {
	}

	public static void register() {

		ServerLifecycleEvents.SERVER_STARTED.register(started -> {
			server = started;
			ACTIVE.clear();
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(CharacterManager::onStopping);
		ServerLifecycleEvents.SERVER_STOPPED.register(stopped -> {
			ACTIVE.clear();
			server = null;
		});
		ServerTickEvents.START_SERVER_TICK.register(CharacterManager::tickBrains);
		ServerTickEvents.END_SERVER_TICK.register(CharacterManager::endTick);

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			LoafCharacter character = get(entity);
			return character == null || character.isGone() || character.brain.allowDamage(source, amount);
		});
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			LoafCharacter character = get(entity);
			if (character != null && !character.isGone()) {
				character.brain.onHurt(source, damageTaken, blocked);
			}
			// Let attackers' brains know they landed a hit.
			LoafCharacter attacker = get(source.getEntity());
			if (attacker != null && !attacker.isGone() && entity != attacker.player) {
				attacker.brain.onDealtDamage(entity, damageTaken);
			}
			// Characters nearby glance at the commotion.
			if (damageTaken > 0.0F) {
				for (LoafCharacter nearby : active()) {
					if (nearby != character && nearby != attacker && nearby.brain != null && !nearby.isGone()
							&& nearby.player.level() == entity.level() && nearby.player.distanceToSqr(entity) < 144.0) {
						nearby.brain.noticeCommotion(entity);
					}
				}
			}
		});
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			LoafCharacter character = get(entity);
			if (character == null) {
				return true;
			}
			if (!character.isGone()) {
				die(character, source);
			}
			character.player.setHealth(character.player.getMaxHealth());
			return false;
		});
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			if (!LoafIds.isCharacter(entity) || LoafIds.isCharacter(player)) {
				return InteractionResult.PASS;
			}
			if (level.isClientSide()) {
				return CharacterInteractions.clientResult(player, hand);
			}
			LoafCharacter character = ACTIVE.get(entity.getUUID());
			if (character == null || character.isGone() || !(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.PASS;
			}
			// The client can send both "interact at" and "interact" for one click; only act once.
			long tick = serverPlayer.level().getServer().getTickCount();
			String who = serverPlayer.getStringUUID() + hand;
			if (character.lastInteractTick == tick && character.lastInteractPlayer.equals(who)) {
				return InteractionResult.SUCCESS;
			}
			InteractionResult result = character.brain.onInteract(serverPlayer, hand);
			if (result.consumesAction()) {
				character.lastInteractTick = tick;
				character.lastInteractPlayer = who;
			}
			return result;
		});
	}

	// ---------------------------------------------------------------- lookups

	public static Collection<LoafCharacter> active() {
		return ACTIVE.values();
	}

	public static @Nullable LoafCharacter get(UUID id) {
		return ACTIVE.get(id);
	}

	public static @Nullable LoafCharacter get(@Nullable Entity entity) {
		return entity instanceof Player && LoafIds.isCharacter(entity.getUUID()) ? ACTIVE.get(entity.getUUID()) : null;
	}

	public static @Nullable LoafBrain brain(@Nullable Entity entity) {
		LoafCharacter character = get(entity);
		return character == null || character.isGone() ? null : character.brain;
	}

	public static @Nullable LoafCharacter byName(String name) {
		for (LoafCharacter character : ACTIVE.values()) {
			if (character.record.profileName().equalsIgnoreCase(name)) {
				return character;
			}
		}
		return null;
	}

	public static LoafWorldData data(MinecraftServer server) {
		return LoafWorldData.get(server);
	}


	/** Vanilla mobs leave friendly characters alone (Lily, Madison, tamed ItzBreadLoafs). */
	public static boolean isFriendlyToMobs(UUID id) {
		LoafCharacter character = ACTIVE.get(id);
		return character != null && character.brain != null && character.brain.isFriendlyToMobs();
	}

	public static boolean wantsToPickUp(Player player, ItemStack stack) {
		LoafCharacter character = ACTIVE.get(player.getUUID());
		return character != null && character.brain != null && !character.isGone() && character.brain.wantsToPickUp(stack);
	}

	/** Real players who can activate characters (not spectators, not characters). */
	public static List<ServerPlayer> realPlayers(MinecraftServer server) {
		List<ServerPlayer> players = new ArrayList<>();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!LoafIds.isCharacter(player) && !player.isSpectator()) {
				players.add(player);
			}
		}
		return players;
	}

	// ---------------------------------------------------------------- creating, activating, despawning

	/**
	 * Creates a brand-new character at a position and spawns it right away (even past the active cap).
	 * {@code setup} runs after its first gear roll: set a home, an owner, a baby flag...
	 */
	public static @Nullable LoafCharacter create(ServerLevel level, CharacterKind kind, Vec3 pos, float yaw,
			@Nullable Consumer<LoafBrain> setup) {
		return create(level, CharacterVariant.base(kind), pos, yaw, setup);
	}

	/** Same, for a specific type (Knight ItzBreadLoaf, Witchy ImLilyLol, Baker Madison...). */
	public static @Nullable LoafCharacter create(ServerLevel level, CharacterVariant variant, Vec3 pos, float yaw,
			@Nullable Consumer<LoafBrain> setup) {
		MinecraftServer owner = level.getServer();
		LoafWorldData data = data(owner);
		int index = data.freeIndex(variant.kind, owner);
		CharacterRecord record = CharacterRecord.create(variant, index, level.dimension(), pos, yaw);
		data.put(record);
		LoafCharacter character = activate(owner, record);
		if (character == null) {
			data.remove(record.id);
			return null;
		}
		character.brain.onFirstSpawn();
		if (setup != null) {
			setup.accept(character.brain);
		}
		character.brain.applyAttributes();
		snapshot(character);
		return character;
	}

	private static @Nullable LoafCharacter activate(MinecraftServer server, CharacterRecord record) {
		if (ACTIVE.containsKey(record.id) || server.getPlayerList().getPlayer(record.id) != null) {
			return null;
		}
		ServerLevel level = server.getLevel(record.dimension);
		if (level == null) {
			level = server.overworld();
			record.dimension = level.dimension();
		}
		EntityPlayerMPFake fake;
		try {
			fake = FakePlayers.spawn(server, level, new GameProfile(record.id, record.profileName()), record.pos, record.yaw, record.pitch);
		} catch (RuntimeException e) {
			ItzBreadLoaf.LOGGER.error("Could not spawn {}", record.profileName(), e);
			return null;
		}
		LoafCharacter character = new LoafCharacter(fake, record);
		ACTIVE.put(record.id, character);
		character.brain = LoafBrain.create(character);
		character.brain.load(record.brain);
		character.brain.applyAttributes();
		applyRecord(record, fake);
		character.brain.onActivated();
		return character;
	}

	/** Writes the saved state into the fake player's real player data. */
	private static void applyRecord(CharacterRecord record, ServerPlayer player) {
		Inventory inventory = player.getInventory();
		inventory.clearContent();
		for (CharacterRecord.Slot slot : record.inventory) {
			if (slot.slot() >= 0 && slot.slot() < inventory.getContainerSize()) {
				inventory.setItem(slot.slot(), slot.stack().copy());
			}
		}
		inventory.setSelectedSlot(Math.max(0, Math.min(8, record.selectedSlot)));
		player.removeAllEffects();
		for (MobEffectInstance effect : record.effects) {
			player.addEffect(new MobEffectInstance(effect));
		}
		player.setHealth(record.health > 0.0F ? Math.min(record.health, player.getMaxHealth()) : player.getMaxHealth());
		player.getFoodData().setFoodLevel(record.food);
		player.getFoodData().setSaturation(record.saturation);
	}

	/** Copies the fake player's current state into its record (and marks the world data for saving). */
	public static void snapshot(LoafCharacter character) {
		CharacterRecord record = character.record;
		ServerPlayer player = character.player;
		record.dimension = player.level().dimension();
		record.pos = player.position();
		record.yaw = player.getYRot();
		record.pitch = player.getXRot();
		record.health = Math.max(1.0F, player.getHealth());
		record.food = player.getFoodData().getFoodLevel();
		record.saturation = player.getFoodData().getSaturationLevel();
		Inventory inventory = player.getInventory();
		List<CharacterRecord.Slot> slots = new ArrayList<>();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty()) {
				slots.add(new CharacterRecord.Slot(i, stack.copy()));
			}
		}
		record.inventory = slots;
		record.selectedSlot = inventory.getSelectedSlot();
		List<MobEffectInstance> effects = new ArrayList<>();
		for (MobEffectInstance effect : player.getActiveEffects()) {
			effects.add(new MobEffectInstance(effect));
		}
		record.effects = effects;
		net.minecraft.nbt.CompoundTag brainTag = new net.minecraft.nbt.CompoundTag();
		character.brain.save(brainTag);
		record.brain = brainTag;
		data(player.level().getServer()).setDirty();
	}

	/** Saves (unless {@code keepRecord} is false) and removes the fake player. */
	public static void deactivate(LoafCharacter character, boolean keepRecord) {
		if (character.deactivating) {
			return;
		}
		MinecraftServer owner = character.player.level().getServer();
		if (keepRecord) {
			snapshot(character);
		}
		character.brain.onDeactivated();
		character.deactivating = true;
		ACTIVE.remove(character.record.id);
		if (!keepRecord) {
			data(owner).remove(character.record.id);
		}
		character.actions().stopAll();
		character.player.kill(Component.literal("LoafCraft: despawned"));
	}

	/** Removes a character for good (the /loafcraft despawn command). */
	public static void delete(LoafCharacter character) {
		deactivate(character, false);
	}

	private static void die(LoafCharacter character, DamageSource source) {
		ServerPlayer player = character.player;
		ServerLevel level = player.level();
		boolean dropInventory = character.brain.onDeath(source);
		if (dropInventory) {
			player.getInventory().dropAll();
		}
		int xp = character.brain.experienceReward();
		if (xp > 0) {
			ExperienceOrb.award(level, player.position(), xp);
		}
		if (level.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES)) {
			Entity killer = source.getEntity();
			Component message = Component.empty().append(player.getDisplayName())
					.append(killer != null && killer != player
							? Component.literal(" was defeated by ").append(killer.getDisplayName())
							: Component.literal(" was defeated"))
					.withStyle(ChatFormatting.GRAY);
			level.getServer().getPlayerList().broadcastSystemMessage(message, false);
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(), character.brain.deathSound(), player.getSoundSource(), 1.0F, 1.0F);
		level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY(0.5), player.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
		deactivate(character, false);
	}

	/** Vanilla /kill on a character: a real death instead of Carpet's silent disconnect. */
	public static boolean killByCommand(ServerPlayer player) {
		LoafCharacter character = get(player);
		if (character == null) {
			return false;
		}
		if (!character.isGone()) {
			die(character, player.damageSources().genericKill());
		}
		return true;
	}

	// ---------------------------------------------------------------- ticking

	private static void tickBrains(MinecraftServer server) {
		if (ACTIVE.isEmpty()) {
			return;
		}
		for (LoafCharacter character : List.copyOf(ACTIVE.values())) {
			if (character.deactivating) {
				continue;
			}
			if (character.player.isRemoved() && !character.player.hasDisconnected() && character.player.isChangingDimension()) {
				continue;
			}
			if (character.player.hasDisconnected() || character.player.isRemoved()) {
				// Removed by something else (Carpet's /player kill, /kick): keep it saved, it will come back.
				snapshot(character);
				character.deactivating = true;
				ACTIVE.remove(character.record.id);
				continue;
			}
			try {
				character.brain.serverTick(server.getTickCount());
			} catch (RuntimeException e) {
				ItzBreadLoaf.LOGGER.error("{}'s brain crashed, pausing it", character.record.profileName(), e);
				character.brain.setPaused(true);
			}
		}
	}

	private static void endTick(MinecraftServer server) {
		int tick = server.getTickCount();
		if (tick % 20 == 0) {
			scan(server);
		}
		if (tick % 1200 == 0) {
			for (LoafCharacter character : ACTIVE.values()) {
				if (!character.isGone()) {
					snapshot(character);
				}
			}
		}
	}

	/** Despawns characters nobody is near, then spawns saved ones a real player came close to (closest first). */
	private static void scan(MinecraftServer server) {
		List<ServerPlayer> real = realPlayers(server);
		double range = ModConfig.get().activationRange;
		for (LoafCharacter character : List.copyOf(ACTIVE.values())) {
			if (character.isGone() || character.brain.keepActive()) {
				continue;
			}
			if (nearestRealDistance(real, character.player.level(), character.player.position()) > range + DEACTIVATION_MARGIN) {
				deactivate(character, !character.brain.isDisposable());
			}
		}
		int cap = Math.max(0, ModConfig.get().maxActiveCharacters);
		if (ACTIVE.size() >= cap || real.isEmpty()) {
			return;
		}
		LoafWorldData data = data(server);
		List<CharacterRecord> candidates = new ArrayList<>();
		Map<CharacterRecord, Double> distances = new LinkedHashMap<>();
		for (CharacterRecord record : data.records()) {
			if (ACTIVE.containsKey(record.id)) {
				continue;
			}
			ServerLevel level = server.getLevel(record.dimension);
			if (level == null || !level.isLoaded(BlockPos.containing(record.pos))) {
				continue;
			}
			double distance = nearestRealDistance(real, level, record.pos);
			if (distance <= range) {
				candidates.add(record);
				distances.put(record, distance);
			}
		}
		candidates.sort(Comparator.comparingDouble(distances::get));
		for (CharacterRecord record : candidates) {
			if (ACTIVE.size() >= cap) {
				break;
			}
			activate(server, record);
		}
	}

	private static double nearestRealDistance(List<ServerPlayer> real, ServerLevel level, Vec3 pos) {
		double best = Double.MAX_VALUE;
		for (ServerPlayer player : real) {
			if (player.level() == level) {
				best = Math.min(best, player.position().distanceTo(pos));
			}
		}
		return best;
	}


	private static void onStopping(MinecraftServer stopping) {
		for (LoafCharacter character : List.copyOf(ACTIVE.values())) {
			if (!character.isGone()) {
				snapshot(character);
				character.brain.onDeactivated();
			}
		}
		ACTIVE.clear();
	}
}
