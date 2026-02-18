package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 铁皮增益：金道1转中档防御，+7护甲+2韧性，非绕甲*0.85
public class IronSkinBuff extends GuBuff {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_iron_skin");
    private static final ResourceLocation ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "iron_skin_armor");
    private static final ResourceLocation TOUGHNESS_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "iron_skin_toughness");

    public IronSkinBuff() {
        super(ID, 350);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).addTransientModifier(
            new AttributeModifier(ARMOR_MOD, 7.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).addTransientModifier(
            new AttributeModifier(TOUGHNESS_MOD, 2.0, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MOD);
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).removeModifier(TOUGHNESS_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return amount * 0.85f;
        }
        return amount;
    }
}
