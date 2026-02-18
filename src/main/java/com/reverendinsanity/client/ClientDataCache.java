package com.reverendinsanity.client;

import com.reverendinsanity.network.SyncApertureContentsPayload;
import com.reverendinsanity.network.SyncCodexPayload;
import com.reverendinsanity.network.SyncDeductionScreenPayload;
import com.reverendinsanity.network.SyncGuMasterDataPayload;
import com.reverendinsanity.network.SyncImmortalAperturePayload;
import java.util.ArrayList;
import java.util.List;

// 客户端修炼数据缓存，存储从服务端同步来的蛊师状态
public class ClientDataCache {

    private static boolean opened = false;
    private static int rankLevel = 1;
    private static int subRankIndex = 0;
    private static String aptitudeName = "";
    private static float currentEssence = 0;
    private static float maxEssence = 100;
    private static float thoughts = 0;
    private static float maxThoughts = 100;
    private static int essenceColor = 0x00CC66;
    private static int guCount = 0;
    private static int equippedMoveCount = 0;
    private static float luck = 1.0f;
    private static String primaryPathName = "";
    private static int primaryPathMarks = 0;
    private static List<BuffDisplayInfo> activeBuffs = new ArrayList<>();
    private static String factionTierName = "";
    private static int factionColor = 0xCCCC44;
    private static String factionName = "";
    private static int meritPoints = 0;

    public record BuffDisplayInfo(String idPath, int remainingSeconds) {}

    private static List<SyncApertureContentsPayload.GuInfo> guList = new ArrayList<>();
    private static List<SyncApertureContentsPayload.MoveInfo> equippedMoveList = new ArrayList<>();
    private static List<SyncApertureContentsPayload.MoveInfo> availableMoveList = new ArrayList<>();

    public static void update(SyncGuMasterDataPayload payload) {
        opened = payload.opened();
        rankLevel = payload.rankLevel();
        subRankIndex = payload.subRankIndex();
        aptitudeName = payload.aptitudeName();
        currentEssence = payload.currentEssence();
        maxEssence = payload.maxEssence();
        thoughts = payload.thoughts();
        maxThoughts = payload.maxThoughts();
        essenceColor = payload.essenceColor();
        guCount = payload.guCount();
        equippedMoveCount = payload.equippedMoveCount();
        luck = payload.luck();
        String ppm = payload.primaryPathMarks();
        if (ppm != null && ppm.contains(":")) {
            String[] parts = ppm.split(":", 2);
            primaryPathName = parts[0];
            try { primaryPathMarks = Integer.parseInt(parts[1]); } catch (Exception e) { primaryPathMarks = 0; }
        } else {
            primaryPathName = "";
            primaryPathMarks = 0;
        }
        activeBuffs = parseBuffData(payload.activeBuffData());
        parseFactionData(payload.factionData());
        lifespan = payload.lifespan();
        maxLifespan = payload.maxLifespan();
        heavenWillAttention = (int) payload.heavenWillAttention();
        meritPoints = payload.meritPoints();
    }

    private static List<BuffDisplayInfo> parseBuffData(String data) {
        List<BuffDisplayInfo> result = new ArrayList<>();
        if (data == null || data.isEmpty()) return result;
        for (String entry : data.split(",")) {
            String[] parts = entry.split("\\|", 2);
            if (parts.length == 2) {
                try {
                    int ticks = Integer.parseInt(parts[1]);
                    result.add(new BuffDisplayInfo(parts[0], Math.max(1, ticks / 20)));
                } catch (NumberFormatException ignored) {}
            }
        }
        return result;
    }

    private static void parseFactionData(String data) {
        if (data == null || data.isEmpty()) {
            factionTierName = "";
            factionName = "";
            factionColor = 0xCCCC44;
            return;
        }
        int maxAbs = 0;
        String dominantFaction = "INDEPENDENT";
        int dominantValue = 0;
        for (String entry : data.split(",")) {
            String[] parts = entry.split(":", 2);
            if (parts.length == 2) {
                try {
                    int val = Integer.parseInt(parts[1]);
                    int abs = Math.abs(val);
                    if (abs > maxAbs || (parts[0].equals("INDEPENDENT") && abs == maxAbs)) {
                        maxAbs = abs;
                        dominantFaction = parts[0];
                        dominantValue = val;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        switch (dominantFaction) {
            case "RIGHTEOUS" -> { factionName = "正道"; factionColor = 0x44AAFF; }
            case "DEMONIC" -> { factionName = "魔道"; factionColor = 0xCC2222; }
            default -> { factionName = "散修"; factionColor = 0xCCCC44; }
        }

        if (dominantValue <= -500) factionTierName = "敌对";
        else if (dominantValue <= -100) factionTierName = "不友好";
        else if (dominantValue < 100) factionTierName = "中立";
        else if (dominantValue < 500) factionTierName = "友好";
        else factionTierName = "崇敬";
    }

    public static void updateContents(SyncApertureContentsPayload payload) {
        guList = new ArrayList<>(payload.guList());
        equippedMoveList = new ArrayList<>(payload.equippedMoves());
        availableMoveList = new ArrayList<>(payload.availableMoves());
    }

    public static boolean isOpened() { return opened; }
    public static int getRankLevel() { return rankLevel; }
    public static int getSubRankIndex() { return subRankIndex; }
    public static String getAptitudeName() { return aptitudeName; }
    public static float getCurrentEssence() { return currentEssence; }
    public static float getMaxEssence() { return maxEssence; }
    public static float getThoughts() { return thoughts; }
    public static float getMaxThoughts() { return maxThoughts; }
    public static int getEssenceColor() { return essenceColor; }
    public static int getGuCount() { return guCount; }
    public static int getEquippedMoveCount() { return equippedMoveCount; }
    public static float getLuck() { return luck; }
    public static String getPrimaryPathName() { return primaryPathName; }
    public static int getPrimaryPathMarks() { return primaryPathMarks; }
    public static List<BuffDisplayInfo> getActiveBuffs() { return activeBuffs; }
    public static String getFactionTierName() { return factionTierName; }
    public static int getFactionColor() { return factionColor; }
    public static String getFactionName() { return factionName; }
    public static int getMeritPoints() { return meritPoints; }
    public static List<SyncApertureContentsPayload.GuInfo> getGuList() { return guList; }
    public static List<SyncApertureContentsPayload.MoveInfo> getEquippedMoveList() { return equippedMoveList; }
    public static List<SyncApertureContentsPayload.MoveInfo> getAvailableMoveList() { return availableMoveList; }

    private static List<SyncCodexPayload.CodexEntry> codexEntries = new ArrayList<>();
    private static int codexDiscoveredCount = 0;

    public static void updateCodex(SyncCodexPayload payload) {
        codexEntries = new ArrayList<>(payload.allEntries());
        codexDiscoveredCount = payload.discoveredCount();
    }

    public static List<SyncCodexPayload.CodexEntry> getCodexEntries() { return codexEntries; }
    public static int getCodexDiscoveredCount() { return codexDiscoveredCount; }

    private static boolean deductionActive = false;
    private static float deductionProgress = 0;
    private static float deductionSuccessRate = 0;

    public static void updateDeduction(boolean active, float progress, float successRate) {
        deductionActive = active;
        deductionProgress = progress;
        deductionSuccessRate = successRate;
    }

    public static boolean isDeductionActive() { return deductionActive; }
    public static float getDeductionProgress() { return deductionProgress; }
    public static float getDeductionSuccessRate() { return deductionSuccessRate; }

    private static List<SyncDeductionScreenPayload.DeductionGuEntry> deductionGuList = new ArrayList<>();
    private static int lastDeductionOutcome = -1;
    private static String lastDeductionMoveName = "";
    private static String lastDeductionMessage = "";

    public static void updateDeductionScreen(SyncDeductionScreenPayload payload) {
        deductionGuList = new ArrayList<>(payload.guList());
        deductionActive = payload.deductionActive();
        deductionProgress = payload.progress();
        deductionSuccessRate = payload.successRate();
    }

    public static void updateDeductionResult(int outcome, String moveName, String message) {
        deductionActive = false;
        deductionProgress = 0;
        lastDeductionOutcome = outcome;
        lastDeductionMoveName = moveName;
        lastDeductionMessage = message;
    }

    public static void clearDeductionResult() {
        lastDeductionOutcome = -1;
        lastDeductionMoveName = "";
        lastDeductionMessage = "";
    }

    public static List<SyncDeductionScreenPayload.DeductionGuEntry> getDeductionGuList() { return deductionGuList; }
    public static int getLastDeductionOutcome() { return lastDeductionOutcome; }
    public static String getLastDeductionMoveName() { return lastDeductionMoveName; }
    public static String getLastDeductionMessage() { return lastDeductionMessage; }

    private static boolean immortalFormed = false;
    private static String immortalGradeName = "";
    private static float immortalIntegrity = 100;
    private static float immortalHeavenQi = 0;
    private static float immortalEarthQi = 0;
    private static float immortalMaxQi = 1000;
    private static int immortalEssenceStones = 0;
    private static boolean immortalCalamityActive = false;
    private static String immortalCalamityTypeName = "";
    private static float immortalCalamityProgress = 0;
    private static int immortalDaysSinceCalamity = 0;
    private static List<SyncImmortalAperturePayload.ResourceEntry> immortalResources = new ArrayList<>();
    private static List<SyncImmortalAperturePayload.DaoMarkEntry> immortalTopDaoMarks = new ArrayList<>();
    private static float immortalDevelopmentLevel = 0;
    private static int immortalBreachCount = 0;
    private static int immortalTotalCalamitiesSurvived = 0;
    private static int immortalTimeFlowRate = 1;

    public static void updateImmortalAperture(SyncImmortalAperturePayload payload) {
        immortalFormed = payload.formed();
        immortalGradeName = payload.gradeName();
        immortalIntegrity = payload.integrity();
        immortalHeavenQi = payload.heavenQi();
        immortalEarthQi = payload.earthQi();
        immortalMaxQi = payload.maxQi();
        immortalEssenceStones = payload.essenceStones();
        immortalCalamityActive = payload.calamityActive();
        immortalCalamityTypeName = payload.calamityTypeName();
        immortalCalamityProgress = payload.calamityProgress();
        immortalDaysSinceCalamity = payload.daysSinceLastCalamity();
        immortalResources = new ArrayList<>(payload.resources());
        immortalTopDaoMarks = new ArrayList<>(payload.topDaoMarks());
        immortalDevelopmentLevel = payload.developmentLevel();
        immortalBreachCount = payload.breachCount();
        immortalTotalCalamitiesSurvived = payload.totalCalamitiesSurvived();
        immortalTimeFlowRate = payload.timeFlowRate();
    }

    public static boolean isImmortalFormed() { return immortalFormed; }
    public static String getImmortalGradeName() { return immortalGradeName; }
    public static float getImmortalIntegrity() { return immortalIntegrity; }
    public static float getImmortalHeavenQi() { return immortalHeavenQi; }
    public static float getImmortalEarthQi() { return immortalEarthQi; }
    public static float getImmortalMaxQi() { return immortalMaxQi; }
    public static int getImmortalEssenceStones() { return immortalEssenceStones; }
    public static boolean isImmortalCalamityActive() { return immortalCalamityActive; }
    public static String getImmortalCalamityTypeName() { return immortalCalamityTypeName; }
    public static float getImmortalCalamityProgress() { return immortalCalamityProgress; }
    public static int getImmortalDaysSinceCalamity() { return immortalDaysSinceCalamity; }
    public static List<SyncImmortalAperturePayload.ResourceEntry> getImmortalResources() { return immortalResources; }
    public static List<SyncImmortalAperturePayload.DaoMarkEntry> getImmortalTopDaoMarks() { return immortalTopDaoMarks; }
    public static float getImmortalDevelopmentLevel() { return immortalDevelopmentLevel; }
    public static int getImmortalBreachCount() { return immortalBreachCount; }
    public static int getImmortalTotalCalamitiesSurvived() { return immortalTotalCalamitiesSurvived; }
    public static int getImmortalTimeFlowRate() { return immortalTimeFlowRate; }

    private static int lifespan = 0;
    private static int maxLifespan = 0;
    private static int heavenWillAttention = 0;
    private static String targetIntelInfo = "";
    private static int targetIntelLevel = 0;

    public static void updateLifespan(int life, int maxLife) {
        lifespan = life;
        maxLifespan = maxLife;
    }

    public static void updateHeavenWill(int attention) {
        heavenWillAttention = attention;
    }

    public static void updateTargetIntel(String info, int level) {
        targetIntelInfo = info;
        targetIntelLevel = level;
    }

    public static void clearTargetIntel() {
        targetIntelInfo = "";
        targetIntelLevel = 0;
    }

    public static int getLifespan() { return lifespan; }
    public static int getMaxLifespan() { return maxLifespan; }
    public static int getHeavenWillAttention() { return heavenWillAttention; }
    public static String getTargetIntelInfo() { return targetIntelInfo; }
    public static int getTargetIntelLevel() { return targetIntelLevel; }
}
