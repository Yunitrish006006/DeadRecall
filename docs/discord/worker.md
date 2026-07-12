# Cloudflare Worker 部署

Discord Bridge 使用 Cloudflare Worker 接收 Minecraft 伺服器的 HTTP 請求，再轉送到 Discord。

## 必要 Secret

| 變數 | 用途 |
| --- | --- |
| `MC_API_KEY` | 驗證 Minecraft 請求 |
| `DISCORD_WEBHOOK_URLS` | Discord Webhook URL 的 JSON 陣列 |

Wrangler 範例：

```bash
npx wrangler secret put MC_API_KEY
npx wrangler secret put DISCORD_WEBHOOK_URLS
npx wrangler deploy
```

`DISCORD_WEBHOOK_URLS` 格式：

```json
["https://discord.com/api/webhooks/ID/TOKEN"]
```

## API 端點

| 端點 | 方法 | 用途 |
| --- | --- | --- |
| `/api/mc/chat` | POST | 轉送玩家聊天 |
| `/api/mc/server/status` | POST | 轉送伺服器狀態 |

所有請求都應包含：

```text
X-API-Key: <MC_API_KEY>
```

## 測試

```bash
curl -X POST https://your-worker.workers.dev/api/mc/chat \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-key" \
  -d '{"username":"TestPlayer","message":"Hello"}'
```

Worker 不應把 Secret、Webhook token 或完整授權標頭寫入 log。