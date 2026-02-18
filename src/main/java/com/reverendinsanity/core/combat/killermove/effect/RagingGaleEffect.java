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

// 狂风怒号杀招效果：大范围风暴击飞+伤害
public class RagingGaleEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(8, 4, 8);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.4f);
            Vec3 away = target.position().subtract(player.position()).normalize();
            target.push(away.x * 2.0, 1.5, away.z * 2.0);
            target.hurtMarked = true;
        }

        VfxHelper.spawn(player, VfxType.TORNADO,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF88DDAA, 3.0f, 35);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 2.0f, 0.8f);
    }
}
