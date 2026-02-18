package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.PhantomImmortalEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

// 幻影蛊仙渲染器：纯粒子效果，不渲染实体模型
public class PhantomImmortalRenderer extends EntityRenderer<PhantomImmortalEntity> {

    public PhantomImmortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(PhantomImmortalEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");
    }
}
