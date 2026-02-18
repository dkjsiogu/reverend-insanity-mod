package com.reverendinsanity.core.combat.custom;

import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

// 玩家自创杀招：核心蛊+辅助蛊自由搭配
public class CustomKillerMove {

    private String name;
    private ResourceLocation coreGuId;
    private List<ResourceLocation> supportGuIds;
    private DaoPath primaryPath;
    private KillerMove.MoveType moveType;
    private float power;
    private float essenceCost;
    private float thoughtsCost;
    private int cooldownTicks;
    private float synergy;

    public CustomKillerMove(String name, ResourceLocation coreGuId, List<ResourceLocation> supportGuIds,
                            DaoPath primaryPath, KillerMove.MoveType moveType,
                            float power, float essenceCost, float thoughtsCost,
                            int cooldownTicks, float synergy) {
        this.name = name;
        this.coreGuId = coreGuId;
        this.supportGuIds = new ArrayList<>(supportGuIds);
        this.primaryPath = primaryPath;
        this.moveType = moveType;
        this.power = power;
        this.essenceCost = essenceCost;
        this.thoughtsCost = thoughtsCost;
        this.cooldownTicks = cooldownTicks;
        this.synergy = synergy;
    }

    public List<ResourceLocation> getAllRequiredGu() {
        List<ResourceLocation> all = new ArrayList<>();
        all.add(coreGuId);
        all.addAll(supportGuIds);
        return all;
    }

    public int getGuCount() { return 1 + supportGuIds.size(); }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putString("coreGu", coreGuId.toString());
        ListTag supports = new ListTag();
        for (ResourceLocation id : supportGuIds) {
            supports.add(StringTag.valueOf(id.toString()));
        }
        tag.put("supportGu", supports);
        tag.putString("path", primaryPath.name());
        tag.putString("moveType", moveType.name());
        tag.putFloat("power", power);
        tag.putFloat("essenceCost", essenceCost);
        tag.putFloat("thoughtsCost", thoughtsCost);
        tag.putInt("cooldown", cooldownTicks);
        tag.putFloat("synergy", synergy);
        return tag;
    }

    public static CustomKillerMove load(CompoundTag tag) {
        try {
            String name = tag.getString("name");
            ResourceLocation coreGu = ResourceLocation.parse(tag.getString("coreGu"));
            ListTag supList = tag.getList("supportGu", Tag.TAG_STRING);
            List<ResourceLocation> supports = new ArrayList<>();
            for (int i = 0; i < supList.size(); i++) {
                supports.add(ResourceLocation.parse(supList.getString(i)));
            }
            DaoPath path = DaoPath.valueOf(tag.getString("path"));
            KillerMove.MoveType moveType = KillerMove.MoveType.valueOf(tag.getString("moveType"));
            return new CustomKillerMove(name, coreGu, supports, path, moveType,
                tag.getFloat("power"), tag.getFloat("essenceCost"),
                tag.getFloat("thoughtsCost"), tag.getInt("cooldown"),
                tag.getFloat("synergy"));
        } catch (Exception e) {
            return null;
        }
    }

    public String getName() { return name; }
    public ResourceLocation getCoreGuId() { return coreGuId; }
    public List<ResourceLocation> getSupportGuIds() { return supportGuIds; }
    public DaoPath getPrimaryPath() { return primaryPath; }
    public KillerMove.MoveType getMoveType() { return moveType; }
    public float getPower() { return power; }
    public float getEssenceCost() { return essenceCost; }
    public float getThoughtsCost() { return thoughtsCost; }
    public int getCooldownTicks() { return cooldownTicks; }
    public float getSynergy() { return synergy; }
}
