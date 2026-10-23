package com.itzbreadloaf.character;

import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.itzbreadloaf.mixin.AvatarAccessor;
import com.itzbreadloaf.mixin.EntityAccessor;
import com.itzbreadloaf.mixin.ServerGamePacketListenerImplAccessor;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Constructor;
import java.util.Set;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Spawns Carpet fake players with a fixed UUID and name.
 *
 * Carpet's own {@code EntityPlayerMPFake.createFake} looks the name up on Mojang's servers (async, random UUID for
 * unknown names) and loads the player data file. LoafCraft needs fixed offline-style UUIDs and keeps its own saves,
 * so this does the same steps as Carpet's createFake callback, just with our profile and without the file load.
 */
public final class FakePlayers {
	private static Constructor<EntityPlayerMPFake> constructor;

	private FakePlayers() {
	}

	public static EntityPlayerMPFake spawn(MinecraftServer server, ServerLevel level, GameProfile profile, Vec3 pos, float yaw, float pitch) {
		EntityPlayerMPFake fake = construct(server, level, profile);
		fake.fixStartingPosition = () -> fake.snapTo(pos.x, pos.y, pos.z, yaw, pitch);
		server.getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), fake,
				new CommonListenerCookie(profile, 0, fake.clientInformation(), false));
		fake.stopRiding();
		fake.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), yaw, pitch, true);
		fake.setHealth(fake.getMaxHealth());
		((EntityAccessor) fake).loaf$unsetRemoved();
		AttributeInstance stepHeight = fake.getAttribute(Attributes.STEP_HEIGHT);
		if (stepHeight != null) {
			stepHeight.setBaseValue(0.6);
		}
		fake.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
		server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(fake, (byte) (fake.yHeadRot * 256.0F / 360.0F)), level.dimension());
		server.getPlayerList().broadcastAll(ClientboundEntityPositionSyncPacket.of(fake), level.dimension());
		// Show every skin layer (hat, jacket, sleeves, pants).
		fake.getEntityData().set(AvatarAccessor.loaf$getModeCustomisation(), (byte) 0x7f);
		fake.getAbilities().flying = false;
		// A real client tells the server when it has loaded the world; until then players can't be hurt.
		((ServerGamePacketListenerImplAccessor) fake.connection).loaf$markClientLoaded();
		return fake;
	}

	private static EntityPlayerMPFake construct(MinecraftServer server, ServerLevel level, GameProfile profile) {
		try {
			if (constructor == null) {
				constructor = EntityPlayerMPFake.class.getDeclaredConstructor(MinecraftServer.class, ServerLevel.class,
						GameProfile.class, ClientInformation.class, boolean.class);
				constructor.setAccessible(true);
			}
			return constructor.newInstance(server, level, profile, ClientInformation.createDefault(), false);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("LoafCraft could not create a Carpet fake player. Is the right Carpet version installed?", e);
		}
	}
}
