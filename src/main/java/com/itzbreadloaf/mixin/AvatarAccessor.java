package com.itzbreadloaf.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The "which skin layers are shown" data, set to all layers for characters (like Carpet does). */
@Mixin(Avatar.class)
public interface AvatarAccessor {
	@Accessor("DATA_PLAYER_MODE_CUSTOMISATION")
	static EntityDataAccessor<Byte> loaf$getModeCustomisation() {
		throw new AssertionError();
	}
}
