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
import java.util.Comparator;
import java.util.List;

// 太古光拳杀招效果：光道高阶单体120%伤害+击退+6格余波40%
public class PrimordialLightFistEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB searchArea = player.getBoundingBox().inflate(12, 6, 12);
        List<LivingEntity> allTargets = player.level().getEntitiesOfClass(LivingEntity.class, searchArea,
                e -> e != player && e.isAlive());

        LivingEntity primary = allTargets.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .orElse(null);

        if (primary != null) {
            primary.hurt(player.damageSources().magic(), calculatedDamage * 1.20f);
            Vec3 knockDir = primary.position().subtract(player.position()).normalize();
            primary.setDeltaMovement(knockDir.x * 0.8, 1.2, knockDir.z * 0.8);
            primary.hurtMarked = true;

            AABB splashArea = primary.getBoundingBox().inflate(6, 3, 6);
            List<LivingEntity> splashTargets = player.level().getEntitiesOfClass(LivingEntity.class, splashArea,
                    e -> e != player && e != primary && e.isAlive());
            for (LivingEntity splash : splashTargets) {
                splash.hurt(player.damageSources().magic(), calculatedDamage * 0.40f);
            }

            VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
                player.getX(), player.getY() + 1.0, player.getZ(),
                (float) knockDir.x, (float) knockDir.y, (float) knockDir.z,
                0xFFFFEE88, 4.0f, 25);
            VfxHelper.spawn(player, VfxType.GLOW_BURST,
                primary.getX(), primary.getY() + 1.0, primary.getZ(),
                0f, 1f, 0f,
                0xFFFFFFCC, 3.5f, 20);
        }

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.0f, 0.8f);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5f, 1.2f);
    }
}
