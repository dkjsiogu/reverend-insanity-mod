package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.BoarChargeBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 黑豕蛊技能：Attribute移速+攻击力，冲刺蓄力增强下次攻击
public class WhiteBoarAbility extends GuAbility {

    public WhiteBoarAbility() {
        super(GuRegistry.id("white_boar_gu"), 12f, 300, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new BoarChargeBuff());

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF884444, 1.2f, 10);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.5f, 1.2f);
    }
}
