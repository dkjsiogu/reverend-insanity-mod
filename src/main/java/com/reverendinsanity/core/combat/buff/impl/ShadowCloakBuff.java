package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

// 暗影斗篷蛊Buff：暗影隐匿——30%伤害减免、阻止怪物主动锁定，400tick
public class ShadowCloakBuff extends GuBuff {

    public ShadowCloakBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "shadow_cloak_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
    }

    @Override
    protected void onRemove(ServerPlayer player) {
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount * 0.7f;
    }

    @Override
    public boolean preventMobTargeting(ServerPlayer player, net.minecraft.world.entity.LivingEntity attacker) {
        return true;
    }
}
