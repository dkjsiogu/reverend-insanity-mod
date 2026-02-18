package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.PoisonCloudManager;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

// 毒雾缠丝：释放大范围毒云并诅咒区域内所有敌人
public class PoisonMistEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        Vec3 look = player.getLookAngle();
        Vec3 cloudCenter = player.position().add(look.x * 5, 1.0, look.z * 5);

        PoisonCloudManager.addCloud((ServerLevel) player.level(),
            cloudCenter, 5.0f, 160, calculatedDamage * 0.15f, player.getUUID());

        AABB area = AABB.ofSize(cloudCenter, 10.0, 6.0, 10.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        for (LivingEntity target : targets) {
            data.getBuffManager().addCursedTarget(target, 160, 0.25f);
            data.getBuffManager().addGlowingTarget(target, 160);
        }

        VfxHelper.spawn(player, VfxType.DOME_FIELD,
            (float) cloudCenter.x, (float) cloudCenter.y, (float) cloudCenter.z,
            0f, 1f, 0f,
            0xFF44AA00, 3.0f, 45);
        player.level().playSound(null, cloudCenter.x, cloudCenter.y, cloudCenter.z,
            SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 1.5f, 0.3f);
    }
}
