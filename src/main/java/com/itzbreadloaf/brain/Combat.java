package com.itzbreadloaf.brain;

import com.itzbreadloaf.brain.ai.PlayStyle;
import com.itzbreadloaf.brain.ai.SkillProfile;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
 * Player-vs-player fighting with real inputs, done the way a person plays (AI overhaul part 4). How well each
 * character does every part of this comes from its {@link SkillProfile}, and how it likes to fight from its
 * {@link PlayStyle}.
 *
 * <ul>
 * <li><b>Reaction time:</b> a new target is noticed 150-400 ms later (by skill); until then they only turn toward it.</li>
 * <li><b>Aim:</b> mouse-like ({@link Controls#humanAim}): it moves toward the target, overshoots, corrects, lags behind
 *   strafing and wobbles a little. They click when the crosshair is on the target, and now and then too early.</li>
 * <li><b>Hotbar:</b> switching takes a few ticks ({@link Controls#switchSlot}) and resets the attack charge.</li>
 * <li><b>Cooldown:</b> hits go in at 93-100% charge for full damage; jump crits hit on the way down.</li>
 * <li><b>Knockback control:</b> W-taps (let go of forward for 1-3 ticks after a sprint hit, so the next hit knocks back
 *   far again and they don't run into the target), S-taps (a short step back), hit-and-run back-offs, strafing in
 *   circles with direction changes at random moments, and jump resets (a jump on the tick they're hit, to be pushed
 *   back less).</li>
 * <li><b>Shields:</b> raised when a hit is coming (the attacker's charge is nearly full, or a bow is drawn), allowing
 *   for the shield's 5-tick raise; never held up for long; not raised against axes by skilled fighters. A blocking
 *   target gets an axe hit (shield disabled for 5 seconds), then it's back to the main weapon.</li>
 * <li><b>Backing off:</b> low on health they back away (shield up, or sprinting off), put a couple of blocks between
 *   themselves and the attacker if they carry some, and eat a golden apple or food or drink a potion there.</li>
 * <li><b>Panic:</b> very low on health, less skilled fighters sometimes do something erratic.</li>
 * <li><b>Focus:</b> Lily and Madison fight basic and defensive (fewer crits, no taps, more blocking) until angry.</li>
 * </ul>
 *
 * Ranged: bows are drawn for the real 20 ticks and released once the aim has settled, crossbows are charged for
 * their real charge time and then fired, tridents are held 10+ ticks and thrown. Arrows are aimed with gravity and
 * target movement.
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
	/** Where an axe goes for a shield break. */
	public static final int AXE_SLOT = 2;

	private enum Tap { NONE, W_TAP, S_TAP, BACK_OFF }

	private final ServerPlayer player;
	private final Controls controls;
	private final RandomSource random;

	private boolean critJump;
	private int critTimer;
	private int strafeDirection = 1;
	private int strafeTimer;
	private boolean strafing = true;
	private int releaseCooldown;
	private boolean drawing;
	private boolean shieldUp;
	private int shieldTicks;
	private int shieldRest;
	private boolean threatSeen;
	private boolean blockThisThreat;
	private @Nullable ThrownTrident thrownTrident;
	public boolean useCrits = true;
	/** How often a jump crit is attempted when the timing allows (Harley on Hard: every time). */
	public float critChance = 0.5F;
	/** Below this share of max health it backs off (shield up or sprinting away) so it can heal and eat. 0 = never. */
	public float retreatBelow;
	private int retreatTicks;
	private int retreatCooldown;
	private int coverPlaced;
	private int coverTries;
	/** Called when a jump crit swing goes off (ItzBreadLoaf yells). */
	public @org.jetbrains.annotations.Nullable Runnable onCrit;
	public boolean useShield = true;
	public boolean strafe = true;
	/** Chance a melee swing misses on purpose by jerking the mouse (Master BreadLoaf's tuned nerf). */
	public float whiffChance;

	// ---- part 4: skill, style and human timing
	/** The fighter's skill numbers (set by the brain from the config). */
	public SkillProfile skill = SkillProfile.fallback(com.itzbreadloaf.character.CharacterKind.BREADLOAF, "normal");
	/** How they like to fight (set by the brain). */
	public PlayStyle style = PlayStyle.BRAWLER;
	/** Fighting with full skill. Lily and Madison only are once they're angry. */
	public boolean focused = true;
	/** Reaction delay to new targets (Master BreadLoaf has his own). */
	public boolean useReaction = true;
	/** Eats or drinks something to heal while backing off; true if it started (set by the brain). */
	public @Nullable BooleanSupplier healHook;
	/** Places cover blocks while backing off (set by the brain; null = never). */
	public @Nullable Builder builder;
	private @Nullable LivingEntity lastTarget;
	private int reactionTicks;
	private float swingAt = 0.95F;
	private boolean misclickDecided;
	private boolean critDecided;
	private boolean goCrit;
	private boolean misclick;
	private Tap tap = Tap.NONE;
	private int tapTicks;
	private int breakTicks;
	private int returnSlot = -1;

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

	/** Ticks a hotbar switch takes for this fighter. */
	private int switchTicks() {
		return Math.max(0, this.skill.hotbarSwitchTicks);
	}

	/**
	 * Puts the best weapon for this distance in hand: a usable ranged weapon when far (if allowed), otherwise the
	 * hardest-hitting melee weapon. The switch takes the fighter's hotbar time. Returns what it picked.
	 */
	public Weapon equipBest(double distance, boolean allowRanged) {
		if (this.breakTicks > 0) {
			// Mid shield break: the axe stays out.
			return this.held();
		}
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
		double rangedFrom = this.style.ranged ? 5.0 : 6.0;
		boolean ranged = rangedSlot >= 0 && allowRanged && (distance > rangedFrom || meleeSlot < 0);
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
				this.controls.switchSlot(empty, this.switchTicks());
			}
			return Weapon.FISTS;
		}
		int hotbar = Inv.toHotbar(this.player, slot, ranged ? RANGED_SLOT : MELEE_SLOT);
		this.controls.switchSlot(hotbar, this.switchTicks());
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
		// Reaction time: a new target is noticed a moment later. Until then they only turn toward it.
		if (target != this.lastTarget) {
			this.lastTarget = target;
			this.reactionTicks = this.useReaction ? this.skill.reactionTicks(this.random) : 0;
			this.misclickDecided = false;
			this.breakTicks = 0;
		}
		if (this.reactionTicks > 0) {
			this.reactionTicks--;
			this.controls.humanAim(target.getEyePosition(), this.skill.aimSpeedDegrees * 0.6F, this.skill.aimErrorDegrees * 2.0F);
			return;
		}
		if (this.strafeTimer-- <= 0) {
			// Direction changes at unpredictable moments; better fighters change more often.
			int base = 8 + Math.round((1.0F - this.skill.strafeChance) * 25.0F);
			this.strafeTimer = base + this.random.nextInt(base + 10);
			this.strafeDirection = this.random.nextBoolean() ? 1 : -1;
			this.strafing = this.strafe && this.random.nextFloat() < this.skill.strafeChance * (this.focused ? 1.0F : 0.5F);
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
		this.tap = Tap.NONE;
		this.tapTicks = 0;
		this.breakTicks = 0;
	}

	/**
	 * Just got hit by {@code attacker}: a jump reset (jumping on the tick of the hit adds the jump's push against the
	 * knockback, so they get knocked back less). Lands by skill.
	 */
	public void onHurt(@Nullable LivingEntity attacker) {
		if (attacker == null || !this.player.onGround() || this.player.isInWater() || this.player.distanceTo(attacker) > 5.0) {
			return;
		}
		if (this.random.nextFloat() < this.skill.jumpResetChance * (this.focused ? 1.0F : 0.6F)) {
			this.controls.lookAt(attacker.getEyePosition());
			this.controls.jumpOnce();
		}
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

	private @Nullable LivingEntity sightTarget;
	private boolean sightResult;
	private int sightTick = -100;

	/** Line of sight (a ray through the world), remembered for 4 ticks per target since it's checked so often. */
	public boolean canSee(LivingEntity target) {
		int now = this.player.tickCount;
		if (target != this.sightTarget || now - this.sightTick >= 4) {
			this.sightTarget = target;
			this.sightTick = now;
			this.sightResult = this.player.hasLineOfSight(target);
		}
		return this.sightResult;
	}

	/** The crosshair is on the target's hitbox, within reach. */
	private boolean onTarget(LivingEntity target, double reach) {
		Vec3 eyes = this.player.getEyePosition();
		Vec3 end = eyes.add(this.player.getViewVector(1.0F).scale(reach));
		return target.getBoundingBox().inflate(0.1).clip(eyes, end).isPresent();
	}

	private float aimError() {
		return this.skill.aimErrorDegrees * (this.focused ? 1.0F : 1.3F);
	}

	private void melee(LivingEntity target, Navigator nav) {
		Vec3 aim = this.aimPoint(target);
		double distance = this.player.getEyePosition().distanceTo(aim);
		double reach = this.reach() - 0.15;
		float charge = this.player.getAttackStrengthScale(0.5F);
		boolean closeEnough = distance <= reach;
		this.controls.humanAim(aim, this.skill.aimSpeedDegrees, this.aimError());

		// Low on health: back off for a couple of seconds (blocking if there's a shield) to make room to heal.
		if (this.retreatBelow > 0.0F && this.retreatTicks <= 0 && this.retreatCooldown <= 0 && distance < 6.0
				&& this.player.getHealth() < this.player.getMaxHealth() * this.retreatBelow) {
			this.retreatTicks = 50;
			this.retreatCooldown = 300;
			this.coverPlaced = 0;
			this.coverTries = 0;
		}
		if (this.retreatTicks > 0) {
			this.retreat(target, nav, distance);
			return;
		}
		if (this.panic(target, charge)) {
			return;
		}
		boolean breaking = this.tickShieldBreak(target, closeEnough, charge, reach);
		this.move(target, nav, distance);
		if (breaking) {
			this.raiseShield(false);
			return;
		}
		this.tickShield(target, distance, charge);

		// Critical hits: jump when the charge is nearly back, hit on the way down.
		if (this.critJump) {
			this.critTimer++;
			boolean falling = !this.player.onGround() && this.player.getDeltaMovement().y < -0.03 && this.player.fallDistance > 0.0;
			if (falling && closeEnough && charge >= 0.95F && this.trySwing(target, reach)) {
				this.critJump = false;
				if (this.onCrit != null) {
					this.onCrit.run();
				}
			} else if (this.critTimer > 14 && this.player.onGround()) {
				// Mistimed: landed before the hit went in.
				this.critJump = false;
			}
			return;
		}
		// Go for a crit or not: decided once per swing, when the charge is nearly back.
		if (!this.critDecided && charge >= 0.8F && charge < 0.95F) {
			this.critDecided = true;
			this.goCrit = this.random.nextFloat() < this.critChance * (this.focused ? 1.0F : 0.5F);
		}
		if (this.useCrits && this.goCrit && this.player.onGround() && !this.player.isInWater() && closeEnough && charge >= 0.8F
				&& charge < 0.95F && this.tap == Tap.NONE) {
			this.goCrit = false;
			this.critJump = true;
			this.critTimer = 0;
			this.controls.setSprinting(false);
			this.controls.jumpOnce();
			return;
		}
		if (closeEnough && charge >= this.swingAt && this.canSee(target)) {
			this.trySwing(target, reach);
		}
	}

	/**
	 * A swing when the crosshair is on the target. Off target, a fighter waits for the aim to land, except that now
	 * and then (mistake chance, decided once per swing) they click anyway and miss. True if a click went in.
	 */
	private boolean trySwing(LivingEntity target, double reach) {
		if (this.controls.isSwitching()) {
			return false;
		}
		if (!this.onTarget(target, reach)) {
			if (!this.misclickDecided) {
				this.misclickDecided = true;
				this.misclick = this.random.nextFloat() < this.skill.mistakeChance;
			}
			if (!this.misclick) {
				return false;
			}
		}
		this.swing();
		return true;
	}

	private void swing() {
		this.raiseShield(false);
		if (this.whiffChance > 0.0F && this.random.nextFloat() < this.whiffChance) {
			// A human miss: the mouse is a little off when the click goes in.
			float off = (this.random.nextBoolean() ? 1 : -1) * (14.0F + this.random.nextFloat() * 10.0F);
			this.controls.lookInstantly(this.player.getYRot() + off, this.player.getXRot() - 6.0F);
		}
		boolean sprinting = this.player.isSprinting();
		this.controls.attackOnce();
		this.misclickDecided = false;
		this.critDecided = false;
		// Next hit at 93-100% charge: people don't click at exactly the same moment every time.
		this.swingAt = 0.93F + this.random.nextFloat() * 0.07F;
		// After the hit: W-tap, S-tap or a hit-and-run back-off.
		if (!this.focused) {
			return;
		}
		if (this.style == PlayStyle.HIT_AND_RUN && this.random.nextFloat() < 0.6F) {
			this.startTap(Tap.BACK_OFF, 10 + this.random.nextInt(8));
		} else if (sprinting && this.random.nextFloat() < this.skill.wTapChance) {
			this.startTap(Tap.W_TAP, 1 + this.random.nextInt(3));
		} else if (this.random.nextFloat() < this.skill.sTapChance) {
			this.startTap(Tap.S_TAP, 2 + this.random.nextInt(3));
		}
	}

	private void startTap(Tap tap, int ticks) {
		this.tap = tap;
		this.tapTicks = ticks;
	}

	/** Moving in a fight: run in, keep at the style's distance, circle, and do any W-tap, S-tap or back-off. */
	private void move(LivingEntity target, Navigator nav, double distance) {
		Vec3 toTarget = target.position().subtract(this.player.position()).multiply(1.0, 0.0, 1.0);
		Vec3 forwardDir = toTarget.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : toTarget.normalize();
		Vec3 sideDir = new Vec3(-forwardDir.z, 0.0, forwardDir.x);
		if (this.tapTicks > 0) {
			this.tapTicks--;
			nav.stop();
			switch (this.tap) {
				case W_TAP -> {
					// Let go of W: no forward input, the sprint ends and starts again on the next hit.
					if (this.strafing) {
						this.controls.walk(sideDir.scale(this.strafeDirection), 0.5F);
					}
					this.controls.setSprinting(false);
				}
				case S_TAP -> {
					this.controls.walk(forwardDir.scale(-1.0), 1.0F);
					this.controls.setSprinting(false);
				}
				case BACK_OFF -> {
					this.controls.walk(forwardDir.scale(-1.0).add(sideDir.scale(this.strafeDirection * 0.6)), 1.0F);
					this.controls.setSprinting(false);
				}
				default -> {
				}
			}
			if (this.tapTicks == 0) {
				this.tap = Tap.NONE;
			}
			return;
		}
		double preferred = Mth.clamp(this.style.ranged ? 2.6 : this.style.preferredRange, 2.2, 3.4);
		if (distance > preferred + 0.7 || !this.canSee(target)) {
			nav.follow(target, Math.max(1.5, preferred - 0.6), Navigator.Speed.SPRINT);
			return;
		}
		nav.stop();
		Vec3 side = sideDir.scale(this.strafing ? this.strafeDirection * 0.6 : 0.0);
		Vec3 forward = forwardDir.scale(distance > preferred ? 1.0 : distance < preferred - 1.0 ? -0.6 : 0.2);
		this.controls.walk(forward.add(side));
		this.controls.setSprinting(distance > 2.0 && !this.critJump);
	}

	// ---------------------------------------------------------------- backing off, panic

	/**
	 * Backing off to heal: walk backwards behind the shield (or sprint away without one), put up to two blocks between
	 * them and the attacker if they carry building blocks, then eat or drink once there's a little room.
	 */
	private void retreat(LivingEntity target, Navigator nav, double distance) {
		this.retreatTicks--;
		this.critJump = false;
		this.tap = Tap.NONE;
		nav.stop();
		Vec3 away = this.player.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
		away = away.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
		Vec3 side = new Vec3(-away.z, 0.0, away.x).scale(this.strafeDirection * 0.4);
		if (distance > 3.5 && this.coverPlaced < 2 && this.builder != null && this.builder.hasBlocks()
				&& Builder.allowed(this.player.level()) && this.placeCover(target)) {
			return;
		}
		if (distance > 4.5 && this.healHook != null && this.retreatTicks % 5 == 0 && this.healHook.getAsBoolean()) {
			this.retreatTicks = 0;
			return;
		}
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
	}

	/** A block at foot height between them and the attacker, then one on top. True while placing. */
	private boolean placeCover(LivingEntity target) {
		Vec3 toward = target.position().subtract(this.player.position());
		Direction direction = Direction.getApproximateNearest(toward.x, 0.0, toward.z);
		BlockPos wall = this.player.blockPosition().relative(direction).above(this.coverPlaced);
		if (!this.player.level().getBlockState(wall).canBeReplaced()) {
			this.coverPlaced++;
			return false;
		}
		// Can't place it (nothing solid to click on): give up on cover after a few tries.
		if (++this.coverTries > 12 || this.builder == null || !this.builder.holdBlocks()) {
			this.coverPlaced = 2;
			return false;
		}
		this.raiseShield(false);
		if (this.builder.place(wall)) {
			this.coverPlaced++;
		}
		return true;
	}

	/**
	 * Very low on health, less skilled fighters sometimes lose their cool for a moment: a random jump, a sudden change
	 * of direction, a click at half charge, or backing off early. Returns false: the fight goes on after it.
	 */
	private boolean panic(LivingEntity target, float charge) {
		if (this.player.getHealth() >= this.player.getMaxHealth() * this.skill.panicHealth) {
			return false;
		}
		float chance = 0.04F * (1.0F - this.skill.shieldSkill * 0.5F) * (this.focused ? 1.0F : 1.5F);
		if (this.random.nextFloat() >= chance) {
			return false;
		}
		switch (this.random.nextInt(4)) {
			case 0 -> this.controls.jumpOnce();
			case 1 -> this.strafeDirection = -this.strafeDirection;
			case 2 -> {
				if (charge > 0.4F) {
					this.controls.attackOnce();
				}
			}
			default -> {
				if (this.retreatCooldown <= 0) {
					this.retreatTicks = 30;
					this.retreatCooldown = 200;
					this.coverPlaced = 0;
				}
			}
		}
		return false;
	}

	/** Backing off to heal right now. */
	public boolean isRetreating() {
		return this.retreatTicks > 0;
	}

	// ---------------------------------------------------------------- shields

	public boolean hasUsableShield() {
		ItemStack offhand = this.player.getOffhandItem();
		return offhand.has(DataComponents.BLOCKS_ATTACKS) && !this.player.getCooldowns().isOnCooldown(offhand)
				&& !this.player.getMainHandItem().has(DataComponents.FOOD);
	}

	public void raiseShield(boolean up) {
		if (up != this.shieldUp) {
			this.shieldUp = up;
			this.shieldTicks = 0;
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

	/**
	 * A melee hit is about to come: a player (or character) facing us whose attack charge is nearly full, close
	 * enough to reach within the shield's 5-tick raise. Mobs: when they're close and our own swing isn't ready.
	 */
	private boolean hitComing(LivingEntity target, double distance, float ourCharge) {
		if (distance > 4.5) {
			return false;
		}
		if (target instanceof Player p) {
			Vec3 toUs = this.player.getEyePosition().subtract(p.getEyePosition()).normalize();
			boolean facing = p.getViewVector(1.0F).dot(toUs) > 0.65;
			return facing && p.getAttackStrengthScale(5.0F) >= 0.9F;
		}
		return distance < 3.5 && ourCharge < 0.8F;
	}

	/**
	 * Shields up when a hit is coming (timed by skill), down to attack, never held more than 1.5 seconds against
	 * melee, and skilled fighters don't block an axe (it would disable the shield).
	 */
	private void tickShield(LivingEntity target, double distance, float charge) {
		boolean ready = this.useShield && this.hasUsableShield() && !this.critJump && this.tap == Tap.NONE;
		if (!ready) {
			this.raiseShield(false);
			return;
		}
		if (this.shieldRest > 0) {
			this.shieldRest--;
			this.raiseShield(false);
			return;
		}
		boolean bow = this.targetIsAiming(target);
		boolean axe = target.getMainHandItem().is(ItemTags.AXES) && this.skill.shieldSkill >= 0.5F;
		boolean threat = bow || !axe && this.hitComing(target, distance, charge);
		if (threat && !this.threatSeen) {
			// Decided once per incoming hit: good fighters block it, others sometimes don't (or block too late).
			this.threatSeen = true;
			float block = (0.35F + 0.65F * this.skill.shieldSkill) * (0.6F + this.style.shieldBias) * (this.focused ? 1.0F : 1.2F);
			this.blockThisThreat = this.random.nextFloat() < block;
		} else if (!threat) {
			this.threatSeen = false;
		}
		boolean up = threat && this.blockThisThreat && charge < this.swingAt;
		// Less skilled fighters are slow to lower it again.
		if (!up && this.shieldUp && this.shieldTicks < 6 && this.random.nextFloat() < (1.0F - this.skill.shieldSkill) * 0.3F) {
			up = true;
		}
		if (this.shieldUp) {
			this.shieldTicks++;
			if (this.shieldTicks > 30 && !bow) {
				up = false;
				this.shieldRest = 8 + this.random.nextInt(8);
			}
		}
		this.raiseShield(up);
	}

	/**
	 * A target blocking with a shield: switch to an axe (if they carry one), hit the shield to disable it, then
	 * switch back to the main weapon. True while busy with that (the normal swing waits).
	 */
	private boolean tickShieldBreak(LivingEntity target, boolean closeEnough, float charge, double reach) {
		boolean holdingAxe = this.player.getMainHandItem().is(ItemTags.AXES);
		if (this.breakTicks > 0) {
			this.breakTicks--;
			if (!target.isBlocking() || this.breakTicks == 0) {
				// Shield's down (or this took too long): back to the main weapon.
				if (this.returnSlot >= 0) {
					this.controls.switchSlot(this.returnSlot, this.switchTicks());
				}
				this.breakTicks = 0;
				this.returnSlot = -1;
				return false;
			}
			if (holdingAxe && !this.controls.isSwitching() && closeEnough && charge >= 0.9F && this.onTarget(target, reach)) {
				this.controls.attackOnce();
			}
			return true;
		}
		if (!target.isBlocking() || !closeEnough || holdingAxe || !this.focused || this.random.nextFloat() >= this.skill.shieldSkill * 0.2F) {
			return false;
		}
		int axe = Inv.find(this.player, stack -> stack.is(ItemTags.AXES));
		if (axe < 0) {
			return false;
		}
		this.returnSlot = this.player.getInventory().getSelectedSlot();
		int hotbar = Inv.toHotbar(this.player, axe, this.returnSlot == AXE_SLOT ? AXE_SLOT + 1 : AXE_SLOT);
		this.controls.switchSlot(hotbar, this.switchTicks());
		this.breakTicks = 60;
		return true;
	}

	// ---------------------------------------------------------------- ranged

	private void keepRange(LivingEntity target, Navigator nav, double min, double max) {
		double distance = this.player.distanceTo(target);
		if (distance > max || !this.canSee(target)) {
			nav.follow(target, max - 2.0, Navigator.Speed.WALK);
		} else {
			nav.stop();
			Vec3 away = this.player.position().subtract(target.position()).multiply(1.0, 0.0, 1.0).normalize();
			Vec3 side = new Vec3(-away.z, 0.0, away.x).scale(this.strafing ? this.strafeDirection * 0.5 : 0.0);
			this.controls.walk(distance < min ? away.add(side) : side, distance < min ? 1.0F : 0.5F);
		}
	}

	/** The aim has settled close enough to the wanted direction to let a shot go. */
	private boolean aimSettled() {
		return this.controls.aimOffset() < 2.5F + this.aimError();
	}

	private void bow(LivingEntity target, Navigator nav) {
		this.keepRange(target, nav, 7.0, 16.0);
		this.raiseShieldOff();
		boolean visible = this.canSee(target);
		this.aimProjectileHuman(target, 3.0, 0.05);
		if (!this.drawing) {
			if (this.releaseCooldown <= 0 && visible && this.hasAmmo() && !this.controls.isSwitching()) {
				this.drawing = true;
				this.controls.holdUse(true);
			}
			return;
		}
		// Full draw is 20 ticks (as fast as a real player's full charge); let go once the aim has settled.
		if (this.player.isUsingItem() && this.player.getTicksUsingItem() >= 20 && visible && (this.aimSettled() || this.player.getTicksUsingItem() > 40)) {
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
			this.controls.humanAim(target.getEyePosition(), this.skill.aimSpeedDegrees, this.aimError());
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
		this.aimProjectileHuman(target, 3.15, 0.05);
		if (this.releaseCooldown <= 0 && this.canSee(target) && this.aimSettled()) {
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
		this.aimProjectileHuman(target, 2.5, 0.05);
		if (!this.drawing) {
			if (this.releaseCooldown <= 0 && this.canSee(target)) {
				this.drawing = true;
				this.controls.holdUse(true);
			}
		} else if (this.player.isUsingItem() && this.player.getTicksUsingItem() >= 12 && this.aimSettled()) {
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
	 * Yaw and pitch for a projectile with speed {@code v} (blocks/tick) and gravity {@code g}: leads a moving target
	 * and tilts up for the drop (low-arc solution of the projectile equation).
	 */
	private float[] projectileAngles(LivingEntity target, double v, double g) {
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
		return new float[]{(float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F, (float) (-angle * Mth.RAD_TO_DEG)};
	}

	/** Aims a projectile exactly, this tick (pearls and other scripted throws). */
	public void aimProjectile(LivingEntity target, double v, double g) {
		float[] angles = this.projectileAngles(target, v, g);
		this.controls.lookInstantly(angles[0], angles[1]);
	}

	/** Aims a projectile with the fighter's mouse-like aim (bows, crossbows, tridents in a fight). */
	private void aimProjectileHuman(LivingEntity target, double v, double g) {
		float[] angles = this.projectileAngles(target, v, g);
		this.controls.humanLook(angles[0], angles[1], this.skill.aimSpeedDegrees, this.aimError() * 0.6F);
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
