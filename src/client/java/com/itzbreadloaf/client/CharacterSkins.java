package com.itzbreadloaf.client;

import com.itzbreadloaf.ItzBreadLoaf;
import com.itzbreadloaf.character.CharacterKind;
import com.itzbreadloaf.character.CharacterVariant;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

/**
 * Picks the skin for a character from assets/itzbreadloaf/textures/entity/, most specific first:
 * Characters with old skins ({@code <character>_alt1.png}, {@code _alt2}...) wear a random one, see {@link #pickSkin}.
 * <ol>
 *   <li>{@code <type>_baby.png} for babies (for example {@code knight_baby.png})</li>
 *   <li>{@code <type>.png} (for example {@code knight.png}, {@code witchy.png}, {@code baker.png})</li>
 *   <li>{@code <character>_baby.png} for babies (for example {@code itzbreadloaf_baby.png})</li>
 *   <li>{@code <character>.png} (itzbreadloaf, imlilylol, madison, master_breadloaf, felixsyn, harley)</li>
 * </ol>
 * The type comes from the character's UUID (saved with it, and sent to every client as part of the player info),
 * baby comes from the synced scale attribute. Arm width (slim or classic) is read from the PNG itself: slim skins
 * leave the right arm's last column empty (pixel 54,20 is transparent).
 */
public final class CharacterSkins {
	private static final Map<String, Optional<PlayerSkin>> CACHE = new HashMap<>();
	private static final Map<String, List<String>> ALTERNATES = new HashMap<>();

	private CharacterSkins() {
	}

	public static PlayerSkin get(CharacterVariant variant, boolean baby, UUID id) {
		CharacterKind kind = variant.kind;
		String main = pickSkin(kind, id);
		// The plain types (ItzBreadLoaf, FelixSyn...) share their file name with the character, so for them the
		// type-specific step is skipped and the random pick decides.
		boolean typed = !variant.id.equals(kind.id);
		String[] names = baby
				? (typed ? new String[]{variant.id + "_baby", variant.id, kind.id + "_baby", main, kind.id}
						: new String[]{kind.id + "_baby", main, kind.id})
				: (typed ? new String[]{variant.id, main, kind.id} : new String[]{main, kind.id});
		for (String name : names) {
			Optional<PlayerSkin> skin = CACHE.computeIfAbsent(name, n -> load(n, kind.slim));
			if (skin.isPresent()) {
				return skin.get();
			}
		}
		// No file at all: a plain texture path so the problem is visible (missing-texture pink) instead of a crash.
		Identifier path = kind.skinTexture();
		return new PlayerSkin(new ClientAsset.ResourceTexture(path, path), null, null, kind.slim ? PlayerModelType.SLIM : PlayerModelType.WIDE, true);
	}

	/** Forget everything (after a resource reload, so new or changed skin files are picked up). */
	public static void clear() {
		CACHE.clear();
		ALTERNATES.clear();
	}

	/**
	 * Which skin this character wears: its main skin ({@code <character>.png}) most of the time, otherwise one of its
	 * old skins ({@code <character>_alt1.png}, {@code _alt2}...) picked evenly. The pick comes from the character's UUID,
	 * so each one keeps its skin forever and every player sees the same one. How often the main skin is worn is
	 * mainSkinChance in the config (0.5 = half of them).
	 */
	private static String pickSkin(CharacterKind kind, UUID id) {
		List<String> alternates = ALTERNATES.computeIfAbsent(kind.id, CharacterSkins::findAlternates);
		if (alternates.isEmpty()) {
			return kind.id;
		}
		// SplitMix64 on the UUID: well spread even though the UUIDs only differ in a few bits.
		long h = id.getMostSignificantBits() * 31 + id.getLeastSignificantBits();
		h = (h ^ (h >>> 30)) * 0xbf58476d1ce4e5b9L;
		h = (h ^ (h >>> 27)) * 0x94d049bb133111ebL;
		h ^= h >>> 31;
		double roll = (h >>> 11) * 0x1.0p-53;
		double mainChance = Math.clamp(com.itzbreadloaf.config.ModConfig.get().mainSkinChance, 0.0, 1.0);
		if (roll < mainChance) {
			return kind.id;
		}
		return alternates.get((int) Math.floorMod(h >>> 3, (long) alternates.size()));
	}

	/** {@code <character>_alt1.png}, {@code _alt2.png}... up to the first missing number. */
	private static List<String> findAlternates(String kindId) {
		List<String> found = new ArrayList<>();
		for (int i = 1; i <= 64; i++) {
			String name = kindId + "_alt" + i;
			if (Minecraft.getInstance().getResourceManager().getResource(ItzBreadLoaf.id("textures/entity/" + name + ".png")).isEmpty()) {
				break;
			}
			found.add(name);
		}
		return found;
	}

	private static Optional<PlayerSkin> load(String name, boolean slimFallback) {
		Identifier path = ItzBreadLoaf.id("textures/entity/" + name + ".png");
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(path);
		if (resource.isEmpty()) {
			return Optional.empty();
		}
		boolean slim = slimFallback;
		try (InputStream stream = resource.get().open(); NativeImage image = NativeImage.read(stream)) {
			if (image.getWidth() == 64 && image.getHeight() == 64) {
				slim = (image.getPixel(54, 20) >>> 24) == 0;
			} else {
				slim = false;
			}
		} catch (Exception e) {
			ItzBreadLoaf.LOGGER.warn("Could not read skin {} to check its arm width, using the default", path, e);
		}
		return Optional.of(new PlayerSkin(new ClientAsset.ResourceTexture(path, path), null, null,
				slim ? PlayerModelType.SLIM : PlayerModelType.WIDE, true));
	}

	/** Master BreadLoaf (any of his skins): gets the glowing eyes layer. */
	public static boolean isMaster(PlayerSkin skin) {
		return skin != null && skin.body() instanceof ClientAsset.ResourceTexture texture
				&& texture.texturePath().getNamespace().equals(ItzBreadLoaf.MOD_ID)
				&& texture.texturePath().getPath().startsWith("textures/entity/master_breadloaf");
	}
}
