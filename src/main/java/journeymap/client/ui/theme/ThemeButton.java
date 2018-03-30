package journeymap.client.ui.theme;

import journeymap.client.Constants;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.component.BooleanPropertyButton;
import journeymap.common.properties.config.BooleanField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class ThemeButton extends BooleanPropertyButton {
    protected Theme theme;
    protected Theme.Control.ButtonSpec buttonSpec;
    protected TextureImpl textureOn;
    protected TextureImpl textureHover;
    protected TextureImpl textureOff;
    protected TextureImpl textureDisabled;
    protected TextureImpl textureIcon;
    protected String iconName;
    protected List<String> additionalTooltips;
    protected boolean staysOn;

    public ThemeButton(final Theme theme, final String rawLabel, final String iconName) {
        this(theme, Constants.getString(rawLabel), Constants.getString(rawLabel), false, iconName);
    }

    public ThemeButton(final Theme theme, final String labelOn, final String labelOff, final boolean toggled, final String iconName) {
        super(labelOn, labelOff, null);
        this.iconName = iconName;
        this.setToggled(toggled);
        this.updateTheme(theme);
    }

    protected ThemeButton(final Theme theme, final String labelOn, final String labelOff, final String iconName, final BooleanField field) {
        super(labelOn, labelOff, field);
        this.iconName = iconName;
        this.updateTheme(theme);
    }

    public boolean isStaysOn() {
        return this.staysOn;
    }

    public void setStaysOn(final boolean staysOn) {
        this.staysOn = staysOn;
    }

    public void updateTheme(final Theme theme) {
        this.theme = theme;
        this.buttonSpec = this.getButtonSpec(theme);
        if (this.buttonSpec.useThemeImages) {
            final String pattern = this.getPathPattern();
            final String prefix = this.buttonSpec.prefix;
            this.textureOn = TextureCache.getThemeTexture(theme, String.format(pattern, prefix, "on"));
            this.textureOff = TextureCache.getThemeTexture(theme, String.format(pattern, prefix, "off"));
            this.textureHover = TextureCache.getThemeTexture(theme, String.format(pattern, prefix, "hover"));
            this.textureDisabled = TextureCache.getThemeTexture(theme, String.format(pattern, prefix, "disabled"));
        } else {
            this.textureOn = null;
            this.textureOff = null;
            this.textureHover = null;
            this.textureDisabled = null;
        }
        this.textureIcon = TextureCache.getThemeTexture(theme, String.format("icon/%s.png", this.iconName));
        this.setWidth(this.buttonSpec.width);
        this.setHeight(this.buttonSpec.height);
        this.setToggled(false, false);
    }

    public boolean hasValidTextures() {
        return !this.buttonSpec.useThemeImages || (GL11.glIsTexture(this.textureOn.getGlTextureId(false)) && GL11.glIsTexture(this.textureOff.getGlTextureId(false)));
    }

    protected String getPathPattern() {
        return "control/%sbutton_%s.png";
    }

    protected Theme.Control.ButtonSpec getButtonSpec(final Theme theme) {
        return theme.control.button;
    }

    public Theme.Control.ButtonSpec getButtonSpec() {
        return this.buttonSpec;
    }

    protected TextureImpl getActiveTexture(final boolean isMouseOver) {
        if (!this.isEnabled()) {
            return this.textureDisabled;
        }
        return this.toggled ? this.textureOn : this.textureOff;
    }

    protected Theme.ColorSpec getIconColor(final boolean isMouseOver) {
        if (!this.isEnabled()) {
            return this.buttonSpec.iconDisabled;
        }
        if (isMouseOver) {
            return this.toggled ? this.buttonSpec.iconHoverOn : this.buttonSpec.iconHoverOff;
        }
        return this.toggled ? this.buttonSpec.iconOn : this.buttonSpec.iconOff;
    }

    protected Theme.ColorSpec getButtonColor(final boolean isMouseOver) {
        if (!this.isEnabled()) {
            return this.buttonSpec.buttonDisabled;
        }
        if (isMouseOver) {
            return this.toggled ? this.buttonSpec.buttonHoverOn : this.buttonSpec.buttonHoverOff;
        }
        return this.toggled ? this.buttonSpec.buttonOn : this.buttonSpec.buttonOff;
    }

    @Override
    public void drawButton(final Minecraft minecraft, final int mouseX, final int mouseY, final float ticks) {
        if (!this.isVisible()) {
            return;
        }
        final boolean hover = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        this.setMouseOver(hover);
        final int hoverState = this.getHoverState(hover);
        final boolean isMouseOver = hoverState == 2;
        final TextureImpl activeTexture = this.getActiveTexture(isMouseOver);
        final Theme.ColorSpec iconColorSpec = this.getIconColor(isMouseOver);
        final int drawX = this.getX();
        final int drawY = this.getY();
        if (this.buttonSpec.useThemeImages) {
            final Theme.ColorSpec buttonColorSpec = this.getButtonColor(isMouseOver);
            float buttonScale = 1.0f;
            if (this.buttonSpec.width != activeTexture.getWidth()) {
                buttonScale = 1.0f * this.buttonSpec.width / activeTexture.getWidth();
            }
            DrawUtil.drawColoredImage(activeTexture, buttonColorSpec.getColor(), buttonColorSpec.alpha, drawX, drawY, buttonScale, 0.0);
        } else {
            this.drawNativeButton(minecraft, mouseX, mouseY);
        }
        float iconScale = 1.0f;
        if (this.theme.icon.width != this.textureIcon.getWidth()) {
            iconScale = 1.0f * this.theme.icon.width / this.textureIcon.getWidth();
        }
        if (!this.buttonSpec.useThemeImages) {
            DrawUtil.drawColoredImage(this.textureIcon, 0, iconColorSpec.alpha, drawX + 0.5, drawY + 0.5, iconScale, 0.0);
        }
        DrawUtil.drawColoredImage(this.textureIcon, iconColorSpec.getColor(), iconColorSpec.alpha, drawX, drawY, iconScale, 0.0);
    }

    public void drawNativeButton(final Minecraft minecraft, final int mouseX, final int mouseY) {
        final int magic = 20;
        minecraft.getTextureManager().bindTexture(ThemeButton.BUTTON_TEXTURES);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        final int k = this.getHoverState(this.isMouseOver());
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        this.drawTexturedModalRect(this.x, this.y, 0, 46 + k * magic, this.width / 2, this.height);
        this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + k * magic, this.width / 2, this.height);
        this.mouseDragged(minecraft, mouseX, mouseY);
        final int l = 14737632;
    }

    public void setAdditionalTooltips(final List<String> additionalTooltips) {
        this.additionalTooltips = additionalTooltips;
    }

    @Override
    public List<String> getTooltip() {
        if (!this.visible) {
            return null;
        }
        final List<String> list = super.getTooltip();
        String style;
        if (!this.isEnabled()) {
            style = this.buttonSpec.tooltipDisabledStyle;
        } else {
            style = (this.toggled ? this.buttonSpec.tooltipOnStyle : this.buttonSpec.tooltipOffStyle);
        }
        list.add(0, style + this.displayString);
        if (this.additionalTooltips != null) {
            list.addAll(this.additionalTooltips);
        }
        return list;
    }
}
