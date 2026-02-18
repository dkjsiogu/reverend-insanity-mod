package com.reverendinsanity.client.gui;

import com.reverendinsanity.client.ClientDataCache;
import com.reverendinsanity.network.DiscardGuPayload;
import com.reverendinsanity.network.EquipMovePayload;
import com.reverendinsanity.network.FeedGuPayload;
import com.reverendinsanity.network.SyncApertureContentsPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.ArrayList;
import java.util.List;

// 空窍管理界面：查看蛊虫、喂养、装备/卸下杀招
public class ApertureScreen extends Screen {

    private static final int PANEL_W = 380;
    private static final int PANEL_H = 280;
    private static final int GU_ENTRY_H = 24;
    private static final int VISIBLE_GU_COUNT = 9;
    private static final int LEFT_PANEL_W = 170;
    private static final int RIGHT_PANEL_W = 190;

    private static final int BG_TOP = 0xCC0A0A1E;
    private static final int BG_BOTTOM = 0xCC1A1A3E;
    private static final int BORDER_OUTER = 0xFF3A3A5A;
    private static final int BORDER_INNER = 0xFF664400;
    private static final int TITLE_COLOR = 0xFFFFD700;
    private static final int TEXT_COLOR = 0xFFCCCCCC;
    private static final int DIM_TEXT = 0xFF888888;

    private int scrollOffset = 0;

    public ApertureScreen() {
        super(Component.literal("空窍管理"));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xCC000000);

        int px = (this.width - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;

        renderPanelBackground(graphics, px, py);
        renderTitleBar(graphics, px, py);

        int dividerX = px + LEFT_PANEL_W + 10;
        for (int y = py + 30; y < py + PANEL_H - 45; y += 2) {
            graphics.fill(dividerX, y, dividerX + 1, y + 1, 0xFF4A4A6A);
        }

        renderGuList(graphics, mouseX, mouseY, px, py);
        renderEquippedMoves(graphics, mouseX, mouseY, px, py);
        renderAvailableMoves(graphics, mouseX, mouseY, px, py);
        renderBottomPanel(graphics, px, py);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPanelBackground(GuiGraphics graphics, int px, int py) {
        int steps = PANEL_H;
        for (int i = 0; i < steps; i++) {
            float ratio = (float) i / steps;
            int r = (int) (((BG_TOP >> 16) & 0xFF) * (1 - ratio) + ((BG_BOTTOM >> 16) & 0xFF) * ratio);
            int g = (int) (((BG_TOP >> 8) & 0xFF) * (1 - ratio) + ((BG_BOTTOM >> 8) & 0xFF) * ratio);
            int b = (int) ((BG_TOP & 0xFF) * (1 - ratio) + (BG_BOTTOM & 0xFF) * ratio);
            int a = (int) (((BG_TOP >> 24) & 0xFF) * (1 - ratio) + ((BG_BOTTOM >> 24) & 0xFF) * ratio);
            int color = (a << 24) | (r << 16) | (g << 8) | b;
            graphics.fill(px, py + i, px + PANEL_W, py + i + 1, color);
        }

        graphics.fill(px, py, px + PANEL_W, py + 2, BORDER_OUTER);
        graphics.fill(px, py + PANEL_H - 2, px + PANEL_W, py + PANEL_H, BORDER_OUTER);
        graphics.fill(px, py, px + 2, py + PANEL_H, BORDER_OUTER);
        graphics.fill(px + PANEL_W - 2, py, px + PANEL_W, py + PANEL_H, BORDER_OUTER);

        graphics.fill(px + 2, py + 2, px + PANEL_W - 2, py + 3, BORDER_INNER);
        graphics.fill(px + 2, py + PANEL_H - 3, px + PANEL_W - 2, py + PANEL_H - 2, BORDER_INNER);
        graphics.fill(px + 2, py + 2, px + 3, py + PANEL_H - 2, BORDER_INNER);
        graphics.fill(px + PANEL_W - 3, py + 2, px + PANEL_W - 2, py + PANEL_H - 2, BORDER_INNER);
    }

    private void renderTitleBar(GuiGraphics graphics, int px, int py) {
        String[] subRankNames = {"初阶", "中阶", "上阶", "巅峰"};
        String subRank = ClientDataCache.getSubRankIndex() >= 0 && ClientDataCache.getSubRankIndex() < 4
            ? subRankNames[ClientDataCache.getSubRankIndex()] : "?";
        String info = ClientDataCache.isOpened()
            ? ClientDataCache.getRankLevel() + "转" + subRank + " [" + ClientDataCache.getAptitudeName() + "]"
            : "空窍未开";
        graphics.drawCenteredString(this.font, "\u2014 \u7A7A\u7A8D\u7BA1\u7406 \u2014", px + PANEL_W / 2, py + 6, TITLE_COLOR);
        graphics.drawCenteredString(this.font, info, px + PANEL_W / 2, py + 18, TEXT_COLOR);
    }

    private void renderGuList(GuiGraphics graphics, int mouseX, int mouseY, int px, int py) {
        int guX = px + 8;
        int guY = py + 34;
        graphics.drawString(this.font, "\u86CA\u866B", guX, guY, TITLE_COLOR);
        guY += 14;

        List<SyncApertureContentsPayload.GuInfo> guList = ClientDataCache.getGuList();
        if (guList == null || guList.isEmpty()) {
            graphics.drawString(this.font, "\u7A8D\u7A74\u7A7A\u7A7A\u5982\u4E5F...", guX, guY, 0xFF666666);
            return;
        }

        int maxScroll = Math.max(0, guList.size() - VISIBLE_GU_COUNT);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        int visibleCount = Math.min(guList.size() - scrollOffset, VISIBLE_GU_COUNT);
        for (int i = 0; i < visibleCount; i++) {
            int actualIndex = scrollOffset + i;
            SyncApertureContentsPayload.GuInfo gu = guList.get(actualIndex);
            int yy = guY + i * GU_ENTRY_H;

            boolean hoverEntry = mouseX >= guX && mouseX < guX + LEFT_PANEL_W && mouseY >= yy && mouseY < yy + GU_ENTRY_H;
            if (hoverEntry) {
                graphics.fill(guX - 2, yy - 1, guX + LEFT_PANEL_W, yy + GU_ENTRY_H - 1, 0x22FFFFFF);
            }

            int nameColor = gu.alive() ? (gu.refined() ? 0xFFAAFFAA : 0xFFCCCC66) : 0xFF666666;
            String label = gu.displayName() + " " + gu.rank() + "\u8F6C";
            graphics.drawString(this.font, label, guX, yy, nameColor);

            String pathLabel = gu.categoryName();
            int pathLabelW = this.font.width(pathLabel);
            graphics.drawString(this.font, pathLabel, guX + LEFT_PANEL_W - 45 - pathLabelW, yy, 0xFF777777);

            int barX = guX;
            int barY2 = yy + 11;
            int barW = 60;
            int barH = 5;
            float ratio = gu.hunger() / 100f;
            int barColor = ratio > 0.5f ? 0xFF00CC00 : (ratio > 0.2f ? 0xFFCCCC00 : 0xFFCC0000);
            graphics.fill(barX, barY2, barX + barW, barY2 + barH, 0xFF222222);
            graphics.fill(barX, barY2, barX + (int)(barW * ratio), barY2 + barH, barColor);

            float profRatio = gu.proficiency() / 100f;
            int profBarX = barX + barW + 4;
            int profBarW = 30;
            graphics.fill(profBarX, barY2, profBarX + profBarW, barY2 + barH, 0xFF222222);
            graphics.fill(profBarX, barY2, profBarX + (int)(profBarW * profRatio), barY2 + barH, 0xFF6688FF);

            if (gu.alive() && gu.hunger() < 100f) {
                int btnX = guX + LEFT_PANEL_W - 40;
                int btnY = yy + 2;
                int btnW = 36;
                int btnH = 14;
                boolean hoverBtn = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
                int btnColor = hoverBtn ? 0xFF44CC44 : 0xFF228822;
                graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnColor);
                graphics.fill(btnX, btnY, btnX + btnW, btnY + 1, 0xFF55DD55);
                graphics.fill(btnX, btnY, btnX + 1, btnY + btnH, 0xFF55DD55);
                graphics.drawCenteredString(this.font, "\u5582\u517B", btnX + btnW / 2, btnY + 3, 0xFFFFFFFF);
            }

            if (hoverEntry && gu.alive()) {
                renderGuTooltip(graphics, mouseX, mouseY, gu);
            }
        }

        if (guList.size() > VISIBLE_GU_COUNT) {
            int scrollBarX = guX + LEFT_PANEL_W - 4;
            int scrollAreaH = VISIBLE_GU_COUNT * GU_ENTRY_H;
            int scrollBarH = Math.max(10, scrollAreaH * VISIBLE_GU_COUNT / guList.size());
            int scrollBarY = guY + (int)((float)scrollOffset / maxScroll * (scrollAreaH - scrollBarH));
            graphics.fill(scrollBarX, guY, scrollBarX + 3, guY + scrollAreaH, 0xFF222222);
            graphics.fill(scrollBarX, scrollBarY, scrollBarX + 3, scrollBarY + scrollBarH, 0xFF666688);
        }
    }

    private void renderEquippedMoves(GuiGraphics graphics, int mouseX, int mouseY, int px, int py) {
        int moveX = px + LEFT_PANEL_W + 18;
        int moveY = py + 34;
        graphics.drawString(this.font, "\u5DF2\u88C5\u5907\u6740\u62DB", moveX, moveY, TITLE_COLOR);
        moveY += 14;

        List<SyncApertureContentsPayload.MoveInfo> equipped = ClientDataCache.getEquippedMoveList();
        if (equipped != null && !equipped.isEmpty()) {
            for (int i = 0; i < equipped.size(); i++) {
                SyncApertureContentsPayload.MoveInfo move = equipped.get(i);
                int yy = moveY + i * 26;
                boolean hover = mouseX >= moveX && mouseX < moveX + RIGHT_PANEL_W - 10 && mouseY >= yy && mouseY < yy + 22;
                if (hover) graphics.fill(moveX - 2, yy - 1, moveX + RIGHT_PANEL_W - 8, yy + 23, 0x22FFFFFF);

                graphics.drawString(this.font, move.displayName(), moveX, yy, 0xFFFF6666);

                String unequipText = "[\u5378\u4E0B]";
                int unequipX = moveX + RIGHT_PANEL_W - 10 - this.font.width(unequipText) - 4;
                boolean hoverUnequip = mouseX >= unequipX && mouseX < unequipX + this.font.width(unequipText) && mouseY >= yy && mouseY < yy + 12;
                graphics.drawString(this.font, unequipText, unequipX, yy, hoverUnequip ? 0xFFFF4444 : 0xFFAA4444);

                graphics.drawString(this.font, move.moveType() + " " + (int)move.essenceCost() + "\u771F\u5143", moveX, yy + 11, DIM_TEXT);
                if (!move.description().isEmpty()) {
                    graphics.drawString(this.font, move.description(), moveX, yy + 21, 0xFF666699);
                }
            }
        } else {
            graphics.drawString(this.font, "\u672A\u88C5\u5907\u6740\u62DB", moveX, moveY, 0xFF666666);
        }
    }

    private void renderAvailableMoves(GuiGraphics graphics, int mouseX, int mouseY, int px, int py) {
        int moveX = px + LEFT_PANEL_W + 18;
        int availY = py + 110;
        graphics.drawString(this.font, "\u53EF\u88C5\u5907\u6740\u62DB", moveX, availY, TITLE_COLOR);
        availY += 14;

        List<SyncApertureContentsPayload.MoveInfo> equipped = ClientDataCache.getEquippedMoveList();
        List<SyncApertureContentsPayload.MoveInfo> available = ClientDataCache.getAvailableMoveList();
        if (available != null) {
            int drawn = 0;
            for (int i = 0; i < available.size() && drawn < 5; i++) {
                SyncApertureContentsPayload.MoveInfo move = available.get(i);
                boolean isEquipped = equipped != null && equipped.stream().anyMatch(e -> e.moveId().equals(move.moveId()));
                if (isEquipped) continue;
                int yy = availY + drawn * 26;
                boolean canEquip = equipped == null || equipped.size() < 2;
                boolean hover = canEquip && mouseX >= moveX && mouseX < moveX + RIGHT_PANEL_W - 10 && mouseY >= yy && mouseY < yy + 22;
                if (hover) graphics.fill(moveX - 2, yy - 1, moveX + RIGHT_PANEL_W - 8, yy + 23, 0x22FFFFFF);

                graphics.drawString(this.font, move.displayName(), moveX, yy, canEquip ? 0xFF66FF66 : 0xFF666666);
                if (canEquip) {
                    String equipText = "[\u88C5\u5907]";
                    int equipX = moveX + RIGHT_PANEL_W - 10 - this.font.width(equipText) - 4;
                    boolean hoverEquip = mouseX >= equipX && mouseX < equipX + this.font.width(equipText) && mouseY >= yy && mouseY < yy + 12;
                    graphics.drawString(this.font, equipText, equipX, yy, hoverEquip ? 0xFF44FF44 : 0xFF448844);
                }
                graphics.drawString(this.font, move.moveType() + " " + (int)move.essenceCost() + "\u771F\u5143", moveX, yy + 11, DIM_TEXT);
                if (hover && !move.description().isEmpty()) {
                    renderTooltipBox(graphics, mouseX, mouseY, move.description());
                }
                drawn++;
            }
        }
    }

    private void renderGuTooltip(GuiGraphics graphics, int mouseX, int mouseY, SyncApertureContentsPayload.GuInfo gu) {
        List<String> lines = new ArrayList<>();
        lines.add(gu.displayName() + " " + gu.rank() + "\u8F6C" + (gu.refined() ? " \u00A7a[\u5DF2\u70BC\u5316]\u00A7r" : " \u00A77[\u672A\u70BC\u5316]\u00A7r"));
        lines.add("\u9053\u8DEF: " + gu.pathName() + "  \u7C7B\u578B: " + gu.categoryName());
        lines.add("\u9965\u997F\u5EA6: " + (int) gu.hunger() + "%");
        lines.add("\u719F\u7EC3\u5EA6: " + (int) gu.proficiency() + "%");
        if (gu.proficiency() >= 100) lines.add("\u00A7e\u5927\u5E08\u7EA7 - \u771F\u5143\u8017\u964D\u4F4E\u00A7r");
        else if (gu.proficiency() >= 75) lines.add("\u00A7b\u7CBE\u901A - \u51B7\u5374\u7F29\u77ED\u00A7r");
        else if (gu.proficiency() >= 50) lines.add("\u00A7a\u719F\u7EC3 - \u6548\u679C\u589E\u5F3A\u00A7r");

        int maxW = 0;
        for (String line : lines) maxW = Math.max(maxW, this.font.width(line));
        int tw = maxW + 10;
        int th = lines.size() * 11 + 6;
        int tx = mouseX + 12;
        int ty = mouseY - th - 4;
        if (tx + tw > this.width) tx = this.width - tw - 4;
        if (ty < 0) ty = mouseY + 12;

        graphics.fill(tx - 3, ty - 3, tx + tw + 3, ty + th + 3, 0xEE0A0A1E);
        graphics.fill(tx - 3, ty - 3, tx + tw + 3, ty - 2, 0xFF664400);
        graphics.fill(tx - 3, ty + th + 2, tx + tw + 3, ty + th + 3, 0xFF664400);
        graphics.fill(tx - 3, ty - 3, tx - 2, ty + th + 3, 0xFF664400);
        graphics.fill(tx + tw + 2, ty - 3, tx + tw + 3, ty + th + 3, 0xFF664400);

        for (int i = 0; i < lines.size(); i++) {
            int color = i == 0 ? 0xFFFFD700 : 0xFFCCCCDD;
            graphics.drawString(this.font, lines.get(i), tx + 2, ty + 2 + i * 11, color);
        }
    }

    private void renderTooltipBox(GuiGraphics graphics, int mouseX, int mouseY, String text) {
        int tw = this.font.width(text) + 8;
        int th = 14;
        int tx = mouseX + 8;
        int ty = mouseY - th - 4;
        if (tx + tw > this.width) tx = this.width - tw - 2;
        if (ty < 0) ty = mouseY + 12;
        graphics.fill(tx - 2, ty - 2, tx + tw + 2, ty + th + 2, 0xEE111122);
        graphics.fill(tx - 2, ty - 2, tx + tw + 2, ty - 1, 0xFF664400);
        graphics.fill(tx - 2, ty + th + 1, tx + tw + 2, ty + th + 2, 0xFF664400);
        graphics.drawString(this.font, text, tx + 2, ty + 2, 0xFFDDDDFF);
    }

    private void renderBottomPanel(GuiGraphics graphics, int px, int py) {
        int bottomY = py + PANEL_H - 42;
        graphics.fill(px + 4, bottomY, px + PANEL_W - 4, bottomY + 1, 0xFF4A4A6A);
        bottomY += 4;

        int barW = 130;
        graphics.drawString(this.font, "\u771F\u5143", px + 8, bottomY, 0xFFAAFFAA);
        int eBarX = px + 35;
        graphics.fill(eBarX, bottomY, eBarX + barW, bottomY + 8, 0xFF222222);
        float eRatio = ClientDataCache.getMaxEssence() > 0 ? ClientDataCache.getCurrentEssence() / ClientDataCache.getMaxEssence() : 0;
        int essColor = ClientDataCache.getEssenceColor() | 0xFF000000;
        if (eRatio > 0) {
            int endColor = blendColor(essColor, 0xFF000000, 0.4f);
            for (int x = 0; x < (int)(barW * eRatio); x++) {
                float r2 = (float) x / (barW * eRatio);
                int c = blendColor(essColor, endColor, r2);
                graphics.fill(eBarX + x, bottomY, eBarX + x + 1, bottomY + 8, c);
            }
        }
        graphics.drawString(this.font, (int) ClientDataCache.getCurrentEssence() + "/" + (int) ClientDataCache.getMaxEssence(), eBarX + barW + 4, bottomY, TEXT_COLOR);

        int tBarX = px + 210;
        graphics.drawString(this.font, "\u5FF5\u5934", tBarX - 25, bottomY, 0xFFAAAAFF);
        graphics.fill(tBarX, bottomY, tBarX + barW, bottomY + 8, 0xFF222222);
        float tRatio = ClientDataCache.getMaxThoughts() > 0 ? ClientDataCache.getThoughts() / ClientDataCache.getMaxThoughts() : 0;
        graphics.fill(tBarX, bottomY, tBarX + (int)(barW * tRatio), bottomY + 8, 0xFF6688CC);
        graphics.drawString(this.font, (int) ClientDataCache.getThoughts() + "/" + (int) ClientDataCache.getMaxThoughts(), tBarX + barW + 4, bottomY, TEXT_COLOR);

        bottomY += 14;
        List<SyncApertureContentsPayload.GuInfo> guList = ClientDataCache.getGuList();
        int total = guList != null ? guList.size() : 0;
        long alive = guList != null ? guList.stream().filter(SyncApertureContentsPayload.GuInfo::alive).count() : 0;
        int maxCapacity = getMaxGuCapacity(ClientDataCache.getRankLevel());
        graphics.drawString(this.font, "\u86CA\u866B: " + alive + "/" + maxCapacity + "\u53EA | \u6B7B\u4EA1: " + (total - alive) + "\u53EA", px + 8, bottomY, DIM_TEXT);
        graphics.drawString(this.font, "\u53F3\u952E\u86CA\u866B\u53EF\u4E22\u5F03", px + PANEL_W - 100, bottomY, 0xFF554444);
    }

    private static int getMaxGuCapacity(int rank) {
        return switch (rank) {
            case 1 -> 5;
            case 2 -> 8;
            case 3 -> 12;
            case 4 -> 16;
            case 5 -> 20;
            default -> 5;
        };
    }

    private int blendColor(int c1, int c2, float t) {
        int a = (int)(((c1 >> 24) & 0xFF) * (1 - t) + ((c2 >> 24) & 0xFF) * t);
        int r = (int)(((c1 >> 16) & 0xFF) * (1 - t) + ((c2 >> 16) & 0xFF) * t);
        int g = (int)(((c1 >> 8) & 0xFF) * (1 - t) + ((c2 >> 8) & 0xFF) * t);
        int b = (int)((c1 & 0xFF) * (1 - t) + (c2 & 0xFF) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int px = (this.width - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;
        int guX = px + 8;
        int guY = py + 48;
        int guAreaH = VISIBLE_GU_COUNT * GU_ENTRY_H;

        if (mouseX >= guX && mouseX < guX + LEFT_PANEL_W && mouseY >= guY && mouseY < guY + guAreaH) {
            List<SyncApertureContentsPayload.GuInfo> guList = ClientDataCache.getGuList();
            if (guList != null) {
                int maxScroll = Math.max(0, guList.size() - VISIBLE_GU_COUNT);
                if (scrollY > 0) {
                    scrollOffset = Math.max(0, scrollOffset - 1);
                } else if (scrollY < 0) {
                    scrollOffset = Math.min(maxScroll, scrollOffset + 1);
                }
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int px = (this.width - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;

        if (button == 1) {
            if (handleGuDiscardClick(mouseX, mouseY, px, py)) return true;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        if (handleGuFeedClick(mouseX, mouseY, px, py)) return true;
        if (handleEquippedMoveClick(mouseX, mouseY, px, py)) return true;
        if (handleAvailableMoveClick(mouseX, mouseY, px, py)) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleGuDiscardClick(double mouseX, double mouseY, int px, int py) {
        int guX = px + 8;
        int guY = py + 48;

        List<SyncApertureContentsPayload.GuInfo> guList = ClientDataCache.getGuList();
        if (guList == null || guList.isEmpty()) return false;

        int visibleCount = Math.min(guList.size() - scrollOffset, VISIBLE_GU_COUNT);
        for (int i = 0; i < visibleCount; i++) {
            int actualIndex = scrollOffset + i;
            int yy = guY + i * GU_ENTRY_H;
            if (mouseX >= guX && mouseX < guX + LEFT_PANEL_W && mouseY >= yy && mouseY < yy + GU_ENTRY_H) {
                PacketDistributor.sendToServer(new DiscardGuPayload(actualIndex));
                return true;
            }
        }
        return false;
    }

    private boolean handleGuFeedClick(double mouseX, double mouseY, int px, int py) {
        int guX = px + 8;
        int guY = py + 48;

        List<SyncApertureContentsPayload.GuInfo> guList = ClientDataCache.getGuList();
        if (guList == null || guList.isEmpty()) return false;

        int visibleCount = Math.min(guList.size() - scrollOffset, VISIBLE_GU_COUNT);
        for (int i = 0; i < visibleCount; i++) {
            int actualIndex = scrollOffset + i;
            SyncApertureContentsPayload.GuInfo gu = guList.get(actualIndex);
            if (!gu.alive() || gu.hunger() >= 100f) continue;

            int yy = guY + i * GU_ENTRY_H;
            int btnX = guX + LEFT_PANEL_W - 40;
            int btnY = yy + 2;
            int btnW = 36;
            int btnH = 14;

            if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                PacketDistributor.sendToServer(new FeedGuPayload(actualIndex));
                return true;
            }
        }
        return false;
    }

    private boolean handleEquippedMoveClick(double mouseX, double mouseY, int px, int py) {
        int moveX = px + LEFT_PANEL_W + 18;
        int moveY = py + 48;

        List<SyncApertureContentsPayload.MoveInfo> equipped = ClientDataCache.getEquippedMoveList();
        if (equipped == null) return false;

        for (int i = 0; i < equipped.size(); i++) {
            SyncApertureContentsPayload.MoveInfo move = equipped.get(i);
            int yy = moveY + i * 26;
            String unequipText = "[\u5378\u4E0B]";
            int unequipX = moveX + RIGHT_PANEL_W - 10 - this.font.width(unequipText) - 4;
            if (mouseX >= unequipX && mouseX < unequipX + this.font.width(unequipText) && mouseY >= yy && mouseY < yy + 12) {
                PacketDistributor.sendToServer(new EquipMovePayload(move.moveId(), false));
                return true;
            }
        }
        return false;
    }

    private boolean handleAvailableMoveClick(double mouseX, double mouseY, int px, int py) {
        int moveX = px + LEFT_PANEL_W + 18;
        int availY = py + 124;

        List<SyncApertureContentsPayload.MoveInfo> equipped = ClientDataCache.getEquippedMoveList();
        if (equipped != null && equipped.size() >= 2) return false;

        List<SyncApertureContentsPayload.MoveInfo> available = ClientDataCache.getAvailableMoveList();
        if (available == null) return false;

        int drawn = 0;
        for (int i = 0; i < available.size() && drawn < 5; i++) {
            SyncApertureContentsPayload.MoveInfo move = available.get(i);
            boolean isEquipped = equipped != null && equipped.stream().anyMatch(e -> e.moveId().equals(move.moveId()));
            if (isEquipped) continue;
            int yy = availY + drawn * 26;
            String equipText = "[\u88C5\u5907]";
            int equipX = moveX + RIGHT_PANEL_W - 10 - this.font.width(equipText) - 4;
            if (mouseX >= equipX && mouseX < equipX + this.font.width(equipText) && mouseY >= yy && mouseY < yy + 12) {
                PacketDistributor.sendToServer(new EquipMovePayload(move.moveId(), true));
                return true;
            }
            drawn++;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
