package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

// 太极蛊Buff：太极反转——反弹25%伤害，400tick
public class TaiChiBuff extends GuBuff {

    public TaiChiBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "tai_chi_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {}

    @Override
    protected void onRemove(ServerPlayer player) {}

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker && attacker.isAlive()) {
            attacker.hurt(player.damageSources().thorns(player), amount * 0.25f);
        }
        return amount * 0.90f;
    }
}
