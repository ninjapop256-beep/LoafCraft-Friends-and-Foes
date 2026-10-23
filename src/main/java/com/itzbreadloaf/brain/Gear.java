package com.itzbreadloaf.brain;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.biome.Biome;

/**
 * ItzBreadLoaf's random weapon (one weapon type, then a tier) and biome-themed armor, written straight into his
 * player data: the weapon in hotbar slot 1, a shield in the offhand, armor in the armor slots, arrows for bows
 * and crossbows. Also equips better armor and shields he picks up.
 */
public final class Gear {
	/** Chance for each armor slot to get a piece. */
	public static final float ARMOR_PIECE_CHANCE = 0.5F;
	/** Chance of a full diamond or netherite set, anywhere. */
	public static final float FULL_SET_CHANCE = 0.03F;
	/** Chance per armor piece of a biome-fitting trim. */
	public static final float TRIM_CHANCE = 0.15F;
	/** Chance for melee users to also carry a shield in the offhand. */
	public static final float SHIELD_CHANCE = 0.4F;
	/** Chance for each item to get a random enchantment. */
	public static final float ENCHANT_CHANCE = 0.10F;
	/** Chance for an ocean-themed helmet to be a turtle shell instead of leather. */
	private static final float TURTLE_HELMET_CHANCE = 0.2F;

	private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	// ------------------------------------------------------------ weapons

	/** Weapon type weights (percent). */
	private enum WeaponType {
		SWORD(35), AXE(20), SPEAR(10), BOW(15), CROSSBOW(10), MACE(3), TRIDENT(2), FISTS(5);

		final int weight;

		WeaponType(int weight) {
			this.weight = weight;
		}
	}

	// Tier weights: wooden, stone, iron, golden, diamond, netherite.
	private static final int[] TIER_WEIGHTS = {25, 25, 25, 10, 12, 3};
	private static final Item[] SWORDS = {Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD,
			Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD};
	private static final Item[] AXES = {Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE,
			Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE};
	private static final Item[] SPEARS = {Items.WOODEN_SPEAR, Items.STONE_SPEAR, Items.IRON_SPEAR,
			Items.GOLDEN_SPEAR, Items.DIAMOND_SPEAR, Items.NETHERITE_SPEAR};

	// ------------------------------------------------------------ armor themes

	private static final int SAND = 0xD8C38A;
	private static final int WHITE = 0xF2F2F2;
	private static final int GREEN = 0x4C7A34;
	private static final int BROWN = 0x6B4A2B;
	private static final int BLUE = 0x3B5DC9;

	private enum Material { LEATHER, CHAINMAIL, IRON, GOLDEN, DIAMOND, NETHERITE, TURTLE }

	private record Pick(Material material, int weight) {
	}

	private enum Theme {
		DESERT(SAND, new Pick[]{new Pick(Material.GOLDEN, 40), new Pick(Material.LEATHER, 60)},
				new ResourceKey[]{TrimPatterns.DUNE}, new ResourceKey[]{TrimMaterials.GOLD, TrimMaterials.COPPER, TrimMaterials.REDSTONE}),
		SNOWY(WHITE, new Pick[]{new Pick(Material.LEATHER, 55), new Pick(Material.CHAINMAIL, 45)},
				new ResourceKey[]{TrimPatterns.WARD, TrimPatterns.SPIRE}, new ResourceKey[]{TrimMaterials.QUARTZ, TrimMaterials.DIAMOND, TrimMaterials.IRON}),
		JUNGLE(GREEN, new Pick[]{new Pick(Material.LEATHER, 100)},
				new ResourceKey[]{TrimPatterns.WILD}, new ResourceKey[]{TrimMaterials.EMERALD, TrimMaterials.COPPER}),
		TAIGA(BROWN, new Pick[]{new Pick(Material.LEATHER, 60), new Pick(Material.IRON, 40)},
				new ResourceKey[]{TrimPatterns.VEX, TrimPatterns.SENTRY}, new ResourceKey[]{TrimMaterials.IRON, TrimMaterials.REDSTONE}),
		PLAINS(BROWN, new Pick[]{new Pick(Material.IRON, 50), new Pick(Material.CHAINMAIL, 50)},
				new ResourceKey[]{TrimPatterns.SENTRY, TrimPatterns.WAYFINDER}, new ResourceKey[]{TrimMaterials.IRON, TrimMaterials.COPPER, TrimMaterials.LAPIS}),
		MOUNTAIN(BROWN, new Pick[]{new Pick(Material.IRON, 85), new Pick(Material.DIAMOND, 15)},
				new ResourceKey[]{TrimPatterns.SHAPER, TrimPatterns.RAISER, TrimPatterns.HOST}, new ResourceKey[]{TrimMaterials.AMETHYST, TrimMaterials.DIAMOND, TrimMaterials.IRON}),
		OCEAN(BLUE, new Pick[]{new Pick(Material.LEATHER, 100)},
				new ResourceKey[]{TrimPatterns.TIDE, TrimPatterns.COAST}, new ResourceKey[]{TrimMaterials.LAPIS, TrimMaterials.DIAMOND, TrimMaterials.COPPER});

		final int leatherColor;
		final Pick[] picks;
		final ResourceKey<TrimPattern>[] patterns;
		final ResourceKey<TrimMaterial>[] materials;

		@SuppressWarnings("unchecked")
		Theme(int leatherColor, Pick[] picks, ResourceKey<?>[] patterns, ResourceKey<?>[] materials) {
			this.leatherColor = leatherColor;
			this.picks = picks;
			this.patterns = (ResourceKey<TrimPattern>[]) patterns;
			this.materials = (ResourceKey<TrimMaterial>[]) materials;
		}
	}

	private Gear() {
	}

	/** ItzBreadLoaf's starting kit: one rolled weapon (plus arrows for bows), maybe a shield, biome armor. */
	public static void rollBreadLoaf(Player player, ServerLevel level, RandomSource random, DifficultyInstance difficulty) {
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			player.setItemSlot(slot, ItemStack.EMPTY);
		}
		player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
		Holder<Biome> biome = level.getBiome(player.blockPosition());

		WeaponType weapon = rollWeaponType(random, biome);
		ItemStack weaponStack = switch (weapon) {
			case SWORD -> new ItemStack(SWORDS[pickIndex(random, TIER_WEIGHTS)]);
			case AXE -> new ItemStack(AXES[pickIndex(random, TIER_WEIGHTS)]);
			case SPEAR -> new ItemStack(SPEARS[pickIndex(random, TIER_WEIGHTS)]);
			case BOW -> new ItemStack(Items.BOW);
			case CROSSBOW -> new ItemStack(Items.CROSSBOW);
			case MACE -> new ItemStack(Items.MACE);
			case TRIDENT -> new ItemStack(Items.TRIDENT);
			case FISTS -> ItemStack.EMPTY;
		};
		player.getInventory().setItem(Combat.MELEE_SLOT, weaponStack);
		player.getInventory().setSelectedSlot(Combat.MELEE_SLOT);
		if (weapon == WeaponType.BOW || weapon == WeaponType.CROSSBOW) {
			player.getInventory().setItem(Inv.HOTBAR, new ItemStack(Items.ARROW, 32));
		}

		boolean meleeOnly = weapon == WeaponType.SWORD || weapon == WeaponType.AXE || weapon == WeaponType.MACE
				|| weapon == WeaponType.FISTS;
		if (meleeOnly && random.nextFloat() < SHIELD_CHANCE) {
			player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
		}

		rollArmor(player, level, random, themeFor(biome));

		// ~10% per item: a random enchantment picked the way vanilla picks mob spawn enchantments.
		maybeEnchant(weaponStack, level, random, difficulty);
		maybeEnchant(player.getOffhandItem(), level, random, difficulty);
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			maybeEnchant(player.getItemBySlot(slot), level, random, difficulty);
		}
	}

	private static void maybeEnchant(ItemStack stack, ServerLevel level, RandomSource random, DifficultyInstance difficulty) {
		if (!stack.isEmpty() && random.nextFloat() < ENCHANT_CHANCE) {
			EnchantmentHelper.enchantItemFromProvider(stack, level.registryAccess(),
					VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficulty, random);
		}
	}

	public static ItemStack enchanted(ServerLevel level, Item item, ResourceKey<Enchantment> enchantment, int enchantLevel) {
		ItemStack stack = new ItemStack(item);
		stack.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment), enchantLevel);
		return stack;
	}

	// ------------------------------------------------------------ picking up better gear

	/**
	 * Wears the best armor in the inventory (moving the old piece back into the inventory), like dragging armor
	 * into the armor slots. Returns true if anything changed.
	 */
	public static boolean equipBestArmor(Player player) {
		boolean changed = false;
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			ItemStack worn = player.getItemBySlot(slot);
			double best = Inv.armorValue(worn, slot);
			int bestSlot = -1;
			for (int i = 0; i < Inv.MAIN; i++) {
				ItemStack stack = player.getInventory().getItem(i);
				Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
				if (equippable != null && equippable.slot() == slot) {
					double value = Inv.armorValue(stack, slot);
					if (value > best) {
						best = value;
						bestSlot = i;
					}
				}
			}
			if (bestSlot >= 0) {
				ItemStack better = player.getInventory().getItem(bestSlot);
				player.getInventory().setItem(bestSlot, worn);
				player.setItemSlot(slot, better);
				changed = true;
			}
		}
		return changed;
	}

	// ------------------------------------------------------------ rolling

	private static WeaponType rollWeaponType(RandomSource random, Holder<Biome> biome) {
		boolean watery = biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER) || biome.is(ConventionalBiomeTags.IS_BEACH);
		while (true) {
			int total = 0;
			for (WeaponType type : WeaponType.values()) {
				total += type.weight;
			}
			int roll = random.nextInt(total);
			for (WeaponType type : WeaponType.values()) {
				roll -= type.weight;
				if (roll < 0) {
					// Tridents only near water: otherwise roll again.
					if (type == WeaponType.TRIDENT && !watery) {
						break;
					}
					return type;
				}
			}
		}
	}

	private static Theme themeFor(Holder<Biome> biome) {
		if (biome.is(ConventionalBiomeTags.IS_MOUNTAIN_PEAK)) {
			return Theme.MOUNTAIN;
		}
		if (biome.is(ConventionalBiomeTags.IS_DESERT) || biome.is(BiomeTags.IS_BADLANDS)) {
			return Theme.DESERT;
		}
		if (biome.is(ConventionalBiomeTags.IS_SNOWY) || biome.is(ConventionalBiomeTags.IS_ICY)) {
			return Theme.SNOWY;
		}
		if (biome.is(BiomeTags.IS_JUNGLE) || biome.is(ConventionalBiomeTags.IS_SWAMP)) {
			return Theme.JUNGLE;
		}
		if (biome.is(BiomeTags.IS_TAIGA) || biome.is(ConventionalBiomeTags.IS_DARK_FOREST)) {
			return Theme.TAIGA;
		}
		if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH)) {
			return Theme.OCEAN;
		}
		if (biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(ConventionalBiomeTags.IS_MOUNTAIN) || biome.is(ConventionalBiomeTags.IS_WINDSWEPT)) {
			return Theme.MOUNTAIN;
		}
		return Theme.PLAINS;
	}

	private static void rollArmor(Player player, ServerLevel level, RandomSource random, Theme theme) {
		float fullSet = random.nextFloat();
		if (fullSet < FULL_SET_CHANCE) {
			// Rare full set anywhere: diamond 3 times out of 4, otherwise netherite.
			Material material = fullSet < FULL_SET_CHANCE * 0.75F ? Material.DIAMOND : Material.NETHERITE;
			for (EquipmentSlot slot : ARMOR_SLOTS) {
				ItemStack stack = new ItemStack(armorItem(material, slot));
				maybeTrim(stack, level, random, theme);
				player.setItemSlot(slot, stack);
			}
			return;
		}
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			if (random.nextFloat() >= ARMOR_PIECE_CHANCE) {
				continue;
			}
			Material material = theme.picks[pickPick(random, theme.picks)].material();
			if (theme == Theme.OCEAN && slot == EquipmentSlot.HEAD && random.nextFloat() < TURTLE_HELMET_CHANCE) {
				material = Material.TURTLE;
			}
			ItemStack stack = new ItemStack(armorItem(material, slot));
			if (material == Material.LEATHER) {
				stack.set(DataComponents.DYED_COLOR, new DyedItemColor(theme.leatherColor));
			}
			maybeTrim(stack, level, random, theme);
			player.setItemSlot(slot, stack);
		}
	}

	private static void maybeTrim(ItemStack stack, ServerLevel level, RandomSource random, Theme theme) {
		if (random.nextFloat() >= TRIM_CHANCE) {
			return;
		}
		HolderLookup.RegistryLookup<TrimPattern> patterns = level.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN);
		HolderLookup.RegistryLookup<TrimMaterial> materials = level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL);
		var pattern = patterns.get(theme.patterns[random.nextInt(theme.patterns.length)]);
		var material = materials.get(theme.materials[random.nextInt(theme.materials.length)]);
		if (pattern.isPresent() && material.isPresent()) {
			stack.set(DataComponents.TRIM, new ArmorTrim(material.get(), pattern.get()));
		}
	}

	private static Item armorItem(Material material, EquipmentSlot slot) {
		return switch (material) {
			case LEATHER -> pickSlot(slot, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
			case CHAINMAIL -> pickSlot(slot, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
			case IRON -> pickSlot(slot, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
			case GOLDEN -> pickSlot(slot, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
			case DIAMOND -> pickSlot(slot, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
			case NETHERITE -> pickSlot(slot, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
			case TURTLE -> Items.TURTLE_HELMET;
		};
	}

	private static Item pickSlot(EquipmentSlot slot, Item head, Item chest, Item legs, Item feet) {
		return switch (slot) {
			case HEAD -> head;
			case CHEST -> chest;
			case LEGS -> legs;
			default -> feet;
		};
	}

	private static int pickPick(RandomSource random, Pick[] picks) {
		int[] weights = new int[picks.length];
		for (int i = 0; i < picks.length; i++) {
			weights[i] = picks[i].weight();
		}
		return pickIndex(random, weights);
	}

	private static int pickIndex(RandomSource random, int[] weights) {
		int total = 0;
		for (int w : weights) {
			total += w;
		}
		int roll = random.nextInt(total);
		for (int i = 0; i < weights.length; i++) {
			roll -= weights[i];
			if (roll < 0) {
				return i;
			}
		}
		return 0;
	}
}
