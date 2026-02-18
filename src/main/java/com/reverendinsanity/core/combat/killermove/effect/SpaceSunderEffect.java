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

// 空间碎裂杀招效果：撕裂空间造成范围伤害
public class SpaceSunderEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        double range = 12.0;

        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            double rx = Math.cos(angle) * range * 0.5;
            double rz = Math.sin(angle) * range * 0.5;
            VfxHelper.spawn(player, VfxType.RIPPLE,
                player.getX() + rx, player.getY() + 1, player.getZ() + rz,
                0f, 1f, 0f,
                0xFFAA55FF, 3.0f, 25);
        }
        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY() + 1, player.getZ(),
            0f, 1f, 0f,
            0xFF8833DD, 4.0f, 30);

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.60f);
            Vec3 kb = target.position().subtract(player.position()).normalize().scale(1.5);
            target.setDeltaMovement(kb.x, 0.6, kb.z);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 2.0f, 0.3f);
    }
}
