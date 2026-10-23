package com.itzbreadloaf.character;

import com.itzbreadloaf.brain.FelixBrain;
import com.itzbreadloaf.config.ModConfig;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

/**
 * FelixSyn (with Harley) in villages: the first time a real player walks into a village, one roll (40% by default,
 * felixVillageChance in the config) to have the duo show up near its middle. Each village is only rolled once,
 * saved with the world.
 */
public final class VillageSpawner {
	private VillageSpawner() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(VillageSpawner::tick);
	}

	private static void tick(MinecraftServer server) {
		if (server.getTickCount() % 100 != 37) {
			return;
		}
		for (ServerPlayer player : CharacterManager.realPlayers(server)) {
			ServerLevel level = player.level();
			StructureStart village = level.structureManager().getStructureWithPieceAt(player.blockPosition(), StructureTags.VILLAGE);
			if (!village.isValid()) {
				continue;
			}
			String id = level.dimension().identifier() + "|" + village.getChunkPos().toLong();
			Data data = server.overworld().getDataStorage().computeIfAbsent(Data.TYPE);
			if (!data.villages.add(id)) {
				continue;
			}
			data.setDirty();
			RandomSource random = level.getRandom();
			if (random.nextDouble() >= ModConfig.get().felixVillageChance) {
				continue;
			}
			// They set up camp at the edge of the village (the camp's marker brings them in); if there's no good
			// spot for a camp, they just show up near the middle like before.
			if (buildCampsite(level, village.getBoundingBox(), random)) {
				continue;
			}
			BlockPos spot = findSpot(level, village.getBoundingBox(), random);
			if (spot != null) {
				FelixBrain.spawnDuo(level, Vec3.atBottomCenterOf(spot), spot, true);
			}
		}
	}

	/** Builds a Village Campsite just outside one side of the village. True if it did. */
	private static boolean buildCampsite(ServerLevel level, BoundingBox village, RandomSource random) {
		int size = com.itzbreadloaf.worldgen.build.StructureCatalog.get("village_campsite").footprint();
		BlockPos center = village.getCenter();
		int[][] corners = {
				{village.maxX() + 3, center.getZ() - size / 2},
				{village.minX() - 3 - size, center.getZ() - size / 2},
				{center.getX() - size / 2, village.maxZ() + 3},
				{center.getX() - size / 2, village.minZ() - 3 - size}};
		int first = random.nextInt(corners.length);
		for (int i = 0; i < corners.length; i++) {
			int x0 = corners[(first + i) % corners.length][0];
			int z0 = corners[(first + i) % corners.length][1];
			if (!level.hasChunkAt(new BlockPos(x0, 64, z0)) || !level.hasChunkAt(new BlockPos(x0 + size, 64, z0 + size))) {
				continue;
			}
			int cx = x0 + size / 2;
			int cz = z0 + size / 2;
			int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz) - 1;
			BlockPos top = new BlockPos(cx, ground, cz);
			if (!level.getFluidState(top).isEmpty() || !level.getBlockState(top).isFaceSturdy(level, top, Direction.UP)) {
				continue;
			}
			BoundingBox built = com.itzbreadloaf.worldgen.build.BlueprintPiece.buildNow(level, "village_campsite", x0, z0, ground, random);
			com.itzbreadloaf.worldgen.RuntimeSites.add(level, "village_campsite", built);
			return true;
		}
		return false;
	}

	/** Somewhere standable near the middle of the village. */
	private static BlockPos findSpot(ServerLevel level, BoundingBox box, RandomSource random) {
		BlockPos center = box.getCenter();
		for (int attempt = 0; attempt < 20; attempt++) {
			int x = center.getX() + random.nextInt(17) - 8;
			int z = center.getZ() + random.nextInt(17) - 8;
			if (!level.isLoaded(new BlockPos(x, 0, z))) {
				continue;
			}
			int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
			BlockPos feet = new BlockPos(x, y, z);
			if (level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP) && level.getFluidState(feet).isEmpty()) {
				return feet;
			}
		}
		return null;
	}

	/** Villages already rolled. */
	public static class Data extends SavedData {
		public static final Codec<Data> CODEC = Codec.STRING.listOf().fieldOf("villages").codec()
				.xmap(Data::new, data -> List.copyOf(data.villages));
		public static final SavedDataType<Data> TYPE = new SavedDataType<>("itzbreadloaf_felix_villages", Data::new, CODEC, null);

		final Set<String> villages;

		public Data() {
			this(List.of());
		}

		public Data(List<String> villages) {
			this.villages = new HashSet<>(villages);
		}
	}
}
