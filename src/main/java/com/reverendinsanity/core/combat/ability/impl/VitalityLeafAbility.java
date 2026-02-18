package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

// 生机叶技能：木道1转消耗型即时治疗，使用后蛊虫移除，治疗10HP
public class VitalityLeafAbility extends GuAbility {

    public VitalityLeafAbility() {
        super(GuRegistry.id("vitality_leaf_gu"), 3f, 0, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        player.heal(10.0f);

        GuInstance guInst = aperture.findGuInstance(GuRegistry.id("vitality_leaf_gu"));
        if (guInst != null) {
            aperture.removeGu(guInst);
        }

        player.displayClientMessage(Component.literal("生机叶消融，伤口愈合"), true);

        VfxHelper.spawn(player, VfxType.HEAL_SPIRAL,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF66EE66, 1.0f, 15);
    }
}
