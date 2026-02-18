package com.reverendinsanity.core.event;

// 天地异象类型枚举
public enum WorldEventType {

    ESSENCE_STORM(2400, false,
        "\u5929\u5730\u7075\u6c14\u8e81\u52a8\uff0c\u771f\u5143\u6062\u590d\u52a0\u901f\uff01",
        "The world's vital essence surges, primeval essence recovery accelerated!"),

    DAO_MARK_SURGE(3600, false,
        "\u9053\u7684\u611f\u609f\u6d8c\u4e0a\u5fc3\u5934\uff0c\u6b64\u65f6\u4fee\u70bc\u4e8b\u534a\u529f\u500d\uff01",
        "Dao comprehension floods your mind, cultivation is twice as effective!"),

    RARE_GU_EMERGENCE(6000, false,
        "\u5929\u5730\u5f02\u53d8\uff0c\u7a00\u6709\u86ca\u866b\u73b0\u4e16\uff01",
        "Heaven and earth shift, rare Gu worms emerge!"),

    THOUGHTS_CLARITY(2400, false,
        "\u610f\u8bc6\u7a7a\u524d\u6e05\u660e\uff0c\u5ff5\u529b\u5145\u6c9b\uff01",
        "Your consciousness reaches unprecedented clarity, thoughts overflow!"),

    HEAVEN_WRATH(1200, true,
        "\u5929\u610f\u9707\u6012\uff0c\u4fee\u70bc\u53d7\u963b\uff01",
        "Heaven's wrath descends, cultivation is hindered!"),

    BEAST_TIDE(6000, true,
        "兽潮来袭，野兽疯狂涌来！",
        "Beast tide surges, wild beasts pour in!");

    private final int duration;
    private final boolean negative;
    private final String zhMessage;
    private final String enMessage;

    WorldEventType(int duration, boolean negative, String zhMessage, String enMessage) {
        this.duration = duration;
        this.negative = negative;
        this.zhMessage = zhMessage;
        this.enMessage = enMessage;
    }

    public int getDuration() { return duration; }
    public boolean isNegative() { return negative; }
    public String getZhMessage() { return zhMessage; }
    public String getEnMessage() { return enMessage; }
}
