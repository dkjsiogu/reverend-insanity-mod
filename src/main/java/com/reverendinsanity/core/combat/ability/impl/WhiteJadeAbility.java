package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.WhiteJadeBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 白玉蛊技能：Attribute极高护甲+击退免疫+自定义吸收池+摔落免疫
public class WhiteJadeAbility extends GuAbility {

    public WhiteJadeAbility() {
        super(GuRegistry.id("white_jade_gu"), 25f, 600, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new WhiteJadeBuff());

        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 30; i++) {
                double ox = (player.getRandom().nextDouble() - 0.5) * 2.0;
                double oy = player.getRandom().nextDouble() * 2.0;
                double oz = (player.getRandom().nextDouble() - 0.5) * 2.0;
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                    1, 0.0, 0.05, 0.0, 0.02);
            }
        }

        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFEEFFEE, 2.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
