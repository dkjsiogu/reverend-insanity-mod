package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.FireBoltEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

// 火弹投射物渲染，基于 ThrownItemRenderer
public class FireBoltRenderer extends ThrownItemRenderer<FireBoltEntity> {
    public FireBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
