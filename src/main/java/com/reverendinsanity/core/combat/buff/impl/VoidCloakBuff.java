package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

// 虚空隐匿蛊增益：隐入虚空，30%概率完全闪避伤害
public class VoidCloakBuff extends GuBuff {

    public VoidCloakBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "void_cloak_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {}

    @Override
    protected void onRemove(ServerPlayer player) {}

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (player.getRandom().nextFloat() < 0.3f) {
            return 0f;
        }
        return amount * 0.85f;
    }
}
