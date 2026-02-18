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

// 宴蛊技能：食道吞噬攻击并恢复
public class FeastAbility extends GuAbility {

    public FeastAbility() {
        super(GuRegistry.id("feast_gu"), 8f, 100, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.FOOD).getTier() * 0.15f;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(6.0));

        VfxHelper.spawn(player, VfxType.HEAL_SPIRAL,
            eye.x, eye.y, eye.z,
            (float) look.x, (float) look.y, (float) look.z,
            0xFF88CC44, 2.0f, 12);

        AABB beam = new AABB(eye, end).inflate(1.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, beam, e -> e != player && e.isAlive());

        float damage = 4f * efficiency * pathBonus;
        float totalHeal = 0f;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            totalHeal += damage * 0.25f;
        }
        player.heal(Math.min(totalHeal, 6f));

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.8f, 1.2f);
    }
}
