package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.entity.BloodBoltEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

// 血雨冲天：向四面八方发射8道追踪血弹，以血为代价换取毁灭性攻击
public class BloodRainEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        float perBoltDamage = calculatedDamage / 4f;
        Vec3 look = player.getLookAngle();

        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2.0 / 8) * i;
            double dx = look.x * Math.cos(angle) - look.z * Math.sin(angle);
            double dy = look.y + (i % 2 == 0 ? 0.2 : -0.1);
            double dz = look.x * Math.sin(angle) + look.z * Math.cos(angle);

            BloodBoltEntity bolt = new BloodBoltEntity(player.level(), player, perBoltDamage);
            bolt.setPos(player.getX(), player.getEyeY(), player.getZ());
            bolt.shoot(dx, dy, dz, 0.6f, 1.0f);
            player.level().addFreshEntity(bolt);
        }

        player.hurt(player.damageSources().magic(), 6.0f);

        VfxHelper.spawn(player, VfxType.IMPACT_BURST,
            player.getX(), player.getY() + 1.0, player.getZ(),
            0f, 1f, 0f,
            0xFFCC0000, 3.0f, 25);
        VfxHelper.spawn(player, VfxType.RIPPLE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFAA0022, 4.0f, 30);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5f, 0.5f);
    }
}
