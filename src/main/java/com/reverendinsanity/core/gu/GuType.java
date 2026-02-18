package com.reverendinsanity.core.gu;

import com.reverendinsanity.core.cultivation.Rank;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.resources.ResourceLocation;

// 蛊虫类型定义（不可变模板）
public record GuType(
    ResourceLocation id,
    String displayName,
    int rank,
    DaoPath path,
    GuCategory category,
    float essenceCost,
    float feedInterval,
    String feedItem
) {
    public boolean isImmortal() {
        return rank >= 6;
    }

    public boolean canBeUsedBy(Rank guMasterRank) {
        return guMasterRank.getLevel() >= this.rank;
    }

    public enum GuCategory {
        ATTACK("攻杀"),
        DEFENSE("防御"),
        MOVEMENT("移动"),
        DETECTION("侦查"),
        SUPPORT("辅助"),
        HEALING("治疗"),
        ENSLAVE("奴役"),
        SPECIAL("特殊");

        private final String displayName;
        GuCategory(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
