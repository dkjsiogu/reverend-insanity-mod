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

// 避祸蛊技能：趋吉避凶——8格范围内敌人减速+弱化
public class MisfortuneWardAbility extends GuAbility {

    public MisfortuneWardAbility() {
        super(GuRegistry.id("misfortune_ward_gu"), 10f, 300, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.LUCK).getTier() * 0.1f;

        AABB area = player.getBoundingBox().inflate(8, 4, 8);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), 2.0f * efficiency * pathBonus);
            target.setDeltaMovement(Vec3.ZERO);
        }

        VfxHelper.spawn(player, VfxType.RIPPLE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFAABB44, 3.0f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.6f, 1.2f);
    }
}
