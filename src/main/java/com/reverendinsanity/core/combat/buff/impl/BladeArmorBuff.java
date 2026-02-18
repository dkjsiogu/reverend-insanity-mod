package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 刀铠蛊Buff：刀意凛然——+30%攻击力、10%伤害减免，400tick
public class BladeArmorBuff extends GuBuff {

    private static final ResourceLocation ATK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "blade_armor_atk");

    public BladeArmorBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "blade_armor_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
            new AttributeModifier(ATK_MOD, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATK_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount * 0.9f;
    }
}
