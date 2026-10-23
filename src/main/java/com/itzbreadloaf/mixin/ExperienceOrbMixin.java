package com.itzbreadloaf.mixin;

import com.itzbreadloaf.character.LoafIds;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Experience is for real players: characters never collect orbs. */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
	@Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
	private void loaf$noCharacterXp(Player player, CallbackInfo ci) {
		if (LoafIds.isCharacter(player)) {
			ci.cancel();
		}
	}
}
