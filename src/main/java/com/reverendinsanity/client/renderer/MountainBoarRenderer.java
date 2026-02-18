package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.MountainBoarEntity;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

// 山猪渲染器
public class MountainBoarRenderer extends MobRenderer<MountainBoarEntity, PigModel<MountainBoarEntity>> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/mountain_boar.png");

    public MountainBoarRenderer(EntityRendererProvider.Context context) {
        super(context, new PigModel<>(context.bakeLayer(ModelLayers.PIG)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(MountainBoarEntity entity) {
        return TEXTURE;
    }
}
