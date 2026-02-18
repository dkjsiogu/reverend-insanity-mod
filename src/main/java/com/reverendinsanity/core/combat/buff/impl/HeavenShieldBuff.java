package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 天护蛊增益：天道庇护，大幅减伤
public class HeavenShieldBuff extends GuBuff {

    private static final ResourceLocation ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "heaven_shield_armor");
    private static final ResourceLocation TOUGH_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "heaven_shield_tough");

    public HeavenShieldBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "heaven_shield_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).addTransientModifier(
            new AttributeModifier(ARMOR_MOD, 5.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).addTransientModifier(
            new AttributeModifier(TOUGH_MOD, 3.0, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MOD);
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).removeModifier(TOUGH_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount * 0.70f;
    }
}
