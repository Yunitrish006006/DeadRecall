# DeadRecall

DeadRecall is the tested, exact-version distribution of the **Totem ecosystem** for **Minecraft 26.2 / Fabric**. Install one JAR to get backpacks and death recovery, Copper Golem automation, dynamic alchemy, enchanting, excavation, Nexus travel, villager production and trade, Discord integration, and quality-of-life changes.

> **Install only the DeadRecall bundle.** Its ten Totem modules are already embedded. Installing the same standalone module JARs beside DeadRecall creates duplicate mod IDs and Fabric will refuse to start.

## Features

- **Backpacks and recovery** — four backpack sizes, dyeing, capacity and utility modules, inventory side panel, server-authoritative death backpacks, recovery beacon, void protection, and configurable container-safety rules.
- **Copper Golem automation** — configure source and destination storage, sorting, collection areas, tools, fuel, and optional rule-assisted decisions with the Copper Wrench.
- **Totem Nexus** — register lodestone Space Units, discover destinations, manage friends, preview travel costs, travel through server-validated sessions, and manage your own death destinations.
- **Dynamic alchemy** — data-driven ingredients can contribute multiple possible effects; success is recalculated as ingredients are added. Includes vanilla-inspired brewing paths and Totem variants.
- **Enchanting and excavation** — Inscribed Bookshelves contribute enchantment power according to their contents, while seven excavation-hammer tiers support controlled area mining.
- **Living villagers** — villagers keep real inventories, food, tools, goods, and emeralds; farmers, miners, lumberjacks, fishermen, and crafters work and trade within an economic cycle. Mangrove villages add production areas suited to the biome.
- **Vanilla-friendly utilities** — container sorting, lectern recipes, concrete-powder hardening, hopper/furnace experience handling, and other survival adjustments.
- **Migration and integration** — legacy `deadrecall:*` items remain readable and migrate safely to canonical `totem:*` IDs. TotemCore supplies the shared manual, event bus, and exact-version client/server handshake.

## Five-minute start

| Goal | What to do |
| --- | --- |
| Open a backpack | Hold it and right-click. Capacity modules expand it from 9 to 36 slots. |
| Recover death items | Follow the red beacon to the server-owned death backpack. |
| Configure a Copper Golem | Craft a Copper Wrench, then right-click the golem to open its configuration. |
| Register a Space Unit | Right-click an unregistered lodestone with a normal compass. |
| Open the Nexus map | Left-click a registered lodestone with a normal compass, then right-click. |
| Remove your death destination | Run `/deadrecall deathnodes`, select your own node, and confirm again within 30 seconds. The death backpack itself is not deleted. |
| Sort a container | Hover the side to sort and press the middle mouse button. |

## Exact bundled versions

| Module | Version | Main responsibility |
| --- | ---: | --- |
| [TotemCore](https://github.com/Yunitrish006006/TotemCore) | `0.5.0` | Shared API, manual, migrations, and version handshake |
| [TotemRemnant](https://github.com/Yunitrish006006/TotemRemnant) | `0.2.10` | Backpacks, death recovery, and container safety |
| [TotemAutomata](https://github.com/Yunitrish006006/TotemAutomata) | `0.1.11` | Copper Golem sorting and collection |
| [TotemNexus](https://github.com/Yunitrish006006/TotemNexus) | `0.2.5` | Space Units, friends, travel, and death destinations |
| [TotemDiscordBridge](https://github.com/Yunitrish006006/TotemDiscordBridge) | `0.1.5` | Minecraft and Discord bridge |
| [TotemAlchemy](https://github.com/Yunitrish006006/TotemAlchemy) | `0.1.22` | Dynamic alchemy and cauldron content |
| [TotemEnchanting](https://github.com/Yunitrish006006/TotemEnchanting) | `0.1.4` | Content-based bookshelf enchantment power |
| [TotemExcavation](https://github.com/Yunitrish006006/TotemExcavation) | `0.1.1` | Area selection and excavation hammers |
| [TotemVanillaTweaks](https://github.com/Yunitrish006006/TotemVanillaTweaks) | `0.1.7` | Recipes, sorting, and vanilla adjustments |
| [TotemVillagers](https://github.com/Yunitrish006006/TotemVillagers) | `0.1.22` | Villager inventories, work, rendering, and economy |

These versions are locked and tested as one set. The client and server handshake rejects missing modules or mismatched versions before joining a world.

## Requirements and compatibility

- Minecraft `26.2`
- Fabric Loader `0.19.3` or newer
- Fabric API `0.154.2+26.2`
- Java `25`
- DeadRecall is required on both client and server
- Trinkets Updated is an optional integration
- License: Apache-2.0

## 中文說明

DeadRecall 是 Minecraft 26.2 / Fabric 的 Totem 系列單一整合發行版。安裝一個 JAR，即可取得已鎖定版本並一起驗證過的背包、死亡回收、銅魁儡自動化、動態煉金、附魔、挖掘、Nexus 傳送、村民生產經濟、Discord 串接及原版玩法調整。

- Client 與 Server 都要安裝相同版本的 DeadRecall、Fabric Loader 與 Fabric API。
- 十個 Totem 子模組已經內嵌，**不要再把相同子模組的獨立 JAR 放進 `mods/`**。
- 舊世界中的 `deadrecall:*` 物品仍可讀取，並會在安全互動流程中轉換成新的 `totem:*` ID。
- 問題回報、原始碼與完整文件請使用本頁的連結。
