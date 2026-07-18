package com.adaptor.deadrecall.network;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpaceUnitMapPayloadCodecTest {
    @Test
    void catalystBreakdownRoundTripsAndEntriesAreImmutable() {
        SpaceUnitMapPayload.Entry entry = entry(5, 4, 4, 2, 3);
        List<SpaceUnitMapPayload.Entry> mutableEntries = new ArrayList<>(List.of(entry));
        SpaceUnitMapPayload payload = payload(mutableEntries);
        mutableEntries.clear();

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            SpaceUnitMapPayload.CODEC.encode(buffer, payload);
            SpaceUnitMapPayload decoded = SpaceUnitMapPayload.CODEC.decode(buffer);
            SpaceUnitMapPayload.Entry decodedEntry = decoded.entries().getFirst();

            assertEquals(1, payload.entries().size());
            assertEquals(5, decodedEntry.baseAmethystCost());
            assertEquals(4, decodedEntry.sourceCatalysts());
            assertEquals(4, decodedEntry.targetCatalysts());
            assertEquals(2, decodedEntry.catalystDiscount());
            assertEquals(3, decodedEntry.amethystCost());
            assertThrows(UnsupportedOperationException.class, () -> decoded.entries().clear());
        } finally {
            buffer.release();
        }
    }

    @Test
    void payloadRejectsTooManyEntriesBeforeEncoding() {
        List<SpaceUnitMapPayload.Entry> entries = Collections.nCopies(
                SpaceUnitMapPayload.MAX_ENTRIES + 1,
                entry(5, 4, 4, 2, 3)
        );

        assertThrows(IllegalArgumentException.class, () -> payload(entries));
    }

    @Test
    void decoderRejectsNegativeAndOversizedEntryCounts() {
        assertRejectedEntryCount(-1);
        assertRejectedEntryCount(SpaceUnitMapPayload.MAX_ENTRIES + 1);
    }

    @Test
    void catalystFieldsRejectImpossibleRangesAndInconsistentTotals() {
        assertThrows(
                IllegalArgumentException.class,
                () -> entry(
                        5,
                        SpaceUnitMapPayload.MAX_CATALYST_BLOCKS_PER_ENDPOINT + 1,
                        0,
                        1,
                        4
                )
        );
        assertThrows(IllegalArgumentException.class, () -> entry(5, 4, 4, 2, 4));
        assertThrows(IllegalArgumentException.class, () -> entry(5, 74, 74, 5, 1));
    }

    private static void assertRejectedEntryCount(int entryCount) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            writePayloadHeader(buffer);
            buffer.writeInt(entryCount);
            assertThrows(DecoderException.class, () -> SpaceUnitMapPayload.CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    private static SpaceUnitMapPayload payload(List<SpaceUnitMapPayload.Entry> entries) {
        return new SpaceUnitMapPayload(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "lodestone",
                "Source",
                "minecraft:overworld",
                1,
                2,
                3,
                entries
        );
    }

    private static SpaceUnitMapPayload.Entry entry(
            int baseCost,
            int sourceCatalysts,
            int targetCatalysts,
            int discount,
            int finalCost
    ) {
        return new SpaceUnitMapPayload.Entry(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "lodestone",
                "Target",
                "private",
                false,
                "minecraft:the_nether",
                4,
                5,
                6,
                0.65D,
                1,
                -1,
                0,
                0,
                0,
                20,
                finalCost,
                16,
                baseCost,
                sourceCatalysts,
                targetCatalysts,
                discount,
                120,
                8,
                10,
                false,
                true,
                true,
                0,
                0,
                true,
                ""
        );
    }

    private static void writePayloadHeader(FriendlyByteBuf buffer) {
        buffer.writeUUID(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        buffer.writeUtf("lodestone", 32);
        buffer.writeUtf("Source", 128);
        buffer.writeUtf("minecraft:overworld", 128);
        buffer.writeInt(1);
        buffer.writeInt(2);
        buffer.writeInt(3);
    }
}
