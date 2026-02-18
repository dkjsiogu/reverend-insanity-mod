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

// 万剑归宗杀招效果：大范围剑雨AOE，60%伤害+击退
public class MyriadSwordsReturnEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(12, 8, 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.60f);
            Vec3 knockDir = target.position().subtract(player.position()).normalize();
            target.setDeltaMovement(knockDir.x * 0.8, 0.3, knockDir.z * 0.8);
        }

        VfxHelper.spawn(player, VfxType.SKY_STRIKE,
            player.getX(), player.getY(), player.getZ(),
            0f, -1f, 0f,
            0xFFCCDDFF, 4.0f, 30);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 2.0f, 1.4f);
    }
}
