package com.reverendinsanity.network;

import com.reverendinsanity.core.combat.IntelligenceManager;
import com.reverendinsanity.core.combat.MeritManager;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.EssenceGrade;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.cultivation.LifespanManager;
import com.reverendinsanity.core.faction.Faction;
import com.reverendinsanity.core.faction.FactionReputation;
import com.reverendinsanity.core.heavenwill.HeavenWillManager;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.entity.GuMasterEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ServerPlayerSyncDispatcher {

    private static final double TARGET_SCAN_DISTANCE = 64.0d;

    private ServerPlayerSyncDispatcher() {
    }

    public static void syncHud(ServerPlayer player, GuMasterData data) {
        Aperture aperture = data.getAperture();

        SyncGuMasterDataPayload payload = new SyncGuMasterDataPayload(
            aperture.isOpened(),
            aperture.getRank().getLevel(),
            aperture.getSubRank().getIndex(),
            aperture.getAptitude().getDisplayName(),
            aperture.getCurrentEssence(),
            aperture.getMaxEssence(),
            aperture.getThoughts(),
            aperture.getMaxThoughts(),
            resolveEssenceColor(aperture),
            aperture.getStoredGu().size(),
            data.getCombatState().getEquippedMoves().size(),
            data.getLuck(),
            resolvePrimaryPathMarks(aperture, data),
            encodeActiveBuffs(data),
            encodeFactionReputation(data.getFactionReputation()),
            data.getLifespan(),
            LifespanManager.getMaxLifespan(aperture.getRank().getLevel()),
            HeavenWillManager.getAttention(player),
            MeritManager.getMerit(player)
        );
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void syncTargetIntel(ServerPlayer player) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookDirection = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookDirection.scale(TARGET_SCAN_DISTANCE));
        AABB scanArea = player.getBoundingBox().expandTowards(lookDirection.scale(TARGET_SCAN_DISTANCE)).inflate(1.0d);

        EntityHitResult lookHit = ProjectileUtil.getEntityHitResult(
            player,
            eyePos,
            endPos,
            scanArea,
            entity -> entity instanceof GuMasterEntity && entity.isAlive(),
            TARGET_SCAN_DISTANCE * TARGET_SCAN_DISTANCE
        );

        if (lookHit != null && lookHit.getEntity() instanceof GuMasterEntity guMaster) {
            String info = IntelligenceManager.getDisplayInfo(player, guMaster);
            int intelLevel = IntelligenceManager.getIntelLevel(player, guMaster).ordinal();
            PacketDistributor.sendToPlayer(player, new IntelSyncPayload(
                guMaster.getId(),
                intelLevel,
                guMaster.getGuRank(),
                guMaster.getPrimaryDaoPath() != null ? guMaster.getPrimaryDaoPath().getDisplayName() : "",
                info
            ));
            return;
        }

        PacketDistributor.sendToPlayer(player, new IntelSyncPayload(-1, 0, 0, "", ""));
    }

    private static int resolveEssenceColor(Aperture aperture) {
        EssenceGrade grade = aperture.getEssenceGrade();
        return grade != null ? grade.getColor() : 0x00CC66;
    }

    private static String resolvePrimaryPathMarks(Aperture aperture, GuMasterData data) {
        DaoPath primaryPath = aperture.getPrimaryPath();
        if (primaryPath != null) {
            return primaryPath.getDisplayName() + ":" + data.getDaoMarks(primaryPath);
        }

        int maxMarks = 0;
        DaoPath maxPath = null;
        for (var entry : data.getAllDaoMarks().entrySet()) {
            if (entry.getValue() > maxMarks) {
                maxMarks = entry.getValue();
                maxPath = entry.getKey();
            }
        }

        if (maxPath == null) {
            return "";
        }
        return maxPath.getDisplayName() + ":" + maxMarks;
    }

    private static String encodeActiveBuffs(GuMasterData data) {
        StringBuilder buffBuilder = new StringBuilder();
        for (var buff : data.getBuffManager().getActiveBuffs()) {
            if (!buff.isActive()) {
                continue;
            }
            if (buffBuilder.length() > 0) {
                buffBuilder.append(",");
            }
            buffBuilder.append(buff.getId().getPath()).append("|").append(buff.getRemainingTicks());
        }
        return buffBuilder.toString();
    }

    private static String encodeFactionReputation(FactionReputation factionReputation) {
        return Faction.RIGHTEOUS.name() + ":" + factionReputation.getReputation(Faction.RIGHTEOUS)
            + "," + Faction.DEMONIC.name() + ":" + factionReputation.getReputation(Faction.DEMONIC)
            + "," + Faction.INDEPENDENT.name() + ":" + factionReputation.getReputation(Faction.INDEPENDENT);
    }
}
