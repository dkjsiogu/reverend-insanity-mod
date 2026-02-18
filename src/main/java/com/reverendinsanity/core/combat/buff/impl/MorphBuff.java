package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

// 变形蛊Buff：变化遁形——25%伤害减免、阻止怪物锁定，400tick
public class MorphBuff extends GuBuff {

    public MorphBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "morph_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {}

    @Override
    protected void onRemove(ServerPlayer player) {}

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount * 0.75f;
    }

    @Override
    public boolean preventMobTargeting(ServerPlayer player, LivingEntity attacker) {
        return true;
    }
}
