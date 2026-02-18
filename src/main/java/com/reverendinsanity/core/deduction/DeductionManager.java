package com.reverendinsanity.core.deduction;

import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.KillerMoveRegistry;
import com.reverendinsanity.core.combat.killermove.DaoResonance;
import com.reverendinsanity.core.combat.killermove.MoveEffectRegistry;
import com.reverendinsanity.core.combat.custom.CompositeBasedMoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.util.AdvancementHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import com.reverendinsanity.network.SyncDeductionPayload;
import com.reverendinsanity.network.DeductionResultPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import javax.annotation.Nullable;
import java.util.*;

// 杀招推演管理器：管理所有玩家的推演状态
public class DeductionManager {

    private static final Map<UUID, DeductionSession> activeSessions = new HashMap<>();
    private static final Map<UUID, Map<ResourceLocation, ImprovedMove>> improvedMoves = new HashMap<>();
    private static final Map<UUID, Map<DaoPath, Float>> deductionExperience = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static boolean startDeduction(ServerPlayer player, MoveBlueprint blueprint) {
        UUID playerId = player.getUUID();
        if (activeSessions.containsKey(playerId)) return false;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();

        if (!aperture.isOpened()) return false;
        if (data.getCombatState().isInCombat()) return false;
        if (!blueprint.validate()) return false;

        for (ResourceLocation guId : blueprint.getAllGu()) {
            boolean found = false;
            for (GuInstance gu : aperture.getStoredGu()) {
                if (gu.getTypeId().equals(guId) && gu.isActive()) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        float successRate = calculateSuccessRate(player, blueprint);
        DeductionSession session = new DeductionSession(
            playerId, blueprint, successRate, aperture.getRank().getLevel()
        );

        if (aperture.getCurrentEssence() < session.getEssenceCost()) return false;
        if (aperture.getThoughts() < session.getThoughtsCost()) return false;

        aperture.consumeEssence(session.getEssenceCost());
        aperture.consumeThoughts(session.getThoughtsCost());
        activeSessions.put(playerId, session);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
        return true;
    }

    public static void tick(ServerPlayer player) {
        UUID playerId = player.getUUID();
        DeductionSession session = activeSessions.get(playerId);
        if (session == null) return;

        session.tick();

        if (player.tickCount % 10 == 0) {
            PacketDistributor.sendToPlayer(player, new SyncDeductionPayload(
                true, session.getProgress(), session.getSuccessRate(), "推演中..."
            ));
        }

        if (!session.isComplete()) return;

        DeductionResult result = session.resolve(RANDOM);
        activeSessions.remove(playerId);
        handleResult(playerId, result, session.getBlueprint());

        if (result.resultMove() != null && result.outcome() != DeductionResult.Outcome.FAILURE) {
            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            var combatState = data.getCombatState();
            if (combatState.getEquippedMoves().size() < 2) {
                combatState.equipMove(result.resultMove().id());
            }
            AdvancementHelper.grant(player, "deduction_success");
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else if (result.outcome() == DeductionResult.Outcome.FAILURE) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 0.6f);
        }

        int outcomeIndex = result.outcome().ordinal();
        String moveName = result.resultMove() != null ? result.resultMove().displayName() : "";
        String moveId = result.resultMove() != null ? result.resultMove().id().toString() : "";
        PacketDistributor.sendToPlayer(player, new DeductionResultPayload(
            outcomeIndex, moveName, result.improvementLevel(), result.message()
        ));
    }

    private static void handleResult(UUID playerId, DeductionResult result, MoveBlueprint blueprint) {
        addDeductionExp(playerId, blueprint.targetPath(), result.experienceGained());

        if (result.resultMove() == null) return;

        if (KillerMoveRegistry.get(result.resultMove().id()) == null) {
            KillerMoveRegistry.register(result.resultMove());
        }

        if (!MoveEffectRegistry.hasEffect(result.resultMove().id())) {
            MoveEffectRegistry.registerForMove(result.resultMove().id(),
                new CompositeBasedMoveEffect(result.resultMove()));
        }

        if (result.outcome() == DeductionResult.Outcome.GREAT_SUCCESS && result.improvementLevel() > 0) {
            Map<ResourceLocation, ImprovedMove> playerMoves =
                improvedMoves.computeIfAbsent(playerId, k -> new HashMap<>());
            ResourceLocation moveId = result.resultMove().id();
            ImprovedMove improved = playerMoves.get(moveId);
            if (improved == null) {
                improved = new ImprovedMove(result.resultMove());
                playerMoves.put(moveId, improved);
            }
            for (int i = 0; i < result.improvementLevel(); i++) {
                if (!improved.canImprove()) break;
                improved.improve();
            }
        }
    }

    public static float calculateSuccessRate(ServerPlayer player, MoveBlueprint blueprint) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        DaoPath targetPath = blueprint.targetPath();

        float base = 0.3f;

        float daoMarkBonus = data.getDaoMarkBonus(targetPath) * 0.3f;

        float pathRealmBonus = aperture.getPathRealm(targetPath).getTier() * 0.08f;

        float rankBonus = aperture.getRank().getLevel() * 0.02f;

        float resonanceBonus = calculateBlueprintResonance(blueprint) - 1.0f;

        float experienceBonus = Math.min(
            getDeductionExp(player.getUUID(), targetPath) * 0.002f, 0.1f
        );

        float complexityPenalty = blueprint.supportGu().size() * 0.05f;

        int crossPathCount = 0;
        for (ResourceLocation guId : blueprint.getAllGu()) {
            GuType type = GuRegistry.get(guId);
            if (type != null && type.path() != targetPath) crossPathCount++;
        }
        float crossPathPenalty = crossPathCount * 0.1f;

        float rate = base + daoMarkBonus + pathRealmBonus + rankBonus
            + resonanceBonus + experienceBonus - complexityPenalty - crossPathPenalty;

        return Mth.clamp(rate, 0.05f, 0.95f);
    }

    private static float calculateBlueprintResonance(MoveBlueprint blueprint) {
        KillerMove tempMove = new KillerMove(
            ResourceLocation.fromNamespaceAndPath("reverend_insanity", "temp"),
            "temp", blueprint.targetPath(), 1,
            blueprint.coreGu(), blueprint.supportGu(),
            0, 0, 0, 0, KillerMove.MoveType.ATTACK
        );
        return DaoResonance.calculate(tempMove);
    }

    @Nullable
    public static KillerMove findMatchingMove(MoveBlueprint blueprint) {
        for (KillerMove move : KillerMoveRegistry.getAll()) {
            if (!move.coreGu().equals(blueprint.coreGu())) continue;

            List<ResourceLocation> required = move.supportGu();
            List<ResourceLocation> provided = blueprint.supportGu();
            if (provided.containsAll(required)) return move;
        }
        return null;
    }

    @Nullable
    public static ImprovedMove getImprovedMove(UUID playerId, ResourceLocation moveId) {
        Map<ResourceLocation, ImprovedMove> playerMoves = improvedMoves.get(playerId);
        return playerMoves != null ? playerMoves.get(moveId) : null;
    }

    public static Map<ResourceLocation, ImprovedMove> getAllImprovedMoves(UUID playerId) {
        return improvedMoves.getOrDefault(playerId, Collections.emptyMap());
    }

    public static void addDeductionExp(UUID playerId, DaoPath path, float amount) {
        Map<DaoPath, Float> playerExp = deductionExperience.computeIfAbsent(playerId, k -> new EnumMap<>(DaoPath.class));
        float current = playerExp.getOrDefault(path, 0f);
        playerExp.put(path, Math.min(current + amount, 1000f));
    }

    public static float getDeductionExp(UUID playerId, DaoPath path) {
        Map<DaoPath, Float> playerExp = deductionExperience.get(playerId);
        return playerExp != null ? playerExp.getOrDefault(path, 0f) : 0f;
    }

    public static boolean isDeducting(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }

    @Nullable
    public static DeductionSession getSession(UUID playerId) {
        return activeSessions.get(playerId);
    }

    public static void cancelDeduction(UUID playerId) {
        DeductionSession session = activeSessions.remove(playerId);
        if (session != null) session.cancel();
    }

    public static void savePlayerData(UUID playerId, CompoundTag tag) {
        Map<ResourceLocation, ImprovedMove> playerMoves = improvedMoves.get(playerId);
        if (playerMoves != null && !playerMoves.isEmpty()) {
            ListTag moveList = new ListTag();
            for (ImprovedMove move : playerMoves.values()) {
                moveList.add(move.save());
            }
            tag.put("improvedMoves", moveList);
        }

        Map<DaoPath, Float> playerExp = deductionExperience.get(playerId);
        if (playerExp != null && !playerExp.isEmpty()) {
            CompoundTag expTag = new CompoundTag();
            for (Map.Entry<DaoPath, Float> entry : playerExp.entrySet()) {
                expTag.putFloat(entry.getKey().name(), entry.getValue());
            }
            tag.put("deductionExp", expTag);
        }
    }

    public static void loadPlayerData(UUID playerId, CompoundTag tag) {
        if (tag.contains("improvedMoves")) {
            ListTag moveList = tag.getList("improvedMoves", Tag.TAG_COMPOUND);
            Map<ResourceLocation, ImprovedMove> playerMoves = new HashMap<>();
            for (int i = 0; i < moveList.size(); i++) {
                ImprovedMove move = ImprovedMove.load(moveList.getCompound(i));
                playerMoves.put(move.getBaseMoveId(), move);
            }
            improvedMoves.put(playerId, playerMoves);
        }

        if (tag.contains("deductionExp")) {
            CompoundTag expTag = tag.getCompound("deductionExp");
            Map<DaoPath, Float> playerExp = new EnumMap<>(DaoPath.class);
            for (String key : expTag.getAllKeys()) {
                try {
                    DaoPath path = DaoPath.valueOf(key);
                    playerExp.put(path, expTag.getFloat(key));
                } catch (Exception ignored) {}
            }
            deductionExperience.put(playerId, playerExp);
        }
    }

    public static void clearPlayer(UUID playerId) {
        activeSessions.remove(playerId);
    }
}
