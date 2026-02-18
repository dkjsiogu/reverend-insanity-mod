package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 幸运蛊Buff：运势加身——+15%移速、+20%攻击力，400tick
public class LuckyBuff extends GuBuff {

    private static final ResourceLocation SPD_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "lucky_speed");
    private static final ResourceLocation ATK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "lucky_atk");

    public LuckyBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "lucky_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
            new AttributeModifier(SPD_MOD, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
            new AttributeModifier(ATK_MOD, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPD_MOD);
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATK_MOD);
    }
}
