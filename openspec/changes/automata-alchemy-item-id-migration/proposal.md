# Proposal: Automata and Alchemy Item ID Migration

Status: released in DeadRecall 2.4.5.
Target Version: DeadRecall 2.4.5.

## 問題

TotemAutomata 與 TotemAlchemy 已是獨立功能 authority，但其自訂 Item 仍只
註冊在 `deadrecall:*`。Remnant 已建立 `totem:<module>/<path>` 的可逆遷移
模式，其餘實際擁有自訂 Item 的模組也應採用相同規則。

Core、Nexus、Discord Bridge、Enchanting 與 Vanilla Tweaks 沒有自訂 Item
註冊，不在本次建立虛構 mapping。

## 目標

- 銅扳手使用 `totem:automata/copper_wrench` canonical ID。
- 八個煉金物品使用 `totem:alchemy/<path>` canonical ID。
- 所有 `deadrecall:*` legacy ID 保持可解析與可使用。
- 新配方、煉藥鍋結果、掉落與創造分頁只產生 canonical Item。
- 轉換流程接受新舊輸入並保留完整 ItemStack Components。
- DeadRecall fallback 在未載入外部模組時繼續提供舊版行為。

## 非目標

- 不更改原版羅盤、回生羅盤、書或地圖 ID；它們不是 Nexus 自訂 Item。
- 不掃描離線玩家、未載入區塊或所有容器。
- 第一階段不移除 legacy Item 註冊。

## Mapping

| Legacy | Canonical |
| --- | --- |
| `deadrecall:copper_wrench` | `totem:automata/copper_wrench` |
| `deadrecall:saltpeter` | `totem:alchemy/saltpeter` |
| `deadrecall:pig_manure` | `totem:alchemy/pig_manure` |
| `deadrecall:wood_ash` | `totem:alchemy/wood_ash` |
| `deadrecall:cocoa_powder` | `totem:alchemy/cocoa_powder` |
| `deadrecall:hot_cocoa` | `totem:alchemy/hot_cocoa` |
| `deadrecall:cherry_brew` | `totem:alchemy/cherry_brew` |
| `deadrecall:stone_bowl` | `totem:alchemy/stone_bowl` |
| `deadrecall:sulfur_bowl` | `totem:alchemy/sulfur_bowl` |
