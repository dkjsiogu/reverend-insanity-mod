package com.reverendinsanity.client.renderer;

import com.reverendinsanity.entity.GuMasterEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

// NPC蛊师渲染器：根据道路选择战斗原型贴图，魔道优先
public class GuMasterRenderer extends HumanoidMobRenderer<GuMasterEntity, PlayerModel<GuMasterEntity>> {

    private static final ResourceLocation TEX_MELEE =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/gu_master_melee.png");
    private static final ResourceLocation TEX_RANGED =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/gu_master_ranged.png");
    private static final ResourceLocation TEX_CONTROL =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/gu_master_control.png");
    private static final ResourceLocation TEX_SUPPORT =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/gu_master_support.png");
    private static final ResourceLocation TEX_RUSH =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/gu_master_rush.png");
    private static final ResourceLocation TEX_DEMONIC =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/gu_master_demonic.png");
    private static final ResourceLocation TEX_DEFAULT =
        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "textures/entity/gu_master.png");

    private static final Set<String> DEMONIC_PATHS = Set.of(
        "BLOOD", "KILL", "DARK", "POISON", "SOUL", "ENSLAVE", "BONE", "STEAL"
    );
    private static final Set<String> MELEE_PATHS = Set.of(
        "STRENGTH", "METAL", "BONE", "EARTH"
    );
    private static final Set<String> RANGED_PATHS = Set.of(
        "MOON", "STAR", "LIGHT", "FIRE", "ICE", "LIGHTNING"
    );
    private static final Set<String> CONTROL_PATHS = Set.of(
        "SOUL", "DARK", "SHADOW", "ILLUSION", "DREAM", "CHARM"
    );
    private static final Set<String> SUPPORT_PATHS = Set.of(
        "BLOOD", "POISON", "WOOD", "WATER"
    );
    private static final Set<String> RUSH_PATHS = Set.of(
        "WIND", "FLIGHT", "SWORD", "BLADE"
    );

    public GuMasterRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(GuMasterEntity entity) {
        String path = entity.getPrimaryPathName();
        if (path == null || path.isEmpty()) return TEX_DEFAULT;

        if (DEMONIC_PATHS.contains(path)) return TEX_DEMONIC;
        if (MELEE_PATHS.contains(path)) return TEX_MELEE;
        if (RANGED_PATHS.contains(path)) return TEX_RANGED;
        if (CONTROL_PATHS.contains(path)) return TEX_CONTROL;
        if (SUPPORT_PATHS.contains(path)) return TEX_SUPPORT;
        if (RUSH_PATHS.contains(path)) return TEX_RUSH;

        return TEX_DEFAULT;
    }
}
