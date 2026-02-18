package com.reverendinsanity.core.event;

import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import java.util.Map;

// 天地异象管理器：管理各维度的世界事件触发与运行，状态持久化到SavedData
public class WorldEventManager {

    private static final int CHECK_INTERVAL = 6000;
    private static final float BASE_TRIGGER_CHANCE = 0.15f;

    public static void tickAllEvents(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        WorldEventSavedData savedData = WorldEventSavedData.get(overworld);
        Map<ResourceKey<Level>, ActiveWorldEvent> activeEvents = savedData.getActiveEvents();

        int tickCounter = savedData.getTickCounter() + 1;
        savedData.setTickCounter(tickCounter);

        for (ServerLevel level : server.getAllLevels()) {
            ResourceKey<Level> key = level.dimension();
            ActiveWorldEvent event = activeEvents.get(key);

            if (event != null) {
                boolean finished = event.tick(level);
                if (finished) {
                    activeEvents.remove(key);
                    savedData.setDirty();
                }
            }

            if (event == null && tickCounter % CHECK_INTERVAL == 0) {
                trySpawnEvent(level, activeEvents, savedData);
            }
        }
    }

    private static void trySpawnEvent(ServerLevel level, Map<ResourceKey<Level>, ActiveWorldEvent> activeEvents, WorldEventSavedData savedData) {
        if (level.players().isEmpty()) return;

        float avgLuck = 0;
        int count = 0;
        for (ServerPlayer player : level.players()) {
            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            if (data.getAperture().isOpened()) {
                avgLuck += data.getLuck();
                count++;
            }
        }
        if (count == 0) return;
        avgLuck /= count;

        float chance = BASE_TRIGGER_CHANCE + (avgLuck - 1.0f) * 0.05f;
        if (level.random.nextFloat() >= chance) return;

        WorldEventType[] types = WorldEventType.values();
        float totalWeight = 0;
        float[] weights = new float[types.length];
        for (int i = 0; i < types.length; i++) {
            if (types[i].isNegative()) {
                weights[i] = 1.0f + (1.0f - avgLuck) * 2.0f;
            } else {
                weights[i] = 1.0f + (avgLuck - 0.5f) * 1.5f;
            }
            weights[i] = Math.max(0.1f, weights[i]);
            totalWeight += weights[i];
        }

        float roll = level.random.nextFloat() * totalWeight;
        WorldEventType chosen = types[0];
        float cumulative = 0;
        for (int i = 0; i < types.length; i++) {
            cumulative += weights[i];
            if (roll < cumulative) {
                chosen = types[i];
                break;
            }
        }

        ActiveWorldEvent event = new ActiveWorldEvent(chosen);
        activeEvents.put(level.dimension(), event);
        savedData.setDirty();

        for (ServerPlayer player : level.players()) {
            player.displayClientMessage(
                Component.literal(chosen.getZhMessage()).withStyle(
                    chosen.isNegative()
                        ? net.minecraft.ChatFormatting.RED
                        : net.minecraft.ChatFormatting.GOLD),
                true);
        }
    }

    public static ActiveWorldEvent getActiveEvent(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            WorldEventSavedData savedData = WorldEventSavedData.get(serverLevel.getServer().overworld());
            return savedData.getActiveEvents().get(level.dimension());
        }
        return null;
    }

    public static boolean isEventActive(Level level, WorldEventType type) {
        ActiveWorldEvent event = getActiveEvent(level);
        return event != null && event.getType() == type;
    }

    public static void clearAll(MinecraftServer server) {
        WorldEventSavedData savedData = WorldEventSavedData.get(server.overworld());
        savedData.getActiveEvents().clear();
        savedData.setTickCounter(0);
        savedData.setDirty();
    }
}
