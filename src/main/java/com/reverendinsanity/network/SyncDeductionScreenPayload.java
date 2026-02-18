package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

// 服务端->客户端：推演界面数据同步（蛊虫列表+推演状态）
public record SyncDeductionScreenPayload(
    List<DeductionGuEntry> guList,
    boolean deductionActive,
    float progress,
    float successRate
) implements CustomPacketPayload {

    public record DeductionGuEntry(String typeId, String displayName, int rank, String pathName) {}

    public static final Type<SyncDeductionScreenPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "sync_deduction_screen"));

    public static final StreamCodec<FriendlyByteBuf, SyncDeductionScreenPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncDeductionScreenPayload decode(FriendlyByteBuf buf) {
                int size = buf.readVarInt();
                List<DeductionGuEntry> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(new DeductionGuEntry(
                        buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readUtf()
                    ));
                }
                boolean active = buf.readBoolean();
                float progress = buf.readFloat();
                float successRate = buf.readFloat();
                return new SyncDeductionScreenPayload(list, active, progress, successRate);
            }

            @Override
            public void encode(FriendlyByteBuf buf, SyncDeductionScreenPayload payload) {
                buf.writeVarInt(payload.guList.size());
                for (DeductionGuEntry entry : payload.guList) {
                    buf.writeUtf(entry.typeId);
                    buf.writeUtf(entry.displayName);
                    buf.writeVarInt(entry.rank);
                    buf.writeUtf(entry.pathName);
                }
                buf.writeBoolean(payload.deductionActive);
                buf.writeFloat(payload.progress);
                buf.writeFloat(payload.successRate);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
