package com.itzbreadloaf.mixin;

import com.itzbreadloaf.brain.LoafBrain;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.CharacterNames;
import com.itzbreadloaf.character.LoafIds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Characters: clean display names ("Master BreadLoaf", gold for Best Friends) and their own hurt sound
 * (ItzBreadLoaf's meow) instead of the player one.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {
	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	private void loaf$characterName(CallbackInfoReturnable<Component> cir) {
		Player self = (Player) (Object) this;
		if (LoafIds.isCharacter(self.getUUID())) {
			cir.setReturnValue(CharacterNames.displayName(self));
		}
	}

	@Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
	private void loaf$characterHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
		Player self = (Player) (Object) this;
		if (!self.level().isClientSide()) {
			LoafBrain brain = CharacterManager.brain(self);
			SoundEvent sound = brain == null ? null : brain.hurtSound();
			if (sound != null) {
				cir.setReturnValue(sound);
			}
		}
	}
}
