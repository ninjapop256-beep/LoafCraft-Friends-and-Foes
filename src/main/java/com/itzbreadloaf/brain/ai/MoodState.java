package com.itzbreadloaf.brain.ai;

import net.minecraft.nbt.CompoundTag;

/**
 * The current mood and how strong it is (0-1). Events push a mood up; strength fades by itself, and when it runs
 * out the character drifts back to its everyday mood (happy in the day, tired at night, bored after a long time with
 * nothing going on). A stronger mood isn't replaced by a weaker push.
 */
public final class MoodState {
	/** Strength lost per second. A full-strength mood lasts a bit under 2 minutes without new events. */
	private static final float FADE_PER_SECOND = 0.01F;

	private Mood mood = Mood.HAPPY;
	private float strength = 0.3F;
	/** Set with /loafcraft mood: held until this game tick, whatever happens. */
	private long lockedUntil;

	public Mood mood() {
		return this.mood;
	}

	public float strength() {
		return this.strength;
	}

	/**
	 * Something happened that makes them feel this way. Returns true if the mood changed (to show it).
	 * Pushing the current mood again adds to its strength.
	 */
	public boolean push(Mood mood, float amount, long gameTime) {
		if (gameTime < this.lockedUntil) {
			return false;
		}
		if (mood == this.mood) {
			this.strength = Math.min(1.0F, this.strength + amount);
			return false;
		}
		if (amount < this.strength * 0.8F) {
			return false;
		}
		this.mood = mood;
		this.strength = Math.min(1.0F, amount);
		return true;
	}

	/** Forces a mood (the test command): it holds for {@code ticks}. */
	public void set(Mood mood, long gameTime, int ticks) {
		this.mood = mood;
		this.strength = 1.0F;
		this.lockedUntil = gameTime + ticks;
	}

	/** Once a second: fade, and settle into the everyday mood once the current one has worn off. Returns true if it changed. */
	public boolean tickSecond(Mood everyday, long gameTime) {
		if (gameTime < this.lockedUntil) {
			return false;
		}
		this.strength = Math.max(0.0F, this.strength - FADE_PER_SECOND);
		if (this.strength <= 0.05F && this.mood != everyday) {
			this.mood = everyday;
			this.strength = 0.3F;
			return true;
		}
		return false;
	}

	public void save(CompoundTag tag) {
		tag.putString("Mood", this.mood.id());
		tag.putFloat("MoodStrength", this.strength);
	}

	public void load(CompoundTag tag) {
		this.mood = Mood.byId(tag.getStringOr("Mood", "happy"));
		this.strength = tag.getFloatOr("MoodStrength", 0.3F);
	}
}
