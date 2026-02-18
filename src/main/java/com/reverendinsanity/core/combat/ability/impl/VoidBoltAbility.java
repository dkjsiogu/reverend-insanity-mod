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

// 虚无蛊技能：释放虚无之力侵蚀前方敌人
public class VoidBoltAbility extends GuAbility {

    public VoidBoltAbility() {
        super(GuRegistry.id("void_bolt_gu"), 10f, 120, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.VOID).getTier() * 0.15f;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        VfxHelper.spawn(player, VfxType.SHADOW_FADE,
            player.getX(), player.getEyeY(), player.getZ(),
            (float) look.x, (float) look.y, (float) look.z,
            0xFF220044, 2.0f, 18);

        AABB hitBox = new AABB(eye, eye.add(look.scale(8.0))).inflate(1.2);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, hitBox, e -> e != player && e.isAlive());

        float damage = 6f * efficiency * pathBonus;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDER_EYE_LAUNCH, SoundSource.PLAYERS, 1.0f, 0.5f);
    }
}
