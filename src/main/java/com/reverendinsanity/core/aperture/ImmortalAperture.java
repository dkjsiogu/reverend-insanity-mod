package com.reverendinsanity.core.aperture;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.*;

// 仙窍：六转以上蛊师的个人小世界，原著还原
// 天地二气、仙元石产出、光阴支流、发展度、漏洞、灾劫
public class ImmortalAperture {

    private boolean formed = false;
    private BlessedLandGrade grade = BlessedLandGrade.LOWER;
    private int daysSinceLastCalamity = 0;
    private int totalDays = 0;
    private int tickCounter = 0;
    private float storedHeavenQi = 0;
    private float storedEarthQi = 0;
    private float maxQi = 1000;
    private int immortalEssenceStones = 0;
    private float apertureIntegrity = 100;
    private final Map<DaoPath, Integer> apertureDaoMarks = new EnumMap<>(DaoPath.class);
    private final ApertureResourceManager resourceManager = new ApertureResourceManager();

    private float developmentLevel = 0;
    private int breachCount = 0;
    private int totalCalamitiesSurvived = 0;

    private boolean inAperture = false;
    private ResourceKey<Level> returnDimension = Level.OVERWORLD;
    private double returnX = 0, returnY = 64, returnZ = 0;
    private float returnYRot = 0, returnXRot = 0;

    public void form(Aperture aperture, GuMasterData data) {
        if (!aperture.getRank().isImmortal()) return;
        this.formed = true;
        this.grade = BlessedLandGrade.determine(aperture, data);
        this.maxQi = grade.getQiCapacity();
        this.storedHeavenQi = maxQi * 0.5f;
        this.storedEarthQi = maxQi * 0.5f;
        this.apertureIntegrity = 100;
        this.developmentLevel = 10;
        this.breachCount = 0;
        this.totalCalamitiesSurvived = 0;
    }

    public void tick(ServerPlayer player) {
        if (!formed) return;
        tickCounter++;

        float qiRate = 0.01f * (1 + grade.ordinal());
        storedHeavenQi = Math.min(storedHeavenQi + qiRate, maxQi);
        storedEarthQi = Math.min(storedEarthQi + qiRate, maxQi);

        Aperture aperture = player.getData(
            com.reverendinsanity.registry.ModAttachments.GU_MASTER_DATA.get()
        ).getAperture();
        float essenceBoost = aperture.getMaxEssence() * 0.003f;
        aperture.regenerateEssence(essenceBoost);

        if (breachCount > 0 && tickCounter % 200 == 0) {
            float qiLoss = breachCount * 0.5f;
            storedHeavenQi = Math.max(0, storedHeavenQi - qiLoss);
            storedEarthQi = Math.max(0, storedEarthQi - qiLoss);
        }

        if (tickCounter % 24000 == 0) {
            totalDays++;
            daysSinceLastCalamity++;
            produceEssenceStones();
            resourceManager.tickProduction(grade, apertureIntegrity, new Random());
            developmentLevel = Math.min(100, developmentLevel + getDevelopmentGrowth());
        }
    }

    private float getDevelopmentGrowth() {
        float base = 0.1f * (1 + grade.ordinal());
        float integrityFactor = apertureIntegrity / 100f;
        float breachPenalty = 1.0f - (breachCount * 0.1f);
        return base * integrityFactor * Math.max(0.1f, breachPenalty);
    }

    private void produceEssenceStones() {
        float devFactor = 0.5f + (developmentLevel / 100f) * 0.5f;
        int production = Math.round(grade.getEssencePerDay() * (apertureIntegrity / 100f) * devFactor);
        immortalEssenceStones += production;
    }

    public void absorbQi(float heaven, float earth) {
        storedHeavenQi = Math.min(storedHeavenQi + heaven, maxQi);
        storedEarthQi = Math.min(storedEarthQi + earth, maxQi);
    }

    public boolean consumeQi(float amount) {
        float half = amount / 2;
        if (storedHeavenQi >= half && storedEarthQi >= half) {
            storedHeavenQi -= half;
            storedEarthQi -= half;
            return true;
        }
        return false;
    }

    public int getDaoMark(DaoPath path) {
        return apertureDaoMarks.getOrDefault(path, 0);
    }

    public void addDaoMark(DaoPath path, int amount) {
        int current = apertureDaoMarks.getOrDefault(path, 0);
        apertureDaoMarks.put(path, Math.min(current + amount, 10000));
    }

    public int getTotalDaoMarks() {
        int total = 0;
        for (int v : apertureDaoMarks.values()) {
            total += v;
        }
        return total;
    }

    public Map<DaoPath, Integer> getAllDaoMarks() {
        return Collections.unmodifiableMap(new EnumMap<>(apertureDaoMarks));
    }

    public void takeDamage(float damage) {
        apertureIntegrity = Math.max(0, apertureIntegrity - damage);
    }

    public void repair(float amount) {
        float cost = amount * 10;
        if (consumeQi(cost)) {
            apertureIntegrity = Math.min(100, apertureIntegrity + amount);
        }
    }

    public void addBreach() {
        breachCount++;
    }

    public void repairBreach() {
        if (breachCount > 0 && consumeQi(200)) {
            breachCount--;
        }
    }

    public void onCalamitySurvived() {
        totalCalamitiesSurvived++;
    }

    public float getDevelopmentLevel() { return developmentLevel; }
    public void setDevelopmentLevel(float level) { this.developmentLevel = Math.max(0, Math.min(100, level)); }
    public int getBreachCount() { return breachCount; }
    public int getTotalCalamitiesSurvived() { return totalCalamitiesSurvived; }

    public boolean consumeEssenceStones(int amount) {
        if (immortalEssenceStones >= amount) {
            immortalEssenceStones -= amount;
            return true;
        }
        return false;
    }

    public void saveReturnPosition(ServerPlayer player) {
        this.returnDimension = player.level().dimension();
        this.returnX = player.getX();
        this.returnY = player.getY();
        this.returnZ = player.getZ();
        this.returnYRot = player.getYRot();
        this.returnXRot = player.getXRot();
    }

    public boolean isFormed() { return formed; }
    public BlessedLandGrade getGrade() { return grade; }
    public float getIntegrity() { return apertureIntegrity; }
    public float getStoredHeavenQi() { return storedHeavenQi; }
    public float getStoredEarthQi() { return storedEarthQi; }
    public float getMaxQi() { return maxQi; }
    public int getImmortalEssenceStones() { return immortalEssenceStones; }
    public int getDaysSinceLastCalamity() { return daysSinceLastCalamity; }
    public void resetCalamityTimer() { daysSinceLastCalamity = 0; }
    public ApertureResourceManager getResourceManager() { return resourceManager; }

    public boolean isInAperture() { return inAperture; }
    public void setInAperture(boolean val) { this.inAperture = val; }
    public ResourceKey<Level> getReturnDimension() { return returnDimension; }
    public Vec3 getReturnPos() { return new Vec3(returnX, returnY, returnZ); }
    public float getReturnYRot() { return returnYRot; }
    public float getReturnXRot() { return returnXRot; }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("formed", formed);
        tag.putString("grade", grade.name());
        tag.putInt("daysSinceLastCalamity", daysSinceLastCalamity);
        tag.putInt("totalDays", totalDays);
        tag.putInt("tickCounter", tickCounter);
        tag.putFloat("heavenQi", storedHeavenQi);
        tag.putFloat("earthQi", storedEarthQi);
        tag.putFloat("maxQi", maxQi);
        tag.putInt("essenceStones", immortalEssenceStones);
        tag.putFloat("integrity", apertureIntegrity);
        tag.putFloat("developmentLevel", developmentLevel);
        tag.putInt("breachCount", breachCount);
        tag.putInt("totalCalamitiesSurvived", totalCalamitiesSurvived);
        CompoundTag marksTag = new CompoundTag();
        for (Map.Entry<DaoPath, Integer> entry : apertureDaoMarks.entrySet()) {
            marksTag.putInt(entry.getKey().name(), entry.getValue());
        }
        tag.put("daoMarks", marksTag);
        tag.put("resources", resourceManager.save());
        tag.putBoolean("inAperture", inAperture);
        tag.putString("returnDim", returnDimension.location().toString());
        tag.putDouble("returnX", returnX);
        tag.putDouble("returnY", returnY);
        tag.putDouble("returnZ", returnZ);
        tag.putFloat("returnYRot", returnYRot);
        tag.putFloat("returnXRot", returnXRot);
        return tag;
    }

    public void load(CompoundTag tag) {
        this.formed = tag.getBoolean("formed");
        try {
            this.grade = BlessedLandGrade.valueOf(tag.getString("grade"));
        } catch (Exception e) {
            this.grade = BlessedLandGrade.LOWER;
        }
        this.daysSinceLastCalamity = tag.getInt("daysSinceLastCalamity");
        this.totalDays = tag.getInt("totalDays");
        this.tickCounter = tag.getInt("tickCounter");
        this.storedHeavenQi = tag.getFloat("heavenQi");
        this.storedEarthQi = tag.getFloat("earthQi");
        this.maxQi = tag.getFloat("maxQi");
        if (maxQi <= 0) maxQi = grade.getQiCapacity();
        this.immortalEssenceStones = tag.getInt("essenceStones");
        this.apertureIntegrity = tag.getFloat("integrity");
        if (apertureIntegrity <= 0 && !tag.contains("integrity")) apertureIntegrity = 100;
        this.developmentLevel = tag.getFloat("developmentLevel");
        if (developmentLevel <= 0 && formed && !tag.contains("developmentLevel")) developmentLevel = 10;
        this.breachCount = tag.getInt("breachCount");
        this.totalCalamitiesSurvived = tag.getInt("totalCalamitiesSurvived");
        apertureDaoMarks.clear();
        if (tag.contains("daoMarks")) {
            CompoundTag marksTag = tag.getCompound("daoMarks");
            for (String key : marksTag.getAllKeys()) {
                try {
                    DaoPath path = DaoPath.valueOf(key);
                    apertureDaoMarks.put(path, marksTag.getInt(key));
                } catch (Exception e) { }
            }
        }
        if (tag.contains("resources")) {
            resourceManager.load(tag.getCompound("resources"));
        }
        this.inAperture = tag.getBoolean("inAperture");
        if (tag.contains("returnDim")) {
            try {
                ResourceLocation dimLoc = ResourceLocation.parse(tag.getString("returnDim"));
                this.returnDimension = ResourceKey.create(Registries.DIMENSION, dimLoc);
            } catch (Exception e) {
                this.returnDimension = Level.OVERWORLD;
            }
        }
        this.returnX = tag.getDouble("returnX");
        this.returnY = tag.getDouble("returnY");
        this.returnZ = tag.getDouble("returnZ");
        this.returnYRot = tag.getFloat("returnYRot");
        this.returnXRot = tag.getFloat("returnXRot");
    }
}
