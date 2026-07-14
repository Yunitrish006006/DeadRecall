# Data Components 與背包資料

DeadRecall 背包內容使用原版 `DataComponents.CONTAINER` 與 `ItemContainerContents` 儲存。

## 必須維持的條件

- 開啟 GUI 時固定追蹤實際被開啟的背包 `ItemStack`。
- 關閉 GUI 或同步資料時，不可只依賴玩家目前主手。
- 寫回前確認追蹤的 Stack 仍是預期背包，且仍存在於玩家物品欄或合法容器位置。
- 開啟期間必須鎖住該 ItemStack 對應的玩家物品欄 slot，阻止 Shift-click、拿取、丟棄、快捷列交換與副手交換。
- 鎖定判斷使用 ItemStack 物件參考，不能只比較 Item 或 Components，否則可能誤鎖另一個內容相同的背包。
- 其他物品的原版拖曳與 Shift-click 行為應保持不變。
- ItemStack 判斷應同時考慮 Item、Components 與必要的唯一識別資料。
- 死亡背包清空時，應等容器操作完成及 GUI 關閉後再移除背包本體。

## 常見錯誤

若使用 `player.getItemInHand(hand)` 作為關閉介面時的唯一寫回目標，玩家在 GUI 開啟期間換手，就可能把背包內容寫入銅鏟或其他物品。這類錯誤通常表現為物品圖示或 Components 融合，但物品原本功能仍部分存在。

只依賴 `Container.canPlaceItem(...)` 禁止背包套娃也不夠。原版 `ChestMenu` 仍允許玩家操作背包本體所在的快捷列 slot；背包本體一旦在 GUI 開啟期間被移動，追蹤參考與 Menu validity 就可能失效。

## 修改檢查

調整背包資料格式或 Menu 行為時，至少測試：

1. 背包位於目前選取的快捷列，開啟後 Shift+左鍵點擊背包本體。
2. 開啟後一般左鍵、右鍵或 Q 鍵操作背包本體。
3. 對其他 slot 使用 1–9 鍵，嘗試與背包所在快捷列格交換。
4. 背包在副手時，開啟後使用副手交換鍵。
5. 其他物品由玩家物品欄 Shift+點擊進入背包。
6. 背包內物品 Shift+點擊回玩家物品欄。
7. 開啟背包後換快捷列位置再關閉。
8. 開啟後交換主手與副手。
9. 清空死亡背包最後一格。
10. 背包掉落、拾取、死亡與重新登入後的資料一致性。