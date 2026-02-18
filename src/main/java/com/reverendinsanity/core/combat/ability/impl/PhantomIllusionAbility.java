package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.buff.impl.PhantomIllusionBuff;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 幻影蛊技能：幻影护身——+30%速度、50%怪物锁定失败率，400tick
public class PhantomIllusionAbility extends GuAbility {

    public PhantomIllusionAbility() {
        super(GuRegistry.id("phantom_gu"), 10f, 400, AbilityType.BUFF);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new PhantomIllusionBuff());

        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFCC88FF, 2.5f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.4f, 1.2f);
    }
}
