package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.IntelligenceManager;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 真视蛊技能：16格范围揭示隐身+提升所有蛊师情报到FULL
public class TrueSightAbility extends GuAbility {

    public TrueSightAbility() {
        super(GuRegistry.id("true_sight_gu"), 80f, 600, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        AABB area = AABB.ofSize(player.position(), 32.0, 32.0, 32.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            if (target.isInvisible()) {
                target.setInvisible(false);
            }
            target.setGlowingTag(true);
        }

        IntelligenceManager.onScoutAbilityUsed(player, 16, IntelligenceManager.IntelLevel.FULL);

        if (player.level() instanceof ServerLevel sl) {
            for (int i = 0; i < 60; i++) {
                double ox = (player.getRandom().nextDouble() - 0.5) * 5.0;
                double oy = player.getRandom().nextDouble() * 4.0;
                double oz = (player.getRandom().nextDouble() - 0.5) * 5.0;
                sl.sendParticles(ParticleTypes.END_ROD,
                    player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                    1, 0.0, 0.15, 0.0, 0.08);
            }
            sl.sendParticles(ParticleTypes.FLASH,
                player.getX(), player.getY() + 1.5, player.getZ(),
                3, 0.0, 0.0, 0.0, 0.0);
        }

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY() + 1.0, player.getZ(),
            0f, 1f, 0f,
            0xFFFFDD44, 4.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2f, 1.5f);
    }
}
