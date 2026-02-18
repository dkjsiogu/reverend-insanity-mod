package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.WaterSpiderBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 水蛛蛊技能：水道1转，+4护甲，水中*0.7减伤+水下呼吸
public class WaterSpiderAbility extends GuAbility {

    public WaterSpiderAbility() {
        super(GuRegistry.id("water_spider_gu"), 8f, 400, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new WaterSpiderBuff());

        VfxHelper.spawn(player, VfxType.DOME_FIELD,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF4488CC, 1.2f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.6f, 1.3f);
    }
}
