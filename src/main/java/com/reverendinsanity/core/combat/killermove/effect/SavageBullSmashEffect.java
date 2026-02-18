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

// 蛮牛碎地：超强AOE地面砸击，大范围击飞+伤害
public class SavageBullSmashEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        Vec3 center = player.position().add(player.getLookAngle().scale(3.0));
        AABB area = AABB.ofSize(center, 10.0, 4.0, 10.0);

        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        float perTargetDamage = calculatedDamage * 0.6f;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), perTargetDamage);
            Vec3 knockDir = target.position().subtract(center).normalize();
            target.setDeltaMovement(knockDir.x * 1.5, 0.9, knockDir.z * 1.5);
            target.hurtMarked = true;
        }

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            (float) center.x, (float) center.y, (float) center.z,
            0f, 1f, 0f,
            0xFFAA5500, 5.0f, 30);
        player.level().playSound(null, center.x, center.y, center.z,
            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5f, 0.4f);
    }
}
