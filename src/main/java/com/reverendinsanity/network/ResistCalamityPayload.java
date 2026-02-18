package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：使用天地二气抵抗灾劫
public record ResistCalamityPayload(float amount) implements CustomPacketPayload {

    public static final Type<ResistCalamityPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "resist_calamity"));

    public static final StreamCodec<FriendlyByteBuf, ResistCalamityPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.FLOAT, ResistCalamityPayload::amount,
            ResistCalamityPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
