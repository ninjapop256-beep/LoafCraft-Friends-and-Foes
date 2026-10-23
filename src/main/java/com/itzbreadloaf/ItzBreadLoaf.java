package com.itzbreadloaf;

import com.itzbreadloaf.block.ModBlocks;
import com.itzbreadloaf.boss.BossSpawner;
import com.itzbreadloaf.character.CharacterManager;
import com.itzbreadloaf.character.NaturalSpawner;
import com.itzbreadloaf.config.ModConfig;
import com.itzbreadloaf.item.ModItems;
import com.itzbreadloaf.sound.ModSounds;
import com.itzbreadloaf.worldgen.ModStructures;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoafCraft: Friends & Foes. Every character (ItzBreadLoaf, ImLilyLol, Madison, Master BreadLoaf) is a Carpet fake
 * player driven by a brain through Carpet's player action pack. Requires Carpet.
 */
public class ItzBreadLoaf implements ModInitializer {
	public static final String MOD_ID = "itzbreadloaf";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfig.load();
		ModSounds.register();
		ModBlocks.register();
		ModItems.register();
		ModStructures.register();
		CharacterManager.register();
		NaturalSpawner.register();
		com.itzbreadloaf.character.VillageSpawner.register();
		BossSpawner.register();
		LoafCraftCommands.register();
		FriendCommand.register();
		com.itzbreadloaf.character.StructureTracker.register();
		com.itzbreadloaf.character.BastionThrones.register();
		LOGGER.info("LoafCraft loaded (characters are Carpet fake players)");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
