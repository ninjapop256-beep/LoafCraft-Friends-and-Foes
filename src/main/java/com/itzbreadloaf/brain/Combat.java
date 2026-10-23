package com.itzbreadloaf.brain;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Player-vs-player fighting with real inputs.
 *
 * Melee: waits for the real attack cooldown ({@code getAttackStrengthScale}), aims at the closest point of the
 * target's hitbox, hits while sprinting for extra knockback, and jumps for critical hits (attacking while falling,
 * not sprinting). Raises a shield (held right-click with a shield in the offhand) between hits.
 * Ranged: bows are drawn for the real 20 ticks and released, crossbows are charged for their real charge time and
 * then fired, tridents are held 10+ ticks and thrown. Arrows are aimed with gravity and target movement.
 */
public final class Combat {
	public enum Weapon {
		FISTS, MELEE, SPEAR, MACE, BOW, CROSSBOW, TRIDENT;

		public boolean isRanged() {
			return this == BOW || this == CROSSBOW || this == TRIDENT;
		}
	}

	public static final int MELEE_SLOT = 0;
	public static final int RANGED_SLOT = 1;

	private final ServerPlayer player;
	private final Controls controls;
	private final RandomSource random;

	private boolean critJump;
	private int critTimer;
	private int strafeDirection = 1;
	private int strafeTimer;
	private int releaseCooldown;
	private boolean drawing;
	private boolean shieldUp;
	private @Nullable ThrownTrident thrownTrident;
	public boolean useCrits = true;
	/** How often a jump crit is attempted when the timing allows (Harley on Hard: every time). */
	public float critChance = 0.5F;
	/** Below this share of max health it backs off (shield up or sprinting away) so it can heal and eat. 0 = never. */
	public float retreatBelow;
	private int retreatTicks;
	private int retreatCooldown;
	/** Called when a jump crit swing goes off (ItzBreadLoaf yells). */
	public @org.jetbrains.annotations.Nullable Runnable onCrit;
	public boolean useShield = true;
	public boolean strafe = true;

	public Combat(ServerPlayer player, Controls controls, RandomSource random) {
		this.player = player;
		this.controls = controls;
		this.random = random;
	}

	// ---------------------------------------------------------------- weapons

	public static Weapon kindOf(ItemStack stack) {
		if (stack.isEmpty()) {
			return Weapon.FISTS;
		}
		if (stack.is(Items.BOW)) {
			return Weapon.BOW;
		}
		if (stack.is(Items.CROSSBOW)) {
			return Weapon.CROSSBOW;
		}
		if (stack.is(Items.TRIDENT)) {
			return Weapon.TRIDENT;
		}
		if (stack.is(Items.MACE)) {
			return Weapon.MACE;
		}
		if (stack.is(ItemTags.SPEARS) || stack.has(DataComponents.KINETIC_WEAPON)) {
			return Weapon.SPEAR;
		}
		return Inv.attackDamage(stack) > 0.0 ? Weapon.MELEE : Weapon.FISTS;
	}

	public boolean hasAmmo() {
		return Inv.count(this.player, stack -> stack.is(ItemTags.ARROWS)) > 0 || this.player.isCreative();
	}

	private boolean usable(ItemStack stack) {
		Weapon weapon = kindOf(stack);
		if (weapon == Weapon.BOW) {
			return this.hasAmmo();
		}
		if (weapon == Weapon.CROSSBOW) {
			return CrossbowItem.isCharged(stack) || this.hasAmmo();
		}
		return weapon != Weapon.FISTS;
	}

	/**
	 * Puts the best weapon for this distance in hand: a usable ranged weapon when far (if allowed),
	 * otherwise the hardest-hitting melee weapon. Returns what it picked.
	 */
	public Weapon equipBest(double distance, boolean allowRanged) {
		int rangedSlot = -1;
		int meleeSlot = -1;
		double bestDamage = 0.0;
		for (int i = 0; i < Inv.MAIN; i++) {
			ItemStack stack = this.player.getInventory().getItem(i);
			Weapon weapon = kindOf(stack);
			if (weapon.isRanged()) {
				if (rangedSlot < 0 && this.usable(stack)) {
					rangedSlot = i;
				}
			} else if (weapon != Weapon.FISTS) {
				double damage = Inv.attackDamage(stack) + (weapon == Weapon.MACE ? 1.0 : 0.0);
				if (damage > bestDamage) {
					bestDamage = damage;
					meleeSlot = i;
				}
			}
		}
		boolean ranged = rangedSlot >= 0 && allowRanged && (distance > 6.0 || meleeSlot < 0);
		int slot = ranged ? rangedSlot : meleeSlot;
		if (slot < 0) {
			// Fists: hold nothing (or anything that isn't a weapon).
			int empty = -1;
			for (int i = 0; i < Inv.HOTBAR; i++) {
				if (this.player.getInventory().getItem(i).isEmpty()) {
					empty = i;
					break;
				}
			}
			if (empty >= 0) {
				this.controls.selectSlot(empty);
			}
			return Weapon.FISTS;
		}
		int hotbar = Inv.toHotbar(this.player, slot, ranged ? RANGED_SLOT : MELEE_SLOT);
		this.controls.selectSlot(hotbar);
		return kindOf(this.player.getInventory().getItem(hotbar));
	}

	public Weapon held() {
		return kindOf(this.player.getMainHandItem());
	}

	// ---------------------------------------------------------------- fighting

	/** One tick of fighting {@code target} with the weapon in hand, including moving toward or around it. */
	public void fight(LivingEntity target, Navigator nav) {
		if (this.releaseCooldown > 0) {
			this.releaseCooldown--;
		}
		if (this.retreatCooldown > 0) {
			this.retreatCooldown--;
		}
		if (this.strafeTimer-- <= 0) {
			this.strafeTimer = 20 + this.random.nextInt(40);
			this.strafeDirection = this.random.nextBoolean() ? 1 : -1;
		}
		Weapon weapon = this.held();
		switch (weapon) {
			case BOW -> this.bow(target, nav);
			case CROSSBOW -> this.crossbow(target, nav);
			case TRIDENT -> this.trident(target, nav);
			default -> this.melee(target, nav);
		}
	}

	/** Stops drawing, blocking and swinging (switching to something else). */
	public void stop() {
		if (this.drawing || this.shieldUp) {
			this.controls.holdUse(false);
		}
		this.drawing = false;
		this.shieldUp = false;
		this.critJump = false;
	}

	/** The point of the target's hitbox closest to our eyes (what a player aims at). */
	public Vec3 aimPoint(LivingEntity target) {
		Vec3 eyes = this.player.getEyePosition();
		AABB box = target.getBoundingBox().deflate(0.1);
		return new Vec3(Mth.clamp(eyes.x, box.minX, box.maxX), Mth.clamp(eyes.y, box.minY, box.maxY), Mth.clamp(eyes.z, box.minZ, box.maxZ));
	}

	public double reach() {
		return this.player.entityInteractionRange();
	}

	public boolean canSee(LivingEntity target) {
		return this.player.hasLineOfSight(target);
	}

	private void melee(LivingEntity target, Navigator nav) {
		Vec3 aim = this.aimPoint(target);
		double distance = this.player.getEyePosition().distanceTo(aim);
		double reach = this.reach() - 0.15;
		float cooldown = this.player.getAttackStrengthScale(0.5F);
		boolean closeEnough = distance <= reach;

		if (distance < 7.0) {
			this.controls.aimAt(aim);
		} else {
			this.controls.lookAt(aim);
		}

		// Low on health: back off for a couple of seconds (blocking if there's a shield) to make room to heal.
		if (this.retreatBelow > 0.0F && this.retreatTicks <= 0 && this.retreatCooldown <= 0 && distance < 6.0
				&& this.player.getHealth() < this.player.getMaxHealth() * this.retreatBelow) {
			this.retreatTicks = 50;
			this.retreatCooldown = 300;
		}
		if (this.retreatTicks > 0) {
			this.retreatTicks--;
			this.critJump = false;
			nav.stop();
			Vec3 away = this.player.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
			away = away.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
			Vec3 side = new Vec3(-away.z, 0.0, away.x).scale(this.strafeDirection * 0.4);
			if (this.useShield && this.hasUsableShield()) {
				// Walk backwards behind the shield, still facing them.
				this.raiseShield(true);
				this.controls.walk(away.add(side));
				this.controls.setSprinting(false);
			} else {
				// No shield: turn and sprint away.
				this.raiseShield(false);
				this.controls.lookAt(this.player.getEyePosition().add(away.scale(5.0)));
				this.controls.walk(away.add(side));
				this.controls.setSprinting(true);
			}
			return;
		}

		// Movement: run in, then keep at about 2.5 blocks and circle a bit like a PvP player.
		if (distance > 3.2 || !this.canSee(target)) {
			nav.follow(target, 2.0, Navigator.Speed.SPRINT);
		} else {
			nav.stop();
			Vec3 toTarget = target.position().subtract(this.player.position()).multiply(1.0, 0.0, 1.0);
			Vec3 side = new Vec3(-toTarget.z, 0.0, toTarget.x).normalize().scale(this.strafe ? this.strafeDirection * 0.6 : 0.0);
			Vec3 forward = toTarget.normalize().scale(distance > 2.6 ? 1.0 : distance < 1.6 ? -0.6 : 0.2);
			this.controls.walk(forward.add(side));
			this.controls.setSprinting(distance > 2.0 && !this.critJump);
		}

		// Shield up between hits, down to attack.
		boolean shieldReady = this.useShield && this.hasUsableShield() && !this.critJump;
		boolean threatened = distance < 4.5 && cooldown < 0.8 || this.targetIsAiming(target);
		if (shieldReady && threatened && cooldown < 0.9F) {
			this.raiseShield(true);
		} else {
			this.raiseShield(false);
		}

		// Critical hits: jump when the cooldown is nearly back, hit on the way down.
		if (this.critJump) {
			this.critTimer++;
			boolean falling = !this.player.onGround() && this.player.getDeltaMovement().y < -0.03 && this.player.fallDistance > 0.0;
			if (falling && closeEnough && cooldown >= 0.95F) {
				this.controls.setSprinting(false);
				this.swing();
				this.critJump = false;
				if (this.onCrit != null) {
					this.onCrit.run();
				}
			} else if (this.critTimer > 14 && this.player.onGround()) {
				this.critJump = false;
			}
			return;
		}
		if (this.useCrits && this.player.onGround() && !this.player.isInWater() && closeEnough && cooldown >= 0.8F
				&& cooldown < 0.95F && this.random.nextFloat() < this.critChance) {
			this.critJump = true;
			this.critTimer = 0;
			this.controls.setSprinting(false);
			this.controls.jumpOnce();
			return;
		}
		if (closeEnough && cooldown >= 0.95F && this.canSee(target)) {
			this.swing();
		}
	}


	private void swing() {
		this.raiseShield(false);
		this.controls.attackOnce();
	}

	/** Backing off to heal right now. */
	public boolean isRetreating() {
		return this.retreatTicks > 0;
	}

	public boolean hasUsableShield() {
		ItemStack offhand = this.player.getOffhandItem();
		return offhand.has(DataComponents.BLOCKS_ATTACKS) && !this.player.getCooldowns().isOnCooldown(offhand)
				&& !this.player.getMainHandItem().has(DataComponents.FOOD);
	}

	public void raiseShield(boolean up) {
		if (up != this.shieldUp) {
			this.shieldUp = up;
			this.controls.holdUse(up);
		}
	}

	public boolean isShieldUp() {
		return this.shieldUp;
	}

	/** Skeletons and players drawing a bow or crossbow at us: worth blocking. */
	private boolean targetIsAiming(LivingEntity target) {
		ItemStack using = target.getUseItem();
		return target.isUsingItem() && (using.is(Items.BOW) || using.is(Items.CROSSBOW) || using.is(Items.TRIDENT));
	}

	// ---------------------------------------------------------------- ranged

	private void keepRange(LivingEntity target, Navigator nav, double min, double max) {
		double distance = this.player.distanceTo(target);
		if (distance > max || !this.canSee(target)) {
			nav.follow(target, max - 2.0, Navigator.Speed.WALK);
		} else {
			nav.stop();
			Vec3 away = this.player.position().subtract(target.position()).multiply(1.0, 0.0, 1.0).normalize();
			Vec3 side = new Vec3(-away.z, 0.0, away.x).scale(this.strafeDirection * 0.5);
			this.controls.walk(distance < min ? away.add(side) : side, distance < min ? 1.0F : 0.5F);
		}
	}

	private void bow(LivingEntity target, Navigator nav) {
		this.keepRange(target, nav, 7.0, 16.0);
		this.raiseShieldOff();
		boolean visible = this.canSee(target);
		this.aimProjectile(target, 3.0, 0.05);
		if (!this.drawing) {
			if (this.releaseCooldown <= 0 && visible && this.hasAmmo()) {
				this.drawing = true;
				this.controls.holdUse(true);
			}
			return;
		}
		// Full draw is 20 ticks (as fast as a real player's full charge).
		if (this.player.isUsingItem() && this.player.getTicksUsingItem() >= 20 && visible) {
			this.drawing = false;
			this.controls.holdUse(false);
			this.releaseCooldown = 6 + this.random.nextInt(8);
		} else if (!this.player.isUsingItem() && this.player.getTicksUsingItem() == 0 && !this.controls.isHoldingUse()) {
			this.drawing = false;
		}
	}

	private void crossbow(LivingEntity target, Navigator nav) {
		this.keepRange(target, nav, 6.0, 14.0);
		this.raiseShieldOff();
		ItemStack crossbow = this.player.getMainHandItem();
		if (!CrossbowItem.isCharged(crossbow)) {
			if (!this.hasAmmo()) {
				return;
			}
			this.controls.lookAt(target.getEyePosition());
			if (!this.drawing) {
				this.drawing = true;
				this.controls.holdUse(true);
			} else if (this.player.isUsingItem() && this.player.getTicksUsingItem() >= CrossbowItem.getChargeDuration(crossbow, this.player) + 1) {
				// Let go once fully charged: the crossbow is loaded.
				this.drawing = false;
				this.controls.holdUse(false);
				this.releaseCooldown = 4;
			}
			return;
		}
		this.aimProjectile(target, 3.15, 0.05);
		if (this.releaseCooldown <= 0 && this.canSee(target)) {
			this.controls.useOnce();
			this.releaseCooldown = 10;
		}
	}

	private void trident(LivingEntity target, Navigator nav) {
		double distance = this.player.distanceTo(target);
		if (distance < 3.5) {
			// Up close a trident is a good melee weapon too.
			this.melee(target, nav);
			return;
		}
		this.keepRange(target, nav, 5.0, 14.0);
		this.aimProjectile(target, 2.5, 0.05);
		if (!this.drawing) {
			if (this.releaseCooldown <= 0 && this.canSee(target)) {
				this.drawing = true;
				this.controls.holdUse(true);
			}
		} else if (this.player.isUsingItem() && this.player.getTicksUsingItem() >= 12) {
			this.drawing = false;
			this.controls.holdUse(false);
			this.releaseCooldown = 20;
		}
	}

	private void raiseShieldOff() {
		if (this.shieldUp) {
			this.shieldUp = false;
			this.controls.holdUse(false);
		}
	}

	/**
	 * Aims a projectile with speed {@code v} (blocks/tick) and gravity {@code g}: leads a moving target and
	 * tilts up for the drop (low-arc solution of the projectile equation).
	 */
	public void aimProjectile(LivingEntity target, double v, double g) {
		Vec3 eyes = this.player.getEyePosition();
		Vec3 aim = target.position().add(0.0, target.getBbHeight() * 0.6, 0.0);
		double flightTicks = eyes.distanceTo(aim) / v;
		Vec3 velocity = target.getDeltaMovement();
		aim = aim.add(velocity.x * flightTicks, 0.0, velocity.z * flightTicks);
		double dx = aim.x - eyes.x;
		double dz = aim.z - eyes.z;
		double dy = aim.y - eyes.y;
		double d = Math.sqrt(dx * dx + dz * dz);
		double v2 = v * v;
		double disc = v2 * v2 - g * (g * d * d + 2.0 * dy * v2);
		double angle = disc < 0.0 ? Math.PI / 4.0 : Math.atan2(v2 - Math.sqrt(disc), g * d);
		float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		float pitch = (float) (-angle * Mth.RAD_TO_DEG);
		this.controls.lookInstantly(yaw, pitch);
	}

	// ---------------------------------------------------------------- trident pickup

	/** A trident we threw that is lying somewhere, to walk over and pick up. */
	public @Nullable ThrownTrident findThrownTrident() {
		if (this.thrownTrident != null && (this.thrownTrident.isRemoved() || this.thrownTrident.getOwner() != this.player)) {
			this.thrownTrident = null;
		}
		if (this.thrownTrident == null) {
			List<ThrownTrident> tridents = this.player.level().getEntitiesOfClass(ThrownTrident.class,
					this.player.getBoundingBox().inflate(48.0), trident -> trident.getOwner() == this.player);
			this.thrownTrident = tridents.isEmpty() ? null : tridents.get(0);
		}
		return this.thrownTrident;
	}

	public boolean hasClearShot(Vec3 from, Vec3 to) {
		return this.player.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.player))
				.getType() == HitResult.Type.MISS;
	}
}
