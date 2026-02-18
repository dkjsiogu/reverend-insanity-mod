package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// S->C: 同步蛊师情报数据（目标实体ID、情报等级、可见信息）
public record IntelSyncPayload(
    int targetEntityId,
    int intelLevel,
    int targetRank,
    String targetPath,
    String displayInfo
) implements CustomPacketPayload {

    public static final Type<IntelSyncPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "intel_sync"));

    public static final StreamCodec<FriendlyByteBuf, IntelSyncPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public IntelSyncPayload decode(FriendlyByteBuf buf) {
                return new IntelSyncPayload(
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readUtf(),
                    buf.readUtf()
                );
            }

            @Override
            public void encode(FriendlyByteBuf buf, IntelSyncPayload payload) {
                buf.writeInt(payload.targetEntityId);
                buf.writeInt(payload.intelLevel);
                buf.writeInt(payload.targetRank);
                buf.writeUtf(payload.targetPath);
                buf.writeUtf(payload.displayInfo);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
