# LoafCraft: Friends & Foes — Changelog

Fabric mod for Minecraft 1.21.11 · mod id `itzbreadloaf` · requires Carpet 1.4.194

Newest changes first. Existing worlds keep working; new structures only appear in chunks you haven't explored yet.

---

## Unreleased (update 2, in progress)

### Fixes
- **No more characters stuck in village wells.**
  - FelixSyn used to walk to the nearest water block as a "hangout spot", and in villages that's the well; Harley followed him in. He now hangs out on dry ground beside the water instead.
  - Characters won't walk or drop into small enclosed pools with no edge to climb out of.
  - Anyone who still ends up stuck in water swims up, and after 5 seconds steps out onto the nearest dry block.

### Harley rebalance
- Full **diamond** armor with Protection II (no more netherite), a plain shield and a plain mace.
- **Harley's Blade** is now a diamond sword with Sharpness II only.
- **26 health** on Normal (was 40); Easy 20, Hard 32.
- No ender pearls and no potions, and only **one golden apple per fight**. Crits are less frequent, and she uses mace smashes less often.
- **Drops:** each armor piece 8.5%, Harley's Blade 25% when she was wild, and a golden apple 10%.

### New structure system
- **Blueprints:** structures are now drawn in memory, checked, and then placed.
  - Reusable pieces: rooms, doors, windows, gable/hip/flat roofs, straight and spiral stairs, ladders, towers, furniture sets, gardens, fields, paths and fences.
  - Biome palettes pick the wood, stone, ground, path, lights and flowers for 20+ biome types, including the Nether and End.
- **Every structure listed in one place:** id, name, category, biomes, rarity, residents, loot level and lore book. `./gradlew exportStructures` writes the worldgen files from it.
- **Ground leveling:** under and around every new structure, the terrain is cleared, filled underneath and sloped around the edges, so nothing floats or ends up half-buried.
- **Wider spacing rules:** structures now also stay off vanilla and other mods' structures, not just each other and villages.
- **Reachability check:** every floor, chest, bed and resident must be reachable.
  - `./gradlew checkStructures` runs it without starting the game.
  - `/loafcraft checkstructures` runs it in game.
- **Lookout Tower rebuilt:** it now has a ladder inside all the way to the top, three floors and a fenced lookout under a roof. Its id is unchanged.

### Remade structures (batch 1: ItzBreadLoaf's places)
- **26 structures rebuilt** on the new system with the same ids, rarity and biomes, so they show up in the same places in existing worlds. See `REMAKE.md` for what was wrong with each and what changed.
  - House, Tent, Bread Fort, Watchtower, Bandit Camp, Bread Bakery, Mining Camp, Jungle Treehouse, Desert Bunker, Igloo Hideout, Ruined Loaf Castle, Sweaty Dojo, Bread Mill, Swamp Hut, Mountain Outpost, Badlands Saloon, Cliffside Cave Base, Snowball Fort, Lookout Tower.
  - Pirate Ship, Fishing Dock, Loaf Lighthouse, Smugglers' Cove and Shipwreck Camp.
  - Village Campsite and Bastion Throne Room, which are built while you play.
- **Tents are walk-in now** (3 tall with an aisle), and campfires sit beside doorways instead of blocking them.
- **Fort towers:** every tower has a door and a ladder up to its crenellated top. Every Knight and the Captain has a bed in the barracks.
- **Village Campsite:** FelixSyn and Harley get a real tent with their beds, plus training dummies and a jump platform over hay for mace practice.
- **Bastion Throne Room:** bigger, with gold trim, soul-lantern pillars and a proper throne. It no longer changes the terrain around it inside the bastion.
- **Desert Bunker arrow trap:** it now fires only arrows from its own loot table, so it can't give out chest loot.
- Every remade structure passes the reachability check in 12 test builds (different seeds and biomes).

### Wild FelixSyns
- **Two kinds of FelixSyn:** the village drifter (as before, always with Harley), and **wild FelixSyns** that spawn out in the world and live in their own homes. Harley comes along with a wild one 30% of the time.
- **Wild gear, always a bit better than ItzBreadLoaf's:**
  - Weapon: mace 60% (sometimes with Density or Wind Burst), sword 25%, axe 10%, spear 5%, with higher tiers.
  - Biome-themed armor one tier better, with more pieces and more full sets.
  - A few wind charges and a golden apple.
- **Still fights every ItzBreadLoaf on sight.** Wild ones sleep in their own beds.
- **4 new homes:**
  - Mace Workshop (plains, savanna)
  - Cliff Hut with a jumping ledge (windswept hills, mountains)
  - Wind Tower with a ladder all the way to the top and a hay landing pad (open biomes)
  - Rival Camp, which only generates 24–96 blocks from an ItzBreadLoaf house or tent
- **Materials match the biome.**
- **New config options:** `wildFelixSpawnWeight`, `wildFelixHarleyChance`.

### Loot rarity system
- **One shared system** for every LoafCraft chest and character drop, with five tiers: Common, Uncommon, Rare, Epic, Legendary. Maces, god apples, netherite, totems and elytras **only** come from Rare-or-better rolls.
- **Loot levels:** basic houses roll mostly Common and Uncommon. Forts, castles, the Giant Loaf and Master BreadLoaf's places roll better tiers more often.
- **Elytras** only drop from End structures.
- **Character drops:**
  - Each character drops its own themed items plus tier rolls.
  - Armor drops 8.5% per piece, and their weapon 15%.
  - Looting gives bonus rolls.
  - FelixSyn sometimes drops wind charges, and rarely his own mace.
  - Tamed characters still drop everything they carry.
- **Master BreadLoaf** keeps his guaranteed Wind Burst mace and trophy, plus 2 extra Epic/Legendary rolls.
- **Config:** every chance and loot level is in the config. The item lists are loot tables a datapack can replace. See LOOT.md.

### FelixSyn's voice
- **58 recorded voice lines**, each registered as its own sound, play when he:
  - idles (every 20–40 seconds)
  - spots you
  - spots an ItzBreadLoaf
  - does a mace smash
  - gets hit
  - dies
  - backs off to heal
  - goes to sleep
  - reaches the village bell
  - meets Harley again
  - beats an ItzBreadLoaf
  - defends Harley or a villager
- **New config options:** `felixVoiceVolume`, `felixIdleVoiceMinSeconds`, `felixIdleVoiceMaxSeconds`.

## Skins from NameMC

- **FelixSyn wears his real skin**: his current NameMC skin.
- **Random skins for ItzBreadLoaf and FelixSyn.** Every ItzBreadLoaf and FelixSyn now wears a random skin from that player's NameMC skin history.
  - About half wear the main skin; the rest wear one of the older skins.
  - ItzBreadLoaf has 10 old skins and FelixSyn has 9.
- **Skins stay put.** Each character keeps its skin forever, and every player sees the same one. Knights, Pirates, Miners and babies get random skins too.
- **New config option:** `mainSkinChance` (default `0.5`) sets how often the main skin is worn.
- **Add your own skins** as `itzbreadloaf_alt11.png`, `felixsyn_alt10.png` and so on, and they're picked up automatically.
- **New FelixSyn spawn egg** colors to match his real skin.

## The Remake

### New structures (20)
- **ItzBreadLoaf:** Swamp Hut on stilts, Mountain Outpost on the peaks, Badlands Mineshaft Saloon, Cliffside Cave Base with a hidden trapdoor entrance.
- **ImLilyLol:** Floating Island Cottage over Cherry Groves, Lantern Garden, Treetop Library in tall birch trees.
- **Madison:** Riverside Mill with a water wheel, Bee Sanctuary full of beehives.
- **FelixSyn & Harley:** Ruined Portal Hideout.
- **Village Campsite:** FelixSyn & Harley's bedrolls, campfire and mace training dummies. It appears at the edge of a village when they arrive.
- **Master BreadLoaf:**
  - Nether Mace Arena: a lava-ringed arena with tall pillars.
  - End Sky Platform: a purpur deck on the outer End islands.
  - Bastion Throne Room: built inside every bastion the first time you walk in.
- **Shared:** Friendship Festival grounds with tables, food, banners and a stage. ItzBreadLoaf, Lily and Madison gather there.
- **Bonus:** Loaf Lighthouse, Starwatch Observatory, Sweet Berry Cabin, Shipwreck Camp, Snowball Fort.

### Upgraded structures
- **Bread Mill** (the windmill) now belongs to ItzBreadLoaf, with a much bigger wheat farm and a water channel.
- **Pumpkin Patch Farm** now has a farm shed, a composter and melons.
- **LoafCraft Village** now has a bakery booth with Baker Madison.

### Structures are easier to find
- **Rare structures are now uncommon:**

  | Structure | Before | Now |
  |---|---|---|
  | Giant Loaf | 80 | 40 |
  | Ruined Loaf Castle | 64 | 36 |
  | Sky Arena | 64 | 40 |
  | PvP Arena | 36 | 30 |
  | Cousins' Camp | 30 | 26 |
  | Mushroom House | 32 | 20 |
  | Nether Mace Training Ground | 28 | 24 |
  | End Mace Tower | 24 | 20 |

  Numbers are the spacing in chunks; smaller means more common.
- **No more overlaps.** Structures no longer generate on top of each other or inside vanilla villages.
- **Less crowding.** Lily's and Madison's houses are spaced out a little more.

### Smarter characters
- **Doors and gates** get closed behind them.
- **No more getting stuck** on the way home or to bed. They retry, and if the way is blocked for 20 seconds, they quietly teleport home when no player is watching.
- **Nights at home:** they stay closer to home at night.
- **Better fighting:** when low on health, characters back off behind their shield or sprint away to eat.
  - ItzBreadLoaf only backs off when it's really bad (scrappy).
  - Harley almost never backs off (the best duelist).
  - Lily and Madison back off early (defensive until provoked).
  - Master BreadLoaf never backs off.
- **Less lag with lots of characters:** path searches are spread out so 20+ characters don't lag the server.

### More personality
- **Idle chatter:** every character type has its own lines, shown in the action bar when you're nearby.
- **Characters talk to each other:** Lily and Madison, ItzBreadLoaf and the cousins, FelixSyn and Harley.
- **Cousin visits:** Lily now walks over to visit Madison, not just the other way round.

### Items & textures
- **New textures:** spawn eggs colored from each character's skin, a new Loaf Bread texture, and a custom Harley's Blade (dark blade, pink edge, gold guard).

### New advancements
- Mill Worker
- Sky Cottage
- Festival Goer
- Throne Room Crasher
- Peak Performance
- Campfire Stories

### New config options
- **Taming chances:** `breadTameChance`, `harleyTameChance`.
- **Health:** `breadHealthMultiplier`, `lilyHealth`, `madisonHealth`, `felixHealth`.
- **When characters back off:** `breadRetreatHealth`, `cousinsRetreatHealth`, `felixRetreatHealth`, `harleyRetreatHealth`.
- **Chatter:** `idleChatter`, `chatterMinSeconds`, `chatterMaxSeconds`.
- **Harley's gifts:** `harleyGiftMinSeconds`, `harleyGiftMaxSeconds`.
- **Performance:** `maxPathSearchesPerTick`.

## Harley's Best Friend

- **Harley adores the player named FelixSyn.** You can change the name with `harleyBestFriend` in the config.
- **She never attacks them**, even if they hit her.
- **She protects them** from anything that hurts them.
- **She tames on the first golden apple** from them.
- **Gifts only for them:** every few minutes she tosses them a gift that only they can pick up, such as flowers, cookies, golden apples, pearls, wind charges, diamonds, or rarely an enchanted golden apple.
- **New skin:** Harley now has her own girl skin from NameMC.

## Voice Lines, Daily Gifts & Missions

- **ItzBreadLoaf has a voice:** 71 recorded voice lines replace the placeholder meow. They play when he:
  - is idle, gets hit, or dies
  - spots you, or gets tamed
  - lands a big crit, or starts or wins a duel
  - is fed steak
- **Daily gift:** every Minecraft morning, a tamed ItzBreadLoaf gives his owner a gift.
- **Daily mission:** he then asks for items, like "bring me 16 wheat".
  - Right-click him holding them to get a reward and XP.
  - Sneak + right-click with an empty hand to see the mission again.

## FelixSyn, Harley & 28 New Structures

### FelixSyn and Harley
- **FelixSyn:**
  - A homeless mace fighter: Density and Wind Burst mace, wind charges, diamond or netherite gear.
  - Shows up in 40% of villages and hangs around the bell and the well.
  - Neutral, but fights ItzBreadLoaf on sight: "FelixSyn and ItzBreadLoaf are fighting!"
- **Harley:**
  - FelixSyn's partner and the best duelist in the mod: perfect timing, crits, strafing, shield, pearls, mace smashes and potions.
  - Easy, Normal and Hard presets.
  - Tame her with golden apples. Wild Harley always drops **Harley's Blade**.
  - If FelixSyn dies, she goes after his killer for 60 seconds.

### New structures
- **5 Lily houses, one per biome:**
  - Meadow cottage
  - Flower Forest cottage
  - Birch reading cabin
  - Snowy spruce cabin
  - Mushroom Fields house
- **8 ItzBreadLoaf structures:**
  - Bakery
  - Lookout Tower
  - Mining Camp
  - Jungle Treehouse
  - Desert Bunker with an arrow trap
  - Igloo Hideout with a secret basement
  - Fishing Dock
  - Ruined Loaf Castle
- **15 more:**
  - Greenhouse
  - Lakeside Gazebo
  - Farmstead
  - Orchard
  - Market Stall
  - Cousins' Camp
  - PvP Arena
  - Giant Loaf
  - Nether Mace Training Ground
  - End Mace Tower
  - Windmill
  - Sweaty Dojo
  - Flower Shrine
  - Smugglers' Cove
  - Pumpkin Patch

### Everything else
- **Loot in barrels:** every barrel in every structure now has loot.
- **`/friend add|remove|list <player>`:** your tamed characters won't attack your friends or their pets. No operator needed.
- **New characters:** Fisherman ItzBreadLoaf and Merchant Madison (trades food for emeralds).
- **Loaf Bread** can always be eaten.
- **New advancements:**
  - Rival Showdown
  - Village Drifter
  - Taken In
  - Untouchable
  - Structure Hunter (find 10 different structures)
  - Lily's Travels (Lily houses in 3 biomes)
  - Loaf Legend (find the Giant Loaf)
- **Creative tab:** FelixSyn egg, Harley egg, Harley's Blade.

### Fixes
- **Skins:** type, baby and alternate skins now show correctly, and slim/classic arms are detected automatically.
- **Advancement names:** they no longer show as raw keys.

## Earlier

- **Characters are real players:** rebuilt every character as a Carpet fake player with its own brain, so they move, fight, eat, build and use items like real players.
- **Types and structures:** added character types (Knight, Archer, Miner, Pirate, Nether, Sweaty, Captain, Bandit, Village, Bookworm, Gardener, Witchy, Farmer, Baker, Fisher, Beekeeper and more) and 8 structures.
- **Breeding stays, friendship goes:** removed the friendship system and kept breeding and babies.
- **Beds:** fixed beds in structures being rotated wrong.
