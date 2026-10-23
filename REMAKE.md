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

Batch 2 (Lily and Madison) and batch 3 (shared, FelixSyn, Nether, End, village) follow.
