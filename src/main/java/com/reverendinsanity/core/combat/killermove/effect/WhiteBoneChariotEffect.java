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

// 白骨战车杀招效果：骨道冲锋碾压，前方直线12格范围粉碎
public class WhiteBoneChariotEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.position();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();

        AABB area = player.getBoundingBox().inflate(14, 4, 14);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, area, e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().subtract(origin);
            double forward = toTarget.dot(look);
            double lateral = Math.abs(toTarget.dot(right));

            if (forward > 0 && forward <= 12.0 && lateral <= 2.0) {
                target.hurt(player.damageSources().magic(), calculatedDamage * 0.70f);
                Vec3 knockDir = right.scale(toTarget.dot(right) > 0 ? 1 : -1);
                target.setDeltaMovement(knockDir.x * 0.6, 0.8, knockDir.z * 0.6);
            }
        }

        for (int i = 0; i < 4; i++) {
            double dist = 3.0 * (i + 1);
            VfxHelper.spawn(player, VfxType.PULSE_WAVE,
                origin.x + look.x * dist, origin.y + 0.5, origin.z + look.z * dist,
                (float) look.x, 0f, (float) look.z,
                0xFFEEDDCC, 2.5f, 20 + i * 3);
        }
        VfxHelper.spawn(player, VfxType.IMPACT_BURST,
            origin.x + look.x * 12, origin.y + 1, origin.z + look.z * 12,
            0f, 1f, 0f,
            0xFFFFFFEE, 3.0f, 25);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.0f, 0.4f);
        player.level().playSound(null, origin.x + look.x * 6, origin.y, origin.z + look.z * 6,
            SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0f, 0.5f);
    }
}
