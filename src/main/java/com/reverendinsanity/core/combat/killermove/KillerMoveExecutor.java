package com.reverendinsanity.core.combat.killermove;

import com.reverendinsanity.core.combat.CombatState;
import com.reverendinsanity.core.combat.DamageNumberManager;
import com.reverendinsanity.core.combat.DefenseManager;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.EssenceGrade;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.deduction.DeductionManager;
import com.reverendinsanity.core.deduction.ImprovedMove;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.core.path.DaoPathSounds;
import com.reverendinsanity.core.path.PathCounterSystem;
import com.reverendinsanity.core.path.PathRealm;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

// 杀招执行器：验证管线 + 多层伤害计算(含道痕/改良) + 效果执行
public class KillerMoveExecutor {

    public static boolean canExecute(ServerPlayer player, Aperture aperture, CombatState combat, KillerMove move) {
        if (!aperture.isOpened()) return false;
        if (!move.canUse(aperture.getRank())) return false;
        float essenceCost = getEffectiveEssenceCost(player, move);
        float thoughtsCost = getEffectiveThoughtsCost(player, move);
        if (aperture.getCurrentEssence() < essenceCost) return false;
        if (aperture.getThoughts() < thoughtsCost) return false;
        if (combat.isMoveCooldown(move.id())) return false;
        for (ResourceLocation guId : move.getAllRequiredGu()) {
            boolean hasGu = aperture.getStoredGu().stream()
                .anyMatch(g -> g.getTypeId().equals(guId) && g.isActive());
            if (!hasGu) return false;
        }
        return true;
    }

    public static float calculateDamage(Aperture aperture, KillerMove move, GuMasterData data) {
        float base = getEffectivePower(data, move);
        EssenceGrade grade = aperture.getEssenceGrade();
        float essenceMult = grade != null ? grade.getEfficiency() : 1.0f;
        PathRealm pathRealm = aperture.getPathRealm(move.primaryPath());
        float pathMult = 1.0f + pathRealm.getTier() * 0.15f;
        float resonance = DaoResonance.calculateWithDaoMarks(move, data);
        float compatibility = aperture.getPathCompatibility(move.primaryPath());
        float profMult = getAverageProficiencyBonus(aperture, move);
        return base * essenceMult * pathMult * resonance * compatibility * profMult;
    }

    public static float calculateDamage(Aperture aperture, KillerMove move) {
        float base = move.power();
        EssenceGrade grade = aperture.getEssenceGrade();
        float essenceMult = grade != null ? grade.getEfficiency() : 1.0f;
        PathRealm pathRealm = aperture.getPathRealm(move.primaryPath());
        float pathMult = 1.0f + pathRealm.getTier() * 0.15f;
        float resonance = DaoResonance.calculate(move);
        float compatibility = aperture.getPathCompatibility(move.primaryPath());
        float profMult = getAverageProficiencyBonus(aperture, move);
        return base * essenceMult * pathMult * resonance * compatibility * profMult;
    }

    public static boolean execute(ServerPlayer player, Aperture aperture, CombatState combat, KillerMove move) {
        if (!canExecute(player, aperture, combat, move)) return false;
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        float compatibility = aperture.getPathCompatibility(move.primaryPath());
        float adjustedEssence = getEffectiveEssenceCost(player, move) / compatibility;
        float adjustedThoughts = getEffectiveThoughtsCost(player, move) / compatibility;
        aperture.consumeEssence(adjustedEssence);
        aperture.consumeThoughts(adjustedThoughts);
        int effectiveCooldown = getEffectiveCooldown(player, move);
        int adjustedCooldown = Math.max(20, (int)(effectiveCooldown / compatibility));
        combat.setMoveCooldown(move.id(), adjustedCooldown);
        float damage = calculateDamage(aperture, move, data);
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        SoundEvent moveSound = DaoPathSounds.getKillerMoveSound(move.moveType(), move.primaryPath());
        float moveVolume = DaoPathSounds.getKillerMoveVolume(move.moveType());
        float movePitch = DaoPathSounds.getKillerMovePitch(move.moveType());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            moveSound, SoundSource.PLAYERS, moveVolume, movePitch);
        MoveEffect effect = MoveEffectRegistry.resolve(move);
        float counterMult = DefenseManager.getCounterMultiplier(player);
        float finalDamage = damage * counterMult;
        effect.execute(player, aperture, move, finalDamage);
        combat.enterCombat();
        com.reverendinsanity.core.heavenwill.HeavenWillManager.addAttention(player, move.power() * 0.5f);
        com.reverendinsanity.core.oath.PoisonOathManager.onKillerMoveUsed(player);
        for (ResourceLocation guId : move.getAllRequiredGu()) {
            GuInstance guInst = aperture.findGuInstance(guId);
            if (guInst != null) guInst.addProficiency(1.0f);
        }
        return true;
    }

    private static float getEffectivePower(GuMasterData data, KillerMove move) {
        ImprovedMove improved = DeductionManager.getImprovedMove(data.getPlayerUUID(), move.id());
        return improved != null ? improved.getEffectivePower() : move.power();
    }

    private static float getEffectiveEssenceCost(ServerPlayer player, KillerMove move) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        ImprovedMove improved = DeductionManager.getImprovedMove(data.getPlayerUUID(), move.id());
        return improved != null ? improved.getEffectiveEssenceCost() : move.essenceCost();
    }

    private static float getEffectiveThoughtsCost(ServerPlayer player, KillerMove move) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        ImprovedMove improved = DeductionManager.getImprovedMove(data.getPlayerUUID(), move.id());
        return improved != null ? improved.getEffectiveThoughtsCost() : move.thoughtsCost();
    }

    private static int getEffectiveCooldown(ServerPlayer player, KillerMove move) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        ImprovedMove improved = DeductionManager.getImprovedMove(data.getPlayerUUID(), move.id());
        return improved != null ? improved.getEffectiveCooldown() : move.cooldownTicks();
    }

    private static float getAverageProficiencyBonus(Aperture aperture, KillerMove move) {
        float totalProf = 0f;
        int count = 0;
        for (ResourceLocation guId : move.getAllRequiredGu()) {
            GuInstance guInst = aperture.findGuInstance(guId);
            if (guInst != null) {
                totalProf += guInst.getProficiency();
                count++;
            }
        }
        if (count == 0) return 1.0f;
        float avgProf = totalProf / count;
        return 1.0f + avgProf * 0.0015f;
    }

    public static float getPathCounterMultiplier(DaoPath attackerPath, LivingEntity target) {
        DaoPath targetPath = null;
        if (target instanceof com.reverendinsanity.entity.GuMasterEntity guMaster) {
            targetPath = guMaster.getPrimaryDaoPath();
        } else if (target instanceof ServerPlayer targetPlayer) {
            GuMasterData targetData = targetPlayer.getData(ModAttachments.GU_MASTER_DATA.get());
            targetPath = targetData.getAperture().getPrimaryPath();
        }
        return PathCounterSystem.getDamageMultiplier(attackerPath, targetPath);
    }

    public static float applyDamageWithPathCounter(ServerPlayer attacker, LivingEntity target, KillerMove move, float baseDamage) {
        float counterMult = getPathCounterMultiplier(move.primaryPath(), target);
        float finalDamage = baseDamage * counterMult;
        DamageNumberManager.DamageType type = counterMult > 1.0f
            ? DamageNumberManager.DamageType.CRITICAL
            : DamageNumberManager.DamageType.ESSENCE;
        DamageNumberManager.broadcastDamageNumber(target, finalDamage, type);
        return finalDamage;
    }
}
