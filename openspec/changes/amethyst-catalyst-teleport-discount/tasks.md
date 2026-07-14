# Tasks: Amethyst Catalyst Teleport Discount

## 1. Data and scanning

- [ ] 1.1 新增 `space_unit_amethyst_catalysts` block tag。
- [ ] 1.2 `SpaceStructureSnapshot` 加入 optional `amethystCatalystBlocks`。
- [ ] 1.3 石碑掃描器計算有效催化方塊。
- [ ] 1.4 驗證舊 SavedData 讀取預設為 0。

## 2. Quote and payment

- [ ] 2.1 將原始紫水晶成本與最終成本分離。
- [ ] 2.2 實作每 4 個催化方塊折抵 1、最低成本 1。
- [ ] 2.3 只讓固定磁石端點提供折抵。
- [ ] 2.4 開始與完成傳送前重新掃描並重算。
- [ ] 2.5 扣款只使用最終成本。

## 3. Networking and UI

- [ ] 3.1 Payload 加入 base cost、catalyst count 與 discount。
- [ ] 3.2 地圖資訊面板顯示「原始成本 - 石碑折抵 = 最終成本」。
- [ ] 3.3 Client／Server codec 同步並加上長度與範圍限制。

## 4. Tests

- [ ] 4.1 0、3、4、8、12 個催化方塊成本矩陣。
- [ ] 4.2 玩家來源、玩家目標與死亡節點不錯誤提供折抵。
- [ ] 4.3 報價後拆除水晶，啟動或完成時成本會更新。
- [ ] 4.4 舊世界 snapshot migration。
- [ ] 4.5 Dedicated Server 跨維度實測。
