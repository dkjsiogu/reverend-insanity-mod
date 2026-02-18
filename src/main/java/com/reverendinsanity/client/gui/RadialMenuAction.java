package com.reverendinsanity.client.gui;

// 转轮菜单动作定义
public enum RadialMenuAction {
    APERTURE("空窍管理", 0xFF4488FF),
    IMMORTAL_APERTURE("仙窍管理", 0xFFFFAA00),
    CODEX("蛊虫图鉴", 0xFF66DD66),
    DEDUCTION("推演界面", 0xFFDD66DD),
    SECLUSION("闭关修炼", 0xFF88CCEE);

    public final String displayName;
    public final int color;

    RadialMenuAction(String name, int color) {
        this.displayName = name;
        this.color = color;
    }

    public static RadialMenuAction fromIndex(int idx) {
        RadialMenuAction[] vals = values();
        if (idx >= 0 && idx < vals.length) return vals[idx];
        return null;
    }
}
