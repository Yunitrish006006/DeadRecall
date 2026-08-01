# Tasks: Soulbound Teleport Interface

## Contracts and ownership

- [x] 在 TotemCore 新增死亡保留物品 policy。
- [x] 在 TotemNexus 保存唯一 active token 並標記成功傳送的介面物品。
- [x] 在 TotemRemnant 加入 persistent stage / respawn restore 流程。
- [x] 移除 DeadRecall `/back` 與 `DeathLocationManager`。

## Safety and compatibility

- [x] 一個堆疊最多保留一個物品。
- [x] 舊 token 因最新 SavedData token 而失效。
- [x] 消失詛咒優先。
- [x] Remnant 不直接 import Nexus classes。
- [x] 驗證 death capture、respawn restore 與 restart persistence。

## Verification and documentation

- [x] TotemCore、TotemNexus、TotemRemnant standalone build。
- [x] DeadRecall tests 與 dependency-boundary checks。
- [x] 八模組 bundled Dedicated Server smoke test。
- [x] 更新玩家與開發者文件。
- [x] 固定 DeadRecall `2.4.6`、Core `0.3.0`、Nexus `0.2.1`、
  Remnant `0.1.6` 與其餘精確 Core 相容模組版本。
