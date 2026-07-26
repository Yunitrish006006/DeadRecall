# TotemVanillaTweaks bundle-cutover contract

## Current state

`TotemVanillaTweaks 0.1.0` is built from
`f18ce5d193bc51fa45c072b30fabbd8072cb978d` and is pinned in the exact
compatibility graph with SHA-512
`f06ab91e4a42bc5fa494424089879dfdd63d9d2aa887d9244c254534e45889a9eef7a3fd7d7175e6fbf8c37602e0a3582e742903ad6c832e67fbafe731f6a251`.
It is a Java 25 module that depends only on TotemCore and Fabric API.

The module owns the `minecraft:lectern` recipe override and bookshelf recipe
removal, water hardening for concrete-powder `ItemEntity` stacks, and furnace
experience release after hopper result extraction. Its atomic Mixin group is
`RecipeManagerMixin`, `ConcretePowderItemEntityMixin`,
`HopperBlockEntityMixin`, and `AbstractFurnaceBlockEntityAccessor`.

## Atomic Mixin decision

`VanillaTweaksCutover` selects external authority only for
`totem-vanilla-tweaks >= 0.1.0`. When selected,
`deadrecall.vanilla-tweaks.mixins.json` disables every legacy vanilla-tweak
Mixin together. The root copies remain compiled as an observation-window
rollback path but cannot apply beside the external group.

`data/minecraft/recipe/lectern.json` is now supplied by the external artifact;
the legacy root GameTests for this recipe and concrete powder were transferred
so root-only validation does not implicitly duplicate this module's authority.

## Resource and runtime contract

Every module build and Dedicated Server gate uses Java 25. The exact-version
bundle starts an isolated server on port `25570`; any legacy-world seed run
uses `25571`. These are harness isolation ports, not gameplay protocol values.

The module owns a vanilla-namespace recipe override, so artifact verification
checks `data/minecraft/**` in addition to preserved `deadrecall` resources.
It intentionally does not own portable-container policy or Stone Bowl recipes:
those remain with TotemRemnant and TotemAlchemy respectively.

## Recorded standalone evidence

The Java 25 GameTest suite ran ten required tests: the Fabric baseline, five
concrete-powder cases, three lectern cases, and a direct original
`tryTakeInItemFromSlot` path test proving hopper extraction awards stored
furnace experience. All passed. With only TotemCore `0.1.2`,
TotemVanillaTweaks `0.1.0` and Fabric API installed, a Java 25 Dedicated
Server on port `25570` loaded both initializers, reached `Done` and saved every
dimension. Exact bundle validation remains the release gate.
