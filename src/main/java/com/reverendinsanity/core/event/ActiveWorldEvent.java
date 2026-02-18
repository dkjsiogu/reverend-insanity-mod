package com.reverendinsanity.core.event;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.entity.WildGuEntity;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;

// 活跃天地事件实例
public class ActiveWorldEvent {

    private final WorldEventType type;
    private int remainingTicks;
    private boolean guSpawned;
    private BeastTideEvent beastTide;

    public ActiveWorldEvent(WorldEventType type) {
        this.type = type;
        this.remainingTicks = type.getDuration();
        this.guSpawned = false;
    }

    public boolean tick(ServerLevel level) {
        remainingTicks--;
        if (remainingTicks <= 0) return true;

        switch (type) {
            case ESSENCE_STORM -> tickEssenceStorm(level);
            case DAO_MARK_SURGE -> tickDaoMarkSurge(level);
            case RARE_GU_EMERGENCE -> tickRareGuEmergence(level);
            case THOUGHTS_CLARITY -> tickThoughtsClarity(level);
            case HEAVEN_WRATH -> tickHeavenWrath(level);
            case BEAST_TIDE -> tickBeastTide(level);
        }
        return false;
    }

    private void tickEssenceStorm(ServerLevel level) {
        if (remainingTicks % 40 != 0) return;
        for (ServerPlayer player : level.players()) {
            Aperture ap = player.getData(ModAttachments.GU_MASTER_DATA.get()).getAperture();
            if (!ap.isOpened()) continue;
            level.sendParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 2.5, player.getZ(),
                8, 3.0, 2.0, 3.0, 0.5);
        }
    }

    private void tickDaoMarkSurge(ServerLevel level) {
        if (remainingTicks % 20 != 0) return;
        for (ServerPlayer player : level.players()) {
            Aperture ap = player.getData(ModAttachments.GU_MASTER_DATA.get()).getAperture();
            if (!ap.isOpened()) continue;
            double angle = (player.tickCount * 0.15) % (2 * Math.PI);
            for (int i = 0; i < 3; i++) {
                double a = angle + i * 2.094;
                double px = player.getX() + Math.cos(a) * 1.5;
                double pz = player.getZ() + Math.sin(a) * 1.5;
                level.sendParticles(ParticleTypes.END_ROD,
                    px, player.getY() + 1.0, pz, 1, 0, 0.1, 0, 0.01);
            }
        }
    }

    private void tickRareGuEmergence(ServerLevel level) {
        if (guSpawned) return;
        guSpawned = true;

        for (ServerPlayer player : level.players()) {
            int count = 3 + level.random.nextInt(3);
            for (int i = 0; i < count; i++) {
                double ox = (level.random.nextDouble() - 0.5) * 60;
                double oz = (level.random.nextDouble() - 0.5) * 60;
                int x = (int) (player.getX() + ox);
                int z = (int) (player.getZ() + oz);
                int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos spawnPos = new BlockPos(x, y, z);

                WildGuEntity gu = ModEntities.WILD_GU.get().create(level);
                if (gu == null) continue;
                gu.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, level.random.nextFloat() * 360, 0);

                java.util.List<String> rarePool = java.util.List.of(
                    "four_flavors_liquor_worm", "gold_light_worm", "iron_bone_gu",
                    "enslave_snake_gu", "moonscar_gu", "blood_wing_gu",
                    "gold_silkworm_gu", "giant_strength_gu", "ice_seal_gu",
                    "blazing_flame_gu", "petrify_gu", "gale_gu",
                    "thunderstorm_gu", "torrent_gu", "soul_crush_gu"
                );
                String chosen = rarePool.get(level.random.nextInt(rarePool.size()));
                gu.setGuTypeId(chosen);
                level.addFreshEntity(gu);

                level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5,
                    30, 0.3, 1.5, 0.3, 0.1);
            }
        }
    }

    private void tickThoughtsClarity(ServerLevel level) {
        if (remainingTicks % 30 != 0) return;
        for (ServerPlayer player : level.players()) {
            Aperture ap = player.getData(ModAttachments.GU_MASTER_DATA.get()).getAperture();
            if (!ap.isOpened()) continue;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 1.5, player.getZ(),
                3, 0.5, 0.5, 0.5, 0.02);
        }
    }

    private void tickHeavenWrath(ServerLevel level) {
        if (remainingTicks % 60 != 0) return;
        for (ServerPlayer player : level.players()) {
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                player.getX(), player.getY() + 2.0, player.getZ(),
                5, 2.0, 1.0, 2.0, 0.1);

            if (level.random.nextFloat() < 0.3f) {
                double ox = (level.random.nextDouble() - 0.5) * 20;
                double oz = (level.random.nextDouble() - 0.5) * 20;
                int lx = (int) (player.getX() + ox);
                int lz = (int) (player.getZ() + oz);
                int ly = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, lx, lz);
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(lx + 0.5, ly, lz + 0.5);
                    bolt.setVisualOnly(true);
                    level.addFreshEntity(bolt);
                }
            }
        }
    }

    private void tickBeastTide(ServerLevel level) {
        if (beastTide == null) {
            beastTide = new BeastTideEvent();
        }
        beastTide.tick(level);
        if (beastTide.isFinished()) {
            remainingTicks = 0;
        }
    }

    public ActiveWorldEvent(WorldEventType type, int remainingTicks, boolean guSpawned) {
        this.type = type;
        this.remainingTicks = remainingTicks;
        this.guSpawned = guSpawned;
    }

    public net.minecraft.nbt.CompoundTag save() {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("type", type.name());
        tag.putInt("remainingTicks", remainingTicks);
        tag.putBoolean("guSpawned", guSpawned);
        return tag;
    }

    public static ActiveWorldEvent load(net.minecraft.nbt.CompoundTag tag) {
        try {
            WorldEventType type = WorldEventType.valueOf(tag.getString("type"));
            return new ActiveWorldEvent(type, tag.getInt("remainingTicks"), tag.getBoolean("guSpawned"));
        } catch (Exception e) {
            return null;
        }
    }

    public WorldEventType getType() { return type; }
    public int getRemainingTicks() { return remainingTicks; }
}
