package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.LightningWolfEntity;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

// 电狼渲染器
public class LightningWolfRenderer extends MobRenderer<LightningWolfEntity, QuadrupedModel<LightningWolfEntity>> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/lightning_wolf.png");

    @SuppressWarnings("unchecked")
    public LightningWolfRenderer(EntityRendererProvider.Context context) {
        super(context, (QuadrupedModel<LightningWolfEntity>)(QuadrupedModel<?>)
            new net.minecraft.client.model.PigModel<>(context.bakeLayer(ModelLayers.PIG)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(LightningWolfEntity entity) {
        return TEXTURE;
    }
}
