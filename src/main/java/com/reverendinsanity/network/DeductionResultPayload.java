package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端->客户端：推演结果通知
public record DeductionResultPayload(
    int outcome,
    String moveName,
    int improvementLevel,
    String message
) implements CustomPacketPayload {

    public static final Type<DeductionResultPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "deduction_result"));

    public static final StreamCodec<FriendlyByteBuf, DeductionResultPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, DeductionResultPayload::outcome,
            ByteBufCodecs.STRING_UTF8, DeductionResultPayload::moveName,
            ByteBufCodecs.INT, DeductionResultPayload::improvementLevel,
            ByteBufCodecs.STRING_UTF8, DeductionResultPayload::message,
            DeductionResultPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
