package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.StrawPuppetEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

// 草人傀儡渲染器
public class StrawPuppetRenderer extends MobRenderer<StrawPuppetEntity, PlayerModel<StrawPuppetEntity>> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/straw_puppet.png");

    public StrawPuppetRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(StrawPuppetEntity entity) {
        return TEXTURE;
    }
}
