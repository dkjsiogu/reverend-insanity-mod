package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端->客户端：伤害数字显示（需要在ModBusEvents中注册）
public record DamageNumberPayload(
    int entityId,
    float damage,
    int damageType,
    double x, double y, double z
) implements CustomPacketPayload {

    public static final Type<DamageNumberPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "damage_number"));

    public static final StreamCodec<FriendlyByteBuf, DamageNumberPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public DamageNumberPayload decode(FriendlyByteBuf buf) {
                return new DamageNumberPayload(
                    buf.readInt(),
                    buf.readFloat(),
                    buf.readInt(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble()
                );
            }

            @Override
            public void encode(FriendlyByteBuf buf, DamageNumberPayload payload) {
                buf.writeInt(payload.entityId);
                buf.writeFloat(payload.damage);
                buf.writeInt(payload.damageType);
                buf.writeDouble(payload.x);
                buf.writeDouble(payload.y);
                buf.writeDouble(payload.z);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
