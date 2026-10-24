package com.itzbreadloaf.brain.ai;

import java.util.Locale;

/**
 * A character's day, by Minecraft time (0 = sunrise, 6000 = noon, 12000 = sunset, 18000 = midnight):
 *
 * <pre>
 *   23500-1000  wake up      stretch, look around, eat if hungry
 *    1000-6000  work         their job or hobby (bake, train, read, garden, farm, mace practice, spar)
 *    6000-7000  friends      lunch with a friend: walk over and hang out
 *    7000-10000 work         more work (or free time when bored)
 *   10000-11500 free time    hobbies, wandering, chatting
 *   11500-12500 head home    back home, near the bed
 *   12500-23500 sleep        bed (the existing night behavior)
 * </pre>
 *
 * Moods bend it: tired goes home and to bed an hour early and gets up late, bored skips work for free time or
 * friends, happy and curious visit friends more. Each hour's choice is fixed per character and day, so they don't
 * flip back and forth.
 */
public final class Routine {
	public enum Activity {
		WAKE_UP, WORK, FRIENDS, FREE, HEAD_HOME, SLEEP;

		public String id() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

	private Routine() {
	}

	public static Activity at(long dayTime, Mood mood, long seed) {
		long day = Math.floorDiv(dayTime, 24000L);
		int time = (int) Math.floorMod(dayTime, 24000L);
		int hour = time / 1000;
		// A stable "dice roll" for this character, day and hour.
		long mix = seed * 31L + day * 1009L + hour * 7919L;
		mix ^= mix >>> 17;
		mix *= 0x9E3779B97F4A7C15L;
		float roll = (float) ((mix >>> 40) & 0xFFFF) / 65536.0F;

		boolean tired = mood == Mood.TIRED;
		if (time >= (tired ? 11500 : 12500) && time < 23500) {
			return Activity.SLEEP;
		}
		if (time >= 23500 || time < (tired ? 2000 : 1000)) {
			return Activity.WAKE_UP;
		}
		if (time >= (tired ? 10500 : 11500)) {
			return Activity.HEAD_HOME;
		}
		if (time >= 10000) {
			return mood == Mood.HAPPY && roll < 0.4F ? Activity.FRIENDS : Activity.FREE;
		}
		if (time >= 6000 && time < 7000) {
			return mood == Mood.SCARED ? Activity.FREE : Activity.FRIENDS;
		}
		// Work hours.
		if (mood == Mood.BORED) {
			return roll < 0.35F ? Activity.FRIENDS : roll < 0.7F ? Activity.FREE : Activity.WORK;
		}
		if ((mood == Mood.HAPPY || mood == Mood.CURIOUS) && roll < 0.15F) {
			return Activity.FRIENDS;
		}
		return roll < 0.1F ? Activity.FREE : Activity.WORK;
	}
}
