package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

// 锁气Buff：减少被动仇恨范围（>10格无法锁定）
public class ScentLockBuff extends GuBuff {

    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_scent_lock");

    public ScentLockBuff() {
        super(ID, 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
    }

    @Override
    protected void onRemove(ServerPlayer player) {
    }

    @Override
    public boolean preventMobTargeting(ServerPlayer player, LivingEntity attacker) {
        return attacker.distanceTo(player) > 10.0;
    }
}
