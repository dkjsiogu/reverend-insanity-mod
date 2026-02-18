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

// 斩杀无赦杀招效果：大范围斩杀，对低血量目标额外伤害，70%基础伤害
public class ExecuteNoMercyEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(12, 6, 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            float dmg = calculatedDamage * 0.70f;
            float healthRatio = target.getHealth() / target.getMaxHealth();
            if (healthRatio < 0.3f) {
                dmg *= 2.0f;
            } else if (healthRatio < 0.5f) {
                dmg *= 1.5f;
            }
            target.hurt(player.damageSources().magic(), dmg);
        }

        VfxHelper.spawn(player, VfxType.SLASH_ARC,
            player.getX(), player.getY() + 1.0, player.getZ(),
            (float) player.getLookAngle().x, 0f, (float) player.getLookAngle().z,
            0xFFCC2233, 5.0f, 25);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0f, 0.7f);
    }
}
