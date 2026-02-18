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

// 万法皆知杀招效果：智慧爆发，精神冲击范围敌人
public class OmniscienceEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        double range = 12.0;

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY() + 1, player.getZ(),
            0f, 1f, 0f,
            0xFF44CCFF, 4.0f, 30);
        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFF2299DD, 5.0f, 25);

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.60f);
        }

        float healAmount = calculatedDamage * 0.10f;
        player.heal(Math.min(healAmount, 15f));

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0f, 1.5f);
    }
}
