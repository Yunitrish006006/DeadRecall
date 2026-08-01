# TOTEM 圖靈騰系統 OpenSpec

TOTEM 圖靈騰系統（以下簡稱圖靈騰；技術品牌為 `Totem`）是由 DeadRecall 演進而來的模組化 Minecraft Fabric 生態系。目標不是維持單一大型模組，而是將現有功能拆成可獨立安裝的模組，並由圖靈騰核心提供共用基礎能力。

## 模組總覽與進度

此表是圖靈騰系列名稱、定位與開發進度的唯一總覽入口；功能細節與驗證證據仍以 [Roadmap](roadmap.md) 與各 OpenSpec change 為準。

| 中文顯示名稱 | 技術名稱 | 定位 | 目前進度 |
|---|---|---|---|
| 圖靈騰核心 | `Totem Core` | 共用 Library、API、網路、資料、設定與 GUI 基礎 | 規劃中；獨立 module、穩定公開 API 與 migration framework 尚待完成。 |
| 圖靈騰樞紐 | `Totem Nexus` | 磁石、Space Unit、傳送、好友、人體磁石與分散重生 | 主要功能與自動化驗證已完成；正進行獨立 module 資格驗證、compatibility-bundle cutover，以及多人真人 UI 驗收。 |
| 圖靈騰殘響 | `Totem Remnant` | 死亡背包、物品回收、死亡紀錄、死亡殘響與離線玩家身體 | 死亡背包核心與第一階段物品 ID 相容遷移已完成；離線玩家身體尚待進行。 |
| 圖靈騰機巧 | `Totem Automata` | 銅傀儡、分類、採集與自動化工作模式 | 核心功能與自動化驗證已完成；正進行獨立 module 資格驗證與 compatibility-bundle cutover。 |
| 圖靈騰掘進 | `Totem Excavation` | 區域採掘錘與未來工程工具 | 待從 Blossom 移植。 |
| 圖靈騰智識 | `Totem Cognition` | Agent Framework、自然語言、規劃、工具呼叫與 Provider | 研究階段；設計為可選模組。 |

`DeadRecall` 目前是既有世界與鎖定版本模組的 compatibility bundle，並非圖靈騰系列中的功能模組。

## 核心依賴規則

```text
圖靈騰核心（Totem Core）
├── 圖靈騰樞紐（Totem Nexus）
├── 圖靈騰殘響（Totem Remnant）
├── 圖靈騰機巧（Totem Automata）
├── 圖靈騰掘進（Totem Excavation）
└── 圖靈騰智識（Totem Cognition）
```

- 圖靈騰核心（`Totem Core`）不得依賴其他圖靈騰模組。
- 功能模組預設只依賴圖靈騰核心（`Totem Core`）。
- 模組間整合使用公開 API、事件或可選整合層。
- 不允許循環依賴。
- 圖靈騰機巧（`Totem Automata`）必須在未安裝圖靈騰智識（`Totem Cognition`）時仍可完整運作。

## 文件

- [`architecture.md`](architecture.md)：平台架構與強制開發規範。
- [`roadmap.md`](roadmap.md)：已完成、進行中、待排程及未完成項目。
- [`specs/space-unit-lodestone/spec.md`](specs/space-unit-lodestone/spec.md)：圖靈騰樞紐（Totem Nexus）的磁石傳送與分散重生規格。
- [`specs/copper-golem/spec.md`](specs/copper-golem/spec.md)：現有銅傀儡目標規格。
- [`specs/offline-player-body/spec.md`](specs/offline-player-body/spec.md)：圖靈騰殘響（Totem Remnant）的玩家下線後保留身體、重連、死亡與防複製規格。
- [`specs/discord-bridge/spec.md`](specs/discord-bridge/spec.md)：Discord Bridge 的事件轉播、Worker 路由與安全規格。
- [`specs/gameplay-recipes/spec.md`](specs/gameplay-recipes/spec.md)：DeadRecall 覆寫或新增的資料層配方規格。
- [`changes/direct-friend-player-teleport/`](changes/direct-friend-player-teleport/)：雙向好友直接傳送，不再逐次確認。
- [`changes/amethyst-catalyst-teleport-discount/`](changes/amethyst-catalyst-teleport-discount/)：傳送石碑紫水晶催化方塊降低跨維度成本。
- [`changes/teleport-interface-item-specializations/`](changes/teleport-interface-item-specializations/)：普通羅盤、回生羅盤、書本與已繪製地圖的傳送介面與特化規格。
- [`changes/lectern-recipe-override/`](changes/lectern-recipe-override/)：以木半磚與書覆寫講台配方。
- [`changes/concrete-powder-item-hardening/`](changes/concrete-powder-item-hardening/)：混凝土粉末掉落物水中硬化功能。
- [`changes/backpack-dyeing/`](changes/backpack-dyeing/)：一般背包原版染色、混色與煉藥鍋洗色。
- [`changes/soulbound-teleport-interface/`](changes/soulbound-teleport-interface/)：上次成功傳送介面的死亡保留與 `/back` 移除。
- [`changes/remnant-item-id-migration/`](changes/remnant-item-id-migration/)：Remnant canonical item ID、legacy 雙註冊與 lazy migration。
- [`changes/automata-alchemy-item-id-migration/`](changes/automata-alchemy-item-id-migration/)：Automata／Alchemy canonical item ID、legacy 雙註冊與 recipe bridge。
- [`changes/safe-multi-repo-modularization/`](changes/safe-multi-repo-modularization/)：一次一個功能 repository、DeadRecall compatibility bundle、識別碼基線與可回滾拆分流程。

## 名稱與相容性

中文顯示名稱使用「圖靈騰」系列；`Totem` 與各模組英文名稱保留作為技術品牌與識別。重新命名不採一次性全域替換；每一批儲存資料識別碼都必須有明確 mapping、legacy 讀取入口、可回滾設計與自動化 migration 驗證，避免玩家既有世界資料消失。

實體拆分採多 repository 架構；`DeadRecall` 在觀察期內保留為鎖定精確模組版本的 compatibility bundle。新 repository 通過獨立安裝、bundle、舊世界、restart 與 Dedicated Server 驗證前，不得刪除原實作。
