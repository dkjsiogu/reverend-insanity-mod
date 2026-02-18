package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

// 炎铠蛊Buff：火焰免疫+被近战攻击时烧伤攻击者2点火焰伤害+点燃3秒，400tick
public class FlameArmorBuff extends GuBuff {

    public FlameArmorBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "flame_armor_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {}

    @Override
    protected void onRemove(ServerPlayer player) {}

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            return 0f;
        }
        if (source.getEntity() instanceof LivingEntity attacker && attacker != player) {
            attacker.hurt(player.damageSources().magic(), 2.0f);
            attacker.igniteForTicks(60);
        }
        return amount;
    }
}
