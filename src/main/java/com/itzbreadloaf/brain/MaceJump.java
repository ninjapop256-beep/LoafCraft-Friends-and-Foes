package com.itzbreadloaf.brain;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * The PvP mace move, with real inputs: look straight down, jump, throw a wind charge at your feet (it launches you
 * up), switch to the mace, drift toward the target and smash them on the way down (a mace hit while falling more than
 * 1.5 blocks is a smash attack; Wind Burst bounces you back up for another). Used by FelixSyn and Harley.
 */
public final class MaceJump {
	private final ServerPlayer player;
	private final Controls controls;
	private final Combat combat;
	private int ticks = -1;
	private int cooldown;

	public MaceJump(ServerPlayer player, Controls controls, Combat combat) {
		this.player = player;
		this.controls = controls;
		this.combat = combat;
	}

	public boolean isActive() {
		return this.ticks >= 0;
	}

	public void tickCooldown() {
		if (this.cooldown > 0) {
			this.cooldown--;
		}
	}

	public boolean canStart() {
		return this.ticks < 0 && this.cooldown <= 0 && this.player.onGround()
				&& Inv.find(this.player, stack -> stack.is(Items.WIND_CHARGE)) >= 0
				&& Inv.find(this.player, stack -> stack.is(Items.MACE)) >= 0;
	}

	public void start(int cooldownTicks) {
		this.ticks = 0;
		this.cooldown = cooldownTicks;
		this.combat.stop();
	}

	public void cancel() {
		this.ticks = -1;
	}

	/** One tick of the move. Returns false once it's over (landed or gave up). */
	public boolean tick(LivingEntity target, float maxSmashFall) {
		this.ticks++;
		if (this.ticks <= 6) {
			int charge = Inv.ready(this.player, stack -> stack.is(Items.WIND_CHARGE), 4);
			if (charge < 0) {
				this.ticks = -1;
				return false;
			}
			this.controls.selectSlot(charge);
			this.controls.lookInstantly(Controls.yawTo(this.player.position(), target.position()), 90.0F);
			if (this.player.onGround() && this.ticks <= 2) {
				this.controls.jumpOnce();
			} else if (!this.player.onGround() && this.ticks >= 3) {
				this.controls.useOnce();
				this.ticks = 6;
			} else if (this.ticks == 6) {
				// The jump never happened (ceiling, stuck): give up.
				this.ticks = -1;
				return false;
			}
			return true;
		}
		int mace = Inv.ready(this.player, stack -> stack.is(Items.MACE), 2);
		if (mace >= 0) {
			this.controls.selectSlot(mace);
		}
		Vec3 aim = this.combat.aimPoint(target);
		this.controls.aimAt(aim);
		this.controls.walkToward(target.position());
		this.controls.setSprinting(false);
		if (this.player.fallDistance > maxSmashFall) {
			this.player.fallDistance = maxSmashFall;
		}
		boolean falling = this.player.getDeltaMovement().y < 0.0 && !this.player.onGround();
		if (falling && this.player.getEyePosition().distanceTo(aim) <= this.combat.reach() - 0.1
				&& this.player.fallDistance > 1.5 && this.player.getAttackStrengthScale(0.5F) >= 0.9F) {
			this.controls.attackOnce();
		}
		if (this.ticks > 12 && this.player.onGround() || this.ticks > 120) {
			this.ticks = -1;
			return false;
		}
		return true;
	}
}
