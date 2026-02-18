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

// 天鹰蛊技能：天鹰俯冲——12格范围极速冲击+AOE（二转）
public class SkyEagleAbility extends GuAbility {

    public SkyEagleAbility() {
        super(GuRegistry.id("sky_eagle_gu"), 20f, 400, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.FLIGHT).getTier() * 0.15f;
        float damage = 8.0f * efficiency * pathBonus;

        Vec3 look = player.getLookAngle();
        Vec3 target = player.position().add(look.scale(8.0));
        player.teleportTo(target.x, target.y, target.z);
        player.fallDistance = 0;

        AABB area = player.getBoundingBox().inflate(5, 3, 5);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        Vec3 center = player.position();
        for (LivingEntity t : targets) {
            t.hurt(player.damageSources().magic(), damage);
            Vec3 away = t.position().subtract(center).normalize().scale(1.0);
            t.setDeltaMovement(away.x, 0.5, away.z);
        }

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY() + 1.0, player.getZ(),
            0f, -1f, 0f,
            0xFF66BBFF, 4.0f, 12);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.5f, 0.8f);
    }
}
