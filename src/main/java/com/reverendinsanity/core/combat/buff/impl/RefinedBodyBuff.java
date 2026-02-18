package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 炼体蛊Buff：炼道强化肉身
public class RefinedBodyBuff extends GuBuff {

    private static final ResourceLocation HEALTH_MOD =
        ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "refined_body_health");

    public RefinedBodyBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "refine_body_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(
            new AttributeModifier(HEALTH_MOD, 6.0, AttributeModifier.Operation.ADD_VALUE));
        player.heal(6f);
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount * 0.85f;
    }
}
