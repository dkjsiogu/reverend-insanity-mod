package com.reverendinsanity.block.entity;

import com.reverendinsanity.registry.ModBlockEntities;
import com.reverendinsanity.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 福地方块实体，存储拥有者并管理福地区域效果
public class BlessedLandBlockEntity extends BlockEntity {

    public static final int RADIUS = 16;
    private static final Map<UUID, BlockPos> ACTIVE_BLESSED_LANDS = new ConcurrentHashMap<>();

    private UUID ownerUUID;
    private boolean active = true;

    public BlessedLandBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLESSED_LAND_CORE.get(), pos, state);
    }

    public void setOwner(UUID owner) {
        this.ownerUUID = owner;
        setChanged();
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
        if (ownerUUID != null) {
            BlockPos tracked = ACTIVE_BLESSED_LANDS.get(ownerUUID);
            if (tracked != null && tracked.equals(worldPosition)) {
                ACTIVE_BLESSED_LANDS.remove(ownerUUID);
            }
        }
        setChanged();
    }

    public void deactivateOtherBlessedLands(ServerLevel level) {
        if (ownerUUID == null) return;
        BlockPos oldPos = ACTIVE_BLESSED_LANDS.get(ownerUUID);
        if (oldPos != null && !oldPos.equals(worldPosition)) {
            BlockEntity be = level.getBlockEntity(oldPos);
            if (be instanceof BlessedLandBlockEntity other && other.active) {
                other.deactivate();
            }
        }
        ACTIVE_BLESSED_LANDS.put(ownerUUID, worldPosition);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlessedLandBlockEntity entity) {
        if (!entity.active || entity.ownerUUID == null) return;
        if (level.getGameTime() % 20 != 0) return;

        ServerLevel serverLevel = (ServerLevel) level;

        for (int i = 0; i < 3; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double radius = level.random.nextDouble() * RADIUS * 0.5;
            double px = pos.getX() + 0.5 + Math.cos(angle) * radius;
            double pz = pos.getZ() + 0.5 + Math.sin(angle) * radius;
            double py = pos.getY() + 0.5 + level.random.nextDouble() * 3.0;
            serverLevel.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0, 0.02, 0, 0.01);
        }

        if (level.getGameTime() % 60 == 0) {
            ResourceLocation slowId = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "blessed_land_slow");
            ResourceLocation weakId = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "blessed_land_weak");
            AABB area = new AABB(pos).inflate(RADIUS);
            for (Monster mob : serverLevel.getEntitiesOfClass(Monster.class, area)) {
                var spdAttr = mob.getAttribute(Attributes.MOVEMENT_SPEED);
                if (spdAttr != null) {
                    spdAttr.removeModifier(slowId);
                    spdAttr.addTransientModifier(new AttributeModifier(slowId, -0.03, AttributeModifier.Operation.ADD_VALUE));
                }
                var dmgAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
                if (dmgAttr != null) {
                    dmgAttr.removeModifier(weakId);
                    dmgAttr.addTransientModifier(new AttributeModifier(weakId, -4.0, AttributeModifier.Operation.ADD_VALUE));
                }
            }
        }
    }

    public boolean isInRange(BlockPos targetPos) {
        if (!active) return false;
        return worldPosition.closerThan(targetPos, RADIUS);
    }

    public static BlockPos getActiveBlessedLandPos(UUID ownerUUID) {
        if (ownerUUID == null) return null;
        return ACTIVE_BLESSED_LANDS.get(ownerUUID);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        tag.putBoolean("Active", active);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
        active = tag.getBoolean("Active");
        if (active && ownerUUID != null) {
            ACTIVE_BLESSED_LANDS.put(ownerUUID, worldPosition);
        }
    }
}
