# Discord 事件繁體中文顯示

## 語系策略

DeadRecall 的 Discord-facing Minecraft 系統事件固定使用繁體中文（`zh_tw`），不依賴：

- Minecraft Client 語言設定。
- 在線玩家的語系。
- Dedicated Server 作業系統語系。
- Discord Worker 或 Bot 的翻譯功能。

Minecraft 26.2 的純 Dedicated Server runtime 不提供完整 Client `zh_tw.json`。因此 DeadRecall 內建與目前 Minecraft 版本鎖定的 Discord 專用翻譯子集，並在 Server 啟動後以 immutable snapshot 使用。

## 已中文化事件

### Advancement

Discord 會顯示繁體中文進度名稱與類型，例如：

```text
Alex 完成了進度「石器時代」
Alex 完成了目標「鑽石！」
Alex 完成了挑戰「獵取怪物」
```

`task`、`goal`、`challenge` 不會直接以英文顯示。

### 村民升級

未命名村民會顯示通用名稱、職業及前後階級：

```text
村民（圖書管理員）升級：新手 → 學徒
```

有自訂名稱時名稱維持原樣：

```text
Archivist E（圖書管理員）升級：學徒 → 老手
```

## 不翻譯的內容

以下文字視為玩家或資料包提供的 literal text，不會被 DeadRecall 改寫：

- 玩家名稱。
- 玩家聊天內容。
- 村民自訂名稱。
- 物品自訂名稱。
- 巢狀 Component 中的 literal 參數。

## 缺漏翻譯

缺少 translation key 時使用中文安全 fallback，例如「未知進度」或「未知實體」。Discord payload 不會直接顯示：

```text
advancements.example.missing.title
```

Fallback 不會建立第二筆 Discord 事件。

## 相容性

中文化不改變：

- `/api/mc/chat` endpoint。
- `event`、`username`、`message`、`channels` payload 欄位。
- `advancement`、`villager_level_up` event ID。
- API Key、Bot Token、Webhook fallback 或多頻道路由。
- SavedData、世界資料或遊戲 identifier。

## 後續範圍

死亡訊息、Boss／實體預設名稱、襲擊結果與難度名稱已保留在同一 OpenSpec 的後續階段，接入時會共用 `DiscordLocalizationService`。

## 人工驗收

1. 啟用 Discord Bridge 並設定測試頻道。
2. 完成至少一個 task、一個 goal 及一個 challenge advancement。
3. 確認 Discord 顯示繁中標題與「進度／目標／挑戰」。
4. 讓未命名圖書管理員從 level 1 升到 level 2。
5. 確認訊息為「村民（圖書管理員）升級：新手 → 學徒」。
6. 為村民設定包含英文或中文的自訂名稱，再次升級並確認名稱未被改寫。
