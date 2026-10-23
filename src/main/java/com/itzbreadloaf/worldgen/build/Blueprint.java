package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.character.CharacterVariant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A structure drawn in memory first, then checked, then placed. Local coordinates: x 0..width-1, z 0..depth-1,
 * y = 0 is the ground layer (basements can go below 0). The front (where doors usually go) is the low-z side.
 * Layouts only describe what goes where; {@link BlueprintPiece} levels the ground and places it in the world,
 * and {@link Reachability} checks every floor, chest, bed and resident can be reached before that.
 */
public final class Blueprint {
	public final int width;
	public final int depth;
	public final int height;
	public final Palette palette;
	/** Seeded from the structure, so every chunk of it builds the same layout. */
	public final RandomSource random;

	final Map<Long, BlockState> blocks = new HashMap<>();
	final List<Container> containers = new ArrayList<>();
	final List<Bed> beds = new ArrayList<>();
	final List<Resident> residents = new ArrayList<>();
	final List<Floor> floors = new ArrayList<>();
	final List<Mob> mobs = new ArrayList<>();
	final List<int[]> hives = new ArrayList<>();
	final List<Pillar> pillars = new ArrayList<>();
	/** Fill any gap under the footprint down to solid ground (off for ships and things on stilts). */
	public boolean foundation = true;
	/** Slope the terrain around the footprint toward the structure's ground level (off for ships). */
	public boolean skirt = true;
	/** Clear the terrain inside the footprint before building (off for things built into hills or underground). */
	public boolean clearAbove = true;
	/** Put the palette's ground on y = 0 wherever the layout leaves it empty. */
	public boolean groundLayer = true;
	private int lowestY;

	public record Container(int x, int y, int z, String loot, boolean barrel) {
	}

	public record Bed(int x, int y, int z, Block block) {
	}

	public record Resident(CharacterVariant variant, int x, int y, int z, int homeX, int homeZ, int radius,
			@Nullable BlockPos bed, @Nullable BlockPos pantry, boolean group) {
	}

	/** An animal or a training dummy: "cow", "sheep", "chicken", "pig", "horse", "dummy" (armor stand). */
	public record Mob(int x, int y, int z, String type) {
	}

	/** A post that goes from (x, y, z) down to the ground when placed (docks, stilts). */
	public record Pillar(int x, int y, int z, BlockState state) {
	}

	/** A walkable area that must be reachable (feet height y). */
	public record Floor(int x0, int z0, int x1, int z1, int y, String name) {
	}

	public Blueprint(int width, int depth, int height, Palette palette, RandomSource random) {
		this.width = width;
		this.depth = depth;
		this.height = height;
		this.palette = palette;
		this.random = random;
	}

	static long key(int x, int y, int z) {
		return BlockPos.asLong(x, y, z);
	}

	public int lowestY() {
		return this.lowestY;
	}

	// ---------------------------------------------------------------- blocks

	public void set(int x, int y, int z, BlockState state) {
		this.blocks.put(key(x, y, z), state);
		this.lowestY = Math.min(this.lowestY, y);
	}

	public void set(int x, int y, int z, Block block) {
		this.set(x, y, z, block.defaultBlockState());
	}

	public @Nullable BlockState get(int x, int y, int z) {
		return this.blocks.get(key(x, y, z));
	}

	public boolean isSet(int x, int y, int z) {
		return this.blocks.containsKey(key(x, y, z));
	}

	public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
		for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
			for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
				for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
					this.set(x, y, z, state);
				}
			}
		}
	}

	public void fill(int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
		this.fill(x0, y0, z0, x1, y1, z1, block.defaultBlockState());
	}

	/** Air (an explicit opening: doorways, stairwells, ladder holes). */
	public void clear(int x0, int y0, int z0, int x1, int y1, int z1) {
		this.fill(x0, y0, z0, x1, y1, z1, Blocks.AIR.defaultBlockState());
	}

	/** The outer ring of a rectangle, from y0 to y1. */
	public void walls(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state) {
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				this.set(x, y, z0, state);
				this.set(x, y, z1, state);
			}
			for (int z = z0; z <= z1; z++) {
				this.set(x0, y, z, state);
				this.set(x1, y, z, state);
			}
		}
	}

	// ---------------------------------------------------------------- things that need more than a block

	/** A chest filled from a loot table ("chests/house" means itzbreadloaf:chests/house). */
	public void chest(int x, int y, int z, String loot) {
		this.set(x, y, z, Blocks.CHEST);
		this.containers.add(new Container(x, y, z, loot, false));
	}

	public void barrel(int x, int y, int z, String loot) {
		this.set(x, y, z, Blocks.BARREL);
		this.containers.add(new Container(x, y, z, loot, true));
	}

	/** A dispenser facing {@code facing}, loaded from a loot table (for traps use one outside chests/). */
	public void dispenser(int x, int y, int z, net.minecraft.core.Direction facing, String loot) {
		this.set(x, y, z, Blocks.DISPENSER.defaultBlockState().setValue(net.minecraft.world.level.block.DispenserBlock.FACING, facing));
		this.containers.add(new Container(x, y, z, loot, true));
	}

	public void mob(int x, int y, int z, String type) {
		this.mobs.add(new Mob(x, y, z, type));
	}

	/** A full beehive with two bees, over a lit campfire (smoke keeps them calm when the honey is taken). */
	public void beehive(int x, int y, int z) {
		this.set(x, y, z, Blocks.CAMPFIRE);
		this.set(x, y + 1, z, Blocks.BEEHIVE.defaultBlockState()
				.setValue(net.minecraft.world.level.block.BeehiveBlock.FACING, net.minecraft.core.Direction.SOUTH)
				.setValue(net.minecraft.world.level.block.BeehiveBlock.HONEY_LEVEL, net.minecraft.world.level.block.BeehiveBlock.MAX_HONEY_LEVELS));
		this.hives.add(new int[]{x, y + 1, z});
	}

	public void pillar(int x, int y, int z, BlockState state) {
		this.set(x, y, z, state);
		this.pillars.add(new Pillar(x, y, z, state));
	}

	/** A bed with its foot at z and head at z + 1 (pointing back). */
	public void bed(int x, int y, int z, Block bed) {
		this.set(x, y, z, bed);
		this.set(x, y, z + 1, bed);
		this.beds.add(new Bed(x, y, z, bed));
	}

	/** Someone who lives here: spawns at (x, y, z) and stays within radius of (homeX, y, homeZ). */
	public void resident(CharacterVariant variant, int x, int y, int z, int homeX, int homeZ, int radius) {
		this.residents.add(new Resident(variant, x, y, z, homeX, homeZ, radius, null, null, false));
	}

	/** A resident with a bed (local foot position) and optional pantry chest; group = they defend each other. */
	public void resident(CharacterVariant variant, int x, int y, int z, int homeX, int homeZ, int radius,
			@Nullable BlockPos bed, @Nullable BlockPos pantry, boolean group) {
		this.residents.add(new Resident(variant, x, y, z, homeX, homeZ, radius, bed, pantry, group));
	}

	/** Declares a floor that players and residents must be able to reach (checked by {@link Reachability}). */
	public void floor(int x0, int z0, int x1, int z1, int y, String name) {
		this.floors.add(new Floor(x0, z0, x1, z1, y, name));
	}

	public List<Container> containers() {
		return this.containers;
	}

	public List<Resident> residents() {
		return this.residents;
	}
}
