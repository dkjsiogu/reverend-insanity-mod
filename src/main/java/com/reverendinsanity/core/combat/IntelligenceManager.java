package com.reverendinsanity.core.combat;

import com.reverendinsanity.entity.GuMasterEntity;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 情报等级管理器：管理玩家对NPC蛊师的情报等级（注视/侦察/完全扫描）
public class IntelligenceManager {

    public enum IntelLevel {
        UNKNOWN, OBSERVED, SCANNED, FULL
    }

    private static final Map<UUID, Map<UUID, IntelLevel>> intelMap = new ConcurrentHashMap<>();
    private static final Map<UUID, ObservationTracker> observationTrackers = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> keenEarBonusTicks = new ConcurrentHashMap<>();

    public static IntelLevel getIntelLevel(ServerPlayer player, Entity target) {
        Map<UUID, IntelLevel> playerIntel = intelMap.get(player.getUUID());
        if (playerIntel == null) return IntelLevel.UNKNOWN;
        return playerIntel.getOrDefault(target.getUUID(), IntelLevel.UNKNOWN);
    }

    public static void setIntelLevel(ServerPlayer player, Entity target, IntelLevel level) {
        Map<UUID, IntelLevel> playerIntel = intelMap.computeIfAbsent(player.getUUID(), k -> new ConcurrentHashMap<>());
        IntelLevel current = playerIntel.getOrDefault(target.getUUID(), IntelLevel.UNKNOWN);
        if (level.ordinal() > current.ordinal()) {
            playerIntel.put(target.getUUID(), level);
        }
    }

    public static void setKeenEarBonus(ServerPlayer player, int ticks) {
        keenEarBonusTicks.put(player.getUUID(), ticks);
    }

    public static boolean hasKeenEarBonus(ServerPlayer player) {
        Integer ticks = keenEarBonusTicks.get(player.getUUID());
        return ticks != null && ticks > 0;
    }

    public static void tickObservation(ServerPlayer player) {
        Integer keenTicks = keenEarBonusTicks.get(player.getUUID());
        if (keenTicks != null) {
            keenTicks--;
            if (keenTicks <= 0) {
                keenEarBonusTicks.remove(player.getUUID());
            } else {
                keenEarBonusTicks.put(player.getUUID(), keenTicks);
            }
        }

        double observeRange = hasKeenEarBonus(player) ? 64.0 : 32.0;
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(observeRange));
        AABB searchArea = player.getBoundingBox().expandTowards(lookVec.scale(observeRange)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            player, eyePos, endPos, searchArea,
            e -> e instanceof GuMasterEntity && e.isAlive(), observeRange * observeRange);

        if (entityHit != null) {
            Entity target = entityHit.getEntity();
            ObservationTracker tracker = observationTrackers.computeIfAbsent(player.getUUID(), k -> new ObservationTracker());
            if (tracker.targetUUID != null && tracker.targetUUID.equals(target.getUUID())) {
                tracker.ticks++;
                if (tracker.ticks >= 40) {
                    setIntelLevel(player, target, IntelLevel.OBSERVED);
                    tracker.ticks = 0;
                }
            } else {
                tracker.targetUUID = target.getUUID();
                tracker.ticks = 1;
            }
            return;
        }
        ObservationTracker tracker = observationTrackers.get(player.getUUID());
        if (tracker != null) {
            tracker.ticks = 0;
            tracker.targetUUID = null;
        }
    }

    public static void onScoutAbilityUsed(ServerPlayer player, int range, IntelLevel level) {
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class,
            player.getBoundingBox().inflate(range), e -> e instanceof GuMasterEntity && e.isAlive());
        for (LivingEntity target : targets) {
            setIntelLevel(player, target, level);
        }
    }

    public static String getDisplayInfo(ServerPlayer player, GuMasterEntity master) {
        IntelLevel level = getIntelLevel(player, master);
        String faction = master.getFaction().getDisplayName();
        return switch (level) {
            case UNKNOWN -> "[" + faction + "] \u86ca\u5e08";
            case OBSERVED -> {
                int rank = master.getGuRank();
                String rankRange = rank <= 1 ? "\u4e00\u8f6c~\u4e8c\u8f6c" : rank == 2 ? "\u4e8c\u8f6c~\u4e09\u8f6c" : "\u4e09\u8f6c+";
                yield "[" + faction + "] \u86ca\u5e08 (" + rankRange + ")";
            }
            case SCANNED -> {
                int rank = master.getGuRank();
                DaoPath path = master.getPrimaryDaoPath();
                String pathName = path != null ? path.getDisplayName() : "\u672a\u77e5";
                yield "[" + faction + "] " + getRankName(rank) + "\u86ca\u5e08\u00b7" + pathName;
            }
            case FULL -> {
                int rank = master.getGuRank();
                DaoPath primary = master.getPrimaryDaoPath();
                DaoPath secondary = master.getSecondaryDaoPath();
                String pathName = primary != null ? primary.getDisplayName() : "\u672a\u77e5";
                StringBuilder sb = new StringBuilder();
                sb.append("[").append(faction).append("] ").append(getRankName(rank)).append("\u86ca\u5e08\u00b7").append(pathName);
                if (secondary != null) {
                    sb.append("/").append(secondary.getDisplayName());
                }
                sb.append(" \u86ca:").append(master.getEquippedGu().size());
                sb.append(" \u62db:").append(master.getAvailableMoves().size());
                yield sb.toString();
            }
        };
    }

    private static String getRankName(int rank) {
        return switch (rank) {
            case 1 -> "\u4e00\u8f6c";
            case 2 -> "\u4e8c\u8f6c";
            case 3 -> "\u4e09\u8f6c";
            case 4 -> "\u56db\u8f6c";
            case 5 -> "\u4e94\u8f6c";
            default -> rank + "\u8f6c";
        };
    }

    public static void clearPlayer(UUID playerUUID) {
        intelMap.remove(playerUUID);
        observationTrackers.remove(playerUUID);
        keenEarBonusTicks.remove(playerUUID);
    }

    public static void clearAll() {
        intelMap.clear();
        observationTrackers.clear();
        keenEarBonusTicks.clear();
    }

    private static class ObservationTracker {
        UUID targetUUID;
        int ticks;
    }
}
