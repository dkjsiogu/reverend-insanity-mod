package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 巨力蛊Buff：持续力量增强——攻击力+6、击退+1.0、击退抗性+0.5，600tick
public class GiantStrengthBuff extends GuBuff {

    private static final ResourceLocation ATK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "giant_str_atk");
    private static final ResourceLocation KB_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "giant_str_kb");
    private static final ResourceLocation KBR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "giant_str_kbr");

    public GiantStrengthBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "giant_strength_gu"), 600);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
            new AttributeModifier(ATK_MOD, 6.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.ATTACK_KNOCKBACK).addTransientModifier(
            new AttributeModifier(KB_MOD, 1.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addTransientModifier(
            new AttributeModifier(KBR_MOD, 0.5, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATK_MOD);
        player.getAttribute(Attributes.ATTACK_KNOCKBACK).removeModifier(KB_MOD);
        player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(KBR_MOD);
    }
}
