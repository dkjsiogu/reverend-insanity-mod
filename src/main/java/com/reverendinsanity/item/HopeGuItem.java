package com.reverendinsanity.item;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.Aptitude;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.reverendinsanity.util.AdvancementHelper;
import org.joml.Vector3f;

import java.util.List;

// 希望蛊：开窍仪式专用，右键使用随机资质开窍，月兰花附近提升资质概率
public class HopeGuItem extends Item {

    public HopeGuItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            Aperture aperture = data.getAperture();

            if (aperture.isOpened()) {
                player.displayClientMessage(Component.literal("你已经开过窍了"), true);
                return InteractionResultHolder.fail(stack);
            }

            int orchidCount = countNearbyOrchids(level, player.blockPosition());
            boolean inFlowerField = orchidCount >= 8;

            float roll = level.getRandom().nextFloat();
            float orchidBonus = Math.min(orchidCount * 0.01f, 0.15f);

            Aptitude aptitude;
            if (inFlowerField) {
                if (roll < 0.20f) aptitude = Aptitude.D;
                else if (roll < 0.50f) aptitude = Aptitude.C;
                else if (roll < 0.80f) aptitude = Aptitude.B;
                else if (roll < 0.96f) aptitude = Aptitude.A;
                else aptitude = Aptitude.EXTREME;
            } else {
                float dThreshold = 0.40f - orchidBonus;
                float cThreshold = 0.70f - orchidBonus * 0.5f;
                float bThreshold = 0.90f - orchidBonus * 0.3f;
                float aThreshold = 0.98f - orchidBonus * 0.1f;
                if (roll < dThreshold) aptitude = Aptitude.D;
                else if (roll < cThreshold) aptitude = Aptitude.C;
                else if (roll < bThreshold) aptitude = Aptitude.B;
                else if (roll < aThreshold) aptitude = Aptitude.A;
                else aptitude = Aptitude.EXTREME;
            }

            ServerLevel serverLevel = (ServerLevel) level;

            player.displayClientMessage(
                Component.literal("希望蛊缓缓飞入体内...").withStyle(ChatFormatting.LIGHT_PURPLE), false);

            spawnCeremonyEffects(serverLevel, player);

            aperture.open(aptitude);
            stack.shrink(1);

            if (player instanceof ServerPlayer sp) {
                AdvancementHelper.grant(sp, "root");
                AdvancementHelper.grant(sp, "open_aperture");
                com.reverendinsanity.core.cultivation.BloodlineManager.assignBloodline(sp);
                GuMasterData dataForBlood = sp.getData(com.reverendinsanity.registry.ModAttachments.GU_MASTER_DATA.get());
                dataForBlood.setBloodlineId(com.reverendinsanity.core.cultivation.BloodlineManager.getBloodline(sp).id);
            }

            player.displayClientMessage(
                Component.literal("空窍开启！资质：" + aptitude.getDisplayName())
                    .withStyle(getAptitudeColor(aptitude)),
                false
            );

            player.displayClientMessage(
                Component.literal(getAptitudeFlavorText(aptitude))
                    .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY),
                false
            );

            if (inFlowerField) {
                player.displayClientMessage(
                    Component.literal("月兰花海的灵气助你提升了资质！")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
                    false
                );
            } else if (orchidCount > 0) {
                player.displayClientMessage(
                    Component.literal("附近的月兰花微微助力了开窍仪式")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),
                    false
                );
            }

            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private int countNearbyOrchids(Level level, BlockPos center) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-5, -3, -5), center.offset(5, 3, 5))) {
            if (level.getBlockState(pos).is(ModBlocks.MOON_ORCHID.get())
                || level.getBlockState(pos).is(ModBlocks.WILD_MOON_ORCHID.get())) {
                count++;
            }
        }
        return count;
    }

    private void spawnCeremonyEffects(ServerLevel level, Player player) {
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        level.playSound(null, player.blockPosition(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.2f);
        level.playSound(null, player.blockPosition(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);

        DustParticleOptions hopeParticle = new DustParticleOptions(
            new Vector3f(0.6f, 0.9f, 1.0f), 1.5f);
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI * 2 / 8;
            double radius = 2.0;
            double ex = px + Math.cos(angle) * radius;
            double ez = pz + Math.sin(angle) * radius;
            level.sendParticles(hopeParticle, ex, py + 0.5, ez, 3, 0.1, 0.3, 0.1, 0.02);
        }

        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
            px, py + 1.0, pz, 80, 0.5, 1.0, 0.5, 0.3);

        level.sendParticles(ParticleTypes.END_ROD,
            px, py + 0.5, pz, 30, 0.3, 1.5, 0.3, 0.05);

        DustParticleOptions spiralParticle = new DustParticleOptions(
            new Vector3f(0.9f, 0.8f, 1.0f), 1.2f);
        for (int h = 0; h < 20; h++) {
            double angle = h * Math.PI / 5;
            double r = 1.5 - h * 0.06;
            double ex = px + Math.cos(angle) * r;
            double ez = pz + Math.sin(angle) * r;
            double ey = py + h * 0.15;
            level.sendParticles(spiralParticle, ex, ey, ez, 2, 0.05, 0.05, 0.05, 0.01);
        }
    }

    private ChatFormatting getAptitudeColor(Aptitude aptitude) {
        return switch (aptitude) {
            case D -> ChatFormatting.GRAY;
            case C -> ChatFormatting.GREEN;
            case B -> ChatFormatting.AQUA;
            case A -> ChatFormatting.GOLD;
            case EXTREME -> ChatFormatting.LIGHT_PURPLE;
            default -> ChatFormatting.WHITE;
        };
    }

    private String getAptitudeFlavorText(Aptitude aptitude) {
        return switch (aptitude) {
            case D -> "丁等资质...路途艰辛，但并非没有希望。";
            case C -> "丙等资质，尚算中庸，勤能补拙。";
            case B -> "乙等资质，天赋不凡，前途可期。";
            case A -> "甲等资质！百年难遇之材！";
            case EXTREME -> "十绝体！千古罕见之资质！";
            default -> "";
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("右键使用进行开窍仪式").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("资质随机，不可逆转").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.literal("月兰花附近使用可提升资质").withStyle(ChatFormatting.AQUA));
    }
}
