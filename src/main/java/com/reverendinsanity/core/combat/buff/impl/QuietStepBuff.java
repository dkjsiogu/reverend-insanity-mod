package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

// 悄步Buff：远距离敌人无法锁定（>16格）
public class QuietStepBuff extends GuBuff {

    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_quiet_step");

    public QuietStepBuff() {
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
        return attacker.distanceTo(player) > 16.0;
    }
}
