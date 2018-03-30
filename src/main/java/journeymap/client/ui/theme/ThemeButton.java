package journeymap.client.ui.theme;

import journeymap.client.ui.component.*;
import journeymap.client.*;
import journeymap.common.properties.config.*;
import journeymap.client.render.texture.*;
import org.lwjgl.opengl.*;
import net.minecraft.client.*;
import journeymap.client.render.draw.*;
import net.minecraft.client.renderer.*;
import java.util.*;

public class ThemeButton extends BooleanPropertyButton
{
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
        }
        else {
            this.textureOn = null;
            this.textureOff = null;
            this.textureHover = null;
            this.textureDisabled = null;
        }
        this.textureIcon = TextureCache.getThemeTexture(theme, String.format("icon/%s.png", this.iconName));
        this.func_175211_a(this.buttonSpec.width);
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
    public void func_191745_a(final Minecraft minecraft, final int mouseX, final int mouseY, final float ticks) {
        if (!this.isVisible()) {
            return;
        }
        final boolean hover = mouseX >= this.field_146128_h && mouseY >= this.field_146129_i && mouseX < this.field_146128_h + this.field_146120_f && mouseY < this.field_146129_i + this.field_146121_g;
        this.setMouseOver(hover);
        final int hoverState = this.func_146114_a(hover);
        final boolean isMouseOver = hoverState == 2;
        final TextureImpl activeTexture = this.getActiveTexture(isMouseOver);
        final Theme.ColorSpec iconColorSpec = this.getIconColor(isMouseOver);
        final int drawX = this.getX();
        final int drawY = this.getY();
        if (this.buttonSpec.useThemeImages) {
            final Theme.ColorSpec buttonColorSpec = this.getButtonColor(isMouseOver);
            DrawUtil.drawQuad(activeTexture, buttonColorSpec.getColor(), buttonColorSpec.alpha, drawX, drawY, this.buttonSpec.width, this.buttonSpec.height, false, 0.0);
        }
        else {
            this.drawNativeButton(minecraft, mouseX, mouseY);
        }
        if (!this.buttonSpec.useThemeImages) {
            DrawUtil.drawQuad(this.textureIcon, 0, iconColorSpec.alpha, drawX + 0.5, drawY + 0.5, this.buttonSpec.width, this.buttonSpec.height, false, 0.0);
        }
        DrawUtil.drawQuad(this.textureIcon, iconColorSpec.getColor(), iconColorSpec.alpha, drawX, drawY, this.buttonSpec.width, this.buttonSpec.height, false, 0.0);
    }
    
    public void drawNativeButton(final Minecraft minecraft, final int mouseX, final int mouseY) {
        final int magic = 20;
        minecraft.func_110434_K().func_110577_a(ThemeButton.field_146122_a);
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        final int k = this.func_146114_a(this.func_146115_a());
        GlStateManager.func_179147_l();
        GlStateManager.func_179112_b(770, 771);
        this.func_73729_b(this.field_146128_h, this.field_146129_i, 0, 46 + k * magic, this.field_146120_f / 2, this.field_146121_g);
        this.func_73729_b(this.field_146128_h + this.field_146120_f / 2, this.field_146129_i, 200 - this.field_146120_f / 2, 46 + k * magic, this.field_146120_f / 2, this.field_146121_g);
        this.func_146119_b(minecraft, mouseX, mouseY);
        final int l = 14737632;
    }
    
    public void setAdditionalTooltips(final List<String> additionalTooltips) {
        this.additionalTooltips = additionalTooltips;
    }
    
    @Override
    public List<String> getTooltip() {
        if (!this.field_146125_m) {
            return null;
        }
        final List<String> list = super.getTooltip();
        String style = null;
        if (!this.isEnabled()) {
            style = this.buttonSpec.tooltipDisabledStyle;
        }
        else {
            style = (this.toggled ? this.buttonSpec.tooltipOnStyle : this.buttonSpec.tooltipOffStyle);
        }
        list.add(0, style + this.field_146126_j);
        if (this.field_146124_l && this.additionalTooltips != null) {
            list.addAll(this.additionalTooltips);
        }
        return list;
    }
}
