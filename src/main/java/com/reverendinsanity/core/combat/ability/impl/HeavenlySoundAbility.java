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

// 天籁蛊技能：天籁灭音——12格范围AOE音爆（二转）
public class HeavenlySoundAbility extends GuAbility {

    public HeavenlySoundAbility() {
        super(GuRegistry.id("heavenly_sound_gu"), 20f, 400, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.SOUND).getTier() * 0.15f;
        float damage = 10.0f * efficiency * pathBonus;

        AABB area = player.getBoundingBox().inflate(12, 6, 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        Vec3 center = player.position();
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            Vec3 away = target.position().subtract(center).normalize().scale(1.5);
            target.setDeltaMovement(away.x, 0.5, away.z);
        }

        VfxHelper.spawn(player, VfxType.RIPPLE,
            player.getX(), player.getY() + 1.0, player.getZ(),
            0f, 1f, 0f,
            0xFFBBDDFF, 5.0f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0f, 0.8f);
    }
}
