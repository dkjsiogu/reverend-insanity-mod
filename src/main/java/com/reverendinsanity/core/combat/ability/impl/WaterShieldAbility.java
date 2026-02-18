package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.WaterShieldBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 水盾蛊技能：水流护盾——+6护甲+4韧性+火焰免疫，400tick
public class WaterShieldAbility extends GuAbility {

    public WaterShieldAbility() {
        super(GuRegistry.id("water_shield_gu"), 10f, 500, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new WaterShieldBuff());

        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF4488FF, 2.5f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.6f, 1.2f);
    }
}
