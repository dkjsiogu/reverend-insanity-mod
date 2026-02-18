package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.core.combat.buff.GuBuff;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 固元蛊增益：消耗HP换取临时最大生命值增加
public class SolidifyOriginBuff extends GuBuff {

    private static final AttributeModifier MAX_HP_MOD = new AttributeModifier(
        GuRegistry.id("solidify_origin_max_hp"), 8.0, AttributeModifier.Operation.ADD_VALUE);

    public SolidifyOriginBuff() {
        super(GuRegistry.id("solidify_origin_gu"), 600);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        var attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null && !attr.hasModifier(MAX_HP_MOD.id())) {
            attr.addTransientModifier(MAX_HP_MOD);
        }
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        var attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) attr.removeModifier(MAX_HP_MOD.id());
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }
}
