package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

// 幽影随行Buff：隐身+敌人无法锁定+烟雾粒子
public class ShadowFollowerBuff extends GuBuff {

    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_shadow_follower");

    public ShadowFollowerBuff() {
        super(ID, 200);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.setInvisible(true);
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.setInvisible(false);
    }

    @Override
    public boolean preventMobTargeting(ServerPlayer player, LivingEntity attacker) {
        return true;
    }

    @Override
    protected void onTick(ServerPlayer player) {
        if (player.level() instanceof ServerLevel serverLevel && player.tickCount % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                player.getX(), player.getY() + 0.5, player.getZ(),
                2, 0.3, 0.3, 0.3, 0.01);
        }
    }
}
