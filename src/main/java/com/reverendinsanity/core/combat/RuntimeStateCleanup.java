package com.reverendinsanity.core.combat;

import com.reverendinsanity.core.cultivation.TribulationManager;
import net.minecraft.server.MinecraftServer;

public final class RuntimeStateCleanup {

    private RuntimeStateCleanup() {
    }

    public static void clearAll(MinecraftServer server) {
        PoisonCloudManager.clearAll();
        FrostManager.clearAll();
        DotManager.clearAll();
        FlashBlindManager.clearAll(server);
        SealManager.clearAll(server);
        TrapManager.clearAll();
        AmbushManager.clearAll();
        SelfDestructManager.clearAll();
        DefenseManager.clearAll();
        IntelligenceManager.clearAll();
        MeritManager.clearAll();
        LifeDeathGateManager.clearAll(server);
        TribulationManager.clearAll();
    }
}
