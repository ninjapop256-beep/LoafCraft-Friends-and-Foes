# LoafCraft: Friends & Foes — Changelog

Fabric mod for Minecraft 1.21.11 · mod id `itzbreadloaf` · requires Carpet 1.4.194

Newest changes first. Existing worlds keep working; new structures only appear in chunks you haven't explored yet.

---

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
