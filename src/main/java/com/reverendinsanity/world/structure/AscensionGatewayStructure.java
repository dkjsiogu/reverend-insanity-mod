package com.reverendinsanity.world.structure;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import java.util.Optional;

// 登天台：通往九天的传说遗迹，庄严神秘的石质祭坛
public class AscensionGatewayStructure extends Structure {

    public static final MapCodec<AscensionGatewayStructure> CODEC = simpleCodec(AscensionGatewayStructure::new);

    public AscensionGatewayStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int cx = context.chunkPos().getMiddleBlockX();
        int cz = context.chunkPos().getMiddleBlockZ();

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
            cx, cz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        if (surfaceY < 60) return Optional.empty();

        BlockPos surfacePos = new BlockPos(cx, surfaceY, cz);

        return Optional.of(new GenerationStub(surfacePos, builder -> {
            builder.addPiece(new AscensionGatewayPiece(surfacePos, context.random()));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.ASCENSION_GATEWAY_TYPE.get();
    }
}
