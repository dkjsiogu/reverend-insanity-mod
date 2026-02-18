package com.reverendinsanity.core.path;

// 流派境界
public enum PathRealm {
    ORDINARY("普通", 0),
    MASTER("大师", 1),
    GRANDMASTER("宗师", 2),
    GREAT_GRANDMASTER("大宗师", 3),
    QUASI_SUPREME("准无上大宗师", 4),
    SUPREME("无上大宗师", 5),
    DAO_LORD("道主", 6);

    private final String displayName;
    private final int tier;

    PathRealm(String displayName, int tier) {
        this.displayName = displayName;
        this.tier = tier;
    }

    public String getDisplayName() { return displayName; }
    public int getTier() { return tier; }

    public PathRealm next() {
        int nextOrd = this.ordinal() + 1;
        if (nextOrd >= values().length) return null;
        return values()[nextOrd];
    }
}
