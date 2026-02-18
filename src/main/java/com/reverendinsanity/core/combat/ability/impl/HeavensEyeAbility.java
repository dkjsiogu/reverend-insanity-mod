package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
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

// 天眼蛊技能：侦查30格内所有生物，用setGlowingTag标记（非药水效果）
public class HeavensEyeAbility extends GuAbility {

    public HeavensEyeAbility() {
        super(GuRegistry.id("heavens_eye_gu"), 20f, 600, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        AABB area = AABB.ofSize(player.position(), 60.0, 60.0, 60.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        for (LivingEntity target : targets) {
            data.getBuffManager().addGlowingTarget(target, 600);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 50; i++) {
                double ox = (player.getRandom().nextDouble() - 0.5) * 4.0;
                double oy = player.getRandom().nextDouble() * 3.0;
                double oz = (player.getRandom().nextDouble() - 0.5) * 4.0;
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                    1, 0.0, 0.1, 0.0, 0.05);
            }
        }

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFFFD700, 5.0f, 25);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
