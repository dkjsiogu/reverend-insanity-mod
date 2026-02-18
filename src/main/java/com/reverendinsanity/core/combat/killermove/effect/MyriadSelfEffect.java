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
import java.util.List;

// 万我杀招效果：力道分身万千，5个分身同时攻击每个目标
public class MyriadSelfEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(16, 8, 16);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, area, e -> e != player && e.isAlive());

        float hitDamage = calculatedDamage * 0.15f;

        for (LivingEntity target : targets) {
            for (int i = 0; i < 5; i++) {
                target.hurt(player.damageSources().magic(), hitDamage);
                target.invulnerableTime = 0;

                double offsetX = (i - 2) * 0.8;
                double offsetZ = (i % 2 == 0 ? 0.5 : -0.5) * 0.8;
                VfxHelper.spawn(player, VfxType.IMPACT_BURST,
                    target.getX() + offsetX, target.getY() + 0.5 + i * 0.3, target.getZ() + offsetZ,
                    0f, 1f, 0f,
                    0xFFDAA520, 1.5f, 10 + i * 3);
            }
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 2.0f, 1.0f);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.8f, 1.3f);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.5f, 0.7f);
    }
}
