package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：催动技能请求
public record ActivateAbilityPayload(int slotIndex) implements CustomPacketPayload {

    public static final Type<ActivateAbilityPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "activate_ability"));

    public static final StreamCodec<FriendlyByteBuf, ActivateAbilityPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, ActivateAbilityPayload::slotIndex,
            ActivateAbilityPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
