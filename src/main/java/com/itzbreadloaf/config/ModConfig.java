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
	/** Harley's best friend: the one real player she adores and gives gifts to (matched by Minecraft name, any case). */
	public String harleyBestFriend = "FelixSyn";
	/** Share (0-1) of ItzBreadLoafs and FelixSyns wearing their main skin; the rest wear a random old skin (<name>_alt<N>.png). */
	public double mainSkinChance = 0.5;
	/** Seconds between Harley's gifts to her best friend (a random time between min and max). */
	public int harleyGiftMinSeconds = 180;
	public int harleyGiftMaxSeconds = 300;

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

	public enum HarleyPreset {
		EASY(30.0, 0.8, 0.115, 0.5F, 200), NORMAL(40.0, 1.0, 0.125, 0.85F, 120), HARD(50.0, 1.25, 0.135, 1.0F, 80);

		public final double health;
		public final double damageMultiplier;
		/** Movement speed attribute (players walk at 0.1). */
		public final double speed;
		/** How often she jumps for a crit when the timing allows. */
		public final float critChance;
		/** Ticks between ender pearls. */
		public final int pearlCooldown;

		HarleyPreset(double health, double damageMultiplier, double speed, float critChance, int pearlCooldown) {
			this.health = health;
			this.damageMultiplier = damageMultiplier;
			this.speed = speed;
			this.critChance = critChance;
			this.pearlCooldown = pearlCooldown;
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
