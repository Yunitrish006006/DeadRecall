# DeadRecall

DeadRecall 是 Minecraft Fabric 26.2 的 Totem 系列相容整合包。單一 JAR
整合可升級背包與死亡保護、銅魁儡分類／採集、Totem Nexus 傳送、
雕紋書櫃附魔、生存煉金、Discord Bridge、容器整理與跨維度 `/back`。

目前開發候選版本為 **2.4.4**；上一個穩定版本為 **2.4.1**。2.4.4 將
八個獨立 Totem 模組以 Fabric nested JAR 封裝回單一安裝檔。詳細內容見
[2.4.4 變更清單](docs/releases/2.4.4.md)。

## 安裝

### 單一整合包（一般玩家／伺服器建議）

1. 安裝 Java 25。
2. 建立 Minecraft 26.2 的 Fabric Loader 0.19.3+ 遊戲實例。
3. 將 Fabric API `0.154.2+26.2` 放入 `mods/`。
4. 將 DeadRecall 2.4.4 的單一發佈 JAR 放入 `mods/`。
5. Client 與 Server 使用相同版本；啟動後確認 log 同時載入八個
   `totem-*` 模組。

> DeadRecall 2.4.4 已內含 TotemCore、Remnant、Automata、Nexus、
> Discord Bridge、Alchemy、Enchanting 與 Vanilla Tweaks。不要再把
> 這些模組的獨立 JAR 放入同一個 `mods/`，否則 Fabric 會偵測到重複
> mod ID。

### 獨立模組（模組包作者／開發者）

不使用 DeadRecall 整合 JAR 時，可以改裝精確相容的獨立模組：

| 模組 | 功能 | Repository |
| --- | --- | --- |
| TotemCore `0.2.0` | 所有功能模組共用的 API | [TotemCore](https://github.com/Yunitrish006006/TotemCore) |
| TotemRemnant `0.1.4` | 背包、死亡背包、容器安全 | [TotemRemnant](https://github.com/Yunitrish006006/TotemRemnant) |
| TotemAutomata `0.1.6` | 銅魁儡分類與採集 | [TotemAutomata](https://github.com/Yunitrish006006/TotemAutomata) |
| TotemNexus `0.2.0` | Space Unit、好友與傳送 | [TotemNexus](https://github.com/Yunitrish006006/TotemNexus) |
| TotemDiscordBridge `0.1.2` | Minecraft ↔ Discord | [TotemDiscordBridge](https://github.com/Yunitrish006006/TotemDiscordBridge) |
| TotemAlchemy `0.1.4` | 生存煉金與煉藥鍋 | [TotemAlchemy](https://github.com/Yunitrish006006/TotemAlchemy) |
| TotemEnchanting `0.1.1` | 雕紋書櫃附魔力 | [TotemEnchanting](https://github.com/Yunitrish006006/TotemEnchanting) |
| TotemVanillaTweaks `0.1.3` | 整理、配方與原版調整 | [TotemVanillaTweaks](https://github.com/Yunitrish006006/TotemVanillaTweaks) |

每個功能模組都要求 `totem-core =0.2.0` 與 Fabric API。不要混用其他
候選版本；完整精確組合記錄於
[lockstep manifest](openspec/changes/safe-multi-repo-modularization/lockstep-manifest.json)。

## 五分鐘快速上手

| 想做的事 | 操作 |
| --- | --- |
| 使用一般背包 | 右鍵背包開啟；依序升級為 9／18／27／36 格 |
| 找回死亡物品 | 前往紅色光柱處打開死亡背包，或使用一次性的 `/back` |
| 管理銅魁儡 | 合成銅扳手，右鍵銅魁儡選取並開啟 GUI |
| 設定分類 | 綁定來源銅箱、加入目的地、放入燃料後啟動 |
| 設定採集 | 設定 Home、兩個角點、採集目標、工具與燃料後啟動 |
| 註冊 Space Unit | 普通羅盤右鍵未註冊磁石 |
| 探索 Space Unit | 普通羅盤左鍵已註冊磁石，再右鍵開啟地圖 |
| 提高附魔力 | 在原版有效位置放雕紋書櫃並裝入書本／附魔書 |
| 整理介面 | 在容器畫面把游標移到要整理的一側，按滑鼠中鍵 |
| 設定 Discord | OP 執行 `/discordbridgeui` 或編輯 `config/discord-bridge.json` |

## 主要功能

- 四級一般背包、Server 權威死亡背包、紅色定位光柱、永久保存與
  虛空保護。
- `/back` 跨維度返回最近一次死亡位置，成功後清除紀錄。
- 銅扳手管理銅魁儡分類、採集、燃料、工具、快取與選配 LLM 判斷。
- Totem Nexus 磁石 Space Unit、好友、地圖、成本報價與安全傳送
  session。
- 雕紋書櫃依書本內容提供最高 64 點附魔力。
- 豬糞、木灰、硝石、缽、熱可可與櫻花釀等資料驅動煉金內容。
- Minecraft 聊天、玩家動態、管理稽核、公開事件與伺服器狀態轉送
  Discord。
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

Discord Bridge 參數見 [指令文件](docs/commands.md)；容器掃描與死亡節點的
安全細節分別見 [可攜式容器規則](docs/backpacks/container-safety.md) 與
[死亡節點管理](docs/nexus/death-node-admin.md)。

## 相容需求

| 項目 | 內容 |
| --- | --- |
| 版本 | 2.4.4（候選） |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3+ |
| Fabric API | 0.154.2+26.2 |
| Java | 25 |
| 授權 | BSD-3-Clause |

## 文件

完整文件入口：[docs/README.md](docs/README.md)

| 分類 | 文件 |
| --- | --- |
| 發佈 | [Release Notes](docs/releases/README.md) |
| 玩家 | [模組概覽](docs/overview.md) |
| 玩家 | [背包系統](docs/backpacks/README.md) |
| 玩家 | [死亡背包與 `/back`](docs/backpacks/death-backpack.md) |
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

一般開發 JAR 輸出至 `build/libs/`。若要從八個已驗證的獨立模組 JAR
建立 nested-JAR 整合檔：

```bash
./gradlew build -PbundleModuleDirectory=/path/to/standalone-modules
```

輸出為 `build/libs/deadrecall-2.4.4-bundled.jar`。輸入目錄必須是同一份
lockstep manifest 驗證過的八個 JAR。

## 文件分工

- `README.md`：專案首頁與快速導覽。
- `docs/`：目前可使用功能的玩家、管理員與開發者說明。
- `openspec/specs/`：已採用的系統規格與 invariant。
- `openspec/changes/`：設計中或尚未完成的變更。
