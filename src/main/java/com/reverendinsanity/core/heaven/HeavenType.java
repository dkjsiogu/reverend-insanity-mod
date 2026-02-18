package com.reverendinsanity.core.heaven;

// 太古九天：九个远古世界的残片
public enum HeavenType {
    WHITE("白天", 0xFFFFFFFF, "purification"),
    RED("赤天", 0xFFFF4444, "destruction"),
    ORANGE("橙天", 0xFFFF8800, "illumination"),
    YELLOW("黄天", 0xFFFFDD00, "trade"),
    GREEN("绿天", 0xFF44BB44, "growth"),
    CYAN("青天", 0xFF00CCCC, "bamboo"),
    BLUE("蓝天", 0xFF4488FF, "starlight"),
    PURPLE("紫天", 0xFFAA44FF, "mystery"),
    BLACK("黑天", 0xFF222222, "void");

    private final String displayName;
    private final int color;
    private final String aspect;

    HeavenType(String displayName, int color, String aspect) {
        this.displayName = displayName;
        this.color = color;
        this.aspect = aspect;
    }

    public String getDisplayName() { return displayName; }
    public int getColor() { return color; }
    public String getAspect() { return aspect; }
}
