package com.reverendinsanity.client;

import com.reverendinsanity.ReverendInsanity;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

// 蛊虫技能和杀招快捷键
public class ModKeybindings {

    public static final String CATEGORY = "key.categories." + ReverendInsanity.MODID;

    public static final KeyMapping ABILITY_SLOT_1 = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".ability_1",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_R,
        CATEGORY
    );

    public static final KeyMapping ABILITY_SLOT_2 = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".ability_2",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_F,
        CATEGORY
    );

    public static final KeyMapping ABILITY_SLOT_3 = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".ability_3",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_V,
        CATEGORY
    );

    public static final KeyMapping ABILITY_SLOT_4 = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".ability_4",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_C,
        CATEGORY
    );

    public static final KeyMapping[] ALL_ABILITY_KEYS = {
        ABILITY_SLOT_1, ABILITY_SLOT_2, ABILITY_SLOT_3, ABILITY_SLOT_4
    };

    public static final KeyMapping KILLER_MOVE_1 = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".killer_move_1",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_Z,
        CATEGORY
    );

    public static final KeyMapping KILLER_MOVE_2 = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".killer_move_2",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_X,
        CATEGORY
    );

    public static final KeyMapping KILLER_MOVE_3 = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".killer_move_3",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_N,
        CATEGORY
    );

    public static final KeyMapping KILLER_MOVE_4 = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".killer_move_4",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_M,
        CATEGORY
    );

    public static final KeyMapping[] ALL_KILLER_MOVE_KEYS = {
        KILLER_MOVE_1, KILLER_MOVE_2, KILLER_MOVE_3, KILLER_MOVE_4
    };

    public static final KeyMapping OPEN_APERTURE = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".open_aperture",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_G,
        CATEGORY
    );

    public static final KeyMapping OPEN_IMMORTAL_APERTURE = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".open_immortal_aperture",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_H,
        CATEGORY
    );

    public static final KeyMapping OPEN_DEDUCTION = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".open_deduction",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_J,
        CATEGORY
    );

    public static final KeyMapping OPEN_CODEX = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".open_codex",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_K,
        CATEGORY
    );

    public static final KeyMapping RADIAL_MENU = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".radial_menu",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_GRAVE,
        CATEGORY
    );

    public static final KeyMapping DEFENSE_SHIELD = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".defense_shield",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_B,
        CATEGORY
    );

    public static final KeyMapping DEFENSE_DODGE = new KeyMapping(
        "key." + ReverendInsanity.MODID + ".defense_dodge",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_LALT,
        CATEGORY
    );
}
