package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.IronSkinBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;

// 铁皮蛊技能：金道1转中档防御，+7护甲+2韧性，非绕甲*0.85
public class IronSkinAbility extends GuAbility {

    public IronSkinAbility() {
        super(GuRegistry.id("iron_skin_gu"), 10f, 400, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new IronSkinBuff());
    }
}
