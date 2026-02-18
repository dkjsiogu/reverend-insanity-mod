package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.HiddenScaleBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 隐鳞蛊技能：自定义隐身200tick，怪物不追踪，攻击/受伤解除
public class HiddenScaleAbility extends GuAbility {

    public HiddenScaleAbility() {
        super(GuRegistry.id("hidden_scale_gu"), 50f, 400, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new HiddenScaleBuff());

        if (player.level() instanceof ServerLevel sl) {
            for (int i = 0; i < 20; i++) {
                double ox = (player.getRandom().nextDouble() - 0.5) * 1.5;
                double oy = player.getRandom().nextDouble() * 2.0;
                double oz = (player.getRandom().nextDouble() - 0.5) * 1.5;
                sl.sendParticles(ParticleTypes.SMOKE,
                    player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                    1, 0.0, 0.02, 0.0, 0.01);
            }
        }

        VfxHelper.spawn(player, VfxType.SHADOW_FADE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF333355, 2.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.4f, 0.5f);
    }
}
