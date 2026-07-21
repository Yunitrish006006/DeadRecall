# Remaining gameplay repository proposals

These are proposed bounded contexts, not approved extractions. Until each has
its own OpenSpec and validation plan, its code remains in DeadRecall.

| Proposed repository | Owns | Required seam before extraction |
|---|---|---|
| `TotemAlchemy` | cauldron recipes and alchemy effects | no dependency on Remnant/Nexus registries |
| `TotemCraftingRules` | lectern recipe override and recipe policy | explicit Fabric recipe hook API |
| `TotemEnchanting` | enchanting-table and enchantment gameplay | no direct menu/item ownership from another feature |
| `TotemContainerSafety` | portable-container nesting policy and diagnostics | a versioned policy API for Remnant consumers |
| `TotemWorldInteractions` | concrete-powder item behavior and future vanilla interaction tweaks | standalone item/entity hooks and GameTests |

None of these owns a Core API merely because it has two callers. Core additions
require two independently extracted modules and a separate API proposal.
