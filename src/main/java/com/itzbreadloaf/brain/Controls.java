package com.itzbreadloaf.brain;

import carpet.helpers.EntityPlayerActionPack;
import carpet.helpers.EntityPlayerActionPack.Action;
import carpet.helpers.EntityPlayerActionPack.ActionType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The character's keyboard and mouse. Brains only ever move, look, jump, click and switch slots through here,
 * and this is the one place that talks to Carpet's {@link EntityPlayerActionPack}.
 *
 * Each tick: the brain sets what it wants (walk direction, where to look, buttons), then {@link #apply()} turns the
 * look into a real yaw/pitch (turning at most {@link #MAX_TURN} degrees per tick, like a fast mouse) and converts
 * the wanted walk direction into forward/strafe input relative to where the player is now facing.
 */
public final class Controls {
	public static final float MAX_TURN = 45.0F;

	private final ServerPlayer player;
	private final EntityPlayerActionPack pack;

	private @Nullable Vec3 walkDirection;
	private float walkScale = 1.0F;
	private @Nullable Float wantYaw;
	private float wantPitch;
	private boolean snapLook;
	private boolean sprint;
	private boolean sneak;
	private boolean holdingJump;
	private boolean holdingUse;
	private boolean holdingAttack;
	private int lastSlot = -1;

	public Controls(ServerPlayer player, EntityPlayerActionPack pack) {
		this.player = player;
		this.pack = pack;
	}

	/** Start of a brain tick: forget last tick's walk and look wishes (held buttons stay held). */
	public void begin() {
		this.walkDirection = null;
		this.walkScale = 1.0F;
		this.wantYaw = null;
		this.snapLook = false;
	}

	// ---------------------------------------------------------------- movement

	/** Walk in a world direction (only x/z matter). The input is converted to forward/strafe in {@link #apply()}. */
	public void walk(Vec3 direction) {
		this.walkDirection = new Vec3(direction.x, 0.0, direction.z);
	}

	public void walk(Vec3 direction, float scale) {
		this.walk(direction);
		this.walkScale = scale;
	}

	public void walkToward(Vec3 point) {
		this.walk(point.subtract(this.player.position()));
	}

	public boolean isWalking() {
		return this.walkDirection != null;
	}

	public void setSprinting(boolean sprint) {
		this.sprint = sprint;
	}

	public void setSneaking(boolean sneak) {
		this.sneak = sneak;
	}

	public boolean isSneaking() {
		return this.sneak;
	}

	// ---------------------------------------------------------------- looking

	public void look(float yaw, float pitch) {
		this.wantYaw = yaw;
		this.wantPitch = pitch;
	}

	/** Look exactly there this tick (no turn speed limit), for aiming. */
	public void lookInstantly(float yaw, float pitch) {
		this.look(yaw, pitch);
		this.snapLook = true;
	}

	public void lookAt(Vec3 point) {
		Vec3 eyes = this.player.getEyePosition();
		Vec3 d = point.subtract(eyes);
		double horizontal = Math.sqrt(d.x * d.x + d.z * d.z);
		this.look((float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0F, (float) -(Mth.atan2(d.y, horizontal) * Mth.RAD_TO_DEG));
	}

	public void aimAt(Vec3 point) {
		this.lookAt(point);
		this.snapLook = true;
	}

	public boolean hasLook() {
		return this.wantYaw != null;
	}

	public static float yawTo(Vec3 from, Vec3 to) {
		return (float) (Mth.atan2(to.z - from.z, to.x - from.x) * Mth.RAD_TO_DEG) - 90.0F;
	}

	// ---------------------------------------------------------------- buttons

	public void jumpOnce() {
		if (!this.holdingJump) {
			this.pack.start(ActionType.JUMP, Action.once());
		}
	}

	public void holdJump(boolean hold) {
		if (hold != this.holdingJump) {
			this.holdingJump = hold;
			this.pack.start(ActionType.JUMP, hold ? Action.continuous() : null);
		}
	}

	/** One right-click: place a block, open a door, throw a pearl, fire a charged crossbow... */
	public void useOnce() {
		this.holdingUse = false;
		this.pack.start(ActionType.USE, Action.once());
	}

	/** Hold right-click: draw a bow, eat, drink, raise a shield. Letting go releases (fires the bow). */
	public void holdUse(boolean hold) {
		if (hold != this.holdingUse) {
			this.holdingUse = hold;
			this.pack.start(ActionType.USE, hold ? Action.continuous() : null);
		}
	}

	public boolean isHoldingUse() {
		return this.holdingUse;
	}

	/** One left-click at whatever the crosshair is on. */
	public void attackOnce() {
		this.holdingAttack = false;
		this.pack.start(ActionType.ATTACK, Action.once());
	}

	/** Hold left-click (mining a block). */
	public void holdAttack(boolean hold) {
		if (hold != this.holdingAttack) {
			this.holdingAttack = hold;
			this.pack.start(ActionType.ATTACK, hold ? Action.continuous() : null);
		}
	}

	public void swapHands() {
		this.pack.start(ActionType.SWAP_HANDS, Action.once());
	}

	/** Hotbar slot 0-8 (Carpet's slots are 1-9). */
	public void selectSlot(int slot) {
		int clamped = Mth.clamp(slot, 0, 8);
		if (clamped != this.player.getInventory().getSelectedSlot() || clamped != this.lastSlot) {
			this.pack.setSlot(clamped + 1);
			this.lastSlot = clamped;
		}
	}

	/** Lets go of everything: no movement, no buttons. */
	public void releaseAll() {
		this.walkDirection = null;
		this.holdJump(false);
		this.holdUse(false);
		this.holdAttack(false);
		this.sprint = false;
		this.pack.setForward(0.0F);
		this.pack.setStrafing(0.0F);
		this.pack.setSprinting(false);
	}

	// ---------------------------------------------------------------- end of tick

	public void apply() {
		if (this.wantYaw != null) {
			float yaw = this.player.getYRot();
			float pitch = this.player.getXRot();
			float dYaw = Mth.wrapDegrees(this.wantYaw - yaw);
			float dPitch = this.wantPitch - pitch;
			if (!this.snapLook) {
				dYaw = Mth.clamp(dYaw, -MAX_TURN, MAX_TURN);
				dPitch = Mth.clamp(dPitch, -MAX_TURN, MAX_TURN);
			}
			this.pack.look(yaw + dYaw, Mth.clamp(pitch + dPitch, -90.0F, 90.0F));
			this.player.setYHeadRot(this.player.getYRot());
		}
		float forward = 0.0F;
		float strafe = 0.0F;
		if (this.walkDirection != null && this.walkDirection.lengthSqr() > 1.0E-4) {
			float moveYaw = (float) (Mth.atan2(this.walkDirection.z, this.walkDirection.x) * Mth.RAD_TO_DEG) - 90.0F;
			float delta = Mth.wrapDegrees(moveYaw - this.player.getYRot()) * Mth.DEG_TO_RAD;
			forward = Mth.cos(delta) * this.walkScale;
			strafe = -Mth.sin(delta) * this.walkScale;
		}
		this.pack.setForward(forward);
		this.pack.setStrafing(strafe);
		// Players can only sprint forwards.
		boolean canSprint = this.sprint && forward > 0.7F && !this.sneak && this.player.getFoodData().getFoodLevel() > 6;
		this.pack.setSprinting(canSprint);
		this.pack.setSneaking(this.sneak);
	}
}
