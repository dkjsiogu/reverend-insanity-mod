package com.reverendinsanity.event;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.client.ModKeybindings;
import com.reverendinsanity.client.gui.CultivationOverlay;
import com.reverendinsanity.client.model.WildGuModel;
import com.reverendinsanity.client.renderer.BloodBoltRenderer;
import com.reverendinsanity.client.renderer.IceBoltRenderer;
import com.reverendinsanity.client.renderer.FireBoltRenderer;
import com.reverendinsanity.client.renderer.FormlessHandRenderer;
import com.reverendinsanity.client.renderer.PhantomImmortalRenderer;
import com.reverendinsanity.client.renderer.AncientGuImmortalRenderer;
import com.reverendinsanity.client.renderer.GoldBeamRenderer;
import com.reverendinsanity.client.renderer.GuMasterRenderer;
import com.reverendinsanity.client.renderer.GuShelfBlockEntityRenderer;
import com.reverendinsanity.client.renderer.GuMerchantRenderer;
import com.reverendinsanity.client.renderer.JadeEyeMonkeyRenderer;
import com.reverendinsanity.client.renderer.LightningWolfRenderer;
import com.reverendinsanity.client.renderer.MoonBladeRenderer;
import com.reverendinsanity.client.renderer.MountainBoarRenderer;
import com.reverendinsanity.client.renderer.MountainSpiderRenderer;
import com.reverendinsanity.client.renderer.StrawPuppetRenderer;
import com.reverendinsanity.client.renderer.ThunderCrownWolfRenderer;
import com.reverendinsanity.client.renderer.WildGuRenderer;
import com.reverendinsanity.core.combat.ability.GuAbilityRegistry;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.RefinementRecipe;
import com.reverendinsanity.core.combat.KillerMoveRegistry;
import com.reverendinsanity.core.combat.killermove.MoveEffectRegistry;
import com.reverendinsanity.entity.AncientGuImmortalEntity;
import com.reverendinsanity.entity.FormlessHandEntity;
import com.reverendinsanity.entity.PhantomImmortalEntity;
import com.reverendinsanity.entity.GuMasterEntity;
import com.reverendinsanity.entity.GuMerchantEntity;
import com.reverendinsanity.entity.JadeEyeMonkeyEntity;
import com.reverendinsanity.entity.LightningWolfEntity;
import com.reverendinsanity.entity.MountainBoarEntity;
import com.reverendinsanity.entity.MountainSpiderEntity;
import com.reverendinsanity.entity.StrawPuppetEntity;
import com.reverendinsanity.entity.ThunderCrownWolfEntity;
import com.reverendinsanity.entity.VenerableEntity;
import com.reverendinsanity.client.renderer.VenerableRenderer;
import com.reverendinsanity.entity.WildGuEntity;
import com.reverendinsanity.network.ActivateAbilityPayload;
import com.reverendinsanity.network.ClientPayloadHandler;
import com.reverendinsanity.network.DiscardGuPayload;
import com.reverendinsanity.network.EquipMovePayload;
import com.reverendinsanity.network.FeedGuPayload;
import com.reverendinsanity.network.OpenAperturePayload;
import com.reverendinsanity.network.OpenCodexPayload;
import com.reverendinsanity.network.ServerPayloadHandler;
import com.reverendinsanity.network.SyncApertureContentsPayload;
import com.reverendinsanity.network.SyncCodexPayload;
import com.reverendinsanity.network.SpawnVfxPayload;
import com.reverendinsanity.network.SyncGuMasterDataPayload;
import com.reverendinsanity.network.UseKillerMovePayload;
import com.reverendinsanity.network.StartDeductionPayload;
import com.reverendinsanity.network.CancelDeductionPayload;
import com.reverendinsanity.network.SyncDeductionPayload;
import com.reverendinsanity.network.DeductionResultPayload;
import com.reverendinsanity.network.EnterAperturePayload;
import com.reverendinsanity.network.ExitAperturePayload;
import com.reverendinsanity.network.ExtractResourcePayload;
import com.reverendinsanity.network.OpenDeductionScreenPayload;
import com.reverendinsanity.network.OpenImmortalAperturePayload;
import com.reverendinsanity.network.RepairAperturePayload;
import com.reverendinsanity.network.RepairBreachPayload;
import com.reverendinsanity.network.ResistCalamityPayload;
import com.reverendinsanity.network.SyncDeductionScreenPayload;
import com.reverendinsanity.network.SyncImmortalAperturePayload;
import com.reverendinsanity.network.RadialMenuPayload;
import com.reverendinsanity.network.IntelSyncPayload;
import com.reverendinsanity.network.DamageNumberPayload;
import com.reverendinsanity.network.DefenseActionPayload;
import com.reverendinsanity.registry.ModBlockEntities;
import com.reverendinsanity.registry.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.minecraft.world.entity.monster.Monster;

// Mod Bus 事件（加载阶段）
@EventBusSubscriber(modid = ReverendInsanity.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModBusEvents {

    @SubscribeEvent
    @SuppressWarnings("removal")
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GuRegistry.registerDefaults();
            KillerMoveRegistry.registerDefaults();
            MoveEffectRegistry.registerDefaults();
            GuAbilityRegistry.registerDefaults();
            RefinementRecipe.registerDefaults();
            ReverendInsanity.LOGGER.info("蛊虫注册完成: {} 种", GuRegistry.getAll().size());
            ReverendInsanity.LOGGER.info("炼蛊配方注册完成: {} 种", RefinementRecipe.getAllRecipes().size());
        });
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.WILD_GU.get(), WildGuEntity.createAttributes().build());
        event.put(ModEntities.GU_MASTER.get(), GuMasterEntity.createAttributes().build());
        event.put(ModEntities.GU_MERCHANT.get(), GuMerchantEntity.createAttributes().build());
        event.put(ModEntities.LIGHTNING_WOLF.get(), LightningWolfEntity.createAttributes().build());
        event.put(ModEntities.THUNDER_CROWN_WOLF.get(), ThunderCrownWolfEntity.createAttributes().build());
        event.put(ModEntities.MOUNTAIN_BOAR.get(), MountainBoarEntity.createAttributes().build());
        event.put(ModEntities.JADE_EYE_MONKEY.get(), JadeEyeMonkeyEntity.createAttributes().build());
        event.put(ModEntities.STRAW_PUPPET.get(), StrawPuppetEntity.createAttributes().build());
        event.put(ModEntities.MOUNTAIN_SPIDER.get(), MountainSpiderEntity.createAttributes().build());
        event.put(ModEntities.ANCIENT_GU_IMMORTAL.get(), AncientGuImmortalEntity.createAttributes().build());
        event.put(ModEntities.VENERABLE.get(), VenerableEntity.createAttributes().build());
        event.put(ModEntities.FORMLESS_HAND.get(), FormlessHandEntity.createAttributes().build());
        event.put(ModEntities.PHANTOM_IMMORTAL.get(), PhantomImmortalEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.WILD_GU.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            WildGuEntity::checkSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.LIGHTNING_WOLF.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Monster::checkMonsterSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.MOUNTAIN_BOAR.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Monster::checkMonsterSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.JADE_EYE_MONKEY.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Monster::checkMonsterSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.MOUNTAIN_SPIDER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.OR);
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ReverendInsanity.MODID);
        registrar.playToServer(
            ActivateAbilityPayload.TYPE,
            ActivateAbilityPayload.STREAM_CODEC,
            ServerPayloadHandler::handleActivateAbility
        );
        registrar.playToServer(
            UseKillerMovePayload.TYPE,
            UseKillerMovePayload.STREAM_CODEC,
            ServerPayloadHandler::handleUseKillerMove
        );
        registrar.playToServer(
            OpenAperturePayload.TYPE,
            OpenAperturePayload.STREAM_CODEC,
            ServerPayloadHandler::handleOpenAperture
        );
        registrar.playToServer(
            EquipMovePayload.TYPE,
            EquipMovePayload.STREAM_CODEC,
            ServerPayloadHandler::handleEquipMove
        );
        registrar.playToServer(
            FeedGuPayload.TYPE,
            FeedGuPayload.STREAM_CODEC,
            ServerPayloadHandler::handleFeedGu
        );
        registrar.playToServer(
            DiscardGuPayload.TYPE,
            DiscardGuPayload.STREAM_CODEC,
            ServerPayloadHandler::handleDiscardGu
        );
        registrar.playToClient(
            SyncGuMasterDataPayload.TYPE,
            SyncGuMasterDataPayload.STREAM_CODEC,
            ClientPayloadHandler::handleSyncGuMasterData
        );
        registrar.playToClient(
            SyncApertureContentsPayload.TYPE,
            SyncApertureContentsPayload.STREAM_CODEC,
            ClientPayloadHandler::handleSyncApertureContents
        );
        registrar.playToClient(
            SpawnVfxPayload.TYPE,
            SpawnVfxPayload.STREAM_CODEC,
            ClientPayloadHandler::handleSpawnVfx
        );
        registrar.playToServer(
            OpenCodexPayload.TYPE,
            OpenCodexPayload.STREAM_CODEC,
            ServerPayloadHandler::handleOpenCodex
        );
        registrar.playToClient(
            SyncCodexPayload.TYPE,
            SyncCodexPayload.STREAM_CODEC,
            ClientPayloadHandler::handleSyncCodex
        );
        registrar.playToServer(
            StartDeductionPayload.TYPE,
            StartDeductionPayload.STREAM_CODEC,
            ServerPayloadHandler::handleStartDeduction
        );
        registrar.playToServer(
            CancelDeductionPayload.TYPE,
            CancelDeductionPayload.STREAM_CODEC,
            ServerPayloadHandler::handleCancelDeduction
        );
        registrar.playToClient(
            SyncDeductionPayload.TYPE,
            SyncDeductionPayload.STREAM_CODEC,
            ClientPayloadHandler::handleSyncDeduction
        );
        registrar.playToClient(
            DeductionResultPayload.TYPE,
            DeductionResultPayload.STREAM_CODEC,
            ClientPayloadHandler::handleDeductionResult
        );
        registrar.playToServer(
            EnterAperturePayload.TYPE,
            EnterAperturePayload.STREAM_CODEC,
            ServerPayloadHandler::handleEnterAperture
        );
        registrar.playToServer(
            ExitAperturePayload.TYPE,
            ExitAperturePayload.STREAM_CODEC,
            ServerPayloadHandler::handleExitAperture
        );
        registrar.playToServer(
            ResistCalamityPayload.TYPE,
            ResistCalamityPayload.STREAM_CODEC,
            ServerPayloadHandler::handleResistCalamity
        );
        registrar.playToServer(
            OpenImmortalAperturePayload.TYPE,
            OpenImmortalAperturePayload.STREAM_CODEC,
            ServerPayloadHandler::handleOpenImmortalAperture
        );
        registrar.playToServer(
            ExtractResourcePayload.TYPE,
            ExtractResourcePayload.STREAM_CODEC,
            ServerPayloadHandler::handleExtractResource
        );
        registrar.playToServer(
            RepairAperturePayload.TYPE,
            RepairAperturePayload.STREAM_CODEC,
            ServerPayloadHandler::handleRepairAperture
        );
        registrar.playToServer(
            RepairBreachPayload.TYPE,
            RepairBreachPayload.STREAM_CODEC,
            ServerPayloadHandler::handleRepairBreach
        );
        registrar.playToClient(
            SyncImmortalAperturePayload.TYPE,
            SyncImmortalAperturePayload.STREAM_CODEC,
            ClientPayloadHandler::handleSyncImmortalAperture
        );
        registrar.playToServer(
            OpenDeductionScreenPayload.TYPE,
            OpenDeductionScreenPayload.STREAM_CODEC,
            ServerPayloadHandler::handleOpenDeductionScreen
        );
        registrar.playToClient(
            SyncDeductionScreenPayload.TYPE,
            SyncDeductionScreenPayload.STREAM_CODEC,
            ClientPayloadHandler::handleSyncDeductionScreen
        );
        registrar.playToServer(
            RadialMenuPayload.TYPE,
            RadialMenuPayload.STREAM_CODEC,
            ServerPayloadHandler::handleRadialMenu
        );
        registrar.playToClient(
            IntelSyncPayload.TYPE,
            IntelSyncPayload.STREAM_CODEC,
            ClientPayloadHandler::handleIntelSync
        );
        registrar.playToClient(
            DamageNumberPayload.TYPE,
            DamageNumberPayload.STREAM_CODEC,
            ClientPayloadHandler::handleDamageNumber
        );
        registrar.playToServer(
            DefenseActionPayload.TYPE,
            DefenseActionPayload.STREAM_CODEC,
            ServerPayloadHandler::handleDefenseAction
        );
    }

    @EventBusSubscriber(modid = ReverendInsanity.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModBusEvents {

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            for (var key : ModKeybindings.ALL_ABILITY_KEYS) {
                event.register(key);
            }
            for (var key : ModKeybindings.ALL_KILLER_MOVE_KEYS) {
                event.register(key);
            }
            event.register(ModKeybindings.OPEN_APERTURE);
            event.register(ModKeybindings.OPEN_IMMORTAL_APERTURE);
            event.register(ModKeybindings.OPEN_DEDUCTION);
            event.register(ModKeybindings.OPEN_CODEX);
            event.register(ModKeybindings.RADIAL_MENU);
            event.register(ModKeybindings.DEFENSE_SHIELD);
            event.register(ModKeybindings.DEFENSE_DODGE);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.MOON_BLADE.get(), MoonBladeRenderer::new);
            event.registerEntityRenderer(ModEntities.GOLD_BEAM.get(), GoldBeamRenderer::new);
            event.registerEntityRenderer(ModEntities.BLOOD_BOLT.get(), BloodBoltRenderer::new);
            event.registerEntityRenderer(ModEntities.ICE_BOLT.get(), IceBoltRenderer::new);
            event.registerEntityRenderer(ModEntities.FIRE_BOLT.get(), FireBoltRenderer::new);
            event.registerEntityRenderer(ModEntities.WILD_GU.get(), WildGuRenderer::new);
            event.registerEntityRenderer(ModEntities.GU_MASTER.get(), GuMasterRenderer::new);
            event.registerEntityRenderer(ModEntities.GU_MERCHANT.get(), GuMerchantRenderer::new);
            event.registerEntityRenderer(ModEntities.LIGHTNING_WOLF.get(), LightningWolfRenderer::new);
            event.registerEntityRenderer(ModEntities.THUNDER_CROWN_WOLF.get(), ThunderCrownWolfRenderer::new);
            event.registerEntityRenderer(ModEntities.MOUNTAIN_BOAR.get(), MountainBoarRenderer::new);
            event.registerEntityRenderer(ModEntities.JADE_EYE_MONKEY.get(), JadeEyeMonkeyRenderer::new);
            event.registerEntityRenderer(ModEntities.STRAW_PUPPET.get(), StrawPuppetRenderer::new);
            event.registerEntityRenderer(ModEntities.MOUNTAIN_SPIDER.get(), MountainSpiderRenderer::new);
            event.registerEntityRenderer(ModEntities.ANCIENT_GU_IMMORTAL.get(), AncientGuImmortalRenderer::new);
            event.registerEntityRenderer(ModEntities.VENERABLE.get(), VenerableRenderer::new);
            event.registerEntityRenderer(ModEntities.FORMLESS_HAND.get(), FormlessHandRenderer::new);
            event.registerEntityRenderer(ModEntities.PHANTOM_IMMORTAL.get(), PhantomImmortalRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.GU_SHELF.get(), GuShelfBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(WildGuModel.LAYER_LOCATION, WildGuModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "cultivation_overlay"),
                CultivationOverlay::render
            );
        }
    }
}
