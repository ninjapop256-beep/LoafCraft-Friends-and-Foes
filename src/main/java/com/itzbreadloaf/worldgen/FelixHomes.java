package com.itzbreadloaf.worldgen;

import static com.itzbreadloaf.worldgen.SpecialHomes.air;
import static com.itzbreadloaf.worldgen.SpecialHomes.hanging;
import static com.itzbreadloaf.worldgen.SpecialHomes.one;
import static com.itzbreadloaf.worldgen.SpecialHomes.site;

import com.itzbreadloaf.character.CharacterVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Wild FelixSyn's homes: Mace Workshop, Cliff Hut, Wind Tower and Rival Camp. Materials come from the biome's palette
 * (planks, logs, slabs, door), with the stone-and-dark-oak look kept where the spec asks for it.
 */
final class FelixHomes {
	private FelixHomes() {
	}

	private static BlockState ladder() {
		return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
	}

	/** An armor stand with arms, wearing a chestplate and holding an iron axe (a sparring dummy). */
	private static void dummy(HomePiece p, WorldGenLevel level, BoundingBox box, int x, int y, int z) {
		BlockPos at = p.pos(x, y, z);
		if (!box.isInside(at)) {
			return;
		}
		ArmorStand stand = EntityType.ARMOR_STAND.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
		if (stand != null) {
			stand.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
			stand.setShowArms(true);
			stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
			stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
			level.addFreshEntityWithPassengers(stand);
		}
	}

	/**
	 * A mace rack: maces on display, built from blocks (a fence handle under a heavy-looking head) so they look the
	 * part without handing out free maces (maces are Rare loot).
	 */
	private static void maceRack(HomePiece p, WorldGenLevel level, BoundingBox box, int x0, int z, int count) {
		for (int i = 0; i < count; i++) {
			int x = x0 + i * 2;
			p.put(level, box, Blocks.DARK_OAK_FENCE.defaultBlockState(), x, 1, z);
			p.put(level, box, Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), x, 2, z);
		}
	}

	/** Mace Workshop (plains, savanna): stone-and-dark-oak workshop with an anvil, smithing table and mace racks. */
	static void maceWorkshop(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random, HomePiece.Palette palette) {
		site(p, level, box, 12, 7, 10, palette.ground());
		p.fill(level, box, 1, 0, 1, 11, 0, 9, Blocks.STONE_BRICKS.defaultBlockState());
		p.walls(level, box, 1, 1, 11, 9, 1, 1, Blocks.STONE_BRICKS.defaultBlockState());
		p.walls(level, box, 1, 1, 11, 9, 2, 4, Blocks.DARK_OAK_PLANKS.defaultBlockState());
		for (int[] c : new int[][]{{1, 1}, {11, 1}, {1, 9}, {11, 9}, {6, 1}, {6, 9}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 4, c[1], Blocks.DARK_OAK_LOG.defaultBlockState());
		}
		// Roof: a slab layer and a raised ridge, in the biome's slab.
		p.fill(level, box, 0, 5, 0, 12, 5, 10, palette.slab());
		p.fill(level, box, 2, 6, 2, 10, 6, 8, palette.slab());
		p.door(level, box, 6, 1, 1, palette.door());
		for (int x : new int[]{3, 9}) {
			p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), x, 2, 1);
			p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), x, 3, 9);
		}
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 1, 3, 5);
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 11, 3, 5);
		// Workbenches along the back.
		p.put(level, box, Blocks.ANVIL.defaultBlockState(), 3, 1, 8);
		p.put(level, box, Blocks.SMITHING_TABLE.defaultBlockState(), 4, 1, 8);
		p.put(level, box, Blocks.GRINDSTONE.defaultBlockState(), 5, 1, 8);
		p.put(level, box, Blocks.BLAST_FURNACE.defaultBlockState(), 7, 1, 8);
		p.chest(level, box, random, 8, 1, 8, ModStructures.lootTable("chests/mace_workshop"));
		p.barrel(level, box, random, 9, 1, 8, ModStructures.BARREL_LOOT);
		maceRack(p, level, box, 2, 3, 3);
		dummy(p, level, box, 9, 1, 3);
		p.bed(level, box, 9, 1, 5, net.minecraft.world.level.block.Blocks.BLACK_BED);
		p.put(level, box, hanging(), 4, 4, 5);
		p.put(level, box, hanging(), 8, 4, 5);
		// Front path.
		p.fill(level, box, 5, 0, 0, 7, 0, 0, Blocks.DIRT_PATH.defaultBlockState());
		one(p, level, box, CharacterVariant.WILD_FELIX, 6, 1, 4, 6, 5, 12, p.pos(9, 1, 5), null);
	}

	/** Cliff Hut (windswept hills, mountains): a little hut with a diving-board ledge for mace drops. */
	static void cliffHut(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random, HomePiece.Palette palette) {
		p.fill(level, box, 0, 1, 0, 10, 7, 10, air());
		p.fill(level, box, 1, 0, 1, 7, 0, 7, palette.foundation());
		p.foundation(level, box, 1, 1, 7, 7, palette.foundation());
		p.walls(level, box, 2, 2, 6, 6, 1, 3, palette.planks());
		for (int[] c : new int[][]{{2, 2}, {6, 2}, {2, 6}, {6, 6}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], 3, c[1], palette.corner());
		}
		p.fill(level, box, 1, 4, 1, 7, 4, 7, palette.slab());
		p.door(level, box, 4, 1, 2, palette.door());
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 2, 2, 4);
		p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 6, 2, 4);
		p.bed(level, box, 3, 1, 4, net.minecraft.world.level.block.Blocks.BLACK_BED);
		p.chest(level, box, random, 5, 1, 5, ModStructures.lootTable("chests/cliff_hut"));
		p.put(level, box, Blocks.LANTERN.defaultBlockState(), 5, 1, 3);
		// The ledge: planks sticking out over the drop, with rails on the sides and the far end open.
		p.fill(level, box, 4, 0, 8, 4, 0, 10, palette.planks());
		p.put(level, box, palette.corner(), 4, -1, 8);
		p.fill(level, box, 3, 1, 8, 3, 1, 9, Blocks.SPRUCE_FENCE.defaultBlockState());
		p.fill(level, box, 5, 1, 8, 5, 1, 9, Blocks.SPRUCE_FENCE.defaultBlockState());
		p.put(level, box, Blocks.TARGET.defaultBlockState(), 8, 1, 2);
		one(p, level, box, CharacterVariant.WILD_FELIX, 4, 1, 1, 4, 4, 10, p.pos(3, 1, 4), null);
	}

	/**
	 * Wind Tower (open biomes): a tall, thin tower he jumps off to train. A ladder runs all the way up the inside to
	 * the lookout on top, with lanterns on the way; hay bales around the bottom for landings.
	 */
	static void windTower(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random, HomePiece.Palette palette) {
		int top = 22;
		site(p, level, box, 8, 26, 8, palette.ground());
		p.fill(level, box, 2, 0, 2, 6, 0, 6, palette.foundation());
		BlockState wall = Blocks.STONE_BRICKS.defaultBlockState();
		p.walls(level, box, 2, 2, 6, 6, 1, top - 1, wall);
		for (int[] c : new int[][]{{2, 2}, {6, 2}, {2, 6}, {6, 6}}) {
			p.fill(level, box, c[0], 1, c[1], c[0], top - 1, c[1], palette.corner());
		}
		p.door(level, box, 4, 1, 2, palette.door());
		// Ladder up the back wall, all the way through the lookout floor.
		for (int y = 1; y <= top; y++) {
			p.put(level, box, ladder(), 4, y, 5);
		}
		for (int y = 6; y < top; y += 5) {
			p.put(level, box, Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true), 3, y, 3);
			p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 2, y, 4);
			p.put(level, box, Blocks.GLASS_PANE.defaultBlockState(), 6, y, 4);
		}
		// Lookout: a wider floor with a fence, and an open gap in the fence to jump from.
		p.fill(level, box, 1, top, 1, 7, top, 7, palette.planks());
		p.put(level, box, ladder(), 4, top, 5);
		p.walls(level, box, 1, 1, 7, 7, top + 1, top + 1, Blocks.SPRUCE_FENCE.defaultBlockState());
		p.put(level, box, air(), 4, top + 1, 1);
		for (int[] c : new int[][]{{1, 1}, {7, 1}, {1, 7}, {7, 7}}) {
			p.put(level, box, Blocks.LANTERN.defaultBlockState(), c[0], top + 2, c[1]);
		}
		p.chest(level, box, random, 2, top + 1, 6, ModStructures.lootTable("chests/wind_tower"));
		// Landing pad.
		for (int x = 0; x <= 8; x++) {
			for (int z = 0; z <= 1; z++) {
				p.put(level, box, Blocks.HAY_BLOCK.defaultBlockState(), x, 0, z);
			}
		}
		p.bed(level, box, 3, 1, 3, net.minecraft.world.level.block.Blocks.BLACK_BED);
		p.barrel(level, box, random, 5, 1, 3, ModStructures.BARREL_LOOT);
		one(p, level, box, CharacterVariant.WILD_FELIX, 4, 1, 0, 4, 4, 12, p.pos(3, 1, 3), null);
	}

	/** Rival Camp: set up within sight of an ItzBreadLoaf house, so the two of them fight all the time. */
	static void rivalCamp(HomePiece p, WorldGenLevel level, BoundingBox box, RandomSource random, HomePiece.Palette palette) {
		site(p, level, box, 10, 4, 8, palette.ground());
		p.fill(level, box, 1, 0, 1, 9, 0, 7, Blocks.COARSE_DIRT.defaultBlockState());
		p.smallTent(level, box, 1, 3, Blocks.BLACK_WOOL.defaultBlockState());
		p.put(level, box, Blocks.CAMPFIRE.defaultBlockState(), 6, 1, 4);
		p.put(level, box, palette.corner().hasProperty(RotatedPillarBlock.AXIS)
				? palette.corner().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X) : palette.corner(), 6, 1, 6);
		p.put(level, box, Blocks.TARGET.defaultBlockState(), 9, 1, 2);
		p.put(level, box, Blocks.TARGET.defaultBlockState(), 9, 1, 6);
		p.fill(level, box, 8, 1, 4, 8, 2, 4, Blocks.HAY_BLOCK.defaultBlockState());
		p.put(level, box, Blocks.CARVED_PUMPKIN.defaultBlockState(), 8, 3, 4);
		// A "scoreboard": red and blue banners for FelixSyn vs ItzBreadLoaf.
		p.put(level, box, Blocks.RED_BANNER.defaultBlockState(), 5, 1, 1);
		p.put(level, box, Blocks.BLUE_BANNER.defaultBlockState(), 7, 1, 1);
		p.chest(level, box, random, 5, 1, 7, ModStructures.lootTable("chests/rival_camp"));
		dummy(p, level, box, 9, 1, 4);
		one(p, level, box, CharacterVariant.WILD_FELIX, 6, 1, 3, 6, 4, 14, p.pos(2, 1, 4), null);
	}
}
