package com.reverendinsanity.util;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

// 成就授予工具
public class AdvancementHelper {

    public static void grant(ServerPlayer player, String advancementId) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, advancementId);
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        if (holder == null) return;

        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
        if (progress.isDone()) return;

        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(holder, criterion);
        }
    }
}
