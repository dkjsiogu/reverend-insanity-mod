package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.MoonBladeEntity;
import com.reverendinsanity.registry.ModItems;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

// 月刃投射物渲染器：使用月光蛊物品贴图
public class MoonBladeRenderer extends ThrownItemRenderer<MoonBladeEntity> {

    public MoonBladeRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0f, true);
    }
}
