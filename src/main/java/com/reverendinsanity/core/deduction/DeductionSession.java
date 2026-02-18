package com.reverendinsanity.core.deduction;

import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.KillerMoveRegistry;
import com.reverendinsanity.core.combat.custom.MoveComposer;
import com.reverendinsanity.core.combat.custom.PathReactionRegistry;
import com.reverendinsanity.core.combat.custom.PathStackingRule;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

// 推演会话：管理一次推演的完整过程
public class DeductionSession {

    private final UUID playerId;
    private final MoveBlueprint blueprint;
    private final int totalTicks;
    private int currentTick;
    private boolean active;
    private boolean cancelled;
    private final float essenceCost;
    private final float thoughtsCost;
    private final float successRate;

    public DeductionSession(UUID playerId, MoveBlueprint blueprint, float successRate, int rankLevel) {
        this.playerId = playerId;
        this.blueprint = blueprint;
        this.successRate = successRate;
        this.active = true;
        this.cancelled = false;
        this.currentTick = 0;

        int guCount = blueprint.getGuCount();
        boolean crossPath = hasCrossPathGu(blueprint);
        this.totalTicks = 100 + guCount * 60 + (crossPath ? 100 : 0);

        float rankScale = Math.max(0.5f, 1.0f - (rankLevel - 1) * 0.1f);
        this.essenceCost = (500 + guCount * 200) * rankScale;
        this.thoughtsCost = 50 + guCount * 20;
    }

    private static boolean hasCrossPathGu(MoveBlueprint blueprint) {
        DaoPath target = blueprint.targetPath();
        for (ResourceLocation guId : blueprint.getAllGu()) {
            GuType type = GuRegistry.get(guId);
            if (type != null && type.path() != target) return true;
        }
        return false;
    }

    public void tick() {
        if (!active || cancelled) return;
        currentTick++;
    }

    public boolean isComplete() {
        return currentTick >= totalTicks;
    }

    public float getProgress() {
        return (float) currentTick / totalTicks;
    }

    public void cancel() {
        this.cancelled = true;
        this.active = false;
    }

    public DeductionResult resolve(Random random) {
        active = false;

        if (cancelled) {
            return new DeductionResult(
                DeductionResult.Outcome.FAILURE, null, 0, 5f, "推演被取消"
            );
        }

        float roll = random.nextFloat();
        KillerMove matched = DeductionManager.findMatchingMove(blueprint);

        if (roll < successRate * 0.3f) {
            return resolveGreatSuccess(matched, random);
        } else if (roll < successRate) {
            return resolveSuccess(matched, random);
        } else if (roll < successRate + (1.0f - successRate) * 0.3f) {
            return resolvePartial(matched);
        } else if (roll > 0.95f && matched == null) {
            return resolveDiscovery(random);
        } else {
            return new DeductionResult(
                DeductionResult.Outcome.FAILURE, null, 0,
                10f + blueprint.getGuCount() * 2f,
                "推演失败，蛊虫组合未能产生共鸣"
            );
        }
    }

    private DeductionResult resolveGreatSuccess(KillerMove matched, Random random) {
        if (matched != null) {
            int improveLevel = 1 + random.nextInt(2);
            return new DeductionResult(
                DeductionResult.Outcome.GREAT_SUCCESS, matched, improveLevel,
                30f + blueprint.getGuCount() * 5f,
                "大成功！领悟了" + matched.displayName() + "的精妙之处"
            );
        }
        KillerMove generated = generateRandomMove(random);
        return new DeductionResult(
            DeductionResult.Outcome.GREAT_SUCCESS, generated, 1,
            35f + blueprint.getGuCount() * 5f,
            "大成功！创造出全新杀招：" + generated.displayName()
        );
    }

    private DeductionResult resolveSuccess(KillerMove matched, Random random) {
        if (matched != null) {
            return new DeductionResult(
                DeductionResult.Outcome.SUCCESS, matched, 0,
                20f + blueprint.getGuCount() * 3f,
                "成功推演出杀招：" + matched.displayName()
            );
        }
        KillerMove generated = generateRandomMove(random);
        return new DeductionResult(
            DeductionResult.Outcome.SUCCESS, generated, 0,
            25f + blueprint.getGuCount() * 3f,
            "推演成功，领悟新杀招：" + generated.displayName()
        );
    }

    private DeductionResult resolvePartial(KillerMove matched) {
        if (matched != null) {
            return new DeductionResult(
                DeductionResult.Outcome.PARTIAL, matched, 0,
                15f + blueprint.getGuCount() * 2f,
                "部分成功，勉强领悟" + matched.displayName() + "的残缺版本"
            );
        }
        return new DeductionResult(
            DeductionResult.Outcome.PARTIAL, null, 0,
            15f + blueprint.getGuCount() * 2f,
            "部分成功，隐约有所感悟但未能凝实"
        );
    }

    private DeductionResult resolveDiscovery(Random random) {
        KillerMove generated = generateRandomMove(random);
        return new DeductionResult(
            DeductionResult.Outcome.DISCOVERY, generated, 0,
            40f + blueprint.getGuCount() * 5f,
            "意外发现！蛊虫产生了意想不到的共鸣：" + generated.displayName()
        );
    }

    private KillerMove generateRandomMove(Random random) {
        GuType coreType = GuRegistry.get(blueprint.coreGu());

        float variance = 0.85f + random.nextFloat() * 0.3f;
        float basePower;
        float baseCost;
        float baseThoughts;
        int baseCooldown;
        KillerMove.MoveType moveType;

        if (coreType != null) {
            moveType = MoveComposer.determineMoveType(coreType, blueprint.supportGu());
            float synergy = MoveComposer.calculateSynergy(coreType, blueprint.supportGu());
            basePower = MoveComposer.calculatePower(coreType, blueprint.supportGu(), synergy) * variance;
            baseCost = MoveComposer.calculateEssenceCost(coreType, blueprint.supportGu(), synergy) * variance;
            baseThoughts = MoveComposer.calculateThoughtsCost(coreType, blueprint.supportGu(), synergy) * variance;
            baseCooldown = (int)(MoveComposer.calculateCooldown(coreType, blueprint.supportGu(), synergy) * variance);
        } else {
            KillerMove.MoveType[] types = KillerMove.MoveType.values();
            moveType = types[random.nextInt(types.length)];
            basePower = 10f + blueprint.getGuCount() * 5f + random.nextFloat() * 10f;
            baseCost = essenceCost * 0.1f + random.nextFloat() * 20f;
            baseThoughts = thoughtsCost * 0.5f;
            baseCooldown = 200 + blueprint.getGuCount() * 50 + random.nextInt(200);
        }

        String name = generateMoveName(blueprint, moveType);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
            "reverend_insanity", "deduced_" + System.currentTimeMillis()
        );

        return new KillerMove(
            id, name, blueprint.targetPath(), 1,
            blueprint.coreGu(), blueprint.supportGu(),
            baseCost, baseThoughts, basePower, baseCooldown, moveType
        );
    }

    private String generateMoveName(MoveBlueprint blueprint, KillerMove.MoveType moveType) {
        List<DaoPath> allPaths = new ArrayList<>();
        allPaths.add(blueprint.targetPath());
        for (ResourceLocation guId : blueprint.supportGu()) {
            GuType t = GuRegistry.get(guId);
            if (t != null) allPaths.add(t.path());
        }

        List<PathReactionRegistry.ReactionEffect> reactions = PathReactionRegistry.findReactions(allPaths);
        if (!reactions.isEmpty()) {
            return reactions.get(0).name();
        }

        Map<DaoPath, Integer> counts = new EnumMap<>(DaoPath.class);
        for (DaoPath p : allPaths) counts.merge(p, 1, Integer::sum);
        List<PathStackingRule.StackThreshold> stacks = PathStackingRule.check(counts);
        if (!stacks.isEmpty()) {
            return stacks.get(stacks.size() - 1).description();
        }

        return blueprint.targetPath().getDisplayName() + "之" + moveType.getDisplayName();
    }

    public UUID getPlayerId() { return playerId; }
    public MoveBlueprint getBlueprint() { return blueprint; }
    public int getTotalTicks() { return totalTicks; }
    public int getCurrentTick() { return currentTick; }
    public boolean isActive() { return active; }
    public boolean isCancelled() { return cancelled; }
    public float getEssenceCost() { return essenceCost; }
    public float getThoughtsCost() { return thoughtsCost; }
    public float getSuccessRate() { return successRate; }
}
