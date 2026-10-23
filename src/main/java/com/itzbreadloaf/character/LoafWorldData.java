package com.itzbreadloaf.character;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

/**
 * Every character in the world, active or not (data/itzbreadloaf_characters.dat in the overworld folder).
 * Characters are saved here instead of Carpet's rejoin or player data files.
 */
public class LoafWorldData extends SavedData {
	public static final Codec<LoafWorldData> CODEC = CharacterRecord.CODEC.listOf().fieldOf("characters").codec()
			.xmap(LoafWorldData::new, data -> List.copyOf(data.records.values()));
	public static final SavedDataType<LoafWorldData> TYPE = new SavedDataType<>("itzbreadloaf_characters", LoafWorldData::new, CODEC, null);

	private final Map<UUID, CharacterRecord> records = new LinkedHashMap<>();

	public LoafWorldData() {
	}

	private LoafWorldData(List<CharacterRecord> list) {
		for (CharacterRecord record : list) {
			this.records.put(record.id, record);
		}
	}

	public static LoafWorldData get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	public Collection<CharacterRecord> records() {
		return this.records.values();
	}

	public @Nullable CharacterRecord get(UUID id) {
		return this.records.get(id);
	}

	public @Nullable CharacterRecord byName(String name) {
		for (CharacterRecord record : this.records.values()) {
			if (record.profileName().equalsIgnoreCase(name)) {
				return record;
			}
		}
		return null;
	}

	public void put(CharacterRecord record) {
		this.records.put(record.id, record);
		this.setDirty();
	}

	public void remove(UUID id) {
		if (this.records.remove(id) != null) {
			this.setDirty();
		}
	}

	/** The lowest free instance number for this kind: 1 is "ItzBreadLoaf", 2 is "ItzBreadLoaf_2"... */
	public int freeIndex(CharacterKind kind, MinecraftServer server) {
		for (int index = 1; ; index++) {
			final int candidate = index;
			boolean taken = this.records.values().stream().anyMatch(r -> r.kind == kind && r.index == candidate)
					|| server.getPlayerList().getPlayerByName(LoafIds.profileName(kind, index)) != null;
			if (!taken) {
				return index;
			}
		}
	}
}
