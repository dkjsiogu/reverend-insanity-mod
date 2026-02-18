package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;

// 幻影蛊Buff：幻影护身——+30%速度、50%概率怪物无法锁定，400tick
public class PhantomIllusionBuff extends GuBuff {

    private static final ResourceLocation SPD_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "phantom_spd");

    public PhantomIllusionBuff() {
        super(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "phantom_gu"), 400);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
            new AttributeModifier(SPD_MOD, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPD_MOD);
    }

    @Override
    public boolean preventMobTargeting(ServerPlayer player, LivingEntity attacker) {
        return player.getRandom().nextFloat() < 0.5f;
    }
}
