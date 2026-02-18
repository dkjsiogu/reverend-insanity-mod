package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 神智蛊增益：智慧护盾，提升护甲和减少魔法伤害
public class MindGuardBuff extends GuBuff {

    private static final ResourceLocation ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "mind_guard_armor");

    public MindGuardBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "mind_guard_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).addTransientModifier(
            new AttributeModifier(ARMOR_MOD, 3.0, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)) {
            return amount * 0.5f;
        }
        return amount * 0.85f;
    }
}
