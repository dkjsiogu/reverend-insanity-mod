package com.reverendinsanity.core.combat.killermove;

import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.cultivation.Aperture;
import net.minecraft.server.level.ServerPlayer;

// 杀招效果执行接口
@FunctionalInterface
public interface MoveEffect {
    void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage);
}
