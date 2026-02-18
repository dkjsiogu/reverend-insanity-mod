package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 白玉增益：极高护甲+完全击退免疫+自定义吸收池+摔落免疫
public class WhiteJadeBuff extends GuBuff {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_white_jade");
    private static final ResourceLocation ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "white_jade_armor");
    private static final ResourceLocation TOUGHNESS_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "white_jade_toughness");
    private static final ResourceLocation KNOCKBACK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "white_jade_knockback");
    private float absorptionPool = 12.0f;

    public WhiteJadeBuff() {
        super(ID, 600);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).addTransientModifier(
            new AttributeModifier(ARMOR_MOD, 20.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).addTransientModifier(
            new AttributeModifier(TOUGHNESS_MOD, 10.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addTransientModifier(
            new AttributeModifier(KNOCKBACK_MOD, 1.0, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MOD);
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).removeModifier(TOUGHNESS_MOD);
        player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(KNOCKBACK_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL)) {
            return 0f;
        }
        if (absorptionPool > 0) {
            float absorbed = Math.min(amount, absorptionPool);
            absorptionPool -= absorbed;
            amount -= absorbed;
        }
        return amount * 0.7f;
    }
}
