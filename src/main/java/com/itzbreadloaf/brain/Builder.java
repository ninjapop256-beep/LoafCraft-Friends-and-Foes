package com.itzbreadloaf.brain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Places and breaks blocks the way a player does: building blocks come from the real inventory, every block is
 * placed with a right-click on the face of a neighbouring block, and digging holds left-click until the block
 * breaks. Pillaring (jump, look down, place under yourself) and bridging (sneak to the edge, place on the side
 * of the block you stand on) are built on that. Only when the mobGriefing game rule is on.
 */
public final class Builder {
	public static final int BLOCK_SLOT = 8;

	private final ServerPlayer player;
	private final Controls controls;
	private int clickCooldown;
	private @Nullable BlockPos mining;
	private int miningTicks;

	public Builder(ServerPlayer player, Controls controls) {
		this.player = player;
		this.controls = controls;
	}

	public static boolean allowed(ServerLevel level) {
		return level.getGameRules().get(GameRules.MOB_GRIEFING);
	}

	public void tick() {
		if (this.clickCooldown > 0) {
			this.clickCooldown--;
		}
	}

	public boolean hasBlocks() {
		return Inv.find(this.player, Inv::isBuildingBlock) >= 0;
	}

	/** Puts building blocks in hand. False if there are none. */
	public boolean holdBlocks() {
		int slot = Inv.ready(this.player, Inv::isBuildingBlock, BLOCK_SLOT);
		if (slot < 0) {
			return false;
		}
		this.controls.selectSlot(slot);
		return true;
	}

	private ServerLevel level() {
		return this.player.level();
	}

	// ---------------------------------------------------------------- placing

	/**
	 * Right-clicks the face of a solid neighbour so a block lands at {@code pos}. Returns true when a click was sent.
	 * The block in hand must already be selected ({@link #holdBlocks()}).
	 */
	public boolean place(BlockPos pos) {
		if (this.clickCooldown > 0 || !this.level().getBlockState(pos).canBeReplaced()) {
			return false;
		}
		Vec3 eyes = this.player.getEyePosition();
		Direction bestSide = null;
		double best = Double.MAX_VALUE;
		for (Direction side : Direction.values()) {
			BlockPos neighbour = pos.relative(side);
			BlockState state = this.level().getBlockState(neighbour);
			if (state.canBeReplaced() || !state.isFaceSturdy(this.level(), neighbour, side.getOpposite())) {
				continue;
			}
			Vec3 face = faceCenter(neighbour, side.getOpposite());
			double distance = eyes.distanceTo(face);
			if (distance < 4.4 && distance < best) {
				best = distance;
				bestSide = side;
			}
		}
		if (bestSide == null) {
			return false;
		}
		this.controls.aimAt(faceCenter(pos.relative(bestSide), bestSide.getOpposite()));
		this.controls.useOnce();
		this.clickCooldown = 4;
		return true;
	}

	private static Vec3 faceCenter(BlockPos block, Direction face) {
		return Vec3.atCenterOf(block).add(face.getStepX() * 0.499, face.getStepY() * 0.499, face.getStepZ() * 0.499);
	}

	// ---------------------------------------------------------------- pillar

	/**
	 * One tick of pillaring up: look straight down, jump, and at the top of the jump place a block where our
	 * feet were. Returns false when there are no blocks left.
	 */
	public boolean pillar() {
		if (!this.holdBlocks()) {
			return false;
		}
		this.controls.lookInstantly(this.player.getYRot(), 90.0F);
		BlockPos feet = BlockPos.containing(this.player.getX(), Math.floor(this.player.getY() - 0.9), this.player.getZ()).above();
		if (this.player.onGround()) {
			this.controls.jumpOnce();
			return true;
		}
		// Feet above the top of the block we're placing: it won't collide with us.
		BlockPos under = BlockPos.containing(this.player.getX(), this.player.getY() - 1.0, this.player.getZ());
		if (this.player.getY() - under.getY() >= 1.05 && this.level().getBlockState(under).canBeReplaced()
				&& !this.level().getBlockState(under.below()).canBeReplaced() && this.clickCooldown <= 0) {
			this.controls.useOnce();
			this.clickCooldown = 3;
		}
		return feet != null;
	}

	// ---------------------------------------------------------------- bridge

	/**
	 * One tick of bridging toward {@code direction}: sneak to the edge of the block we stand on, lean over it,
	 * and place a block against its side. Returns false when there are no blocks left.
	 */
	public boolean bridge(Direction direction) {
		BlockPos standing = BlockPos.containing(this.player.getX(), this.player.getY() - 0.2, this.player.getZ());
		BlockPos next = standing.relative(direction);
		this.controls.setSneaking(true);
		if (!this.level().getBlockState(next).canBeReplaced()) {
			// Already a block there: step onto it.
			this.controls.walk(Vec3.atLowerCornerOf(direction.getUnitVec3i()), 0.6F);
			return true;
		}
		if (!this.holdBlocks()) {
			return false;
		}
		double edge = direction.getAxis().choose(standing.getX(), standing.getY(), standing.getZ()) + 0.5 + direction.getAxisDirection().getStep() * 0.5;
		double position = direction.getAxis().choose(this.player.getX(), this.player.getY(), this.player.getZ());
		double over = (position - edge) * direction.getAxisDirection().getStep();
		Vec3 face = faceCenter(standing, direction);
		if (over < 0.2) {
			// Creep out over the edge (sneaking keeps us from falling).
			this.controls.walk(Vec3.atLowerCornerOf(direction.getUnitVec3i()), 0.5F);
			this.controls.lookAt(face);
			return true;
		}
		this.controls.aimAt(face);
		if (this.clickCooldown <= 0) {
			this.controls.useOnce();
			this.clickCooldown = 5;
		}
		return true;
	}

	// ---------------------------------------------------------------- digging

	/** Mines a block by holding left-click on it. Returns true while still digging. */
	public boolean dig(BlockPos pos) {
		BlockState state = this.level().getBlockState(pos);
		if (state.isAir() || state.getDestroySpeed(this.level(), pos) < 0 || state.getDestroySpeed(this.level(), pos) > 3.0F) {
			this.stopDigging();
			return false;
		}
		if (!pos.equals(this.mining)) {
			this.mining = pos;
			this.miningTicks = 0;
		}
		if (++this.miningTicks > 200) {
			this.stopDigging();
			return false;
		}
		this.controls.aimAt(Vec3.atCenterOf(pos));
		this.controls.holdAttack(true);
		return true;
	}

	public void stopDigging() {
		if (this.mining != null) {
			this.controls.holdAttack(false);
			this.mining = null;
		}
	}

	public boolean isDigging() {
		return this.mining != null;
	}
}
