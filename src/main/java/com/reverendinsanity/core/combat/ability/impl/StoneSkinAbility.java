package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.StoneSkinBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;

// 石皮蛊技能：金道1转低价防御，+5护甲，非绕甲*0.9
public class StoneSkinAbility extends GuAbility {

    public StoneSkinAbility() {
        super(GuRegistry.id("stone_skin_gu"), 6f, 400, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new StoneSkinBuff());
    }
}
