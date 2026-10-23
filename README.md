# LoafCraft: Friends & Foes

A Fabric mod for Minecraft **1.21.11** that fills your world with ItzBreadLoaf, ImLilyLol, Madison, FelixSyn, Harley and Master BreadLoaf. They're real players, run by [Carpet](https://github.com/gnembon/fabric-carpet) fake players with their own brains, so they walk, sprint, fight, eat, build, fish and sleep the way a player does.

## Requirements

| | Version |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.19.5 or newer |
| Fabric API | 0.141.6+1.21.11 |
| Carpet | 1.21.11-1.4.194 |

## Install

1. Install Fabric Loader for 1.21.11.
2. Put **Fabric API**, **Carpet** and `itzbreadloaf-1.0.0.jar` (from the [Releases](../../releases) page) in your `mods` folder.
3. Start the game and create a new world, or keep playing an old one. New structures only appear in chunks you haven't explored yet.

## What's inside

- **The characters:**
  - **ItzBreadLoaf:** scrappy fighter with 10 types (Knight, Pirate, Miner, Sweaty, Captain, Bandit...). Has voice lines; tamed ones give a daily gift and a daily mission.
  - **ImLilyLol:** shy gardener, bookworm and witch who visits her cousin Madison.
  - **Madison:** farmer, baker, beekeeper and merchant who guards her pantry.
  - **FelixSyn:** mace-wielding drifter and ItzBreadLoaf's rival.
  - **Harley:** FelixSyn's partner and the best duelist in the mod.
  - **Master BreadLoaf:** boss with mace and elytra tactics.
- **Taming:** ItzBreadLoaf with steak and Harley with golden apples. Tamed characters follow you through portals, protect you, and sit or follow when you right-click.
- **Families:** babies with their parents' traits.
- **60 structures** across the Overworld, Nether and End, each with residents and its own loot. See [STRUCTURES.md](STRUCTURES.md) for where each one spawns and its `/locate` command.
- **39 advancements** in their own LoafCraft tab, plus a LoafCraft creative tab.

## Commands

| Command | What it does |
|---|---|
| `/friend add\|remove\|list <player>` | Your tamed characters never attack your friends (anyone can use it) |
| `/loafcraft spawn <type> [pos]` | Spawn a character (operators) |
| `/loafcraft list` | List all characters |
| `/loafcraft boss [pos]` | Summon Master BreadLoaf |

## Config

`config/itzbreadloaf.json` is created on first launch. It covers:
- **Characters:** taming chances, health, when characters back off in a fight, chatter, action-bar messages, and how many characters can be active at once.
- **FelixSyn and Harley:** how often FelixSyn shows up in villages, Harley's difficulty (easy/normal/hard), her best friend, and how often she gives gifts.
- **Master BreadLoaf:** boss difficulty.
- **Skins:** how often ItzBreadLoaf and FelixSyn wear their main skin.

Restart the game after editing it.

## Building from source

```bash
./gradlew build
```

Needs JDK 25. The jar ends up in `build/libs/`. To test, run `./gradlew runClient`.

## Changelog

See [CHANGELOG.md](CHANGELOG.md).
