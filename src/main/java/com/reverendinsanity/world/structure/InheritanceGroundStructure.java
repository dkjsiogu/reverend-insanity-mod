package com.reverendinsanity.world.structure;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import java.util.Optional;

// 传承秘境：大能蛊师死后留下的隐藏地下结构，内含试炼、守卫和珍贵蛊虫奖励
public class InheritanceGroundStructure extends Structure {

    public static final MapCodec<InheritanceGroundStructure> CODEC = simpleCodec(InheritanceGroundStructure::new);

    public InheritanceGroundStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int cx = context.chunkPos().getMiddleBlockX();
        int cz = context.chunkPos().getMiddleBlockZ();

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
            cx, cz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        int maxY = Math.min(surfaceY - 30, 30);
        if (maxY < 5) return Optional.empty();

        int y = context.random().nextIntBetweenInclusive(5, maxY);
        BlockPos genPos = new BlockPos(cx, y, cz);

        return Optional.of(new GenerationStub(genPos, builder -> {
            builder.addPiece(new InheritanceGroundPiece(genPos, context.random()));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.INHERITANCE_GROUND_TYPE.get();
    }
}
