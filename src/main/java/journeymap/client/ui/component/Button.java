package journeymap.client.ui.component;

import java.awt.geom.*;
import java.util.function.*;
import java.awt.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.*;
import net.minecraft.client.audio.*;
import net.minecraft.client.renderer.*;
import journeymap.client.render.draw.*;
import journeymap.common.*;
import journeymap.common.log.*;
import java.util.*;
import net.minecraft.util.text.*;
import journeymap.client.*;

public class Button extends GuiButton implements ScrollPane.Scrollable
{
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
        this.clickListeners = new ArrayList<Function<Button, Boolean>>(0);
        this.finishInit();
    }
    
    public void resetLabelColors() {
        this.labelColor = 14737632;
        this.hoverLabelColor = 16777120;
        this.disabledLabelColor = Color.lightGray.getRGB();
    }
    
    protected void finishInit() {
        this.setEnabled(true);
        this.setDrawButton(true);
        this.setDrawFrame(true);
        this.setDrawBackground(true);
        if (this.field_146121_g == 0) {
            this.setHeight(20);
        }
        if (this.field_146120_f == 0) {
            this.func_175211_a(200);
        }
        this.updateBounds();
    }
    
    protected void updateLabel() {
    }
    
    public boolean isActive() {
        return this.isEnabled();
    }
    
    public int getFitWidth(final FontRenderer fr) {
        final int max = fr.func_78256_a(this.field_146126_j);
        return max + this.WIDTH_PAD + (fr.func_78260_a() ? ((int)Math.ceil(max * 0.25)) : 0);
    }
    
    public void fitWidth(final FontRenderer fr) {
        this.func_175211_a(this.getFitWidth(fr));
    }
    
    public void drawPartialScrollable(final Minecraft minecraft, final int x, final int y, final int width, final int height) {
        minecraft.func_110434_K().func_110577_a(Button.field_146122_a);
        final int k = 0;
        this.func_73729_b(x, y, 0, 46 + k * 20, width / 2, height);
        this.func_73729_b(x + width / 2, y, 200 - width / 2, 46 + k * 20, width / 2, height);
    }
    
    public void showDisabledOnHover(final boolean show) {
        this.showDisabledHoverText = show;
    }
    
    public boolean func_146115_a() {
        return super.func_146115_a();
    }
    
    public void setMouseOver(final boolean hover) {
        this.setHovered(hover);
    }
    
    public void func_146113_a(final SoundHandler soundHandler) {
        if (this.isEnabled()) {
            super.func_146113_a(soundHandler);
        }
    }
    
    public void func_191745_a(final Minecraft minecraft, final int mouseX, final int mouseY, final float partialTicks) {
        if (!this.isVisible()) {
            return;
        }
        if (this.defaultStyle) {
            super.func_191745_a(minecraft, mouseX, mouseY, partialTicks);
        }
        else {
            minecraft.func_110434_K().func_110577_a(Button.field_146122_a);
            GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
            this.setHovered(mouseX >= this.field_146128_h && mouseY >= this.field_146129_i && mouseX < this.field_146128_h + this.field_146120_f && mouseY < this.field_146129_i + this.field_146121_g);
            final int hoverState = this.func_146114_a(this.isHovered());
            if (this.isDrawFrame()) {
                DrawUtil.drawRectangle(this.field_146128_h, this.field_146129_i, this.field_146120_f, 1.0, this.customFrameColorLight, 1.0f);
                DrawUtil.drawRectangle(this.field_146128_h, this.field_146129_i, 1.0, this.field_146121_g, this.customFrameColorLight, 1.0f);
                DrawUtil.drawRectangle(this.field_146128_h, this.field_146129_i + this.field_146121_g - 1, this.field_146120_f - 1, 1.0, this.customFrameColorDark, 1.0f);
                DrawUtil.drawRectangle(this.field_146128_h + this.field_146120_f - 1, this.field_146129_i + 1, 1.0, this.field_146121_g - 1, this.customFrameColorDark, 1.0f);
            }
            if (this.isDrawBackground()) {
                DrawUtil.drawRectangle(this.field_146128_h + 1, this.field_146129_i + 1, this.field_146120_f - 2, this.field_146121_g - 2, (hoverState == 2) ? this.customBgHoverColor : this.customBgColor, 1.0f);
            }
            else if (this.isEnabled() && this.isHovered()) {
                DrawUtil.drawRectangle(this.field_146128_h + 1, this.field_146129_i + 1, this.field_146120_f - 2, this.field_146121_g - 2, this.customBgHoverColor2, 0.5f);
            }
            this.func_146119_b(minecraft, mouseX, mouseY);
            Integer varLabelColor = this.labelColor;
            if (!this.isEnabled()) {
                varLabelColor = this.disabledLabelColor;
                if (this.drawBackground) {
                    final float alpha = 0.7f;
                    final int widthOffset = this.field_146120_f - ((this.field_146121_g >= 20) ? 3 : 2);
                    DrawUtil.drawRectangle(this.getX() + 1, this.getY() + 1, widthOffset, this.field_146121_g - 2, this.disabledBgColor, alpha);
                }
            }
            else if (this.isHovered()) {
                varLabelColor = this.hoverLabelColor;
            }
            else if (this.labelColor != null) {
                varLabelColor = this.labelColor;
            }
            else if (this.packedFGColour != 0) {
                varLabelColor = this.packedFGColour;
            }
            DrawUtil.drawCenteredLabel(this.field_146126_j, this.getCenterX(), this.getMiddleY(), null, 0.0f, varLabelColor, 1.0f, 1.0, this.drawLabelShadow);
        }
    }
    
    public void drawCenteredString(final FontRenderer fontRenderer, final String text, final float x, final float y, final int color) {
        fontRenderer.func_175063_a(text, x - fontRenderer.func_78256_a(text) / 2, y, color);
    }
    
    public void drawUnderline() {
        if (this.isVisible()) {
            DrawUtil.drawRectangle(this.field_146128_h, this.field_146129_i + this.field_146121_g, this.field_146120_f, 1.0, this.customFrameColorDark, 1.0f);
        }
    }
    
    public void secondaryDrawButton() {
    }
    
    public boolean func_146116_c(final Minecraft minecraft, final int mouseX, final int mouseY) {
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
            }
            catch (Throwable t) {
                Journeymap.getLogger().error("Error trying to toggle button '" + this.field_146126_j + "': " + LogFormatter.toString(t));
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
                list.addAll(JmUI.fontRenderer().func_78271_c(line, 200));
            }
        }
        if (!this.isEnabled() && this.showDisabledHoverText) {
            list.add(TextFormatting.RED + Constants.getString("jm.common.disabled_feature"));
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
        return this.field_146120_f;
    }
    
    public void func_175211_a(final int width) {
        if (this.field_146120_f != width) {
            this.field_146120_f = width;
            this.bounds = null;
        }
    }
    
    public void setScrollableWidth(final int width) {
        this.func_175211_a(width);
    }
    
    public int getHeight() {
        return this.field_146121_g;
    }
    
    public void setHeight(final int height) {
        if (this.field_146121_g != height) {
            this.field_146121_g = height;
            this.bounds = null;
            if (height != 20) {
                this.defaultStyle = false;
            }
        }
    }
    
    public void setTextOnly(final FontRenderer fr) {
        this.setHeight(fr.field_78288_b + 1);
        this.fitWidth(fr);
        this.setDrawBackground(false);
        this.setDrawFrame(false);
    }
    
    public void drawScrollable(final Minecraft mc, final int mouseX, final int mouseY) {
        this.func_191745_a(mc, mouseX, mouseY, 0.0f);
    }
    
    public void clickScrollable(final Minecraft mc, final int mouseX, final int mouseY) {
    }
    
    public int getX() {
        return this.field_146128_h;
    }
    
    public void setX(final int x) {
        if (this.field_146128_h != x) {
            this.field_146128_h = x;
            this.bounds = null;
        }
    }
    
    public int getY() {
        return this.field_146129_i;
    }
    
    public void setY(final int y) {
        if (this.field_146129_i != y) {
            this.field_146129_i = y;
            this.bounds = null;
        }
    }
    
    public int getCenterX() {
        return this.field_146128_h + this.field_146120_f / 2;
    }
    
    public int getMiddleY() {
        return this.field_146129_i + this.field_146121_g / 2;
    }
    
    public int getBottomY() {
        return this.field_146129_i + this.field_146121_g;
    }
    
    public int getRightX() {
        return this.field_146128_h + this.field_146120_f;
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
        this.setX(x - this.field_146120_f / 2);
        return this;
    }
    
    public Button centerVerticalOn(final int y) {
        this.setY(y - this.field_146121_g / 2);
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
        return super.field_146124_l;
    }
    
    public void setEnabled(final boolean enabled) {
        super.field_146124_l = enabled;
    }
    
    public boolean isVisible() {
        return this.field_146125_m;
    }
    
    public void setDrawButton(final boolean drawButton) {
        if (drawButton != this.field_146125_m) {
            this.field_146125_m = drawButton;
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
        return super.field_146123_n;
    }
    
    public void setHovered(final boolean hovered) {
        super.field_146123_n = hovered;
    }
    
    public void addClickListener(final Function<Button, Boolean> listener) {
        this.clickListeners.add(listener);
    }
}
