package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

// 清明梦蛊Buff：梦境护盾——20%伤害减免，每40tick恢复1HP，400tick
public class LucidDreamBuff extends GuBuff {

    public LucidDreamBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "lucid_dream_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
    }

    @Override
    protected void onRemove(ServerPlayer player) {
    }

    @Override
    protected void onTick(ServerPlayer player) {
        if (getRemainingTicks() % 40 == 0) {
            player.heal(1.0f);
        }
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount * 0.8f;
    }
}
