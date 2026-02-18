package com.reverendinsanity.event;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.entity.AncientGuImmortalEntity;
import com.reverendinsanity.entity.GuMasterEntity;
import com.reverendinsanity.entity.JadeEyeMonkeyEntity;
import com.reverendinsanity.entity.LightningWolfEntity;
import com.reverendinsanity.entity.MountainBoarEntity;
import com.reverendinsanity.entity.MountainSpiderEntity;
import com.reverendinsanity.entity.ThunderCrownWolfEntity;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.registry.ModItems;
import com.reverendinsanity.util.AdvancementHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

// 怪物掉落蛊虫和元石，受气运影响
@EventBusSubscriber(modid = ReverendInsanity.MODID)
public class LootEvents {

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        var entity = event.getEntity();
        var random = entity.getRandom();

        float luck = 1.0f;
        if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide()) {
            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            luck = data.getLuck();
        }

        Item guDrop = null;
        float chance = 0f;

        if (entity instanceof Husk) {
            guDrop = ModItems.BLOOD_GU.get(); chance = 0.05f;
        } else if (entity instanceof Drowned) {
            guDrop = ModItems.SELF_HEAL_GU.get(); chance = 0.05f;
        } else if (entity instanceof Zombie && !(entity instanceof ZombifiedPiglin)) {
            guDrop = ModItems.HOPE_GU.get(); chance = 0.03f;
        } else if (entity instanceof Stray) {
            guDrop = ModItems.SOLIDIFY_ORIGIN_GU.get(); chance = 0.05f;
        } else if (entity instanceof Skeleton && !(entity instanceof WitherSkeleton)) {
            guDrop = ModItems.MOONLIGHT_GU.get(); chance = 0.05f;
        } else if (entity instanceof CaveSpider) {
            guDrop = ModItems.POISON_BEE_GU.get(); chance = 0.08f;
        } else if (entity instanceof Spider) {
            guDrop = ModItems.STEALTH_SCALES_GU.get(); chance = 0.08f;
        } else if (entity instanceof Witch) {
            guDrop = ModItems.LIQUOR_WORM.get(); chance = 0.10f;
        } else if (entity instanceof PiglinBrute) {
            guDrop = ModItems.TORRENT_GU.get(); chance = 0.08f;
        } else if (entity instanceof Piglin) {
            guDrop = ModItems.BEAR_STRENGTH_GU.get(); chance = 0.08f;
        } else if (entity instanceof IronGolem) {
            guDrop = ModItems.JADE_SKIN_GU.get(); chance = 0.15f;
        } else if (entity instanceof Hoglin) {
            guDrop = ModItems.WHITE_BOAR_GU.get(); chance = 0.10f;
        } else if (entity instanceof WitherSkeleton) {
            guDrop = ModItems.IRON_BONE_GU.get(); chance = 0.05f;
        } else if (entity instanceof EnderMan) {
            guDrop = ModItems.MOONSCAR_GU.get(); chance = 0.03f;
        } else if (entity instanceof Vindicator) {
            guDrop = ModItems.FOUR_FLAVORS_LIQUOR_WORM.get(); chance = 0.05f;
        } else if (entity instanceof Evoker) {
            guDrop = ModItems.ENSLAVE_SNAKE_GU.get(); chance = 0.08f;
        } else if (entity instanceof ElderGuardian) {
            guDrop = ModItems.THUNDER_SHIELD_GU.get(); chance = 0.10f;
        } else if (entity instanceof Guardian) {
            guDrop = ModItems.GOLD_LIGHT_WORM.get(); chance = 0.05f;
        } else if (entity instanceof Blaze) {
            guDrop = ModItems.FLESH_BONE_GU.get(); chance = 0.08f;
        } else if (entity instanceof Phantom) {
            guDrop = ModItems.DISPLACEMENT_GU.get(); chance = 0.05f;
        } else if (entity instanceof Ravager) {
            guDrop = ModItems.BLOOD_WING_GU.get(); chance = 0.15f;
        } else if (entity instanceof Pillager) {
            guDrop = ModItems.GOLD_SILKWORM_GU.get(); chance = 0.03f;
        } else if (entity instanceof Creeper) {
            guDrop = ModItems.TAISHAN_GU.get(); chance = 0.05f;
        } else if (entity instanceof Silverfish) {
            guDrop = ModItems.SAVAGE_BULL_GU.get(); chance = 0.08f;
        } else if (entity instanceof MagmaCube) {
            guDrop = ModItems.FIRE_SEED_GU.get(); chance = 0.08f;
        } else if (entity instanceof ZombifiedPiglin) {
            guDrop = ModItems.FLAME_ARMOR_GU.get(); chance = 0.05f;
        } else if (entity instanceof Ghast) {
            guDrop = ModItems.BLAZING_FLAME_GU.get(); chance = 0.05f;
        } else if (entity instanceof Slime && !(entity instanceof MagmaCube)) {
            guDrop = ModItems.EARTH_WALL_GU.get(); chance = 0.05f;
        } else if (entity instanceof Endermite) {
            guDrop = ModItems.EARTH_SPLIT_GU.get(); chance = 0.08f;
        } else if (entity instanceof Shulker) {
            guDrop = ModItems.PETRIFY_GU.get(); chance = 0.08f;
        } else if (entity instanceof Breeze) {
            guDrop = ModItems.BREEZE_GU.get(); chance = 0.08f;
        } else if (entity instanceof Vex) {
            guDrop = ModItems.WIND_BLADE_GU.get(); chance = 0.08f;
        } else if (entity instanceof Zoglin) {
            guDrop = ModItems.GALE_GU.get(); chance = 0.10f;
        } else if (entity instanceof Bogged) {
            guDrop = ModItems.LIGHTNING_GU.get(); chance = 0.05f;
        } else if (entity instanceof Warden) {
            guDrop = ModItems.THUNDERSTORM_GU.get(); chance = 0.15f;
        } else if (entity instanceof Squid && !(entity instanceof GlowSquid)) {
            guDrop = ModItems.TIDE_GU.get(); chance = 0.05f;
        } else if (entity instanceof Dolphin) {
            guDrop = ModItems.WATER_SHIELD_GU.get(); chance = 0.03f;
        } else if (entity instanceof Allay) {
            guDrop = ModItems.SOUL_SEARCH_GU.get(); chance = 0.05f;
        } else if (entity instanceof GlowSquid) {
            guDrop = ModItems.SOUL_SHIELD_GU.get(); chance = 0.05f;
        } else if (entity instanceof Strider) {
            guDrop = ModItems.SOUL_CRUSH_GU.get(); chance = 0.08f;
        } else if (entity instanceof Bee) {
            guDrop = ModItems.LIGHT_BEAM_GU.get(); chance = 0.03f;
        } else if (entity instanceof Sheep) {
            guDrop = ModItems.RADIANCE_GU.get(); chance = 0.03f;
        } else if (entity instanceof PolarBear) {
            guDrop = ModItems.BLAZING_LIGHT_GU.get(); chance = 0.08f;
        } else if (entity instanceof Bat) {
            guDrop = ModItems.DARK_BOLT_GU.get(); chance = 0.08f;
        } else if (entity instanceof Cat) {
            guDrop = ModItems.SHADOW_CLOAK_GU.get(); chance = 0.03f;
        } else if (entity instanceof Wolf) {
            guDrop = ModItems.ABYSS_DEVOUR_GU.get(); chance = 0.08f;
        } else if (entity instanceof Rabbit) {
            guDrop = ModItems.DREAM_GU.get(); chance = 0.03f;
        } else if (entity instanceof Fox) {
            guDrop = ModItems.LUCID_DREAM_GU.get(); chance = 0.05f;
        } else if (entity instanceof MushroomCow) {
            guDrop = ModItems.NIGHTMARE_GU.get(); chance = 0.08f;
        } else if (entity instanceof Ocelot) {
            guDrop = ModItems.PHANTOM_GU.get(); chance = 0.05f;
        } else if (entity instanceof Pufferfish) {
            guDrop = ModItems.MIRAGE_GU.get(); chance = 0.05f;
        } else if (entity instanceof Parrot) {
            guDrop = ModItems.GRAND_ILLUSION_GU.get(); chance = 0.08f;
        } else if (entity instanceof Chicken) {
            guDrop = ModItems.FLYING_SWORD_GU.get(); chance = 0.03f;
        } else if (entity instanceof Cow && !(entity instanceof MushroomCow)) {
            guDrop = ModItems.SWORD_SHIELD_GU.get(); chance = 0.03f;
        } else if (entity instanceof Pig) {
            guDrop = ModItems.MYRIAD_SWORD_GU.get(); chance = 0.08f;
        } else if (entity instanceof Turtle) {
            guDrop = ModItems.MOON_SLASH_GU.get(); chance = 0.05f;
        } else if (entity instanceof Goat) {
            guDrop = ModItems.BLADE_ARMOR_GU.get(); chance = 0.05f;
        } else if (entity instanceof Llama) {
            guDrop = ModItems.HEAVEN_BLADE_GU.get(); chance = 0.08f;
        } else if (entity instanceof Horse) {
            guDrop = ModItems.STARLIGHT_GU.get(); chance = 0.03f;
        } else if (entity instanceof Donkey) {
            guDrop = ModItems.STAR_SHIELD_GU.get(); chance = 0.03f;
        } else if (entity instanceof Axolotl) {
            guDrop = ModItems.STAR_FALL_GU.get(); chance = 0.08f;
        } else if (entity instanceof Frog) {
            guDrop = ModItems.LUCKY_GU.get(); chance = 0.05f;
        } else if (entity instanceof Panda) {
            guDrop = ModItems.MISFORTUNE_WARD_GU.get(); chance = 0.05f;
        } else if (entity instanceof SnowGolem) {
            guDrop = ModItems.HEAVENS_SECRET_GU.get(); chance = 0.08f;
        } else if (entity instanceof Camel) {
            guDrop = ModItems.KILL_INTENT_GU.get(); chance = 0.05f;
        } else if (entity instanceof Salmon) {
            guDrop = ModItems.KILLING_CHANCE_GU.get(); chance = 0.03f;
        } else if (entity instanceof Cod) {
            guDrop = ModItems.DEATH_STRIKE_GU.get(); chance = 0.08f;
        } else if (entity instanceof Armadillo) {
            guDrop = ModItems.SHRINK_GROUND_GU.get(); chance = 0.05f;
        } else if (entity instanceof Sniffer) {
            guDrop = ModItems.MORPH_GU.get(); chance = 0.05f;
        } else if (entity instanceof TropicalFish) {
            guDrop = ModItems.HEAVEN_CHANGE_GU.get(); chance = 0.08f;
        } else if (entity instanceof Mule) {
            guDrop = ModItems.FORMATION_SOLDIER_GU.get(); chance = 0.05f;
        } else if (entity instanceof SkeletonHorse) {
            guDrop = ModItems.GOLDEN_ARMOR_GU.get(); chance = 0.05f;
        } else if (entity instanceof ZombieHorse) {
            guDrop = ModItems.THOUSAND_ARMY_GU.get(); chance = 0.08f;
        } else if (entity instanceof WanderingTrader) {
            guDrop = ModItems.SOUND_WAVE_GU.get(); chance = 0.05f;
        } else if (entity instanceof Villager) {
            guDrop = ModItems.SILENCE_GU.get(); chance = 0.03f;
        }

        if (entity instanceof LightningWolfEntity) {
            int boneCount = random.nextInt(3);
            if (boneCount > 0) {
                ItemEntity boneEntity = new ItemEntity(entity.level(),
                    entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(ModItems.BEAST_BONE.get(), boneCount));
                event.getDrops().add(boneEntity);
            }
        } else if (entity instanceof ThunderCrownWolfEntity) {
            int stoneCount = 2 + random.nextInt(3);
            ItemEntity stoneEntity = new ItemEntity(entity.level(),
                entity.getX(), entity.getY(), entity.getZ(),
                new ItemStack(ModItems.PRIMEVAL_STONE.get(), stoneCount));
            event.getDrops().add(stoneEntity);
            if (random.nextFloat() < applyLuck(0.15f, luck)) {
                java.util.List<Item> rank2Pool = java.util.List.of(
                    ModItems.FOUR_FLAVORS_LIQUOR_WORM.get(),
                    ModItems.GOLD_LIGHT_WORM.get(),
                    ModItems.IRON_BONE_GU.get(),
                    ModItems.ENSLAVE_SNAKE_GU.get(),
                    ModItems.MOONSCAR_GU.get()
                );
                Item rank2Drop = rank2Pool.get(random.nextInt(rank2Pool.size()));
                ItemEntity guEntity = new ItemEntity(entity.level(),
                    entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(rank2Drop));
                event.getDrops().add(guEntity);
            }
        } else if (entity instanceof MountainBoarEntity) {
            int boneCount = 1 + random.nextInt(3);
            ItemEntity boneEntity = new ItemEntity(entity.level(),
                entity.getX(), entity.getY(), entity.getZ(),
                new ItemStack(ModItems.BEAST_BONE.get(), boneCount));
            event.getDrops().add(boneEntity);
            if (random.nextFloat() < 0.5f) {
                ItemEntity leatherEntity = new ItemEntity(entity.level(),
                    entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(net.minecraft.world.item.Items.LEATHER));
                event.getDrops().add(leatherEntity);
            }
        } else if (entity instanceof JadeEyeMonkeyEntity) {
            int eyeCount = random.nextFloat() < 0.6f ? 1 : 0;
            if (eyeCount > 0) {
                ItemEntity eyeEntity = new ItemEntity(entity.level(),
                    entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(ModItems.JADE_EYE.get(), eyeCount));
                event.getDrops().add(eyeEntity);
            }
        } else if (entity instanceof MountainSpiderEntity) {
            int silkCount = 1 + random.nextInt(3);
            ItemEntity silkEntity = new ItemEntity(entity.level(),
                entity.getX(), entity.getY(), entity.getZ(),
                new ItemStack(ModItems.SPIDER_SILK.get(), silkCount));
            event.getDrops().add(silkEntity);
            int stringCount = random.nextInt(3);
            if (stringCount > 0) {
                ItemEntity stringEntity = new ItemEntity(entity.level(),
                    entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(net.minecraft.world.item.Items.STRING, stringCount));
                event.getDrops().add(stringEntity);
            }
        }

        if (guDrop != null) {
            float adjustedChance = applyLuck(chance, luck);
            if (random.nextFloat() < adjustedChance) {
                ItemEntity itemEntity = new ItemEntity(
                        entity.level(),
                        entity.getX(), entity.getY(), entity.getZ(),
                        new ItemStack(guDrop));
                event.getDrops().add(itemEntity);
            }
        }

        if (entity instanceof AncientGuImmortalEntity) {
            if (event.getSource().getEntity() instanceof ServerPlayer sp) {
                AdvancementHelper.grant(sp, "defeat_ancient_immortal");
            }
        }

        if (entity instanceof GuMasterEntity) {
            if (event.getSource().getEntity() instanceof ServerPlayer sp) {
                AdvancementHelper.grant(sp, "defeat_gu_master");
            }
        }

        if (entity instanceof Enemy) {
            float stoneChance = applyLuck(0.15f, luck);
            if (random.nextFloat() < stoneChance) {
                int baseCount = 1 + random.nextInt(3);
                if (luck > 1.0f) {
                    baseCount += random.nextInt((int) Math.ceil(luck));
                }
                ItemEntity stoneEntity = new ItemEntity(
                        entity.level(),
                        entity.getX(), entity.getY(), entity.getZ(),
                        new ItemStack(ModItems.PRIMEVAL_STONE.get(), baseCount));
                event.getDrops().add(stoneEntity);
            }
        }

        if (entity instanceof Witch || entity instanceof Evoker) {
            if (random.nextFloat() < applyLuck(0.06f, luck)) {
                Item scoutDrop = random.nextFloat() < 0.5f ? ModItems.SNAKE_TONGUE_GU.get() : ModItems.EARTH_LISTENER_GU.get();
                event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(scoutDrop)));
            }
        }
        if (entity instanceof Villager) {
            if (random.nextFloat() < applyLuck(0.03f, luck)) {
                event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(ModItems.KEEN_EAR_GU.get())));
            }
        }
        if (entity instanceof Phantom || entity instanceof EnderMan) {
            if (random.nextFloat() < applyLuck(0.05f, luck)) {
                event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(ModItems.HIDDEN_SCALE_GU.get())));
            }
        }
        if (entity instanceof Blaze || entity instanceof ElderGuardian) {
            if (random.nextFloat() < applyLuck(0.05f, luck)) {
                event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(ModItems.TRUE_SIGHT_GU.get())));
            }
        }
        if (entity instanceof LightningWolfEntity) {
            if (random.nextFloat() < applyLuck(0.08f, luck)) {
                event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(ModItems.ELECTRIC_EYE_GU.get())));
            }
        }
    }

    private static float applyLuck(float baseChance, float luck) {
        if (luck == 0f) {
            return baseChance * 0.5f;
        }
        return Math.min(baseChance * luck, 1.0f);
    }
}
