package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 土壁蛊Buff：厚重大地防御——护甲+10、韧性+6、击退抗性+0.8，500tick
public class EarthWallBuff extends GuBuff {

    private static final ResourceLocation ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "earth_wall_armor");
    private static final ResourceLocation TOUGH_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "earth_wall_tough");
    private static final ResourceLocation KBR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "earth_wall_kbr");

    public EarthWallBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "earth_wall_gu"), 500);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).addTransientModifier(
            new AttributeModifier(ARMOR_MOD, 10.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).addTransientModifier(
            new AttributeModifier(TOUGH_MOD, 6.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addTransientModifier(
            new AttributeModifier(KBR_MOD, 0.8, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MOD);
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).removeModifier(TOUGH_MOD);
        player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(KBR_MOD);
    }
}
