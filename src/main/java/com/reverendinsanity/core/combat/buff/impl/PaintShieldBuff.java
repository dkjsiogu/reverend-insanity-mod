package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

// 画盾蛊Buff：画道防御幻象
public class PaintShieldBuff extends GuBuff {

    public PaintShieldBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "paint_shield_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
    }

    @Override
    protected void onRemove(ServerPlayer player) {
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (player.getRandom().nextFloat() < 0.20f) {
            return 0f;
        }
        return amount * 0.85f;
    }
}
