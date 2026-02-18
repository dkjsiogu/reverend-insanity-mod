package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.HeavenShieldBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 天护蛊技能：召唤天道庇护
public class HeavenShieldAbility extends GuAbility {

    public HeavenShieldAbility() {
        super(GuRegistry.id("heaven_shield_gu"), 10f, 500, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        player.getData(ModAttachments.GU_MASTER_DATA.get()).getBuffManager()
            .applyBuff(player, new HeavenShieldBuff());

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY() + 1.0, player.getZ(),
            0f, 1f, 0f,
            0xFFFFEE66, 3.0f, 20);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
