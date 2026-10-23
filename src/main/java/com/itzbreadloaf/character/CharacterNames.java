package com.itzbreadloaf.character;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Clean display names: "Master BreadLoaf" instead of the profile name "MasterBreadLoaf", "Knight ItzBreadLoaf"
 * for a knight, "ItzBreadLoaf" for ItzBreadLoaf_2 too. Works the same on server and client (it comes from the UUID).
 */
public final class CharacterNames {
	private CharacterNames() {
	}

	public static Component displayName(Player player) {
		CharacterVariant variant = LoafIds.variant(player.getUUID());
		return Component.literal(variant == null ? player.getGameProfile().name() : variant.displayName());
	}
}
