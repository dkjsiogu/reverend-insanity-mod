package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.IntelligenceManager;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.entity.GuMasterEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

// 电眼蛊技能：对注视目标单体侦察到SCANNED级别+雷光粒子
public class ElectricEyeAbility extends GuAbility {

    public ElectricEyeAbility() {
        super(GuRegistry.id("electric_eye_gu"), 15f, 100, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(12.0));
        AABB searchArea = player.getBoundingBox().expandTowards(lookVec.scale(12.0)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            player, eyePos, endPos, searchArea,
            e -> e instanceof LivingEntity && e.isAlive() && e != player, 12.0 * 12.0);

        if (entityHit != null) {
            Entity target = entityHit.getEntity();
            if (target instanceof GuMasterEntity) {
                IntelligenceManager.setIntelLevel(player, target, IntelligenceManager.IntelLevel.SCANNED);
            }
            target.setGlowingTag(true);

            if (player.level() instanceof ServerLevel sl) {
                for (int i = 0; i < 15; i++) {
                    double ox = (player.getRandom().nextDouble() - 0.5) * 1.0;
                    double oy = player.getRandom().nextDouble() * 2.0;
                    double oz = (player.getRandom().nextDouble() - 0.5) * 1.0;
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        target.getX() + ox, target.getY() + oy, target.getZ() + oz,
                        1, 0.0, 0.1, 0.0, 0.05);
                }
            }

            VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
                player.getX(), player.getEyeY(), player.getZ(),
                (float)(target.getX() - player.getX()),
                (float)(target.getEyeY() - player.getEyeY()),
                (float)(target.getZ() - player.getZ()),
                0xFFFFFF00, 0.5f, 10);
        } else {
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getEyeY(), player.getZ(),
                    8, 0.3, 0.3, 0.3, 0.05);
            }
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5f, 2.0f);
    }
}
