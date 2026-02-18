package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 天蓬增益：金道3转终极防御，+14护甲+8韧性，所有伤害(含绕甲)*0.75
public class HeavenCanopyBuff extends GuBuff {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_heaven_canopy");
    private static final ResourceLocation ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "heaven_canopy_armor");
    private static final ResourceLocation TOUGHNESS_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "heaven_canopy_toughness");

    public HeavenCanopyBuff() {
        super(ID, 600);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).addTransientModifier(
            new AttributeModifier(ARMOR_MOD, 14.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).addTransientModifier(
            new AttributeModifier(TOUGHNESS_MOD, 8.0, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MOD);
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).removeModifier(TOUGHNESS_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount * 0.75f;
    }
}
