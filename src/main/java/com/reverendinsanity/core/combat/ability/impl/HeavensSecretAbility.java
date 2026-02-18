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

// 天机蛊技能：天机一击——锁定最近敌人，发动因果打击
public class HeavensSecretAbility extends GuAbility {

    public HeavensSecretAbility() {
        super(GuRegistry.id("heavens_secret_gu"), 25f, 500, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.LUCK).getTier() * 0.2f;
        float damage = 12.0f * efficiency * pathBonus;

        AABB area = player.getBoundingBox().inflate(12, 6, 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        targets.stream()
            .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
            .ifPresent(target -> {
                target.hurt(player.damageSources().magic(), damage);
                target.setDeltaMovement(0, 0.5, 0);
            });

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY() + 3.0, player.getZ(),
            0f, -1f, 0f,
            0xFFFFCC00, 3.5f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0f, 1.5f);
    }
}
