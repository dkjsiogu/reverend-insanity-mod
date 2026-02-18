package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.FrostManager;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

// 霜甲蛊Buff：被近战攻击时冻伤攻击者（减速60%持续40tick+1点魔法伤害），400tick
public class FrostArmorBuff extends GuBuff {

    public FrostArmorBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "frost_armor_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {}

    @Override
    protected void onRemove(ServerPlayer player) {}

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker && attacker != player) {
            FrostManager.applySlow(attacker, 40, 0.6);
            attacker.hurt(player.damageSources().magic(), 1.0f);
        }
        return amount;
    }
}
