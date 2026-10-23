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
	/** Chance (0-1) that Harley comes along with a wild FelixSyn (she's always with the village drifter). */
	public double wildFelixHarleyChance = 0.3;
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

	public enum HarleyPreset {
		EASY(20.0, 0.85, 0.11, 0.25F), NORMAL(26.0, 1.0, 0.115, 0.4F), HARD(32.0, 1.15, 0.12, 0.6F);

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

	public enum BossPreset {
		EASY(150, 0.7, 4.0F), NORMAL(200, 1.0, 6.0F), HARD(300, 1.3, 9.0F);

		public final double health;
		public final double damageMultiplier;
		/** Largest fall distance a mace smash counts (mace damage grows with the fall). */
		public final float maxSmashFall;

		BossPreset(double health, double damageMultiplier, float maxSmashFall) {
			this.health = health;
			this.damageMultiplier = damageMultiplier;
			this.maxSmashFall = maxSmashFall;
		}
	}

	public BossPreset bossPreset() {
		return switch (this.bossDifficulty == null ? "" : this.bossDifficulty.toLowerCase(java.util.Locale.ROOT)) {
			case "easy" -> BossPreset.EASY;
			case "hard" -> BossPreset.HARD;
			default -> BossPreset.NORMAL;
		};
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
		// Write it back so new options show up in old files.
		try (Writer writer = Files.newBufferedWriter(path)) {
			GSON.toJson(instance, writer);
		} catch (IOException e) {
			ItzBreadLoaf.LOGGER.warn("Could not write {}", path, e);
		}
	}
}
