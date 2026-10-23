package com.itzbreadloaf.sound;

import com.itzbreadloaf.ItzBreadLoaf;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * The mod's sounds. ItzBreadLoaf's events play his recorded voice lines (sounds/breadloaf/*.ogg, several per event,
 * one picked at random);
 * ImLilyLol's voice plays sounds/imlilylol_voice.ogg. To change what an event plays later,
 * only change its "sounds" list in assets/itzbreadloaf/sounds.json. No code change needed.
 */
public final class ModSounds {
	public static final SoundEvent MEOW = register("itzbreadloaf.meow");
	public static final SoundEvent AMBIENT = register("itzbreadloaf.ambient");
	public static final SoundEvent HURT = register("itzbreadloaf.hurt");
	public static final SoundEvent DEATH = register("itzbreadloaf.death");
	public static final SoundEvent TAUNT = register("itzbreadloaf.taunt");
	public static final SoundEvent TAMED = register("itzbreadloaf.tamed");
	/** A big (critical) hit. */
	public static final SoundEvent CRIT = register("itzbreadloaf.crit");
	/** Starting a fight with a rival (FelixSyn). */
	public static final SoundEvent START_DUEL = register("itzbreadloaf.start_duel");
	/** Winning a fight against a player or rival. */
	public static final SoundEvent END_DUEL = register("itzbreadloaf.end_duel");
	/** Giving someone a gift (not used yet: ready for a gift feature). */
	public static final SoundEvent GIVES_GIFT = register("itzbreadloaf.gives_gift");
	/** Being fed / given steak. */
	public static final SoundEvent GIVEN_GIFT = register("itzbreadloaf.given_gift");
	/** Handing out a daily mission (not used yet: ready for a mission feature). */
	public static final SoundEvent DAILY_MISSION = register("itzbreadloaf.daily_mission");
	/** ImLilyLol's own voice (assets/itzbreadloaf/sounds/imlilylol_voice.ogg). Played quieter and higher-pitched. */
	public static final SoundEvent LILY_VOICE = register("imlilylol.voice");
	/** Madison's own voice (assets/itzbreadloaf/sounds/madison_voice.ogg). */
	public static final SoundEvent MADISON_VOICE = register("madison.voice");

	private ModSounds() {
	}

	private static SoundEvent register(String name) {
		Identifier id = ItzBreadLoaf.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void register() {
		// Loads the class so the static fields above run.
	}
}
