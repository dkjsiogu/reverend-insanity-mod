package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.ThunderCrownWolfEntity;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

// 雷冠头狼渲染器
public class ThunderCrownWolfRenderer extends MobRenderer<ThunderCrownWolfEntity, QuadrupedModel<ThunderCrownWolfEntity>> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/thunder_crown_wolf.png");

    @SuppressWarnings("unchecked")
    public ThunderCrownWolfRenderer(EntityRendererProvider.Context context) {
        super(context, (QuadrupedModel<ThunderCrownWolfEntity>)(QuadrupedModel<?>)
            new net.minecraft.client.model.PigModel<>(context.bakeLayer(ModelLayers.PIG)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(ThunderCrownWolfEntity entity) {
        return TEXTURE;
    }
}
