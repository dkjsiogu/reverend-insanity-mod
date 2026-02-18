package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.IntelligenceManager;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

// 顺风耳蛊Buff：扩展注视侦察范围从32格到64格，持续300tick
public class KeenEarBuff extends GuBuff {

    public KeenEarBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_keen_ear_gu"), 300);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        IntelligenceManager.setKeenEarBonus(player, 300);
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        IntelligenceManager.setKeenEarBonus(player, 0);
    }

    @Override
    protected void onTick(ServerPlayer player) {
        IntelligenceManager.setKeenEarBonus(player, getRemainingTicks());
    }
}
