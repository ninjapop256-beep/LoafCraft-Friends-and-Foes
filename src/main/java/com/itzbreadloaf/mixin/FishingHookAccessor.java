package com.itzbreadloaf.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fisher Madison watches her bobber: true while a fish is on the hook (a real player sees the bobber dip). */
@Mixin(FishingHook.class)
public interface FishingHookAccessor {
	@Accessor("biting")
	boolean loaf$isBiting();
}
