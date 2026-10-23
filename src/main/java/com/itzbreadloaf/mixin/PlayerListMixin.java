package com.itzbreadloaf.mixin;

import com.itzbreadloaf.character.LoafIds;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Characters are players to the game, but not to the player list:
 * they don't count toward the player count or max players, get no "joined the game" message,
 * and never write player data, stats or advancement files.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@Shadow
	public abstract List<ServerPlayer> getPlayers();

	@ModifyReturnValue(method = "getPlayerCount", at = @At("RETURN"))
	private int loaf$excludeCharacters(int count) {
		return count - loaf$countCharacters(this.getPlayers());
	}

	@ModifyExpressionValue(method = "canPlayerLogin", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
	private int loaf$excludeCharactersFromMaxPlayers(int count) {
		return count - loaf$countCharacters(this.getPlayers());
	}

	@WrapWithCondition(method = "placeNewPlayer", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
	private boolean loaf$hideJoinMessage(PlayerList playerList, Component message, boolean overlay,
			@Local(argsOnly = true) ServerPlayer player) {
		return !LoafIds.isCharacter(player);
	}

	@Inject(method = "save", at = @At("HEAD"), cancellable = true)
	private void loaf$noPlayerFiles(ServerPlayer player, CallbackInfo ci) {
		if (LoafIds.isCharacter(player)) {
			ci.cancel();
		}
	}

	private static int loaf$countCharacters(List<ServerPlayer> players) {
		int characters = 0;
		for (ServerPlayer player : players) {
			if (LoafIds.isCharacter(player)) {
				characters++;
			}
		}
		return characters;
	}
}
