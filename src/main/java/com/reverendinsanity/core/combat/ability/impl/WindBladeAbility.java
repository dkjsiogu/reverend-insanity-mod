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

// 风刃蛊技能：风刃斩——向前方6格释放风刃切割（魔法伤害+击退）
public class WindBladeAbility extends GuAbility {

    public WindBladeAbility() {
        super(GuRegistry.id("wind_blade_gu"), 10f, 100, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        Vec3 look = player.getLookAngle();
        Vec3 center = player.position().add(look.scale(3));
        AABB area = new AABB(center.x - 3, center.y - 1.5, center.z - 3,
                             center.x + 3, center.y + 1.5, center.z + 3);

        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.WIND).getTier() * 0.1f;
        float damage = 3.0f * efficiency * pathBonus;

        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            Vec3 knockback = target.position().subtract(player.position()).normalize().scale(0.8);
            target.push(knockback.x, 0.4, knockback.z);
            target.hurtMarked = true;
        }

        VfxHelper.spawn(player, VfxType.SLASH_ARC,
            player.getX(), player.getY() + 1.0, player.getZ(),
            (float) look.x, (float) look.y, (float) look.z,
            0xFF88FFAA, 2.0f, 10);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.8f, 1.8f);
    }
}
