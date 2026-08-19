# DeadRecall

**Final compatibility transition bundle for the Totem ecosystem.**

DeadRecall 2.4.22 is for existing Minecraft 26.2 / Fabric worlds that previously used DeadRecall and now want to move to standalone Totem modules safely.

## What changes in 2.4.22

- TotemCore `0.7.1` permanently owns the 14 retained `deadrecall:*` item identifiers.
- Hidden legacy stacks remain decode-safe even after the DeadRecall host is removed.
- Canonical migration remains lazy and preserves the full Data Component patch and item count.
- **TotemVillagers is no longer bundled or required.** Install it separately only if you want it.

## Included transition modules

| Module | Version |
| --- | ---: |
| TotemCore | `0.7.1` |
| TotemRemnant | `0.2.13` |
| TotemDiscordBridge | `0.1.8` |
| TotemAutomata | `0.1.15` |
| TotemAlchemy | `0.1.26` |
| TotemEnchanting | `0.1.8` |
| TotemExcavation | `0.1.5` |
| TotemLocksmith | `0.1.4` |
| TotemVanillaTweaks | `0.1.10` |
| TotemNexus | `0.3.0` |

## Recommended migration

1. Back up the world.
2. Replace DeadRecall 2.4.21 with `deadrecall-2.4.22-bundled.jar`.
3. Start the server normally once and confirm the log reports all **14 TotemCore-owned legacy item aliases** were verified.
4. Stop the server normally.
5. You may then remove DeadRecall and install only TotemCore 0.7.1+ plus the standalone Totem modules you actually want.
6. TotemVillagers may be omitted.

The one-time 2.4.22 boot is a compatibility checkpoint rather than a destructive whole-world rewrite. Safety comes from TotemCore permanently retaining the old item registry aliases, including items in offline player data and unloaded containers.

## Requirements

- Minecraft `26.2`
- Fabric Loader `0.19.3` or newer
- Fabric API `0.154.2+26.2`
- Java `25`
- Apache-2.0 license
