package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

// 雷霆万钧杀招效果：大范围多重雷击+额外魔法伤害
public class ThunderJudgmentEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(12, 6, 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        List<LivingEntity> closest = targets.stream()
                .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .limit(5)
                .toList();

        for (LivingEntity target : closest) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(player.level());
            if (bolt != null) {
                bolt.moveTo(target.getX(), target.getY(), target.getZ());
                bolt.setCause(player);
                player.level().addFreshEntity(bolt);
            }
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.5f);
        }

        VfxHelper.spawn(player, VfxType.SKY_STRIKE,
            player.getX(), player.getY(), player.getZ(),
            0f, -1f, 0f,
            0xFFFFFF88, 5.0f, 30);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 3.0f, 0.6f);
    }
}
