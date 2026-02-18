package com.reverendinsanity.world.dimension;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.ReverendInsanity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

// 仙窍维度注册
public class ModDimensions {

    public static final ResourceKey<Level> APERTURE_DIM =
        ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "immortal_aperture"));

    public static final ResourceKey<DimensionType> APERTURE_DIM_TYPE =
        ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "immortal_aperture"));

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
        DeferredRegister.create(Registries.CHUNK_GENERATOR, ReverendInsanity.MODID);

    public static final Supplier<MapCodec<ApertureChunkGenerator>> APERTURE_GENERATOR =
        CHUNK_GENERATORS.register("aperture_generator", () -> ApertureChunkGenerator.CODEC);
}
