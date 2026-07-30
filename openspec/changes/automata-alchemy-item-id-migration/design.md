# Design: Automata and Alchemy Item ID Migration

## 共用原則

各模組自行註冊 canonical 與 legacy Item，並保留 legacy item definition 與
翻譯。公開常數改指 canonical Item，另以 `LEGACY_*` 常數提供資料讀取與測試。
canonical Item 使用 `assets/totem/items/<module>/...` definition，現階段可
重用既有 DeadRecall texture/model。

所有 helper 使用 ItemStack `transmuteCopy`，保留完整 component patch。
canonical 或非 legacy stack 原樣返回。

## Automata

`CopperWrenchSelection` 同時辨識兩組 ID。Server 在銅扳手 attack-block、
use-block 或 use-entity authority 入口先轉換玩家手持 stack，再讀寫選取的
銅傀儡 UUID，因此 legacy custom data 不會遺失。新 crafting result、
advancement icon 與 creative entry 使用 canonical Item。

## Alchemy

Alchemy 的 legacy Item 保留原本 class 與 consumable properties，因此未
轉換的舊食物、飲品、豬糞與缽仍可使用。新取得路徑使用 canonical 常數：

- stone bowl crafting、wood ash smelting 與 cauldron results 產生 canonical。
- custom cocoa/flint recipes 同時接受 legacy/canonical stone bowl。
- cauldron ingredient arrays 同時列出新舊 cocoa powder、wood ash 與 pig
  manure，remainder/result 使用 canonical。
- gunpowder recipe 以雙 ID item tag 接受 sulfur bowl 與 saltpeter。
- legacy sulfur bowl 的 craft remainder 是 canonical stone bowl。
- pig-manure projectile detection 以 item family helper 同時接受兩組 ID。

legacy hot cocoa 與 cherry brew 在被飲用後本來就轉為原版玻璃瓶，無須在
玩家 inventory 上做 eager rewrite。

## DeadRecall fallback

外部 Automata/Alchemy authority 存在時，DeadRecall 不註冊 fallback Item 或
對應 mixin。外部模組不存在時，fallback 仍保留 `deadrecall:*` graph。bundle
驗證必須確認外部雙註冊不與 fallback 重複。

## Rollback

legacy ID 不刪除，因此舊世界可回滾到 legacy graph。正式發布 canonical
stack 後，後續版本不得只移除 canonical 註冊；必須持續雙註冊或提供明確
反向轉換。
