package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：主动防御操作（需要在ModBusEvents中注册）
public record DefenseActionPayload(int action) implements CustomPacketPayload {

    public static final int SHIELD = 0;
    public static final int DODGE = 1;

    public static final Type<DefenseActionPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "defense_action"));

    public static final StreamCodec<FriendlyByteBuf, DefenseActionPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, DefenseActionPayload::action,
            DefenseActionPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
