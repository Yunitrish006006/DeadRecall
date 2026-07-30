# Tasks: Remnant Item ID Migration

## 註冊與轉換

- [x] 註冊五個 `totem:remnant/*` canonical 背包物品。
- [x] 保留五個 `deadrecall:*` legacy 物品註冊。
- [x] 新增保留完整 ItemStack Components 的 legacy-to-canonical helper。
- [x] 在使用 tiered 與 death backpack 時先完成 lazy migration。
- [x] 讓死亡背包掉落物光柱同時辨識 canonical 與 legacy 類型。

## 資源與取得路徑

- [x] 新增 canonical item definition、model 與翻譯。
- [x] 讓新背包鍛造配方只輸出 canonical ID。
- [x] 讓 upgrade recipe 同時接受 legacy 與 canonical base。
- [x] 讓 legacy 染色同時完成 canonical migration。
- [x] 更新 advancement、portable-container 與 cauldron tag。

## 回歸驗證

- [x] 編譯 main、client、gametest 與 resource source sets。
- [x] 驗證 canonical 與 legacy registry ID 並存。
- [x] 驗證使用轉換保留內容、名稱、染色與 addon Components。
- [x] 驗證 legacy dye 與 canonical dye recipe。
- [x] 驗證 restart probe 能保存 canonical death backpack。
- [x] 驗證 DeadRecall bundled Dedicated Server 可載入雙 ID graph。

## 文件與版本

- [x] 更新 TotemRemnant 與 DeadRecall 的 ID 相容說明。
- [x] 更新模組版本與整合包 lockstep metadata。
  - TotemRemnant `0.1.5` 與 DeadRecall `2.4.5` 已固定 source commit、
    production JAR SHA-512 與 exact checkout workflow。
