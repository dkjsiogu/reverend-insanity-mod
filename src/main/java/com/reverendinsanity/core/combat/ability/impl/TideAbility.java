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

// 潮涌蛊技能：潮涌冲击——前方6格水浪推击+伤害
public class TideAbility extends GuAbility {

    public TideAbility() {
        super(GuRegistry.id("tide_gu"), 10f, 100, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        Vec3 look = player.getLookAngle();
        Vec3 center = player.position().add(look.scale(3));
        AABB area = new AABB(center.x - 3, center.y - 1.5, center.z - 3,
                             center.x + 3, center.y + 1.5, center.z + 3);

        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.WATER).getTier() * 0.1f;
        float damage = 3.0f * efficiency * pathBonus;

        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            Vec3 push = look.scale(1.2);
            target.push(push.x, 0.5, push.z);
            target.hurtMarked = true;
        }

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY(), player.getZ(),
            (float) look.x, (float) look.y, (float) look.z,
            0xFF4488FF, 3.0f, 12);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0f, 0.8f);
    }
}
