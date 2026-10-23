package com.itzbreadloaf.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Carpet's fake player spawn calls Entity.unsetRemoved (protected); LoafCraft does the same steps. */
@Mixin(Entity.class)
public interface EntityAccessor {
	@Invoker("unsetRemoved")
	void loaf$unsetRemoved();
}
