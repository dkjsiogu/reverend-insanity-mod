package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 花豕增益：+3攻击力，200tick
public class FlowerBoarBuff extends GuBuff {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_flower_boar");
    private static final ResourceLocation DAMAGE_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "flower_boar_damage");

    public FlowerBoarBuff() {
        super(ID, 200);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
            new AttributeModifier(DAMAGE_MOD, 3.0, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(DAMAGE_MOD);
    }
}
