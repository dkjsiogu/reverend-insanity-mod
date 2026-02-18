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

// 炼火蛊技能：炼道之火焚烧敌人
public class RefineFireAbility extends GuAbility {

    public RefineFireAbility() {
        super(GuRegistry.id("refine_fire_gu"), 10f, 120, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.REFINEMENT).getTier() * 0.15f;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(8.0));

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            eye.x, eye.y, eye.z,
            (float) look.x, (float) look.y, (float) look.z,
            0xFFFF8844, 2.5f, 15);

        AABB beam = new AABB(eye, end).inflate(1.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, beam, e -> e != player && e.isAlive());

        float damage = 5f * efficiency * pathBonus;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            target.setRemainingFireTicks(60);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.2f);
    }
}
