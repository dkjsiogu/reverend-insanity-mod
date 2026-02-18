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
import java.util.Comparator;
import java.util.List;

// 入梦蛊技能：入梦——锁定8格内最近敌人，造成魔法伤害+短暂冻结
public class DreamAbility extends GuAbility {

    public DreamAbility() {
        super(GuRegistry.id("dream_gu"), 10f, 120, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.DREAM).getTier() * 0.15f;
        float damage = 4.0f * efficiency * pathBonus;

        AABB area = player.getBoundingBox().inflate(8);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        targets.stream()
               .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
               .ifPresent(target -> {
                   target.hurt(player.damageSources().magic(), damage);
                   target.setDeltaMovement(Vec3.ZERO);
                   target.hurtMarked = true;
               });

        VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
            player.getX(), player.getY() + 1.0, player.getZ(),
            (float) player.getLookAngle().x, (float) player.getLookAngle().y, (float) player.getLookAngle().z,
            0xFF9966FF, 2.0f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.6f, 0.8f);
    }
}
