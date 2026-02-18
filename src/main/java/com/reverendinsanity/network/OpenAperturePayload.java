package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：请求打开空窍界面
public record OpenAperturePayload() implements CustomPacketPayload {

    public static final Type<OpenAperturePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "open_aperture"));

    public static final StreamCodec<FriendlyByteBuf, OpenAperturePayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public OpenAperturePayload decode(FriendlyByteBuf buf) {
                return new OpenAperturePayload();
            }
            @Override
            public void encode(FriendlyByteBuf buf, OpenAperturePayload payload) {
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
