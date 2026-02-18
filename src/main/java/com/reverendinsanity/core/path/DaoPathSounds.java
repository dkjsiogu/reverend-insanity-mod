package com.reverendinsanity.core.path;

import com.reverendinsanity.core.combat.KillerMove;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

// 道路音效映射：48道路差异化技能/杀招音效
public final class DaoPathSounds {

    private DaoPathSounds() {}

    public static SoundEvent getAbilitySound(DaoPath path) {
        return switch (path) {
            case STRENGTH -> SoundEvents.IRON_GOLEM_HURT;
            case BLOOD -> SoundEvents.GENERIC_SPLASH;
            case SWORD -> SoundEvents.PLAYER_ATTACK_SWEEP;
            case BLADE -> SoundEvents.PLAYER_ATTACK_CRIT;
            case KILL -> SoundEvents.WITHER_SHOOT;
            case SOLDIER -> SoundEvents.TRIDENT_THROW.value();
            case SOUL -> SoundEvents.WARDEN_SONIC_BOOM;
            case DREAM -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case WISDOM -> SoundEvents.ENCHANTMENT_TABLE_USE;
            case CHARM -> SoundEvents.AMETHYST_CLUSTER_PLACE;
            case ILLUSION -> SoundEvents.ENDERMAN_TELEPORT;
            case FIRE -> SoundEvents.FIRECHARGE_USE;
            case WATER -> SoundEvents.GENERIC_SPLASH;
            case EARTH -> SoundEvents.ANVIL_LAND;
            case METAL -> SoundEvents.ANVIL_USE;
            case WOOD -> SoundEvents.GRASS_BREAK;
            case WIND -> SoundEvents.ELYTRA_FLYING;
            case LIGHTNING -> SoundEvents.TRIDENT_THUNDER.value();
            case ICE -> SoundEvents.GLASS_BREAK;
            case LIGHT -> SoundEvents.BEACON_ACTIVATE;
            case DARK -> SoundEvents.WARDEN_SONIC_BOOM;
            case SHADOW -> SoundEvents.ENDERMAN_TELEPORT;
            case CLOUD -> SoundEvents.WOOL_PLACE;
            case SPACE -> SoundEvents.CHORUS_FRUIT_TELEPORT;
            case TIME -> SoundEvents.BELL_BLOCK;
            case RULE -> SoundEvents.BEACON_DEACTIVATE;
            case LUCK -> SoundEvents.NOTE_BLOCK_CHIME.value();
            case HEAVEN -> SoundEvents.TRIDENT_THUNDER.value();
            case HUMAN -> SoundEvents.VILLAGER_YES;
            case STAR -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case REFINEMENT -> SoundEvents.ANVIL_USE;
            case FORMATION -> SoundEvents.BEACON_ACTIVATE;
            case PILL -> SoundEvents.BREWING_STAND_BREW;
            case ENSLAVE -> SoundEvents.WARDEN_SONIC_BOOM;
            case FOOD -> SoundEvents.GENERIC_EAT;
            case PAINT -> SoundEvents.GLOW_INK_SAC_USE;
            case STEAL -> SoundEvents.ENDERMAN_TELEPORT;
            case BONE -> SoundEvents.SKELETON_HURT;
            case SOUND -> SoundEvents.NOTE_BLOCK_CHIME.value();
            case INFORMATION -> SoundEvents.ENCHANTMENT_TABLE_USE;
            case POISON -> SoundEvents.HONEY_DRINK;
            case TRANSFORMATION -> SoundEvents.ENDERMAN_TELEPORT;
            case YIN_YANG -> SoundEvents.BELL_BLOCK;
            case FLIGHT -> SoundEvents.ELYTRA_FLYING;
            case MOON -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case QI -> SoundEvents.BEACON_ACTIVATE;
            case VOID -> SoundEvents.WARDEN_SONIC_BOOM;
            case RESTRICTION -> SoundEvents.ANVIL_USE;
        };
    }

    public static float getAbilityVolume(DaoPath path) {
        return switch (path.getCategory()) {
            case COMBAT -> 0.5f;
            case SPIRITUAL -> 0.35f;
            case ELEMENTAL -> 0.45f;
            case RULE -> 0.4f;
            case SUPPORT -> 0.35f;
            case SPECIAL -> 0.4f;
        };
    }

    public static float getAbilityPitch(DaoPath path) {
        return switch (path) {
            case STRENGTH, EARTH, BONE -> 0.7f;
            case BLOOD, KILL -> 0.8f;
            case SWORD, BLADE, SOLDIER, METAL -> 1.0f;
            case SOUL -> 1.4f;
            case DREAM, CHARM, MOON -> 1.6f;
            case WISDOM, INFORMATION -> 1.2f;
            case ILLUSION, SHADOW, STEAL -> 1.8f;
            case FIRE, LIGHTNING -> 0.9f;
            case WATER, CLOUD, QI, WOOD -> 1.1f;
            case WIND, FLIGHT -> 1.5f;
            case ICE -> 1.3f;
            case LIGHT, STAR, HEAVEN -> 1.6f;
            case DARK, VOID -> 0.6f;
            case SPACE, TIME, RULE -> 1.0f;
            case LUCK -> 1.4f;
            case HUMAN -> 1.1f;
            case REFINEMENT, FORMATION, RESTRICTION -> 0.9f;
            case PILL, FOOD -> 1.2f;
            case ENSLAVE -> 0.8f;
            case PAINT -> 1.3f;
            case SOUND -> 1.5f;
            case POISON -> 0.9f;
            case TRANSFORMATION -> 1.2f;
            case YIN_YANG -> 1.0f;
        };
    }

    public static SoundEvent getKillerMoveSound(KillerMove.MoveType type, DaoPath path) {
        return switch (type) {
            case HEAL -> SoundEvents.BEACON_ACTIVATE;
            case ULTIMATE -> SoundEvents.GENERIC_EXPLODE.value();
            default -> getAbilitySound(path);
        };
    }

    public static float getKillerMoveVolume(KillerMove.MoveType type) {
        return switch (type) {
            case ATTACK -> 0.8f;
            case ULTIMATE -> 1.0f;
            case DEFENSE, BUFF -> 0.6f;
            case CONTROL, DEBUFF -> 0.7f;
            case MOVEMENT -> 0.5f;
            case HEAL -> 0.5f;
        };
    }

    public static float getKillerMovePitch(KillerMove.MoveType type) {
        return switch (type) {
            case ATTACK -> 0.7f;
            case ULTIMATE -> 0.5f;
            case DEFENSE, BUFF -> 1.1f;
            case CONTROL, DEBUFF -> 1.3f;
            case MOVEMENT -> 1.7f;
            case HEAL -> 1.1f;
        };
    }

    public static SoundEvent getBreakthroughSound() {
        return SoundEvents.TRIDENT_THUNDER.value();
    }

    public static SoundEvent getApertureOpenSound() {
        return SoundEvents.BEACON_ACTIVATE;
    }
}
