package com.itzbreadloaf.character;

import com.itzbreadloaf.ItzBreadLoaf;
import java.util.Locale;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * The four LoafCraft characters. Each one is a Carpet fake player with a fixed name, skin and arm width.
 */
public enum CharacterKind {
	BREADLOAF("itzbreadloaf", "ItzBreadLoaf", "ItzBreadLoaf", true, "itzbreadloaf"),
	LILY("imlilylol", "ImLilyLol", "ImLilyLol", true, "imlilylol"),
	MADISON("madison", "Madison", "Madison", false, "madison"),
	MASTER("master_breadloaf", "MasterBreadLoaf", "Master BreadLoaf", true, ""),
	/** Homeless mace fighter who drifts between villages; ItzBreadLoaf's rival. */
	FELIXSYN("felixsyn", "FelixSyn", "FelixSyn", false, ""),
	/** FelixSyn's duo partner, the best fighter outside Master BreadLoaf. Tamable with golden apples. */
	HARLEY("harley", "Harley", "Harley", true, "");

	/** Command and save name, also the skin file name in assets/itzbreadloaf/textures/entity/. */
	public final String id;
	/** Player profile name (at most 16 characters, numbered for extra copies: ItzBreadLoaf_2). */
	public final String profileName;
	/** Clean name shown above their head, in chat and in messages. */
	public final String displayName;
	/** Alex-style 3px arms (true) or classic 4px arms (false). */
	public final boolean slim;
	/** Criterion name in the "Squad Goals" advancement. */
	public final String friendKey;

	CharacterKind(String id, String profileName, String displayName, boolean slim, String friendKey) {
		this.id = id;
		this.profileName = profileName;
		this.displayName = displayName;
		this.slim = slim;
		this.friendKey = friendKey;
	}

	public Identifier skinTexture() {
		return ItzBreadLoaf.id("textures/entity/" + this.id + ".png");
	}

	/** Not used for anything since friendship hearts were removed; kept for older code paths. */
	public boolean canBeFriends() {
		return this != MASTER;
	}

	/** Lily and Madison: never attack first. */
	public boolean isNeutral() {
		return this == LILY || this == MADISON;
	}

	public static @Nullable CharacterKind byId(String id) {
		String key = id.toLowerCase(Locale.ROOT);
		for (CharacterKind kind : values()) {
			if (kind.id.equals(key) || kind.name().toLowerCase(Locale.ROOT).equals(key)
					|| kind.profileName.toLowerCase(Locale.ROOT).equals(key)) {
				return kind;
			}
		}
		return null;
	}

	public static @Nullable CharacterKind byOrdinal(int ordinal) {
		CharacterKind[] kinds = values();
		return ordinal >= 0 && ordinal < kinds.length ? kinds[ordinal] : null;
	}
}
