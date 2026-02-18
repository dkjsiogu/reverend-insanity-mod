package com.reverendinsanity.registry;

import com.reverendinsanity.core.cultivation.GuMasterData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

// GuMasterData 的 NBT 序列化器
public class GuMasterDataSerializer implements net.neoforged.neoforge.attachment.IAttachmentSerializer<CompoundTag, GuMasterData> {

    @Override
    public GuMasterData read(net.neoforged.neoforge.attachment.IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
        GuMasterData data = new GuMasterData();
        data.load(tag);
        return data;
    }

    @Override
    public CompoundTag write(GuMasterData data, HolderLookup.Provider provider) {
        return data.save();
    }
}
