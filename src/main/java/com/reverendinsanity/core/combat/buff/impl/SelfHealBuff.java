package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.core.combat.buff.GuBuff;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

// 自愈蛊增益：受伤后30%伤害转化为6秒缓慢回血
public class SelfHealBuff extends GuBuff {

    private float storedHeal = 0f;
    private int healTicksRemaining = 0;

    public SelfHealBuff() {
        super(GuRegistry.id("self_heal_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {}

    @Override
    protected void onRemove(ServerPlayer player) {
        storedHeal = 0f;
        healTicksRemaining = 0;
    }

    @Override
    protected void onTick(ServerPlayer player) {
        if (storedHeal > 0 && healTicksRemaining > 0) {
            float healPerTick = storedHeal / healTicksRemaining;
            player.heal(healPerTick);
            storedHeal -= healPerTick;
            healTicksRemaining--;
            if (healTicksRemaining <= 0) storedHeal = 0;
        }
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        float healAmount = amount * 0.3f;
        storedHeal += healAmount;
        healTicksRemaining = 120;
        return amount;
    }
}
