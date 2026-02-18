package com.reverendinsanity.client.gui;

import com.reverendinsanity.client.ClientDataCache;
import com.reverendinsanity.core.combat.custom.PathReactionRegistry;
import com.reverendinsanity.core.combat.custom.PathStackingRule;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.network.CancelDeductionPayload;
import com.reverendinsanity.network.StartDeductionPayload;
import com.reverendinsanity.network.SyncDeductionScreenPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// 杀招推演界面：选择蛊虫组合推演自创杀招
public class DeductionScreen extends Screen {

    private static final int PANEL_W = 420;
    private static final int PANEL_H = 300;

    private static final int BG = 0xCC0A0A1E;
    private static final int BG2 = 0xCC1A1A3E;
    private static final int BORDER = 0xFF3A3A5A;
    private static final int GOLD = 0xFF664400;
    private static final int TITLE_COL = 0xFFFFD700;
    private static final int HEADER_COL = 0xFFCC9900;
    private static final int TEXT_COL = 0xFFCCCCCC;
    private static final int DIM_COL = 0xFF888888;
    private static final int SELECTED_COL = 0xFF44CC44;
    private static final int CORE_COL = 0xFFFF8844;
    private static final int BTN_BG = 0xFF333355;
    private static final int BTN_HOVER = 0xFF444477;
    private static final int BTN_TEXT = 0xFFFFDD88;
    private static final int BAR_BG = 0xFF222244;
    private static final int BAR_FILL = 0xFF6688FF;

    private static final String[] DAO_PATHS = {
        "STRENGTH", "BLOOD", "POISON", "ICE", "FIRE", "EARTH", "WIND", "LIGHTNING",
        "WATER", "SOUL", "LIGHT", "DARK", "MOON", "METAL", "WOOD", "DREAM",
        "ILLUSION", "SWORD", "BLADE", "STAR", "LUCK", "KILL", "TRANSFORMATION",
        "ARMY", "SOUND", "BONE", "FLIGHT", "QI", "YIN_YANG", "SPACE", "TIME",
        "CHARM", "WISDOM", "VOID", "RESTRICTION", "HEAVEN", "RULE", "SHADOW",
        "CLOUD", "FORMATION", "REFINE", "PILL", "PAINT", "STEAL", "INFO",
        "HUMAN", "ENSLAVE", "FOOD"
    };

    private static final String[] DAO_PATH_NAMES = {
        "力道", "血道", "毒道", "冰道", "炎道", "土道", "风道", "雷道",
        "水道", "魂道", "光道", "暗道", "月道", "金道", "木道", "梦道",
        "幻道", "剑道", "刀道", "星道", "运道", "杀道", "变化道",
        "兵道", "音道", "骨道", "飞行道", "气道", "阴阳道", "宇道", "宙道",
        "魅道", "智道", "虚道", "禁道", "天道", "律道", "影道",
        "云道", "阵道", "炼道", "丹道", "画道", "偷道", "信道",
        "人道", "奴道", "食道"
    };

    private int selectedCoreIndex = -1;
    private final List<Integer> selectedSupportIndices = new ArrayList<>();
    private int selectedPathIndex = 0;
    private int guScrollOffset = 0;
    private int pathScrollOffset = 0;
    private boolean showPathSelector = false;

    public DeductionScreen() {
        super(Component.literal("杀招推演"));
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xCC000000);

        int px = (this.width - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;

        renderPanelBg(g, px, py);
        renderTitle(g, px, py);

        if (showPathSelector) {
            renderPathSelector(g, mouseX, mouseY, px, py);
        } else {
            renderGuList(g, mouseX, mouseY, px, py);
            renderRecipe(g, mouseX, mouseY, px, py);
            renderDeductionStatus(g, px, py);
            renderDeductionResult(g, px, py);
            renderButtons(g, mouseX, mouseY, px, py);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderPanelBg(GuiGraphics g, int px, int py) {
        g.fill(px - 2, py - 2, px + PANEL_W + 2, py + PANEL_H + 2, BORDER);
        g.fill(px - 1, py - 1, px + PANEL_W + 1, py + PANEL_H + 1, GOLD);
        g.fill(px, py, px + PANEL_W, py + PANEL_H / 2, BG);
        g.fill(px, py + PANEL_H / 2, px + PANEL_W, py + PANEL_H, BG2);
    }

    private void renderTitle(GuiGraphics g, int px, int py) {
        String title = "杀 招 推 演";
        int tw = this.font.width(title);
        g.fill(px, py, px + PANEL_W, py + 20, 0xCC1A0A2E);
        g.drawString(this.font, title, px + (PANEL_W - tw) / 2, py + 6, TITLE_COL);
    }

    private void renderGuList(GuiGraphics g, int mouseX, int mouseY, int px, int py) {
        int sx = px + 10;
        int sy = py + 28;
        int listW = 160;
        int listH = 180;

        g.drawString(this.font, "[ 空窍蛊虫 ]", sx, sy, HEADER_COL);
        sy += 14;

        List<SyncDeductionScreenPayload.DeductionGuEntry> guList = ClientDataCache.getDeductionGuList();
        g.fill(sx, sy, sx + listW, sy + listH, 0x44000000);

        int maxVisible = listH / 14;
        int end = Math.min(guList.size(), guScrollOffset + maxVisible);

        for (int i = guScrollOffset; i < end; i++) {
            SyncDeductionScreenPayload.DeductionGuEntry gu = guList.get(i);
            int ey = sy + (i - guScrollOffset) * 14;
            boolean hovered = mouseX >= sx && mouseX <= sx + listW && mouseY >= ey && mouseY < ey + 13;

            int bgCol = 0x00000000;
            if (i == selectedCoreIndex) bgCol = 0x44FF8844;
            else if (selectedSupportIndices.contains(i)) bgCol = 0x4444CC44;
            else if (hovered) bgCol = 0x22FFFFFF;

            if (bgCol != 0) g.fill(sx, ey, sx + listW, ey + 13, bgCol);

            String marker = "";
            int textColor = TEXT_COL;
            if (i == selectedCoreIndex) {
                marker = "[核] ";
                textColor = CORE_COL;
            } else if (selectedSupportIndices.contains(i)) {
                marker = "[辅] ";
                textColor = SELECTED_COL;
            }

            String display = marker + gu.displayName();
            if (this.font.width(display) > listW - 40) {
                display = display.substring(0, Math.min(display.length(), 8)) + "..";
            }
            g.drawString(this.font, display, sx + 2, ey + 2, textColor);

            String rankPath = gu.rank() + "转 " + gu.pathName();
            int rpw = this.font.width(rankPath);
            g.drawString(this.font, rankPath, sx + listW - rpw - 2, ey + 2, DIM_COL);
        }

        if (guList.size() > maxVisible) {
            int scrollBarH = Math.max(10, listH * maxVisible / guList.size());
            int scrollBarY = sy + (listH - scrollBarH) * guScrollOffset / Math.max(1, guList.size() - maxVisible);
            g.fill(sx + listW - 3, scrollBarY, sx + listW, scrollBarY + scrollBarH, 0x88FFFFFF);
        }

        if (guList.isEmpty()) {
            g.drawString(this.font, "空窍中无蛊虫", sx + 10, sy + listH / 2 - 4, DIM_COL);
        }

        sy += listH + 4;
        g.drawString(this.font, "左键=核心  右键=辅助", sx, sy, DIM_COL);
    }

    private void renderRecipe(GuiGraphics g, int mouseX, int mouseY, int px, int py) {
        int sx = px + 185;
        int sy = py + 28;

        g.drawString(this.font, "[ 推演配方 ]", sx, sy, HEADER_COL);
        sy += 16;

        List<SyncDeductionScreenPayload.DeductionGuEntry> guList = ClientDataCache.getDeductionGuList();

        g.drawString(this.font, "核心蛊:", sx, sy, TEXT_COL);
        if (selectedCoreIndex >= 0 && selectedCoreIndex < guList.size()) {
            String name = guList.get(selectedCoreIndex).displayName();
            g.drawString(this.font, name, sx + 55, sy, CORE_COL);
        } else {
            g.drawString(this.font, "(未选择)", sx + 55, sy, DIM_COL);
        }
        sy += 15;

        for (int slot = 0; slot < 4; slot++) {
            g.drawString(this.font, "辅助" + (slot + 1) + ":", sx, sy, TEXT_COL);
            if (slot < selectedSupportIndices.size()) {
                int idx = selectedSupportIndices.get(slot);
                if (idx < guList.size()) {
                    String name = guList.get(idx).displayName();
                    g.drawString(this.font, name, sx + 55, sy, SELECTED_COL);
                }
            } else {
                g.drawString(this.font, "(空)", sx + 55, sy, DIM_COL);
            }
            sy += 13;
        }

        sy += 8;
        g.drawString(this.font, "目标道:", sx, sy, TEXT_COL);
        boolean pathHover = mouseX >= sx + 55 && mouseX <= sx + 160 && mouseY >= sy - 1 && mouseY <= sy + 11;
        g.fill(sx + 55, sy - 1, sx + 160, sy + 11, pathHover ? BTN_HOVER : BTN_BG);
        g.drawString(this.font, DAO_PATH_NAMES[selectedPathIndex] + " ▼", sx + 58, sy + 1, pathHover ? 0xFFFFFFFF : BTN_TEXT);

        sy += 18;
        g.fill(sx, sy, sx + PANEL_W - 195, sy + 1, 0xFF444466);
        sy += 6;

        if (selectedCoreIndex >= 0) {
            int guCount = 1 + selectedSupportIndices.size();
            boolean crossPath = hasCrossPath();
            int totalTicks = 100 + guCount * 60 + (crossPath ? 100 : 0);
            float seconds = totalTicks / 20f;
            float essenceCost = 500 + guCount * 200;
            float thoughtsCost = 50 + guCount * 20;

            g.drawString(this.font, "蛊虫数: " + guCount, sx, sy, TEXT_COL);
            sy += 13;
            g.drawString(this.font, "预估时间: " + String.format("%.0f", seconds) + "秒", sx, sy, TEXT_COL);
            sy += 13;
            g.drawString(this.font, "消耗真元: ~" + (int) essenceCost, sx, sy, 0xFF88FF88);
            sy += 13;
            g.drawString(this.font, "消耗念头: ~" + (int) thoughtsCost, sx, sy, 0xFF88AAFF);

            if (crossPath) {
                sy += 13;
                g.drawString(this.font, "跨道推演 (难度+)", sx, sy, 0xFFFF8844);
            }

            List<DaoPath> allPaths = collectSelectedPaths(guList);
            if (!allPaths.isEmpty()) {
                List<PathReactionRegistry.ReactionEffect> reactions = PathReactionRegistry.findReactions(allPaths);
                Map<DaoPath, Integer> counts = new EnumMap<>(DaoPath.class);
                for (DaoPath p : allPaths) counts.merge(p, 1, Integer::sum);
                List<PathStackingRule.StackThreshold> stacks = PathStackingRule.check(counts);

                if (!reactions.isEmpty() || !stacks.isEmpty()) {
                    sy += 15;
                    g.fill(sx, sy, sx + PANEL_W - 195, sy + 1, 0xFF664400);
                    sy += 4;
                    g.drawString(this.font, "[ 道共鸣 ]", sx, sy, TITLE_COL);
                    sy += 12;

                    for (int ri = 0; ri < Math.min(reactions.size(), 2); ri++) {
                        PathReactionRegistry.ReactionEffect r = reactions.get(ri);
                        g.fill(sx, sy, sx + 3, sy + 8, r.vfxColor());
                        String rn = r.name();
                        if (this.font.width(rn) > PANEL_W - 210) rn = rn.substring(0, 6) + "..";
                        g.drawString(this.font, " " + rn, sx + 5, sy, 0xFFFFDD44);
                        sy += 11;
                    }
                    for (int si = 0; si < Math.min(stacks.size(), 2); si++) {
                        PathStackingRule.StackThreshold s = stacks.get(si);
                        g.fill(sx, sy, sx + 3, sy + 8, 0xFF88CCFF);
                        String sn = s.description();
                        if (this.font.width(sn) > PANEL_W - 210) sn = sn.substring(0, 6) + "..";
                        g.drawString(this.font, " " + sn, sx + 5, sy, 0xFF88CCFF);
                        sy += 11;
                    }
                }
            }
        } else {
            g.drawString(this.font, "请选择核心蛊虫", sx, sy, DIM_COL);
        }
    }

    private void renderDeductionStatus(GuiGraphics g, int px, int py) {
        int sx = px + 10;
        int sy = py + PANEL_H - 52;

        if (ClientDataCache.isDeductionActive()) {
            float progress = ClientDataCache.getDeductionProgress();
            float rate = ClientDataCache.getDeductionSuccessRate();

            g.drawString(this.font, "推演进行中...", sx, sy, TITLE_COL);
            g.drawString(this.font, String.format("成功率: %.0f%%", rate * 100), sx + 100, sy, 0xFF88FF88);
            sy += 14;

            int barW = PANEL_W - 20;
            g.fill(sx, sy, sx + barW, sy + 10, BAR_BG);
            int filled = (int)(barW * progress);
            if (filled > 0) {
                g.fill(sx, sy, sx + filled, sy + 10, BAR_FILL);
            }
            g.fill(sx, sy, sx + barW, sy + 1, 0x44FFFFFF);
            String pctText = String.format("%.0f%%", progress * 100);
            int ptw = this.font.width(pctText);
            g.drawString(this.font, pctText, sx + (barW - ptw) / 2, sy + 1, 0xFFFFFFFF);
        }
    }

    private void renderDeductionResult(GuiGraphics g, int px, int py) {
        int outcome = ClientDataCache.getLastDeductionOutcome();
        if (outcome < 0 || ClientDataCache.isDeductionActive()) return;

        int sx = px + 185;
        int sy = py + PANEL_H - 70;

        int borderCol = switch (outcome) {
            case 0 -> 0xFFFFD700;
            case 1 -> 0xFF44CC44;
            case 2 -> 0xFFCCCC44;
            case 3 -> 0xFFCC4444;
            case 4 -> 0xFFCC66FF;
            default -> BORDER;
        };
        int textCol = switch (outcome) {
            case 0 -> 0xFFFFD700;
            case 1 -> 0xFF88FF88;
            case 2 -> 0xFFFFFF88;
            case 3 -> 0xFFFF8888;
            case 4 -> 0xFFDD88FF;
            default -> TEXT_COL;
        };

        int boxW = PANEL_W - 195;
        g.fill(sx - 1, sy - 1, sx + boxW + 1, sy + 41, borderCol);
        g.fill(sx, sy, sx + boxW, sy + 40, 0xCC0A0A1E);

        String label = switch (outcome) {
            case 0 -> "大成功!";
            case 1 -> "成功";
            case 2 -> "部分成功";
            case 3 -> "失败";
            case 4 -> "新发现!";
            default -> "完成";
        };
        g.drawString(this.font, "[ " + label + " ]", sx + 4, sy + 3, textCol);

        String moveName = ClientDataCache.getLastDeductionMoveName();
        if (!moveName.isEmpty()) {
            String display = "杀招: " + moveName;
            if (this.font.width(display) > boxW - 8) {
                display = display.substring(0, Math.min(display.length(), 14)) + "..";
            }
            g.drawString(this.font, display, sx + 4, sy + 16, TITLE_COL);
        }

        String msg = ClientDataCache.getLastDeductionMessage();
        if (!msg.isEmpty()) {
            if (this.font.width(msg) > boxW - 8) {
                msg = msg.substring(0, Math.min(msg.length(), 18)) + "..";
            }
            g.drawString(this.font, msg, sx + 4, sy + 28, DIM_COL);
        }
    }

    private void renderButtons(GuiGraphics g, int mouseX, int mouseY, int px, int py) {
        int by = py + PANEL_H - 25;

        if (ClientDataCache.isDeductionActive()) {
            renderButton(g, "取消推演", px + 10, by, 80, mouseX, mouseY);
        } else {
            boolean canStart = selectedCoreIndex >= 0;
            renderButton(g, "开始推演", px + 10, by, 80, mouseX, mouseY, canStart);
        }
        renderButton(g, "清空选择", px + 100, by, 80, mouseX, mouseY);
        renderButton(g, "关闭", px + PANEL_W - 60, by, 50, mouseX, mouseY);
    }

    private void renderButton(GuiGraphics g, String text, int x, int y, int w, int mouseX, int mouseY) {
        renderButton(g, text, x, y, w, mouseX, mouseY, true);
    }

    private void renderButton(GuiGraphics g, String text, int x, int y, int w, int mouseX, int mouseY, boolean enabled) {
        boolean hovered = enabled && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 16;
        int bg = enabled ? (hovered ? BTN_HOVER : BTN_BG) : 0xFF222233;
        g.fill(x, y, x + w, y + 16, bg);
        g.fill(x, y, x + w, y + 1, enabled ? 0xFF555577 : 0xFF333344);
        g.fill(x, y + 15, x + w, y + 16, 0xFF222244);
        int tw = this.font.width(text);
        int textCol = enabled ? (hovered ? 0xFFFFFFFF : BTN_TEXT) : DIM_COL;
        g.drawString(this.font, text, x + (w - tw) / 2, y + 4, textCol);
    }

    private void renderPathSelector(GuiGraphics g, int mouseX, int mouseY, int px, int py) {
        int sx = px + 60;
        int sy = py + 30;
        int selectorW = 300;
        int selectorH = 240;

        g.fill(sx - 1, sy - 1, sx + selectorW + 1, sy + selectorH + 1, GOLD);
        g.fill(sx, sy, sx + selectorW, sy + selectorH, 0xEE0A0A1E);

        g.drawString(this.font, "选择目标道路", sx + 10, sy + 5, TITLE_COL);
        sy += 20;

        int cols = 4;
        int cellW = selectorW / cols;
        int cellH = 16;
        int maxRows = (selectorH - 30) / cellH;
        int totalRows = (DAO_PATH_NAMES.length + cols - 1) / cols;
        int visibleRows = Math.min(totalRows - pathScrollOffset, maxRows);

        for (int row = 0; row < visibleRows; row++) {
            for (int col = 0; col < cols; col++) {
                int idx = (row + pathScrollOffset) * cols + col;
                if (idx >= DAO_PATH_NAMES.length) break;

                int cx = sx + col * cellW;
                int cy = sy + row * cellH;
                boolean hovered = mouseX >= cx && mouseX < cx + cellW && mouseY >= cy && mouseY < cy + cellH;
                boolean selected = idx == selectedPathIndex;

                if (selected) g.fill(cx, cy, cx + cellW - 1, cy + cellH - 1, 0x44FFD700);
                else if (hovered) g.fill(cx, cy, cx + cellW - 1, cy + cellH - 1, 0x22FFFFFF);

                int col2 = selected ? TITLE_COL : (hovered ? 0xFFFFFFFF : TEXT_COL);
                g.drawString(this.font, DAO_PATH_NAMES[idx], cx + 4, cy + 3, col2);
            }
        }

        int closeY = sy + visibleRows * cellH + 5;
        renderButton(g, "确定", sx + selectorW / 2 - 30, closeY, 60, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int px = (this.width - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;

        if (showPathSelector) {
            return handlePathSelectorClick(mouseX, mouseY, px, py);
        }

        int by = py + PANEL_H - 25;

        if (ClientDataCache.isDeductionActive()) {
            if (isInButton(mouseX, mouseY, px + 10, by, 80)) {
                PacketDistributor.sendToServer(new CancelDeductionPayload());
                return true;
            }
        } else {
            if (selectedCoreIndex >= 0 && isInButton(mouseX, mouseY, px + 10, by, 80)) {
                startDeduction();
                return true;
            }
        }

        if (isInButton(mouseX, mouseY, px + 100, by, 80)) {
            clearSelection();
            return true;
        }

        if (isInButton(mouseX, mouseY, px + PANEL_W - 60, by, 50)) {
            this.onClose();
            return true;
        }

        int pathBtnX = px + 185 + 55;
        int pathBtnY = py + 28 + 16 + 15 + 13 * 4 + 8;
        if (mouseX >= pathBtnX && mouseX <= pathBtnX + 105 && mouseY >= pathBtnY - 1 && mouseY <= pathBtnY + 11) {
            showPathSelector = true;
            return true;
        }

        return handleGuListClick(mouseX, mouseY, button, px, py);
    }

    private boolean handleGuListClick(double mouseX, double mouseY, int button, int px, int py) {
        int sx = px + 10;
        int sy = py + 42;
        int listW = 160;
        int listH = 180;

        if (mouseX < sx || mouseX > sx + listW || mouseY < sy || mouseY > sy + listH) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        List<SyncDeductionScreenPayload.DeductionGuEntry> guList = ClientDataCache.getDeductionGuList();
        int maxVisible = listH / 14;
        int clickedIdx = guScrollOffset + (int)((mouseY - sy) / 14);

        if (clickedIdx < 0 || clickedIdx >= guList.size()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (button == 0) {
            if (clickedIdx == selectedCoreIndex) {
                selectedCoreIndex = -1;
            } else {
                selectedSupportIndices.remove(Integer.valueOf(clickedIdx));
                selectedCoreIndex = clickedIdx;
            }
        } else if (button == 1) {
            if (clickedIdx == selectedCoreIndex) return true;
            if (selectedSupportIndices.contains(clickedIdx)) {
                selectedSupportIndices.remove(Integer.valueOf(clickedIdx));
            } else if (selectedSupportIndices.size() < 4) {
                selectedSupportIndices.add(clickedIdx);
            }
        }

        return true;
    }

    private boolean handlePathSelectorClick(double mouseX, double mouseY, int px, int py) {
        int sx = px + 60;
        int sy = py + 50;
        int selectorW = 300;
        int cols = 4;
        int cellW = selectorW / cols;
        int cellH = 16;
        int maxRows = 210 / cellH;

        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < cols; col++) {
                int idx = (row + pathScrollOffset) * cols + col;
                if (idx >= DAO_PATH_NAMES.length) break;

                int cx = sx + col * cellW;
                int cy = sy + row * cellH;
                if (mouseX >= cx && mouseX < cx + cellW && mouseY >= cy && mouseY < cy + cellH) {
                    selectedPathIndex = idx;
                    return true;
                }
            }
        }

        int visibleRows = Math.min((DAO_PATH_NAMES.length + cols - 1) / cols - pathScrollOffset, maxRows);
        int closeY = sy + visibleRows * cellH + 5;
        if (isInButton(mouseX, mouseY, sx + selectorW / 2 - 30, closeY, 60)) {
            showPathSelector = false;
            return true;
        }

        return true;
    }

    private void startDeduction() {
        List<SyncDeductionScreenPayload.DeductionGuEntry> guList = ClientDataCache.getDeductionGuList();
        if (selectedCoreIndex < 0 || selectedCoreIndex >= guList.size()) return;

        String coreId = guList.get(selectedCoreIndex).typeId();
        List<String> supportIds = new ArrayList<>();
        for (int idx : selectedSupportIndices) {
            if (idx >= 0 && idx < guList.size()) {
                supportIds.add(guList.get(idx).typeId());
            }
        }

        PacketDistributor.sendToServer(new StartDeductionPayload(
            coreId, supportIds, DAO_PATHS[selectedPathIndex]
        ));
    }

    private void clearSelection() {
        selectedCoreIndex = -1;
        selectedSupportIndices.clear();
    }

    private boolean hasCrossPath() {
        List<SyncDeductionScreenPayload.DeductionGuEntry> guList = ClientDataCache.getDeductionGuList();
        String targetPathName = DAO_PATH_NAMES[selectedPathIndex];
        if (selectedCoreIndex >= 0 && selectedCoreIndex < guList.size()) {
            if (!guList.get(selectedCoreIndex).pathName().equals(targetPathName)) return true;
        }
        for (int idx : selectedSupportIndices) {
            if (idx >= 0 && idx < guList.size()) {
                if (!guList.get(idx).pathName().equals(targetPathName)) return true;
            }
        }
        return false;
    }

    private List<DaoPath> collectSelectedPaths(List<SyncDeductionScreenPayload.DeductionGuEntry> guList) {
        List<DaoPath> paths = new ArrayList<>();
        if (selectedCoreIndex >= 0 && selectedCoreIndex < guList.size()) {
            DaoPath p = pathFromTypeId(guList.get(selectedCoreIndex).typeId());
            if (p != null) paths.add(p);
        }
        for (int idx : selectedSupportIndices) {
            if (idx >= 0 && idx < guList.size()) {
                DaoPath p = pathFromTypeId(guList.get(idx).typeId());
                if (p != null) paths.add(p);
            }
        }
        return paths;
    }

    private DaoPath pathFromTypeId(String typeId) {
        try {
            GuType type = GuRegistry.get(ResourceLocation.parse(typeId));
            return type != null ? type.path() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (showPathSelector) {
            int totalRows = (DAO_PATH_NAMES.length + 3) / 4;
            int maxRows = 210 / 16;
            if (scrollY > 0 && pathScrollOffset > 0) pathScrollOffset--;
            else if (scrollY < 0 && pathScrollOffset < Math.max(0, totalRows - maxRows)) pathScrollOffset++;
            return true;
        }

        List<SyncDeductionScreenPayload.DeductionGuEntry> guList = ClientDataCache.getDeductionGuList();
        int maxVisible = 180 / 14;
        if (scrollY > 0 && guScrollOffset > 0) guScrollOffset--;
        else if (scrollY < 0 && guScrollOffset < Math.max(0, guList.size() - maxVisible)) guScrollOffset++;
        return true;
    }

    private boolean isInButton(double mx, double my, int x, int y, int w) {
        return mx >= x && mx <= x + w && my >= y && my <= y + 16;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
