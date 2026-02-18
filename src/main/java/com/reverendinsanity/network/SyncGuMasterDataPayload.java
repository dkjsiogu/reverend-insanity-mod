package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端->客户端：同步蛊师修炼状态数据
public record SyncGuMasterDataPayload(
    boolean opened,
    int rankLevel,
    int subRankIndex,
    String aptitudeName,
    float currentEssence,
    float maxEssence,
    float thoughts,
    float maxThoughts,
    int essenceColor,
    int guCount,
    int equippedMoveCount,
    float luck,
    String primaryPathMarks,
    String activeBuffData,
    String factionData,
    int lifespan,
    int maxLifespan,
    float heavenWillAttention,
    int meritPoints
) implements CustomPacketPayload {

    public static final Type<SyncGuMasterDataPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "sync_gu_master_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncGuMasterDataPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncGuMasterDataPayload decode(FriendlyByteBuf buf) {
                return new SyncGuMasterDataPayload(
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readUtf(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readFloat(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readFloat(),
                    buf.readInt()
                );
            }

            @Override
            public void encode(FriendlyByteBuf buf, SyncGuMasterDataPayload payload) {
                buf.writeBoolean(payload.opened);
                buf.writeInt(payload.rankLevel);
                buf.writeInt(payload.subRankIndex);
                buf.writeUtf(payload.aptitudeName);
                buf.writeFloat(payload.currentEssence);
                buf.writeFloat(payload.maxEssence);
                buf.writeFloat(payload.thoughts);
                buf.writeFloat(payload.maxThoughts);
                buf.writeInt(payload.essenceColor);
                buf.writeInt(payload.guCount);
                buf.writeInt(payload.equippedMoveCount);
                buf.writeFloat(payload.luck);
                buf.writeUtf(payload.primaryPathMarks);
                buf.writeUtf(payload.activeBuffData);
                buf.writeUtf(payload.factionData);
                buf.writeInt(payload.lifespan);
                buf.writeInt(payload.maxLifespan);
                buf.writeFloat(payload.heavenWillAttention);
                buf.writeInt(payload.meritPoints);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
