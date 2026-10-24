package com.itzbreadloaf.brain.ai;

/**
 * Moments a character says something about (AI overhaul part 6). Each character has its own lines for each; a line may
 * contain %s (a friend's name, or the item a player is holding).
 */
public enum Situation {
	/** A trusted player shows up again. */
	GREET_PLAYER,
	/** A friend character shows up again (%s = their name). */
	GREET_FRIEND,
	/** A fight starts. */
	FIGHT_START,
	/** A fight with the rival starts (ItzBreadLoaf and FelixSyn trash-talk). */
	RIVAL,
	/** Won a fight. */
	FIGHT_WON,
	/** Very low on health in a fight. */
	LOW_HEALTH,
	/** Jumping in to defend a friend. */
	DEFEND_FRIEND,
	/** A friend got hurt and they're scared for them. */
	SCARED_FOR_FRIEND,
	/** Catching their breath after a close fight. */
	REST,
	/** Stretching in the morning. */
	MORNING,
	/** Looking at the night sky. */
	NIGHT,
	/** Caught in the rain. */
	RAIN,
	/** Looking at what a player is holding (%s = the item). */
	ITEM,
	/** Watching a player build or mine. */
	WATCH
}
