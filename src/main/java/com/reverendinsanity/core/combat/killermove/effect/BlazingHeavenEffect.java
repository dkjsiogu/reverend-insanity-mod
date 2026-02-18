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

// 烈焰焚天：前方扇形大范围火焰覆盖，引燃+伤害
public class BlazingHeavenEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        Vec3 look = player.getLookAngle();
        Vec3 center = player.position().add(look.scale(4.0));
        AABB area = AABB.ofSize(center, 12.0, 4.0, 12.0);

        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        float perTargetDamage = calculatedDamage * 0.5f;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), perTargetDamage);
            target.igniteForTicks(200);
        }

        VfxHelper.spawn(player, VfxType.SKY_STRIKE,
            (float) center.x, player.getY(), (float) center.z,
            0f, -1f, 0f,
            0xFFFF4400, 4.0f, 35);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5f, 0.8f);
    }
}
