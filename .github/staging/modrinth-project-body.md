# DeadRecall

**Final compatibility transition bundle for the Totem ecosystem.**

DeadRecall 2.4.22 is for existing Minecraft 26.2 / Fabric worlds that previously used DeadRecall and now want to move to standalone Totem modules safely.

## Changes in 2.4.22

- TotemCore `0.7.2` permanently owns the 14 retained `deadrecall:*` item identifiers and seeds the alias table independently of Fabric entrypoint ordering.
- Hidden legacy stacks remain decode-safe even after the DeadRecall host is removed.
- Canonical migration remains lazy and preserves the full Data Component patch and item count.
- **TotemVillagers is no longer bundled or required.** It remains an optional standalone module.

## Included modules

| Module | Version |
| --- | ---: |
| TotemCore | `0.7.2` |
| TotemRemnant | `0.2.13` |
| TotemDiscordBridge | `0.1.8` |
| TotemAutomata | `0.1.15` |
| TotemAlchemy | `0.1.26` |
| TotemEnchanting | `0.1.8` |
| TotemExcavation | `0.1.5` |
| TotemLocksmith | `0.1.4` |
| TotemVanillaTweaks | `0.1.10` |
| TotemNexus | `0.3.0` |

## Migration

1. Back up the world.
2. Replace DeadRecall 2.4.21 with `deadrecall-2.4.22-bundled.jar`.
3. If the world previously used TotemVillagers, install standalone TotemVillagers 0.1.32 **before the first 2.4.22 boot** so its SavedData and runtime state remain available during the checkpoint. Omit it only when you intentionally want to retire Villagers functionality.
4. Start the server normally once and confirm the log reports all **14 TotemCore-owned legacy item aliases** were verified.
5. Stop the server normally.
6. You may then remove DeadRecall and install only TotemCore 0.7.2+ plus the standalone Totem modules you actually want.
7. Keep standalone TotemVillagers installed if you want the existing Villagers systems to continue; remove it only as an explicit feature-retirement choice.

The one-time 2.4.22 boot is a compatibility checkpoint rather than a destructive whole-world rewrite. Item safety comes from TotemCore permanently retaining the old item registry aliases, including items in offline player data and unloaded containers. TotemVillagers SavedData is a separate feature-data boundary and should not be implicitly retired by the transition bundle.

## Requirements

- Minecraft `26.2`
- Fabric Loader `0.19.3` or newer
- Fabric API `0.154.2+26.2`
- Java `25`
- Apache-2.0 license