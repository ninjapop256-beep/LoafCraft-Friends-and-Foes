package com.itzbreadloaf.boss;

import com.itzbreadloaf.brain.MasterBreadLoafBrain;
import com.itzbreadloaf.character.CharacterKind;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.LoafCharacter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Where Master BreadLoaf appears. Never in the Overworld naturally.
 * - Nether fortresses and bastion remnants: when a player first enters one, a single roll (25%); at most one per structure.
 * - End cities: one per city, on its highest part (end ship / top tower).
 * - The Ender Dragon fight: once per dragon, when it drops to half health.
 * Which structures and dragons were already handled is saved with the world.
 */
public final class BossSpawner {
	public static final float NETHER_CHANCE = 0.25F;

	private BossSpawner() {
	}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(BossSpawner::tick);
		// "Mace Master": remember who took fall damage near a living boss.
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damage, blocked) -> {
			if (entity instanceof ServerPlayer player && source.is(DamageTypeTags.IS_FALL)
					&& !com.itzbreadloaf.character.LoafIds.isCharacter(player)) {
				for (LoafCharacter character : CharacterManager.active()) {
					if (character.brain instanceof MasterBreadLoafBrain boss && character.player.level() == player.level()
							&& character.player.distanceTo(player) < 96.0) {
						boss.markFallDamage(player.getUUID());
					}
				}
			}
		});
	}

	private static void tick(ServerLevel level) {
		if (level.getGameTime() % 100 != 0) {
			return;
		}
		if (level.dimension() == Level.NETHER) {
			checkStructures(level, BuiltinStructures.FORTRESS, NETHER_CHANCE, false);
			checkStructures(level, BuiltinStructures.BASTION_REMNANT, NETHER_CHANCE, false);
		} else if (level.dimension() == Level.END) {
			checkStructures(level, BuiltinStructures.END_CITY, 1.0F, true);
			checkDragon(level);
		}
	}

	private static Data data(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(Data.TYPE);
	}

	private static void checkStructures(ServerLevel level, ResourceKey<Structure> key, float chance, boolean top) {
		Structure structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(key);
		if (structure == null) {
			return;
		}
		for (ServerPlayer player : level.players()) {
			if (com.itzbreadloaf.character.LoafIds.isCharacter(player) || player.isSpectator()) {
				continue;
			}
			StructureStart start = level.structureManager().getStructureWithPieceAt(player.blockPosition(), structure);
			if (!start.isValid()) {
				continue;
			}
			String id = level.dimension().identifier() + "|" + key.identifier() + "|" + start.getChunkPos().toLong();
			Data data = data(level);
			if (!data.structures.add(id)) {
				continue;
			}
			data.setDirty();
			if (level.getRandom().nextFloat() < chance) {
				BlockPos spot = findSpot(level, start, top, player);
				if (spot != null) {
					spawnBoss(level, spot);
				}
			}
		}
	}

	private static void checkDragon(ServerLevel level) {
		for (EnderDragon dragon : level.getDragons()) {
			if (dragon.isAlive() && dragon.getHealth() <= dragon.getMaxHealth() * 0.5F) {
				Data data = data(level);
				if (data.dragons.add(dragon.getUUID())) {
					data.setDirty();
					int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 6, 0);
					spawnBoss(level, new BlockPos(6, y, 0));
				}
			}
		}
	}

	/** A standable spot inside the structure: on its highest piece for end cities, a random piece otherwise. */
	private static BlockPos findSpot(ServerLevel level, StructureStart start, boolean top, ServerPlayer player) {
		List<StructurePiece> pieces = new ArrayList<>(start.getPieces());
		if (pieces.isEmpty()) {
			return null;
		}
		RandomSource random = level.getRandom();
		if (top) {
			pieces.sort((a, b) -> Integer.compare(b.getBoundingBox().maxY(), a.getBoundingBox().maxY()));
		}
		for (int p = 0; p < Math.min(pieces.size(), top ? 4 : 12); p++) {
			StructurePiece piece = top ? pieces.get(p) : pieces.get(random.nextInt(pieces.size()));
			BoundingBox box = piece.getBoundingBox();
			for (int attempt = 0; attempt < 24; attempt++) {
				int x = box.minX() + random.nextInt(box.getXSpan());
				int z = box.minZ() + random.nextInt(box.getZSpan());
				for (int y = box.maxY(); y > box.minY(); y--) {
					BlockPos pos = new BlockPos(x, y, z);
					if (!level.isLoaded(pos)) {
						break;
					}
					if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
							&& level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
							&& (top || pos.distSqr(player.blockPosition()) > 10 * 10)) {
						return pos;
					}
				}
			}
		}
		return null;
	}

	/** Master BreadLoaf arrives (a new Carpet fake player), with a sound and a message to everyone near. */
	public static LoafCharacter spawnBoss(ServerLevel level, BlockPos pos) {
		LoafCharacter boss = CharacterManager.create(level, CharacterKind.MASTER, net.minecraft.world.phys.Vec3.atBottomCenterOf(pos),
				level.getRandom().nextFloat() * 360.0F, null);
		if (boss == null) {
			return null;
		}
		level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 1.2F);
		for (ServerPlayer player : level.players()) {
			if (player.blockPosition().distSqr(pos) < 128 * 128 && !com.itzbreadloaf.character.LoafIds.isCharacter(player)) {
				player.sendSystemMessage(Component.literal("Master BreadLoaf has appeared!").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
			}
		}
		return boss;
	}

	/** Saved per world: structures already rolled and dragons already handled. */
	public static class Data extends SavedData {
		public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.listOf().fieldOf("structures").forGetter(data -> List.copyOf(data.structures)),
				UUIDUtil.CODEC.listOf().fieldOf("dragons").forGetter(data -> List.copyOf(data.dragons))
		).apply(instance, Data::new));
		public static final SavedDataType<Data> TYPE = new SavedDataType<>("itzbreadloaf_boss_spawns", Data::new, CODEC, null);

		final Set<String> structures;
		final Set<UUID> dragons;

		public Data() {
			this(List.of(), List.of());
		}

		public Data(List<String> structures, List<UUID> dragons) {
			this.structures = new HashSet<>(structures);
			this.dragons = new HashSet<>(dragons);
		}
	}
}
