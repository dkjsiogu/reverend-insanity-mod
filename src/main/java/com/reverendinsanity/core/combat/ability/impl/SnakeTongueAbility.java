package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.IntelligenceManager;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 蛇信蛊技能：20格范围内所有实体发光12秒+提升情报到OBSERVED
public class SnakeTongueAbility extends GuAbility {

    public SnakeTongueAbility() {
        super(GuRegistry.id("snake_tongue_gu"), 40f, 200, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        AABB area = AABB.ofSize(player.position(), 40.0, 40.0, 40.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        for (LivingEntity target : targets) {
            data.getBuffManager().addGlowingTarget(target, 240);
        }

        IntelligenceManager.onScoutAbilityUsed(player, 20, IntelligenceManager.IntelLevel.OBSERVED);

        if (player.level() instanceof ServerLevel sl) {
            for (int i = 0; i < 30; i++) {
                double ox = (player.getRandom().nextDouble() - 0.5) * 6.0;
                double oy = player.getRandom().nextDouble() * 2.0;
                double oz = (player.getRandom().nextDouble() - 0.5) * 6.0;
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                    1, 0.0, 0.05, 0.0, 0.02);
            }
        }

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF4488CC, 3.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.4f);
    }
}
