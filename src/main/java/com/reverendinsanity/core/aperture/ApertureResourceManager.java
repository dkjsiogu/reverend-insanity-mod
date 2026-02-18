package com.reverendinsanity.core.aperture;

import net.minecraft.nbt.CompoundTag;
import java.util.*;

// 仙窍资源管理：自动产出蛊虫喂食材料、仙元、灵药
public class ApertureResourceManager {

    public enum ResourceType {
        PRIMEVAL_STONE("元石", 1.0f),
        MOON_PETAL("月桂花瓣", 0.3f),
        BEAST_BONE("兽骨", 0.5f),
        BITTER_WINE("苦酒", 0.2f),
        SPIDER_SILK("蛛丝", 0.4f),
        JADE_BEAD("玉眼石珠", 0.15f),
        IMMORTAL_ESSENCE("仙元石", 0.1f);

        private final String name;
        private final float baseRate;

        ResourceType(String name, float baseRate) {
            this.name = name;
            this.baseRate = baseRate;
        }

        public String getDisplayName() { return name; }
        public float getBaseRate() { return baseRate; }
    }

    private final Map<ResourceType, Integer> storedResources = new EnumMap<>(ResourceType.class);

    public void tickProduction(BlessedLandGrade grade, float integrity, Random random) {
        float modifier = grade.getResourceDensity() * (integrity / 100f);
        for (ResourceType type : ResourceType.values()) {
            float chance = type.baseRate * modifier;
            if (random.nextFloat() < chance) {
                storedResources.merge(type, 1, Integer::sum);
            }
        }
    }

    public int getResource(ResourceType type) {
        return storedResources.getOrDefault(type, 0);
    }

    public boolean consumeResource(ResourceType type, int amount) {
        int current = storedResources.getOrDefault(type, 0);
        if (current < amount) return false;
        storedResources.put(type, current - amount);
        return true;
    }

    public Map<ResourceType, Integer> getAllResources() {
        return Collections.unmodifiableMap(new EnumMap<>(storedResources));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<ResourceType, Integer> entry : storedResources.entrySet()) {
            tag.putInt(entry.getKey().name(), entry.getValue());
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        storedResources.clear();
        for (ResourceType type : ResourceType.values()) {
            if (tag.contains(type.name())) {
                storedResources.put(type, tag.getInt(type.name()));
            }
        }
    }
}
