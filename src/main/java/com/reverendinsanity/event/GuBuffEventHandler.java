package com.reverendinsanity.event;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.buff.GuBuffManager;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.core.clone.CloneManager;
import com.reverendinsanity.core.dream.DreamExplorationManager;
import com.reverendinsanity.core.transformation.TransformationManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

// 蛊虫增益事件拦截器：伤害修改、目标锁定防护、攻击回调
@EventBusSubscriber(modid = ReverendInsanity.MODID)
public class GuBuffEventHandler {

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        float amount = event.getAmount();

        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            GuMasterData data = attacker.getData(ModAttachments.GU_MASTER_DATA.get());
            if (data.getAperture().isOpened() && event.getEntity() instanceof LivingEntity target) {
                amount = com.reverendinsanity.core.combat.AmbushManager.processAmbushDamage(attacker, target, amount);
                amount = data.getBuffManager().processOutgoingDamage(attacker, target, amount);
                amount += CloneManager.getExtraDamage(attacker, amount);
            }
        }

        if (event.getEntity() instanceof ServerPlayer defender) {
            GuMasterData data = defender.getData(ModAttachments.GU_MASTER_DATA.get());
            if (data.getAperture().isOpened()) {
                amount = data.getBuffManager().processIncomingDamage(defender, event.getSource(), amount);
            }
            amount = com.reverendinsanity.core.combat.DefenseManager.onHurt(defender, amount);
            String moveId = null;
            com.reverendinsanity.core.combat.DeathRecapManager.recordDamage(defender, event.getSource(), amount, moveId);
            DreamExplorationManager.onDamaged(defender);
            if (CloneManager.onIncomingDamage(defender)) {
                event.setCanceled(true);
                return;
            }
            com.reverendinsanity.core.gu.GuDamageManager.onPlayerHurt(defender, event.getAmount());
        }

        if (event.getSource().getEntity() instanceof LivingEntity attacker && !(attacker instanceof ServerPlayer)) {
            for (ServerPlayer player : attacker.level().getServer().getPlayerList().getPlayers()) {
                GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
                if (data.getAperture().isOpened() && data.getBuffManager().isCursed(attacker)) {
                    float retribution = amount * data.getBuffManager().getCursedRetribution(attacker);
                    if (retribution > 0) {
                        attacker.hurt(attacker.damageSources().magic(), retribution);
                    }
                    break;
                }
            }
        }

        event.setAmount(amount);
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        if (data.getAperture().isOpened()) {
            data.getBuffManager().onPlayerAttack(player, target);
        }
        TransformationManager.onPlayerAttack(player, 0);
    }

    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof ServerPlayer player)) return;
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        if (data.getAperture().isOpened()) {
            if (data.getBuffManager().shouldPreventTargeting(player, event.getEntity())) {
                event.setNewAboutToBeSetTarget(null);
            }
        }
    }
}
