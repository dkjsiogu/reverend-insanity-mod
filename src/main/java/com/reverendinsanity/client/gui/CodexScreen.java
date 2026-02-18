package com.reverendinsanity.client.gui;

import com.reverendinsanity.client.ClientDataCache;
import com.reverendinsanity.network.SyncCodexPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 蛊虫图鉴界面：按大道分类展示已发现和未发现的蛊虫
public class CodexScreen extends Screen {

    private static final int BG_COLOR = 0xCC000000;
    private static final int PANEL_COLOR = 0xAA1A1A2E;
    private static final int BORDER_COLOR = 0xFF4A4A6A;
    private static final int TITLE_COLOR = 0xFFFFD700;
    private static final int TEXT_COLOR = 0xFFCCCCCC;
    private static final int UNDISCOVERED_COLOR = 0xFF444444;
    private static final int PATH_HEADER_COLOR = 0xFF8888CC;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private List<RenderLine> renderLines = new ArrayList<>();

    public CodexScreen() {
        super(Component.literal("蛊虫图鉴"));
    }

    @Override
    protected void init() {
        buildRenderLines();
    }

    private void buildRenderLines() {
        renderLines.clear();
        List<SyncCodexPayload.CodexEntry> entries = ClientDataCache.getCodexEntries();

        Map<String, List<SyncCodexPayload.CodexEntry>> byPath = new LinkedHashMap<>();
        for (SyncCodexPayload.CodexEntry entry : entries) {
            byPath.computeIfAbsent(entry.pathName(), k -> new ArrayList<>()).add(entry);
        }

        for (Map.Entry<String, List<SyncCodexPayload.CodexEntry>> group : byPath.entrySet()) {
            renderLines.add(new RenderLine(RenderLine.Type.PATH_HEADER, group.getKey(), null));
            for (SyncCodexPayload.CodexEntry entry : group.getValue()) {
                renderLines.add(new RenderLine(RenderLine.Type.GU_ENTRY, null, entry));
            }
            renderLines.add(new RenderLine(RenderLine.Type.SPACER, null, null));
        }

        int lineHeight = 14;
        int panelH = Math.min(this.height - 60, 300);
        int contentH = renderLines.size() * lineHeight;
        int viewH = panelH - 40;
        maxScroll = Math.max(0, contentH - viewH);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, BG_COLOR);

        int panelW = 320;
        int panelH = Math.min(this.height - 60, 300);
        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;

        graphics.fill(px, py, px + panelW, py + panelH, PANEL_COLOR);
        graphics.fill(px, py, px + panelW, py + 1, BORDER_COLOR);
        graphics.fill(px, py + panelH - 1, px + panelW, py + panelH, BORDER_COLOR);
        graphics.fill(px, py, px + 1, py + panelH, BORDER_COLOR);
        graphics.fill(px + panelW - 1, py, px + panelW, py + panelH, BORDER_COLOR);

        List<SyncCodexPayload.CodexEntry> entries = ClientDataCache.getCodexEntries();
        int total = entries.size();
        int discovered = ClientDataCache.getCodexDiscoveredCount();

        graphics.drawCenteredString(this.font, "— 蛊虫图鉴 —", px + panelW / 2, py + 5, TITLE_COLOR);
        graphics.drawCenteredString(this.font, "已发现: " + discovered + " / " + total, px + panelW / 2, py + 17, TEXT_COLOR);

        int contentX = px + 8;
        int contentY = py + 32;
        int viewH = panelH - 40;

        graphics.enableScissor(px + 1, contentY, px + panelW - 1, contentY + viewH);

        int lineHeight = 14;
        int y = contentY - scrollOffset;
        for (RenderLine line : renderLines) {
            if (y + lineHeight > contentY - lineHeight && y < contentY + viewH + lineHeight) {
                switch (line.type) {
                    case PATH_HEADER -> {
                        graphics.fill(contentX, y + 1, contentX + panelW - 16, y + 2, BORDER_COLOR);
                        graphics.drawString(this.font, "[ " + line.pathName + " ]", contentX + 2, y + 3, PATH_HEADER_COLOR);
                    }
                    case GU_ENTRY -> {
                        SyncCodexPayload.CodexEntry entry = line.entry;
                        if (entry.discovered()) {
                            int rankColor = getRankColor(entry.rank());
                            String label = entry.displayName() + "  " + entry.rank() + "转 " + entry.categoryName();
                            graphics.drawString(this.font, label, contentX + 6, y + 2, rankColor);
                        } else {
                            graphics.drawString(this.font, "???", contentX + 6, y + 2, UNDISCOVERED_COLOR);
                        }
                    }
                    case SPACER -> {}
                }
            }
            y += lineHeight;
        }

        graphics.disableScissor();

        if (maxScroll > 0) {
            int scrollBarX = px + panelW - 6;
            int scrollBarH = viewH;
            float scrollRatio = (float) scrollOffset / maxScroll;
            int thumbH = Math.max(10, (int)((float)viewH / (renderLines.size() * lineHeight) * scrollBarH));
            int thumbY = contentY + (int)(scrollRatio * (scrollBarH - thumbH));
            graphics.fill(scrollBarX, contentY, scrollBarX + 4, contentY + scrollBarH, 0xFF222222);
            graphics.fill(scrollBarX, thumbY, scrollBarX + 4, thumbY + thumbH, 0xFF666688);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int)(scrollY * 14)));
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static int getRankColor(int rank) {
        return switch (rank) {
            case 1 -> 0xFFCCCCCC;
            case 2 -> 0xFF55FF55;
            case 3 -> 0xFF55FFFF;
            case 4 -> 0xFFFF55FF;
            case 5 -> 0xFFFFAA00;
            case 6 -> 0xFFFF5555;
            default -> 0xFFAA0000;
        };
    }

    private record RenderLine(Type type, String pathName, SyncCodexPayload.CodexEntry entry) {
        enum Type { PATH_HEADER, GU_ENTRY, SPACER }
    }
}
