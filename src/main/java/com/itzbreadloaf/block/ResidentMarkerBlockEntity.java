package com.itzbreadloaf.block;

import com.itzbreadloaf.brain.BreadLoafBrain;
import com.itzbreadloaf.brain.MadisonBrain;
import com.itzbreadloaf.boss.BossSpawner;
import com.itzbreadloaf.brain.LoafBrain;
import com.itzbreadloaf.character.CharacterKind;
import com.itzbreadloaf.character.CharacterVariant;
import com.itzbreadloaf.character.LoafCharacter;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.LoafIds;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Who lives at this house or tent, waiting to be created when a real player first comes near. */
public class ResidentMarkerBlockEntity extends BlockEntity {
	public static final double WAKE_DISTANCE = 48.0;

	private final List<CompoundTag> residents = new ArrayList<>();

	public ResidentMarkerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.RESIDENT_MARKER_ENTITY, pos, state);
	}

	public void addResident(CharacterVariant kind, BlockPos spawn, BlockPos home, int radius, @Nullable BlockPos bed,
			@Nullable UUID group, @Nullable BlockPos pantry) {
		CompoundTag tag = new CompoundTag();
		tag.putString("Kind", kind.id);
		tag.store("Spawn", BlockPos.CODEC, spawn.immutable());
		tag.store("Home", BlockPos.CODEC, home.immutable());
		tag.putInt("Radius", radius);
		if (bed != null) {
			tag.store("Bed", BlockPos.CODEC, bed.immutable());
		}
		if (group != null) {
			tag.store("Group", UUIDUtil.CODEC, group);
		}
		if (pantry != null) {
			tag.store("Pantry", BlockPos.CODEC, pantry.immutable());
		}
		this.residents.add(tag);
		this.setChanged();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ResidentMarkerBlockEntity marker) {
		if (!(level instanceof ServerLevel serverLevel) || level.getGameTime() % 20 != 0) {
			return;
		}
		Player near = serverLevel.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, WAKE_DISTANCE,
				player -> LoafIds.isRealPlayer(player) && !player.isSpectator());
		if (near == null) {
			return;
		}
		for (CompoundTag tag : marker.residents) {
			CharacterVariant kind = CharacterVariant.byId(tag.getStringOr("Kind", "itzbreadloaf"));
			BlockPos spawn = tag.read("Spawn", BlockPos.CODEC).orElse(pos);
			BlockPos home = tag.read("Home", BlockPos.CODEC).orElse(spawn);
			int radius = tag.getIntOr("Radius", 10);
			BlockPos bed = tag.read("Bed", BlockPos.CODEC).orElse(null);
			UUID group = tag.read("Group", UUIDUtil.CODEC).orElse(null);
			BlockPos pantry = tag.read("Pantry", BlockPos.CODEC).orElse(null);
			CharacterVariant variant = kind == null ? CharacterVariant.BREADLOAF : kind;
			java.util.function.Consumer<LoafBrain> setup = brain -> {
				brain.setupHome(home, radius, bed);
				if (brain instanceof BreadLoafBrain bread) {
					bread.setGroupId(group);
				}
				if (brain instanceof MadisonBrain madison) {
					madison.setPantryPos(pantry);
				}
			};
			if (variant == CharacterVariant.WILD_FELIX) {
				// A wild FelixSyn at his own home (Harley sometimes).
				com.itzbreadloaf.brain.FelixBrain.spawnWild(serverLevel, Vec3.atBottomCenterOf(spawn), home, radius, bed);
			} else if (variant.kind == CharacterKind.FELIXSYN) {
				// A FelixSyn in a village or structure: Harley only sometimes (felixHarleyChance).
				com.itzbreadloaf.brain.FelixBrain.spawnDuo(serverLevel, Vec3.atBottomCenterOf(spawn), home, true);
			} else if (variant.kind == CharacterKind.MASTER) {
				// The boss makes an entrance (sound and message).
				LoafCharacter boss = BossSpawner.spawnBoss(serverLevel, spawn);
				if (boss != null) {
					setup.accept(boss.brain);
				}
			} else {
				CharacterManager.create(serverLevel, variant, Vec3.atBottomCenterOf(spawn), serverLevel.getRandom().nextFloat() * 360.0F, setup);
			}
		}
		marker.residents.clear();
		level.removeBlock(pos, false);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("Residents", CompoundTag.CODEC.listOf(), List.copyOf(this.residents));
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.residents.clear();
		input.read("Residents", CompoundTag.CODEC.listOf()).ifPresent(this.residents::addAll);
	}
}
