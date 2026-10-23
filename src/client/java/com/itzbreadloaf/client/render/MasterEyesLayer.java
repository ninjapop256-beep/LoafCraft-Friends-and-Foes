package com.itzbreadloaf.client.render;

import com.itzbreadloaf.ItzBreadLoaf;
import com.itzbreadloaf.client.CharacterSkins;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Master BreadLoaf's glowing purple eyes, drawn at full brightness like spider and enderman eyes.
 * Added to the player renderer, and only drawn for players wearing his skin.
 */
public class MasterEyesLayer extends EyesLayer<AvatarRenderState, PlayerModel> {
	private static final RenderType EYES = RenderTypes.eyes(ItzBreadLoaf.id("textures/entity/master_breadloaf_eyes.png"));

	public MasterEyesLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
		super(parent);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
		if (CharacterSkins.isMaster(state.skin) && !state.isInvisible) {
			super.submit(poseStack, collector, light, state, yRot, xRot);
		}
	}

	@Override
	public RenderType renderType() {
		return EYES;
	}
}
