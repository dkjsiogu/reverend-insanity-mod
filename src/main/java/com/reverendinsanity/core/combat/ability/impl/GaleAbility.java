package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.GaleBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 疾风蛊技能：极速疾风——速度+60%、跳跃+1.0、重力-50%、安全坠落+10，400tick（二转）
public class GaleAbility extends GuAbility {

    public GaleAbility() {
        super(GuRegistry.id("gale_gu"), 18f, 500, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new GaleBuff());

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF66FFBB, 2.5f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 0.6f, 1.2f);
    }
}
