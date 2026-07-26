# TotemAlchemy bundle-cutover contract

## Current state

`TotemAlchemy 0.1.1` is built from
`c01504a0b8a512c43f211c7a8b9882f7b13fe976` and is pinned in the exact
compatibility graph with SHA-512
`68d380b37f6d0a80cfbe55005c9a43dd3d948dbebd7a1b6030dd71fac1646475123a5e4eafc2676ed62330e84f3b080d393c82e996f5b22324005c27285f289e`.
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

`0.1.1` was clean-built and its production JAR retained all 50 owned resources.
With only TotemCore `0.1.2`, TotemAlchemy `0.1.1` and Fabric API installed, a
Java 25 Dedicated Server on port `25570` loaded both initializers, all 1,693
advancements and the three cauldron recipes, reached `Done`, saved every
dimension and restarted the same world without an advancement-load error. The
module-owned `deadrecall:alchemy_root` parent makes the preserved Alchemy
advancement IDs valid when DeadRecall is absent.
