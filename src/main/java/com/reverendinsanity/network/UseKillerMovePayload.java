package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：施展杀招请求
public record UseKillerMovePayload(int slotIndex) implements CustomPacketPayload {

    public static final Type<UseKillerMovePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "use_killer_move"));

    public static final StreamCodec<FriendlyByteBuf, UseKillerMovePayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, UseKillerMovePayload::slotIndex,
            UseKillerMovePayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
