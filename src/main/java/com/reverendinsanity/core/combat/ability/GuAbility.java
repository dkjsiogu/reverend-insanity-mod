package com.reverendinsanity.core.combat.ability;

import com.reverendinsanity.core.combat.CombatState;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.path.DaoMarkTracker;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.reverendinsanity.core.path.DaoPathSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

// 蛊虫单独催动技能，兼容性影响真元消耗和冷却
public abstract class GuAbility {

    private final ResourceLocation guTypeId;
    private final float essenceCost;
    private final int cooldownTicks;
    private final AbilityType abilityType;

    protected GuAbility(ResourceLocation guTypeId, float essenceCost, int cooldownTicks, AbilityType abilityType) {
        this.guTypeId = guTypeId;
        this.essenceCost = essenceCost;
        this.cooldownTicks = cooldownTicks;
        this.abilityType = abilityType;
    }

    protected abstract void onActivate(ServerPlayer player, Aperture aperture);

    private float getAdjustedEssenceCost(Aperture aperture) {
        GuType guType = GuRegistry.get(guTypeId);
        if (guType == null) return essenceCost;
        float compatibility = aperture.getPathCompatibility(guType.path());
        return essenceCost / compatibility;
    }

    private int getAdjustedCooldown(Aperture aperture) {
        GuType guType = GuRegistry.get(guTypeId);
        if (guType == null) return cooldownTicks;
        float compatibility = aperture.getPathCompatibility(guType.path());
        return Math.max(10, (int)(cooldownTicks / compatibility));
    }

    public boolean canUse(ServerPlayer player, Aperture aperture, CombatState combatState) {
        if (!aperture.isOpened()) return false;
        if (aperture.getCurrentEssence() < getAdjustedEssenceCost(aperture)) return false;
        if (combatState.isAbilityOnCooldown(guTypeId)) return false;
        return aperture.getStoredGu().stream()
            .anyMatch(g -> g.getTypeId().equals(guTypeId) && g.isActive());
    }

    public boolean execute(ServerPlayer player, Aperture aperture, CombatState combatState) {
        if (!canUse(player, aperture, combatState)) return false;
        float adjustedCost = getAdjustedEssenceCost(aperture);
        int adjustedCooldown = getAdjustedCooldown(aperture);
        aperture.consumeEssence(adjustedCost);
        combatState.setAbilityCooldown(guTypeId, adjustedCooldown);
        combatState.enterCombat();
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        GuType guType = GuRegistry.get(guTypeId);
        DaoPath path = guType != null ? guType.path() : DaoPath.STRENGTH;
        SoundEvent sound = DaoPathSounds.getAbilitySound(path);
        float volume = DaoPathSounds.getAbilityVolume(path);
        float pitch = DaoPathSounds.getAbilityPitch(path);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            sound, SoundSource.PLAYERS, volume, pitch);
        onActivate(player, aperture);
        if (guType != null) {
            DaoMarkTracker.onAbilityUsed(player, guType.path());
        }
        GuInstance guInst = aperture.findGuInstance(guTypeId);
        if (guInst != null) {
            guInst.addProficiency(0.5f);
        }
        return true;
    }

    public ResourceLocation getGuTypeId() { return guTypeId; }
    public float getEssenceCost() { return essenceCost; }
    public int getCooldownTicks() { return cooldownTicks; }
    public AbilityType getAbilityType() { return abilityType; }

    public enum AbilityType {
        PROJECTILE, BUFF, INSTANT
    }
}
