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

// 黑暗侵蚀杀招效果：大范围暗影侵蚀+生命汲取
public class DarkErosionEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(10, 5, 10);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        float totalDrained = 0f;
        for (LivingEntity target : targets) {
            float dmg = calculatedDamage * 0.5f;
            target.hurt(player.damageSources().magic(), dmg);
            totalDrained += dmg * 0.2f;
        }

        if (totalDrained > 0) {
            player.heal(Math.min(totalDrained, 20f));
        }

        VfxHelper.spawn(player, VfxType.DOME_FIELD,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF331144, 4.0f, 40);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5f, 0.4f);
    }
}
