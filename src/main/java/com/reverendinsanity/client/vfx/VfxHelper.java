package com.reverendinsanity.client.vfx;

import com.reverendinsanity.network.SpawnVfxPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

// 服务端工具类，方便地发送VFX网络包
public class VfxHelper {

    public static void spawn(ServerPlayer player, VfxType type, int color, float scale, int durationTicks) {
        Vec3 look = player.getLookAngle();
        spawn(player, type, player.getX(), player.getEyeY(), player.getZ(),
            (float) look.x, (float) look.y, (float) look.z,
            color, scale, durationTicks);
    }

    public static void spawn(ServerPlayer player, VfxType type,
                              double x, double y, double z,
                              float dirX, float dirY, float dirZ,
                              int color, float scale, int durationTicks) {
        SpawnVfxPayload payload = new SpawnVfxPayload(
            (byte) type.ordinal(),
            x, y, z,
            dirX, dirY, dirZ,
            color, scale, durationTicks
        );
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
    }
}
