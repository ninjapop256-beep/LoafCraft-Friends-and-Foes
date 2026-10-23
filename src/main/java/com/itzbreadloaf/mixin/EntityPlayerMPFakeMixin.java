package com.itzbreadloaf.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.itzbreadloaf.character.CharacterManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Carpet turns /kill on a fake player into a silent disconnect. For characters it is a real (permanent) death. */
@Mixin(EntityPlayerMPFake.class)
public abstract class EntityPlayerMPFakeMixin {
	@Inject(method = "kill(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("HEAD"), cancellable = true)
	private void loaf$realDeath(ServerLevel level, CallbackInfo ci) {
		if (CharacterManager.killByCommand((EntityPlayerMPFake) (Object) this)) {
			ci.cancel();
		}
	}
}
