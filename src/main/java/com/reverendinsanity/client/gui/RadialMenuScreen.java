package com.reverendinsanity.client.gui;

import com.reverendinsanity.client.ClientDataCache;
import com.reverendinsanity.network.RadialMenuPayload;
import com.reverendinsanity.network.SyncApertureContentsPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

// 转轮式交互菜单：动态显示系统动作+蛊虫技能+杀招
public class RadialMenuScreen extends Screen {

    private static final int OUTER_RADIUS = 100;
    private static final int INNER_RADIUS = 35;
    private static final int BG_COLOR = 0xCC0A0A1E;
    private static final int BORDER_COLOR = 0xFF3A3A5A;
    private static final int HOVER_COLOR = 0x44FFD700;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int CENTER_COLOR = 0xFF1A1A3E;

    private static final int COLOR_SYSTEM = 0xFF88CCEE;
    private static final int COLOR_ABILITY = 0xFF66FF88;
    private static final int COLOR_MOVE = 0xFFFF8866;

    private final List<RadialEntry> entries = new ArrayList<>();
    private int hoveredIndex = -1;

    public RadialMenuScreen() {
        super(Component.literal("蛊师菜单"));
        buildEntries();
    }

    private void buildEntries() {
        entries.clear();

        entries.add(new RadialEntry("空窍", COLOR_SYSTEM, RadialMenuPayload.TYPE_SYSTEM, 0));
        if (ClientDataCache.getRankLevel() >= 6) {
            entries.add(new RadialEntry("仙窍", COLOR_SYSTEM, RadialMenuPayload.TYPE_SYSTEM, 1));
        }
        entries.add(new RadialEntry("图鉴", COLOR_SYSTEM, RadialMenuPayload.TYPE_SYSTEM, 2));
        entries.add(new RadialEntry("推演", COLOR_SYSTEM, RadialMenuPayload.TYPE_SYSTEM, 3));
        entries.add(new RadialEntry("闭关", COLOR_SYSTEM, RadialMenuPayload.TYPE_SYSTEM, 4));

        List<SyncApertureContentsPayload.GuInfo> guList = ClientDataCache.getGuList();
        for (int i = 0; i < guList.size(); i++) {
            SyncApertureContentsPayload.GuInfo gu = guList.get(i);
            entries.add(new RadialEntry(gu.displayName(), COLOR_ABILITY, RadialMenuPayload.TYPE_ABILITY, i));
        }

        List<SyncApertureContentsPayload.MoveInfo> moveList = ClientDataCache.getEquippedMoveList();
        for (int i = 0; i < moveList.size(); i++) {
            SyncApertureContentsPayload.MoveInfo move = moveList.get(i);
            entries.add(new RadialEntry("杀·" + move.displayName(), COLOR_MOVE, RadialMenuPayload.TYPE_MOVE, i));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x80000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int count = entries.size();
        if (count == 0) {
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        float dx = mouseX - cx;
        float dy = mouseY - cy;
        float dist = Mth.sqrt(dx * dx + dy * dy);

        hoveredIndex = -1;
        if (dist > INNER_RADIUS && dist < OUTER_RADIUS + 30) {
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
            if (angle < 0) angle += 360;
            float sectorSize = 360.0f / count;
            hoveredIndex = (int) (angle / sectorSize);
            if (hoveredIndex >= count) hoveredIndex = 0;
        }

        drawRadialBackground(graphics, cx, cy);
        drawSectors(graphics, cx, cy, count);
        drawCenterCircle(graphics, cx, cy);

        if (hoveredIndex >= 0 && hoveredIndex < count) {
            RadialEntry entry = entries.get(hoveredIndex);
            String name = entry.name;
            int tw = this.font.width(name);
            graphics.drawString(this.font, name, cx - tw / 2, cy + OUTER_RADIUS + 15, TEXT_COLOR, true);

            String typeLabel = switch (entry.actionType) {
                case RadialMenuPayload.TYPE_SYSTEM -> "[系统]";
                case RadialMenuPayload.TYPE_ABILITY -> "[技能]";
                case RadialMenuPayload.TYPE_MOVE -> "[杀招]";
                default -> "";
            };
            int tlw = this.font.width(typeLabel);
            int labelColor = entry.color;
            graphics.drawString(this.font, typeLabel, cx - tlw / 2, cy + OUTER_RADIUS + 28, labelColor, true);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawRadialBackground(GuiGraphics graphics, int cx, int cy) {
        for (int y = -OUTER_RADIUS; y <= OUTER_RADIUS; y++) {
            for (int x = -OUTER_RADIUS; x <= OUTER_RADIUS; x++) {
                float d = Mth.sqrt(x * x + y * y);
                if (d >= INNER_RADIUS && d <= OUTER_RADIUS) {
                    graphics.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, BG_COLOR);
                }
            }
        }
    }

    private void drawSectors(GuiGraphics graphics, int cx, int cy, int count) {
        float sectorSize = 360.0f / count;

        for (int i = 0; i < count; i++) {
            RadialEntry entry = entries.get(i);
            boolean hovered = (i == hoveredIndex);
            float startAngle = i * sectorSize;
            float midAngle = startAngle + sectorSize / 2;

            if (hovered) {
                double midRad = Math.toRadians(midAngle);
                int iconR = (INNER_RADIUS + OUTER_RADIUS) / 2;
                int hx = cx + (int) (iconR * Math.cos(midRad));
                int hy = cy + (int) (iconR * Math.sin(midRad));
                int hs = Math.max(12, (int) (sectorSize * 0.3));
                graphics.fill(hx - hs, hy - hs, hx + hs, hy + hs, HOVER_COLOR);
            }

            double midRad = Math.toRadians(midAngle);
            int labelR = (INNER_RADIUS + OUTER_RADIUS) / 2;
            int lx = cx + (int) (labelR * Math.cos(midRad));
            int ly = cy + (int) (labelR * Math.sin(midRad));

            String shortName = truncate(entry.name, 4);
            int tw = this.font.width(shortName);
            int color = hovered ? entry.color : 0xFFCCCCCC;
            graphics.drawString(this.font, shortName, lx - tw / 2, ly - 4, color, true);

            double lineRad = Math.toRadians(startAngle);
            int ix = cx + (int) (INNER_RADIUS * Math.cos(lineRad));
            int iy = cy + (int) (INNER_RADIUS * Math.sin(lineRad));
            int ox = cx + (int) (OUTER_RADIUS * Math.cos(lineRad));
            int oy = cy + (int) (OUTER_RADIUS * Math.sin(lineRad));
            drawLine(graphics, ix, iy, ox, oy, BORDER_COLOR);
        }
    }

    private void drawCenterCircle(GuiGraphics graphics, int cx, int cy) {
        for (int y = -INNER_RADIUS; y <= INNER_RADIUS; y++) {
            for (int x = -INNER_RADIUS; x <= INNER_RADIUS; x++) {
                if (x * x + y * y <= INNER_RADIUS * INNER_RADIUS) {
                    graphics.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, CENTER_COLOR);
                }
            }
        }
        String title = "蛊";
        int tw = this.font.width(title);
        graphics.drawString(this.font, title, cx - tw / 2, cy - 4, 0xFFFFD700, true);
    }

    private void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        for (int i = 0; i < dx + dy + 1; i++) {
            graphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }

    private String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredIndex >= 0 && hoveredIndex < entries.size()) {
            RadialEntry entry = entries.get(hoveredIndex);
            PacketDistributor.sendToServer(new RadialMenuPayload(entry.actionType, entry.actionIndex));
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (com.reverendinsanity.client.ModKeybindings.RADIAL_MENU.matches(keyCode, scanCode)) {
            if (hoveredIndex >= 0 && hoveredIndex < entries.size()) {
                RadialEntry entry = entries.get(hoveredIndex);
                PacketDistributor.sendToServer(new RadialMenuPayload(entry.actionType, entry.actionIndex));
            }
            this.onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    record RadialEntry(String name, int color, int actionType, int actionIndex) {}
}
