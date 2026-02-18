package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 蛮力天牛蛊Buff：短暂爆发力——攻击力+8、击退+1.5、速度+30%，100tick
public class SavageBullBuff extends GuBuff {

    private static final ResourceLocation ATK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "savage_bull_atk");
    private static final ResourceLocation KB_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "savage_bull_kb");
    private static final ResourceLocation SPD_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "savage_bull_spd");

    public SavageBullBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "savage_bull_gu"), 100);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
            new AttributeModifier(ATK_MOD, 8.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.ATTACK_KNOCKBACK).addTransientModifier(
            new AttributeModifier(KB_MOD, 1.5, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
            new AttributeModifier(SPD_MOD, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATK_MOD);
        player.getAttribute(Attributes.ATTACK_KNOCKBACK).removeModifier(KB_MOD);
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPD_MOD);
    }
}
