package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 熊力增益：Attribute增加攻击力和击退力
public class BearStrengthBuff extends GuBuff {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_bear_strength");
    private static final ResourceLocation ATTACK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "bear_strength_attack");
    private static final ResourceLocation KNOCKBACK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "bear_strength_knockback");

    public BearStrengthBuff() {
        super(ID, 300);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
            new AttributeModifier(ATTACK_MOD, 4.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.ATTACK_KNOCKBACK).addTransientModifier(
            new AttributeModifier(KNOCKBACK_MOD, 0.5, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_MOD);
        player.getAttribute(Attributes.ATTACK_KNOCKBACK).removeModifier(KNOCKBACK_MOD);
    }
}
