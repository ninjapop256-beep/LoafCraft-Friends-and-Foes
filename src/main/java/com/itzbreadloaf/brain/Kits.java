package com.itzbreadloaf.brain;

import com.itzbreadloaf.character.CharacterVariant;
import com.itzbreadloaf.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Starting gear for every character type, written straight into the player's inventory and armor slots.
 * (The original ItzBreadLoaf rolls random biome gear in {@link Gear}; Master BreadLoaf has his own kit.)
 */
public final class Kits {
	private Kits() {
	}

	private static void armor(Player player, Item head, Item chest, Item legs, Item feet) {
		player.setItemSlot(EquipmentSlot.HEAD, head == null ? ItemStack.EMPTY : new ItemStack(head));
		player.setItemSlot(EquipmentSlot.CHEST, chest == null ? ItemStack.EMPTY : new ItemStack(chest));
		player.setItemSlot(EquipmentSlot.LEGS, legs == null ? ItemStack.EMPTY : new ItemStack(legs));
		player.setItemSlot(EquipmentSlot.FEET, feet == null ? ItemStack.EMPTY : new ItemStack(feet));
	}

	private static void dye(Player player, EquipmentSlot slot, int color) {
		ItemStack stack = player.getItemBySlot(slot);
		if (!stack.isEmpty()) {
			stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
		}
	}

	private static void enchantArmor(Player player, ServerLevel level, int protection) {
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			ItemStack stack = player.getItemBySlot(slot);
			if (!stack.isEmpty() && !stack.is(Items.RED_BANNER)) {
				stack.enchant(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
						.getOrThrow(Enchantments.PROTECTION), protection);
			}
		}
	}

	/** Gear for the ItzBreadLoaf types (not the original, which rolls random biome gear). */
	public static void breadLoaf(Player player, ServerLevel level, CharacterVariant variant, RandomSource random) {
		Inventory inventory = player.getInventory();
		player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
		switch (variant) {
			case KNIGHT -> {
				armor(player, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
				inventory.setItem(0, random.nextInt(4) == 0 ? Gear.enchanted(level, Items.IRON_SWORD, Enchantments.SHARPNESS, 1) : new ItemStack(Items.IRON_SWORD));
				player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
			}
			case ARCHER -> {
				armor(player, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, null, Items.CHAINMAIL_BOOTS);
				dye(player, EquipmentSlot.HEAD, 0x3F7A2E);
				dye(player, EquipmentSlot.CHEST, 0x3F7A2E);
				inventory.setItem(0, random.nextBoolean() ? Gear.enchanted(level, Items.BOW, Enchantments.POWER, 1) : new ItemStack(Items.BOW));
				inventory.setItem(3, new ItemStack(Items.STONE_SWORD));
				inventory.setItem(9, new ItemStack(Items.ARROW, 64));
			}
			case MINER -> {
				armor(player, Items.IRON_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
				dye(player, EquipmentSlot.CHEST, 0x6B4A2B);
				dye(player, EquipmentSlot.LEGS, 0x4A4A4A);
				dye(player, EquipmentSlot.FEET, 0x4A4A4A);
				inventory.setItem(0, new ItemStack(random.nextInt(5) == 0 ? Items.DIAMOND_PICKAXE : Items.IRON_PICKAXE));
				inventory.setItem(4, new ItemStack(Items.TORCH, 32));
				inventory.setItem(10, new ItemStack(Items.RAW_IRON, 2 + random.nextInt(5)));
				inventory.setItem(11, new ItemStack(Items.COAL, 4 + random.nextInt(8)));
			}
			case PIRATE -> {
				armor(player, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, null, Items.LEATHER_BOOTS);
				dye(player, EquipmentSlot.HEAD, 0x202020);
				dye(player, EquipmentSlot.CHEST, 0x8B1A1A);
				dye(player, EquipmentSlot.FEET, 0x202020);
				inventory.setItem(0, new ItemStack(Items.TRIDENT));
				inventory.setItem(3, new ItemStack(Items.IRON_SWORD));
				inventory.setItem(10, new ItemStack(Items.GOLD_NUGGET, 4 + random.nextInt(10)));
			}
			case NETHER -> {
				armor(player, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
				inventory.setItem(0, Gear.enchanted(level, Items.GOLDEN_SWORD, Enchantments.SHARPNESS, 2));
				inventory.setItem(10, new ItemStack(Items.GOLD_INGOT, 2 + random.nextInt(4)));
			}
			case SWEATY -> {
				armor(player, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
				enchantArmor(player, level, 2);
				inventory.setItem(0, Gear.enchanted(level, Items.DIAMOND_SWORD, Enchantments.SHARPNESS, 3));
				player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
				inventory.setItem(5, new ItemStack(Items.ENDER_PEARL, 8));
				inventory.setItem(6, new ItemStack(Items.GOLDEN_APPLE, 4));
			}
			case CAPTAIN -> {
				armor(player, Items.RED_BANNER, Items.NETHERITE_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
				enchantArmor(player, level, 1);
				inventory.setItem(0, Gear.enchanted(level, Items.DIAMOND_AXE, Enchantments.SHARPNESS, 2));
				player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
				inventory.setItem(6, new ItemStack(Items.GOLDEN_APPLE, 2));
				inventory.setItem(10, new ItemStack(Items.EMERALD, 4 + random.nextInt(6)));
			}
			case BANDIT -> {
				armor(player, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, null);
				dye(player, EquipmentSlot.HEAD, 0x1A1A1A);
				dye(player, EquipmentSlot.CHEST, 0x1A1A1A);
				dye(player, EquipmentSlot.LEGS, 0x3A2A1A);
				if (random.nextInt(3) == 0) {
					inventory.setItem(0, new ItemStack(Items.CROSSBOW));
					inventory.setItem(9, new ItemStack(Items.ARROW, 32));
				} else {
					inventory.setItem(0, new ItemStack(random.nextBoolean() ? Items.IRON_AXE : Items.STONE_SWORD));
				}
				inventory.setItem(10, new ItemStack(Items.EMERALD, 1 + random.nextInt(3)));
			}
			case FISHERMAN -> {
				armor(player, Items.LEATHER_HELMET, null, null, Items.LEATHER_BOOTS);
				dye(player, EquipmentSlot.HEAD, 0xE0C060);
				dye(player, EquipmentSlot.FEET, 0x3B5DC9);
				inventory.setItem(0, new ItemStack(Items.STONE_AXE));
				inventory.setItem(1, new ItemStack(Items.FISHING_ROD));
				inventory.setItem(10, new ItemStack(Items.COD, 3 + random.nextInt(6)));
			}
			case VILLAGER -> {
				armor(player, null, Items.LEATHER_CHESTPLATE, null, Items.LEATHER_BOOTS);
				dye(player, EquipmentSlot.CHEST, 0x9C6B3C);
				dye(player, EquipmentSlot.FEET, 0x5A3A1C);
				inventory.setItem(0, new ItemStack(Items.IRON_SWORD));
				inventory.setItem(10, new ItemStack(Items.WHEAT, 6));
			}
			default -> {
			}
		}
		inventory.setSelectedSlot(0);
	}

	/** Lily types: the fight weapon (hotbar 3), what she holds when calm (hotbar 1), and her type's supplies. */
	public static void lily(Player player, ServerLevel level, CharacterVariant variant, RandomSource random) {
		Inventory inventory = player.getInventory();
		inventory.setItem(NeutralBrain.WEAPON_SLOT, new ItemStack(Items.IRON_SWORD));
		switch (variant) {
			case BOOKWORM -> {
				inventory.setItem(NeutralBrain.CALM_SLOT, new ItemStack(Items.BOOK));
				inventory.setItem(10, new ItemStack(Items.PAPER, 6));
			}
			case GARDENER -> {
				inventory.setItem(NeutralBrain.CALM_SLOT, new ItemStack(LilyBrain.randomFlower(random)));
				inventory.setItem(4, new ItemStack(Items.BONE_MEAL, 32));
				inventory.setItem(5, new ItemStack(LilyBrain.randomFlower(random), 16));
				armor(player, null, null, null, Items.LEATHER_BOOTS);
				dye(player, EquipmentSlot.FEET, 0x4C7A34);
			}
			case WITCHY -> {
				armor(player, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, null, null);
				dye(player, EquipmentSlot.HEAD, 0x5B2A86);
				dye(player, EquipmentSlot.CHEST, 0x5B2A86);
				inventory.setItem(NeutralBrain.CALM_SLOT, new ItemStack(Items.GLOWSTONE_DUST, 3));
				inventory.setItem(10, LoafBrain.potion(Items.SPLASH_POTION, Potions.STRONG_HEALING));
				inventory.setItem(11, LoafBrain.potion(Items.SPLASH_POTION, Potions.STRONG_HEALING));
				inventory.setItem(12, LoafBrain.potion(Items.SPLASH_POTION, Potions.STRONG_HEALING));
				inventory.setItem(13, LoafBrain.potion(Items.SPLASH_POTION, Potions.STRONG_SLOWNESS));
				inventory.setItem(14, LoafBrain.potion(Items.SPLASH_POTION, Potions.STRONG_SLOWNESS));
				inventory.setItem(15, LoafBrain.potion(Items.SPLASH_POTION, Potions.WEAKNESS));
				inventory.setItem(16, LoafBrain.potion(Items.SPLASH_POTION, Potions.WEAKNESS));
			}
			case ARCHER_LILY -> {
				inventory.setItem(NeutralBrain.WEAPON_SLOT, random.nextBoolean() ? Gear.enchanted(level, Items.BOW, Enchantments.POWER, 2) : new ItemStack(Items.BOW));
				inventory.setItem(3, new ItemStack(Items.IRON_SWORD));
				inventory.setItem(9, new ItemStack(Items.ARROW, 64));
				armor(player, null, Items.LEATHER_CHESTPLATE, null, null);
				dye(player, EquipmentSlot.CHEST, 0xF4A6C6);
			}
			default -> inventory.setItem(NeutralBrain.CALM_SLOT, random.nextBoolean() ? new ItemStack(LilyBrain.randomFlower(random)) : ItemStack.EMPTY);
		}
		inventory.setSelectedSlot(NeutralBrain.CALM_SLOT);
	}

	/** Madison types: axe (hotbar 3), calm item (hotbar 1) and the tools of her trade. */
	public static void madison(Player player, ServerLevel level, CharacterVariant variant, RandomSource random) {
		Inventory inventory = player.getInventory();
		inventory.setItem(NeutralBrain.WEAPON_SLOT, new ItemStack(random.nextBoolean() ? Items.STONE_AXE : Items.IRON_AXE));
		switch (variant) {
			case FARMER -> {
				inventory.setItem(NeutralBrain.CALM_SLOT, new ItemStack(Items.IRON_HOE));
				inventory.setItem(4, new ItemStack(Items.WHEAT_SEEDS, 16));
				inventory.setItem(5, new ItemStack(Items.CARROT, 8));
				inventory.setItem(6, new ItemStack(Items.POTATO, 8));
				armor(player, null, null, null, Items.LEATHER_BOOTS);
				dye(player, EquipmentSlot.FEET, 0x6B4A2B);
			}
			case BAKER -> {
				inventory.setItem(NeutralBrain.CALM_SLOT, new ItemStack(ModItems.LOAF_BREAD));
				inventory.setItem(10, new ItemStack(ModItems.LOAF_BREAD, 32));
				inventory.setItem(11, new ItemStack(Items.CAKE, 4));
				inventory.setItem(12, new ItemStack(Items.BREAD, 32));
				inventory.setItem(13, new ItemStack(Items.COOKIE, 32));
				armor(player, null, Items.LEATHER_CHESTPLATE, null, null);
				dye(player, EquipmentSlot.CHEST, 0xF2F2F2);
			}
			case FISHER -> {
				inventory.setItem(NeutralBrain.CALM_SLOT, new ItemStack(Items.FISHING_ROD));
				armor(player, Items.LEATHER_HELMET, null, null, Items.LEATHER_BOOTS);
				dye(player, EquipmentSlot.HEAD, 0xE0C060);
				dye(player, EquipmentSlot.FEET, 0x3B5DC9);
			}
			case MERCHANT -> {
				inventory.setItem(NeutralBrain.CALM_SLOT, new ItemStack(Items.EMERALD));
				inventory.setItem(10, new ItemStack(Items.GOLDEN_CARROT, 16));
				inventory.setItem(11, new ItemStack(Items.PUMPKIN_PIE, 12));
				inventory.setItem(12, new ItemStack(Items.APPLE, 24));
				inventory.setItem(13, new ItemStack(Items.HONEY_BOTTLE, 8));
				inventory.setItem(14, new ItemStack(Items.MELON_SLICE, 32));
				inventory.setItem(15, new ItemStack(Items.EMERALD, 8));
				armor(player, null, Items.LEATHER_CHESTPLATE, null, null);
				dye(player, EquipmentSlot.CHEST, 0x2E8B57);
			}
			case BEEKEEPER -> {
				inventory.setItem(NeutralBrain.CALM_SLOT, new ItemStack(Items.HONEYCOMB));
				inventory.setItem(4, new ItemStack(Items.GLASS_BOTTLE, 16));
				armor(player, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, null, null);
				dye(player, EquipmentSlot.HEAD, 0xF2E36B);
				dye(player, EquipmentSlot.CHEST, 0xF2E36B);
			}
			default -> {
				float roll = random.nextFloat();
				inventory.setItem(NeutralBrain.CALM_SLOT, roll < 0.3F ? new ItemStack(Items.CARROT) : roll < 0.5F ? new ItemStack(Items.WHEAT) : ItemStack.EMPTY);
			}
		}
		inventory.setSelectedSlot(NeutralBrain.CALM_SLOT);
	}

	private static ItemStack enchant(ServerLevel level, ItemStack stack, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantment, int lvl) {
		stack.enchant(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(enchantment), lvl);
		return stack;
	}

	/**
	 * FelixSyn: diamond armor (each piece 30% netherite) with Protection II-III, a Density + Wind Burst mace, a diamond or
	 * netherite sword as backup, wind charges, golden apples, Strength and Healing potions.
	 */
	public static void felixSyn(Player player, ServerLevel level, RandomSource random) {
		Item[][] pieces = {
				{Items.DIAMOND_HELMET, Items.NETHERITE_HELMET}, {Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE},
				{Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS}, {Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS}};
		EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
		for (int i = 0; i < 4; i++) {
			ItemStack piece = new ItemStack(pieces[i][random.nextFloat() < 0.3F ? 1 : 0]);
			player.setItemSlot(slots[i], enchant(level, piece, Enchantments.PROTECTION, 2 + random.nextInt(2)));
		}
		Inventory inventory = player.getInventory();
		ItemStack mace = enchant(level, new ItemStack(Items.MACE), Enchantments.DENSITY, 3 + random.nextInt(2));
		inventory.setItem(2, enchant(level, mace, Enchantments.WIND_BURST, 1));
		ItemStack sword = new ItemStack(random.nextBoolean() ? Items.NETHERITE_SWORD : Items.DIAMOND_SWORD);
		inventory.setItem(0, enchant(level, sword, Enchantments.SHARPNESS, 3));
		inventory.setItem(4, new ItemStack(Items.WIND_CHARGE, 24));
		inventory.setItem(6, new ItemStack(Items.GOLDEN_APPLE, 3));
		inventory.setItem(9, LoafBrain.potion(Items.POTION, Potions.STRENGTH));
		inventory.setItem(10, LoafBrain.potion(Items.SPLASH_POTION, Potions.STRONG_HEALING));
		inventory.setItem(11, LoafBrain.potion(Items.SPLASH_POTION, Potions.STRONG_HEALING));
		inventory.setSelectedSlot(0);
	}

	/**
	 * Harley: full netherite (Protection IV, Unbreaking III), Harley's Blade (Sharpness V netherite sword), a Breach +
	 * Wind Burst mace, a shield, ender pearls, golden apples, wind charges, Strength II and Swiftness II potions.
	 */
	public static void harley(Player player, ServerLevel level, RandomSource random) {
		// Full diamond with Protection II (no netherite), a shield, Harley's Blade and a plain mace. No pearls,
		// no potions, and one golden apple.
		Item[] armor = {Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS};
		EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
		for (int i = 0; i < 4; i++) {
			player.setItemSlot(slots[i], enchant(level, new ItemStack(armor[i]), Enchantments.PROTECTION, 2));
		}
		player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
		Inventory inventory = player.getInventory();
		inventory.setItem(0, ModItems.harleysBlade(level.registryAccess()));
		inventory.setItem(2, new ItemStack(Items.MACE));
		inventory.setItem(4, new ItemStack(Items.WIND_CHARGE, 8));
		inventory.setItem(6, new ItemStack(Items.GOLDEN_APPLE, 1));
		inventory.setSelectedSlot(0);
	}

	/** Pirates swim fast and breathe underwater; Nether ItzBreadLoafs don't burn. Re-applied when they restock. */
	public static void passiveEffects(Player player, CharacterVariant variant) {
		if (variant == CharacterVariant.PIRATE) {
			player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, MobEffectInstance.INFINITE_DURATION, 0, false, false));
			player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, MobEffectInstance.INFINITE_DURATION, 0, false, false));
		} else if (variant == CharacterVariant.NETHER) {
			player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0, false, false));
		}
	}
}
