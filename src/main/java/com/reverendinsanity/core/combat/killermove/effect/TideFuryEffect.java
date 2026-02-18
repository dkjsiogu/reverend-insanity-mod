package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

// 潮汐怒涛杀招效果：大范围水流冲击+巨力击退
public class TideFuryEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(10, 4, 10);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.45f);
            Vec3 away = target.position().subtract(player.position()).normalize();
            target.push(away.x * 2.5, 1.0, away.z * 2.5);
            target.hurtMarked = true;
        }

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF4488CC, 5.0f, 30);
        VfxHelper.spawn(player, VfxType.RIPPLE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF66AADD, 4.0f, 35);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 2.5f, 0.5f);
    }
}
