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
import java.util.List;

// 信息轰炸杀招效果：信道大范围精神冲击
public class HeavenInfoEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        double range = 14.0;

        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
                player.getX(), player.getY() + 1, player.getZ(),
                (float) Math.cos(angle), 0.3f, (float) Math.sin(angle),
                0xFF22AAFF, 3.0f, 25);
        }
        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFF44CCFF, 6.0f, 30);

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.65f);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDER_EYE_LAUNCH, SoundSource.PLAYERS, 2.0f, 0.3f);
    }
}
