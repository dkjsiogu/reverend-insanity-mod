package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.ShadowFollowerBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 幽影随行蛊技能：影道隐身+敌人无法锁定
public class ShadowFollowerAbility extends GuAbility {

    public ShadowFollowerAbility() {
        super(GuRegistry.id("shadow_follower_gu"), 20f, 600, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new ShadowFollowerBuff());

        VfxHelper.spawn(player, VfxType.SHADOW_FADE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF111133, 2.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PHANTOM_AMBIENT, SoundSource.PLAYERS, 0.5f, 0.6f);
    }
}
