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

// 月痕连斩杀招效果：7道月刃扇形发射
public class MoonscarChainSlashEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        Vec3 look = player.getLookAngle();

        float[] angles = {-30f, -20f, -10f, 0f, 10f, 20f, 30f};
        for (float angle : angles) {
            double radians = Math.toRadians(angle);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double newX = look.x * cos - look.z * sin;
            double newZ = look.x * sin + look.z * cos;
            float bladeDmg = calculatedDamage * 0.6f;
            MoonBladeEntity blade = new MoonBladeEntity(player.level(), player, bladeDmg);
            blade.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            blade.shoot(newX, look.y, newZ, 2.0f, 0.0f);
            player.level().addFreshEntity(blade);
        }

        VfxHelper.spawn(player, VfxType.SLASH_ARC, 0xFF88AAFF, 4.5f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8f, 1.2f);
    }
}
