# Proposal: Backpack Dyeing

Status: implementation.
Target Version: DeadRecall 2.4.5 / TotemRemnant 0.1.5

## 問題

一般背包目前共用固定棕色外觀，玩家無法用原版染料區分用途。自行實作染色
若沒有完整複製 ItemStack Data Components，可能在合成時遺失背包內容、自訂
名稱或其他相容資料。

## 目標

- 四級一般背包可在工作台與一個以上原版染料合成。
- 重複染色採用原版皮革物品混色規則。
- 染色時保留背包內容、自訂名稱與所有既有 Data Components。
- 染色背包可用裝水煉藥鍋洗回未染色狀態。
- 物品欄、手持與掉落物模型都依 `minecraft:dyed_color` 顯示顏色。
- 死亡背包維持專用外觀且不可染色。

## 非目標

- 不新增自訂染料、染色 GUI 或逐區域圖案。
- 不改變任何背包容量、防護、巢狀限制或死亡擷取規則。
- 不讓死亡背包加入一般背包染色流程。

## 相容性

染色資料使用原版 `minecraft:dyed_color` Data Component。未染色舊背包不需
資料遷移；染色與洗色只改變該 Component，其他資料必須逐項保持。
