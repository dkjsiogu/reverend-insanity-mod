package com.reverendinsanity.core.combat;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import java.util.*;

// 持续伤害管理器：追踪毒素/流血/凋零DOT并每秒结算伤害
public class DotManager {

    private static final List<DotEffect> activeEffects = new ArrayList<>();

    public static void applyPoison(LivingEntity target, LivingEntity source, float damagePerSecond, int durationTicks) {
        removeExisting(target, DotType.POISON);
        activeEffects.add(new DotEffect(target, source, DotType.POISON, damagePerSecond, Math.max(20, durationTicks)));
    }

    public static void applyBleed(LivingEntity target, LivingEntity source, float damagePerSecond, int durationTicks) {
        removeExisting(target, DotType.BLEED);
        activeEffects.add(new DotEffect(target, source, DotType.BLEED, damagePerSecond, Math.max(20, durationTicks)));
    }

    public static void applyWither(LivingEntity target, LivingEntity source, float damagePerSecond, int durationTicks) {
        removeExisting(target, DotType.WITHER);
        activeEffects.add(new DotEffect(target, source, DotType.WITHER, damagePerSecond, Math.max(20, durationTicks)));
    }

    public static void tick() {
        Iterator<DotEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            DotEffect dot = it.next();
            dot.remainingTicks--;

            if (dot.remainingTicks <= 0 || !dot.target.isAlive()) {
                it.remove();
                continue;
            }

            dot.tickCounter++;
            if (dot.tickCounter % 20 == 0) {
                dot.target.hurt(dot.target.damageSources().magic(), dot.damagePerSecond);
            }
            if (dot.tickCounter % 10 == 0 && dot.target.level() instanceof ServerLevel sl) {
                ParticleOptions particle = switch (dot.type) {
                    case POISON -> ParticleTypes.ITEM_SLIME;
                    case BLEED -> ParticleTypes.DAMAGE_INDICATOR;
                    case WITHER -> ParticleTypes.SMOKE;
                };
                sl.sendParticles(particle, dot.target.getX(), dot.target.getY() + 1, dot.target.getZ(),
                    3, 0.3, 0.4, 0.3, 0.01);
            }
        }
    }

    private static void removeExisting(LivingEntity target, DotType type) {
        activeEffects.removeIf(e -> e.target == target && e.type == type);
    }

    public static void clearAll() {
        activeEffects.clear();
    }

    private enum DotType { POISON, BLEED, WITHER }

    private static class DotEffect {
        final LivingEntity target;
        final LivingEntity source;
        final DotType type;
        final float damagePerSecond;
        int remainingTicks;
        int tickCounter;

        DotEffect(LivingEntity target, LivingEntity source, DotType type, float dps, int ticks) {
            this.target = target;
            this.source = source;
            this.type = type;
            this.damagePerSecond = dps;
            this.remainingTicks = ticks;
            this.tickCounter = 0;
        }
    }
}
