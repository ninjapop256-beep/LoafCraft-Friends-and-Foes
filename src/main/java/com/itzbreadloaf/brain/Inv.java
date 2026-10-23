package com.itzbreadloaf.brain;

import java.util.function.Predicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Helpers for a character's real player inventory (0-8 hotbar, 9-35 main, 36-39 armor, 40 offhand).
 * Moving things between slots writes the player data directly, like dragging items in the inventory screen.
 */
public final class Inv {
	public static final int HOTBAR = 9;
	public static final int MAIN = 36;

	private Inv() {
	}

	/** First slot (hotbar first, then the rest of the inventory) holding a matching item, or -1. */
	public static int find(Player player, Predicate<ItemStack> test) {
		Inventory inventory = player.getInventory();
		for (int i = 0; i < MAIN; i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && test.test(stack)) {
				return i;
			}
		}
		return -1;
	}

	public static int count(Player player, Predicate<ItemStack> test) {
		Inventory inventory = player.getInventory();
		int total = 0;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && test.test(stack)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	/** Makes sure the item in {@code slot} is on the hotbar (swapping it into {@code preferred} if not). */
	public static int toHotbar(Player player, int slot, int preferred) {
		if (slot < 0) {
			return -1;
		}
		if (slot < HOTBAR) {
			return slot;
		}
		Inventory inventory = player.getInventory();
		ItemStack moving = inventory.getItem(slot);
		ItemStack displaced = inventory.getItem(preferred);
		inventory.setItem(preferred, moving);
		inventory.setItem(slot, displaced);
		return preferred;
	}

	/** Finds a matching item and puts it on the hotbar. Returns the hotbar slot, or -1 if there is none. */
	public static int ready(Player player, Predicate<ItemStack> test, int preferred) {
		return toHotbar(player, find(player, test), preferred);
	}

	public static boolean give(Player player, ItemStack stack) {
		return player.getInventory().add(stack);
	}

	/** Puts a stack in a specific slot (hotbar, main, armor 36-39 or offhand 40), returning what was there. */
	public static ItemStack set(Player player, int slot, ItemStack stack) {
		Inventory inventory = player.getInventory();
		ItemStack old = inventory.getItem(slot);
		inventory.setItem(slot, stack);
		return old;
	}

	/** Everyday food (golden apples are saved for fights, bad food is never eaten). */
	public static boolean isFood(ItemStack stack) {
		return stack.has(DataComponents.FOOD) && !stack.is(net.minecraft.world.item.Items.ROTTEN_FLESH)
				&& !stack.is(net.minecraft.world.item.Items.GOLDEN_APPLE) && !stack.is(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE)
				&& !stack.is(net.minecraft.world.item.Items.SPIDER_EYE) && !stack.is(net.minecraft.world.item.Items.POISONOUS_POTATO)
				&& !stack.is(net.minecraft.world.item.Items.PUFFERFISH) && !stack.is(net.minecraft.world.item.Items.CHORUS_FRUIT);
	}

	/** Full, solid building blocks (dirt, cobblestone, planks...). */
	public static boolean isBuildingBlock(ItemStack stack) {
		return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock().defaultBlockState().isSolidRender()
				&& !blockItem.getBlock().defaultBlockState().hasBlockEntity()
				&& blockItem.getBlock().defaultBlockState().getLightEmission() == 0;
	}

	/** Attack damage the item adds in the main hand (0 for non-weapons). */
	public static double attackDamage(ItemStack stack) {
		ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		double damage = 0.0;
		for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
			if (entry.attribute().equals(Attributes.ATTACK_DAMAGE) && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE
					&& entry.slot().test(EquipmentSlot.MAINHAND)) {
				damage += entry.modifier().amount();
			}
		}
		return damage;
	}

	/** Armor points the item gives in its slot. */
	public static double armorValue(ItemStack stack, EquipmentSlot slot) {
		ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		double armor = 0.0;
		for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
			if ((entry.attribute().equals(Attributes.ARMOR) || entry.attribute().equals(Attributes.ARMOR_TOUGHNESS))
					&& entry.slot().test(slot)) {
				armor += entry.modifier().amount();
			}
		}
		return armor;
	}
}
