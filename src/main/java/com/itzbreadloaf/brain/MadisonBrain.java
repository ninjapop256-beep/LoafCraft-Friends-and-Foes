package com.itzbreadloaf.brain;

import com.itzbreadloaf.ModAdvancements;
import com.itzbreadloaf.character.CharacterKind;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.CharacterVariant;
import com.itzbreadloaf.character.LoafCharacter;
import com.itzbreadloaf.character.LoafIds;
import com.itzbreadloaf.item.ModItems;
import com.itzbreadloaf.mixin.FishingHookAccessor;
import com.itzbreadloaf.sound.ModSounds;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Madison, ImLilyLol's cousin, and her types: slightly shy (keeps about 3 blocks from strangers, but walks, never
 * runs), trusts a player after 3 gifts, fast (about 5 blocks/s walking), tends her garden, opens her pantry chest to
 * check on it, visits Lily, and gets very angry (45 s, stone or iron axe) at anyone who raids her pantry. Putting
 * the food back calms her down.
 *
 * Types (each does her job with real clicks):
 * - Farmer: breaks fully grown crops near her home and replants them with the right seed.
 * - Baker: runs the bakery. Right-click her with an emerald for 2 Loaf Bread, crouch with 3 emeralds for a cake.
 * - Fisher: casts a fishing rod into water near her home, waits for a bite, and reels in real fishing loot.
 * - Beekeeper: collects honey from full beehives with glass bottles (her hives sit over campfires so the bees stay calm).
 */
public class MadisonBrain extends NeutralBrain {
	public static final int GIFTS_TO_TRUST = 3;
	public static final int ANGER_TICKS = 900;
	public static final int HOME_RADIUS = 10;
	public static final double PANTRY_WATCH_DISTANCE = 16.0;
	public static final double KEEP_DISTANCE = 3.0;
	/** Player walk speed is 0.1 (4.3 blocks/s). 0.116 is about 5 blocks/s. */
	public static final double SPEED = 0.116;

	private enum Idle { NONE, TEND_GARDEN, CHECK_PANTRY, VISIT_LILY, VISIT_FRIEND }

	private @Nullable BlockPos pantryPos;
	private int lastPantryFood = -1;
	/** Food in the pantry before it was raided; putting it back calms her down. */
	private int foodBeforeTheft = -1;
	private @Nullable UUID thief;

	private Idle idle = Idle.NONE;
	private int idleTicks;
	private @Nullable BlockPos spot;
	private @Nullable Entity buddy;
	private boolean pantryOpen;

	// ---- job state
	private @Nullable BlockPos jobPos;
	private int jobStep;
	private int jobTimer;
	private int jobCooldown;
	private @Nullable Item replant;
	private int fishingTicks;
	private int pickupTicks;
	private final Fishing fishing = new Fishing(this);

	public MadisonBrain(LoafCharacter character) {
		super(character);
		// Defensive until provoked, then she backs off early to heal.
		this.combat.retreatBelow = (float) com.itzbreadloaf.config.ModConfig.get().cousinsRetreatHealth;
	}

	@Override
	protected double baseMaxHealth() {
		return com.itzbreadloaf.config.ModConfig.get().madisonHealth;
	}

	@Override
	protected double baseSpeed() {
		return SPEED;
	}

	@Override
	protected void rollGear() {
		Kits.madison(this.player, this.player.level(), this.variant, this.random);
	}

	@Override
	protected void restock() {
		if (this.baby) {
			return;
		}
		if (Inv.count(this.player, Inv::isFood) < 4) {
			Inv.give(this.player, new ItemStack(Items.BREAD, 6));
			Inv.give(this.player, new ItemStack(Items.BAKED_POTATO, 6));
		}
		switch (this.variant) {
			case BAKER -> {
				if (Inv.count(this.player, stack -> stack.is(ModItems.LOAF_BREAD)) < 16) {
					Inv.give(this.player, new ItemStack(ModItems.LOAF_BREAD, 32));
				}
				if (Inv.count(this.player, stack -> stack.is(Items.CAKE)) < 2) {
					Inv.give(this.player, new ItemStack(Items.CAKE, 2));
				}
			}
			case MERCHANT -> {
				if (Inv.count(this.player, stack -> stack.is(Items.APPLE)) < 8) {
					Inv.give(this.player, new ItemStack(Items.APPLE, 16));
					Inv.give(this.player, new ItemStack(Items.GOLDEN_CARROT, 6));
					Inv.give(this.player, new ItemStack(Items.MELON_SLICE, 16));
				}
				if (Inv.count(this.player, stack -> stack.is(Items.EMERALD)) < 3) {
					Inv.give(this.player, new ItemStack(Items.EMERALD, 4));
				}
			}
			case BEEKEEPER -> {
				if (Inv.count(this.player, stack -> stack.is(Items.GLASS_BOTTLE)) < 4) {
					Inv.give(this.player, new ItemStack(Items.GLASS_BOTTLE, 12));
				}
			}
			case FARMER -> {
				if (Inv.count(this.player, stack -> stack.is(Items.WHEAT_SEEDS)) < 4) {
					Inv.give(this.player, new ItemStack(Items.WHEAT_SEEDS, 8));
				}
			}
			default -> {
			}
		}
	}

	// ---------------------------------------------------------------- personality settings

	@Override
	public int giftsToTrust() {
		return GIFTS_TO_TRUST;
	}

	@Override
	public boolean isTrustGift(ItemStack stack) {
		return isFavoriteGift(stack) || stack.is(ModItems.LOAF_BREAD)
				|| this.variant == CharacterVariant.FISHER && stack.is(ItemTags.FISHES)
				|| this.variant == CharacterVariant.FARMER && (stack.is(Items.BONE_MEAL) || stack.is(Items.BEETROOT_SEEDS));
	}

	/** Madison's favorite gifts: golden carrots, pumpkin pie, honey bottles, glow berries. */
	public static boolean isFavoriteGift(ItemStack stack) {
		return stack.is(Items.GOLDEN_CARROT) || stack.is(Items.PUMPKIN_PIE) || stack.is(Items.HONEY_BOTTLE) || stack.is(Items.GLOW_BERRIES);
	}

	@Override
	protected int angerTicksOnHit() {
		return ANGER_TICKS;
	}

	@Override
	protected ItemStack createFightWeapon() {
		return new ItemStack(this.random.nextBoolean() ? Items.STONE_AXE : Items.IRON_AXE);
	}

	@Override
	public void playVoice() {
		this.playSound(ModSounds.MADISON_VOICE, 0.8F, 1.0F);
	}

	@Override
	protected int defaultHomeRadius() {
		return HOME_RADIUS;
	}

	@Override
	protected String cousinCriterion() {
		return "madison";
	}

	@Override
	protected void onBefriended(ServerPlayer friend) {
		this.say(friend, "okay, you're cool. friends!");
	}

	@Override
	protected String[] helloLines() {
		return switch (this.variant) {
			case MERCHANT -> new String[]{"fresh produce! one emerald!", "hey! got any crops to sell?", "best apples in town!"};
			case FARMER -> new String[]{"hey! harvest's looking good", "hi! want some carrots?", "*wipes her hands* hey!"};
			case BAKER -> new String[]{"welcome to the bakery!", "fresh loaves, one emerald!", "hey! smell that?"};
			case FISHER -> new String[]{"shh, they're biting", "hey! caught a big one earlier", "hi! nice day for fishing"};
			case BEEKEEPER -> new String[]{"hey! careful, the bees are busy", "hi! want some honey?", "*buzz buzz* hey!"};
			default -> new String[]{"hey!", "oh, hi there", "want a snack?", "hi! the carrots are growing nicely"};
		};
	}

	@Override
	public Item breedFood() {
		return Items.PUMPKIN_PIE;
	}

	/** Farmers pick up their harvest, fishers their catch. */
	@Override
	public boolean wantsToPickUp(ItemStack stack) {
		if (this.variant == CharacterVariant.FARMER) {
			return stack.is(Items.WHEAT) || stack.is(Items.WHEAT_SEEDS) || stack.is(Items.CARROT) || stack.is(Items.POTATO)
					|| stack.is(Items.BEETROOT) || stack.is(Items.BEETROOT_SEEDS) || stack.is(Items.POISONOUS_POTATO);
		}
		if (this.variant == CharacterVariant.FISHER) {
			return this.fishing.isCollecting() || stack.is(ItemTags.FISHES);
		}
		return false;
	}

	// ---------------------------------------------------------------- baker trading

	@Override
	protected @Nullable InteractionResult interact(ServerPlayer clicker, ItemStack stack) {
		if (this.variant == CharacterVariant.BAKER && stack.is(Items.EMERALD) && !this.baby) {
			boolean cake = clicker.isShiftKeyDown() && stack.getCount() >= 3;
			int price = cake ? 3 : 1;
			Item sold = cake ? Items.CAKE : ModItems.LOAF_BREAD;
			int amount = cake ? 1 : 2;
			int slot = Inv.find(this.player, held -> held.is(sold) && held.getCount() >= amount);
			if (slot < 0) {
				this.say(clicker, "sold out! come back tomorrow");
				return InteractionResult.FAIL;
			}
			// Real items change hands: her stock out, your emeralds in.
			ItemStack goods = this.player.getInventory().getItem(slot).split(amount);
			Inv.give(this.player, stack.copyWithCount(price));
			stack.consume(price, clicker);
			clicker.getInventory().placeItemBackInInventory(goods);
			this.player.swing(InteractionHand.MAIN_HAND);
			this.particles(ParticleTypes.HAPPY_VILLAGER, 5);
			this.say(clicker, cake ? "one cake, fresh from the oven!" : "two loaves, still warm! enjoy");
			ModAdvancements.award(clicker, ModAdvancements.FRESH_FROM_THE_OVEN);
			return InteractionResult.SUCCESS;
		}
		if (this.variant == CharacterVariant.MERCHANT && !this.baby) {
			InteractionResult trade = this.marketTrade(clicker, stack);
			if (trade != null) {
				return trade;
			}
		}
		return super.interact(clicker, stack);
	}

	/**
	 * Merchant Madison at her market stall. An emerald buys a bundle of food from her stock; 16 crops (wheat, carrots,
	 * potatoes, beetroots) earn you an emerald from her purse. Real items change hands both ways.
	 */
	private @Nullable InteractionResult marketTrade(ServerPlayer clicker, ItemStack stack) {
		if (stack.is(Items.EMERALD)) {
			Item[] goods = {Items.GOLDEN_CARROT, Items.PUMPKIN_PIE, Items.APPLE, Items.HONEY_BOTTLE, Items.MELON_SLICE};
			int[] amounts = {3, 2, 4, 1, 8};
			int pick = this.random.nextInt(goods.length);
			Item good = goods[pick];
			int slot = Inv.find(this.player, held -> held.is(good) && held.getCount() >= amounts[pick]);
			if (slot < 0) {
				this.say(clicker, "out of that one, try again!");
				return InteractionResult.FAIL;
			}
			ItemStack bought = this.player.getInventory().getItem(slot).split(amounts[pick]);
			Inv.give(this.player, stack.copyWithCount(1));
			stack.consume(1, clicker);
			clicker.getInventory().placeItemBackInInventory(bought);
			this.player.swing(InteractionHand.MAIN_HAND);
			this.particles(ParticleTypes.HAPPY_VILLAGER, 5);
			this.say(clicker, "fresh from the farm! thanks!");
			return InteractionResult.SUCCESS;
		}
		boolean crop = stack.is(Items.WHEAT) || stack.is(Items.CARROT) || stack.is(Items.POTATO) || stack.is(Items.BEETROOT);
		if (crop && stack.getCount() >= 16) {
			int purse = Inv.find(this.player, held -> held.is(Items.EMERALD));
			if (purse < 0) {
				this.say(clicker, "I'm out of emeralds right now, sorry!");
				return InteractionResult.FAIL;
			}
			ItemStack pay = this.player.getInventory().getItem(purse).split(1);
			Inv.give(this.player, stack.copyWithCount(16));
			stack.consume(16, clicker);
			clicker.getInventory().placeItemBackInInventory(pay);
			this.say(clicker, "nice crops! here's an emerald");
			return InteractionResult.SUCCESS;
		}
		return null;
	}

	// ---------------------------------------------------------------- pantry

	public void setPantryPos(@Nullable BlockPos pos) {
		this.pantryPos = pos == null ? null : pos.immutable();
	}

	public @Nullable BlockPos getPantryPos() {
		return this.pantryPos;
	}

	private @Nullable Container getPantry(ServerLevel level) {
		if (this.pantryPos == null || !level.isLoaded(this.pantryPos)) {
			return null;
		}
		BlockEntity blockEntity = level.getBlockEntity(this.pantryPos);
		return blockEntity instanceof Container container ? container : null;
	}

	private static int countFood(Container container) {
		int food = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.has(DataComponents.FOOD)) {
				food += stack.getCount();
			}
		}
		return food;
	}

	/**
	 * Every half second: did the food in her pantry drop while a real player she doesn't trust had it open
	 * (and she was close enough to notice)? Did it come back?
	 */
	private void watchPantry() {
		ServerLevel level = this.player.level();
		Container pantry = this.getPantry(level);
		if (pantry == null) {
			return;
		}
		int food = countFood(pantry);
		if (this.lastPantryFood >= 0 && food < this.lastPantryFood) {
			ServerPlayer culprit = this.findPlayerUsing(level, pantry);
			boolean watching = this.player.position().distanceTo(this.pantryPos.getCenter()) <= PANTRY_WATCH_DISTANCE;
			if (culprit != null && watching && !this.trustsPlayer(culprit) && !culprit.isCreative() && !this.baby) {
				if (!this.isAngry() || this.foodBeforeTheft < 0) {
					this.foodBeforeTheft = this.lastPantryFood;
				}
				this.thief = culprit.getUUID();
				this.becomeAngryAt(culprit, ANGER_TICKS);
				this.playVoice();
				this.say(culprit, "Hey! That's my food!");
				ModAdvancements.award(culprit, ModAdvancements.HANDS_OFF_THE_PANTRY);
			}
		}
		// Putting the food back calms her down instantly.
		if (this.isAngry() && this.foodBeforeTheft >= 0 && food >= this.foodBeforeTheft) {
			ServerPlayer returner = this.thief == null ? null : level.getServer().getPlayerList().getPlayer(this.thief);
			this.calmDown();
			this.foodBeforeTheft = -1;
			this.thief = null;
			if (returner != null) {
				this.say(returner, "...oh. thank you. we're okay.");
				ModAdvancements.award(returner, ModAdvancements.PEACE_OFFERING);
			}
		}
		this.lastPantryFood = food;
	}

	/** The real player who has this exact chest open right now (or is standing at it). */
	private @Nullable ServerPlayer findPlayerUsing(ServerLevel level, Container pantry) {
		ServerPlayer nearest = null;
		for (ServerPlayer player : level.players()) {
			if (!LoafIds.isRealPlayer(player)) {
				continue;
			}
			if (player.containerMenu instanceof ChestMenu menu && menu.getContainer() == pantry) {
				return player;
			}
			if (player.position().distanceTo(this.pantryPos.getCenter()) < 5.0) {
				nearest = player;
			}
		}
		return nearest;
	}

	@Override
	public void calmDown() {
		super.calmDown();
		this.foodBeforeTheft = -1;
	}

	// ---------------------------------------------------------------- routine

	@Override
	protected void tickBehavior() {
		if (this.ticks % 10 == 0) {
			this.watchPantry();
		}
		if (this.pickupTicks > 0) {
			this.pickupTicks--;
		}
		if (this.jobCooldown > 0) {
			this.jobCooldown--;
		}
		if (this.target != null || this.anger > 0) {
			this.stopJob();
		}
		super.tickBehavior();
	}

	@Override
	protected void tickCalm() {
		// Keeps about 3 blocks from players she doesn't know, but doesn't run away.
		Player stranger = this.nearestStranger(KEEP_DISTANCE);
		if (stranger != null) {
			this.endIdle();
			this.stopJob();
			if (!this.nav.isMoving()) {
				this.moveAwayFrom(stranger.position(), KEEP_DISTANCE + 1.5, Navigator.Speed.WALK);
			}
			return;
		}
		if (this.goToBed() || this.returnHome()) {
			this.stopJob();
			return;
		}
		if (this.tickRoutine() || this.tickJob() || this.tickIdle() || this.tickIdleActions()) {
			return;
		}
		this.wander(this.variant == CharacterVariant.BAKER ? 3.0 : HOME_RADIUS);
		this.lookAround(6.0);
	}

	@Override
	protected void tickCalmWithFriend() {
		if (!this.tickJob()) {
			this.idleWithOwner();
		}
	}

	// ---------------------------------------------------------------- jobs

	/** Her type's job. True while busy with it. */
	private boolean tickJob() {
		return switch (this.variant) {
			case FARMER -> this.farm();
			case BEEKEEPER -> this.collectHoney();
			case FISHER -> this.fish();
			default -> false;
		};
	}

	private void stopJob() {
		if (this.variant == CharacterVariant.FISHER) {
			// Reel in before doing anything else.
			this.fishing.stop();
		}
		this.jobPos = null;
		this.jobStep = 0;
	}

	private BlockPos workCenter() {
		return this.home != null ? this.home : this.player.blockPosition();
	}

	/** Farmer: break a fully grown crop, wait for the drops, plant the right seed back on the farmland. */
	private boolean farm() {
		if (this.jobPos == null) {
			if (this.jobCooldown > 0 || this.ticks % 20 != 0) {
				return false;
			}
			this.jobPos = this.findBlock(this.workCenter(), 12, 3, state -> state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state));
			this.jobStep = 0;
			if (this.jobPos == null) {
				this.jobCooldown = 200;
				return false;
			}
		}
		ServerLevel level = this.player.level();
		switch (this.jobStep) {
			case 0 -> {
				BlockState state = level.getBlockState(this.jobPos);
				if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) {
					this.stopJob();
					return false;
				}
				if (!this.canReach(this.jobPos)) {
					this.nav.moveTo(this.jobPos, 2.0, Navigator.Speed.WALK);
					return true;
				}
				this.nav.stop();
				this.replant = seedFor(state.getBlock());
				this.hitBlock(this.jobPos);
				this.jobStep = 1;
				this.jobTimer = 15;
			}
			case 1 -> {
				// Step onto the spot to collect the drops.
				this.nav.moveTo(this.jobPos, 0.4, Navigator.Speed.WALK);
				if (--this.jobTimer <= 0) {
					this.jobStep = 2;
				}
			}
			case 2 -> {
				BlockPos farmland = this.jobPos.below();
				if (level.getBlockState(this.jobPos).isAir() && level.getBlockState(farmland).is(Blocks.FARMLAND) && this.replant != null) {
					Item seed = this.replant;
					if (!this.canReach(farmland)) {
						this.nav.moveTo(this.jobPos, 2.0, Navigator.Speed.WALK);
						return true;
					}
					this.useItemOn(farmland, Direction.UP, stack -> stack.is(seed));
				}
				this.jobCooldown = 30;
				this.stopJob();
				this.controls.selectSlot(CALM_SLOT);
			}
			default -> this.stopJob();
		}
		return true;
	}

	private static Item seedFor(Block crop) {
		if (crop == Blocks.CARROTS) {
			return Items.CARROT;
		}
		if (crop == Blocks.POTATOES) {
			return Items.POTATO;
		}
		if (crop == Blocks.BEETROOTS) {
			return Items.BEETROOT_SEEDS;
		}
		return Items.WHEAT_SEEDS;
	}

	/** Beekeeper: right-click a full beehive with a glass bottle. */
	private boolean collectHoney() {
		if (this.jobPos == null) {
			if (this.jobCooldown > 0 || this.ticks % 20 != 0) {
				return false;
			}
			this.jobPos = this.findBlock(this.workCenter(), 12, 4, state -> state.is(BlockTags.BEEHIVES)
					&& state.hasProperty(BeehiveBlock.HONEY_LEVEL) && state.getValue(BeehiveBlock.HONEY_LEVEL) >= BeehiveBlock.MAX_HONEY_LEVELS);
			if (this.jobPos == null) {
				this.jobCooldown = 300;
				return false;
			}
		}
		BlockState state = this.player.level().getBlockState(this.jobPos);
		if (!state.is(BlockTags.BEEHIVES) || state.getValue(BeehiveBlock.HONEY_LEVEL) < BeehiveBlock.MAX_HONEY_LEVELS) {
			this.stopJob();
			return false;
		}
		if (!this.canReach(this.jobPos)) {
			this.nav.moveTo(this.jobPos, 2.0, Navigator.Speed.WALK);
			return true;
		}
		this.nav.stop();
		Vec3 offset = this.player.getEyePosition().subtract(Vec3.atCenterOf(this.jobPos));
		Direction face = Direction.getApproximateNearest(offset.x, offset.y, offset.z);
		this.useItemOn(this.jobPos, face, stack -> stack.is(Items.GLASS_BOTTLE));
		this.jobCooldown = 60;
		this.jobPos = null;
		return true;
	}

	/** Fisher: stand by the water, cast, wait for a bite, reel in (the catch flies to her and she picks it up). */
	private boolean fish() {
		return this.fishing.tick(this.workCenter(), CALM_SLOT);
	}

	/**
	 * Work hours: her job (farming, honey, fishing), the baker works at her oven, and otherwise she tends the garden
	 * and organizes her pantry. Merchants stay at their stall.
	 */
	@Override
	protected boolean doWork() {
		if (this.tickJob()) {
			return true;
		}
		return switch (this.variant) {
			case MERCHANT -> false;
			case BAKER -> this.activities.useStation(state -> state.is(Blocks.FURNACE) || state.is(Blocks.SMOKER)
					|| state.is(Blocks.CRAFTING_TABLE), 6);
			default -> this.tickIdle(this.random.nextBoolean() ? Idle.TEND_GARDEN : Idle.CHECK_PANTRY);
		};
	}

	/** Friends time: cousin Lily first, any other friend otherwise. */
	@Override
	protected boolean socialize() {
		return this.tickIdle(Idle.VISIT_LILY) || this.activities.visitFriend();
	}

	/** Tend the garden, check the pantry, visit cousin Lily, or walk up to her friend. True while busy. */
	private boolean tickIdle() {
		return this.tickIdle(null);
	}

	/** With {@code forced}: start that one (the routine asked for it) instead of a random one now and then. */
	private boolean tickIdle(@Nullable Idle forced) {
		if (this.idle == Idle.NONE) {
			if (forced != null) {
				if (forced != Idle.VISIT_LILY && this.random.nextInt(40) != 0) {
					this.lookAround(6.0);
					return true;
				}
				this.idle = forced;
			} else {
				if (this.random.nextInt(200) != 0) {
					return false;
				}
				Idle[] options = {Idle.TEND_GARDEN, Idle.CHECK_PANTRY, Idle.VISIT_LILY, Idle.VISIT_FRIEND};
				this.idle = options[this.random.nextInt(options.length)];
			}
			this.spot = null;
			this.buddy = null;
			switch (this.idle) {
				case TEND_GARDEN -> this.spot = this.findBlock(this.workCenter(), 12, 3, state -> state.is(BlockTags.CROPS));
				case CHECK_PANTRY -> this.spot = this.pantryPos;
				case VISIT_LILY -> {
					for (LoafCharacter other : CharacterManager.active()) {
						if (other.kind() == CharacterKind.LILY && other.player.level() == this.player.level()
								&& other.player.distanceTo(this.player) < 48.0) {
							this.buddy = other.player;
							break;
						}
					}
				}
				case VISIT_FRIEND -> this.buddy = this.player.level().getNearestPlayer(this.player.getX(), this.player.getY(),
						this.player.getZ(), 16.0, p -> p != this.player && p instanceof Player pl && this.trustsPlayer(pl));
				default -> {
				}
			}
			if (this.spot == null && this.buddy == null) {
				this.idle = Idle.NONE;
				return false;
			}
			this.idleTicks = this.idle == Idle.VISIT_LILY ? 400 : 160;
		}
		if (--this.idleTicks <= 0) {
			this.endIdle();
			return false;
		}
		if (this.spot != null) {
			double distance = this.player.position().distanceTo(this.spot.getCenter());
			this.controls.lookAt(Vec3.atCenterOf(this.spot));
			if (distance > 2.5) {
				this.nav.moveTo(this.spot, 2.0, Navigator.Speed.WALK);
			} else {
				this.nav.stop();
				if (this.idle == Idle.CHECK_PANTRY) {
					// Really opens the chest (right-click), looks inside, closes it again.
					if (!this.pantryOpen && this.idleTicks % 40 == 0) {
						this.controls.aimAt(Vec3.atCenterOf(this.spot));
						this.controls.useOnce();
						this.pantryOpen = true;
					} else if (this.pantryOpen && this.idleTicks % 40 == 20) {
						this.player.closeContainer();
						this.pantryOpen = false;
					}
				} else if (this.idleTicks % 30 == 0) {
					this.player.swing(InteractionHand.MAIN_HAND);
				}
			}
		} else if (this.buddy != null && this.buddy.isAlive()) {
			this.controls.lookAt(this.buddy.getEyePosition());
			if (this.player.distanceTo(this.buddy) > 2.5) {
				this.nav.follow(this.buddy, 2.0, Navigator.Speed.WALK);
			} else {
				this.nav.stop();
				if (this.idle == Idle.VISIT_FRIEND && this.idleTicks % 60 == 0) {
					this.playVoice();
				}
			}
		} else {
			this.endIdle();
			return false;
		}
		return true;
	}

	@Override
	protected String[] chatterLines() {
		return switch (this.variant) {
			case BAKER -> new String[]{"fresh bread coming up!", "*kneads dough*", "the oven's nice and hot", "who wants a cookie?"};
			case FARMER -> new String[]{"these carrots won't plant themselves!", "*wipes her forehead*", "rain would be nice", "harvest time soon"};
			case BEEKEEPER -> new String[]{"the bees are extra busy today", "honey's almost ready", "*buzz buzz*", "don't swat them!"};
			case FISHER, MERCHANT -> new String[]{"nice day for it!", "business is good", "*hums*", "come back anytime!"};
			default -> new String[]{"need to restock the pantry", "anyone hungry?", "Lily's probably reading again", "*hums a farm song*"};
		};
	}

	@Override
	protected @Nullable String[] chatWith(LoafBrain other) {
		return switch (other.kind()) {
			case LILY -> new String[]{"hey Lily! I baked something for you", "cousin time!", "your flowers look amazing, Lily"};
			case BREADLOAF -> new String[]{"don't touch my pantry, ItzBreadLoaf", "want some bread? ask nicely", "hey you"};
			case MADISON -> new String[]{"nice hat!", "how's your farm doing?"};
			default -> null;
		};
	}

	private void endIdle() {
		if (this.pantryOpen) {
			this.player.closeContainer();
			this.pantryOpen = false;
		}
		this.idle = Idle.NONE;
		this.spot = null;
		this.buddy = null;
	}

	@Override
	public void onDeactivated() {
		this.endIdle();
		super.onDeactivated();
	}

	// ---------------------------------------------------------------- saving

	@Override
	public void save(CompoundTag tag) {
		super.save(tag);
		if (this.pantryPos != null) {
			tag.store("Pantry", BlockPos.CODEC, this.pantryPos);
		}
		tag.putInt("FoodBeforeTheft", this.foodBeforeTheft);
		if (this.thief != null) {
			tag.store("Thief", UUIDUtil.CODEC, this.thief);
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.pantryPos = tag.read("Pantry", BlockPos.CODEC).orElse(null);
		this.foodBeforeTheft = tag.getIntOr("FoodBeforeTheft", -1);
		this.thief = tag.read("Thief", UUIDUtil.CODEC).orElse(null);
	}

	// ---------------------------------------------------------------- personality (AI overhaul part 6)

	/** Madison: cheerful, energetic, protective farmer. */
	@Override
	protected String[] lines(com.itzbreadloaf.brain.ai.Situation situation) {
		return switch (situation) {
			case GREET_PLAYER -> new String[]{"hey hey! welcome back!", "hiii! how's it going?", "oh, it's you! hi!"};
			case GREET_FRIEND -> new String[]{"hey %s!", "%s!! hi hi!", "look who it is, %s!"};
			case FIGHT_START -> new String[]{"back off!", "you're in big trouble!", "not on my farm!"};
			case RIVAL -> new String[]{"hey!"};
			case FIGHT_WON -> new String[]{"and don't come back!", "hmph! that'll teach you"};
			case LOW_HEALTH -> new String[]{"ouch! okay, okay!", "I need a snack, fast!"};
			case DEFEND_FRIEND -> new String[]{"hey! leave my cousin alone!", "nobody hurts my friends!"};
			case SCARED_FOR_FRIEND -> new String[]{"hey, are you okay?!", "oh no!"};
			case REST -> new String[]{"*wipes her forehead* phew", "need a minute..."};
			case MORNING -> new String[]{"morning! the chickens are up!", "time to feed the animals!"};
			case NIGHT -> new String[]{"night already? gotta lock up the barn", "*yawns* bedtime"};
			case RAIN -> new String[]{"good for the crops!", "rain! the garden will love this"};
			case ITEM -> new String[]{"ooh, %s! nice!", "want to trade that %s?", "where'd you get the %s?"};
			case WATCH -> new String[]{"need any help?", "looking good!"};
		};
	}

	@Override
	protected String[] moodLines(com.itzbreadloaf.brain.ai.Mood mood) {
		return switch (mood) {
			case HAPPY -> new String[]{"*hums a farm song*", "what a day!"};
			case BORED -> new String[]{"the pantry needs sorting...", "*drums her fingers*"};
			case TIRED -> new String[]{"*yawns* long day", "my feet hurt..."};
			case CURIOUS -> new String[]{"ooh, what's this?", "huh?"};
			case SCARED -> new String[]{"yikes!", "eep!"};
			case ANGRY -> new String[]{"okay, now I'm mad!", "that's it!"};
		};
	}
}
