package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 影幕蛊Buff：影道防御，闪避+减伤
public class ShadowVeilBuff extends GuBuff {

    private static final ResourceLocation SPEED_MOD =
        ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "shadow_veil_speed");

    public ShadowVeilBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "shadow_veil_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
            new AttributeModifier(SPEED_MOD, 0.03, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (player.getRandom().nextFloat() < 0.25f) {
            return 0f;
        }
        return amount * 0.85f;
    }
}
