package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

// 信息网蛊Buff：信道增伤洞察
public class InfoNetBuff extends GuBuff {

    public InfoNetBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "info_net_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
    }

    @Override
    protected void onRemove(ServerPlayer player) {
    }

    @Override
    public float modifyOutgoingDamage(ServerPlayer player, LivingEntity target, float amount) {
        return amount * 1.20f;
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount * 0.90f;
    }
}
