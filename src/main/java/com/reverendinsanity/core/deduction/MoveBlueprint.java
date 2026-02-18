package com.reverendinsanity.core.deduction;

import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

// 杀招蓝图：推演中的蛊虫组合方案
public record MoveBlueprint(
    ResourceLocation coreGu,
    List<ResourceLocation> supportGu,
    DaoPath targetPath
) {
    public List<ResourceLocation> getAllGu() {
        List<ResourceLocation> all = new ArrayList<>();
        all.add(coreGu);
        all.addAll(supportGu);
        return all;
    }

    public int getGuCount() {
        return 1 + supportGu.size();
    }

    public boolean validate() {
        if (coreGu == null || targetPath == null) return false;
        if (GuRegistry.get(coreGu) == null) return false;
        for (ResourceLocation id : supportGu) {
            if (GuRegistry.get(id) == null) return false;
        }
        return true;
    }
}
