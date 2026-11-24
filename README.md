# Cobblemon TC Paid Healing

A Cobblemon mod that turns the vanilla Healing Machine into a paid service. Each healing costs an amount based on the number of Pokémon actually healed, instead of being free.

## How it works

When a player uses a Healing Machine, the mod checks how many Pokémon need healing and whether the player can afford it before allowing the operation. If the balance is enough, the machine works as usual and the charge is applied after healing completes. If the balance is too low or the payment fails, the healing is cancelled and the machine's charge is restored to its previous state.

CobbleDollars is used when that mod is installed. Otherwise, the mod falls back to a configurable currency item. The price per Pokémon, currency mode and currency item are all defined in the config rather than hardcoded.

The price tag is drawn as a fixed-size HUD overlay attached to the machine: a green `$` for CobbleDollars, or the currency item icon with its amount for item mode.

## Supported versions

| Component | Version |
|---|---|
| Minecraft | 1.21.1 |
| Cobblemon | 1.8.4 and above |
| Java | 21 |
| Mod loaders | Fabric, NeoForge |
| CobbleDollars | Optional (preferred when installed) |

## Links

- Ko-fi: https://ko-fi.com/toancao
- YouTube: https://www.youtube.com/@toancaomc
- Discord: https://discord.gg/WP75Uxbsrq
- CurseForge: https://www.curseforge.com/members/toancao/projects
