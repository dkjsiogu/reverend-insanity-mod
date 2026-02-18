package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.VoidCloakBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 虚空隐匿蛊技能：隐入虚空之间，闪避攻击
public class VoidCloakAbility extends GuAbility {

    public VoidCloakAbility() {
        super(GuRegistry.id("void_cloak_gu"), 10f, 500, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        player.getData(ModAttachments.GU_MASTER_DATA.get()).getBuffManager()
            .applyBuff(player, new VoidCloakBuff());

        VfxHelper.spawn(player, VfxType.SHADOW_FADE,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFF110033, 3.0f, 20);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 0.3f);
    }
}
