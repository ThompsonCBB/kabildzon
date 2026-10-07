<div align="center">

![Kabildzon](docs/hero.gif)

<img src="https://img.shields.io/badge/Minecraft-1.20.1-62B47A?style=for-the-badge&logo=minecraft&logoColor=white" />
<img src="https://img.shields.io/badge/Loader-Fabric-DBD0B4?style=for-the-badge" />
<img src="https://img.shields.io/badge/Status-Alpha-FF6B6B?style=for-the-badge" />
<img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />

**Kabildzon** is a loyal companion for Minecraft.<br/>
He follows you around, fights by your side and brings you everything that drops on the ground.

</div>

---

## 🍎 Features

<table>
<tr>
<td width="55%">

![Fetch](docs/fetch.gif)

</td>
<td>

### Fetches items
An apple fell from a tree, a mob dropped loot, or you threw something away — Kabildzon runs over, picks it up and puts it straight into your inventory.

He plays his own sound when handing the item over 🔊

</td>
</tr>
<tr>
<td>

### A reliable companion
- 🌍 Spawns naturally and wanders the Overworld
- 🚶 Follows his owner and teleports back if left behind
- ⚔️ Attacks your enemies and protects you
- 🪑 Stays put on command
- 💔 Has his own hurt sound
- 🥇 Always holds a gold ingot in his left hand — it drops if he dies

</td>
<td width="45%">

![Walk](docs/walk.gif)

</td>
</tr>
</table>

---

## 🎮 How to play

| | Action | How |
|:-:|---|---|
| 🌍 | **Find** | Look for him on grass in any Overworld biome — he is rare |
| <img src="docs/egg.png" width="40"/> | **Summon** | `Kabildzon Egg` in the Spawn Eggs tab, or `/summon companion:companion` |
| 🥇 | **Tame** | Right-click with a **gold ingot** — 1 in 3 chance, hearts ❤️ mean success |
| ✋ | **Sit / stand** | Right-click with an empty hand |
| <img src="docs/apple.png" width="40"/> | **Heal** | Right-click with any food |
| <img src="docs/iron_sword.png" width="40"/> | **Sword sound** | Hold any sword — every player nearby will hear it |

---

## 📊 Telemetry & privacy

Kabildzon sends **anonymous** usage statistics to help improve the mod. It is **enabled by default (opt-out)** and a one-time notice is shown in chat the first time you join a world.

**What is collected:**
- A random install ID (UUID generated locally, not linked to your account)
- Mod, Minecraft, Fabric Loader and Fabric API versions, environment (client/server), number of installed mods
- OS, architecture, Java version, RAM amount, CPU core count, game language
- Launches, world joins (singleplayer/multiplayer, time since launch), session length
- Error stack traces **only** if they come from this mod

**What is NOT collected:** usernames, UUIDs, IP-based location, chat, world names, server addresses or any other personal data.

**How to disable:** open `config/kabildzon-telemetry.json` and set `"enabled": false`.

## 📦 Installation

1. Install **[Fabric Loader](https://fabricmc.net/use/)** for Minecraft **1.20.1**.
2. Put into your `mods` folder:
   - **[Fabric API](https://modrinth.com/mod/fabric-api)** for 1.20.1;
   - `kabildzon-x.x.x.jar` — the latest build from **Actions → latest run → Artifacts**.
3. Launch the game and go find Kabildzon 🥚

---

## 🛠️ Building from source

```bash
# requires JDK 17
gradle build
# output: build/libs/kabildzon-<version>.jar
```

<details>
<summary>📁 Asset locations</summary>

| File | Purpose |
|---|---|
| `assets/companion/textures/entity/companion.png` | HD skin 512×512 (player layout) |
| `assets/companion/sounds/companion/give.ogg` | item hand-over sound |
| `assets/companion/sounds/companion/hurt.ogg` | hurt sound |
| `assets/companion/sounds/companion/sword_draw.ogg` | sword draw sound |

</details>

<div align="center">

<sub>Made with ❤️ and gold</sub>

</div>
