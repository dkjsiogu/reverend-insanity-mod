package com.reverendinsanity.core.combat.ability;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.ability.impl.*;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

// 蛊虫技能注册表，映射蛊虫类型到对应技能
public class GuAbilityRegistry {

    private static final Map<ResourceLocation, GuAbility> REGISTRY = new LinkedHashMap<>();

    public static void register(GuAbility ability) {
        REGISTRY.put(ability.getGuTypeId(), ability);
    }

    public static GuAbility get(ResourceLocation guTypeId) {
        return REGISTRY.get(guTypeId);
    }

    public static Collection<GuAbility> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static void registerDefaults() {
        register(new MoonBladeAbility());
        register(new BearStrengthAbility());
        register(new JadeSkinAbility());
        register(new WhiteBoarAbility());
        register(new StealthAbility());
        register(new LiquorWormAbility());
        register(new FourFlavorsLiquorAbility());
        register(new GoldLightAbility());
        register(new IronBoneAbility());
        register(new EnslaveSnakeAbility());
        register(new MoonscarAbility());
        register(new SilverMoonAbility());
        register(new WhiteJadeAbility());
        register(new HeavensEyeAbility());
        register(new FleshBoneAbility());
        register(new DisplacementAbility());
        register(new BloodBoltAbility());
        register(new SelfHealAbility());
        register(new SolidifyOriginAbility());
        register(new BloodWingAbility());
        register(new PoisonBeeAbility());
        register(new GoldSilkwormAbility());
        register(new SavageBullAbility());
        register(new TaishanAbility());
        register(new GiantStrengthAbility());
        register(new ColdIceAbility());
        register(new FrostArmorAbility());
        register(new IceSealAbility());
        register(new FireSeedAbility());
        register(new FlameArmorAbility());
        register(new BlazingFlameAbility());
        register(new EarthWallAbility());
        register(new EarthSplitAbility());
        register(new PetrifyAbility());
        register(new BreezeAbility());
        register(new WindBladeAbility());
        register(new GaleAbility());
        register(new LightningAbility());
        register(new ThunderShieldAbility());
        register(new ThunderstormAbility());
        register(new TideAbility());
        register(new WaterShieldAbility());
        register(new TorrentAbility());
        register(new SoulSearchAbility());
        register(new SoulShieldAbility());
        register(new SoulCrushAbility());
        register(new LightBeamAbility());
        register(new RadianceAbility());
        register(new BlazingLightAbility());
        register(new DarkBoltAbility());
        register(new ShadowCloakAbility());
        register(new AbyssDevourAbility());
        register(new DreamAbility());
        register(new LucidDreamAbility());
        register(new NightmareAbility());
        register(new PhantomIllusionAbility());
        register(new MirageAbility());
        register(new GrandIllusionAbility());
        register(new FlyingSwordAbility());
        register(new SwordShieldAbility());
        register(new MyriadSwordAbility());
        register(new MoonSlashAbility());
        register(new BladeArmorAbility());
        register(new HeavenBladeAbility());
        register(new StarLightAbility());
        register(new StarShieldAbility());
        register(new StarFallAbility());
        register(new LuckyAbility());
        register(new MisfortuneWardAbility());
        register(new HeavensSecretAbility());
        register(new KillIntentAbility());
        register(new KillingChanceAbility());
        register(new DeathStrikeAbility());
        register(new ShrinkGroundAbility());
        register(new MorphAbility());
        register(new HeavenChangeAbility());
        register(new FormationSoldierAbility());
        register(new GoldenArmorAbility());
        register(new ThousandArmyAbility());
        register(new SoundWaveAbility());
        register(new SilenceAbility());
        register(new HeavenlySoundAbility());
        register(new BoneSpearAbility());
        register(new BoneArmorAbility());
        register(new WhiteBoneAbility());
        register(new CloudRideAbility());
        register(new FlightWingAbility());
        register(new SkyEagleAbility());
        register(new TrueQiAbility());
        register(new QiShieldAbility());
        register(new ProfoundQiAbility());
        register(new YinYangAbility());
        register(new TaiChiAbility());
        register(new PrimordialAbility());
        register(new WarpAbility());
        register(new SpaceBarrierAbility());
        register(new TimeDecelAbility());
        register(new TimeShieldAbility());
        register(new TimeReversalAbility());
        register(new CharmAbility());
        register(new BewitchAbility());
        register(new SoulCharmAbility());
        register(new ThoughtAbility());
        register(new MindGuardAbility());
        register(new VoidBoltAbility());
        register(new VoidCloakAbility());
        register(new VoidAnnihilationAbility());
        register(new SealAbility());
        register(new RestrictionAbility());
        register(new HeavenSealAbility());
        register(new HeavenWillAbility());
        register(new HeavenShieldAbility());
        register(new HeavenPunishmentAbility());
        register(new RuleAbility());
        register(new OrderAbility());
        register(new SupremeLawAbility());
        register(new ShadowDartAbility());
        register(new ShadowVeilAbility());
        register(new ShadowDevourAbility());
        register(new MistAbility());
        register(new CloudArmorAbility());
        register(new CloudStormAbility());
        register(new TrapFormationAbility());
        register(new FormationShieldAbility());
        register(new GrandFormationAbility());
        register(new RefineFireAbility());
        register(new RefineBodyAbility());
        register(new HeavenRefineAbility());
        register(new PillPoisonAbility());
        register(new PillShieldAbility());
        register(new ImmortalPillAbility());
        register(new PaintBrushAbility());
        register(new PaintShieldAbility());
        register(new MyriadPaintAbility());
        register(new StealQiAbility());
        register(new StealHideAbility());
        register(new HeavenStealAbility());
        register(new InfoDartAbility());
        register(new InfoNetAbility());
        register(new HeavenInfoAbility());
        register(new HumanHeartAbility());
        register(new HumanBondAbility());
        register(new HumanWillAbility());
        register(new EnslaveWormAbility());
        register(new EnslaveShieldAbility());
        register(new FeastAbility());
        register(new SnakeTongueAbility());
        register(new EarthListenerAbility());
        register(new KeenEarAbility());
        register(new HiddenScaleAbility());
        register(new TrueSightAbility());
        register(new ElectricEyeAbility());

        register(new BronzeSariraAbility());
        register(new IronSariraAbility());
        register(new SilverSariraAbility());
        register(new GoldSariraAbility());
        register(new BoarAccumulationAbility());
        register(new BearAccumulationAbility());
        register(new FlowerBoarAbility());
        register(new YellowCamelBeetleAbility());

        register(new StoneSkinAbility());
        register(new IronSkinAbility());
        register(new BeastSkinAbility());
        register(new BlackBristleAbility());
        register(new SteelBristleAbility());
        register(new HeavenCanopyAbility());
        register(new VitalityGrassAbility());
        register(new VitalityLeafAbility());
        register(new WaterSpiderAbility());

        register(new SignalGuAbility());
        register(new FlashGuAbility());
        register(new ShadowFollowerAbility());
        register(new DragonCricketAbility());
        register(new QuietStepAbility());
        register(new ScentLockAbility());
        register(new LoveSeparationAbility());
        ReverendInsanity.LOGGER.info("蛊虫技能注册完成: {} 种", REGISTRY.size());
    }
}
