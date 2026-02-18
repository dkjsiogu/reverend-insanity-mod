package com.reverendinsanity.core.combat;

import com.reverendinsanity.core.combat.killermove.KillerMoveExecutor;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.combat.killermove.MoveEffectRegistry;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 状态杀招管理：BUFF/DEFENSE类杀招可切换开/关，持续消耗真元
public class ToggleMoveManager {

    private static final Map<UUID, Set<ResourceLocation>> activeMoves = new ConcurrentHashMap<>();
    private static final int DRAIN_INTERVAL = 20;
    private static final int REFRESH_INTERVAL = 100;

    public static boolean isToggleable(KillerMove move) {
        return move.moveType() == KillerMove.MoveType.BUFF || move.moveType() == KillerMove.MoveType.DEFENSE;
    }

    public static boolean isToggled(UUID uuid, ResourceLocation moveId) {
        Set<ResourceLocation> moves = activeMoves.get(uuid);
        return moves != null && moves.contains(moveId);
    }

    public static boolean toggleMove(ServerPlayer player, KillerMove move) {
        if (!isToggleable(move)) return false;
        UUID uuid = player.getUUID();

        if (isToggled(uuid, move.id())) {
            deactivate(player, move);
            return true;
        }

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        CombatState combat = data.getCombatState();

        if (KillerMoveExecutor.execute(player, aperture, combat, move)) {
            activeMoves.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(move.id());
            combat.setMoveCooldown(move.id(), 0);
            player.displayClientMessage(
                Component.literal("「" + move.displayName() + "」持续开启").withStyle(ChatFormatting.GREEN), true);
            return true;
        }
        return false;
    }

    private static void deactivate(ServerPlayer player, KillerMove move) {
        UUID uuid = player.getUUID();
        Set<ResourceLocation> moves = activeMoves.get(uuid);
        if (moves != null) {
            moves.remove(move.id());
            if (moves.isEmpty()) activeMoves.remove(uuid);
        }
        player.displayClientMessage(
            Component.literal("「" + move.displayName() + "」已关闭").withStyle(ChatFormatting.YELLOW), true);
    }

    public static void tickPlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Set<ResourceLocation> moves = activeMoves.get(uuid);
        if (moves == null || moves.isEmpty()) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        int tick = player.tickCount;

        Set<ResourceLocation> toRemove = new HashSet<>();
        for (ResourceLocation moveId : moves) {
            KillerMove move = KillerMoveRegistry.get(moveId);
            if (move == null) {
                toRemove.add(moveId);
                continue;
            }

            if (tick % DRAIN_INTERVAL == 0) {
                float drain = move.essenceCost() * 0.05f;
                if (aperture.getCurrentEssence() >= drain) {
                    aperture.consumeEssence(drain);
                } else {
                    toRemove.add(moveId);
                    player.displayClientMessage(
                        Component.literal("真元耗尽，「" + move.displayName() + "」自动关闭")
                            .withStyle(ChatFormatting.RED), false);
                }
            }

            if (tick % REFRESH_INTERVAL == 0) {
                MoveEffect effect = MoveEffectRegistry.resolve(move);
                float damage = KillerMoveExecutor.calculateDamage(aperture, move, data);
                effect.execute(player, aperture, move, damage);
            }
        }

        for (ResourceLocation id : toRemove) {
            moves.remove(id);
        }
        if (moves.isEmpty()) activeMoves.remove(uuid);
    }

    public static void onPlayerLogout(ServerPlayer player) {
        activeMoves.remove(player.getUUID());
    }

    public static Set<ResourceLocation> getActiveMoves(UUID uuid) {
        Set<ResourceLocation> moves = activeMoves.get(uuid);
        return moves != null ? Set.copyOf(moves) : Set.of();
    }
}
