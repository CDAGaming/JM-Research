package journeymap.client.ui.component;

import journeymap.common.properties.config.BooleanField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.config.GuiUtils;

public class CheckBox extends BooleanPropertyButton {
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
        this.setHeight(this.fontRenderer.FONT_HEIGHT + 2);
        this.setWidth(this.getFitWidth(this.fontRenderer));
    }

    @Override
    public int getFitWidth(final FontRenderer fr) {
        return super.getFitWidth(fr) + this.boxWidth + 2;
    }

    @Override
    public void drawButton(final Minecraft mc, final int mouseX, final int mouseY, final float ticks) {
        if (this.visible) {
            this.setHovered(this.isEnabled() && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height);
            final int yoffset = (this.height - this.boxWidth) / 2;
            GuiUtils.drawContinuousTexturedBox(CheckBox.BUTTON_TEXTURES, this.x, this.y + yoffset, 0, 46, this.boxWidth, this.boxWidth, 200, 20, 2, 3, 2, 2, this.zLevel);
            this.mouseDragged(mc, mouseX, mouseY);
            int color = 14737632;
            if (this.isHovered()) {
                color = 16777120;
            } else if (!this.isEnabled()) {
                color = 4210752;
            } else if (this.labelColor != null) {
                color = this.labelColor;
            } else if (this.packedFGColour != 0) {
                color = this.packedFGColour;
            }
            final int labelPad = 4;
            if (this.toggled) {
                this.drawCenteredString(this.fontRenderer, this.glyph, this.x + this.boxWidth / 2 + 1, this.y + 1 + yoffset, color);
            }
            this.drawString(this.fontRenderer, this.displayString, this.x + this.boxWidth + labelPad, this.y + 2 + yoffset, color);
        }
    }

    @Override
    public boolean mousePressed(final Minecraft mc, final int mouseX, final int mouseY) {
        if (this.isEnabled() && this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height) {
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
