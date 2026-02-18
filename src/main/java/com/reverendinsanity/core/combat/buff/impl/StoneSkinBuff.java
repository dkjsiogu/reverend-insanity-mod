package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 石皮增益：金道1转低价防御，+5护甲，非绕甲伤害*0.9
public class StoneSkinBuff extends GuBuff {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_stone_skin");
    private static final ResourceLocation ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "stone_skin_armor");

    public StoneSkinBuff() {
        super(ID, 300);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).addTransientModifier(
            new AttributeModifier(ARMOR_MOD, 5.0, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return amount * 0.9f;
        }
        return amount;
    }
}
