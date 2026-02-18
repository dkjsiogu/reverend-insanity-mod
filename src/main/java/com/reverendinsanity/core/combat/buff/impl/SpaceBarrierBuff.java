package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 空间壁障蛊增益：空间扭曲屏障，减少25%伤害+提升移速
public class SpaceBarrierBuff extends GuBuff {

    private static final ResourceLocation SPEED_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "space_barrier_speed");

    public SpaceBarrierBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "space_barrier_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
            new AttributeModifier(SPEED_MOD, 0.02, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount * 0.75f;
    }
}
