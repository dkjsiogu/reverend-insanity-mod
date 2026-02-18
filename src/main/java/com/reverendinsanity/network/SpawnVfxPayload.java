package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端->客户端：生成VFX视觉效果
public record SpawnVfxPayload(
    byte vfxType,
    double x, double y, double z,
    float dirX, float dirY, float dirZ,
    int color,
    float scale,
    int durationTicks
) implements CustomPacketPayload {

    public static final Type<SpawnVfxPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "spawn_vfx"));

    public static final StreamCodec<FriendlyByteBuf, SpawnVfxPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SpawnVfxPayload decode(FriendlyByteBuf buf) {
                return new SpawnVfxPayload(
                    buf.readByte(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readInt(),
                    buf.readFloat(),
                    buf.readInt()
                );
            }

            @Override
            public void encode(FriendlyByteBuf buf, SpawnVfxPayload payload) {
                buf.writeByte(payload.vfxType);
                buf.writeDouble(payload.x);
                buf.writeDouble(payload.y);
                buf.writeDouble(payload.z);
                buf.writeFloat(payload.dirX);
                buf.writeFloat(payload.dirY);
                buf.writeFloat(payload.dirZ);
                buf.writeInt(payload.color);
                buf.writeFloat(payload.scale);
                buf.writeInt(payload.durationTicks);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
