package com.itzbreadloaf.mixin.client;

import com.itzbreadloaf.character.CharacterVariant;
import com.itzbreadloaf.character.LoafIds;
import com.itzbreadloaf.client.CharacterSkins;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Characters wear their LoafCraft skin instead of a Steve/Alex default: chosen by their type (from the UUID) and
 * whether they're a baby (the scale attribute, synced like any attribute).
 */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
	@Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
	private void loaf$characterSkin(CallbackInfoReturnable<PlayerSkin> cir) {
		AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
		CharacterVariant variant = LoafIds.variant(self.getUUID());
		if (variant != null) {
			cir.setReturnValue(CharacterSkins.get(variant, self.getScale() < 0.95F, self.getUUID()));
		}
	}
}
