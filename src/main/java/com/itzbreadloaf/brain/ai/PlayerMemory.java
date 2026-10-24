package com.itzbreadloaf.brain.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * What a character remembers about each real player: who fed them, who hurt them, when they last saw them, and a
 * trust score built from that (-100 to 100). Saved with the character; the 32 most recently seen players are kept.
 *
 * This sits next to the existing trust and taming systems (Lily's 5 gifts, taming food) and doesn't replace them:
 * it changes how they feel and act around a player, not whether they're tamed or trusted.
 */
public final class PlayerMemory {
	public static final int MAX_PLAYERS = 32;

	public static final class Entry {
		public int fed;
		public int hurt;
		public float damageTaken;
		public int helped;
		public long lastHurt = -1;
		public long lastSeen;
		public int trust;

		/** Hurt them in the last 20 minutes, or hurt them a lot and never made up for it. */
		public boolean isFeared(long gameTime) {
			return this.lastHurt >= 0 && gameTime - this.lastHurt < 24000 || this.trust <= -30;
		}

		public boolean isLiked() {
			return this.trust >= 30;
		}
	}

	private final Map<UUID, Entry> players = new HashMap<>();

	public @Nullable Entry get(UUID player) {
		return this.players.get(player);
	}

	public int trust(UUID player) {
		Entry entry = this.players.get(player);
		return entry == null ? 0 : entry.trust;
	}

	public boolean isFeared(UUID player, long gameTime) {
		Entry entry = this.players.get(player);
		return entry != null && entry.isFeared(gameTime);
	}

	public boolean isLiked(UUID player) {
		Entry entry = this.players.get(player);
		return entry != null && entry.isLiked();
	}

	public boolean knows(UUID player) {
		return this.players.containsKey(player);
	}

	private Entry entry(UUID player, long gameTime) {
		Entry entry = this.players.computeIfAbsent(player, id -> new Entry());
		entry.lastSeen = gameTime;
		if (this.players.size() > MAX_PLAYERS) {
			this.players.entrySet().stream().min(Comparator.comparingLong(e -> e.getValue().lastSeen))
					.map(Map.Entry::getKey).ifPresent(this.players::remove);
		}
		return entry;
	}

	/** Gave them food or a gift. */
	public void fed(UUID player, long gameTime) {
		Entry entry = this.entry(player, gameTime);
		entry.fed++;
		entry.trust = Math.min(100, entry.trust + 8);
	}

	/** Hurt them. A bigger hit costs more trust. */
	public void hurt(UUID player, float damage, long gameTime) {
		Entry entry = this.entry(player, gameTime);
		entry.hurt++;
		entry.damageTaken += damage;
		entry.lastHurt = gameTime;
		entry.trust = Math.max(-100, entry.trust - Math.max(5, Math.round(damage * 3)));
	}

	/** Fought next to them, defended them, tamed them. */
	public void helped(UUID player, int amount, long gameTime) {
		Entry entry = this.entry(player, gameTime);
		entry.helped++;
		entry.trust = Math.min(100, entry.trust + amount);
	}

	/** Saw them nearby. Returns true the first time ever (something new to be curious about). */
	public boolean seen(UUID player, long gameTime) {
		boolean first = !this.players.containsKey(player);
		this.entry(player, gameTime);
		return first;
	}

	public void save(CompoundTag tag) {
		ListTag list = new ListTag();
		for (Map.Entry<UUID, Entry> e : this.players.entrySet()) {
			CompoundTag t = new CompoundTag();
			t.store("Id", UUIDUtil.CODEC, e.getKey());
			Entry entry = e.getValue();
			t.putInt("Fed", entry.fed);
			t.putInt("Hurt", entry.hurt);
			t.putFloat("Damage", entry.damageTaken);
			t.putInt("Helped", entry.helped);
			t.putLong("LastHurt", entry.lastHurt);
			t.putLong("LastSeen", entry.lastSeen);
			t.putInt("Trust", entry.trust);
			list.add(t);
		}
		tag.put("Memory", list);
	}

	public void load(CompoundTag tag) {
		this.players.clear();
		List<Tag> list = new ArrayList<>(tag.getListOrEmpty("Memory"));
		for (Tag element : list) {
			if (!(element instanceof CompoundTag t)) {
				continue;
			}
			UUID id = t.read("Id", UUIDUtil.CODEC).orElse(null);
			if (id == null) {
				continue;
			}
			Entry entry = new Entry();
			entry.fed = t.getIntOr("Fed", 0);
			entry.hurt = t.getIntOr("Hurt", 0);
			entry.damageTaken = t.getFloatOr("Damage", 0.0F);
			entry.helped = t.getIntOr("Helped", 0);
			entry.lastHurt = t.getLongOr("LastHurt", -1);
			entry.lastSeen = t.getLongOr("LastSeen", 0);
			entry.trust = t.getIntOr("Trust", 0);
			this.players.put(id, entry);
		}
	}
}
