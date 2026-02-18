package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.YellowCamelBeetleBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 黄骆天牛蛊：耐力增强buff
public class YellowCamelBeetleAbility extends GuAbility {

    public YellowCamelBeetleAbility() {
        super(GuRegistry.id("yellow_camel_beetle_gu"), 5f, 200, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new YellowCamelBeetleBuff());

        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFBB8800, 1.0f, 10);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 0.6f, 0.8f);
    }
}
