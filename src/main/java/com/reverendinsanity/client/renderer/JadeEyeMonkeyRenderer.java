package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.JadeEyeMonkeyEntity;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

// 玉眼石猴渲染器
public class JadeEyeMonkeyRenderer extends MobRenderer<JadeEyeMonkeyEntity, SilverfishModel<JadeEyeMonkeyEntity>> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/jade_eye_monkey.png");

    @SuppressWarnings("unchecked")
    public JadeEyeMonkeyRenderer(EntityRendererProvider.Context context) {
        super(context, (SilverfishModel<JadeEyeMonkeyEntity>)(SilverfishModel<?>)
            new SilverfishModel<>(context.bakeLayer(ModelLayers.SILVERFISH)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(JadeEyeMonkeyEntity entity) {
        return TEXTURE;
    }
}
