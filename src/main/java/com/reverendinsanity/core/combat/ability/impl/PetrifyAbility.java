package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.FrostManager;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 岩化蛊技能：石化范围内敌人，极大幅度减速（二转）
public class PetrifyAbility extends GuAbility {

    public PetrifyAbility() {
        super(GuRegistry.id("petrify_gu"), 20f, 600, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        AABB area = player.getBoundingBox().inflate(5.0, 3.0, 5.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            FrostManager.applySlow(target, 80, 0.9);
            target.hurt(player.damageSources().magic(), 3.0f);
        }

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF888888, 4.0f, 30);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
    }
}
