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
- [ ] Query-payload permission denial test proving no private snapshot is returned.
- [x] Offline player filtering and old-name/UUID lookup tests.
- [x] Pagination stability under concurrent node changes.
- [x] Single and batch confirmation expiry tests.
- [x] Delete-node-then-recover-backpack test.
- [ ] Duplicate backpack-binding diagnostics and persisted reverse-binding tests.
- [ ] Dedicated Server restart persistence test.
