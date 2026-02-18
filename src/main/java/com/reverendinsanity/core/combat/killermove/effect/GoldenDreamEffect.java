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

// 黄粱一梦杀招效果：大范围梦魇冻结+持续伤害
public class GoldenDreamEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(12, 6, 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.5f);
            target.setDeltaMovement(Vec3.ZERO);
            target.hurtMarked = true;
        }

        VfxHelper.spawn(player, VfxType.DOME_FIELD,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFDDAA55, 4.0f, 40);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 2.0f, 0.4f);
    }
}
