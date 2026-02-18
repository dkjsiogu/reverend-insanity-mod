package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.BloodBoltEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

// 血弹投射物渲染器：使用血蛊物品贴图
public class BloodBoltRenderer extends ThrownItemRenderer<BloodBoltEntity> {

    public BloodBoltRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0f, true);
    }
}
