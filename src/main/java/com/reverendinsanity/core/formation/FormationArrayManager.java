package com.reverendinsanity.core.formation;

import com.reverendinsanity.block.FormationStoneBlock;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.cultivation.Rank;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

// 蛊阵管理器，处理阵法石十字布阵的区域效果
public class FormationArrayManager {

    private static final List<ActiveFormation> activeFormations = new ArrayList<>();

    private static final ResourceLocation FORMATION_SLOW_MOD =
            ResourceLocation.fromNamespaceAndPath("reverend_insanity", "formation_slow");
    private static final ResourceLocation FORMATION_ARMOR_MOD =
            ResourceLocation.fromNamespaceAndPath("reverend_insanity", "formation_armor");

    public static boolean tryActivate(ServerPlayer player, BlockPos pos, ResourceLocation guId) {
        Level level = player.level();

        if (!checkCrossPattern(level, pos)) {
            player.displayClientMessage(Component.literal("阵法石不足，需要十字排列五块阵法石"), true);
            return false;
        }

        FormationType type = FormationType.fromGuId(guId);
        if (type == null) return false;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();

        if (type == FormationType.GRAND && aperture.getRank().getLevel() < 2) {
            player.displayClientMessage(Component.literal("境界不足，天地大阵需要二转以上"), true);
            return false;
        }

        if (aperture.findGuInstance(guId) == null) {
            player.displayClientMessage(Component.literal("空窍中没有对应的阵道蛊虫"), true);
            return false;
        }

        for (ActiveFormation af : activeFormations) {
            if (af.center.equals(pos)) {
                player.displayClientMessage(Component.literal("此处已有活跃阵法"), true);
                return false;
            }
        }

        ActiveFormation formation = new ActiveFormation();
        formation.center = pos;
        formation.type = type;
        formation.remainingTicks = type.duration;
        formation.ownerId = player.getUUID();
        formation.dimension = player.level().dimension();
        activeFormations.add(formation);

        setCrossActive(level, pos, true);

        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.2f);
        player.displayClientMessage(Component.literal(type.displayName + "布阵成功！"), true);
        return true;
    }

    private static boolean checkCrossPattern(Level level, BlockPos center) {
        BlockPos[] positions = {
                center,
                center.east(2), center.west(2),
                center.north(2), center.south(2)
        };
        for (BlockPos p : positions) {
            if (!(level.getBlockState(p).getBlock() instanceof FormationStoneBlock)) return false;
        }
        return true;
    }

    private static void setCrossActive(Level level, BlockPos center, boolean active) {
        BlockPos[] positions = {
                center,
                center.east(2), center.west(2),
                center.north(2), center.south(2)
        };
        for (BlockPos p : positions) {
            var state = level.getBlockState(p);
            if (state.getBlock() instanceof FormationStoneBlock) {
                level.setBlock(p, state.setValue(FormationStoneBlock.ACTIVE, active), 2);
            }
        }
    }

    public static void tickFormations(ServerLevel level) {
        Iterator<ActiveFormation> it = activeFormations.iterator();
        while (it.hasNext()) {
            ActiveFormation f = it.next();
            if (!f.dimension.equals(level.dimension())) continue;
            f.remainingTicks--;

            if (f.remainingTicks <= 0) {
                setCrossActive(level, f.center, false);
                it.remove();
                continue;
            }

            AABB area = new AABB(f.center).inflate(f.type.radius);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

            for (LivingEntity entity : entities) {
                boolean isOwnerOrAlly = entity instanceof Player;

                switch (f.type) {
                    case TRAP -> {
                        if (!isOwnerOrAlly) {
                            applySlowModifier(entity, -0.4);
                            if (f.remainingTicks % 60 == 0) {
                                entity.hurt(entity.damageSources().magic(), 2.0f);
                            }
                        }
                    }
                    case SHIELD -> {
                        if (isOwnerOrAlly) {
                            applyArmorModifier(entity, 4.0);
                        }
                    }
                    case GRAND -> {
                        if (!isOwnerOrAlly) {
                            applySlowModifier(entity, -0.3);
                            if (f.remainingTicks % 60 == 0) {
                                entity.hurt(entity.damageSources().magic(), 3.0f);
                            }
                        } else {
                            applyArmorModifier(entity, 6.0);
                            if (f.remainingTicks % 100 == 0) {
                                entity.heal(2.0f);
                            }
                        }
                    }
                }
            }

            removeModifiersOutOfRange(level, f);

            if (f.remainingTicks % 20 == 0) {
                spawnFormationParticles(level, f);
            }
        }
    }

    private static void applySlowModifier(LivingEntity entity, double amount) {
        var attr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null && !attr.hasModifier(FORMATION_SLOW_MOD)) {
            attr.addTransientModifier(new AttributeModifier(
                    FORMATION_SLOW_MOD, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void applyArmorModifier(LivingEntity entity, double amount) {
        var attr = entity.getAttribute(Attributes.ARMOR);
        if (attr != null && !attr.hasModifier(FORMATION_ARMOR_MOD)) {
            attr.addTransientModifier(new AttributeModifier(
                    FORMATION_ARMOR_MOD, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void removeModifiersOutOfRange(ServerLevel level, ActiveFormation f) {
        AABB extended = new AABB(f.center).inflate(f.type.radius + 4);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, extended);
        for (LivingEntity entity : nearby) {
            double dist = entity.blockPosition().distSqr(f.center);
            double maxDist = f.type.radius * f.type.radius;
            if (dist > maxDist) {
                var speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAttr != null) speedAttr.removeModifier(FORMATION_SLOW_MOD);
                var armorAttr = entity.getAttribute(Attributes.ARMOR);
                if (armorAttr != null) armorAttr.removeModifier(FORMATION_ARMOR_MOD);
            }
        }
    }

    private static void spawnFormationParticles(ServerLevel level, ActiveFormation f) {
        int color = f.type.color;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(r, g, b), 1.2f);

        double cx = f.center.getX() + 0.5;
        double cy = f.center.getY() + 1.0;
        double cz = f.center.getZ() + 0.5;

        for (int i = 0; i < 16; i++) {
            double angle = (i / 16.0) * Math.PI * 2;
            double px = cx + Math.cos(angle) * f.type.radius;
            double pz = cz + Math.sin(angle) * f.type.radius;
            level.sendParticles(dust, px, cy, pz, 1, 0.1, 0.2, 0.1, 0);
        }

        if (f.type == FormationType.GRAND) {
            DustParticleOptions goldDust = new DustParticleOptions(new Vector3f(1.0f, 0.85f, 0.2f), 1.5f);
            for (int i = 0; i < 8; i++) {
                double angle = (i / 8.0) * Math.PI * 2;
                double px = cx + Math.cos(angle) * (f.type.radius * 0.6);
                double pz = cz + Math.sin(angle) * (f.type.radius * 0.6);
                level.sendParticles(goldDust, px, cy + 0.5, pz, 1, 0.1, 0.3, 0.1, 0);
            }
        }
    }

    public static void clearPlayer(UUID playerId) {
        activeFormations.removeIf(f -> f.ownerId.equals(playerId));
    }

    public static class ActiveFormation {
        BlockPos center;
        FormationType type;
        int remainingTicks;
        UUID ownerId;
        ResourceKey<Level> dimension;
    }

    public enum FormationType {
        TRAP("困阵", "trap_formation_gu", 600, 8, 0xCC2222),
        SHIELD("盾阵", "formation_shield_gu", 600, 8, 0x4488CC),
        GRAND("天地大阵", "grand_formation_gu", 800, 12, 0xCCCC44);

        final String displayName;
        final String guIdStr;
        final int duration;
        final int radius;
        final int color;

        FormationType(String displayName, String guIdStr, int duration, int radius, int color) {
            this.displayName = displayName;
            this.guIdStr = guIdStr;
            this.duration = duration;
            this.radius = radius;
            this.color = color;
        }

        public static FormationType fromGuId(ResourceLocation guId) {
            String path = guId.getPath();
            for (FormationType ft : values()) {
                if (ft.guIdStr.equals(path)) return ft;
            }
            return null;
        }
    }
}
