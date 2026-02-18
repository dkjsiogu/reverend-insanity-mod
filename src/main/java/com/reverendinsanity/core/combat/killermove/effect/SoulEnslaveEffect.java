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

// 万奴归心杀招效果：奴道大范围控制
public class SoulEnslaveEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        double range = 12.0;

        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3;
            VfxHelper.spawn(player, VfxType.AURA_RING,
                player.getX() + Math.cos(angle) * 4, player.getY() + 0.5, player.getZ() + Math.sin(angle) * 4,
                0f, 1f, 0f,
                0xFF336633, 2.0f, 25);
        }
        VfxHelper.spawn(player, VfxType.DOME_FIELD,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF448844, 4.0f, 35);

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.55f);
            target.setDeltaMovement(target.getDeltaMovement().multiply(0.2, 1.0, 0.2));
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 2.0f, 0.3f);
    }
}
