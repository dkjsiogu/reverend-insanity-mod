package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.IceBoltEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

// 冰弹投射物渲染器：使用寒冰蛊物品贴图
public class IceBoltRenderer extends ThrownItemRenderer<IceBoltEntity> {

    public IceBoltRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0f, true);
    }
}
