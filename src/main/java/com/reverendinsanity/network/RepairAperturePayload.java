package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：修复仙窍
public record RepairAperturePayload(float amount) implements CustomPacketPayload {

    public static final Type<RepairAperturePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "repair_aperture"));

    public static final StreamCodec<FriendlyByteBuf, RepairAperturePayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public RepairAperturePayload decode(FriendlyByteBuf buf) {
                return new RepairAperturePayload(buf.readFloat());
            }

            @Override
            public void encode(FriendlyByteBuf buf, RepairAperturePayload payload) {
                buf.writeFloat(payload.amount);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
