package com.reverendinsanity.entity;

import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.item.GuItem;
import com.reverendinsanity.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.faction.Faction;
import com.reverendinsanity.registry.ModAttachments;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 蛊商人：和平NPC，以元石为货币交易蛊虫，道路动态商品+赌石+战功兑换
public class GuMerchantEntity extends AbstractVillager {

    private static final EntityDataAccessor<Integer> GU_RANK =
        SynchedEntityData.defineId(GuMerchantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FACTION =
        SynchedEntityData.defineId(GuMerchantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_PRIMARY_PATH =
        SynchedEntityData.defineId(GuMerchantEntity.class, EntityDataSerializers.STRING);

    public GuMerchantEntity(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractVillager.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GU_RANK, 1);
        builder.define(DATA_FACTION, Faction.INDEPENDENT.ordinal());
        builder.define(DATA_PRIMARY_PATH, "");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);
        float roll = this.random.nextFloat();
        int rank;
        if (roll < 0.50f) {
            rank = 1;
        } else if (roll < 0.85f) {
            rank = 2;
        } else {
            rank = 3;
        }
        setGuRank(rank);

        float factionRoll = this.random.nextFloat();
        if (factionRoll < 0.70f) {
            setFaction(Faction.INDEPENDENT);
        } else if (factionRoll < 0.90f) {
            setFaction(Faction.RIGHTEOUS);
        } else {
            setFaction(Faction.DEMONIC);
        }

        assignRandomPath();

        return spawnData;
    }

    private void assignRandomPath() {
        DaoPath[] allPaths = DaoPath.values();
        DaoPath chosen = allPaths[this.random.nextInt(allPaths.length)];
        setPrimaryPath(chosen.name());
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = this.getOffers();
        int rank = getGuRank();

        if (getPrimaryPath().isEmpty()) {
            assignRandomPath();
        }

        offers.add(makeBuyTrade(ModItems.PRIMEVAL_STONE.get().getDefaultInstance(), 5,
            new ItemStack(ModItems.GAMBLING_STONE_LOW.get()), 5));
        offers.add(makeTrade(ModItems.BREAKTHROUGH_STONE.get().getDefaultInstance(), 20, 3));
        offers.add(makeTrade(new ItemStack(ModItems.CULTIVATION_MANUAL.get()), 3, 5));

        offers.add(makeTrade(new ItemStack(ModItems.GAMBLING_STONE_LOW.get()), 5, 10));
        offers.add(makeTrade(new ItemStack(ModItems.GAMBLING_STONE_MEDIUM.get()), 15, 5));
        offers.add(makeTrade(new ItemStack(ModItems.GAMBLING_STONE_HIGH.get()), 30, 3));

        addPathTrades(offers, rank);
        addSellbackTrades(offers);
        addRareTrade(offers, rank);
    }

    private void addPathTrades(MerchantOffers offers, int rank) {
        String pathName = getPrimaryPath();
        DaoPath path;
        try {
            path = DaoPath.valueOf(pathName);
        } catch (Exception e) {
            return;
        }

        List<GuType> pathGu = GuRegistry.getByPath(path);
        List<GuType> eligible = new ArrayList<>();
        for (GuType gu : pathGu) {
            if (gu.rank() <= rank && gu.rank() <= 3) {
                eligible.add(gu);
            }
        }

        Collections.shuffle(eligible, new java.util.Random(this.getUUID().hashCode()));
        int count = Math.min(eligible.size(), 5 + this.random.nextInt(4));

        for (int i = 0; i < count; i++) {
            GuType gu = eligible.get(i);
            Item item = BuiltInRegistries.ITEM.get(gu.id());
            if (item == Items.AIR) continue;

            int price = switch (gu.rank()) {
                case 1 -> 4 + this.random.nextInt(4);
                case 2 -> 12 + this.random.nextInt(8);
                case 3 -> 25 + this.random.nextInt(15);
                default -> 5;
            };

            offers.add(makeTrade(new ItemStack(item), price, 3));
        }
    }

    private void addSellbackTrades(MerchantOffers offers) {
        offers.add(new MerchantOffer(
            new ItemCost(ModItems.HOPE_GU.get(), 1),
            new ItemStack(ModItems.PRIMEVAL_STONE.get(), 3),
            10, 1, 0.05f
        ));

        List<GuType> rank1Gu = GuRegistry.getByRank(1);
        for (GuType gu : rank1Gu) {
            if (gu.id().getPath().equals("hope_gu")) continue;
            Item item = BuiltInRegistries.ITEM.get(gu.id());
            if (item == Items.AIR || !(item instanceof GuItem)) continue;
            offers.add(new MerchantOffer(
                new ItemCost(item, 1),
                new ItemStack(ModItems.PRIMEVAL_STONE.get(), 3),
                10, 1, 0.05f
            ));
            break;
        }

        List<GuType> rank2Gu = GuRegistry.getByRank(2);
        if (!rank2Gu.isEmpty()) {
            Item item2 = BuiltInRegistries.ITEM.get(rank2Gu.get(0).id());
            if (item2 != Items.AIR && item2 instanceof GuItem) {
                offers.add(new MerchantOffer(
                    new ItemCost(item2, 1),
                    new ItemStack(ModItems.PRIMEVAL_STONE.get(), 8),
                    10, 1, 0.05f
                ));
            }
        }

        List<GuType> rank3Gu = GuRegistry.getByRank(3);
        if (!rank3Gu.isEmpty()) {
            Item item3 = BuiltInRegistries.ITEM.get(rank3Gu.get(0).id());
            if (item3 != Items.AIR && item3 instanceof GuItem) {
                offers.add(new MerchantOffer(
                    new ItemCost(item3, 1),
                    new ItemStack(ModItems.PRIMEVAL_STONE.get(), 20),
                    10, 1, 0.05f
                ));
            }
        }
    }

    private void addRareTrade(MerchantOffers offers, int rank) {
        int rareRank = Math.min(rank + 1, 3);
        List<GuType> rarePool = GuRegistry.getByRank(rareRank);
        if (rarePool.isEmpty()) return;

        GuType rare = rarePool.get(Math.abs(this.getUUID().hashCode()) % rarePool.size());
        Item item = BuiltInRegistries.ITEM.get(rare.id());
        if (item == Items.AIR) return;

        int price = switch (rareRank) {
            case 2 -> 40 + this.random.nextInt(20);
            case 3 -> 80 + this.random.nextInt(40);
            default -> 20;
        };

        offers.add(makeTrade(new ItemStack(item), price, 1));
    }

    private MerchantOffer makeTrade(ItemStack result, int primevalStoneCost, int maxUses) {
        return new MerchantOffer(
            new ItemCost(ModItems.PRIMEVAL_STONE.get(), primevalStoneCost),
            result,
            maxUses,
            1,
            0.05f
        );
    }

    private MerchantOffer makeBuyTrade(ItemStack cost, int costCount, ItemStack result, int maxUses) {
        return new MerchantOffer(
            new ItemCost(cost.getItem(), costCount),
            result,
            maxUses,
            1,
            0.05f
        );
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (offer.shouldRewardExp() && !this.level().isClientSide()) {
            this.level().addFreshEntity(new ExperienceOrb(
                this.level(), this.getX(), this.getY() + 0.5, this.getZ(),
                offer.getXp()
            ));
        }
        if (this.getTradingPlayer() instanceof net.minecraft.server.level.ServerPlayer sp) {
            com.reverendinsanity.util.AdvancementHelper.grant(sp, "trade_with_merchant");
            GuMasterData data = sp.getData(ModAttachments.GU_MASTER_DATA.get());
            data.getFactionReputation().addReputation(getFaction(), 3);
        }
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        super.setTradingPlayer(player);
        if (player instanceof net.minecraft.server.level.ServerPlayer sp && !this.level().isClientSide()) {
            GuMasterData data = sp.getData(ModAttachments.GU_MASTER_DATA.get());
            float discount = data.getFactionReputation().getTradeDiscount(getFaction());
            MerchantOffers offers = this.getOffers();
            for (MerchantOffer offer : offers) {
                int baseCost = offer.getBaseCostA().getCount();
                int adjusted = Math.max(1, Math.round(baseCost * discount));
                int diff = adjusted - baseCost;
                offer.addToSpecialPriceDiff(diff);
            }
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(net.minecraft.server.level.ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GuRank", getGuRank());
        tag.putInt("Faction", getFaction().ordinal());
        tag.putString("PrimaryPath", getPrimaryPath());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GuRank")) {
            setGuRank(tag.getInt("GuRank"));
        }
        if (tag.contains("Faction")) {
            int fIdx = tag.getInt("Faction");
            Faction[] factions = Faction.values();
            setFaction(fIdx >= 0 && fIdx < factions.length ? factions[fIdx] : Faction.INDEPENDENT);
        }
        if (tag.contains("PrimaryPath")) {
            setPrimaryPath(tag.getString("PrimaryPath"));
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean isYesSound) {
        return isYesSound ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public int getGuRank() {
        return this.entityData.get(GU_RANK);
    }

    public void setGuRank(int rank) {
        this.entityData.set(GU_RANK, Math.max(1, Math.min(3, rank)));
    }

    public Faction getFaction() {
        int idx = this.entityData.get(DATA_FACTION);
        Faction[] factions = Faction.values();
        return idx >= 0 && idx < factions.length ? factions[idx] : Faction.INDEPENDENT;
    }

    public void setFaction(Faction faction) {
        this.entityData.set(DATA_FACTION, faction.ordinal());
    }

    public String getPrimaryPath() {
        return this.entityData.get(DATA_PRIMARY_PATH);
    }

    public void setPrimaryPath(String path) {
        this.entityData.set(DATA_PRIMARY_PATH, path != null ? path : "");
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        Faction faction = getFaction();
        net.minecraft.network.chat.Component factionTag = net.minecraft.network.chat.Component.literal("[" + faction.getDisplayName() + "] ").withColor(faction.getColor());
        String pathDisplay = "";
        try {
            DaoPath dp = DaoPath.valueOf(getPrimaryPath());
            pathDisplay = " (" + dp.getDisplayName() + ")";
        } catch (Exception ignored) {}
        return factionTag.copy().append(super.getDisplayName()).append(net.minecraft.network.chat.Component.literal(pathDisplay));
    }
}
