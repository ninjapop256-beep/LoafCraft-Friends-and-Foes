package com.itzbreadloaf.item;

import com.itzbreadloaf.ItzBreadLoaf;
import com.itzbreadloaf.block.ModBlocks;
import com.itzbreadloaf.character.CharacterKind;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public final class ModItems {
	// Spawn eggs create Carpet fake players (characters), not mobs.
	public static final Item ITZBREADLOAF_SPAWN_EGG = registerEgg("itzbreadloaf_spawn_egg", CharacterKind.BREADLOAF);
	public static final Item IMLILYLOL_SPAWN_EGG = registerEgg("imlilylol_spawn_egg", CharacterKind.LILY);
	public static final Item MADISON_SPAWN_EGG = registerEgg("madison_spawn_egg", CharacterKind.MADISON);
	public static final Item MASTER_BREADLOAF_SPAWN_EGG = registerEgg("master_breadloaf_spawn_egg", CharacterKind.MASTER);
	/** Spawns FelixSyn together with his partner Harley. */
	public static final Item FELIXSYN_SPAWN_EGG = registerEgg("felixsyn_spawn_egg", CharacterKind.FELIXSYN);
	public static final Item HARLEY_SPAWN_EGG = registerEgg("harley_spawn_egg", CharacterKind.HARLEY);

	/** Harley's netherite sword. Dropped (always) by a wild Harley, enchanted Sharpness V and Unbreaking III. */
	public static final Item HARLEYS_BLADE = register("harleys_blade", properties -> new Item(properties
			.sword(net.minecraft.world.item.ToolMaterial.NETHERITE, 3.0F, -2.4F).rarity(Rarity.EPIC).fireResistant()));

	/** Food you can always eat (even full, like a golden apple): Saturation and Speed I for 10 seconds. Sold by Baker Madison. */
	public static final Item LOAF_BREAD = register("loaf_bread", properties -> new Item(properties
			.food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).alwaysEdible().build(),
					Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
							new MobEffectInstance(MobEffects.SATURATION, 200),
							new MobEffectInstance(MobEffects.SPEED, 200)))).build())));
	public static final Item MASTER_LOAF_TROPHY = register("master_loaf_trophy",
			properties -> new BlockItem(ModBlocks.MASTER_LOAF_TROPHY, properties.useBlockDescriptionPrefix().rarity(Rarity.EPIC)));

	public static final ResourceKey<CreativeModeTab> LOAFCRAFT_TAB_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, ItzBreadLoaf.id("loafcraft"));
	public static final CreativeModeTab LOAFCRAFT_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, LOAFCRAFT_TAB_KEY,
			FabricItemGroup.builder()
					.title(Component.translatable("itemGroup.itzbreadloaf.loafcraft"))
					.icon(() -> new ItemStack(ITZBREADLOAF_SPAWN_EGG))
					.displayItems((parameters, output) -> {
						output.accept(ITZBREADLOAF_SPAWN_EGG);
						output.accept(IMLILYLOL_SPAWN_EGG);
						output.accept(MADISON_SPAWN_EGG);
						output.accept(MASTER_BREADLOAF_SPAWN_EGG);
						output.accept(FELIXSYN_SPAWN_EGG);
						output.accept(HARLEY_SPAWN_EGG);
						output.accept(harleysBlade(parameters.holders()));
						output.accept(LOAF_BREAD);
						output.accept(MASTER_LOAF_TROPHY);
					})
					.build());

	private ModItems() {
	}

	private static Item register(String name, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ItzBreadLoaf.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
	}

	private static Item registerEgg(String name, CharacterKind kind) {
		return register(name, properties -> new LoafSpawnEggItem(kind, properties));
	}

	/** Harley's Blade as she carries and drops it: Sharpness V, Unbreaking III. */
	public static ItemStack harleysBlade(net.minecraft.core.HolderLookup.Provider registries) {
		ItemStack blade = new ItemStack(HARLEYS_BLADE);
		var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
		blade.enchant(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS), 5);
		blade.enchant(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING), 3);
		return blade;
	}

	public static void register() {
		// The eggs also stay in the vanilla Spawn Eggs tab.
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
			entries.accept(ITZBREADLOAF_SPAWN_EGG);
			entries.accept(IMLILYLOL_SPAWN_EGG);
			entries.accept(MADISON_SPAWN_EGG);
			entries.accept(MASTER_BREADLOAF_SPAWN_EGG);
			entries.accept(FELIXSYN_SPAWN_EGG);
			entries.accept(HARLEY_SPAWN_EGG);
		});
	}
}
