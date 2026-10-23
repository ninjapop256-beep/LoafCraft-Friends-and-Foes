package com.itzbreadloaf.character;

import com.itzbreadloaf.worldgen.HomeKind;
import com.itzbreadloaf.worldgen.HomePiece;
import com.itzbreadloaf.worldgen.RuntimeSites;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Bastion Throne Rooms: the first time a real player walks into a bastion remnant, Master BreadLoaf's throne room is
 * carved into the middle of it (a closed blackstone-and-gold hall, so no lava gets in) with Master waiting inside.
 * Each bastion gets one, saved with the world. Find bastions with /locate structure minecraft:bastion_remnant.
 */
public final class BastionThrones {
	private BastionThrones() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(BastionThrones::tick);
	}

	private static void tick(MinecraftServer server) {
		if (server.getTickCount() % 100 != 61) {
			return;
		}
		ServerLevel nether = server.getLevel(Level.NETHER);
		if (nether == null) {
			return;
		}
		for (ServerPlayer player : CharacterManager.realPlayers(server)) {
			if (player.level() != nether) {
				continue;
			}
			StructureStart bastion = nether.structureManager().getStructureWithPieceAt(player.blockPosition(),
					holder -> holder.is(BuiltinStructures.BASTION_REMNANT));
			if (!bastion.isValid()) {
				continue;
			}
			Data data = server.overworld().getDataStorage().computeIfAbsent(Data.TYPE);
			String id = String.valueOf(bastion.getChunkPos().toLong());
			if (data.bastions.contains(id)) {
				continue;
			}
			BoundingBox area = bastion.getBoundingBox();
			BlockPos center = area.getCenter();
			int size = com.itzbreadloaf.worldgen.build.StructureCatalog.get("bastion_throne_room").width();
			int x0 = center.getX() - size / 2;
			int z0 = center.getZ() - size / 2;
			// Wait until the whole room's area is loaded.
			if (!nether.hasChunkAt(new BlockPos(x0, 64, z0)) || !nether.hasChunkAt(new BlockPos(x0 + size, 64, z0 + size))) {
				continue;
			}
			data.bastions.add(id);
			data.setDirty();
			int floor = findFloor(nether, center, area);
			BoundingBox built = com.itzbreadloaf.worldgen.build.BlueprintPiece.buildNow(nether, "bastion_throne_room", x0, z0, floor, nether.getRandom());
			RuntimeSites.add(nether, "bastion_throne_room", built);
		}
	}

	/** A floor height near the middle of the bastion: the first solid block with air above, going down from the center. */
	private static int findFloor(ServerLevel level, BlockPos center, BoundingBox area) {
		int top = Math.min(area.maxY() - 8, center.getY() + 6);
		for (int y = top; y > area.minY() + 1; y--) {
			BlockPos at = new BlockPos(center.getX(), y, center.getZ());
			if (!level.getBlockState(at).isAir() && level.getFluidState(at).isEmpty() && level.getBlockState(at.above()).isAir()) {
				return y;
			}
		}
		return center.getY();
	}

	/** Bastions that already have their throne room. */
	public static class Data extends SavedData {
		public static final Codec<Data> CODEC = Codec.STRING.listOf().fieldOf("bastions").codec()
				.xmap(Data::new, data -> List.copyOf(data.bastions));
		public static final SavedDataType<Data> TYPE = new SavedDataType<>("itzbreadloaf_bastion_thrones", Data::new, CODEC, null);

		final Set<String> bastions;

		public Data() {
			this(List.of());
		}

		public Data(List<String> bastions) {
			this.bastions = new HashSet<>(bastions);
		}
	}
}
