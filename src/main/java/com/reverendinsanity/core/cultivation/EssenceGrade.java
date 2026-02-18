package com.reverendinsanity.core.cultivation;

// 真元品质，每转有四个小阶段的真元颜色
public enum EssenceGrade {

    RANK1_INITIAL(Rank.RANK_1, SubRank.INITIAL, "翠绿", 0x00CC66, 1.0f),
    RANK1_MIDDLE(Rank.RANK_1, SubRank.MIDDLE, "苍绿", 0x009944, 1.25f),
    RANK1_UPPER(Rank.RANK_1, SubRank.UPPER, "深绿", 0x006633, 1.5f),
    RANK1_PEAK(Rank.RANK_1, SubRank.PEAK, "墨绿", 0x003311, 1.75f),

    RANK2_INITIAL(Rank.RANK_2, SubRank.INITIAL, "浅红", 0xFF9999, 3.0f),
    RANK2_MIDDLE(Rank.RANK_2, SubRank.MIDDLE, "绯红", 0xFF4444, 3.75f),
    RANK2_UPPER(Rank.RANK_2, SubRank.UPPER, "深红", 0xCC0000, 4.5f),
    RANK2_PEAK(Rank.RANK_2, SubRank.PEAK, "暗红", 0x880000, 5.25f),

    RANK3_INITIAL(Rank.RANK_3, SubRank.INITIAL, "淡银", 0xDDDDDD, 9.0f),
    RANK3_MIDDLE(Rank.RANK_3, SubRank.MIDDLE, "花银", 0xCCCCCC, 11.25f),
    RANK3_UPPER(Rank.RANK_3, SubRank.UPPER, "亮银", 0xBBBBBB, 13.5f),
    RANK3_PEAK(Rank.RANK_3, SubRank.PEAK, "雪银", 0xAAAAAA, 15.75f),

    RANK4_INITIAL(Rank.RANK_4, SubRank.INITIAL, "淡金", 0xFFEE88, 27.0f),
    RANK4_MIDDLE(Rank.RANK_4, SubRank.MIDDLE, "亮金", 0xFFDD44, 33.75f),
    RANK4_UPPER(Rank.RANK_4, SubRank.UPPER, "精金", 0xFFCC00, 40.5f),
    RANK4_PEAK(Rank.RANK_4, SubRank.PEAK, "真金", 0xDDAA00, 47.25f),

    RANK5_INITIAL(Rank.RANK_5, SubRank.INITIAL, "淡紫", 0xCC99FF, 81.0f),
    RANK5_MIDDLE(Rank.RANK_5, SubRank.MIDDLE, "嫣紫", 0xAA55FF, 101.25f),
    RANK5_UPPER(Rank.RANK_5, SubRank.UPPER, "深紫", 0x8822DD, 121.5f),
    RANK5_PEAK(Rank.RANK_5, SubRank.PEAK, "晶紫", 0x6600AA, 141.75f);

    private final Rank rank;
    private final SubRank subRank;
    private final String displayName;
    private final int color;
    private final float efficiency;

    EssenceGrade(Rank rank, SubRank subRank, String displayName, int color, float efficiency) {
        this.rank = rank;
        this.subRank = subRank;
        this.displayName = displayName;
        this.color = color;
        this.efficiency = efficiency;
    }

    public Rank getRank() { return rank; }
    public SubRank getSubRank() { return subRank; }
    public String getDisplayName() { return displayName; }
    public int getColor() { return color; }
    public float getEfficiency() { return efficiency; }

    public static EssenceGrade of(Rank rank, SubRank subRank) {
        for (EssenceGrade g : values()) {
            if (g.rank == rank && g.subRank == subRank) return g;
        }
        return null;
    }
}
