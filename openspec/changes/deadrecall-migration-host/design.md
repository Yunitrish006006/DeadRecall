# Design: Migration-Only Compatibility Host

## Ownership

- DeadRecall：14 個舊物品 ID、遷移 manifest、nested-JAR 組裝。
- TotemCore：程序內遷移表與完整 Data Component patch 轉換。
- Feature modules：canonical Item 與所有玩法行為。

DeadRecall 以 exact-version 模組的公開 canonical Item 常數建立遷移目標，
並驗證該物件的實際 registry ID。Core 直接保存 canonical Item 物件，
避免在 entrypoint 順序不同時進行第二次 registry ID 反查。

## Artifact Boundary

DeadRecall thin JAR 只允許：

- `Deadrecall` initializer。
- `migration/**` class。
- mod metadata、icon、語言與 `migration/item_ids.json`。

發佈 JAR 在 thin JAR 上加入八個 `META-INF/jars/*.jar`。任何其他 class、
Mixin metadata、玩法 data 或 diagnostics asset 都使建置失敗。

## Compatibility

舊堆疊第一次進入支援的互動路徑時，以 `ItemStack.transmuteCopy` 換成
canonical Item，因此 count 與完整 component patch 保留。未互動的舊物品
仍可由 alias 解碼，不執行全世界掃描。
