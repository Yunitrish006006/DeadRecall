# TOTEM 圖靈騰系統：拆分作業交接備忘錄

更新日期：2026-07-29

## 目前結論

DeadRecall 端的相容整合包拆分已經完成主要程式與資源切換；尚未能宣告整體拆分完成，原因是部分獨立模組的正式 cutover 條件要求跨兩個發行版本的觀察紀錄。這是發行／觀察工作，不是目前發現的程式碼缺口。

已完成的安全檢查：

```bash
bash .github/scripts/check-lockstep-manifest.sh
bash .github/scripts/check-modularization-baseline.sh
bash .github/scripts/check-feature-dependency-boundaries.sh
git diff --check
```

以上皆通過。lockstep manifest 也已固定目前與回滾用的外部模組 artifact／commit。

## 未結項目

- `openspec/changes/safe-multi-repo-modularization/tasks.md` 的 Automata（5.4）、Nexus（6.4）以及後續 Alchemy／Enchanting／Vanilla Tweaks 的觀察窗口尚未結束。
- 觀察完成前，不要勾選父項或宣告拆分完成。
- 各模組的正式程式庫位於獨立 repository；本工作區的 DeadRecall 是相容整合包，不能在此直接完成那些 repository 的發行與觀察。
- 進行真正發布前，需先決定版本號、發行範圍與 Modrinth／GitHub Release 操作權限。

## 本次已完成的顯示調整（尚未提交）

- 將文件中的 TOTEM 系列統一為中文品牌與英文技術名稱：
  - TOTEM 圖靈騰系統
  - 圖靈騰核心（Totem Core）
  - 圖靈騰樞紐（Totem Nexus）
  - 圖靈騰殘響（Totem Remnant）
  - 圖靈騰機巧（Totem Automata）
  - 圖靈騰掘進（Totem Excavation）
  - 圖靈騰智識（Totem Cognition）
- 根進度頁的顯示名稱改為 `TOTEM 圖靈騰系統`（繁中）、`TOTEM 图灵腾系统`（簡中）、`TOTEM System`（英文）。
- 根進度 ID 仍為 `deadrecall:root`，以保護既有世界的 Advancement 進度；待 Totem Core 成為相容整合包的正式依賴後，才可讓不同模組以 `totem-core:root` 作共同父節點，顯示在同一個遊戲分頁。

## 建置／GameTest 狀態

本機原本只有 Java 17，而 Fabric Loom 1.17.12 需要 Java 21 以上。已在暫存區取得 Java 25：

```text
/tmp/temurin-25
```

完整建置曾執行至 113 個 GameTest，編譯與資源處理皆成功；其中一項逾時：

```text
deadrecall-gametest:copper_golem_gathering_lifecycle_game_test_home_removal_during_return_preserves_storage_and_blocks
```

錯誤是 500 ticks 內未完成；日誌同時顯示嚴重的伺服器落後與大量平行測試，因此尚未確定是實際回歸或整批測試負載造成的問題。

曾嘗試以下篩選指令，但 `-Dfabric-api.gametest.filter` 只傳給 Gradle，沒有傳入啟動的遊戲 JVM，因此仍執行 113 個測試：

```bash
./gradlew -Dorg.gradle.java.home=/tmp/temurin-25 \
  -Dfabric-api.gametest.filter=deadrecall-gametest:copper_golem_gathering_lifecycle_game_test_home_removal_during_return_preserves_storage_and_blocks \
  runGameTest --no-daemon
```

已在 `build.gradle` 增加可選的 `-PgameTestFilter`：只有提供這個 Gradle property 時，才會把 `fabric-api.gametest.filter` 放進 Loom `gameTest` 的遊戲 JVM；完整套件的預設行為不變。Loom 1.17.12 的 `RunConfigSettings.property(...)` 已核對可用，且下列 `--dry-run` 成功載入所有 GameTest task。

```bash
./gradlew -Dorg.gradle.java.home=/tmp/temurin-25 \
  -PgameTestFilter=deadrecall-gametest:copper_golem_gathering_lifecycle_game_test_home_removal_during_return_preserves_storage_and_blocks \
  runGameTest --no-daemon
```

本次啟動精準重跑時已進入 Fabric GameTest Server，且產生新的 test world 資料；但主機程序在輸出最終 GameTest 結果前結束，沒有產出通過／失敗報告或 Java crash log。因此仍沒有可信的單一案例結論，不能據此放寬 `maxTicks`。下次應先用上述同一指令取得單一測試結果，再決定是否需要修正測試或程式碼。

## 工作區注意事項

- 此次所有未提交變更皆為中文命名／文件整理與 Advancement 顯示文字，開始前工作區是乾淨的。
- 沒有背景 Gradle 或 GameTest 程序仍在執行。
- 已使用 `git diff --check` 驗證目前差異沒有空白格式問題。
