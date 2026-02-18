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
import java.util.Comparator;
import java.util.List;

// 暗箭蛊技能：暗箭——锁定8格内最近敌人造成魔法伤害
public class DarkBoltAbility extends GuAbility {

    public DarkBoltAbility() {
        super(GuRegistry.id("dark_bolt_gu"), 10f, 120, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.DARK).getTier() * 0.15f;
        float damage = 5.5f * efficiency * pathBonus;

        AABB area = player.getBoundingBox().inflate(8);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        targets.stream()
               .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
               .ifPresent(target -> target.hurt(player.damageSources().magic(), damage));

        VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
            player.getX(), player.getY() + 1.0, player.getZ(),
            (float) player.getLookAngle().x, (float) player.getLookAngle().y, (float) player.getLookAngle().z,
            0xFF440066, 2.0f, 10);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDER_EYE_LAUNCH, SoundSource.PLAYERS, 0.7f, 0.8f);
    }
}
