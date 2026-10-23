package com.itzbreadloaf.mixin;

import com.itzbreadloaf.character.LoafIds;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Characters don't count for playersSleepingPercentage: not as players who must sleep, not as sleepers. */
@Mixin(SleepStatus.class)
public abstract class SleepStatusMixin {
	@ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true)
	private List<ServerPlayer> loaf$realPlayersOnly(List<ServerPlayer> players) {
		return loaf$filter(players);
	}

	@ModifyVariable(method = "areEnoughDeepSleeping", at = @At("HEAD"), argsOnly = true)
	private List<ServerPlayer> loaf$realDeepSleepersOnly(List<ServerPlayer> players) {
		return loaf$filter(players);
	}

	private static List<ServerPlayer> loaf$filter(List<ServerPlayer> players) {
		for (ServerPlayer player : players) {
			if (LoafIds.isCharacter(player)) {
				return players.stream().filter(p -> !LoafIds.isCharacter(p)).toList();
			}
		}
		return players;
	}
}
