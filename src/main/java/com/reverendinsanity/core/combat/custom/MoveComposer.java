package com.reverendinsanity.core.combat.custom;

import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.resources.ResourceLocation;
import java.util.*;;

// 杀招组合计算器：根据蛊虫搭配计算杀招属性
public class MoveComposer {

    public static final int MAX_SUPPORT_GU = 5;
    public static final int MAX_CUSTOM_MOVES = 8;

    public static CustomKillerMove compose(String name, ResourceLocation coreGuId, List<ResourceLocation> supportGuIds) {
        GuType coreType = GuRegistry.get(coreGuId);
        if (coreType == null) return null;
        if (supportGuIds.size() > MAX_SUPPORT_GU) return null;

        DaoPath primaryPath = coreType.path();
        KillerMove.MoveType moveType = determineMoveType(coreType, supportGuIds);
        float synergy = calculateSynergy(coreType, supportGuIds);
        float basePower = calculatePower(coreType, supportGuIds, synergy);
        float essenceCost = calculateEssenceCost(coreType, supportGuIds, synergy);
        float thoughtsCost = calculateThoughtsCost(coreType, supportGuIds, synergy);
        int cooldown = calculateCooldown(coreType, supportGuIds, synergy);

        return new CustomKillerMove(name, coreGuId, supportGuIds, primaryPath, moveType,
            basePower, essenceCost, thoughtsCost, cooldown, synergy);
    }

    public static float calculateSynergy(GuType coreType, List<ResourceLocation> supportGuIds) {
        float synergy = 1.0f;
        DaoPath corePath = coreType.path();
        DaoPath.Category coreCategory = corePath.getCategory();

        List<DaoPath> allPaths = new java.util.ArrayList<>();
        allPaths.add(corePath);

        for (ResourceLocation supId : supportGuIds) {
            GuType supType = GuRegistry.get(supId);
            if (supType == null) continue;

            allPaths.add(supType.path());

            if (supType.path() == corePath) {
                synergy += 0.15f;
            } else if (supType.path().getCategory() == coreCategory) {
                synergy += 0.08f;
            } else {
                synergy -= 0.05f;
            }

            if (supType.category() == coreType.category()) {
                synergy += 0.05f;
            }
        }

        synergy += calculateComplementBonus(allPaths);

        List<PathReactionRegistry.ReactionEffect> reactions = PathReactionRegistry.findReactions(allPaths);
        synergy += reactions.size() * 0.15f;

        Map<DaoPath, Integer> counts = new EnumMap<>(DaoPath.class);
        for (DaoPath p : allPaths) counts.merge(p, 1, Integer::sum);
        synergy += PathStackingRule.check(counts).size() * 0.10f;

        return Math.max(0.5f, Math.min(2.0f, synergy));
    }

    private static float calculateComplementBonus(List<DaoPath> paths) {
        float bonus = 0;
        for (int i = 0; i < paths.size(); i++) {
            for (int j = i + 1; j < paths.size(); j++) {
                if (isComplementary(paths.get(i), paths.get(j))) {
                    bonus += 0.12f;
                }
            }
        }
        return bonus;
    }

    private static boolean isComplementary(DaoPath a, DaoPath b) {
        return (a == DaoPath.ICE && b == DaoPath.FIRE) || (a == DaoPath.FIRE && b == DaoPath.ICE)
            || (a == DaoPath.LIGHT && b == DaoPath.DARK) || (a == DaoPath.DARK && b == DaoPath.LIGHT)
            || (a == DaoPath.SPACE && b == DaoPath.TIME) || (a == DaoPath.TIME && b == DaoPath.SPACE)
            || (a == DaoPath.WIND && b == DaoPath.EARTH) || (a == DaoPath.EARTH && b == DaoPath.WIND)
            || (a == DaoPath.WATER && b == DaoPath.FIRE) || (a == DaoPath.FIRE && b == DaoPath.WATER)
            || (a == DaoPath.BLOOD && b == DaoPath.SOUL) || (a == DaoPath.SOUL && b == DaoPath.BLOOD)
            || (a == DaoPath.SWORD && b == DaoPath.BLADE) || (a == DaoPath.BLADE && b == DaoPath.SWORD)
            || (a == DaoPath.DREAM && b == DaoPath.ILLUSION) || (a == DaoPath.ILLUSION && b == DaoPath.DREAM)
            || (a == DaoPath.STRENGTH && b == DaoPath.BONE) || (a == DaoPath.BONE && b == DaoPath.STRENGTH)
            || (a == DaoPath.POISON && b == DaoPath.PILL) || (a == DaoPath.PILL && b == DaoPath.POISON);
    }

    public static float calculatePower(GuType coreType, List<ResourceLocation> supportGuIds, float synergy) {
        float basePower = coreType.rank() * 15f;

        for (ResourceLocation supId : supportGuIds) {
            GuType supType = GuRegistry.get(supId);
            if (supType == null) continue;

            float contribution = supType.rank() * 5f;
            if (supType.path() == coreType.path()) {
                contribution *= 1.3f;
            } else {
                contribution *= 0.8f;
            }

            if (supType.category() == GuType.GuCategory.ATTACK) {
                contribution *= 1.5f;
            }
            basePower += contribution;
        }

        return basePower * synergy;
    }

    public static float calculateEssenceCost(GuType coreType, List<ResourceLocation> supportGuIds, float synergy) {
        float cost = coreType.essenceCost() * 2f;

        for (ResourceLocation supId : supportGuIds) {
            GuType supType = GuRegistry.get(supId);
            if (supType == null) continue;

            float supCost = supType.essenceCost() * 1.2f;
            if (supType.category() == GuType.GuCategory.SUPPORT) {
                supCost *= 0.5f;
            }
            cost += supCost;
        }

        float synergyDiscount = (synergy - 1.0f) * 0.3f;
        return Math.max(5f, cost * (1f - synergyDiscount));
    }

    public static float calculateThoughtsCost(GuType coreType, List<ResourceLocation> supportGuIds, float synergy) {
        float base = 10f + supportGuIds.size() * 8f;
        float rankFactor = coreType.rank() * 5f;
        float synergyDiscount = (synergy - 1.0f) * 0.2f;
        return Math.max(3f, (base + rankFactor) * (1f - synergyDiscount));
    }

    public static int calculateCooldown(GuType coreType, List<ResourceLocation> supportGuIds, float synergy) {
        int base = 60 + supportGuIds.size() * 20 + coreType.rank() * 10;
        float synergyDiscount = (synergy - 1.0f) * 0.15f;
        return Math.max(20, (int)(base * (1f - synergyDiscount)));
    }

    public static ValidationResult validate(Aperture aperture, ResourceLocation coreGuId, List<ResourceLocation> supportGuIds) {
        if (!aperture.isOpened()) return ValidationResult.NOT_OPENED;
        if (coreGuId == null) return ValidationResult.NO_CORE;
        if (supportGuIds.size() > MAX_SUPPORT_GU) return ValidationResult.TOO_MANY_SUPPORT;

        GuType coreType = GuRegistry.get(coreGuId);
        if (coreType == null) return ValidationResult.INVALID_GU;
        if (!coreType.canBeUsedBy(aperture.getRank())) return ValidationResult.RANK_TOO_LOW;

        boolean coreFound = false;
        for (GuInstance gu : aperture.getStoredGu()) {
            if (gu.getTypeId().equals(coreGuId) && gu.isActive()) {
                coreFound = true;
                break;
            }
        }
        if (!coreFound) return ValidationResult.MISSING_GU;

        for (ResourceLocation supId : supportGuIds) {
            if (supId.equals(coreGuId)) return ValidationResult.DUPLICATE_GU;
            GuType supType = GuRegistry.get(supId);
            if (supType == null) return ValidationResult.INVALID_GU;
            if (!supType.canBeUsedBy(aperture.getRank())) return ValidationResult.RANK_TOO_LOW;

            boolean supFound = false;
            for (GuInstance gu : aperture.getStoredGu()) {
                if (gu.getTypeId().equals(supId) && gu.isActive()) {
                    supFound = true;
                    break;
                }
            }
            if (!supFound) return ValidationResult.MISSING_GU;
        }

        for (int i = 0; i < supportGuIds.size(); i++) {
            for (int j = i + 1; j < supportGuIds.size(); j++) {
                if (supportGuIds.get(i).equals(supportGuIds.get(j))) {
                    return ValidationResult.DUPLICATE_GU;
                }
            }
        }

        return ValidationResult.OK;
    }

    public static KillerMove.MoveType determineMoveType(GuType coreType, List<ResourceLocation> supportGuIds) {
        KillerMove.MoveType base = categoryToMoveType(coreType.category());
        List<DaoPath> paths = new ArrayList<>();
        paths.add(coreType.path());
        for (ResourceLocation id : supportGuIds) {
            GuType t = GuRegistry.get(id);
            if (t != null) paths.add(t.path());
        }
        for (PathReactionRegistry.ReactionEffect r : PathReactionRegistry.findReactions(paths)) {
            if (r.moveTypeOverride() != null) return r.moveTypeOverride();
        }
        return base;
    }

    public static KillerMove.MoveType categoryToMoveType(GuType.GuCategory category) {
        return switch (category) {
            case ATTACK -> KillerMove.MoveType.ATTACK;
            case DEFENSE -> KillerMove.MoveType.DEFENSE;
            case MOVEMENT -> KillerMove.MoveType.MOVEMENT;
            case DETECTION -> KillerMove.MoveType.CONTROL;
            case SUPPORT -> KillerMove.MoveType.BUFF;
            case HEALING -> KillerMove.MoveType.HEAL;
            case ENSLAVE -> KillerMove.MoveType.CONTROL;
            case SPECIAL -> KillerMove.MoveType.ULTIMATE;
        };
    }

    public enum ValidationResult {
        OK(""),
        NOT_OPENED("空窍未开"),
        NO_CORE("需要选择核心蛊"),
        TOO_MANY_SUPPORT("辅助蛊数量超过上限"),
        INVALID_GU("蛊虫无效"),
        RANK_TOO_LOW("境界不足"),
        MISSING_GU("窍穴中缺少所需蛊虫"),
        DUPLICATE_GU("不能重复使用同一蛊虫");

        private final String message;
        ValidationResult(String message) { this.message = message; }
        public String getMessage() { return message; }
        public boolean isOk() { return this == OK; }
    }
}
