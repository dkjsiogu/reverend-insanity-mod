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

// 杀意蛊技能：杀意锁敌——锁定最近敌人发动杀意打击
public class KillIntentAbility extends GuAbility {

    public KillIntentAbility() {
        super(GuRegistry.id("kill_intent_gu"), 10f, 120, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.KILL).getTier() * 0.15f;
        float damage = 6.0f * efficiency * pathBonus;

        AABB area = player.getBoundingBox().inflate(10, 5, 10);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        targets.stream()
            .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
            .ifPresent(target -> {
                target.hurt(player.damageSources().magic(), damage);
                target.setDeltaMovement(0, 0.3, 0);
            });

        VfxHelper.spawn(player, VfxType.SLASH_ARC,
            player.getX(), player.getY() + 1.0, player.getZ(),
            0f, 0f, 1f,
            0xFFDD1111, 2.5f, 10);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.9f, 0.8f);
    }
}
