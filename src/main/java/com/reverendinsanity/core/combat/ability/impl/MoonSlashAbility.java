package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

// 斩月蛊技能：弧月斩——前方6格弧形斩击
public class MoonSlashAbility extends GuAbility {

    public MoonSlashAbility() {
        super(GuRegistry.id("moon_slash_gu"), 8f, 100, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.BLADE).getTier() * 0.15f;
        float damage = 5.0f * efficiency * pathBonus;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        AABB area = new AABB(eye, eye.add(look.scale(6.0))).inflate(2.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            Vec3 knockDir = target.position().subtract(player.position()).normalize().scale(0.6);
            target.setDeltaMovement(target.getDeltaMovement().add(knockDir));
        }

        VfxHelper.spawn(player, VfxType.SLASH_ARC,
            player.getX(), player.getY() + 1.0, player.getZ(),
            (float) look.x, (float) look.y, (float) look.z,
            0xFFCC8844, 3.0f, 10);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 0.8f);
    }
}
