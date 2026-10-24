package com.itzbreadloaf.brain;

import com.itzbreadloaf.brain.ai.PlayStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The mace moves (AI overhaul part 5), shared by FelixSyn, Harley and Master BreadLoaf, with real inputs only.
 * Mechanics checked against Minecraft 1.21.11 (AI_DESIGN.md section 2):
 *
 * <ul>
 * <li><b>Wind jump:</b> look down, jump, throw a wind charge at your feet (launches you up ~6-8 blocks), mace out,
 *   drift onto the target and smash on the way down (a smash needs more than 1.5 blocks of fall and not gliding;
 *   the fall bonus isn't reduced by the attack charge, so switching to the mace mid-air is fine).</li>
 * <li><b>Wind chain:</b> a wind jump, then after each smash a wind charge thrown straight down at the target bursts
 *   on them and launches you up again (a Wind Burst mace bounces you by itself). Each new fall starts at 0 (a smash
 *   resets fall distance).</li>
 * <li><b>Pearl catch:</b> a wind jump, then at the top of it a wind charge and a pearl thrown down the same line one
 *   tick apart, so the pearl meets the wind charge in the air: you teleport there (a pearl teleport keeps your
 *   momentum, resets fall distance to 0 and costs 5 damage) and the burst launches you again, from mid-air.
 *   <b>[check]</b> Vanilla makes this inconsistent (the wind charge doesn't always count as having left you, so it
 *   may not burst on you). When the catch doesn't happen they simply fall and smash from the first jump.</li>
 * <li><b>Diagonal pearl:</b> from 8-18 blocks away, a pearl thrown in an arc to land beside the target (2-3 blocks to
 *   one side), then a wind jump from there: they come down on the target from the side.</li>
 * <li><b>Elytra mace:</b> swap the chestplate for the elytra (right-click it from the hotbar), take off, climb on
 *   firework rockets to ~16 blocks over the target, glide over them, swap the chestplate back on (the glide stops;
 *   gliding slowly keeps fall distance at 1, so the fall only counts from here), mace out, smash.</li>
 * </ul>
 *
 * The mace is picked for the moment: Breach against heavy armor, Density for big drops, Wind Burst for chains.
 * Moves fail now and then by skill (a pearl thrown off line, a dive that comes down next to the target). Each user
 * sets {@link Limits}: how much fall a smash may count, how many hits a chain may have, and (Master BreadLoaf) a
 * warning wind-up before the move.
 */
public final class MaceTechniques {
	public enum Technique {
		WIND_JUMP, WIND_CHAIN, PEARL_CATCH, DIAGONAL_PEARL, ELYTRA_MACE;

		public String id() {
			return this.name().toLowerCase(Locale.ROOT);
		}

		/** By id, accepting the test command's names ("elytra_mace" and "wind_chain" included). */
		public static @Nullable Technique byId(String id) {
			for (Technique t : values()) {
				if (t.id().equalsIgnoreCase(id)) {
					return t;
				}
			}
			return null;
		}
	}

	/** What a user allows. */
	public static final class Limits {
		/** Largest fall a smash may count (mace damage grows with the fall). */
		public float maxFall = 6.0F;
		/** Most smashes per move (Wind Burst bounces and wind-charge relaunches included). */
		public int maxChain = 3;
		/** Ticks of warning before the move starts (and before a dive comes down). 0 = none. */
		public int warnTicks;
		/** Shows the warning (true = with sound). */
		public @Nullable Consumer<Boolean> warn;
		/** Chance the move goes wrong on purpose (by skill). */
		public float failChance = 0.15F;
	}

	private enum Phase { WARN, PEARL_IN, LAUNCH, RISE, CATCH, AIR, RELAUNCH, TAKEOFF, CLIMB, CRUISE, OVER, DROP, ARMOR }

	public static final int MACE_SLOT = 2;
	public static final int WIND_SLOT = 4;
	public static final int PEARL_SLOT = 5;
	public static final int ROCKET_SLOT = 7;
	public static final int ELYTRA_SLOT = 8;

	private final ServerPlayer player;
	private final Controls controls;
	private final Combat combat;
	private final RandomSource random;
	public Limits limits = new Limits();

	private @Nullable Technique technique;
	private Phase phase = Phase.WARN;
	private int phaseTicks;
	private int totalTicks;
	private int cooldown;
	private int chainHits;
	private boolean failed;
	private Vec3 missOffset = Vec3.ZERO;
	private int sideSign = 1;
	private boolean warnedBounce;
	private boolean wasRising;
	private @Nullable Vec3 lastPos;
	private double lastDy;
	private boolean caught;
	private int lastRocketTick = -100;
	private double diveTopY;
	private int tidyTicks;

	public MaceTechniques(ServerPlayer player, Controls controls, Combat combat, RandomSource random) {
		this.player = player;
		this.controls = controls;
		this.combat = combat;
		this.random = random;
	}

	public boolean isActive() {
		return this.technique != null;
	}

	public @Nullable Technique current() {
		return this.technique;
	}

	public boolean isReady() {
		return this.technique == null && this.cooldown <= 0;
	}

	public void tickCooldown() {
		if (this.cooldown > 0) {
			this.cooldown--;
		}
	}

	/**
	 * No fight going on: drop any move. Called every idle tick, so it also puts the chestplate back on (right-click
	 * from the hotbar) if a move ended with the elytra still worn.
	 */
	public void cancel() {
		this.technique = null;
		if (this.player.onGround() && this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
			int chestplate = Inv.ready(this.player, MaceTechniques::isChestplate, ELYTRA_SLOT);
			if (chestplate >= 0 && ++this.tidyTicks % 3 == 1) {
				this.controls.selectSlot(chestplate);
				this.controls.look(this.player.getYRot(), -60.0F);
				this.controls.useOnce();
			}
		}
	}

	// ---------------------------------------------------------------- what can be done

	private int count(net.minecraft.world.item.Item item) {
		return Inv.count(this.player, stack -> stack.is(item));
	}

	private boolean roomAbove(int blocks) {
		AABB above = this.player.getBoundingBox().move(0.0, 1.0, 0.0).expandTowards(0.0, blocks, 0.0);
		return this.player.level().noCollision(this.player, above);
	}

	/** Open sky over the target (nothing to crash into on a dive). */
	private boolean skyOver(LivingEntity target) {
		int top = this.player.level().getHeight(Heightmap.Types.MOTION_BLOCKING, target.getBlockX(), target.getBlockZ());
		return top <= target.getBlockY() + 1;
	}

	private static boolean isChestplate(ItemStack stack) {
		EquipmentSlot slot = stack.has(net.minecraft.core.component.DataComponents.EQUIPPABLE)
				? stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE).slot() : null;
		return slot == EquipmentSlot.CHEST && !stack.is(Items.ELYTRA);
	}

	/** Has what the move needs, is on the ground with room, and (for pearls) has the health to spare. */
	public boolean canStart(Technique t, LivingEntity target) {
		if (!this.player.onGround() || this.count(Items.MACE) == 0 || this.player.isInWater()) {
			return false;
		}
		int wind = this.count(Items.WIND_CHARGE);
		int pearls = this.count(Items.ENDER_PEARL);
		boolean healthy = this.player.getHealth() > 10.0F;
		return switch (t) {
			case WIND_JUMP -> wind >= 1 && this.roomAbove(5);
			case WIND_CHAIN -> wind >= 2 && this.roomAbove(5);
			case PEARL_CATCH -> wind >= 2 && pearls >= 1 && healthy && this.roomAbove(12);
			case DIAGONAL_PEARL -> wind >= 1 && pearls >= 1 && healthy;
			case ELYTRA_MACE -> this.count(Items.ELYTRA) + (this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA) ? 1 : 0) > 0
					&& this.count(Items.FIREWORK_ROCKET) >= 2 && this.roomAbove(8) && this.skyOver(target)
					&& Inv.find(this.player, MaceTechniques::isChestplate) >= 0;
		};
	}

	/**
	 * A sensible move for this distance and target, or null: wind jumps and chains up close (chains when the mace has
	 * Wind Burst or the target is heavily armored), pearl catches at 3-8 blocks, diagonal pearls at 8-18, elytra dives
	 * at 12-40. Aerialists (FelixSyn) favor the pearl and elytra moves.
	 */
	public @Nullable Technique choose(LivingEntity target, double distance, PlayStyle style) {
		if (!this.isReady()) {
			return null;
		}
		boolean armored = target.getArmorValue() >= 15;
		boolean windBurst = Inv.find(this.player, stack -> stack.is(Items.MACE) && this.level(stack, Enchantments.WIND_BURST) > 0) >= 0;
		float air = style == PlayStyle.AERIALIST ? 1.6F : 1.0F;
		List<Technique> options = new ArrayList<>();
		List<Float> weights = new ArrayList<>();
		if (distance > 1.5 && distance < 5.5) {
			options.add(Technique.WIND_JUMP);
			weights.add(3.0F);
			options.add(Technique.WIND_CHAIN);
			weights.add(windBurst || armored ? 3.0F : 1.0F);
		}
		if (distance > 3.0 && distance < 8.0) {
			options.add(Technique.PEARL_CATCH);
			weights.add((armored ? 2.5F : 1.2F) * air);
		}
		if (distance > 8.0 && distance < 18.0) {
			options.add(Technique.DIAGONAL_PEARL);
			weights.add(2.5F * air);
		}
		if (distance > 12.0 && distance < 40.0) {
			options.add(Technique.ELYTRA_MACE);
			weights.add(2.0F * air);
		}
		float total = 0.0F;
		for (int i = options.size() - 1; i >= 0; i--) {
			if (!this.canStart(options.get(i), target)) {
				options.remove(i);
				weights.remove(i);
			} else {
				total += weights.get(i);
			}
		}
		if (options.isEmpty()) {
			return null;
		}
		float roll = this.random.nextFloat() * total;
		for (int i = 0; i < options.size(); i++) {
			roll -= weights.get(i);
			if (roll <= 0.0F) {
				return options.get(i);
			}
		}
		return options.get(options.size() - 1);
	}

	// ---------------------------------------------------------------- running a move

	public void start(Technique t, LivingEntity target, int cooldownTicks) {
		this.technique = t;
		this.cooldown = cooldownTicks;
		this.totalTicks = 0;
		this.chainHits = 0;
		this.caught = false;
		this.warnedBounce = false;
		this.wasRising = false;
		this.failed = this.random.nextFloat() < this.limits.failChance;
		this.sideSign = this.random.nextBoolean() ? 1 : -1;
		double angle = this.random.nextDouble() * Math.PI * 2.0;
		this.missOffset = this.failed && (t == Technique.ELYTRA_MACE || t == Technique.WIND_JUMP)
				? new Vec3(Math.cos(angle) * 4.0, 0.0, Math.sin(angle) * 4.0) : Vec3.ZERO;
		this.diveTopY = target.getY() + 16.0;
		this.combat.stop();
		this.setPhase(this.limits.warnTicks > 0 ? Phase.WARN : this.firstPhase(t));
	}

	private Phase firstPhase(Technique t) {
		return switch (t) {
			case DIAGONAL_PEARL -> Phase.PEARL_IN;
			case ELYTRA_MACE -> Phase.TAKEOFF;
			default -> Phase.LAUNCH;
		};
	}

	private void setPhase(Phase phase) {
		this.phase = phase;
		this.phaseTicks = 0;
	}

	/** Ends the move; puts the chestplate back on first if the elytra is still worn. */
	private boolean finish() {
		if (this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA) && Inv.find(this.player, MaceTechniques::isChestplate) >= 0
				&& this.phase != Phase.ARMOR) {
			this.setPhase(Phase.ARMOR);
			return true;
		}
		this.technique = null;
		return false;
	}

	private void warn(boolean sound) {
		if (this.limits.warn != null) {
			this.limits.warn.accept(sound);
		}
	}

	/** One tick of the move. Returns false once it's over. */
	public boolean tick(LivingEntity target) {
		if (this.technique == null) {
			return false;
		}
		this.totalTicks++;
		this.phaseTicks++;
		if (this.totalTicks > 500 || !target.isAlive()) {
			return this.finish();
		}
		Vec3 pos = this.player.position();
		boolean teleported = this.lastPos != null && pos.distanceTo(this.lastPos) > 2.5;
		// A wind-charge burst mid-air shows as a sudden upward kick.
		double dy = this.player.getDeltaMovement().y;
		boolean boosted = dy - this.lastDy > 0.4;
		this.lastPos = pos;
		this.lastDy = dy;
		return switch (this.phase) {
			case WARN -> this.warnPhase(target);
			case PEARL_IN -> this.pearlIn(target, teleported);
			case LAUNCH -> this.launch(target);
			case RISE -> this.rise(target);
			case CATCH -> this.catchPearl(target, teleported || boosted);
			case AIR -> this.air(target);
			case RELAUNCH -> this.relaunch(target);
			case TAKEOFF -> this.takeoff(target);
			case CLIMB -> this.climb(target);
			case CRUISE -> this.cruise(target);
			case OVER -> this.over(target);
			case DROP -> this.drop(target);
			case ARMOR -> this.armor();
		};
	}

	/** Standing still, facing them, while the warning plays (Master BreadLoaf). */
	private boolean warnPhase(LivingEntity target) {
		this.controls.lookAt(target.getEyePosition());
		if (this.phaseTicks == 1) {
			this.warn(true);
		} else if (this.phaseTicks % 4 == 0) {
			this.warn(false);
		}
		if (this.phaseTicks >= this.limits.warnTicks) {
			this.setPhase(this.firstPhase(this.technique));
		}
		return true;
	}

	/** Look down, jump, throw a wind charge at the feet. */
	private boolean launch(LivingEntity target) {
		int slot = Inv.ready(this.player, stack -> stack.is(Items.WIND_CHARGE), WIND_SLOT);
		if (slot < 0) {
			return this.finish();
		}
		this.controls.selectSlot(slot);
		this.controls.lookInstantly(Controls.yawTo(this.player.position(), target.position()), 90.0F);
		if (this.player.onGround() && this.phaseTicks <= 2) {
			this.controls.jumpOnce();
		} else if (!this.player.onGround() && this.phaseTicks >= 3) {
			this.controls.useOnce();
			this.wasRising = true;
			this.setPhase(this.technique == Technique.PEARL_CATCH ? Phase.RISE : Phase.AIR);
		} else if (this.phaseTicks >= 6) {
			// The jump never happened (a ceiling, stuck): give up.
			return this.finish();
		}
		return true;
	}

	/** Pearl catch: ride the wind jump up, drifting toward them, until near the top. */
	private boolean rise(LivingEntity target) {
		this.controls.walkToward(target.position());
		this.controls.lookAt(target.position());
		if (this.phaseTicks > 3 && this.player.getDeltaMovement().y < 0.12 || this.phaseTicks > 25) {
			this.setPhase(Phase.CATCH);
		}
		return true;
	}

	/**
	 * The catch: a wind charge, then a pearl down the same line the next tick (steeply down and toward the target; a
	 * failed one is thrown off line). If the pearl meets the wind charge, they teleport there and the burst launches
	 * them again. Either way, after a moment they go on to the smash.
	 */
	private boolean catchPearl(LivingEntity target, boolean teleported) {
		float yaw = Controls.yawTo(this.player.position(), target.position()) + (this.failed ? 25.0F * this.sideSign : 0.0F);
		float pitch = this.failed ? 50.0F : 78.0F;
		if (this.phaseTicks == 1) {
			int wind = Inv.ready(this.player, stack -> stack.is(Items.WIND_CHARGE), WIND_SLOT);
			if (wind < 0) {
				this.setPhase(Phase.AIR);
				return true;
			}
			this.controls.selectSlot(wind);
			this.controls.lookInstantly(yaw, pitch);
			this.controls.useOnce();
			return true;
		}
		if (this.phaseTicks == 2) {
			int pearl = Inv.ready(this.player, stack -> stack.is(Items.ENDER_PEARL), PEARL_SLOT);
			if (pearl >= 0) {
				this.controls.selectSlot(pearl);
				this.controls.lookInstantly(yaw, pitch);
				this.controls.useOnce();
			}
			return true;
		}
		if (teleported) {
			this.caught = true;
		}
		if (this.phaseTicks >= 10 || this.caught && this.phaseTicks >= 4) {
			this.setPhase(Phase.AIR);
		}
		return true;
	}

	/**
	 * Diagonal pearl: an arcing pearl to land 2-3 blocks beside the target (a failed throw lands a few blocks wide),
	 * then a wind jump from there once the pearl has brought them over.
	 */
	private boolean pearlIn(LivingEntity target, boolean teleported) {
		if (this.phaseTicks == 1) {
			int pearl = Inv.ready(this.player, stack -> stack.is(Items.ENDER_PEARL), PEARL_SLOT);
			if (pearl < 0) {
				return this.finish();
			}
			this.controls.selectSlot(pearl);
			Vec3 toTarget = target.position().subtract(this.player.position()).multiply(1.0, 0.0, 1.0).normalize();
			Vec3 side = new Vec3(-toTarget.z, 0.0, toTarget.x).scale(this.sideSign * (this.failed ? 6.0 : 2.5));
			Vec3 spot = target.position().add(side).add(target.getDeltaMovement().multiply(10.0, 0.0, 10.0));
			float[] angles = arc(this.player.getEyePosition(), spot.add(0.0, 0.2, 0.0), 1.5, 0.03);
			this.controls.lookInstantly(angles[0], angles[1]);
			return true;
		}
		if (this.phaseTicks == 2) {
			this.controls.useOnce();
			return true;
		}
		this.controls.lookAt(target.getEyePosition());
		if (teleported && this.player.onGround() || teleported && this.phaseTicks > 4) {
			this.setPhase(Phase.LAUNCH);
			return true;
		}
		if (this.phaseTicks > 70) {
			// The pearl was lost (fell somewhere, hit something): no move this time.
			return this.finish();
		}
		return true;
	}

	/**
	 * In the air: the right mace for the drop, drift onto them and smash on the way down when the crosshair is on
	 * them. Wind Burst bounces them up for another; a wind chain throws a wind charge down at them after each hit.
	 */
	private boolean air(LivingEntity target) {
		double drop = this.player.getY() - target.getY();
		this.selectMace(target, drop);
		Vec3 aim = this.combat.aimPoint(target).add(this.missOffset);
		this.controls.humanAim(aim, this.combat.skill.aimSpeedDegrees * 1.2F, this.combat.skill.aimErrorDegrees * 0.7F);
		this.controls.walkToward(target.position().add(this.missOffset));
		this.controls.setSprinting(false);
		if (this.player.fallDistance > this.limits.maxFall) {
			this.player.fallDistance = this.limits.maxFall;
		}
		double dy = this.player.getDeltaMovement().y;
		boolean falling = dy < 0.0 && !this.player.onGround();
		boolean canHit = this.chainHits < this.limits.maxChain;
		// Chains: warn again at the top of each bounce (Master BreadLoaf), once per bounce.
		if (this.wasRising && dy <= 0.0 && this.chainHits > 0 && canHit && !this.warnedBounce) {
			this.warnedBounce = true;
			this.warn(true);
		}
		this.wasRising = dy > 0.0;
		if (canHit && falling && this.player.fallDistance > 1.5 && this.onTarget(target)) {
			this.controls.attackOnce();
			this.chainHits++;
			this.warnedBounce = false;
			ItemStack mace = this.player.getMainHandItem();
			if (this.technique == Technique.WIND_CHAIN && this.chainHits < this.limits.maxChain
					&& this.level(mace, Enchantments.WIND_BURST) == 0 && this.count(Items.WIND_CHARGE) > 0) {
				this.setPhase(Phase.RELAUNCH);
				return true;
			}
		}
		if (this.player.onGround() && this.phaseTicks > 8 || this.phaseTicks > 160) {
			return this.finish();
		}
		return true;
	}

	/** Right after a smash: a wind charge straight down at their feet bursts on them and launches us up again. */
	private boolean relaunch(LivingEntity target) {
		int wind = Inv.ready(this.player, stack -> stack.is(Items.WIND_CHARGE), WIND_SLOT);
		if (wind < 0) {
			this.setPhase(Phase.AIR);
			return true;
		}
		this.controls.selectSlot(wind);
		this.controls.lookInstantly(Controls.yawTo(this.player.position(), target.position()), 88.0F);
		if (this.phaseTicks == 2) {
			this.controls.useOnce();
			this.wasRising = true;
			this.setPhase(Phase.AIR);
		}
		return true;
	}

	/** The crosshair is on the target, within reach. */
	private boolean onTarget(LivingEntity target) {
		Vec3 eyes = this.player.getEyePosition();
		Vec3 end = eyes.add(this.player.getViewVector(1.0F).scale(this.combat.reach() - 0.1));
		return target.getBoundingBox().inflate(0.1).clip(eyes, end).isPresent();
	}

	// ---------------------------------------------------------------- elytra mace

	/** Swap the chestplate for the elytra (right-click it in the hotbar), jump, and start gliding. */
	private boolean takeoff(LivingEntity target) {
		boolean wearingElytra = this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
		if (!wearingElytra) {
			int elytra = Inv.ready(this.player, stack -> stack.is(Items.ELYTRA), ELYTRA_SLOT);
			if (elytra < 0 || this.phaseTicks > 10) {
				return this.finish();
			}
			this.controls.selectSlot(elytra);
			this.controls.look(this.player.getYRot(), -60.0F);
			if (this.phaseTicks % 3 == 1) {
				this.controls.useOnce();
			}
			return true;
		}
		this.controls.look(Controls.yawTo(this.player.position(), target.position()), -60.0F);
		if (this.player.isFallFlying()) {
			this.useRocket();
			this.setPhase(Phase.CLIMB);
			return true;
		}
		if (this.player.onGround()) {
			this.controls.jumpOnce();
		} else if (this.player.getDeltaMovement().y < 0.1) {
			// The jump key in mid-air: start gliding.
			this.player.tryToStartFallFlying();
		}
		return this.phaseTicks <= 40 || this.finish();
	}

	private void useRocket() {
		if (this.totalTicks - this.lastRocketTick < 18) {
			return;
		}
		int rocket = Inv.ready(this.player, stack -> stack.is(Items.FIREWORK_ROCKET), ROCKET_SLOT);
		if (rocket < 0) {
			return;
		}
		this.controls.selectSlot(rocket);
		this.controls.useOnce();
		this.lastRocketTick = this.totalTicks;
	}

	/** Rocket up at a steep angle until well above the target (or a ceiling). */
	private boolean climb(LivingEntity target) {
		if (!this.player.isFallFlying()) {
			this.setPhase(Phase.DROP);
			return true;
		}
		this.controls.look(Controls.yawTo(this.player.position(), target.position()), -55.0F);
		this.useRocket();
		if (this.player.getY() >= this.diveTopY || !this.roomAbove(4) || this.phaseTicks > 160) {
			this.diveTopY = Math.max(target.getY() + 8.0, Math.min(this.diveTopY, this.player.getY()));
			this.setPhase(Phase.CRUISE);
		}
		return true;
	}

	/** Glide to a point above the target (or beside them, on a dive that goes wrong). */
	private boolean cruise(LivingEntity target) {
		if (!this.player.isFallFlying()) {
			this.setPhase(Phase.DROP);
			return true;
		}
		Vec3 spot = target.position().add(this.missOffset);
		Vec3 over = new Vec3(spot.x, this.diveTopY, spot.z);
		double horizontal = Math.sqrt(Math.pow(spot.x - this.player.getX(), 2) + Math.pow(spot.z - this.player.getZ(), 2));
		float pitch = this.player.getY() < this.diveTopY - 3.0 ? -25.0F : horizontal > 20.0 ? 5.0F : 15.0F;
		this.controls.look(Controls.yawTo(this.player.position(), over), pitch);
		double speed = this.player.getDeltaMovement().horizontalDistance();
		if ((speed < 0.6 || this.player.getY() < this.diveTopY - 5.0) && horizontal > 8.0) {
			this.useRocket();
		}
		if (horizontal < 4.5 || this.phaseTicks > 200) {
			this.setPhase(this.limits.warnTicks > 0 ? Phase.OVER : Phase.DROP);
		}
		return true;
	}

	/** Over the target: pull up to bleed speed while the warning plays (Master BreadLoaf only). */
	private boolean over(LivingEntity target) {
		this.controls.look(Controls.yawTo(this.player.position(), target.position()), -35.0F);
		if (this.phaseTicks == 1) {
			this.warn(true);
		} else if (this.phaseTicks % 4 == 0) {
			this.warn(false);
		}
		if (this.phaseTicks >= this.limits.warnTicks || !this.player.isFallFlying()) {
			this.setPhase(Phase.DROP);
		}
		return true;
	}

	/** Chestplate back on (stops the glide, so the fall starts counting), then the smash. */
	private boolean drop(LivingEntity target) {
		if (this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
			int chestplate = Inv.ready(this.player, MaceTechniques::isChestplate, ELYTRA_SLOT);
			if (chestplate >= 0 && this.phaseTicks <= 6) {
				this.controls.selectSlot(chestplate);
				this.controls.look(this.player.getYRot(), -60.0F);
				if (this.phaseTicks % 2 == 1) {
					this.controls.useOnce();
				}
				return true;
			}
		}
		this.setPhase(Phase.AIR);
		return true;
	}

	/** After the move: the chestplate back on if the elytra is still worn. */
	private boolean armor() {
		int chestplate = Inv.ready(this.player, MaceTechniques::isChestplate, ELYTRA_SLOT);
		if (chestplate < 0 || this.phaseTicks > 12 || !this.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
			this.technique = null;
			return false;
		}
		this.controls.selectSlot(chestplate);
		this.controls.look(this.player.getYRot(), -60.0F);
		if (this.phaseTicks % 3 == 1) {
			this.controls.useOnce();
		}
		return true;
	}

	// ---------------------------------------------------------------- mace choice and aiming

	private int level(ItemStack stack, ResourceKey<Enchantment> key) {
		Holder<Enchantment> holder = this.player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
		return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
	}

	/** Breach against heavy armor, Density for big drops, Wind Burst for chains; any mace otherwise. */
	public void selectMace(LivingEntity target, double drop) {
		int best = -1;
		int bestScore = -1;
		boolean chain = this.technique == Technique.WIND_CHAIN || this.technique == Technique.WIND_JUMP;
		for (int i = 0; i < Inv.MAIN; i++) {
			ItemStack stack = this.player.getInventory().getItem(i);
			if (!stack.is(Items.MACE)) {
				continue;
			}
			int score = 1;
			if (target.getArmorValue() >= 15) {
				score += this.level(stack, Enchantments.BREACH) * 3;
			}
			if (drop >= 6.0) {
				score += this.level(stack, Enchantments.DENSITY) * 2;
			}
			if (chain) {
				score += this.level(stack, Enchantments.WIND_BURST) * 3;
			}
			if (score > bestScore) {
				bestScore = score;
				best = i;
			}
		}
		if (best >= 0) {
			this.controls.selectSlot(Inv.toHotbar(this.player, best, MACE_SLOT));
		}
	}

	/** Yaw and pitch to land a projectile (speed v, gravity g) on a point, low arc. */
	static float[] arc(Vec3 from, Vec3 to, double v, double g) {
		double dx = to.x - from.x;
		double dz = to.z - from.z;
		double dy = to.y - from.y;
		double d = Math.sqrt(dx * dx + dz * dz);
		double v2 = v * v;
		double disc = v2 * v2 - g * (g * d * d + 2.0 * dy * v2);
		double angle = disc < 0.0 ? Math.PI / 4.0 : Math.atan2(v2 - Math.sqrt(disc), g * d);
		return new float[]{(float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F, (float) (-angle * Mth.RAD_TO_DEG)};
	}
}
