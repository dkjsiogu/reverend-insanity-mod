package com.reverendinsanity.registry;

import com.reverendinsanity.world.dimension.ModDimensions;
import net.neoforged.bus.api.IEventBus;

// 统一注册入口
public class ModRegistries {

    public static void register(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModDimensions.CHUNK_GENERATORS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModStructures.STRUCTURE_TYPES.register(modEventBus);
        ModStructures.STRUCTURE_PIECES.register(modEventBus);
    }
}
