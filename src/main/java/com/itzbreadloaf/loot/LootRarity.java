package com.itzbreadloaf.loot;

import com.itzbreadloaf.ItzBreadLoaf;
import com.itzbreadloaf.brain.LoafBrain;
import com.itzbreadloaf.config.ModConfig;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * The shared loot rarity system. The item lists are plain loot tables (data/itzbreadloaf/loot_table/tiers/), and
 * every LoafCraft chest table and character drop table gets one extra pool when the game loads loot tables: a few
 * rolls that each pick a tier (Common, Uncommon, Rare, Epic, Legendary) with the odds for that table's loot level.
 * That pool is added here in code only because its odds come from the config; everything else is JSON.
 */
public final class LootRarity {
	private static final String[] TIERS = {"common", "uncommon", "rare", "epic", "legendary"};
	private static final int[][] DEFAULT_WEIGHTS = {{700, 240, 50, 9, 1}, {600, 250, 100, 40, 10}, {480, 280, 160, 65, 15}, {350, 280, 220, 110, 40}};
	private static final int[][] DEFAULT_ROLLS = {{1, 2}, {2, 3}, {2, 4}, {3, 5}};

	private LootRarity() {
	}

	public static void register() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!key.identifier().getNamespace().equals(ItzBreadLoaf.MOD_ID)) {
				return;
			}
			String path = key.identifier().getPath();
			if (path.startsWith("chests/") || path.startsWith("characters/")) {
				tableBuilder.withPool(tierPool(path));
			}
		});
	}

	/** The loot level (1-4) of a table like "chests/house" or "characters/felixsyn". */
	public static int levelOf(String path) {
		Integer level = ModConfig.get().lootLevels == null ? null : ModConfig.get().lootLevels.get(path);
		if (level == null && path.startsWith("chests/")) {
			var def = com.itzbreadloaf.worldgen.build.StructureCatalog.get(path.substring("chests/".length()));
			level = def == null ? null : def.lootLevel();
		}
		if (level == null) {
			level = LootLevels.DEFAULTS.getOrDefault(path, 1);
		}
		return Math.clamp(level, 1, 4);
	}

	private static LootPool.Builder tierPool(String path) {
		ModConfig config = ModConfig.get();
		int level = levelOf(path) - 1;
		int[] weights = row(config.lootTierWeights, level, DEFAULT_WEIGHTS, 5);
		int[] rolls = row(config.lootTierRolls, level, DEFAULT_ROLLS, 2);
		LootPool.Builder pool = LootPool.lootPool().setRolls(UniformGenerator.between(rolls[0], Math.max(rolls[0], rolls[1])));
		if (path.startsWith("characters/")) {
			// Looting: the killer's Looting level is passed in as luck, each level adds this many extra rolls.
			pool.setBonusRolls(ConstantValue.exactly((float) config.lootingBonusRolls));
		}
		for (int tier = 0; tier < 5; tier++) {
			if (weights[tier] <= 0) {
				continue;
			}
			// Elytras only come from End structures' legendary rolls.
			String name = tier == 4 && LootLevels.END_TABLES.contains(path) ? "legendary_end" : TIERS[tier];
			pool.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, ItzBreadLoaf.id("tiers/" + name)))
					.setWeight(weights[tier]));
		}
		return pool;
	}

	private static int[] row(int[][] table, int level, int[][] defaults, int size) {
		int[] row = table != null && level < table.length && table[level] != null && table[level].length >= size ? table[level] : defaults[level];
		return row;
	}

	// ---------------------------------------------------------------- character drops

	/** Rolls the character's own drop table (characters/<name>): its themed items plus tier rolls, more with Looting. */
	public static void dropCharacterLoot(LoafBrain brain, ServerPlayer self, DamageSource source) {
		ServerLevel level = self.level();
		ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, ItzBreadLoaf.id("characters/" + brain.kind().id));
		LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
		Entity killer = source.getEntity();
		float looting = 0.0F;
		if (killer instanceof LivingEntity living) {
			Holder<Enchantment> enchantment = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
			looting = EnchantmentHelper.getItemEnchantmentLevel(enchantment, living.getMainHandItem());
		}
		LootParams params = new LootParams.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, self)
				.withParameter(LootContextParams.ORIGIN, self.position())
				.withParameter(LootContextParams.DAMAGE_SOURCE, source)
				.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, killer)
				.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity())
				.withLuck(looting)
				.create(LootContextParamSets.ENTITY);
		for (ItemStack stack : table.getRandomItems(params)) {
			self.spawnAtLocation(level, stack);
		}
	}

	/** Like a normal mob: each armor piece 8.5%, and (if {@code weapon}) their weapon 15% (both in the config). */
	public static void dropGear(ServerPlayer self, RandomSource random, boolean weapon) {
		ServerLevel level = self.level();
		ModConfig config = ModConfig.get();
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			ItemStack piece = self.getItemBySlot(slot);
			if (!piece.isEmpty() && random.nextFloat() < config.characterArmorDropChance) {
				self.spawnAtLocation(level, piece.copy());
			}
		}
		if (!weapon || random.nextFloat() >= config.characterWeaponDropChance) {
			return;
		}
		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = self.getInventory().getItem(slot);
			if (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(Items.MACE) || stack.is(Items.BOW)
					|| stack.is(Items.CROSSBOW) || stack.is(Items.TRIDENT)) {
				self.spawnAtLocation(level, stack.copy());
				return;
			}
		}
	}
}
