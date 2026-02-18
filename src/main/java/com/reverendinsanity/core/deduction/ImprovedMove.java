package com.reverendinsanity.core.deduction;

import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.KillerMoveRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import java.util.Random;

// 改良杀招：在原始杀招基础上的增强版本
public class ImprovedMove {

    private static final String[] LEVEL_NAMES = {"初", "良", "精", "极", "完美"};
    private static final Random RANDOM = new Random();

    private final ResourceLocation baseMoveId;
    private int improvementLevel;
    private float powerMultiplier;
    private float costMultiplier;
    private float cooldownMultiplier;

    public ImprovedMove(KillerMove baseMove) {
        this.baseMoveId = baseMove.id();
        this.improvementLevel = 0;
        this.powerMultiplier = 1.0f;
        this.costMultiplier = 1.0f;
        this.cooldownMultiplier = 1.0f;
    }

    private ImprovedMove(ResourceLocation baseMoveId, int level, float power, float cost, float cooldown) {
        this.baseMoveId = baseMoveId;
        this.improvementLevel = level;
        this.powerMultiplier = power;
        this.costMultiplier = cost;
        this.cooldownMultiplier = cooldown;
    }

    public KillerMove getBaseMove() {
        return KillerMoveRegistry.get(baseMoveId);
    }

    public ResourceLocation getBaseMoveId() {
        return baseMoveId;
    }

    public float getEffectivePower() {
        KillerMove base = getBaseMove();
        return base != null ? base.power() * powerMultiplier : 0;
    }

    public float getEffectiveEssenceCost() {
        KillerMove base = getBaseMove();
        return base != null ? base.essenceCost() * costMultiplier : 0;
    }

    public float getEffectiveThoughtsCost() {
        KillerMove base = getBaseMove();
        return base != null ? base.thoughtsCost() * costMultiplier : 0;
    }

    public int getEffectiveCooldown() {
        KillerMove base = getBaseMove();
        return base != null ? (int)(base.cooldownTicks() * cooldownMultiplier) : 0;
    }

    public boolean improve() {
        if (!canImprove()) return false;
        improvementLevel++;
        int roll = RANDOM.nextInt(3);
        switch (roll) {
            case 0 -> powerMultiplier += 0.15f;
            case 1 -> costMultiplier = Math.max(0.3f, costMultiplier - 0.15f);
            case 2 -> cooldownMultiplier = Math.max(0.3f, cooldownMultiplier - 0.20f);
        }
        return true;
    }

    public String getLevelName() {
        return LEVEL_NAMES[Math.min(improvementLevel, LEVEL_NAMES.length - 1)];
    }

    public boolean canImprove() {
        return improvementLevel < 4;
    }

    public int getImprovementLevel() {
        return improvementLevel;
    }

    public float getPowerMultiplier() { return powerMultiplier; }
    public float getCostMultiplier() { return costMultiplier; }
    public float getCooldownMultiplier() { return cooldownMultiplier; }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("baseMoveId", baseMoveId.toString());
        tag.putInt("level", improvementLevel);
        tag.putFloat("power", powerMultiplier);
        tag.putFloat("cost", costMultiplier);
        tag.putFloat("cooldown", cooldownMultiplier);
        return tag;
    }

    public static ImprovedMove load(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.parse(tag.getString("baseMoveId"));
        return new ImprovedMove(
            id,
            tag.getInt("level"),
            tag.getFloat("power"),
            tag.getFloat("cost"),
            tag.getFloat("cooldown")
        );
    }
}
