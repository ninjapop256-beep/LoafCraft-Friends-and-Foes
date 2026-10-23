package com.itzbreadloaf.worldgen;

import com.itzbreadloaf.brain.LilyBrain;
import com.itzbreadloaf.character.CharacterVariant;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Lily, Madison, shared and special structures: greenhouse, lakeside gazebo, farmstead, orchard, market stall,
 * Cousins' Camp, PvP Arena, Giant Loaf, Nether Mace Training Ground, End Mace Tower, and five extras (windmill,
 * Sweaty Dojo, Flower Shrine, Smugglers' Cove, Pumpkin Patch).
 */
final class SpecialHomes {
	static final Block[] FLOWERS = {Blocks.POPPY, Blocks.CORNFLOWER, Blocks.ALLIUM, Blocks.OXEYE_DAISY,
			Blocks.AZURE_BLUET, Blocks.LILY_OF_THE_VALLEY, Blocks.PINK_TULIP, Blocks.DANDELION, Blocks.RED_TULIP};

	private SpecialHomes() {
	}

	static BlockState air() {
		return Blocks.AIR.defaultBlockState();
	}

	static BlockState hanging() {
		return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
	}

	/** Clears the space and lays a ground layer with a foundation underneath. */
	static void site(HomePiece p, WorldGenLevel level, BoundingBox box, int maxX, int height, int maxZ, BlockState ground) {
		p.fill(level, box, 0, 1, 0, maxX, height, maxZ, air());
		p.fill(level, box, 0, 0, 0, maxX, 0, maxZ, ground);
		p.foundation(level, box, 0, 0, maxX, maxZ, Blocks.DIRT.defaultBlockState());
	}

	/** A farm animal standing at local coordinates (kept forever, like structure animals). */
	static void animal(HomePiece p, WorldGenLevel level, BoundingBox box, EntityType<? extends Mob> type, int x, int y, int z) {
		BlockPos at = p.pos(x, y, z);
		if (!box.isInside(at)) {
			return;
		}
		Mob mob = type.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
		if (mob != null) {
			mob.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
			mob.setPersistenceRequired();
			level.addFreshEntityWithPassengers(mob);
		}
	}

	static void one(HomePiece p, WorldGenLevel level, BoundingBox box, CharacterVariant type, int x, int y, int z,
			int homeX, int homeZ, int radius, BlockPos bed, BlockPos pantry) {
		BlockPos spawn = p.pos(x, y, z);
		if (p.claimSpawn(spawn, box)) {
			BlockPos home = p.pos(homeX, y, homeZ);
			p.placeMarker(level, spawn, m -> m.addResident(type, spawn, home, radius, bed, null, pantry));
		}
	}

	// ------------------------------------------------------------ ImLilyLol

	/** Glass greenhouse with flower planters and saplings. */
	static void greenhouse(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 8, 6, 10, Blocks.GRASS_BLOCK.defaultBlockState());
		p.fill(level, box, 1, 0, 1, 7, 0, 9, Blocks.STONE_BRICKS.defaultBlockState());
		p.walls(level, box, 1, 1, 7, 9, 1, 4, Blocks.GLASS.defaultBlockState());
		for (int[] c : new int[][]{{1, 1}, {7, 1}, {1, 9}, {7, 9}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 5, c[1], Blocks.WHITE_CONCRETE.defaultBlockState());
		}
		p.fill(level, box, 1, 5, 1, 7, 5, 9, Blocks.GLASS.defaultBlockState());
		p.door(level, box, 4, 1, 1, Blocks.BIRCH_DOOR);
		// Planters along both sides: dirt beds with flowers and saplings.
		for (int z = 3; z <= 8; z++) {
			for (int x : new int[]{2, 6}) {
				p.put(level, box, Blocks.DIRT.defaultBlockState(), x, 0, z);
				Block plant = z % 3 == 0 ? (random.nextBoolean() ? Blocks.CHERRY_SAPLING : Blocks.BIRCH_SAPLING) : FLOWERS[random.nextInt(FLOWERS.length)];
				p.put(level, box, plant.defaultBlockState(), x, 1, z);
			}
		}
		p.put(level, box, Blocks.WATER.defaultBlockState(), 4, 0, 8);
		p.chest(level, box, random, 4, 1, 9 - 1, ModStructures.lootTable("chests/greenhouse"));
		p.barrel(level, box, random, 3, 1, 8, ModStructures.BARREL_LOOT);
		p.put(level, box, hanging(), 4, 4, 5);
		one(p, level, box, CharacterVariant.GARDENER, 4, 1, 3, 4, 5, LilyBrain.HOME_RADIUS, null, null);
	}

	/** A gazebo by a little pond, with benches and a lectern: Lily's calm reading spot. */
	static void gazebo(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 12, 6, 12, Blocks.GRASS_BLOCK.defaultBlockState());
		// Pond.
		for (int x = 0; x <= 12; x++) {
			for (int z = 8; z <= 12; z++) {
				double d = Math.sqrt((x - 6) * (x - 6) + (z - 10) * (z - 10) * 2.0);
				if (d < 5.5) {
					p.put(level, box, Blocks.DIRT.defaultBlockState(), x, -1, z);
					p.put(level, box, Blocks.WATER.defaultBlockState(), x, 0, z);
				}
			}
		}
		p.put(level, box, Blocks.LILY_PAD.defaultBlockState(), 4, 1, 10);
		p.put(level, box, Blocks.LILY_PAD.defaultBlockState(), 8, 1, 11);
		// Gazebo: posts, a slab roof, benches, a lectern.
		p.fill(level, box, 3, 0, 1, 9, 0, 6, Blocks.BIRCH_PLANKS.defaultBlockState());
		for (int[] c : new int[][]{{3, 1}, {9, 1}, {3, 6}, {9, 6}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 3, c[1], Blocks.BIRCH_FENCE.defaultBlockState());
		}
		p.fill(level, box, 2, 4, 0, 10, 4, 7, Blocks.CHERRY_SLAB.defaultBlockState());
		p.fill(level, box, 4, 5, 2, 8, 5, 5, Blocks.CHERRY_SLAB.defaultBlockState());
		BlockState bench = Blocks.BIRCH_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
		p.fill(level, box, 4, 1, 6, 8, 1, 6, bench);
		p.put(level, box, Blocks.LECTERN.defaultBlockState(), 6, 1, 2);
		p.put(level, box, hanging(), 6, 3, 4);
		p.chest(level, box, random, 4, 1, 2, ModStructures.lootTable("chests/lakeside_gazebo"));
		for (int i = 0; i < 6; i++) {
			int x = random.nextInt(13);
			p.put(level, box, FLOWERS[random.nextInt(FLOWERS.length)].defaultBlockState(), x, 1, random.nextBoolean() ? 0 : 7);
		}
		CharacterVariant type = random.nextBoolean() ? CharacterVariant.BOOKWORM : CharacterVariant.LILY;
		one(p, level, box, type, 6, 1, 3, 6, 4, LilyBrain.HOME_RADIUS, null, null);
	}

	/** Flower Shrine: lanterns on posts around flower beds and a small altar. */
	static void flowerShrine(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 8, 6, 8, Blocks.MOSS_BLOCK.defaultBlockState());
		p.fill(level, box, 3, 0, 3, 5, 0, 5, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
		p.put(level, box, Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState(), 4, 1, 4);
		p.put(level, box, Blocks.POTTED_LILY_OF_THE_VALLEY.defaultBlockState(), 4, 2, 4);
		for (int[] c : new int[][]{{1, 1}, {7, 1}, {1, 7}, {7, 7}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 2, c[1], Blocks.CHERRY_FENCE.defaultBlockState());
			p.put(level, box, Blocks.LANTERN.defaultBlockState(), c[0], 3, c[1]);
		}
		for (int x = 0; x <= 8; x++) {
			for (int z = 0; z <= 8; z++) {
				boolean ring = (x == 2 || x == 6 || z == 2 || z == 6) && x >= 2 && x <= 6 && z >= 2 && z <= 6;
				if (ring && random.nextInt(4) != 0) {
					p.put(level, box, FLOWERS[random.nextInt(FLOWERS.length)].defaultBlockState(), x, 1, z);
				}
			}
		}
		p.put(level, box, Blocks.PINK_PETALS.defaultBlockState(), 0, 1, 4);
		p.chest(level, box, random, 4, 1, 6 + 1, ModStructures.lootTable("chests/flower_shrine"));
		one(p, level, box, CharacterVariant.GARDENER, 4, 1, 1, 4, 4, 8, null, null);
	}

	// ------------------------------------------------------------ Madison

	/** Farmstead: a red barn, fenced pens with animals, crop fields and a big pantry Madison watches. */
	static void farmstead(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 24, 10, 20, Blocks.GRASS_BLOCK.defaultBlockState());
		// Barn.
		BlockState red = Blocks.RED_TERRACOTTA.defaultBlockState();
		p.fill(level, box, 1, 0, 1, 10, 0, 10, Blocks.SPRUCE_PLANKS.defaultBlockState());
		p.walls(level, box, 1, 1, 10, 10, 1, 5, red);
		for (int[] c : new int[][]{{1, 1}, {10, 1}, {1, 10}, {10, 10}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 6, c[1], Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
		}
		for (int y = 6; y <= 9; y++) {
			int inset = y - 6;
			p.fill(level, box, 1 + inset, y, 1, 10 - inset, y, 10, Blocks.DARK_OAK_PLANKS.defaultBlockState());
		}
		p.fill(level, box, 2, 6, 2, 9, 6, 9, air());
		p.fill(level, box, 4, 1, 1, 7, 4, 1, air());
		p.fill(level, box, 2, 1, 7, 4, 2, 9, Blocks.HAY_BLOCK.defaultBlockState());
		p.chest(level, box, random, 8, 1, 9, ModStructures.MADISON_PANTRY);
		p.chest(level, box, random, 9, 1, 9, ModStructures.MADISON_PANTRY);
		p.barrel(level, box, random, 9, 1, 7, ModStructures.BARREL_LOOT);
		p.bed(level, box, 8, 1, 4, Blocks.YELLOW_BED);
		p.put(level, box, hanging(), 5, 5, 5);
		// Animal pens.
		p.walls(level, box, 12, 1, 18, 9, 1, 1, Blocks.OAK_FENCE.defaultBlockState());
		p.walls(level, box, 19, 1, 23, 9, 1, 1, Blocks.OAK_FENCE.defaultBlockState());
		p.put(level, box, Blocks.OAK_FENCE_GATE.defaultBlockState(), 15, 1, 9);
		p.put(level, box, Blocks.OAK_FENCE_GATE.defaultBlockState(), 21, 1, 9);
		animal(p, level, box, EntityType.COW, 14, 1, 4);
		animal(p, level, box, EntityType.COW, 16, 1, 6);
		animal(p, level, box, EntityType.SHEEP, 21, 1, 4);
		animal(p, level, box, EntityType.SHEEP, 22, 1, 6);
		animal(p, level, box, EntityType.CHICKEN, 13, 1, 7);
		animal(p, level, box, EntityType.CHICKEN, 20, 1, 3);
		p.put(level, box, Blocks.WATER_CAULDRON.defaultBlockState(), 17, 1, 2);
		// Crop fields with a water channel.
		BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);
		Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS};
		for (int x = 12; x <= 23; x++) {
			for (int z = 12; z <= 19; z++) {
				if (z == 15) {
					p.put(level, box, Blocks.WATER.defaultBlockState(), x, 0, z);
					continue;
				}
				p.put(level, box, farmland, x, 0, z);
				net.minecraft.world.level.block.CropBlock crop = (net.minecraft.world.level.block.CropBlock) crops[(x / 3) % crops.length];
				p.put(level, box, crop.getStateForAge(random.nextInt(crop.getMaxAge() + 1)), x, 1, z);
			}
		}
		BlockPos bed = p.pos(8, 1, 5);
		BlockPos pantry = p.pos(8, 1, 9);
		one(p, level, box, CharacterVariant.FARMER, 6, 1, 3, 12, 11, 12, bed, pantry);
	}

	/** Orchard and beehive farm: rows of blossoming trees and hives over campfires. */
	static void orchard(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 18, 8, 18, Blocks.GRASS_BLOCK.defaultBlockState());
		for (int tx = 3; tx <= 15; tx += 6) {
			for (int tz = 3; tz <= 15; tz += 6) {
				p.fill(level, box, tx, 1, tz, tx, 3, tz, Blocks.OAK_LOG.defaultBlockState());
				for (int dx = -2; dx <= 2; dx++) {
					for (int dz = -2; dz <= 2; dz++) {
						for (int y = 3; y <= 5; y++) {
							int r = Math.abs(dx) + Math.abs(dz) + (y - 4) * (y - 4) * 2;
							if ((dx != 0 || dz != 0 || y > 3) && r <= 3 && random.nextInt(6) != 0) {
								Block leaf = random.nextInt(3) == 0 ? Blocks.FLOWERING_AZALEA_LEAVES : Blocks.OAK_LEAVES;
								p.put(level, box, leaf.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), tx + dx, y, tz + dz);
							}
						}
					}
				}
			}
		}
		// Beehives on the paths between the trees.
		p.beehive(level, box, random, 6, 1, 9);
		p.beehive(level, box, random, 12, 1, 9);
		p.beehive(level, box, random, 9, 1, 6);
		p.beehive(level, box, random, 9, 1, 12);
		for (int i = 0; i < 20; i++) {
			int x = random.nextInt(19);
			int z = random.nextInt(19);
			p.put(level, box, FLOWERS[random.nextInt(FLOWERS.length)].defaultBlockState(), x, 1, z);
		}
		p.chest(level, box, random, 9, 1, 9, ModStructures.lootTable("chests/orchard"));
		p.barrel(level, box, random, 10, 1, 9, ModStructures.BARREL_LOOT);
		one(p, level, box, CharacterVariant.BEEKEEPER, 9, 1, 8, 9, 9, 10, null, null);
	}

	/** Market stall: a striped awning over a counter with produce barrels. Merchant Madison. */
	static void marketStall(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 8, 5, 6, Blocks.DIRT_PATH.defaultBlockState());
		for (int[] c : new int[][]{{1, 1}, {7, 1}, {1, 5}, {7, 5}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 3, c[1], Blocks.OAK_FENCE.defaultBlockState());
		}
		for (int x = 1; x <= 7; x++) {
			Block wool = x % 2 == 0 ? Blocks.WHITE_WOOL : Blocks.LIME_WOOL;
			p.fill(level, box, x, 4, 1, x, 4, 5, wool.defaultBlockState());
		}
		p.fill(level, box, 2, 1, 2, 6, 1, 2, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(net.minecraft.world.level.block.SlabBlock.TYPE,
				net.minecraft.world.level.block.state.properties.SlabType.TOP));
		p.put(level, box, Blocks.MELON.defaultBlockState(), 2, 2, 2);
		p.put(level, box, Blocks.PUMPKIN.defaultBlockState(), 6, 2, 2);
		p.barrel(level, box, random, 2, 1, 4, ModStructures.lootTable("chests/market_stall"));
		p.barrel(level, box, random, 6, 1, 4, ModStructures.lootTable("chests/market_stall"));
		p.put(level, box, Blocks.LANTERN.defaultBlockState(), 4, 2, 2);
		one(p, level, box, CharacterVariant.MERCHANT, 4, 1, 3, 4, 3, 3, null, null);
	}

	/** Windmill: a stone base, a wooden tower and wool sails. Farmer Madison and a Village ItzBreadLoaf. */
	static void windmill(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 14, 16, 14, Blocks.GRASS_BLOCK.defaultBlockState());
		p.fill(level, box, 2, 0, 2, 8, 0, 8, Blocks.COBBLESTONE.defaultBlockState());
		p.walls(level, box, 2, 2, 8, 8, 1, 4, Blocks.COBBLESTONE.defaultBlockState());
		p.walls(level, box, 3, 3, 7, 7, 5, 10, Blocks.SPRUCE_PLANKS.defaultBlockState());
		p.fill(level, box, 3, 4, 3, 7, 4, 7, Blocks.SPRUCE_PLANKS.defaultBlockState());
		p.fill(level, box, 4, 11, 4, 6, 11, 6, Blocks.SPRUCE_SLAB.defaultBlockState());
		p.door(level, box, 5, 1, 2, Blocks.SPRUCE_DOOR);
		// Sails on the front: an X of white wool around a log hub.
		p.put(level, box, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z), 5, 8, 2);
		for (int i = 1; i <= 4; i++) {
			p.put(level, box, Blocks.WHITE_WOOL.defaultBlockState(), 5 + i, 8 + i, 1);
			p.put(level, box, Blocks.WHITE_WOOL.defaultBlockState(), 5 - i, 8 - i, 1);
			p.put(level, box, Blocks.WHITE_WOOL.defaultBlockState(), 5 + i, 8 - i, 1);
			p.put(level, box, Blocks.WHITE_WOOL.defaultBlockState(), 5 - i, 8 + i, 1);
		}
		p.put(level, box, Blocks.GRINDSTONE.defaultBlockState(), 4, 1, 6);
		p.fill(level, box, 6, 1, 6, 7, 2, 7, Blocks.HAY_BLOCK.defaultBlockState());
		p.chest(level, box, random, 3, 1, 6, ModStructures.lootTable("chests/windmill"));
		p.barrel(level, box, random, 3, 1, 7, ModStructures.BARREL_LOOT);
		p.put(level, box, hanging(), 5, 3, 5);
		// A little wheat field around it.
		BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);
		for (int x = 0; x <= 14; x++) {
			for (int z = 10; z <= 14; z++) {
				if (z == 12) {
					// Water channel keeps the field wet.
					p.put(level, box, Blocks.WATER.defaultBlockState(), x, 0, z);
					continue;
				}
				p.put(level, box, farmland, x, 0, z);
				p.put(level, box, ((net.minecraft.world.level.block.CropBlock) Blocks.WHEAT).getStateForAge(4 + random.nextInt(4)), x, 1, z);
			}
		}
		// Hay stacks and a flour barrel by the field.
		p.fill(level, box, 11, 1, 3, 12, 2, 4, Blocks.HAY_BLOCK.defaultBlockState());
		p.barrel(level, box, random, 12, 1, 6, ModStructures.lootTable("chests/windmill"));
		BlockPos spawn = p.pos(5, 1, 4);
		if (p.claimSpawn(spawn, box)) {
			BlockPos home = p.pos(5, 1, 5);
			p.placeMarker(level, spawn, m -> {
				// The Bread Mill is ItzBreadLoaf's; Farmer Madison helps with the field.
				m.addResident(CharacterVariant.BREADLOAF, spawn, home, 9, null, null, null);
				m.addResident(CharacterVariant.FARMER, spawn, home, 9, null, null, null);
			});
		}
	}

	/** Pumpkin Patch: a field of pumpkins (and a few jack o'lanterns) with a scarecrow. Farmer Madison. */
	static void pumpkinPatch(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 14, 6, 12, Blocks.GRASS_BLOCK.defaultBlockState());
		p.walls(level, box, 0, 0, 14, 12, 1, 1, Blocks.SPRUCE_FENCE.defaultBlockState());
		p.put(level, box, Blocks.SPRUCE_FENCE_GATE.defaultBlockState(), 7, 1, 0);
		for (int x = 1; x <= 13; x++) {
			for (int z = 1; z <= 11; z++) {
				if (random.nextInt(4) == 0) {
					Block pumpkin = random.nextInt(8) == 0 ? Blocks.JACK_O_LANTERN : Blocks.PUMPKIN;
					BlockState state = pumpkin == Blocks.PUMPKIN ? pumpkin.defaultBlockState()
							: pumpkin.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
					p.put(level, box, state, x, 1, z);
				} else if (random.nextInt(3) == 0) {
					p.put(level, box, Blocks.COARSE_DIRT.defaultBlockState(), x, 0, z);
				}
			}
		}
		// Farm shed: tools, composter and the pumpkin barrel.
		p.fill(level, box, 10, 0, 8, 13, 0, 11, Blocks.SPRUCE_PLANKS.defaultBlockState());
		p.fill(level, box, 10, 1, 8, 13, 3, 11, air());
		p.walls(level, box, 10, 8, 13, 11, 1, 3, Blocks.SPRUCE_PLANKS.defaultBlockState());
		p.fill(level, box, 9, 4, 7, 14, 4, 12, Blocks.SPRUCE_SLAB.defaultBlockState());
		p.door(level, box, 11, 1, 8, Blocks.SPRUCE_DOOR);
		p.put(level, box, Blocks.COMPOSTER.defaultBlockState(), 12, 1, 10);
		p.barrel(level, box, random, 11, 1, 10, ModStructures.lootTable("chests/pumpkin_patch"));
		p.put(level, box, Blocks.LANTERN.defaultBlockState(), 12, 1, 9);
		p.put(level, box, Blocks.MELON.defaultBlockState(), 3, 1, 3);
		p.put(level, box, Blocks.MELON.defaultBlockState(), 4, 1, 9);
		// Scarecrow.
		p.fill(level, box, 7, 1, 6, 7, 2, 6, Blocks.OAK_FENCE.defaultBlockState());
		p.put(level, box, Blocks.HAY_BLOCK.defaultBlockState(), 7, 3, 6);
		p.put(level, box, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.NORTH), 7, 4, 6);
		p.put(level, box, Blocks.OAK_FENCE.defaultBlockState(), 6, 3, 6);
		p.put(level, box, Blocks.OAK_FENCE.defaultBlockState(), 8, 3, 6);
		p.chest(level, box, random, 1, 1, 11, ModStructures.lootTable("chests/pumpkin_patch"));
		p.barrel(level, box, random, 2, 1, 11, ModStructures.BARREL_LOOT);
		one(p, level, box, CharacterVariant.FARMER, 7, 1, 2, 7, 6, 8, null, null);
	}

	// ------------------------------------------------------------ shared

	/** Cousins' Camp: Lily's pink tent and Madison's yellow tent around one campfire. */
	static void cousinsCamp(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 12, 4, 10, Blocks.GRASS_BLOCK.defaultBlockState());
		p.smallTent(level, box, 1, 2, Blocks.PINK_WOOL.defaultBlockState());
		p.smallTent(level, box, 9, 2, Blocks.YELLOW_WOOL.defaultBlockState());
		p.put(level, box, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 6, 1, 6);
		BlockState bench = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
		p.put(level, box, bench, 5, 1, 8);
		p.put(level, box, bench, 6, 1, 8);
		p.put(level, box, bench, 7, 1, 8);
		p.chest(level, box, random, 6, 1, 10, ModStructures.lootTable("chests/cousins_camp"));
		p.barrel(level, box, random, 5, 1, 10, ModStructures.BARREL_LOOT);
		for (int i = 0; i < 6; i++) {
			p.put(level, box, FLOWERS[random.nextInt(FLOWERS.length)].defaultBlockState(), random.nextInt(13), 1, random.nextBoolean() ? 0 : 1);
		}
		BlockPos spawn = p.pos(6, 1, 4);
		if (p.claimSpawn(spawn, box)) {
			BlockPos fire = p.pos(6, 1, 7);
			BlockPos lilyBed = p.pos(2, 1, 3);
			BlockPos madisonBed = p.pos(10, 1, 3);
			p.placeMarker(level, spawn, m -> {
				m.addResident(CharacterVariant.LILY, spawn, fire, 8, lilyBed, null, null);
				m.addResident(CharacterVariant.MADISON, spawn, fire, 8, madisonBed, null, null);
			});
		}
	}

	/** PvP Arena: sand floor, fences, stands. An ItzBreadLoaf and FelixSyn (with Harley) live here, so they fight. */
	static void pvpArena(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 22, 5, 22, Blocks.GRASS_BLOCK.defaultBlockState());
		p.fill(level, box, 2, 0, 2, 20, 0, 20, Blocks.SAND.defaultBlockState());
		p.walls(level, box, 2, 2, 20, 20, 1, 1, Blocks.OAK_FENCE.defaultBlockState());
		p.put(level, box, Blocks.OAK_FENCE_GATE.defaultBlockState(), 11, 1, 2);
		p.put(level, box, Blocks.OAK_FENCE_GATE.defaultBlockState(), 11, 1, 20);
		// Stands on two sides.
		for (int x = 4; x <= 18; x++) {
			p.put(level, box, Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), x, 1, 0);
			p.put(level, box, Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), x, 1, 22);
		}
		for (int[] c : new int[][]{{2, 2}, {20, 2}, {2, 20}, {20, 20}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 3, c[1], Blocks.STONE_BRICK_WALL.defaultBlockState());
			p.put(level, box, Blocks.LANTERN.defaultBlockState(), c[0], 4, c[1]);
		}
		p.put(level, box, Blocks.RED_BANNER.defaultBlockState(), 7, 1, 11);
		p.put(level, box, Blocks.BLUE_BANNER.defaultBlockState(), 15, 1, 11);
		p.chest(level, box, random, 1, 1, 11, ModStructures.lootTable("chests/pvp_arena"));
		BlockPos spawn = p.pos(6, 1, 11);
		if (p.claimSpawn(spawn, box)) {
			BlockPos breadSide = p.pos(6, 1, 11);
			BlockPos felixSide = p.pos(16, 1, 11);
			p.placeMarker(level, spawn, m -> {
				m.addResident(CharacterVariant.BREADLOAF, breadSide, breadSide, 8, null, null, null);
				m.addResident(CharacterVariant.FELIXSYN, felixSide, felixSide, 8, null, null, null);
			});
		}
	}

	/** Giant Loaf (very rare easter egg): a huge loaf of hay, terracotta and sandstone with a treasure room inside. */
	static void giantLoaf(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 26, 11, 14, Blocks.GRASS_BLOCK.defaultBlockState());
		// An elongated dome: crust of orange/brown terracotta, sandstone ends, hay "crumb" inside the walls.
		for (int x = 1; x <= 25; x++) {
			for (int z = 1; z <= 13; z++) {
				for (int y = 0; y <= 10; y++) {
					double dx = (x - 13) / 12.5;
					double dz = (z - 7) / 6.5;
					double dy = y / 10.0;
					double d = dx * dx * dx * dx + dz * dz + dy * dy;
					if (d > 1.0) {
						continue;
					}
					boolean shell = d > 0.7;
					BlockState state;
					if (!shell) {
						state = y >= 1 && y <= 5 && Math.abs(x - 13) <= 7 && Math.abs(z - 7) <= 3 ? air() : Blocks.HAY_BLOCK.defaultBlockState();
					} else if (Math.abs(x - 13) >= 11) {
						state = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
					} else if (y >= 8 && (x + z) % 5 == 0) {
						state = Blocks.WHITE_TERRACOTTA.defaultBlockState();
					} else {
						state = (x * 7 + z * 3 + y) % 4 == 0 ? Blocks.BROWN_TERRACOTTA.defaultBlockState() : Blocks.ORANGE_TERRACOTTA.defaultBlockState();
					}
					p.put(level, box, state, x, y, z);
				}
			}
		}
		// Door in at one end, a tunnel to the treasure room.
		p.fill(level, box, 1, 1, 7, 6, 2, 7, air());
		p.put(level, box, Blocks.SANDSTONE.defaultBlockState(), 1, 3, 7);
		p.fill(level, box, 6, 0, 4, 20, 0, 10, Blocks.SMOOTH_SANDSTONE.defaultBlockState());
		p.chest(level, box, random, 13, 1, 4, ModStructures.lootTable("chests/giant_loaf"));
		p.chest(level, box, random, 13, 1, 10, ModStructures.lootTable("chests/giant_loaf"));
		p.chest(level, box, random, 19, 1, 7, ModStructures.lootTable("chests/giant_loaf"));
		p.barrel(level, box, random, 8, 1, 4, ModStructures.lootTable("chests/bread_bakery"));
		p.barrel(level, box, random, 8, 1, 10, ModStructures.lootTable("chests/bread_bakery"));
		p.put(level, box, Blocks.GOLD_BLOCK.defaultBlockState(), 13, 1, 7);
		p.put(level, box, Blocks.CAKE.defaultBlockState(), 13, 2, 7);
		p.put(level, box, hanging(), 10, 5, 7);
		p.put(level, box, hanging(), 16, 5, 7);
	}

	/** Nether Mace Training Ground: blackstone pillars of different heights with platforms for mace drops. */
	static void netherTrainingGround(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		p.fill(level, box, 0, 1, 0, 20, 21, 20, air());
		p.fill(level, box, 0, 0, 0, 20, 0, 20, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
		p.foundation(level, box, 0, 0, 20, 20, Blocks.BASALT.defaultBlockState());
		int[][] pillars = {{3, 3, 8}, {17, 3, 12}, {3, 17, 16}, {17, 17, 10}, {10, 10, 20}};
		for (int[] pillar : pillars) {
			p.fill(level, box, pillar[0], 1, pillar[1], pillar[0], pillar[2], pillar[1], Blocks.POLISHED_BASALT.defaultBlockState());
			p.fill(level, box, pillar[0] - 1, pillar[2], pillar[1] - 1, pillar[0] + 1, pillar[2], pillar[1] + 1, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
			for (int y = 1; y < pillar[2]; y++) {
				p.put(level, box, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH), pillar[0], y, pillar[1] - 1);
			}
			p.put(level, box, Blocks.SOUL_LANTERN.defaultBlockState(), pillar[0], pillar[2] + 1, pillar[1]);
		}
		// Targets on the ground to aim smashes at.
		for (int[] t : new int[][]{{7, 7}, {13, 7}, {7, 13}, {13, 13}}) {
			p.put(level, box, Blocks.TARGET.defaultBlockState(), t[0], 1, t[1]);
		}
		p.chest(level, box, random, 1, 1, 10, ModStructures.lootTable("chests/nether_training_ground"));
		p.barrel(level, box, random, 1, 1, 11, ModStructures.BARREL_LOOT);
		BlockPos spawn = p.pos(10, 1, 5);
		if (p.claimSpawn(spawn, box)) {
			p.placeMarker(level, spawn, m -> m.addResident(CharacterVariant.MASTER, spawn, spawn, 10, null, null, null));
		}
	}

	/** End Mace Tower: a tall purpur tower with a ladder and a lookout on top where Master BreadLoaf waits. */
	static void endMaceTower(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		p.fill(level, box, 0, 1, 0, 8, 31, 8, air());
		p.fill(level, box, 0, 0, 0, 8, 0, 8, Blocks.END_STONE_BRICKS.defaultBlockState());
		p.foundation(level, box, 0, 0, 8, 8, Blocks.END_STONE.defaultBlockState());
		p.walls(level, box, 1, 1, 7, 7, 1, 26, Blocks.PURPUR_BLOCK.defaultBlockState());
		for (int[] c : new int[][]{{1, 1}, {7, 1}, {1, 7}, {7, 7}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 28, c[1], Blocks.PURPUR_PILLAR.defaultBlockState());
		}
		p.fill(level, box, 4, 1, 1, 4, 2, 1, air());
		for (int y = 1; y <= 27; y++) {
			p.put(level, box, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH), 4, y, 6);
		}
		for (int y : new int[]{8, 15, 22}) {
			p.put(level, box, Blocks.END_ROD.defaultBlockState(), 2, y, 2);
			p.put(level, box, Blocks.END_ROD.defaultBlockState(), 6, y, 6 - 1);
		}
		for (int x = 0; x <= 8; x++) {
			for (int z = 0; z <= 8; z++) {
				if (x != 4 || z != 6) {
					p.put(level, box, Blocks.PURPUR_SLAB.defaultBlockState(), x, 27, z);
				}
			}
		}
		for (int i = 0; i <= 8; i += 2) {
			for (int[] c : new int[][]{{i, 0}, {i, 8}, {0, i}, {8, i}}) {
				p.put(level, box, Blocks.PURPUR_BLOCK.defaultBlockState(), c[0], 28, c[1]);
			}
		}
		p.chest(level, box, random, 2, 28, 2, ModStructures.lootTable("chests/end_mace_tower"));
		BlockPos spawn = p.pos(4, 28, 3);
		if (p.claimSpawn(spawn, box)) {
			p.placeMarker(level, spawn, m -> m.addResident(CharacterVariant.MASTER, spawn, spawn, 3, null, null, null));
		}
	}

	/** Sweaty Dojo: a training hall with targets, a straw dummy and an armor rack; a Sweaty ItzBreadLoaf trains here. */
	static void sweatyDojo(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		site(p, level, box, 12, 6, 12, Blocks.GRASS_BLOCK.defaultBlockState());
		p.fill(level, box, 1, 0, 1, 11, 0, 11, Blocks.BAMBOO_MOSAIC.defaultBlockState());
		for (int[] c : new int[][]{{1, 1}, {11, 1}, {1, 11}, {11, 11}, {6, 1}, {6, 11}, {1, 6}, {11, 6}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 4, c[1], Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
		}
		p.fill(level, box, 0, 5, 0, 12, 5, 12, Blocks.DARK_OAK_SLAB.defaultBlockState());
		p.fill(level, box, 2, 6, 2, 10, 6, 10, Blocks.DARK_OAK_SLAB.defaultBlockState());
		p.put(level, box, Blocks.TARGET.defaultBlockState(), 3, 1, 9);
		p.put(level, box, Blocks.TARGET.defaultBlockState(), 9, 1, 9);
		p.fill(level, box, 6, 1, 9, 6, 2, 9, Blocks.HAY_BLOCK.defaultBlockState());
		p.put(level, box, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.NORTH), 6, 3, 9);
		p.put(level, box, Blocks.ANVIL.defaultBlockState(), 10, 1, 3);
		p.put(level, box, hanging(), 6, 4, 6);
		p.chest(level, box, random, 2, 1, 3, ModStructures.lootTable("chests/sweaty_dojo"));
		p.barrel(level, box, random, 2, 1, 4, ModStructures.BARREL_LOOT);
		one(p, level, box, CharacterVariant.SWEATY, 6, 1, 4, 6, 6, 6, null, null);
	}

	/** Smugglers' Cove: a beach shack, crates and a little dock. A Pirate and a Bandit share the loot. */
	static void smugglersCove(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random) {
		p.fill(level, box, 0, 1, 0, 12, 6, 14, air());
		p.fill(level, box, 0, 0, 0, 12, 0, 7, Blocks.SAND.defaultBlockState());
		p.foundation(level, box, 0, 0, 12, 7, Blocks.SANDSTONE.defaultBlockState());
		p.walls(level, box, 1, 1, 7, 6, 1, 3, Blocks.DARK_OAK_PLANKS.defaultBlockState());
		p.fill(level, box, 1, 4, 1, 7, 4, 6, Blocks.DARK_OAK_SLAB.defaultBlockState());
		p.door(level, box, 4, 1, 6, Blocks.DARK_OAK_DOOR);
		p.chest(level, box, random, 2, 1, 2, ModStructures.lootTable("chests/smugglers_cove"));
		p.chest(level, box, random, 3, 1, 2, ModStructures.lootTable("chests/smugglers_cove"));
		p.barrel(level, box, random, 6, 1, 2, ModStructures.BARREL_LOOT);
		p.barrel(level, box, random, 9, 1, 3, ModStructures.FISH_BARREL);
		p.barrel(level, box, random, 10, 1, 3, ModStructures.BARREL_LOOT);
		p.put(level, box, hanging(), 4, 3, 4);
		// Dock out into the water.
		p.fill(level, box, 9, 0, 6, 11, 0, 14, Blocks.SPRUCE_PLANKS.defaultBlockState());
		for (int z = 8; z <= 14; z += 3) {
			p.column(level, box, Blocks.SPRUCE_LOG.defaultBlockState(), 9, -1, z);
			p.column(level, box, Blocks.SPRUCE_LOG.defaultBlockState(), 11, -1, z);
		}
		p.put(level, box, Blocks.BLACK_BANNER.defaultBlockState(), 10, 1, 14);
		BlockPos spawn = p.pos(10, 1, 8);
		if (p.claimSpawn(spawn, box)) {
			UUID group = p.group();
			BlockPos home = p.pos(6, 1, 8);
			p.placeMarker(level, spawn, m -> {
				m.addResident(CharacterVariant.PIRATE, spawn, home, 8, null, group, null);
				m.addResident(CharacterVariant.BANDIT, spawn, home, 8, null, group, null);
			});
		}
	}
}
