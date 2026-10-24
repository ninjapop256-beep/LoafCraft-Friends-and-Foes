package com.itzbreadloaf.brain;

import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.LoafCharacter;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The things characters do in their daily routine (AI overhaul part 3), all with real player inputs: work at a
 * station (open the furnace, the brewing stand, the lectern, then close it), train on a dummy or shadow-box, practice
 * wind-charge jumps, spar, patrol, visit a friend, and head home in the evening.
 *
 * Work comes in stints of 30-60 seconds with short breaks in between, like a person would.
 */
final class Activities {
	private final LoafBrain brain;

	private int stintTicks;
	private int breakTicks;
	private @Nullable BlockPos station;
	private int stationSearch;
	private boolean stationOpen;
	private int stationTicks;
	private @Nullable ArmorStand dummy;
	private int dummySearch;
	private boolean critJump;
	private int strafeDir = 1;
	private int strafeTimer;
	private int practiceCooldown = 200;
	private int practiceTicks = -1;
	private @Nullable LoafBrain visiting;
	private int visitTicks;
	private int visitCooldown;
	private @Nullable Vec3 patrolPoint;
	private int patrolPause;

	Activities(LoafBrain brain) {
		this.brain = brain;
	}

	// ---------------------------------------------------------------- stints and breaks

	/** True while on a break between work stints (the brain then looks around). */
	private boolean onBreak() {
		if (this.breakTicks > 0) {
			this.breakTicks--;
			this.brain.lookAround(8.0);
			return true;
		}
		if (++this.stintTicks > 600 + this.brain.random.nextInt(600)) {
			this.stintTicks = 0;
			this.breakTicks = 100 + this.brain.random.nextInt(120);
			this.stopWork();
			return true;
		}
		return false;
	}

	private BlockPos center() {
		return this.brain.home != null ? this.brain.home : this.brain.player.blockPosition();
	}

	/** Stops whatever work was going on (closes any open station). */
	void stopWork() {
		if (this.stationOpen) {
			this.brain.player.closeContainer();
			this.stationOpen = false;
		}
		this.critJump = false;
		this.practiceTicks = -1;
		this.brain.controls.setSneaking(false);
	}

	void stopVisit() {
		this.visiting = null;
	}

	void stopAll() {
		this.stopWork();
		this.stopVisit();
	}

	// ---------------------------------------------------------------- stations

	/**
	 * Works at the nearest matching block near home: walks over, opens it (right-click), looks at it for a couple of
	 * seconds, closes it, and keeps busy with their hands in between. False if there's no such block.
	 */
	boolean useStation(Predicate<BlockState> test, int radius) {
		if (this.onBreak()) {
			return true;
		}
		if (this.station == null || !test.test(this.brain.player.level().getBlockState(this.station))) {
			if (--this.stationSearch > 0) {
				return false;
			}
			this.stationSearch = 100;
			this.station = this.brain.findBlock(this.center(), radius, 4, test);
			if (this.station == null) {
				return false;
			}
		}
		Vec3 at = Vec3.atCenterOf(this.station);
		if (this.brain.player.getEyePosition().distanceTo(at) > 3.5) {
			if (this.stationOpen) {
				this.stopWork();
			}
			this.brain.nav.moveTo(this.station, 2.0, Navigator.Speed.WALK);
			return true;
		}
		this.brain.nav.stop();
		this.brain.controls.lookAt(at);
		this.stationTicks++;
		if (!this.stationOpen && this.stationTicks % 100 == 0) {
			this.brain.controls.aimAt(at);
			this.brain.controls.useOnce();
			this.stationOpen = true;
		} else if (this.stationOpen && this.stationTicks % 100 == 50) {
			this.brain.player.closeContainer();
			this.stationOpen = false;
		} else if (!this.stationOpen && this.stationTicks % 18 == 0) {
			this.brain.player.swing(InteractionHand.MAIN_HAND);
		}
		return true;
	}

	/** ItzBreadLoaf's baking: the nearest furnace, smoker or crafting table (to knead dough on) near home. */
	boolean bake() {
		return this.useStation(state -> state.is(net.minecraft.world.level.block.Blocks.FURNACE)
				|| state.is(net.minecraft.world.level.block.Blocks.SMOKER)
				|| state.is(net.minecraft.world.level.block.Blocks.CRAFTING_TABLE), this.brain.homeRadius + 6);
	}

	// ---------------------------------------------------------------- training and sparring

	/** Nothing alive (other than {@code except}) right in front, so a practice swing can't hit anyone by accident. */
	private boolean clearInFront(@Nullable Entity except) {
		Vec3 look = this.brain.player.getLookAngle();
		AABB box = this.brain.player.getBoundingBox().move(look.scale(2.0)).inflate(1.5);
		List<Entity> near = this.brain.player.level().getEntities(this.brain.player, box,
				e -> e instanceof LivingEntity && e != except && !(e instanceof ArmorStand));
		return near.isEmpty();
	}

	/**
	 * Training: hits a training dummy (an armor stand near home) at full charge, with jump crits now and then
	 * (full-charge hits are far apart, so the stand only wobbles and never breaks). No dummy: shadow-boxing in place.
	 */
	boolean train() {
		if (this.onBreak()) {
			return true;
		}
		if (this.dummy == null || !this.dummy.isAlive() || this.dummy.distanceTo(this.brain.player) > 16.0) {
			this.dummy = null;
			if (--this.dummySearch <= 0) {
				this.dummySearch = 100;
				BlockPos c = this.center();
				List<ArmorStand> stands = this.brain.player.level().getEntitiesOfClass(ArmorStand.class, new AABB(c).inflate(12.0));
				this.dummy = stands.isEmpty() ? null : stands.get(0);
			}
		}
		float charge = this.brain.player.getAttackStrengthScale(0.5F);
		if (this.dummy != null) {
			Vec3 aim = this.dummy.getBoundingBox().getCenter();
			double distance = this.brain.player.getEyePosition().distanceTo(aim);
			if (distance > 3.0) {
				this.brain.nav.moveTo(this.dummy.blockPosition(), 2.2, Navigator.Speed.WALK);
				return true;
			}
			this.brain.nav.stop();
			this.brain.controls.aimAt(aim);
			this.strafe(0.25F);
			this.swingWhenReady(charge, this.dummy);
			return true;
		}
		// Shadow-boxing: circle a spot, swing at the air at full charge, jump for "crits".
		this.brain.nav.stop();
		this.strafe(0.45F);
		this.brain.controls.look(this.brain.player.getYRot() + (this.brain.random.nextFloat() - 0.5F) * 6.0F, -5.0F);
		this.swingWhenReady(charge, null);
		return true;
	}

	private void strafe(float scale) {
		if (--this.strafeTimer <= 0) {
			this.strafeTimer = 15 + this.brain.random.nextInt(30);
			this.strafeDir = this.brain.random.nextBoolean() ? 1 : -1;
		}
		Vec3 look = this.brain.player.getLookAngle().multiply(1.0, 0.0, 1.0);
		if (look.lengthSqr() < 1.0E-4) {
			return;
		}
		look = look.normalize();
		this.brain.controls.walk(new Vec3(-look.z, 0.0, look.x).scale(this.strafeDir * scale), 1.0F);
		this.brain.controls.setSprinting(false);
	}

	private void swingWhenReady(float charge, @Nullable Entity target) {
		if (this.critJump) {
			boolean falling = !this.brain.player.onGround() && this.brain.player.getDeltaMovement().y < -0.03;
			if (falling && charge >= 0.95F) {
				if (this.clearInFront(target)) {
					this.brain.controls.attackOnce();
				}
				this.critJump = false;
			} else if (this.brain.player.onGround() && this.brain.player.getDeltaMovement().y <= 0.0 && charge >= 0.99F) {
				this.critJump = false;
			}
			return;
		}
		if (charge >= 0.85F && charge < 0.95F && this.brain.player.onGround() && this.brain.random.nextInt(3) == 0) {
			this.critJump = true;
			this.brain.controls.jumpOnce();
			return;
		}
		if (charge >= 0.95F && this.brain.random.nextInt(4) == 0 && this.clearInFront(target)) {
			this.brain.controls.attackOnce();
		}
	}

	/**
	 * Sparring with a partner (Harley with FelixSyn): face each other about 3 blocks apart, circle, and swing at
	 * the air well to one side of them (35 degrees off, so no hit ever lands).
	 */
	boolean spar(LivingEntity partner) {
		if (this.onBreak()) {
			return true;
		}
		double distance = this.brain.player.distanceTo(partner);
		if (distance > 4.5) {
			this.brain.nav.follow(partner, 3.0, Navigator.Speed.WALK);
			return true;
		}
		this.brain.nav.stop();
		if (distance < 2.2) {
			Vec3 away = this.brain.player.position().subtract(partner.position()).multiply(1.0, 0.0, 1.0);
			this.brain.controls.walk(away, 0.6F);
		} else {
			this.strafe(0.5F);
		}
		float yaw = Controls.yawTo(this.brain.player.position(), partner.position()) + 35.0F * this.strafeDir;
		this.brain.controls.look(yaw, 0.0F);
		float charge = this.brain.player.getAttackStrengthScale(0.5F);
		if (charge >= 0.95F && this.brain.random.nextInt(5) == 0 && this.clearInFront(null)) {
			this.brain.controls.attackOnce();
		}
		return true;
	}

	// ---------------------------------------------------------------- mace practice

	/** Nothing a wind charge would push or flip (doors, buttons, levers, bells, gates) right around the feet. */
	private boolean safeForWindCharge() {
		BlockPos feet = this.brain.player.blockPosition();
		for (BlockPos pos : BlockPos.betweenClosed(feet.offset(-2, -1, -2), feet.offset(2, 2, 2))) {
			var block = this.brain.player.level().getBlockState(pos).getBlock();
			if (block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock
					|| block instanceof ButtonBlock || block instanceof LeverBlock || block instanceof BellBlock) {
				return false;
			}
		}
		AABB around = this.brain.player.getBoundingBox().inflate(5.0);
		// Friends (Harley) don't count: getting pushed by his practice jumps is part of hanging out with him.
		return this.brain.player.level().getEntities(this.brain.player, around, e -> e instanceof LivingEntity
				&& !(CharacterManager.brain(e) instanceof LoafBrain other && this.brain.isFriendCharacter(other))).isEmpty()
				&& this.brain.player.level().noCollision(this.brain.player, this.brain.player.getBoundingBox().expandTowards(0.0, 8.0, 0.0));
	}

	/**
	 * FelixSyn practicing mace jumps: every 20-35 seconds, look down, jump and throw a wind charge at his feet, then
	 * swing on the way down. Shadow-boxing in between. Only out in the open, away from people and doors.
	 */
	boolean practiceMaceJump() {
		if (this.practiceTicks >= 0) {
			this.practiceTicks++;
			int t = this.practiceTicks;
			if (t == 1) {
				int slot = Inv.find(this.brain.player, stack -> stack.is(Items.WIND_CHARGE));
				if (slot < 0) {
					this.practiceTicks = -1;
					return false;
				}
				slot = slot < 9 ? slot : Inv.toHotbar(this.brain.player, slot, 6);
				this.brain.controls.selectSlot(slot);
			}
			if (t <= 8) {
				this.brain.nav.stop();
				this.brain.controls.lookInstantly(this.brain.player.getYRot(), 90.0F);
				if (t == 3 && this.brain.player.onGround()) {
					this.brain.controls.jumpOnce();
				} else if (t == 5 && !this.brain.player.onGround()) {
					this.brain.controls.useOnce();
				}
				return true;
			}
			if (t == 9) {
				int mace = Inv.find(this.brain.player, stack -> stack.is(Items.MACE));
				if (mace >= 0 && mace < 9) {
					this.brain.controls.selectSlot(mace);
				}
			}
			this.brain.controls.look(this.brain.player.getYRot(), 30.0F);
			if (!this.brain.player.onGround() && this.brain.player.getDeltaMovement().y < 0.0 && this.brain.player.fallDistance > 1.5
					&& t % 6 == 0 && this.clearInFront(null)) {
				this.brain.controls.attackOnce();
			}
			if (t > 12 && this.brain.player.onGround() || t > 100) {
				this.practiceTicks = -1;
				this.practiceCooldown = 400 + this.brain.random.nextInt(300);
			}
			return true;
		}
		if (this.onBreak()) {
			return true;
		}
		if (--this.practiceCooldown <= 0 && this.brain.player.onGround() && this.safeForWindCharge()) {
			this.practiceTicks = 0;
			return true;
		}
		return this.train();
	}

	// ---------------------------------------------------------------- patrol

	/** Archers on watch: walk to points around the edge of home, stop and look out. */
	boolean patrol() {
		if (this.onBreak()) {
			return true;
		}
		if (this.patrolPause > 0) {
			this.patrolPause--;
			if (this.patrolPause % 40 == 0) {
				this.brain.controls.look(this.brain.player.getYRot() + (this.brain.random.nextFloat() - 0.5F) * 120.0F, -2.0F);
			}
			return true;
		}
		if (this.patrolPoint == null || this.brain.player.position().distanceTo(this.patrolPoint) < 1.5 || !this.brain.nav.isMoving()) {
			if (this.patrolPoint != null && this.brain.player.position().distanceTo(this.patrolPoint) < 1.5) {
				this.patrolPause = 80 + this.brain.random.nextInt(80);
				this.patrolPoint = null;
				return true;
			}
			double angle = this.brain.random.nextDouble() * Math.PI * 2.0;
			int r = Math.max(4, this.brain.homeRadius - 1);
			BlockPos c = this.center();
			this.patrolPoint = this.brain.findSpotNear(c.offset((int) (Math.cos(angle) * r), 0, (int) (Math.sin(angle) * r)), 1);
			if (this.patrolPoint == null) {
				return false;
			}
			this.brain.nav.moveTo(this.patrolPoint, 1.0, Navigator.Speed.WALK);
		}
		return true;
	}

	// ---------------------------------------------------------------- friends and home

	/**
	 * Visits a friend character nearby: walks over, greets them with two quick crouches, and hangs out for about
	 * 15 seconds looking at them. False if no friend is around (or after a recent visit).
	 */
	boolean visitFriend() {
		if (this.visiting == null) {
			if (this.visitCooldown > 0) {
				this.visitCooldown--;
				return false;
			}
			this.visitCooldown = 200;
			LoafBrain best = null;
			double bestDistance = 40.0;
			for (LoafCharacter other : CharacterManager.active()) {
				LoafBrain o = other.brain;
				if (o == null || o == this.brain || other.player.level() != this.brain.player.level() || o.target != null
						|| other.player.isSleeping() || !this.brain.isFriendCharacter(o)) {
					continue;
				}
				double d = other.player.distanceTo(this.brain.player);
				if (d < bestDistance) {
					bestDistance = d;
					best = o;
				}
			}
			if (best == null) {
				return false;
			}
			this.visiting = best;
			this.visitTicks = 0;
		}
		LoafBrain friend = this.visiting;
		if (!friend.player.isAlive() || friend.player.isRemoved() || friend.player.level() != this.brain.player.level()) {
			this.visiting = null;
			return false;
		}
		double distance = this.brain.player.distanceTo(friend.player);
		this.brain.controls.lookAt(friend.player.getEyePosition());
		if (distance > 2.8) {
			this.brain.nav.follow(friend.player, 2.2, distance > 16.0 ? Navigator.Speed.SPRINT : Navigator.Speed.WALK);
			return true;
		}
		this.brain.nav.stop();
		this.visitTicks++;
		// Greeting: two quick crouches when they arrive.
		this.brain.controls.setSneaking(this.visitTicks < 16 && this.visitTicks % 8 < 4);
		if (this.visitTicks > 300) {
			this.brain.controls.setSneaking(false);
			this.visiting = null;
			this.visitCooldown = 1200;
			return false;
		}
		return true;
	}

	/** Evening: back home and close to the bed. False when already there. */
	boolean stayNearHome() {
		BlockPos spot = this.brain.bed != null ? this.brain.bed : this.brain.home;
		if (spot == null) {
			return false;
		}
		if (this.brain.player.position().distanceTo(Vec3.atBottomCenterOf(spot)) > 4.5) {
			this.brain.nav.moveTo(spot, 2.5, Navigator.Speed.WALK);
			return true;
		}
		return false;
	}
}
