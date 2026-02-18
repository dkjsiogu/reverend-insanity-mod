package com.reverendinsanity.world.structure;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import java.util.Optional;

// 血湖墓地：血道蛊师的地下墓室，血红色深暗氛围
public class BloodLakeTombStructure extends Structure {

    public static final MapCodec<BloodLakeTombStructure> CODEC = simpleCodec(BloodLakeTombStructure::new);

    public BloodLakeTombStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int cx = context.chunkPos().getMiddleBlockX();
        int cz = context.chunkPos().getMiddleBlockZ();

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
            cx, cz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        int maxY = Math.min(surfaceY - 25, 20);
        if (maxY < -20) return Optional.empty();

        int y = context.random().nextIntBetweenInclusive(-20, maxY);
        BlockPos genPos = new BlockPos(cx, y, cz);

        return Optional.of(new GenerationStub(genPos, builder -> {
            builder.addPiece(new BloodLakeTombPiece(genPos, context.random()));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.BLOOD_LAKE_TOMB_TYPE.get();
    }
}
