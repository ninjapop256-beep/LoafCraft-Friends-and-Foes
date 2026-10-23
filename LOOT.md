# LoafCraft loot rarity

Every LoafCraft chest and character drop rolls from one shared set of tiers. Rare, Epic and Legendary items (maces, god apples, netherite, totems, elytras...) **only** come from tier rolls, never from a structure's own themed items.

## Tier odds per roll, by loot level

| Loot level | Rolls | Common | Uncommon | Rare | Epic | Legendary |
|---|---|---|---|---|---|---|
| 1 - Basic | 1-2 | 70.0% | 24.0% | 5.0% | 0.9% | 0.1% |
| 2 - Standard | 2-3 | 60.0% | 25.0% | 10.0% | 4.0% | 1.0% |
| 3 - Hard | 2-4 | 48.0% | 28.0% | 16.0% | 6.5% | 1.5% |
| 4 - Master BreadLoaf | 3-5 | 35.0% | 28.0% | 22.0% | 11.0% | 4.0% |

Change these in `config/itzbreadloaf.json`: `lootTierWeights` (per 1000, one row per level), `lootTierRolls`, `lootLevels` (any table's level), `lootingBonusRolls`. The item lists are the tier tables in `data/itzbreadloaf/loot_table/tiers/` (a datapack can replace them).

## Common

Bread (1-4), Loaf bread (1-2), Cooked beef (1-3), Cooked porkchop (1-3), Cooked chicken (1-3), Baked potato (1-3), Apple (1-3), Cookie (1-4), Wheat (2-8), Carrot (1-4), Potato (1-4), Wheat seeds (2-6), Stick (2-6), Torch (2-8), Coal (1-5), Arrow (2-10), String (1-4), Leather (1-3), Bone (1-3), Paper (1-4), Oak log (2-6), Spruce log (2-6), Cobblestone (4-16), Dirt (4-12)

## Uncommon

Iron ingot (1-4), Gold ingot (1-3), Emerald (1-4), Redstone (2-8), Lapis lazuli (2-8), Honey bottle (1-3), Book (1-3), Experience bottle (1-4), Name tag, Bucket, Bow, Crossbow, Shield, Iron sword, Iron pickaxe, Iron axe, Iron shovel, Leather helmet, Leather chestplate, Leather leggings, Leather boots, Chainmail helmet, Chainmail chestplate, Chainmail leggings, Chainmail boots, Harley's Blade (Sharpness II)

## Rare

Diamond (1-2), Golden apple (1-2), Ender pearl (1-3), Wind charge (2-6), Spyglass, Saddle, Breeze rod (1-2), Gold block, Ender eye, Shulker shell, Goat horn, Bee nest, Enchanted book (low level), Diamond sword, Diamond pickaxe, Diamond axe, Diamond helmet, Diamond chestplate, Diamond leggings, Diamond boots, Mace (no enchantments), Sentry armor trim smithing template, Coast armor trim smithing template, Wild armor trim smithing template, Dune armor trim smithing template

## Epic

Mace with Density III-V, Mace with Breach II-IV, Diamond sword with good enchantments, Diamond chestplate with good enchantments, Diamond pickaxe with good enchantments, Enchanted book (high level), Totem of undying, Heavy core, Ancient debris (1-2), Netherite scrap, Netherite upgrade smithing template, Heart of the sea, Music disc cat, Music disc pigstep, Music disc otherside, Music disc relic, Music disc creator

## Legendary

Enchanted golden apple, Netherite ingot, Netherite sword (enchanted), Netherite chestplate (enchanted), Netherite pickaxe, Mace with Wind Burst I-III, Master loaf trophy, Elytra (End structures only)

## Loot levels

| Table | Level |
|---|---|
| chests/badlands_saloon | 2 |
| chests/bakery | 1 |
| chests/bandit_camp | 2 |
| chests/barrel_supplies | 1 |
| chests/bastion_throne_room | 4 |
| chests/bee_sanctuary | 1 |
| chests/berry_cabin | 1 |
| chests/bread_bakery | 1 |
| chests/cliff_hut | 2 |
| chests/cliffside_base | 2 |
| chests/cousins_camp | 1 |
| chests/desert_bunker | 2 |
| chests/end_mace_tower | 4 |
| chests/end_sky_platform | 4 |
| chests/fishing_barrel | 1 |
| chests/fishing_dock | 1 |
| chests/floating_island_cottage | 2 |
| chests/flower_shrine | 1 |
| chests/fort | 3 |
| chests/friendship_festival | 1 |
| chests/giant_loaf | 3 |
| chests/greenhouse | 1 |
| chests/house | 1 |
| chests/igloo_hideout | 1 |
| chests/jungle_treehouse | 2 |
| chests/lakeside_gazebo | 1 |
| chests/lantern_garden | 1 |
| chests/lily_flower_cottage | 1 |
| chests/lily_house | 1 |
| chests/lily_meadow_cottage | 1 |
| chests/lily_mushroom_house | 1 |
| chests/lily_reading_cabin | 1 |
| chests/lily_snowy_cabin | 1 |
| chests/loaf_lighthouse | 1 |
| chests/lookout_tower | 2 |
| chests/mace_workshop | 2 |
| chests/madison_pantry | 1 |
| chests/market_stall | 1 |
| chests/mining_camp | 2 |
| chests/mountain_outpost | 2 |
| chests/nether_mace_arena | 4 |
| chests/nether_training_ground | 4 |
| chests/orchard | 1 |
| chests/pirate_ship | 2 |
| chests/pumpkin_patch | 1 |
| chests/pvp_arena | 2 |
| chests/rival_camp | 2 |
| chests/riverside_mill | 1 |
| chests/ruined_loaf_castle | 3 |
| chests/ruined_portal_hideout | 2 |
| chests/shipwreck_camp | 2 |
| chests/sky_arena | 4 |
| chests/smugglers_cove | 2 |
| chests/snowball_fort | 1 |
| chests/starwatch_observatory | 1 |
| chests/swamp_hut | 1 |
| chests/sweaty_dojo | 2 |
| chests/tent | 1 |
| chests/treehouse | 1 |
| chests/treetop_library | 1 |
| chests/village | 1 |
| chests/village_campsite | 2 |
| chests/watchtower | 2 |
| chests/wind_tower | 2 |
| chests/windmill | 1 |
| characters/itzbreadloaf | 1 |
| characters/imlilylol | 1 |
| characters/madison | 1 |
| characters/felixsyn | 2 |
| characters/harley | 2 |
| characters/master_breadloaf | 4 |

## Character drops

- **Everyone:** their own themed items (below) + tier rolls at their loot level; Looting adds bonus tier rolls; each armor piece 8.5%, their weapon 15%. Tamed characters drop their whole inventory instead (it's your gear).
- **ItzBreadLoaf:** bread, Loaf Bread, steak, arrows.
- **ImLilyLol:** flowers, cookies, paper.
- **Madison:** bread, carrots, potatoes, baked potatoes, pumpkin pie, apples.
- **FelixSyn:** porkchops, string; 25% some of his wind charges; 5% his own enchanted mace.
- **Harley:** cookies, a tulip, steak; Harley's Blade 25% when wild.
- **Master BreadLoaf:** guaranteed Wind Burst mace and Master Loaf Trophy, plus 2 extra Epic/Legendary rolls.

