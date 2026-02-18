package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：请求打开推演界面
public record OpenDeductionScreenPayload() implements CustomPacketPayload {

    public static final Type<OpenDeductionScreenPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "open_deduction_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenDeductionScreenPayload> STREAM_CODEC =
        StreamCodec.unit(new OpenDeductionScreenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
