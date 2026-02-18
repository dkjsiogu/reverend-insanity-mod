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

// 天刀蛊技能：天刀裂空——前方12格巨大劈斩
public class HeavenBladeAbility extends GuAbility {

    public HeavenBladeAbility() {
        super(GuRegistry.id("heaven_blade_gu"), 20f, 400, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.BLADE).getTier() * 0.2f;
        float damage = 10.0f * efficiency * pathBonus;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        AABB area = new AABB(eye, eye.add(look.scale(12.0))).inflate(2.5);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            Vec3 knockDir = target.position().subtract(player.position()).normalize().scale(1.2);
            target.setDeltaMovement(knockDir.x, 0.4, knockDir.z);
        }

        VfxHelper.spawn(player, VfxType.SLASH_ARC,
            player.getX(), player.getY() + 1.5, player.getZ(),
            (float) look.x, (float) look.y, (float) look.z,
            0xFFFF6600, 5.0f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.2f, 0.6f);
    }
}
