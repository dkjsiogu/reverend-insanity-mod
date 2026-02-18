package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 黑豕增益：移速提升+攻击力，冲刺蓄力后下次攻击附加额外伤害
public class BoarChargeBuff extends GuBuff {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_boar_charge");
    private static final ResourceLocation SPEED_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "boar_charge_speed");
    private static final ResourceLocation ATTACK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "boar_charge_attack");
    private float chargeAccumulated = 0f;

    public BoarChargeBuff() {
        super(ID, 200);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
            new AttributeModifier(SPEED_MOD, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
            new AttributeModifier(ATTACK_MOD, 2.0, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MOD);
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_MOD);
    }

    @Override
    protected void onTick(ServerPlayer player) {
        if (player.isSprinting()) {
            chargeAccumulated = Math.min(chargeAccumulated + 0.1f, 4.0f);
        }
    }

    @Override
    public float modifyOutgoingDamage(ServerPlayer player, LivingEntity target, float amount) {
        if (chargeAccumulated > 0.5f) {
            float bonus = chargeAccumulated;
            chargeAccumulated = 0f;
            return amount + bonus;
        }
        return amount;
    }
}
