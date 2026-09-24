# CreeperConfettiPro: An Unofficial Fork of CreeperConfetti for High-Version Servers

![CreeperConfettiPro](https://cdn.modrinth.com/data/cached_images/5b949dc2e24db1e4abd05c1bd5e885436f567614.gif)

As a Minecraft server administrator, have you ever been disappointed that the classic CreeperConfetti plugin simply won't run on modern servers? Do you wish that Creeper explosions on 1.20+ and even 1.21+ servers were no longer dull destruction, but a screen full of dazzling fireworks and confetti?

**CreeperConfettiPro** — an upgraded branch of CreeperConfetti, built for high-version servers, bringing the classic fun into the modern era!

## Where can I use CreeperConfettiPro?

![bukkit](https://wsrv.nl/?url=https%3A%2F%2Fcdn.jsdelivr.net%2Fnpm%2F%40intergrav%2Fdevins-badges%403%2Fassets%2Fcozy%2Fsupported%2Fbukkit_vector.svg&n=-1) ![spigot](https://wsrv.nl/?url=https%3A%2F%2Fcdn.jsdelivr.net%2Fnpm%2F%40intergrav%2Fdevins-badges%403%2Fassets%2Fcozy%2Fsupported%2Fspigot_vector.svg&n=-1) ![paper](https://wsrv.nl/?url=https%3A%2F%2Fcdn.jsdelivr.net%2Fnpm%2F%40intergrav%2Fdevins-badges%403%2Fassets%2Fcozy%2Fsupported%2Fpaper_vector.svg&n=-1) ![purpur](https://wsrv.nl/?url=https%3A%2F%2Fcdn.jsdelivr.net%2Fnpm%2F%40intergrav%2Fdevins-badges%403%2Fassets%2Fcozy%2Fsupported%2Fpurpur_vector.svg&n=-1)

> Two separate builds are provided — **Bukkit** and **Folia**. Choose the one that matches your server core.

## I. What the Plugin Is For

CreeperConfettiPro is the official upgraded branch of the original CreeperConfetti plugin, and its core goal is **full compatibility with high-version Minecraft servers**.

While keeping every fun feature of the original version, it resolves the compatibility problems the original has on 1.20 and above — not working, throwing errors, unresponsive commands and so on — and it completely rebuilt the command system, the firework system and the configuration structure, so high-version servers can enjoy Creeper explosions in full color too.

## II. Feature Highlights

### 1. Broad Multi-Version Support

- **Version coverage**:
  - **Bukkit build**: supports mainstream server cores including Spigot / Paper / Purpur;
  - **Folia build**: natively supports **Folia 1.19.4 ~ latest**, adapted through regionized thread scheduling — no more "installed it, but it won't run".

### 2. Multiple Firework Styles + Weighted Random (new in 4.0.0)

- **Random styles**: configure any number of firework styles; when a Creeper explodes, **one is picked at random by probability** — it is never the same every time;
- **Independent chances**: every style has its own trigger chance, with an **automatic compensation mechanism** — however you tune them, the total always stays at exactly 100%, so you will never end up with "a style that can never trigger" or "probability left unused";
- **Three color formats**: color names (`RED`, `LIME`, …), hex (`#FF0000` / `#f00`) or RGB maps (`{RED: 255, GREEN: 0, BLUE: 0}`) — whichever you prefer;
- **Adjustable shapes**: supports firework shapes such as `BALL`, `BALL_LARGE`, `STAR`, `BURST` and `CREEPER`, with independent flicker and trail toggles.

### 3. Graphical Management GUI (new in 4.0.0)

Run `/cc gui` to open a fully interactive interface — everything by mouse, no more hand-editing config files:

- Style list: browse every style and its chance, with pagination;
- One-click add: turn the firework you are holding into a new style;
- Chance tuning: `-10 / -5 / -1 / +1 / +5 / +10 %` buttons with automatic compensation;
- Preview / delete / equalize — all included.

## III. Installation & Usage

### Installation

1. Download the CreeperConfettiPro JAR that matches your server (Bukkit or Folia build);
2. Drop the JAR into the `plugins` folder of your server directory;
3. Start the server.

### Commands

The main command is `/creeperconfetti`, with the aliases `/cc` and `/confetti`.

| Command | What it does | Permission |
| --- | --- | --- |
| `/cc help [page\|command]` | Show help; page buttons are clickable in chat | `creeperconfetti.command` |
| `/cc gui` | Open the graphical manager | `creeperconfetti.command.effect` |
| `/cc effect list` | List all firework styles and their chances | `creeperconfetti.command.effect` |
| `/cc effect add [name]` | Add the firework in your hand as a new style | `creeperconfetti.command.effect` |
| `/cc effect set <name>` | Overwrite a style with the firework in your hand | `creeperconfetti.command.effect` |
| `/cc effect remove <name>` | Delete a style | `creeperconfetti.command.effect` |
| `/cc effect reset` | Restore the default styles | `creeperconfetti.command.effect` |
| `/cc effect preview [name]` | Preview a style | `creeperconfetti.command.effect` |
| `/cc effect chance [name] [percent]` | View / set the chance of each style | `creeperconfetti.command.effect` |
| `/cc chance [0-100]` | View / set the overall confetti chance | `creeperconfetti.command.chance` |
| `/cc language <info\|list\|set\|reload>` | Language management | `creeperconfetti.command.language` |
| `/cc reload` | Reload the config and firework styles (no restart needed) | `creeperconfetti.command.reload` |

> All of these permissions default to OP only; granting the parent permission `creeperconfetti.command` opens up every subcommand.

## IV. Why Choose CreeperConfettiPro?

- **Compatibility done right**: the Bukkit build covers 1.8 ~ latest, plus a native Folia build (1.19.4 ~ latest);
- **More than the classic**: keeps everything from the original and adds multiple firework styles, independent chances, a graphical GUI and paginated help;
- **Continuously maintained**: keeps up with new Minecraft releases to guarantee long-term usability.

## Summary

### Key Points

1. CreeperConfettiPro is an upgraded branch of CreeperConfetti. Its core value is **full compatibility with high-version servers**, and it ships a **Bukkit build (1.13 ~ latest)** and a **Folia build (1.19.4 ~ latest)**;
2. While keeping all the fun of the original (Creeper explosion confetti effects, configurable parameters, permission control, etc.), it adds a great deal of new capability, including **multiple firework styles with weighted randomness** and **a graphical management GUI**.

## Where can I get CreeperConfettiPro?

[![github](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/github_vector.svg)](https://github.com/Hi-PolarBear/CreeperConfettiPro)[![modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/modrinth_vector.svg)](https://modrinth.com/plugin/creeperconfettipro)[![hangar](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/hangar_vector.svg)](https://hangar.papermc.io/Hi-PolarBear/CreeperConfettiPro)
[![spigot](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/spigot_vector.svg)](https://www.spigotmc.org/resources/creeperconfettipro.133573)

### This plugin is an unofficial fork of CreeperConfetti. Please do not report issues with this plugin to the official CreeperConfetti team — report them to the official CreeperConfettiPro repository instead.


### Original CreeperConfetti plugin

Original plugin page: 

<details>
<summary>Spoiler</summary>

https://www.spigotmc.org/resources/creeperconfetti.85204/

</details>



### [Usage Statistics](https://bstats.org/plugin/bukkit/CreeperConfettiPro/29666)

![Usage Statistics Image](https://bstats.org/signatures/bukkit/CreeperConfettiPro.svg)
