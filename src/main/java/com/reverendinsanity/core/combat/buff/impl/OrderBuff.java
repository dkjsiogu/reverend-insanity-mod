package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

// 秩序蛊增益：律道秩序之力，减伤+反击减速
public class OrderBuff extends GuBuff {

    public OrderBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "order_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {}

    @Override
    protected void onRemove(ServerPlayer player) {}

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker && attacker.isAlive()) {
            attacker.setDeltaMovement(0, 0, 0);
        }
        return amount * 0.80f;
    }
}
