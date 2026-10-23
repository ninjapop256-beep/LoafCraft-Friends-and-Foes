package com.itzbreadloaf.worldgen.build;

import com.itzbreadloaf.ItzBreadLoaf;
import com.itzbreadloaf.block.ModBlocks;
import com.itzbreadloaf.block.ResidentMarkerBlockEntity;
import com.itzbreadloaf.worldgen.ModStructures;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Places a blueprint in the world: picks the palette from the biome, builds the layout (seeded, so every chunk of
 * the structure agrees), levels the ground under and around it, then places blocks, chests, barrels, beds and a
 * resident marker. The piece is {@link #MARGIN} blocks bigger than the footprint on each side for the leveling skirt.
 */
public class BlueprintPiece extends ScatteredFeaturePiece {
	public static final int MARGIN = 3;
	/** Foundations and posts stop this far down (no pillars to the bottom of the world over a cliff or the void). */
	private static final int FOUNDATION_DEPTH = 16;
	private static final int PILLAR_DEPTH = 32;

	private final String id;
	private final long seed;
	private boolean spawned;
	private boolean checked;

	public BlueprintPiece(String id, RandomSource random, int x, int z) {
		super(ModStructures.BLUEPRINT_PIECE, x - MARGIN, 64, z - MARGIN, width(id) + 2 * MARGIN, height(id), depth(id) + 2 * MARGIN,
				Direction.Plane.HORIZONTAL.getRandomDirection(random));
		this.id = id;
		this.seed = random.nextLong();
	}

	/**
	 * Builds a structure right now in a loaded world (Village Campsites, Bastion Throne Rooms). (x, z) is the
	 * footprint's corner and floorY the ground layer's height. Returns the area it covers.
	 */
	public static BoundingBox buildNow(net.minecraft.server.level.ServerLevel level, String id, int x, int z, int floorY, RandomSource random) {
		BlueprintPiece piece = new BlueprintPiece(id, random, x, z);
		piece.heightPosition = floorY;
		piece.boundingBox.move(0, floorY - piece.boundingBox.minY(), 0);
		BoundingBox area = piece.getBoundingBox();
		piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), random, area,
				new ChunkPos(area.getCenter()), area.getCenter());
		return area;
	}

	public BlueprintPiece(CompoundTag tag) {
		super(ModStructures.BLUEPRINT_PIECE, tag);
		this.id = tag.getStringOr("Id", "");
		this.seed = tag.getLongOr("Seed", 0L);
		this.spawned = tag.getBooleanOr("Spawned", false);
	}

	private static int width(String id) {
		StructureCatalog.Def def = StructureCatalog.get(id);
		return def == null ? 1 : def.width();
	}

	private static int depth(String id) {
		StructureCatalog.Def def = StructureCatalog.get(id);
		return def == null ? 1 : def.depth();
	}

	private static int height(String id) {
		StructureCatalog.Def def = StructureCatalog.get(id);
		return def == null ? 1 : def.height();
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		super.addAdditionalSaveData(context, tag);
		tag.putString("Id", this.id);
		tag.putLong("Seed", this.seed);
		tag.putBoolean("Spawned", this.spawned);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
			BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
		StructureCatalog.Def def = StructureCatalog.get(this.id);
		if (def == null || !Blueprints.has(this.id)) {
			return;
		}
		if (def.placement() == StructureCatalog.Placement.NETHER_FLOOR) {
			this.settleOnNetherFloor(level);
		} else if (!this.updateAverageGroundHeight(level, chunkBox, -1)) {
			return;
		}
		Holder<Biome> biome = level.getBiome(this.getWorldPos(MARGIN + def.width() / 2, 1, MARGIN + def.depth() / 2));
		Blueprint blueprint = new Blueprint(def.width(), def.depth(), def.height(), Palette.forBiome(biome), RandomSource.create(this.seed));
		Blueprints.build(this.id, blueprint);
		if (!this.checked && FabricLoader.getInstance().isDevelopmentEnvironment()) {
			// In the dev environment, report layouts with places you can't get to.
			this.checked = true;
			for (String problem : Reachability.check(blueprint)) {
				ItzBreadLoaf.LOGGER.warn("Structure {}: {}", this.id, problem);
			}
		}
		this.levelGround(level, chunkBox, blueprint);
		this.placeBlocks(level, chunkBox, blueprint, random);
	}

	/** The Nether's heightmap is its ceiling: find the floor under the middle instead. */
	private void settleOnNetherFloor(WorldGenLevel level) {
		if (this.heightPosition >= 0) {
			return;
		}
		BlockPos center = this.getWorldPos(this.boundingBox.getXSpan() / 2, 0, this.boundingBox.getZSpan() / 2);
		int floor = 64;
		for (int y = 100; y > 32; y--) {
			BlockPos at = new BlockPos(center.getX(), y, center.getZ());
			if (level.getBlockState(at).isAir() && level.getBlockState(at.above()).isAir()
					&& !level.getBlockState(at.below()).isAir() && level.getFluidState(at.below()).isEmpty()) {
				floor = y - 1;
				break;
			}
		}
		this.heightPosition = floor;
		this.boundingBox.move(0, floor - this.boundingBox.minY(), 0);
	}

	/**
	 * Terrain leveling: inside the footprint the terrain is cleared (if the layout wants that), the ground layer laid
	 * and any gap under it filled down to solid ground, so nothing floats. Around it, a skirt up to {@link #MARGIN}
	 * blocks wide slopes the terrain toward the structure's ground level, raising dips and cutting bumps, so nothing
	 * looks buried or perched.
	 */
	private void levelGround(WorldGenLevel level, BoundingBox box, Blueprint b) {
		BlockState ground = b.palette.ground();
		BlockState foundation = b.palette.foundation();
		BlockState air = Blocks.AIR.defaultBlockState();
		for (int lx = -MARGIN; lx < b.width + MARGIN; lx++) {
			for (int lz = -MARGIN; lz < b.depth + MARGIN; lz++) {
				int x = lx + MARGIN;
				int z = lz + MARGIN;
				int outX = lx < 0 ? -lx : lx >= b.width ? lx - b.width + 1 : 0;
				int outZ = lz < 0 ? -lz : lz >= b.depth ? lz - b.depth + 1 : 0;
				int distance = Math.max(outX, outZ);
				if (distance == 0) {
					if (b.clearAbove) {
						for (int y = 1; y < b.height; y++) {
							if (!b.isSet(lx, y, lz)) {
								this.placeBlock(level, air, x, y, z, box);
							}
						}
					}
					if (b.groundLayer && !b.isSet(lx, 0, lz)) {
						this.placeBlock(level, ground, x, 0, z, box);
					}
					if (b.foundation) {
						this.columnDown(level, foundation, x, Math.min(-1, b.lowestY() - 1), z, box, FOUNDATION_DEPTH);
					}
					continue;
				}
				if (!b.skirt) {
					continue;
				}
				BlockPos world = this.getWorldPos(x, 0, z);
				if (!box.isInside(world)) {
					continue;
				}
				int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, world.getX(), world.getZ()) - 1 - world.getY();
				int raiseTo = -(distance - 1);
				int cutAbove = distance;
				if (top < raiseTo) {
					for (int y = Math.max(top + 1, raiseTo - 8); y <= raiseTo; y++) {
						this.placeBlock(level, y == raiseTo ? ground : foundation, x, y, z, box);
					}
				} else if (top > cutAbove) {
					for (int y = cutAbove + 1; y <= Math.min(top, cutAbove + 12); y++) {
						this.placeBlock(level, air, x, y, z, box);
					}
					this.placeBlock(level, ground, x, cutAbove, z, box);
				}
			}
		}
	}

	private void placeBlocks(WorldGenLevel level, BoundingBox box, Blueprint b, RandomSource random) {
		Set<Long> special = new HashSet<>();
		for (Blueprint.Bed bed : b.beds) {
			special.add(Blueprint.key(bed.x(), bed.y(), bed.z()));
			special.add(Blueprint.key(bed.x(), bed.y(), bed.z() + 1));
		}
		for (Blueprint.Container container : b.containers) {
			special.add(Blueprint.key(container.x(), container.y(), container.z()));
		}
		for (Map.Entry<Long, BlockState> entry : b.blocks.entrySet()) {
			if (special.contains(entry.getKey())) {
				continue;
			}
			long key = entry.getKey();
			this.placeBlock(level, toPieceFrame(entry.getValue()), BlockPos.getX(key) + MARGIN, BlockPos.getY(key), BlockPos.getZ(key) + MARGIN, box);
		}
		this.connectShapes(level, box, b);
		for (Blueprint.Container container : b.containers) {
			var loot = ModStructures.lootTable(container.loot());
			int x = container.x() + MARGIN;
			int z = container.z() + MARGIN;
			if (container.barrel()) {
				// Barrels and dispensers: the block the layout chose (rotated with the structure), then its loot.
				BlockPos at = this.getWorldPos(x, container.y(), z);
				if (box.isInside(at)) {
					BlockState state = b.get(container.x(), container.y(), container.z());
					this.placeBlock(level, toPieceFrame(state == null ? Blocks.BARREL.defaultBlockState() : state), x, container.y(), z, box);
					RandomizableContainer.setBlockEntityLootTable(level, random, at, loot);
				}
			} else {
				this.createChest(level, box, random, x, container.y(), z, loot);
			}
		}
		for (Blueprint.Lectern lectern : b.lecterns) {
			BlockPos at = this.getWorldPos(lectern.x() + MARGIN, lectern.y(), lectern.z() + MARGIN);
			if (box.isInside(at) && level.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity entity) {
				entity.setBook(com.itzbreadloaf.item.LilyBooks.stack(lectern.book()));
			}
		}
		for (Blueprint.Bed bed : b.beds) {
			// Point the bed from foot to head in real world positions, so both halves match however it's rotated.
			BlockPos foot = this.getWorldPos(bed.x() + MARGIN, bed.y(), bed.z() + MARGIN);
			BlockPos head = this.getWorldPos(bed.x() + MARGIN, bed.y(), bed.z() + 1 + MARGIN);
			if (box.isInside(foot) && box.isInside(head)) {
				Direction facing = Direction.getApproximateNearest(head.getX() - foot.getX(), 0, head.getZ() - foot.getZ());
				BlockState state = bed.block().defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, BedPart.FOOT);
				level.setBlock(foot, state, 2);
				level.setBlock(head, state.setValue(BedBlock.PART, BedPart.HEAD), 2);
			}
		}
		for (Blueprint.Pillar pillar : b.pillars) {
			this.columnDown(level, pillar.state(), pillar.x() + MARGIN, pillar.y() - 1, pillar.z() + MARGIN, box, PILLAR_DEPTH);
		}
		for (int[] hive : b.hives) {
			BlockPos at = this.getWorldPos(hive[0] + MARGIN, hive[1], hive[2] + MARGIN);
			if (box.isInside(at) && level.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.BeehiveBlockEntity beehive) {
				for (int i = 0; i < 2; i++) {
					beehive.storeBee(net.minecraft.world.level.block.entity.BeehiveBlockEntity.Occupant.create(random.nextInt(599)));
				}
			}
		}
		for (Blueprint.Mob mob : b.mobs) {
			this.placeMob(level, box, mob);
		}
		this.placeResidents(level, box, b);
	}

	/**
	 * Blueprints use north = low z (the front). Vanilla's piece placement (getWorldPos plus the piece's mirror and
	 * rotation) treats a block's NORTH as local +z instead, so stairs, ladders, doors and trapdoors along z came out
	 * backwards. Mirroring north/south first makes every facing land the right way for all four orientations.
	 */
	private static BlockState toPieceFrame(BlockState state) {
		return state.mirror(net.minecraft.world.level.block.Mirror.LEFT_RIGHT);
	}

	/**
	 * Blocks are placed without neighbor updates, so glass panes, iron bars, fences and walls stood as lone posts
	 * and stairs didn't form corners. Fix their shapes from their neighbors: while the world generates, through
	 * vanilla's post-processing (it runs once the neighboring chunks exist, so chunk edges connect too); for
	 * structures built during play, right away.
	 */
	private void connectShapes(WorldGenLevel level, BoundingBox box, Blueprint b) {
		boolean live = level instanceof net.minecraft.server.level.ServerLevel;
		for (Map.Entry<Long, BlockState> entry : b.blocks.entrySet()) {
			net.minecraft.world.level.block.Block block = entry.getValue().getBlock();
			if (!(block instanceof net.minecraft.world.level.block.CrossCollisionBlock || block instanceof net.minecraft.world.level.block.WallBlock
					|| block instanceof net.minecraft.world.level.block.StairBlock)) {
				continue;
			}
			long key = entry.getKey();
			BlockPos at = this.getWorldPos(BlockPos.getX(key) + MARGIN, BlockPos.getY(key), BlockPos.getZ(key) + MARGIN);
			if (!box.isInside(at)) {
				continue;
			}
			if (live) {
				BlockState placed = level.getBlockState(at);
				BlockState shaped = net.minecraft.world.level.block.Block.updateFromNeighbourShapes(placed, level, at);
				if (shaped != placed) {
					level.setBlock(at, shaped, 2);
				}
			} else {
				level.getChunk(at).markPosForPostprocessing(at);
			}
		}
	}

	/** Like fillColumnDown, but only through air, water and plants, and at most {@code maxDepth} blocks. */
	private void columnDown(WorldGenLevel level, BlockState state, int x, int y, int z, BoundingBox box, int maxDepth) {
		for (int i = 0; i < maxDepth; i++) {
			BlockPos at = this.getWorldPos(x, y - i, z);
			if (!box.isInside(at) || at.getY() <= level.getMinY()) {
				return;
			}
			BlockState current = level.getBlockState(at);
			if (!current.isAir() && current.getFluidState().isEmpty() && !current.canBeReplaced()) {
				return;
			}
			level.setBlock(at, state, 2);
		}
	}

	private void placeMob(WorldGenLevel level, BoundingBox box, Blueprint.Mob mob) {
		BlockPos at = this.getWorldPos(mob.x() + MARGIN, mob.y(), mob.z() + MARGIN);
		if (!box.isInside(at)) {
			return;
		}
		net.minecraft.world.entity.EntityType<?> type = switch (mob.type()) {
			case "cow" -> net.minecraft.world.entity.EntityType.COW;
			case "sheep" -> net.minecraft.world.entity.EntityType.SHEEP;
			case "chicken" -> net.minecraft.world.entity.EntityType.CHICKEN;
			case "pig" -> net.minecraft.world.entity.EntityType.PIG;
			case "horse" -> net.minecraft.world.entity.EntityType.HORSE;
			case "strider" -> net.minecraft.world.entity.EntityType.STRIDER;
			default -> net.minecraft.world.entity.EntityType.ARMOR_STAND;
		};
		net.minecraft.world.entity.Entity entity = type.create(level.getLevel(), net.minecraft.world.entity.EntitySpawnReason.STRUCTURE);
		if (entity == null) {
			return;
		}
		entity.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
		if (entity instanceof net.minecraft.world.entity.decoration.ArmorStand stand) {
			// A training dummy in old armor with an iron axe (not a mace: maces are Rare loot).
			stand.setShowArms(true);
			stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CHAINMAIL_CHESTPLATE));
			stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LEATHER_HELMET));
			stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_AXE));
		} else if (entity instanceof net.minecraft.world.entity.Mob animal) {
			animal.setPersistenceRequired();
		}
		level.addFreshEntityWithPassengers(entity);
	}

	/** One marker block at the first resident's spawn holds everyone who lives here; they appear when a player comes near. */
	private void placeResidents(WorldGenLevel level, BoundingBox box, Blueprint b) {
		List<Blueprint.Resident> residents = b.residents();
		if (residents.isEmpty() || this.spawned) {
			return;
		}
		Blueprint.Resident first = residents.get(0);
		BlockPos markerPos = this.getWorldPos(first.x() + MARGIN, first.y(), first.z() + MARGIN);
		if (!box.isInside(markerPos)) {
			return;
		}
		this.spawned = true;
		UUID group = Mth.createInsecureUUID(RandomSource.create(this.seed ^ 0x5DEECE66DL));
		level.setBlock(markerPos, ModBlocks.RESIDENT_MARKER.defaultBlockState(), 2);
		if (level.getBlockEntity(markerPos) instanceof ResidentMarkerBlockEntity marker) {
			for (Blueprint.Resident r : residents) {
				BlockPos spawn = this.getWorldPos(r.x() + MARGIN, r.y(), r.z() + MARGIN);
				BlockPos home = this.getWorldPos(r.homeX() + MARGIN, r.y(), r.homeZ() + MARGIN);
				BlockPos bed = r.bed() == null ? null : this.getWorldPos(r.bed().getX() + MARGIN, r.bed().getY(), r.bed().getZ() + MARGIN);
				BlockPos pantry = r.pantry() == null ? null : this.getWorldPos(r.pantry().getX() + MARGIN, r.pantry().getY(), r.pantry().getZ() + MARGIN);
				marker.addResident(r.variant(), spawn, home, r.radius(), bed, r.group() ? group : null, pantry);
			}
			marker.setChanged();
		}
	}
}
