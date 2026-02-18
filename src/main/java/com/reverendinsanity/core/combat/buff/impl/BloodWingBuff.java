package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.core.combat.buff.GuBuff;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.server.level.ServerPlayer;

// 血翼蛊增益：消耗HP获得飞行能力
public class BloodWingBuff extends GuBuff {

    private boolean wasFlying = false;

    public BloodWingBuff() {
        super(GuRegistry.id("blood_wing_gu"), 200);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        wasFlying = player.getAbilities().mayfly;
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = wasFlying;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    @Override
    protected void onTick(ServerPlayer player) {
        if (getRemainingTicks() % 20 == 0) {
            if (player.getHealth() > 2.0f) {
                player.hurt(player.damageSources().magic(), 1.0f);
            } else {
                remove(player);
            }
        }
    }
}
