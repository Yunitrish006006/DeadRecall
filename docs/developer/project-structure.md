# 專案結構

DeadRecall 現在是 Totem 生態的 **thin compatibility host**，不再持有各玩法模組的實作。外層 JAR 只負責舊 ID 遷移與 exact-version nested-JAR 整合。

## DeadRecall 主程式

| 路徑 | 責任 |
| --- | --- |
| `src/main/java/com/adaptor/deadrecall/Deadrecall.java` | DeadRecall 外層入口與相容性初始化 |
| `src/main/java/com/adaptor/deadrecall/migration/` | 舊 `deadrecall:*` 物品 ID 的受控遷移 |
| `src/main/resources/data/deadrecall/migration/item_ids.json` | legacy → canonical 物品 ID 對照 |
| `src/main/resources/fabric.mod.json` | **唯一現行 Fabric metadata**；宣告 DeadRecall 與十一個 Totem 模組的 exact dependencies |
| `src/main/resources/assets/deadrecall/icon.png` | DeadRecall 外層圖示 |

`build.gradle` 的 main source set 只允許上述 Java / resource surface 進入 thin host。`verifyDeadRecallThinJar` 會拒絕其他 gameplay、Mixin、GUI、payload 或額外資料資源意外回流到 DeadRecall 外層。

## Gameplay 所屬模組

| Repository | 主要責任 |
| --- | --- |
| `TotemCore` | 共用 API、事件、手冊、版本握手、遷移契約 |
| `TotemRemnant` | 背包、死亡背包、死亡物品回收、容器安全 |
| `TotemAutomata` | 銅魁儡分類與採集自動化 |
| `TotemNexus` | Space Unit、好友、地圖與傳送 |
| `TotemDiscordBridge` | Minecraft ↔ Discord 事件傳輸 |
| `TotemAlchemy` | 煉金、動態釀造與研究紀錄 |
| `TotemEnchanting` | 雕紋書櫃附魔力 |
| `TotemExcavation` | 區域挖掘與錘具 |
| `TotemLocksmith` | 固定容器網路鎖、權限與鑰匙 |
| `TotemVanillaTweaks` | 原版相容調整、整理與配方 |
| `TotemVillagers` | 村民庫存、工作、經濟與村莊 worldgen |

新增或修正 gameplay 時應修改對應 standalone repository，不應在 DeadRecall 重新建立第二份實作。

## Bundle 與發佈入口

- `build.gradle`：thin host build、nested-JAR bundle 組裝與 exact module filenames。
- `gradle.properties`：DeadRecall 當前版本與 Minecraft/Fabric build properties。
- `docs/releases/`：目前 release graph 與玩家可見變更。
- `.github/workflows/build.yml`：Core pin、thin-host build/tests、Modrinth dry-run。
- `.github/workflows/validate.yml`：當前十一模組 exact dependency graph 驗證。
- `.github/scripts/publish-modrinth.sh`：DeadRecall 發布 artifact 驗證與不可覆寫版本政策。
- `.github/staging/modrinth-standalone/`：歷史 standalone staging 資料；其中 2.4.13 manifest/bundle contract 是 archived evidence，不代表目前 bundle 版本。

## 重要不變條件

1. Fabric metadata 只以 `src/main/resources/fabric.mod.json` 為準。
2. DeadRecall 外層不得重新加入 gameplay Mixin、GUI、payload 或 feature-owned data。
3. 每個 nested module 都使用 exact version pin；不同版本不得混裝。
4. 已發布的版本號不得對應到不同 JAR SHA-512；內容變更必須升版。
