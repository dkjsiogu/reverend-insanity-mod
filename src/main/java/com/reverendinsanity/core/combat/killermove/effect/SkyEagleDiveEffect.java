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

// 天鹰击杀招效果：高速冲刺12格+落地AOE，55%伤害
public class SkyEagleDiveEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        Vec3 look = player.getLookAngle();
        Vec3 dest = player.position().add(look.scale(12.0));
        player.teleportTo(dest.x, dest.y, dest.z);
        player.fallDistance = 0;

        AABB area = player.getBoundingBox().inflate(8, 4, 8);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        Vec3 center = player.position();
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.55f);
            Vec3 away = target.position().subtract(center).normalize().scale(1.5);
            target.setDeltaMovement(away.x, 0.6, away.z);
        }

        VfxHelper.spawn(player, VfxType.IMPACT_BURST,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF88CCAA, 4.0f, 25);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 2.0f, 0.7f);
    }
}
