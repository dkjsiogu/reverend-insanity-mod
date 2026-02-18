package com.reverendinsanity.entity;

import com.reverendinsanity.core.path.DaoPath;

// 十大九转尊者类型枚举
public enum VenerableType {
    YUAN_SHI("元始仙尊", "Primordial Origin", 0xFFFFD700, 1500, 30, 25, 0.34, DaoPath.QI, DaoPath.ENSLAVE),
    XING_XIU("星宿仙尊", "Star Constellation", 0xFF6699FF, 1200, 25, 20, 0.36, DaoPath.WISDOM, DaoPath.STAR),
    YUAN_LIAN("元莲仙尊", "Genesis Lotus", 0xFF33CC33, 1800, 22, 18, 0.32, DaoPath.WOOD, DaoPath.PAINT),
    JU_YANG("巨阳仙尊", "Giant Sun", 0xFFFF8800, 1300, 30, 25, 0.35, DaoPath.LUCK, DaoPath.BLOOD),
    LE_TU("乐土仙尊", "Paradise Earth", 0xFFC8A86E, 1600, 20, 30, 0.33, DaoPath.EARTH, DaoPath.HEAVEN),
    WU_JI("无极魔尊", "Limitless", 0xFF990099, 1400, 35, 28, 0.35, DaoPath.RULE, DaoPath.RESTRICTION),
    KUANG_MAN("狂蛮魔尊", "Reckless Savage", 0xFFCC0000, 2000, 40, 15, 0.40, DaoPath.STRENGTH, DaoPath.TRANSFORMATION),
    DAO_TIAN("盗天魔尊", "Thieving Heaven", 0xFF444444, 1100, 28, 22, 0.42, DaoPath.STEAL, DaoPath.SPACE),
    YOU_HUN("幽魂魔尊", "Spectral Soul", 0xFF330066, 1400, 35, 12, 0.37, DaoPath.SOUL, DaoPath.SHADOW),
    HONG_LIAN("红莲魔尊", "Red Lotus", 0xFFFF0033, 1200, 32, 20, 0.38, DaoPath.TIME, DaoPath.SPACE);

    public final String displayNameCN;
    public final String displayNameEN;
    public final int color;
    public final double maxHealth;
    public final double attackDamage;
    public final double armor;
    public final double moveSpeed;
    private final DaoPath primaryPath;
    private final DaoPath secondaryPath;

    VenerableType(String cn, String en, int color, double hp, double dmg, double armor, double speed, DaoPath primary, DaoPath secondary) {
        this.displayNameCN = cn;
        this.displayNameEN = en;
        this.color = color;
        this.maxHealth = hp;
        this.attackDamage = dmg;
        this.armor = armor;
        this.moveSpeed = speed;
        this.primaryPath = primary;
        this.secondaryPath = secondary;
    }

    public DaoPath getPrimaryPath() { return primaryPath; }
    public DaoPath getSecondaryPath() { return secondaryPath; }

    public boolean isDemon() {
        return this == WU_JI || this == KUANG_MAN || this == DAO_TIAN || this == YOU_HUN || this == HONG_LIAN;
    }

    public static VenerableType fromName(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return YUAN_SHI;
        }
    }
}
