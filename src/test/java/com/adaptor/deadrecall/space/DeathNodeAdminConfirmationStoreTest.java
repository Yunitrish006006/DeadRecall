package com.adaptor.deadrecall.space;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeathNodeAdminConfirmationStoreTest {
    @Test
    void confirmationIsBoundToTheExactOperatorNodeActionAndToken() {
        DeathNodeAdminService.DestructiveConfirmationStore confirmations =
                new DeathNodeAdminService.DestructiveConfirmationStore();
        UUID administrator = UUID.randomUUID();
        UUID node = UUID.randomUUID();
        DeathNodeAdminService.DestructiveConfirmation confirmation = confirmations.issue(
                administrator,
                node,
                DeathNodeAdminService.ACTION_PURGE,
                1_000L,
                30_000L
        );

        assertEquals(
                DeathNodeAdminService.ConfirmationConsumeResult.MISMATCH,
                confirmations.consume(
                        administrator,
                        UUID.randomUUID(),
                        DeathNodeAdminService.ACTION_PURGE,
                        confirmation.token(),
                        2_000L
                )
        );
        assertEquals(
                DeathNodeAdminService.ConfirmationConsumeResult.CONFIRMED,
                confirmations.consume(
                        administrator,
                        node,
                        DeathNodeAdminService.ACTION_PURGE,
                        confirmation.token(),
                        2_000L
                )
        );
        assertEquals(
                DeathNodeAdminService.ConfirmationConsumeResult.MISSING,
                confirmations.consume(
                        administrator,
                        node,
                        DeathNodeAdminService.ACTION_PURGE,
                        confirmation.token(),
                        2_000L
                )
        );
    }

    @Test
    void expiredConfirmationCannotBeConsumed() {
        DeathNodeAdminService.DestructiveConfirmationStore confirmations =
                new DeathNodeAdminService.DestructiveConfirmationStore();
        UUID administrator = UUID.randomUUID();
        UUID node = UUID.randomUUID();
        DeathNodeAdminService.DestructiveConfirmation confirmation = confirmations.issue(
                administrator,
                node,
                DeathNodeAdminService.ACTION_PURGE,
                1_000L,
                10L
        );

        assertEquals(
                DeathNodeAdminService.ConfirmationConsumeResult.EXPIRED,
                confirmations.consume(
                        administrator,
                        node,
                        DeathNodeAdminService.ACTION_PURGE,
                        confirmation.token(),
                        1_010L
                )
        );
        assertEquals(
                DeathNodeAdminService.ConfirmationConsumeResult.MISSING,
                confirmations.consume(
                        administrator,
                        node,
                        DeathNodeAdminService.ACTION_PURGE,
                        confirmation.token(),
                        1_011L
                )
        );
    }

    @Test
    void batchConfirmationIsBoundToTheServerSideFilterSummary() {
        DeathNodeAdminService.DestructiveConfirmationStore confirmations =
                new DeathNodeAdminService.DestructiveConfirmationStore();
        UUID administrator = UUID.randomUUID();
        DeathNodeAdminService.DestructiveConfirmation confirmation = confirmations.issue(
                administrator,
                DeathNodeAdminService.BATCH_NODE_ID,
                DeathNodeAdminService.ACTION_BATCH_DISABLE,
                "owner=alice|dimension=minecraft:overworld",
                1_000L,
                30_000L
        );

        assertEquals(
                DeathNodeAdminService.ConfirmationConsumeResult.MISMATCH,
                confirmations.consume(
                        administrator,
                        DeathNodeAdminService.BATCH_NODE_ID,
                        DeathNodeAdminService.ACTION_BATCH_DISABLE,
                        "owner=bob|dimension=minecraft:overworld",
                        confirmation.token(),
                        2_000L
                )
        );
        assertEquals(
                DeathNodeAdminService.ConfirmationConsumeResult.CONFIRMED,
                confirmations.consume(
                        administrator,
                        DeathNodeAdminService.BATCH_NODE_ID,
                        DeathNodeAdminService.ACTION_BATCH_DISABLE,
                        "owner=alice|dimension=minecraft:overworld",
                        confirmation.token(),
                        2_000L
                )
        );
    }

    @Test
    void expiredBatchConfirmationCannotBeConsumed() {
        DeathNodeAdminService.DestructiveConfirmationStore confirmations =
                new DeathNodeAdminService.DestructiveConfirmationStore();
        UUID administrator = UUID.randomUUID();
        String filter = "owner=alice|status=active";
        DeathNodeAdminService.DestructiveConfirmation confirmation = confirmations.issue(
                administrator,
                DeathNodeAdminService.BATCH_NODE_ID,
                DeathNodeAdminService.ACTION_BATCH_DISABLE,
                filter,
                1_000L,
                10L
        );

        assertEquals(
                DeathNodeAdminService.ConfirmationConsumeResult.EXPIRED,
                confirmations.consume(
                        administrator,
                        DeathNodeAdminService.BATCH_NODE_ID,
                        DeathNodeAdminService.ACTION_BATCH_DISABLE,
                        filter,
                        confirmation.token(),
                        1_010L
                )
        );
        assertEquals(
                DeathNodeAdminService.ConfirmationConsumeResult.MISSING,
                confirmations.consume(
                        administrator,
                        DeathNodeAdminService.BATCH_NODE_ID,
                        DeathNodeAdminService.ACTION_BATCH_DISABLE,
                        filter,
                        confirmation.token(),
                        1_011L
                )
        );
    }
}
