package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.FrostManager;
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

// 冰刃风暴杀招效果：冰道+风道融合，12格AOE 50%伤害+冻结+风暴中心额外30%
public class IceBladeStormEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(12, 6, 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        LivingEntity closest = targets.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .orElse(null);

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.50f);
            FrostManager.applyFreeze(target, 80);
        }

        if (closest != null) {
            closest.hurt(player.damageSources().magic(), calculatedDamage * 0.30f);
        }

        VfxHelper.spawn(player, VfxType.TORNADO,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF88CCFF, 4.5f, 35);
        VfxHelper.spawn(player, VfxType.DOME_FIELD,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFE0E8FF, 5.0f, 30);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0f, 0.4f);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5f, 1.2f);
    }
}
