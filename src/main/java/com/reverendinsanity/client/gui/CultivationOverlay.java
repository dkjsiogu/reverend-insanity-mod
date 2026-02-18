package com.reverendinsanity.client.gui;

import com.reverendinsanity.client.ClientDataCache;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.List;

// 修炼状态HUD：显示境界、真元、念头、道痕、蛊虫、杀招、气运、增益信息
public class CultivationOverlay {

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        if (!ClientDataCache.isOpened()) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int x = 5;
        int y = screenHeight - 79;

        String rankText = getRankName(ClientDataCache.getRankLevel()) + "\u00b7" + getSubRankName(ClientDataCache.getSubRankIndex());
        String aptText = " \u27e8" + ClientDataCache.getAptitudeName() + "\u27e9";
        graphics.drawString(font, rankText + aptText, x, y, 0xFFFFFF);

        y += 12;
        int barWidth = 120;
        int barHeight = 8;
        float essenceRatio = ClientDataCache.getCurrentEssence() / Math.max(ClientDataCache.getMaxEssence(), 1);
        int essenceColor = ClientDataCache.getEssenceColor();
        graphics.fill(x, y, x + barWidth, y + barHeight, 0x88000000);
        graphics.fill(x, y, x + (int) (barWidth * essenceRatio), y + barHeight, 0xFF000000 | essenceColor);
        String essenceText = (int) ClientDataCache.getCurrentEssence() + "/" + (int) ClientDataCache.getMaxEssence() + " \u771f\u5143";
        graphics.drawString(font, essenceText, x + barWidth + 4, y, essenceColor);

        y += 12;
        float thoughtsRatio = ClientDataCache.getThoughts() / Math.max(ClientDataCache.getMaxThoughts(), 1);
        graphics.fill(x, y, x + barWidth, y + barHeight, 0x88000000);
        graphics.fill(x, y, x + (int) (barWidth * thoughtsRatio), y + barHeight, 0xFF66CCFF);
        String thoughtsText = (int) ClientDataCache.getThoughts() + "/" + (int) ClientDataCache.getMaxThoughts() + " \u5ff5\u5934";
        graphics.drawString(font, thoughtsText, x + barWidth + 4, y, 0x66CCFF);

        y += 12;
        renderDaoMarks(graphics, font, x, y, barWidth, barHeight);

        y += 12;
        graphics.drawString(font, "\u86ca:" + ClientDataCache.getGuCount() + "  \u6740\u62db:" + ClientDataCache.getEquippedMoveCount(), x, y, 0xAAAAAA);

        String fName = ClientDataCache.getFactionName();
        if (fName != null && !fName.isEmpty()) {
            String tierName = ClientDataCache.getFactionTierName();
            int fColor = ClientDataCache.getFactionColor();
            int factionLabelX = x + font.width("\u86ca:" + ClientDataCache.getGuCount() + "  \u6740\u62db:" + ClientDataCache.getEquippedMoveCount()) + 8;
            graphics.drawString(font, fName + "\u00b7" + tierName, factionLabelX, y, fColor);
        }

        float luck = ClientDataCache.getLuck();
        if (luck != 1.0f) {
            y += 12;
            renderLuckStatus(graphics, font, x, y, luck);
        }

        List<ClientDataCache.BuffDisplayInfo> buffs = ClientDataCache.getActiveBuffs();
        if (!buffs.isEmpty()) {
            y += 12;
            renderActiveBuffs(graphics, font, x, y, buffs);
        }

        if (ClientDataCache.isDeductionActive()) {
            y += 14;
            renderDeductionStatus(graphics, font, x, y);
        }

        int maxLifespan = ClientDataCache.getMaxLifespan();
        if (maxLifespan > 0) {
            y += 14;
            renderLifespan(graphics, font, x, y, barWidth, barHeight);
        }

        int heavenWill = ClientDataCache.getHeavenWillAttention();
        if (heavenWill > 50) {
            renderHeavenWillWarning(graphics, font, screenWidth);
        }

        String targetInfo = ClientDataCache.getTargetIntelInfo();
        if (targetInfo != null && !targetInfo.isEmpty()) {
            renderTargetInfo(graphics, font, screenWidth, screenHeight, targetInfo);
        }
    }

    private static void renderActiveBuffs(GuiGraphics graphics, Font font, int x, int y, List<ClientDataCache.BuffDisplayInfo> buffs) {
        int drawX = x;
        for (ClientDataCache.BuffDisplayInfo buff : buffs) {
            String name = resolveBuffName(buff.idPath());
            int color = getBuffColor(buff.idPath());
            String label = name + " " + buff.remainingSeconds() + "s";
            int labelW = font.width(label) + 4;
            graphics.fill(drawX, y, drawX + labelW, y + 10, 0x88000000);
            graphics.fill(drawX, y, drawX + 2, y + 10, 0xFF000000 | color);
            graphics.drawString(font, label, drawX + 3, y + 1, color);
            drawX += labelW + 2;
            if (drawX > 300) {
                drawX = x;
                y += 11;
            }
        }
    }

    private static String resolveBuffName(String idPath) {
        String stripped = idPath;
        if (stripped.startsWith("buff_")) stripped = stripped.substring(5);
        if (stripped.endsWith("_gu")) stripped = stripped.substring(0, stripped.length() - 3);
        String itemKey = "item.reverend_insanity." + stripped + "_gu";
        String translated = Component.translatable(itemKey).getString();
        if (!translated.equals(itemKey)) {
            if (translated.endsWith("\u86ca")) {
                return translated.substring(0, translated.length() - 1);
            }
            return translated;
        }
        return formatIdPath(stripped);
    }

    private static String formatIdPath(String path) {
        StringBuilder sb = new StringBuilder();
        for (String part : path.split("_")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    private static int getBuffColor(String idPath) {
        if (idPath.contains("shield") || idPath.contains("armor") || idPath.contains("wall")
            || idPath.contains("cloak") || idPath.contains("barrier") || idPath.contains("guard")
            || idPath.contains("jade") || idPath.contains("bone_armor") || idPath.contains("iron_bone")
            || idPath.contains("bastion") || idPath.contains("bronze") || idPath.contains("veil")
            || idPath.contains("order") || idPath.contains("restriction") || idPath.contains("formation_shield")) {
            return 0x55AAFF;
        }
        if (idPath.contains("strength") || idPath.contains("bull") || idPath.contains("strike")
            || idPath.contains("kill") || idPath.contains("blade") || idPath.contains("sword")
            || idPath.contains("boar") || idPath.contains("giant") || idPath.contains("shadow_strike")) {
            return 0xFF6644;
        }
        if (idPath.contains("heal") || idPath.contains("wing") || idPath.contains("flight")
            || idPath.contains("stealth") || idPath.contains("lucky") || idPath.contains("breeze")
            || idPath.contains("gale") || idPath.contains("radiance") || idPath.contains("morph")
            || idPath.contains("lucid") || idPath.contains("steal")) {
            return 0x55FF88;
        }
        if (idPath.contains("frost") || idPath.contains("ice") || idPath.contains("cold")) {
            return 0x88DDFF;
        }
        if (idPath.contains("flame") || idPath.contains("fire") || idPath.contains("blaz")) {
            return 0xFF8833;
        }
        return 0xCC88FF;
    }

    private static void renderDaoMarks(GuiGraphics graphics, Font font, int x, int y, int barWidth, int barHeight) {
        String pathName = ClientDataCache.getPrimaryPathName();
        int marks = ClientDataCache.getPrimaryPathMarks();
        if (pathName == null || pathName.isEmpty()) return;

        int nextThreshold = getNextThreshold(marks);
        float ratio = (float) marks / nextThreshold;
        int color = getDaoMarkColor(marks);

        graphics.fill(x, y, x + barWidth, y + barHeight, 0x88000000);
        graphics.fill(x, y, x + (int) (barWidth * Math.min(ratio, 1.0f)), y + barHeight, 0xFF000000 | color);
        String markText = pathName + " \u9053\u75d5:" + marks + "/" + nextThreshold;
        graphics.drawString(font, markText, x + barWidth + 4, y, color);
    }

    private static int getNextThreshold(int marks) {
        if (marks < 100) return 100;
        if (marks < 500) return 500;
        if (marks < 1500) return 1500;
        if (marks < 3000) return 3000;
        if (marks < 5000) return 5000;
        if (marks < 8000) return 8000;
        return 10000;
    }

    private static int getDaoMarkColor(int marks) {
        if (marks < 100) return 0x888888;
        if (marks < 500) return 0x55FF55;
        if (marks < 1500) return 0x5555FF;
        if (marks < 3000) return 0xAA55FF;
        if (marks < 5000) return 0xFFAA00;
        if (marks < 8000) return 0xFF5555;
        return 0xFFFFFF;
    }

    private static void renderLuckStatus(GuiGraphics graphics, Font font, int x, int y, float luck) {
        String luckText;
        int color;

        if (luck > 1.2f) {
            luckText = "\u9e3f\u8fd0";
            color = 0xFFAA00;
        } else if (luck > 1.0f) {
            luckText = "\u597d\u8fd0";
            color = 0x55FF55;
        } else if (luck >= 0.7f) {
            luckText = "\u5384\u8fd0";
            color = 0xFF5555;
        } else if (luck >= 0.5f) {
            luckText = "\u5927\u51f6";
            color = 0xAA0000;
            long time = System.currentTimeMillis();
            if ((time / 500) % 2 == 0) {
                color = 0x550000;
            }
        } else {
            luckText = "\u5927\u51f6";
            color = 0xAA0000;
            long time = System.currentTimeMillis();
            if ((time / 300) % 2 == 0) {
                color = 0x550000;
            }
        }

        graphics.drawString(font, "\u6c14\u8fd0:" + luckText, x, y, color);
    }

    private static String getRankName(int level) {
        return switch (level) {
            case 1 -> "\u4e00\u8f6c";
            case 2 -> "\u4e8c\u8f6c";
            case 3 -> "\u4e09\u8f6c";
            case 4 -> "\u56db\u8f6c";
            case 5 -> "\u4e94\u8f6c";
            case 6 -> "\u516d\u8f6c";
            case 7 -> "\u4e03\u8f6c";
            case 8 -> "\u516b\u8f6c";
            case 9 -> "\u4e5d\u8f6c";
            default -> "\u672a\u77e5";
        };
    }

    private static String getSubRankName(int index) {
        return switch (index) {
            case 0 -> "\u521d\u9636";
            case 1 -> "\u4e2d\u9636";
            case 2 -> "\u4e0a\u9636";
            case 3 -> "\u5dc5\u5cf0";
            default -> "\u521d\u9636";
        };
    }

    private static void renderDeductionStatus(GuiGraphics graphics, Font font, int x, int y) {
        float progress = ClientDataCache.getDeductionProgress();
        int barW = 80;
        int barH = 6;
        int filled = (int)(barW * progress);

        long time = System.currentTimeMillis();
        int textCol = (time / 500) % 2 == 0 ? 0xFFDD88 : 0xFFCC66;

        graphics.drawString(font, "\u63a8\u6f14", x, y, textCol);
        graphics.fill(x + 30, y + 1, x + 30 + barW, y + 1 + barH, 0x88000000);
        if (filled > 0) {
            graphics.fill(x + 30, y + 1, x + 30 + filled, y + 1 + barH, 0xFF6688FF);
        }
        String pct = String.format("%.0f%%", progress * 100);
        graphics.drawString(font, pct, x + 30 + barW + 4, y, 0x88AAFF);
    }

    private static void renderLifespan(GuiGraphics graphics, Font font, int x, int y, int barWidth, int barHeight) {
        int lifespan = ClientDataCache.getLifespan();
        int maxLifespan = ClientDataCache.getMaxLifespan();
        if (maxLifespan <= 0) return;

        float ratio = (float) lifespan / maxLifespan;
        int color;
        if (ratio > 0.5f) {
            color = 0x44DD44;
        } else if (ratio > 0.2f) {
            color = 0xDDDD44;
        } else {
            color = 0xDD4444;
            long time = System.currentTimeMillis();
            if ((time / 400) % 2 == 0) color = 0x882222;
        }

        graphics.fill(x, y, x + barWidth, y + barHeight, 0x88000000);
        graphics.fill(x, y, x + (int)(barWidth * ratio), y + barHeight, 0xFF000000 | color);
        String lifespanText = lifespan + "/" + maxLifespan + " \u5bff\u5143";
        graphics.drawString(font, lifespanText, x + barWidth + 4, y, color);
    }

    private static void renderHeavenWillWarning(GuiGraphics graphics, Font font, int screenWidth) {
        int heavenWill = ClientDataCache.getHeavenWillAttention();
        long time = System.currentTimeMillis();
        int alpha = (int)(200 + 55 * Math.sin(time / 200.0));
        int color = (alpha << 24) | 0xDD2222;

        String warning;
        if (heavenWill >= 90) {
            warning = "\u2620 \u5929\u610f\u964d\u7f5a \u2620";
        } else if (heavenWill >= 75) {
            warning = "\u26a0 \u5929\u610f\u538b\u5236";
        } else {
            warning = "\u26a0 \u5929\u610f\u5173\u6ce8";
        }

        int warnX = screenWidth - font.width(warning) - 5;
        int warnY = 5;
        graphics.fill(warnX - 2, warnY - 1, screenWidth - 3, warnY + 10, 0x88000000);
        graphics.drawString(font, warning, warnX, warnY, color);
    }

    private static void renderTargetInfo(GuiGraphics graphics, Font font, int screenWidth, int screenHeight, String info) {
        int infoWidth = font.width(info) + 8;
        int infoX = screenWidth - infoWidth - 5;
        int infoY = screenHeight / 2 - 5;

        graphics.fill(infoX - 2, infoY - 2, infoX + infoWidth, infoY + 12, 0xAA000000);
        graphics.drawString(font, info, infoX + 2, infoY + 1, 0xEEEEEE);
    }
}
