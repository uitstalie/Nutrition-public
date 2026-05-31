package com.uitstalie.neotrition.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.uitstalie.neotrition.client.ClientNutritionState;
import com.uitstalie.neotrition.client.ClientNutritionState.SyncedAttribute;
import com.uitstalie.neotrition.client.ClientNutritionState.SyncedEffect;
import com.uitstalie.neotrition.client.ClientNutritionState.SyncedGroup;
import com.uitstalie.neotrition.util.log.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

/**
 * 营养成分查看型 GUI。
 *
 * <p>N 键打开，不暂停游戏。展示玩家全部营养组的当前营养值，
 * 含 icon、名称、百分比文本和进度条。支持页面标签骨架和滚动。</p>
 */
public class GameGui extends Screen {

    // ── Layout constants ──
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 260;
    private static final int TAB_WIDTH = 70;
    private static final int TITLE_HEIGHT = 18;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int CELL_MIN_WIDTH = 90;
    private static final int CELL_HEIGHT = 42;
    private static final int CELL_GAP = 4;
    private static final int COLUMNS = 2;
    private static final int CONTENT_PADDING = 6;
    private static final int TAB_HEIGHT = 20;
    private static final int TAB_GAP = 3;

    private static final int COLOR_PANEL_BACKGROUND = 0xC0101010;
    private static final int COLOR_PANEL_BORDER = 0xFF666666;
    private static final int COLOR_TAB_ACTIVE = 0xFF555555;
    private static final int COLOR_TAB_HOVER = 0xFF444444;
    private static final int COLOR_TAB_BACKGROUND = 0xFF333333;
    private static final int COLOR_TEXT_PRIMARY = 0xFFDDDDDD;
    private static final int COLOR_TEXT_SECONDARY = 0xFF888888;
    private static final int COLOR_EFFECT_HEADER = 0xFFAAFFAA;
    private static final int COLOR_ATTRIBUTE_HEADER = 0xFFFFAA55;

    private static final String TAB_INFO = "neotrition.gui.tab.info";
    private static final String TAB_EFFECTS = "neotrition.gui.tab.effects";

    // ── Computed layout ──
    private int guiLeft, guiTop;
    private int contentLeft, contentTop, contentWidth, contentHeight;
    private int panelW, panelH;

    // ── State ──
    private double scrollOffset;
    private String activeTab = TAB_INFO;

    public GameGui() {
        super(Component.translatable("neotrition.gui.title"));
    }

    @Override
    public void init() {
        recalculateLayout();
    }

    @Override
    public void resize(@NotNull Minecraft mc, int width, int height) {
        super.resize(mc, width, height);
        recalculateLayout();
    }

    private void recalculateLayout() {
        panelW = Math.min(PANEL_WIDTH, this.width - 20);
        panelH = Math.min(PANEL_HEIGHT, this.height - 20);
        guiLeft = (this.width - panelW) / 2;
        guiTop = (this.height - panelH) / 2;

        contentLeft = guiLeft + TAB_WIDTH + 8;
        contentTop = guiTop + TITLE_HEIGHT + 8;
        contentWidth = panelW - TAB_WIDTH - 8 - SCROLLBAR_WIDTH - 4;
        contentHeight = panelH - TITLE_HEIGHT - 16;
    }

    // ── Render ──────────────────────────────────────────────────────────

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        drawPanel(guiGraphics);
        drawTitleBar(guiGraphics);
        drawTabArea(guiGraphics, mouseX, mouseY);
        drawContent(guiGraphics, mouseX, mouseY);
        drawScrollbar(guiGraphics);
    }

    private void drawPanel(GuiGraphics guiGraphics) {
        // Background
        guiGraphics.fill(guiLeft, guiTop, guiLeft + panelW, guiTop + panelH, COLOR_PANEL_BACKGROUND);

        // Border
        int border = COLOR_PANEL_BORDER;
        guiGraphics.fill(guiLeft - 1, guiTop - 1, guiLeft + panelW + 1, guiTop, border);
        guiGraphics.fill(guiLeft - 1, guiTop + panelH, guiLeft + panelW + 1, guiTop + panelH + 1, border);
        guiGraphics.fill(guiLeft - 1, guiTop, guiLeft, guiTop + panelH, border);
        guiGraphics.fill(guiLeft + panelW, guiTop, guiLeft + panelW + 1, guiTop + panelH, border);
    }

    private void drawTitleBar(GuiGraphics guiGraphics) {
        int x = guiLeft + panelW / 2;
        int y = guiTop + 4;
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("neotrition.gui.title"),
                x, y, 0xFFFFFFFF);
    }

    private void drawTabArea(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawTab(guiGraphics, TAB_INFO, 0, mouseX, mouseY);
        drawTab(guiGraphics, TAB_EFFECTS, 1, mouseX, mouseY);
    }

    private void drawTab(GuiGraphics guiGraphics, String tabKey, int index, int mouseX, int mouseY) {
        int tabX = tabX();
        int tabY = tabY(index);
        int tabW = tabWidth();
        boolean hovered = isInTab(mouseX, mouseY, index);
        int background = tabKey.equals(activeTab)
                ? COLOR_TAB_ACTIVE
                : (hovered ? COLOR_TAB_HOVER : COLOR_TAB_BACKGROUND);
        guiGraphics.fill(tabX, tabY, tabX + tabW, tabY + TAB_HEIGHT, background);
        guiGraphics.drawCenteredString(this.font, Component.translatable(tabKey),
                tabX + tabW / 2, tabY + 6, COLOR_TEXT_PRIMARY);
    }

    private int tabX() {
        return guiLeft + 8;
    }

    private int tabY(int index) {
        return contentTop + index * (TAB_HEIGHT + TAB_GAP);
    }

    private int tabWidth() {
        return TAB_WIDTH - 8;
    }

    private boolean isInTab(double mouseX, double mouseY, int index) {
        int tabX = tabX();
        int tabY = tabY(index);
        int tabW = tabWidth();
        return mouseX >= tabX && mouseX < tabX + tabW
                && mouseY >= tabY && mouseY < tabY + TAB_HEIGHT;
    }

    // ── Effects page ──────────────────────────────────────────────────

    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private void drawEffectsContent(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<SyncedEffect> effects = ClientNutritionState.getEffects();
        List<SyncedAttribute> attributes = ClientNutritionState.getAttributes();

        int y = contentTop + CONTENT_PADDING;
        int x = contentLeft + CONTENT_PADDING;

        // ── Effects section ──
        guiGraphics.drawString(this.font, Component.translatable("neotrition.gui.effects.header"), x, y, COLOR_EFFECT_HEADER);
        y += 14;
        if (effects.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable("neotrition.gui.none"), x, y, COLOR_TEXT_SECONDARY);
            y += 12;
        } else {
            for (SyncedEffect e : effects) {
                String level = e.amplifier() < ROMAN.length ? ROMAN[e.amplifier()] : String.valueOf(e.amplifier() + 1);
                String text = "  " + e.effectId().getPath() + " " + level;
                guiGraphics.drawString(this.font, Component.literal(text), x, y, COLOR_TEXT_PRIMARY);
                y += 12;
            }
        }

        // ── Attributes section ──
        y += 6;
        guiGraphics.drawString(this.font, Component.translatable("neotrition.gui.attributes.header"), x, y, COLOR_ATTRIBUTE_HEADER);
        y += 14;
        if (attributes.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable("neotrition.gui.none"), x, y, COLOR_TEXT_SECONDARY);
        } else {
            for (SyncedAttribute a : attributes) {
                String text = String.format("  %s  +%.1f  (%s)",
                        a.attributeId().getPath(), a.amount(), a.operation());
                guiGraphics.drawString(this.font, Component.literal(text), x, y, COLOR_TEXT_PRIMARY);
                y += 12;
            }
        }
    }

    // ── Info page (nutrition groups) ──────────────────────────────────

    private void drawContent(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (TAB_EFFECTS.equals(activeTab)) {
            drawEffectsContent(guiGraphics, mouseX, mouseY);
        } else {
            drawInfoContent(guiGraphics, mouseX, mouseY);
        }
    }

    private void drawInfoContent(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<SyncedGroup> groups = ClientNutritionState.getGroups();
        Map<String, Integer> values = ClientNutritionState.getNutritionValues();

        // ── Empty state ──
        if (groups.isEmpty()) {
            int cx = contentLeft + contentWidth / 2;
            int cy = contentTop + contentHeight / 2 - 8;
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("neotrition.gui.no_groups"),
                    cx, cy, COLOR_TEXT_SECONDARY);
            return;
        }

        int cols = calcColumns();
        int cellW = (contentWidth + CONTENT_PADDING - (cols - 1) * CELL_GAP) / cols;
        int cellH = CELL_HEIGHT;
        int rows = (groups.size() + cols - 1) / cols;

        int totalH = rows * (cellH + CELL_GAP) - CELL_GAP + CONTENT_PADDING * 2;
        int maxScroll = Math.max(0, totalH - contentHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        // Scissor clipping for content + scrollbar area
        guiGraphics.enableScissor(contentLeft, contentTop,
                contentLeft + contentWidth + SCROLLBAR_WIDTH,
                contentTop + contentHeight);

        int scrollY = contentTop + CONTENT_PADDING - (int) scrollOffset;

        for (int i = 0; i < groups.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int cx = contentLeft + CONTENT_PADDING + col * (cellW + CELL_GAP);
            int cy = scrollY + row * (cellH + CELL_GAP);

            // Skip cells completely outside visible area
            if (cy + cellH < contentTop || cy > contentTop + contentHeight) continue;

            SyncedGroup group = groups.get(i);
            int nutrition = values.getOrDefault(group.groupName(), 0);
            drawGroupCell(guiGraphics, cx, cy, cellW, cellH, group, nutrition);
        }

        guiGraphics.disableScissor();
    }

    private int calcColumns() {
        return Math.min(COLUMNS, Math.max(1, (contentWidth + CELL_GAP) / (CELL_MIN_WIDTH + CELL_GAP)));
    }

    // ── Arc progress renderer ──────────────────────────────────────────

    /**
     * Draws a ring segment arc via immediate-mode triangle strip.
     * Angles are in degrees, 0° = right, clockwise (screen Y-down).
     */
    private static void drawArc(GuiGraphics guiGraphics, float cx, float cy,
                                float innerR, float outerR,
                                double startAngleDeg, double sweepAngleDeg,
                                int colorARGB, int segments) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = guiGraphics.pose().last().pose();

        float a = ((colorARGB >> 24) & 0xFF) / 255.0f;
        float r = ((colorARGB >> 16) & 0xFF) / 255.0f;
        float g = ((colorARGB >> 8) & 0xFF) / 255.0f;
        float b = (colorARGB & 0xFF) / 255.0f;
        if (a == 0f) a = 1f;

        int steps = Math.max(2, (int) (segments * Math.abs(sweepAngleDeg) / 360.0));
        for (int i = 0; i <= steps; i++) {
            double angle = Math.toRadians(startAngleDeg + sweepAngleDeg * i / steps);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            builder.addVertex(matrix, cx + outerR * cos, cy + outerR * sin, 0).setColor(r, g, b, a);
            builder.addVertex(matrix, cx + innerR * cos, cy + innerR * sin, 0).setColor(r, g, b, a);
        }

        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    // ── Group cell ─────────────────────────────────────────────────────

    private void drawGroupCell(GuiGraphics guiGraphics, int cx, int cy, int cw, int ch,
                                SyncedGroup group, int nutrition) {
        // ── Row 1: icon centered ──
        int iconX = cx + (cw - 16) / 2;
        int iconY = cy + 3;
        try {
            ResourceLocation rl = ResourceLocation.tryParse(group.groupIcon());
            if (rl != null) {
                var item = BuiltInRegistries.ITEM.getOptional(rl);
                if (item.isPresent() && item.get() != Items.AIR) {
                    guiGraphics.renderItem(new ItemStack(item.get()), iconX, iconY);
                }
            }
        } catch (Exception e) {
            Log.w("GameGui", "Failed rendering icon '" + group.groupIcon() + "': " + e.getMessage());
        }

        float pct = nutrition / 1000.0f;           // [0, 100000] → 0.000 ~ 100.000

        // ── Arc ring around icon ──
        float arcCx = cx + cw / 2.0f;
        float arcCy = cy + 11;                      // icon center (cy+3 + 8)
        float outerR = 10.5f;
        float innerR = 8.0f;
        double startAngle = 135.0;                   // top-right, gap at bottom
        double sweep = 260.0;                        // slightly shorter than 270°, cleaner gap

        // Background: gray arc
        drawArc(guiGraphics, arcCx, arcCy, innerR, outerR, startAngle, sweep,
                0xFF555555, 64);

        // Foreground: colored by percentage
        if (pct > 0) {
            double fillAngle = sweep * Math.min(pct / 100.0, 1.0);
            int fillColor = group.guiPngColor();
            // Ensure fully opaque
            fillColor = 0xFF000000 | (fillColor & 0x00FFFFFF);
            drawArc(guiGraphics, arcCx, arcCy, innerR, outerR, startAngle, fillAngle,
                    fillColor, 64);
        }

        // ── Row 2: "name: xx.xxx%" centered ──
        Component translatedName = Component.translatable("neotrition.group." + group.groupName());
        Component line = Component.translatable("neotrition.gui.group_pct", translatedName, String.format("%.3f%%", pct));
        int textX = cx + Math.max(0, (cw - this.font.width(line)) / 2);
        int textY = cy + 28;
        guiGraphics.drawString(this.font, line, textX, textY, group.guiTextColor());
    }

    // ── Scrollbar ──────────────────────────────────────────────────────

    private void drawScrollbar(GuiGraphics guiGraphics) {
        List<SyncedGroup> groups = ClientNutritionState.getGroups();
        if (groups.isEmpty()) return;

        int cols = calcColumns();
        int cellH = CELL_HEIGHT;
        int rows = (groups.size() + cols - 1) / cols;
        int totalH = rows * (cellH + CELL_GAP) - CELL_GAP + CONTENT_PADDING * 2;

        if (totalH <= contentHeight) return;

        int sbX = contentLeft + contentWidth;
        int sbY = contentTop;
        int sbH = contentHeight;

        // Track
        guiGraphics.fill(sbX, sbY, sbX + SCROLLBAR_WIDTH, sbY + sbH, COLOR_TAB_BACKGROUND);

        // Thumb
        int maxScroll = totalH - contentHeight;
        int thumbH = Math.max(16, (int) ((float) contentHeight / totalH * sbH));
        int thumbY = sbY + (int) ((float) scrollOffset / maxScroll * (sbH - thumbH));
        guiGraphics.fill(sbX, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + thumbH, COLOR_TEXT_SECONDARY);
    }

    // ── Input ──────────────────────────────────────────────────────────

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isInContentArea(mouseX, mouseY)) {
            scrollOffset -= scrollY * 20;
            scrollOffset = Math.max(0, scrollOffset);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInTab(mouseX, mouseY, 0)) {
            activeTab = TAB_INFO;
            scrollOffset = 0;
            return true;
        }

        if (isInTab(mouseX, mouseY, 1)) {
            activeTab = TAB_EFFECTS;
            scrollOffset = 0;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // N key toggles GUI close (same key as open)
        if (keyCode == InputConstants.KEY_N) {
            this.onClose();
            return true;
        }
        // ESC closes (handled by super, but make explicit)
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean isInContentArea(double mouseX, double mouseY) {
        return mouseX >= contentLeft
                && mouseX <= contentLeft + contentWidth + SCROLLBAR_WIDTH
                && mouseY >= contentTop
                && mouseY <= contentTop + contentHeight;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
