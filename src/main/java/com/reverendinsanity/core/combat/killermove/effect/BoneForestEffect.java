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

// 白骨森林杀招效果：12格AOE骨刺丛生，60%伤害+减速
public class BoneForestEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(12, 6, 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        Vec3 center = player.position();
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.60f);
            Vec3 away = target.position().subtract(center).normalize().scale(0.3);
            target.setDeltaMovement(away.x, 0.2, away.z);
        }

        VfxHelper.spawn(player, VfxType.IMPACT_BURST,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFDDDDBB, 5.0f, 30);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0f, 0.3f);
    }
}
