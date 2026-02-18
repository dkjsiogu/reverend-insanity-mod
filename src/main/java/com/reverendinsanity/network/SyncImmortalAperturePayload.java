package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

// 服务端->客户端：同步仙窍福地数据
public record SyncImmortalAperturePayload(
    boolean formed,
    String gradeName,
    float integrity,
    float heavenQi,
    float earthQi,
    float maxQi,
    int essenceStones,
    boolean calamityActive,
    String calamityTypeName,
    float calamityProgress,
    int daysSinceLastCalamity,
    List<ResourceEntry> resources,
    List<DaoMarkEntry> topDaoMarks,
    float developmentLevel,
    int breachCount,
    int totalCalamitiesSurvived,
    int timeFlowRate
) implements CustomPacketPayload {

    public record ResourceEntry(int ordinal, String name, int amount) {}
    public record DaoMarkEntry(String pathName, int marks) {}

    public static final Type<SyncImmortalAperturePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "sync_immortal_aperture"));

    public static final StreamCodec<FriendlyByteBuf, SyncImmortalAperturePayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncImmortalAperturePayload decode(FriendlyByteBuf buf) {
                boolean formed = buf.readBoolean();
                String gradeName = buf.readUtf();
                float integrity = buf.readFloat();
                float heavenQi = buf.readFloat();
                float earthQi = buf.readFloat();
                float maxQi = buf.readFloat();
                int essenceStones = buf.readInt();
                boolean calamityActive = buf.readBoolean();
                String calamityTypeName = buf.readUtf();
                float calamityProgress = buf.readFloat();
                int daysSinceLastCalamity = buf.readInt();

                int resCount = buf.readInt();
                List<ResourceEntry> resources = new ArrayList<>();
                for (int i = 0; i < resCount; i++) {
                    resources.add(new ResourceEntry(buf.readInt(), buf.readUtf(), buf.readInt()));
                }

                int markCount = buf.readInt();
                List<DaoMarkEntry> topDaoMarks = new ArrayList<>();
                for (int i = 0; i < markCount; i++) {
                    topDaoMarks.add(new DaoMarkEntry(buf.readUtf(), buf.readInt()));
                }

                float developmentLevel = buf.readFloat();
                int breachCount = buf.readInt();
                int totalCalamitiesSurvived = buf.readInt();
                int timeFlowRate = buf.readInt();

                return new SyncImmortalAperturePayload(
                    formed, gradeName, integrity, heavenQi, earthQi, maxQi, essenceStones,
                    calamityActive, calamityTypeName, calamityProgress, daysSinceLastCalamity,
                    resources, topDaoMarks,
                    developmentLevel, breachCount, totalCalamitiesSurvived, timeFlowRate
                );
            }

            @Override
            public void encode(FriendlyByteBuf buf, SyncImmortalAperturePayload payload) {
                buf.writeBoolean(payload.formed);
                buf.writeUtf(payload.gradeName);
                buf.writeFloat(payload.integrity);
                buf.writeFloat(payload.heavenQi);
                buf.writeFloat(payload.earthQi);
                buf.writeFloat(payload.maxQi);
                buf.writeInt(payload.essenceStones);
                buf.writeBoolean(payload.calamityActive);
                buf.writeUtf(payload.calamityTypeName);
                buf.writeFloat(payload.calamityProgress);
                buf.writeInt(payload.daysSinceLastCalamity);

                buf.writeInt(payload.resources.size());
                for (ResourceEntry r : payload.resources) {
                    buf.writeInt(r.ordinal);
                    buf.writeUtf(r.name);
                    buf.writeInt(r.amount);
                }

                buf.writeInt(payload.topDaoMarks.size());
                for (DaoMarkEntry m : payload.topDaoMarks) {
                    buf.writeUtf(m.pathName);
                    buf.writeInt(m.marks);
                }

                buf.writeFloat(payload.developmentLevel);
                buf.writeInt(payload.breachCount);
                buf.writeInt(payload.totalCalamitiesSurvived);
                buf.writeInt(payload.timeFlowRate);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
