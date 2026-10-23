package com.itzbreadloaf.brain;

import com.itzbreadloaf.ModAdvancements;
import com.itzbreadloaf.character.CharacterVariant;
import com.itzbreadloaf.character.LoafCharacter;
import com.itzbreadloaf.item.ModItems;
import com.itzbreadloaf.sound.ModSounds;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * ImLilyLol and her types: very shy and neutral. Never attacks first and keeps away from players she doesn't
 * trust (turns and walks off within 6 blocks, sprints home and hides if someone sprints at her), but hits very hard
 * (12 with her iron sword) for 30 seconds if attacked. Trusts a player after 5 gifts given while crouching.
 *
 * Types:
 * - Bookworm: reads a book (crouched, looking down) instead of looking at flowers; likes paper, leather and books;
 *   gives her new friend an enchanted book. Lives in a treehouse library.
 * - Gardener: plants flowers around her home and uses bone meal on the grass (real right-clicks).
 * - Witchy: throws Slowness and Weakness potions before she fights, and Healing at her friend (or herself) when hurt.
 * - Archer: shoots with a bow from range, sword up close. Guards LoafCraft villages.
 */
public class LilyBrain extends NeutralBrain {
	public static final int GIFTS_TO_TRUST = 5;
	public static final int ANGER_TICKS = 600;
	public static final float VOICE_VOLUME = 0.6F;
	public static final float VOICE_PITCH = 1.2F;
	public static final int HOME_RADIUS = 8;
	public static final double AVOID_DISTANCE = 6.0;
	public static final double SCARE_DISTANCE = 14.0;

	private static final Item[] CALM_FLOWERS = {
			Items.PINK_TULIP, Items.ALLIUM, Items.CORNFLOWER, Items.OXEYE_DAISY, Items.LILY_OF_THE_VALLEY, Items.PEONY
	};

	private enum Idle { NONE, SIT, LOOK_AT_FLOWER, TOGGLE_FLOWER, READ, PLANT, VISIT_MADISON }

	private Idle idle = Idle.NONE;
	private int idleTicks;
	private @Nullable BlockPos flower;
	private @Nullable Player scary;
	/** Madison, while Lily is visiting her. */
	private @Nullable Player cousin;
	private double scaryLastDistance = -1;
	private int hideTicks;
	private int potionCooldown;
	private int plantsDone;

	public LilyBrain(LoafCharacter character) {
		super(character);
		// Defensive until provoked, then she backs off early to heal.
		this.combat.retreatBelow = (float) com.itzbreadloaf.config.ModConfig.get().cousinsRetreatHealth;
	}

	@Override
	protected double baseMaxHealth() {
		return com.itzbreadloaf.config.ModConfig.get().lilyHealth;
	}

	/** 7 + iron sword (+5) = 12 damage per hit. */
	@Override
	protected double baseAttackDamage() {
		return 7.0;
	}

	@Override
	protected void rollGear() {
		Kits.lily(this.player, this.player.level(), this.variant, this.random);
	}

	@Override
	protected void restock() {
		if (this.baby) {
			return;
		}
		if (Inv.count(this.player, Inv::isFood) < 4) {
			Inv.give(this.player, new ItemStack(Items.COOKIE, 8));
			Inv.give(this.player, new ItemStack(Items.SWEET_BERRIES, 8));
		}
		if (this.variant == CharacterVariant.GARDENER) {
			if (Inv.count(this.player, stack -> stack.is(Items.BONE_MEAL)) < 8) {
				Inv.give(this.player, new ItemStack(Items.BONE_MEAL, 16));
			}
			if (Inv.count(this.player, stack -> stack.is(ItemTags.SMALL_FLOWERS)) < 4) {
				Inv.give(this.player, new ItemStack(randomFlower(this.random), 8));
			}
		} else if (this.variant == CharacterVariant.WITCHY) {
			if (Inv.count(this.player, stack -> isPotion(stack, Items.SPLASH_POTION, Potions.STRONG_HEALING)) < 2) {
				Inv.give(this.player, potion(Items.SPLASH_POTION, Potions.STRONG_HEALING));
				Inv.give(this.player, potion(Items.SPLASH_POTION, Potions.STRONG_HEALING));
			}
			if (Inv.count(this.player, stack -> isPotion(stack, Items.SPLASH_POTION, Potions.STRONG_SLOWNESS)) < 1) {
				Inv.give(this.player, potion(Items.SPLASH_POTION, Potions.STRONG_SLOWNESS));
			}
			if (Inv.count(this.player, stack -> isPotion(stack, Items.SPLASH_POTION, Potions.WEAKNESS)) < 1) {
				Inv.give(this.player, potion(Items.SPLASH_POTION, Potions.WEAKNESS));
			}
		} else if (this.variant == CharacterVariant.ARCHER_LILY && Inv.count(this.player, stack -> stack.is(ItemTags.ARROWS)) < 16) {
			Inv.give(this.player, new ItemStack(Items.ARROW, 32));
		}
	}

	public static Item randomFlower(RandomSource random) {
		return CALM_FLOWERS[random.nextInt(CALM_FLOWERS.length)];
	}

	// ---------------------------------------------------------------- personality settings

	@Override
	public int giftsToTrust() {
		return GIFTS_TO_TRUST;
	}

	@Override
	public boolean isTrustGift(ItemStack stack) {
		if (this.variant == CharacterVariant.BOOKWORM && (stack.is(Items.PAPER) || stack.is(Items.LEATHER) || stack.is(Items.ENCHANTED_BOOK))) {
			return true;
		}
		if (this.variant == CharacterVariant.WITCHY && (stack.is(Items.GLOWSTONE_DUST) || stack.is(Items.NETHER_WART) || stack.is(Items.SUGAR))) {
			return true;
		}
		return isFavoriteGift(stack) || stack.is(Items.SWEET_BERRIES) || stack.is(ModItems.LOAF_BREAD);
	}

	/** Lily's favorite gifts: cake, cookies, flowers, books. */
	public static boolean isFavoriteGift(ItemStack stack) {
		return stack.is(Items.CAKE) || stack.is(Items.COOKIE) || stack.is(ItemTags.FLOWERS) || stack.is(Items.BOOK);
	}

	@Override
	protected boolean giftsNeedCrouch() {
		return true;
	}

	@Override
	protected int angerTicksOnHit() {
		return ANGER_TICKS;
	}

	@Override
	protected ItemStack createFightWeapon() {
		return new ItemStack(this.variant == CharacterVariant.ARCHER_LILY ? Items.BOW : Items.IRON_SWORD);
	}

	@Override
	protected boolean fightsAtRange() {
		return this.variant == CharacterVariant.ARCHER_LILY;
	}

	@Override
	public void playVoice() {
		this.playSound(ModSounds.LILY_VOICE, VOICE_VOLUME, VOICE_PITCH);
	}

	@Override
	protected int defaultHomeRadius() {
		return HOME_RADIUS;
	}

	@Override
	protected String cousinCriterion() {
		return "lily";
	}

	@Override
	protected void onBefriended(ServerPlayer friend) {
		this.say(friend, "...you're kind of nice. okay. friends?");
		ModAdvancements.award(friend, ModAdvancements.EARNED_HER_TRUST);
		if (this.variant == CharacterVariant.BOOKWORM) {
			// A thank-you present: one of her enchanted books.
			ItemStack book = EnchantmentHelper.enchantItem(this.random, new ItemStack(Items.BOOK), 20 + this.random.nextInt(11),
					this.player.level().registryAccess(), Optional.empty());
			this.giveTo(friend, book);
			this.say(friend, "here... this one's my favorite. you can have it");
		}
	}

	@Override
	protected String[] helloLines() {
		return switch (this.variant) {
			case BOOKWORM -> new String[]{"oh... hi. I'm on a good chapter", "*looks up from her book*", "have you read this one?"};
			case GARDENER -> new String[]{"oh, hi... the tulips are blooming", "*shows you a flower*", "hi... watch the flowers"};
			case WITCHY -> new String[]{"oh... hi. careful, that one's bubbling", "*stirs something*", "hi... need a potion?"};
			case ARCHER_LILY -> new String[]{"oh... hi. all quiet out here", "*waves with her bow*", "hi... I'm on watch"};
			default -> new String[]{"oh... hi", "um... hello", "*waves quietly*", "hi again..."};
		};
	}

	@Override
	public Item breedFood() {
		return Items.CAKE;
	}

	// ---------------------------------------------------------------- fighting extras (Witchy)

	@Override
	protected boolean fightTrick(LivingEntity target) {
		if (this.potionCooldown > 0) {
			this.potionCooldown--;
		}
		if (this.variant != CharacterVariant.WITCHY || this.potionCooldown > 0) {
			return false;
		}
		if (this.player.getHealth() < this.player.getMaxHealth() * 0.4F && this.throwSplash(this.player, Potions.STRONG_HEALING)) {
			this.potionCooldown = 60;
			return true;
		}
		double distance = this.player.distanceTo(target);
		if (distance > 2.5 && distance < 10.0) {
			boolean slowed = target.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);
			if (!slowed && this.throwSplash(target, Potions.STRONG_SLOWNESS)
					|| slowed && !target.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS) && this.throwSplash(target, Potions.WEAKNESS)) {
				this.potionCooldown = 40;
				return true;
			}
		}
		return false;
	}

	/** Witchy Lily heals her friend with a splash potion when they're hurt. */
	private boolean healFriend() {
		if (this.variant != CharacterVariant.WITCHY) {
			return false;
		}
		if (this.potionCooldown > 0) {
			this.potionCooldown--;
			return false;
		}
		ServerPlayer friend = this.getOwnerPlayer();
		if (friend == null || friend.level() != this.player.level() || friend.distanceTo(this.player) > 10.0
				|| friend.getHealth() > friend.getMaxHealth() * 0.5F || !this.player.hasLineOfSight(friend)) {
			return false;
		}
		if (this.throwSplash(friend, Potions.STRONG_HEALING)) {
			this.potionCooldown = 100;
			this.say(friend, "hold still... this'll help");
			return true;
		}
		return false;
	}

	// ---------------------------------------------------------------- shy routine

	@Override
	protected void tickCalm() {
		if (this.runHome()) {
			return;
		}
		// Turns away and walks off when a player she doesn't trust comes within 6 blocks.
		Player stranger = this.nearestStranger(AVOID_DISTANCE);
		if (stranger != null) {
			this.endIdle();
			if (!this.nav.isMoving()) {
				this.moveAwayFrom(stranger.position(), 8.0, Navigator.Speed.WALK);
			}
			return;
		}
		if (this.goToBed() || this.returnHome()) {
			return;
		}
		if (this.tickIdle()) {
			return;
		}
		this.wander(HOME_RADIUS);
	}

	/** A stranger sprinting at her: she sprints into her house and hides (crouched, facing away) for a while. */
	private boolean runHome() {
		if (this.hideTicks > 0) {
			if (this.nav.isMoving()) {
				this.hideTicks = Math.max(this.hideTicks, 40);
				return true;
			}
			this.hideTicks--;
			this.controls.setSneaking(true);
			if (this.scary != null) {
				Vec3 away = this.player.position().subtract(this.scary.position()).add(this.player.position());
				this.controls.lookAt(new Vec3(away.x, this.player.getEyeY(), away.z));
			}
			if (this.hideTicks == 0) {
				this.scary = null;
				this.controls.setSneaking(false);
			}
			return true;
		}
		Player player = this.nearestStranger(SCARE_DISTANCE);
		if (player == null || !player.isSprinting()) {
			this.scaryLastDistance = -1;
			return false;
		}
		double distance = this.player.distanceTo(player);
		boolean approaching = this.scaryLastDistance > 0 && distance < this.scaryLastDistance - 0.3;
		this.scaryLastDistance = distance;
		if (!approaching) {
			return false;
		}
		this.scary = player;
		this.hideTicks = 160;
		this.endIdle();
		if (this.home != null) {
			this.nav.moveTo(this.home, 1.0, Navigator.Speed.SPRINT);
		} else {
			this.moveAwayFrom(player.position(), 16.0, Navigator.Speed.SPRINT);
		}
		return true;
	}

	/** Lily's quiet idle actions (and her type's hobby). True while doing one. */
	private boolean tickIdle() {
		if (this.idle == Idle.NONE) {
			int chance = this.variant == CharacterVariant.GARDENER ? 120 : 300;
			if (this.random.nextInt(chance) != 0 || !this.player.onGround()) {
				return false;
			}
			this.idle = switch (this.variant) {
				case BOOKWORM -> this.random.nextInt(3) == 0 ? Idle.SIT : Idle.READ;
				case GARDENER -> this.random.nextInt(3) == 0 ? Idle.LOOK_AT_FLOWER : Idle.PLANT;
				default -> new Idle[]{Idle.SIT, Idle.LOOK_AT_FLOWER, Idle.TOGGLE_FLOWER}[this.random.nextInt(3)];
			};
			// Sometimes she goes to see cousin Madison instead, if Madison is around.
			if (this.random.nextInt(4) == 0) {
				this.cousin = this.findMadison();
				if (this.cousin != null) {
					this.idle = Idle.VISIT_MADISON;
				}
			}
			switch (this.idle) {
				case SIT, READ -> {
					this.idleTicks = 120 + this.random.nextInt(200);
					this.nav.stop();
				}
				case LOOK_AT_FLOWER -> {
					this.flower = this.findBlock(this.player.blockPosition(), 6, 2, state -> state.is(BlockTags.FLOWERS));
					this.idleTicks = this.flower == null ? 0 : 100;
					if (this.flower != null) {
						this.nav.moveTo(this.flower, 1.5, Navigator.Speed.WALK);
					}
				}
				case PLANT -> {
					// A grass block near home with room above it.
					BlockPos center = this.home != null ? this.home : this.player.blockPosition();
					this.flower = this.findBlock(center.offset(this.random.nextInt(9) - 4, 0, this.random.nextInt(9) - 4), 4, 2,
							state -> state.is(Blocks.GRASS_BLOCK));
					if (this.flower != null && !this.player.level().getBlockState(this.flower.above()).isAir()) {
						this.flower = null;
					}
					this.idleTicks = this.flower == null ? 0 : 160;
				}
				case VISIT_MADISON -> this.idleTicks = 400;
				case TOGGLE_FLOWER -> {
					// Takes out or puts away her flower: scrolls between the flower slot and an empty one.
					this.idleTicks = 1;
					int selected = this.player.getInventory().getSelectedSlot();
					this.controls.selectSlot(selected == CALM_SLOT ? 1 : CALM_SLOT);
					if (this.player.getInventory().getItem(CALM_SLOT).isEmpty()) {
						this.player.getInventory().setItem(CALM_SLOT, new ItemStack(randomFlower(this.random)));
					}
				}
				default -> {
				}
			}
		}
		if (this.idleTicks-- <= 0) {
			this.endIdle();
			return false;
		}
		switch (this.idle) {
			case SIT -> this.controls.setSneaking(true);
			case READ -> {
				// Book in hand, crouched, looking down at the pages.
				this.controls.selectSlot(CALM_SLOT);
				this.controls.setSneaking(true);
				this.controls.look(this.player.getYRot(), 55.0F);
			}
			case LOOK_AT_FLOWER -> {
				if (this.flower != null) {
					this.controls.lookAt(Vec3.atCenterOf(this.flower).add(0.0, -0.2, 0.0));
				}
			}
			case PLANT -> this.tickPlanting();
			case VISIT_MADISON -> this.tickVisit();
			default -> {
			}
		}
		return true;
	}

	private @Nullable Player findMadison() {
		for (com.itzbreadloaf.character.LoafCharacter other : com.itzbreadloaf.character.CharacterManager.active()) {
			if (other.kind() == com.itzbreadloaf.character.CharacterKind.MADISON && other.player.level() == this.player.level()
					&& other.player.distanceTo(this.player) < 48.0) {
				return other.player;
			}
		}
		return null;
	}

	/** Walks over to Madison and hangs out with her for a while (hearts and a little chat). */
	private void tickVisit() {
		Player madison = this.cousin;
		if (madison == null || !madison.isAlive() || madison.level() != this.player.level()) {
			this.idleTicks = 0;
			this.cousin = null;
			return;
		}
		this.controls.lookAt(madison.getEyePosition());
		if (this.player.distanceTo(madison) > 2.5) {
			this.nav.follow(madison, 2.0, Navigator.Speed.WALK);
		} else {
			this.nav.stop();
			if (this.idleTicks % 80 == 0) {
				this.playVoice();
				this.particles(net.minecraft.core.particles.ParticleTypes.HEART, 2);
			}
		}
		if (this.idleTicks <= 1) {
			this.cousin = null;
		}
	}

	@Override
	protected String[] chatterLines() {
		return switch (this.variant) {
			case BOOKWORM -> new String[]{"*turns a page*", "just one more chapter...", "this book is so good", "shh, I'm reading"};
			case GARDENER -> new String[]{"the tulips are blooming", "*waters the flowers*", "bees are good for gardens", "pink is the best color"};
			case WITCHY -> new String[]{"*stirs the cauldron*", "that potion smells funny...", "need more glowstone", "bubble bubble"};
			case ARCHER_LILY -> new String[]{"all quiet out here", "*checks her arrows*", "nothing gets past me", "the view is nice up here"};
			default -> new String[]{"*hums quietly*", "the cherry trees are so pretty", "maybe I'll visit Madison later",
					"I wonder what's in the next chapter...", "shh... a bee"};
		};
	}

	@Override
	protected @Nullable String[] chatWith(LoafBrain other) {
		return switch (other.kind()) {
			case MADISON -> new String[]{"Madison! want to pick flowers?", "how's the garden, Madison?", "cousin hug!"};
			case BREADLOAF -> new String[]{"um... please don't fight near my flowers", "hi ItzBreadLoaf...", "*waves shyly*"};
			case LILY -> new String[]{"oh, hi... me?", "nice dress!"};
			default -> null;
		};
	}

	/** Gardener: walk to the grass block, then bone meal it or plant a flower on it. */
	private void tickPlanting() {
		if (this.flower == null) {
			this.idleTicks = 0;
			return;
		}
		if (!this.canReach(this.flower)) {
			this.nav.moveTo(this.flower.above(), 2.0, Navigator.Speed.WALK);
			return;
		}
		this.nav.stop();
		boolean boneMeal = this.plantsDone % 3 == 2;
		boolean done = boneMeal
				? this.useItemOn(this.flower, Direction.UP, stack -> stack.is(Items.BONE_MEAL))
				: this.useItemOn(this.flower, Direction.UP, stack -> stack.is(ItemTags.SMALL_FLOWERS));
		if (done) {
			this.plantsDone++;
			this.idleTicks = 10;
			this.flower = null;
		}
	}

	private void endIdle() {
		if (this.idle == Idle.SIT || this.idle == Idle.READ) {
			this.controls.setSneaking(false);
		}
		this.idle = Idle.NONE;
		this.flower = null;
	}

	@Override
	protected void tickCalmWithFriend() {
		if (this.healFriend()) {
			return;
		}
		// No staring: she glances around instead.
		if (!this.tickIdle()) {
			this.lookAround(4.0);
		}
	}
}
