package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

// 折空蛊技能：扭曲前方空间造成伤害
public class WarpAbility extends GuAbility {

    public WarpAbility() {
        super(GuRegistry.id("warp_gu"), 8f, 100, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        float efficiency = aperture.getEssenceGrade().getEfficiency();

        for (int i = 1; i <= 6; i++) {
            Vec3 p = eye.add(look.scale(i));
            VfxHelper.spawn(player, VfxType.RIPPLE,
                p.x, p.y, p.z, (float) look.x, (float) look.y, (float) look.z,
                0xFF8844CC, 1.5f, 12);
        }

        AABB hitBox = new AABB(eye, eye.add(look.scale(6.0))).inflate(1.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, hitBox, e -> e != player && e.isAlive());

        float damage = 6f * efficiency;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            Vec3 kb = target.position().subtract(player.position()).normalize().scale(0.5);
            target.setDeltaMovement(target.getDeltaMovement().add(kb.x, 0.2, kb.z));
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.5f);
    }
}
