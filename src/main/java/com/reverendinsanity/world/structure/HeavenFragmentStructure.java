package com.reverendinsanity.world.structure;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import java.util.Optional;

// 九天碎片遗迹：太古九天陨落后散落世间的碎片坠落点
public class HeavenFragmentStructure extends Structure {

    public static final MapCodec<HeavenFragmentStructure> CODEC = simpleCodec(HeavenFragmentStructure::new);

    public HeavenFragmentStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int cx = context.chunkPos().getMiddleBlockX();
        int cz = context.chunkPos().getMiddleBlockZ();

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
            cx, cz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        if (surfaceY < 10) return Optional.empty();

        int y = surfaceY - 3;
        BlockPos genPos = new BlockPos(cx, y, cz);

        return Optional.of(new GenerationStub(genPos, builder -> {
            builder.addPiece(new HeavenFragmentPiece(genPos, context.random()));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.HEAVEN_FRAGMENT_TYPE.get();
    }
}
