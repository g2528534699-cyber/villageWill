package com.villagewill.entity.client;

import com.villagewill.VillageWill;
import com.villagewill.entity.StoneGolem;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SnowGolemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.SnowGolem;

/**
 * 石傀儡渲染：复用雪傀儡模型，替换为灰色岩石纹理
 */
public class StoneGolemRenderer extends SnowGolemRenderer {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(VillageWill.MODID, "textures/entity/stone_golem.png");

    public StoneGolemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SnowGolem entity) {
        return TEXTURE;
    }
}
