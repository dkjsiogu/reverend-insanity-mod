package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.FrostManager;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 石牢困阵：大范围石化+伤害
public class StonePrisonEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(7.0, 3.0, 7.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        float perTargetDamage = calculatedDamage * 0.35f;
        for (LivingEntity target : targets) {
            FrostManager.applySlow(target, 120, 0.95);
            target.hurt(player.damageSources().magic(), perTargetDamage);
        }

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF887755, 5.0f, 40);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.5f, 0.3f);
    }
}
