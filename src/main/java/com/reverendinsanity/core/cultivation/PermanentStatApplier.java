package com.reverendinsanity.core.cultivation;

import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 永久属性应用器：读取GuMasterData永久属性并通过AttributeModifier应用
public class PermanentStatApplier {

    private static final ResourceLocation PERM_STRENGTH = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "perm_strength");
    private static final ResourceLocation PERM_DEFENSE = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "perm_defense");
    private static final ResourceLocation PERM_SPEED = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "perm_speed");
    private static final ResourceLocation PERM_MAX_HEALTH = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "perm_max_health");

    public static void refresh(ServerPlayer player) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());

        applyAttribute(player, Attributes.ATTACK_DAMAGE, PERM_STRENGTH, data.getPermanentStat("strength"));
        applyAttribute(player, Attributes.ARMOR, PERM_DEFENSE, data.getPermanentStat("defense"));
        applyAttribute(player, Attributes.MOVEMENT_SPEED, PERM_SPEED, data.getPermanentStat("speed") * 0.01f);
        applyAttribute(player, Attributes.MAX_HEALTH, PERM_MAX_HEALTH, data.getPermanentStat("max_health"));
    }

    private static void applyAttribute(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                        ResourceLocation modId, float value) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(modId);
        if (value > 0) {
            instance.addTransientModifier(new AttributeModifier(modId, value, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
