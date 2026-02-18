package com.reverendinsanity.core.event;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;

// 天地事件持久化存储
public class WorldEventSavedData extends SavedData {

    private static final String DATA_NAME = "reverend_insanity_world_events";
    private final Map<ResourceKey<Level>, ActiveWorldEvent> activeEvents = new HashMap<>();
    private int tickCounter = 0;

    public static SavedData.Factory<WorldEventSavedData> factory() {
        return new SavedData.Factory<>(
            WorldEventSavedData::new,
            WorldEventSavedData::load
        );
    }

    public static WorldEventSavedData get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("tickCounter", tickCounter);
        CompoundTag eventsTag = new CompoundTag();
        for (var entry : activeEvents.entrySet()) {
            eventsTag.put(entry.getKey().location().toString(), entry.getValue().save());
        }
        tag.put("events", eventsTag);
        return tag;
    }

    public static WorldEventSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        WorldEventSavedData data = new WorldEventSavedData();
        data.tickCounter = tag.getInt("tickCounter");
        if (tag.contains("events")) {
            CompoundTag eventsTag = tag.getCompound("events");
            for (String key : eventsTag.getAllKeys()) {
                try {
                    ResourceLocation loc = ResourceLocation.parse(key);
                    ResourceKey<Level> dimKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, loc);
                    ActiveWorldEvent event = ActiveWorldEvent.load(eventsTag.getCompound(key));
                    if (event != null) {
                        data.activeEvents.put(dimKey, event);
                    }
                } catch (Exception ignored) {}
            }
        }
        return data;
    }

    public Map<ResourceKey<Level>, ActiveWorldEvent> getActiveEvents() { return activeEvents; }
    public int getTickCounter() { return tickCounter; }
    public void setTickCounter(int value) { tickCounter = value; }
}
