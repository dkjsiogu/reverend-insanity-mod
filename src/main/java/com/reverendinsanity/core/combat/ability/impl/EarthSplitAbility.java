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

// 地裂蛊技能：大地震裂，以自身为中心AOE伤害+击飞
public class EarthSplitAbility extends GuAbility {

    public EarthSplitAbility() {
        super(GuRegistry.id("earth_split_gu"), 10f, 200, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.EARTH).getTier() * 0.15f;
        float damage = 6.0f * efficiency * pathBonus;

        AABB area = player.getBoundingBox().inflate(5.0, 2.0, 5.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            Vec3 knockDir = target.position().subtract(player.position()).normalize();
            target.setDeltaMovement(knockDir.x * 0.8, 0.6, knockDir.z * 0.8);
            target.hurtMarked = true;
        }

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF664422, 4.0f, 25);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8f, 0.5f);
    }
}
