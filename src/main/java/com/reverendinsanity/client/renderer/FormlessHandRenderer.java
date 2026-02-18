package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.FormlessHandEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

// 无相手渲染器：纯粒子效果，不渲染实体模型
public class FormlessHandRenderer extends EntityRenderer<FormlessHandEntity> {

    public FormlessHandRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(FormlessHandEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");
    }
}
