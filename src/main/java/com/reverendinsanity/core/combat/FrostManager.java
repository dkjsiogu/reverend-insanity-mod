package com.reverendinsanity.core.combat;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.*;

// 冰霜效果管理器：追踪被减速/冻结的实体并自动清除
public class FrostManager {

    private static final List<FrostEffect> activeEffects = new ArrayList<>();
    private static final ResourceLocation FROST_SLOW_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "frost_slow");
    private static final ResourceLocation FROST_FREEZE_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "frost_freeze");

    public static void applySlow(LivingEntity entity, int durationTicks, double speedReduction) {
        removeExistingSlow(entity);
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            AttributeModifier mod = new AttributeModifier(FROST_SLOW_MOD,
                -speedReduction, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            speed.addTransientModifier(mod);
            activeEffects.add(new FrostEffect(entity, durationTicks, FrostType.SLOW));
        }
    }

    public static void applyFreeze(LivingEntity entity, int durationTicks) {
        removeExistingFreeze(entity);
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            AttributeModifier mod = new AttributeModifier(FROST_FREEZE_MOD,
                -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            speed.addTransientModifier(mod);
            activeEffects.add(new FrostEffect(entity, durationTicks, FrostType.FREEZE));
        }
    }

    public static void tick() {
        Iterator<FrostEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            FrostEffect effect = it.next();
            effect.remainingTicks--;
            if (effect.remainingTicks <= 0 || !effect.entity.isAlive()) {
                removeModifier(effect);
                it.remove();
            }
        }
    }

    private static void removeModifier(FrostEffect effect) {
        if (!effect.entity.isAlive()) return;
        AttributeInstance speed = effect.entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        if (effect.type == FrostType.SLOW) {
            speed.removeModifier(FROST_SLOW_MOD);
        } else {
            speed.removeModifier(FROST_FREEZE_MOD);
        }
    }

    private static void removeExistingSlow(LivingEntity entity) {
        activeEffects.removeIf(e -> {
            if (e.entity == entity && e.type == FrostType.SLOW) {
                removeModifier(e);
                return true;
            }
            return false;
        });
    }

    private static void removeExistingFreeze(LivingEntity entity) {
        activeEffects.removeIf(e -> {
            if (e.entity == entity && e.type == FrostType.FREEZE) {
                removeModifier(e);
                return true;
            }
            return false;
        });
    }

    public static void clearAll() {
        for (FrostEffect e : activeEffects) {
            removeModifier(e);
        }
        activeEffects.clear();
    }

    private enum FrostType { SLOW, FREEZE }

    private static class FrostEffect {
        final LivingEntity entity;
        int remainingTicks;
        final FrostType type;

        FrostEffect(LivingEntity entity, int ticks, FrostType type) {
            this.entity = entity;
            this.remainingTicks = ticks;
            this.type = type;
        }
    }
}
