# DeadRecall

**One Fabric JAR. Eleven coordinated Totem modules. One survival ecosystem.**

DeadRecall 是 Minecraft 26.2 / Fabric 的 Totem 系列整合發行版。外層 DeadRecall 僅保留 legacy item-ID migration 與 exact-version nested-JAR 組裝；背包、死亡回收、銅魔像自動化、村民生產經濟、煉金、附魔、範圍挖掘、倉儲鎖、Nexus 傳送、Discord 事件與原版調整皆由十一個 Totem 模組負責。

> **Install only the DeadRecall bundle.／只安裝 DeadRecall 整合 JAR。** Eleven Totem modules are already embedded. Do not install duplicate standalone copies beside DeadRecall.

## Core Loop

**Gather → Automate → Trade → Prepare → Travel → Recover**

- Excavation、Copper Golems 與 working villagers 生產與搬運實際資源。
- Remnant 背包與 Locksmith 固定倉儲保護資源。
- Villagers 以實際庫存、食物、工具與存量交易維持生產循環。
- Alchemy 與 Enchanting 把採集材料轉成探索前準備。
- Nexus Space Units 提供 server-authoritative 移動與死亡目的地。
- Remnant death backpacks 把死亡回收重新接回探索流程。

## Included Modules

| Module | Version | Main role |
| --- | ---: | --- |
| TotemCore | `0.6.1` | Shared API, Totem Manual, events, migrations, version handshake |
| TotemRemnant | `0.2.12` | Backpacks, death recovery, container safety |
| TotemDiscordBridge | `0.1.7` | Optional Discord events, status and audit delivery |
| TotemAutomata | `0.1.13` | Copper Golem sorting, gathering, tools and fuel |
| TotemAlchemy | `0.1.24` | Dynamic brewing, multi-outcome ingredients and discovery |
| TotemEnchanting | `0.1.7` | Content-based chiseled-bookshelf enchanting power |
| TotemExcavation | `0.1.3` | Server-owned area selection and excavation hammers |
| TotemLocksmith | `0.1.1` | Connected storage locks, permissions, keys and audits |
| TotemVanillaTweaks | `0.1.9` | Vanilla-friendly recipes, sorting and gameplay adjustments |
| TotemNexus | `0.2.7` | Space Units, friends, travel and death destinations |
| TotemVillagers | `0.1.31` | Villager inventory, work, economy and settlement production |

These versions form one lockstep release. Do not replace only one embedded module with a different standalone version.

## Highlights

### Backpacks & Recovery
TotemRemnant provides four backpack tiers, dyeing, inventory-side access, death backpacks, recovery guidance, void protection and portable-container safety.

### Automation
TotemAutomata turns Copper Golems into configurable workers. TotemExcavation adds seven hammer tiers with bounded server-side area harvesting, normal protection hooks, loot and durability behavior.

### Villages
TotemVillagers gives villagers real inventories, food, tools, stock-aware trading, profession-specific work and production-oriented settlement generation.

### Alchemy & Enchanting
TotemAlchemy provides ingredient-driven multi-outcome brewing, success rates, Firefly Strength and discovery tracking in the Totem Manual. TotemEnchanting uses books stored in valid, unobstructed chiseled bookshelves to provide up to 64 enchanting power.

### Travel
TotemNexus provides material-sensitive Space Units, friends, mapping, cost validation and safe server-authoritative teleport sessions.

### Server Tools
TotemLocksmith protects supported fixed-container networks with padlocks, permissions, physical keys, automation rules and break auditing. TotemDiscordBridge can optionally relay configured Minecraft events through a separately configured Worker. TotemVanillaTweaks supplies shared vanilla-friendly adjustments such as container sorting.

## Requirements

- Minecraft `26.2`
- Fabric Loader `0.19.3` or newer
- Fabric API `0.154.2+26.2`
- Java `25`
- DeadRecall on both client and server
- Apache-2.0 license

## Install

1. Install the matching Minecraft, Fabric Loader, Fabric API and Java versions.
2. Put `deadrecall-2.4.20-bundled.jar` in `mods/` on both client and server.
3. Do **not** add standalone copies of the eleven embedded Totem modules.
4. Back up an important existing world before first migration.
5. On startup, confirm Fabric reports the expected exact Totem module set.

The release pipeline rebuilds all eleven modules from immutable source pins, verifies their exact identities, assembles the nested bundle, starts a dedicated Fabric server to the normal `Done` state and performs a Modrinth dry-run before publication.
