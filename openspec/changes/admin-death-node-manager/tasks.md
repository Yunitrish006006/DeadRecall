# Tasks: Admin Death Node Manager

## Specification and data access

- [x] Define a server-side death-node query service with pagination and stable sorting.
- [x] Add diagnostic status calculation without mutating data during reads.
- [x] Add permission checks for every query and mutation endpoint.

## GUI and networking

- [x] Add the admin command or entry point that opens the manager.
- [x] Implement player, dimension, status and time filters.
- [x] Implement paginated result rows and detail inspection.
- [x] Implement short-lived confirmation tokens for destructive operations.
- [x] Localize every player-visible label, tooltip and error.

## Operations

- [x] Implement safe teleport-to-node.
- [x] Implement single disable and delete.
- [x] Implement server-recomputed batch disable and delete.
- [x] Make backpack recovery idempotent when the bound node no longer exists.
- [x] Add audit logging and optional Discord Bridge reporting.

## Tests

- [x] Mutation permission denial against a forged death-node UUID test.
- [x] Query-payload permission denial test proving no private snapshot is returned.
- [x] Offline player filtering and old-name/UUID lookup tests.
- [x] Pagination stability under concurrent node changes.
- [x] Single and batch confirmation expiry tests.
- [x] Delete-node-then-recover-backpack test.
- [x] Duplicate backpack-binding diagnostics and persisted reverse-binding tests.
- [x] Dedicated Server restart persistence test.

## Verification evidence

- TotemNexus unit suite: 21 tests passed, including legacy records without
  `backpack_id`, codec round-trip persistence, and duplicate active binding
  diagnostics.
- TotemNexus Fabric GameTest: 26/26 required tests passed, including denial of
  a forged non-administrator query before any private snapshot is sent.
- TotemRemnant Fabric GameTest: 9/9 required tests passed, including delivery
  of the spawned backpack ItemEntity UUID to the Core lifecycle authority and
  transactional rollback when reverse-binding persistence fails.
- Three isolated Java 25 Dedicated Server JVMs produced `seed.ok`,
  `migrate.ok`, and `verify.ok`; the external Nexus authority retained the
  reverse backpack binding through migration and the following restart.
