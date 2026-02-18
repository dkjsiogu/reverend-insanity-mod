package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：进入仙窍
public record EnterAperturePayload() implements CustomPacketPayload {

    public static final Type<EnterAperturePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "enter_aperture"));

    public static final StreamCodec<FriendlyByteBuf, EnterAperturePayload> STREAM_CODEC =
        StreamCodec.unit(new EnterAperturePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
