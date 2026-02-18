package com.reverendinsanity.registry;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.block.ApertureExitPortalBlock;
import com.reverendinsanity.block.BlessedLandBlock;
import com.reverendinsanity.block.FormationStoneBlock;
import com.reverendinsanity.block.GuShelfBlock;
import com.reverendinsanity.block.MoonOrchidBlock;
import com.reverendinsanity.block.NaiveMushroomBlock;
import com.reverendinsanity.block.RainbowStalactiteBlock;
import com.reverendinsanity.block.RefinementCauldronBlock;
import com.reverendinsanity.block.SpearBambooBlock;
import com.reverendinsanity.block.SpiritSpringBlock;
import com.reverendinsanity.block.WildMoonOrchidBlock;
import com.reverendinsanity.block.WineJarBlock;

import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 方块注册
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(Registries.BLOCK, ReverendInsanity.MODID);

    public static final DeferredHolder<Block, RefinementCauldronBlock> REFINEMENT_CAULDRON =
        BLOCKS.register("refinement_cauldron", () -> new RefinementCauldronBlock(
            BlockBehaviour.Properties.of()
                .strength(3.5f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
                .noOcclusion()
                .lightLevel(state -> 5)
        ));

    public static final DeferredHolder<Block, DropExperienceBlock> PRIMEVAL_STONE_ORE =
        BLOCKS.register("primeval_stone_ore", () -> new DropExperienceBlock(
            UniformInt.of(2, 5),
            BlockBehaviour.Properties.of()
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
        ));

    public static final DeferredHolder<Block, DropExperienceBlock> DEEPSLATE_PRIMEVAL_STONE_ORE =
        BLOCKS.register("deepslate_primeval_stone_ore", () -> new DropExperienceBlock(
            UniformInt.of(3, 7),
            BlockBehaviour.Properties.of()
                .strength(4.5f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE)
        ));

    public static final DeferredHolder<Block, MoonOrchidBlock> MOON_ORCHID =
        BLOCKS.register("moon_orchid", () -> new MoonOrchidBlock(
            BlockBehaviour.Properties.of()
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
                .pushReaction(PushReaction.DESTROY)
        ));

    public static final DeferredHolder<Block, SpiritSpringBlock> SPIRIT_SPRING =
        BLOCKS.register("spirit_spring", () -> new SpiritSpringBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WATER)
                .strength(50.0f, 1200.0f)
                .noOcclusion()
                .lightLevel(s -> 10)
                .sound(SoundType.AMETHYST)
        ));

    public static final DeferredHolder<Block, BlessedLandBlock> BLESSED_LAND_CORE =
        BLOCKS.register("blessed_land_core", () -> new BlessedLandBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.GOLD)
                .strength(50.0f, 1200.0f)
                .noOcclusion()
                .lightLevel(s -> 10)
                .sound(SoundType.AMETHYST)
                .pushReaction(PushReaction.BLOCK)
        ));

    public static final DeferredHolder<Block, WildMoonOrchidBlock> WILD_MOON_ORCHID =
        BLOCKS.register("wild_moon_orchid", () -> new WildMoonOrchidBlock(
            BlockBehaviour.Properties.of()
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
                .lightLevel(s -> 8)
                .pushReaction(PushReaction.DESTROY)
        ));

    public static final DeferredHolder<Block, RainbowStalactiteBlock> RAINBOW_STALACTITE =
        BLOCKS.register("rainbow_stalactite", () -> new RainbowStalactiteBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(1.5f)
                .sound(SoundType.AMETHYST)
                .lightLevel(s -> 7)
                .noOcclusion()
                .randomTicks()
        ));

    public static final DeferredHolder<Block, SpearBambooBlock> SPEAR_BAMBOO =
        BLOCKS.register("spear_bamboo", () -> new SpearBambooBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(1.0f)
                .sound(SoundType.BAMBOO)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY)
        ));

    public static final DeferredHolder<Block, GuShelfBlock> GU_SHELF =
        BLOCKS.register("gu_shelf", () -> new GuShelfBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.5f)
                .sound(SoundType.WOOD)
                .noOcclusion()
        ));

    public static final DeferredHolder<Block, WineJarBlock> WINE_JAR =
        BLOCKS.register("wine_jar", () -> new WineJarBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_BROWN)
                .strength(1.0f)
                .sound(SoundType.DECORATED_POT)
        ));

    public static final DeferredHolder<Block, FormationStoneBlock> FORMATION_STONE =
        BLOCKS.register("formation_stone", () -> new FormationStoneBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
                .lightLevel(s -> s.getValue(FormationStoneBlock.ACTIVE) ? 12 : 0)
                .randomTicks()
        ));

    public static final DeferredHolder<Block, NaiveMushroomBlock> NAIVE_MUSHROOM =
        BLOCKS.register("naive_mushroom", () -> new NaiveMushroomBlock(
            BlockBehaviour.Properties.of().strength(0).noCollission().lightLevel(s -> 10)
                .sound(SoundType.FUNGUS).pushReaction(PushReaction.DESTROY)));

    public static final DeferredHolder<Block, ApertureExitPortalBlock> APERTURE_EXIT_PORTAL =
        BLOCKS.register("aperture_exit_portal", () -> new ApertureExitPortalBlock(
            BlockBehaviour.Properties.of()
                .strength(-1.0f, 3600000.0f)
                .noOcclusion()
                .lightLevel(state -> 12)
                .mapColor(MapColor.COLOR_CYAN)
                .sound(SoundType.AMETHYST)));
}
