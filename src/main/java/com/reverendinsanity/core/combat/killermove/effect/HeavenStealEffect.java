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

// 偷天杀招效果：窃取生命精华
public class HeavenStealEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        double range = 12.0;

        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3;
            VfxHelper.spawn(player, VfxType.SHADOW_FADE,
                player.getX() + Math.cos(angle) * 4, player.getY() + 0.5, player.getZ() + Math.sin(angle) * 4,
                (float) -Math.cos(angle), 0.2f, (float) -Math.sin(angle),
                0xFF553377, 3.5f, 25);
        }
        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFF775599, 5.0f, 30);

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        float totalHeal = 0f;
        for (LivingEntity target : targets) {
            float dealt = calculatedDamage * 0.60f;
            target.hurt(player.damageSources().magic(), dealt);
            totalHeal += dealt * 0.15f;
        }
        player.heal(Math.min(totalHeal, 25f));

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 2.0f, 0.2f);
    }
}
