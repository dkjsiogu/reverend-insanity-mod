package com.reverendinsanity.core.combat.buff;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

// 蛊虫增益效果基类，用Attribute修改和事件拦截替代药水效果
public abstract class GuBuff {

    private final ResourceLocation id;
    private int remainingTicks;
    private boolean active;

    protected GuBuff(ResourceLocation id, int durationTicks) {
        this.id = id;
        this.remainingTicks = durationTicks;
        this.active = false;
    }

    public void apply(ServerPlayer player) {
        this.active = true;
        onApply(player);
    }

    public void remove(ServerPlayer player) {
        if (!active) return;
        this.active = false;
        onRemove(player);
    }

    public void tick(ServerPlayer player) {
        if (!active) return;
        remainingTicks--;
        onTick(player);
        if (remainingTicks <= 0) {
            remove(player);
        }
    }

    protected abstract void onApply(ServerPlayer player);
    protected abstract void onRemove(ServerPlayer player);
    protected void onTick(ServerPlayer player) {}

    public float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) { return amount; }
    public float modifyOutgoingDamage(ServerPlayer player, LivingEntity target, float amount) { return amount; }
    public boolean preventMobTargeting(ServerPlayer player, LivingEntity attacker) { return false; }
    public void onPlayerAttack(ServerPlayer player, LivingEntity target) {}
    public boolean shouldBreakOnDamage() { return false; }

    public boolean isExpired() { return remainingTicks <= 0; }
    public boolean isActive() { return active; }
    public ResourceLocation getId() { return id; }
    public int getRemainingTicks() { return remainingTicks; }
}
