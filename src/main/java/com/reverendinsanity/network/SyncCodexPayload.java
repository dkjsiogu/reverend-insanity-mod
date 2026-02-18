package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

// 服务端->客户端：同步蛊虫图鉴数据
public record SyncCodexPayload(
    List<CodexEntry> allEntries,
    int discoveredCount
) implements CustomPacketPayload {

    public record CodexEntry(String typeId, String displayName, int rank, String pathName, String categoryName, boolean discovered) {}

    public static final Type<SyncCodexPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "sync_codex"));

    public static final StreamCodec<FriendlyByteBuf, SyncCodexPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncCodexPayload decode(FriendlyByteBuf buf) {
                int count = buf.readInt();
                List<CodexEntry> entries = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    entries.add(new CodexEntry(
                        buf.readUtf(), buf.readUtf(), buf.readInt(),
                        buf.readUtf(), buf.readUtf(), buf.readBoolean()
                    ));
                }
                int discovered = buf.readInt();
                return new SyncCodexPayload(entries, discovered);
            }

            @Override
            public void encode(FriendlyByteBuf buf, SyncCodexPayload payload) {
                buf.writeInt(payload.allEntries.size());
                for (CodexEntry e : payload.allEntries) {
                    buf.writeUtf(e.typeId()); buf.writeUtf(e.displayName());
                    buf.writeInt(e.rank()); buf.writeUtf(e.pathName());
                    buf.writeUtf(e.categoryName()); buf.writeBoolean(e.discovered());
                }
                buf.writeInt(payload.discoveredCount);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
