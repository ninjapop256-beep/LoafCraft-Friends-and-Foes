package com.itzbreadloaf.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** "Lily's Collected Works" (creative tab): use it to get every one of Lily's books. */
public class CollectedWorksItem extends Item {
	public CollectedWorksItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			for (String id : LilyBooks.all().keySet()) {
				ItemStack book = LilyBooks.stack(id);
				if (!player.getInventory().add(book)) {
					player.drop(book, false);
				}
			}
			player.displayClientMessage(Component.translatable("item.itzbreadloaf.lilys_collected_works.used", LilyBooks.all().size()), true);
			if (!player.getAbilities().instabuild) {
				player.getItemInHand(hand).shrink(1);
			}
		}
		return InteractionResult.SUCCESS;
	}
}
