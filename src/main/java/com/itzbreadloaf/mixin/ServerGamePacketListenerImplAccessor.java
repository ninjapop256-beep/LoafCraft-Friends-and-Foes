package com.itzbreadloaf.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Marks a character's connection as "world loaded" right away (players can't be hurt until it is). */
@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {
	@Invoker("markClientLoaded")
	void loaf$markClientLoaded();
}
