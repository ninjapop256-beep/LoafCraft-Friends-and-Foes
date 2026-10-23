# LoafCraft structures

Rarity numbers are **spacing/separation in chunks** (a structure set's grid: one try per spacing x spacing chunk area,
at least separation chunks apart). Smaller spacing = more common.

**Where to change them:** `src/main/resources/data/itzbreadloaf/worldgen/structure_set/<file>.json` -> `placement.spacing`
and `placement.separation` (separation must stay smaller than spacing). The numbers are also kept in `tools/rarity.js`
(the NEW table at the top) and the `tools/*.json` specs, so regenerating doesn't undo your change.
Every overworld set also has `"exclusion_zone": {"other_set": "minecraft:villages", "chunk_count": 4}` so nothing generates
inside vanilla villages, and LoafCraft structures skip spots where a bigger LoafCraft structure would overlap them.

| Structure | Category | Biomes | Residents | Old rarity | Rarity | Loot level | Lore book | Find it |
|---|---|---|---|---|---|---|---|---|
| Badlands Mineshaft Saloon | ItzBreadLoaf | #is_badlands | Miner + ItzBreadLoaf | new | Uncommon 26/8 | 2 Standard | - | `/locate structure itzbreadloaf:badlands_saloon` |
| Madison's Bakery | Madison | plains, sunflower_plains, meadow, flower_forest, forest | Baker Madison | Uncommon (far apart) 30/9 | Uncommon (far apart) 30/9 | 1 Basic | - | `/locate structure itzbreadloaf:bakery` |
| Bandit Camp | ItzBreadLoaf | #is_forest, #is_taiga, dark_forest, #is_badlands, #is_savanna | 3-4 Bandits | Uncommon (far apart) 30/9 | Uncommon (far apart) 30/9 | 2 Standard | - | `/locate structure itzbreadloaf:bandit_camp` |
| Bee Sanctuary | Madison | flower_forest, meadow, sunflower_plains, cherry_grove, plains | Beekeeper Madison | new | Uncommon 26/8 | 1 Basic | - | `/locate structure itzbreadloaf:bee_sanctuary` |
| Sweet Berry Cabin (my idea) | Madison | taiga, snowy_taiga, old_growth_pine_taiga, old_growth_spruce_taiga | Madison | new | Uncommon 24/8 | 1 Basic | - | `/locate structure itzbreadloaf:berry_cabin` |
| Bread Bakery | ItzBreadLoaf | plains, sunflower_plains, meadow | ItzBreadLoaf | Uncommon 26/8 | Uncommon 26/8 | 1 Basic | - | `/locate structure itzbreadloaf:bread_bakery` |
| Cliffside Cave Base | ItzBreadLoaf | windswept_hills, windswept_gravelly_hills, windswept_forest, meadow, stony_shore | ItzBreadLoaf | new | Uncommon 26/8 | 2 Standard | - | `/locate structure itzbreadloaf:cliffside_base` |
| Cliff Hut | FelixSyn | windswept_hills, windswept_gravelly_hills, windswept_forest, stony_peaks, jagged_peaks, meadow | Wild FelixSyn (30% Harley) | new | Uncommon 24/8 | 2 Standard | - | `/locate structure itzbreadloaf:cliff_hut` |
| Cousins' Camp | Shared | forest, birch_forest, flower_forest, meadow, cherry_grove, plains | Lily + Madison | Uncommon 26/9 | Uncommon 26/9 | 1 Basic | - | `/locate structure itzbreadloaf:cousins_camp` |
| Desert Bunker | ItzBreadLoaf | desert, #is_badlands | ItzBreadLoaf | Uncommon 26/8 | Uncommon 26/8 | 2 Standard | - | `/locate structure itzbreadloaf:desert_bunker` |
| End Mace Tower | End | end_highlands, end_midlands | Master BreadLoaf | Common 20/7 | Common 20/7 | 4 Master | - | `/locate structure itzbreadloaf:end_mace_tower` |
| End Sky Platform | End | end_highlands, end_midlands | Master BreadLoaf | new | Uncommon 22/7 | 4 Master | - | `/locate structure itzbreadloaf:end_sky_platform` |
| Farmstead | Madison | plains, sunflower_plains, savanna | Farmer Madison + farm animals | Uncommon (far apart) 34/11 | Uncommon (far apart) 34/11 | 1 Basic | - | `/locate structure itzbreadloaf:farmstead` |
| Fishing Dock | ItzBreadLoaf | river, beach, snowy_beach | Fisherman | Common 20/6 | Common 20/6 | 1 Basic | - | `/locate structure itzbreadloaf:fishing_dock` |
| Floating Island Cottage | ImLilyLol | cherry_grove | ImLilyLol | new | Common 20/7 | 2 Standard | - | `/locate structure itzbreadloaf:floating_island_cottage` |
| Flower Shrine | ImLilyLol | flower_forest, cherry_grove, meadow, sunflower_plains | Gardener Lily | Uncommon (far apart) 30/10 | Uncommon (far apart) 30/10 | 1 Basic | - | `/locate structure itzbreadloaf:flower_shrine` |
| Bread Fort | ItzBreadLoaf | plains, meadow, #is_forest, #is_taiga, #is_savanna, windswept_hills, snowy_plains | 3 Knights + Captain | Uncommon (far apart) 34/11 | Uncommon (far apart) 34/11 | 3 Hard | - | `/locate structure itzbreadloaf:fort` |
| Friendship Festival | Shared | plains, meadow, flower_forest, sunflower_plains, cherry_grove | ItzBreadLoaf, Lily, Madison | new | Uncommon (far apart) 30/10 | 1 Basic | - | `/locate structure itzbreadloaf:friendship_festival` |
| Giant Loaf | Easter egg | plains, sunflower_plains, meadow | nobody (treasure) | Uncommon (far apart) 40/14 | Uncommon (far apart) 40/14 | 3 Hard | - | `/locate structure itzbreadloaf:giant_loaf` |
| Greenhouse | ImLilyLol | flower_forest, meadow, cherry_grove, plains | Gardener Lily | Uncommon 28/9 | Uncommon 28/9 | 1 Basic | - | `/locate structure itzbreadloaf:greenhouse` |
| House | ItzBreadLoaf | plains, sunflower_plains, meadow, #is_forest, #is_taiga, #is_savanna, snowy_plains, snowy_taiga, desert | ItzBreadLoaf | Common 20/7 | Common 20/7 | 1 Basic | - | `/locate structure itzbreadloaf:house` |
| Igloo Hideout | ItzBreadLoaf | snowy_plains, snowy_taiga, ice_spikes, snowy_slopes | ItzBreadLoaf | Uncommon 24/8 | Uncommon 24/8 | 1 Basic | - | `/locate structure itzbreadloaf:igloo_hideout` |
| Jungle Treehouse | ItzBreadLoaf | #is_jungle, dark_forest | ItzBreadLoaf | Uncommon 24/8 | Uncommon 24/8 | 2 Standard | - | `/locate structure itzbreadloaf:jungle_treehouse` |
| Lakeside Gazebo | ImLilyLol | birch_forest, old_growth_birch_forest, flower_forest, forest, cherry_grove | Bookworm or Lily | Uncommon 28/9 | Uncommon 28/9 | 1 Basic | - | `/locate structure itzbreadloaf:lakeside_gazebo` |
| Lantern Garden | ImLilyLol | flower_forest, cherry_grove, meadow, birch_forest | Gardener Lily | new | Uncommon 26/8 | 1 Basic | - | `/locate structure itzbreadloaf:lantern_garden` |
| Flower Forest Cottage | ImLilyLol | flower_forest | Lily / Gardener / Witchy | Common 16/6 | Common 16/6 | 1 Basic | - | `/locate structure itzbreadloaf:lily_flower_cottage` |
| Lily's House | ImLilyLol | cherry_grove | Lily / Gardener / Witchy Lily | Common 16/5 | Common 16/5 | 1 Basic | - | `/locate structure itzbreadloaf:lily_house` |
| Meadow Cottage | ImLilyLol | meadow | Lily / Gardener | Common 16/6 | Common 16/6 | 1 Basic | - | `/locate structure itzbreadloaf:lily_meadow_cottage` |
| Mushroom House | ImLilyLol | mushroom_fields | Lily / Witchy | Common 20/7 | Common 20/7 | 1 Basic | - | `/locate structure itzbreadloaf:lily_mushroom_house` |
| Reading Cabin | ImLilyLol | birch_forest, old_growth_birch_forest | Bookworm / Lily | Common 16/6 | Common 16/6 | 1 Basic | - | `/locate structure itzbreadloaf:lily_reading_cabin` |
| Snowy Spruce Cabin | ImLilyLol | snowy_taiga | Lily / Witchy / Bookworm | Common 16/6 | Common 16/6 | 1 Basic | - | `/locate structure itzbreadloaf:lily_snowy_cabin` |
| Loaf Lighthouse (my idea) | ItzBreadLoaf | #is_beach, stony_shore | Fisherman | new | Uncommon 26/8 | 1 Basic | - | `/locate structure itzbreadloaf:loaf_lighthouse` |
| Lookout Tower | ItzBreadLoaf | #is_forest, #is_taiga, dark_forest, old_growth_pine_taiga, old_growth_spruce_taiga | Archer | Uncommon 24/8 | Uncommon 24/8 | 2 Standard | - | `/locate structure itzbreadloaf:lookout_tower` |
| Mace Workshop | FelixSyn | plains, sunflower_plains, savanna, savanna_plateau | Wild FelixSyn (30% Harley) | new | Uncommon 28/9 | 2 Standard | - | `/locate structure itzbreadloaf:mace_workshop` |
| Madison's House | Madison | plains, meadow, sunflower_plains, flower_forest | Madison / Farmer / Beekeeper | Common 18/6 | Common 18/6 | 1 Basic | - | `/locate structure itzbreadloaf:madison_house` |
| Market Stall | Madison | plains, sunflower_plains, savanna, meadow, forest | Merchant Madison | Uncommon 24/8 | Uncommon 24/8 | 1 Basic | - | `/locate structure itzbreadloaf:market_stall` |
| Mining Camp | ItzBreadLoaf | #is_overworld | Miner | Uncommon (far apart) 32/10 | Uncommon (far apart) 32/10 | 2 Standard | - | `/locate structure itzbreadloaf:mining_camp` |
| Mountain Outpost | ItzBreadLoaf | jagged_peaks, frozen_peaks, stony_peaks, snowy_slopes | Archer + Knight | new | Uncommon 24/8 | 2 Standard | - | `/locate structure itzbreadloaf:mountain_outpost` |
| Nether Mace Arena | Nether | #is_nether | Master BreadLoaf | new | Uncommon 26/8 | 4 Master | - | `/locate structure itzbreadloaf:nether_mace_arena` |
| Nether Mace Training Ground | Nether | #is_nether | Master BreadLoaf | Uncommon 24/8 | Uncommon 24/8 | 4 Master | - | `/locate structure itzbreadloaf:nether_training_ground` |
| Orchard | Madison | plains, sunflower_plains, flower_forest, meadow | Beekeeper Madison | Uncommon (far apart) 30/10 | Uncommon (far apart) 30/10 | 1 Basic | - | `/locate structure itzbreadloaf:orchard` |
| Pirate Ship | ItzBreadLoaf | #is_ocean | 2-3 Pirates | Uncommon 28/8 | Uncommon 28/8 | 2 Standard | - | `/locate structure itzbreadloaf:pirate_ship` |
| Pumpkin Patch Farm (id pumpkin_patch) | Madison | plains, forest, taiga, dark_forest | Farmer Madison | Uncommon 28/9 | Uncommon 28/9 | 1 Basic | - | `/locate structure itzbreadloaf:pumpkin_patch` |
| PvP Arena | Shared | plains, savanna, desert, badlands | ItzBreadLoaf vs FelixSyn & Harley | Uncommon (far apart) 30/10 | Uncommon (far apart) 30/10 | 2 Standard | - | `/locate structure itzbreadloaf:pvp_arena` |
| Rival Camp (near ItzBreadLoaf houses) | FelixSyn | plains, sunflower_plains, meadow, forest, birch_forest, taiga, snowy_taiga, savanna, snowy_plains, desert | Wild FelixSyn (30% Harley) | new | Uncommon 24/8 | 2 Standard | - | `/locate structure itzbreadloaf:rival_camp` |
| Riverside Mill | Madison | river | Madison | new | Uncommon 22/7 | 1 Basic | - | `/locate structure itzbreadloaf:riverside_mill` |
| Ruined Loaf Castle | ItzBreadLoaf | plains, sunflower_plains, #is_savanna | Captain, Knights, ItzBreadLoafs (3-5) | Uncommon (far apart) 36/12 | Uncommon (far apart) 36/12 | 3 Hard | - | `/locate structure itzbreadloaf:ruined_loaf_castle` |
| Ruined Portal Hideout | FelixSyn | plains, forest, taiga, savanna, desert, birch_forest, dark_forest, snowy_plains | FelixSyn & Harley | new | Uncommon (far apart) 30/10 | 2 Standard | - | `/locate structure itzbreadloaf:ruined_portal_hideout` |
| Shipwreck Camp (my idea) | Ocean/beach | #is_beach | 2 Pirates | new | Uncommon 28/9 | 2 Standard | - | `/locate structure itzbreadloaf:shipwreck_camp` |
| Sky Arena | Master BreadLoaf | plains, desert, snowy_plains, #is_savanna, #is_badlands, meadow | Master BreadLoaf | Uncommon (far apart) 40/14 | Uncommon (far apart) 40/14 | 4 Master | - | `/locate structure itzbreadloaf:sky_arena` |
| Smugglers' Cove | Ocean/beach | #is_beach, stony_shore | Pirate + Bandit | Uncommon (far apart) 30/10 | Uncommon (far apart) 30/10 | 2 Standard | - | `/locate structure itzbreadloaf:smugglers_cove` |
| Snowball Fort (my idea) | ItzBreadLoaf | snowy_plains, ice_spikes, snowy_taiga, grove | ItzBreadLoaf + Village Bread | new | Uncommon 26/8 | 1 Basic | - | `/locate structure itzbreadloaf:snowball_fort` |
| Starwatch Observatory (my idea) | ImLilyLol | meadow, snowy_slopes, windswept_hills, snowy_plains | Bookworm Lily | new | Uncommon 28/9 | 1 Basic | - | `/locate structure itzbreadloaf:starwatch_observatory` |
| Swamp Hut | ItzBreadLoaf | swamp, mangrove_swamp | ItzBreadLoaf | new | Uncommon 24/8 | 1 Basic | - | `/locate structure itzbreadloaf:swamp_hut` |
| Sweaty Dojo | ItzBreadLoaf | bamboo_jungle, jungle, cherry_grove, savanna, plains | Sweaty | Uncommon (far apart) 32/10 | Uncommon (far apart) 32/10 | 2 Standard | - | `/locate structure itzbreadloaf:sweaty_dojo` |
| Tent | ItzBreadLoaf | plains, sunflower_plains, meadow, #is_forest, #is_taiga, #is_savanna, snowy_plains, snowy_taiga, desert | 2-4 ItzBreadLoafs | Uncommon 25/8 | Uncommon 25/8 | 1 Basic | - | `/locate structure itzbreadloaf:tent` |
| Lily's Treehouse | ImLilyLol | cherry_grove, forest, birch_forest, flower_forest, old_growth_birch_forest, meadow | Bookworm Lily | Uncommon 28/8 | Uncommon 28/8 | 1 Basic | - | `/locate structure itzbreadloaf:treehouse` |
| Treetop Library | ImLilyLol | birch_forest, old_growth_birch_forest | Bookworm Lily | new | Uncommon 22/7 | 1 Basic | - | `/locate structure itzbreadloaf:treetop_library` |
| LoafCraft Village | Shared | plains, sunflower_plains, meadow, #is_forest, #is_savanna, snowy_plains | Village Breads, Archer Lily, Farmer, Fisher and Baker Madison (+40% FelixSyn & Harley) | Uncommon (far apart) 36/12 | Uncommon (far apart) 36/12 | 1 Basic | - | `/locate structure itzbreadloaf:village` |
| Watchtower | ItzBreadLoaf | plains, sunflower_plains, #is_forest, #is_taiga, #is_savanna, desert, snowy_plains, meadow | Archer | Uncommon 24/8 | Uncommon 24/8 | 2 Standard | - | `/locate structure itzbreadloaf:watchtower` |
| Bread Mill (id windmill) | ItzBreadLoaf | plains, sunflower_plains, meadow | ItzBreadLoaf + Farmer Madison | Uncommon 28/9 | Uncommon 28/9 | 1 Basic | - | `/locate structure itzbreadloaf:windmill` |
| Wind Tower | FelixSyn | plains, sunflower_plains, savanna, meadow, snowy_plains, desert, badlands | Wild FelixSyn (30% Harley) | new | Uncommon (far apart) 30/10 | 2 Standard | - | `/locate structure itzbreadloaf:wind_tower` |
| Village Campsite | FelixSyn | edge of vanilla villages | FelixSyn & Harley | new | 40% of villages (felixVillageChance) | 2 Standard | - | walk into a village |
| Bastion Throne Room | Nether | inside bastion remnants | Master BreadLoaf | new | every bastion | 4 Master | - | `/locate structure minecraft:bastion_remnant` |
