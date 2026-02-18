package com.reverendinsanity.block.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.reverendinsanity.core.gu.RefinementRecipe;
import com.reverendinsanity.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 炼蛊炉方块实体，存储最多6个物品
public class RefinementCauldronBlockEntity extends BlockEntity {

    private NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);

    public RefinementCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REFINEMENT_CAULDRON.get(), pos, state);
    }

    public boolean addItem(ItemStack stack) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack.copyWithCount(1));
                setChanged();
                return true;
            }
        }
        return false;
    }

    public List<ItemStack> getItems() {
        return items.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    public void removeMatchingItems(RefinementRecipe recipe) {
        Map<String, Integer> toRemove = new HashMap<>();
        for (RefinementRecipe.Ingredient ingredient : recipe.getIngredients()) {
            toRemove.put(ingredient.itemId(), ingredient.count());
        }

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;
            String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            Integer remaining = toRemove.get(key);
            if (remaining != null && remaining > 0) {
                int remove = Math.min(remaining, stack.getCount());
                stack.shrink(remove);
                toRemove.put(key, remaining - remove);
                if (stack.isEmpty()) {
                    items.set(i, ItemStack.EMPTY);
                }
            }
        }
        setChanged();
    }

    public void clearItems() {
        Collections.fill(items, ItemStack.EMPTY);
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(6, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }
}
