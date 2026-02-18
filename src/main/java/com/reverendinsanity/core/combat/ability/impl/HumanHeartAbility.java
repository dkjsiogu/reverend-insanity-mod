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
import net.minecraft.world.phys.Vec3;
import java.util.List;

// 人心蛊技能：人道意志冲击
public class HumanHeartAbility extends GuAbility {

    public HumanHeartAbility() {
        super(GuRegistry.id("human_heart_gu"), 10f, 120, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.HUMAN).getTier() * 0.15f;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(8.0));

        VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
            eye.x, eye.y, eye.z,
            (float) look.x, (float) look.y, (float) look.z,
            0xFFFFDD44, 2.0f, 12);

        AABB beam = new AABB(eye, end).inflate(1.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, beam, e -> e != player && e.isAlive());

        float damage = 5f * efficiency * pathBonus;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.2f);
    }
}
