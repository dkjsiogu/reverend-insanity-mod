package com.reverendinsanity;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.reverendinsanity.registry.ModRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

// 蛊真人模组主入口
@Mod(ReverendInsanity.MODID)
public class ReverendInsanity {

    public static final String MODID = "reverend_insanity";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ReverendInsanity(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        ModRegistries.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("人是万物之灵，蛊是天地真精。蛊真人模组加载完成。");
    }
}
