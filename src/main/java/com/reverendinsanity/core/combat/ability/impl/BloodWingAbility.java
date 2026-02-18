package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.BloodWingBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 血翼蛊技能：消耗HP获得飞行能力
public class BloodWingAbility extends GuAbility {

    public BloodWingAbility() {
        super(GuRegistry.id("blood_wing_gu"), 20f, 400, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new BloodWingBuff());

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFAA0000, 3.0f, 25);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.0f, 0.6f);
    }
}
