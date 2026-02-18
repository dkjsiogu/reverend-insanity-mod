package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.MountainSpiderEntity;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

// 山地蜘蛛渲染器
public class MountainSpiderRenderer extends MobRenderer<MountainSpiderEntity, SpiderModel<MountainSpiderEntity>> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/mountain_spider.png");

    public MountainSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), 1.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(MountainSpiderEntity entity) {
        return TEXTURE;
    }
}
