package com.reverendinsanity.core.economy;

import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 赌石产出管理：按转数分组蛊虫池，随机抽取蛊虫物品
public class GamblingStoneManager {

    private static Map<Integer, List<GuType>> rankPoolCache = null;

    private static void buildCache() {
        if (rankPoolCache != null) return;
        rankPoolCache = new HashMap<>();
        for (GuType type : GuRegistry.getAll()) {
            if (type.id().getPath().equals("hope_gu")) continue;
            if (type.id().getPath().equals("spring_autumn_cicada")) continue;
            rankPoolCache.computeIfAbsent(type.rank(), k -> new ArrayList<>()).add(type);
        }
    }

    public static ItemStack rollGuItem(ServerPlayer player, int minRank, int maxRank) {
        buildCache();

        List<GuType> pool = new ArrayList<>();
        for (int r = minRank; r <= maxRank; r++) {
            List<GuType> rankList = rankPoolCache.get(r);
            if (rankList != null) {
                pool.addAll(rankList);
            }
        }

        if (pool.isEmpty()) return ItemStack.EMPTY;

        GuType chosen = pool.get(player.getRandom().nextInt(pool.size()));
        Item item = BuiltInRegistries.ITEM.get(chosen.id());
        if (item == Items.AIR) return ItemStack.EMPTY;

        return new ItemStack(item);
    }

    public static void invalidateCache() {
        rankPoolCache = null;
    }
}
