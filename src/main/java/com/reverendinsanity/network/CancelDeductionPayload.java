package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：取消推演
public record CancelDeductionPayload() implements CustomPacketPayload {

    public static final Type<CancelDeductionPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "cancel_deduction"));

    public static final StreamCodec<FriendlyByteBuf, CancelDeductionPayload> STREAM_CODEC =
        StreamCodec.unit(new CancelDeductionPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
