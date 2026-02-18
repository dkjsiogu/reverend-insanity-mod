package com.reverendinsanity.core.gu;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 蛊吞噬系统：牺牲一只蛊虫来强化另一只同道路蛊虫的熟练度
public class GuDevourManager {

    public static boolean devour(ServerPlayer player, int feedIndex, int targetIndex) {
        if (feedIndex == targetIndex) return false;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return false;

        var stored = aperture.getStoredGu();
        if (feedIndex < 0 || feedIndex >= stored.size()) return false;
        if (targetIndex < 0 || targetIndex >= stored.size()) return false;

        GuInstance feed = stored.get(feedIndex);
        GuInstance target = stored.get(targetIndex);

        GuType feedType = feed.getType();
        GuType targetType = target.getType();
        if (feedType == null || targetType == null) return false;

        float profGain;
        if (feedType.path() == targetType.path()) {
            profGain = 20f + feedType.rank() * 15f;
        } else {
            profGain = 5f + feedType.rank() * 5f;
        }

        if (feedType.rank() > targetType.rank()) {
            player.displayClientMessage(
                    Component.literal("低转蛊虫无法吞噬高转蛊虫").withStyle(ChatFormatting.RED), true);
            return false;
        }

        int removeIdx = Math.max(feedIndex, targetIndex);
        int keepIdx = Math.min(feedIndex, targetIndex);
        if (feedIndex > targetIndex) {
            aperture.removeGuAt(feedIndex);
        } else {
            aperture.removeGuAt(feedIndex);
            target = aperture.getStoredGu().get(targetIndex - 1);
        }

        target.addProficiency(profGain);

        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1, player.getZ(),
                    20, 0.5, 0.5, 0.5, 0.3);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.7f, 1.3f);

        player.displayClientMessage(
                Component.literal(feedType.displayName() + " 被吞噬！" + targetType.displayName() + " 熟练度 +" + (int) profGain)
                        .withStyle(ChatFormatting.DARK_PURPLE), false);

        return true;
    }
}
