package com.itzbreadloaf.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itzbreadloaf.ItzBreadLoaf;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Settings in config/itzbreadloaf.json (created on first launch). Edit the file and restart the game.
 */
public final class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ModConfig instance = new ModConfig();

	/** Short action-bar lines like "get over here!" and "oh... hi". */
	public boolean actionBarMessages = true;
	/** ItzBreadLoaf's random natural spawn weight (zombies are 95). Most of them live in houses and tents instead. */
	public int itzBreadLoafSpawnWeight = 5;
	/** Master BreadLoaf preset: "easy", "normal" or "hard" (health, damage, how hard his mace smashes can hit). */
	public String bossDifficulty = "normal";
	/** Most characters (Carpet fake players) spawned at the same time. The closest ones to real players win. */
	public int maxActiveCharacters = 20;
	/** Characters exist only while a real player is this close (blocks, about 4 chunks). Farther away they are saved and despawned. */
	public double activationRange = 64.0;

	/** Harley preset: "easy", "normal" or "hard" (health, damage, speed, how often she crits, how fast she pearls). */
	public String harleyDifficulty = "normal";
	/** Chance (0-1) that a village gets FelixSyn (and Harley) the first time a player visits it. */
	public double felixVillageChance = 0.4;
	/** Wild FelixSyn's random natural spawn weight (like itzBreadLoafSpawnWeight). Most live in their own homes. */
	public int wildFelixSpawnWeight = 2;
	/** Chance (0-1) that Harley comes along with a wild FelixSyn (20%). Renamed so old config files lose the old 30%. */
	public double wildFelixWithHarleyChance = 0.2;
	/** Chance a FelixSyn in a village or structure has Harley with him (spawn eggs and /loafcraft spawn always do). */
	public double felixHarleyChance = 0.2;
	/** Harley's best friend: the one real player she adores and gives gifts to (matched by Minecraft name, any case). */
	public String harleyBestFriend = "FelixSyn";
	/** Share (0-1) of ItzBreadLoafs and FelixSyns wearing their main skin; the rest wear a random old skin (<name>_alt<N>.png). */
	public double mainSkinChance = 0.5;
	/** Seconds between Harley's gifts to her best friend (a random time between min and max). */
	public int harleyGiftMinSeconds = 180;
	public int harleyGiftMaxSeconds = 300;

	// ---------------------------------------------------------------- FelixSyn's voice
	/** Volume of FelixSyn's recorded voice lines (1.0 = normal). */
	public double felixVoiceVolume = 1.0;
	/**
	 * Seconds between his idle lines while he wanders (a random time between min and max). New names, so config files
	 * saved with the old, chattier 20-40 second setting don't keep it.
	 */
	public int felixIdleLineMinSeconds = 90;
	public int felixIdleLineMaxSeconds = 180;
	/** He never says two lines closer together than this many seconds (fights, smashes, getting hit...). */
	public int felixVoiceGapSeconds = 15;
	/** Chance that getting hit plays one of his "ow" lines (when he hasn't just spoken); otherwise the normal hurt sound. */
	public double felixHurtVoiceChance = 0.25;

	// ---------------------------------------------------------------- taming and health
	/** Taming chance: 1 in this many steaks for ItzBreadLoaf. */
	public int breadTameChance = 3;
	/** Taming chance: 1 in this many golden apples for Harley (her best friend always tames her on the first). */
	public int harleyTameChance = 5;
	/** Taming chance: 1 in this many cooked porkchops for FelixSyn (wild ones too). */
	public int felixTameChance = 4;
	/** Multiplies every ItzBreadLoaf type's health (Knight 30, Captain 40, normal 20...). */
	public double breadHealthMultiplier = 1.0;
	public double lilyHealth = 30.0;
	public double madisonHealth = 26.0;
	public double felixHealth = 30.0;

	// ---------------------------------------------------------------- combat
	/** Health fraction (0-1) where each character backs off to block, heal and eat. 0 = never. */
	public double breadRetreatHealth = 0.3;
	public double cousinsRetreatHealth = 0.4;
	public double felixRetreatHealth = 0.3;
	public double harleyRetreatHealth = 0.2;

	// ---------------------------------------------------------------- personality
	/** Random idle lines when a player is close, and characters talking to each other. */
	public boolean idleChatter = true;
	public int chatterMinSeconds = 60;
	public int chatterMaxSeconds = 180;

	// ---------------------------------------------------------------- performance
	/** Most path searches all characters together may run in one tick (the rest wait a tick or two). */
	public int maxPathSearchesPerTick = 6;

	// ---------------------------------------------------------------- loot rarity (see LOOT.md)
	/**
	 * Odds of each tier per tier roll, out of 1000. One row per loot level (1 basic, 2 standard, 3 hard,
	 * 4 Master BreadLoaf), columns Common, Uncommon, Rare, Epic, Legendary.
	 */
	public int[][] lootTierWeights = {{700, 240, 50, 9, 1}, {600, 250, 100, 40, 10}, {480, 280, 160, 65, 15}, {350, 280, 220, 110, 40}};
	/** Tier rolls per chest or character drop (min, max), one row per loot level. */
	public int[][] lootTierRolls = {{1, 2}, {2, 3}, {2, 4}, {3, 5}};
	/** Loot level (1-4) of each table, by path ("chests/house", "characters/felixsyn"...). Missing ones use the defaults. */
	public java.util.Map<String, Integer> lootLevels = new java.util.LinkedHashMap<>(com.itzbreadloaf.loot.LootLevels.DEFAULTS);
	/** Extra tier rolls per level of Looting when killing a character. */
	public double lootingBonusRolls = 0.5;
	/** Chance for each armor piece a character wears to drop (normal mobs: 0.085). */
	public double characterArmorDropChance = 0.085;
	/** Chance a character drops its weapon. */
	public double characterWeaponDropChance = 0.15;
	/**
	 * Maces are super rare. They're in no loot tier; each chest gets one separate mace roll with this chance, by loot
	 * level 1-4 (0.1%, 0.3%, 0.5%, 0.8%). Character drop tables never roll one.
	 */
	public double[] maceChestChance = {0.001, 0.003, 0.005, 0.008};
	/** Chance a character carrying a mace (FelixSyn, Harley, Master BreadLoaf, wild or tamed) drops it when it dies. */
	public double maceDropChance = 0.008;

	public enum HarleyPreset {
		// Crit chance is per swing since the AI overhaul (it used to be rolled every tick of the ~2-tick window, which
		// made it about 1-(1-c)^2): these keep her crit rate where it was.
		EASY(20.0, 0.85, 0.11, 0.44F), NORMAL(26.0, 1.0, 0.115, 0.64F), HARD(32.0, 1.15, 0.12, 0.84F);

		public final double health;
		public final double damageMultiplier;
		/** Movement speed attribute (players walk at 0.1). */
		public final double speed;
		/** How often she jumps for a crit when the timing allows. */
		public final float critChance;

		HarleyPreset(double health, double damageMultiplier, double speed, float critChance) {
			this.health = health;
			this.damageMultiplier = damageMultiplier;
			this.speed = speed;
			this.critChance = critChance;
		}
	}

	public HarleyPreset harleyPreset() {
		return switch (this.harleyDifficulty == null ? "" : this.harleyDifficulty.toLowerCase(java.util.Locale.ROOT)) {
			case "easy" -> HarleyPreset.EASY;
			case "hard" -> HarleyPreset.HARD;
			default -> HarleyPreset.NORMAL;
		};
	}

	/**
	 * Master BreadLoaf's difficulty (the big nerf: an average player in full diamond can beat Normal). Every preset keeps
	 * the 16-damage cap per hit ({@link #BOSS_MAX_HIT_DAMAGE}) and the 1-second warning before every smash and dive.
	 */
	public enum BossPreset {
		// Tuned with tools/boss_fight_sim.py (1.21.11 damage, armor and Protection rules): on Normal an average player in
		// plain full diamond with a sword, shield and 8 golden apples wins about 70% of fights.
		//   health, damage, prot, sharp, strength, smashCd, techCd, fail,  react ticks, whiff, fall, phase2, crit
		EASY(70, 0.40, 1, 0, 0, 300, 600, 0.35F, 7, 9, 0.18F, 3.0F, 0.05, 0.15F),
		NORMAL(100, 0.55, 2, 0, 0, 240, 400, 0.25F, 6, 8, 0.12F, 3.0F, 0.08, 0.25F),
		HARD(120, 0.50, 3, 0, 1, 240, 400, 0.15F, 5, 7, 0.08F, 4.0F, 0.12, 0.30F);

		public final double health;
		public final double damageMultiplier;
		/** Protection level on his diamond armor. */
		public final int protection;
		/** Sharpness level on his diamond sword (0 = none). */
		public final int sharpness;
		/** Strength potion level he drinks at the start (0 = none). */
		public final int strength;
		/** Ticks between mace smashes (at least 160 = 8 seconds). */
		public final int smashCooldown;
		/** Ticks between elytra dives and pearl catches (at least 400 = 20 seconds). */
		public final int techniqueCooldown;
		/** Chance he fails a dive or pearl catch on purpose. */
		public final float techniqueFailChance;
		/** Reaction delay to a new target, in ticks (1 tick = 50 ms). */
		public final int reactionMin;
		public final int reactionMax;
		/** Chance a sword swing misses on purpose. */
		public final float whiffChance;
		/** Largest fall distance a mace smash counts (mace damage grows with the fall). */
		public final float maxSmashFall;
		/** Extra speed in phase 2 (under half health). */
		public final double phase2Speed;
		/** How often he jumps for a sword crit when the timing allows. */
		public final float critChance;

		BossPreset(double health, double damageMultiplier, int protection, int sharpness, int strength, int smashCooldown,
				int techniqueCooldown, float techniqueFailChance, int reactionMin, int reactionMax, float whiffChance,
				float maxSmashFall, double phase2Speed, float critChance) {
			this.health = health;
			this.damageMultiplier = damageMultiplier;
			this.protection = protection;
			this.sharpness = sharpness;
			this.strength = strength;
			this.smashCooldown = Math.max(160, smashCooldown);
			this.techniqueCooldown = Math.max(400, techniqueCooldown);
			this.techniqueFailChance = techniqueFailChance;
			this.reactionMin = reactionMin;
			this.reactionMax = reactionMax;
			this.whiffChance = whiffChance;
			this.maxSmashFall = maxSmashFall;
			this.phase2Speed = phase2Speed;
			this.critChance = critChance;
		}
	}

	/** No single hit from Master BreadLoaf deals more than this (8 hearts), whatever the preset or fall height. */
	public static final float BOSS_MAX_HIT_DAMAGE = 16.0F;

	public BossPreset bossPreset() {
		return switch (this.bossDifficulty == null ? "" : this.bossDifficulty.toLowerCase(java.util.Locale.ROOT)) {
			case "easy" -> BossPreset.EASY;
			case "hard" -> BossPreset.HARD;
			default -> BossPreset.NORMAL;
		};
	}

	// ---------------------------------------------------------------- AI overhaul (part 3)

	/** Fighting skill preset for every character: "easy", "normal" or "hard". Harley uses harleyDifficulty, Master BreadLoaf bossDifficulty. */
	public String aiDifficulty = "normal";
	/** Per-character preset, overriding aiDifficulty: e.g. {"felixsyn": "hard"}. Keys: itzbreadloaf, imlilylol, madison, master_breadloaf, felixsyn, harley. */
	public java.util.Map<String, String> aiDifficultyOverrides = new java.util.LinkedHashMap<>();
	/** The skill numbers for each character and preset (filled in with the defaults; edit freely). */
	public java.util.Map<String, java.util.Map<String, com.itzbreadloaf.brain.ai.SkillProfile>> skillProfiles = new java.util.LinkedHashMap<>();
	/** Play style per character, or "auto" to pick from who they are (ItzBreadLoaf's types differ). E.g. {"itzbreadloaf": "turtle"}. */
	public java.util.Map<String, String> playStyleOverrides = new java.util.LinkedHashMap<>();
	/** Moods (happy, bored, tired, scared, angry, curious) that change how characters act. */
	public boolean moods = true;
	/** Daily routines: wake up, work, see friends, free time, head home, sleep. */
	public boolean routines = true;
	/** Characters within this many blocks of a real player think every tick (full AI). */
	public double aiFullDetailRange = 32.0;
	/** Out to this range they think every 4th tick (simple AI); beyond it they stand still until a player comes closer. */
	public double aiSimpleDetailRange = 56.0;
	/** Personality lines: greetings, mood lines, fight and rivalry trash-talk, morning and rain comments (actionBarMessages turns off every line). */
	public boolean personalityLines = true;

	/** The preset name used for a character. */
	public String aiPresetFor(com.itzbreadloaf.character.CharacterKind kind) {
		String preset = this.aiDifficultyOverrides == null ? null : this.aiDifficultyOverrides.get(kind.id);
		if (preset == null || preset.isBlank()) {
			preset = switch (kind) {
				case HARLEY -> this.harleyDifficulty;
				case MASTER -> this.bossDifficulty;
				default -> this.aiDifficulty;
			};
		}
		preset = preset == null ? "normal" : preset.toLowerCase(java.util.Locale.ROOT);
		return preset.equals("easy") || preset.equals("hard") ? preset : "normal";
	}

	/** The skill profile a character fights with right now. */
	public com.itzbreadloaf.brain.ai.SkillProfile skillFor(com.itzbreadloaf.character.CharacterKind kind) {
		String preset = this.aiPresetFor(kind);
		var byPreset = this.skillProfiles == null ? null : this.skillProfiles.get(kind.id);
		var profile = byPreset == null ? null : byPreset.get(preset);
		return profile != null ? profile : com.itzbreadloaf.brain.ai.SkillProfile.fallback(kind, preset);
	}

	/** Fills in any missing skill profiles with the defaults (so they show up in the file). */
	private void fillDefaults() {
		if (this.skillProfiles == null) {
			this.skillProfiles = new java.util.LinkedHashMap<>();
		}
		if (this.aiDifficultyOverrides == null) {
			this.aiDifficultyOverrides = new java.util.LinkedHashMap<>();
		}
		if (this.playStyleOverrides == null) {
			this.playStyleOverrides = new java.util.LinkedHashMap<>();
		}
		com.itzbreadloaf.brain.ai.SkillProfile.defaults().forEach((kind, presets) -> {
			var existing = this.skillProfiles.computeIfAbsent(kind, k -> new java.util.LinkedHashMap<>());
			presets.forEach(existing::putIfAbsent);
		});
		for (com.itzbreadloaf.character.CharacterKind kind : com.itzbreadloaf.character.CharacterKind.values()) {
			this.playStyleOverrides.putIfAbsent(kind.id, "auto");
		}
	}

	public static ModConfig get() {
		return instance;
	}

	public static void load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(ItzBreadLoaf.MOD_ID + ".json");
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (IOException | RuntimeException e) {
				ItzBreadLoaf.LOGGER.warn("Could not read {}, using defaults", path, e);
			}
		}
		instance.fillDefaults();
		// Write it back so new options show up in old files.
		try (Writer writer = Files.newBufferedWriter(path)) {
			GSON.toJson(instance, writer);
		} catch (IOException e) {
			ItzBreadLoaf.LOGGER.warn("Could not write {}", path, e);
		}
	}
}
