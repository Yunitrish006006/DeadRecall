# Design: Backpack Dyeing

## 原版資料路徑

每個 canonical 一般背包提供一份 `minecraft:crafting_dye` 配方，target 與
result 都是相同的 `totem:remnant/backpack_*` ID。legacy
`deadrecall:backpack_*` 另有 bridge recipe：target 保持 legacy，result
改為同 tier canonical ID。原版 `DyeRecipe` 會先從 target 複製完整 ItemStack
Components，再依既有 `minecraft:dyed_color` 與本次染料計算混色結果。

不得用一般 shaped/shapeless recipe 取代此流程，因為固定輸出 ItemStack
無法自然保留背包內容與 addon Components。

## 洗色

新舊共八個一般背包 ID 加入 `minecraft:cauldron_can_remove_dye` item tag。原版
裝水煉藥鍋互動只移除 `minecraft:dyed_color` 並降低一層水位。死亡背包不在
此 tag。

## 顯示

四個 item definition 的模型加入 `minecraft:dye` tint source。未染色時
使用白色預設 tint，保留現有棕色材質；染色後由引擎把 dye tint 套到 layer 0。
item definition 同時服務 inventory、手持與掉落物呈現，不新增 client-only
染色程式。

## 模組邊界

TotemRemnant 是配方、tag 與模型資源的 authority。DeadRecall 整合包透過
nested Remnant JAR 載入同一份資源；legacy ID 保留作為相容入口，新取得
路徑只產生 canonical ID，避免形成第二份互相漂移的染色實作。

## 驗證

- Remnant GameTest 從 Server RecipeManager 取得真實資料包配方並實際 assemble。
- 測試至少涵蓋四個 tier、兩種染料混色、既有染色再次混色、內容與自訂名稱
  保存，以及死亡背包拒絕。
- GameTest 以 mock ServerPlayer 實際對裝水煉藥鍋互動，驗證只移除顏色、
  保留 Components 並降低水位。
- DeadRecall bundle 以實際 nested-JAR Dedicated Server smoke test 再驗證
  Remnant 資源在整合環境可載入。
