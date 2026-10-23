package com.itzbreadloaf.client;

import com.itzbreadloaf.client.render.MasterEyesLayer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

/**
 * Client side of LoafCraft. Characters are players, so they use the vanilla player renderer: skins come from the
 * AbstractClientPlayer skin mixin (picked by UUID, slim or classic arms per character), the Tab list hides them
 * (PlayerTabOverlay mixin), their name tags show their type ("Knight ItzBreadLoaf"), and Master BreadLoaf gets his
 * glowing eyes as an extra player-renderer layer.
 */
public class ItzBreadLoafClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
			if (renderer instanceof AvatarRenderer<?> avatar) {
				@SuppressWarnings("unchecked")
				RenderLayerParent<AvatarRenderState, PlayerModel> parent = (RenderLayerParent<AvatarRenderState, PlayerModel>) (Object) avatar;
				helper.register(new MasterEyesLayer(parent));
			}
		});
	}
}
