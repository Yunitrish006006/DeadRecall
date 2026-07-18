# Design: Discord zh-TW Event Localization

## Goals

- Discord 的 Minecraft 系統文字固定輸出繁體中文。
- 本地化只在 Server 執行，不依賴玩家 Client locale。
- 玩家名稱、聊天、自訂名稱與 datapack literal text 維持原樣。
- 翻譯失敗不得影響原本遊戲事件。

## Dedicated Server runtime decision

Java 25／Dedicated GameTest classpath 驗證顯示 Minecraft 26.2 不提供 Client 資產：

```text
assets/minecraft/lang/zh_tw.json
```

因此 DeadRecall 不使用 Client language manager，也不修改全域 `Language` instance。第一階段內建 Minecraft 26.2 鎖定的 Discord 專用翻譯子集：

```text
assets/deadrecall/lang/discord_zh_tw/*.json
```

各檔案在 class initialization 時合併，並以 `Map.copyOf` 發布 immutable snapshot。第一階段不支援 runtime resource reload；翻譯更新隨 DeadRecall 版本發布。

## Processing boundary

```text
Server event
→ unresolved Component / semantic registry path
→ DiscordLocalizationService
→ DiscordEventFormatter
→ immutable DiscordEventPayload
→ existing DiscordBridge async HTTP transport
```

非同步 HTTP worker 只接收字串，不得讀取 Entity、Level 或 registry mutable state。

## Component rendering

Resolver 支援：

- literal Component 與 sibling。
- translatable Component。
- `%s`、`%1$s`、`%%` placeholder。
- 巢狀 Component 參數。
- 玩家、物品與村民自訂名稱 literal 保留。

未知 advancement、entity 或其他 key 使用中文安全 fallback；不得把 raw translation key 傳到 Discord。

## Advancement

來源傳遞未提前 `getString()` 的 title Component，frame type 映射：

| Type | 顯示 |
|---|---|
| `task` | 進度 |
| `goal` | 目標 |
| `challenge` | 挑戰 |

格式：`Alex 完成了進度「石器時代」`。同一次完成只建立一筆 `advancement` payload。

## Villager level-up

來源傳遞 custom name、profession path、previous level 與 current level。

- 未命名：使用「村民」。
- Profession：使用繁中名稱。
- Level 1–5：新手、學徒、老手、專家、大師。
- 自訂名稱：原樣保留。

格式：`村民（圖書管理員）升級：新手 → 學徒`。同一次升級只建立一筆 `villager_level_up` payload。

## Compatibility

不改變 Worker endpoint、`event/username/message/channels` 欄位、event ID、API Key、Bot Token／Webhook fallback、多頻道路由、SavedData 或世界 identifier。

## Phasing

本階段完成使用者回報的 advancement 與村民中文化。死亡訊息、Boss／實體名稱、raid result 與 difficulty 後續接入同一 service，不得建立第二套翻譯架構。

## Tests

JUnit 驗證 title、frame type、nested literal、fallback、村民 profession／level 與 exactly-once payload。Dedicated Server GameTest 驗證 bundled snapshot 可載入，且 advancement／村民各產生一筆 localized payload。完整 CI 仍包含 Server GameTests 與兩套 restart probes。
