package com.villagewill.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.villagewill.entity.GuardCaptain;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * 警卫队长渲染：玩家模型（Steve 皮肤）+ 盔甲渲染层（下界合金套/附魔装备可见），体型稍大以示威仪
 */
public class GuardCaptainRenderer extends HumanoidMobRenderer<GuardCaptain, PlayerModel<GuardCaptain>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");

    public GuardCaptainRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.6F);
        // 盔甲渲染层（同原版 PlayerRenderer 写法），使下界合金套等装备材质显示
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
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
