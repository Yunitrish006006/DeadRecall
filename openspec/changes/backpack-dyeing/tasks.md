# Tasks: Backpack Dyeing

## 資料與顯示

- [x] 為四級一般背包新增原版 `crafting_dye` 配方。
- [x] 將四級一般背包加入原版煉藥鍋洗色 tag。
- [x] 在四個 item definition 加入 dye tint。
- [x] 確認死亡背包未加入染色配方與洗色 tag。

## 回歸驗證

- [x] 驗證四個 tier 都能染色與混色。
- [x] 驗證染色保留內容、自訂名稱與其他 Data Components。
- [x] 驗證裝水煉藥鍋洗色保留非顏色 Components 並消耗一層水。
- [x] 驗證死亡背包不能染色或洗色。
- [x] 驗證 TotemRemnant standalone 與 DeadRecall bundle。

## 文件與版本

- [x] 更新 TotemRemnant 與 DeadRecall 使用教學。
- [x] 更新模組版本與整合包 lockstep metadata。
  - TotemRemnant `0.1.5` 與 DeadRecall `2.4.5` 已固定 source commit、
    production JAR SHA-512 與 exact checkout workflow。
