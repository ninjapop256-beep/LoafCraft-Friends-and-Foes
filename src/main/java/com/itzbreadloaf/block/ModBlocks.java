package com.itzbreadloaf.block;

import com.itzbreadloaf.ItzBreadLoaf;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ModBlocks {
	public static final ResourceKey<Block> MASTER_LOAF_TROPHY_KEY =
			ResourceKey.create(Registries.BLOCK, ItzBreadLoaf.id("master_loaf_trophy"));

	/** Decorative trophy dropped by Master BreadLoaf: a gold cup with a loaf on top. */
	public static final Block MASTER_LOAF_TROPHY = Registry.register(BuiltInRegistries.BLOCK, MASTER_LOAF_TROPHY_KEY,
			new TrophyBlock(BlockBehaviour.Properties.of().setId(MASTER_LOAF_TROPHY_KEY)
					.strength(1.0F).sound(SoundType.METAL).noOcclusion()));

	public static final ResourceKey<Block> RESIDENT_MARKER_KEY =
			ResourceKey.create(Registries.BLOCK, ItzBreadLoaf.id("resident_marker"));

	/** Invisible marker placed by structures: spawns the residents when a real player first comes near. */
	public static final Block RESIDENT_MARKER = Registry.register(BuiltInRegistries.BLOCK, RESIDENT_MARKER_KEY,
			new ResidentMarkerBlock(BlockBehaviour.Properties.of().setId(RESIDENT_MARKER_KEY)
					.noCollision().noLootTable().noOcclusion().strength(-1.0F, 3600000.0F).replaceable()));

	public static final net.minecraft.world.level.block.entity.BlockEntityType<ResidentMarkerBlockEntity> RESIDENT_MARKER_ENTITY =
			Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ItzBreadLoaf.id("resident_marker"),
					net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder
							.create(ResidentMarkerBlockEntity::new, RESIDENT_MARKER).build());

	private ModBlocks() {
	}

	public static void register() {
		// Loads the class so the static fields above run.
	}

	public static class TrophyBlock extends Block {
		public static final MapCodec<TrophyBlock> CODEC = simpleCodec(TrophyBlock::new);
		private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 13, 13);

		public TrophyBlock(BlockBehaviour.Properties properties) {
			super(properties);
		}

		@Override
		protected MapCodec<? extends Block> codec() {
			return CODEC;
		}

		@Override
		protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
			return SHAPE;
		}
	}
}
