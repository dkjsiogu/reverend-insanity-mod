package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

// 妖媚蛊增益：魅惑之力使敌人难以锁定自身
public class BewitchBuff extends GuBuff {

    public BewitchBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "bewitch_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {}

    @Override
    protected void onRemove(ServerPlayer player) {}

    @Override
    public boolean preventMobTargeting(ServerPlayer player, LivingEntity attacker) {
        return player.getRandom().nextFloat() < 0.6f;
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return amount * 0.85f;
    }
}
