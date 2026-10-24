package com.itzbreadloaf.brain;

import com.itzbreadloaf.ModAdvancements;
import com.itzbreadloaf.character.LoafCharacter;
import com.itzbreadloaf.character.LoafIds;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Shared by the neutral girls, ImLilyLol and Madison: they never attack first, get angry when hurt (their weapon
 * only comes out while they fight: it waits in hotbar slot 3 and they switch to it), trust a player after enough
 * gifts, defend whoever they trust, and live around their home.
 */
public abstract class NeutralBrain extends LoafBrain {
	/** Hotbar slot of the calm item (a flower, a carrot... or nothing). */
	public static final int CALM_SLOT = 0;
	/** Hotbar slot of the weapon they switch to when angry. */
	public static final int WEAPON_SLOT = 2;

	private static final Codec<Map<UUID, Integer>> TRUST_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT);

	protected final Map<UUID, Integer> trust = new HashMap<>();
	protected int anger;
	protected boolean homeAssigned;
	private int nextVoiceTick;
	private int nextHelloTick;

	protected NeutralBrain(LoafCharacter character) {
		super(character);
	}

	// ---------------------------------------------------------------- per-character settings

	public abstract int giftsToTrust();

	public abstract boolean isTrustGift(ItemStack stack);

	/** Lily only takes gifts from crouching players. */
	protected boolean giftsNeedCrouch() {
		return false;
	}

	protected abstract int angerTicksOnHit();

	/** The weapon that waits in hotbar slot 3 for when she fights. */
	protected abstract ItemStack createFightWeapon();

	public abstract void playVoice();

	protected abstract int defaultHomeRadius();

	/** Criterion name in the "Cousin Club" advancement ("lily" or "madison"). */
	protected abstract String cousinCriterion();

	/** Called once when she starts trusting a player. */
	protected abstract void onBefriended(ServerPlayer friend);

	protected abstract String[] helloLines();

	/** Her calm routine (shyness, idle actions, wandering). */
	protected abstract void tickCalm();

	// ---------------------------------------------------------------- basics

	@Override
	public boolean isFriendlyToMobs() {
		return true;
	}

	@Override
	public boolean isAngry() {
		return this.anger > 0;
	}

	@Override
	protected SoundSource soundSource() {
		return SoundSource.NEUTRAL;
	}

	/** Lily and Madison don't pick things up off the ground. */
	@Override
	public boolean wantsToPickUp(ItemStack stack) {
		return false;
	}

	/** Archer Lily draws her bow at range; everyone else fights up close. */
	protected boolean fightsAtRange() {
		return false;
	}

	/** A move before the normal fight (Witchy Lily throws a potion). True if it used this tick. */
	protected boolean fightTrick(LivingEntity target) {
		return false;
	}

	// ---------------------------------------------------------------- behavior

	@Override
	protected void tickBehavior() {
		// She always has a home: her house, or wherever she first appeared.
		if (!this.homeAssigned && !this.isTame()) {
			this.homeAssigned = true;
			if (this.home == null) {
				this.setupHome(this.player.blockPosition(), this.defaultHomeRadius(), null);
			}
		}
		if (this.anger > 0) {
			this.anger--;
			if (this.anger == 0 || this.target == null || !this.target.isAlive()) {
				this.calmDown();
			}
		}
		if (this.ticks % 10 == 0) {
			this.noticePlayers();
		}
		if (this.target != null && !this.baby) {
			this.drawWeapon();
			if (!this.fightTrick(this.target)) {
				this.fightTarget(this.fightsAtRange());
			}
			return;
		}
		if (this.target != null) {
			// Babies don't fight: they run to a parent or away.
			this.setTarget(null);
		}
		this.holsterWeapon();
		if (this.tryEat() || this.tickLove() || this.followParent()) {
			return;
		}
		if (this.isTame()) {
			this.protectOwner(false);
			if (this.target != null) {
				return;
			}
			if (!this.followOwner()) {
				this.tickCalmWithFriend();
			}
			return;
		}
		this.tickCalm();
	}

	/** With her friend: mostly the same quiet routine, but staying close. */
	protected void tickCalmWithFriend() {
		this.idleWithOwner();
	}

	/** Players she trusts (her gifts system) count as liked, too: she greets them. */
	@Override
	public boolean likesPlayer(Player player) {
		return super.likesPlayer(player) || this.trustsPlayer(player);
	}

	/** Stepping in for a friend: angry at whoever hurt them (without the trust reset that hitting her causes). */
	@Override
	protected void defendAgainst(LivingEntity attacker) {
		this.anger = this.angerTicksOnHit();
		this.setTarget(attacker);
		this.drawWeapon();
		this.sitting = false;
	}

	private void drawWeapon() {
		if (this.player.getInventory().getItem(WEAPON_SLOT).isEmpty()) {
			int weapon = Inv.find(this.player, stack -> Combat.kindOf(stack) == Combat.Weapon.MELEE);
			if (weapon >= 0) {
				Inv.toHotbar(this.player, weapon, WEAPON_SLOT);
			} else {
				this.player.getInventory().setItem(WEAPON_SLOT, this.createFightWeapon());
			}
		}
		// Only from the calm item (book, flower...): once she has a weapon or bow out, the fight code picks what to hold.
		if (this.player.getInventory().getSelectedSlot() == CALM_SLOT) {
			this.controls.switchSlot(WEAPON_SLOT, this.skill().hotbarSwitchTicks);
		}
	}

	private void holsterWeapon() {
		if (this.player.getInventory().getSelectedSlot() == WEAPON_SLOT && !this.isEating()) {
			this.controls.selectSlot(CALM_SLOT);
		}
	}

	/** Plays her voice when she first notices someone (10 s cooldown) and says hi to her friend now and then. */
	private void noticePlayers() {
		Player nearest = this.player.level().getNearestPlayer(this.player.getX(), this.player.getY(), this.player.getZ(), 10.0,
				p -> p != this.player && LoafIds.isRealPlayer(p) && !p.isSpectator());
		if (nearest == null) {
			return;
		}
		if (this.ticks >= this.nextVoiceTick) {
			this.playVoice();
			this.nextVoiceTick = this.ticks + 200;
		}
		if (nearest instanceof ServerPlayer friend && this.isOwnedBy(friend) && this.ticks >= this.nextHelloTick) {
			this.say(friend, com.itzbreadloaf.ModMessages.pick(this.random, this.helloLines()));
			this.nextHelloTick = this.ticks + 3600;
		}
	}

	/**
	 * The nearest real player within {@code range} she doesn't trust, or null. She remembers who hurt her: those count
	 * from twice as far away, and everyone counts from a bit farther when she's scared.
	 */
	protected @Nullable Player nearestStranger(double range) {
		double near = this.mood.mood() == com.itzbreadloaf.brain.ai.Mood.SCARED ? range * 1.3 : range;
		long gameTime = this.player.level().getGameTime();
		return this.player.level().getNearestPlayer(this.player.getX(), this.player.getY(), this.player.getZ(), range * 2.0,
				p -> p != this.player && p instanceof Player pl && LoafIds.isRealPlayer(pl) && !pl.isSpectator() && !this.trustsPlayer(pl)
						&& (pl.distanceTo(this.player) <= near || this.memory.isFeared(pl.getUUID(), gameTime)));
	}

	/** Walks to a spot {@code distance} blocks away from {@code from}, trying angled escapes if straight away is blocked. */
	protected boolean moveAwayFrom(Vec3 from, double distance, Navigator.Speed speed) {
		Vec3 away = this.player.position().subtract(from).multiply(1.0, 0.0, 1.0);
		if (away.lengthSqr() < 1.0E-4) {
			away = new Vec3(1.0, 0.0, 0.0);
		}
		away = away.normalize();
		for (double angle : new double[]{0, 35, -35, 70, -70, 110, -110}) {
			Vec3 direction = away.yRot((float) Math.toRadians(angle));
			Vec3 goal = this.player.position().add(direction.scale(distance));
			Vec3 spot = this.findSpotNear(net.minecraft.core.BlockPos.containing(goal), 0);
			if (spot != null && spot.distanceToSqr(from) > this.player.position().distanceToSqr(from)) {
				this.nav.moveTo(spot, 1.0, speed);
				return true;
			}
		}
		return false;
	}

	// ---------------------------------------------------------------- anger

	@Override
	public void onHurt(DamageSource source, float damage, boolean blocked) {
		super.onHurt(source, damage, blocked);
		if (source.getEntity() instanceof LivingEntity attacker && attacker != this.player && !this.baby) {
			this.becomeAngryAt(attacker, this.angerTicksOnHit());
		}
	}

	public void becomeAngryAt(LivingEntity attacker, int ticks) {
		this.anger = ticks;
		this.setTarget(attacker);
		this.drawWeapon();
		this.sitting = false;
		if (attacker instanceof Player player) {
			// Attacking her resets her trust to 0, even for her friend.
			this.trust.put(player.getUUID(), 0);
			if (this.isOwnedBy(player)) {
				this.owner = null;
				this.homeAssigned = false;
			}
		}
	}

	public void calmDown() {
		this.anger = 0;
		this.setTarget(null);
		this.lastAttacker = null;
		this.nav.stop();
		if (this.home != null) {
			this.nav.moveTo(this.home, 2.0, Navigator.Speed.WALK);
		}
	}

	// ---------------------------------------------------------------- trust

	public int getTrust(Player player) {
		return this.trust.getOrDefault(player.getUUID(), 0);
	}

	public boolean trustsPlayer(Player player) {
		return this.isOwnedBy(player);
	}

	/** Makes the player her friend: she follows them, sits on command and defends them. */
	public void befriend(ServerPlayer friend) {
		this.trust.put(friend.getUUID(), this.giftsToTrust());
		this.setOwner(friend);
		ModAdvancements.award(friend, ModAdvancements.COUSIN_CLUB, this.cousinCriterion());
		this.onBefriended(friend);
	}

	@Override
	public void onAdopted(UUID ownerId) {
		this.trust.put(ownerId, this.giftsToTrust());
		this.homeAssigned = true;
	}

	/** A gift from someone she doesn't trust yet: it goes into her inventory. */
	protected InteractionResult receiveTrustGift(ServerPlayer giver, ItemStack stack) {
		if (this.giftsNeedCrouch() && !giver.isShiftKeyDown()) {
			// Too shy to take it from someone standing over her.
			this.particles(ParticleTypes.SMOKE, 7);
			this.say(giver, "(she's too shy... try crouching)");
			return InteractionResult.FAIL;
		}
		ItemStack gift = stack.copyWithCount(1);
		stack.consume(1, giver);
		Inv.give(this.player, gift);
		int newTrust = Math.min(this.giftsToTrust(), this.getTrust(giver) + 1);
		this.trust.put(giver.getUUID(), newTrust);
		this.playVoice();
		if (newTrust >= this.giftsToTrust() && !this.isTame()) {
			this.befriend(giver);
			this.particles(ParticleTypes.HEART, 7);
		} else {
			this.particles(ParticleTypes.HAPPY_VILLAGER, 7);
			this.say(giver, "...thank you (" + newTrust + "/" + this.giftsToTrust() + ")");
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult onInteract(ServerPlayer clicker, net.minecraft.world.InteractionHand hand) {
		if (this.isAngry()) {
			return InteractionResult.PASS;
		}
		return super.onInteract(clicker, hand);
	}

	@Override
	protected @Nullable InteractionResult interact(ServerPlayer clicker, ItemStack stack) {
		if (this.isOwnedBy(clicker)) {
			if (stack.is(this.breedFood()) && this.baby) {
				stack.consume(1, clicker);
				this.ageUp(GROW_UP_TICKS / 10);
				this.particles(ParticleTypes.HAPPY_VILLAGER, 7);
				return InteractionResult.SUCCESS;
			}
			if (this.isTrustGift(stack) && this.player.getHealth() < this.player.getMaxHealth()) {
				ItemStack gift = stack.copyWithCount(1);
				stack.consume(1, clicker);
				this.player.heal(4.0F);
				this.particles(ParticleTypes.HEART, 7);
				return InteractionResult.SUCCESS;
			}
			if (stack.is(this.breedFood()) && this.canFallInLove()) {
				stack.consume(1, clicker);
				this.setInLove();
				this.playVoice();
				return InteractionResult.SUCCESS;
			}
			if (stack.isEmpty()) {
				this.setSitting(!this.sitting);
				return InteractionResult.SUCCESS;
			}
			return null;
		}
		if (this.isTrustGift(stack)) {
			return this.receiveTrustGift(clicker, stack);
		}
		return null;
	}

	// ---------------------------------------------------------------- saving

	@Override
	public void save(CompoundTag tag) {
		super.save(tag);
		tag.store("Trust", TRUST_CODEC, Map.copyOf(this.trust));
		tag.putInt("Anger", this.anger);
		tag.putBoolean("HomeAssigned", this.homeAssigned);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.trust.clear();
		tag.read("Trust", TRUST_CODEC).ifPresent(this.trust::putAll);
		this.anger = 0;
		this.homeAssigned = tag.getBooleanOr("HomeAssigned", false);
	}
}
