package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

// 青丝隐匿：真正隐身+怪物无法锁定，攻击或受击时打破
public class StealthBuff extends GuBuff {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_stealth");

    public StealthBuff() {
        super(ID, 160);
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
