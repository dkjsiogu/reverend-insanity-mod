package com.reverendinsanity.client;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.client.gui.RadialMenuScreen;
import com.reverendinsanity.client.render.DamageNumberRenderer;
import com.reverendinsanity.network.ActivateAbilityPayload;
import com.reverendinsanity.network.DefenseActionPayload;
import com.reverendinsanity.network.OpenAperturePayload;
import com.reverendinsanity.network.OpenCodexPayload;
import com.reverendinsanity.network.OpenDeductionScreenPayload;
import com.reverendinsanity.network.OpenImmortalAperturePayload;
import com.reverendinsanity.network.UseKillerMovePayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// 客户端事件处理：监听技能、杀招、空窍管理、防御按键
@EventBusSubscriber(modid = ReverendInsanity.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        for (int i = 0; i < ModKeybindings.ALL_ABILITY_KEYS.length; i++) {
            while (ModKeybindings.ALL_ABILITY_KEYS[i].consumeClick()) {
                PacketDistributor.sendToServer(new ActivateAbilityPayload(i));
            }
        }
        for (int i = 0; i < ModKeybindings.ALL_KILLER_MOVE_KEYS.length; i++) {
            while (ModKeybindings.ALL_KILLER_MOVE_KEYS[i].consumeClick()) {
                PacketDistributor.sendToServer(new UseKillerMovePayload(i));
            }
        }
        while (ModKeybindings.OPEN_APERTURE.consumeClick()) {
            PacketDistributor.sendToServer(new OpenAperturePayload());
        }
        while (ModKeybindings.OPEN_IMMORTAL_APERTURE.consumeClick()) {
            PacketDistributor.sendToServer(new OpenImmortalAperturePayload());
        }
        while (ModKeybindings.OPEN_DEDUCTION.consumeClick()) {
            PacketDistributor.sendToServer(new OpenDeductionScreenPayload());
        }
        while (ModKeybindings.OPEN_CODEX.consumeClick()) {
            PacketDistributor.sendToServer(new OpenCodexPayload());
        }
        while (ModKeybindings.RADIAL_MENU.consumeClick()) {
            Minecraft.getInstance().setScreen(new RadialMenuScreen());
        }
        while (ModKeybindings.DEFENSE_SHIELD.consumeClick()) {
            PacketDistributor.sendToServer(new DefenseActionPayload(DefenseActionPayload.SHIELD));
        }
        while (ModKeybindings.DEFENSE_DODGE.consumeClick()) {
            PacketDistributor.sendToServer(new DefenseActionPayload(DefenseActionPayload.DODGE));
        }
        DamageNumberRenderer.tick();
    }
}
