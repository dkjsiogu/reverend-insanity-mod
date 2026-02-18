package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：离开仙窍
public record ExitAperturePayload() implements CustomPacketPayload {

    public static final Type<ExitAperturePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "exit_aperture"));

    public static final StreamCodec<FriendlyByteBuf, ExitAperturePayload> STREAM_CODEC =
        StreamCodec.unit(new ExitAperturePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
