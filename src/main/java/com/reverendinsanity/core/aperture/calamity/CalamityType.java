package com.reverendinsanity.core.aperture.calamity;

import com.reverendinsanity.core.aperture.BlessedLandGrade;

// 灾劫类型：天劫(稀有强力)和地灾(常见弱力)
public enum CalamityType {
    EARTH_CRACK("地裂", Category.EARTH_DISASTER, 10, 5.0f),
    BEAST_TIDE("兽潮", Category.EARTH_DISASTER, 15, 8.0f),
    FIRE_SPREAD("火灾", Category.EARTH_DISASTER, 8, 3.0f),
    VOID_EROSION("虚空侵蚀", Category.EARTH_DISASTER, 20, 12.0f),
    THUNDER_TRIBULATION("雷劫", Category.HEAVENLY_TRIBULATION, 30, 20.0f),
    SILVER_SERPENT("银角青鳞蟒", Category.HEAVENLY_TRIBULATION, 50, 35.0f),
    CHAOS_STORM("混沌风暴", Category.HEAVENLY_TRIBULATION, 40, 25.0f);

    private final String displayName;
    private final Category category;
    private final int baseDuration;
    private final float baseDamage;

    CalamityType(String displayName, Category category, int baseDuration, float baseDamage) {
        this.displayName = displayName;
        this.category = category;
        this.baseDuration = baseDuration;
        this.baseDamage = baseDamage;
    }

    public boolean isHeavenlyTribulation() {
        return category == Category.HEAVENLY_TRIBULATION;
    }

    public float getScaledDamage(BlessedLandGrade grade) {
        return baseDamage * (1 + grade.ordinal() * 0.5f);
    }

    public int getScaledDuration(BlessedLandGrade grade) {
        return baseDuration * 20;
    }

    public String getDisplayName() { return displayName; }
    public Category getCategory() { return category; }
    public int getBaseDuration() { return baseDuration; }
    public float getBaseDamage() { return baseDamage; }

    public enum Category {
        EARTH_DISASTER("地灾"),
        HEAVENLY_TRIBULATION("天劫");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }
}
