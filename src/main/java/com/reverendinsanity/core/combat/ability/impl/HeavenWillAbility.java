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

// 天意蛊技能：天道意志降下神罚
public class HeavenWillAbility extends GuAbility {

    public HeavenWillAbility() {
        super(GuRegistry.id("heaven_will_gu"), 10f, 120, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.HEAVEN).getTier() * 0.15f;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getEyeY(), player.getZ(),
            (float) look.x, (float) look.y, (float) look.z,
            0xFFFFDD44, 2.0f, 18);

        AABB hitBox = new AABB(eye, eye.add(look.scale(8.0))).inflate(1.2);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, hitBox, e -> e != player && e.isAlive());

        float damage = 7f * efficiency * pathBonus;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            Vec3 kb = target.position().subtract(player.position()).normalize().scale(0.6);
            target.setDeltaMovement(kb.x, 0.3, kb.z);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);
    }
}
