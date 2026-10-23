package com.itzbreadloaf.item;

import com.itzbreadloaf.boss.BossSpawner;
import com.itzbreadloaf.character.CharacterKind;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.LoafCharacter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

/**
 * Spawn egg for a character: use it on a block to spawn a new Carpet fake player there
 * (ItzBreadLoaf, ImLilyLol, Madison, or Master BreadLoaf with his boss entrance).
 */
public class LoafSpawnEggItem extends Item {
	private final CharacterKind kind;

	public LoafSpawnEggItem(CharacterKind kind, Properties properties) {
		super(properties);
		this.kind = kind;
	}

	public CharacterKind kind() {
		return this.kind;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (!(context.getLevel() instanceof ServerLevel level)) {
			return InteractionResult.SUCCESS;
		}
		BlockPos clicked = context.getClickedPos();
		BlockPos pos = level.getBlockState(clicked).getCollisionShape(level, clicked).isEmpty()
				? clicked : clicked.relative(context.getClickedFace());
		Player player = context.getPlayer();
		float yaw = player == null ? 0.0F : player.getYRot() + 180.0F;
		LoafCharacter character;
		if (this.kind == CharacterKind.MASTER) {
			character = BossSpawner.spawnBoss(level, pos);
		} else if (this.kind == CharacterKind.FELIXSYN) {
			character = com.itzbreadloaf.brain.FelixBrain.spawnDuo(level, Vec3.atBottomCenterOf(pos), pos);
		} else {
			character = CharacterManager.create(level, this.kind, Vec3.atBottomCenterOf(pos), yaw, null);
		}
		if (character != null && player != null && !player.getAbilities().instabuild) {
			context.getItemInHand().shrink(1);
		}
		return InteractionResult.SUCCESS;
	}
}
