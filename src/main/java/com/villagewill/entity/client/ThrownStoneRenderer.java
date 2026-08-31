package com.villagewill.entity.client;

import com.villagewill.entity.ThrownStone;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/**
 * 石头球渲染：物品图标渲染
 */
public class ThrownStoneRenderer extends ThrownItemRenderer<ThrownStone> {
    public ThrownStoneRenderer(EntityRendererProvider.Context context) {
        super(context, 0.75F, true);
    }
}
