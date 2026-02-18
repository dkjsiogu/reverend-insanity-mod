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
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 圣光裁决杀招效果：大范围光爆+对亡灵额外伤害
public class SacredLightJudgmentEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(12, 6, 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            float dmg = calculatedDamage * 0.55f;
            if (target instanceof Zombie || target instanceof AbstractSkeleton) {
                dmg *= 1.5f;
            }
            target.hurt(player.damageSources().magic(), dmg);
            target.setRemainingFireTicks(80);
        }

        VfxHelper.spawn(player, VfxType.SKY_STRIKE,
            player.getX(), player.getY(), player.getZ(),
            0f, -1f, 0f,
            0xFFFFEECC, 5.0f, 35);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 2.0f, 0.6f);
    }
}
