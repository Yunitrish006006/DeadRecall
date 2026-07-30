# Proposal: Remnant Item ID Migration

Status: implementation.
Target Version: DeadRecall 2.4.5 / TotemRemnant 0.1.5

## 問題

Remnant 已是背包功能的獨立 authority，但它的物品仍使用
`deadrecall:*` ID。這讓資料所有權停留在舊整合包 namespace，後續無法逐步
把其他 Remnant 物品移到一致的 `totem:remnant/*` 命名。

直接移除舊註冊會讓既有世界中的 ItemStack 變成 missing item，直接全世界
掃描又會觸碰離線玩家與未載入區塊，風險過高。

## 目標

- 五個 Remnant 背包以 `totem:remnant/*` 作為 canonical ID。
- 保留全部 `deadrecall:*` legacy ID，確保舊世界與舊 NBT 仍能解析。
- 新合成、死亡擷取與創造分頁只產生 canonical 物品。
- 玩家使用 legacy 背包時，以保留完整 Data Components 的方式轉換。
- legacy 一般背包可透過染色配方轉成相同 tier 的 canonical 物品。
- upgrade recipe 同時接受 legacy 與 canonical base。

## 非目標

- 不在啟動時掃描所有區塊、容器或離線玩家資料。
- 第一階段不移除 legacy ID，也不設定移除期限。
- 不改動背包容量、防護、內容格式、死亡節點綁定或容器安全語意。

## Canonical mapping

| Legacy | Canonical |
| --- | --- |
| `deadrecall:backpack_basic` | `totem:remnant/backpack_basic` |
| `deadrecall:backpack_standard` | `totem:remnant/backpack_standard` |
| `deadrecall:backpack_advanced` | `totem:remnant/backpack_advanced` |
| `deadrecall:backpack_netherite` | `totem:remnant/backpack_netherite` |
| `deadrecall:death_backpack` | `totem:remnant/death_backpack` |
