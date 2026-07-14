# Tasks: Concrete Powder Item Hardening

## 1. Version and API verification

- [ ] 1.1 確認 Minecraft 26.2、modern-yarn、Fabric Loader、Fabric API 與 Java 版本。
- [ ] 1.2 確認 `ItemEntity` tick 方法與目前 mappings 的水中判定 API。
- [ ] 1.3 確認 `ItemStack.transmuteCopy` 或等效 Components 保留 API。
- [ ] 1.4 檢查現有 ItemEntity Mixin，避免與死亡背包保護／渲染流程衝突。

## 2. Concrete mapping

- [ ] 2.1 建立 16 種原版混凝土粉末到同色混凝土的不可變映射。
- [ ] 2.2 加入映射完整性測試，確認沒有缺色、重複或錯色。
- [ ] 2.3 非支援物品必須以常數時間快速返回。

## 3. Server-side transformation

- [ ] 3.1 新增不需要世界全量掃描的 ItemEntity server-side 更新入口。
- [ ] 3.2 只在目前 stack 仍是支援的混凝土粉末時繼續。
- [ ] 3.3 驗證 ItemEntity 實際接觸 water-tagged fluid。
- [ ] 3.4 使用同一 ItemEntity 原子替換成對應混凝土 stack。
- [ ] 3.5 保留 count、合法 Components、位置、速度、Owner、pickup delay 與 age。
- [ ] 3.6 Client side 不修改 ItemStack，不生成幽靈物品。
- [ ] 3.7 不建立第二個 ItemEntity，不產生複製或遺失窗口。

## 4. Edge cases

- [ ] 4.1 水源可轉換。
- [ ] 4.2 流動水可轉換。
- [ ] 4.3 靠近水但未接觸時不轉換。
- [ ] 4.4 雨中不轉換。
- [ ] 4.5 物品欄、容器、背包與銅傀儡內不轉換。
- [ ] 4.6 已轉成混凝土後不重複執行。
- [ ] 4.7 若其他模組在同 tick 修改 stack，重新讀取目前 stack 後安全退出或轉換。

## 5. Tests

- [ ] 5.1 16 色對應 GameTest 或單元測試。
- [ ] 5.2 單個與整疊 64 個數量保存測試。
- [ ] 5.3 自訂名稱與可相容 Components 保存測試。
- [ ] 5.4 水源、流動水、非接觸水與雨天測試。
- [ ] 5.5 ItemEntity identity／位置／速度／pickup delay 保存測試。
- [ ] 5.6 Dedicated server 啟動與多人丟入水流測試。
- [ ] 5.7 大量 ItemEntity 壓力測試，確認沒有全世界 entity 掃描。

## 6. Documentation and release

- [ ] 6.1 更新玩家功能文件。
- [ ] 6.2 更新 changelog／版本變更清單。
- [ ] 6.3 記錄水鍋不在第一版範圍內。
- [ ] 6.4 執行 `./gradlew build`。
- [ ] 6.5 執行可行的 GameTest／Dedicated Server 回歸測試。
- [ ] 6.6 合併後將此 change 的完成狀態同步到 `openspec/roadmap.md`。