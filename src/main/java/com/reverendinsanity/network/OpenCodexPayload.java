package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：请求打开蛊虫图鉴
public record OpenCodexPayload() implements CustomPacketPayload {

    public static final Type<OpenCodexPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "open_codex"));

    public static final StreamCodec<FriendlyByteBuf, OpenCodexPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public OpenCodexPayload decode(FriendlyByteBuf buf) {
                return new OpenCodexPayload();
            }
            @Override
            public void encode(FriendlyByteBuf buf, OpenCodexPayload payload) {
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
