package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.entity.MoonBladeEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

// 银月暴风杀招效果：以玩家为中心360度发射12道月刃
public class SilverMoonTempestEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        Vec3 look = player.getLookAngle();

        for (int i = 0; i < 12; i++) {
            float angle = i * 30f;
            double radians = Math.toRadians(angle);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double dirX = look.x * cos - look.z * sin;
            double dirZ = look.x * sin + look.z * cos;
            float bladeDmg = calculatedDamage * 0.4f;
            MoonBladeEntity blade = new MoonBladeEntity(player.level(), player, bladeDmg);
            blade.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            blade.shoot(dirX, 0.0, dirZ, 2.5f, 0.0f);
            player.level().addFreshEntity(blade);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 100; i++) {
                double ox = (player.getRandom().nextDouble() - 0.5) * 6.0;
                double oy = player.getRandom().nextDouble() * 3.0;
                double oz = (player.getRandom().nextDouble() - 0.5) * 6.0;
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                    1, 0.0, 0.1, 0.0, 0.08);
            }
        }

        VfxHelper.spawn(player, VfxType.SLASH_ARC, 0xFFCCDDFF, 5.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WITHER_BREAK_BLOCK, SoundSource.PLAYERS, 1.0f, 0.5f);
    }
}
