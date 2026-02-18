package com.reverendinsanity.core.cultivation;

import com.reverendinsanity.core.aperture.ImmortalAperture;
import com.reverendinsanity.core.combat.CombatState;
import com.reverendinsanity.core.combat.buff.GuBuffManager;
import com.reverendinsanity.core.deduction.DeductionManager;
import com.reverendinsanity.core.faction.FactionReputation;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

// 蛊师数据：附加到玩家的完整修炼状态，含仙窍
public class GuMasterData {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuMasterData.class);

    private final Aperture aperture;
    private final CombatState combatState;
    private final GuBuffManager buffManager;
    private final ImmortalAperture immortalAperture;
    private final Set<ResourceLocation> discoveredGu;
    private final Map<DaoPath, Integer> daoMarks = new EnumMap<>(DaoPath.class);
    private final FactionReputation factionReputation = new FactionReputation();
    private final Map<String, Float> permanentStats = new HashMap<>();
    private float luck = 1.0f;
    private int lifespan = 0;
    private int bloodlineId = 0;
    private UUID playerUUID;

    public GuMasterData() {
        this.aperture = new Aperture();
        this.combatState = new CombatState(aperture);
        this.buffManager = new GuBuffManager();
        this.immortalAperture = new ImmortalAperture();
        this.discoveredGu = new HashSet<>();
    }

    public void tick() {
        if (!aperture.isOpened()) return;
        aperture.getStoredGu().forEach(gu -> gu.tick());
        combatState.tick();
    }

    public Aperture getAperture() { return aperture; }
    public CombatState getCombatState() { return combatState; }
    public GuBuffManager getBuffManager() { return buffManager; }
    public ImmortalAperture getImmortalAperture() { return immortalAperture; }
    public FactionReputation getFactionReputation() { return factionReputation; }

    public UUID getPlayerUUID() { return playerUUID; }
    public void setPlayerUUID(UUID uuid) { this.playerUUID = uuid; }

    public void discoverGu(ResourceLocation guTypeId) {
        discoveredGu.add(guTypeId);
    }

    public boolean hasDiscovered(ResourceLocation guTypeId) {
        return discoveredGu.contains(guTypeId);
    }

    public Set<ResourceLocation> getDiscoveredGu() {
        return Collections.unmodifiableSet(discoveredGu);
    }

    public float getLuck() { return luck; }

    public void setLuck(float luck) { this.luck = Math.max(0, Math.min(2.0f, luck)); }

    public void reduceLuck(float amount) { setLuck(this.luck - amount); }

    public int getLifespan() { return lifespan; }
    public void setLifespan(int lifespan) { this.lifespan = Math.max(0, lifespan); }
    public void consumeLifespan(int amount) { this.lifespan = Math.max(0, this.lifespan - amount); }
    public void addLifespan(int amount) { this.lifespan += amount; }

    public int getBloodlineId() { return bloodlineId; }
    public void setBloodlineId(int id) { this.bloodlineId = id; }

    public float getPermanentStat(String key) {
        return permanentStats.getOrDefault(key, 0f);
    }

    public void addPermanentStat(String key, float amount) {
        permanentStats.merge(key, amount, Float::sum);
    }

    public Map<String, Float> getAllPermanentStats() {
        return Collections.unmodifiableMap(permanentStats);
    }

    public void addDaoMarks(DaoPath path, int amount) {
        int rank = aperture.getRank().getLevel();
        if (rank < 6) amount = Math.max(1, amount / 5);
        int current = daoMarks.getOrDefault(path, 0);
        daoMarks.put(path, Math.min(current + amount, 10000));
    }

    public int getDaoMarks(DaoPath path) {
        return daoMarks.getOrDefault(path, 0);
    }

    public float getDaoMarkBonus(DaoPath path) {
        return Math.min(getDaoMarks(path) / 1000.0f, 1.0f);
    }

    public Map<DaoPath, Integer> getAllDaoMarks() {
        return Collections.unmodifiableMap(daoMarks);
    }

    public int getTotalDaoMarks() {
        return daoMarks.values().stream().mapToInt(Integer::intValue).sum();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("aperture", aperture.save());
        ListTag discoveredList = new ListTag();
        for (ResourceLocation id : discoveredGu) {
            discoveredList.add(StringTag.valueOf(id.toString()));
        }
        tag.put("discoveredGu", discoveredList);
        tag.putFloat("luck", luck);
        tag.putInt("lifespan", lifespan);
        tag.putInt("bloodlineId", bloodlineId);
        CompoundTag daoMarksTag = new CompoundTag();
        for (Map.Entry<DaoPath, Integer> entry : daoMarks.entrySet()) {
            daoMarksTag.putInt(entry.getKey().name(), entry.getValue());
        }
        tag.put("daoMarks", daoMarksTag);
        CompoundTag permTag = new CompoundTag();
        for (Map.Entry<String, Float> entry : permanentStats.entrySet()) {
            permTag.putFloat(entry.getKey(), entry.getValue());
        }
        tag.put("permanentStats", permTag);
        tag.put("immortalAperture", immortalAperture.save());
        tag.put("factionReputation", factionReputation.save());
        if (playerUUID != null) {
            tag.putUUID("playerUUID", playerUUID);
            CompoundTag deductionTag = new CompoundTag();
            DeductionManager.savePlayerData(playerUUID, deductionTag);
            tag.put("deduction", deductionTag);
            CompoundTag calamityTag = new CompoundTag();
            com.reverendinsanity.core.aperture.calamity.CalamityManager.savePlayerData(playerUUID, calamityTag);
            tag.put("calamity", calamityTag);
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("aperture")) {
            aperture.load(tag.getCompound("aperture"));
        }
        if (tag.contains("discoveredGu")) {
            ListTag list = tag.getList("discoveredGu", Tag.TAG_STRING);
            discoveredGu.clear();
            for (int i = 0; i < list.size(); i++) {
                discoveredGu.add(ResourceLocation.parse(list.getString(i)));
            }
        }
        this.luck = tag.getFloat("luck");
        if (luck == 0 && !tag.contains("luck")) luck = 1.0f;
        this.lifespan = tag.getInt("lifespan");
        this.bloodlineId = tag.getInt("bloodlineId");
        daoMarks.clear();
        if (tag.contains("daoMarks")) {
            CompoundTag daoMarksTag = tag.getCompound("daoMarks");
            for (String key : daoMarksTag.getAllKeys()) {
                try {
                    DaoPath path = DaoPath.valueOf(key);
                    daoMarks.put(path, daoMarksTag.getInt(key));
                } catch (Exception e) { LOGGER.warn("Invalid daoMark entry in NBT: {}", key); }
            }
        }
        permanentStats.clear();
        if (tag.contains("permanentStats")) {
            CompoundTag permTag = tag.getCompound("permanentStats");
            for (String key : permTag.getAllKeys()) {
                permanentStats.put(key, permTag.getFloat(key));
            }
        }
        if (tag.contains("immortalAperture")) {
            immortalAperture.load(tag.getCompound("immortalAperture"));
        }
        if (tag.contains("factionReputation")) {
            factionReputation.load(tag.getCompound("factionReputation"));
        }
        if (tag.hasUUID("playerUUID")) {
            this.playerUUID = tag.getUUID("playerUUID");
            if (tag.contains("deduction")) {
                DeductionManager.loadPlayerData(playerUUID, tag.getCompound("deduction"));
            }
            if (tag.contains("calamity")) {
                com.reverendinsanity.core.aperture.calamity.CalamityManager.loadPlayerData(playerUUID, tag.getCompound("calamity"));
            }
        }
    }
}
