# Design: Safe Multi-Repository Modularization

## 1. Target repository topology

```text
TotemCore
├── TotemDiscordBridge
├── TotemRemnant
├── TotemAutomata
└── TotemNexus

DeadRecall compatibility bundle
├── pins one tested version of every required module
├── preserves the existing single-install experience
├── owns cross-repository integration and migration tests
└── temporarily retains gameplay that has no approved repository owner
```

Feature repositories may depend on `TotemCore`. They must not directly depend on another feature repository. Optional cross-feature behavior is implemented by versioned Core events or by a small optional integration adapter owned by the consumer.

The machine-readable initial ownership map is [`repository-map.json`](repository-map.json).

## 2. Compatibility invariants

Repository extraction must not by itself change:

- the `deadrecall` registry namespace;
- item, block, effect, recipe, criterion, tag or creative-tab IDs;
- Payload IDs or their codecs;
- SavedData keys, `data_version` fields or codec meaning;
- translation keys, config paths or command behavior;
- existing addon API behavior;
- the ability to upgrade an existing world through the DeadRecall bundle.

[`compatibility-surface.txt`](compatibility-surface.txt) is the Phase 0 snapshot of visible `deadrecall:*` identifiers and packaged `assets/deadrecall` / `data/deadrecall` paths. CI compares the current source tree with this snapshot. Once files live in other repositories, the same snapshot must be checked against the assembled compatibility bundle instead of only the DeadRecall source tree.

Removing or renaming an entry requires a separate OpenSpec change with owner, migration, rollback and assembled-bundle evidence. Updating the baseline merely to make CI green is forbidden.

## 3. Extraction protocol

Every feature repository uses the same sequence:

1. **Inventory** — identify implementation, API, Payload, Mixin, client, resource, GameTest and persistent identifiers.
2. **Seam** — replace direct feature-to-feature calls with a Core API/event or an optional adapter while code still lives in DeadRecall.
3. **Copy** — create the new repository from an isolated temporary history export. Never rewrite the active DeadRecall repository history.
4. **Dual validation** — build the new module independently and as part of the DeadRecall bundle while the old implementation remains available behind one bootstrap choice.
5. **Cutover** — make the bundle load the new module artifact and prove that only one implementation registers each identifier, receiver, event and Mixin.
6. **Deletion** — remove the old implementation only after standalone, bundle, migration, restart and Dedicated Server gates pass.
7. **Observation** — keep versions lockstep and retain rollback compatibility for at least two DeadRecall releases before independent versioning.

At no point may two implementations register the same registry or Payload ID in one runtime.

An external repository reaching a standalone build, extracted primitive tests, or
an inactive cutover seam is not a bundle cutover.  A feature is cut over only
when its external implementation is the sole live owner of its complete
authority and registration surface in the assembled DeadRecall runtime.  The
matching legacy implementation remains source-visible but disabled throughout
the lockstep observation window, so a previous immutable bundle pin can restore
it without changing persisted world data.

## 4. Initial ordering

### TotemCore

Core is created first but contains no gameplay. Its first release provides only:

- versioned public API package conventions;
- event registration and lifecycle contracts;
- migration/version helpers;
- common identifier and permission primitives that are proven to have at least two consumers.

Existing death addon interfaces remain compatible. Moving or replacing their package requires forwarding types and a separate addon migration window.

### TotemDiscordBridge pilot

Discord Bridge is the first physical extraction because it has a relatively clear service boundary and no world registry content. It validates cross-repository checkout, Core dependency, bundle assembly, secrets, config migration and release pinning before persistent gameplay moves.

### TotemRemnant

Before extraction, death backpack creation/recovery must stop calling Nexus implementation directly. Remnant publishes versioned death/backpack lifecycle events; Nexus and Discord adapters subscribe without mutating Remnant private state.

### TotemAutomata

Copper Golem implementation, payloads, client screens, Mixin and restart probes move together. Shared `ModItems`, `ModMenus`, language files and client bootstrap must first be split into owner-specific registration classes.

### TotemNexus

Nexus moves last among current gameplay modules because it owns the largest connected group of SavedData, Payload, client UI, teleport sessions and Mixin accessors. All existing SavedData keys and `deadrecall:*` identifiers remain readable.

## 5. Unassigned gameplay

Alchemy, enchanting changes, recipe overrides, concrete-powder behavior, portable-container policy and other legacy gameplay remain in DeadRecall until each has an approved bounded-context repository. They must not be placed in TotemCore merely to empty DeadRecall.

## 6. Build and release contract

Initial module releases use the same Minecraft and Java compatibility line and are pinned by an immutable manifest in DeadRecall. A compatibility build must resolve exact module versions; floating ranges such as `latest` are forbidden.

Each module repository eventually runs:

- Java 25 compile and unit tests;
- relevant Fabric Server GameTests;
- Dedicated Server client-class isolation checks;
- its own restart/migration probes when it persists world state;
- a compatibility contract check against the Core API version.

DeadRecall additionally runs the assembled bundle, legacy-world fixtures and cross-module combinations. Independent Modrinth publishing is enabled only after the bundle has consumed two successful lockstep releases.

### Locked verification runtime

Every module build, GameTest and assembled-bundle verification uses **Java
25**.  The verifier rejects another Java major version before it stages a
world, so a workstation's default Java 17 installation cannot produce
misleading lockstep evidence.

The isolated exact-version Dedicated Server always writes
`server-port=25570`.  A legacy-world seed run which is executed alongside it
uses `server-port=25571`.  Compatibility probes must not rely on Minecraft's
default `25565`, because a developer server or another probe may already own
that port.  The port numbers are test-harness isolation settings, not part of
the persisted world or public mod protocol.

## 7. Visual test evidence

Tests which validate a client screen, rendering, mouse/keyboard interaction or a
manual visual acceptance criterion must save screenshot evidence in the current
repository working directory.  The required location is
`test-artifacts/screenshots/<change-id>/<test-id>-<stage>.png`; `<stage>` is at
least `before` and `after`, and includes an asserted error or confirmation state
when that state is the subject of the test.  A deterministic name is required so
that a re-run replaces only evidence for the same test case.

The screenshot must show the relevant game state and must not include secrets,
tokens, private chat, or unrelated desktop windows.  Tests running under a
virtual display capture that display; manual tests capture the game window.
The test report records the command, test ID, and relative screenshot paths.

Pure JVM tests and headless Fabric Server GameTests have no meaningful client
framebuffer and therefore continue to provide their JUnit/GameTest reports
instead of fabricated screenshots.  A visual requirement must be covered by a
Client GameTest or an explicitly documented manual test case.  CI uploads
`test-artifacts/screenshots/**` with the normal validation reports, including
when the test fails.

## 8. Installation matrix

For each extracted feature, CI must cover:

| Installation | Expected result |
|---|---|
| Core only | Starts without gameplay registration |
| Core + feature | Feature starts and passes its own tests |
| DeadRecall bundle | Preserves current all-in-one behavior |
| Core + feature without unrelated feature | Starts without class-loading or Mixin failure |
| Legacy world + DeadRecall bundle | Loads without missing registry or SavedData loss |
| Dedicated Server | Loads no client-only classes |

Pairwise feature combinations are required only where an explicit integration exists. The full bundle remains the authoritative end-to-end combination.

## 9. Rollback

- Every cutover is one feature and one reversible PR.
- The old DeadRecall implementation is not deleted in the same commit that first consumes a new external module.
- Persistent schema changes are prohibited during repository movement.
- The compatibility manifest can pin the previous known-good module artifact without changing world data.
- Repository history extraction occurs only in a temporary clone or mirror; the source repository is never force-rewritten.
