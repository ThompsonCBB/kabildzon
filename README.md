<div align="center">

![Kabildzon](docs/hero.gif)

<img src="https://img.shields.io/badge/Minecraft-1.20.1-62B47A?style=for-the-badge&logo=minecraft&logoColor=white" />
<img src="https://img.shields.io/badge/Loader-Fabric-DBD0B4?style=for-the-badge" />
<img src="https://img.shields.io/badge/Status-Alpha-FF6B6B?style=for-the-badge" />
<img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />

**Kabildzon** — твой верный напарник в Minecraft.<br/>
Ходит за тобой, защищает и **приносит всё, что упало**.

</div>

---

## 🍎 Что он умеет

<table>
<tr>
<td width="55%">

![Fetch](docs/fetch.gif)

</td>
<td>

### Апорт!
Упало яблоко с дерева, выпал лут с моба, ты что-то выкинул — Kabildzon **сам подбежит, поднимет и принесёт** прямо тебе в инвентарь.

Когда отдаёт предмет — играет фирменный звук 🔊

</td>
</tr>
<tr>
<td>

### Как пёс, только лучше
- 🚶 Ходит за хозяином, телепортируется, если отстал
- ⚔️ Бьёт тех, кого бьёшь ты, и защищает тебя
- 🪑 Сидит на месте по команде
- 💔 Свой звук при получении урона

</td>
<td width="45%">

![Walk](docs/walk.gif)

</td>
</tr>
</table>

---

## 🎮 Как играть

| | Действие | Как |
|:-:|---|---|
| <img src="docs/egg.png" width="40"/> | **Призвать** | `Kabildzon Egg` во вкладке «Яйца призыва» или `/summon companion:companion` |
| <img src="docs/apple.png" width="40"/> | **Приручить** | ПКМ **яблоком** (шанс 1 к 3, появятся сердечки ❤️) |
| ✋ | **Сесть / встать** | ПКМ пустой рукой |
| 🍖 | **Вылечить** | ПКМ любой едой |
| <img src="docs/iron_sword.png" width="40"/> | **Звук меча** | Возьми любой меч в руку — услышат все рядом |

---

## 📦 Установка

1. Поставь **[Fabric Loader](https://fabricmc.net/use/)** для Minecraft **1.20.1**
2. Положи в папку `mods`:
   - **[Fabric API](https://modrinth.com/mod/fabric-api)** (версия для 1.20.1)
   - `kabildzon-x.x.x.jar` — свежая сборка во вкладке **Actions → последний запуск → Artifacts**
3. Запускай и ищи яйцо 🥚

---

## 🛠️ Сборка из исходников

```bash
# нужен JDK 17
gradle build
# готовый jar: build/libs/kabildzon-<version>.jar
```

<details>
<summary>📁 Где лежат ассеты</summary>

| Файл | Что это |
|---|---|
| `assets/companion/textures/entity/companion.png` | HD-скин 512×512 (развёртка игрока) |
| `assets/companion/sounds/companion/give.ogg` | звук, когда отдаёт предмет |
| `assets/companion/sounds/companion/hurt.ogg` | звук урона |
| `assets/companion/sounds/companion/sword_draw.ogg` | звук доставания меча |

</details>

---

## 🗺️ Планы

- [x] Моб-напарник с моделью игрока
- [x] Апорт предметов
- [x] Свои звуки
- [x] HD-скин
- [ ] 3D-рюкзак и причёска (Blockbench-модель)
- [ ] Свои анимации
- [ ] Прокачка напарника

<div align="center">

<sub>Сделано с ❤️ и яблоками · alpha</sub>

</div>
