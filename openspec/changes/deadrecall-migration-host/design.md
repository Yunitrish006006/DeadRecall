# Design: Final Compatibility Transition

## Ownership

- **TotemCore 0.7.1+**：永久擁有 14 個保留的 `deadrecall:*` 物品 alias、decode-safe placeholder 與 lazy canonical migration。
- **DeadRecall 2.4.22**：只保留 migration manifest、handoff 驗證與 exact nested-JAR 過渡組裝；不再註冊舊物品 ID。
- **Feature modules**：只擁有 `totem:*` canonical Item 與所有玩法行為。

Core 在 canonical feature module 尚未載入時先保留 legacy ID 與 canonical ID 的 deferred mapping。舊 stack 因此能安全解碼；當對應 canonical Item 存在後，第一次支援的互動會以 `ItemStack.transmuteCopy` 轉換，保留 count 與完整 Data Component patch。

這個設計刻意不要求掃描離線玩家或未載入 chunk。即使世界中仍存在多年未被載入的 legacy stack，移除 DeadRecall 後仍由 TotemCore 保留其 registry ID，不會因 missing item ID 遺失。

## Transition Bundle

DeadRecall 2.4.22 包含 10 個 exact-version module：

- TotemCore 0.7.1
- TotemRemnant 0.2.13
- TotemDiscordBridge 0.1.8
- TotemAutomata 0.1.15
- TotemAlchemy 0.1.26
- TotemEnchanting 0.1.8
- TotemExcavation 0.1.5
- TotemLocksmith 0.1.4
- TotemVanillaTweaks 0.1.10
- TotemNexus 0.3.0

TotemVillagers 不再被 DeadRecall 硬依賴，也不包含在 2.4.22 transition bundle；需要時獨立安裝。對既有 2.4.21 世界而言，Villagers 的 SavedData 與 runtime ownership 是獨立於 legacy item alias 的資料邊界：若世界曾使用 Villagers，第一次 2.4.22 transition boot 前應同時安裝 standalone TotemVillagers 0.1.32；只有伺服器管理者明確決定停用該功能時才直接省略。

## Artifact Boundary

DeadRecall thin JAR 只允許：

- `Deadrecall` initializer；
- `migration/**` handoff verifier；
- mod metadata、icon 與 `migration/item_ids.json`。

DeadRecall 不得再包含 legacy placeholder Item implementation。CI 必須驗證 `DeadRecallLegacyItems` / `LegacyMigratingItem` 不存在，且 nested TotemCore 包含新的 Core-owned alias bootstrap。

## Recommended Cutover

1. 備份世界。
2. 從 DeadRecall 2.4.21 升到 2.4.22。
3. 若世界曾使用 TotemVillagers，在第一次 2.4.22 啟動前同時安裝 standalone TotemVillagers 0.1.32；若明確要停用 Villagers 才省略。
4. 正常啟動並停止一次。
5. 啟動 log 必須確認 14 個 Core-owned aliases 全部通過 handoff verification。
6. 之後可以移除 DeadRecall host，只留下 TotemCore 0.7.1+ 與需要的 standalone Totem modules。
7. TotemVillagers 維持獨立可選；需要保留既有 Villagers 系統時繼續安裝，明確停用時才移除。

這次啟動是 compatibility checkpoint，不是強制全世界 destructive rewrite；item safety 來自 alias authority 已永久轉移到 Core，而 Villagers 是否保留則是獨立且必須明確做出的 feature-retirement 決策。