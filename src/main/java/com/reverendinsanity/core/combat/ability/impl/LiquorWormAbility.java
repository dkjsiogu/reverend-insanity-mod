package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.CombatState;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 酒虫技能：消耗念头恢复真元
public class LiquorWormAbility extends GuAbility {

    private static final float THOUGHTS_COST = 2f;

    public LiquorWormAbility() {
        super(GuRegistry.id("liquor_worm"), 0f, 1200, AbilityType.INSTANT);
    }

    @Override
    public boolean canUse(ServerPlayer player, Aperture aperture, CombatState combatState) {
        if (!aperture.isOpened()) return false;
        if (aperture.getThoughts() < THOUGHTS_COST) return false;
        if (combatState.isAbilityOnCooldown(getGuTypeId())) return false;
        return aperture.getStoredGu().stream()
            .anyMatch(g -> g.getTypeId().equals(getGuTypeId()) && g.isActive());
    }

    @Override
    public boolean execute(ServerPlayer player, Aperture aperture, CombatState combatState) {
        if (!canUse(player, aperture, combatState)) return false;
        aperture.consumeThoughts(THOUGHTS_COST);
        combatState.setAbilityCooldown(getGuTypeId(), getCooldownTicks());
        onActivate(player, aperture);
        return true;
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float restoreAmount = aperture.getMaxEssence() * 0.3f;
        aperture.regenerateEssence(restoreAmount);
        player.displayClientMessage(Component.literal("酒虫催动，恢复 " + (int) restoreAmount + " 真元"), true);

        VfxHelper.spawn(player, VfxType.HEAL_SPIRAL,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFFFAA22, 1.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.8f, 1.0f);
    }
}
