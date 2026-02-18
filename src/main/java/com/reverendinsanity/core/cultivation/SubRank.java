package com.reverendinsanity.core.cultivation;

// 蛊师境界内的小境界：初阶、中阶、上阶、巅峰
public enum SubRank {
    INITIAL("初阶", 0),
    MIDDLE("中阶", 1),
    UPPER("上阶", 2),
    PEAK("巅峰", 3);

    private final String displayName;
    private final int index;

    SubRank(String displayName, int index) {
        this.displayName = displayName;
        this.index = index;
    }

    public String getDisplayName() { return displayName; }
    public int getIndex() { return index; }

    public SubRank next() {
        int nextOrd = this.ordinal() + 1;
        if (nextOrd >= values().length) return null;
        return values()[nextOrd];
    }
}
