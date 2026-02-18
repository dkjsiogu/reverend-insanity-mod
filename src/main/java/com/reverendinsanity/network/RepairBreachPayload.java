package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：请求修复仙窍漏洞
public record RepairBreachPayload() implements CustomPacketPayload {

    public static final Type<RepairBreachPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "repair_breach"));

    public static final StreamCodec<FriendlyByteBuf, RepairBreachPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public RepairBreachPayload decode(FriendlyByteBuf buf) {
                return new RepairBreachPayload();
            }

            @Override
            public void encode(FriendlyByteBuf buf, RepairBreachPayload payload) {
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
