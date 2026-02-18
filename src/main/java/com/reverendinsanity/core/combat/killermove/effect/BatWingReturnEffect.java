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

// 返实蝠翼杀招效果：风道蝠翼回旋切割，前冲8格后回旋拉拽
public class BatWingReturnEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.position();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();

        AABB pathArea = player.getBoundingBox().inflate(10, 4, 10);
        List<LivingEntity> pathTargets = player.level().getEntitiesOfClass(
            LivingEntity.class, pathArea, e -> e != player && e.isAlive());

        for (LivingEntity target : pathTargets) {
            Vec3 toTarget = target.position().subtract(origin);
            double forward = toTarget.dot(look);
            double lateral = Math.abs(toTarget.dot(right));

            if (forward > 0 && forward <= 8.0 && lateral <= 2.0) {
                target.hurt(player.damageSources().magic(), calculatedDamage * 0.50f);
            }
        }

        for (int i = 0; i < 3; i++) {
            double dist = 2.5 * (i + 1);
            VfxHelper.spawn(player, VfxType.SLASH_ARC,
                origin.x + look.x * dist, origin.y + 1, origin.z + look.z * dist,
                (float) look.x, 0f, (float) look.z,
                0xFF663399, 2.0f, 15 + i * 3);
        }

        Vec3 dest = origin.add(look.scale(8.0));
        player.teleportTo(dest.x, dest.y, dest.z);
        player.fallDistance = 0;

        AABB returnArea = player.getBoundingBox().inflate(8, 4, 8);
        List<LivingEntity> returnTargets = player.level().getEntitiesOfClass(
            LivingEntity.class, returnArea, e -> e != player && e.isAlive());

        Vec3 playerPos = player.position();
        for (LivingEntity target : returnTargets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.30f);
            Vec3 pull = playerPos.subtract(target.position()).normalize().scale(0.8);
            target.setDeltaMovement(pull.x, 0.3, pull.z);
        }

        VfxHelper.spawn(player, VfxType.TORNADO,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF998877, 3.5f, 30);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PHANTOM_BITE, SoundSource.PLAYERS, 2.0f, 0.8f);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5f, 1.2f);
    }
}
