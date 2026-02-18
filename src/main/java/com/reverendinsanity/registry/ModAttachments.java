package com.reverendinsanity.registry;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.cultivation.GuMasterData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

// 附加数据注册（用于将蛊师数据绑定到玩家）
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ReverendInsanity.MODID);

    public static final Supplier<AttachmentType<GuMasterData>> GU_MASTER_DATA =
        ATTACHMENTS.register("gu_master_data", () -> AttachmentType.builder(GuMasterData::new)
            .serialize(new GuMasterDataSerializer())
            .build());
}
