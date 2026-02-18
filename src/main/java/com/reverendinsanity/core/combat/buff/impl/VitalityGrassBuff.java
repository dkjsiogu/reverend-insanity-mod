package com.reverendinsanity.core.combat.buff.impl;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

// 九叶生机草增益：每秒恢复1HP持续10秒
public class VitalityGrassBuff extends GuBuff {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "buff_vitality_grass");

    private int healCounter = 0;

    public VitalityGrassBuff() {
        super(ID, 200);
    }

    @Override
    protected void onApply(ServerPlayer player) {
        healCounter = 0;
    }

    @Override
    protected void onRemove(ServerPlayer player) {
        healCounter = 0;
    }

    @Override
    protected void onTick(ServerPlayer player) {
        healCounter++;
        if (healCounter >= 20) {
            healCounter = 0;
            player.heal(1.0f);
        }
    }
}
