# TotemEnchanting bundle-cutover contract

## Current state

`TotemEnchanting 0.1.0` is built from
`1fcf1483b06040cde80293acde63c2b8ffbde780` and is pinned in the exact
compatibility graph with SHA-512
`ce8fb9b77d22ed0d439e70cd9783693f87b33c6c19a2b9cdd9df841d090a459a8866a61fbaebcf9ed0850e4564a353eaa8c8d39a241611e5e3fd7a93be35cb40`.
It is a Java 25 module that depends only on TotemCore and Fabric API.

The module owns the weighted Chiseled Bookshelf power rule and all three
enchanting behavior Mixins: `EnchantmentHelperMixin`,
`EnchantmentMenuMixin` and `EnchantingTableBlockMixin`. It introduces no
custom registry IDs or `deadrecall` data/asset resource owner paths; vanilla
enchantment IDs and item components remain the persistent compatibility
surface.

## Atomic Mixin decision

`EnchantingCutover` selects external authority only for
`totem-enchanting >= 0.1.0`. When selected,
`deadrecall.enchanting.mixins.json` disables all three legacy enchanting
Mixins together. The legacy helper and Mixins remain compiled as an
observation-window rollback path but cannot apply beside the external Mixins.

The external helper reads `STORED_ENCHANTMENTS` for Enchanted Books. This
corrects the former rollback implementation, which incorrectly inspected the
ordinary-item `ENCHANTMENTS` component and therefore assigned enchanted books
zero extra power. The correction is mirrored in the root rollback source.

## Resource and runtime contract

Every module build and Dedicated Server gate uses Java 25. The exact-version
bundle starts an isolated server on port `25570`; any legacy-world seed run
uses `25571`. These are harness isolation ports, not gameplay protocol values.

The module has no owned `assets/deadrecall/**` or `data/deadrecall/**` paths,
so the artifact resource check deliberately records an empty compatibility
resource surface. The assembled bundle must still expose exactly one copy of
each enchanting Mixin and initializer.

## Required evidence before the observation window

1. TotemEnchanting clean build and Java 25 dedicated-server GameTests for
   normal/enchant-book weights and the 64-power cap.
2. Standalone Dedicated Server startup with only TotemCore and Fabric API.
3. Exact lockstep assembly with one enchanting Mixin owner and no duplicate
   initializer before independent release publication is enabled.

## Recorded standalone evidence

The module's GameTest suite ran three required tests: the Fabric baseline plus
weighted normal/enchant-book power and the 64-power cap. All passed. With only
TotemCore `0.1.2`, TotemEnchanting `0.1.0` and Fabric API installed, a Java 25
Dedicated Server on port `25570` loaded both initializers, reached `Done` and
saved every dimension. Exact bundle validation remains the release gate.
