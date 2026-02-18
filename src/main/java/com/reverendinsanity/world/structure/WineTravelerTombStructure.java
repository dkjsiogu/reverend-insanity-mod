package com.reverendinsanity.world.structure;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import java.util.Optional;

// 花酒行者遗藏：五转魔道蛊师花酒行者的地下墓室，酒香弥漫，酒虫守护
public class WineTravelerTombStructure extends Structure {

    public static final MapCodec<WineTravelerTombStructure> CODEC = simpleCodec(WineTravelerTombStructure::new);

    public WineTravelerTombStructure(StructureSettings settings) {
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
            builder.addPiece(new WineTravelerTombPiece(genPos, context.random()));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.WINE_TRAVELER_TOMB_TYPE.get();
    }
}
