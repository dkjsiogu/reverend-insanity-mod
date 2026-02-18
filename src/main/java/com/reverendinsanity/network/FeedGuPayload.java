package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：喂养蛊虫
public record FeedGuPayload(int guIndex) implements CustomPacketPayload {

    public static final Type<FeedGuPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "feed_gu"));

    public static final StreamCodec<FriendlyByteBuf, FeedGuPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public FeedGuPayload decode(FriendlyByteBuf buf) {
                return new FeedGuPayload(buf.readInt());
            }
            @Override
            public void encode(FriendlyByteBuf buf, FeedGuPayload payload) {
                buf.writeInt(payload.guIndex);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
