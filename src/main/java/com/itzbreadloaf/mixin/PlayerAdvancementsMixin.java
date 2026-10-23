package com.itzbreadloaf.mixin;

import com.itzbreadloaf.character.LoafIds;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Characters never earn advancements (no toasts, no chat messages, no advancement files). */
@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {
	@Shadow
	private ServerPlayer player;

	@Inject(method = "award", at = @At("HEAD"), cancellable = true)
	private void loaf$noAdvancements(AdvancementHolder advancement, String criterion, CallbackInfoReturnable<Boolean> cir) {
		if (LoafIds.isCharacter(this.player)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "save", at = @At("HEAD"), cancellable = true)
	private void loaf$noAdvancementFile(CallbackInfo ci) {
		if (LoafIds.isCharacter(this.player)) {
			ci.cancel();
		}
	}
}
