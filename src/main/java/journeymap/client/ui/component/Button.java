package journeymap.client.ui.component;

import journeymap.client.Constants;
import journeymap.client.render.draw.DrawUtil;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Button extends GuiButton implements ScrollPane.Scrollable {
    protected Integer customFrameColorLight;
    protected Integer customFrameColorDark;
    protected Integer customBgColor;
    protected Integer customBgHoverColor;
    protected Integer customBgHoverColor2;
    protected Integer labelColor;
    protected Integer hoverLabelColor;
    protected Integer disabledLabelColor;
    protected Integer disabledBgColor;
    protected boolean drawFrame;
    protected boolean drawBackground;
    protected boolean drawLabelShadow;
    protected boolean showDisabledHoverText;
    protected boolean defaultStyle;
    protected int WIDTH_PAD;
    protected String[] tooltip;
    protected FontRenderer fontRenderer;
    protected Rectangle2D.Double bounds;
    protected ArrayList<Function<Button, Boolean>> clickListeners;

    public Button(final String label) {
        this(0, 0, label);
        this.resetLabelColors();
    }

    public Button(final int width, final int height, final String label) {
        super(0, 0, 0, width, height, label);
        this.customFrameColorLight = new Color(160, 160, 160).getRGB();
        this.customFrameColorDark = new Color(120, 120, 120).getRGB();
        this.customBgColor = new Color(100, 100, 100).getRGB();
        this.customBgHoverColor = new Color(125, 135, 190).getRGB();
        this.customBgHoverColor2 = new Color(100, 100, 100).getRGB();
        this.disabledBgColor = Color.darkGray.getRGB();
        this.drawLabelShadow = true;
        this.defaultStyle = true;
        this.WIDTH_PAD = 12;
        this.fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
        this.clickListeners = new ArrayList<Function<Button, Boolean>>(0);
        this.finishInit();
    }

    public void resetLabelColors() {
        this.labelColor = new Color(14737632).getRGB();
        this.hoverLabelColor = new Color(16777120).getRGB();
        this.disabledLabelColor = Color.lightGray.getRGB();
    }

    protected void finishInit() {
        this.setEnabled(true);
        this.setDrawButton(true);
        this.setDrawFrame(true);
        this.setDrawBackground(true);
        if (this.height == 0) {
            this.setHeight(20);
        }
        if (this.width == 0) {
            this.setWidth(200);
        }
        this.updateBounds();
    }

    protected void updateLabel() {
    }

    public boolean isActive() {
        return this.isEnabled();
    }

    public int getFitWidth(final FontRenderer fr) {
        final int max = fr.getStringWidth(this.displayString);
        return max + this.WIDTH_PAD + (fr.getBidiFlag() ? ((int) Math.ceil(max * 0.25)) : 0);
    }

    public void fitWidth(final FontRenderer fr) {
        this.setWidth(this.getFitWidth(fr));
    }

    public void drawPartialScrollable(final Minecraft minecraft, final int x, final int y, final int width, final int height) {
        minecraft.getTextureManager().bindTexture(Button.BUTTON_TEXTURES);
        final int k = 0;
        this.drawTexturedModalRect(x, y, 0, 46 + k * 20, width / 2, height);
        this.drawTexturedModalRect(x + width / 2, y, 200 - width / 2, 46 + k * 20, width / 2, height);
    }

    public void showDisabledOnHover(final boolean show) {
        this.showDisabledHoverText = show;
    }

    public boolean isMouseOver() {
        return super.isMouseOver();
    }

    public void setMouseOver(final boolean hover) {
        this.setHovered(hover);
    }

    public void playPressSound(final SoundHandler soundHandler) {
        if (this.isEnabled()) {
            super.playPressSound(soundHandler);
        }
    }

    public void drawButton(final Minecraft minecraft, final int mouseX, final int mouseY, final float partialTicks) {
        if (!this.isVisible()) {
            return;
        }
        if (this.defaultStyle) {
            super.drawButton(minecraft, mouseX, mouseY, partialTicks);
        } else {
            minecraft.getTextureManager().bindTexture(Button.BUTTON_TEXTURES);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.setHovered(mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height);
            final int hoverState = this.getHoverState(this.isHovered());
            if (this.isDrawFrame()) {
                DrawUtil.drawRectangle(this.x, this.y, this.width, 1.0, this.customFrameColorLight, 1.0f);
                DrawUtil.drawRectangle(this.x, this.y, 1.0, this.height, this.customFrameColorLight, 1.0f);
                DrawUtil.drawRectangle(this.x, this.y + this.height - 1, this.width - 1, 1.0, this.customFrameColorDark, 1.0f);
                DrawUtil.drawRectangle(this.x + this.width - 1, this.y + 1, 1.0, this.height - 1, this.customFrameColorDark, 1.0f);
            }
            if (this.isDrawBackground()) {
                DrawUtil.drawRectangle(this.x + 1, this.y + 1, this.width - 2, this.height - 2, (hoverState == 2) ? this.customBgHoverColor : this.customBgColor, 1.0f);
            } else if (this.isEnabled() && this.isHovered()) {
                DrawUtil.drawRectangle(this.x + 1, this.y + 1, this.width - 2, this.height - 2, this.customBgHoverColor2, 0.5f);
            }
            this.mouseDragged(minecraft, mouseX, mouseY);
            Integer varLabelColor = this.labelColor;
            if (!this.isEnabled()) {
                varLabelColor = this.disabledLabelColor;
                if (this.drawBackground) {
                    final float alpha = 0.7f;
                    final int widthOffset = this.width - ((this.height >= 20) ? 3 : 2);
                    DrawUtil.drawRectangle(this.getX() + 1, this.getY() + 1, widthOffset, this.height - 2, this.disabledBgColor, alpha);
                }
            } else if (this.isHovered()) {
                varLabelColor = this.hoverLabelColor;
            } else if (this.labelColor != null) {
                varLabelColor = this.labelColor;
            } else if (this.packedFGColour != 0) {
                varLabelColor = this.packedFGColour;
            }
            DrawUtil.drawCenteredLabel(this.displayString, this.getCenterX(), this.getMiddleY(), null, 0.0f, varLabelColor, 1.0f, 1.0, this.drawLabelShadow);
        }
    }

    public void drawCenteredString(final FontRenderer fontRenderer, final String text, final float x, final float y, final int color) {
        fontRenderer.drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2, y, color);
    }

    public void drawUnderline() {
        if (this.isVisible()) {
            DrawUtil.drawRectangle(this.x, this.y + this.height, this.width, 1.0, this.customFrameColorDark, 1.0f);
        }
    }

    public void secondaryDrawButton() {
    }

    public boolean mousePressed(final Minecraft minecraft, final int mouseX, final int mouseY) {
        return this.mousePressed(minecraft, mouseX, mouseY, true);
    }

    public boolean mousePressed(final Minecraft minecraft, final int mouseX, final int mouseY, final boolean checkClickListeners) {
        final boolean clicked = this.isEnabled() && this.isVisible() && this.mouseOver(mouseX, mouseY);
        return clicked && this.checkClickListeners();
    }

    public boolean checkClickListeners() {
        boolean clicked = true;
        if (!this.clickListeners.isEmpty()) {
            try {
                for (final Function<Button, Boolean> listener : this.clickListeners) {
                    if (!listener.apply(this)) {
                        break;
                    }
                }
            } catch (Throwable t) {
                Journeymap.getLogger().error("Error trying to toggle button '" + this.displayString + "': " + LogFormatter.toString(t));
                clicked = false;
            }
        }
        return clicked;
    }

    public String getUnformattedTooltip() {
        if (this.tooltip != null && this.tooltip.length > 0) {
            return this.tooltip[0];
        }
        return null;
    }

    public List<String> getTooltip() {
        final ArrayList<String> list = new ArrayList<String>();
        if (this.tooltip != null) {
            for (final String line : this.tooltip) {
                list.addAll(this.fontRenderer.listFormattedStringToWidth(line, 200));
            }
            return list;
        }
        if (!this.isEnabled() && this.showDisabledHoverText) {
            list.add(TextFormatting.ITALIC + Constants.getString("jm.common.disabled_feature"));
        }
        return list;
    }

    public void setTooltip(final String... tooltip) {
        this.tooltip = tooltip;
    }

    public boolean mouseOver(final int mouseX, final int mouseY) {
        return this.isVisible() && this.getBounds().contains(mouseX, mouseY);
    }

    protected Rectangle2D.Double updateBounds() {
        return this.bounds = new Rectangle2D.Double(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    public Rectangle2D.Double getBounds() {
        if (this.bounds == null) {
            return this.updateBounds();
        }
        return this.bounds;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(final int width) {
        if (this.width != width) {
            this.width = width;
            this.bounds = null;
        }
    }

    public void setScrollableWidth(final int width) {
        this.setWidth(width);
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(final int height) {
        if (this.height != height) {
            this.height = height;
            this.bounds = null;
            if (height != 20) {
                this.defaultStyle = false;
            }
        }
    }

    public void setTextOnly(final FontRenderer fr) {
        this.setHeight(fr.FONT_HEIGHT + 1);
        this.fitWidth(fr);
        this.setDrawBackground(false);
        this.setDrawFrame(false);
    }

    public void drawScrollable(final Minecraft mc, final int mouseX, final int mouseY) {
        this.drawButton(mc, mouseX, mouseY, 0.0f);
    }

    public void clickScrollable(final Minecraft mc, final int mouseX, final int mouseY) {
    }

    public int getX() {
        return this.x;
    }

    public void setX(final int x) {
        if (this.x != x) {
            this.x = x;
            this.bounds = null;
        }
    }

    public int getY() {
        return this.y;
    }

    public void setY(final int y) {
        if (this.y != y) {
            this.y = y;
            this.bounds = null;
        }
    }

    public int getCenterX() {
        return this.x + this.width / 2;
    }

    public int getMiddleY() {
        return this.y + this.height / 2;
    }

    public int getBottomY() {
        return this.y + this.height;
    }

    public int getRightX() {
        return this.x + this.width;
    }

    public void setPosition(final int x, final int y) {
        this.setX(x);
        this.setY(y);
    }

    public Button leftOf(final int x) {
        this.setX(x - this.getWidth());
        return this;
    }

    public Button rightOf(final int x) {
        this.setX(x);
        return this;
    }

    public Button centerHorizontalOn(final int x) {
        this.setX(x - this.width / 2);
        return this;
    }

    public Button centerVerticalOn(final int y) {
        this.setY(y - this.height / 2);
        return this;
    }

    public Button leftOf(final Button other, final int margin) {
        this.setX(other.getX() - this.getWidth() - margin);
        return this;
    }

    public Button rightOf(final Button other, final int margin) {
        this.setX(other.getX() + other.getWidth() + margin);
        return this;
    }

    public Button above(final Button other, final int margin) {
        this.setY(other.getY() - this.getHeight() - margin);
        return this;
    }

    public Button above(final int y) {
        this.setY(y - this.getHeight());
        return this;
    }

    public Button below(final Button other, final int margin) {
        this.setY(other.getY() + other.getHeight() + margin);
        return this;
    }

    public Button below(final ButtonList list, final int margin) {
        this.setY(list.getBottomY() + margin);
        return this;
    }

    public Button below(final int y) {
        this.setY(y);
        return this;
    }

    public Button alignTo(final Button other, final DrawUtil.HAlign hAlign, final int hgap, final DrawUtil.VAlign vAlign, final int vgap) {
        int x = this.getX();
        int y = this.getY();
        switch (hAlign) {
            case Right: {
                x = other.getRightX() + hgap;
                break;
            }
            case Left: {
                x = other.getX() - hgap;
                break;
            }
            case Center: {
                x = other.getCenterX();
                break;
            }
        }
        switch (vAlign) {
            case Above: {
                y = other.getY() - vgap - this.getHeight();
                break;
            }
            case Below: {
                y = other.getBottomY() + vgap;
                break;
            }
            case Middle: {
                y = other.getMiddleY() - this.getHeight() / 2;
                break;
            }
        }
        this.setX(x);
        this.setY(y);
        return this;
    }

    public boolean isEnabled() {
        return super.enabled;
    }

    public void setEnabled(final boolean enabled) {
        super.enabled = enabled;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setDrawButton(final boolean drawButton) {
        if (drawButton != this.visible) {
            this.visible = drawButton;
        }
    }

    public boolean isDrawFrame() {
        return this.drawFrame;
    }

    public void setDrawFrame(final boolean drawFrame) {
        this.drawFrame = drawFrame;
    }

    public boolean isDrawBackground() {
        return this.drawBackground;
    }

    public void setDrawBackground(final boolean drawBackground) {
        this.drawBackground = drawBackground;
    }

    public boolean isDefaultStyle() {
        return this.defaultStyle;
    }

    public void setDefaultStyle(final boolean defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    public boolean keyTyped(final char c, final int i) {
        return false;
    }

    public void setBackgroundColors(final Integer customBgColor, final Integer customBgHoverColor, final Integer customBgHoverColor2) {
        this.customBgColor = customBgColor;
        this.customBgHoverColor = customBgHoverColor;
        this.customBgHoverColor2 = customBgHoverColor2;
    }

    public void setDrawLabelShadow(final boolean draw) {
        this.drawLabelShadow = draw;
    }

    public void setLabelColors(final Integer labelColor, final Integer hoverLabelColor, final Integer disabledLabelColor) {
        this.labelColor = labelColor;
        this.packedFGColour = labelColor;
        if (hoverLabelColor != null) {
            this.hoverLabelColor = hoverLabelColor;
        }
        if (disabledLabelColor != null) {
            this.disabledLabelColor = disabledLabelColor;
        }
    }

    public void refresh() {
    }

    public Integer getLabelColor() {
        return this.labelColor;
    }

    public boolean isHovered() {
        return super.hovered;
    }

    public void setHovered(final boolean hovered) {
        super.hovered = hovered;
    }

    public void addClickListener(final Function<Button, Boolean> listener) {
        this.clickListeners.add(listener);
    }
}
