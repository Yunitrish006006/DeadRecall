# Tasks: Safe Multi-Repository Modularization

## 0. Safety contract and inventory

- [x] 0.1 Define one repository per major feature and retain DeadRecall as the compatibility bundle.
- [x] 0.2 Record the target dependency rule: Core has no feature dependency; features depend only on Core by default.
- [x] 0.3 Record the current `deadrecall:*` identifier and packaged-resource compatibility surface.
- [x] 0.4 Add an automated CI check that rejects an unexplained compatibility-surface change.
- [x] 0.5 Define the copy, dual-validation, cutover, deletion, observation and rollback protocol.
- [x] 0.6 Record current shared seams and unassigned gameplay instead of assigning them to Core.
- [x] 0.7 Define visual-test screenshot evidence in the repository working directory and retain it in CI artifacts.

## 1. Internal boundaries inside DeadRecall

- [x] 1.1 Split the server initializer into owner-specific bootstrap classes without changing registration order or behavior.
- [x] 1.2 Split client initialization by feature ownership.
- [x] 1.3 Split Payload type/receiver registration by feature ownership.
- [x] 1.4 Split Server and Client Mixin configs by future module ownership while keeping one artifact.
- [x] 1.5 Split shared registry holders and resource generation into owner-specific registration classes.
- [x] 1.6 Add a dependency-boundary check for direct feature-to-feature imports.
- [x] 1.7 Run full Build, Validate, Server GameTests and both restart probes.

## 2. TotemCore repository

- [x] 2.1 Specify the minimal Core API and explicitly reject gameplay classes.
- [x] 2.2 Create the `TotemCore` repository without rewriting DeadRecall history.
- [x] 2.3 Publish a versioned Core development artifact.
- [x] 2.4 Add Core compatibility and Dedicated Server tests.
- [x] 2.5 Document addon API versioning and deprecation policy.

## 3. TotemDiscordBridge pilot repository

- [x] 3.1 Replace root-initializer Discord registration with a feature bootstrap.
- [x] 3.2 Isolate Discord Payload, Mixin, client config UI, language and config ownership.
- [x] 3.3 Create `TotemDiscordBridge` from a temporary filtered history export.
- [x] 3.4 Validate Core + Discord standalone installation.
- [x] 3.5 Consume the module from the DeadRecall compatibility bundle without duplicate registration.
- [x] 3.6 Keep lockstep versions through the observation window before removing the old implementation.

## 4. TotemRemnant repository

- [x] 4.1 Replace direct death-to-Nexus calls with versioned lifecycle events and optional adapters.
- [x] 4.2 Assign backpack items, inventory, addon API, Trinkets adapter, Payload and death Mixin ownership.
- [x] 4.3 Create `TotemRemnant` and preserve existing addon API compatibility.
- [x] 4.4 Pass death capture/recovery, restart, legacy world, multi-player and Dedicated Server tests.
- [x] 4.5 Cut the bundle over after dual validation; retain the conditional legacy fallback during the lockstep compatibility window.

## 5. TotemAutomata repository

- [x] 5.1 Isolate copper item/menu/registry/client/Payload/Mixin registration.
- [x] 5.2 Create `TotemAutomata` with no required Cognition dependency.
- [x] 5.3 Pass sorting, gathering, pressure, restart and standalone installation tests.
- [ ] 5.4 Cut the bundle over only after all Automata cutover gates pass; retain the disabled legacy implementation for the observation window.
  - Implementation boundary: [`totem-automata-cutover-contract.md`](totem-automata-cutover-contract.md).
  - [x] 5.4.1 Move the complete Copper Wrench authority (interaction callbacks, menu opening, sorting/gathering runtime, persistence, permissions and server ticks) into `TotemAutomata` without a direct DeadRecall feature dependency.
  - [x] 5.4.2 Activate Automata-owned item, menu, criterion, Payload, client-screen and Mixin registrations as one atomic path, while gating the matching legacy registrations when `totem-automata` is present.
  - [x] 5.4.3 Prove the assembled bundle has exactly one registration for every preserved identifier, payload receiver, event callback and Mixin, and pass standalone, bundle, legacy-world, restart and Dedicated Server gates.
  - [x] 5.4.4 Publish or stage an immutable Automata artifact, pin it in the exact-version bundle manifest, and retain the previous pin as the rollback target through the observation window.

## 6. TotemNexus repository

- [x] 6.1 Isolate Space Unit, teleport, friend, death-node and distributed-spawn ownership.
- [x] 6.2 Create `TotemNexus` while preserving all SavedData, Payload and resource IDs.
- [x] 6.3 Pass teleport, privacy, multi-player, dimension, restart and legacy-world tests.
- [ ] 6.4 Cut the bundle over only after all Nexus cutover gates pass; retain the disabled legacy implementation for the observation window.
  - [x] 6.4.1 Move the complete Space Unit authority (SavedData ownership, death-node lifecycle, friends/map projection, lodestone management, teleport sessions, item/block hooks and server ticks) into `TotemNexus` without a direct DeadRecall feature dependency.
  - [x] 6.4.2 Activate Nexus-owned payload, client UI, interaction and Mixin registrations as one atomic path, while gating the matching legacy registrations when `totem-nexus` is present.
  - [x] 6.4.3 Prove the assembled bundle has exactly one registration for every preserved identifier, payload receiver, event callback and Mixin, and pass standalone, bundle, legacy-world, restart, multi-player and Dedicated Server gates.
  - [x] 6.4.4 Publish or stage an immutable Nexus artifact, pin it in the exact-version bundle manifest, and retain the previous pin as the rollback target through the observation window.

## 7. Remaining gameplay and compatibility bundle

- [x] 7.1 Propose explicit repositories for each remaining bounded context.
- [x] 7.2 Keep unassigned gameplay in DeadRecall and out of Core until approved.
- [x] 7.3 Convert DeadRecall into an exact-version compatibility bundle and E2E test repository.
  - [x] 7.3.1 Stage content-addressed Core, Discord, Automata and Nexus artifacts with immutable versions and source commits.
  - [x] 7.3.2 Assemble the staged graph and prove it starts as one dedicated-server compatibility bundle without duplicate registration.
  - [x] 7.3.3 Make CI build the pinned graph from exact source revisions or published artifacts before its assembled-bundle E2E gate.
- [x] 7.4 Validate at least two lockstep releases with rollback evidence.
- [ ] 7.5 Enable independent versions and Modrinth publishing for stable repositories.
  - [ ] 7.5.1 Complete the Automata and Nexus cutover gates and two-release observation windows before declaring either repository stable.
  - [ ] 7.5.2 Configure independent release/version metadata and Modrinth publication only for modules with completed observation windows.
- [x] 7.6 Approve the three remaining bounded-context repositories without moving their implementation into Core.
  - [x] 7.6.1 Assign Alchemy Cauldron gameplay and its resources to `TotemAlchemy`.
  - [x] 7.6.2 Assign enchanting-power behavior and its Mixins to `TotemEnchanting`.
  - [x] 7.6.3 Assign general recipe/concrete-powder and other vanilla tweaks to `TotemVanillaTweaks`, while retaining portable-container ownership in `TotemRemnant`.
  - [x] 7.6.4 Create and publish the initial `TotemAlchemy`, `TotemEnchanting` and `TotemVanillaTweaks` repository scaffolds.
- [ ] 7.7 Inventory, extract, dual-validate and cut over each approved remaining gameplay repository independently.
  - [x] 7.7.1 Inventory the Alchemy Cauldron surface and record its ownership, preserved IDs, resource transfer and rollback seams in [`totem-alchemy-cutover-contract.md`](totem-alchemy-cutover-contract.md).
  - [x] 7.7.2 Extract TotemAlchemy 0.1.2 as a Java 25, Core-only module and pin its immutable source commit and SHA-512 artifact.
  - [x] 7.7.3 Record TotemAlchemy standalone Dedicated Server, cauldron restart and assembled-bundle cutover evidence; retain its two-release observation window.
  - [x] 7.7.4 Inventory and stage TotemEnchanting 0.1.0 with its atomic Mixin cutover; record exact-bundle evidence and retain its two-release observation window.
  - [ ] 7.7.5 Independently inventory and extract TotemVanillaTweaks after the Alchemy cutover gate is green.
