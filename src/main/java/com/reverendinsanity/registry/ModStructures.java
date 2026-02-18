package com.reverendinsanity.registry;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.world.structure.GuCavePiece;
import com.reverendinsanity.world.structure.GuCaveStructure;
import com.reverendinsanity.world.structure.InheritanceGroundPiece;
import com.reverendinsanity.world.structure.InheritanceGroundStructure;
import com.reverendinsanity.world.structure.WineTravelerTombPiece;
import com.reverendinsanity.world.structure.WineTravelerTombStructure;
import com.reverendinsanity.world.structure.ClanSettlementPiece;
import com.reverendinsanity.world.structure.ClanSettlementStructure;
import com.reverendinsanity.world.structure.MoonOrchidCavePiece;
import com.reverendinsanity.world.structure.MoonOrchidCaveStructure;
import com.reverendinsanity.world.structure.BloodLakeTombPiece;
import com.reverendinsanity.world.structure.BloodLakeTombStructure;
import com.reverendinsanity.world.structure.GuAcademyPiece;
import com.reverendinsanity.world.structure.GuAcademyStructure;
import com.reverendinsanity.world.structure.CaravanCampPiece;
import com.reverendinsanity.world.structure.CaravanCampStructure;
import com.reverendinsanity.world.structure.AscensionGatewayPiece;
import com.reverendinsanity.world.structure.AscensionGatewayStructure;
import com.reverendinsanity.world.structure.HeavenFragmentPiece;
import com.reverendinsanity.world.structure.HeavenFragmentStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 结构注册
public class ModStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
        DeferredRegister.create(Registries.STRUCTURE_TYPE, ReverendInsanity.MODID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
        DeferredRegister.create(Registries.STRUCTURE_PIECE, ReverendInsanity.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<GuCaveStructure>> GU_CAVE_TYPE =
        STRUCTURE_TYPES.register("gu_cave", () -> () -> GuCaveStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> GU_CAVE_PIECE =
        STRUCTURE_PIECES.register("gu_cave_piece", () ->
            (StructurePieceType)(ctx, tag) -> new GuCavePiece(ctx, tag));

    public static final DeferredHolder<StructureType<?>, StructureType<InheritanceGroundStructure>> INHERITANCE_GROUND_TYPE =
        STRUCTURE_TYPES.register("inheritance_ground", () -> () -> InheritanceGroundStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> INHERITANCE_GROUND_PIECE =
        STRUCTURE_PIECES.register("inheritance_ground_piece", () ->
            (StructurePieceType)(ctx, tag) -> new InheritanceGroundPiece(ctx, tag));

    public static final DeferredHolder<StructureType<?>, StructureType<WineTravelerTombStructure>> WINE_TRAVELER_TOMB_TYPE =
        STRUCTURE_TYPES.register("wine_traveler_tomb", () -> () -> WineTravelerTombStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> WINE_TRAVELER_TOMB_PIECE =
        STRUCTURE_PIECES.register("wine_traveler_tomb_piece", () ->
            (StructurePieceType)(ctx, tag) -> new WineTravelerTombPiece(ctx, tag));

    public static final DeferredHolder<StructureType<?>, StructureType<ClanSettlementStructure>> CLAN_SETTLEMENT_TYPE =
        STRUCTURE_TYPES.register("clan_settlement", () -> () -> ClanSettlementStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> CLAN_SETTLEMENT_PIECE =
        STRUCTURE_PIECES.register("clan_settlement_piece", () ->
            (StructurePieceType)(ctx, tag) -> new ClanSettlementPiece(ctx, tag));

    public static final DeferredHolder<StructureType<?>, StructureType<MoonOrchidCaveStructure>> MOON_ORCHID_CAVE_TYPE =
        STRUCTURE_TYPES.register("moon_orchid_cave", () -> () -> MoonOrchidCaveStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> MOON_ORCHID_CAVE_PIECE =
        STRUCTURE_PIECES.register("moon_orchid_cave_piece", () ->
            (StructurePieceType)(ctx, tag) -> new MoonOrchidCavePiece(ctx, tag));

    public static final DeferredHolder<StructureType<?>, StructureType<BloodLakeTombStructure>> BLOOD_LAKE_TOMB_TYPE =
        STRUCTURE_TYPES.register("blood_lake_tomb", () -> () -> BloodLakeTombStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> BLOOD_LAKE_TOMB_PIECE =
        STRUCTURE_PIECES.register("blood_lake_tomb_piece", () ->
            (StructurePieceType)(ctx, tag) -> new BloodLakeTombPiece(ctx, tag));

    public static final DeferredHolder<StructureType<?>, StructureType<GuAcademyStructure>> GU_ACADEMY_TYPE =
        STRUCTURE_TYPES.register("gu_academy", () -> () -> GuAcademyStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> GU_ACADEMY_PIECE =
        STRUCTURE_PIECES.register("gu_academy_piece", () ->
            (StructurePieceType)(ctx, tag) -> new GuAcademyPiece(ctx, tag));

    public static final DeferredHolder<StructureType<?>, StructureType<CaravanCampStructure>> CARAVAN_CAMP_TYPE =
        STRUCTURE_TYPES.register("caravan_camp", () -> () -> CaravanCampStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> CARAVAN_CAMP_PIECE =
        STRUCTURE_PIECES.register("caravan_camp_piece", () ->
            (StructurePieceType)(ctx, tag) -> new CaravanCampPiece(ctx, tag));

    public static final DeferredHolder<StructureType<?>, StructureType<AscensionGatewayStructure>> ASCENSION_GATEWAY_TYPE =
        STRUCTURE_TYPES.register("ascension_gateway", () -> () -> AscensionGatewayStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> ASCENSION_GATEWAY_PIECE =
        STRUCTURE_PIECES.register("ascension_gateway_piece", () ->
            (StructurePieceType)(ctx, tag) -> new AscensionGatewayPiece(ctx, tag));

    public static final DeferredHolder<StructureType<?>, StructureType<HeavenFragmentStructure>> HEAVEN_FRAGMENT_TYPE =
        STRUCTURE_TYPES.register("heaven_fragment", () -> () -> HeavenFragmentStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> HEAVEN_FRAGMENT_PIECE =
        STRUCTURE_PIECES.register("heaven_fragment_piece", () ->
            (StructurePieceType)(ctx, tag) -> new HeavenFragmentPiece(ctx, tag));
}
