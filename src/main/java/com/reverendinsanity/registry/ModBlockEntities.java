package com.reverendinsanity.registry;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.block.entity.BlessedLandBlockEntity;
import com.reverendinsanity.block.entity.GuShelfBlockEntity;
import com.reverendinsanity.block.entity.RefinementCauldronBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 方块实体注册
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ReverendInsanity.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RefinementCauldronBlockEntity>> REFINEMENT_CAULDRON =
        BLOCK_ENTITIES.register("refinement_cauldron", () ->
            BlockEntityType.Builder.of(RefinementCauldronBlockEntity::new, ModBlocks.REFINEMENT_CAULDRON.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlessedLandBlockEntity>> BLESSED_LAND_CORE =
        BLOCK_ENTITIES.register("blessed_land_core", () ->
            BlockEntityType.Builder.of(BlessedLandBlockEntity::new, ModBlocks.BLESSED_LAND_CORE.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GuShelfBlockEntity>> GU_SHELF =
        BLOCK_ENTITIES.register("gu_shelf", () ->
            BlockEntityType.Builder.of(GuShelfBlockEntity::new, ModBlocks.GU_SHELF.get())
                .build(null));
}
