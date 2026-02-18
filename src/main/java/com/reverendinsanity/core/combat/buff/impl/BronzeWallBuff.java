package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 铜皮铁骨杀招增益：高护甲+自定义吸收池+物理减伤30%
public class BronzeWallBuff extends GuBuff {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_bronze_wall");
    private static final ResourceLocation ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "bronze_wall_armor");
    private static final ResourceLocation TOUGHNESS_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "bronze_wall_toughness");
    private float absorptionPool;

    public BronzeWallBuff(int durationTicks, float absorptionAmount) {
        super(ID, durationTicks);
        this.absorptionPool = absorptionAmount;
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).addTransientModifier(
            new AttributeModifier(ARMOR_MOD, 10.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).addTransientModifier(
            new AttributeModifier(TOUGHNESS_MOD, 6.0, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MOD);
        player.getAttribute(Attributes.ARMOR_TOUGHNESS).removeModifier(TOUGHNESS_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (absorptionPool > 0) {
            float absorbed = Math.min(amount, absorptionPool);
            absorptionPool -= absorbed;
            amount -= absorbed;
        }
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            amount *= 0.7f;
        }
        return amount;
    }
}
