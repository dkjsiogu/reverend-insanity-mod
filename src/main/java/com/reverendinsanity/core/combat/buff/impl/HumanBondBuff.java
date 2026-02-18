package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 人情蛊增益：人道团结之力
public class HumanBondBuff extends GuBuff {

    private static final ResourceLocation ARMOR_ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "human_bond_armor");

    public HumanBondBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "human_bond_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).addTransientModifier(
            new AttributeModifier(ARMOR_ID, 2, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_ID);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount * 0.85f;
    }

    @Override
    public float modifyOutgoingDamage(ServerPlayer player, net.minecraft.world.entity.LivingEntity target, float amount) {
        return amount * 1.15f;
    }
}
