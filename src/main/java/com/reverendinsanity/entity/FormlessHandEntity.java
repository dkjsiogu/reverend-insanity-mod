package com.reverendinsanity.entity;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 无相手实体：盗天魔尊偷蛊手段，伸入空窍抓取蛊虫，抓住后可被玩家打破夺回
public class FormlessHandEntity extends Mob {

    public enum HandState { APPROACHING, RETREATING }

    private HandState state = HandState.APPROACHING;
    private UUID ownerUUID;
    private UUID targetUUID;
    private int fingerCount = 1;
    private CompoundTag stolenGuData;
    private boolean guReturned = false;
    private int lifeTicks = 0;
    private static final int MAX_LIFE = 200;

    public FormlessHandEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public FormlessHandEntity(Level level, Entity owner, ServerPlayer target, int fingerCount) {
        super(ModEntities.FORMLESS_HAND.get(), level);
        this.ownerUUID = owner.getUUID();
        this.targetUUID = target.getUUID();
        this.fingerCount = Math.max(1, Math.min(5, fingerCount));
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void registerGoals() {}

    // ==================== TICK ====================

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;
        if (lifeTicks > MAX_LIFE) {
            safeReturnGu();
            this.discard();
            return;
        }

        if (!(this.level() instanceof ServerLevel sl)) return;

        if (state == HandState.APPROACHING) {
            tickApproaching(sl);
        } else {
            tickRetreating(sl);
        }
        spawnHandParticles(sl);
    }

    private void tickApproaching(ServerLevel sl) {
        ServerPlayer target = findTarget(sl);
        if (target == null) {
            this.discard();
            return;
        }

        double dx = target.getX() - this.getX();
        double dy = (target.getY() + 1.0) - this.getY();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist < 1.5) {
            stealFromAperture(target, sl);
        } else {
            double speed = 0.35;
            this.setDeltaMovement(dx / dist * speed, dy / dist * speed, dz / dist * speed);
        }
    }

    private void stealFromAperture(ServerPlayer player, ServerLevel sl) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        List<GuInstance> guList = new ArrayList<>(aperture.getStoredGu());

        List<GuInstance> eligible = new ArrayList<>();
        for (GuInstance gu : guList) {
            GuType type = GuRegistry.get(gu.getTypeId());
            if (type != null && type.rank() <= fingerCount) {
                eligible.add(gu);
            }
        }

        if (eligible.isEmpty()) {
            this.discard();
            return;
        }

        GuInstance stolen = eligible.get(this.random.nextInt(eligible.size()));
        aperture.removeGu(stolen);
        stolenGuData = stolen.save();
        state = HandState.RETREATING;

        sl.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2f, 1.8f);
        GuType guType = GuRegistry.get(stolen.getTypeId());
        String guName = guType != null ? guType.displayName() : "\u86ca\u866b";
        player.sendSystemMessage(Component.literal(
            "\u00a79\u00a7l[\u65e0\u76f8\u624b] \u00a7b\u6de1\u84dd\u8272\u7684\u624b\u4f38\u5165\u4f60\u7684\u7a7a\u7a8d\uff0c\u6293\u4f4f\u4e86" + guName + "\uff01"));

        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.5, player.getZ(),
            40, 1.0, 1.5, 1.0, 0.03);
    }

    private void tickRetreating(ServerLevel sl) {
        if (ownerUUID == null) {
            safeReturnGu();
            this.discard();
            return;
        }
        Entity owner = sl.getEntity(ownerUUID);
        if (owner == null || !owner.isAlive()) {
            safeReturnGu();
            this.discard();
            return;
        }

        double dx = owner.getX() - this.getX();
        double dy = (owner.getY() + 1.0) - this.getY();
        double dz = owner.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist < 2.0) {
            ServerPlayer player = findTarget(sl);
            if (player != null) {
                GuType guType = stolenGuData != null ? GuRegistry.get(ResourceLocation.parse(stolenGuData.getString("type"))) : null;
                String guName = guType != null ? guType.displayName() : "\u86ca\u866b";
                player.sendSystemMessage(Component.literal(
                    "\u00a79\u00a7l[\u65e0\u76f8\u624b] \u00a7c\u4f60\u7684" + guName + "\u88ab\u5077\u8d70\u4e86\uff01"));
            }
            guReturned = true;
            this.discard();
        } else {
            double speed = 0.25;
            this.setDeltaMovement(dx / dist * speed, dy / dist * speed, dz / dist * speed);
        }
    }

    // ==================== GU RETURN ====================

    private void safeReturnGu() {
        if (guReturned || stolenGuData == null) return;
        guReturned = true;
        if (!(this.level() instanceof ServerLevel sl)) return;

        ServerPlayer player = findTarget(sl);
        if (player == null || !player.isAlive()) return;

        GuInstance returned = GuInstance.load(stolenGuData);
        if (returned == null) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getAperture().addGu(returned);

        GuType guType = GuRegistry.get(returned.getTypeId());
        String guName = guType != null ? guType.displayName() : "\u86ca\u866b";
        player.sendSystemMessage(Component.literal(
            "\u00a79\u00a7l[\u65e0\u76f8\u624b] \u00a7a" + guName + "\u4ece\u7834\u788e\u7684\u624b\u4e2d\u98de\u56de\u4e86\u4f60\u7684\u7a7a\u7a8d\uff01"));

        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.5, player.getZ(),
            20, 0.5, 1.0, 0.5, 0.02);
    }

    // ==================== COMBAT ====================

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (state == HandState.APPROACHING) return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (state == HandState.APPROACHING) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    public void die(DamageSource source) {
        safeReturnGu();
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason != RemovalReason.DISCARDED || !guReturned) {
            safeReturnGu();
        }
        super.remove(reason);
    }

    // ==================== PARTICLES ====================

    private void spawnHandParticles(ServerLevel sl) {
        int count = state == HandState.RETREATING ? 8 : 5;
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
            this.getX(), this.getY() + 0.5, this.getZ(),
            count, 0.2, 0.3, 0.2, 0.01);

        double angle = (lifeTicks * 0.15) % (Math.PI * 2);
        for (int f = 0; f < fingerCount; f++) {
            double fa = angle + (Math.PI * 2 * f / fingerCount);
            sl.sendParticles(ParticleTypes.SOUL,
                this.getX() + Math.cos(fa) * 0.4, this.getY() + 0.6, this.getZ() + Math.sin(fa) * 0.4,
                1, 0.05, 0.05, 0.05, 0.0);
        }

        if (state == HandState.RETREATING) {
            sl.sendParticles(ParticleTypes.END_ROD,
                this.getX(), this.getY() + 0.4, this.getZ(),
                2, 0.1, 0.1, 0.1, 0.005);
        }
    }

    // ==================== SAVE/LOAD ====================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("HandState", state.name());
        tag.putInt("FingerCount", fingerCount);
        tag.putInt("LifeTicks", lifeTicks);
        tag.putBoolean("GuReturned", guReturned);
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        if (targetUUID != null) tag.putUUID("TargetUUID", targetUUID);
        if (stolenGuData != null) tag.put("StolenGu", stolenGuData.copy());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HandState")) {
            try { state = HandState.valueOf(tag.getString("HandState")); }
            catch (Exception e) { state = HandState.APPROACHING; }
        }
        fingerCount = Math.max(1, Math.min(5, tag.getInt("FingerCount")));
        lifeTicks = tag.getInt("LifeTicks");
        guReturned = tag.getBoolean("GuReturned");
        if (tag.hasUUID("OwnerUUID")) ownerUUID = tag.getUUID("OwnerUUID");
        if (tag.hasUUID("TargetUUID")) targetUUID = tag.getUUID("TargetUUID");
        if (tag.contains("StolenGu")) stolenGuData = tag.getCompound("StolenGu").copy();
    }

    // ==================== UTILITY ====================

    private ServerPlayer findTarget(ServerLevel sl) {
        if (targetUUID == null) return null;
        return sl.getServer().getPlayerList().getPlayer(targetUUID);
    }

    @Override
    public boolean canBeCollidedWith() { return true; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected boolean shouldDespawnInPeaceful() { return false; }
}
