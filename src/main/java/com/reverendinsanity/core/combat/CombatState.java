package com.reverendinsanity.core.combat;

import com.reverendinsanity.core.cultivation.Aperture;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

// 战斗状态管理器
public class CombatState {

    private final Aperture aperture;
    private final Map<ResourceLocation, Long> cooldowns = new HashMap<>();
    private final Map<ResourceLocation, Integer> abilityCooldowns = new HashMap<>();
    private final List<ResourceLocation> equippedMoves = new ArrayList<>();
    private boolean inCombat = false;
    private long currentTick = 0;

    public CombatState(Aperture aperture) {
        this.aperture = aperture;
    }

    public void tick() {
        currentTick++;
        abilityCooldowns.entrySet().removeIf(entry -> {
            entry.setValue(entry.getValue() - 1);
            return entry.getValue() <= 0;
        });
        if (inCombat) {
            aperture.regenerateThoughts(0.5f);
        } else {
            aperture.regenerateEssence(aperture.getMaxEssence() * 0.002f);
            aperture.regenerateThoughts(2.0f);
        }
    }

    public void setMoveCooldown(ResourceLocation moveId, int cooldownTicks) {
        cooldowns.put(moveId, currentTick + cooldownTicks);
    }

    public boolean isMoveCooldown(ResourceLocation moveId) {
        Long cd = cooldowns.get(moveId);
        return cd != null && currentTick < cd;
    }

    public float calculateDamage(KillerMove move) {
        float base = move.power();
        float efficiencyMult = 1.0f;
        var grade = aperture.getEssenceGrade();
        if (grade != null) {
            efficiencyMult = grade.getEfficiency();
        }
        var pathRealm = aperture.getPathRealm(move.primaryPath());
        float pathMult = 1.0f + pathRealm.getTier() * 0.15f;
        return base * efficiencyMult * pathMult;
    }

    public void equipMove(ResourceLocation moveId) {
        if (!equippedMoves.contains(moveId)) {
            equippedMoves.add(moveId);
        }
    }

    public void unequipMove(ResourceLocation moveId) {
        equippedMoves.remove(moveId);
    }

    public void enterCombat() { inCombat = true; }
    public void exitCombat() { inCombat = false; }
    public boolean isInCombat() { return inCombat; }
    public List<ResourceLocation> getEquippedMoves() { return Collections.unmodifiableList(equippedMoves); }

    public boolean isAbilityOnCooldown(ResourceLocation guTypeId) {
        return abilityCooldowns.containsKey(guTypeId) && abilityCooldowns.get(guTypeId) > 0;
    }

    public void setAbilityCooldown(ResourceLocation guTypeId, int ticks) {
        abilityCooldowns.put(guTypeId, ticks);
    }

    public int getAbilityCooldownRemaining(ResourceLocation guTypeId) {
        return abilityCooldowns.getOrDefault(guTypeId, 0);
    }
}
