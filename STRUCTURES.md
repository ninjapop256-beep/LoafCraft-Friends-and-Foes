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
| Amethyst Garden | ImLilyLol | meadow, forest, cherry_grove | Witchy Lily | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:amethyst_garden` |
| Archer Lily's Glade | ImLilyLol | forest, birch_forest, taiga, old_growth_birch_forest | Archer Lily | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:archers_glade` |
| Archery Range | ItzBreadLoaf | plains, sunflower_plains, meadow, savanna, forest | Archer | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:archery_range` |
| Badlands Mineshaft Saloon | ItzBreadLoaf | #is_badlands | Miner + ItzBreadLoaf | new | Uncommon (far apart) 31/10 | 2 Standard | - | `/locate structure itzbreadloaf:badlands_saloon` |
| Baguette Tower | ItzBreadLoaf | plains, sunflower_plains, meadow, savanna | Archer | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:baguette_tower` |
| Madison's Bakery | Madison | plains, sunflower_plains, meadow, flower_forest, forest | Baker Madison | Uncommon (far apart) 36/11 | Uncommon (far apart) 36/11 | 1 Basic | - | `/locate structure itzbreadloaf:bakery` |
| Bandit Camp | ItzBreadLoaf | #is_forest, #is_taiga, dark_forest, #is_badlands, #is_savanna | 3-4 Bandits | Uncommon (far apart) 36/11 | Uncommon (far apart) 36/11 | 2 Standard | - | `/locate structure itzbreadloaf:bandit_camp` |
| Bandit Lookout | ItzBreadLoaf | dark_forest, taiga, forest | 2 Bandits | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:bandit_lookout` |
| Basalt Watchtower | Nether | basalt_deltas, nether_wastes | Nether ItzBreadLoaf | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:basalt_watchtower` |
| Beach Snack Hut | Ocean/beach | beach | ItzBreadLoaf + Village ItzBreadLoaf | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:beach_snack_hut` |
| Beekeeper's Cottage | Madison | flower_forest, meadow, plains | Beekeeper Madison | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:beekeepers_cottage` |
| Bee Sanctuary | Madison | flower_forest, meadow, sunflower_plains, cherry_grove, plains | Beekeeper Madison | new | Uncommon (far apart) 31/10 | 1 Basic | - | `/locate structure itzbreadloaf:bee_sanctuary` |
| Sweet Berry Cabin (my idea) | Madison | taiga, snowy_taiga, old_growth_pine_taiga, old_growth_spruce_taiga | Madison | new | Uncommon (far apart) 29/10 | 1 Basic | - | `/locate structure itzbreadloaf:berry_cabin` |
| Bookbinder's Workshop | ImLilyLol | birch_forest, forest, plains | Bookworm Lily | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:bookbinders_workshop` |
| Bread Bakery | ItzBreadLoaf | plains, sunflower_plains, meadow | ItzBreadLoaf | Uncommon (far apart) 31/10 | Uncommon (far apart) 31/10 | 1 Basic | - | `/locate structure itzbreadloaf:bread_bakery` |
| Bread Bank | ItzBreadLoaf | plains, sunflower_plains, meadow, savanna | Captain (guard) | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:bread_bank` |
| ItzBreadLoaf's Bread Cellar | Underground | plains, sunflower_plains, meadow, #is_forest | ItzBreadLoaf | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:bread_cellar` |
| Portal to the Bread Dimension | Easter egg | plains, sunflower_plains, meadow | nobody | new | Very rare 70/22 | 2 Standard | - | `/locate structure itzbreadloaf:bread_portal` |
| Roadside Bread Stand | ItzBreadLoaf | plains, sunflower_plains, meadow, forest | ItzBreadLoaf | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:bread_stand` |
| Breeze Hunter's Camp | FelixSyn | plains, sunflower_plains, meadow, #is_forest, savanna, taiga | Wild FelixSyn (Harley sometimes) | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:breeze_hunter_camp` |
| The Buried Library | Underground | plains, sunflower_plains, meadow, #is_forest, birch_forest | Bookworm Lily | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:buried_library` |
| Captain's Manor | ItzBreadLoaf | plains, sunflower_plains, meadow, forest, birch_forest | Captain + Knight | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:captains_manor` |
| The Cartographer's Tent | Lore | plains, sunflower_plains, meadow, savanna, taiga | nobody | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:cartographers_tent` |
| Cherry Tea House | ImLilyLol | cherry_grove | ImLilyLol | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:cherry_tea_house` |
| Chicken Coop Farm | Madison | plains, sunflower_plains, meadow, savanna | Farmer Madison + chickens | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:chicken_coop` |
| Chorus Garden | End | end_highlands, end_midlands | Gardener Lily | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:chorus_garden` |
| Cliffside Cave Base | ItzBreadLoaf | windswept_hills, windswept_gravelly_hills, windswept_forest, meadow, stony_shore | ItzBreadLoaf | new | Uncommon (far apart) 31/10 | 2 Standard | - | `/locate structure itzbreadloaf:cliffside_base` |
| Cliff Hut | FelixSyn | windswept_hills, windswept_gravelly_hills, windswept_forest, stony_peaks, jagged_peaks, meadow | Wild FelixSyn (30% Harley) | new | Uncommon (far apart) 29/10 | 2 Standard | - | `/locate structure itzbreadloaf:cliff_hut` |
| Treehouse Clubhouse | Shared | #is_forest, birch_forest | ItzBreadLoaf, Lily, Madison | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:clubhouse_tree` |
| Community Garden | Shared | plains, sunflower_plains, meadow, flower_forest | Gardener Lily, Farmer Madison, ItzBreadLoaf | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:community_garden` |
| Cookie Cottage | Madison | plains, sunflower_plains, meadow, forest, sparse_jungle | Baker Madison | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:cookie_cottage` |
| Cousins' Camp | Shared | forest, birch_forest, flower_forest, meadow, cherry_grove, plains | Lily + Madison | Uncommon (far apart) 31/11 | Uncommon (far apart) 31/11 | 1 Basic | - | `/locate structure itzbreadloaf:cousins_camp` |
| The Cousins' Old Treehouse | Lore | #is_forest, birch_forest | nobody | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:cousins_old_treehouse` |
| Crimson Outpost | Nether | crimson_forest, nether_wastes | 2 Nether ItzBreadLoafs | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:crimson_outpost` |
| The Crossroads Signpost | Lore | plains, sunflower_plains, meadow, savanna | nobody | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:crossroads_signpost` |
| Crumb Trail Camp | ItzBreadLoaf | #is_forest, taiga | 2 ItzBreadLoafs | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:crumb_trail_camp` |
| Dairy Barn | Madison | plains, sunflower_plains, meadow | Farmer Madison + cows | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:dairy_barn` |
| The Deep Forge | Underground | plains, sunflower_plains, meadow, windswept_hills, stony_peaks, taiga | Miner | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:deep_forge` |
| Desert Bunker | ItzBreadLoaf | desert, #is_badlands | ItzBreadLoaf | Uncommon (far apart) 31/10 | Uncommon (far apart) 31/10 | 2 Standard | - | `/locate structure itzbreadloaf:desert_bunker` |
| Desert Oasis Camp | ItzBreadLoaf | desert | ItzBreadLoaf + Village ItzBreadLoaf | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:desert_oasis_camp` |
| The Developer's Desk | Easter egg | plains, sunflower_plains, meadow, forest | nobody | new | Very rare 76/24 | 1 Basic | - | `/locate structure itzbreadloaf:developers_desk` |
| Dragon Lookout | End | end_highlands, end_midlands | nobody | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:dragon_lookout` |
| The Drifters' Rest | Lore | plains, sunflower_plains, meadow, #is_forest, savanna | nobody | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:drifters_rest` |
| FelixSyn & Harley's Cabin | FelixSyn | #is_forest, taiga | FelixSyn (Harley sometimes) | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:duo_cabin` |
| End Mace Tower | End | end_highlands, end_midlands | Master BreadLoaf | Common 16/4 | Common 16/4 | 4 Master | - | `/locate structure itzbreadloaf:end_mace_tower` |
| End Sky Platform | End | end_highlands, end_midlands | Master BreadLoaf | new | Common 16/4 | 4 Master | - | `/locate structure itzbreadloaf:end_sky_platform` |
| Fairy Ring | ImLilyLol | dark_forest, old_growth_spruce_taiga, forest | Witchy Lily | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:fairy_ring` |
| Farmers' Market | Madison | plains, sunflower_plains, meadow | Merchant + Farmer Madison | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:farmers_market` |
| Farmstead | Madison | plains, sunflower_plains, savanna | Farmer Madison + farm animals | Rare 41/13 | Rare 41/13 | 1 Basic | - | `/locate structure itzbreadloaf:farmstead` |
| FelixSyn's Secret Bunker | Underground | plains, sunflower_plains, meadow, savanna, forest | Wild FelixSyn (Harley sometimes) | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:felix_bunker` |
| FelixSyn's Trophy Hall | FelixSyn | plains, sunflower_plains, meadow | Wild FelixSyn (Harley sometimes) | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:felix_trophy_hall` |
| River Ferry Landing | Ocean/beach | river | Fisher Madison | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:ferry_landing` |
| Ruins of the First Bakery | Lore | plains, sunflower_plains, meadow | nobody | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:first_bakery_ruins` |
| Fishermen's Wharf | Ocean/beach | beach, river, stony_shore | Fisher Madison + Fisherman | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:fishermens_wharf` |
| Fishing Dock | ItzBreadLoaf | river, beach, snowy_beach | Fisherman | Uncommon 24/7 | Uncommon 24/7 | 1 Basic | - | `/locate structure itzbreadloaf:fishing_dock` |
| Fish Smokehouse | Madison | taiga, forest, birch_forest | Fisher Madison | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:fish_smokehouse` |
| Floating Island Cottage | ImLilyLol | cherry_grove | ImLilyLol | new | Uncommon 24/8 | 2 Standard | - | `/locate structure itzbreadloaf:floating_island_cottage` |
| Flower Shrine | ImLilyLol | flower_forest, cherry_grove, meadow, sunflower_plains | Gardener Lily | Uncommon (far apart) 36/12 | Uncommon (far apart) 36/12 | 1 Basic | - | `/locate structure itzbreadloaf:flower_shrine` |
| Pressed Flower Studio | ImLilyLol | meadow, cherry_grove, flower_forest | ImLilyLol | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:flower_studio` |
| Bread Fort | ItzBreadLoaf | plains, meadow, #is_forest, #is_taiga, #is_savanna, windswept_hills, snowy_plains | 3 Knights + Captain | Rare 41/13 | Rare 41/13 | 3 Hard | - | `/locate structure itzbreadloaf:fort` |
| Friendship Festival | Shared | plains, meadow, flower_forest, sunflower_plains, cherry_grove | ItzBreadLoaf, Lily, Madison | new | Uncommon (far apart) 36/12 | 1 Basic | - | `/locate structure itzbreadloaf:friendship_festival` |
| Friends' Picnic | Shared | plains, sunflower_plains, meadow, flower_forest | ItzBreadLoaf, Lily, Madison | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:friends_picnic` |
| Giant Loaf | Easter egg | plains, sunflower_plains, meadow | nobody (treasure) | Rare 48/17 | Rare 48/17 | 3 Hard | - | `/locate structure itzbreadloaf:giant_loaf` |
| The Giant Mace | Easter egg | plains, sunflower_plains, meadow, savanna, windswept_hills | nobody | new | Very rare 80/24 | 3 Hard | - | `/locate structure itzbreadloaf:giant_mace` |
| Gold Trading Post | Nether | nether_wastes, crimson_forest, basalt_deltas | Nether ItzBreadLoaf | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:gold_trading_post` |
| Grain Silo | Madison | plains, sunflower_plains, meadow, savanna | Farmer Madison | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:grain_silo` |
| Greenhouse | ImLilyLol | flower_forest, meadow, cherry_grove, plains | Gardener Lily | Uncommon (far apart) 34/11 | Uncommon (far apart) 34/11 | 1 Basic | - | `/locate structure itzbreadloaf:greenhouse` |
| Harley's Training Grove | FelixSyn | flower_forest, forest, birch_forest, cherry_grove | Harley | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:harleys_training_grove` |
| Harvest Fair | Shared | plains, sunflower_plains, meadow, forest, taiga | Madison, Farmer Madison, ItzBreadLoaf | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:harvest_fair` |
| Hay Bale Maze | ItzBreadLoaf | plains, sunflower_plains, meadow, savanna | ItzBreadLoaf | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:hay_maze` |
| Hellfire Bakery | Nether | nether_wastes, crimson_forest | Nether ItzBreadLoaf | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:hellfire_bakery` |
| Herbalist's Stilt Hut | ImLilyLol | swamp, mangrove_swamp | Witchy Lily | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:herbalist_hut` |
| Horse Stables | Madison | plains, sunflower_plains, meadow, savanna | Farmer Madison + horses | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:horse_stables` |
| House | ItzBreadLoaf | plains, sunflower_plains, meadow, #is_forest, #is_taiga, #is_savanna, snowy_plains, snowy_taiga, desert | ItzBreadLoaf | Uncommon 24/8 | Uncommon 24/8 | 1 Basic | - | `/locate structure itzbreadloaf:house` |
| Igloo Hideout | ItzBreadLoaf | snowy_plains, snowy_taiga, ice_spikes, snowy_slopes | ItzBreadLoaf | Uncommon (far apart) 29/10 | Uncommon (far apart) 29/10 | 1 Basic | - | `/locate structure itzbreadloaf:igloo_hideout` |
| Jungle Treehouse | ItzBreadLoaf | #is_jungle, dark_forest | ItzBreadLoaf | Uncommon (far apart) 29/10 | Uncommon (far apart) 29/10 | 2 Standard | - | `/locate structure itzbreadloaf:jungle_treehouse` |
| Knight Training Yard | ItzBreadLoaf | plains, sunflower_plains, meadow, forest | 2 Knights | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:knight_training_yard` |
| Lakeside Gazebo | ImLilyLol | birch_forest, old_growth_birch_forest, flower_forest, forest, cherry_grove | Bookworm or Lily | Uncommon (far apart) 34/11 | Uncommon (far apart) 34/11 | 1 Basic | - | `/locate structure itzbreadloaf:lakeside_gazebo` |
| Lantern Garden | ImLilyLol | flower_forest, cherry_grove, meadow, birch_forest | Gardener Lily | new | Uncommon (far apart) 31/10 | 1 Basic | - | `/locate structure itzbreadloaf:lantern_garden` |
| The Last Loaf | End | end_highlands, end_midlands | Master BreadLoaf | new | Rare 60/20 | 4 Master | - | `/locate structure itzbreadloaf:last_loaf_shrine` |
| Lava Fishing Spot | Nether | nether_wastes, basalt_deltas | Nether ItzBreadLoaf | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:lava_fishing_spot` |
| Lifeguard Tower | Ocean/beach | beach | Fisherman | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:lifeguard_tower` |
| Flower Forest Cottage | ImLilyLol | flower_forest | Lily / Gardener / Witchy | Common 19/7 | Common 19/7 | 1 Basic | - | `/locate structure itzbreadloaf:lily_flower_cottage` |
| Lily's House | ImLilyLol | cherry_grove | Lily / Gardener / Witchy Lily | Common 19/6 | Common 19/6 | 1 Basic | - | `/locate structure itzbreadloaf:lily_house` |
| Lily's Letter Post | ImLilyLol | plains, sunflower_plains, meadow, forest | ImLilyLol | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:lily_letter_post` |
| Meadow Cottage | ImLilyLol | meadow | Lily / Gardener | Common 19/7 | Common 19/7 | 1 Basic | - | `/locate structure itzbreadloaf:lily_meadow_cottage` |
| Mushroom House | ImLilyLol | mushroom_fields | Lily / Witchy | Uncommon 24/8 | Uncommon 24/8 | 1 Basic | - | `/locate structure itzbreadloaf:lily_mushroom_house` |
| Reading Cabin | ImLilyLol | birch_forest, old_growth_birch_forest | Bookworm / Lily | Common 19/7 | Common 19/7 | 1 Basic | - | `/locate structure itzbreadloaf:lily_reading_cabin` |
| Snowy Spruce Cabin | ImLilyLol | snowy_taiga | Lily / Witchy / Bookworm | Common 19/7 | Common 19/7 | 1 Basic | - | `/locate structure itzbreadloaf:lily_snowy_cabin` |
| Lily's Abandoned Writing Cabin | Lore | birch_forest, forest, meadow | nobody | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:lily_writing_cabin` |
| Loaf Lighthouse (my idea) | ItzBreadLoaf | #is_beach, stony_shore | Fisherman | new | Uncommon (far apart) 31/10 | 1 Basic | - | `/locate structure itzbreadloaf:loaf_lighthouse` |
| Statue of ItzBreadLoaf | ItzBreadLoaf | plains, sunflower_plains, meadow | Village ItzBreadLoaf (caretaker) | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:loaf_statue` |
| Lookout Tower | ItzBreadLoaf | #is_forest, #is_taiga, dark_forest, old_growth_pine_taiga, old_growth_spruce_taiga | Archer | Uncommon (far apart) 29/10 | Uncommon (far apart) 29/10 | 2 Standard | - | `/locate structure itzbreadloaf:lookout_tower` |
| The Lost Loaf Wagon | Lore | plains, sunflower_plains, meadow, savanna | nobody | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:lost_loaf_wagon` |
| Mace Drop Tower | FelixSyn | plains, sunflower_plains, meadow, savanna, windswept_hills | Wild FelixSyn (Harley sometimes) | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:mace_drop_tower` |
| Mace Workshop | FelixSyn | plains, sunflower_plains, savanna, savanna_plateau | Wild FelixSyn (30% Harley) | new | Uncommon (far apart) 34/11 | 2 Standard | - | `/locate structure itzbreadloaf:mace_workshop` |
| Madison's House | Madison | plains, meadow, sunflower_plains, flower_forest | Madison / Farmer / Beekeeper | Uncommon 22/7 | Uncommon 22/7 | 1 Basic | - | `/locate structure itzbreadloaf:madison_house` |
| Market Stall | Madison | plains, sunflower_plains, savanna, meadow, forest | Merchant Madison | Uncommon (far apart) 29/10 | Uncommon (far apart) 29/10 | 1 Basic | - | `/locate structure itzbreadloaf:market_stall` |
| Master's Departure Point | Lore | plains, sunflower_plains, meadow, windswept_hills | nobody | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:masters_departure` |
| Master BreadLoaf's Old Home | Lore | plains, sunflower_plains, meadow, forest | nobody | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:masters_old_home` |
| Melon & Berry Farm | Madison | plains, sunflower_plains, meadow, savanna | Farmer Madison | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:melon_farm` |
| The Memorial Grove | Lore | plains, sunflower_plains, meadow, #is_forest | nobody | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:memorial_grove` |
| Merchant Caravan | Madison | savanna, desert, plains | Merchant Madison | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:merchant_caravan` |
| Miner's Rest Stop | ItzBreadLoaf | windswept_hills, windswept_forest, stony_peaks, meadow, taiga | Miner | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:miners_rest` |
| Miner's Shaft | Underground | plains, sunflower_plains, meadow, windswept_hills, taiga, savanna | Miner | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:miners_shaft` |
| Mining Camp | ItzBreadLoaf | #is_overworld | Miner | Uncommon (far apart) 38/12 | Uncommon (far apart) 38/12 | 2 Standard | - | `/locate structure itzbreadloaf:mining_camp` |
| Mini Golf Course | Shared | plains, sunflower_plains, meadow | ItzBreadLoaf, Madison | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:mini_golf` |
| Moonlit Reading Nook | ImLilyLol | birch_forest, old_growth_birch_forest, forest, meadow | Bookworm Lily | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:moonlit_reading_nook` |
| Mountain Outpost | ItzBreadLoaf | jagged_peaks, frozen_peaks, stony_peaks, snowy_slopes | Archer + Knight | new | Uncommon (far apart) 29/10 | 2 Standard | - | `/locate structure itzbreadloaf:mountain_outpost` |
| Movie Night Screen | Shared | plains, sunflower_plains, meadow | ItzBreadLoaf, Lily, Madison | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:movie_night` |
| Glowing Mushroom Grotto | Underground | #is_forest, dark_forest, taiga, mushroom_fields | Gardener Lily | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:mushroom_grotto` |
| Lily's Naturalist Camp | Lore | #is_forest, taiga, meadow | nobody | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:naturalist_camp` |
| Nether Mace Arena | Nether | #is_nether | Master BreadLoaf | new | Common 16/5 | 4 Master | - | `/locate structure itzbreadloaf:nether_mace_arena` |
| Nether Mace Training Ground | Nether | #is_nether | Master BreadLoaf | Common 16/5 | Common 16/5 | 4 Master | - | `/locate structure itzbreadloaf:nether_training_ground` |
| Witchy Lily's Nether Garden | Nether | soul_sand_valley, nether_wastes, warped_forest | Witchy Lily | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:nether_wart_garden` |
| Obstacle Course | Shared | plains, sunflower_plains, meadow, savanna | ItzBreadLoaf, Lily | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:obstacle_course` |
| The Archive Under the Old Loaf | Lore | plains, sunflower_plains, meadow | nobody | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:old_loaf_archive` |
| Orchard | Madison | plains, sunflower_plains, flower_forest, meadow | Beekeeper Madison | Uncommon (far apart) 36/12 | Uncommon (far apart) 36/12 | 1 Basic | - | `/locate structure itzbreadloaf:orchard` |
| FelixSyn's Parkour Tower | FelixSyn | plains, sunflower_plains, meadow, savanna, desert, badlands | Wild FelixSyn (Harley sometimes) | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:parkour_tower` |
| Pearl Diver's Raft | Ocean/beach | warm_ocean, lukewarm_ocean, deep_lukewarm_ocean | Fisherman | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:pearl_diver_raft` |
| Madison's Pie Shop | Madison | plains, sunflower_plains, meadow, forest | Baker Madison | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:pie_shop` |
| Pirate Ship | ItzBreadLoaf | #is_ocean | 2-3 Pirates | Uncommon (far apart) 34/10 | Uncommon (far apart) 34/10 | 2 Standard | - | `/locate structure itzbreadloaf:pirate_ship` |
| Pumpkin Patch Farm (id pumpkin_patch) | Madison | plains, forest, taiga, dark_forest | Farmer Madison | Uncommon (far apart) 34/11 | Uncommon (far apart) 34/11 | 1 Basic | - | `/locate structure itzbreadloaf:pumpkin_patch` |
| Purpur Vault | End | end_highlands, end_midlands | nobody | new | Rare 60/20 | 4 Master | - | `/locate structure itzbreadloaf:purpur_vault` |
| PvP Arena | Shared | plains, savanna, desert, badlands | ItzBreadLoaf vs FelixSyn & Harley | Uncommon (far apart) 36/12 | Uncommon (far apart) 36/12 | 2 Standard | - | `/locate structure itzbreadloaf:pvp_arena` |
| The Old Battle Site | Lore | plains, sunflower_plains, meadow, savanna | nobody | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:rivalry_battle_site` |
| The Rivalry Wall | FelixSyn | plains, sunflower_plains, meadow, savanna | Wild FelixSyn (Harley sometimes) | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:rivalry_wall` |
| Rival Camp (near ItzBreadLoaf houses) | FelixSyn | plains, sunflower_plains, meadow, forest, birch_forest, taiga, snowy_taiga, savanna, snowy_plains, desert | Wild FelixSyn (30% Harley) | new | Uncommon (far apart) 29/10 | 2 Standard | - | `/locate structure itzbreadloaf:rival_camp` |
| Riverside Mill | Madison | river | Madison | new | Uncommon 26/8 | 1 Basic | - | `/locate structure itzbreadloaf:riverside_mill` |
| Madison's Root Cellar | Underground | plains, sunflower_plains, meadow, taiga, forest | Farmer Madison | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:root_cellar` |
| Rose Arch Garden | ImLilyLol | flower_forest, meadow, sunflower_plains | Gardener Lily | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:rose_arch_garden` |
| Ruined Loaf Castle | ItzBreadLoaf | plains, sunflower_plains, #is_savanna | Captain, Knights, ItzBreadLoafs (3-5) | Rare 43/14 | Rare 43/14 | 3 Hard | - | `/locate structure itzbreadloaf:ruined_loaf_castle` |
| Ruined Portal Hideout | FelixSyn | plains, forest, taiga, savanna, desert, birch_forest, dark_forest, snowy_plains | FelixSyn & Harley | new | Uncommon (far apart) 36/12 | 2 Standard | - | `/locate structure itzbreadloaf:ruined_portal_hideout` |
| The Sealed Library | Lore | plains, sunflower_plains, meadow, #is_forest | nobody | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:sealed_library` |
| Sea Glass Cove | Ocean/beach | beach, stony_shore | ImLilyLol | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:sea_glass_cove` |
| Witchy Lily's Secret Lab | Underground | #is_forest, swamp, dark_forest | Witchy Lily | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:secret_lab` |
| Sheep Ranch | Madison | meadow, plains, windswept_hills | Farmer Madison + sheep | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:sheep_ranch` |
| Shipwreck Camp (my idea) | Ocean/beach | #is_beach | 2 Pirates | new | Uncommon (far apart) 34/11 | 2 Standard | - | `/locate structure itzbreadloaf:shipwreck_camp` |
| Sky Arena | Master BreadLoaf | plains, desert, snowy_plains, #is_savanna, #is_badlands, meadow | Master BreadLoaf | Rare 48/17 | Rare 48/17 | 4 Master | - | `/locate structure itzbreadloaf:sky_arena` |
| Smash Crater | FelixSyn | plains, sunflower_plains, meadow, savanna, windswept_hills | Wild FelixSyn (Harley sometimes) | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:smash_crater` |
| Smugglers' Cove | Ocean/beach | #is_beach, stony_shore | Pirate + Bandit | Uncommon (far apart) 36/12 | Uncommon (far apart) 36/12 | 2 Standard | - | `/locate structure itzbreadloaf:smugglers_cove` |
| Snowball Fort (my idea) | ItzBreadLoaf | snowy_plains, ice_spikes, snowy_taiga, grove | ItzBreadLoaf + Village Bread | new | Uncommon (far apart) 31/10 | 1 Basic | - | `/locate structure itzbreadloaf:snowball_fort` |
| Snowy Bakehouse | ItzBreadLoaf | snowy_plains, snowy_taiga, grove, snowy_slopes | ItzBreadLoaf | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:snowy_bakehouse` |
| Soul Camp | Nether | soul_sand_valley | 2 Nether ItzBreadLoafs | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:soul_camp` |
| Sparring Ring | FelixSyn | plains, sunflower_plains, meadow, savanna, forest | Wild FelixSyn (Harley sometimes) | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:sparring_ring` |
| Starwatch Observatory (my idea) | ImLilyLol | meadow, snowy_slopes, windswept_hills, snowy_plains | Bookworm Lily | new | Uncommon (far apart) 34/11 | 1 Basic | - | `/locate structure itzbreadloaf:starwatch_observatory` |
| Campfire Story Circle | Shared | #is_forest, taiga | ItzBreadLoaf, Lily, Madison | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:story_circle` |
| Strider Stables | Nether | nether_wastes, basalt_deltas, crimson_forest | Nether ItzBreadLoaf + striders | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:strider_stables` |
| Swamp Hut | ItzBreadLoaf | swamp, mangrove_swamp | ItzBreadLoaf | new | Uncommon (far apart) 29/10 | 1 Basic | - | `/locate structure itzbreadloaf:swamp_hut` |
| Sweaty Dojo | ItzBreadLoaf | bamboo_jungle, jungle, cherry_grove, savanna, plains | Sweaty | Uncommon (far apart) 38/12 | Uncommon (far apart) 38/12 | 2 Standard | - | `/locate structure itzbreadloaf:sweaty_dojo` |
| Sweaty Gym | ItzBreadLoaf | plains, sunflower_plains, meadow, desert, savanna | Sweaty ItzBreadLoaf | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:sweaty_gym` |
| Tent | ItzBreadLoaf | plains, sunflower_plains, meadow, #is_forest, #is_taiga, #is_savanna, snowy_plains, snowy_taiga, desert | 2-4 ItzBreadLoafs | Uncommon (far apart) 30/10 | Uncommon (far apart) 30/10 | 1 Basic | - | `/locate structure itzbreadloaf:tent` |
| The Tiny House | Easter egg | plains, sunflower_plains, meadow, #is_forest | nobody | new | Rare 64/20 | 1 Basic | - | `/locate structure itzbreadloaf:tiny_house` |
| Toaster House | ItzBreadLoaf | plains, sunflower_plains, meadow, #is_forest | ItzBreadLoaf | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:toaster_house` |
| Lily's Treehouse | ImLilyLol | cherry_grove, forest, birch_forest, flower_forest, old_growth_birch_forest, meadow | Bookworm Lily | Uncommon (far apart) 34/10 | Uncommon (far apart) 34/10 | 1 Basic | - | `/locate structure itzbreadloaf:treehouse` |
| Treetop Library | ImLilyLol | birch_forest, old_growth_birch_forest | Bookworm Lily | new | Uncommon 26/8 | 1 Basic | - | `/locate structure itzbreadloaf:treetop_library` |
| The Truce Pavilion | Shared | plains, sunflower_plains, meadow | Lily, Madison, FelixSyn (Harley sometimes) | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:truce_pavilion` |
| Tulip Terrace | ImLilyLol | flower_forest, meadow, plains | Gardener Lily | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:tulip_terrace` |
| The Upside-Down House | Easter egg | plains, sunflower_plains, meadow, forest | nobody | new | Very rare 70/22 | 2 Standard | - | `/locate structure itzbreadloaf:upside_down_house` |
| LoafCraft Village | Shared | plains, sunflower_plains, meadow, #is_forest, #is_savanna, snowy_plains | Village Breads, Archer Lily, Farmer, Fisher and Baker Madison (+40% FelixSyn & Harley) | Rare 43/14 | Rare 43/14 | 1 Basic | - | `/locate structure itzbreadloaf:village` |
| Void Fishing Platform | End | end_highlands, end_midlands | Fisherman | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:void_fishing_platform` |
| Void Observatory | End | end_highlands, end_midlands | Bookworm Lily | new | Rare 52/16 | 3 Hard | - | `/locate structure itzbreadloaf:void_observatory` |
| Watchtower | ItzBreadLoaf | plains, sunflower_plains, #is_forest, #is_taiga, #is_savanna, desert, snowy_plains, meadow | Archer | Uncommon (far apart) 29/10 | Uncommon (far apart) 29/10 | 2 Standard | - | `/locate structure itzbreadloaf:watchtower` |
| The Whispering Stones | Lore | #is_forest, taiga, dark_forest | nobody | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:whispering_stones` |
| Willow Swing Pond | ImLilyLol | forest, flower_forest, birch_forest, plains, meadow | ImLilyLol | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:willow_swing` |
| Bread Mill (id windmill) | ItzBreadLoaf | plains, sunflower_plains, meadow | ItzBreadLoaf + Farmer Madison | Uncommon (far apart) 34/11 | Uncommon (far apart) 34/11 | 1 Basic | - | `/locate structure itzbreadloaf:windmill` |
| Wind Charge Launch Pad | FelixSyn | plains, sunflower_plains, meadow, savanna | Wild FelixSyn (Harley sometimes) | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:wind_launch_pad` |
| Wind Tower | FelixSyn | plains, sunflower_plains, savanna, meadow, snowy_plains, desert, badlands | Wild FelixSyn (30% Harley) | new | Uncommon (far apart) 36/12 | 2 Standard | - | `/locate structure itzbreadloaf:wind_tower` |
| Winter Dome Garden | ImLilyLol | snowy_plains, snowy_taiga, grove, snowy_slopes | Gardener Lily | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:winter_dome_garden` |
| Friends' Winter Lodge | Shared | snowy_plains, snowy_taiga, grove, snowy_slopes | ItzBreadLoaf, Lily, Madison | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:winter_lodge` |
| Stargazing Hill | ImLilyLol | meadow, plains, snowy_plains | Bookworm Lily | new | Uncommon (far apart) 40/12 | 1 Basic | - | `/locate structure itzbreadloaf:wishing_hill` |
| Witchy Potion Shop | ImLilyLol | swamp, dark_forest, forest | Witchy Lily | new | Rare 44/14 | 2 Standard | - | `/locate structure itzbreadloaf:witchy_potion_shop` |
| Village Campsite | FelixSyn | edge of vanilla villages | FelixSyn & Harley | new | 40% of villages (felixVillageChance) | 2 Standard | - | walk into a village |
| Bastion Throne Room | Nether | inside bastion remnants | Master BreadLoaf | new | every bastion | 4 Master | - | `/locate structure minecraft:bastion_remnant` |
