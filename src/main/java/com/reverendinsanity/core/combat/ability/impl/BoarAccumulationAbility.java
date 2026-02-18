package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.cultivation.PermanentStatApplier;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 黑豕积累蛊：永久积累力量+0.3，上限10
public class BoarAccumulationAbility extends GuAbility {

    public BoarAccumulationAbility() {
        super(GuRegistry.id("black_boar_gu"), 20f, 1200, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        float current = data.getPermanentStat("strength");
        if (current >= 10.0f) {
            player.sendSystemMessage(Component.literal("§c黑豕之力已达上限"));
            return;
        }
        data.addPermanentStat("strength", 0.3f);
        PermanentStatApplier.refresh(player);
        float total = data.getPermanentStat("strength");
        player.sendSystemMessage(Component.literal("§6黑豕之力灌注！力量+0.3 (当前: " + String.format("%.1f", total) + ")"));

        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFFF6600, 1.5f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 0.8f, 0.6f);
    }
}
