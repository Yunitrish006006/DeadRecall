# 開發參考

本頁提供程式碼入口與維護注意事項。系統需求與行為不變條件應以 `openspec/` 為準。

## 主要程式區域

| 路徑 | 責任 |
| --- | --- |
| `Deadrecall.java` | 模組入口、註冊、事件與指令 |
| `DiscordBridge.java` | Cloudflare Worker HTTP 橋接 |
| `DeathLocationManager.java` | 玩家死亡維度與座標管理 |
| `alchemy/` | 投料、煉藥鍋與豬糞流程 |
| `block/` | 糞便地面方塊、煉製中煉藥鍋與 Block Entity |
| `entity/ai/` | 豬吃草與產生豬糞方塊的 AI |
| `item/` | 物品註冊、背包與創造模式頁籤 |
| `item/copper/` | 銅板手及銅魁儡互動輔助 |
| `inventory/BackpackInventory.java` | 背包容器資料讀寫 |
| `mixin/` | 背包實體防護、虛空處理、豬 AI 與銅魁儡搬運攔截 |

## 重要資源

- 一般配方：`src/main/resources/data/deadrecall/recipe/`
- 煉藥鍋配方：`src/main/resources/data/deadrecall/deadrecall/cauldron_recipes/`
- 語言檔：`src/main/resources/assets/deadrecall/lang/`
- Discord Worker 範例：`cloudflare-worker-example.js`
- Fabric metadata：`fabric.mod.json`

## 關鍵實作原則

### 背包資料

- 使用 `DataComponents.CONTAINER` 與 `ItemContainerContents`。
- 開啟介面時固定追蹤原始背包 ItemStack；寫回前確認該 Stack 仍有效且仍在玩家物品欄。
- 不可依賴玩家目前主手，否則換手後可能把內容寫入錯誤物品。
- ItemStack 識別與資料回注應同時驗證物品類型、身份及 Components。

### 死亡背包

- 死亡前先記錄附近既有掉落物 UUID。
- 死亡後延遲到原版掉落完成，再只收集新出現的物品。
- 使用唯一 Custom Data 防止不同死亡背包實體合併。
- 維持單次死亡 guard，避免重複建立。

### Discord Bridge

- 聊天及啟動狀態以單執行緒 Executor 非同步送出，避免阻塞伺服器主執行緒。
- 關閉通知在停止流程中同步送出，降低程序提前結束造成遺失的機率。
- 設定檔位於 `<server>/config/discord-bridge.json`。

### 銅魁儡

玩家操作見 `docs/copper-golem/README.md`；完整行為與資料模型見 `openspec/specs/copper-golem/spec.md`。新功能應先修改 OpenSpec，再同步玩家文件。

## 文件更新規則

1. 玩家可見行為改動：更新 `docs/`。
2. 系統需求或 invariant 改動：更新 `openspec/specs/`。
3. 尚未完成的設計：放入 `openspec/changes/`。
4. 不再把所有內容重新合併成單一完整文檔。