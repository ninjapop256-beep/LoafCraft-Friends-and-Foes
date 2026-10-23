# LoafCraft structures

Rarity numbers are **spacing/separation in chunks** (a structure set's grid: one try per spacing x spacing chunk area,
at least separation chunks apart). Smaller spacing = more common.

**Where to change them:** `src/main/resources/data/itzbreadloaf/worldgen/structure_set/<file>.json` -> `placement.spacing`
and `placement.separation` (separation must stay smaller than spacing). The numbers are also kept in `tools/rarity.js`
(the NEW table at the top) and the `tools/*.json` specs, so regenerating doesn't undo your change.
Every overworld set also has `"exclusion_zone": {"other_set": "minecraft:villages", "chunk_count": 4}` so nothing generates
inside vanilla villages, and LoafCraft structures skip spots where a bigger LoafCraft structure would overlap them.

| Structure | Biomes | Residents | Old rarity | New rarity | Find it |
|---|---|---|---|---|---|
| Badlands Mineshaft Saloon | #is_badlands | Miner + ItzBreadLoaf | new | Uncommon 26/8 | `/locate structure itzbreadloaf:badlands_saloon` |
| Madison's Bakery | plains, sunflower_plains, meadow, flower_forest, forest | Baker Madison | Uncommon (far apart) 30/9 | Uncommon (far apart) 30/9 | `/locate structure itzbreadloaf:bakery` |
| Bandit Camp | #is_forest, #is_taiga, dark_forest, #is_badlands, #is_savanna | 3-4 Bandits | Uncommon (far apart) 30/9 | Uncommon (far apart) 30/9 | `/locate structure itzbreadloaf:bandit_camp` |
| Bee Sanctuary | flower_forest, meadow, sunflower_plains, cherry_grove, plains | Beekeeper Madison | new | Uncommon 26/8 | `/locate structure itzbreadloaf:bee_sanctuary` |
| Sweet Berry Cabin (my idea) | taiga, snowy_taiga, old_growth_pine_taiga, old_growth_spruce_taiga | Madison | new | Uncommon 24/8 | `/locate structure itzbreadloaf:berry_cabin` |
| Bread Bakery | plains, sunflower_plains, meadow | ItzBreadLoaf | Uncommon 26/8 | Uncommon 26/8 | `/locate structure itzbreadloaf:bread_bakery` |
| Cliffside Cave Base | windswept_hills, windswept_gravelly_hills, windswept_forest, meadow, stony_shore | ItzBreadLoaf | new | Uncommon 26/8 | `/locate structure itzbreadloaf:cliffside_base` |
| Cousins' Camp | forest, birch_forest, flower_forest, meadow, cherry_grove, plains | Lily + Madison | Uncommon (far apart) 30/10 | Uncommon 26/9 | `/locate structure itzbreadloaf:cousins_camp` |
| Desert Bunker | desert, #is_badlands | ItzBreadLoaf | Uncommon 26/8 | Uncommon 26/8 | `/locate structure itzbreadloaf:desert_bunker` |
| End Mace Tower | end_highlands, end_midlands | Master BreadLoaf | Uncommon 24/8 | Common 20/7 | `/locate structure itzbreadloaf:end_mace_tower` |
| End Sky Platform | end_highlands, end_midlands | Master BreadLoaf | new | Uncommon 22/7 | `/locate structure itzbreadloaf:end_sky_platform` |
| Farmstead | plains, sunflower_plains, savanna | Farmer Madison + farm animals | Uncommon (far apart) 34/11 | Uncommon (far apart) 34/11 | `/locate structure itzbreadloaf:farmstead` |
| Fishing Dock | river, beach, snowy_beach | Fisherman | Common 20/6 | Common 20/6 | `/locate structure itzbreadloaf:fishing_dock` |
| Floating Island Cottage | cherry_grove | ImLilyLol | new | Common 20/7 | `/locate structure itzbreadloaf:floating_island_cottage` |
| Flower Shrine | flower_forest, cherry_grove, meadow, sunflower_plains | Gardener Lily | Uncommon (far apart) 30/10 | Uncommon (far apart) 30/10 | `/locate structure itzbreadloaf:flower_shrine` |
| Bread Fort | plains, meadow, #is_forest, #is_taiga, #is_savanna, windswept_hills, snowy_plains | 3 Knights + Captain | Uncommon (far apart) 36/10 | Uncommon (far apart) 34/11 | `/locate structure itzbreadloaf:fort` |
| Friendship Festival | plains, meadow, flower_forest, sunflower_plains, cherry_grove | ItzBreadLoaf, Lily, Madison | new | Uncommon (far apart) 30/10 | `/locate structure itzbreadloaf:friendship_festival` |
| Giant Loaf | plains, sunflower_plains, meadow | nobody (treasure) | Very rare 80/30 | Uncommon (far apart) 40/14 | `/locate structure itzbreadloaf:giant_loaf` |
| Greenhouse | flower_forest, meadow, cherry_grove, plains | Gardener Lily | Uncommon 28/9 | Uncommon 28/9 | `/locate structure itzbreadloaf:greenhouse` |
| House | plains, sunflower_plains, meadow, #is_forest, #is_taiga, #is_savanna, snowy_plains, snowy_taiga, desert | ItzBreadLoaf | Common 18/6 | Common 20/7 | `/locate structure itzbreadloaf:house` |
| Igloo Hideout | snowy_plains, snowy_taiga, ice_spikes, snowy_slopes | ItzBreadLoaf | Uncommon 24/8 | Uncommon 24/8 | `/locate structure itzbreadloaf:igloo_hideout` |
| Jungle Treehouse | #is_jungle, dark_forest | ItzBreadLoaf | Uncommon 24/8 | Uncommon 24/8 | `/locate structure itzbreadloaf:jungle_treehouse` |
| Lakeside Gazebo | birch_forest, old_growth_birch_forest, flower_forest, forest, cherry_grove | Bookworm or Lily | Uncommon 28/9 | Uncommon 28/9 | `/locate structure itzbreadloaf:lakeside_gazebo` |
| Lantern Garden | flower_forest, cherry_grove, meadow, birch_forest | Gardener Lily | new | Uncommon 26/8 | `/locate structure itzbreadloaf:lantern_garden` |
| Flower Forest Cottage | flower_forest | Lily / Gardener / Witchy | Common 14/5 | Common 16/6 | `/locate structure itzbreadloaf:lily_flower_cottage` |
| Lily's House | cherry_grove | Lily / Gardener / Witchy Lily | Common 12/4 | Common 16/5 | `/locate structure itzbreadloaf:lily_house` |
| Meadow Cottage | meadow | Lily / Gardener | Common 14/5 | Common 16/6 | `/locate structure itzbreadloaf:lily_meadow_cottage` |
| Mushroom House | mushroom_fields | Lily / Witchy | Uncommon (far apart) 32/10 | Common 20/7 | `/locate structure itzbreadloaf:lily_mushroom_house` |
| Reading Cabin | birch_forest, old_growth_birch_forest | Bookworm / Lily | Common 14/5 | Common 16/6 | `/locate structure itzbreadloaf:lily_reading_cabin` |
| Snowy Spruce Cabin | snowy_taiga | Lily / Witchy / Bookworm | Common 14/5 | Common 16/6 | `/locate structure itzbreadloaf:lily_snowy_cabin` |
| Loaf Lighthouse (my idea) | #is_beach, stony_shore | Fisherman | new | Uncommon 26/8 | `/locate structure itzbreadloaf:loaf_lighthouse` |
| Lookout Tower | #is_forest, #is_taiga, dark_forest, old_growth_pine_taiga, old_growth_spruce_taiga | Archer | Uncommon 24/8 | Uncommon 24/8 | `/locate structure itzbreadloaf:lookout_tower` |
| Madison's House | plains, meadow, sunflower_plains, flower_forest | Madison / Farmer / Beekeeper | Common 15/5 | Common 18/6 | `/locate structure itzbreadloaf:madison_house` |
| Market Stall | plains, sunflower_plains, savanna, meadow, forest | Merchant Madison | Uncommon 24/8 | Uncommon 24/8 | `/locate structure itzbreadloaf:market_stall` |
| Mining Camp | #is_overworld | Miner | Uncommon (far apart) 32/10 | Uncommon (far apart) 32/10 | `/locate structure itzbreadloaf:mining_camp` |
| Mountain Outpost | jagged_peaks, frozen_peaks, stony_peaks, snowy_slopes | Archer + Knight | new | Uncommon 24/8 | `/locate structure itzbreadloaf:mountain_outpost` |
| Nether Mace Arena | #is_nether | Master BreadLoaf | new | Uncommon 26/8 | `/locate structure itzbreadloaf:nether_mace_arena` |
| Nether Mace Training Ground | #is_nether | Master BreadLoaf | Uncommon 28/9 | Uncommon 24/8 | `/locate structure itzbreadloaf:nether_training_ground` |
| Orchard | plains, sunflower_plains, flower_forest, meadow | Beekeeper Madison | Uncommon (far apart) 30/10 | Uncommon (far apart) 30/10 | `/locate structure itzbreadloaf:orchard` |
| Pirate Ship | #is_ocean | 2-3 Pirates | Uncommon 28/8 | Uncommon 28/8 | `/locate structure itzbreadloaf:pirate_ship` |
| Pumpkin Patch Farm (id pumpkin_patch) | plains, forest, taiga, dark_forest | Farmer Madison | Uncommon 28/9 | Uncommon 28/9 | `/locate structure itzbreadloaf:pumpkin_patch` |
| PvP Arena | plains, savanna, desert, badlands | ItzBreadLoaf vs FelixSyn & Harley | Uncommon (far apart) 36/12 | Uncommon (far apart) 30/10 | `/locate structure itzbreadloaf:pvp_arena` |
| Riverside Mill | river | Madison | new | Uncommon 22/7 | `/locate structure itzbreadloaf:riverside_mill` |
| Ruined Loaf Castle | plains, sunflower_plains, #is_savanna | Captain, Knights, ItzBreadLoafs (3-5) | Rare 64/24 | Uncommon (far apart) 36/12 | `/locate structure itzbreadloaf:ruined_loaf_castle` |
| Ruined Portal Hideout | plains, forest, taiga, savanna, desert, birch_forest, dark_forest, snowy_plains | FelixSyn & Harley | new | Uncommon (far apart) 30/10 | `/locate structure itzbreadloaf:ruined_portal_hideout` |
| Shipwreck Camp (my idea) | #is_beach | 2 Pirates | new | Uncommon 28/9 | `/locate structure itzbreadloaf:shipwreck_camp` |
| Sky Arena | plains, desert, snowy_plains, #is_savanna, #is_badlands, meadow | Master BreadLoaf | Rare 64/20 | Uncommon (far apart) 40/14 | `/locate structure itzbreadloaf:sky_arena` |
| Smugglers' Cove | #is_beach, stony_shore | Pirate + Bandit | Uncommon (far apart) 30/10 | Uncommon (far apart) 30/10 | `/locate structure itzbreadloaf:smugglers_cove` |
| Snowball Fort (my idea) | snowy_plains, ice_spikes, snowy_taiga, grove | ItzBreadLoaf + Village Bread | new | Uncommon 26/8 | `/locate structure itzbreadloaf:snowball_fort` |
| Starwatch Observatory (my idea) | meadow, snowy_slopes, windswept_hills, snowy_plains | Bookworm Lily | new | Uncommon 28/9 | `/locate structure itzbreadloaf:starwatch_observatory` |
| Swamp Hut | swamp, mangrove_swamp | ItzBreadLoaf | new | Uncommon 24/8 | `/locate structure itzbreadloaf:swamp_hut` |
| Sweaty Dojo | bamboo_jungle, jungle, cherry_grove, savanna, plains | Sweaty | Uncommon (far apart) 32/10 | Uncommon (far apart) 32/10 | `/locate structure itzbreadloaf:sweaty_dojo` |
| Tent | plains, sunflower_plains, meadow, #is_forest, #is_taiga, #is_savanna, snowy_plains, snowy_taiga, desert | 2-4 ItzBreadLoafs | Uncommon 25/8 | Uncommon 25/8 | `/locate structure itzbreadloaf:tent` |
| Lily's Treehouse | cherry_grove, forest, birch_forest, flower_forest, old_growth_birch_forest, meadow | Bookworm Lily | Uncommon 28/8 | Uncommon 28/8 | `/locate structure itzbreadloaf:treehouse` |
| Treetop Library | birch_forest, old_growth_birch_forest | Bookworm Lily | new | Uncommon 22/7 | `/locate structure itzbreadloaf:treetop_library` |
| LoafCraft Village | plains, sunflower_plains, meadow, #is_forest, #is_savanna, snowy_plains | Village Breads, Archer Lily, Farmer, Fisher and Baker Madison (+40% FelixSyn & Harley) | Uncommon (far apart) 40/12 | Uncommon (far apart) 36/12 | `/locate structure itzbreadloaf:village` |
| Watchtower | plains, sunflower_plains, #is_forest, #is_taiga, #is_savanna, desert, snowy_plains, meadow | Archer | Uncommon 24/8 | Uncommon 24/8 | `/locate structure itzbreadloaf:watchtower` |
| Bread Mill (id windmill) | plains, sunflower_plains, meadow | ItzBreadLoaf + Farmer Madison | Uncommon (far apart) 32/10 | Uncommon 28/9 | `/locate structure itzbreadloaf:windmill` |
| Village Campsite | edge of vanilla villages | FelixSyn & Harley | new | 40% of villages (felixVillageChance) | walk into a village |
| Bastion Throne Room | inside bastion remnants | Master BreadLoaf | new | every bastion | `/locate structure minecraft:bastion_remnant` |
