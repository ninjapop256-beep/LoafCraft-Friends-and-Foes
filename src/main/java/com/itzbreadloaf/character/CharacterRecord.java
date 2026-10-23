package com.itzbreadloaf.character;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Everything about one character that survives while they are despawned: where they are, their real player
 * inventory (every slot, armor and offhand included), health, hunger, potion effects, and their brain's data
 * (owner, home, bed, friendship, trust, baby age...). Stored in {@link LoafWorldData}, never in player files.
 */
public final class CharacterRecord {
	/** One non-empty inventory slot (0-35 main, 36-39 armor, 40 offhand). */
	public record Slot(int slot, ItemStack stack) {
		public static final Codec<Slot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("slot").forGetter(Slot::slot),
				ItemStack.CODEC.fieldOf("item").forGetter(Slot::stack)
		).apply(instance, Slot::new));
	}

	public static final Codec<CharacterRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUIDUtil.CODEC.fieldOf("id").forGetter(r -> r.id),
			Codec.STRING.fieldOf("kind").forGetter(r -> r.kind.id),
			Codec.INT.fieldOf("index").forGetter(r -> r.index),
			Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(r -> r.dimension),
			Vec3.CODEC.fieldOf("pos").forGetter(r -> r.pos),
			Codec.FLOAT.fieldOf("yaw").forGetter(r -> r.yaw),
			Codec.FLOAT.fieldOf("pitch").forGetter(r -> r.pitch),
			Codec.FLOAT.fieldOf("health").forGetter(r -> r.health),
			Codec.INT.fieldOf("food").forGetter(r -> r.food),
			Codec.FLOAT.fieldOf("saturation").forGetter(r -> r.saturation),
			Slot.CODEC.listOf().fieldOf("inventory").forGetter(r -> r.inventory),
			Codec.INT.fieldOf("selected").forGetter(r -> r.selectedSlot),
			MobEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(r -> r.effects),
			CompoundTag.CODEC.fieldOf("brain").forGetter(r -> r.brain)
	).apply(instance, CharacterRecord::new));

	public final UUID id;
	public final CharacterKind kind;
	public final int index;
	public ResourceKey<Level> dimension;
	public Vec3 pos;
	public float yaw;
	public float pitch;
	/** 0 means "full health" (a brand-new character). */
	public float health;
	public int food;
	public float saturation;
	public List<Slot> inventory;
	public int selectedSlot;
	public List<MobEffectInstance> effects;
	public CompoundTag brain;

	private CharacterRecord(UUID id, String kind, int index, ResourceKey<Level> dimension, Vec3 pos, float yaw, float pitch,
			float health, int food, float saturation, List<Slot> inventory, int selectedSlot, List<MobEffectInstance> effects,
			CompoundTag brain) {
		this.id = id;
		CharacterKind parsed = CharacterKind.byId(kind);
		this.kind = parsed == null ? CharacterKind.BREADLOAF : parsed;
		this.index = index;
		this.dimension = dimension;
		this.pos = pos;
		this.yaw = yaw;
		this.pitch = pitch;
		this.health = health;
		this.food = food;
		this.saturation = saturation;
		this.inventory = new ArrayList<>(inventory);
		this.selectedSlot = selectedSlot;
		this.effects = new ArrayList<>(effects);
		this.brain = brain.copy();
	}

	public static CharacterRecord create(CharacterVariant variant, int index, ResourceKey<Level> dimension, Vec3 pos, float yaw) {
		return new CharacterRecord(LoafIds.uuid(variant, index), variant.kind.id, index, dimension, pos, yaw, 0.0F, 0.0F, 20, 5.0F,
				List.of(), 0, List.of(), new CompoundTag());
	}

	public CharacterVariant variant() {
		CharacterVariant variant = LoafIds.variant(this.id);
		return variant == null ? CharacterVariant.base(this.kind) : variant;
	}

	public String profileName() {
		return LoafIds.profileName(this.kind, this.index);
	}
}
