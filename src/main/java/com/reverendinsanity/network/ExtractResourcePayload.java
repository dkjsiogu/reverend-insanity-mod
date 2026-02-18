package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：从仙窍提取资源
public record ExtractResourcePayload(int resourceOrdinal, int amount) implements CustomPacketPayload {

    public static final Type<ExtractResourcePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "extract_resource"));

    public static final StreamCodec<FriendlyByteBuf, ExtractResourcePayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public ExtractResourcePayload decode(FriendlyByteBuf buf) {
                return new ExtractResourcePayload(buf.readInt(), buf.readInt());
            }

            @Override
            public void encode(FriendlyByteBuf buf, ExtractResourcePayload payload) {
                buf.writeInt(payload.resourceOrdinal);
                buf.writeInt(payload.amount);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
