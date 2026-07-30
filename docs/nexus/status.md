# 圖靈騰樞紐（Totem Nexus）實作狀態

系列名稱與整體進度統一列於 [TOTEM 圖靈騰系統總覽與進度](../../openspec/README.md)。本頁保留圖靈騰樞紐的功能與驗證摘要。

## 已完成核心與驗證

- Space Unit 世界 SavedData。
- 玩家探索資料 SavedData。
- 磁石 Space Unit 註冊與羅盤綁定。
- 羅盤左鍵探索磁石。
- 私有 Owner 權限基礎。
- 探索與權限雙重過濾。
- 以磁石或玩家位置作為來源的地圖請求、相對位置地圖 GUI、Dimension 分頁、清單、縮放、節點選取與重新整理。
- 伺服器傳送報價、傳送 session、倒數、重新驗證、成本扣除、安全落點、偏差、抵達風險與石碑磨損／退化。
- 磁石註冊確認、節點改名、可見性、校準與管理 GUI，以及地圖搜尋、分類、收藏與好友篩選。
- 好友邀請、好友磁石、人體磁石、線上好友目的地、分散重生 Gamerule 與密度加權分配。
- 死亡背包建立後的死亡 Space Unit 節點、回收停用與地圖隱藏。
- 傳送介面四種物品入口、回生羅盤、書本與已繪製地圖特化；29 個 required Server GameTests 全數通過。

## 待驗收與拆分

- 獨立 module 的 standalone、legacy-world、restart 與 compatibility-bundle cutover 驗證。
- 磁石完整管理 UX、離線玩家查詢、批次調整，以及好友／人體磁石進階互動。
- 兩名以上真人玩家的 Client UI、動態目標與多人流程驗收。
