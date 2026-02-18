package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.FrostManager;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

// 雷盾蛊Buff：电击反伤——被近战攻击时电击攻击者（2魔法伤害+减速50%持续40tick），400tick
public class ThunderShieldBuff extends GuBuff {

    public ThunderShieldBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "thunder_shield_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {}

    @Override
    protected void onRemove(ServerPlayer player) {}

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker && attacker != player) {
            attacker.hurt(player.damageSources().magic(), 2.0f);
            FrostManager.applySlow(attacker, 40, 0.5);
        }
        return amount;
    }
}
