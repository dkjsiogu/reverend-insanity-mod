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

// 醉仙乱舞杀招效果：食道狂暴攻击并大量回复
public class DrunkenFrenzyEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        double range = 10.0;

        VfxHelper.spawn(player, VfxType.HEAL_SPIRAL,
            player.getX(), player.getY() + 1, player.getZ(),
            0f, 1f, 0f,
            0xFF88CC44, 4.0f, 30);
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2;
            VfxHelper.spawn(player, VfxType.RIPPLE,
                player.getX() + Math.cos(angle) * 3, player.getY() + 0.5, player.getZ() + Math.sin(angle) * 3,
                (float) -Math.cos(angle), 0.3f, (float) -Math.sin(angle),
                0xFFAADD66, 2.5f, 20);
        }

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        float totalHeal = 0f;
        for (LivingEntity target : targets) {
            float dealt = calculatedDamage * 0.50f;
            target.hurt(player.damageSources().magic(), dealt);
            totalHeal += dealt * 0.20f;
        }
        player.heal(Math.min(totalHeal, 30f));

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 2.0f, 0.5f);
    }
}
