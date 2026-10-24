package com.itzbreadloaf.brain.ai;

import com.itzbreadloaf.character.CharacterKind;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * How well a character fights, as numbers. Every character has an Easy, Normal and Hard profile in
 * config/itzbreadloaf.json ("skillProfiles"), filled in with these defaults the first time; edit them there.
 * Which preset is used: "aiDifficulty" (or "aiDifficultyOverrides" per character); Harley follows
 * "harleyDifficulty" and Master BreadLoaf "bossDifficulty".
 *
 * Harley's crit chance and Master BreadLoaf's reaction, crits, misses and technique fails come from their own presets
 * (HarleyPreset, BossPreset) so their balance stays exactly as tuned.
 *
 * Fields are public and plain so Gson can read and write them.
 */
public final class SkillProfile {
	/** Reaction delay to a new threat, picked between these each time (milliseconds). */
	public int reactionMinMs;
	public int reactionMaxMs;
	/** How far off the aim wobbles (degrees) and how fast the view can turn (degrees per tick). */
	public float aimErrorDegrees;
	public float aimSpeedDegrees;
	/** Ticks a hotbar slot change takes. */
	public int hotbarSwitchTicks;
	/** Chance to go for a jump crit when the timing allows. */
	public float critChance;
	/** Chance to W-tap (re-sprint) or S-tap (step back) after a hit. */
	public float wTapChance;
	public float sTapChance;
	/** Chance to keep circling the target instead of standing still. */
	public float strafeChance;
	/** Chance to jump on the tick they're hit, to take less knockback. */
	public float jumpResetChance;
	/** 0-1: how well-timed shield blocks are (1 = only when the hit is coming, 0 = random). */
	public float shieldSkill;
	/** Chance a swing misses on purpose. */
	public float mistakeChance;
	/** Chance a mace technique (pearl catch, dive, chain) goes wrong. */
	public float techniqueFailChance;
	/** Below this share of health they may panic (erratic moves, early retreat). */
	public float panicHealth;
	/** 0-1: how far they chase and how fast they engage. */
	public float aggression;

	public SkillProfile() {
	}

	private SkillProfile(int reactionMinMs, int reactionMaxMs, float aimErrorDegrees, float aimSpeedDegrees, int hotbarSwitchTicks,
			float critChance, float wTapChance, float sTapChance, float strafeChance, float jumpResetChance, float shieldSkill,
			float mistakeChance, float techniqueFailChance, float panicHealth, float aggression) {
		this.reactionMinMs = reactionMinMs;
		this.reactionMaxMs = reactionMaxMs;
		this.aimErrorDegrees = aimErrorDegrees;
		this.aimSpeedDegrees = aimSpeedDegrees;
		this.hotbarSwitchTicks = hotbarSwitchTicks;
		this.critChance = critChance;
		this.wTapChance = wTapChance;
		this.sTapChance = sTapChance;
		this.strafeChance = strafeChance;
		this.jumpResetChance = jumpResetChance;
		this.shieldSkill = shieldSkill;
		this.mistakeChance = mistakeChance;
		this.techniqueFailChance = techniqueFailChance;
		this.panicHealth = panicHealth;
		this.aggression = aggression;
	}

	/** Reaction delay in ticks, picked at random between min and max. */
	public int reactionTicks(net.minecraft.util.RandomSource random) {
		int min = Math.max(0, this.reactionMinMs);
		int max = Math.max(min, this.reactionMaxMs);
		return Math.round((min + random.nextInt(max - min + 1)) / 50.0F);
	}

	/** The Normal profile for each character. */
	private static SkillProfile normal(CharacterKind kind) {
		return switch (kind) {
			// Average, aggressive, scrappy, sometimes reckless.
			case BREADLOAF -> new SkillProfile(200, 300, 4.0F, 30.0F, 4, 0.60F, 0.50F, 0.10F, 0.50F, 0.25F, 0.30F, 0.08F, 0.40F, 0.20F, 0.90F);
			// Shy, defensive and basic until angry.
			case LILY -> new SkillProfile(220, 300, 5.0F, 25.0F, 5, 0.30F, 0.20F, 0.30F, 0.40F, 0.15F, 0.50F, 0.12F, 0.50F, 0.40F, 0.30F);
			// Defender: good with a shield, slow to start a fight.
			case MADISON -> new SkillProfile(200, 300, 4.5F, 27.0F, 4, 0.35F, 0.30F, 0.20F, 0.40F, 0.20F, 0.70F, 0.10F, 0.50F, 0.30F, 0.40F);
			// Knows everything; the boss preset limits him (reaction, crits, misses and fails come from it).
			case MASTER -> new SkillProfile(300, 400, 5.0F, 28.0F, 4, 0.25F, 0.40F, 0.20F, 0.50F, 0.30F, 0.0F, 0.12F, 0.25F, 0.0F, 0.80F);
			// Great at mace movement, decent on the ground.
			case FELIXSYN -> new SkillProfile(170, 250, 3.0F, 35.0F, 3, 0.75F, 0.50F, 0.30F, 0.70F, 0.40F, 0.40F, 0.06F, 0.15F, 0.15F, 0.70F);
			// Clean and consistent duelist (crit chance comes from harleyDifficulty).
			case HARLEY -> new SkillProfile(150, 220, 2.0F, 40.0F, 2, 0.40F, 0.70F, 0.40F, 0.80F, 0.60F, 0.70F, 0.03F, 0.10F, 0.10F, 0.60F);
		};
	}

	/** Easy: slower, sloppier, fewer tricks. */
	private static SkillProfile easy(CharacterKind kind) {
		SkillProfile n = normal(kind);
		return new SkillProfile(Math.round(n.reactionMinMs * 1.25F), Math.round(n.reactionMaxMs * 1.25F), n.aimErrorDegrees * 1.5F,
				n.aimSpeedDegrees * 0.8F, n.hotbarSwitchTicks + 2, n.critChance * 0.7F, n.wTapChance * 0.7F, n.sTapChance * 0.7F,
				n.strafeChance * 0.7F, n.jumpResetChance * 0.6F, n.shieldSkill * 0.7F, Math.min(0.5F, n.mistakeChance * 1.6F),
				Math.min(0.9F, n.techniqueFailChance * 1.4F), Math.min(0.6F, n.panicHealth * 1.3F), n.aggression * 0.85F);
	}

	/** Hard: quicker, cleaner, more tricks (never perfect). */
	private static SkillProfile hard(CharacterKind kind) {
		SkillProfile n = normal(kind);
		return new SkillProfile(Math.round(n.reactionMinMs * 0.85F), Math.round(n.reactionMaxMs * 0.85F), n.aimErrorDegrees * 0.7F,
				n.aimSpeedDegrees * 1.2F, Math.max(1, n.hotbarSwitchTicks - 1), Math.min(0.95F, n.critChance * 1.25F),
				Math.min(0.95F, n.wTapChance * 1.25F), Math.min(0.95F, n.sTapChance * 1.25F), Math.min(0.95F, n.strafeChance * 1.2F),
				Math.min(0.9F, n.jumpResetChance * 1.25F), Math.min(0.95F, n.shieldSkill * 1.2F), n.mistakeChance * 0.6F,
				n.techniqueFailChance * 0.6F, n.panicHealth * 0.8F, Math.min(1.0F, n.aggression * 1.1F));
	}

	/** All defaults: character id -> "easy"/"normal"/"hard" -> profile. */
	public static Map<String, Map<String, SkillProfile>> defaults() {
		Map<String, Map<String, SkillProfile>> all = new LinkedHashMap<>();
		for (CharacterKind kind : CharacterKind.values()) {
			Map<String, SkillProfile> presets = new LinkedHashMap<>();
			presets.put("easy", easy(kind));
			presets.put("normal", normal(kind));
			presets.put("hard", hard(kind));
			all.put(kind.id, presets);
		}
		return all;
	}

	/** The built-in profile, for when the config's is missing. */
	public static SkillProfile fallback(CharacterKind kind, String preset) {
		return switch (preset) {
			case "easy" -> easy(kind);
			case "hard" -> hard(kind);
			default -> normal(kind);
		};
	}
}
