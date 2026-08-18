# DeadRecall

**One Fabric JAR. Eleven coordinated Totem modules. One survival ecosystem.**

DeadRecall 是 Minecraft 26.2 / Fabric 的 Totem 系列整合發行版。它把背包與死亡回收、銅魔像自動化、村民生產經濟、煉金、附魔、範圍挖掘、倉儲鎖、Nexus 傳送、Discord 事件整合與原版調整整合成一個經過固定版本驗證的生存系統。

> **Install only the DeadRecall bundle.／只安裝 DeadRecall 整合 JAR。** The eleven Totem modules are already embedded as nested JARs. Do not install duplicate standalone copies beside DeadRecall.

## Why DeadRecall

DeadRecall is not a loose mod pack. The outer host stays intentionally small while the eleven feature modules keep their own gameplay ownership. TotemCore provides the shared contracts between them: exact-version handshake, integrated manuals, neutral events, migrations, and cross-module death interfaces.

That gives the bundle three practical guarantees:

- **One install path** — client and server use the same verified module graph.
- **One progression language** — the Totem Manual and advancement flow connect otherwise separate systems.
- **One server authority model** — travel, death recovery, protected storage, inventory ownership, and economy mutations remain server-decided.

## Core Loop

**Gather → Automate → Trade → Prepare → Travel → Recover**

- Excavation tools, Copper Golems, and working villagers produce and move real resources.
- Backpacks and protected storage keep those resources usable and safe.
- Villagers trade from real stock and feed production chains instead of infinite hidden inventories.
- Alchemy and enchanting turn gathered materials into preparation.
- Nexus Space Units move players between established destinations.
- Remnant death backpacks and Nexus death destinations connect recovery back into exploration.

## Highlights

### Backpacks

TotemRemnant provides four backpack tiers with dyeing, 9–36 base slots, capacity and utility modules, an integrated 3×3 crafting area, inventory-side access, sorting compatibility, death recovery, beacon guidance, void protection, and safe module-removal checks.

Portable-container nesting and death-backpack behavior can be governed by server rules instead of relying on client trust.

### Automation

TotemAutomata turns Copper Golems into configurable workers. Use the Copper Wrench to bind sources and destinations, choose sorting or gathering behavior, configure collection zones and targets, assign tools and fuel, and integrate with protected storage rules.

TotemExcavation adds seven hammer tiers for controlled area excavation, giving automation and villages a practical resource supply without replacing normal survival progression.

### Villages

TotemVillagers gives villagers real inventories, food, tools, goods, and emeralds. Profession-specific workers gather, craft, consume resources, render held tools, and trade through the vanilla trade screen.

Naturally generated mangrove production villages extend that economy into world generation instead of keeping it inside menus only.

### Alchemy

TotemAlchemy uses ingredient-driven brewing where materials can contribute multiple possible effects and each addition changes the success calculation. Discoveries are recorded in the Totem Manual.

TotemEnchanting complements that progression with content-sensitive Inscribed Bookshelves that can contribute up to 64 enchanting power according to their actual contents.

### Travel

TotemNexus lets players register, discover, favorite, map, and safely travel between material-sensitive Space Units. Travel remains server validated and can integrate with friends and player-owned death destinations.

The death system is deliberately split by responsibility: Nexus chooses eligible destinations while Remnant owns item capture and the physical recovery backpack lifecycle.

### Server Tools

TotemLocksmith protects supported fixed-container networks with Padlocks, permissions, physical keys, automation rules, split semantics, and break auditing.

TotemDiscordBridge can relay configured Minecraft events, activity, public events, attachments, audit messages, and bot presence through an optional separately configured Worker. Discord delivery is optional; the gameplay bundle does not require it to function.

TotemVanillaTweaks provides shared vanilla-friendly adjustments such as generic container sorting and compatibility behavior used across the ecosystem.

## Quick Start

| Goal | What to do |
| --- | --- |
| Read the integrated guide | Enter a world, craft a module source block, then use a Book or Totem Manual on it to acquire that module chapter. |
| Open a backpack | Hold it and right-click. Capacity modules expand usable storage. |
| Use inventory-side storage | Keep a backpack available and open the normal player inventory. |
| Recover death items | Follow the recovery beacon to the server-owned death backpack. |
| Configure a Copper Golem | Craft a Copper Wrench and right-click the golem. |
| Register a Space Unit | Right-click an unregistered lodestone with a normal compass. |
| Open the Nexus map | Left-click a registered lodestone with a normal compass, then right-click. |
| Protect storage | Use a Padlock on a supported fixed container network. |
| Sort a container | Hover the side to sort and press the middle mouse button. |
| Manage death nodes | Run `/deadrecall deathnodes`, select your own node, and confirm deletion. |

## Included Modules

| Module | Version | Main role |
| --- | ---: | --- |
| [TotemCore](https://github.com/Yunitrish006006/TotemCore) | `0.6.0` | Shared API, Totem Manual, events, migrations, version handshake |
| [TotemRemnant](https://github.com/Yunitrish006006/TotemRemnant) | `0.2.11` | Backpacks, inventory integration, death recovery, container safety |
| [TotemAutomata](https://github.com/Yunitrish006006/TotemAutomata) | `0.1.12` | Copper Golem sorting, routing, gathering, protected-storage integration |
| [TotemNexus](https://github.com/Yunitrish006006/TotemNexus) | `0.2.6` | Space Units, friends, travel, death destinations |
| [TotemDiscordBridge](https://github.com/Yunitrish006006/TotemDiscordBridge) | `0.1.6` | Optional Discord event delivery and audit integration |
| [TotemAlchemy](https://github.com/Yunitrish006006/TotemAlchemy) | `0.1.23` | Dynamic alchemy and ingredient research |
| [TotemEnchanting](https://github.com/Yunitrish006006/TotemEnchanting) | `0.1.5` | Content-based enchanting power |
| [TotemExcavation](https://github.com/Yunitrish006006/TotemExcavation) | `0.1.2` | Area selection and excavation hammers |
| [TotemLocksmith](https://github.com/Yunitrish006006/TotemLocksmith) | `0.1.0` | Connected storage locks, permissions, keys, audits |
| [TotemVanillaTweaks](https://github.com/Yunitrish006006/TotemVanillaTweaks) | `0.1.8` | Recipes, sorting, vanilla-compatible adjustments |
| [TotemVillagers](https://github.com/Yunitrish006006/TotemVillagers) | `0.1.30` | Villager inventory, work, economy, rendering, world generation |

These versions form one lockstep release. Do not replace only one embedded module with a different standalone version.

## Requirements

- Minecraft `26.2`
- Fabric Loader `0.19.3` or newer
- Fabric API `0.154.2+26.2`
- Java `25`
- DeadRecall on both client and server
- Trinkets Updated is optional integration
- A separately configured Cloudflare Worker is only needed for Discord delivery
- Apache-2.0 license

## Install

1. Install the matching Minecraft, Fabric Loader, Fabric API, and Java versions.
2. Put `deadrecall-2.4.19-bundled.jar` in `mods/` on both client and server.
3. Do **not** add standalone copies of the eleven embedded Totem modules.
4. Back up an important existing world before first migration.
5. On startup, confirm Fabric reports the expected exact Totem module set.

## Migration

The DeadRecall host preserves controlled compatibility for legacy `deadrecall:*` item IDs and migrates them toward canonical `totem:*` ownership while preserving item components. It intentionally avoids an unsafe blanket rewrite of every untouched item during startup.

For source, issue tracking, technical documentation, and module-specific details, use the project links on this page or the individual Totem repositories listed above.
