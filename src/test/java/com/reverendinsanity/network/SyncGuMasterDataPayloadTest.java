package com.reverendinsanity.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SyncGuMasterDataPayloadTest {

    @Test
    void streamCodecRoundTripKeepsStructuredFields() {
        SyncGuMasterDataPayload payload = new SyncGuMasterDataPayload(
            true,
            5,
            2,
            "aptitude",
            120.5f,
            240.0f,
            80.0f,
            140.0f,
            0x00CC66,
            9,
            4,
            1.25f,
            new SyncGuMasterDataPayload.PrimaryPathData("strength", 135),
            List.of(
                new SyncGuMasterDataPayload.BuffData("iron_skin", 120),
                new SyncGuMasterDataPayload.BuffData("battle_frenzy", 40)
            ),
            new SyncGuMasterDataPayload.FactionData(260, -35, 5),
            950,
            1200,
            3.5f,
            77
        );

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        SyncGuMasterDataPayload.STREAM_CODEC.encode(buffer, payload);
        SyncGuMasterDataPayload decoded = SyncGuMasterDataPayload.STREAM_CODEC.decode(buffer);

        assertEquals(payload, decoded);
    }

    @Test
    void streamCodecNormalizesNullStructuredFields() {
        SyncGuMasterDataPayload payload = new SyncGuMasterDataPayload(
            false,
            1,
            0,
            "",
            0,
            100,
            0,
            100,
            0x00CC66,
            0,
            0,
            1.0f,
            null,
            null,
            null,
            1000,
            1000,
            0,
            0
        );

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        SyncGuMasterDataPayload.STREAM_CODEC.encode(buffer, payload);
        SyncGuMasterDataPayload decoded = SyncGuMasterDataPayload.STREAM_CODEC.decode(buffer);

        assertEquals(new SyncGuMasterDataPayload.PrimaryPathData("", 0), decoded.primaryPath());
        assertEquals(List.of(), decoded.activeBuffs());
        assertEquals(new SyncGuMasterDataPayload.FactionData(0, 0, 0), decoded.faction());
    }
}
