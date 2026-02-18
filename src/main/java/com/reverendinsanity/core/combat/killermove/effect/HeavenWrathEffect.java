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

// 天罚神威杀招效果：天道降下毁灭性神罚
public class HeavenWrathEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        double range = 14.0;

        VfxHelper.spawn(player, VfxType.SKY_STRIKE,
            player.getX(), player.getY(), player.getZ(),
            0f, -1f, 0f,
            0xFFFFEE00, 5.0f, 35);

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.70f);
            Vec3 kb = target.position().subtract(player.position()).normalize().scale(2.0);
            target.setDeltaMovement(kb.x, 0.8, kb.z);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0f, 0.2f);
    }
}
