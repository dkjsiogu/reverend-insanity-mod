package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 奴蛇蛊技能：控制周围被动生物跟随自己
public class EnslaveSnakeAbility extends GuAbility {

    public EnslaveSnakeAbility() {
        super(GuRegistry.id("enslave_snake_gu"), 25f, 600, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        AABB area = player.getBoundingBox().inflate(10.0);
        List<Mob> mobs = player.level().getEntitiesOfClass(Mob.class, area,
            mob -> mob instanceof Animal && mob.isAlive());

        int count = 0;
        int maxTargets = 5;
        for (Mob mob : mobs) {
            if (count >= maxTargets) break;
            mob.setTarget(null);
            mob.getNavigation().moveTo(player.getX(), player.getY(), player.getZ(), 1.2);
            mob.setLeashedTo(player, true);
            count++;
        }
        if (count > 0) {
            player.displayClientMessage(Component.literal("奴蛇蛊催动，控制了 " + count + " 只生物"), true);
        } else {
            player.displayClientMessage(Component.literal("周围没有可控制的生物"), true);
        }

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF44AA44, 3.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
