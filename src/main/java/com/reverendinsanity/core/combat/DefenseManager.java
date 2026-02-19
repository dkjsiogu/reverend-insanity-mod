package com.reverendinsanity.core.combat;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 主动防御系统：真元护盾+紧急闪避+反击窗口
public class DefenseManager {

    private static final Map<UUID, DefenseState> states = new ConcurrentHashMap<>();

    public static DefenseState getOrCreate(UUID playerId) {
        return states.computeIfAbsent(playerId, k -> new DefenseState());
    }

    public static void remove(UUID playerId) {
        states.remove(playerId);
    }

    public static boolean activateShield(ServerPlayer player) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return false;
        if (aperture.getCurrentEssence() < 50) return false;

        DefenseState state = getOrCreate(player.getUUID());
        if (state.shieldActive) return false;

        aperture.consumeEssence(50);
        state.shieldActive = true;
        state.shieldMaxHP = (float)(player.getMaxHealth() * 0.3);
        state.shieldHP = state.shieldMaxHP;
        state.shieldTicks = 100;

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.4f);
        return true;
    }

    public static boolean activateDodge(ServerPlayer player) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return false;
        if (aperture.getCurrentEssence() < 30) return false;

        DefenseState state = getOrCreate(player.getUUID());
        if (state.dodgeCooldown > 0) return false;

        aperture.consumeEssence(30);
        state.dodgeCooldown = 20;

        Vec3 look = player.getLookAngle();
        Vec3 dodge = new Vec3(-look.x, 0, -look.z).normalize().scale(2.0);
        player.teleportTo(player.getX() + dodge.x, player.getY(), player.getZ() + dodge.z);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.6f);
        return true;
    }

    public static float onHurt(ServerPlayer player, float damage) {
        DefenseState state = states.get(player.getUUID());
        if (state == null) return damage;

        if (state.shieldActive && state.shieldHP > 0) {
            float absorbed = Math.min(damage, state.shieldHP);
            state.shieldHP -= absorbed;
            damage -= absorbed;

            state.counterAttackWindow = 20;
            state.counterMultiplier = 1.5f;

            if (state.shieldHP <= 0) {
                state.shieldActive = false;
                state.shieldTicks = 0;
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 0.8f);
            }
        }
        return damage;
    }

    public static float getCounterMultiplier(ServerPlayer player) {
        DefenseState state = states.get(player.getUUID());
        if (state != null && state.counterAttackWindow > 0) {
            state.counterAttackWindow = 0;
            return state.counterMultiplier;
        }
        return 1.0f;
    }

    public static void tick(ServerPlayer player) {
        DefenseState state = states.get(player.getUUID());
        if (state == null) return;

        if (state.shieldActive) {
            state.shieldTicks--;
            if (state.shieldTicks <= 0) {
                state.shieldActive = false;
                state.shieldHP = 0;
            }
        }
        if (state.dodgeCooldown > 0) {
            state.dodgeCooldown--;
        }
        if (state.counterAttackWindow > 0) {
            state.counterAttackWindow--;
        }
    }

    public static boolean isShieldActive(UUID playerId) {
        DefenseState state = states.get(playerId);
        return state != null && state.shieldActive;
    }

    public static float getShieldHP(UUID playerId) {
        DefenseState state = states.get(playerId);
        return state != null ? state.shieldHP : 0;
    }

    public static float getShieldMaxHP(UUID playerId) {
        DefenseState state = states.get(playerId);
        return state != null ? state.shieldMaxHP : 0;
    }

    public static class DefenseState {
        public boolean shieldActive;
        public float shieldHP;
        public float shieldMaxHP;
        public int shieldTicks;
        public int dodgeCooldown;
        public int counterAttackWindow;
        public float counterMultiplier;
    }

    public static void clearAll() {
        states.clear();
    }
}
