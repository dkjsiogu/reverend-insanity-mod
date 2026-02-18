package com.reverendinsanity.network;

import com.reverendinsanity.client.ClientDataCache;
import com.reverendinsanity.client.gui.ApertureScreen;
import com.reverendinsanity.client.gui.CodexScreen;
import com.reverendinsanity.client.gui.DeductionScreen;
import com.reverendinsanity.client.gui.ImmortalApertureScreen;
import com.reverendinsanity.client.render.DamageNumberRenderer;
import com.reverendinsanity.client.vfx.VfxEffect;
import com.reverendinsanity.client.vfx.VfxManager;
import com.reverendinsanity.client.vfx.VfxType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// 客户端网络包处理
public class ClientPayloadHandler {

    public static void handleSyncGuMasterData(final SyncGuMasterDataPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.update(payload);
        });
    }

    public static void handleSyncApertureContents(final SyncApertureContentsPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.updateContents(payload);
            Minecraft.getInstance().setScreen(new ApertureScreen());
        });
    }

    public static void handleSpawnVfx(final SpawnVfxPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            VfxType type = VfxType.values()[payload.vfxType()];
            VfxEffect effect = new VfxEffect(type, payload.x(), payload.y(), payload.z(),
                payload.dirX(), payload.dirY(), payload.dirZ(),
                payload.color(), payload.scale(), payload.durationTicks());
            VfxManager.getInstance().addEffect(effect);
        });
    }

    public static void handleSyncCodex(final SyncCodexPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.updateCodex(payload);
            Minecraft.getInstance().setScreen(new CodexScreen());
        });
    }

    public static void handleSyncDeduction(final SyncDeductionPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.updateDeduction(payload.active(), payload.progress(), payload.successRate());
            if (!payload.message().isEmpty()) {
                Minecraft.getInstance().player.displayClientMessage(
                    Component.literal(payload.message()).withStyle(ChatFormatting.YELLOW), true);
            }
        });
    }

    public static void handleDeductionResult(final DeductionResultPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.updateDeductionResult(payload.outcome(), payload.moveName(), payload.message());
            ChatFormatting color = switch (payload.outcome()) {
                case 0 -> ChatFormatting.GOLD;
                case 1 -> ChatFormatting.GREEN;
                case 2 -> ChatFormatting.YELLOW;
                case 3 -> ChatFormatting.RED;
                case 4 -> ChatFormatting.LIGHT_PURPLE;
                default -> ChatFormatting.WHITE;
            };
            Minecraft.getInstance().player.displayClientMessage(
                Component.literal(payload.message()).withStyle(color, ChatFormatting.BOLD), false);
        });
    }

    public static void handleSyncImmortalAperture(final SyncImmortalAperturePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.updateImmortalAperture(payload);
            Minecraft.getInstance().setScreen(new ImmortalApertureScreen());
        });
    }

    public static void handleSyncDeductionScreen(final SyncDeductionScreenPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.updateDeductionScreen(payload);
            Minecraft.getInstance().setScreen(new DeductionScreen());
        });
    }

    public static void handleDamageNumber(final DamageNumberPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            DamageNumberRenderer.addEntry(payload.damage(), payload.damageType(),
                payload.x(), payload.y(), payload.z());
        });
    }

    public static void handleIntelSync(final IntelSyncPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.targetEntityId() == -1) {
                ClientDataCache.clearTargetIntel();
            } else {
                ClientDataCache.updateTargetIntel(payload.displayInfo(), payload.intelLevel());
            }
        });
    }
}
