# Tasks: Direct Friend Player Teleport

## 1. Remove per-teleport consent

- [ ] 1.1 移除 pending consent 常數、Map、record 與過期清理。
- [ ] 1.2 簡化 `startTeleport`，好友 PLAYER 目標直接建立 session。
- [ ] 1.3 羅盤右鍵玩家只保留好友邀請／接受，不再接受傳送請求。
- [ ] 1.4 移除不再使用的翻譯 key 或保留 migration 期間相容說明。

## 2. Session safety

- [ ] 2.1 每 tick 重新驗證目標在線與雙向好友。
- [ ] 2.2 完成傳送前重新驗證目標與好友關係。
- [ ] 2.3 解除好友時取消雙方相關 PLAYER 目標 session。
- [ ] 2.4 目標離線、死亡或被移除時回傳明確取消原因。

## 3. UX and privacy

- [ ] 3.1 目標玩家收到好友開始傳送的通知。
- [ ] 3.2 保持 Client 粗略座標，不同步精確位置。
- [ ] 3.3 更新地圖資訊面板，不再顯示「等待同意」。

## 4. Tests

- [ ] 4.1 雙向好友可直接啟動 PLAYER 傳送。
- [ ] 4.2 非好友、單向邀請與自己都無法作為 PLAYER 目標。
- [ ] 4.3 解除好友、目標離線、死亡與切維度會取消。
- [ ] 4.4 完成時使用目標最新位置並找到安全落點。
- [ ] 4.5 Dedicated Server 與多人測試。
