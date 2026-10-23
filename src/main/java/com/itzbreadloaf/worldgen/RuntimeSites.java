package com.itzbreadloaf.worldgen;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

/**
 * Structures the mod builds while the game runs instead of at world generation (Village Campsites at the edge of
 * villages, Bastion Throne Rooms inside bastions). /locate can't find these, so their boxes are saved here and the
 * structure tracker checks them for "found it" and their advancements.
 */
public final class RuntimeSites {
	/** One built site: what it is, which dimension, and its box. */
	public record Site(String kind, String dimension, BoundingBox box) {
		String encode() {
			return this.kind + "|" + this.dimension + "|" + this.box.minX() + "|" + this.box.minY() + "|" + this.box.minZ()
					+ "|" + this.box.maxX() + "|" + this.box.maxY() + "|" + this.box.maxZ();
		}

		static @Nullable Site decode(String text) {
			String[] parts = text.split("\\|");
			if (parts.length != 8) {
				return null;
			}
			try {
				int[] n = new int[6];
				for (int i = 0; i < 6; i++) {
					n[i] = Integer.parseInt(parts[i + 2]);
				}
				return new Site(parts[0], parts[1], new BoundingBox(n[0], n[1], n[2], n[3], n[4], n[5]));
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}

	private RuntimeSites() {
	}

	public static void add(ServerLevel level, HomeKind kind, BoundingBox box) {
		Data data = data(level.getServer());
		data.sites.add(new Site(kind.getSerializedName(), level.dimension().identifier().toString(), box));
		data.setDirty();
	}

	public static List<Site> all(MinecraftServer server) {
		return data(server).sites;
	}

	private static Data data(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(Data.TYPE);
	}

	public static class Data extends SavedData {
		public static final Codec<Data> CODEC = Codec.STRING.listOf().fieldOf("sites").codec()
				.xmap(Data::new, data -> data.sites.stream().map(Site::encode).toList());
		public static final SavedDataType<Data> TYPE = new SavedDataType<>("itzbreadloaf_runtime_sites", Data::new, CODEC, null);

		final List<Site> sites = new ArrayList<>();

		public Data() {
		}

		public Data(List<String> stored) {
			for (String text : stored) {
				Site site = Site.decode(text);
				if (site != null) {
					this.sites.add(site);
				}
			}
		}
	}
}
