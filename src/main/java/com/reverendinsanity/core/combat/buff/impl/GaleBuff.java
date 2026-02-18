package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 疾风蛊Buff：极速疾风——速度+60%、跳跃+1.0、重力-50%、安全坠落+10，400tick
public class GaleBuff extends GuBuff {

    private static final ResourceLocation SPD_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gale_spd");
    private static final ResourceLocation JUMP_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gale_jump");
    private static final ResourceLocation GRAV_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gale_grav");
    private static final ResourceLocation FALL_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gale_fall");

    public GaleBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gale_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
            new AttributeModifier(SPD_MOD, 0.6, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        player.getAttribute(Attributes.JUMP_STRENGTH).addTransientModifier(
            new AttributeModifier(JUMP_MOD, 1.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.GRAVITY).addTransientModifier(
            new AttributeModifier(GRAV_MOD, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        player.getAttribute(Attributes.SAFE_FALL_DISTANCE).addTransientModifier(
            new AttributeModifier(FALL_MOD, 10.0, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPD_MOD);
        player.getAttribute(Attributes.JUMP_STRENGTH).removeModifier(JUMP_MOD);
        player.getAttribute(Attributes.GRAVITY).removeModifier(GRAV_MOD);
        player.getAttribute(Attributes.SAFE_FALL_DISTANCE).removeModifier(FALL_MOD);
    }
}
