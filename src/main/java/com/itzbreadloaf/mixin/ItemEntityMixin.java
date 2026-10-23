package com.itzbreadloaf.mixin;

import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.LoafIds;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Characters only pick up items their brain wants (ItzBreadLoaf: better weapons, armor, food, blocks, arrows). */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
	private void loaf$characterPickup(Player player, CallbackInfo ci) {
		if (LoafIds.isCharacter(player) && !CharacterManager.wantsToPickUp(player, ((ItemEntity) (Object) this).getItem())) {
			ci.cancel();
		}
	}
}
