package com.reverendinsanity.core.aperture.calamity;

import com.reverendinsanity.core.aperture.ImmortalAperture;
import com.reverendinsanity.world.dimension.ApertureDimensionManager;
import com.reverendinsanity.world.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import java.util.Random;

// 灾劫实例：一次正在发生的灾劫
public class Calamity {

    private final CalamityType type;
    private final int totalTicks;
    private int currentTick = 0;
    private boolean active = true;
    private float damageDealt = 0;
    private float damageReduced = 0;

    public Calamity(CalamityType type, int totalTicks) {
        this.type = type;
        this.totalTicks = totalTicks;
    }

    public void tick(ServerPlayer player, ImmortalAperture aperture) {
        if (!active || isFinished()) return;
        currentTick++;

        ServerLevel level = player.serverLevel();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        switch (type) {
            case EARTH_CRACK -> tickEarthCrack(player, level, x, y, z, aperture);
            case BEAST_TIDE -> tickBeastTide(player, level, x, y, z, aperture);
            case FIRE_SPREAD -> tickFireSpread(player, level, x, y, z, aperture);
            case VOID_EROSION -> tickVoidErosion(player, level, x, y, z, aperture);
            case THUNDER_TRIBULATION -> tickThunderTribulation(player, level, x, y, z, aperture);
            case SILVER_SERPENT -> tickSilverSerpent(player, level, x, y, z, aperture);
            case CHAOS_STORM -> tickChaosStorm(player, level, x, y, z, aperture);
        }

        if (player.level().dimension() == ModDimensions.APERTURE_DIM && player.getServer() != null) {
            tickDimensionEffect(player, level, aperture);
        }
    }

    private void tickEarthCrack(ServerPlayer player, ServerLevel level, double x, double y, double z, ImmortalAperture aperture) {
        if (currentTick % 60 == 0) {
            player.hurt(level.damageSources().magic(), 2.0f);
            applyDamage(aperture, 0.5f);
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 15, 2, 0.5, 2, 0.02);
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.WEATHER, 0.8f, 0.5f);
        }
    }

    private void tickBeastTide(ServerPlayer player, ServerLevel level, double x, double y, double z, ImmortalAperture aperture) {
        if (currentTick % 100 == 0) {
            for (int i = 0; i < 2; i++) {
                double ox = x + (level.random.nextDouble() - 0.5) * 8;
                double oz = z + (level.random.nextDouble() - 0.5) * 8;
                if (level.random.nextBoolean()) {
                    Zombie zombie = EntityType.ZOMBIE.create(level);
                    if (zombie != null) {
                        zombie.moveTo(ox, y, oz, level.random.nextFloat() * 360, 0);
                        level.addFreshEntity(zombie);
                    }
                } else {
                    Skeleton skeleton = EntityType.SKELETON.create(level);
                    if (skeleton != null) {
                        skeleton.moveTo(ox, y, oz, level.random.nextFloat() * 360, 0);
                        level.addFreshEntity(skeleton);
                    }
                }
            }
            applyDamage(aperture, 0.8f);
            level.playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.0f, 0.8f);
        }
    }

    private void tickFireSpread(ServerPlayer player, ServerLevel level, double x, double y, double z, ImmortalAperture aperture) {
        if (currentTick % 40 == 0) {
            player.igniteForTicks(60);
            player.hurt(level.damageSources().magic(), 1.5f);
            applyDamage(aperture, 0.3f);
            level.sendParticles(ParticleTypes.FLAME, x, y + 1, z, 20, 1.5, 1, 1.5, 0.05);
            level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.WEATHER, 0.7f, 1.2f);
        }
    }

    private void tickVoidErosion(ServerPlayer player, ServerLevel level, double x, double y, double z, ImmortalAperture aperture) {
        if (currentTick % 60 == 0) {
            applyDamage(aperture, 1.0f);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + 1, z, 30, 2, 2, 2, 0.1);
            level.sendParticles(ParticleTypes.PORTAL, x, y + 1, z, 20, 3, 2, 3, 0.5);
        }
    }

    private void tickThunderTribulation(ServerPlayer player, ServerLevel level, double x, double y, double z, ImmortalAperture aperture) {
        if (currentTick % 80 == 0) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(x, y, z);
                level.addFreshEntity(bolt);
            }
            player.hurt(level.damageSources().magic(), 5.0f);
            applyDamage(aperture, 2.0f);
        }
    }

    private void tickSilverSerpent(ServerPlayer player, ServerLevel level, double x, double y, double z, ImmortalAperture aperture) {
        if (currentTick % 120 == 0) {
            player.hurt(level.damageSources().magic(), 8.0f);
            applyDamage(aperture, 3.0f);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, x, y + 1, z, 40, 3, 2, 3, 0.1);
            level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.2f, 0.6f);
        }
    }

    private void tickChaosStorm(ServerPlayer player, ServerLevel level, double x, double y, double z, ImmortalAperture aperture) {
        if (currentTick % 60 == 0) {
            player.hurt(level.damageSources().magic(), 4.0f);
            applyDamage(aperture, 1.5f);

            if (currentTick % 120 == 0) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    double ox = x + (level.random.nextDouble() - 0.5) * 6;
                    double oz = z + (level.random.nextDouble() - 0.5) * 6;
                    bolt.moveTo(ox, y, oz);
                    level.addFreshEntity(bolt);
                }
            }
            player.igniteForTicks(40);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 1, z, 25, 3, 2, 3, 0.08);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 2, z, 15, 2, 1, 2, 0.1);
        }
    }

    private void tickDimensionEffect(ServerPlayer player, ServerLevel level, ImmortalAperture aperture) {
        ApertureDimensionManager mgr = ApertureDimensionManager.get(player.getServer());
        ApertureDimensionManager.PlayerSlot slot = mgr.getOrAssignSlot(player.getUUID(), aperture.getGrade().getRadius(), null);
        int cx = slot.centerX();
        int cz = slot.centerZ();
        int radius = aperture.getGrade().getFeatureRadius();
        Random rng = new Random();

        switch (type) {
            case FIRE_SPREAD -> {
                if (currentTick % 40 == 0) {
                    int fx = cx + rng.nextInt(radius * 2) - radius;
                    int fz = cz + rng.nextInt(radius * 2) - radius;
                    for (int fy = 80; fy >= 50; fy--) {
                        BlockPos pos = new BlockPos(fx, fy, fz);
                        if (level.getBlockState(pos).isAir() && !level.getBlockState(pos.below()).isAir()) {
                            level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                            level.sendParticles(ParticleTypes.LAVA, fx + 0.5, fy + 1, fz + 0.5, 5, 0.3, 0.5, 0.3, 0.02);
                            break;
                        }
                    }
                }
            }
            case EARTH_CRACK -> {
                if (currentTick % 60 == 0) {
                    int ex = cx + rng.nextInt(radius * 2) - radius;
                    int ez = cz + rng.nextInt(radius * 2) - radius;
                    for (int ey = 80; ey >= 50; ey--) {
                        BlockPos pos = new BlockPos(ex, ey, ez);
                        if (!level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, ex + 0.5, ey + 0.5, ez + 0.5, 8, 0.3, 0.3, 0.3, 0.02);
                            break;
                        }
                    }
                }
            }
            case BEAST_TIDE -> {
                if (currentTick % 100 == 0) {
                    double angle = rng.nextDouble() * Math.PI * 2;
                    double dist = radius * 0.7 + rng.nextDouble() * radius * 0.3;
                    double sx = cx + Math.cos(angle) * dist;
                    double sz = cz + Math.sin(angle) * dist;
                    for (int i = 0; i < 3; i++) {
                        double ox = sx + (rng.nextDouble() - 0.5) * 3;
                        double oz = sz + (rng.nextDouble() - 0.5) * 3;
                        if (rng.nextBoolean()) {
                            Zombie zombie = EntityType.ZOMBIE.create(level);
                            if (zombie != null) {
                                zombie.moveTo(ox, 65, oz, rng.nextFloat() * 360, 0);
                                level.addFreshEntity(zombie);
                            }
                        } else {
                            Skeleton skeleton = EntityType.SKELETON.create(level);
                            if (skeleton != null) {
                                skeleton.moveTo(ox, 65, oz, rng.nextFloat() * 360, 0);
                                level.addFreshEntity(skeleton);
                            }
                        }
                    }
                }
            }
            case VOID_EROSION -> {
                if (currentTick % 80 == 0) {
                    double angle = rng.nextDouble() * Math.PI * 2;
                    double dist = radius * 0.8 + rng.nextDouble() * radius * 0.2;
                    int vx = cx + (int)(Math.cos(angle) * dist);
                    int vz = cz + (int)(Math.sin(angle) * dist);
                    for (int vy = 80; vy >= 50; vy--) {
                        BlockPos pos = new BlockPos(vx, vy, vz);
                        if (!level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            level.sendParticles(ParticleTypes.REVERSE_PORTAL, vx + 0.5, vy + 0.5, vz + 0.5, 15, 0.5, 0.5, 0.5, 0.1);
                            break;
                        }
                    }
                }
            }
            case THUNDER_TRIBULATION -> {
                if (currentTick % 30 == 0) {
                    int lx = cx + rng.nextInt(radius * 2) - radius;
                    int lz = cz + rng.nextInt(radius * 2) - radius;
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                    if (bolt != null) {
                        bolt.moveTo(lx, 65, lz);
                        bolt.setVisualOnly(false);
                        level.addFreshEntity(bolt);
                    }
                }
            }
            case SILVER_SERPENT -> {
                if (currentTick % 20 == 0) {
                    int count = 2 + rng.nextInt(3);
                    for (int i = 0; i < count; i++) {
                        int lx = cx + rng.nextInt(radius * 2) - radius;
                        int lz = cz + rng.nextInt(radius * 2) - radius;
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                        if (bolt != null) {
                            bolt.moveTo(lx, 65, lz);
                            bolt.setVisualOnly(false);
                            level.addFreshEntity(bolt);
                        }
                    }
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, cx, 75, cz, 30, radius, 5, radius, 0.1);
                }
            }
            case CHAOS_STORM -> {
                if (currentTick % 40 == 0) {
                    int dx = cx + rng.nextInt(radius * 2) - radius;
                    int dz = cz + rng.nextInt(radius * 2) - radius;
                    for (int dy = 80; dy >= 50; dy--) {
                        BlockPos pos = new BlockPos(dx, dy, dz);
                        if (!level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            break;
                        }
                    }
                }
                if (currentTick % 60 == 0) {
                    player.hurt(level.damageSources().magic(), 2.0f);
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1, player.getZ(), 60, 2, 2, 2, 0.08);
                    level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, player.getX(), player.getY(), player.getZ(), 20, 1.5, 0.5, 1.5, 0.02);
                }
                if (currentTick % 50 == 0) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                    if (bolt != null) {
                        bolt.moveTo(cx + rng.nextInt(radius * 2) - radius, 65, cz + rng.nextInt(radius * 2) - radius);
                        bolt.setVisualOnly(false);
                        level.addFreshEntity(bolt);
                    }
                }
            }
        }
    }

    private void applyDamage(ImmortalAperture aperture, float amount) {
        float actual = Math.max(0, amount - damageReduced);
        damageReduced = Math.max(0, damageReduced - amount);
        if (actual > 0) {
            damageDealt += actual;
            aperture.takeDamage(actual);
        }
    }

    public void resist(float amount) {
        damageReduced += amount;
    }

    public boolean isFinished() {
        return currentTick >= totalTicks || !active;
    }

    public void cancel() {
        this.active = false;
    }

    public float getProgress() {
        return (float) currentTick / totalTicks;
    }

    public float getFinalDamage() {
        return damageDealt;
    }

    public Calamity(CalamityType type, int totalTicks, int currentTick, float damageDealt, float damageReduced) {
        this.type = type;
        this.totalTicks = totalTicks;
        this.currentTick = currentTick;
        this.damageDealt = damageDealt;
        this.damageReduced = damageReduced;
    }

    public net.minecraft.nbt.CompoundTag save() {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("type", type.name());
        tag.putInt("totalTicks", totalTicks);
        tag.putInt("currentTick", currentTick);
        tag.putFloat("damageDealt", damageDealt);
        tag.putFloat("damageReduced", damageReduced);
        return tag;
    }

    public static Calamity load(net.minecraft.nbt.CompoundTag tag) {
        try {
            CalamityType type = CalamityType.valueOf(tag.getString("type"));
            return new Calamity(type, tag.getInt("totalTicks"), tag.getInt("currentTick"),
                tag.getFloat("damageDealt"), tag.getFloat("damageReduced"));
        } catch (Exception e) {
            return null;
        }
    }

    public CalamityType getType() { return type; }
    public int getCurrentTick() { return currentTick; }
    public int getTotalTicks() { return totalTicks; }
    public boolean isActive() { return active; }
}
