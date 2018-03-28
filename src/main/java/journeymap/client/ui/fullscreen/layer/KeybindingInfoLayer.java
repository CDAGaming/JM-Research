package journeymap.client.ui.fullscreen.layer;

import journeymap.client.Constants;
import journeymap.client.forge.event.KeyEventHandler;
import journeymap.client.io.ThemeLoader;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.theme.Theme;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeybindingInfoLayer implements LayerDelegate.Layer {
    private final List<DrawStep> drawStepList;
    private final KeybindingInfoStep keybindingInfoStep;
    private final Fullscreen fullscreen;
    private final Minecraft mc;
    private FontRenderer fontRenderer;
    private FullMapProperties fullMapProperties;

    public KeybindingInfoLayer(final Fullscreen fullscreen) {
        this.drawStepList = new ArrayList<DrawStep>(1);
        this.fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
        this.fullMapProperties = Journeymap.getClient().getFullMapProperties();
        this.mc = FMLClientHandler.instance().getClient();
        this.fullscreen = fullscreen;
        this.keybindingInfoStep = new KeybindingInfoStep();
        this.drawStepList.add(this.keybindingInfoStep);
    }

    @Override
    public List<DrawStep> onMouseMove(final Minecraft mc, final GridRenderer gridRenderer, final Point2D.Double mousePosition, final BlockPos blockPos, final float fontScale, final boolean isScrolling) {
        if (this.fullMapProperties.showKeys.get()) {
            if (this.keybindingInfoStep.panelRect.contains(mousePosition)) {
                this.keybindingInfoStep.hide();
            } else {
                this.keybindingInfoStep.show();
            }
            return this.drawStepList;
        }
        return (List<DrawStep>) Collections.EMPTY_LIST;
    }

    @Override
    public List<DrawStep> onMouseClick(final Minecraft mc, final GridRenderer gridRenderer, final Point2D.Double mousePosition, final BlockPos blockCoord, final int button, final boolean doubleClick, final float fontScale) {
        return this.fullMapProperties.showKeys.get() ? this.drawStepList : Collections.EMPTY_LIST;
    }

    @Override
    public boolean propagateClick() {
        return true;
    }

    class KeybindingInfoStep implements DrawStep {
        Rectangle2D panelRect;
        Theme theme;
        Theme.LabelSpec statusLabelSpec;
        int bgColor;
        float fgAlphaDefault;
        float bgAlphaDefault;
        float fgAlpha;
        float bgAlpha;
        private double screenWidth;
        private double screenHeight;
        private double fontScale;
        private int pad;
        private ArrayList<Tuple<String, String>> lines;
        private int keyNameWidth;
        private int keyDescWidth;
        private int lineHeight;

        KeybindingInfoStep() {
            this.keyNameWidth = 0;
            this.keyDescWidth = 0;
            this.lineHeight = 0;
            this.panelRect = new Rectangle2D.Double();
            this.theme = ThemeLoader.getCurrentTheme();
            this.fgAlphaDefault = 1.0f;
            this.bgAlphaDefault = 0.7f;
            this.fgAlpha = this.fgAlphaDefault;
            this.bgAlpha = this.bgAlphaDefault;
        }

        @Override
        public void draw(final Pass pass, final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double fontScale, final double rotation) {
            if (pass == Pass.Text) {
                if (KeybindingInfoLayer.this.fullscreen.getMenuToolbarBounds() == null) {
                    return;
                }
                this.updateLayout(gridRenderer, fontScale);
                DrawUtil.drawRectangle(this.panelRect.getX(), this.panelRect.getY(), this.panelRect.getWidth(), this.panelRect.getHeight(), this.bgColor, this.bgAlpha);
                final int x = (int) this.panelRect.getX() + this.pad + this.keyNameWidth;
                int y = (int) this.panelRect.getY() + this.pad;
                final int firstColor = this.theme.fullscreen.statusLabel.highlight.getColor();
                final int secondColor = this.theme.fullscreen.statusLabel.foreground.getColor();
                try {
                    GlStateManager.enableBlend();
                    for (final Tuple<String, String> line : this.lines) {
                        DrawUtil.drawLabel((String) line.getFirst(), x, y, DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, null, 0.0f, firstColor, this.fgAlpha, fontScale, false);
                        DrawUtil.drawLabel((String) line.getSecond(), x + this.pad, y, DrawUtil.HAlign.Right, DrawUtil.VAlign.Middle, null, 0.0f, secondColor, this.fgAlpha, fontScale, false);
                        y += this.lineHeight;
                    }
                } finally {
                    GlStateManager.disableBlend();
                }
            }
        }

        @Override
        public int getDisplayOrder() {
            return 0;
        }

        @Override
        public String getModId() {
            return "journeymap";
        }

        void hide() {
            this.bgAlpha = 0.2f;
            this.fgAlpha = 0.2f;
        }

        void show() {
            this.bgAlpha = this.bgAlphaDefault;
            this.fgAlpha = this.fgAlphaDefault;
        }

        private void updateLayout(final GridRenderer gridRenderer, final double fontScale) {
            final Theme theme = ThemeLoader.getCurrentTheme();
            this.statusLabelSpec = theme.fullscreen.statusLabel;
            this.bgColor = this.statusLabelSpec.background.getColor();
            if (fontScale != this.fontScale || this.screenWidth != gridRenderer.getWidth() || this.screenHeight != gridRenderer.getHeight()) {
                this.screenWidth = gridRenderer.getWidth();
                this.screenHeight = gridRenderer.getHeight();
                this.fontScale = fontScale;
                this.pad = (int) (10.0 * fontScale);
                this.lineHeight = (int) (3.0 + fontScale * KeybindingInfoLayer.this.fontRenderer.FONT_HEIGHT);
                this.initLines(fontScale);
                final int panelWidth = this.keyNameWidth + this.keyDescWidth + 4 * this.pad;
                final int panelHeight = this.lines.size() * this.lineHeight + this.pad;
                final int scaleFactor = KeybindingInfoLayer.this.fullscreen.getScreenScaleFactor();
                final int panelX = (int) this.screenWidth - theme.container.toolbar.vertical.margin * scaleFactor - panelWidth;
                int panelY = (int) this.screenHeight - theme.container.toolbar.horizontal.margin * scaleFactor - panelHeight;
                this.panelRect.setRect(panelX, panelY, panelWidth, panelHeight);
                final Rectangle2D.Double menuToolbarRect = KeybindingInfoLayer.this.fullscreen.getMenuToolbarBounds();
                if (menuToolbarRect != null && menuToolbarRect.intersects(this.panelRect) && panelX <= menuToolbarRect.getMaxX()) {
                    panelY = (int) menuToolbarRect.getMinY() - 5 - panelHeight;
                    this.panelRect.setRect(panelX, panelY, panelWidth, panelHeight);
                }
            }
        }

        private void initLines(final double fontScale) {
            this.lines = new ArrayList<Tuple<String, String>>();
            this.keyDescWidth = 0;
            this.keyNameWidth = 0;
            this.bgAlpha = this.fgAlphaDefault;
            this.bgAlpha = this.bgAlphaDefault;
            for (final KeyBinding keyBinding : KeyEventHandler.INSTANCE.getInGuiKeybindings()) {
                this.initLine(keyBinding, fontScale);
            }
            this.initLine(KeybindingInfoLayer.this.mc.gameSettings.keyBindChat, fontScale);
        }

        private void initLine(final KeyBinding keyBinding, final double fontScale) {
            final String keyName = keyBinding.getDisplayName();
            final String keyDesc = Constants.getString(keyBinding.getKeyDescription());
            final Tuple<String, String> line = (Tuple<String, String>) new Tuple((Object) keyName, (Object) keyDesc);
            this.lines.add(line);
            this.keyNameWidth = (int) Math.max(this.keyNameWidth, fontScale * KeybindingInfoLayer.this.fontRenderer.getStringWidth(keyName));
            this.keyDescWidth = (int) Math.max(this.keyDescWidth, fontScale * KeybindingInfoLayer.this.fontRenderer.getStringWidth(keyDesc));
        }
    }
}
