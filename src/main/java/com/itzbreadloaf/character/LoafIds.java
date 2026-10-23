package com.itzbreadloaf.character;

import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Fixed, offline-style (version 3) UUIDs for every character instance, so the same character always comes back
 * with the same UUID and both server and client can tell who a player entity is (and what type) from its UUID:
 * <pre>
 *   4c6f6146-VVKK-3000-8000-4c4f41IIIIII
 *   "LoaF"   type+character  v3   tag   instance index
 * </pre>
 * Characters from before types existed have VV = 00, which is the original type.
 */
public final class LoafIds {
	private static final long MSB_TAG = 0x4C6F6146L;
	private static final long MSB_VERSION = 0x3000L;
	private static final long LSB_TAG = 0x80004C4F41000000L;
	private static final long LSB_MASK = 0xFFFFFFFFFF000000L;
	public static final int MAX_NAME_LENGTH = 16;

	private LoafIds() {
	}

	public static UUID uuid(CharacterVariant variant, int index) {
		long msb = (MSB_TAG << 32) | ((long) (variant.index() & 0xFF) << 24) | ((long) (variant.kind.ordinal() & 0xFF) << 16) | MSB_VERSION;
		long lsb = LSB_TAG | (index & 0xFFFFFFL);
		return new UUID(msb, lsb);
	}

	public static UUID uuid(CharacterKind kind, int index) {
		return uuid(CharacterVariant.base(kind), index);
	}

	public static boolean isCharacter(@Nullable UUID id) {
		if (id == null) {
			return false;
		}
		long msb = id.getMostSignificantBits();
		return (msb >>> 32) == MSB_TAG && (msb & 0xFFFFL) == MSB_VERSION
				&& (id.getLeastSignificantBits() & LSB_MASK) == LSB_TAG;
	}

	/** True for LoafCraft character players (never for real players or other entities). */
	public static boolean isCharacter(@Nullable Entity entity) {
		return entity instanceof Player && isCharacter(entity.getUUID());
	}

	/** A real (human) player: a player that is not a LoafCraft character. */
	public static boolean isRealPlayer(@Nullable Entity entity) {
		return entity instanceof Player && !isCharacter(entity.getUUID());
	}

	public static @Nullable CharacterKind kind(@Nullable UUID id) {
		if (!isCharacter(id)) {
			return null;
		}
		return CharacterKind.byOrdinal((int) ((id.getMostSignificantBits() >>> 16) & 0xFF));
	}

	public static @Nullable CharacterVariant variant(@Nullable UUID id) {
		CharacterKind kind = kind(id);
		if (kind == null) {
			return null;
		}
		return CharacterVariant.of(kind, (int) ((id.getMostSignificantBits() >>> 24) & 0xFF));
	}

	public static int index(UUID id) {
		return (int) (id.getLeastSignificantBits() & 0xFFFFFFL);
	}

	/** ItzBreadLoaf, ItzBreadLoaf_2, ... shortened if needed so the name fits Minecraft's 16-character limit. */
	public static String profileName(CharacterKind kind, int index) {
		if (index <= 1) {
			return kind.profileName;
		}
		String suffix = "_" + index;
		String base = kind.profileName;
		if (base.length() + suffix.length() > MAX_NAME_LENGTH) {
			base = base.substring(0, MAX_NAME_LENGTH - suffix.length());
		}
		return base + suffix;
	}
}
