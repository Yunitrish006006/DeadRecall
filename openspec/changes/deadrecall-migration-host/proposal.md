# Proposal: DeadRecall Migration-Only Host

## Summary

DeadRecall 2.4.7 外層縮減為舊物品 ID 遷移與 Fabric nested-JAR 組裝主機。
所有玩法 authority 由八個 `totem-*` 模組擁有；外層不再編譯或封裝玩法
Mixin、GUI、Payload、指令、SavedData 或 fallback 實作。

## Scope

- 將 14 個 `deadrecall:*` 物品 alias 的唯一註冊權集中到 DeadRecall。
- 在 TotemCore 提供 component-preserving 的共用遷移表。
- 從 TotemRemnant、TotemAutomata、TotemAlchemy 移除舊物品註冊。
- 保留八個 exact-version nested JAR 的單檔安裝體驗。
- 以建置 gate 拒絕 DeadRecall 外層出現非遷移 gameplay class 或資源。

## Out of Scope

- 變更 SavedData、Payload 或既有資料資源 ID。
- 主動掃描離線玩家、未載入區塊或所有容器。
- 把任何玩法實作搬回 DeadRecall 外層。
