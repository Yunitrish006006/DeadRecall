# Design: Remnant Item ID Migration

## 雙註冊

TotemRemnant 同時註冊 canonical 與 legacy Item。公開的
`BACKPACK_*`／`DEATH_BACKPACK` 常數指向 canonical Item；額外的
`LEGACY_*` 常數只用於解析舊資料與轉換測試。兩組 ID 都保留對應 item
definition、model、翻譯、tag 與行為。

DeadRecall fallback 仍保留舊註冊。整合包載入 TotemRemnant 時，DeadRecall
不啟動 fallback authority，因此不會重複註冊。

## Lazy migration

不做 eager world scan。玩家在主手或副手使用 legacy 背包時，Remnant 透過
ItemStack `transmuteCopy` 建立 canonical stack，先替換手中物品，再建立
inventory/menu。此順序確保 inventory 的 identity 與有效性檢查綁定到新
stack。

`transmuteCopy` 保留 component patch，因此容器內容、自訂名稱、染色、addon
custom data 與死亡節點綁定都不需逐欄重建。canonical 或非 legacy stack
原樣返回，避免無意義複製。

## 資料取得路徑

- 原有 recipe ID 暫時保持 `deadrecall:*`，避免不必要地重設 advancement
  recipe unlock；結果一律改為 canonical Item。
- tier upgrade base 改用 `totem:remnant/backpacks/<tier>` tag，每個 tag
  同時包含 canonical 與 legacy ID。
- canonical 一般背包有獨立的 `crafting_dye` recipe。
- legacy 染色 recipe 的 target 保持 legacy，但 result 改為 canonical；
  原版 dye recipe 會複製原 stack Components。
- 新死亡背包 factory 與創造分頁只引用 canonical 常數。

## 顯示與 tag

canonical asset 路徑使用 `assets/totem/.../remnant/...`，暫時重用既有
DeadRecall 背包 texture。legacy asset 不刪除。portable-container tag 與
一般背包 cauldron tag 同時列出新舊 ID；advancement criteria 透過雙 ID tier
tag 接受兩者，但 icon 使用 canonical ID。

## Rollback

回滾新取得路徑時，legacy ID 與資料仍存在，因此不會使已載入舊物品失效。
已轉成 canonical 的 stack 需要保留本變更的 canonical 註冊才能解析；在正式
發布後不得只刪除 canonical 註冊來回滾，必須提供反向資料轉換或保留雙註冊。
