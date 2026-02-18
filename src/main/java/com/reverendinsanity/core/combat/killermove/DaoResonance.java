package com.reverendinsanity.core.combat.killermove;

import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.deduction.MoveBlueprint;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

// 道共鸣计算：同道蛊虫越多共鸣越高，道痕积累倍增杀招威力
public class DaoResonance {

    public static float calculate(KillerMove move) {
        DaoPath primary = move.primaryPath();
        DaoPath.Category primaryCategory = primary.getCategory();
        float resonance = 1.0f;

        for (ResourceLocation guId : move.getAllRequiredGu()) {
            GuType guType = GuRegistry.get(guId);
            if (guType == null) continue;

            DaoPath guPath = guType.path();
            if (guPath == primary) {
                resonance += 0.1f;
            } else if (guPath.getCategory() == primaryCategory) {
                resonance += 0.05f;
            } else {
                resonance -= 0.1f;
            }
        }

        return Mth.clamp(resonance, 0.7f, 1.5f);
    }

    public static float calculateWithDaoMarks(KillerMove move, GuMasterData data) {
        float baseResonance = calculate(move);
        float daoMarkMult = 1.0f + data.getDaoMarkBonus(move.primaryPath()) * 0.5f;
        boolean isImmortal = data.getAperture().getRank().isImmortal();
        if (isImmortal) {
            daoMarkMult += 0.2f;
        }
        return Mth.clamp(baseResonance * daoMarkMult, 0.5f, 3.0f);
    }

    public static float calculateForBlueprint(MoveBlueprint blueprint) {
        DaoPath primary = blueprint.targetPath();
        DaoPath.Category primaryCategory = primary.getCategory();
        float resonance = 1.0f;

        for (ResourceLocation guId : blueprint.getAllGu()) {
            GuType guType = GuRegistry.get(guId);
            if (guType == null) continue;

            DaoPath guPath = guType.path();
            if (guPath == primary) {
                resonance += 0.1f;
            } else if (guPath.getCategory() == primaryCategory) {
                resonance += 0.05f;
            } else {
                resonance -= 0.1f;
            }
        }

        return Mth.clamp(resonance, 0.7f, 1.5f);
    }
}
