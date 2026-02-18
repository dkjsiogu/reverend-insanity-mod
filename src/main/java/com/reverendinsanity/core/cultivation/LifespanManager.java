package com.reverendinsanity.core.cultivation;

import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 寿元系统：蛊师有有限寿元，境界越高寿元越长，某些操作消耗寿元，寿元耗尽则死亡
public class LifespanManager {

    private static final Map<UUID, Integer> warnedThresholds = new ConcurrentHashMap<>();
    private static final int TICK_INTERVAL = 200;

    public static int getMaxLifespan(int rankLevel) {
        return switch (rankLevel) {
            case 1 -> 500;
            case 2 -> 1000;
            case 3 -> 3000;
            case 4 -> 8000;
            case 5 -> 20000;
            default -> 500;
        };
    }

    public static void tick(ServerPlayer player) {
        if (player.tickCount % TICK_INTERVAL != 0) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return;

        int maxLifespan = getMaxLifespan(aperture.getRank().getLevel());
        int currentLifespan = data.getLifespan();

        if (currentLifespan <= 0) {
            data.setLifespan(maxLifespan);
            currentLifespan = maxLifespan;
        }

        if (currentLifespan > maxLifespan) {
            data.setLifespan(maxLifespan);
            currentLifespan = maxLifespan;
        }

        data.consumeLifespan(1);
        currentLifespan--;

        float ratio = (float) currentLifespan / maxLifespan;
        UUID uuid = player.getUUID();
        int warned = warnedThresholds.getOrDefault(uuid, 100);

        if (ratio <= 0.05f && warned > 5) {
            player.displayClientMessage(
                    Component.literal("【寿元将尽】生命即将走到尽头！剩余寿元: " + currentLifespan)
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
            warnedThresholds.put(uuid, 5);
        } else if (ratio <= 0.1f && warned > 10) {
            player.displayClientMessage(
                    Component.literal("寿元不足一成！需尽快突破或寻找延寿之法。剩余: " + currentLifespan)
                            .withStyle(ChatFormatting.RED), false);
            warnedThresholds.put(uuid, 10);
        } else if (ratio <= 0.25f && warned > 25) {
            player.displayClientMessage(
                    Component.literal("寿元已不足四分之一。剩余: " + currentLifespan)
                            .withStyle(ChatFormatting.GOLD), false);
            warnedThresholds.put(uuid, 25);
        } else if (ratio <= 0.5f && warned > 50) {
            player.displayClientMessage(
                    Component.literal("寿元过半。剩余: " + currentLifespan)
                            .withStyle(ChatFormatting.YELLOW), true);
            warnedThresholds.put(uuid, 50);
        }

        if (currentLifespan <= 0) {
            player.displayClientMessage(
                    Component.literal("寿元耗尽，油尽灯枯...")
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
            player.hurt(player.damageSources().starve(), Float.MAX_VALUE);
        }
    }

    public static void onBreakthrough(ServerPlayer player, int newRankLevel) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        int oldMax = getMaxLifespan(newRankLevel - 1);
        int newMax = getMaxLifespan(newRankLevel);
        int bonus = newMax - oldMax;
        data.addLifespan(bonus);
        warnedThresholds.remove(player.getUUID());

        player.displayClientMessage(
                Component.literal("突破成功！寿元增加 " + bonus + "，当前寿元: " + data.getLifespan())
                        .withStyle(ChatFormatting.GREEN), false);
    }

    public static void consumeForAbility(ServerPlayer player, int amount) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.consumeLifespan(amount);

        if (amount >= 10) {
            player.displayClientMessage(
                    Component.literal("消耗寿元 " + amount + "，剩余: " + data.getLifespan())
                            .withStyle(ChatFormatting.GRAY), true);
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
        warnedThresholds.remove(player.getUUID());
    }
}
