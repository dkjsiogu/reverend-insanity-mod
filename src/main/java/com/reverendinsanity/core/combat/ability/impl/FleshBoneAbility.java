package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 肉骨蛊技能：治疗回复
public class FleshBoneAbility extends GuAbility {

    public FleshBoneAbility() {
        super(GuRegistry.id("flesh_bone_gu"), 15f, 600, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        player.heal(16f);
        player.displayClientMessage(Component.literal("肉骨蛊催动，血肉再生"), true);

        VfxHelper.spawn(player, VfxType.HEAL_SPIRAL,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFFF4444, 1.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.5f);
    }
}
