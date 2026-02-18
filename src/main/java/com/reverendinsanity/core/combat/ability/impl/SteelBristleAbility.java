package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.SteelBristleBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 钢鬃蛊技能：力道3转钢针，+10护甲+6韧性，非绕甲*0.8，近战反弹3.0伤害
public class SteelBristleAbility extends GuAbility {

    public SteelBristleAbility() {
        super(GuRegistry.id("steel_bristle_gu"), 25f, 600, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new SteelBristleBuff());

        VfxHelper.spawn(player, VfxType.DOME_FIELD,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFC0C0C0, 1.5f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 0.7f, 1.2f);
    }
}
