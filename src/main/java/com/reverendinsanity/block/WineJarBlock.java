package com.reverendinsanity.block;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// 酒坛，右键用空瓶取酒，共16次使用次数
public class WineJarBlock extends Block {

    public static final MapCodec<WineJarBlock> CODEC = simpleCodec(WineJarBlock::new);
    public static final int MAX_USES = 16;
    public static final IntegerProperty USES = IntegerProperty.create("uses", 0, MAX_USES);
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 14, 13);

    public WineJarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(USES, MAX_USES));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(USES);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        int uses = state.getValue(USES);
        if (uses <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!stack.is(Items.GLASS_BOTTLE)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        stack.shrink(1);
        ItemStack wine = new ItemStack(ModItems.AGED_WINE.get());
        if (!player.getInventory().add(wine)) {
            player.drop(wine, false);
        }
        level.setBlock(pos, state.setValue(USES, uses - 1), 2);
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
        return ItemInteractionResult.SUCCESS;
    }
}
