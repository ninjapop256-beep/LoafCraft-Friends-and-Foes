package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Update 2's new Nether structures (part 8, batch 6). Built on the Nether floor from the Nether palette (crimson or
 * warped wood, blackstone, soul lanterns). No beds: they explode in the Nether.
 */
final class NetherBlueprints {
	private NetherBlueprints() {
	}

	private static double dist(int x, int z, double cx, double cz) {
		return Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
	}

	private static void lampPost(Blueprint b, int x, int z) {
		b.set(x, 1, z, b.palette.stoneWall());
		Pieces.lantern(b, x, 2, z, false);
	}

	/** A flat stone roof with a slab rim. */
	private static void flatRoof(Blueprint b, int x0, int z0, int x1, int z1, int y) {
		b.fill(x0, y, z0, x1, y, z1, b.palette.bricks());
		b.walls(x0, z0, x1, z1, y + 1, y + 1, b.palette.stoneSlab());
	}

	/** Hellfire Bakery: a blackstone bakery with a lava oven glowing behind glass, smokers and hay "Nether bread". */
	static void hellfireBakery(Blueprint b) {
		Palette p = b.palette;
		Pieces.stoneRoom(b, 1, 1, 11, 8, 0, 4, "hellfire bakery");
		flatRoof(b, 1, 1, 11, 8, 5);
		b.set(6, 5, 4, Blocks.SHROOMLIGHT);
		Pieces.door(b, 6, 1, 1, Direction.NORTH);
		Pieces.window(b, 3, 2, 1);
		Pieces.window(b, 9, 2, 1);
		Pieces.path(b, 6, 0, 6, 0, 0);
		// The lava oven: lava sealed in stone behind glass in the back wall.
		b.fill(4, 0, 9, 8, 3, 10, p.stoneBase());
		b.fill(5, 1, 9, 7, 1, 9, Blocks.LAVA);
		b.fill(5, 1, 8, 7, 1, 8, Blocks.GLASS);
		for (int x = 3; x <= 8; x++) {
			b.set(x, 1, 4, Pieces.topSlab(p.stoneSlab()));
		}
		b.set(4, 2, 4, Blocks.POTTED_CRIMSON_FUNGUS);
		b.set(7, 2, 4, Blocks.POTTED_WARPED_FUNGUS);
		b.set(2, 1, 7, Blocks.SMOKER);
		b.set(3, 1, 7, Blocks.SMOKER);
		b.chest(4, 1, 7, "chests/hellfire_bakery");
		b.set(9, 1, 7, Blocks.HAY_BLOCK);
		b.set(10, 1, 7, Blocks.HAY_BLOCK);
		b.set(10, 2, 7, Blocks.HAY_BLOCK);
		Pieces.lantern(b, 2, 1, 2, false);
		b.resident(CharacterVariant.NETHER, 6, 1, 0, 6, 4, 10);
	}

	/** Crimson Outpost: a palisade of stems with crenels, a watchtower with a ladder, a tent and lamp posts. */
	static void crimsonOutpost(Blueprint b) {
		Palette p = b.palette;
		b.walls(1, 1, 13, 13, 1, 3, p.log());
		for (int i = 1; i <= 13; i += 2) {
			b.set(i, 4, 1, p.log());
			b.set(i, 4, 13, p.log());
			b.set(1, 4, i, p.log());
			b.set(13, 4, i, p.log());
		}
		b.clear(7, 1, 1, 7, 2, 1);
		Pieces.tower(b, 8, 8, 5, 0, 10, 5, true);
		Pieces.door(b, 10, 1, 8, Direction.NORTH);
		b.walls(8, 8, 12, 12, 11, 11, p.fence());
		b.chest(11, 11, 9, "chests/crimson_outpost");
		Pieces.tent(b, 2, 6, 5, Blocks.RED_WOOL.defaultBlockState());
		b.barrel(3, 1, 8, "chests/barrel_supplies");
		b.barrel(5, 1, 8, "chests/barrel_supplies");
		lampPost(b, 4, 3);
		lampPost(b, 10, 3);
		b.floor(2, 2, 12, 5, 1, "yard");
		for (int i = 0; i < 2; i++) {
			b.resident(CharacterVariant.NETHER, 7, 1, 4, 7, 7, 10, null, null, true);
		}
	}

	/** Witchy Lily's Nether Garden: rows of nether wart on soul sand, a fungus patch, a brewing gazebo. */
	static void netherWartGarden(Blueprint b) {
		Palette p = b.palette;
		BlockState wart = Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, 3);
		for (int x = 1; x <= 5; x++) {
			for (int z = 1; z <= 11; z++) {
				if (z % 2 == 1) {
					b.set(x, 0, z, Blocks.SOUL_SAND);
					b.set(x, 1, z, wart);
				} else {
					b.set(x, 0, z, p.path());
				}
			}
		}
		for (int x = 8; x <= 11; x++) {
			for (int z = 8; z <= 11; z++) {
				boolean crimson = (x + z) % 2 == 0;
				b.set(x, 0, z, crimson ? Blocks.CRIMSON_NYLIUM : Blocks.WARPED_NYLIUM);
				if (b.random.nextBoolean()) {
					b.set(x, 1, z, crimson ? Blocks.CRIMSON_FUNGUS : Blocks.WARPED_FUNGUS);
				}
			}
		}
		for (int[] c : new int[][]{{7, 1}, {11, 1}, {7, 5}, {11, 5}}) {
			b.fill(c[0], 1, c[1], c[0], 3, c[1], p.stoneWall());
		}
		b.fill(7, 4, 1, 11, 4, 5, p.stoneSlab());
		b.set(9, 1, 3, Blocks.BREWING_STAND);
		b.set(8, 1, 3, Blocks.WATER_CAULDRON);
		b.chest(10, 1, 4, "chests/nether_wart_garden");
		Pieces.lantern(b, 9, 3, 2, true);
		b.floor(8, 2, 10, 4, 1, "gazebo");
		b.resident(CharacterVariant.WITCHY, 6, 1, 6, 6, 6, 10);
	}

	/** Lava Fishing Spot: a dock out over a lava pool (railed, of course), a little blackstone hut with the catch. */
	static void lavaFishingSpot(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				double d = dist(x, z, 8, 5);
				if (d <= 4.0) {
					b.set(x, -1, z, p.stoneBase());
					b.set(x, 0, z, Blocks.LAVA);
				} else if (d <= 5.0) {
					b.set(x, 0, z, p.stoneBase());
					b.set(x, 1, z, p.stoneWall());
				}
			}
		}
		b.fill(7, 0, 0, 9, 0, 6, p.stoneFloor());
		b.clear(7, 1, 0, 9, 1, 1);
		b.fill(7, 1, 2, 7, 1, 6, p.fence());
		b.fill(9, 1, 2, 9, 1, 6, p.fence());
		b.set(8, 1, 6, p.fence());
		Pieces.stoneRoom(b, 0, 9, 4, 12, 0, 3, "fishing hut");
		flatRoof(b, 0, 9, 4, 12, 4);
		Pieces.door(b, 2, 1, 9, Direction.NORTH);
		b.chest(1, 1, 11, "chests/lava_fishing_spot");
		b.barrel(3, 1, 11, "chests/fishing_barrel");
		b.floor(8, 4, 8, 5, 1, "dock end");
		b.resident(CharacterVariant.NETHER, 8, 1, 1, 8, 4, 10);
	}

	/** Basalt Watchtower: a tall blackstone tower with polished basalt corners, a ladder through three floors, a lookout. */
	static void basaltWatchtower(Blueprint b) {
		Palette p = b.palette;
		int top = 16;
		Pieces.tower(b, 2, 2, 5, 0, top, 8, true);
		for (int y = 1; y <= top; y++) {
			for (int[] c : new int[][]{{2, 2}, {6, 2}, {2, 6}, {6, 6}}) {
				b.set(c[0], y, c[1], Blocks.POLISHED_BASALT);
			}
		}
		Pieces.door(b, 4, 1, 2, Direction.NORTH);
		b.walls(2, 2, 6, 6, top + 1, top + 1, p.fence());
		Pieces.lantern(b, 2, top + 2, 2, false);
		Pieces.lantern(b, 6, top + 2, 6, false);
		b.chest(3, top + 1, 3, "chests/basalt_watchtower");
		b.resident(CharacterVariant.NETHER, 5, top + 1, 3, 4, 4, 6);
	}

	/** Soul Camp: soul campfire with benches, two black tents, a bone "fossil", soul lanterns on posts. */
	static void soulCamp(Blueprint b) {
		Palette p = b.palette;
		for (int x = 0; x < b.width; x++) {
			for (int z = 0; z < b.depth; z++) {
				if (b.random.nextInt(3) == 0) {
					b.set(x, 0, z, Blocks.SOUL_SOIL);
				}
			}
		}
		b.set(7, 1, 4, Blocks.SOUL_CAMPFIRE);
		b.set(5, 1, 4, Pieces.logAlong(p.log(), Direction.Axis.Z));
		b.set(9, 1, 4, Pieces.logAlong(p.log(), Direction.Axis.Z));
		b.set(7, 1, 2, Pieces.logAlong(p.log(), Direction.Axis.X));
		Pieces.tent(b, 1, 7, 5, Blocks.BLACK_WOOL.defaultBlockState());
		Pieces.tent(b, 9, 7, 5, Blocks.BLACK_WOOL.defaultBlockState());
		b.chest(4, 1, 10, "chests/soul_camp");
		b.barrel(10, 1, 10, "chests/barrel_supplies");
		b.fill(10, 1, 1, 13, 1, 1, Pieces.logAlong(Blocks.BONE_BLOCK.defaultBlockState(), Direction.Axis.X));
		b.fill(11, 2, 1, 11, 3, 1, Blocks.BONE_BLOCK);
		b.fill(13, 2, 1, 13, 3, 1, Blocks.BONE_BLOCK);
		lampPost(b, 1, 1);
		lampPost(b, 13, 5);
		b.floor(6, 5, 8, 6, 1, "camp");
		for (int i = 0; i < 2; i++) {
			b.resident(CharacterVariant.NETHER, 7, 1, 6, 7, 6, 10, null, null, true);
		}
	}

	/** Gold Trading Post: a blackstone hall with gilded stripes, a counter with gold on it, gold and chests behind. */
	static void goldTradingPost(Blueprint b) {
		Palette p = b.palette;
		Pieces.stoneRoom(b, 1, 1, 11, 8, 0, 4, "trading post");
		flatRoof(b, 1, 1, 11, 8, 5);
		for (int x = 1; x <= 11; x += 2) {
			b.set(x, 5, 1, Blocks.GILDED_BLACKSTONE);
			b.set(x, 5, 8, Blocks.GILDED_BLACKSTONE);
		}
		b.clear(5, 1, 1, 7, 2, 1);
		for (int x = 2; x <= 9; x++) {
			b.set(x, 1, 4, Pieces.topSlab(p.stoneSlab()));
		}
		b.set(3, 2, 4, Blocks.GOLD_BLOCK);
		b.set(8, 2, 4, Blocks.GOLD_BLOCK);
		b.set(2, 1, 7, Blocks.GOLD_BLOCK);
		b.set(10, 1, 7, Blocks.GOLD_BLOCK);
		b.chest(3, 1, 7, "chests/gold_trading_post");
		b.chest(9, 1, 7, "chests/gold_trading_post");
		b.barrel(6, 1, 7, "chests/barrel_supplies");
		Pieces.lantern(b, 4, 4, 3, true);
		Pieces.lantern(b, 8, 4, 3, true);
		b.floor(3, 5, 9, 6, 1, "behind the counter");
		b.resident(CharacterVariant.NETHER, 6, 1, 6, 6, 5, 6);
	}

	/** Strider Stables: a stone-rimmed lava channel with striders in it, a railed walkway, the stable hut. */
	static void striderStables(Blueprint b) {
		Palette p = b.palette;
		for (int z = 1; z <= 11; z++) {
			for (int x = 4; x <= 7; x++) {
				b.set(x, -1, z, p.stoneBase());
				boolean channel = (x == 5 || x == 6) && z >= 2 && z <= 10;
				b.set(x, 0, z, channel ? Blocks.LAVA.defaultBlockState() : p.stoneBase());
			}
			b.set(4, 1, z, p.fence());
			b.set(7, 1, z, p.fence());
		}
		b.mob(5, 0, 4, "strider");
		b.mob(6, 0, 8, "strider");
		Pieces.stoneRoom(b, 9, 2, 13, 7, 0, 3, "stable hut");
		flatRoof(b, 9, 2, 13, 7, 4);
		Pieces.door(b, 9, 1, 4, Direction.WEST);
		b.chest(12, 1, 6, "chests/strider_stables");
		b.barrel(12, 1, 3, "chests/barrel_supplies");
		lampPost(b, 8, 1);
		lampPost(b, 8, 11);
		b.floor(8, 2, 8, 10, 1, "walkway");
		b.resident(CharacterVariant.NETHER, 8, 1, 9, 8, 6, 8);
	}
}
