package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 飞翼蛊Buff：凌空飞翼——+40%移速+减免30%摔落伤害，400tick
public class FlightWingBuff extends GuBuff {

    private static final ResourceLocation SPD_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "flight_wing_spd");

    public FlightWingBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "flight_wing_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
            new AttributeModifier(SPD_MOD, 0.4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPD_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (source == player.damageSources().fall()) {
            return amount * 0.3f;
        }
        return amount;
    }
}
