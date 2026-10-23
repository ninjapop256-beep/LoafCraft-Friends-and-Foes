package com.itzbreadloaf.character;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

/**
 * Types of each character. The first type of each character is the original. A character's type is chosen when
 * it is created and is part of its UUID, so it never changes and the client knows it too (for the name tag).
 */
public enum CharacterVariant {
	// ItzBreadLoaf
	BREADLOAF(CharacterKind.BREADLOAF, "itzbreadloaf", ""),
	KNIGHT(CharacterKind.BREADLOAF, "knight", "Knight"),
	ARCHER(CharacterKind.BREADLOAF, "archer", "Archer"),
	MINER(CharacterKind.BREADLOAF, "miner", "Miner"),
	PIRATE(CharacterKind.BREADLOAF, "pirate", "Pirate"),
	NETHER(CharacterKind.BREADLOAF, "nether", "Nether"),
	SWEATY(CharacterKind.BREADLOAF, "sweaty", "Sweaty"),
	CAPTAIN(CharacterKind.BREADLOAF, "captain", "Captain"),
	BANDIT(CharacterKind.BREADLOAF, "bandit", "Bandit"),
	VILLAGER(CharacterKind.BREADLOAF, "villager", "Village"),
	// ImLilyLol
	LILY(CharacterKind.LILY, "imlilylol", ""),
	BOOKWORM(CharacterKind.LILY, "bookworm", "Bookworm"),
	GARDENER(CharacterKind.LILY, "gardener", "Gardener"),
	WITCHY(CharacterKind.LILY, "witchy", "Witchy"),
	ARCHER_LILY(CharacterKind.LILY, "archer_lily", "Archer"),
	// Madison
	MADISON(CharacterKind.MADISON, "madison", ""),
	FARMER(CharacterKind.MADISON, "farmer", "Farmer"),
	BAKER(CharacterKind.MADISON, "baker", "Baker"),
	FISHER(CharacterKind.MADISON, "fisher", "Fisher"),
	BEEKEEPER(CharacterKind.MADISON, "beekeeper", "Beekeeper"),
	// Master BreadLoaf
	MASTER(CharacterKind.MASTER, "master_breadloaf", ""),
	// FelixSyn and Harley
	FELIXSYN(CharacterKind.FELIXSYN, "felixsyn", ""),
	HARLEY(CharacterKind.HARLEY, "harley", ""),
	// Added later (kept at the end so older characters keep their type number)
	FISHERMAN(CharacterKind.BREADLOAF, "fisherman", "Fisherman"),
	MERCHANT(CharacterKind.MADISON, "merchant", "Merchant"),
	/** FelixSyn out in the wild with his own home and rolled gear (the village drifter is FELIXSYN). */
	WILD_FELIX(CharacterKind.FELIXSYN, "wild_felixsyn", "Wild");

	public final CharacterKind kind;
	/** Command name (/loafcraft spawn knight). */
	public final String id;
	/** Word in front of the character's name ("Knight ItzBreadLoaf"), empty for the original. */
	public final String title;

	CharacterVariant(CharacterKind kind, String id, String title) {
		this.kind = kind;
		this.id = id;
		this.title = title;
	}

	/** Position among this character's types (0 = the original). Stored in the UUID. */
	public int index() {
		return variantsOf(this.kind).indexOf(this);
	}

	public boolean isBase() {
		return this.index() == 0;
	}

	public String displayName() {
		return this.title.isEmpty() ? this.kind.displayName : this.title + " " + this.kind.displayName;
	}

	public static List<CharacterVariant> variantsOf(CharacterKind kind) {
		List<CharacterVariant> list = new ArrayList<>();
		for (CharacterVariant variant : values()) {
			if (variant.kind == kind) {
				list.add(variant);
			}
		}
		return list;
	}

	public static CharacterVariant base(CharacterKind kind) {
		return variantsOf(kind).get(0);
	}

	public static CharacterVariant of(CharacterKind kind, int index) {
		List<CharacterVariant> list = variantsOf(kind);
		return index >= 0 && index < list.size() ? list.get(index) : list.get(0);
	}

	public static @Nullable CharacterVariant byId(String id) {
		String key = id.toLowerCase(Locale.ROOT);
		for (CharacterVariant variant : values()) {
			if (variant.id.equals(key) || variant.name().toLowerCase(Locale.ROOT).equals(key)) {
				return variant;
			}
		}
		CharacterKind kind = CharacterKind.byId(key);
		return kind == null ? null : base(kind);
	}
}
