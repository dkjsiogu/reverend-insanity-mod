package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.IntelligenceManager;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import java.util.List;

// 地听肉耳草技能：60格范围标记所有生物位置+提升情报到SCANNED
public class EarthListenerAbility extends GuAbility {

    public EarthListenerAbility() {
        super(GuRegistry.id("earth_listener_gu"), 60f, 400, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        AABB area = AABB.ofSize(player.position(), 120.0, 120.0, 120.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        IntelligenceManager.onScoutAbilityUsed(player, 60, IntelligenceManager.IntelLevel.SCANNED);

        if (player.level() instanceof ServerLevel sl) {
            DustParticleOptions marker = new DustParticleOptions(new Vector3f(0.3f, 0.8f, 0.3f), 1.2f);
            for (LivingEntity target : targets) {
                for (int i = 0; i < 5; i++) {
                    double ox = (player.getRandom().nextDouble() - 0.5) * 0.8;
                    double oz = (player.getRandom().nextDouble() - 0.5) * 0.8;
                    sl.sendParticles(marker,
                        target.getX() + ox, target.getY() + 0.1, target.getZ() + oz,
                        2, 0.0, 0.3, 0.0, 0.01);
                }
            }

            sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                player.getX(), player.getY(), player.getZ(),
                20, 3.0, 0.2, 3.0, 0.02);
        }

        VfxHelper.spawn(player, VfxType.RIPPLE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF44CC44, 6.0f, 30);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.6f);
    }
}
