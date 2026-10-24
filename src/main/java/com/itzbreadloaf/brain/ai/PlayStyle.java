package com.itzbreadloaf.brain.ai;

import com.itzbreadloaf.character.CharacterKind;
import com.itzbreadloaf.character.CharacterVariant;
import java.util.Locale;

/**
 * How a character likes to fight. Picked from who they are (ItzBreadLoaf's types each lean their own way) unless
 * "playStyleOverrides" in the config says otherwise. Part 4 (combat) reads these numbers.
 */
public enum PlayStyle {
	/** ItzBreadLoaf: sprint in, W-tap, jump crits, rarely blocks, chases too far. */
	BRAWLER(2.6, 0.15F, 1.4F, 0.9F, false),
	/** Knight: shield up, steady, waits for openings. */
	TURTLE(2.8, 0.70F, 0.9F, 0.5F, false),
	/** Pirate: axe first to break shields, then swings hard. */
	SHIELD_BREAKER(2.7, 0.20F, 1.2F, 0.8F, false),
	/** Archer types: keep distance and shoot, back off when rushed. */
	KITER(9.0, 0.25F, 0.8F, 0.4F, true),
	/** Sweaty: combos (W-tap, strafe, jump resets), tight spacing. */
	COMBO(2.4, 0.30F, 1.2F, 0.85F, false),
	/** Bandit: hit, back off, come back from another side. */
	HIT_AND_RUN(3.2, 0.10F, 1.0F, 0.7F, false),
	/** Captain: steady front-liner, blocks and trades evenly. */
	FRONTLINER(2.7, 0.45F, 1.0F, 0.7F, false),
	/** FelixSyn: fights from the air with wind charges, pearls and the mace. */
	AERIALIST(3.5, 0.20F, 1.1F, 0.75F, false),
	/** Harley: clean duelist, perfect spacing and timing. */
	DUELIST(2.6, 0.45F, 1.0F, 0.7F, false),
	/** Lily: keeps her distance, bow and potions, runs when scared. */
	SUPPORT(7.0, 0.35F, 0.6F, 0.3F, true),
	/** Madison: shield-heavy, stands between danger and her friends and animals. */
	DEFENDER(2.6, 0.65F, 0.7F, 0.4F, false),
	/** Master BreadLoaf: sword pressure between big warned moves. */
	SHOWMAN(2.8, 0.0F, 1.2F, 0.8F, false);

	/** Distance they like to fight at (blocks). */
	public final double preferredRange;
	/** How much they lean on the shield (0-1). */
	public final float shieldBias;
	/** How far they chase, as a multiple of normal. */
	public final float chaseMultiplier;
	/** How eager they are to start and press a fight (0-1). */
	public final float pressure;
	public final boolean ranged;

	PlayStyle(double preferredRange, float shieldBias, float chaseMultiplier, float pressure, boolean ranged) {
		this.preferredRange = preferredRange;
		this.shieldBias = shieldBias;
		this.chaseMultiplier = chaseMultiplier;
		this.pressure = pressure;
		this.ranged = ranged;
	}

	public String id() {
		return this.name().toLowerCase(Locale.ROOT);
	}

	public static PlayStyle forCharacter(CharacterKind kind, CharacterVariant variant) {
		return switch (kind) {
			case BREADLOAF -> switch (variant) {
				case KNIGHT -> TURTLE;
				case PIRATE -> SHIELD_BREAKER;
				case ARCHER -> KITER;
				case SWEATY -> COMBO;
				case BANDIT -> HIT_AND_RUN;
				case CAPTAIN -> FRONTLINER;
				default -> BRAWLER;
			};
			case LILY -> variant == CharacterVariant.ARCHER_LILY ? KITER : SUPPORT;
			case MADISON -> DEFENDER;
			case MASTER -> SHOWMAN;
			case FELIXSYN -> AERIALIST;
			case HARLEY -> DUELIST;
		};
	}

	/** A style by id, or null if unknown (or "auto"). */
	public static PlayStyle byId(String id) {
		for (PlayStyle style : values()) {
			if (style.id().equalsIgnoreCase(id)) {
				return style;
			}
		}
		return null;
	}
}
