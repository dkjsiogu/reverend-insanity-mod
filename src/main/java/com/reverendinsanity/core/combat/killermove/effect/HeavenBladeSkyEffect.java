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

// 天刀裂空杀招效果：前方扇形巨大劈斩，65%伤害+强力击飞
public class HeavenBladeSkyEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        AABB area = new AABB(eye, eye.add(look.scale(14.0))).inflate(4.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.65f);
            Vec3 knockDir = target.position().subtract(player.position()).normalize();
            target.setDeltaMovement(knockDir.x * 1.5, 0.8, knockDir.z * 1.5);
        }

        VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
            player.getX(), player.getY() + 1.5, player.getZ(),
            (float) look.x, (float) look.y, (float) look.z,
            0xFFDDEEFF, 5.0f, 30);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 2.0f, 0.5f);
    }
}
