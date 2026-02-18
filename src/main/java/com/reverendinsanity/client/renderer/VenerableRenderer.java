package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.VenerableEntity;
import com.reverendinsanity.entity.VenerableType;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import java.util.EnumMap;
import java.util.Map;

// 十大尊者渲染器：根据尊者类型选择不同贴图
public class VenerableRenderer extends HumanoidMobRenderer<VenerableEntity, PlayerModel<VenerableEntity>> {

    private static final Map<VenerableType, ResourceLocation> TEXTURES = new EnumMap<>(VenerableType.class);
    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/venerable_yuan_shi.png");

    static {
        for (VenerableType type : VenerableType.values()) {
            TEXTURES.put(type, ResourceLocation.fromNamespaceAndPath("reverend_insanity",
                "textures/entity/venerable_" + type.name().toLowerCase() + ".png"));
        }
    }

    public VenerableRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(VenerableEntity entity) {
        String typeName = entity.getVenerableTypeName();
        VenerableType type = VenerableType.fromName(typeName);
        return TEXTURES.getOrDefault(type, DEFAULT_TEXTURE);
    }
}
