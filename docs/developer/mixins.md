# Mixin 參考

DeadRecall 使用 Mixin 攔截部分無法單靠事件 API 完成的原版行為。

| Mixin 類型 | 用途 |
| --- | --- |
| Entity below-world handling | 防止死亡背包被虛空直接移除 |
| ItemEntity damage／discard | 依背包等級處理防護與內容物掉落 |
| Pig goal registration | 將豬糞相關 AI 加入豬的目標選擇器 |
| Copper golem transport | 攔截並擴充銅魁儡容器搬運行為 |
| ItemEntity rendering | 為地上的死亡背包加入紅色光柱 |

## 維護原則

- 優先使用 Fabric event 或公開 API；只有缺乏穩定入口時才使用 Mixin。
- 每個注入點都應記錄目標方法、注入時機與取消原版行為的條件。
- Minecraft 升級時先驗證 method descriptor、mapping 與 locals，不可假設舊注入點仍有效。
- 取消原版流程前要確認替代流程完整處理資料、同步與實體生命週期。
- client-only rendering Mixin 不得修改伺服器權威資料。