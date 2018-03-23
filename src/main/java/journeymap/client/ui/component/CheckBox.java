package journeymap.client.ui.component;

import journeymap.common.properties.config.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.*;
import net.minecraftforge.fml.client.config.*;

public class CheckBox extends BooleanPropertyButton
{
    public int boxWidth;
    String glyph;
    
    public CheckBox(final String displayString, final boolean checked) {
        this(displayString, null);
        this.toggled = checked;
    }
    
    public CheckBox(final String displayString, final BooleanField field) {
        super(displayString, displayString, field);
        this.boxWidth = 11;
        this.glyph = "\u2714";
        this.setHeight(this.fontRenderer.field_78288_b + 2);
        this.func_175211_a(this.getFitWidth(this.fontRenderer));
    }
    
    @Override
    public int getFitWidth(final FontRenderer fr) {
        return super.getFitWidth(fr) + this.boxWidth + 2;
    }
    
    @Override
    public void func_191745_a(final Minecraft mc, final int mouseX, final int mouseY, final float ticks) {
        if (this.field_146125_m) {
            this.setHovered(this.isEnabled() && mouseX >= this.field_146128_h && mouseY >= this.field_146129_i && mouseX < this.field_146128_h + this.field_146120_f && mouseY < this.field_146129_i + this.field_146121_g);
            final int yoffset = (this.field_146121_g - this.boxWidth) / 2;
            GuiUtils.drawContinuousTexturedBox(CheckBox.field_146122_a, this.field_146128_h, this.field_146129_i + yoffset, 0, 46, this.boxWidth, this.boxWidth, 200, 20, 2, 3, 2, 2, this.field_73735_i);
            this.func_146119_b(mc, mouseX, mouseY);
            int color = 14737632;
            if (this.isHovered()) {
                color = 16777120;
            }
            else if (!this.isEnabled()) {
                color = 4210752;
            }
            else if (this.labelColor != null) {
                color = this.labelColor;
            }
            else if (this.packedFGColour != 0) {
                color = this.packedFGColour;
            }
            final int labelPad = 4;
            if (this.toggled) {
                this.func_73732_a(this.fontRenderer, this.glyph, this.field_146128_h + this.boxWidth / 2 + 1, this.field_146129_i + 1 + yoffset, color);
            }
            this.func_73731_b(this.fontRenderer, this.field_146126_j, this.field_146128_h + this.boxWidth + labelPad, this.field_146129_i + 2 + yoffset, color);
        }
    }
    
    @Override
    public boolean func_146116_c(final Minecraft p_146116_1_, final int mouseX, final int mouseY) {
        if (this.isEnabled() && this.field_146125_m && mouseX >= this.field_146128_h && mouseY >= this.field_146129_i && mouseX < this.field_146128_h + this.field_146120_f && mouseY < this.field_146129_i + this.field_146121_g) {
            this.toggle();
            return this.checkClickListeners();
        }
        return false;
    }
    
    @Override
    public boolean keyTyped(final char c, final int i) {
        if (this.isEnabled() && i == 57) {
            this.toggle();
            return true;
        }
        return false;
    }
}
