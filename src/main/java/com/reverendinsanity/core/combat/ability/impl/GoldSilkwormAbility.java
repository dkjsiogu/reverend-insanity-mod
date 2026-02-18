package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

// 金蚕蛊技能：标记最近敌人，该敌人攻击时受到自身攻击力30%的反噬伤害
public class GoldSilkwormAbility extends GuAbility {

    public GoldSilkwormAbility() {
        super(GuRegistry.id("gold_silkworm_gu"), 25f, 600, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        Vec3 eye = player.getEyePosition();
        AABB area = AABB.ofSize(eye, 20.0, 20.0, 20.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity target : targets) {
            double dist = target.distanceToSqr(player);
            if (dist < closestDist) {
                closestDist = dist;
                closest = target;
            }
        }

        if (closest != null) {
            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            data.getBuffManager().addCursedTarget(closest, 200, 0.3f);
            data.getBuffManager().addGlowingTarget(closest, 200);

            VfxHelper.spawn(player, VfxType.PULSE_WAVE,
                (float) closest.getX(), (float) closest.getY(), (float) closest.getZ(),
                0f, 1f, 0f,
                0xFFFFD700, 2.0f, 20);
            player.level().playSound(null, closest.getX(), closest.getY(), closest.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 0.4f);
        }
    }
}
