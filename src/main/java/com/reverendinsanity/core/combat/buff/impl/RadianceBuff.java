package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 耀光蛊Buff：光之壁垒——+4护甲、+20%攻击力，400tick
public class RadianceBuff extends GuBuff {

    private static final ResourceLocation ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "radiance_armor");
    private static final ResourceLocation ATK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "radiance_atk");

    public RadianceBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "radiance_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).addTransientModifier(
            new AttributeModifier(ARMOR_MOD, 4.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
            new AttributeModifier(ATK_MOD, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MOD);
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATK_MOD);
    }
}
