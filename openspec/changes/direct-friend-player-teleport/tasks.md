# Tasks: Direct Friend Player Teleport

## 1. Remove per-teleport consent

- [ ] 1.1 移除核心檔案中的 pending consent 常數、Map、record、方法與過期清理；目前啟動與新傳送入口會清空 legacy Map，但實體死碼仍待直接刪除。
- [x] 1.2 簡化公開傳送入口：線上雙向好友 PLAYER 目標直接進入既有已授權 session 流程。
- [x] 1.3 支援中的羅盤右鍵流程只處理好友邀請／接受；Server 啟動時清除 legacy pending consent，新的好友傳送不再建立逐次請求。
- [ ] 1.4 移除不再使用的翻譯 key 或保留 migration 期間相容說明。

## 2. Session safety

- [x] 2.1 每 tick 透過 `resolveTeleportTarget` 重新驗證目標在線與雙向好友。
- [x] 2.2 完成傳送前透過 `completeTeleport` 重新解析目標與好友關係。
- [x] 2.3 解除好友時立即取消任一方向以對方為 PLAYER 目標的傳送 session，取消不再等待下一個 Server tick。
- [ ] 2.4 目標離線、死亡或被移除時回傳更精確的取消原因；目前統一使用目標失效訊息。

## 3. UX and privacy

- [x] 3.1 只有好友傳送 session 實際建立成功後，目標玩家才收到「Teleport: requester → target」本地化通知。
- [x] 3.2 保持 Client 粗略座標，不同步精確位置。
- [x] 3.3 新的傳送請求不再進入「等待同意」流程。

## 4. Tests

- [ ] 4.1 雙向好友可直接啟動 PLAYER 傳送的多人整合測試。
- [ ] 4.2 非好友、單向邀請與自己都無法作為 PLAYER 目標的多人整合測試。
- [x] 4.3 雙方向好友關係 session 配對與無關 session 排除已有單元測試；解除好友、目標離線、死亡與切維度仍待遊戲內回歸。
- [ ] 4.4 完成時使用目標最新位置並找到安全落點。
- [x] 4.5 Java 25 build 與 Dedicated Server 啟動／Mixin 套用煙霧測試通過。
- [ ] 4.6 兩名以上真實玩家的多人回歸測試。
