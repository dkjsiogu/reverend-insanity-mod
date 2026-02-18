package com.reverendinsanity.core.gu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

// 蛊虫实例（可变状态：饥饿度、炼化状态等）
public class GuInstance {

    private final ResourceLocation typeId;
    private float hunger;
    private boolean refined;
    private int ticksSinceLastFed;
    private float proficiency;
    private boolean damaged;

    public GuInstance(ResourceLocation typeId) {
        this.typeId = typeId;
        this.hunger = 100f;
        this.refined = false;
        this.ticksSinceLastFed = 0;
        this.proficiency = 0f;
    }

    public GuInstance(ResourceLocation typeId, float hunger, boolean refined) {
        this.typeId = typeId;
        this.hunger = hunger;
        this.refined = refined;
        this.proficiency = 0f;
    }

    public void tick() {
        ticksSinceLastFed++;
        GuType type = GuRegistry.get(typeId);
        if (type != null && ticksSinceLastFed >= type.feedInterval() * 20) {
            hunger -= 5f;
            ticksSinceLastFed = 0;
        }
        hunger = Math.max(0, hunger);
    }

    public boolean feed() {
        if (hunger >= 100f) return false;
        hunger = Math.min(100f, hunger + 30f);
        ticksSinceLastFed = 0;
        return true;
    }

    public boolean refine(float essenceAvailable) {
        if (refined) return true;
        GuType type = GuRegistry.get(typeId);
        if (type == null) return false;
        if (essenceAvailable >= type.essenceCost() * 2) {
            refined = true;
            return true;
        }
        return false;
    }

    public boolean isAlive() {
        return hunger > 0;
    }

    public boolean isActive() {
        return refined && hunger > 20f;
    }

    public void addProficiency(float amount) {
        this.proficiency = Math.min(100f, this.proficiency + amount);
    }

    public float getProficiency() { return proficiency; }

    public float getProficiencyDamageBonus() {
        return 1.0f + proficiency * 0.0015f;
    }

    public float getProficiencyCooldownReduction() {
        return 1.0f - proficiency * 0.001f;
    }

    public float getProficiencyEssenceReduction() {
        return 1.0f - proficiency * 0.001f;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", typeId.toString());
        tag.putFloat("hunger", hunger);
        tag.putBoolean("refined", refined);
        tag.putInt("ticksSinceLastFed", ticksSinceLastFed);
        tag.putFloat("proficiency", proficiency);
        tag.putBoolean("damaged", damaged);
        return tag;
    }

    public static GuInstance load(CompoundTag tag) {
        if (!tag.contains("type")) return null;
        ResourceLocation typeId = ResourceLocation.parse(tag.getString("type"));
        GuInstance instance = new GuInstance(typeId);
        instance.hunger = tag.getFloat("hunger");
        instance.refined = tag.getBoolean("refined");
        instance.ticksSinceLastFed = tag.getInt("ticksSinceLastFed");
        instance.proficiency = tag.contains("proficiency") ? tag.getFloat("proficiency") : 0f;
        instance.damaged = tag.getBoolean("damaged");
        return instance;
    }

    public ResourceLocation getTypeId() { return typeId; }
    public GuType getType() { return GuRegistry.get(typeId); }
    public float getHunger() { return hunger; }
    public boolean isRefined() { return refined; }
    public boolean isDamaged() { return damaged; }
    public void setDamaged(boolean damaged) { this.damaged = damaged; }
    public float getDamageMultiplier() { return damaged ? 0.5f : 1.0f; }
}
