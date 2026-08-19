# DeadRecall

DeadRecall 是 Minecraft Fabric 26.2 的 Totem 系列相容整合主機。目前
發佈版本為 **2.4.21**；外層只負責舊 `deadrecall:*` 物品 ID 遷移與十一個
Totem 模組的 exact-version nested-JAR 組裝，玩法本身由各 Totem 子模組擁有。

## 安裝

1. 安裝 Java 25。
2. 使用 Minecraft 26.2、Fabric Loader 0.19.3+。
3. 安裝 Fabric API `0.154.2+26.2`。
4. 將 `deadrecall-2.4.21-bundled.jar` 放入 client 與 server 的 `mods/`。
5. 不要再放入下列十一個 Totem standalone JAR，否則 Fabric 會偵測到重複 mod ID。

## DeadRecall 2.4.21 exact module graph

| 模組 | 版本 | 功能 |
| --- | ---: | --- |
| TotemCore | `0.7.0` | 共用 API、Totem Manual、好友關係、事件、migration、版本握手 |
| TotemRemnant | `0.2.13` | 背包、死亡回收、容器安全 |
| TotemDiscordBridge | `0.1.8` | Minecraft ↔ Discord 事件與稽核 |
| TotemAutomata | `0.1.15` | 銅魔像分類、多種類採集、工具與燃料 |
| TotemAlchemy | `0.1.26` | 動態煉金、鍋釜混合藥液、釀造與素材發現 |
| TotemEnchanting | `0.1.8` | 雕紋書櫃內容式附魔力 |
| TotemExcavation | `0.1.5` | 區域挖掘與錘具 |
| TotemLocksmith | `0.1.4` | 相連容器鎖、GUI 權限、Core 好友與鑰匙 |
| TotemVanillaTweaks | `0.1.10` | 原版友善調整與容器整理 |
| TotemNexus | `0.3.0` | Space Unit、好友介面與安全傳送 |
| TotemVillagers | `0.1.32` | 村民工作、庫存、經濟與聚落 |

這是一組 lockstep release。不要只替換其中一個 nested module 為不同 standalone 版本。

## 主要玩法

- Core：整個 Totem 系列共用的好友／邀請資料與 API；舊世界仍沿用 `deadrecall:space_friends`，不需重新加好友。
- Remnant：四級可染色背包、死亡背包、定位、虛空保護與巢狀容器安全。
- Automata：使用銅扳手管理銅魔像分類、採集、工具、燃料與來源／目的地；採集背包可在總容量內同時保存不同物品種類。
- Nexus：註冊與探索 Space Unit、好友介面、地圖、成本與 server-authoritative 傳送；好友資料本身由 Core 擁有。
- Villagers：村民擁有實際庫存、食物、工具、交易存量、工作循環與聚落生產。
- Alchemy：鍋釜可混合藥水與未完成反應、保存中途裝瓶進度、對沖相反效果；Brewing Stand 仍保留標準快速釀造路線。
- Enchanting：有效路徑上的雕紋書櫃依實際書本內容提供最高 64 點附魔力，阻擋路徑時不貢獻力量。
- Excavation：七級錘具、server-owned 區域選擇、分 tick 挖掘與原版掉落／保護事件；成功完成後完整清除兩個選區角點。
- Locksmith：固定容器網路鎖、Access / Members / Keys GUI、實體鑰匙、Core 好友權限、automation 規則與破壞稽核。
- VanillaTweaks：容器整理、講台、混凝土粉末、熔爐經驗與書櫃相關生存調整。
- Discord Bridge：可選的聊天、玩家事件、公開事件、稽核與伺服器狀態轉送。

## 五分鐘快速上手

| 目標 | 操作 |
| --- | --- |
| 查看模組指南 | 使用 Totem Manual，依進度取得各模組章節 |
| 使用背包 | 手持背包右鍵；玩家物品欄也提供側邊存取 |
| 找回死亡物品 | 前往死亡背包定位光柱／Nexus death destination |
| 管理銅魔像 | 銅扳手右鍵銅魔像 |
| 設定區域挖掘 | 使用 Excavation 錘具選定兩個角點後挖掘 |
| 註冊 Space Unit | 羅盤右鍵未註冊磁石 |
| 開啟 Nexus | 羅盤左鍵已註冊磁石，再右鍵開啟介面 |
| 提高附魔力 | 在有效空氣間隔位置放置雕紋書櫃並裝入書／附魔書 |
| 鎖住倉庫 | 掛鎖右鍵支援的固定容器網路；空手 Shift + 右鍵開啟管理 GUI |
| 整理容器 | 游標移到要整理的一側後按滑鼠中鍵 |

## 相容需求

| 項目 | 內容 |
| --- | --- |
| 版本 | 2.4.21 |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3+ |
| Fabric API | 0.154.2+26.2 |
| Java | 25 |
| 授權 | Apache-2.0 |

## 文件

- [Release Notes](docs/releases/README.md)
- [模組概覽](docs/overview.md)
- [背包系統](docs/backpacks/README.md)
- [死亡背包](docs/backpacks/death-backpack.md)
- [銅魔像指南](docs/copper-golem/README.md)
- [Totem Nexus / Space Unit](docs/nexus/README.md)
- [附魔台與雕紋書櫃](docs/enchanting/README.md)
- [煉金系統](docs/alchemy/README.md)
- [Discord Bridge](docs/discord/README.md)
- [開發者文件](docs/developer/README.md)
- [OpenSpec](openspec/README.md)

## 建置

```bash
./gradlew build
```

從驗證過的十一個 standalone JAR 組裝 nested bundle：

```bash
./gradlew build -PbundleModuleDirectory=/path/to/standalone-modules
```

輸出為 `build/libs/deadrecall-2.4.21-bundled.jar`。`bundleJar` 會重新讀取每個 nested JAR 的
`fabric.mod.json` 並寫入 exact-version dependencies；Bundle Smoke 另外會從 immutable source pins
重建全部十一個模組、啟動 dedicated server、執行 Modrinth dry-run，成功後保留 verified bundle artifact。
