package com.itzbreadloaf.mixin;

import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.LoafIds;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Vanilla mobs ignore friendly characters (Lily, Madison, tamed ItzBreadLoafs) but still fight hostile ones. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "canBeSeenAsEnemy", at = @At("HEAD"), cancellable = true)
	private void loaf$friendlyCharacters(CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!self.level().isClientSide() && LoafIds.isCharacter(self) && CharacterManager.isFriendlyToMobs(self.getUUID())) {
			cir.setReturnValue(false);
		}
	}
}
