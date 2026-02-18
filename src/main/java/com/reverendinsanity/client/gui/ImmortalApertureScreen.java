package com.reverendinsanity.client.gui;

import com.reverendinsanity.client.ClientDataCache;
import com.reverendinsanity.network.EnterAperturePayload;
import com.reverendinsanity.network.ExitAperturePayload;
import com.reverendinsanity.network.ExtractResourcePayload;
import com.reverendinsanity.network.RepairAperturePayload;
import com.reverendinsanity.network.RepairBreachPayload;
import com.reverendinsanity.network.ResistCalamityPayload;
import com.reverendinsanity.network.SyncImmortalAperturePayload;
import com.reverendinsanity.world.dimension.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.List;

// 仙窍管理界面：查看福地状态、资源、道痕、灾劫，执行修复/提取
public class ImmortalApertureScreen extends Screen {

    private static final int PANEL_W = 400;
    private static final int PANEL_H = 300;

    private static final int BG_TOP = 0xCC0A0A1E;
    private static final int BG_BOTTOM = 0xCC1A1A3E;
    private static final int BORDER_OUTER = 0xFF3A3A5A;
    private static final int BORDER_GOLD = 0xFF664400;
    private static final int TITLE_COLOR = 0xFFFFD700;
    private static final int HEADER_COLOR = 0xFFCC9900;
    private static final int TEXT_COLOR = 0xFFCCCCCC;
    private static final int DIM_TEXT = 0xFF888888;
    private static final int BAR_BG = 0xFF222244;
    private static final int BAR_INTEGRITY = 0xFF44CC44;
    private static final int BAR_INTEGRITY_LOW = 0xFFCC4444;
    private static final int BAR_HEAVEN = 0xFF6688FF;
    private static final int BAR_EARTH = 0xFF886622;
    private static final int CALAMITY_COLOR = 0xFFFF4444;
    private static final int BUTTON_BG = 0xFF333355;
    private static final int BUTTON_HOVER = 0xFF444477;
    private static final int BUTTON_TEXT = 0xFFFFDD88;

    private int resourceScrollOffset = 0;

    public ImmortalApertureScreen() {
        super(Component.literal("仙窍管理"));
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

        if (!ClientDataCache.isImmortalFormed()) {
            renderNotFormed(g, px, py);
        } else {
            renderTitle(g, px, py);
            renderOverview(g, px, py);
            renderResources(g, mouseX, mouseY, px, py);
            renderDaoMarks(g, px, py);
            renderCalamity(g, px, py);
            renderButtons(g, mouseX, mouseY, px, py);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderPanelBg(GuiGraphics g, int px, int py) {
        g.fill(px - 2, py - 2, px + PANEL_W + 2, py + PANEL_H + 2, BORDER_OUTER);
        g.fill(px - 1, py - 1, px + PANEL_W + 1, py + PANEL_H + 1, BORDER_GOLD);
        g.fill(px, py, px + PANEL_W, py + PANEL_H / 2, BG_TOP);
        g.fill(px, py + PANEL_H / 2, px + PANEL_W, py + PANEL_H, BG_BOTTOM);
    }

    private void renderNotFormed(GuiGraphics g, int px, int py) {
        String title = "仙 窍 未 开";
        int tw = this.font.width(title);
        g.drawString(this.font, title, px + (PANEL_W - tw) / 2, py + PANEL_H / 2 - 20, TITLE_COLOR);

        String hint = "需达到六转蛊仙境界方可开辟仙窍";
        int hw = this.font.width(hint);
        g.drawString(this.font, hint, px + (PANEL_W - hw) / 2, py + PANEL_H / 2 + 5, DIM_TEXT);
    }

    private void renderTitle(GuiGraphics g, int px, int py) {
        String title = "仙窍 · " + ClientDataCache.getImmortalGradeName();
        int tw = this.font.width(title);
        g.fill(px, py, px + PANEL_W, py + 20, 0xCC1A0A2E);
        g.drawString(this.font, title, px + (PANEL_W - tw) / 2, py + 6, TITLE_COLOR);
    }

    private void renderOverview(GuiGraphics g, int px, int py) {
        int sx = px + 10;
        int sy = py + 28;
        int halfW = PANEL_W / 2 - 15;

        g.drawString(this.font, "[ 状态概览 ]", sx, sy, HEADER_COLOR);
        sy += 14;

        float integrity = ClientDataCache.getImmortalIntegrity();
        g.drawString(this.font, "完整度", sx, sy, TEXT_COLOR);
        renderBar(g, sx + 50, sy, halfW - 55, integrity, 100, integrity > 30 ? BAR_INTEGRITY : BAR_INTEGRITY_LOW);
        g.drawString(this.font, String.format("%.1f%%", integrity), sx + halfW - 2, sy, integrity > 30 ? 0xFF88FF88 : 0xFFFF8888);
        sy += 14;

        float hqi = ClientDataCache.getImmortalHeavenQi();
        float maxQi = ClientDataCache.getImmortalMaxQi();
        g.drawString(this.font, "天气", sx, sy, TEXT_COLOR);
        renderBar(g, sx + 50, sy, halfW - 55, hqi, maxQi, BAR_HEAVEN);
        g.drawString(this.font, String.format("%.0f", hqi), sx + halfW - 2, sy, 0xFF88AAFF);
        sy += 14;

        float eqi = ClientDataCache.getImmortalEarthQi();
        g.drawString(this.font, "地气", sx, sy, TEXT_COLOR);
        renderBar(g, sx + 50, sy, halfW - 55, eqi, maxQi, BAR_EARTH);
        g.drawString(this.font, String.format("%.0f", eqi), sx + halfW - 2, sy, 0xFFCCAA66);
        sy += 14;

        g.drawString(this.font, "仙元石: " + ClientDataCache.getImmortalEssenceStones(), sx, sy, 0xFFAADDFF);
        sy += 14;

        int tfr = ClientDataCache.getImmortalTimeFlowRate();
        g.drawString(this.font, "光阴支流: 1:" + tfr, sx, sy, 0xFFDDCCAA);
        sy += 14;

        float devLevel = ClientDataCache.getImmortalDevelopmentLevel();
        g.drawString(this.font, "发展度", sx, sy, TEXT_COLOR);
        int devBarW = halfW - 55;
        renderBar(g, sx + 50, sy, devBarW, devLevel, 100, 0xFF44AACC);
        g.drawString(this.font, String.format("%.0f%%", devLevel), sx + halfW - 2, sy, 0xFF88DDFF);
        sy += 14;

        int breachCount = ClientDataCache.getImmortalBreachCount();
        g.drawString(this.font, "漏洞: ", sx, sy, TEXT_COLOR);
        if (breachCount > 0) {
            g.drawString(this.font, breachCount + "处", sx + 40, sy, 0xFFFF4444);
        } else {
            g.drawString(this.font, "无", sx + 40, sy, 0xFF88FF88);
        }
        sy += 14;

        int survived = ClientDataCache.getImmortalTotalCalamitiesSurvived();
        g.drawString(this.font, "存活灾劫: " + survived + "次", sx, sy, 0xFFCCCCDD);
    }

    private void renderBar(GuiGraphics g, int x, int y, int w, float value, float max, int color) {
        g.fill(x, y + 1, x + w, y + 9, BAR_BG);
        int filled = max > 0 ? (int)(w * Math.min(value / max, 1.0f)) : 0;
        if (filled > 0) {
            g.fill(x, y + 1, x + filled, y + 9, color);
        }
        g.fill(x, y + 1, x + w, y + 2, 0x44FFFFFF);
    }

    private void renderResources(GuiGraphics g, int mouseX, int mouseY, int px, int py) {
        int sx = px + PANEL_W / 2 + 5;
        int sy = py + 28;
        int halfW = PANEL_W / 2 - 15;

        g.drawString(this.font, "[ 资源储备 ]", sx, sy, HEADER_COLOR);
        sy += 14;

        List<SyncImmortalAperturePayload.ResourceEntry> resources = ClientDataCache.getImmortalResources();
        int maxVisible = 7;
        int end = Math.min(resources.size(), resourceScrollOffset + maxVisible);

        for (int i = resourceScrollOffset; i < end; i++) {
            SyncImmortalAperturePayload.ResourceEntry res = resources.get(i);
            boolean hovered = mouseX >= sx && mouseX <= sx + halfW && mouseY >= sy && mouseY < sy + 12;
            int textCol = hovered ? 0xFFFFFFFF : TEXT_COLOR;

            g.drawString(this.font, res.name(), sx, sy, textCol);
            String countStr = "x" + res.amount();
            int cw = this.font.width(countStr);
            g.drawString(this.font, countStr, sx + halfW - cw, sy, res.amount() > 0 ? 0xFF88FF88 : DIM_TEXT);

            if (hovered && res.amount() > 0) {
                int btnX = sx + halfW - cw - 30;
                g.fill(btnX, sy - 1, btnX + 26, sy + 10, BUTTON_HOVER);
                g.drawString(this.font, "提取", btnX + 2, sy, BUTTON_TEXT);
            }
            sy += 13;
        }

        if (resources.size() > maxVisible) {
            g.drawString(this.font, "... (" + resources.size() + "种)", sx, sy, DIM_TEXT);
        }
    }

    private void renderDaoMarks(GuiGraphics g, int px, int py) {
        int sx = px + 10;
        int sy = py + 130;

        g.drawString(this.font, "[ 仙窍道痕 ]", sx, sy, HEADER_COLOR);
        sy += 14;

        List<SyncImmortalAperturePayload.DaoMarkEntry> marks = ClientDataCache.getImmortalTopDaoMarks();
        if (marks.isEmpty()) {
            g.drawString(this.font, "暂无道痕", sx, sy, DIM_TEXT);
        } else {
            for (int i = 0; i < Math.min(marks.size(), 6); i++) {
                SyncImmortalAperturePayload.DaoMarkEntry mark = marks.get(i);
                int barColor = getMarkBarColor(i);
                g.drawString(this.font, mark.pathName(), sx, sy, TEXT_COLOR);
                renderBar(g, sx + 50, sy, 100, mark.marks(), 10000, barColor);
                g.drawString(this.font, String.valueOf(mark.marks()), sx + 155, sy, 0xFFAADDFF);
                sy += 13;
            }
        }
    }

    private int getMarkBarColor(int index) {
        return switch (index) {
            case 0 -> 0xFFFFD700;
            case 1 -> 0xFFC0C0C0;
            case 2 -> 0xFFCD7F32;
            case 3 -> 0xFF88CCFF;
            case 4 -> 0xFF88FF88;
            default -> 0xFFAAAAAA;
        };
    }

    private void renderCalamity(GuiGraphics g, int px, int py) {
        int sx = px + PANEL_W / 2 + 5;
        int sy = py + 130;

        g.drawString(this.font, "[ 灾劫 ]", sx, sy, HEADER_COLOR);
        sy += 14;

        if (ClientDataCache.isImmortalCalamityActive()) {
            g.drawString(this.font, "灾劫进行中!", sx, sy, CALAMITY_COLOR);
            sy += 13;
            g.drawString(this.font, ClientDataCache.getImmortalCalamityTypeName(), sx, sy, 0xFFFF8844);
            sy += 13;
            float progress = ClientDataCache.getImmortalCalamityProgress();
            g.drawString(this.font, "进度", sx, sy, TEXT_COLOR);
            renderBar(g, sx + 35, sy, 120, progress, 1.0f, 0xFFFF4444);
            g.drawString(this.font, String.format("%.0f%%", progress * 100), sx + 160, sy, CALAMITY_COLOR);
        } else {
            g.drawString(this.font, "当前平安", sx, sy, 0xFF88FF88);
            sy += 13;
            int days = ClientDataCache.getImmortalDaysSinceCalamity();
            g.drawString(this.font, "距上次灾劫: " + days + "天", sx, sy, DIM_TEXT);
        }
    }

    private void renderButtons(GuiGraphics g, int mouseX, int mouseY, int px, int py) {
        int by = py + PANEL_H - 30;

        renderButton(g, "修复仙窍", px + 10, by, 80, mouseX, mouseY);
        renderButton(g, "全部提取", px + 100, by, 80, mouseX, mouseY);
        if (ClientDataCache.isImmortalCalamityActive()) {
            renderButton(g, "抵抗灾劫", px + 190, by, 80, mouseX, mouseY);
        }
        if (ClientDataCache.getImmortalBreachCount() > 0) {
            renderButton(g, "修复漏洞", px + 280, by, 80, mouseX, mouseY);
        }

        boolean inAperture = Minecraft.getInstance().level != null &&
            Minecraft.getInstance().level.dimension().equals(ModDimensions.APERTURE_DIM);
        String apText = inAperture ? "离开仙窍" : "进入仙窍";
        int apColor = inAperture ? 0xFF445544 : 0xFF334466;
        int apBtnX = px + PANEL_W - 150;
        boolean apHover = mouseX >= apBtnX && mouseX <= apBtnX + 80 && mouseY >= by && mouseY <= by + 16;
        g.fill(apBtnX, by, apBtnX + 80, by + 16, apHover ? 0xFF5566AA : apColor);
        g.fill(apBtnX, by, apBtnX + 80, by + 1, 0xFF6688BB);
        g.fill(apBtnX, by + 15, apBtnX + 80, by + 16, 0xFF223344);
        int atw = this.font.width(apText);
        g.drawString(this.font, apText, apBtnX + (80 - atw) / 2, by + 4, apHover ? 0xFFFFFFFF : 0xFFAADDFF);

        renderButton(g, "关闭", px + PANEL_W - 60, by, 50, mouseX, mouseY);
    }

    private void renderButton(GuiGraphics g, String text, int x, int y, int w, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 16;
        g.fill(x, y, x + w, y + 16, hovered ? BUTTON_HOVER : BUTTON_BG);
        g.fill(x, y, x + w, y + 1, 0xFF555577);
        g.fill(x, y + 15, x + w, y + 16, 0xFF222244);
        int tw = this.font.width(text);
        g.drawString(this.font, text, x + (w - tw) / 2, y + 4, hovered ? 0xFFFFFFFF : BUTTON_TEXT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int px = (this.width - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;
        int by = py + PANEL_H - 30;

        if (isInButton(mouseX, mouseY, px + 10, by, 80)) {
            PacketDistributor.sendToServer(new RepairAperturePayload(5.0f));
            return true;
        }

        if (isInButton(mouseX, mouseY, px + 100, by, 80)) {
            List<SyncImmortalAperturePayload.ResourceEntry> resources = ClientDataCache.getImmortalResources();
            for (SyncImmortalAperturePayload.ResourceEntry res : resources) {
                if (res.amount() > 0) {
                    PacketDistributor.sendToServer(new ExtractResourcePayload(res.ordinal(), res.amount()));
                }
            }
            return true;
        }

        if (ClientDataCache.isImmortalCalamityActive() && isInButton(mouseX, mouseY, px + 190, by, 80)) {
            PacketDistributor.sendToServer(new ResistCalamityPayload(20.0f));
            return true;
        }

        if (ClientDataCache.getImmortalBreachCount() > 0 && isInButton(mouseX, mouseY, px + 280, by, 80)) {
            PacketDistributor.sendToServer(new RepairBreachPayload());
            return true;
        }

        int apBtnX = px + PANEL_W - 150;
        if (isInButton(mouseX, mouseY, apBtnX, by, 80)) {
            boolean inAperture = Minecraft.getInstance().level != null &&
                Minecraft.getInstance().level.dimension().equals(ModDimensions.APERTURE_DIM);
            if (inAperture) {
                PacketDistributor.sendToServer(new ExitAperturePayload());
            } else {
                PacketDistributor.sendToServer(new EnterAperturePayload());
            }
            this.onClose();
            return true;
        }

        if (isInButton(mouseX, mouseY, px + PANEL_W - 60, by, 50)) {
            this.onClose();
            return true;
        }

        int rsx = px + PANEL_W / 2 + 5;
        int rsy = py + 42;
        int halfW = PANEL_W / 2 - 15;
        List<SyncImmortalAperturePayload.ResourceEntry> resources = ClientDataCache.getImmortalResources();
        int maxVisible = 7;
        int end = Math.min(resources.size(), resourceScrollOffset + maxVisible);
        for (int i = resourceScrollOffset; i < end; i++) {
            SyncImmortalAperturePayload.ResourceEntry res = resources.get(i);
            int ry = rsy + (i - resourceScrollOffset) * 13;
            if (res.amount() > 0 && mouseX >= rsx && mouseX <= rsx + halfW && mouseY >= ry && mouseY < ry + 12) {
                PacketDistributor.sendToServer(new ExtractResourcePayload(res.ordinal(), 1));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<SyncImmortalAperturePayload.ResourceEntry> resources = ClientDataCache.getImmortalResources();
        if (scrollY > 0 && resourceScrollOffset > 0) {
            resourceScrollOffset--;
        } else if (scrollY < 0 && resourceScrollOffset < Math.max(0, resources.size() - 7)) {
            resourceScrollOffset++;
        }
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
