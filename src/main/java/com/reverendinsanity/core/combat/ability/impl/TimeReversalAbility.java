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
import java.util.List;

// 逆时蛊技能：逆转时间流，回复自身生命并对范围敌人造成伤害
public class TimeReversalAbility extends GuAbility {

    public TimeReversalAbility() {
        super(GuRegistry.id("time_reversal_gu"), 22f, 400, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();

        VfxHelper.spawn(player, VfxType.HEAL_SPIRAL,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFF6699FF, 3.0f, 25);
        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFF4477CC, 4.0f, 20);

        float heal = 10f * efficiency;
        player.heal(heal);

        double range = 10.0;
        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        float damage = 8f * efficiency;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            target.setDeltaMovement(target.getDeltaMovement().scale(0.3));
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 0.3f);
    }
}
