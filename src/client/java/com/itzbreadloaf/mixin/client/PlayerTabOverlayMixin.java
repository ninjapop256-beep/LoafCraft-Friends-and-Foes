package com.itzbreadloaf.mixin.client;

import com.itzbreadloaf.character.LoafIds;
import java.util.List;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Characters don't show up in the Tab player list. */
@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
	@Inject(method = "getPlayerInfos", at = @At("RETURN"), cancellable = true)
	private void loaf$hideCharacters(CallbackInfoReturnable<List<PlayerInfo>> cir) {
		List<PlayerInfo> infos = cir.getReturnValue();
		for (PlayerInfo info : infos) {
			if (LoafIds.isCharacter(info.getProfile().id())) {
				cir.setReturnValue(infos.stream().filter(i -> !LoafIds.isCharacter(i.getProfile().id())).toList());
				return;
			}
		}
	}
}
