package com.reverendinsanity.core.path;

// 蛊修流派（大道），原著48种流派
public enum DaoPath {
    STRENGTH("力道", Category.COMBAT),
    BLOOD("血道", Category.COMBAT),
    SWORD("剑道", Category.COMBAT),
    BLADE("刀道", Category.COMBAT),
    KILL("杀道", Category.COMBAT),
    SOLDIER("兵道", Category.COMBAT),

    SOUL("魂道", Category.SPIRITUAL),
    DREAM("梦道", Category.SPIRITUAL),
    WISDOM("智道", Category.SPIRITUAL),
    CHARM("魅情道", Category.SPIRITUAL),
    ILLUSION("幻道", Category.SPIRITUAL),

    FIRE("炎道", Category.ELEMENTAL),
    WATER("水道", Category.ELEMENTAL),
    EARTH("土道", Category.ELEMENTAL),
    METAL("金道", Category.ELEMENTAL),
    WOOD("木道", Category.ELEMENTAL),
    WIND("风道", Category.ELEMENTAL),
    LIGHTNING("雷道", Category.ELEMENTAL),
    ICE("冰雪道", Category.ELEMENTAL),
    LIGHT("光道", Category.ELEMENTAL),
    DARK("暗道", Category.ELEMENTAL),
    SHADOW("影道", Category.ELEMENTAL),
    CLOUD("云道", Category.ELEMENTAL),

    SPACE("宇道", Category.RULE),
    TIME("宙道", Category.RULE),
    RULE("律道", Category.RULE),
    LUCK("运道", Category.RULE),
    HEAVEN("天道", Category.RULE),
    HUMAN("人道", Category.RULE),
    STAR("星道", Category.RULE),

    REFINEMENT("炼道", Category.SUPPORT),
    FORMATION("阵道", Category.SUPPORT),
    PILL("丹道", Category.SUPPORT),
    ENSLAVE("奴道", Category.SUPPORT),
    FOOD("食道", Category.SUPPORT),
    PAINT("画道", Category.SUPPORT),
    STEAL("偷道", Category.SUPPORT),
    BONE("骨道", Category.SUPPORT),
    SOUND("音道", Category.SUPPORT),
    INFORMATION("信道", Category.SUPPORT),

    POISON("毒道", Category.SPECIAL),
    TRANSFORMATION("变化道", Category.SPECIAL),
    YIN_YANG("阴阳道", Category.SPECIAL),
    FLIGHT("飞行道", Category.SPECIAL),
    MOON("月道", Category.SPECIAL),
    QI("气道", Category.SPECIAL),
    VOID("虚道", Category.SPECIAL),
    RESTRICTION("禁道", Category.SPECIAL);

    private final String displayName;
    private final Category category;

    DaoPath(String displayName, Category category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() { return displayName; }
    public Category getCategory() { return category; }

    public int getColor() {
        return switch (this) {
            case STRENGTH -> 0xCC4400;
            case BLOOD -> 0xAA0000;
            case SWORD -> 0xCCCCFF;
            case BLADE -> 0x888888;
            case KILL -> 0x660000;
            case SOLDIER -> 0x886633;
            case SOUL -> 0x8866FF;
            case DREAM -> 0xCC88FF;
            case WISDOM -> 0x4488CC;
            case CHARM -> 0xFF66AA;
            case ILLUSION -> 0xAA66CC;
            case FIRE -> 0xFF4400;
            case WATER -> 0x2288FF;
            case EARTH -> 0x886622;
            case METAL -> 0xFFDD00;
            case WOOD -> 0x22AA22;
            case WIND -> 0x88FFCC;
            case LIGHTNING -> 0xFFFF00;
            case ICE -> 0x88DDFF;
            case LIGHT -> 0xFFFFCC;
            case DARK -> 0x330066;
            case SHADOW -> 0x333355;
            case CLOUD -> 0xCCCCDD;
            case SPACE -> 0x2222CC;
            case TIME -> 0xCCAA44;
            case RULE -> 0xFFCC00;
            case LUCK -> 0x00CC44;
            case HEAVEN -> 0xDDDDFF;
            case HUMAN -> 0xFFAA88;
            case STAR -> 0xFFFFAA;
            case REFINEMENT -> 0xFF6600;
            case FORMATION -> 0x4466AA;
            case PILL -> 0x44CC88;
            case ENSLAVE -> 0x664488;
            case FOOD -> 0xCC8844;
            case PAINT -> 0xFF44FF;
            case STEAL -> 0x444444;
            case BONE -> 0xDDDDCC;
            case SOUND -> 0x66CCCC;
            case INFORMATION -> 0x44AACC;
            case POISON -> 0x44AA00;
            case TRANSFORMATION -> 0xCC44CC;
            case YIN_YANG -> 0xAAAAAA;
            case FLIGHT -> 0xAADDFF;
            case MOON -> 0xCCDDFF;
            case QI -> 0x88CCAA;
            case VOID -> 0x220044;
            case RESTRICTION -> 0xAA4444;
        };
    }

    public enum Category {
        COMBAT("战斗"),
        SPIRITUAL("精神"),
        ELEMENTAL("元素"),
        RULE("规则"),
        SUPPORT("辅助"),
        SPECIAL("特殊");

        private final String displayName;
        Category(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
