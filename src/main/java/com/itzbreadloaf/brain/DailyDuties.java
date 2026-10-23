package com.itzbreadloaf.brain;

import com.itzbreadloaf.ModMessages;
import com.itzbreadloaf.item.ModItems;
import com.itzbreadloaf.sound.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A tamed ItzBreadLoaf's daily routine with his owner:
 * <ul>
 *   <li>Daily gift: each Minecraft morning, when the owner is within 12 blocks, he tosses them a small gift.</li>
 *   <li>Daily mission: a bit later he asks for some items ("bring me 16 wheat"). Right-click him holding them to
 *   hand them over and get a reward. Sneak + right-click with an empty hand repeats the mission. A new one comes
 *   every day (an unfinished one is replaced).</li>
 * </ul>
 */
final class DailyDuties {
	private static final double OWNER_RANGE = 12.0;
	/** The gift can come any time in the morning (first 4000 ticks of the day, about 3 minutes 20 seconds). */
	private static final long MORNING_END = 4000L;
	/** Ticks between the gift and the mission (10 seconds). */
	private static final int MISSION_DELAY = 200;

	private record Mission(Item item, int min, int max, String line) {
	}

	private static final Mission[] MISSIONS = {
			new Mission(Items.WHEAT, 12, 24, "I need wheat for bread. Bring me %d wheat!"),
			new Mission(Items.COAL, 8, 16, "My furnace is cold... bring me %d coal?"),
			new Mission(Items.IRON_INGOT, 4, 8, "Gonna make some armor. I need %d iron ingots."),
			new Mission(Items.GOLD_INGOT, 2, 5, "Shiny stuff! Bring me %d gold ingots."),
			new Mission(Items.STRING, 4, 10, "My bow string snapped. Get me %d string!"),
			new Mission(Items.BONE, 4, 10, "Skeletons have been bugging me. Bring me %d bones as proof!"),
			new Mission(Items.ROTTEN_FLESH, 8, 16, "Zombie hunt! Bring me %d rotten flesh."),
			new Mission(Items.GUNPOWDER, 2, 5, "Creepers... I need %d gunpowder. Don't ask why."),
			new Mission(Items.OAK_LOG, 12, 24, "Building a new base. I need %d oak logs!"),
			new Mission(Items.COBBLESTONE, 32, 64, "Help me build a wall: %d cobblestone!"),
			new Mission(Items.SUGAR_CANE, 6, 12, "I want to make paper. Bring me %d sugar cane."),
			new Mission(Items.COOKED_BEEF, 3, 6, "I'm SO hungry. Bring me %d steak!"),
	};

	private final BreadLoafBrain brain;
	private long lastGiftDay = -1;
	private long missionDay = -1;
	private @Nullable Item missionItem;
	private int missionCount;
	private boolean missionDone;
	private int missionDueTick = -1;

	DailyDuties(BreadLoafBrain brain) {
		this.brain = brain;
	}

	/** Called every tick for a tamed, grown-up ItzBreadLoaf that isn't fighting. */
	void tick() {
		ServerPlayer owner = this.brain.getOwnerPlayer();
		if (owner == null || owner.level() != this.brain.player.level() || this.brain.player.isSleeping()) {
			return;
		}
		boolean near = owner.distanceTo(this.brain.player) < OWNER_RANGE && this.brain.player.hasLineOfSight(owner);
		long time = this.brain.player.level().getDayTime();
		long day = time / 24000L;
		if (near && day != this.lastGiftDay && time % 24000L < MORNING_END) {
			this.lastGiftDay = day;
			this.giveGift(owner);
			this.missionDueTick = this.brain.ticks + MISSION_DELAY;
		}
		if (near && day != this.missionDay && this.missionDueTick >= 0 && this.brain.ticks >= this.missionDueTick) {
			this.missionDueTick = -1;
			this.missionDay = day;
			this.newMission(owner);
		}
	}

	private void giveGift(ServerPlayer owner) {
		RandomSource random = this.brain.random;
		int roll = random.nextInt(100);
		ItemStack gift;
		if (roll < 30) {
			gift = new ItemStack(ModItems.LOAF_BREAD, 2 + random.nextInt(3));
		} else if (roll < 50) {
			gift = new ItemStack(Items.COOKED_BEEF, 2 + random.nextInt(3));
		} else if (roll < 65) {
			gift = new ItemStack(Items.IRON_INGOT, 2 + random.nextInt(4));
		} else if (roll < 75) {
			gift = new ItemStack(Items.EMERALD, 1 + random.nextInt(3));
		} else if (roll < 83) {
			gift = new ItemStack(Items.ENDER_PEARL, 1 + random.nextInt(2));
		} else if (roll < 90) {
			gift = new ItemStack(Items.GOLDEN_APPLE);
		} else if (roll < 96) {
			gift = new ItemStack(Items.WIND_CHARGE, 4 + random.nextInt(5));
		} else {
			gift = new ItemStack(Items.DIAMOND);
		}
		this.toss(owner, gift);
		this.brain.playSound(ModSounds.GIVES_GIFT, 1.0F, 1.0F);
		this.brain.particles(ParticleTypes.HEART, 5);
		ModMessages.say(owner, this.brain.player, ModMessages.pick(random,
				"good morning! got you something.", "here, take this. don't say I never gave you anything.",
				"found this lying around, it's yours.", "morning gift! you're welcome."));
	}

	private void newMission(ServerPlayer owner) {
		Mission mission = MISSIONS[this.brain.random.nextInt(MISSIONS.length)];
		this.missionItem = mission.item();
		this.missionCount = mission.min() + this.brain.random.nextInt(mission.max() - mission.min() + 1);
		this.missionDone = false;
		this.brain.playSound(ModSounds.DAILY_MISSION, 1.0F, 1.0F);
		ModMessages.say(owner, this.brain.player, String.format(mission.line(), this.missionCount));
		owner.sendSystemMessage(Component.literal("Daily mission: bring " + this.missionCount + " " + this.itemName()
				+ ". Right-click him holding them. (Sneak + right-click with an empty hand to see it again.)").withStyle(ChatFormatting.GOLD));
	}

	/** Shows the current mission. True if there was one to show. */
	boolean remind(ServerPlayer owner) {
		if (this.missionItem == null) {
			owner.sendSystemMessage(Component.literal("No mission yet. He hands one out each morning.").withStyle(ChatFormatting.GRAY));
			return true;
		}
		if (this.missionDone) {
			owner.sendSystemMessage(Component.literal("Today's mission is done! Come back tomorrow morning.").withStyle(ChatFormatting.GREEN));
		} else {
			owner.sendSystemMessage(Component.literal("Daily mission: bring " + this.missionCount + " " + this.itemName() + ".")
					.withStyle(ChatFormatting.GOLD));
		}
		return true;
	}

	/** The owner right-clicked holding {@code stack}. True if it was for the mission (handled). */
	boolean tryComplete(ServerPlayer owner, ItemStack stack) {
		if (this.missionItem == null || this.missionDone || !stack.is(this.missionItem)) {
			return false;
		}
		int have = owner.getInventory().countItem(this.missionItem);
		if (have < this.missionCount) {
			owner.sendSystemMessage(Component.literal("You need " + this.missionCount + " " + this.itemName() + " (you have " + have + ").")
					.withStyle(ChatFormatting.RED));
			return true;
		}
		// Take them from the inventory (the stack in hand first).
		int left = this.missionCount;
		int fromHand = Math.min(left, stack.getCount());
		stack.shrink(fromHand);
		left -= fromHand;
		var inventory = owner.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize() && left > 0; slot++) {
			ItemStack other = inventory.getItem(slot);
			if (other.is(this.missionItem)) {
				int take = Math.min(left, other.getCount());
				other.shrink(take);
				left -= take;
			}
		}
		this.missionDone = true;
		this.brain.playSound(ModSounds.GIVEN_GIFT, 1.0F, 1.0F);
		this.brain.particles(ParticleTypes.HAPPY_VILLAGER, 10);
		this.toss(owner, this.reward());
		ExperienceOrb.award(owner.level(), owner.position(), 15 + this.brain.random.nextInt(16));
		ModMessages.say(owner, this.brain.player, ModMessages.pick(this.brain.random,
				"you actually did it! here's your reward.", "mission complete! you're the best.", "GG. take this."));
		return true;
	}

	private ItemStack reward() {
		RandomSource random = this.brain.random;
		int roll = random.nextInt(100);
		if (roll < 35) {
			return new ItemStack(Items.EMERALD, 3 + random.nextInt(5));
		} else if (roll < 55) {
			return new ItemStack(Items.GOLDEN_APPLE, 1 + random.nextInt(2));
		} else if (roll < 70) {
			return new ItemStack(Items.DIAMOND, 1 + random.nextInt(2));
		} else if (roll < 82) {
			return new ItemStack(Items.ENDER_PEARL, 2 + random.nextInt(3));
		} else if (roll < 92) {
			return new ItemStack(Items.EXPERIENCE_BOTTLE, 4 + random.nextInt(5));
		} else if (roll < 98) {
			return new ItemStack(Items.WIND_CHARGE, 8 + random.nextInt(9));
		}
		return new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
	}

	/** Throws an item to the owner. Only the owner can pick it up, so he doesn't grab it back. */
	private void toss(ServerPlayer owner, ItemStack stack) {
		var self = this.brain.player;
		ItemEntity item = new ItemEntity(self.level(), self.getX(), self.getEyeY() - 0.3, self.getZ(), stack);
		Vec3 toward = owner.position().subtract(self.position());
		Vec3 motion = toward.lengthSqr() > 0.01 ? toward.normalize().scale(0.3) : Vec3.ZERO;
		item.setDeltaMovement(motion.x, 0.25, motion.z);
		item.setPickUpDelay(10);
		item.setTarget(owner.getUUID());
		self.level().addFreshEntity(item);
		this.brain.controls.lookAt(owner.getEyePosition());
	}

	private String itemName() {
		return this.missionItem == null ? "?" : new ItemStack(this.missionItem).getHoverName().getString().toLowerCase(java.util.Locale.ROOT);
	}

	void save(CompoundTag tag) {
		tag.putLong("GiftDay", this.lastGiftDay);
		tag.putLong("MissionDay", this.missionDay);
		if (this.missionItem != null) {
			tag.putString("MissionItem", BuiltInRegistries.ITEM.getKey(this.missionItem).toString());
			tag.putInt("MissionCount", this.missionCount);
			tag.putBoolean("MissionDone", this.missionDone);
		}
	}

	void load(CompoundTag tag) {
		this.lastGiftDay = tag.getLongOr("GiftDay", -1L);
		this.missionDay = tag.getLongOr("MissionDay", -1L);
		String id = tag.getStringOr("MissionItem", "");
		Identifier key = id.isEmpty() ? null : Identifier.tryParse(id);
		this.missionItem = key == null ? null : BuiltInRegistries.ITEM.getOptional(key).orElse(null);
		this.missionCount = tag.getIntOr("MissionCount", 0);
		this.missionDone = tag.getBooleanOr("MissionDone", false);
	}
}
