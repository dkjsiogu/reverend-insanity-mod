package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 杀机蛊Buff：杀意灌顶——+40%攻击力，400tick
public class KillingChanceBuff extends GuBuff {

    private static final ResourceLocation ATK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "killing_chance_atk");

    public KillingChanceBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "killing_chance_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
            new AttributeModifier(ATK_MOD, 0.4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATK_MOD);
    }
}
