package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.buff.impl.ShadowStrikeBuff;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 幽影突袭杀招：隐身+极速+蓄力一击（攻击附加魔法伤害后打破隐身）
public class ShadowStrikeEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new ShadowStrikeBuff(calculatedDamage));

        VfxHelper.spawn(player, VfxType.SHADOW_FADE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF222244, 2.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PHANTOM_BITE, SoundSource.PLAYERS, 1.0f, 0.8f);
    }
}
