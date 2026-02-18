package com.reverendinsanity.world.structure;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import java.util.Optional;

// 古月山寨：蛊师家族聚落地表结构
public class ClanSettlementStructure extends Structure {

    public static final MapCodec<ClanSettlementStructure> CODEC = simpleCodec(ClanSettlementStructure::new);

    public ClanSettlementStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int cx = context.chunkPos().getMiddleBlockX();
        int cz = context.chunkPos().getMiddleBlockZ();

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
            cx, cz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        if (surfaceY < 50) return Optional.empty();

        BlockPos surfacePos = new BlockPos(cx, surfaceY, cz);

        return Optional.of(new GenerationStub(surfacePos, builder -> {
            builder.addPiece(new ClanSettlementPiece(surfacePos, context.random()));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.CLAN_SETTLEMENT_TYPE.get();
    }
}
