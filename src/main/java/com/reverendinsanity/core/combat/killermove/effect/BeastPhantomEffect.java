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

// 兽影杀招效果：力道三倍兽影分身碾压，10格AOE 80%伤害+兽影践踏25%+击飞
public class BeastPhantomEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(10, 5, 10);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        Vec3 center = player.position();
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.80f);
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.25f);
            Vec3 away = target.position().subtract(center).normalize();
            target.setDeltaMovement(away.x * 1.2, 0.8, away.z * 1.2);
            target.hurtMarked = true;
        }

        VfxHelper.spawn(player, VfxType.IMPACT_BURST,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF8B4513, 4.5f, 30);
        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFFDAA520, 5.0f, 25);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 2.0f, 0.6f);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5f, 0.5f);
    }
}
