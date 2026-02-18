package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：请求打开仙窍管理界面
public record OpenImmortalAperturePayload() implements CustomPacketPayload {

    public static final Type<OpenImmortalAperturePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "open_immortal_aperture"));

    public static final StreamCodec<FriendlyByteBuf, OpenImmortalAperturePayload> STREAM_CODEC =
        StreamCodec.unit(new OpenImmortalAperturePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
