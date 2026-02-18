package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.SavageBullBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 蛮力天牛蛊技能：短暂爆发力量+速度
public class SavageBullAbility extends GuAbility {

    public SavageBullAbility() {
        super(GuRegistry.id("savage_bull_gu"), 5f, 200, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new SavageBullBuff());

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFDD6600, 2.5f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.8f, 1.2f);
    }
}
