package com.reverendinsanity.block;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.formation.FormationArrayManager;
import com.reverendinsanity.item.GuItem;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

// 阵法石，右键使用阵道蛊虫布阵，红石激活恢复真元
public class FormationStoneBlock extends Block {

    public static final MapCodec<FormationStoneBlock> CODEC = simpleCodec(FormationStoneBlock::new);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public FormationStoneBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (stack.getItem() instanceof GuItem guItem) {
            var guId = guItem.getGuTypeId();
            if (isFormationGu(stack)) {
                boolean success = FormationArrayManager.tryActivate(sp, pos, guId);
                return success ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private boolean isFormationGu(ItemStack stack) {
        return stack.is(ModItems.TRAP_FORMATION_GU.get())
                || stack.is(ModItems.FORMATION_SHIELD_GU.get())
                || stack.is(ModItems.GRAND_FORMATION_GU.get());
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(ACTIVE)) {
            level.setBlock(pos, state.setValue(ACTIVE, powered), 2);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(ACTIVE)) {
            level.setBlock(pos, state.setValue(ACTIVE, powered), 2);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) return;

        AABB area = new AABB(pos).inflate(8.0);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, area);
        for (ServerPlayer player : players) {
            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            Aperture aperture = data.getAperture();
            if (aperture.isOpened()) {
                aperture.regenerateEssence(2.0f);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) return;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        for (int i = 0; i < 2; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 0.3 + random.nextDouble() * 0.3;
            level.addParticle(ParticleTypes.ENCHANT,
                    x + Math.cos(angle) * radius,
                    y + random.nextDouble() * 0.3,
                    z + Math.sin(angle) * radius,
                    0, 0.05, 0);
        }
    }
}
