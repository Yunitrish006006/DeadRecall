# DeadRecall

DeadRecall 是 Minecraft Fabric 26.2 的 Totem 系列相容過渡主機。目前準備中的版本為 **2.4.22**。

2.4.22 的重點不是新增玩法，而是讓既有 DeadRecall 世界可以安全切換成只安裝需要的 Totem standalone modules。

## 2.4.22 migration safety

從 TotemCore **0.7.1** 開始，14 個歷史 `deadrecall:*` 物品 ID 的永久 registry ownership 已移到 Core。這表示：

- 離線玩家身上的舊物品仍能解碼；
- 未載入 chunk 裡箱子的舊物品仍能解碼；
- 不需要為了避免物品遺失而強制掃描整個世界；
- 對應 feature module 存在時，舊 stack 會在支援互動時以 `ItemStack.transmuteCopy` 轉成 `totem:*` canonical item，保留數量與完整 Data Component patch；
- 對應 feature module 暫時沒裝時，legacy placeholder 會原樣保留，不會被清空。

DeadRecall 2.4.22 本身不再註冊這些 legacy item IDs，只會驗證 Core 的 14 個 alias handoff 都存在。

## TotemVillagers

**TotemVillagers 不再是 DeadRecall 2.4.22 的依賴，也不會包含在 bundle 裡。**

全新世界、或確定不再需要 Villagers 系統的世界可以完全省略它。若既有 DeadRecall 2.4.21 世界曾使用 TotemVillagers，建議在 **第一次 2.4.22 啟動前** 同時安裝 standalone **TotemVillagers 0.1.32**，讓既有 Villagers SavedData 與運作狀態在 transition boot 中持續由相同模組讀取。只有在你確定要停用 Villagers 功能時才直接省略它。

## DeadRecall 2.4.22 transition graph

| 模組 | 版本 |
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

## 從 2.4.21 遷移

1. **先備份世界。**
2. 將 DeadRecall 2.4.21 換成 `deadrecall-2.4.22-bundled.jar`。
3. **若這個世界曾使用 TotemVillagers，第一次啟動前同時放入 standalone `totem-villagers-0.1.32.jar`。** 若你確定要停用 Villagers，才省略這一步。
4. 正常啟動伺服器一次。
5. 確認 log 出現 DeadRecall 已驗證 **14 TotemCore-owned legacy item aliases**。
6. 正常停止伺服器。
7. 接著可以移除 DeadRecall host，改成安裝 TotemCore 0.7.1+ 與你需要的 standalone Totem modules。
8. TotemVillagers 仍是獨立可選模組：要保留既有 Villagers 系統就繼續安裝；確定不需要時才移除。

這次啟動是 compatibility checkpoint，不是全世界 destructive rewrite。真正避免 hidden legacy stack 遺失的保證來自 TotemCore 0.7.1 永久保留 `deadrecall:*` alias。TotemVillagers 的 SavedData 屬於另一條功能資料邊界，因此是否保留 Villagers 應由伺服器管理者明確決定，而不是由 transition bundle 隱式替你決定。

## 相容需求

| 項目 | 內容 |
| --- | --- |
| DeadRecall | 2.4.22 |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3+ |
| Fabric API | 0.154.2+26.2 |
| Java | 25 |
| 授權 | Apache-2.0 |

## 建置

```bash
./gradlew build
```

從驗證過的 10 個 standalone JAR 組裝 transition bundle：

```bash
./gradlew build -PbundleModuleDirectory=/path/to/standalone-modules
```

輸出：

```text
deadrecall-2.4.22-bundled.jar
```

CI 會從 immutable source pins 重建 10 個模組、確認 TotemVillagers 不存在、啟動 dedicated server、驗證 14 個 Core-owned legacy aliases，並檢查 DeadRecall 外層已不再包含舊 alias Item implementation。

## 文件

- [2.4.22 Release Notes](docs/releases/2.4.22.md)
- [Release Notes Index](docs/releases/README.md)
- [OpenSpec](openspec/README.md)