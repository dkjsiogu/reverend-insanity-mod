package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 幽影突袭杀招增益：隐身+极速+蓄力一击（首次攻击附加魔法伤害后打破）
public class ShadowStrikeBuff extends GuBuff {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_shadow_strike");
    private static final ResourceLocation SPEED_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "shadow_strike_speed");
    private final float empoweredDamage;

    public ShadowStrikeBuff(float empoweredDamage) {
        super(ID, 100);
        this.empoweredDamage = empoweredDamage;
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.setInvisible(true);
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
            new AttributeModifier(SPEED_MOD, 0.6, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.setInvisible(false);
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MOD);
    }

    @Override
    public boolean preventMobTargeting(ServerPlayer player, LivingEntity attacker) {
        return true;
    }

    @Override
    public void onPlayerAttack(ServerPlayer player, LivingEntity target) {
        target.hurt(player.damageSources().magic(), empoweredDamage);
        remove(player);
    }
}
