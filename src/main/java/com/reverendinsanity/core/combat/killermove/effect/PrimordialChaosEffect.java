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

// 混元归一杀招效果：14格AOE阴阳大爆发，65%伤害+强击退
public class PrimordialChaosEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(14, 7, 14);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        Vec3 center = player.position();
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.65f);
            Vec3 away = target.position().subtract(center).normalize().scale(2.0);
            target.setDeltaMovement(away.x, 0.8, away.z);
        }

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY() + 1.5, player.getZ(),
            0f, 1f, 0f,
            0xFFDDCC88, 5.0f, 25);
        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF332244, 6.0f, 30);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.5f, 0.5f);
    }
}
