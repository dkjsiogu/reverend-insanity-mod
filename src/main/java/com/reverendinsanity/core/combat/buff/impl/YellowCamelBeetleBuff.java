package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 黄骆天牛增益：+2最大生命+0.3击退抗性，300tick
public class YellowCamelBeetleBuff extends GuBuff {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_yellow_camel_beetle");
    private static final ResourceLocation HEALTH_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "yellow_camel_beetle_health");
    private static final ResourceLocation KB_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "yellow_camel_beetle_kb");

    public YellowCamelBeetleBuff() {
        super(ID, 300);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(
            new AttributeModifier(HEALTH_MOD, 2.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addTransientModifier(
            new AttributeModifier(KB_MOD, 0.3, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH_MOD);
        player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(KB_MOD);
    }
}
