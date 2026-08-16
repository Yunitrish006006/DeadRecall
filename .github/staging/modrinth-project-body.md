# DeadRecall

**One verified Fabric JAR, eleven coordinated Totem modules, one continuous survival ecosystem.**

DeadRecall 是 Minecraft 26.2 / Fabric 的 Totem 系列整合發行版。它不是只提供單一功能的模組，也不是把一堆互不相干的 JAR 隨意打包；它把儲存、死亡回收、自動化、生產經濟、煉金、附魔、挖掘、遠距旅行、伺服器事件與原版調整鎖成一組可驗證的系統。

> **Install only the DeadRecall bundle.／只安裝 DeadRecall 整合 JAR。** The eleven Totem modules listed below are already embedded. Installing the same standalone modules beside DeadRecall creates duplicate mod IDs and Fabric will refuse to start.

## Overview

DeadRecall is the **distribution and compatibility host** for the current Totem ecosystem:

- It ships the exact eleven-module combination in one Fabric nested-JAR bundle.
- TotemCore supplies the shared contracts: client/server version handshake, common APIs, manual registry, neutral event bus, death-system interfaces, achievements, and migration support.
- Each feature module still owns its own gameplay data and logic. Core coordinates them without becoming a second copy of every system.
- The outer DeadRecall host is deliberately small: it assembles the verified bundle and preserves compatibility for 14 legacy `deadrecall:*` item IDs.
- A server rejects a client with a missing or mismatched bundled module before that player enters the world.

換句話說，DeadRecall 的價值不只是「一次安裝十一個功能」，而是讓十一個功能使用同一套版本契約、教學入口、死亡流程與事件邊界，避免整合包常見的版本漂移與重複實作。

## Modules

| Layer / 層次 | Modules | Role in the complete experience / 整體作用 |
| --- | --- | --- |
| Foundation / 共同基座 | **TotemCore** | Exact-version handshake, shared API, combined Totem Manual, event bus, migration, and cross-module death contracts. |
| Survival inventory / 生存物資 | **TotemRemnant** | Four backpack tiers, modules, inventory integration, server-owned death backpacks, recovery, and container-safety policy. |
| Storage protection / 倉儲保護 | **TotemLocksmith** | One Padlock per fixed-Hopper network, root-side split semantics, ACLs, physical keys, automation rules, and break audits. |
| Production / 生產供應 | **TotemAutomata**, **TotemExcavation** | Copper Golem sorting and gathering, controlled area excavation, tool recognition, storage routing, fuel, and collection sessions. |
| Living economy / 村莊經濟 | **TotemVillagers** | Villagers carry real food, tools, goods, and emeralds; work, gather, craft from live recipes, and trade through the vanilla trade screen. |
| Knowledge and progression / 知識成長 | **TotemAlchemy**, **TotemEnchanting** | Dynamic multi-effect brewing, ingredient research, potion variants, and content-driven enchanting power. |
| Mobility and operations / 移動營運 | **TotemNexus**, **TotemDiscordBridge**, **TotemVanillaTweaks** | Space Unit travel, friends, death destinations, server event relay, container sorting, recipes, and vanilla-friendly utility rules. |

## Gameplay

### Production

- Excavation tools and Copper Golems increase controlled resource gathering and storage throughput.
- Farmers, miners, lumberjacks, fishermen, and crafters use actual inventories rather than infinite hidden stock.
- Villagers buy and sell with emeralds; toolsmith supply chains consume wood and mined materials, provide tools to workers, and can manufacture backpack tiers from available inputs.
- Backpacks hold and sort the results; alchemy and enchanting turn those materials into preparation for the next expedition.

這使村莊、自動化與玩家裝備不是三套分離玩法，而是「採集 → 原料 → 工具／商品 → 交易 → 再生產」的同一條供應鏈。玩家仍然透過原版交易介面與村民互動。

### Recovery

- Nexus registers lodestone Space Units, maps destinations, quotes costs, manages friends, and validates travel sessions on the server.
- Remnant captures ordinary drops into a server-authoritative death backpack and owns its physical recovery lifecycle.
- Through TotemCore contracts, Nexus provides the death destination and retained-interface eligibility while Remnant performs item capture and respawn restoration.
- Remnant scans eligible Nexus interfaces at death and retains one deterministically even if it has never completed a teleport, without bypassing normal travel cost or safety checks.
- Discord Bridge can subscribe to neutral Core events for death, recovery, space-unit, and administrative notifications without directly linking the feature modules together.

死亡背包、死亡傳送點與靈魂綁定介面因此是跨模組共同完成的一條流程，不是三套互相衝突的死亡保護。

### Knowledge

- A basic guide is granted once when a player first enters a world.
- Every participating module gates its tree behind crafting the first source block, then using a Book or Totem Manual on that block to acquire its method chapter.
- Later module advancements branch only after the chapter is acquired; branch roots no longer grant themselves on world entry.
- When a player holds multiple Totem-series manuals, they can be combined into one ordered Totem Manual and later split back into individual books.
- The manual supports illustrated two-page spreads, persistent discoveries, dynamic alchemy research, and current world-rule status.
- Receiving the tutorial book unlocks the **Knowledge Is Power / 知識就是力量** advancement.

### Compatibility

- Backpack screens extend the vanilla visual language and allow normal server-authoritative take, place, swap, drag, throw, and shift-click behavior.
- VanillaTweaks sorts compatible menus through generic container behavior, so Remnant backpacks work without a hard gameplay dependency.
- Villager crafting checks the server's current recipe manager; removed or replaced recipes are not silently recreated by a fixed hidden list.
- Sensitive operations such as death recovery, travel, inventory ownership, and economy stock changes are decided by the server.

## Features

- **Backpacks and recovery** — four sizes, dyeing, 9–36 base slots, capacity and utility modules, a single-row upgrade bay, integrated 3×3 crafting, inventory side panel, recovery beacon, void protection, and safe module removal checks.
- **Connected storage locks** — one Padlock protects supported containers connected by fixed Hoppers; cutting the graph leaves only the original root side locked, and successful non-owner breaks can notify Discord.
- **Configurable survival rules** — death-backpack generation, owner-only pickup, and portable-container nesting behavior can be governed per world; the Totem Manual exposes relevant current states.
- **Copper Golem automation** — source/destination binding, sorting, collection zones, targets, tools, fuel, cache, and optional rule-assisted decisions through the Copper Wrench.
- **Dynamic alchemy** — ingredients can contribute several possible effects, every addition changes the success calculation, and discoveries are recorded in the manual. Vanilla-inspired paths coexist with Totem variants such as cherry and glow-themed brews.
- **Enchanting and excavation** — Inscribed Bookshelves contribute up to 64 enchantment power according to real contents; seven hammer tiers provide controlled area excavation.
- **Living villages** — stocked villagers eat, hold tools in their rendered hands, work with profession-specific actions, trade goods they possess, and support naturally generated mangrove production villages.
- **Nexus travel** — register, discover, map, favorite, and safely travel between material-sensitive Space Units; players can inspect and delete only their own death destinations with confirmation.
- **Server integration** — optional Discord delivery for chat, player activity, public events, attachments, audit messages, and bot presence through a separately configured Worker.

## Quick Start

| Goal | What to do |
| --- | --- |
| Read the integrated guide | Enter a world to receive the basic guide, craft a module's source block, then use a Book or Totem Manual on it to acquire that chapter. |
| Open a backpack | Hold it and right-click. Capacity modules expand its usable storage. |
| Use the backpack beside inventory | Keep a backpack available, open the normal player inventory, then take and place items directly in the side panel. |
| Recover death items | Follow the red beacon to the server-owned death backpack. |
| Configure a Copper Golem | Craft a Copper Wrench, then right-click the golem. |
| Register a Space Unit | Right-click an unregistered lodestone with a normal compass. |
| Open the Nexus map | Left-click a registered lodestone with a normal compass, then right-click. |
| Remove your death destination | Run `/deadrecall deathnodes`, select your own node, and confirm again within 30 seconds. The death backpack itself is not deleted. |
| Sort a container | Hover the side to sort and press the middle mouse button. |
| Protect connected storage | Use a Padlock on a supported container; fixed Hoppers and supported containers in that network share the same lock. |

## Versions

| Module | Version | Responsibility |
| --- | ---: | --- |
| [TotemCore](https://github.com/Yunitrish006006/TotemCore) | `0.6.0` | Shared API, guided manuals, events, migrations, and version handshake |
| [TotemRemnant](https://github.com/Yunitrish006006/TotemRemnant) | `0.2.11` | Backpacks, interactive inventory panel, death recovery, and container safety |
| [TotemAutomata](https://github.com/Yunitrish006006/TotemAutomata) | `0.1.12` | Copper Golem sorting, routing, collection, and Locksmith-aware automation |
| [TotemNexus](https://github.com/Yunitrish006006/TotemNexus) | `0.2.6` | Space Units, friends, travel, and soulbound interface eligibility |
| [TotemDiscordBridge](https://github.com/Yunitrish006006/TotemDiscordBridge) | `0.1.6` | Minecraft events, Locksmith break alerts, and Discord delivery |
| [TotemAlchemy](https://github.com/Yunitrish006006/TotemAlchemy) | `0.1.23` | Dynamic alchemy, ingredient research, and cauldron content |
| [TotemEnchanting](https://github.com/Yunitrish006006/TotemEnchanting) | `0.1.5` | Content-based bookshelf enchantment power |
| [TotemExcavation](https://github.com/Yunitrish006006/TotemExcavation) | `0.1.2` | Area selection and excavation hammers |
| TotemLocksmith | `0.1.0` | Fixed-container network locks, permissions, keys, and audits |
| [TotemVanillaTweaks](https://github.com/Yunitrish006006/TotemVanillaTweaks) | `0.1.8` | Recipes, sorting, and vanilla-compatible adjustments |
| TotemVillagers | `0.1.23` | Villager inventory, work, economy, rendering, and world generation |

These versions form one lockstep release. Do not replace only one embedded module with a different standalone version.

## Requirements

- Minecraft `26.2`
- Fabric Loader `0.19.3` or newer
- Fabric API `0.154.2+26.2`
- Java `25`
- DeadRecall is required on both client and server
- Trinkets Updated is an optional integration
- A separately configured Cloudflare Worker is optional and only needed for Discord delivery
- License: Apache-2.0

### Installation

1. Install the matching Minecraft, Fabric Loader, Fabric API, and Java versions.
2. Place `deadrecall-2.4.11-bundled.jar` in `mods/` on both client and server.
3. Do **not** add standalone copies of the eleven embedded Totem modules.
4. Back up an important existing world before first migration, then confirm the startup log reports the expected exact module set.

Legacy `deadrecall:*` items remain readable and migrate through controlled interaction paths to canonical `totem:*` IDs while preserving their components. DeadRecall does not perform an unsafe blanket rewrite of every untouched item during startup.
