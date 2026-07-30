# Tasks: Automata and Alchemy Item ID Migration

## Automata

- [x] 註冊 canonical 與 legacy copper wrench。
- [x] 讓 wrench selection/helper 同時辨識兩組 ID。
- [x] 在 Server wrench interaction 前保留 Components 轉換。
- [x] 切換 crafting、advancement、creative entry 與 asset 到 canonical。
- [x] 通過 registry、recipe、selection Components 與 interaction GameTests。

## Alchemy

- [x] 註冊八組 canonical 與 legacy Alchemy Item。
- [x] 切換 crafting、smelting、cauldron、drop 與 creative entry 到 canonical。
- [x] 讓 custom recipes、cauldron ingredients、pig manure projectile 與
  gunpowder recipe 接受新舊 Item。
- [x] 新增 canonical item definitions、translations 與雙 ID tags。
- [x] 通過 registry、Components、custom recipe、cauldron 與 remainder
  GameTests。

## 整合與發佈

- [x] 更新 Automata、Alchemy、DeadRecall README 與 OpenSpec 索引。
- [x] 建置兩個正式 JAR 並通過 DeadRecall bundled Dedicated Server smoke。
- [x] 更新正式版本號與 lockstep manifest。
  - TotemAutomata `0.1.7`、TotemAlchemy `0.1.5` 與 DeadRecall `2.4.5`
    已固定 source commit、production JAR SHA-512 與 exact checkout workflow。
