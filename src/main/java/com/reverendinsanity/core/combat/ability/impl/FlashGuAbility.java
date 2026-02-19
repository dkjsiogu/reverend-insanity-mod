package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.core.combat.FlashBlindManager;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 闪光蛊技能：消耗型AoE致盲+破隐——削减索敌范围+清除隐身+停止导航
public class FlashGuAbility extends GuAbility {

    public FlashGuAbility() {
        super(GuRegistry.id("flash_gu"), 8f, 0, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuInstance guInst = aperture.findGuInstance(GuRegistry.id("flash_gu"));
        if (guInst != null) aperture.removeGu(guInst);

        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();
        ServerLevel serverLevel = (ServerLevel) player.level();

        AABB area = new AABB(px - 8, py - 4, pz - 8, px + 8, py + 4, pz + 8);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            FlashBlindManager.apply(target, 60);
        }

        for (int i = 0; i < 40; i++) {
            double ox = px + (serverLevel.random.nextDouble() - 0.5) * 10;
            double oy = py + serverLevel.random.nextDouble() * 3;
            double oz = pz + (serverLevel.random.nextDouble() - 0.5) * 10;
            serverLevel.sendParticles(ParticleTypes.END_ROD, ox, oy, oz,
                3, 0.2, 0.2, 0.2, 0.05);
        }

        player.level().playSound(null, px, py, pz,
            SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 2.0f, 1.5f);
    }
}
