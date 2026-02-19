package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

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
    PrimaryPathData primaryPath,
    List<BuffData> activeBuffs,
    FactionData faction,
    int lifespan,
    int maxLifespan,
    float heavenWillAttention,
    int meritPoints
) implements CustomPacketPayload {

    public record PrimaryPathData(String name, int marks) {}

    public record BuffData(String idPath, int remainingTicks) {}

    public record FactionData(int righteous, int demonic, int independent) {}

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
                    new PrimaryPathData(buf.readUtf(), buf.readInt()),
                    readBuffData(buf),
                    new FactionData(buf.readInt(), buf.readInt(), buf.readInt()),
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
                PrimaryPathData primaryPath = payload.primaryPath() != null
                    ? payload.primaryPath()
                    : new PrimaryPathData("", 0);
                buf.writeUtf(primaryPath.name());
                buf.writeInt(primaryPath.marks());

                List<BuffData> activeBuffs = payload.activeBuffs() != null
                    ? payload.activeBuffs()
                    : List.of();
                writeBuffData(buf, activeBuffs);

                FactionData faction = payload.faction() != null
                    ? payload.faction()
                    : new FactionData(0, 0, 0);
                buf.writeInt(faction.righteous());
                buf.writeInt(faction.demonic());
                buf.writeInt(faction.independent());
                buf.writeInt(payload.lifespan);
                buf.writeInt(payload.maxLifespan);
                buf.writeFloat(payload.heavenWillAttention);
                buf.writeInt(payload.meritPoints);
            }
        };

    private static List<BuffData> readBuffData(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<BuffData> buffs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            buffs.add(new BuffData(buf.readUtf(), buf.readInt()));
        }
        return buffs;
    }

    private static void writeBuffData(FriendlyByteBuf buf, List<BuffData> buffs) {
        buf.writeInt(buffs.size());
        for (BuffData buff : buffs) {
            buf.writeUtf(buff.idPath());
            buf.writeInt(buff.remainingTicks());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
