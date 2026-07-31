# Proposal: Soulbound Teleport Interface

Status: implemented in the 2.4.6 lockstep candidate; awaiting release.
Target: DeadRecall 2.4.6 / TotemCore 0.3.0 / TotemNexus 0.2.1 /
TotemRemnant 0.1.6.

## 問題

DeadRecall 的 `/back` 是獨立於 Nexus 傳送規則的免費指令，也讓 compatibility
bundle 繼續持有玩家玩法與暫存死亡座標。單純要求玩家死亡時攜帶回生羅盤並不
合理，且任何「可作為傳送介面」的書本或地圖都免掉落會造成濫用。

## 目標

- 上一次成功完成 Nexus 傳送時實際使用的介面物品成為唯一靈魂綁定物品。
- 死亡時只保留該物品的一個，不把它放入死亡背包或原版掉落。
- 堆疊中的其他物品照常由 Remnant 捕獲。
- 保留物品跨重生持久化並 exactly once 還原。
- 消失詛咒優先，不保留帶有該效果的物品。
- 移除 DeadRecall `/back` 與自訂死亡座標管理。

## 非目標

- 不讓所有羅盤、書本或地圖自動免除死亡掉落。
- 不繞過 Nexus 的報價、成本、準備時間、權限或安全落點。
- 不讓 Remnant 直接依賴 Nexus implementation classes。
- 不新增可交易的靈魂綁定物品或配方。
