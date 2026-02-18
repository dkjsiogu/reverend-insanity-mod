package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

// 守魂蛊Buff：魂甲——25%全伤害减免，400tick
public class SoulShieldBuff extends GuBuff {

    public SoulShieldBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "soul_shield_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {}

    @Override
    protected void onRemove(ServerPlayer player) {}

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount * 0.75f;
    }
}
