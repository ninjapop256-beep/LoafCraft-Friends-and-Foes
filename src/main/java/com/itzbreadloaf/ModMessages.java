package com.itzbreadloaf;

import com.itzbreadloaf.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

/** Short action-bar lines like "<ItzBreadLoaf> get over here!". Turned off with actionBarMessages=false in the config. */
public final class ModMessages {
	private ModMessages() {
	}

	public static void say(ServerPlayer player, Entity speaker, String text) {
		if (!ModConfig.get().actionBarMessages) {
			return;
		}
		Component line = Component.literal("<").append(speaker.getDisplayName()).append("> ")
				.append(Component.literal(text).withStyle(ChatFormatting.ITALIC));
		player.displayClientMessage(line, true);
	}

	public static String pick(RandomSource random, String... lines) {
		return lines[random.nextInt(lines.length)];
	}
}
