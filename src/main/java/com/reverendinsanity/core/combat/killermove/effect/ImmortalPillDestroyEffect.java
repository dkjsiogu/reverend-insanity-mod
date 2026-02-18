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

// 仙丹灭世杀招效果：丹道仙力毁灭+恢复
public class ImmortalPillDestroyEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        double range = 12.0;

        VfxHelper.spawn(player, VfxType.HEAL_SPIRAL,
            player.getX(), player.getY() + 1, player.getZ(),
            0f, 1f, 0f,
            0xFF33FF33, 5.0f, 30);
        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFF44DD44, 5.0f, 30);

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        float totalHeal = 0f;
        for (LivingEntity target : targets) {
            float dealt = calculatedDamage * 0.55f;
            target.hurt(player.damageSources().magic(), dealt);
            totalHeal += dealt * 0.15f;
        }
        player.heal(Math.min(totalHeal, 25f));

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 2.0f, 0.3f);
    }
}
