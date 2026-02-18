package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 信号蛊技能：消耗型信号弹，高空烟火粒子+坐标广播
public class SignalGuAbility extends GuAbility {

    public SignalGuAbility() {
        super(GuRegistry.id("signal_gu"), 5f, 0, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuInstance guInst = aperture.findGuInstance(GuRegistry.id("signal_gu"));
        if (guInst != null) aperture.removeGu(guInst);

        ServerLevel serverLevel = (ServerLevel) player.level();
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        for (int i = 0; i < 30; i++) {
            double height = py + 60 + serverLevel.random.nextDouble() * 40;
            double ox = px + (serverLevel.random.nextDouble() - 0.5) * 8;
            double oz = pz + (serverLevel.random.nextDouble() - 0.5) * 8;
            serverLevel.sendParticles(ParticleTypes.FIREWORK, ox, height, oz,
                8, 2.0, 2.0, 2.0, 0.15);
        }

        for (int i = 0; i < 10; i++) {
            double height = py + 50 + i * 5;
            serverLevel.sendParticles(ParticleTypes.END_ROD, px, height, pz,
                5, 0.5, 0.5, 0.5, 0.02);
        }

        AABB broadcastArea = new AABB(px - 64, py - 64, pz - 64, px + 64, py + 64, pz + 64);
        List<Player> nearbyPlayers = player.level().getEntitiesOfClass(Player.class, broadcastArea);
        String coordMsg = String.format("方位信号！[%s] X:%d Y:%d Z:%d",
            player.getName().getString(), (int) px, (int) py, (int) pz);
        for (Player nearby : nearbyPlayers) {
            nearby.sendSystemMessage(Component.literal(coordMsg));
        }

        player.level().playSound(null, px, py, pz,
            SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 3.0f, 0.5f);
    }
}
