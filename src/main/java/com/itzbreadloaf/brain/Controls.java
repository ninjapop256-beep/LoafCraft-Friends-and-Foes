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
 *
 * Human aim ({@link #humanAim}): instead of snapping, the view moves like a hand on a mouse: it speeds toward the
 * point (capped at the fighter's aim speed), overshoots a little and corrects, lags behind a target that's moving,
 * and never sits perfectly still (a small wobble the size of the fighter's aim error).
 *
 * Hotbar switches can take time ({@link #switchSlot}): a few ticks, like pressing a number key or scrolling.
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
	private boolean humanLook;
	private float aimSpeed = 30.0F;
	private float aimError = 3.0F;
	private float yawVelocity;
	private float pitchVelocity;
	private float wobbleYaw;
	private float wobblePitch;
	private int pendingSlot = -1;
	private int pendingTicks;

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
		this.humanLook = false;
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

	/**
	 * Aim at a point like a person with a mouse: at most {@code speed} degrees per tick, overshooting and correcting,
	 * with a wobble of about {@code error} degrees.
	 */
	public void humanAim(Vec3 point, float speed, float error) {
		this.lookAt(point);
		this.humanLook = true;
		this.aimSpeed = Math.max(4.0F, speed);
		this.aimError = Math.max(0.0F, error);
	}

	/** {@link #humanAim} for a direction instead of a point. */
	public void humanLook(float yaw, float pitch, float speed, float error) {
		this.look(yaw, pitch);
		this.humanLook = true;
		this.aimSpeed = Math.max(4.0F, speed);
		this.aimError = Math.max(0.0F, error);
	}

	/** How far (degrees) the view is from where it's being asked to look, or 0 with no look this tick. */
	public float aimOffset() {
		if (this.wantYaw == null) {
			return 0.0F;
		}
		return Math.abs(Mth.wrapDegrees(this.wantYaw - this.player.getYRot())) + Math.abs(this.wantPitch - this.player.getXRot());
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

	/**
	 * Switch to a hotbar slot after {@code delayTicks} (a person pressing a key or scrolling). Asking again for the
	 * same slot doesn't restart the wait. Switching resets the attack charge, as it does for players.
	 */
	public void switchSlot(int slot, int delayTicks) {
		int clamped = Mth.clamp(slot, 0, 8);
		if (clamped == this.player.getInventory().getSelectedSlot()) {
			// Already holding it: any other switch on its way is called off.
			this.pendingSlot = -1;
			return;
		}
		if (clamped == this.pendingSlot) {
			return;
		}
		if (delayTicks <= 0) {
			this.selectSlot(clamped);
			return;
		}
		this.pendingSlot = clamped;
		this.pendingTicks = delayTicks;
	}

	/** A slot switch is still on its way. */
	public boolean isSwitching() {
		return this.pendingSlot >= 0;
	}

	/** Hotbar slot 0-8 (Carpet's slots are 1-9), right now. Cancels any slower switch on its way. */
	public void selectSlot(int slot) {
		this.pendingSlot = -1;
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
		if (this.pendingSlot >= 0 && --this.pendingTicks <= 0) {
			int slot = this.pendingSlot;
			this.selectSlot(slot);
		}
		if (this.wantYaw != null) {
			float yaw = this.player.getYRot();
			float pitch = this.player.getXRot();
			float dYaw;
			float dPitch;
			if (this.humanLook && !this.snapLook) {
				// The hand never holds perfectly still: a drifting wobble about the size of the aim error.
				var random = this.player.getRandom();
				this.wobbleYaw += (random.nextFloat() - 0.5F) * this.aimError * 0.6F - this.wobbleYaw * 0.15F;
				this.wobblePitch += (random.nextFloat() - 0.5F) * this.aimError * 0.3F - this.wobblePitch * 0.15F;
				this.wobbleYaw = Mth.clamp(this.wobbleYaw, -this.aimError * 1.5F, this.aimError * 1.5F);
				this.wobblePitch = Mth.clamp(this.wobblePitch, -this.aimError, this.aimError);
				float toYaw = Mth.wrapDegrees(this.wantYaw + this.wobbleYaw - yaw);
				float toPitch = this.wantPitch + this.wobblePitch - pitch;
				// A spring that's a little loose: speeds up toward the point, overshoots ~15% and settles in ~6 ticks.
				this.yawVelocity = Mth.clamp(this.yawVelocity * 0.35F + toYaw * 0.55F, -this.aimSpeed, this.aimSpeed);
				this.pitchVelocity = Mth.clamp(this.pitchVelocity * 0.35F + toPitch * 0.55F, -this.aimSpeed, this.aimSpeed);
				dYaw = this.yawVelocity;
				dPitch = this.pitchVelocity;
			} else {
				dYaw = Mth.wrapDegrees(this.wantYaw - yaw);
				dPitch = this.wantPitch - pitch;
				if (!this.snapLook) {
					dYaw = Mth.clamp(dYaw, -MAX_TURN, MAX_TURN);
					dPitch = Mth.clamp(dPitch, -MAX_TURN, MAX_TURN);
				}
				this.yawVelocity = dYaw * 0.5F;
				this.pitchVelocity = dPitch * 0.5F;
			}
			this.pack.look(yaw + dYaw, Mth.clamp(pitch + dPitch, -90.0F, 90.0F));
			this.player.setYHeadRot(this.player.getYRot());
		} else {
			this.yawVelocity *= 0.5F;
			this.pitchVelocity *= 0.5F;
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
