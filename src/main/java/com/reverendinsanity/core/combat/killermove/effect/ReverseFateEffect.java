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
import java.util.Comparator;
import java.util.List;

// 逆天改命杀招效果：锁定最强敌人发动因果打击，80%伤害集中于单体
public class ReverseFateEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(15, 8, 15);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        targets.stream()
            .max(Comparator.comparingDouble(LivingEntity::getMaxHealth))
            .ifPresent(target -> {
                target.hurt(player.damageSources().magic(), calculatedDamage * 0.80f);
                target.setDeltaMovement(0, 1.0, 0);
                player.heal(calculatedDamage * 0.15f);
            });

        VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
            player.getX(), player.getY() + 1.5, player.getZ(),
            (float) player.getLookAngle().x, (float) player.getLookAngle().y, (float) player.getLookAngle().z,
            0xFFDD99FF, 4.0f, 30);
        for (LivingEntity target : targets) {
            if (target.getHealth() > 0) {
                target.hurt(player.damageSources().magic(), calculatedDamage * 0.15f);
            }
        }

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0f, 0.8f);
    }
}
