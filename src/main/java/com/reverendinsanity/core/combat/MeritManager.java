package com.reverendinsanity.core.combat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 战功管理器：兽潮击杀积累战功，战功可通过蛊商兑换
public class MeritManager {

    private static final Map<UUID, Integer> meritPoints = new ConcurrentHashMap<>();
    private static boolean beastTideActive = false;

    public static void setBeastTideActive(boolean active) {
        beastTideActive = active;
    }

    public static boolean isBeastTideActive() {
        return beastTideActive;
    }

    public static void addMerit(ServerPlayer player, int amount) {
        UUID uuid = player.getUUID();
        int current = meritPoints.getOrDefault(uuid, 0);
        meritPoints.put(uuid, current + amount);
        player.displayClientMessage(
            Component.literal("+" + amount + " 战功").withStyle(ChatFormatting.GOLD), true);
    }

    public static int getMerit(ServerPlayer player) {
        return meritPoints.getOrDefault(player.getUUID(), 0);
    }

    public static int getMerit(UUID uuid) {
        return meritPoints.getOrDefault(uuid, 0);
    }

    public static boolean consumeMerit(ServerPlayer player, int amount) {
        UUID uuid = player.getUUID();
        int current = meritPoints.getOrDefault(uuid, 0);
        if (current < amount) return false;
        meritPoints.put(uuid, current - amount);
        return true;
    }

    public static void onBeastTideEnd(ServerPlayer player) {
        int merit = meritPoints.getOrDefault(player.getUUID(), 0);
        if (merit > 0) {
            player.displayClientMessage(
                Component.literal("兽潮结束！你获得了" + merit + "战功。").withStyle(ChatFormatting.GOLD), false);
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
    }

    public static void clearAll() {
        meritPoints.clear();
        beastTideActive = false;
    }

    public static void onKillDuringBeastTide(ServerPlayer player, boolean isElite, boolean isBossKing, boolean isThunderCrown) {
        if (!beastTideActive) return;

        int merit;
        if (isBossKing) {
            merit = 50;
        } else if (isThunderCrown) {
            merit = 10;
        } else if (isElite) {
            merit = 5;
        } else {
            merit = 1;
        }

        addMerit(player, merit);
    }
}
