package com.reverendinsanity.core.faction;

import net.minecraft.nbt.CompoundTag;
import java.util.EnumMap;
import java.util.Map;

// 势力声望追踪：正道/魔道互斥，声望等级影响交易/伤害
public class FactionReputation {

    private final Map<Faction, Integer> reputation = new EnumMap<>(Faction.class);

    public enum ReputationTier {
        HOSTILE("敌对", -1000, -500),
        UNFRIENDLY("不友好", -499, -100),
        NEUTRAL("中立", -99, 99),
        FRIENDLY("友好", 100, 499),
        HONORED("崇敬", 500, 1000);

        private final String displayName;
        private final int min, max;

        ReputationTier(String displayName, int min, int max) {
            this.displayName = displayName;
            this.min = min;
            this.max = max;
        }

        public String getDisplayName() { return displayName; }

        public static ReputationTier fromValue(int value) {
            for (ReputationTier tier : values()) {
                if (value >= tier.min && value <= tier.max) return tier;
            }
            return value < -1000 ? HOSTILE : HONORED;
        }
    }

    public FactionReputation() {
        reputation.put(Faction.RIGHTEOUS, 0);
        reputation.put(Faction.DEMONIC, 0);
        reputation.put(Faction.INDEPENDENT, 50);
    }

    public void addReputation(Faction faction, int amount) {
        int current = reputation.getOrDefault(faction, 0);
        reputation.put(faction, clamp(current + amount));

        if (faction == Faction.RIGHTEOUS && amount > 0) {
            int demonic = reputation.getOrDefault(Faction.DEMONIC, 0);
            reputation.put(Faction.DEMONIC, clamp(demonic - amount / 2));
        } else if (faction == Faction.DEMONIC && amount > 0) {
            int righteous = reputation.getOrDefault(Faction.RIGHTEOUS, 0);
            reputation.put(Faction.RIGHTEOUS, clamp(righteous - amount / 2));
        }
    }

    public int getReputation(Faction faction) {
        return reputation.getOrDefault(faction, 0);
    }

    public ReputationTier getTier(Faction faction) {
        return ReputationTier.fromValue(getReputation(faction));
    }

    public float getTradeDiscount(Faction faction) {
        return switch (getTier(faction)) {
            case HOSTILE -> 1.5f;
            case UNFRIENDLY -> 1.2f;
            case NEUTRAL -> 1.0f;
            case FRIENDLY -> 0.85f;
            case HONORED -> 0.7f;
        };
    }

    public float getDamageBonus(Faction faction) {
        return switch (getTier(faction)) {
            case FRIENDLY -> 1.05f;
            case HONORED -> 1.10f;
            default -> 1.0f;
        };
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<Faction, Integer> entry : reputation.entrySet()) {
            tag.putInt(entry.getKey().name(), entry.getValue());
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        for (Faction faction : Faction.values()) {
            if (tag.contains(faction.name())) {
                reputation.put(faction, tag.getInt(faction.name()));
            }
        }
    }

    private static int clamp(int value) {
        return Math.max(-1000, Math.min(1000, value));
    }
}
