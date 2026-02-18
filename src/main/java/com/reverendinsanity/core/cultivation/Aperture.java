package com.reverendinsanity.core.cultivation;

import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.core.path.PathRealm;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

// 空窍：蛊师修炼核心，存储真元和蛊虫
public class Aperture {

    private static final Logger LOGGER = LoggerFactory.getLogger(Aperture.class);

    private Rank rank = Rank.RANK_1;
    private SubRank subRank = SubRank.INITIAL;
    private Aptitude aptitude = Aptitude.D;
    private float currentEssence;
    private float maxEssence;
    private float thoughts;
    private float maxThoughts;
    private final List<GuInstance> storedGu = new ArrayList<>();
    private final Map<DaoPath, PathRealm> pathRealms = new EnumMap<>(DaoPath.class);
    private DaoPath primaryPath;
    private boolean opened = false;

    public Aperture() {
        recalculate();
    }

    public void open(Aptitude aptitude) {
        this.aptitude = aptitude;
        this.opened = true;
        this.rank = Rank.RANK_1;
        this.subRank = SubRank.INITIAL;
        recalculate();
        this.currentEssence = maxEssence;
        this.thoughts = maxThoughts;
    }

    private void recalculate() {
        this.maxEssence = rank.getBasePrimevalEssence() * aptitude.getEssenceRatio();
        this.maxThoughts = 100 + (rank.getLevel() - 1) * 50;
    }

    public boolean tryAdvanceSubRank() {
        SubRank next = subRank.next();
        if (next != null) {
            float cost = maxEssence * 0.3f;
            if (currentEssence >= cost) {
                currentEssence -= cost;
                subRank = next;
                recalculate();
                return true;
            }
        }
        return false;
    }

    public boolean tryAdvanceRank() {
        if (subRank != SubRank.PEAK) return false;
        Rank next = rank.next();
        if (next == null || !aptitude.canAdvanceTo(next)) return false;
        float cost = maxEssence * 0.8f;
        if (currentEssence >= cost) {
            currentEssence = 0;
            rank = next;
            subRank = SubRank.INITIAL;
            recalculate();
            return true;
        }
        return false;
    }

    public boolean consumeEssence(float amount) {
        if (currentEssence >= amount) {
            currentEssence -= amount;
            return true;
        }
        return false;
    }

    public boolean consumeThoughts(float amount) {
        if (thoughts >= amount) {
            thoughts -= amount;
            return true;
        }
        return false;
    }

    public void regenerateEssence(float amount) {
        currentEssence = Math.min(currentEssence + amount, maxEssence);
    }

    public void regenerateThoughts(float amount) {
        thoughts = Math.min(thoughts + amount, maxThoughts);
    }

    public void addGu(GuInstance gu) {
        storedGu.add(gu);
    }

    public boolean removeGu(GuInstance gu) {
        return storedGu.remove(gu);
    }

    public GuInstance removeGuAt(int index) {
        if (index >= 0 && index < storedGu.size()) {
            return storedGu.remove(index);
        }
        return null;
    }

    public EssenceGrade getEssenceGrade() {
        return EssenceGrade.of(rank, subRank);
    }

    public void setPathRealm(DaoPath path, PathRealm realm) {
        pathRealms.put(path, realm);
    }

    public PathRealm getPathRealm(DaoPath path) {
        return pathRealms.getOrDefault(path, PathRealm.ORDINARY);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("opened", opened);
        tag.putInt("rank", rank.getLevel());
        tag.putInt("subRank", subRank.getIndex());
        tag.putString("aptitude", aptitude.name());
        tag.putFloat("essence", currentEssence);
        tag.putFloat("thoughts", thoughts);
        if (primaryPath != null) {
            tag.putString("primaryPath", primaryPath.name());
        }
        CompoundTag pathTag = new CompoundTag();
        for (Map.Entry<DaoPath, PathRealm> entry : pathRealms.entrySet()) {
            pathTag.putInt(entry.getKey().name(), entry.getValue().getTier());
        }
        tag.put("pathRealms", pathTag);
        ListTag guList = new ListTag();
        for (GuInstance gu : storedGu) {
            guList.add(gu.save());
        }
        tag.put("storedGu", guList);
        return tag;
    }

    public void load(CompoundTag tag) {
        this.opened = tag.getBoolean("opened");
        this.rank = Rank.fromLevel(tag.getInt("rank"));
        if (this.rank == null) this.rank = Rank.RANK_1;
        this.subRank = SubRank.values()[Math.min(tag.getInt("subRank"), SubRank.values().length - 1)];
        try { this.aptitude = Aptitude.valueOf(tag.getString("aptitude")); } catch (Exception e) { this.aptitude = Aptitude.D; }
        this.currentEssence = tag.getFloat("essence");
        this.thoughts = tag.getFloat("thoughts");
        if (tag.contains("primaryPath")) {
            try { this.primaryPath = DaoPath.valueOf(tag.getString("primaryPath")); } catch (Exception e) { LOGGER.warn("Invalid primaryPath in NBT: {}", tag.getString("primaryPath")); }
        }
        pathRealms.clear();
        if (tag.contains("pathRealms")) {
            CompoundTag pathTag = tag.getCompound("pathRealms");
            for (String key : pathTag.getAllKeys()) {
                try {
                    DaoPath path = DaoPath.valueOf(key);
                    int tier = pathTag.getInt(key);
                    for (PathRealm pr : PathRealm.values()) {
                        if (pr.getTier() == tier) { pathRealms.put(path, pr); break; }
                    }
                } catch (Exception e) { LOGGER.warn("Invalid pathRealm entry in NBT: {}", key); }
            }
        }
        storedGu.clear();
        if (tag.contains("storedGu")) {
            ListTag guList = tag.getList("storedGu", Tag.TAG_COMPOUND);
            for (int i = 0; i < guList.size(); i++) {
                GuInstance gu = GuInstance.load(guList.getCompound(i));
                if (gu != null) storedGu.add(gu);
            }
        }
        recalculate();
    }

    public void setRank(Rank rank) {
        this.rank = rank;
        recalculate();
        this.currentEssence = Math.min(currentEssence, maxEssence);
    }

    public void setSubRank(SubRank subRank) {
        this.subRank = subRank;
    }

    public void setCurrentEssence(float amount) {
        this.currentEssence = Math.max(0, Math.min(amount, maxEssence));
    }

    public void setThoughts(float amount) {
        this.thoughts = Math.max(0, Math.min(amount, maxThoughts));
    }

    public void clearGu() {
        storedGu.clear();
    }

    public void reset() {
        this.opened = false;
        this.rank = Rank.RANK_1;
        this.subRank = SubRank.INITIAL;
        this.aptitude = Aptitude.D;
        this.currentEssence = 0;
        this.thoughts = 0;
        this.primaryPath = null;
        storedGu.clear();
        pathRealms.clear();
        recalculate();
    }

    public int getMaxGuCapacity() {
        return switch (rank.getLevel()) {
            case 1 -> 5;
            case 2 -> 8;
            case 3 -> 12;
            case 4 -> 16;
            case 5 -> 20;
            default -> 5;
        };
    }

    public boolean hasGuCapacity() {
        long aliveCount = storedGu.stream().filter(GuInstance::isAlive).count();
        return aliveCount < getMaxGuCapacity();
    }

    public GuInstance findGuInstance(net.minecraft.resources.ResourceLocation typeId) {
        for (GuInstance gu : storedGu) {
            if (gu.getTypeId().equals(typeId) && gu.isActive()) return gu;
        }
        return null;
    }

    public DaoPath getDominantPath() {
        Map<DaoPath, Integer> pathCounts = new EnumMap<>(DaoPath.class);
        for (GuInstance gu : storedGu) {
            if (!gu.isAlive()) continue;
            com.reverendinsanity.core.gu.GuType type = com.reverendinsanity.core.gu.GuRegistry.get(gu.getTypeId());
            if (type != null) {
                pathCounts.merge(type.path(), 1, Integer::sum);
            }
        }
        return pathCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    public float getPathCompatibility(DaoPath abilityPath) {
        if (storedGu.isEmpty()) return 1.0f;
        int samePathCount = 0;
        int totalAlive = 0;
        for (GuInstance gu : storedGu) {
            if (!gu.isAlive()) continue;
            totalAlive++;
            com.reverendinsanity.core.gu.GuType type = com.reverendinsanity.core.gu.GuRegistry.get(gu.getTypeId());
            if (type != null && type.path() == abilityPath) {
                samePathCount++;
            }
        }
        if (totalAlive == 0) return 1.0f;
        float sameRatio = (float) samePathCount / totalAlive;
        return 0.85f + sameRatio * 0.30f;
    }

    public boolean isOpened() { return opened; }
    public Rank getRank() { return rank; }
    public SubRank getSubRank() { return subRank; }
    public Aptitude getAptitude() { return aptitude; }
    public float getCurrentEssence() { return currentEssence; }
    public float getMaxEssence() { return maxEssence; }
    public float getThoughts() { return thoughts; }
    public float getMaxThoughts() { return maxThoughts; }
    public List<GuInstance> getStoredGu() { return Collections.unmodifiableList(storedGu); }
    public DaoPath getPrimaryPath() { return primaryPath; }
    public void setPrimaryPath(DaoPath path) { this.primaryPath = path; }
}
