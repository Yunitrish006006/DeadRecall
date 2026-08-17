# DeadRecall

DeadRecall 是 Minecraft Fabric 26.2 的 Totem 系列相容整合主機。目前
發佈版本為 **2.4.13**；外層只負責兩件事：

1. 註冊並轉換 14 個舊 `deadrecall:*` 物品 ID。
2. 以 Fabric nested JAR 精確整合十一個 `totem-*` 模組。

背包、死亡保護、銅魁儡、Nexus、鎖箱、村民、挖掘、附魔、煉金、Discord
與原版調整等玩法
全部由 nested modules 擁有；DeadRecall 外層不再包含玩法 Mixin、GUI、
payload、指令或 fallback 實作。

## 安裝

### 單一整合包（一般玩家／伺服器建議）

1. 安裝 Java 25。
2. 建立 Minecraft 26.2 的 Fabric Loader 0.19.3+ 遊戲實例。
3. 將 Fabric API `0.154.2+26.2` 放入 `mods/`。
4. 將 DeadRecall 2.4.13 的單一發佈 JAR 放入 `mods/`。
5. Client 與 Server 使用相同版本；啟動後確認 log 同時載入十一個
   `totem-*` 模組。

> DeadRecall 2.4.13 已內含 TotemCore、Remnant、Automata、Nexus、
> Discord Bridge、Alchemy、Enchanting、Excavation、Locksmith、
> Vanilla Tweaks 與 Villagers。不要再把
> 這些模組的獨立 JAR 放入同一個 `mods/`，否則 Fabric 會偵測到重複
> mod ID。

多人遊戲會在玩家進入世界前逐一比對 DeadRecall 與上述十一個內含模組的
實際版本。Client 缺少握手、缺少任一模組或任何一個版本不同時，Server
會拒絕連線並列出不一致項目。這個通用連線 gate 由內含的 TotemCore
提供，DeadRecall 外層仍只有舊 ID 遷移與 nested-JAR 組裝兩項責任。

### 獨立模組（模組包作者／開發者）

不使用 DeadRecall 整合 JAR 時，可以改裝精確相容的獨立模組：

| 模組 | 功能 | Repository |
| --- | --- | --- |
| TotemCore `0.6.0` | 共用 API、手冊、事件與舊 ID 遷移 | [TotemCore](https://github.com/Yunitrish006006/TotemCore) |
| TotemRemnant `0.2.11` | 背包、死亡背包、容器安全 | [TotemRemnant](https://github.com/Yunitrish006006/TotemRemnant) |
| TotemAutomata `0.1.12` | 銅魁儡分類與採集 | [TotemAutomata](https://github.com/Yunitrish006006/TotemAutomata) |
| TotemNexus `0.2.6` | Space Unit、好友與傳送 | [TotemNexus](https://github.com/Yunitrish006006/TotemNexus) |
| TotemDiscordBridge `0.1.6` | Minecraft ↔ Discord | [TotemDiscordBridge](https://github.com/Yunitrish006006/TotemDiscordBridge) |
| TotemAlchemy `0.1.23` | 生存煉金與煉藥鍋 | [TotemAlchemy](https://github.com/Yunitrish006006/TotemAlchemy) |
| TotemEnchanting `0.1.5` | 雕紋書櫃附魔力 | [TotemEnchanting](https://github.com/Yunitrish006006/TotemEnchanting) |
| TotemExcavation `0.1.2` | 區域挖掘與錘具 | [TotemExcavation](https://github.com/Yunitrish006006/TotemExcavation) |
| TotemLocksmith `0.1.0` | 相連容器鎖與權限 | Modrinth standalone project |
| TotemVanillaTweaks `0.1.8` | 整理、配方與原版調整 | [TotemVanillaTweaks](https://github.com/Yunitrish006006/TotemVanillaTweaks) |
| TotemVillagers `0.1.25` | 村民工作、庫存與聚落 | Modrinth standalone project |

每個功能模組都要求 `totem-core =0.6.0` 與 Fabric API。不要混用其他
候選版本；完整精確組合記錄於
[lockstep manifest](openspec/changes/safe-multi-repo-modularization/lockstep-manifest.json)。

## 五分鐘快速上手

| 想做的事 | 操作 |
| --- | --- |
| 開始學習模組 | 首次進入世界會取得基礎手冊；依進度製作來源方塊，再以書本或手冊右鍵取得模組章節 |
| 使用一般背包 | 右鍵開啟；可升級為 9／18／27／36 格，並在工作台用原版染料染色 |
| 快速存取背包 | 打開玩家物品欄後，直接在側邊背包格拿取、放入或移動物品 |
| 找回死亡物品 | 前往紅色光柱；死亡時會自動掃描合格的 Nexus 傳送介面並保留一個，不必先傳送過 |
| 鎖住相連倉庫 | 用掛鎖右鍵箱子；固定漏斗連起來的容器共用一把鎖，從中切斷時僅根容器側保留鎖定 |
| 管理銅魁儡 | 合成銅扳手，右鍵銅魁儡選取並開啟 GUI |
| 設定分類 | 綁定來源銅箱、加入目的地、放入燃料後啟動 |
| 設定採集 | 設定 Home、兩個角點、採集目標、工具與燃料後啟動 |
| 註冊 Space Unit | 普通羅盤右鍵未註冊磁石 |
| 探索 Space Unit | 普通羅盤左鍵已註冊磁石，再右鍵開啟地圖 |
| 提高附魔力 | 在原版有效位置放雕紋書櫃並裝入書本／附魔書 |
| 整理介面 | 在容器畫面把游標移到要整理的一側，按滑鼠中鍵 |
| 設定 Discord | OP 執行 `/discordbridgeui` 或編輯 `config/discord-bridge.json` |

## 主要功能

- 四級可染色一般背包、Server 權威死亡背包、紅色定位光柱、永久保存與
  虛空保護。一般背包支援原版混色與裝水煉藥鍋洗色，死亡背包不可染色。
- 新取得的 Remnant 背包使用 `totem:remnant/*` canonical ID；既有
  `deadrecall:backpack_*`／`deadrecall:death_backpack` 仍可載入，並在玩家
  使用或替一般背包染色時保留全部 Components 轉換。未互動的舊物品不會被
  啟動掃描改寫。
- 新合成的銅扳手使用 `totem:automata/copper_wrench`，八個煉金物品使用
  `totem:alchemy/*`；對應 `deadrecall:*` ID 繼續可讀。扳手互動、煉金
  recipes 與 cauldron 流程會接受舊物品並安全轉成 canonical 結果。
- Remnant 在死亡時依主手、副手、快捷列與其餘物品欄的穩定順序自動掃描
  合格的 Nexus 傳送介面並保留一個，不再要求先成功傳送。
- 掛鎖可保護箱子、陷阱箱、木桶與固定漏斗相連的整個容器網路；他人仍可
  正常破壞，但成功破壞後會發布一次可由 Discord Bridge 傳送的稽核事件。
- 銅扳手管理銅魁儡分類、採集、燃料、工具、快取與選配 LLM 判斷。
- Totem Nexus 磁石 Space Unit、好友、地圖、成本報價與安全傳送
  session。
- 雕紋書櫃依書本內容提供最高 64 點附魔力。
- 豬糞、木灰、硝石、缽、熱可可與櫻花釀等資料驅動煉金內容。
- Minecraft 聊天、玩家動態、管理稽核、公開事件與伺服器狀態轉送
  Discord。
- Remnant 與 Nexus 透過 TotemCore event bus 發布跨模組事件，Discord
  Bridge 自行訂閱；整合包不再安裝功能專屬的反射 listener。
- 原版容器整理、講台配方、混凝土粉末硬化、漏斗熔爐經驗與書櫃
  生存規則。

## 管理員常用指令

| 指令 | 用途 |
| --- | --- |
| `/deadrecall containers scan [player]` | 唯讀掃描線上玩家的非法可攜式容器巢狀 |
| `/deadrecall deathnodes` | 開啟死亡 Space Unit 管理介面 |
| `/discordbridge reload` | 重新載入 Discord Bridge 設定 |
| `/discordbridge set <enabled> <url> <key>` | 設定 Worker URL 與 API Key |
| `/discordbridge channel add/remove/list ...` | 管理 Discord 目標頻道 |
| `/locksmith` | 檢查與管理目前鎖網路、成員、模式與鑰匙 |

Discord Bridge 參數見 [指令文件](docs/commands.md)；容器掃描與死亡節點的
安全細節分別見 [可攜式容器規則](docs/backpacks/container-safety.md) 與
[死亡節點管理](docs/nexus/death-node-admin.md)。

## 相容需求

| 項目 | 內容 |
| --- | --- |
| 版本 | 2.4.13 |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3+ |
| Fabric API | 0.154.2+26.2 |
| Java | 25 |
| 授權 | Apache-2.0 |

## 文件

完整文件入口：[docs/README.md](docs/README.md)

| 分類 | 文件 |
| --- | --- |
| 發佈 | [Release Notes](docs/releases/README.md) |
| 玩家 | [模組概覽](docs/overview.md) |
| 玩家 | [背包系統](docs/backpacks/README.md) |
| 玩家 | [死亡背包與靈魂綁定傳送物品](docs/backpacks/death-backpack.md) |
| 玩家 | [銅魁儡指南](docs/copper-golem/README.md) |
| 玩家 | [圖靈騰樞紐（Totem Nexus）／Space Unit](docs/nexus/README.md) |
| 玩家 | [附魔台與雕紋書櫃](docs/enchanting/README.md) |
| 玩家 | [煉金系統](docs/alchemy/README.md) |
| 管理員 | [Discord Bridge](docs/discord/README.md) |
| 開發者 | [開發者文件](docs/developer/README.md) |
| 規格 | [TOTEM 圖靈騰系統總覽與進度](openspec/README.md) |
| 規格 | [OpenSpec 索引](OPENSPEC_INDEX.md) |

## 建置

使用 Java 25：

```bash
./gradlew build
```

一般開發 JAR 輸出至 `build/libs/`。若要從十一個已驗證的獨立模組 JAR
建立 nested-JAR 整合檔：

```bash
./gradlew build -PbundleModuleDirectory=/path/to/standalone-modules
```

輸出為 `build/libs/deadrecall-2.4.18-bundled.jar`。輸入目錄必須是同一份
lockstep manifest 驗證過的十一個 JAR；建置會從每個 JAR 的
`fabric.mod.json` 自動寫入精確版本依賴，避免更新 bundle 時遺漏模組或
沿用舊版 pin。

## 文件分工

- `README.md`：專案首頁與快速導覽。
- `docs/`：目前可使用功能的玩家、管理員與開發者說明。
- `openspec/specs/`：已採用的系統規格與 invariant。
- `openspec/changes/`：設計中或尚未完成的變更。
