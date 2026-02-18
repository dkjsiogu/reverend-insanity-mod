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
import net.minecraft.world.phys.Vec3;
import java.util.List;

// 云劫杀招效果：云暴天劫覆压一切
public class CloudCalamityEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        double range = 14.0;

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY() + 4, player.getZ(),
            0f, -1f, 0f,
            0xFFBBBBCC, 6.0f, 30);
        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFFDDDDEE, 5.0f, 30);

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.60f);
            Vec3 vel = target.getDeltaMovement();
            target.setDeltaMovement(vel.x * 0.15, -1.0, vel.z * 0.15);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0f, 0.3f);
    }
}
