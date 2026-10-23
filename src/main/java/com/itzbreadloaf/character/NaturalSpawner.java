package com.itzbreadloaf.character;

import com.itzbreadloaf.config.ModConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Wild ItzBreadLoafs: like a rare monster spawn, but a Carpet fake player. On the surface: ItzBreadLoaf (1 in 100 is a
 * Sweaty one); in caves: Miners; in the Nether: Nether ItzBreadLoafs.
 * the Overworld, a small chance (from itzBreadLoafSpawnWeight in the config; zombies are 95) to spawn one 24-48 blocks
 * away on grass/dirt, stone, sand, gravel or snow, at any light level. Never on Peaceful, and not if two are already
 * near. Wild ones are forgotten when they despawn unless someone befriended or tamed them.
 */
public final class NaturalSpawner {
	public static final int INTERVAL = 400;
	public static final int MAX_NEARBY = 2;

	private NaturalSpawner() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(NaturalSpawner::tick);
	}

	private static void tick(MinecraftServer server) {
		if (server.getTickCount() % INTERVAL != 0) {
			return;
		}
		int weight = ModConfig.get().itzBreadLoafSpawnWeight;
		if (weight <= 0) {
			return;
		}
		// Weight 5 -> 2.5% per player per check (about one every 13 minutes near each player).
		float chance = weight / 200.0F;
		for (ServerPlayer player : CharacterManager.realPlayers(server)) {
			ServerLevel level = player.level();
			RandomSource random = level.getRandom();
			if (level.getDifficulty() == Difficulty.PEACEFUL || !level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.SPAWN_MONSTERS)
					|| random.nextFloat() >= chance || nearbyBreads(player) >= MAX_NEARBY) {
				continue;
			}
			CharacterVariant variant;
			BlockPos spot;
			if (level.dimension() == Level.NETHER) {
				// Nether ItzBreadLoafs: gold armor, never burn.
				variant = CharacterVariant.NETHER;
				spot = findCaveSpot(level, player.blockPosition(), random, false);
			} else if (level.dimension() == Level.OVERWORLD && isUnderground(level, player.blockPosition())) {
				// Miners: down in the caves with their pickaxes and torches.
				variant = CharacterVariant.MINER;
				spot = findCaveSpot(level, player.blockPosition(), random, true);
			} else if (level.dimension() == Level.OVERWORLD) {
				// 1 in 100 is a Sweaty ItzBreadLoaf.
				variant = random.nextInt(100) == 0 ? CharacterVariant.SWEATY : CharacterVariant.BREADLOAF;
				spot = findSpot(level, player.blockPosition(), random);
			} else {
				continue;
			}
			if (spot != null) {
				CharacterManager.create(level, variant, Vec3.atBottomCenterOf(spot), random.nextFloat() * 360.0F, brain -> brain.setWild(true));
			}
		}
		// Wild FelixSyns on the surface (rarer than ItzBreadLoafs; most live in their own homes).
		int felixWeight = ModConfig.get().wildFelixSpawnWeight;
		for (ServerPlayer player : felixWeight <= 0 ? java.util.List.<ServerPlayer>of() : CharacterManager.realPlayers(server)) {
			ServerLevel level = player.level();
			RandomSource random = level.getRandom();
			if (level.dimension() != Level.OVERWORLD || level.getDifficulty() == Difficulty.PEACEFUL
					|| random.nextFloat() >= felixWeight / 200.0F || isUnderground(level, player.blockPosition()) || nearbyFelixes(player) >= 1) {
				continue;
			}
			BlockPos spot = findSpot(level, player.blockPosition(), random);
			if (spot != null) {
				LoafCharacter felix = com.itzbreadloaf.brain.FelixBrain.spawnWild(level, Vec3.atBottomCenterOf(spot), null, 0, null);
				if (felix != null && felix.brain != null) {
					felix.brain.setWild(true);
				}
			}
		}
	}

	private static boolean isUnderground(Level level, BlockPos pos) {
		return pos.getY() < level.getSeaLevel() - 10 && level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos) == 0;
	}

	/** A dark spot in a cave (or anywhere in the Nether) 12-28 blocks away, within 10 blocks up or down. */
	private static BlockPos findCaveSpot(Level level, BlockPos center, RandomSource random, boolean needsDark) {
		for (int attempt = 0; attempt < 16; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double distance = 12.0 + random.nextDouble() * 16.0;
			int x = center.getX() + (int) Math.round(Math.cos(angle) * distance);
			int z = center.getZ() + (int) Math.round(Math.sin(angle) * distance);
			for (int dy = 10; dy >= -10; dy--) {
				BlockPos feet = new BlockPos(x, center.getY() + dy, z);
				if (!level.isLoaded(feet)) {
					break;
				}
				if (level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
						&& level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
						&& level.getFluidState(feet).isEmpty() && level.getFluidState(feet.below()).isEmpty()
						&& level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP)
						&& (!needsDark || level.getBrightness(net.minecraft.world.level.LightLayer.SKY, feet) == 0)) {
					return feet;
				}
			}
		}
		return null;
	}

	private static int nearbyFelixes(ServerPlayer player) {
		int count = 0;
		for (LoafCharacter character : CharacterManager.active()) {
			if (character.kind() == CharacterKind.FELIXSYN && character.player.level() == player.level()
					&& character.player.distanceTo(player) < 96.0) {
				count++;
			}
		}
		return count;
	}

	private static int nearbyBreads(ServerPlayer player) {
		int count = 0;
		for (LoafCharacter character : CharacterManager.active()) {
			if (character.kind() == CharacterKind.BREADLOAF && character.player.level() == player.level()
					&& character.player.distanceTo(player) < 64.0) {
				count++;
			}
		}
		return count;
	}

	private static BlockPos findSpot(Level level, BlockPos center, RandomSource random) {
		for (int attempt = 0; attempt < 10; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double distance = 24.0 + random.nextDouble() * 24.0;
			int x = center.getX() + (int) Math.round(Math.cos(angle) * distance);
			int z = center.getZ() + (int) Math.round(Math.sin(angle) * distance);
			BlockPos column = new BlockPos(x, 0, z);
			if (!level.isLoaded(column)) {
				continue;
			}
			int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
			BlockPos feet = new BlockPos(x, y, z);
			BlockState below = level.getBlockState(feet.below());
			boolean ground = below.is(BlockTags.DIRT) || below.is(BlockTags.BASE_STONE_OVERWORLD) || below.is(BlockTags.SAND)
					|| below.is(Blocks.GRAVEL) || below.is(Blocks.SNOW_BLOCK);
			if (ground && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
					&& level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
					&& level.getFluidState(feet).isEmpty()) {
				return feet;
			}
		}
		return null;
	}
}
