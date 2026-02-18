package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.SelfHealBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 自愈蛊技能：受伤后30%伤害转化为缓慢回血
public class SelfHealAbility extends GuAbility {

    public SelfHealAbility() {
        super(GuRegistry.id("self_heal_gu"), 10f, 500, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new SelfHealBuff());

        VfxHelper.spawn(player, VfxType.HEAL_SPIRAL,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFCC4444, 2.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8f, 0.8f);
    }
}
