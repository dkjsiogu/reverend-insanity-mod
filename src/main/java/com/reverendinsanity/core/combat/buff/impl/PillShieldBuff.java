package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

// 丹甲蛊Buff：丹道防御护体
public class PillShieldBuff extends GuBuff {

    public PillShieldBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "pill_shield_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.heal(4f);
    }

    @Override
    protected void onRemove(ServerPlayer player) {
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount * 0.80f;
    }

    @Override
    public void onTick(ServerPlayer player) {
        if (player.tickCount % 40 == 0) {
            player.heal(1f);
        }
    }
}
