# Structure remake (update 2, part 7)

Every old structure is rebuilt on the blueprint system with the **same id, rarity, salt and biomes**, so existing
worlds keep finding them in the same places and pieces that already generated still load.

## Problems every old structure had

| Old problem | Fix in the remake |
|---|---|
| Built chunk by chunk with each chunk's own random, so a building crossing a chunk border could get mismatched parts | The whole layout is drawn once from the structure's own seed, then placed |
| Terrain adaptation was only `beard_thin`: corners could float over slopes or sink into hills | New leveling: clears inside the footprint, lays a ground layer, fills foundations down to solid ground (up to 16 blocks), slopes a 3-block skirt around it |
| Nothing checked that floors, chests, beds and spawn spots could be reached | Every layout must pass the reachability check (`./gradlew checkStructures`, 12 test builds each, different seeds and biomes) |
| Posts over cliffs or the void could run very deep | Foundations stop at 16 blocks, posts and stilts at 32 |

Wood and stone come from the biome palette (18 families), so the same building looks different in a taiga, desert,
jungle or snowy biome.

## Batch 1: ItzBreadLoaf's places (26)

Sizes are width x depth x height.

| Structure | Old | What was wrong | What changed (new) |
|---|---|---|---|
| House | 7x7x7 cabin | Shared problems only | 9x9x12: gable-roofed cottage with windows, a bed for the resident, chest, crafting table, furnace, table and chair, a path and flower beds out front |
| Tent | 9x10x6 | One block tall, so you couldn't walk in | 11x11x7: walk-in tent 3 tall with an aisle, 2 beds, chest, barrel and lantern; campfire with log benches beside the doorway; 2-4 residents, two with beds |
| Bread Fort | 19x19x9 | Shared problems only | 21x21x14: crenellated walls, four corner towers with ladders to crenellated tops and doors into the courtyard, barracks with a bed for every Knight and the Captain, treasure room (2 chests), armory (anvil, grindstone, barrel), training dummies, lamp posts |
| Watchtower | 7x7x20 wooden tower | Shared problems only | 7x7x22: stone tower with a ladder all the way up through every floor and a walled lookout for the Archer |
| Bandit Camp | 15x15x6 | Shared problems only | 15x15x7: dark walk-in tents around a campfire, a pile of stolen goods, beds; the Bandits defend each other |
| Bread Bakery | 11x11x8 | Shared problems only | 11x11x12: ovens, a counter, flour and a brick chimney |
| Mining Camp | 15x13x7 | Shared problems only | 13x13x8: tent and campfire beside a log-framed shaft with a ladder down to a lit mine room with rails |
| Jungle Treehouse | 13x13x18 | Shared problems only | 13x13x24: hut on a platform around a big jungle trunk, ladder up the trunk onto a railed balcony, canopy and vines |
| Desert Bunker | 11x13x6 | Shared problems only | 11x11x6: sandstone hut over stairs down to a hidden room; the arrow trap is a dispenser and pressure plate loaded from a new arrows-only table (`traps/arrows`), so it can't hand out chest loot |
| Igloo Hideout | 9x9x6 | Shared problems only | 9x9x7: snow dome with a hidden trapdoor behind the furnace, ladder down to a stone basement |
| Ruined Loaf Castle | 27x27x14 | Shared problems only | 27x27x16: crumbling walls and towers, two-floor keep with a great hall (long table, carpet, throne at the far end), stairs up to a treasury |
| Sweaty Dojo | 13x13x7 | Shared problems only | 13x13x10: training hall with targets, dummies, an anvil and a weapon chest |
| Bread Mill (`windmill`) | 15x15x17 | Shared problems only | 15x15x18: stone mill floor, wooden tower with a ladder up to the sail room, sails, wheat farm |
| Swamp Hut | 11x11x10 | Shared problems only | 11x11x12: shack on stilts (posts down to the swamp floor) with a porch, a ladder up and a cauldron; builds on land or water |
| Mountain Outpost | 9x9x12 | Shared problems only | 9x9x14: two-floor stone lookout with a crenellated roof and a campfire |
| Badlands Mineshaft Saloon | 15x13x9 | Shared problems only | 15x13x10: saloon with a bar, stools and tables; ladder down a mine shaft under the back room |
| Cliffside Cave Base | 13x13x8 | Shared problems only | 13x13x4: lit stone room underground, reached through a trapdoor hidden in a bush |
| Snowball Fort | 19x13x4 | Shared problems only | 19x13x5: two snow forts facing each other, a snowman between them, snowball stashes |
| Lookout Tower | 7x7x22 | No way up to the top | 7x7x23 (fixed in part 6): ladder through a hole in every floor, fenced lookout under a hip roof, windows and a lantern on every floor |
| Pirate Ship | 9x21x12 | Shared problems only | 9x23x15: floats on the ocean, captain's cabin, hold below deck, mast with sails and a crow's nest |
| Fishing Dock | 7x15x6 | Shared problems only | 7x17x7: shore shack and a dock on posts over the water, with lanterns; builds on land or water |
| Loaf Lighthouse | 9x9x22 | Shared problems only | 9x9x23: striped tower with a ladder up to a glowing lamp room and a railed gallery |
| Smugglers' Cove | 13x15x7 | Shared problems only | 13x17x8: beach shack full of crates on log posts down to the sand or water, and a little dock |
| Shipwreck Camp | 17x11x8 | Shared problems only | 17x11x8: Pirates camping beside the broken half of their ship, campfire with benches |
| Village Campsite (runtime) | 11x9x5 patch | Beds, chest and campfire out in the open, no shelter | 13x11x7: walk-in tent with FelixSyn's and Harley's beds, chest and barrel, campfire with benches, two training dummies, a target, a ladder-up jump platform over hay bales for mace practice |
| Bastion Throne Room (runtime) | 11x11x8 | Shared problems only | 13x13x9: closed blackstone hall (no leveling inside the bastion, so nothing outside gets changed), gilded aisle and carpet, gold trim, pillars with soul lanterns, throne with gold arms, tall back and crown, a chest each side |

## Batch 2: ImLilyLol's and Madison's places (23)

| Structure | Old | What was wrong | What changed (new) |
|---|---|---|---|
| Lily's House | 9x11x7 cherry house | Shared problems only | 11x13x9: cherry cottage with a tall gable roof, bed, chest, bookshelves and a lectern, table and chairs, a yard of pink petals and flowering azaleas |
| Meadow Cottage | 9x11x7 | Shared problems only | 11x11x8: cottage with stone along the bottom of the walls, in a fenced flower garden with a gate and a beehive |
| Flower Forest Cottage | 9x11x7 | Shared problems only | 11x11x9: mossy walls, vines on every side, a hip roof, flowers all around, a potion corner (brewing stand, cauldron) |
| Reading Cabin | 9x11x7 | Shared problems only | 11x11x10: bookshelves along two walls, a lectern and reading table, and a loft for Lily's bed up a ladder |
| Snowy Spruce Cabin | 9x11x8 | Shared problems only | 11x11x10: stone fireplace with a real campfire and a chimney up past the roof, a rug, kitchen corner, lamp posts and a woodpile |
| Mushroom House | 11x11x9 | Shared problems only | 13x13x9: a round mushroom-stem room under a big red cap, windows, bed, chest, brewing stand, little mushrooms all around |
| Lily's Treehouse | 13x13x16 | Shared problems only | 15x15x20: a 3x3 trunk with stairs winding up around it into a library room, a railed stairwell, a balcony all around and a leafy canopy on the roof |
| Greenhouse | 9x11x7 | Shared problems only | 11x13x8: glass walls and a raised glass roof, flower beds either side of a path with water, potted saplings (they can't grow and break the glass), a composter |
| Lakeside Gazebo | 13x13x7 | Shared problems only | 15x13x9: hip-roofed gazebo with benches facing a pond with lily pads, reeds and a small dock |
| Flower Shrine | 9x9x7 | Shared problems only | 11x11x7: stone shrine with a lit candle altar and lanterns, a ring of flowers, four paths and lamp posts |
| Floating Island Cottage | 15x15x30, ladder pillar | Shared problems only | 15x15x28: island with a stone-and-dirt underside and hanging roots, cottage, cherry tree and petals on top; spiral stairs up a cherry post replace the ladder |
| Lantern Garden | 13x13x6 | Shared problems only | 13x13x5: hedged garden with lamp posts, cross paths to a lily pond in the middle, and benches |
| Treetop Library | 13x13x22 | Shared problems only | 15x15x24: library room on a platform held up by four tall birches, spiral stairs up a post in the middle, a railed balcony |
| Starwatch Observatory | 11x11x12 | Shared problems only | 11x11x14: stone tower with a ladder through a map room and Lily's bedroom up to a glass dome with a telescope |
| Madison's House | 11x14x7 | Shared problems only | 13x15x9: white-walled cottage with a kitchen, pantry chest, bed and table, a fenced carrot garden behind and two beehives |
| Madison's Bakery | 11x12x8 | Shared problems only | 13x11x11: shop counter with a cake, customer table, smokers, a brick oven with a smoking chimney, hay, the baker's bed |
| Farmstead | 25x21x11 | Shared problems only | 25x21x12: barn with a wide opening, hay, the pantry and the farmer's bed, a fenced pen with cows, sheep, a pig and chickens, wheat and potato fields |
| Orchard | 19x19x9 | Shared problems only | 19x19x9: fenced meadow with seven blossoming trees, four hives over campfires and the beekeeper's honey shed in the middle |
| Market Stall | 9x7x6 | Shared problems only | 11x9x6: striped awning on posts, a counter of produce, hanging lanterns, the merchant's chest, benches out front |
| Pumpkin Patch Farm | 15x13x7 | Shared problems only | 17x15x8: fenced pumpkin rows with a scarecrow (fence arms, hay body, carved pumpkin head), a farm shed with a bed |
| Riverside Mill | 13x11x10 | Shared problems only | 15x13x13: stone mill floor (grindstone, flour sacks), a loft with Madison's bed up a ladder, and a water wheel turning in its own mill race; builds on land or water |
| Bee Sanctuary | 17x17x8 | Shared problems only | 17x17x8: fenced meadow thick with flowers, eight hives over campfires, the beekeeper's apiary hut |
| Sweet Berry Cabin | 11x13x8 | Shared problems only | 13x13x9: cozy cabin with a kitchen and table, a fenced patch of ripe sweet berries on podzol, a woodpile and a lamp post |

## Batch 3: shared places, FelixSyn's homes, Master BreadLoaf's arenas (15)

| Structure | Old | What was wrong | What changed (new) |
|---|---|---|---|
| LoafCraft Village | 31x31x10 | Its open well was where characters got stuck (part 1 fixed the AI side) | 31x31x10: the well is fenced and roofed so nobody can fall in; two ItzBreadLoaf cabins with doors facing the village, Archer Lily's cabin with a fletching table and targets, a fishing pond with a dock, Baker Madison's booth, a market corner, a fenced farm with mixed crops, lamp posts and a bell; FelixSyn still drifts in sometimes (`felixVillageChance`) |
| Cousins' Camp | 13x11x5 | Shared problems only | 15x13x6: Lily's pink and Madison's yellow walk-in tents with their beds, a campfire with benches, a picnic table with a cake |
| PvP Arena | 23x23x6 fenced arena | Shared problems only | 25x25x6: round arena with a low wall, two rows of stands facing in, entrances east and west, lanterns; the ItzBreadLoaf there is a geared-up Sweaty one, so the FelixSyn rivalry starts |
| Friendship Festival | 21x17x7 | Shared problems only | 23x19x8: a raised stage with steps and bunting, two long tables with chairs and cakes, banner poles and lamp posts |
| Giant Loaf | 27x15x12 | Shared problems only | 27x17x11: a real loaf shape (crust, crumb inside, score marks on top) with a hall inside; the treasure room is now hidden under the floor: a carpet hides a trapdoor and a ladder down |
| Ruined Portal Hideout | 13x11x10 | Shared problems only | 15x13x9: a broken obsidian and crying obsidian portal on netherrack, FelixSyn and Harley's walk-in tent behind it, a campfire, gold and a training dummy |
| Mace Workshop | 13x11x8 | Shared problems only | 13x11x10: stone-and-wood workshop (biome materials) with an anvil, smithing table, grindstone, blast furnace, block-built mace racks, a dummy, a gable roof |
| Cliff Hut | 11x11x8 | The diving ledge was flush with the ground, so on flat land there was nothing to dive from | 11x11x9: hip-roofed hut and a real diving board: a ladder up a log post to a railed board 6 high, hay underneath its end |
| Wind Tower | 9x9x27 | Shared problems only | 9x9x27: ladder all the way up past windows and torches to a wide fenced lookout with a jump gap, hay landing pad at the bottom |
| Rival Camp | 11x9x5 | Shared problems only | 11x9x5: walk-in black tent with a bed, campfire with benches, targets, a dummy and red-vs-blue banners; still only within 24-96 blocks of an ItzBreadLoaf House or Tent |
| Sky Arena | 21x21x38, ladder up | Shared problems only | 21x21x34: a round stone arena on a pillar with stairs winding up around it (instead of a long ladder), a rail around the stairwell, a rim with lanterns |
| Nether Mace Training Ground | 21x21x22 | Shared problems only | 21x21x23: round blackstone floor, four pillars 8, 12, 16 and 20 high, each with a ladder, hay landing pads, dummies and soul lanterns |
| Nether Mace Arena | 25x25x26 | Shared problems only | 25x25x26: round floor inside a ring of lava with four bridges across, a rim that keeps the lava in, four tall pillars with ladders |
| End Mace Tower | 9x9x32 | Shared problems only | 11x11x32: End-stone tower with purpur pillars and floors, a ladder through five floors, a crenellated top with end rods |
| End Sky Platform | 15x15x20 | Shared problems only | 15x15x20: purpur deck on a purpur pillar, purpur stairs winding up around it, a rail around the stairwell, end rods on the rim |

All 64 structures are now on the blueprint system (62 in world generation, 2 built while you play).
