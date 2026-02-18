package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：丢弃蛊虫
public record DiscardGuPayload(int guIndex) implements CustomPacketPayload {

    public static final Type<DiscardGuPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "discard_gu"));

    public static final StreamCodec<FriendlyByteBuf, DiscardGuPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public DiscardGuPayload decode(FriendlyByteBuf buf) {
                return new DiscardGuPayload(buf.readInt());
            }
            @Override
            public void encode(FriendlyByteBuf buf, DiscardGuPayload payload) {
                buf.writeInt(payload.guIndex);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
