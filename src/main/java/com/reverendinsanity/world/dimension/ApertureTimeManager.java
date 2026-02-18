package com.reverendinsanity.world.dimension;

import com.reverendinsanity.core.aperture.ImmortalAperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

// 光阴支流：仙窍维度内时间加速，福地等级决定时间流速比
public class ApertureTimeManager {

    public static void tickTimeAcceleration(ServerPlayer player) {
        if (!player.level().dimension().equals(ModDimensions.APERTURE_DIM)) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        ImmortalAperture ap = data.getImmortalAperture();
        if (!ap.isFormed()) return;

        int rate = ap.getGrade().getTimeFlowRate();
        for (int i = 1; i < rate; i++) {
            ap.tick(player);
        }

        ApertureSpawnManager.tickSpawning(player);
        ApertureInvasionManager.tickInvasion(player);
        ApertureAmbientManager.tickAmbient(player);
    }

    public static int getRandomTickSpeedMultiplier(ServerLevel level) {
        if (!level.dimension().equals(ModDimensions.APERTURE_DIM)) return 1;
        return 3;
    }
}
