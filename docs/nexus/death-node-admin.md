# 死亡節點管理介面

DeadRecall 提供 OP／管理員專用的死亡節點管理介面，用於處理舊世界留下的孤立 Death Node、已回收但仍保留的停用紀錄，以及大量玩家死亡節點的資料整理。

## 開啟方式

需要 Minecraft 管理員指令權限：

```text
/deadrecall deathnodes
```

相容別名：

```text
/deadrecall deathpoints
```

指令必須由遊戲內玩家執行，Console 不會開啟 GUI。

## 篩選

介面可依下列條件篩選，按「套用篩選」後由 Server 重新查詢；Client 不會自行決定結果集：

- 玩家：輸入目前或已快取的玩家名稱，或完整 UUID；離線玩家會由 Server 的 profile cache 解析。
- Dimension：輸入完整維度 ID，例如 `minecraft:overworld`。
- 狀態：全部、`ACTIVE`、`DISABLED`。
- 建立時間：全部、一天、七天或三十天內（以 Server game time 計算）。

結果由 Server 依 Owner 名稱、建立時間與 UUID 的穩定鍵排序。使用上一頁／下一頁瀏覽所有相符節點；任何停用、刪除或傳送操作都會重新讀取 Server 端節點資料，不會信任 Client 列表。

每筆資料會顯示：

- 死亡節點自動名稱。
- Owner 名稱；離線且無可用名稱時顯示 UUID 前 8 碼。
- Dimension 與方塊座標。
- 節點 UUID 前 8 碼。
- 目前狀態。

選取一筆資料後，介面會另外顯示完整的 Owner／節點 UUID、建立與最後更新的遊戲刻，以及由 Server 計算的診斷旗標。診斷只讀取 Space Unit 與 discovery SavedData，絕不改寫資料、載入 chunk 或掃描世界實體；目前包含：

- `owner discovery missing`：活動節點不在其 Owner 的 discovery index。
- `non-private visibility`、`unexpected access list`、`unexpected structure data`：死亡節點具有建立流程不會寫入的設定。
- `duplicate active location`：同一 Owner、維度與座標有多個活動死亡節點。

死亡背包 ItemEntity 可能因 chunk 卸載而暫時不在記憶體中，因此管理介面不會以「目前未載入」推論背包遺失或自動清理。

## 安全操作

### 安全傳送

選取節點後可使用「安全傳送」。Server 會重新讀取節點 UUID、Dimension 與座標，在節點周圍搜尋可站立、無流體且不在危險方塊中的落點；Client 提交的座標不會被信任。若目標 Dimension 不可用或附近沒有安全落點，傳送不會執行。

### 停用節點

`ACTIVE` 節點只能先執行「停用節點」。停用後：

- 節點不再出現在一般玩家的 Nexus 地圖。
- Space Unit record 仍保留，方便追查及避免立即破壞死亡背包 binding。
- 對應死亡背包若稍後被回收，不會重新啟用節點。

### 永久刪除

只有非 `ACTIVE` 節點可以永久刪除。第一次按下「永久刪除」時，Server 會簽發一枚只綁定該管理員、該節點與刪除操作的確認 token；必須在 30 秒內再按一次「再次確認」才能執行。Client 無法以自行組裝的 UUID 或 token 繞過此驗證。永久刪除會：

- 從 Space Unit SavedData 移除該 Death Node record。
- 從所有玩家的 discovery 與 favorite 集合移除對應 UUID。
- 寫入伺服器管理 log。

管理員不能直接永久刪除 `ACTIVE` 節點，必須先停用。

### 批次停用與刪除

篩選列下方的批次按鈕會處理所有符合目前 Server 篩選的節點，而不只處理目前頁面或 Client 顯示的結果：

- 「停用符合條件的節點」只處理 `ACTIVE` 節點。
- 「刪除符合條件的節點」只處理非 `ACTIVE` 節點，並同步清掉 discovery 與 favorite reference。

兩種批次操作都需要在 30 秒內再次確認。確認 token 綁定管理員、操作種類與 Server 保存的篩選摘要；篩選改變、Client 自行提交 UUID 清單或 token 用在另一種操作時，Server 都會拒絕，並在確認時重新計算目前目標集合。

## 命名限制

Death Node 會使用自動名稱，例如：

```text
Death Echo 120, 64, -32
```

目前重新命名功能只適用於可管理的固定磁石 `LODESTONE`。Death Node 不支援重新命名，管理介面也不提供改名功能。
