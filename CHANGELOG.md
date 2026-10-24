# LoafCraft: Friends & Foes — Changelog

Fabric mod for Minecraft 1.21.11 · mod id `itzbreadloaf` · requires Carpet 1.4.194

Newest changes first. Existing worlds keep working; new structures only appear in chunks you haven't explored yet.

---

## 2.1.0: AI overhaul

### AI overhaul part 8: balancing and fixes
- Fixed: Archer Lily (and any Lily or Madison) could get stuck unable to switch from her weapon to her bow.
- FelixSyn and Harley now take a couple of ticks to switch back to their sword after a mace move, like a player.
- Crit chances retuned after the switch to one crit decision per swing: Harley keeps her old crit rate exactly, and ItzBreadLoaf, FelixSyn, Lily and Madison crit a little more by default.
- Final balance table, boss simulation results and an in-game test checklist in `AI_DESIGN.md` section 12.

### AI overhaul part 7: optimization and debug tools
- **Faster AI:** characters think on different ticks instead of all at once; characters far from players think less (every 4th tick past 32 blocks) or pause (past 56 blocks) until someone comes near; paths are shared between characters and reused longer; line-of-sight checks in fights run less often.
- New config options: `aiFullDetailRange` and `aiSimpleDetailRange`.
- `/loafcraft debug ai` shows every character's goal, mood, detail level and how long its AI takes per tick, plus totals and path stats.
- `/loafcraft duel <a> <b>` makes two characters fight each other.

### AI overhaul part 6: personality and social behavior
- **Idle actions:** characters greet trusted players with a crouch-spam and a wave, greet their friends by name, jump when happy, look at what you're holding (and comment on it), wander over to dropped items, sort and fiddle with their hotbar, sit on chairs, stretch in the morning, stargaze at night, watch you build, tinker with a couple of blocks (and put them back), mine a bit of stone, fish, run home from the rain, catch their breath after a close fight, celebrate a win, turn to look at nearby fights, and step aside when you stand in them.
- **Friends stick together:** when one is attacked, nearby friends step in (Harley still stays out of FelixSyn's duels with ItzBreadLoaf).
- **Rivalry trash-talk:** ItzBreadLoaf and FelixSyn swap short, family-friendly lines before they fight.
- **Lines for every character** for greetings, fights, wins, low health, morning, night, rain and more, plus their own mood lines. New config option `personalityLines` to turn them off.

### AI overhaul part 5: mace techniques
- **Five mace moves,** shared by FelixSyn, Harley and Master BreadLoaf: wind-charge jumps, wind-charge chains (a wind charge down at you after each smash launches them again; a Wind Burst mace bounces by itself), pearl catches, diagonal pearls (an arcing pearl lands them beside you, then they wind-jump and come down from the side), and elytra rocket mace dives.
- They pick the move for the moment (distance, room above, your armor, their health) and the mace for it (Breach against heavy armor, Density for big drops, Wind Burst for chains). Smashes only land if their aim is on you, and moves go wrong now and then depending on skill.
- **FelixSyn** loves the air moves and now carries ender pearls, firework rockets and an elytra. **Harley** keeps it to wind jumps and short chains so she stays balanced. **Master BreadLoaf** adds pearl catches, still limited by his nerf (once every 20 seconds, warned, capped, sometimes failed).
- New command: `/loafcraft technique <name> <move> [target]` to make them do a move for testing.

### AI overhaul part 4: human-like combat
- **Mouse-like aim:** characters no longer snap onto you. Their view moves like a hand on a mouse: it overshoots, corrects, lags behind you when you strafe, and wobbles a little. They only swing when their crosshair is on you, and sometimes click too early and miss. Better fighters aim faster and steadier.
- **Reaction time:** 150-400 ms before they react to a new target, different every time.
- **Hotbar switching takes a few ticks,** like pressing a key, and resets their attack charge.
- **Real PvP moves:** full-charge hits, jump crits, W-taps and S-taps after hits, hit-and-run for Bandits, strafing in circles with random direction changes, and jump resets when they're hit.
- **Shields:** raised when a hit is actually coming (and early enough for the shield to work), never held up for long, not raised against axes by good fighters. They switch to an axe to break your shield, then back to their sword.
- **Backing off:** low on health they retreat, put up a couple of blocks for cover if they carry some, and eat a golden apple, drink a potion or eat food. They drink buff potions before a fight if they have any. Low-skill fighters sometimes panic when nearly dead.
- **Lily and Madison** fight basic and defensive until they're angry, then focus up.

### AI overhaul part 3: moods, memory, routines, skill profiles
- **Moods:** every character is happy, bored, tired, scared, angry or curious, depending on what's going on (getting hurt, a friend getting hurt, being fed, night, nothing to do, new faces). Moods change their speed, how soon they back off from a fight and what they do with their day, and they say a short line when their mood changes.
- **Memory:** characters remember players: who fed them, who hurt them, who they trust. Lily and Madison keep away from someone who hurt them from twice as far, and back off from them sooner in a fight.
- **Daily routines:** wake up, work, see friends at noon, more work, free time, head home, sleep. ItzBreadLoaf trains on dummies or bakes, Lily reads, gardens, brews or patrols, Madison farms, bakes and sorts her pantry, FelixSyn practices wind-charge mace jumps, and Harley spars with him. Friends walk over to each other and say hi with a quick crouch.
- **Skill profiles and play styles:** every character has Easy, Normal and Hard fighting numbers in the config (`skillProfiles`, chosen with `aiDifficulty`), and a play style (ItzBreadLoaf's types each fight their own way). Their swings now miss now and then, by skill.
- New command: `/loafcraft mood <name> [mood]` to see or set a character's mood.
- Config: `aiDifficulty`, `aiDifficultyOverrides`, `skillProfiles`, `playStyleOverrides`, `moods`, `routines`.

### AI overhaul part 2: Master BreadLoaf nerf
- **Much weaker:** 100 health (was 200), diamond armor with Protection II (was netherite Protection IV), and a plain diamond sword whose hits do 55% damage on Normal. He drinks one Speed I potion (no Strength) and has one golden apple per fight. No healing potions.
- **One mace:** Wind Burst I. The Density V and Breach IV maces are gone.
- **No hit of his can deal more than 8 hearts**, whatever the fall height. On Normal his smashes count at most 3 blocks of fall.
- **Smashes:** at least 12 seconds apart on Normal (never under 8 on any preset), and at most 2 hits per Wind Burst chain.
- **Big warnings:** before every smash and every elytra dive he stops for 1 second with a loud charge-up sound, sparks and portal particles.
- **More human:** he takes 300-400 ms to react to a new target, some of his sword swings miss, he crits less, his pearls are a little off, and a quarter of his elytra dives miss on purpose. Dives happen at most every 20 seconds.
- **Two phases:** at half health he gets 8% faster. That's all. He no longer summons ItzBreadLoafs.
- **Ender Dragon:** he now shows up after the dragon dies, never during the fight.
- Easy is weaker still. Hard brings back a little (120 health, Protection III, a Strength I potion) but keeps the damage cap and warnings.
- A Master BreadLoaf saved in an old world gets the new kit and health the next time he loads.
- Design doc for the whole AI overhaul: `AI_DESIGN.md`.

- Fixed: enchanted books from LoafCraft loot (Rare and Epic tiers) were blank, with no enchantments. They now get real enchantments (low level from Rare, high level from Epic).

## 2.0.0: Update 2 complete

### Part 10: advancements

- 10 new advancements: Wild Card (tame a wild FelixSyn), Wind Tower Climber, Bookworm (read 5 of Lily's books), Lore Keeper (visit all 15 lore places), The First Bakery, Where It All Began, Explorer I/II/III (25/50/100 structures) and Lucky Find (get a Legendary item).
- A book counts as read when it's in your inventory or you open the lectern it's on.
- Legendary loot is now tagged so Lucky Find can spot it (it won't stack with the plain version of the same item).
- Lily's Collected Works: a creative item that gives you every one of Lily's books. Each book is also in the LoafCraft creative tab.
- New lang keys for all of the above, plus the found-a-structure, lore and book messages.

### Part 9: Lily's books

- 26 books written and signed by ImLilyLol (2-5 pages each), about how the rivalry began, Master BreadLoaf, the Giant Loaf, Lily and Madison growing up, FelixSyn and Harley's drifting, Lily's notes on creatures and places, and some mysterious notes.
- 16 of them sit open on the lore structures' lecterns; 10 more are in the lore chests (the cousins' treehouse chest has two).
- The books point to each other's places like a treasure hunt: the Strange Note leads to the Sealed Library, which leads to the Archive Under the Old Loaf, whose Archive Index lists all 15 lore places; the Cartographer's Notes lead to the Crossroads Signpost.
- Every book carries a hidden `lily_book` tag so part 10's advancements can tell them apart.

## 1.1.0-alpha.5

- Structures are much more common: old structures spawn about 40% closer together, new ones 40-60 chunks apart (were 64-128).
- 15 lore structures: Lily's Abandoned Writing Cabin, Ruins of the First Bakery, The Memorial Grove, The Old Battle Site, The Sealed Library (hidden entrance under a bush), The Cartographer's Tent, Master BreadLoaf's Old Home, The Archive Under the Old Loaf, The Cousins' Old Treehouse, The Drifters' Rest, Lily's Naturalist Camp, Master's Departure Point, The Whispering Stones, The Crossroads Signpost, The Lost Loaf Wagon. Each has a lectern and chest ready for Lily's books.
- All 110 new structures are in (part 8 done).

## Unreleased (update 2, in progress)

### Fixes
- **No more characters stuck in village wells.**
  - FelixSyn used to walk to the nearest water block as a "hangout spot", and in villages that's the well; Harley followed him in. He now hangs out on dry ground beside the water instead.
  - Characters won't walk or drop into small enclosed pools with no edge to climb out of.
  - Anyone who still ends up stuck in water swims up, and after 5 seconds steps out onto the nearest dry block.

### New structures (part 8, batch 7: underground, 8)
- Each has a small sign of itself at the surface, and its rooms are 5-16 blocks down. Stairways and shafts are lined with stone, so caves, gravel and water stay out.
  - **ItzBreadLoaf's Bread Cellar:** a stairway down to a vaulted cellar of bread barrels.
  - **Miner's Shaft:** a headframe over a 14-deep ladder shaft to a miner's camp with ore in the walls.
  - **Witchy Lily's Secret Lab:** a hatch hidden in a bush, down to brewing stands and an enchanting table.
  - **FelixSyn's Secret Bunker:** a hatch by a boulder, down to a training bunker.
  - **Glowing Mushroom Grotto:** a cave with giant mushrooms, shroomlights and a pool.
  - **The Deep Forge:** a hut over a 16-deep shaft to a forge hall with a lava trough behind bars.
  - **The Buried Library**
  - **Madison's Root Cellar**
- `./gradlew checkStructures` now also checks that every item in the mod's loot tables exists in this Minecraft version. It caught `minecraft:chain`, which is `minecraft:iron_chain` in 1.21.11.

### FelixSyn can be tamed, and stops picking fights
- **Tameable:** feed him cooked porkchops (1 in 4, `felixTameChance`), only while he isn't fighting. This works for wild FelixSyns too.
  - Tamed, he follows you, defends you and helps fight what you attack, like the other tamed characters.
  - An empty-hand right-click makes him sit or stand.
  - If he dies tamed, he drops his inventory (any mace only 0.8% of the time).
- **He never starts fights with ItzBreadLoafs any more.** He only fights one that attacks him (or Harley).
  - Geared ItzBreadLoafs can still pick a fight with a wild FelixSyn outside villages, but they leave a tamed one alone.

### New structures (part 8, batch 6: Nether 8, End 6)
- **Nether** (on the Nether floor; no beds, since they explode there):
  - **Hellfire Bakery:** a lava oven behind glass.
  - **Crimson Outpost:** a palisade and a watchtower.
  - **Witchy Lily's Nether Garden:** nether wart and a brewing gazebo.
  - **Lava Fishing Spot:** a railed dock over lava.
  - **Basalt Watchtower**
  - **Soul Camp:** soul campfires and a bone fossil.
  - **Gold Trading Post**
  - **Strider Stables:** striders in a lava channel.
- **End** (outer islands):
  - **Void Observatory**
  - **Chorus Garden**
  - **The Last Loaf:** a golden loaf shrine where Master BreadLoaf meditates.
  - **Purpur Vault:** shulker boxes of loot and a dragon head.
  - **Dragon Lookout**
  - **Void Fishing Platform**
- Structures can now include striders, and loot containers other than chests and barrels (shulker boxes).

### New structures (part 8, batch 5: shared 10, ocean, river and beach 6)
- **Shared** (the friends together):
  - **Friends' Picnic:** three blankets in their colors.
  - **Campfire Story Circle:** three tents around a big fire.
  - **Movie Night Screen:** rows of seats, a projector booth and popcorn.
  - **Friends' Winter Lodge:** three beds, a long table, a fireplace and a snowman outside.
  - **Obstacle Course:** hay hurdles, a balance beam, a climbing wall and a podium.
  - **Treehouse Clubhouse:** a secret clubhouse in a big tree, reached through a hatch.
  - **Harvest Fair:** a prize-winning giant pumpkin, stalls and a hay-ride wagon.
  - **Community Garden:** crop plots, a safe well and a tool shed.
  - **Mini Golf Course:** three holes with a windmill, a ramp and slime bumpers.
  - **The Truce Pavilion:** where FelixSyn and ItzBreadLoaf signed a truce, with a red-and-blue table and the treaty on a lectern.
- **Ocean, river and beach:**
  - **Beach Snack Hut:** with umbrellas and a sand castle.
  - **Lifeguard Tower**
  - **River Ferry Landing:** a dock with a bell and a ferry raft.
  - **Pearl Diver's Raft:** floats on warm oceans, with a ladder down into the sea.
  - **Sea Glass Cove:** a rock pool with sea glass.
  - **Fishermen's Wharf:** a long pier with a net shed and nets drying on the rails.

### New structures (part 8, batch 4: Madison, 12)
- **Dairy Barn:** three cow stalls behind gated fences and "milk" cauldrons.
- **Madison's Pie Shop:** a big display window, a counter of cakes and stacks of pumpkins.
- **Fish Smokehouse:** smokers, a smoking chimney, dried-kelp racks and a fishing pond.
- **Chicken Coop Farm:** a fenced run with chickens and a raised coop with a ramp and hay nests.
- **Beekeeper's Cottage:** hives over campfires along the wall, honeycomb shelves and beeswax candles.
- **Horse Stables:** gated stalls with horses and a fenced paddock.
- **Farmers' Market:** three striped stalls (produce, cakes, flowers) and a little fountain.
- **Grain Silo:** a round silo half full of hay "grain", with a ladder up to a lookout under its cap.
- **Melon & Berry Farm:** melons on their stems, ripe berry bushes, a scarecrow and a farm stand.
- **Cookie Cottage:** cookie-dough walls with chocolate chips, and cocoa pods growing on a jungle log.
- **Sheep Ranch:** a pasture with sheep and a shearing shed with looms and wool.
- **Merchant Caravan:** a covered wagon on wheels with a step up the back, a hitched horse and a campfire.

### New structures (part 8, batch 3: ImLilyLol, 15)
- **Witchy Potion Shop:** brewing stands on the counter, shelves of potted mushrooms and a bubbling cauldron out back.
- **Moonlit Reading Nook:** a round stone tower with bookshelves and a ladder up to a loft bed, a telescope and a glass skylight.
- **Tulip Terrace:** three stepped terraces of tulips with stairs up the middle and a gazebo on top.
- **Willow Swing Pond:** a willow with hanging vines over a lily pond, a swing and a picnic.
- **Archer Lily's Glade:** targets on the tree trunks, a raised tree stand up a ladder, and her tent.
- **Pressed Flower Studio:** looms, an easel and flower "paintings" set into the wall.
- **Herbalist's Stilt Hut:** a hut on stilts over the swamp, with a cauldron, brewing stand and composter.
- **Stargazing Hill:** a grassy mound with a blanket and a telescope on top, and Big Dipper star stones at its foot.
- **Amethyst Garden:** amethyst clusters around a pool and a tinted-glass pavilion.
- **Lily's Letter Post:** a wall of mailbox barrels, a counter with a bell, and a mailbox out front.
- **Fairy Ring:** a ring of mushrooms, standing stones and a candle-lit pedestal.
- **Winter Dome Garden:** a glass dome over a warm garden in the snow.
- **Cherry Tea House:** a raised tea room with paper walls, cushions and a koi pond.
- **Bookbinder's Workshop:** bookshelves, lecterns and an anvil "press".
- **Rose Arch Garden:** a tunnel of leafy arches lined with roses, leading to a safe wishing well.

### New structures (part 8, batch 2: FelixSyn 10, easter eggs 5)
- **FelixSyn** (wild FelixSyns live here, Harley sometimes):
  - **Wind Charge Launch Pad:** a bullseye pad to land wind-charge jumps on, with targets up on posts.
  - **Smash Crater:** a crater from a huge mace smash, with cracked stone in the middle.
  - **FelixSyn's Trophy Hall:** trophies on pedestals, a red-vs-blue banner scoreboard and a gold podium.
  - **FelixSyn & Harley's Cabin:** a black bed and a pink bed, and a porch with two chairs.
  - **Parkour Tower:** a rainbow parkour course around a tower, with hay to land on.
  - **Sparring Ring:** a raised ring with rope fences and red and blue corners.
  - **Mace Drop Tower:** three jump boards out over a deep pool.
  - **The Rivalry Wall:** a giant "F vs B" with the win tally (FelixSyn is winning).
  - **Breeze Hunter's Camp:** a drying rack of breeze "rods" and a map table.
  - **Harley's Training Grove:** her pink tent in a clearing of petals under blossom trees.
- **Easter eggs** (very rare, nobody home):
  - **The Giant Mace:** half-buried in a cracked crater.
  - **The Tiny House:** with a mailbox.
  - **The Upside-Down House:** standing on its roof, the furniture on the ceiling and the garden on top.
  - **Portal to the Bread Dimension**
  - **The Developer's Desk:** a huge monitor, a whiteboard and a messy bin.

### New structures (part 8, batch 1: ItzBreadLoaf, 15)
- **Roadside Bread Stand:** a little stall under a bread-colored awning.
- **Toaster House:** a giant toaster with two slices sticking out of the top and a lever on the side, and you can live in it.
- **Baguette Tower:** a golden tower with score marks and a ladder through four floors to a lookout in its tip (Archer).
- **Crumb Trail Camp:** a hay-crumb trail to a walk-in tent, with a picnic blanket.
- **Knight Training Yard:** dummies, targets, banners and the knights' armory.
- **Bread Bank:** a stone bank with a teller counter, and a vault behind an iron door (the button is beside it). A Captain guards it.
- **Sweaty Gym:** a parkour course, anvil weights and a locker room.
- **Hay Bale Maze:** a real maze, different every time, with the prize in the far corner.
- **Miner's Rest Stop:** a cabin, ore piles and rails into a little tunnel.
- **Bandit Lookout:** a hut on a stilted platform up a ladder, with stolen goods stashed below.
- **Statue of ItzBreadLoaf:** a loaf with a face and a raised sword, on a plinth in a garden.
- **Archery Range:** a covered shooting line and lanes to targets.
- **Desert Oasis Camp:** a pond with reeds under palms, and a tent.
- **Snowy Bakehouse:** a stone bakehouse with a brick oven and a smoking chimney.
- **Captain's Manor:** two floors, with a hall, fireplace, railed stairs, bedrooms and a study.
- Every new structure has its own chest loot plus the rarity tiers, and passes the reachability check. New structures are spaced further apart than the old ones (64-96 chunks), so the world doesn't fill up.

### Remade structures (batch 3: shared places, FelixSyn's homes, Master BreadLoaf's arenas)
- **15 structures rebuilt**, which finishes the remake: all 64 LoafCraft structures now use the new system. See `REMAKE.md`.
  - LoafCraft Village, Cousins' Camp, PvP Arena, Friendship Festival, Giant Loaf and Ruined Portal Hideout.
  - FelixSyn's Mace Workshop, Cliff Hut, Wind Tower and Rival Camp.
  - Master BreadLoaf's Sky Arena, Nether Mace Training Ground, Nether Mace Arena, End Mace Tower and End Sky Platform.
- **Village well is safe:** it's fenced and roofed, so nobody can fall in.
- **Sky Arena and End Sky Platform:** stairs wind up around the pillar instead of a long ladder, with a rail around the stairwell at the top.
- **Nether Mace Arena:** four bridges cross the lava ring, and a rim keeps the lava from spreading.
- **Giant Loaf:** shaped like a real loaf, with a hidden treasure room under a carpet-covered trapdoor.
- **PvP Arena:** its ItzBreadLoaf now has gear, so the FelixSyn rivalry actually starts there.
- All 64 structures pass the reachability check in 12 test builds each.

### Remade structures (batch 2: ImLilyLol's and Madison's places)
- **23 structures rebuilt** with the same ids, rarity and biomes. See `REMAKE.md` for each one.
  - Lily: her House, the Meadow, Flower Forest, Reading, Snowy and Mushroom cottages, the Treehouse, Greenhouse, Lakeside Gazebo, Flower Shrine, Floating Island Cottage, Lantern Garden, Treetop Library and Starwatch Observatory.
  - Madison: her House, the Bakery, Farmstead, Orchard, Market Stall, Pumpkin Patch Farm, Riverside Mill, Bee Sanctuary and Sweet Berry Cabin.
- **Every high place has a way up:** stairs wind around the treehouse trunk, spiral stairs go up to the Treetop Library and the Floating Island, and ladders lead to the lofts and the observatory dome. Stairwells have rails.
- **More detail:** fireplaces with chimneys, a mill wheel in its own water channel, a scarecrow, animals in the farmstead pen, beehives with campfires, lily ponds, and lamp posts.
- All 49 remade structures pass the reachability check in 12 test builds each.

### FelixSyn: quieter, and peaceful in villages
- **He talks a lot less.**
  - Idle lines every 1.5-3 minutes (was every 20-40 seconds).
  - At least 15 seconds between any two lines.
  - Only about 1 in 4 hits plays an "ow" line; the rest use the normal hurt sound.
  - Config: `felixIdleLineMinSeconds`, `felixIdleLineMaxSeconds`, `felixVoiceGapSeconds`, `felixHurtVoiceChance`.
- **No fights in villages.** He doesn't start fights in or next to a village, and a rival fight that moves into one stops. He still fights back if he or Harley gets hit.
- **The rivalry is only with geared ItzBreadLoafs.** He only fights ItzBreadLoafs wearing armor or holding a weapon, and ItzBreadLoafs without gear don't go after him either.

### Structures are spread out more
- Every overworld LoafCraft structure is now about twice as far apart (for example Houses every 40 chunks instead of 20). Nether and End structures are unchanged.

### FelixSyn is usually alone
- FelixSyns in villages and structures now have Harley with them only 20% of the time (`felixHarleyChance`), and wild FelixSyns 20% (was 30%, `wildFelixWithHarleyChance`). Spawn eggs and `/loafcraft spawn felixsyn` still always bring her.

### Maces are super rare (under 1% everywhere)
- Maces are no longer in the Rare, Epic or Legendary loot tiers. Each chest gets one separate mace roll instead: 0.1% (Basic), 0.3% (Standard), 0.5% (Hard), 0.8% (Master).
- Characters that carry a mace drop it only 0.8% of the time: FelixSyn (was 5%), wild FelixSyns, and any tamed character carrying one. Harley never drops hers. Master BreadLoaf's Wind Burst mace is no longer guaranteed (still 0.8%); his trophy still is.
- Character drop tables never roll a mace.
- Config: `maceChestChance` (one per loot level) and `maceDropChance`.

### Structure blocks face the right way
- Stairs, ladders, doors and trapdoors in the remade structures faced backwards when they ran front to back. The blueprints and Minecraft's structure rotation disagreed about which way north is. They now line up however a structure is rotated. Ladders were also hanging on the wrong side of their wall, so they could pop off.
- Glass panes, iron bars, fences and walls now connect to the blocks around them instead of standing as thin posts. This applies to remade and old structures, including across chunk edges.
- Stairs in the remade structures also form proper corners.
- Only newly generated chunks get the fix; structures that already generated keep their old blocks.

### Harley stays out of FelixSyn's duels
- When her FelixSyn is dueling an ItzBreadLoaf, Harley watches instead of joining in. A stray hit from his opponent doesn't pull her in either. She still helps him in every other fight.

### Only one Harley follows FelixSyn
- Before, every Harley near the real player FelixSyn (`harleyBestFriend` in the config) followed him around. Now only one at a time does.
  - A Harley he tamed takes the spot over a wild one.
  - The others still greet him, send hearts and give gifts, but go about their day. Other Harleys he tamed wait where they are.
  - If his Harley dies, sits, or gets left behind, another one can take over after 2 seconds.

### Mace smashes work again
- FelixSyn, Harley and Master BreadLoaf jumped with a wind charge but never swung the mace on the way down. Switching to the mace in mid-air resets the attack charge, and they waited for a full charge, which a mace can't reach before landing. They now smash as soon as they're in range while falling.

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
