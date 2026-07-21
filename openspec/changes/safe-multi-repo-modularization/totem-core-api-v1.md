# TotemCore API v1 contract

## Scope

`TotemCore` v1 is a Fabric library mod.  It provides stable, versioned types for
communication between independently installed Totem feature mods; it does not
provide a gameplay feature.  The public API root is `dev.totem.core.api.v1` and
the implementation root is `dev.totem.core.internal`.

Only the following API families may be added in v1:

- `event` — lifecycle event interfaces and registration handles.  Event payloads
  are immutable records and carry a `contractVersion` when their meaning can
  evolve independently.
- `migration` — data-version constants, migration-step registration, and a
  result type.  A feature owns its data and registers migrations; Core must not
  know a feature SavedData key or schema.
- `identifier` — namespace-safe identifier construction and validation helpers.
- `permission` — server-side permission predicates and a neutral subject/context
  interface, without feature command names or policies.
- `version` — API-version negotiation and feature metadata used by the
  compatibility bundle.

Core may depend on Minecraft, Fabric Loader and Fabric API only where needed to
implement these contracts.  Its public API must not expose an implementation
class from another Totem module.

## Explicit exclusions

The following are rejected from Core even when more than one module currently
uses them:

- item, block, entity, menu, registry, recipe or creative-tab registration;
- death backpack capture or recovery, inventories, Trinkets, or the existing
  `com.adaptor.deadrecall.api.death` addon API;
- Space Unit, lodestone, teleport, friend, death-node, or distributed-spawn
  behaviour and SavedData;
- Copper Golem behaviour, screens, data components, scanners, or Cognition
  integrations;
- Discord transport, configuration, payloads, language resources, or UI;
- alchemy, enchanting, recipes, concrete powder, and container gameplay;
- client-only screens, renderers, Mixins, or client networking state.

Code is eligible for Core only after two independently owned feature modules
need the *same abstraction*, with an API proposal naming both consumers.  A
copy of a feature helper is not sufficient evidence.

## Compatibility and release policy

Core publishes a normal mod artifact and a separately consumable API artifact,
both with the same semantic version.  Feature modules declare an exact tested
Core version during the lockstep observation period; the DeadRecall bundle pins
that same exact version in its immutable manifest.

- A patch release fixes implementation defects without changing public API
  signatures or event semantics.
- A minor release may add source- and binary-compatible API.  New optional
  event fields require a new record/type or an explicit contract version; they
  must not silently alter an existing event's meaning.
- A major release is required for any incompatible API removal, signature
  change, changed permission meaning, or incompatible migration contract.

Deprecated public API remains functional for at least two released lockstep
bundle versions and one published minor Core release.  It is annotated with
`@Deprecated(since = "x.y", forRemoval = false)`, documents its replacement,
and has a compatibility test.  Removal requires a major Core release, migration
notes, and assembled DeadRecall bundle evidence.  Existing
`com.adaptor.deadrecall.api.death` types remain owned by TotemRemnant and must
be supplied as forwarding compatibility types during its approved addon
migration window; they are not moved into Core.

## Required verification in TotemCore

Every Core release runs API binary-compatibility checks against the previous
minor version, unit tests for event ordering/version negotiation/migration
dispatch, and a Dedicated Server startup test with Core as the only Totem mod.
The startup test must prove no client-only class loads and that Core registers
no gameplay registry content.  Each feature repository additionally tests its
declared Core version; the bundle tests the exact pinned graph.
