package com.reverendinsanity.world.dimension;

import com.reverendinsanity.core.aperture.ImmortalAperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 仙窍维度管理器：玩家坐标分配(10万格间距)、进出传送、岛屿注册、SavedData持久化
public class ApertureDimensionManager extends SavedData {

    private static final String DATA_NAME = "reverend_insanity_aperture_slots";

    public record PlayerSlot(int centerX, int centerZ, int radius, boolean terrainGenerated, String primaryPathName) {
        public DaoPath getPrimaryPath() {
            if (primaryPathName == null || primaryPathName.isEmpty()) return null;
            try { return DaoPath.valueOf(primaryPathName); } catch (Exception e) { return null; }
        }
    }

    private final Map<UUID, PlayerSlot> slots = new HashMap<>();
    private int nextSlotIndex = 0;

    public static ApertureDimensionManager get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(ApertureDimensionManager::new, ApertureDimensionManager::load),
            DATA_NAME
        );
    }

    public PlayerSlot getOrAssignSlot(UUID playerId, int radius, DaoPath primaryPath) {
        PlayerSlot existing = slots.get(playerId);
        if (existing != null) {
            if (primaryPath != null && !primaryPath.name().equals(existing.primaryPathName)) {
                PlayerSlot updated = new PlayerSlot(existing.centerX(), existing.centerZ(), existing.radius(), existing.terrainGenerated(), primaryPath.name());
                slots.put(playerId, updated);
                setDirty();
                ApertureChunkGenerator.registerIsland(updated.centerX(), updated.centerZ(), updated.radius(), primaryPath);
                return updated;
            }
            ApertureChunkGenerator.registerIsland(existing.centerX(), existing.centerZ(), existing.radius(), existing.getPrimaryPath());
            return existing;
        }
        int gridX = (nextSlotIndex % 100) * ApertureChunkGenerator.GRID_SPACING;
        int gridZ = (nextSlotIndex / 100) * ApertureChunkGenerator.GRID_SPACING;
        nextSlotIndex++;
        String pathName = primaryPath != null ? primaryPath.name() : "";
        PlayerSlot slot = new PlayerSlot(gridX, gridZ, radius, false, pathName);
        slots.put(playerId, slot);
        ApertureChunkGenerator.registerIsland(gridX, gridZ, radius, primaryPath);
        setDirty();
        return slot;
    }

    public void markTerrainGenerated(UUID playerId) {
        PlayerSlot old = slots.get(playerId);
        if (old != null) {
            slots.put(playerId, new PlayerSlot(old.centerX(), old.centerZ(), old.radius(), true, old.primaryPathName()));
            setDirty();
        }
    }

    private void registerAllIslands() {
        ApertureChunkGenerator.clearIslands();
        for (PlayerSlot slot : slots.values()) {
            ApertureChunkGenerator.registerIsland(slot.centerX(), slot.centerZ(), slot.radius(), slot.getPrimaryPath());
        }
    }

    public static void enterAperture(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        ServerLevel apertureLevel = server.getLevel(ModDimensions.APERTURE_DIM);
        if (apertureLevel == null) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        ImmortalAperture ap = data.getImmortalAperture();
        ap.saveReturnPosition(player);

        DaoPath primaryPath = computePrimaryPath(ap.getAllDaoMarks());

        ApertureDimensionManager mgr = get(server);
        int radius = ap.getGrade().getRadius();
        PlayerSlot slot = mgr.getOrAssignSlot(player.getUUID(), radius, primaryPath);

        if (!slot.terrainGenerated()) {
            ApertureTerrainBuilder.generate(apertureLevel,
                new BlockPos(slot.centerX(), 64, slot.centerZ()),
                ap.getGrade(), ap.getAllDaoMarks());
            mgr.markTerrainGenerated(player.getUUID());
        }

        Vec3 targetPos = new Vec3(slot.centerX() + 0.5, 65, slot.centerZ() + 0.5);
        DimensionTransition transition = new DimensionTransition(
            apertureLevel, targetPos, Vec3.ZERO, player.getYRot(), player.getXRot(),
            DimensionTransition.PLAY_PORTAL_SOUND);
        player.changeDimension(transition);
        ap.setInAperture(true);
    }

    private static DaoPath computePrimaryPath(Map<DaoPath, Integer> daoMarks) {
        if (daoMarks == null || daoMarks.isEmpty()) return null;
        DaoPath best = null;
        int bestCount = 0;
        for (Map.Entry<DaoPath, Integer> entry : daoMarks.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                best = entry.getKey();
            }
        }
        return best;
    }

    public static void exitAperture(ServerPlayer player) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        ImmortalAperture ap = data.getImmortalAperture();

        ServerLevel returnLevel = player.getServer().getLevel(ap.getReturnDimension());
        if (returnLevel == null) returnLevel = player.server.overworld();

        DimensionTransition transition = new DimensionTransition(
            returnLevel, ap.getReturnPos(), Vec3.ZERO,
            ap.getReturnYRot(), ap.getReturnXRot(),
            DimensionTransition.PLAY_PORTAL_SOUND);
        player.changeDimension(transition);
        ap.setInAperture(false);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("nextSlotIndex", nextSlotIndex);
        ListTag slotList = new ListTag();
        for (Map.Entry<UUID, PlayerSlot> entry : slots.entrySet()) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putUUID("player", entry.getKey());
            slotTag.putInt("centerX", entry.getValue().centerX());
            slotTag.putInt("centerZ", entry.getValue().centerZ());
            slotTag.putInt("radius", entry.getValue().radius());
            slotTag.putBoolean("terrainGenerated", entry.getValue().terrainGenerated());
            if (entry.getValue().primaryPathName() != null && !entry.getValue().primaryPathName().isEmpty()) {
                slotTag.putString("primaryPath", entry.getValue().primaryPathName());
            }
            slotList.add(slotTag);
        }
        tag.put("slots", slotList);
        return tag;
    }

    public static ApertureDimensionManager load(CompoundTag tag, HolderLookup.Provider registries) {
        ApertureDimensionManager mgr = new ApertureDimensionManager();
        mgr.nextSlotIndex = tag.getInt("nextSlotIndex");
        ListTag slotList = tag.getList("slots", Tag.TAG_COMPOUND);
        for (int i = 0; i < slotList.size(); i++) {
            CompoundTag slotTag = slotList.getCompound(i);
            UUID playerId = slotTag.getUUID("player");
            int centerX = slotTag.getInt("centerX");
            int centerZ = slotTag.getInt("centerZ");
            int radius = slotTag.contains("radius") ? slotTag.getInt("radius") : 1500;
            boolean terrainGenerated = slotTag.getBoolean("terrainGenerated");
            String primaryPath = slotTag.contains("primaryPath") ? slotTag.getString("primaryPath") : "";
            mgr.slots.put(playerId, new PlayerSlot(centerX, centerZ, radius, terrainGenerated, primaryPath));
        }
        mgr.registerAllIslands();
        return mgr;
    }
}
