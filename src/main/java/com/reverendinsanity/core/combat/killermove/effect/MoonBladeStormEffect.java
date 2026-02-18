package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.entity.MoonBladeEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

// 月刃风暴：扇形发射5道月刃
public class MoonBladeStormEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        float perBladeDamage = calculatedDamage / 3f;
        Vec3 look = player.getLookAngle();
        for (int i = -2; i <= 2; i++) {
            float angle = i * 15f;
            double radians = Math.toRadians(angle);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double newX = look.x * cos - look.z * sin;
            double newZ = look.x * sin + look.z * cos;
            MoonBladeEntity blade = new MoonBladeEntity(player.level(), player, perBladeDamage);
            blade.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            blade.shoot(newX, look.y, newZ, 1.5f, 0.0f);
            player.level().addFreshEntity(blade);
        }

        VfxHelper.spawn(player, VfxType.SLASH_ARC, 0xFF4488FF, 4.0f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 1.5f, 0.8f);
    }
}
