package com.reverendinsanity.core.combat;

import com.reverendinsanity.core.cultivation.Rank;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

// 杀招定义：核心蛊 + 辅助蛊组合产生的战斗招式
public record KillerMove(
    ResourceLocation id,
    String displayName,
    DaoPath primaryPath,
    int minRank,
    ResourceLocation coreGu,
    List<ResourceLocation> supportGu,
    float essenceCost,
    float thoughtsCost,
    float power,
    int cooldownTicks,
    MoveType moveType
) {
    public boolean canUse(Rank userRank) {
        return userRank.getLevel() >= minRank;
    }

    public List<ResourceLocation> getAllRequiredGu() {
        List<ResourceLocation> all = new ArrayList<>();
        all.add(coreGu);
        all.addAll(supportGu);
        return all;
    }

    public int getGuCount() {
        return getAllRequiredGu().size();
    }

    public enum MoveType {
        ATTACK("攻击"),
        DEFENSE("防御"),
        MOVEMENT("移动"),
        CONTROL("控制"),
        HEAL("治疗"),
        BUFF("增益"),
        DEBUFF("减益"),
        ULTIMATE("必杀");

        private final String displayName;
        MoveType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
