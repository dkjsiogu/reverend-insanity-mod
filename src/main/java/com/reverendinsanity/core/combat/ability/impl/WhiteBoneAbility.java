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

// 白骨蛊技能：白骨森森——10格范围AOE骨刺爆发（二转）
public class WhiteBoneAbility extends GuAbility {

    public WhiteBoneAbility() {
        super(GuRegistry.id("white_bone_gu"), 22f, 400, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.BONE).getTier() * 0.15f;
        float damage = 8.5f * efficiency * pathBonus;

        AABB area = player.getBoundingBox().inflate(10, 5, 10);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        Vec3 center = player.position();
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            Vec3 away = target.position().subtract(center).normalize().scale(0.6);
            target.setDeltaMovement(away.x, 0.4, away.z);
        }

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFFEEDDCC, 4.5f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5f, 0.4f);
    }
}
