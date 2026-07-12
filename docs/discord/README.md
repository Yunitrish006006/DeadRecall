# Discord Bridge

DeadRecall 可將 Minecraft 聊天、死亡訊息與伺服器狀態經 Cloudflare Worker 傳送到 Discord。

## 架構

```text
Minecraft Server
  → DeadRecall
  → Cloudflare Worker
  → Discord Webhook / configured channels
```

## 文件

目前完整部署、設定、API 端點、指令與故障排除請見：

- [Discord Bridge 部署指南](../discord-bridge-setup.md)
- [指令參考](../commands.md)

## 安全原則

- 只有 OP 可以修改 Bridge 設定。
- `MC_API_KEY` 應使用 Cloudflare Secret，不應提交到 Git。
- 不要在 issue、log 或截圖中公開完整 Webhook URL、Bot Token 或 API Key。
- 聊天與一般狀態通知應非同步送出，避免阻塞伺服器 tick。