package com.reverendinsanity.client.renderer;

import com.reverendinsanity.client.model.WildGuModel;
import com.reverendinsanity.entity.WildGuEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

// 野蛊渲染器：缩小渲染，使用统一贴图
public class WildGuRenderer extends MobRenderer<WildGuEntity, WildGuModel> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/wild_gu.png");

    public WildGuRenderer(EntityRendererProvider.Context context) {
        super(context, new WildGuModel(context.bakeLayer(WildGuModel.LAYER_LOCATION)), 0.15f);
    }

    @Override
    public ResourceLocation getTextureLocation(WildGuEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(WildGuEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.5f, 0.5f, 0.5f);
    }
}
