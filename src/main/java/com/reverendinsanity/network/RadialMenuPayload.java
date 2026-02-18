package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：转轮菜单操作请求（系统动作/蛊虫技能/杀招）
public record RadialMenuPayload(int actionType, int actionIndex) implements CustomPacketPayload {

    public static final int TYPE_SYSTEM = 0;
    public static final int TYPE_ABILITY = 1;
    public static final int TYPE_MOVE = 2;

    public static final Type<RadialMenuPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "radial_menu"));

    public static final StreamCodec<FriendlyByteBuf, RadialMenuPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, RadialMenuPayload::actionType,
            ByteBufCodecs.INT, RadialMenuPayload::actionIndex,
            RadialMenuPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
