package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端->服务端：装备/卸下杀招
public record EquipMovePayload(String moveId, boolean equip) implements CustomPacketPayload {

    public static final Type<EquipMovePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "equip_move"));

    public static final StreamCodec<FriendlyByteBuf, EquipMovePayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public EquipMovePayload decode(FriendlyByteBuf buf) {
                return new EquipMovePayload(buf.readUtf(), buf.readBoolean());
            }
            @Override
            public void encode(FriendlyByteBuf buf, EquipMovePayload payload) {
                buf.writeUtf(payload.moveId);
                buf.writeBoolean(payload.equip);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
