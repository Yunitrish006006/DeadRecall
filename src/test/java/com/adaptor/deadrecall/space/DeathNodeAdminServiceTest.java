package com.adaptor.deadrecall.space;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeathNodeAdminServiceTest {
    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void queriesDeathNodesWithStableSortingAndPagination() {
        TestNode aliceOlder = deathNode("00000000-0000-0000-0000-000000000030", ALICE, 20, true);
        TestNode aliceNewer = deathNode("00000000-0000-0000-0000-000000000020", ALICE, 40, true);
        TestNode bob = deathNode("00000000-0000-0000-0000-000000000010", BOB, 99, true);
        TestNode lodestone = deathNode("00000000-0000-0000-0000-000000000040", BOB, 1, false);

        Map<UUID, String> names = Map.of(ALICE, "alice", BOB, "Bob");
        DeathNodeAdminService.Page<TestNode> firstPage = page(
                List.of(bob, lodestone, aliceOlder, aliceNewer),
                names::get,
                new DeathNodeAdminService.PageRequest(0, 2)
        );
        DeathNodeAdminService.Page<TestNode> secondPage = page(
                List.of(aliceNewer, bob, aliceOlder, lodestone),
                names::get,
                new DeathNodeAdminService.PageRequest(1, 2)
        );

        assertEquals(List.of(aliceNewer.id(), aliceOlder.id()), firstPage.entries().stream().map(TestNode::id).toList());
        assertEquals(List.of(bob.id()), secondPage.entries().stream().map(TestNode::id).toList());
        assertEquals(3, firstPage.totalEntries());
        assertTrue(firstPage.hasNextPage());
        assertTrue(secondPage.hasPreviousPage());
        assertFalse(secondPage.hasNextPage());
    }

    @Test
    void clampsUntrustedPageBoundsWithoutReturningUnrelatedRecords() {
        TestNode node = deathNode("00000000-0000-0000-0000-000000000050", ALICE, 5, true);

        DeathNodeAdminService.Page<TestNode> clamped = page(
                List.of(node),
                ignored -> "Alice",
                new DeathNodeAdminService.PageRequest(-4, 0)
        );
        DeathNodeAdminService.Page<TestNode> beyondEnd = page(
                List.of(node),
                ignored -> "Alice",
                new DeathNodeAdminService.PageRequest(Integer.MAX_VALUE, Integer.MAX_VALUE)
        );

        assertEquals(0, clamped.page());
        assertEquals(1, clamped.pageSize());
        assertEquals(List.of(node), clamped.entries());
        assertEquals(DeathNodeAdminService.MAX_PAGE_SIZE, beyondEnd.pageSize());
        assertTrue(beyondEnd.entries().isEmpty());
        assertEquals(1, beyondEnd.totalEntries());
    }

    @Test
    void keepsTheRelativeOrderOfExistingRowsStableWhenNodesChangeBetweenPageRequests() {
        TestNode first = deathNode("00000000-0000-0000-0000-000000000081", ALICE, 100, true);
        TestNode second = deathNode("00000000-0000-0000-0000-000000000082", ALICE, 100, true);
        TestNode third = deathNode("00000000-0000-0000-0000-000000000083", ALICE, 100, true);
        TestNode fourth = deathNode("00000000-0000-0000-0000-000000000084", ALICE, 100, true);
        TestNode addedDuringRefresh = deathNode("00000000-0000-0000-0000-000000000080", ALICE, 100, true);

        List<UUID> before = pagedIds(
                List.of(fourth, second, first, third),
                new DeathNodeAdminService.PageRequest(0, 2),
                new DeathNodeAdminService.PageRequest(1, 2)
        );
        List<UUID> after = pagedIds(
                List.of(third, addedDuringRefresh, first, fourth, second),
                new DeathNodeAdminService.PageRequest(0, 2),
                new DeathNodeAdminService.PageRequest(1, 2),
                new DeathNodeAdminService.PageRequest(2, 2)
        );

        assertEquals(List.of(first.id(), second.id(), third.id(), fourth.id()), before);
        assertEquals(List.of(addedDuringRefresh.id(), first.id(), second.id(), third.id(), fourth.id()), after);
        assertEquals(before, after.stream().filter(before::contains).toList());
    }

    @Test
    void appliesOwnerDimensionStatusAndCreationTimeFiltersWithoutClientRecords() {
        DeathNodeAdminService.DeathNodeQuery query = new DeathNodeAdminService.DeathNodeQuery(
                "Alice",
                "minecraft:overworld",
                "active",
                100L,
                200L,
                new DeathNodeAdminService.PageRequest(0, 20)
        );

        assertTrue(DeathNodeAdminService.matchesQuery(
                ALICE,
                "Alice",
                "minecraft:overworld",
                "active",
                150L,
                query
        ));
        assertTrue(DeathNodeAdminService.matchesQuery(
                ALICE,
                "offline name is resolved by the server boundary",
                "minecraft:overworld",
                "active",
                150L,
                new DeathNodeAdminService.DeathNodeQuery(
                        ALICE.toString(),
                        "minecraft:overworld",
                        "active",
                        100L,
                        200L,
                        new DeathNodeAdminService.PageRequest(0, 20)
                )
        ));
        assertFalse(DeathNodeAdminService.matchesQuery(
                ALICE,
                "Alice",
                "minecraft:the_nether",
                "active",
                150L,
                query
        ));
        assertFalse(DeathNodeAdminService.matchesQuery(
                ALICE,
                "Alice",
                "minecraft:overworld",
                "disabled",
                150L,
                query
        ));
        assertFalse(DeathNodeAdminService.matchesQuery(
                ALICE,
                "Alice",
                "minecraft:overworld",
                "active",
                201L,
                query
        ));
    }

    @Test
    void resolvesAnOfflineOrFormerNameToItsServerCachedUuidBeforeFiltering() {
        DeathNodeAdminService.DeathNodeQuery cachedNameQuery = new DeathNodeAdminService.DeathNodeQuery(
                "AliceBeforeRename",
                "minecraft:overworld",
                "active",
                100L,
                200L,
                new DeathNodeAdminService.PageRequest(4, 20)
        );

        DeathNodeAdminService.DeathNodeQuery resolved = DeathNodeAdminService.resolveOwnerQuery(
                cachedNameQuery,
                name -> "AliceBeforeRename".equals(name) ? ALICE : null
        );

        assertEquals(ALICE.toString(), resolved.ownerQuery());
        assertEquals(cachedNameQuery.pageRequest(), resolved.pageRequest());
        assertTrue(DeathNodeAdminService.matchesQuery(
                ALICE,
                "AliceAfterRename",
                "minecraft:overworld",
                "active",
                150L,
                resolved
        ));
    }

    @Test
    void derivesDiagnosticsFromPersistedRecordsWithoutChangingTheDiscoveryIndex() {
        UUID cleanId = UUID.fromString("00000000-0000-0000-0000-000000000061");
        UUID inconsistentId = UUID.fromString("00000000-0000-0000-0000-000000000062");
        UUID inactiveId = UUID.fromString("00000000-0000-0000-0000-000000000063");
        DeathNodeAdminService.DeathNodeDiagnosticInput clean = new DeathNodeAdminService.DeathNodeDiagnosticInput(
                cleanId,
                ALICE,
                true,
                true,
                false,
                false,
                "alice|minecraft:overworld|10,64,10"
        );
        DeathNodeAdminService.DeathNodeDiagnosticInput inconsistent = new DeathNodeAdminService.DeathNodeDiagnosticInput(
                inconsistentId,
                ALICE,
                true,
                false,
                true,
                true,
                "alice|minecraft:overworld|10,64,10"
        );
        DeathNodeAdminService.DeathNodeDiagnosticInput inactive = new DeathNodeAdminService.DeathNodeDiagnosticInput(
                inactiveId,
                ALICE,
                false,
                true,
                false,
                false,
                "alice|minecraft:overworld|10,64,10"
        );
        Set<UUID> discovered = new HashSet<>(Set.of(cleanId));
        Map<UUID, Set<UUID>> discoveryByPlayer = new HashMap<>();
        discoveryByPlayer.put(ALICE, discovered);

        Map<UUID, DeathNodeAdminService.DeathNodeDiagnostics> diagnostics =
                DeathNodeAdminService.diagnoseDeathNodeInputs(
                        List.of(clean, inconsistent, inactive),
                        discoveryByPlayer
                );

        assertTrue(diagnostics.get(cleanId).flags().contains(
                DeathNodeAdminService.DiagnosticFlag.DUPLICATE_ACTIVE_LOCATION));
        assertTrue(diagnostics.get(inconsistentId).flags().containsAll(Set.of(
                DeathNodeAdminService.DiagnosticFlag.ORPHANED_OWNER_DISCOVERY,
                DeathNodeAdminService.DiagnosticFlag.NON_PRIVATE_VISIBILITY,
                DeathNodeAdminService.DiagnosticFlag.UNEXPECTED_ACCESS_LIST,
                DeathNodeAdminService.DiagnosticFlag.UNEXPECTED_STRUCTURE,
                DeathNodeAdminService.DiagnosticFlag.DUPLICATE_ACTIVE_LOCATION
        )));
        assertFalse(diagnostics.get(inactiveId).hasFlags());
        assertEquals(Set.of(cleanId), discovered);
    }

    @Test
    void batchTargetsIgnoreTheRequestedPageAndRecomputeEligibleRecordsFromTheServerFilter() {
        UUID activeFirst = UUID.fromString("00000000-0000-0000-0000-000000000071");
        UUID activeSecond = UUID.fromString("00000000-0000-0000-0000-000000000072");
        UUID disabled = UUID.fromString("00000000-0000-0000-0000-000000000073");
        UUID differentOwner = UUID.fromString("00000000-0000-0000-0000-000000000074");
        List<BatchNode> units = List.of(
                new BatchNode(activeFirst, ALICE, 10L, true),
                new BatchNode(activeSecond, ALICE, 10L, true),
                new BatchNode(disabled, ALICE, 10L, false),
                new BatchNode(differentOwner, BOB, 10L, true)
        );

        List<UUID> disableTargets = DeathNodeAdminService.batchTargets(
                units,
                node -> node.owner().equals(ALICE),
                BatchNode::active,
                BatchNode::owner,
                BatchNode::createdGameTime,
                BatchNode::id,
                owner -> owner.equals(ALICE) ? "Alice" : "Bob",
                false
        ).stream().map(BatchNode::id).toList();
        List<UUID> purgeTargets = DeathNodeAdminService.batchTargets(
                units,
                node -> node.owner().equals(ALICE),
                BatchNode::active,
                BatchNode::owner,
                BatchNode::createdGameTime,
                BatchNode::id,
                owner -> owner.equals(ALICE) ? "Alice" : "Bob",
                true
        ).stream().map(BatchNode::id).toList();

        assertEquals(List.of(activeFirst, activeSecond), disableTargets);
        assertEquals(List.of(disabled), purgeTargets);
    }

    private static DeathNodeAdminService.Page<TestNode> page(
            List<TestNode> nodes,
            java.util.function.Function<UUID, String> names,
            DeathNodeAdminService.PageRequest request) {
        return DeathNodeAdminService.pageDeathNodes(
                nodes,
                TestNode::deathNode,
                TestNode::owner,
                TestNode::createdGameTime,
                TestNode::id,
                names,
                request
        );
    }

    private static List<UUID> pagedIds(
            List<TestNode> nodes,
            DeathNodeAdminService.PageRequest... requests) {
        return java.util.Arrays.stream(requests)
                .flatMap(request -> page(nodes, ignored -> "Alice", request).entries().stream())
                .map(TestNode::id)
                .toList();
    }

    private static TestNode deathNode(String id, UUID owner, long createdGameTime, boolean deathNode) {
        return new TestNode(UUID.fromString(id), owner, createdGameTime, deathNode);
    }

    private record TestNode(UUID id, UUID owner, long createdGameTime, boolean deathNode) {
    }

    private record BatchNode(UUID id, UUID owner, long createdGameTime, boolean active) {
    }
}
