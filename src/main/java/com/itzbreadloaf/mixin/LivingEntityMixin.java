package com.itzbreadloaf.mixin;

import com.itzbreadloaf.brain.MasterBreadLoafBrain;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.LoafIds;
import com.itzbreadloaf.config.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	/** Vanilla mobs ignore friendly characters (Lily, Madison, tamed ItzBreadLoafs) but still fight hostile ones. */
	@Inject(method = "canBeSeenAsEnemy", at = @At("HEAD"), cancellable = true)
	private void loaf$friendlyCharacters(CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!self.level().isClientSide() && LoafIds.isCharacter(self) && CharacterManager.isFriendlyToMobs(self.getUUID())) {
			cir.setReturnValue(false);
		}
	}

	/**
	 * Master BreadLoaf's damage cap: no single hit of his (sword, mace smash, anything he causes) deals more than
	 * 16 damage, whatever the fall height. Capped before armor, so after armor it's even less.
	 */
	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
	private float loaf$capBossHits(float amount, ServerLevel level, DamageSource source) {
		if (amount > ModConfig.BOSS_MAX_HIT_DAMAGE && source.getEntity() != null
				&& CharacterManager.brain(source.getEntity()) instanceof MasterBreadLoafBrain) {
			return ModConfig.BOSS_MAX_HIT_DAMAGE;
		}
		return amount;
	}
}
