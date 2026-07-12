# Totem Platform 開發狀態與 Roadmap

## 已完成或已有可運作基礎

### Totem Remnant / DeadRecall

- 死亡背包核心流程。
- 死亡物品收集與回收。
- 自訂物品及 Data Component 遷移基礎。
- 多人伺服器運作基礎。
- Minecraft 26.2 / Fabric 遷移工作。

### Totem Automata / Copper Golem

- 銅傀儡綁定與 GUI 基礎。
- 箱子分類模式。
- 物品搬運與庫存狀態。
- 採集模式的規格與部分實作基礎。
- LLM 設定與封包雛形。
- 客戶端範圍及路徑視覺化基礎。

### 共用基礎

- 自訂 Payload 與 Client/Server 同步。
- 自訂 GUI。
- 世界及設定資料存取。
- ItemStack 自訂資料處理。
- OpenSpec 銅傀儡文件。

## 進行中

- 銅傀儡模式切換與欄位清空驗證。
- 資源採集、Home 銅箱及 LLM 規則整合。
- 死亡背包資料安全、複製及物品回注問題修正。
- OpenSpec 統一與平台架構整理。
- DeadRecall 向 Totem 模組化架構過渡。

## 尚未完成

### Totem Core

- 獨立 repository／module。
- 穩定公開 API。
- 共用 Payload registry。
- SavedData migration framework。
- 共用 Config、GUI 與 permission API。
- 第三方 addon 範例與文件。

### Totem Nexus

- Space Unit 世界 SavedData。
- 磁石石碑註冊、右鍵確認與羅盤綁定。
- 玩家到場後以羅盤左鍵啟用磁石。
- 權限及探索雙重過濾。
- 相對位置地圖 GUI。
- 飢餓值及食物傳送成本。
- 跨維度紫水晶成本。
- 穩定度、偏差、安全落點及準備時間。
- 石碑磨損與方塊退化。
- 好友邀請、好友磁石及人體磁石。
- 分散重生 Gamerule 及密度加權分配。
- DeadRecall 死亡節點整合。

### Totem Excavation

- Blossom 錘子移植至 26.2。
- ItemStack Data Component 選區。
- 多玩家狀態隔離。
- 分 tick 採掘 session。
- Tag 驅動採掘規則。
- Client-side 選區框線。

### Totem Cognition

- Agent API。
- Provider abstraction。
- OpenAI、Gemini、Claude、Ollama 等 Provider。
- Tool calling、memory、planner 與 permission sandbox。
- Automata 可選整合。
- 未安裝 Cognition 時的完整 fallback。

## 建議開發順序

1. 先穩定目前 DeadRecall 2.x 的死亡背包及銅傀儡資料安全。
2. 抽出 Totem Core 最小共用層，但暫不大規模更改 mod ID。
3. 完成 Automata 的無 LLM 核心模式。
4. 實作 Nexus SavedData、磁石互動及探索權限。
5. 完成同維度基礎傳送，再加入穩定度及偏差。
6. 加入跨維度紫水晶、結構磨損及地圖 GUI。
7. 實作好友、人體磁石、分散重生及 Remnant 整合。
8. 移植 Excavation。
9. 最後建立 Cognition Agent Framework，作為可選模組。

## 重新命名策略

在模組真正拆分前：

- repository、mod ID 與 package 可暫時維持 DeadRecall。
- UI 與文件可逐步使用 Totem 品牌。
- 不直接更改現有 component、SavedData、item 或 block identifier。

正式拆分時：

- 為每個模組建立獨立 mod ID。
- 提供舊 DeadRecall identifier 到新模組 identifier 的 migration。
- 提供整合版或相容層，讓舊世界能安全升級。