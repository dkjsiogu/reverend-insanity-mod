package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 隐鳞蛊Buff：自定义隐身机制——阻止怪物追踪+玩家隐身，攻击/技能解除，200tick
public class HiddenScaleBuff extends GuBuff {

    private static final ResourceLocation STEALTH_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "hidden_scale_stealth");

    public HiddenScaleBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_hidden_scale_gu"), 200);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.setInvisible(true);
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.setInvisible(false);
    }

    @Override
    public boolean preventMobTargeting(ServerPlayer player, LivingEntity attacker) {
        return true;
    }

    @Override
    public void onPlayerAttack(ServerPlayer player, LivingEntity target) {
        remove(player);
    }

    @Override
    public boolean shouldBreakOnDamage() {
        return true;
    }
}
