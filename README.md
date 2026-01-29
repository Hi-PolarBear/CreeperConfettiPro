# CreeperConfettiPro: an unofficial fork of CreeperConfetti for high-version servers
![CreeperConfettiPro](https://cdn.modrinth.com/data/cached_images/5b949dc2e24db1e4abd05c1bd5e885436f567614.gif)

As a Minecraft server administrator, have you ever regretted that the classic CreeperConfetti plugin fails to run properly on high-version servers? Do you want Creepers' explosions in 1.20+ and even 1.21+ servers to no longer be monotonous destruction but full-screen colorful particle effects? CreeperConfettiPro — a professional upgraded branch of the CreeperConfetti plugin, tailored for high-version Minecraft servers, letting classic fun adapt to the new era!

## Where can I use CreeperConfettiPro?

<img src="https://wsrv.nl/?url=https%3A%2F%2Fcdn.jsdelivr.net%2Fnpm%2F%40intergrav%2Fdevins-badges%403%2Fassets%2Fcozy%2Fsupported%2Fbukkit_vector.svg&n=-1" alt="bukkit"> <img src="https://wsrv.nl/?url=https%3A%2F%2Fcdn.jsdelivr.net%2Fnpm%2F%40intergrav%2Fdevins-badges%403%2Fassets%2Fcozy%2Fsupported%2Fspigot_vector.svg&n=-1" alt="spigot"> <img src="https://wsrv.nl/?url=https%3A%2F%2Fcdn.jsdelivr.net%2Fnpm%2F%40intergrav%2Fdevins-badges%403%2Fassets%2Fcozy%2Fsupported%2Fpaper_vector.svg&n=-1" alt="paper"> <img src="https://wsrv.nl/?url=https%3A%2F%2Fcdn.jsdelivr.net%2Fnpm%2F%40intergrav%2Fdevins-badges%403%2Fassets%2Fcozy%2Fsupported%2Fpurpur_vector.svg&n=-1" alt="purpur"></p>

## I. Core Positioning of the Plugin
CreeperConfettiPro is the official upgraded branch of the original CreeperConfetti plugin, with its core goal to **fully adapt to high-version Minecraft servers**. On the basis of completely retaining the fun functions of the original plugin, it resolves compatibility issues (such as malfunction, server crashes, unresponsive commands, etc.) of the original plugin in version 1.18 and above, enabling high-version servers to also enjoy the colorful explosion effects of Creepers.

## II. Core Feature Highlights
### 1. Full-Dimension High-Version Adaptation (Core Advantage of Pro Version)
- **Version Coverage**: Perfectly supports all mainstream high-version Minecraft servers from 1.20 to 1.21.1, compatible with mainstream server cores including Paper, Spigot and Purpur;
- **API Adaptation**: Core code rebuilt based on high-version Bukkit/Spigot API, abandoning outdated API calling methods to eliminate plugin errors caused by API version incompatibility.

### 2. Retain All Fun Functions of the Original Version
CreeperConfettiPro does not remove any classic functions of the original version, allowing you to enjoy the same fun in high versions:
- **Creeper Explosion Effect**: Automatically generate colorful confetti particle effects when Creepers explode, replacing monotonous explosion particles for a more joyful visual experience;
- **Customizable Effect Parameters**: Modify particle color, density, flight speed and duration via configuration file to customize effects according to server style;
- **Permission Control**: Control whether players can trigger effects through the permission node (`creeperconfetti.use`), and globally enable or disable the effect function;
- **Compatibility Mode**: Support effect adaptation for special Creeper types such as Charged Creepers and modded Creepers (needs to be enabled in configuration file).

## III. Installation & Usage (User-Friendly for Beginners)
### Installation Steps
1. Download the CreeperConfettiPro plugin JAR file matching your server version;
2. Place the JAR file into the `plugins` folder under the server directory;
3. Restart the server, and the plugin will automatically generate configuration files;
4. (Optional) Edit the `plugins/CreeperConfettiPro/config.yml` file to customize particle effect parameters.

### Common Commands
| Command | Function | Permission Requirement |
|---------|----------|------------------------|
| `/creeperconfetti reload` | Reload plugin configuration file, making parameter modifications take effect without server restart | OP or `creeperconfetti.admin` |
| `/creeperconfetti seteffect` | Customize Creeper explosion particle parameters (e.g., color, density) for quick configuration via command line | OP or `creeperconfetti.admin` |
| `/creeperconfetti reseteffect` | Restore particle effect parameters to plugin defaults for quick reset of personalized configurations | OP or `creeperconfetti.admin` |

## IV. Why Choose CreeperConfettiPro?
- **Full Compatibility**: Built specifically for high versions, eliminating the embarrassment of "plugin installed but unusable";
- **Authentic Experience**: Retain all original fun functions without the need to adapt to new operation logic;
- **Stable & Reliable**: Rebuilt based on high-version API, tested across multiple versions with no crashes or missing effects;
- **Easy Maintenance**: Configuration file structure consistent with the original version, no need for administrators to re-learn configuration methods;
- **Continuous Updates**: Timely adapt to new Minecraft version iterations to ensure long-term usability.

## Summary
### Key Points Recap
1. CreeperConfettiPro is an upgraded branch of CreeperConfetti, with core value of **adapting to high-version Minecraft servers from 1.20 to 1.21.1**;
2. Retain all fun functions of the original version (Creeper explosion confetti effect, customizable parameters, permission control, etc.).

## Where can we get CreeperConfettiPro?
[![github](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/github_vector.svg)](https://github.com/Hi-PolarBear/CreeperConfettiPro)[![modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/modrinth_vector.svg)](https://modrinth.com/plugin/creeperconfettipro)[![hangar](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/hangar_vector.svg)](https://hangar.papermc.io/Hi-PolarBear/CreeperConfettiPro)  

### This plugin is an unofficial fork of CreeperConfetti. Do not report issues of this plugin to the official CreeperConfetti team; instead, submit them to the official repository of CreeperConfettiPro.
### Official address of the original CreeperConfetti plugin

<details>
<summary>Official Address of the Original Version</summary>

https://www.spigotmc.org/resources/creeperconfetti.85204/

</details>

