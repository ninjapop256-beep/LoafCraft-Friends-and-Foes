package com.itzbreadloaf.character;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Client side of right-clicking a character. Characters are players, and right-clicking a player normally does
 * nothing, so the client would go on to use the held item (eat the steak you meant to give). For characters the
 * client counts the click as handled and only sends it to the server, unless the item is something you'd want
 * to fire or throw while looking at them (bows, tridents, pearls, potions...).
 */
public final class CharacterInteractions {
	private CharacterInteractions() {
	}

	public static InteractionResult clientResult(Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		ItemStack stack = player.getItemInHand(hand);
		return isAimedItem(stack) ? InteractionResult.PASS : InteractionResult.SUCCESS;
	}

	public static boolean isAimedItem(ItemStack stack) {
		return stack.is(Items.BOW) || stack.is(Items.CROSSBOW) || stack.is(Items.TRIDENT) || stack.is(Items.SHIELD)
				|| stack.is(Items.ENDER_PEARL) || stack.is(Items.ENDER_EYE) || stack.is(Items.SNOWBALL)
				|| stack.is(Items.WIND_CHARGE) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)
				|| stack.is(Items.FISHING_ROD) || stack.is(Items.FIREWORK_ROCKET) || stack.is(Items.EXPERIENCE_BOTTLE)
				|| stack.is(ItemTags.SPEARS) || stack.has(DataComponents.KINETIC_WEAPON);
	}
}
