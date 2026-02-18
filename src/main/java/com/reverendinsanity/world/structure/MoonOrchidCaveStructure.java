package com.reverendinsanity.world.structure;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import java.util.Optional;

// 月兰花海洞穴：地下大型溶洞，遍地月兰花，七彩钟乳石，地下河
public class MoonOrchidCaveStructure extends Structure {

    public static final MapCodec<MoonOrchidCaveStructure> CODEC = simpleCodec(MoonOrchidCaveStructure::new);

    public MoonOrchidCaveStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int cx = context.chunkPos().getMiddleBlockX();
        int cz = context.chunkPos().getMiddleBlockZ();

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
            cx, cz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        int maxY = Math.min(surfaceY - 25, 40);
        if (maxY < 10) return Optional.empty();

        int y = context.random().nextIntBetweenInclusive(10, maxY);
        BlockPos genPos = new BlockPos(cx, y, cz);

        return Optional.of(new GenerationStub(genPos, builder -> {
            builder.addPiece(new MoonOrchidCavePiece(genPos, context.random()));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.MOON_ORCHID_CAVE_TYPE.get();
    }
}
