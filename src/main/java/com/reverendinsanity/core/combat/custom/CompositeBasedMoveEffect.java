package com.reverendinsanity.core.combat.custom;

import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import net.minecraft.server.level.ServerPlayer;

// 桥接类：让通过KillerMoveExecutor执行的推演杀招使用组件效果系统
public class CompositeBasedMoveEffect implements MoveEffect {

    private final KillerMove move;

    public CompositeBasedMoveEffect(KillerMove move) {
        this.move = move;
    }

    public CompositeBasedMoveEffect() {
        this.move = null;
    }

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        CompositeEffectExecutor.executeFromKillerMove(player, aperture, move, calculatedDamage);
    }
}
