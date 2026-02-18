package com.reverendinsanity.core.cultivation;

// 空窍特性：投资真元发展不同方面，创造构建多样性
public enum ApertureFeature {
    SPIRIT_SPRING("灵泉", "真元恢复 +10%/级"),
    THOUGHT_POOL("念池", "念头恢复 +10%/级"),
    GU_GARDEN("蛊园", "蛊虫饥饿 -8%/级"),
    ESSENCE_VAULT("真元储", "真元上限 +8%/级"),
    THOUGHT_VAULT("念力储", "念头上限 +8%/级"),
    DAO_RESONANCE("道痕共鸣", "道痕积累 +10%/级");

    public static final int MAX_LEVEL = 5;

    private final String displayName;
    private final String description;

    ApertureFeature(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public float getUpgradeCost(int currentLevel) {
        return switch (currentLevel) {
            case 0 -> 0.5f;
            case 1 -> 1.0f;
            case 2 -> 2.0f;
            case 3 -> 4.0f;
            case 4 -> 8.0f;
            default -> Float.MAX_VALUE;
        };
    }

    public float getBonusPerLevel() {
        return switch (this) {
            case SPIRIT_SPRING, THOUGHT_POOL, DAO_RESONANCE -> 0.10f;
            case GU_GARDEN -> 0.08f;
            case ESSENCE_VAULT, THOUGHT_VAULT -> 0.08f;
        };
    }

    public float getBonus(int level) {
        return getBonusPerLevel() * level;
    }
}
