package com.reverendinsanity.network;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

// 服务端->客户端：空窍详细内容（蛊虫列表+杀招列表）
public record SyncApertureContentsPayload(
    List<GuInfo> guList,
    List<MoveInfo> equippedMoves,
    List<MoveInfo> availableMoves
) implements CustomPacketPayload {

    public record GuInfo(String typeId, String displayName, int rank, String pathName, String categoryName, float hunger, boolean refined, boolean alive, float proficiency) {}
    public record MoveInfo(String moveId, String displayName, String moveType, int minRank, float essenceCost, float thoughtsCost, String description) {}

    public static final Type<SyncApertureContentsPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "sync_aperture_contents"));

    public static final StreamCodec<FriendlyByteBuf, SyncApertureContentsPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncApertureContentsPayload decode(FriendlyByteBuf buf) {
                int guCount = buf.readInt();
                List<GuInfo> guList = new ArrayList<>();
                for (int i = 0; i < guCount; i++) {
                    guList.add(new GuInfo(
                        buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readUtf(),
                        buf.readUtf(), buf.readFloat(), buf.readBoolean(), buf.readBoolean(),
                        buf.readFloat()
                    ));
                }
                int equippedCount = buf.readInt();
                List<MoveInfo> equipped = new ArrayList<>();
                for (int i = 0; i < equippedCount; i++) {
                    equipped.add(new MoveInfo(
                        buf.readUtf(), buf.readUtf(), buf.readUtf(),
                        buf.readInt(), buf.readFloat(), buf.readFloat(),
                        buf.readUtf()
                    ));
                }
                int availCount = buf.readInt();
                List<MoveInfo> available = new ArrayList<>();
                for (int i = 0; i < availCount; i++) {
                    available.add(new MoveInfo(
                        buf.readUtf(), buf.readUtf(), buf.readUtf(),
                        buf.readInt(), buf.readFloat(), buf.readFloat(),
                        buf.readUtf()
                    ));
                }
                return new SyncApertureContentsPayload(guList, equipped, available);
            }

            @Override
            public void encode(FriendlyByteBuf buf, SyncApertureContentsPayload payload) {
                buf.writeInt(payload.guList.size());
                for (GuInfo g : payload.guList) {
                    buf.writeUtf(g.typeId()); buf.writeUtf(g.displayName());
                    buf.writeInt(g.rank()); buf.writeUtf(g.pathName());
                    buf.writeUtf(g.categoryName()); buf.writeFloat(g.hunger());
                    buf.writeBoolean(g.refined()); buf.writeBoolean(g.alive());
                    buf.writeFloat(g.proficiency());
                }
                buf.writeInt(payload.equippedMoves.size());
                for (MoveInfo m : payload.equippedMoves) {
                    buf.writeUtf(m.moveId()); buf.writeUtf(m.displayName());
                    buf.writeUtf(m.moveType()); buf.writeInt(m.minRank());
                    buf.writeFloat(m.essenceCost()); buf.writeFloat(m.thoughtsCost());
                    buf.writeUtf(m.description());
                }
                buf.writeInt(payload.availableMoves.size());
                for (MoveInfo m : payload.availableMoves) {
                    buf.writeUtf(m.moveId()); buf.writeUtf(m.displayName());
                    buf.writeUtf(m.moveType()); buf.writeInt(m.minRank());
                    buf.writeFloat(m.essenceCost()); buf.writeFloat(m.thoughtsCost());
                    buf.writeUtf(m.description());
                }
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
