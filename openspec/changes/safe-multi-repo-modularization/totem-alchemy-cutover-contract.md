# TotemAlchemy bundle-cutover contract

## Current state

`TotemAlchemy 0.1.2` is built from
`fff959536487bd5de4ed6e12c7e6ea2a52b65653` and is pinned in the exact
compatibility graph with SHA-512
`129e1da238384511b3586a0ea8fd44fe33d51876eda0dc494fb9284e3d258b9b02f1b6df4a611365c0f25bd193580e11e4849d3ed86c32805c5857e6281cb5ea`.
It is a Java 25 module that depends only on TotemCore and Fabric API.

The module preserves the established `deadrecall:*` block, block-entity,
item, effect, criterion and recipe identifiers. It owns the Alchemy Cauldron,
Pig Manure, Cherry Brew, their two Mixins, cauldron persistence and all 50
verified compatibility data/asset paths. The shared locale files remain in
DeadRecall until a later extraction can avoid duplicate paths.

## Atomic registration decision

`AlchemyCutover` selects external authority only for
`totem-alchemy >= 0.1.0`. When selected, DeadRecall skips together:

- `ModBlocks`, `ModBlockEntities`, `ModMobEffects`,
  `LegacyGameplayItemRegistration` and `LegacyGameplayCriteriaRegistration`;
- `LegacyGameplayBootstrap` Alchemy callbacks and
  `LegacyGameplayItemGroupRegistration`; and
- the legacy `PigMixin` and `SnowballMixin` through
  `deadrecall.alchemy.mixins.json`.

The old source remains compiled as the observation-window rollback path, but
may not register when the module is installed. The general Flint-from-Bowl
serializer remains in DeadRecall pending TotemVanillaTweaks; it resolves the
preserved `deadrecall:stone_bowl` registry ID and does not load the legacy
Alchemy item owner.

## Resource and runtime contract

The 49 preserved legacy owner paths are registered in
`delegated-compatibility-surface.txt`, removed from the root JAR, verified
byte-for-byte in TotemAlchemy's production JAR and checked for duplicate paths
in the assembled bundle. This preserves the committed compatibility baseline
without allowing two resource owners.

Every module build and Dedicated Server gate uses Java 25. The exact-version
bundle starts an isolated server on port `25570`; any legacy-world seed run
uses `25571`. These are harness isolation ports, not gameplay protocol values.

## Required evidence before completion

1. TotemAlchemy clean build, resource-artifact check and standalone Dedicated
   Server startup with Core and Fabric API only.
2. Exact lockstep assembly with a single registration and initializer for all
   preserved Alchemy ownership, including no duplicate resource path or Mixin.
3. Alchemy Cauldron legacy-state/reload/restart coverage and one compatibility
   bundle restart observation before independent releases are enabled.

## Recorded standalone evidence

`0.1.2` was clean-built; its production JAR retained all 50 owned resources.
Its Java 25 Dedicated Server GameTest suite ran three required tests: the
framework baseline plus cauldron NBT round-trip and legacy `HOT_COCOA` state
migration. All passed and the test world saved every dimension. With only
TotemCore `0.1.2`, TotemAlchemy `0.1.2` and Fabric API installed, a Java 25
Dedicated Server on port `25570` loaded both initializers, all 1,693
advancements and the three cauldron recipes, reached `Done` and saved every
dimension. The module-owned `deadrecall:alchemy_root` parent makes the
preserved Alchemy advancement IDs valid when DeadRecall is absent.

The root lockstep workflow rebuilds this exact source, runs its GameTests,
checks its resource surface and starts the assembled bundle. The resulting
exact-version bundle run is required before closing the observation window.
