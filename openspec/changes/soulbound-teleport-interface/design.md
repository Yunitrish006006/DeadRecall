# Design: Soulbound Teleport Interface

## Ownership

- TotemCore 提供 `DeathRetainedItemPolicy` 選配契約。
- TotemNexus 在傳送完成後產生新 token，寫入實際使用的 ItemStack，並將
  玩家目前有效 token 保存於既有 discovery SavedData。
- TotemRemnant 在死亡擷取前詢問 Core policy，抽出最多一個有效物品，
  保存於 `totem:remnant/soulbound_items` SavedData，重生後還原。
- DeadRecall 不參與判定、保存或還原，並移除 `/back`。

## Unique binding

物品上的 owner 與 token 必須同時符合 Nexus 保存的玩家目前 token。
每次成功傳送都換發新 token，因此舊物品即使離開玩家物品欄而未能清除標記，
也不再取得死亡保留資格。只開啟地圖、查看報價、取消或失敗傳送都不換 token。

## Death transaction

Remnant 只掃描玩家 Inventory 中 Server 權威的 ItemStack。找到有效候選後：

1. 若物品有消失詛咒效果則跳過。
2. 複製一個物品到 pending SavedData。
3. 從原 slot 移除一個。
4. 其餘物品繼續進入正常死亡背包 transaction。
5. 重生時優先放回原 slot；被占用時使用其他空 slot。
6. 成功放回後才刪除 pending record。

若 policy 失敗、資料無效或沒有空 slot，系統不得複製或把 pending item 丟到
世界；pending record 保留供後續登入重試。
