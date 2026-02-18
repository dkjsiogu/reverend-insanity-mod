package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.FlightWingBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 飞翼蛊技能：凌空飞翼——施加飞翼Buff
public class FlightWingAbility extends GuAbility {

    public FlightWingAbility() {
        super(GuRegistry.id("flight_wing_gu"), 10f, 500, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        player.getData(ModAttachments.GU_MASTER_DATA.get()).getBuffManager()
            .applyBuff(player, new FlightWingBuff());

        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY() + 1.0, player.getZ(),
            0f, 1f, 0f,
            0xFF88CCFF, 3.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 0.6f, 1.5f);
    }
}
