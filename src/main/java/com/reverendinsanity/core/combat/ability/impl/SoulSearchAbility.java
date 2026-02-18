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

// 搜魂蛊技能：搜魂——锁定8格内最近敌人造成魔法伤害（无视护甲）
public class SoulSearchAbility extends GuAbility {

    public SoulSearchAbility() {
        super(GuRegistry.id("soul_search_gu"), 12f, 200, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.SOUL).getTier() * 0.15f;
        float damage = 5.0f * efficiency * pathBonus;

        AABB area = player.getBoundingBox().inflate(8);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        targets.stream()
               .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
               .ifPresent(target -> target.hurt(player.damageSources().magic(), damage));

        VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
            player.getX(), player.getY() + 1.0, player.getZ(),
            (float) player.getLookAngle().x, (float) player.getLookAngle().y, (float) player.getLookAngle().z,
            0xFFAA44FF, 2.0f, 10);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6f, 1.5f);
    }
}
