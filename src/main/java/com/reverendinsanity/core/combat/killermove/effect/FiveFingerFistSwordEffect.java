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

// 五指拳心剑杀招效果：剑道前方16格60度锥形，五剑齐发各20%伤害
public class FiveFingerFistSwordEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        AABB area = new AABB(eye, eye.add(look.scale(16.0))).inflate(5.0);
        List<LivingEntity> candidates = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        Vec3 lookFlat = new Vec3(look.x, 0, look.z).normalize();
        double coneAngleCos = Math.cos(Math.toRadians(30));

        for (LivingEntity target : candidates) {
            Vec3 toTarget = target.position().subtract(player.position());
            Vec3 toTargetFlat = new Vec3(toTarget.x, 0, toTarget.z).normalize();
            double dot = lookFlat.dot(toTargetFlat);
            if (dot < coneAngleCos) continue;
            if (target.distanceTo(player) > 16.0f) continue;

            for (int i = 0; i < 5; i++) {
                target.hurt(player.damageSources().magic(), calculatedDamage * 0.20f);
                target.invulnerableTime = 0;

                double offsetX = (i - 2) * 0.4;
                double offsetY = (i % 2 == 0) ? 0.3 : -0.3;
                VfxHelper.spawn(player, VfxType.SLASH_ARC,
                    (float)(target.getX() + offsetX), (float)(target.getY() + 1.0 + offsetY), (float)target.getZ(),
                    (float) look.x, (float) look.y, (float) look.z,
                    0xFFCCCCFF, 2.0f, 10 + i * 3);
            }
        }

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 2.0f, 1.5f);
    }
}
