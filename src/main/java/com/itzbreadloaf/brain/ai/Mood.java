package com.itzbreadloaf.brain.ai;

import java.util.Locale;

/**
 * How a character feels right now. Moods change speed, how soon they back off from a fight, what they do in their
 * free time, and what they say.
 */
public enum Mood {
	HAPPY(0.03, 0.0F),
	BORED(-0.03, 0.0F),
	TIRED(-0.10, 0.05F),
	SCARED(0.10, 0.15F),
	ANGRY(0.05, -0.10F),
	CURIOUS(0.0, 0.0F);

	/** Extra movement speed (a share of their normal speed). */
	public final double speedBonus;
	/** Added to the share of health below which they back off from a fight (scared: sooner, angry: later). */
	public final float retreatShift;

	Mood(double speedBonus, float retreatShift) {
		this.speedBonus = speedBonus;
		this.retreatShift = retreatShift;
	}

	public String id() {
		return this.name().toLowerCase(Locale.ROOT);
	}

	public static Mood byId(String id) {
		for (Mood mood : values()) {
			if (mood.id().equalsIgnoreCase(id)) {
				return mood;
			}
		}
		return HAPPY;
	}
}
