package com.reverendinsanity.core.cultivation;

// 蛊师资质，决定真元占空窍比例和可达到的最高境界
public enum Aptitude {
    NONE(0, 0f, 0, "无资质"),
    D(1, 0.25f, 2, "丁等"),
    C(2, 0.45f, 3, "丙等"),
    B(3, 0.65f, 4, "乙等"),
    A(4, 0.85f, 5, "甲等"),
    EXTREME(5, 0.98f, 5, "十绝体");

    private final int tier;
    private final float essenceRatio;
    private final int maxRank;
    private final String displayName;

    Aptitude(int tier, float essenceRatio, int maxRank, String displayName) {
        this.tier = tier;
        this.essenceRatio = essenceRatio;
        this.maxRank = maxRank;
        this.displayName = displayName;
    }

    public int getTier() { return tier; }
    public float getEssenceRatio() { return essenceRatio; }
    public int getMaxRank() { return maxRank; }
    public String getDisplayName() { return displayName; }

    public boolean canAdvanceTo(Rank rank) {
        return rank.getLevel() <= maxRank;
    }
}
