package journeymap.client.ui.theme;

import journeymap.client.render.texture.*;
import net.minecraft.client.*;
import journeymap.client.render.draw.*;
import net.minecraft.client.gui.*;
import journeymap.client.ui.component.*;
import java.util.*;

public class ThemeToolbar extends Button
{
    private final ButtonList buttonList;
    private Theme theme;
    private Theme.Container.Toolbar.ToolbarSpec toolbarSpec;
    private TextureImpl textureBegin;
    private TextureImpl textureInner;
    private TextureImpl textureEnd;
    
    public ThemeToolbar(final Theme theme, final Button... buttons) {
        this(theme, new ButtonList(buttons));
    }
    
    public ThemeToolbar(final Theme theme, final ButtonList buttonList) {
        super(0, 0, "");
        this.buttonList = buttonList;
        this.updateTheme(theme);
    }
    
    public void updateTheme(final Theme theme) {
        this.theme = theme;
        this.updateTextures();
    }
    
    public Theme.Container.Toolbar.ToolbarSpec updateTextures() {
        Theme.Container.Toolbar.ToolbarSpec toolbarSpec;
        if (this.buttonList.isHorizontal()) {
            toolbarSpec = this.theme.container.toolbar.horizontal;
            this.func_175211_a(toolbarSpec.begin.width + toolbarSpec.inner.width * this.buttonList.getVisibleButtonCount() + toolbarSpec.end.width);
            this.setHeight(toolbarSpec.inner.height);
        }
        else {
            toolbarSpec = this.theme.container.toolbar.vertical;
            this.func_175211_a(toolbarSpec.inner.width);
            this.setHeight(toolbarSpec.begin.height + toolbarSpec.inner.height * this.buttonList.getVisibleButtonCount() + toolbarSpec.end.height);
        }
        if (this.toolbarSpec == null || toolbarSpec != this.toolbarSpec) {
            this.toolbarSpec = toolbarSpec;
            if (toolbarSpec.useThemeImages) {
                final String pathPattern = "container/" + toolbarSpec.prefix + "toolbar_%s.png";
                this.textureBegin = TextureCache.getThemeTexture(this.theme, String.format(pathPattern, "begin"));
                this.textureInner = TextureCache.getThemeTexture(this.theme, String.format(pathPattern, "inner"));
                this.textureEnd = TextureCache.getThemeTexture(this.theme, String.format(pathPattern, "end"));
            }
        }
        return this.toolbarSpec;
    }
    
    public void updateLayout() {
        this.updateTextures();
        final boolean isHorizontal = this.buttonList.isHorizontal();
        int drawX;
        int drawY;
        if (isHorizontal) {
            drawX = this.buttonList.getLeftX() - (this.field_146120_f - this.buttonList.getWidth(this.toolbarSpec.padding)) / 2;
            drawY = this.buttonList.getTopY() - (this.field_146121_g - this.theme.control.button.height) / 2;
        }
        else {
            drawX = this.buttonList.getLeftX() - (this.toolbarSpec.inner.width - this.theme.control.button.width) / 2;
            drawY = this.buttonList.getTopY() - (this.field_146121_g - this.buttonList.getHeight(this.toolbarSpec.padding)) / 2;
        }
        this.setPosition(drawX, drawY);
    }
    
    public Theme.Container.Toolbar.ToolbarSpec getToolbarSpec() {
        return this.toolbarSpec;
    }
    
    private ButtonList getButtonList() {
        return this.buttonList;
    }
    
    public boolean contains(final GuiButton button) {
        return this.buttonList.contains(button);
    }
    
    public <B extends Button> void add(final B... buttons) {
        this.buttonList.addAll(Arrays.asList(buttons));
    }
    
    public int getVMargin() {
        if (this.buttonList.isHorizontal()) {
            final int heightDiff = (this.toolbarSpec.inner.height - this.theme.control.button.height) / 2;
            return heightDiff + this.toolbarSpec.margin;
        }
        return this.toolbarSpec.margin;
    }
    
    public int getHMargin() {
        if (this.buttonList.isHorizontal()) {
            return this.toolbarSpec.begin.width + this.toolbarSpec.margin;
        }
        final int widthDiff = (this.toolbarSpec.inner.width - this.theme.control.button.width) / 2;
        return widthDiff + this.toolbarSpec.margin;
    }
    
    public void setDrawToolbar(final boolean draw) {
        super.setDrawButton(draw);
        for (final Button button : this.buttonList) {
            button.setDrawButton(draw);
        }
    }
    
    @Override
    public void func_191745_a(final Minecraft minecraft, final int mouseX, final int mouseY, final float f) {
        if (!this.field_146125_m) {
            return;
        }
        final boolean isHorizontal = this.buttonList.isHorizontal();
        double drawX = this.getX();
        double drawY = this.getY();
        if (!this.toolbarSpec.useThemeImages) {
            return;
        }
        if (this.field_146125_m) {
            float scale = 1.0f;
            if (this.toolbarSpec.begin.width > 0 && this.toolbarSpec.begin.height > 0) {
                if (this.toolbarSpec.begin.width != this.textureBegin.getWidth()) {
                    scale = 1.0f * this.toolbarSpec.begin.width / this.textureBegin.getWidth();
                }
                DrawUtil.drawClampedImage(this.textureBegin, this.toolbarSpec.begin.getColor(), this.toolbarSpec.begin.alpha, drawX, drawY, scale, 0.0);
            }
            if (isHorizontal) {
                drawX += this.toolbarSpec.begin.width;
            }
            else {
                drawY += this.toolbarSpec.begin.height;
            }
            scale = 1.0f;
            if (this.toolbarSpec.inner.width != this.textureInner.getWidth()) {
                scale = 1.0f * this.toolbarSpec.inner.width / this.textureInner.getWidth();
            }
            for (final Button button : this.buttonList) {
                if (button.isVisible()) {
                    DrawUtil.drawClampedImage(this.textureInner, this.toolbarSpec.inner.getColor(), this.toolbarSpec.inner.alpha, drawX, drawY, scale, 0.0);
                    if (isHorizontal) {
                        drawX += this.toolbarSpec.inner.width;
                    }
                    else {
                        drawY += this.toolbarSpec.inner.height;
                    }
                }
            }
            if (this.toolbarSpec.end.width > 0 && this.toolbarSpec.end.height > 0) {
                scale = 1.0f;
                if (this.toolbarSpec.end.width != this.textureEnd.getWidth()) {
                    scale = 1.0f * this.toolbarSpec.end.width / this.textureEnd.getWidth();
                }
                DrawUtil.drawClampedImage(this.textureEnd, this.toolbarSpec.end.getColor(), this.toolbarSpec.end.alpha, drawX, drawY, scale, 0.0);
            }
        }
    }
    
    @Override
    public int getCenterX() {
        return this.field_146128_h + this.field_146120_f / 2;
    }
    
    @Override
    public int getMiddleY() {
        return this.field_146129_i + this.field_146121_g / 2;
    }
    
    @Override
    public int getBottomY() {
        return this.field_146129_i + this.field_146121_g;
    }
    
    @Override
    public int getRightX() {
        return this.field_146128_h + this.field_146120_f;
    }
    
    @Override
    public ArrayList<String> getTooltip() {
        return null;
    }
    
    public ButtonList layoutHorizontal(final int startX, final int y, final boolean leftToRight, final int hgap) {
        this.buttonList.layoutHorizontal(startX, y, leftToRight, hgap);
        this.updateLayout();
        return this.buttonList;
    }
    
    public ButtonList layoutCenteredVertical(final int x, final int centerY, final boolean leftToRight, final int vgap) {
        this.buttonList.layoutCenteredVertical(x, centerY, leftToRight, vgap);
        this.updateLayout();
        return this.buttonList;
    }
    
    public ButtonList layoutVertical(final int x, final int startY, final boolean leftToRight, final int vgap) {
        this.buttonList.layoutVertical(x, startY, leftToRight, vgap);
        this.updateLayout();
        return this.buttonList;
    }
    
    public ButtonList layoutCenteredHorizontal(final int centerX, final int y, final boolean leftToRight, final int hgap) {
        this.buttonList.layoutCenteredHorizontal(centerX, y, leftToRight, hgap);
        this.updateLayout();
        return this.buttonList;
    }
    
    public ButtonList layoutDistributedHorizontal(final int leftX, final int y, final int rightX, final boolean leftToRight) {
        this.buttonList.layoutDistributedHorizontal(leftX, y, rightX, leftToRight);
        this.updateLayout();
        return this.buttonList;
    }
    
    public ButtonList layoutFilledHorizontal(final FontRenderer fr, final int leftX, final int y, final int rightX, final int hgap, final boolean leftToRight) {
        this.buttonList.layoutFilledHorizontal(fr, leftX, y, rightX, hgap, leftToRight);
        this.updateLayout();
        return this.buttonList;
    }
    
    public void setLayout(final ButtonList.Layout layout, final ButtonList.Direction direction) {
        this.buttonList.setLayout(layout, direction);
        this.updateLayout();
    }
    
    public ButtonList reverse() {
        this.buttonList.reverse();
        this.updateLayout();
        return this.buttonList;
    }
    
    public void addAllButtons(final JmUI gui) {
        gui.getButtonList().add(this);
        gui.getButtonList().addAll(this.buttonList);
    }
}
