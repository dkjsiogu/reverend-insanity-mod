package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 金甲蛊Buff：金光护体——+6护甲+2韧性，400tick
public class GoldenArmorBuff extends GuBuff {

    private static final ResourceLocation ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "golden_armor_armor");
    private static final ResourceLocation TOUGH_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "golden_armor_tough");

    public GoldenArmorBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "golden_armor_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).addTransientModifier(
            new AttributeModifier(ARMOR_MOD, 6.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).addTransientModifier(
            new AttributeModifier(TOUGH_MOD, 2.0, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MOD);
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).removeModifier(TOUGH_MOD);
    }
}
