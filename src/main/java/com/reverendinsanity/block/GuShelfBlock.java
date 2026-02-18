package com.reverendinsanity.block;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.block.entity.GuShelfBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// 蛊虫架，存放蛊虫的功能性家具方块
public class GuShelfBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final MapCodec<GuShelfBlock> CODEC = simpleCodec(GuShelfBlock::new);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 14, 15);

    public GuShelfBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GuShelfBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GuShelfBlockEntity shelf)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!stack.isEmpty() && shelf.getDisplayItem().isEmpty()) {
            String namespace = stack.getItem().builtInRegistryHolder().key().location().getNamespace();
            if (namespace.equals(ReverendInsanity.MODID)) {
                if (!level.isClientSide()) {
                    shelf.setDisplayItem(stack);
                    stack.shrink(1);
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.0f);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GuShelfBlockEntity shelf)) {
            return InteractionResult.PASS;
        }

        if (!shelf.getDisplayItem().isEmpty()) {
            if (!level.isClientSide()) {
                ItemStack removed = shelf.removeDisplayItem();
                if (!player.getInventory().add(removed)) {
                    popResource(level, pos.above(), removed);
                }
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 0.8f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GuShelfBlockEntity shelf) {
                ItemStack item = shelf.getDisplayItem();
                if (!item.isEmpty()) {
                    popResource(level, pos, item.copy());
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
