package com.reverendinsanity.core.combat.buff;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.util.*;

// 蛊虫增益管理器，管理玩家所有活跃的自定义增益效果
public class GuBuffManager {

    private final Map<ResourceLocation, GuBuff> activeBuffs = new LinkedHashMap<>();
    private final List<GlowTarget> glowingTargets = new ArrayList<>();
    private final List<CursedTarget> cursedTargets = new ArrayList<>();
    private boolean managedInvisibility = false;

    public void applyBuff(ServerPlayer player, GuBuff buff) {
        GuBuff existing = activeBuffs.get(buff.getId());
        if (existing != null && existing.isActive()) {
            existing.remove(player);
        }
        activeBuffs.put(buff.getId(), buff);
        buff.apply(player);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.3f, 1.6f);
        if (player.isInvisible()) managedInvisibility = true;
    }

    public void removeBuff(ServerPlayer player, ResourceLocation id) {
        GuBuff buff = activeBuffs.remove(id);
        if (buff != null && buff.isActive()) {
            buff.remove(player);
        }
    }

    public void tick(ServerPlayer player) {
        Iterator<Map.Entry<ResourceLocation, GuBuff>> it = activeBuffs.entrySet().iterator();
        while (it.hasNext()) {
            GuBuff buff = it.next().getValue();
            buff.tick(player);
            if (buff.isExpired() || !buff.isActive()) {
                it.remove();
            }
        }
        if (managedInvisibility && !player.isInvisible()) {
            managedInvisibility = false;
        }
        tickGlowing();
        tickCursed();
    }

    public void clearAll(ServerPlayer player) {
        for (GuBuff buff : activeBuffs.values()) {
            if (buff.isActive()) buff.remove(player);
        }
        activeBuffs.clear();
        if (managedInvisibility) {
            player.setInvisible(false);
            managedInvisibility = false;
        }
        clearGlowing();
        cursedTargets.clear();
    }

    public float processIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        List<GuBuff> toBreak = new ArrayList<>();
        for (GuBuff buff : activeBuffs.values()) {
            if (!buff.isActive()) continue;
            amount = buff.modifyIncomingDamage(player, source, amount);
            if (buff.shouldBreakOnDamage()) {
                toBreak.add(buff);
            }
        }
        for (GuBuff buff : toBreak) {
            buff.remove(player);
        }
        return Math.max(0, amount);
    }

    public float processOutgoingDamage(ServerPlayer player, LivingEntity target, float amount) {
        for (GuBuff buff : activeBuffs.values()) {
            if (buff.isActive()) {
                amount = buff.modifyOutgoingDamage(player, target, amount);
            }
        }
        return amount;
    }

    public boolean shouldPreventTargeting(ServerPlayer player, LivingEntity attacker) {
        for (GuBuff buff : activeBuffs.values()) {
            if (buff.isActive() && buff.preventMobTargeting(player, attacker)) {
                return true;
            }
        }
        return false;
    }

    public void onPlayerAttack(ServerPlayer player, LivingEntity target) {
        for (GuBuff buff : new ArrayList<>(activeBuffs.values())) {
            if (buff.isActive()) {
                buff.onPlayerAttack(player, target);
            }
        }
    }

    public void addGlowingTarget(Entity entity, int durationTicks) {
        entity.setGlowingTag(true);
        glowingTargets.add(new GlowTarget(entity, durationTicks));
    }

    private void tickGlowing() {
        Iterator<GlowTarget> it = glowingTargets.iterator();
        while (it.hasNext()) {
            GlowTarget target = it.next();
            target.remainingTicks--;
            if (target.remainingTicks <= 0 || !target.entity.isAlive()) {
                if (target.entity.isAlive()) {
                    target.entity.setGlowingTag(false);
                }
                it.remove();
            }
        }
    }

    private void clearGlowing() {
        for (GlowTarget target : glowingTargets) {
            if (target.entity.isAlive()) {
                target.entity.setGlowingTag(false);
            }
        }
        glowingTargets.clear();
    }

    public GuBuff getBuff(ResourceLocation id) { return activeBuffs.get(id); }
    public boolean hasBuff(ResourceLocation id) {
        GuBuff buff = activeBuffs.get(id);
        return buff != null && buff.isActive();
    }
    public Collection<GuBuff> getActiveBuffs() { return Collections.unmodifiableCollection(activeBuffs.values()); }

    public void addCursedTarget(Entity entity, int durationTicks, float retributionPercent) {
        cursedTargets.removeIf(c -> c.entity == entity);
        cursedTargets.add(new CursedTarget(entity, durationTicks, retributionPercent));
    }

    public boolean isCursed(Entity entity) {
        for (CursedTarget ct : cursedTargets) {
            if (ct.entity == entity && ct.entity.isAlive()) return true;
        }
        return false;
    }

    public float getCursedRetribution(Entity entity) {
        for (CursedTarget ct : cursedTargets) {
            if (ct.entity == entity) return ct.retributionPercent;
        }
        return 0f;
    }

    private void tickCursed() {
        Iterator<CursedTarget> it = cursedTargets.iterator();
        while (it.hasNext()) {
            CursedTarget ct = it.next();
            ct.remainingTicks--;
            if (ct.remainingTicks <= 0 || !ct.entity.isAlive()) {
                it.remove();
            }
        }
    }

    private static class GlowTarget {
        final Entity entity;
        int remainingTicks;
        GlowTarget(Entity entity, int ticks) {
            this.entity = entity;
            this.remainingTicks = ticks;
        }
    }

    private static class CursedTarget {
        final Entity entity;
        int remainingTicks;
        final float retributionPercent;
        CursedTarget(Entity entity, int ticks, float percent) {
            this.entity = entity;
            this.remainingTicks = ticks;
            this.retributionPercent = percent;
        }
    }
}
