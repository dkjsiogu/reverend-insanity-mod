package com.reverendinsanity.core.path;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 道痕积累追踪器：技能使用时自动积累道痕，道痕达标自动提升流派境界
public class DaoMarkTracker {

    private static final int[] REALM_THRESHOLDS = { 100, 500, 1500, 3000, 5000, 8000 };
    private static final PathRealm[] REALM_VALUES = {
        PathRealm.MASTER, PathRealm.GRANDMASTER, PathRealm.GREAT_GRANDMASTER,
        PathRealm.QUASI_SUPREME, PathRealm.SUPREME, PathRealm.DAO_LORD
    };

    public static void onAbilityUsed(ServerPlayer player, DaoPath path) {
        if (path == null) return;
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return;

        int amount = 5;
        if (aperture.getPrimaryPath() == path) {
            amount += 3;
        }

        if (com.reverendinsanity.core.event.WorldEventManager.isEventActive(player.level(),
                com.reverendinsanity.core.event.WorldEventType.DAO_MARK_SURGE)) {
            amount *= 2;
        }

        data.addDaoMarks(path, amount);

        int marks = data.getDaoMarks(path);
        PathRealm currentRealm = aperture.getPathRealm(path);

        for (int i = REALM_THRESHOLDS.length - 1; i >= 0; i--) {
            if (marks >= REALM_THRESHOLDS[i] && currentRealm.getTier() < REALM_VALUES[i].getTier()) {
                aperture.setPathRealm(path, REALM_VALUES[i]);
                player.displayClientMessage(
                    Component.literal(path.getDisplayName() + " 境界提升至 " + REALM_VALUES[i].getDisplayName() + "！")
                        .withStyle(ChatFormatting.GOLD),
                    false
                );
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.2f);
                break;
            }
        }
    }
}
