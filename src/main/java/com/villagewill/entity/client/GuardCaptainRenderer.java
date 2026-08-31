package com.villagewill.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.villagewill.entity.GuardCaptain;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 警卫队长渲染：玩家模型（Steve 皮肤），带盔甲层，体型稍大以示威仪
 */
public class GuardCaptainRenderer extends HumanoidMobRenderer<GuardCaptain, PlayerModel<GuardCaptain>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");

    public GuardCaptainRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(GuardCaptain entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(GuardCaptain entity, PoseStack poseStack, float partialTicks) {
        poseStack.scale(1.15F, 1.15F, 1.15F);
    }
}
