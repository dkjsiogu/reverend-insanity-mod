package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 天禁蛊技能：最强禁制，封锁大范围敌人
public class HeavenSealAbility extends GuAbility {

    public HeavenSealAbility() {
        super(GuRegistry.id("heaven_seal_gu"), 22f, 400, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.RESTRICTION).getTier() * 0.2f;
        double range = 10.0;

        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFFFFAA00, 5.0f, 25);
        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFFCC7700, 4.0f, 25);

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        float damage = 8f * efficiency * pathBonus;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            target.setDeltaMovement(0, 0, 0);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5f, 0.3f);
    }
}
