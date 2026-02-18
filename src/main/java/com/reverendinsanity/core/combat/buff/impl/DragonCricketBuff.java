package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// 龙丸蛐蛐Buff：25%闪避+移速提升
public class DragonCricketBuff extends GuBuff {

    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_dragon_cricket");
    private static final ResourceLocation SPD_MOD =
        ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "dragon_cricket_spd");

    public DragonCricketBuff() {
        super(ID, 300);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
            new AttributeModifier(SPD_MOD, 0.05, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPD_MOD);
    }

    @Override
    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (player.getRandom().nextFloat() < 0.25f) {
            player.displayClientMessage(Component.literal("闪避！"), true);
            return 0f;
        }
        return amount;
    }
}
