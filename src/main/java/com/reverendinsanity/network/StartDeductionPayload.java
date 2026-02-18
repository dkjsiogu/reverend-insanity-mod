package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：开始杀招推演
public record StartDeductionPayload(
    String coreGuId,
    java.util.List<String> supportGuIds,
    String targetPath
) implements CustomPacketPayload {

    public static final Type<StartDeductionPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "start_deduction"));

    public static final StreamCodec<FriendlyByteBuf, StartDeductionPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, StartDeductionPayload::coreGuId,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), StartDeductionPayload::supportGuIds,
            ByteBufCodecs.STRING_UTF8, StartDeductionPayload::targetPath,
            StartDeductionPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
