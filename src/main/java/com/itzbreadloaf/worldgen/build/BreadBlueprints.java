package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/** ItzBreadLoaf's structures (and the pirates', bandits' and knights'), remade on the blueprint system. */
final class BreadBlueprints {
	private static final Block[] WOOL = {Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.LIGHT_BLUE_WOOL, Blocks.YELLOW_WOOL,
			Blocks.LIME_WOOL, Blocks.CYAN_WOOL, Blocks.RED_WOOL, Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.BLUE_WOOL};
	private static final Block[] DARK_WOOL = {Blocks.BLACK_WOOL, Blocks.GRAY_WOOL, Blocks.BROWN_WOOL};

	private BreadBlueprints() {
	}

	private static BlockState wool(Blueprint b, Block[] colors) {
		return colors[b.random.nextInt(colors.length)].defaultBlockState();
	}

	private static BlockPos at(int x, int y, int z) {
		return new BlockPos(x, y, z);
	}

	/** Crenellations: every other block along the ring at height y. */
	private static void crenellate(Blueprint b, int x0, int z0, int x1, int z1, int y, BlockState state) {
		for (int x = x0; x <= x1; x += 2) {
			b.set(x, y, z0, state);
			b.set(x, y, z1, state);
		}
		for (int z = z0; z <= z1; z += 2) {
			b.set(x0, y, z, state);
			b.set(x1, y, z, state);
		}
	}

	// ================================================================ homes

	/** House: a gable-roofed cottage with a bed, chest, kitchen corner and table, a path and flowers out front. */
	static void house(Blueprint b) {
		Palette p = b.palette;
		Pieces.cottage(b, 1, 1, 7, 7, 0, 3, Pieces.Roof.GABLE_X, "house");
		b.bed(2, 1, 5, p.bed());
		b.chest(3, 1, 6, "chests/house");
		b.set(6, 1, 6, Blocks.CRAFTING_TABLE);
		b.set(6, 1, 5, Blocks.FURNACE);
		Pieces.table(b, 5, 1, 3, false, false);
		b.set(4, 1, 3, Pieces.stairs(p.stairs(), Direction.WEST));
		Pieces.path(b, 4, 0, 4, 0, 0);
		Pieces.garden(b, 0, 0, 2, 0, 0);
		Pieces.garden(b, 6, 0, 8, 0, 0);
		b.resident(CharacterVariant.BREADLOAF, 4, 1, 4, 4, 4, 10, at(2, 1, 5), null, false);
	}

	/** Tent: a walk-in tent for 2-4 ItzBreadLoafs with beds, a chest and barrel, and a campfire out front. */
	static void tent(Blueprint b) {
		Palette p = b.palette;
		Pieces.tent(b, 3, 3, 7, wool(b, WOOL));
		b.bed(4, 1, 4, p.bed());
		b.bed(6, 1, 4, p.bed());
		b.chest(4, 1, 7, "chests/tent");
		b.barrel(6, 1, 7, "chests/barrel_supplies");
		Pieces.lantern(b, 5, 1, 8, false);
		// In front of the tent, off the doorway line so the way in stays open.
		Pieces.campfire(b, 5, 1, 1);
		int members = 2 + b.random.nextInt(3);
		for (int i = 0; i < members; i++) {
			BlockPos bed = i == 0 ? at(4, 1, 4) : i == 1 ? at(6, 1, 4) : null;
			b.resident(CharacterVariant.BREADLOAF, 6, 1, 1, 5, 5, 10, bed, null, true);
		}
	}

	/** Bread Bakery: ItzBreadLoaf's own bakery with ovens, a counter, flour and a brick chimney. */
	static void breadBakery(Blueprint b) {
		Palette p = b.palette;
		Pieces.cottage(b, 1, 1, 9, 9, 0, 3, Pieces.Roof.HIP, "bakery");
		for (int x = 3; x <= 6; x++) {
			b.set(x, 1, 8, x == 6 ? Blocks.SMOKER : Blocks.FURNACE);
		}
		b.chest(7, 1, 8, "chests/bread_bakery");
		b.barrel(8, 1, 7, "chests/barrel_supplies");
		b.fill(2, 1, 2, 2, 2, 2, Blocks.HAY_BLOCK);
		// Counter (upside-down stairs) with a gap to walk through.
		for (int x = 4; x <= 7; x++) {
			b.set(x, 1, 5, Pieces.upsideDown(p.stairs(), Direction.NORTH));
		}
		b.bed(2, 1, 6, p.bed());
		// Chimney through the roof, with smoke.
		b.fill(8, 1, 9, 8, 8, 9, Blocks.BRICKS);
		b.set(8, 9, 9, Blocks.CAMPFIRE);
		Pieces.path(b, 5, 0, 5, 0, 0);
		b.resident(CharacterVariant.BREADLOAF, 5, 1, 3, 5, 5, 10, at(2, 1, 6), null, false);
	}

	/** Jungle Treehouse: a hut on a platform around a big jungle trunk, a ladder up the trunk, a canopy and vines. */
	static void jungleTreehouse(Blueprint b) {
		Palette p = b.palette;
		BlockState log = Blocks.JUNGLE_LOG.defaultBlockState();
		b.fill(5, 1, 5, 6, 18, 6, log);
		int deck = 10;
		b.fill(2, deck, 2, 10, deck, 10, p.planks());
		b.fill(5, deck, 5, 6, deck, 6, log);
		Pieces.room(b, 3, 4, 9, 9, deck, 3, "treehouse");
		b.fill(5, deck + 1, 5, 6, deck + 3, 6, log);
		Pieces.door(b, 7, deck + 1, 4, Direction.NORTH);
		Pieces.window(b, 3, deck + 2, 7);
		Pieces.window(b, 9, deck + 2, 6);
		Pieces.roof(b, Pieces.Roof.GABLE_X, 3, 4, 9, 9, deck + 3);
		// Balcony rail, with the ladder coming up through the front.
		b.walls(2, 2, 10, 10, deck + 1, deck + 1, p.fence());
		// Ladder up the front, onto the balcony through a hole in the deck.
		Pieces.ladder(b, 5, 1, deck, 3, Direction.NORTH);
		// Inside.
		b.bed(8, deck + 1, 7, p.bed());
		b.chest(4, deck + 1, 8, "chests/jungle_treehouse");
		Pieces.lantern(b, 4, deck + 1, 5, false);
		// Canopy and vines.
		for (int dx = -4; dx <= 4; dx++) {
			for (int dz = -4; dz <= 4; dz++) {
				for (int y = 17; y <= 20; y++) {
					if (Math.abs(dx) + Math.abs(dz) + Math.abs(y - 18) * 2 <= 6) {
						b.set(5 + dx, y, 5 + dz, Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
					}
				}
			}
		}
		for (int y = deck - 3; y < deck; y++) {
			b.set(10, y, 4, Blocks.VINE.defaultBlockState().setValue(net.minecraft.world.level.block.VineBlock.WEST, true));
		}
		b.resident(CharacterVariant.BREADLOAF, 7, deck + 1, 6, 6, 6, 6, at(8, deck + 1, 7), null, false);
	}

	/** Desert Bunker: a sandstone hut over stairs down to a hidden room, with an arrow trap at the door. */
	static void desertBunker(Blueprint b) {
		Palette p = b.palette;
		// Bunker room underground.
		Pieces.stoneRoom(b, 1, 1, 9, 9, -6, 4, "bunker");
		b.fill(1, -1, 1, 9, -1, 9, p.bricks());
		// The hut on top and the stairs down.
		b.walls(3, 1, 7, 5, 1, 2, p.bricks());
		b.fill(3, 3, 1, 7, 3, 5, p.stoneSlab());
		Pieces.door(b, 5, 1, 1, Direction.NORTH);
		Pieces.stairsRun(b, 5, -5, 8, Direction.NORTH, 6);
		// Arrow trap: a pressure plate just inside the door, a dispenser beside it.
		b.set(5, 1, 2, Blocks.STONE_PRESSURE_PLATE);
		b.dispenser(6, 1, 2, Direction.WEST, "traps/arrows");
		// Inside the bunker.
		b.bed(2, -5, 2, p.bed());
		b.bed(8, -5, 2, p.bed());
		b.chest(2, -5, 7, "chests/desert_bunker");
		b.chest(3, -5, 8, "chests/desert_bunker");
		b.barrel(8, -5, 8, "chests/barrel_supplies");
		b.set(8, -5, 6, Blocks.CRAFTING_TABLE);
		b.set(7, -5, 8, Blocks.CARTOGRAPHY_TABLE);
		Pieces.lantern(b, 5, -5, 2, false);
		Pieces.lantern(b, 2, -5, 5, false);
		b.resident(CharacterVariant.BREADLOAF, 4, -5, 4, 5, 5, 6, at(2, -5, 2), null, false);
	}

	/** Igloo Hideout: a snow dome with a hidden trapdoor (behind the furnace) down to a stone basement. */
	static void igloo(Blueprint b) {
		Palette p = b.palette;
		BlockState snow = Blocks.SNOW_BLOCK.defaultBlockState();
		for (int x = 0; x <= 8; x++) {
			for (int z = 0; z <= 8; z++) {
				for (int y = 1; y <= 5; y++) {
					double d = Math.sqrt((x - 4) * (x - 4) + (z - 4) * (z - 4) + (y - 1) * (y - 1) * 1.6);
					if (d <= 4.2) {
						b.set(x, y, z, d > 3.2 ? snow : Blocks.AIR.defaultBlockState());
					}
				}
			}
		}
		b.fill(1, 0, 1, 7, 0, 7, Blocks.PACKED_ICE);
		b.fill(2, 1, 2, 6, 1, 6, Blocks.WHITE_CARPET);
		b.clear(4, 1, 0, 4, 2, 1);
		b.floor(3, 3, 5, 5, 1, "igloo");
		b.bed(2, 1, 3, p.bed());
		b.set(6, 1, 3, Blocks.FURNACE);
		b.set(6, 1, 2, Blocks.CRAFTING_TABLE);
		Pieces.lantern(b, 3, 1, 6, false);
		// Hidden hatch beside the furnace, a ladder down to the basement.
		Pieces.stoneRoom(b, 2, 2, 7, 7, -6, 4, "basement");
		b.fill(2, -1, 2, 7, -1, 7, p.bricks());
		Pieces.ladder(b, 6, -5, -1, 4, Direction.NORTH);
		b.set(6, 0, 4, Blocks.SPRUCE_TRAPDOOR);
		b.set(6, 1, 4, Blocks.AIR);
		b.chest(3, -5, 3, "chests/igloo_hideout");
		b.barrel(3, -5, 6, "chests/barrel_supplies");
		b.set(5, -5, 6, Blocks.BREWING_STAND);
		Pieces.lantern(b, 4, -5, 3, false);
		b.resident(CharacterVariant.BREADLOAF, 4, 1, 4, 4, 4, 8, at(2, 1, 3), null, false);
	}

	/** Swamp Hut on stilts: a shack over the water with a porch, a ladder and a cauldron. */
	static void swampHut(Blueprint b) {
		Palette p = b.palette;
		b.foundation = false;
		b.groundLayer = false;
		b.skirt = false;
		int deck = 3;
		for (int[] c : new int[][]{{2, 2}, {8, 2}, {2, 8}, {8, 8}, {2, 1}, {8, 1}}) {
			b.pillar(c[0], deck - 1, c[1], p.log());
		}
		b.fill(2, deck, 1, 8, deck, 1, p.planks());
		Pieces.room(b, 2, 2, 8, 8, deck, 3, "hut");
		Pieces.door(b, 5, deck + 1, 2, Direction.NORTH);
		Pieces.windowsAround(b, 2, 2, 8, 8, deck + 2, 5);
		Pieces.roof(b, Pieces.Roof.GABLE_X, 2, 2, 8, 8, deck + 3);
		b.fill(2, deck + 1, 1, 4, deck + 1, 1, p.fence());
		b.fill(6, deck + 1, 1, 8, deck + 1, 1, p.fence());
		Pieces.ladder(b, 5, 1, deck, 0, Direction.NORTH);
		b.bed(3, deck + 1, 6, p.bed());
		b.set(7, deck + 1, 7, Blocks.WATER_CAULDRON);
		b.set(7, deck + 1, 3, Blocks.CRAFTING_TABLE);
		b.chest(7, deck + 1, 5, "chests/swamp_hut");
		b.barrel(3, deck + 1, 3, "chests/barrel_supplies");
		Pieces.lantern(b, 5, deck + 1, 7, false);
		b.resident(CharacterVariant.BREADLOAF, 5, deck + 1, 4, 5, 5, 10, at(3, deck + 1, 6), null, false);
	}

	/** Cliffside Cave Base: a lit stone room underground, reached through a trapdoor hidden in a bush. */
	static void cliffsideBase(Blueprint b) {
		Palette p = b.palette;
		b.clearAbove = false;
		b.groundLayer = false;
		b.skirt = false;
		b.foundation = false;
		BlockState wall = p.bricks();
		b.fill(1, -8, 1, 11, -2, 11, wall);
		b.clear(2, -7, 2, 10, -3, 10);
		b.fill(2, -8, 2, 10, -8, 10, p.stoneFloor());
		b.floor(2, 2, 10, 10, -7, "cave base");
		Pieces.ladder(b, 6, -7, -1, 6, Direction.NORTH);
		b.set(6, 0, 6, Blocks.SPRUCE_TRAPDOOR);
		BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
		for (int[] c : new int[][]{{5, 5}, {6, 5}, {7, 5}, {5, 6}, {7, 6}, {5, 7}, {7, 7}}) {
			b.set(c[0], 1, c[1], leaves);
		}
		b.bed(3, -7, 8, p.bed());
		b.set(9, -7, 9, Blocks.CRAFTING_TABLE);
		b.set(9, -7, 8, Blocks.FURNACE);
		b.set(9, -7, 3, Blocks.SMITHING_TABLE);
		b.chest(3, -7, 3, "chests/cliffside_base");
		b.chest(4, -7, 3, "chests/cliffside_base");
		b.barrel(9, -7, 5, "chests/barrel_supplies");
		b.mob(8, -7, 8, "dummy");
		for (int[] l : new int[][]{{4, 6}, {8, 4}, {3, 10}}) {
			Pieces.lantern(b, l[0], -7, l[1], false);
		}
		b.fill(5, -8, 4, 7, -8, 4, Blocks.RED_WOOL);
		b.resident(CharacterVariant.BREADLOAF, 6, -7, 4, 6, 6, 4, at(3, -7, 8), null, false);
	}

	// ================================================================ outposts and towers

	/**
	 * Lookout Tower (remade): the old one had no way up. Now a 5x5 tower on a stone pad with three floors, a ladder up
	 * the inside of the back wall through a hole in every floor, a fenced lookout on top under a hip roof, windows and
	 * a lantern on every floor. Archer ItzBreadLoaf keeps watch from the top.
	 */
	static void lookoutTower(Blueprint b) {
		Palette p = b.palette;
		b.fill(0, 0, 0, 6, 0, 6, p.stoneFloor());
		Pieces.tower(b, 1, 1, 5, 0, 15, 5, false);
		b.walls(1, 1, 5, 5, 1, 1, p.bricks());
		Pieces.door(b, 3, 1, 1, Direction.NORTH);
		b.fill(0, 15, 0, 6, 15, 6, p.planks());
		Pieces.ladder(b, 3, 15, 15, 4, Direction.NORTH);
		b.walls(0, 0, 6, 6, 16, 16, p.fence());
		for (int[] c : new int[][]{{0, 0}, {6, 0}, {0, 6}, {6, 6}}) {
			b.fill(c[0], 17, c[1], c[0], 18, c[1], p.fence());
		}
		Pieces.hipRoof(b, 1, 1, 5, 5, 19);
		b.floor(1, 1, 5, 5, 16, "lookout");
		b.bed(2, 1, 2, p.bed());
		b.chest(4, 1, 2, "chests/lookout_tower");
		b.barrel(4, 1, 3, "chests/barrel_supplies");
		b.set(4, 6, 2, Blocks.CRAFTING_TABLE);
		b.set(4, 6, 3, Blocks.BOOKSHELF);
		b.set(4, 11, 2, Blocks.TARGET);
		b.chest(1, 16, 1, "chests/lookout_tower");
		Pieces.lantern(b, 5, 16, 5, false);
		b.resident(CharacterVariant.ARCHER, 3, 16, 2, 3, 3, 2);
	}

	/** Watchtower: a tall stone tower with a ladder all the way up and a walled lookout. Archer on top. */
	static void watchtower(Blueprint b) {
		Palette p = b.palette;
		b.fill(0, 0, 0, 6, 0, 6, p.stoneFloor());
		Pieces.tower(b, 1, 1, 5, 0, 16, 8, true);
		Pieces.door(b, 3, 1, 1, Direction.NORTH);
		b.fill(0, 16, 0, 6, 16, 6, p.bricks());
		Pieces.ladder(b, 3, 16, 16, 4, Direction.NORTH);
		b.walls(0, 0, 6, 6, 17, 17, p.stoneWall());
		for (int[] c : new int[][]{{0, 0}, {6, 0}, {0, 6}, {6, 6}}) {
			Pieces.lantern(b, c[0], 18, c[1], false);
		}
		b.floor(1, 1, 5, 5, 17, "lookout");
		b.bed(2, 1, 2, p.bed());
		b.barrel(4, 1, 2, "chests/barrel_supplies");
		b.chest(1, 17, 1, "chests/watchtower");
		b.resident(CharacterVariant.ARCHER, 3, 17, 2, 3, 3, 2);
	}

	/** Mountain Outpost: a two-floor stone lookout on the peaks with a crenellated roof and a campfire. */
	static void mountainOutpost(Blueprint b) {
		Palette p = b.palette;
		b.fill(0, 0, 0, 8, 0, 8, p.stoneFloor());
		Pieces.tower(b, 1, 1, 7, 0, 8, 4, true);
		Pieces.door(b, 4, 1, 1, Direction.NORTH);
		crenellate(b, 1, 1, 7, 7, 9, p.bricks());
		b.set(3, 9, 3, Blocks.CAMPFIRE);
		b.bed(2, 1, 4, p.bed());
		b.bed(2, 5, 4, p.bed());
		b.set(6, 1, 2, Blocks.FURNACE);
		b.chest(6, 1, 4, "chests/mountain_outpost");
		b.barrel(6, 5, 2, "chests/barrel_supplies");
		b.set(6, 5, 4, Blocks.FLETCHING_TABLE);
		b.resident(CharacterVariant.ARCHER, 4, 9, 4, 4, 4, 3, at(2, 5, 4), null, true);
		b.resident(CharacterVariant.KNIGHT, 4, 1, 3, 4, 4, 8, at(2, 1, 4), null, true);
	}

	/** Mining Camp: a tent and campfire beside a log-framed shaft down to a lit mine room with rails. */
	static void miningCamp(Blueprint b) {
		Palette p = b.palette;
		Pieces.tent(b, 1, 5, 6, Blocks.BROWN_WOOL.defaultBlockState());
		b.bed(2, 1, 7, p.bed());
		b.barrel(4, 1, 9, "chests/barrel_supplies");
		Pieces.campfire(b, 3, 1, 2);
		// Shaft head: four posts and a slab roof over a ladder going down.
		for (int[] c : new int[][]{{8, 8}, {11, 8}, {8, 11}, {11, 11}}) {
			b.fill(c[0], 1, c[1], c[0], 2, c[1], p.fence());
		}
		b.fill(8, 3, 8, 11, 3, 11, p.slab());
		Pieces.ladder(b, 9, -9, 0, 10, Direction.NORTH);
		b.fill(8, 0, 8, 11, 0, 11, p.planks());
		Pieces.ladder(b, 9, 0, 0, 10, Direction.NORTH);
		// Mine room.
		Pieces.stoneRoom(b, 5, 5, 12, 12, -10, 4, "mine");
		b.fill(5, -6, 5, 12, -6, 12, p.stoneBase());
		Pieces.ladder(b, 9, -9, -6, 10, Direction.NORTH);
		b.fill(6, -9, 6, 11, -9, 6, Blocks.RAIL);
		b.set(6, -8, 12, Blocks.COAL_ORE);
		b.set(12, -8, 8, Blocks.IRON_ORE);
		b.set(5, -9, 9, Blocks.COPPER_ORE);
		b.chest(11, -9, 11, "chests/mining_camp");
		b.barrel(6, -9, 11, "chests/barrel_supplies");
		Pieces.lantern(b, 7, -9, 9, false);
		Pieces.lantern(b, 11, -9, 7, false);
		b.fill(6, 1, 10, 7, 1, 10, Blocks.RAIL);
		b.resident(CharacterVariant.MINER, 3, 1, 3, 6, 6, 10, at(2, 1, 7), null, false);
	}

	// ================================================================ fortifications

	/** Bread Fort: stone walls with crenellations, four towers with ladders, barracks, an armory and a treasure room. */
	static void fort(Blueprint b) {
		Palette p = b.palette;
		BlockState brick = p.bricks();
		b.walls(0, 0, 20, 20, 1, 4, brick);
		crenellate(b, 0, 0, 20, 20, 5, brick);
		b.clear(9, 1, 0, 11, 3, 0);
		Pieces.path(b, 10, 0, 10, 0, 12);
		for (int[] c : new int[][]{{0, 0}, {16, 0}, {0, 16}, {16, 16}}) {
			Pieces.tower(b, c[0], c[1], 5, 0, 9, 9, true);
			crenellate(b, c[0], c[1], c[0] + 4, c[1] + 4, 10, brick);
			// Door into the courtyard: the front towers open sideways, the back ones forward (the barracks is beside them).
			if (c[1] == 0) {
				Pieces.door(b, c[0] == 0 ? 4 : 16, 1, 2, c[0] == 0 ? Direction.EAST : Direction.WEST);
			} else {
				Pieces.door(b, c[0] + 2, 1, 16, Direction.NORTH);
			}
		}
		// Barracks along the back.
		Pieces.stoneRoom(b, 5, 13, 15, 18, 0, 4, "barracks");
		Pieces.door(b, 10, 1, 13, Direction.NORTH);
		Pieces.roof(b, Pieces.Roof.FLAT, 5, 13, 15, 18, 4);
		for (int x : new int[]{6, 8, 12, 14}) {
			b.bed(x, 1, 16, p.bed());
		}
		Pieces.lantern(b, 10, 1, 17, false);
		b.barrel(7, 1, 14, "chests/barrel_supplies");
		// Treasure room and armory either side of the courtyard.
		Pieces.stoneRoom(b, 4, 5, 8, 9, 0, 3, "treasure room");
		Pieces.door(b, 8, 1, 7, Direction.EAST);
		Pieces.roof(b, Pieces.Roof.FLAT, 4, 5, 8, 9, 3);
		b.chest(5, 1, 6, "chests/fort");
		b.chest(5, 1, 8, "chests/fort");
		Pieces.lantern(b, 7, 1, 8, false);
		Pieces.stoneRoom(b, 12, 5, 16, 9, 0, 3, "armory");
		Pieces.door(b, 12, 1, 7, Direction.WEST);
		Pieces.roof(b, Pieces.Roof.FLAT, 12, 5, 16, 9, 3);
		b.set(15, 1, 6, Blocks.ANVIL);
		b.set(15, 1, 8, Blocks.GRINDSTONE);
		b.barrel(14, 1, 8, "chests/barrel_supplies");
		Pieces.lantern(b, 13, 1, 6, false);
		// Courtyard: training dummies and lamp posts.
		b.mob(7, 1, 11, "dummy");
		b.mob(13, 1, 11, "dummy");
		for (int[] l : new int[][]{{6, 3}, {14, 3}, {6, 11}, {14, 11}}) {
			b.set(l[0], 1, l[1], p.fence());
			Pieces.lantern(b, l[0], 2, l[1], false);
		}
		BlockPos[] beds = {at(6, 1, 16), at(8, 1, 16), at(12, 1, 16), at(14, 1, 16)};
		b.resident(CharacterVariant.CAPTAIN, 10, 1, 11, 10, 10, 9, beds[0], null, true);
		for (int i = 1; i < 4; i++) {
			b.resident(CharacterVariant.KNIGHT, 10, 1, 11, 10, 10, 9, beds[i], null, true);
		}
	}

	/** Ruined Loaf Castle: crumbling walls and towers, a two-floor keep with a great hall, a throne and a treasury. */
	static void ruinedCastle(Blueprint b) {
		Palette p = b.palette;
		BlockState[] stones = {Blocks.STONE_BRICKS.defaultBlockState(), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
				Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState()};
		// Outer walls with gaps.
		for (int y = 1; y <= 6; y++) {
			for (int i = 0; i <= 26; i++) {
				for (int[] c : new int[][]{{i, 0}, {i, 26}, {0, i}, {26, i}}) {
					if (y <= 2 || b.random.nextInt(5) != 0) {
						b.set(c[0], y, c[1], stones[b.random.nextInt(stones.length)]);
					}
				}
			}
		}
		b.clear(12, 1, 0, 14, 4, 0);
		b.fill(12, 4, 0, 14, 4, 0, Blocks.IRON_BARS);
		b.clear(26, 1, 10, 26, 6, 12);
		// Corner towers; some tops have fallen.
		for (int[] c : new int[][]{{0, 0}, {22, 0}, {0, 22}, {22, 22}}) {
			int height = 8 + b.random.nextInt(4);
			Pieces.tower(b, c[0], c[1], 5, 0, height, height, true);
			Pieces.door(b, c[0] == 0 ? c[0] + 4 : c[0], 1, c[1] + 2, c[0] == 0 ? Direction.EAST : Direction.WEST);
		}
		// The keep: great hall below, treasury above, stairs between.
		Pieces.stoneRoom(b, 8, 8, 18, 18, 0, 5, "great hall");
		Pieces.door(b, 13, 1, 8, Direction.NORTH);
		b.clear(12, 1, 8, 14, 2, 8);
		b.fill(8, 6, 8, 18, 6, 18, p.planks());
		Pieces.stoneRoom(b, 8, 8, 18, 18, 6, 4, "treasury");
		// Stairs up the back wall; the floor above them is open, the last step leads onto the treasury floor.
		Pieces.stairsRun(b, 9, 1, 16, Direction.EAST, 5);
		b.clear(10, 6, 16, 13, 6, 16);
		crenellate(b, 8, 8, 18, 18, 11, stones[0]);
		// Long table down the hall, and the throne at the far end.
		for (int z = 11; z <= 14; z++) {
			Pieces.table(b, 13, 1, z, true, false);
		}
		b.set(13, 1, 17, Pieces.stairs(p.stairs(), Direction.NORTH));
		b.set(12, 1, 17, p.bricks());
		b.set(14, 1, 17, p.bricks());
		b.fill(13, 1, 9, 13, 1, 10, Blocks.RED_CARPET);
		Pieces.lantern(b, 9, 1, 9, false);
		Pieces.lantern(b, 17, 1, 9, false);
		Pieces.lantern(b, 17, 1, 17, false);
		b.chest(17, 7, 17, "chests/ruined_loaf_castle");
		b.chest(9, 7, 9, "chests/ruined_loaf_castle");
		b.barrel(17, 7, 9, "chests/barrel_supplies");
		Pieces.lantern(b, 13, 7, 13, false);
		// Rubble in the courtyard.
		for (int i = 0; i < 14; i++) {
			int x = 2 + b.random.nextInt(23);
			int z = 2 + b.random.nextInt(23);
			if ((x < 7 || x > 19 || z < 7 || z > 19) && !b.isSet(x, 1, z)) {
				b.set(x, 1, z, b.random.nextBoolean() ? Blocks.MOSSY_COBBLESTONE : Blocks.COBBLESTONE);
			}
		}
		int members = 3 + b.random.nextInt(3);
		b.resident(CharacterVariant.CAPTAIN, 11, 1, 12, 13, 13, 12, null, null, true);
		for (int i = 1; i < members; i++) {
			b.resident(i % 2 == 0 ? CharacterVariant.BREADLOAF : CharacterVariant.KNIGHT, 11, 1, 12, 13, 13, 12, null, null, true);
		}
	}

	/** Bandit Camp: dark tents around a campfire, a pile of stolen goods, 3-4 Bandits who defend each other. */
	static void banditCamp(Blueprint b) {
		Palette p = b.palette;
		Pieces.tent(b, 1, 6, 6, wool(b, DARK_WOOL));
		Pieces.tent(b, 9, 6, 6, wool(b, DARK_WOOL));
		b.bed(2, 1, 8, p.bed());
		b.bed(12, 1, 8, p.bed());
		Pieces.campfire(b, 7, 1, 3);
		b.fill(6, 1, 12, 8, 1, 12, Blocks.HAY_BLOCK);
		b.chest(7, 1, 13, "chests/bandit_camp");
		b.barrel(6, 1, 13, "chests/barrel_supplies");
		b.barrel(8, 1, 13, "chests/barrel_supplies");
		for (int[] l : new int[][]{{1, 1}, {13, 1}}) {
			b.set(l[0], 1, l[1], p.fence());
			Pieces.lantern(b, l[0], 2, l[1], false);
		}
		int members = 3 + b.random.nextInt(2);
		for (int i = 0; i < members; i++) {
			BlockPos bed = i == 0 ? at(2, 1, 8) : i == 1 ? at(12, 1, 8) : null;
			b.resident(CharacterVariant.BANDIT, 7, 1, 4, 7, 7, 10, bed, null, true);
		}
	}

	/** Sweaty Dojo: a training hall with targets, dummies, an anvil and a weapon chest. */
	static void sweatyDojo(Blueprint b) {
		Palette p = b.palette;
		Pieces.cottage(b, 1, 1, 11, 11, 0, 4, Pieces.Roof.HIP, "dojo");
		b.fill(4, 0, 4, 8, 0, 8, Blocks.RED_WOOL);
		b.fill(5, 0, 5, 7, 0, 7, Blocks.WHITE_WOOL);
		b.set(3, 1, 10, Blocks.TARGET);
		b.set(9, 1, 10, Blocks.TARGET);
		b.mob(5, 1, 9, "dummy");
		b.mob(7, 1, 9, "dummy");
		b.set(10, 1, 3, Blocks.ANVIL);
		b.set(10, 1, 4, Blocks.GRINDSTONE);
		b.chest(2, 1, 3, "chests/sweaty_dojo");
		b.barrel(2, 1, 4, "chests/barrel_supplies");
		b.bed(2, 1, 8, p.bed());
		Pieces.lantern(b, 6, 1, 3, false);
		b.resident(CharacterVariant.SWEATY, 6, 1, 5, 6, 6, 6, at(2, 1, 8), null, false);
	}

	/** Snowball Fort: two snow forts facing each other, a snowman between them, snowball stashes. */
	static void snowballFort(Blueprint b) {
		BlockState snow = Blocks.SNOW_BLOCK.defaultBlockState();
		for (int[] fort : new int[][]{{1, 6, 6}, {12, 17, 12}}) {
			int x0 = fort[0];
			int x1 = fort[1];
			int front = fort[2];
			b.fill(front, 1, 3, front, 2, 9, snow);
			b.fill(x0, 1, 3, x1, 1, 3, snow);
			b.fill(x0, 1, 9, x1, 1, 9, snow);
			for (int z = 3; z <= 9; z += 2) {
				b.set(front, 3, z, snow);
			}
		}
		b.fill(9, 1, 6, 9, 2, 6, snow);
		b.set(9, 3, 6, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.NORTH));
		b.chest(2, 1, 5, "chests/snowball_fort");
		b.chest(16, 1, 7, "chests/snowball_fort");
		b.floor(2, 4, 5, 8, 1, "left fort");
		b.floor(13, 4, 16, 8, 1, "right fort");
		b.resident(CharacterVariant.BREADLOAF, 4, 1, 6, 9, 6, 10, null, null, true);
		b.resident(CharacterVariant.VILLAGER, 14, 1, 6, 9, 6, 10, null, null, true);
	}

	// ================================================================ special buildings

	/** Bread Mill (the windmill): a stone mill floor, a wooden tower with a ladder to the sail room, sails, a wheat farm. */
	static void breadMill(Blueprint b) {
		Palette p = b.palette;
		Pieces.stoneRoom(b, 3, 3, 11, 11, 0, 4, "mill floor");
		Pieces.door(b, 7, 1, 3, Direction.NORTH);
		Pieces.window(b, 3, 2, 7);
		Pieces.window(b, 11, 2, 7);
		// Wooden tower on top, two floors, a ladder all the way up.
		b.fill(3, 5, 3, 11, 5, 11, p.slab());
		b.fill(4, 5, 4, 10, 5, 10, p.planks());
		b.walls(4, 4, 10, 10, 6, 11, p.planks());
		for (int[] c : new int[][]{{4, 4}, {10, 4}, {4, 10}, {10, 10}}) {
			b.fill(c[0], 6, c[1], c[0], 11, c[1], p.log());
		}
		b.fill(5, 9, 5, 9, 9, 9, p.planks());
		b.floor(5, 5, 9, 9, 6, "grain loft");
		b.floor(5, 5, 9, 9, 10, "sail room");
		Pieces.roof(b, Pieces.Roof.GABLE_X, 4, 4, 10, 10, 11);
		Pieces.ladder(b, 7, 1, 9, 9, Direction.NORTH);
		Pieces.window(b, 4, 7, 7);
		Pieces.window(b, 10, 7, 7);
		// Sails: a hub on the front of the tower and four wool blades.
		b.set(7, 9, 3, Pieces.logAlong(p.log(), Direction.Axis.Z));
		for (int i = 1; i <= 4; i++) {
			for (int[] d : new int[][]{{1, 1}, {-1, -1}, {1, -1}, {-1, 1}}) {
				b.set(7 + d[0] * i, 9 + d[1] * i, 2, Blocks.WHITE_WOOL);
			}
		}
		// Inside: grindstone, hay, flour, a bed for the miller.
		b.set(5, 1, 9, Blocks.GRINDSTONE);
		b.fill(9, 1, 9, 10, 2, 10, Blocks.HAY_BLOCK);
		b.chest(4, 1, 6, "chests/windmill");
		b.barrel(4, 1, 7, "chests/barrel_supplies");
		b.bed(9, 1, 5, p.bed());
		Pieces.lantern(b, 5, 1, 4, false);
		Pieces.lantern(b, 5, 6, 5, false);
		b.barrel(9, 6, 9, "chests/windmill");
		Pieces.field(b, 0, 12, 14, 14, 0, Blocks.WHEAT);
		Pieces.path(b, 7, 0, 7, 0, 2);
		b.resident(CharacterVariant.BREADLOAF, 7, 1, 5, 7, 7, 9, at(9, 1, 5), null, false);
		b.resident(CharacterVariant.FARMER, 7, 1, 5, 7, 13, 9);
	}

	/** Badlands Mineshaft Saloon: a saloon with a bar, stools and tables, and a mine shaft under the back room. */
	static void badlandsSaloon(Blueprint b) {
		Palette p = b.palette;
		Pieces.room(b, 1, 1, 13, 11, 0, 5, "saloon");
		b.fill(1, 6, 1, 13, 6, 11, p.slab());
		// False front with a sign stripe.
		b.fill(1, 6, 1, 13, 8, 1, p.planks());
		b.fill(4, 7, 1, 10, 7, 1, Blocks.YELLOW_TERRACOTTA);
		// Swinging doors (gates) and windows.
		b.clear(6, 1, 1, 8, 2, 1);
		b.set(6, 1, 1, p.wood().gate());
		b.set(8, 1, 1, p.wood().gate());
		Pieces.windowsAround(b, 1, 1, 13, 11, 2, 7);
		// Bar along the back with barrels behind it, stools in front.
		for (int x = 2; x <= 8; x++) {
			b.set(x, 1, 8, Pieces.upsideDown(p.stairs(), Direction.NORTH));
			b.set(x, 1, 10, Blocks.BARREL);
		}
		for (int x = 3; x <= 7; x += 2) {
			b.set(x, 1, 6, p.fence());
			b.set(x, 2, 6, Blocks.BROWN_CARPET);
		}
		Pieces.table(b, 3, 1, 3, true, false);
		Pieces.table(b, 11, 1, 3, true, false);
		b.chest(2, 1, 9, "chests/badlands_saloon");
		b.barrel(9, 1, 10, "chests/barrel_supplies");
		Pieces.lantern(b, 5, 1, 4, false);
		Pieces.lantern(b, 12, 1, 8, false);
		// Mine shaft in the back corner.
		Pieces.ladder(b, 11, -7, 0, 9, Direction.NORTH);
		Pieces.stoneRoom(b, 4, 6, 12, 10, -8, 3, "mine");
		b.fill(4, -4, 6, 12, -4, 10, p.planks());
		Pieces.ladder(b, 11, -7, -4, 9, Direction.NORTH);
		b.fill(5, -7, 8, 10, -7, 8, Blocks.RAIL);
		b.chest(5, -7, 9, "chests/badlands_saloon");
		Pieces.lantern(b, 7, -7, 7, false);
		b.bed(12, 1, 5, p.bed());
		b.resident(CharacterVariant.MINER, 7, 1, 5, 7, 6, 10, null, null, true);
		b.resident(CharacterVariant.BREADLOAF, 7, 1, 5, 7, 6, 10, at(12, 1, 5), null, true);
	}

	// ================================================================ by the water

	/** Fishing Dock: a shack on the shore and a dock on posts out over the water, with lanterns. Fisherman. */
	static void fishingDock(Blueprint b) {
		Palette p = b.palette;
		b.foundation = false;
		b.groundLayer = false;
		b.skirt = false;
		for (int[] c : new int[][]{{1, 0}, {5, 0}, {1, 4}, {5, 4}}) {
			b.pillar(c[0], -1, c[1], p.log());
		}
		Pieces.room(b, 1, 0, 5, 4, 0, 3, "shack");
		Pieces.door(b, 3, 1, 4, Direction.SOUTH);
		Pieces.window(b, 1, 2, 2);
		Pieces.window(b, 5, 2, 2);
		Pieces.roof(b, Pieces.Roof.GABLE_Z, 1, 0, 5, 4, 3);
		b.bed(2, 1, 1, p.bed());
		b.barrel(4, 1, 1, "chests/fishing_barrel");
		b.chest(4, 1, 2, "chests/fishing_dock");
		Pieces.lantern(b, 4, 1, 3, false);
		Pieces.dock(b, 2, 5, 4, 16, 0);
		Pieces.lantern(b, 2, 2, 9, false);
		Pieces.lantern(b, 4, 2, 14, false);
		b.barrel(3, 1, 16, "chests/fishing_barrel");
		b.floor(3, 6, 3, 15, 1, "dock");
		b.resident(CharacterVariant.FISHERMAN, 3, 1, 9, 3, 10, 6, at(2, 1, 1), null, false);
	}

	/** Loaf Lighthouse: a striped tower with a ladder up to a glowing lamp room and a railed gallery. Fisherman. */
	static void lighthouse(Blueprint b) {
		Palette p = b.palette;
		b.fill(0, 0, 0, 8, 0, 8, p.bricks());
		for (int y = 1; y <= 16; y++) {
			BlockState stripe = (y - 1) / 3 % 2 == 0 ? Blocks.WHITE_CONCRETE.defaultBlockState() : Blocks.RED_CONCRETE.defaultBlockState();
			b.walls(2, 2, 6, 6, y, y, stripe);
		}
		b.clear(3, 1, 3, 5, 16, 5);
		b.fill(3, 0, 3, 5, 0, 5, p.planks());
		Pieces.door(b, 4, 1, 2, Direction.NORTH);
		Pieces.ladder(b, 4, 1, 17, 5, Direction.NORTH);
		Pieces.window(b, 2, 6, 4);
		Pieces.window(b, 6, 6, 4);
		Pieces.window(b, 2, 12, 4);
		Pieces.window(b, 6, 12, 4);
		b.fill(1, 17, 1, 7, 17, 7, p.bricks());
		Pieces.ladder(b, 4, 17, 17, 5, Direction.NORTH);
		b.walls(1, 1, 7, 7, 18, 18, Blocks.IRON_BARS.defaultBlockState());
		b.set(4, 18, 4, Blocks.SEA_LANTERN);
		b.set(4, 19, 4, Blocks.GLOWSTONE);
		b.fill(3, 20, 3, 5, 20, 5, p.stoneSlab());
		b.floor(2, 2, 6, 6, 18, "gallery");
		b.bed(3, 1, 3, p.bed());
		b.barrel(5, 1, 3, "chests/fishing_barrel");
		b.chest(5, 1, 4, "chests/loaf_lighthouse");
		b.set(3, 1, 5, Blocks.LANTERN);
		b.resident(CharacterVariant.FISHERMAN, 4, 1, 1, 4, 4, 10, at(3, 1, 3), null, false);
	}

	/** Pirate Ship: floating on the ocean with a captain's cabin, a hold below deck, a mast with sails and a crow's nest. */
	static void pirateShip(Blueprint b) {
		b.foundation = false;
		b.groundLayer = false;
		b.skirt = false;
		BlockState hull = Blocks.DARK_OAK_PLANKS.defaultBlockState();
		BlockState deck = Blocks.SPRUCE_PLANKS.defaultBlockState();
		for (int z = 1; z <= 21; z++) {
			int half = z <= 18 ? 3 : 21 - z;
			for (int y = -3; y <= 0; y++) {
				int hw = y == -3 ? Math.max(0, half - 1) : half;
				for (int x = 4 - hw; x <= 4 + hw; x++) {
					boolean edge = x == 4 - hw || x == 4 + hw || y == -3 || z == 1 || z == 21;
					b.set(x, y, z, edge ? hull : y == 0 ? deck : Blocks.AIR.defaultBlockState());
				}
			}
		}
		// Rails, with a gap amidships to climb aboard.
		for (int z = 1; z <= 21; z++) {
			int half = z <= 18 ? 3 : 21 - z;
			if (z != 10) {
				b.set(4 - half, 1, z, Blocks.DARK_OAK_FENCE);
				b.set(4 + half, 1, z, Blocks.DARK_OAK_FENCE);
			}
		}
		b.floor(2, 7, 6, 17, 1, "deck");
		// Captain's cabin at the stern.
		b.walls(2, 2, 6, 6, 1, 3, hull);
		b.clear(3, 1, 3, 5, 3, 5);
		b.fill(2, 4, 2, 6, 4, 6, Blocks.DARK_OAK_SLAB);
		Pieces.door(b, 4, 1, 6, Direction.SOUTH);
		b.bed(3, 1, 3, Blocks.RED_BED);
		b.chest(5, 1, 3, "chests/pirate_ship");
		Pieces.lantern(b, 5, 1, 5, false);
		b.floor(3, 3, 5, 5, 1, "cabin");
		// The hold: a hatch and ladder down, loot and barrels.
		Pieces.ladder(b, 4, -2, 0, 8, Direction.NORTH);
		b.floor(3, 9, 5, 16, -2, "hold");
		b.chest(3, -2, 14, "chests/pirate_ship");
		b.barrel(5, -2, 14, "chests/barrel_supplies");
		b.barrel(5, -2, 12, "chests/fishing_barrel");
		Pieces.lantern(b, 4, -2, 16, false);
		// Mast, sails and crow's nest.
		b.fill(4, 1, 13, 4, 12, 13, Blocks.DARK_OAK_LOG);
		Pieces.ladder(b, 4, 1, 11, 12, Direction.NORTH);
		b.fill(1, 5, 14, 7, 10, 14, Blocks.WHITE_WOOL);
		b.fill(3, 11, 12, 5, 11, 14, deck);
		b.set(4, 11, 12, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.NORTH));
		b.walls(3, 12, 5, 14, 12, 12, Blocks.DARK_OAK_FENCE.defaultBlockState());
		b.set(4, 12, 12, Blocks.AIR);
		b.set(4, 13, 13, Blocks.BLACK_BANNER);
		b.fill(4, 1, 22, 4, 1, 22, Blocks.DARK_OAK_FENCE);
		int members = 2 + b.random.nextInt(2);
		for (int i = 0; i < members; i++) {
			b.resident(CharacterVariant.PIRATE, 4, 1, 9, 4, 11, 8, i == 0 ? at(3, 1, 3) : null, null, true);
		}
	}

	/** Smugglers' Cove: a beach shack full of crates and a little dock. A Pirate and a Bandit share the loot. */
	static void smugglersCove(Blueprint b) {
		Palette p = b.palette;
		b.skirt = false;
		b.groundLayer = false;
		b.foundation = false;
		for (int[] c : new int[][]{{1, 1}, {7, 1}, {1, 6}, {7, 6}}) {
			b.pillar(c[0], -1, c[1], p.log());
		}
		Pieces.room(b, 1, 1, 7, 6, 0, 3, "shack");
		Pieces.door(b, 4, 1, 6, Direction.SOUTH);
		Pieces.window(b, 1, 2, 3);
		Pieces.window(b, 7, 2, 3);
		Pieces.roof(b, Pieces.Roof.GABLE_X, 1, 1, 7, 6, 3);
		b.chest(2, 1, 2, "chests/smugglers_cove");
		b.chest(3, 1, 2, "chests/smugglers_cove");
		b.barrel(6, 1, 2, "chests/barrel_supplies");
		b.barrel(6, 1, 3, "chests/barrel_supplies");
		b.bed(2, 1, 4, p.bed());
		Pieces.lantern(b, 5, 1, 2, false);
		Pieces.dock(b, 9, 6, 11, 16, 0);
		b.barrel(10, 1, 16, "chests/fishing_barrel");
		b.set(10, 1, 15, Blocks.BLACK_BANNER);
		b.floor(10, 7, 10, 14, 1, "dock");
		b.resident(CharacterVariant.PIRATE, 10, 1, 8, 6, 8, 8, at(2, 1, 4), null, true);
		b.resident(CharacterVariant.BANDIT, 10, 1, 8, 6, 8, 8, null, null, true);
	}

	/** Shipwreck Camp: Pirates camping beside the broken half of their ship. */
	static void shipwreckCamp(Blueprint b) {
		BlockState hull = Blocks.DARK_OAK_PLANKS.defaultBlockState();
		for (int x = 2; x <= 11; x++) {
			for (int y = 1; y <= 3; y++) {
				for (int z : new int[]{2, 7}) {
					if (y == 1 || b.random.nextInt(5) != 0) {
						b.set(x, y, z, hull);
					}
				}
			}
		}
		b.fill(2, 1, 3, 2, 3, 6, hull);
		b.fill(3, 0, 3, 11, 0, 6, Blocks.SPRUCE_PLANKS);
		b.clear(3, 1, 3, 11, 3, 6);
		b.fill(6, 1, 4, 6, 6, 4, Blocks.DARK_OAK_LOG);
		b.fill(7, 4, 4, 7, 5, 4, Blocks.WHITE_WOOL);
		b.chest(4, 1, 5, "chests/shipwreck_camp");
		b.chest(9, 1, 5, "chests/shipwreck_camp");
		b.barrel(10, 1, 3, "chests/fishing_barrel");
		b.floor(3, 3, 11, 6, 1, "wreck");
		Pieces.tent(b, 12, 4, 6, Blocks.BLACK_WOOL.defaultBlockState());
		b.bed(13, 1, 5, Blocks.BLACK_BED);
		Pieces.campfire(b, 14, 1, 2);
		b.set(16, 1, 0, Blocks.BLACK_BANNER);
		b.resident(CharacterVariant.PIRATE, 14, 1, 3, 10, 4, 10, at(13, 1, 5), null, true);
		b.resident(CharacterVariant.PIRATE, 14, 1, 3, 10, 4, 10, null, null, true);
	}

	// ================================================================ built while the game runs

	/**
	 * Village Campsite (built at the edge of a village): FelixSyn's camp. A walk-in tent with his and Harley's beds,
	 * a campfire with benches, two training dummies, and a ladder-up jump platform over hay bales for mace practice.
	 */
	static void villageCampsite(Blueprint b) {
		b.fill(1, 0, 1, 11, 0, 9, Blocks.COARSE_DIRT);
		Pieces.tent(b, 1, 4, 6, wool(b, DARK_WOOL));
		b.bed(2, 1, 5, Blocks.BLACK_BED);
		b.bed(4, 1, 5, Blocks.PINK_BED);
		b.chest(2, 1, 8, "chests/village_campsite");
		b.barrel(4, 1, 8, "chests/barrel_supplies");
		Pieces.lantern(b, 3, 1, 8, false);
		Pieces.campfire(b, 8, 1, 3);
		b.mob(10, 1, 6, "dummy");
		b.mob(10, 1, 8, "dummy");
		b.set(12, 1, 5, Blocks.TARGET);
		// Jump platform: climb the log post, jump onto the hay.
		b.fill(12, 1, 10, 12, 3, 10, b.palette.log());
		Pieces.ladder(b, 12, 1, 3, 9, Direction.NORTH);
		b.fill(10, 1, 9, 11, 1, 10, Blocks.HAY_BLOCK);
		b.floor(12, 10, 12, 10, 4, "jump platform");
		Pieces.path(b, 7, 0, 9, 0, 0);
		b.resident(CharacterVariant.FELIXSYN, 8, 1, 5, 7, 5, 24, at(2, 1, 5), null, false);
	}

	/**
	 * Bastion Throne Room (carved into a bastion the first time a player walks in): Master BreadLoaf's closed
	 * blackstone-and-gold hall, so no lava gets in. A gilded aisle up to the throne, pillars with soul lanterns, and
	 * a treasure chest on each side of the throne.
	 */
	static void bastionThroneRoom(Blueprint b) {
		// Inside a bastion: no terrain leveling, the room is its own closed box.
		b.clearAbove = false;
		b.groundLayer = false;
		b.foundation = false;
		b.skirt = false;
		b.fill(0, 0, 0, 12, 8, 12, Blocks.POLISHED_BLACKSTONE_BRICKS);
		b.clear(1, 1, 1, 11, 7, 11);
		b.fill(1, 0, 1, 11, 0, 11, Blocks.POLISHED_BLACKSTONE);
		b.fill(6, 0, 0, 6, 0, 9, Blocks.GILDED_BLACKSTONE);
		b.fill(6, 1, 1, 6, 1, 8, Blocks.RED_CARPET);
		// Gold trim around the walls and a gold-capped entrance arch.
		b.fill(0, 4, 1, 0, 4, 11, Blocks.GILDED_BLACKSTONE);
		b.fill(12, 4, 1, 12, 4, 11, Blocks.GILDED_BLACKSTONE);
		b.fill(1, 4, 12, 11, 4, 12, Blocks.GILDED_BLACKSTONE);
		b.clear(5, 1, 0, 7, 3, 0);
		b.fill(5, 4, 0, 7, 4, 0, Blocks.GOLD_BLOCK);
		for (int[] c : new int[][]{{3, 3}, {9, 3}, {3, 7}, {9, 7}}) {
			b.fill(c[0], 1, c[1], c[0], 6, c[1], Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
			b.set(c[0], 7, c[1], Blocks.SOUL_LANTERN);
		}
		b.set(6, 7, 5, Blocks.SOUL_LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
		// The throne: a blackstone seat between gold arms, with a tall gold back and a gilded crown.
		b.set(6, 1, 10, Pieces.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS.defaultBlockState(), Direction.SOUTH));
		b.set(5, 1, 10, Blocks.GOLD_BLOCK);
		b.set(7, 1, 10, Blocks.GOLD_BLOCK);
		b.fill(6, 1, 11, 6, 4, 11, Blocks.GOLD_BLOCK);
		b.set(5, 1, 11, Blocks.GOLD_BLOCK);
		b.set(7, 1, 11, Blocks.GOLD_BLOCK);
		b.set(6, 5, 11, Blocks.GILDED_BLACKSTONE);
		b.chest(2, 1, 10, "chests/bastion_throne_room");
		b.chest(10, 1, 10, "chests/bastion_throne_room");
		b.set(1, 1, 11, Blocks.GOLD_BLOCK);
		b.set(11, 1, 11, Blocks.GOLD_BLOCK);
		b.floor(1, 1, 11, 9, 1, "throne hall");
		b.resident(CharacterVariant.MASTER, 6, 1, 7, 6, 7, 5);
	}
}
