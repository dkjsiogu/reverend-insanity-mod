package com.reverendinsanity.core.cultivation;

import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 闭关系统：蛊师原地入定，加速获取道痕和真元，但不能移动
public class SeclusionManager {

    private static final Map<UUID, SeclusionState> activeSeclusions = new ConcurrentHashMap<>();

    private static final int MIN_DURATION = 200;
    private static final int MARK_INTERVAL = 100;
    private static final double MOVEMENT_THRESHOLD = 0.5;

    public static boolean enterSeclusion(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (activeSeclusions.containsKey(uuid)) {
            exitSeclusion(player, false);
            return true;
        }

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return false;

        activeSeclusions.put(uuid, new SeclusionState(
                player.getX(), player.getY(), player.getZ(), 0
        ));

        player.displayClientMessage(
                Component.literal("进入闭关...保持不动以获得修炼加成")
                        .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5f, 1.5f);

        return true;
    }

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();
        SeclusionState state = activeSeclusions.get(uuid);
        if (state == null) return;

        double dx = player.getX() - state.startX;
        double dy = player.getY() - state.startY;
        double dz = player.getZ() - state.startZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq > MOVEMENT_THRESHOLD * MOVEMENT_THRESHOLD) {
            exitSeclusion(player, state.ticksInSeclusion >= MIN_DURATION);
            return;
        }

        state.ticksInSeclusion++;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();

        aperture.regenerateEssence(aperture.getMaxEssence() * 0.005f);

        if (state.ticksInSeclusion % MARK_INTERVAL == 0) {
            DaoPath primary = aperture.getPrimaryPath();
            if (primary != null) {
                int bonus = 2 + (aperture.getRank().getLevel());
                data.addDaoMarks(primary, bonus);
            }
        }

        if (player.tickCount % 20 == 0 && player.level() instanceof ServerLevel level) {
            float progress = Math.min(1.0f, state.ticksInSeclusion / 600f);
            level.sendParticles(
                    new DustParticleOptions(new Vector3f(0.6f + progress * 0.4f, 0.3f, 1.0f - progress * 0.5f), 0.8f + progress),
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    3 + (int)(progress * 8), 0.3, 0.5, 0.3, 0);
        }

        if (state.ticksInSeclusion % 600 == 0 && state.ticksInSeclusion > 0) {
            int minutes = state.ticksInSeclusion / 1200;
            player.displayClientMessage(
                    Component.literal("闭关中... " + (minutes > 0 ? minutes + "分钟" : "30秒"))
                            .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
    }

    private static void exitSeclusion(ServerPlayer player, boolean successful) {
        UUID uuid = player.getUUID();
        SeclusionState state = activeSeclusions.remove(uuid);
        if (state == null) return;

        if (successful) {
            int seconds = state.ticksInSeclusion / 20;
            player.displayClientMessage(
                    Component.literal("闭关结束！修炼 " + seconds + " 秒，道痕和真元获得大量提升")
                            .withStyle(ChatFormatting.GREEN), false);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.2f);
        } else {
            player.displayClientMessage(
                    Component.literal("闭关中断").withStyle(ChatFormatting.GRAY), true);
        }
    }

    public static boolean isInSeclusion(ServerPlayer player) {
        return activeSeclusions.containsKey(player.getUUID());
    }

    public static void onPlayerDeath(ServerPlayer player) {
        activeSeclusions.remove(player.getUUID());
    }

    public static void onPlayerLogout(ServerPlayer player) {
        activeSeclusions.remove(player.getUUID());
    }

    private static class SeclusionState {
        double startX, startY, startZ;
        int ticksInSeclusion;

        SeclusionState(double x, double y, double z, int ticks) {
            this.startX = x; this.startY = y; this.startZ = z;
            this.ticksInSeclusion = ticks;
        }
    }
}
