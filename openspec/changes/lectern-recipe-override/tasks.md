# Tasks: Lectern Recipe Override

## 1. Specification

- [x] 1.1 確定替代配方為 4 個木半磚＋1 本書。
- [x] 1.2 確定覆寫 `minecraft:lectern`，不保留重複 recipe ID。

## 2. Resource implementation

- [x] 2.1 新增 `data/minecraft/recipe/lectern.json`。
- [x] 2.2 使用 26.2 現行 shaped recipe schema。
- [x] 2.3 使用 `minecraft:wooden_slabs` Tag。

## 3. Validation

- [ ] 3.1 執行 `./gradlew build`。
- [ ] 3.2 Dedicated Server 啟動並確認 recipe 無 parse error。
- [ ] 3.3 遊戲內測試不同木種半磚。
- [ ] 3.4 確認講台村民職業、書本與紅石行為不受影響。

## 4. Documentation

- [x] 4.1 新增 gameplay recipe 規格。
- [ ] 4.2 發佈時加入版本變更紀錄。
