package com.reverendinsanity.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.reverendinsanity.core.combat.CombatState;
import com.reverendinsanity.core.combat.KillerMoveRegistry;
import com.reverendinsanity.core.cultivation.*;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// 管理命令：/gu 系列
public class GuCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gu").requires(s -> s.hasPermission(2))
            .then(Commands.literal("give")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("gu_id", StringArgumentType.string())
                        .executes(ctx -> giveGu(ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "player"),
                            StringArgumentType.getString(ctx, "gu_id"))))))
            .then(Commands.literal("setrank")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("rank", IntegerArgumentType.integer(1, 9))
                        .executes(ctx -> setRank(ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "player"),
                            IntegerArgumentType.getInteger(ctx, "rank"))))))
            .then(Commands.literal("info")
                .executes(ctx -> showInfo(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> showInfo(ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "player")))))
            .then(Commands.literal("open")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("aptitude", StringArgumentType.string())
                        .executes(ctx -> openAperture(ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "player"),
                            StringArgumentType.getString(ctx, "aptitude"))))))
            .then(Commands.literal("equip")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("move_id", StringArgumentType.string())
                        .executes(ctx -> equipMove(ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "player"),
                            StringArgumentType.getString(ctx, "move_id"))))))
            .then(Commands.literal("reset")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> resetData(ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "player")))))
            .then(Commands.literal("essence")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                        .executes(ctx -> setEssence(ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "player"),
                            FloatArgumentType.getFloat(ctx, "amount"))))))
            .then(Commands.literal("luck")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> showLuck(ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "player")))
                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0, 2))
                        .executes(ctx -> setLuck(ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "player"),
                            FloatArgumentType.getFloat(ctx, "amount"))))))
            .then(Commands.literal("diagnose")
                .executes(ctx -> DiagnosticCommand.diagnoseAll(ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("module", StringArgumentType.string())
                    .executes(ctx -> DiagnosticCommand.diagnoseModule(
                        ctx.getSource().getPlayerOrException(),
                        StringArgumentType.getString(ctx, "module")))))
            .then(Commands.literal("tutorial")
                .executes(ctx -> {
                    com.reverendinsanity.core.tutorial.TutorialManager.resetTutorial(ctx.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("all")
                    .executes(ctx -> {
                        com.reverendinsanity.core.tutorial.TutorialManager.showAllSteps(ctx.getSource().getPlayerOrException());
                        return 1;
                    })))
            .then(Commands.literal("test")
                .executes(ctx -> WalkthroughTestRunner.start(ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("ok")
                    .executes(ctx -> WalkthroughTestRunner.respond(ctx.getSource().getPlayerOrException(), "ok")))
                .then(Commands.literal("fail")
                    .executes(ctx -> WalkthroughTestRunner.respond(ctx.getSource().getPlayerOrException(), "fail")))
                .then(Commands.literal("skip")
                    .executes(ctx -> WalkthroughTestRunner.respond(ctx.getSource().getPlayerOrException(), "skip")))
                .then(Commands.literal("stop")
                    .executes(ctx -> WalkthroughTestRunner.respond(ctx.getSource().getPlayerOrException(), "stop"))))
        );
    }

    private static int giveGu(CommandSourceStack source, ServerPlayer player, String guId) {
        ResourceLocation id = ResourceLocation.parse(guId.contains(":") ? guId : "reverend_insanity:" + guId);
        GuType type = GuRegistry.get(id);
        if (type == null) {
            source.sendFailure(Component.literal("未知蛊虫: " + guId));
            return 0;
        }

        Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
        if (item == net.minecraft.world.item.Items.AIR) {
            source.sendFailure(Component.literal("找不到对应物品: " + guId));
            return 0;
        }

        player.getInventory().add(new ItemStack(item));
        source.sendSuccess(() -> Component.literal("已给予 " + player.getName().getString() + " " + type.displayName()), true);
        return 1;
    }

    private static int setRank(CommandSourceStack source, ServerPlayer player, int rank) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) {
            source.sendFailure(Component.literal("该玩家空窍未开"));
            return 0;
        }
        Rank targetRank = Rank.values()[rank - 1];
        aperture.setRank(targetRank);
        aperture.setSubRank(SubRank.INITIAL);
        source.sendSuccess(() -> Component.literal("已将 " + player.getName().getString() + " 设为 " + rank + "转·初阶"), true);
        return 1;
    }

    private static int showInfo(CommandSourceStack source, ServerPlayer player) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) {
            source.sendSuccess(() -> Component.literal(player.getName().getString() + ": 空窍未开"), false);
            return 1;
        }
        CombatState combatState = data.getCombatState();
        String info = String.format("%s: %d转·%s <%s> 真元:%.0f/%.0f 念头:%.0f/%.0f 蛊虫:%d 杀招:%d",
            player.getName().getString(),
            aperture.getRank().getLevel(),
            aperture.getSubRank().getDisplayName(),
            aperture.getAptitude().getDisplayName(),
            aperture.getCurrentEssence(), aperture.getMaxEssence(),
            aperture.getThoughts(), aperture.getMaxThoughts(),
            aperture.getStoredGu().size(),
            combatState.getEquippedMoves().size()
        );
        source.sendSuccess(() -> Component.literal(info), false);
        return 1;
    }

    private static int openAperture(CommandSourceStack source, ServerPlayer player, String aptitudeStr) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (aperture.isOpened()) {
            source.sendFailure(Component.literal("空窍已开"));
            return 0;
        }
        Aptitude apt;
        switch (aptitudeStr.toUpperCase()) {
            case "D" -> apt = Aptitude.D;
            case "C" -> apt = Aptitude.C;
            case "B" -> apt = Aptitude.B;
            case "A" -> apt = Aptitude.A;
            case "EXTREME" -> apt = Aptitude.EXTREME;
            default -> {
                source.sendFailure(Component.literal("无效资质，可选: D, C, B, A, EXTREME"));
                return 0;
            }
        }
        aperture.open(apt);
        source.sendSuccess(() -> Component.literal("已为 " + player.getName().getString() + " 开窍，资质: " + apt.getDisplayName()), true);
        return 1;
    }

    private static int equipMove(CommandSourceStack source, ServerPlayer player, String moveIdStr) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        CombatState combatState = data.getCombatState();
        ResourceLocation moveId = ResourceLocation.parse(moveIdStr.contains(":") ? moveIdStr : "reverend_insanity:" + moveIdStr);
        var move = KillerMoveRegistry.get(moveId);
        if (move == null) {
            source.sendFailure(Component.literal("未知杀招: " + moveIdStr));
            return 0;
        }
        combatState.equipMove(moveId);
        source.sendSuccess(() -> Component.literal("已为 " + player.getName().getString() + " 装备杀招: " + move.displayName()), true);
        return 1;
    }

    private static int resetData(CommandSourceStack source, ServerPlayer player) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        aperture.reset();
        source.sendSuccess(() -> Component.literal("已重置 " + player.getName().getString() + " 的修炼数据"), true);
        return 1;
    }

    private static int setEssence(CommandSourceStack source, ServerPlayer player, float amount) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) {
            source.sendFailure(Component.literal("空窍未开"));
            return 0;
        }
        aperture.setCurrentEssence(amount);
        source.sendSuccess(() -> Component.literal("已将 " + player.getName().getString() + " 真元设为 " + (int) amount), true);
        return 1;
    }

    private static int showLuck(CommandSourceStack source, ServerPlayer player) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        float luck = data.getLuck();
        String status;
        if (luck > 1.2f) status = "鸿运当头";
        else if (luck > 1.0f) status = "好运";
        else if (luck == 1.0f) status = "正常";
        else if (luck >= 0.7f) status = "运气不佳";
        else if (luck >= 0.5f) status = "厄运缠身";
        else status = "大凶之兆";
        source.sendSuccess(() -> Component.literal(
            player.getName().getString() + " 气运: " + String.format("%.2f", luck) + " (" + status + ")"
        ), false);
        return 1;
    }

    private static int setLuck(CommandSourceStack source, ServerPlayer player, float amount) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.setLuck(amount);
        source.sendSuccess(() -> Component.literal(
            "已将 " + player.getName().getString() + " 气运设为 " + String.format("%.2f", amount)
        ), true);
        return 1;
    }
}
