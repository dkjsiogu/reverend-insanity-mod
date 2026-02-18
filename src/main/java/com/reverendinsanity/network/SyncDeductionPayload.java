package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端->客户端：推演进度同步
public record SyncDeductionPayload(
    boolean active,
    float progress,
    float successRate,
    String message
) implements CustomPacketPayload {

    public static final Type<SyncDeductionPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "sync_deduction"));

    public static final StreamCodec<FriendlyByteBuf, SyncDeductionPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncDeductionPayload::active,
            ByteBufCodecs.FLOAT, SyncDeductionPayload::progress,
            ByteBufCodecs.FLOAT, SyncDeductionPayload::successRate,
            ByteBufCodecs.STRING_UTF8, SyncDeductionPayload::message,
            SyncDeductionPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
