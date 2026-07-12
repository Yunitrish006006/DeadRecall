# DeadRecall 文件

此目錄是 DeadRecall 的主要文件入口。玩家操作、伺服器設定、開發參考與規格文件分開維護，避免所有內容集中在單一大型 Markdown 檔案。

## 玩家文件

- [模組概覽](overview.md)
- [一般背包系統](backpacks.md)
- [死亡背包與 `/back`](death-and-recovery.md)
- [銅魁儡使用指南](copper-golem/README.md)
- [煉金、材料與配方](alchemy.md)

## 管理員文件

- [Discord Bridge 設定](discord-bridge-setup.md)

## 開發者文件

- [開發參考](development.md)
- [OpenSpec 索引](../OPENSPEC_INDEX.md)
- [OpenSpec 目錄](../openspec/README.md)
- [架構說明](../openspec/architecture.md)
- [開發路線圖](../openspec/roadmap.md)

## 文件維護原則

- `docs/`：描述目前玩家或管理員可使用的功能。
- `openspec/specs/`：描述已採用的系統規格與不變條件。
- `openspec/changes/`：描述尚在設計、實作或驗證中的變更。
- 功能行為改動時，應同步更新對應的 `docs/` 與 OpenSpec。
- 版本資訊以 `gradle.properties`、`fabric.mod.json` 與發佈頁面為準，文件不再複製一份容易過期的版本號。