# Totem 模組分支審核（2026-08-17）

本次審核先更新所有 GitHub remote refs，再用下列條件決定是否合併：

1. 分支必須有共同祖先，不能把 `filtered-history` 當一般功能分支合併。
2. 變更必須符合 DeadRecall 僅作相容性 bundle、玩法由獨立 Totem 模組擁有的方向。
3. 不得新增第二套事件、Mixin、Payload、registry、配方或初始化註冊。
4. 已由 squash/merge 導入 default 的 patch-equivalent 分支不得重複合併。
5. 合併後必須能使用分支宣告的精確 Core 版本通過建置與測試。

## 已合併到本機 default，等待審查

| Repository | 來源分支 | 本機提交 | 審核結果 | 驗證 |
| --- | --- | --- | --- | --- |
| DeadRecall | `release/modrinth-header-review` | `e1ba6ad` 快轉，後續 `4e9f906` | 採用 migration-only host、11 模組 graph 與目前 Modrinth 整合方向 | thin-host guard、bundle build、獨立 Dedicated Server 啟動 |
| TotemCore | `agent/soulbound-lockstep-versions` | `edf61da` | 共用手冊、模組章節、版本握手方向一致；另修正雙手合併 | `check` 通過 |
| TotemAutomata | `agent/soulbound-lockstep-versions` | `c087199` | 導入 0.1.9 lockstep 與 Core 0.4.0 pin；未建立第二套 copper authority | 16/16 GameTests 與 JUnit 通過 |
| TotemDiscordBridge | `agent/soulbound-lockstep-versions` | `b1378dd` | 新增 Core event subscriber 與 advancement resource localization；subscriber 有一次性註冊保護 | JUnit 通過 |
| TotemEnchanting | `agent/soulbound-lockstep-versions` | `dbec99f` | 僅更新 0.1.3 / Core 0.4.0 lockstep，沒有第二套附魔實作 | 3/3 GameTests 通過 |
| TotemRemnant | `agent/soulbound-lockstep-versions` | `213cd7e` | 升級同一套背包 authority 到 0.2.11，加入共用手冊、升級模組、死亡保留與事件發布 | 45/45 GameTests 與 JUnit 通過 |

以上皆只存在本機，尚未 push。

## 已結束的重複分支

- `TotemAutomata/agent/automata-cutover-seams`
  - PR #1 已合併。
  - 分支的 8 個提交相對 default 全部為 patch-equivalent。
  - 已刪除 GitHub 遠端分支，沒有刪除唯一功能。

## 保留、不合併

### 抽取歷史

下列 refs 與目前 default 沒有共同 merge base，是從舊 DeadRecall 過濾出的歷史，不是可直接合併的功能分支：

- `TotemAutomata/origin/filtered-history`
- `TotemDiscordBridge/origin/filtered-history`
- `TotemRemnant/origin/filtered-history`
- `TotemNexus/origin/filtered-space-history`

它們保留作來源追溯；強行合併會把相同玩法用另一條歷史再次帶入。

### DeadRecall 舊 lockstep 分支

- `DeadRecall/origin/agent/soulbound-lockstep-versions` 對應開啟中的 PR #70。
- 它與已採用的 release 分支共用 migration-host 基礎，但尾端保留較舊版本 pin，並修改已由獨立 DiscordBridge 擁有的 legacy Discord source。
- 這些變更不應再併回 migration-only host；需要的在地化與 event subscriber 已由 TotemDiscordBridge 分支承接。

### DeadRecall 其他殘留 refs

- 可見的舊 feature/test/CI refs 都對應已合併 PR、已關閉草稿或被後續 PR 取代。
- 因 GitHub 常以 squash merge 導入，單看 `git branch --no-merged` 仍會把它們列出；本次沒有重複合併。
- 本次只刪除具完整 patch-equivalent 證據的 Automata 分支，其餘保留供最後遠端清理審查。

## 沒有未合併功能分支

- TotemAlchemy
- TotemExcavation
- TotemLocksmith
- TotemVanillaTweaks
- TotemVillagers

## 來源與發佈版本落差

目前 2.4.15 bundle 中多個已發佈 JAR 的版本高於 GitHub default 可重建的版本：

| Module | 本機審核後 source | Bundle artifact |
| --- | ---: | ---: |
| TotemAlchemy | 0.1.7 | 0.1.23 |
| TotemAutomata | 0.1.9 | 0.1.12 |
| TotemDiscordBridge | 0.1.4 | 0.1.6 |
| TotemEnchanting | 0.1.3 | 0.1.5 |
| TotemExcavation | 0.1.0 | 0.1.2 |
| TotemNexus | 0.2.3 | 0.2.6 |
| TotemVanillaTweaks | 0.1.6 | 0.1.8 |

這些 artifact 與 DeadRecall 的 Modrinth staging manifest SHA-512 相符，但目前沒有相同版本的可合併 Git 分支。它們可以安全載入本次 bundle，卻還不能從 GitHub default 完整重建；後續應把對應 source 提交補回各模組，而不是從 `filtered-history` 強行合併或從 class 檔推測來源。

已對齊的模組為 TotemCore 0.6.0、TotemRemnant 0.2.11、TotemLocksmith 0.1.0、TotemVillagers 0.1.26。
