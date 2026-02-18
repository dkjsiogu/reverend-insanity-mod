package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 清风蛊Buff：轻盈之风——速度+40%、跳跃+0.5，300tick
public class BreezeBuff extends GuBuff {

    private static final ResourceLocation SPD_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "breeze_spd");
    private static final ResourceLocation JUMP_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "breeze_jump");

    public BreezeBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "breeze_gu"), 300);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
            new AttributeModifier(SPD_MOD, 0.4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        player.getAttribute(Attributes.JUMP_STRENGTH).addTransientModifier(
            new AttributeModifier(JUMP_MOD, 0.5, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPD_MOD);
        player.getAttribute(Attributes.JUMP_STRENGTH).removeModifier(JUMP_MOD);
    }
}
