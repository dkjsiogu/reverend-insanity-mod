package com.reverendinsanity.network;

import com.reverendinsanity.core.combat.CombatState;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.KillerMoveRegistry;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.ability.GuAbilityRegistry;
import com.reverendinsanity.core.combat.killermove.KillerMoveExecutor;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.deduction.DeductionManager;
import com.reverendinsanity.core.deduction.MoveBlueprint;
import com.reverendinsanity.core.deduction.DeductionSession;
import com.reverendinsanity.core.aperture.ImmortalAperture;
import com.reverendinsanity.core.aperture.ApertureResourceManager;
import com.reverendinsanity.core.aperture.calamity.Calamity;
import com.reverendinsanity.core.aperture.calamity.CalamityManager;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.reverendinsanity.util.AdvancementHelper;
import com.reverendinsanity.client.gui.RadialMenuAction;
import com.reverendinsanity.core.cultivation.SeclusionManager;
import com.reverendinsanity.core.combat.ToggleMoveManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 服务端处理客户端请求（技能催动、杀招施展、空窍界面）
public class ServerPayloadHandler {

    public static void handleActivateAbility(final ActivateAbilityPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            Aperture aperture = data.getAperture();
            CombatState combatState = data.getCombatState();

            if (!aperture.isOpened()) {
                player.displayClientMessage(Component.literal("空窍未开，无法催动蛊虫"), true);
                return;
            }

            List<GuAbility> availableAbilities = new ArrayList<>();
            for (GuInstance gu : aperture.getStoredGu()) {
                if (!gu.isActive()) continue;
                GuAbility ability = GuAbilityRegistry.get(gu.getTypeId());
                if (ability != null && !availableAbilities.contains(ability)) {
                    availableAbilities.add(ability);
                }
            }

            int slotIndex = payload.slotIndex();
            if (slotIndex < 0 || slotIndex >= availableAbilities.size()) {
                player.displayClientMessage(Component.literal("该技能栏位无蛊虫"), true);
                return;
            }

            GuAbility ability = availableAbilities.get(slotIndex);
            if (ability.execute(player, aperture, combatState)) {
                GuType guType = GuRegistry.get(ability.getGuTypeId());
                String name = guType != null ? guType.displayName() : "蛊虫";
                player.displayClientMessage(Component.literal(name + " 催动成功"), true);
            } else {
                if (combatState.isAbilityOnCooldown(ability.getGuTypeId())) {
                    player.displayClientMessage(Component.literal("技能冷却中"), true);
                } else {
                    player.displayClientMessage(Component.literal("真元不足"), true);
                }
            }
        });
    }

    public static void handleUseKillerMove(final UseKillerMovePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            Aperture aperture = data.getAperture();
            CombatState combatState = data.getCombatState();

            if (!aperture.isOpened()) {
                player.displayClientMessage(Component.literal("空窍未开"), true);
                return;
            }

            List<ResourceLocation> equipped = combatState.getEquippedMoves();
            int slot = payload.slotIndex();
            if (slot < 0 || slot >= equipped.size()) {
                player.displayClientMessage(Component.literal("杀招栏位为空"), true);
                return;
            }

            KillerMove move = KillerMoveRegistry.get(equipped.get(slot));
            if (move == null) {
                player.displayClientMessage(Component.literal("杀招不存在"), true);
                return;
            }

            if (KillerMoveExecutor.execute(player, aperture, combatState, move)) {
                player.displayClientMessage(Component.literal("杀招「" + move.displayName() + "」施展成功！"), false);
                AdvancementHelper.grant(player, "first_killer_move");
            } else {
                if (!move.canUse(aperture.getRank())) {
                    player.displayClientMessage(Component.literal("境界不足，无法施展此杀招"), true);
                } else if (aperture.getCurrentEssence() < move.essenceCost()) {
                    player.displayClientMessage(Component.literal("真元不足"), true);
                } else if (aperture.getThoughts() < move.thoughtsCost()) {
                    player.displayClientMessage(Component.literal("念头不足，无法驱动杀招"), true);
                } else if (combatState.isMoveCooldown(move.id())) {
                    player.displayClientMessage(Component.literal("杀招冷却中"), true);
                } else {
                    player.displayClientMessage(Component.literal("缺少必要蛊虫"), true);
                }
            }
        });
    }

    public static void handleOpenAperture(final OpenAperturePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            Aperture aperture = data.getAperture();
            CombatState combatState = data.getCombatState();

            List<SyncApertureContentsPayload.GuInfo> guInfoList = new ArrayList<>();
            for (GuInstance gu : aperture.getStoredGu()) {
                GuType type = GuRegistry.get(gu.getTypeId());
                if (type != null) {
                    guInfoList.add(new SyncApertureContentsPayload.GuInfo(
                        gu.getTypeId().toString(),
                        type.displayName(),
                        type.rank(),
                        type.path().getDisplayName(),
                        type.category().getDisplayName(),
                        gu.getHunger(),
                        gu.isRefined(),
                        gu.isAlive(),
                        gu.getProficiency()
                    ));
                }
            }

            List<SyncApertureContentsPayload.MoveInfo> equippedList = new ArrayList<>();
            for (ResourceLocation moveId : combatState.getEquippedMoves()) {
                KillerMove move = KillerMoveRegistry.get(moveId);
                if (move != null) {
                    equippedList.add(new SyncApertureContentsPayload.MoveInfo(
                        moveId.toString(), move.displayName(),
                        move.moveType().getDisplayName(), move.minRank(),
                        move.essenceCost(), move.thoughtsCost(),
                        buildMoveDescription(move)
                    ));
                }
            }

            List<SyncApertureContentsPayload.MoveInfo> availableList = new ArrayList<>();
            for (KillerMove move : KillerMoveRegistry.getAll()) {
                availableList.add(new SyncApertureContentsPayload.MoveInfo(
                    move.id().toString(), move.displayName(),
                    move.moveType().getDisplayName(), move.minRank(),
                    move.essenceCost(), move.thoughtsCost(),
                    buildMoveDescription(move)
                ));
            }

            PacketDistributor.sendToPlayer(player,
                new SyncApertureContentsPayload(guInfoList, equippedList, availableList));
        });
    }

    public static void handleEquipMove(final EquipMovePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            CombatState combatState = data.getCombatState();

            ResourceLocation moveId = ResourceLocation.parse(payload.moveId());
            if (payload.equip()) {
                combatState.equipMove(moveId);
                player.displayClientMessage(Component.literal("杀招已装备"), true);
            } else {
                combatState.unequipMove(moveId);
                player.displayClientMessage(Component.literal("杀招已卸下"), true);
            }

            handleOpenAperture(new OpenAperturePayload(), context);
        });
    }

    public static void handleFeedGu(final FeedGuPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            Aperture aperture = data.getAperture();

            if (!aperture.isOpened()) {
                player.displayClientMessage(Component.literal("空窍未开"), true);
                return;
            }

            List<GuInstance> guList = aperture.getStoredGu();
            int idx = payload.guIndex();
            if (idx < 0 || idx >= guList.size()) {
                player.displayClientMessage(Component.literal("无效的蛊虫索引"), true);
                return;
            }

            GuInstance gu = guList.get(idx);
            if (!gu.isAlive()) {
                player.displayClientMessage(Component.literal("蛊虫已死，无法喂养"), true);
                return;
            }

            boolean hasPrimevalStone = false;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(ModItems.PRIMEVAL_STONE.get())) {
                    hasPrimevalStone = true;
                    stack.shrink(1);
                    break;
                }
            }

            if (!hasPrimevalStone) {
                player.displayClientMessage(Component.literal("缺少元石，无法喂养"), true);
                return;
            }

            if (gu.feed()) {
                player.displayClientMessage(Component.literal("喂养成功！饥饿度: " + (int)gu.getHunger() + "%"), true);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.GENERIC_EAT, net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.2f);
                AdvancementHelper.grant(player, "feed_gu");
            } else {
                player.displayClientMessage(Component.literal("蛊虫饱食，无需喂养"), true);
            }

            handleOpenAperture(new OpenAperturePayload(), context);
        });
    }

    public static void handleDiscardGu(final DiscardGuPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            Aperture aperture = data.getAperture();

            if (!aperture.isOpened()) return;

            int idx = payload.guIndex();
            List<GuInstance> guList = aperture.getStoredGu();
            if (idx < 0 || idx >= guList.size()) return;

            GuType type = GuRegistry.get(guList.get(idx).getTypeId());
            GuInstance removed = aperture.removeGuAt(idx);
            if (removed != null) {
                String name = type != null ? type.displayName() : "蛊虫";
                player.displayClientMessage(Component.literal(name + " 已从窍穴中丢弃"), true);
            }

            handleOpenAperture(new OpenAperturePayload(), context);
        });
    }

    public static void handleOpenCodex(final OpenCodexPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            Set<ResourceLocation> discovered = data.getDiscoveredGu();

            List<SyncCodexPayload.CodexEntry> entries = new ArrayList<>();
            for (GuType type : GuRegistry.getAll()) {
                boolean isDiscovered = discovered.contains(type.id());
                entries.add(new SyncCodexPayload.CodexEntry(
                    type.id().toString(),
                    type.displayName(),
                    type.rank(),
                    type.path().getDisplayName(),
                    type.category().getDisplayName(),
                    isDiscovered
                ));
            }

            PacketDistributor.sendToPlayer(player,
                new SyncCodexPayload(entries, discovered.size()));
        });
    }

    public static void handleOpenDeductionScreen(final OpenDeductionScreenPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            Aperture aperture = data.getAperture();

            List<SyncDeductionScreenPayload.DeductionGuEntry> guEntries = new ArrayList<>();
            for (GuInstance gu : aperture.getStoredGu()) {
                if (!gu.isActive() || !gu.isAlive()) continue;
                GuType type = GuRegistry.get(gu.getTypeId());
                if (type != null) {
                    guEntries.add(new SyncDeductionScreenPayload.DeductionGuEntry(
                        gu.getTypeId().toString(), type.displayName(), type.rank(), type.path().getDisplayName()
                    ));
                }
            }

            boolean deducting = DeductionManager.isDeducting(player.getUUID());
            float progress = 0;
            float successRate = 0;
            if (deducting) {
                DeductionSession session = DeductionManager.getSession(player.getUUID());
                if (session != null) {
                    progress = session.getProgress();
                    successRate = session.getSuccessRate();
                }
            }

            PacketDistributor.sendToPlayer(player,
                new SyncDeductionScreenPayload(guEntries, deducting, progress, successRate));
        });
    }

    public static void handleStartDeduction(final StartDeductionPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            ResourceLocation coreGuId = ResourceLocation.parse(payload.coreGuId());
            List<ResourceLocation> supportIds = new ArrayList<>();
            for (String s : payload.supportGuIds()) {
                if (s != null && !s.isEmpty()) {
                    supportIds.add(ResourceLocation.parse(s));
                }
            }

            DaoPath targetPath = null;
            try {
                targetPath = DaoPath.valueOf(payload.targetPath());
            } catch (Exception e) {
                player.displayClientMessage(Component.literal("无效的道"), true);
                return;
            }

            MoveBlueprint blueprint = new MoveBlueprint(coreGuId, supportIds, targetPath);
            if (DeductionManager.startDeduction(player, blueprint)) {
                player.displayClientMessage(Component.literal("开始推演杀招...").withStyle(net.minecraft.ChatFormatting.GOLD), false);
            }
        });
    }

    public static void handleCancelDeduction(final CancelDeductionPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            DeductionManager.cancelDeduction(player.getUUID());
            player.displayClientMessage(Component.literal("推演已取消"), true);
        });
    }

    public static void handleEnterAperture(final EnterAperturePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            ImmortalAperture immortalAp = data.getImmortalAperture();

            if (!immortalAp.isFormed()) {
                if (!data.getAperture().getRank().isImmortal()) {
                    player.displayClientMessage(Component.literal("境界不足，无法开辟仙窍"), true);
                    return;
                }
                immortalAp.form(data.getAperture(), data);
                player.displayClientMessage(
                    Component.literal("仙窍开辟成功！" + immortalAp.getGrade().getDisplayName() + "！")
                        .withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD), false);
                AdvancementHelper.grant(player, "form_immortal_aperture");
            }

            if (player.level().dimension().equals(com.reverendinsanity.world.dimension.ModDimensions.APERTURE_DIM)) {
                player.displayClientMessage(Component.literal("你已在仙窍之中"), true);
                return;
            }

            com.reverendinsanity.world.dimension.ApertureDimensionManager.enterAperture(player);
            player.displayClientMessage(
                Component.literal("进入仙窍·" + immortalAp.getGrade().getDisplayName())
                    .withStyle(net.minecraft.ChatFormatting.AQUA, net.minecraft.ChatFormatting.BOLD), false);
        });
    }

    public static void handleExitAperture(final ExitAperturePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            if (!player.level().dimension().equals(com.reverendinsanity.world.dimension.ModDimensions.APERTURE_DIM)) {
                player.displayClientMessage(Component.literal("你不在仙窍中"), true);
                return;
            }

            com.reverendinsanity.world.dimension.ApertureDimensionManager.exitAperture(player);
            player.displayClientMessage(
                Component.literal("离开仙窍，返回原处").withStyle(net.minecraft.ChatFormatting.GREEN), false);
        });
    }

    public static void handleResistCalamity(final ResistCalamityPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            ImmortalAperture immortalAp = data.getImmortalAperture();

            if (!CalamityManager.isInCalamity(player.getUUID())) {
                player.displayClientMessage(Component.literal("当前没有灾劫"), true);
                return;
            }

            float amount = Math.min(payload.amount(), 50.0f);
            if (immortalAp.consumeQi(amount)) {
                CalamityManager.resistCalamity(player.getUUID(), amount);
                player.displayClientMessage(
                    Component.literal("消耗天地二气抵抗灾劫，减免伤害" + String.format("%.1f", amount))
                        .withStyle(net.minecraft.ChatFormatting.GREEN), true);
            } else {
                player.displayClientMessage(Component.literal("天地二气不足"), true);
            }
        });
    }

    public static void handleOpenImmortalAperture(final OpenImmortalAperturePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            ImmortalAperture immortalAp = data.getImmortalAperture();

            if (!immortalAp.isFormed() && data.getAperture().getRank().isImmortal()) {
                immortalAp.form(data.getAperture(), data);
                player.displayClientMessage(
                    Component.literal("仙窍开辟成功！" + immortalAp.getGrade().getDisplayName() + "！")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
                AdvancementHelper.grant(player, "form_immortal_aperture");
            }

            syncImmortalApertureToClient(player, data);
        });
    }

    public static void handleExtractResource(final ExtractResourcePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            ImmortalAperture immortalAp = data.getImmortalAperture();
            if (!immortalAp.isFormed()) return;

            ApertureResourceManager.ResourceType[] types = ApertureResourceManager.ResourceType.values();
            int ord = payload.resourceOrdinal();
            if (ord < 0 || ord >= types.length) return;

            ApertureResourceManager.ResourceType resType = types[ord];
            int amount = Math.min(payload.amount(), 64);
            ApertureResourceManager resMgr = immortalAp.getResourceManager();

            int extracted = 0;
            for (int i = 0; i < amount; i++) {
                if (resMgr.consumeResource(resType, 1)) {
                    extracted++;
                } else break;
            }

            if (extracted > 0) {
                ItemStack stack = getItemForResource(resType, extracted);
                if (!stack.isEmpty()) {
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                    player.displayClientMessage(
                        Component.literal("提取了 " + extracted + "个 " + resType.getDisplayName())
                            .withStyle(ChatFormatting.GREEN), true);
                }
            } else {
                player.displayClientMessage(Component.literal("该资源不足"), true);
            }

            syncImmortalApertureToClient(player, data);
        });
    }

    public static void handleRepairAperture(final RepairAperturePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            ImmortalAperture immortalAp = data.getImmortalAperture();
            if (!immortalAp.isFormed()) return;

            float before = immortalAp.getIntegrity();
            immortalAp.repair(Math.min(payload.amount(), 10.0f));
            float after = immortalAp.getIntegrity();

            if (after > before) {
                player.displayClientMessage(
                    Component.literal("仙窍修复: " + String.format("%.1f", before) + "% → " + String.format("%.1f", after) + "%")
                        .withStyle(ChatFormatting.GREEN), true);
            } else {
                player.displayClientMessage(Component.literal("天地二气不足，无法修复"), true);
            }

            syncImmortalApertureToClient(player, data);
        });
    }

    public static void handleRepairBreach(final RepairBreachPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            ImmortalAperture ap = data.getImmortalAperture();
            if (!ap.isFormed()) return;

            if (ap.getBreachCount() > 0) {
                ap.repairBreach();
                player.displayClientMessage(
                    Component.literal("[仙窍] 修复一处漏洞").withStyle(ChatFormatting.GREEN), false);
            } else {
                player.displayClientMessage(
                    Component.literal("[仙窍] 无漏洞需要修复").withStyle(ChatFormatting.GRAY), true);
            }

            syncImmortalApertureToClient(player, data);
        });
    }

    public static void syncImmortalApertureToClient(ServerPlayer player, GuMasterData data) {
        ImmortalAperture ap = data.getImmortalAperture();

        List<SyncImmortalAperturePayload.ResourceEntry> resources = new ArrayList<>();
        if (ap.isFormed()) {
            ApertureResourceManager resMgr = ap.getResourceManager();
            for (ApertureResourceManager.ResourceType type : ApertureResourceManager.ResourceType.values()) {
                resources.add(new SyncImmortalAperturePayload.ResourceEntry(
                    type.ordinal(), type.getDisplayName(), resMgr.getResource(type)));
            }
        }

        List<SyncImmortalAperturePayload.DaoMarkEntry> topMarks = new ArrayList<>();
        if (ap.isFormed()) {
            List<Map.Entry<DaoPath, Integer>> sorted = new ArrayList<>();
            for (DaoPath path : DaoPath.values()) {
                int marks = ap.getDaoMark(path);
                if (marks > 0) {
                    sorted.add(Map.entry(path, marks));
                }
            }
            sorted.sort(Comparator.<Map.Entry<DaoPath, Integer>>comparingInt(Map.Entry::getValue).reversed());
            for (int i = 0; i < Math.min(sorted.size(), 8); i++) {
                Map.Entry<DaoPath, Integer> e = sorted.get(i);
                topMarks.add(new SyncImmortalAperturePayload.DaoMarkEntry(
                    e.getKey().getDisplayName(), e.getValue()));
            }
        }

        boolean calamityActive = CalamityManager.isInCalamity(player.getUUID());
        String calamityName = "";
        float calamityProgress = 0;
        if (calamityActive) {
            Calamity cal = CalamityManager.getActiveCalamity(player.getUUID());
            if (cal != null) {
                calamityName = cal.getType().getDisplayName();
                calamityProgress = cal.getProgress();
            }
        }

        PacketDistributor.sendToPlayer(player, new SyncImmortalAperturePayload(
            ap.isFormed(),
            ap.isFormed() ? ap.getGrade().getDisplayName() : "",
            ap.getIntegrity(),
            ap.getStoredHeavenQi(),
            ap.getStoredEarthQi(),
            ap.getMaxQi(),
            ap.getImmortalEssenceStones(),
            calamityActive,
            calamityName,
            calamityProgress,
            ap.getDaysSinceLastCalamity(),
            resources,
            topMarks,
            ap.getDevelopmentLevel(),
            ap.getBreachCount(),
            ap.getTotalCalamitiesSurvived(),
            ap.isFormed() ? ap.getGrade().getTimeFlowRate() : 1
        ));
    }

    private static ItemStack getItemForResource(ApertureResourceManager.ResourceType type, int count) {
        return switch (type) {
            case PRIMEVAL_STONE -> new ItemStack(ModItems.PRIMEVAL_STONE.get(), count);
            case MOON_PETAL -> new ItemStack(ModItems.MOON_ORCHID_PETAL.get(), count);
            case BEAST_BONE -> new ItemStack(ModItems.BEAST_BONE.get(), count);
            case BITTER_WINE -> new ItemStack(ModItems.BITTER_WINE.get(), count);
            case SPIDER_SILK -> new ItemStack(ModItems.SPIDER_SILK.get(), count);
            case JADE_BEAD -> new ItemStack(ModItems.JADE_EYE.get(), count);
            case IMMORTAL_ESSENCE -> new ItemStack(ModItems.PRIMEVAL_STONE.get(), count * 5);
        };
    }

    private static String buildMoveDescription(KillerMove move) {
        StringBuilder sb = new StringBuilder();
        sb.append(move.primaryPath().getDisplayName()).append("\u9053 | ");
        sb.append("\u5a01\u529b:").append(String.format("%.0f", move.power()));
        sb.append(" \u51b7\u5374:").append(move.cooldownTicks() / 20).append("s");
        GuType coreType = GuRegistry.get(move.coreGu());
        if (coreType != null) {
            sb.append(" | \u6838\u5fc3:").append(coreType.displayName());
        }
        if (!move.supportGu().isEmpty()) {
            sb.append("+");
            for (int i = 0; i < move.supportGu().size(); i++) {
                GuType supType = GuRegistry.get(move.supportGu().get(i));
                if (supType != null) {
                    if (i > 0) sb.append(",");
                    sb.append(supType.displayName());
                }
            }
        }
        return sb.toString();
    }

    public static void handleDefenseAction(final DefenseActionPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            switch (payload.action()) {
                case DefenseActionPayload.SHIELD -> {
                    if (com.reverendinsanity.core.combat.DefenseManager.activateShield(player)) {
                        player.displayClientMessage(Component.literal("\u771f\u5143\u62a4\u76fe\u5f00\u542f\uff01").withStyle(ChatFormatting.AQUA), true);
                    } else {
                        player.displayClientMessage(Component.literal("\u65e0\u6cd5\u5f00\u542f\u62a4\u76fe\uff08\u771f\u5143\u4e0d\u8db3\u6216\u5df2\u6fc0\u6d3b\uff09"), true);
                    }
                }
                case DefenseActionPayload.DODGE -> {
                    if (com.reverendinsanity.core.combat.DefenseManager.activateDodge(player)) {
                        player.displayClientMessage(Component.literal("\u7d27\u6025\u95ea\u907f\uff01").withStyle(ChatFormatting.GREEN), true);
                    } else {
                        player.displayClientMessage(Component.literal("\u95ea\u907f\u51b7\u5374\u4e2d\u6216\u771f\u5143\u4e0d\u8db3"), true);
                    }
                }
            }
        });
    }

    public static void handleRadialMenu(final RadialMenuPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            switch (payload.actionType()) {
                case RadialMenuPayload.TYPE_SYSTEM -> {
                    RadialMenuAction action = RadialMenuAction.fromIndex(payload.actionIndex());
                    if (action == null) return;
                    switch (action) {
                        case APERTURE -> handleOpenAperture(new OpenAperturePayload(), context);
                        case IMMORTAL_APERTURE -> handleOpenImmortalAperture(new OpenImmortalAperturePayload(), context);
                        case DEDUCTION -> handleOpenDeductionScreen(new OpenDeductionScreenPayload(), context);
                        case CODEX -> handleOpenCodex(new OpenCodexPayload(), context);
                        case SECLUSION -> {
                            if (SeclusionManager.isInSeclusion(player)) {
                                player.displayClientMessage(Component.literal("已在闭关中，移动即可中断"), true);
                            } else if (SeclusionManager.enterSeclusion(player)) {
                                player.displayClientMessage(Component.literal("进入闭关状态...").withStyle(ChatFormatting.AQUA), false);
                            } else {
                                player.displayClientMessage(Component.literal("无法进入闭关"), true);
                            }
                        }
                    }
                }
                case RadialMenuPayload.TYPE_ABILITY -> handleActivateAbility(new ActivateAbilityPayload(payload.actionIndex()), context);
                case RadialMenuPayload.TYPE_MOVE -> {
                    GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
                    CombatState combatState = data.getCombatState();
                    List<ResourceLocation> equipped = combatState.getEquippedMoves();
                    int slot = payload.actionIndex();
                    if (slot < 0 || slot >= equipped.size()) return;
                    KillerMove move = KillerMoveRegistry.get(equipped.get(slot));
                    if (move == null) return;
                    if (ToggleMoveManager.isToggleable(move)) {
                        ToggleMoveManager.toggleMove(player, move);
                    } else {
                        handleUseKillerMove(new UseKillerMovePayload(slot), context);
                    }
                }
            }
        });
    }
}
