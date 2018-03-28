package journeymap.client.ui.component;

import journeymap.common.properties.config.IntegerField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiUtils;

public class IntSliderButton extends Button implements IConfigFieldHolder<IntegerField> {
    public String prefix;
    public boolean dragging;
    public int minValue;
    public int maxValue;
    public String suffix;
    public boolean drawString;
    IntegerField field;

    public IntSliderButton(final IntegerField field, final String prefix, final String suf) {
        this(field, prefix, suf, field.getMinValue(), field.getMaxValue(), true);
    }

    public IntSliderButton(final IntegerField field, final String prefix, final String suf, final int minVal, final int maxVal, final boolean drawStr) {
        super(prefix);
        this.prefix = "";
        this.dragging = false;
        this.minValue = 0;
        this.maxValue = 0;
        this.suffix = "";
        this.drawString = true;
        this.minValue = minVal;
        this.maxValue = maxVal;
        this.prefix = prefix;
        this.suffix = suf;
        this.field = field;
        this.setValue(field.get());
        super.disabledLabelColor = 4210752;
    }

    public int getHoverState(final boolean par1) {
        return 0;
    }

    protected void mouseDragged(final Minecraft par1Minecraft, final int par2, final int par3) {
        if (this.visible && this.isEnabled()) {
            if (this.dragging) {
                this.setSliderValue((par2 - (this.x + 4)) / (this.width - 8));
            }
            final int k = this.getHoverState(this.isEnabled());
            if (this.isEnabled() || this.dragging) {
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                final double sliderValue = this.getSliderValue();
                GuiUtils.drawContinuousTexturedBox(IntSliderButton.BUTTON_TEXTURES, this.x + 1 + (int) (sliderValue * (this.width - 10)), this.y + 1, 0, 66, 8, this.height - 2, 200, 20, 2, 3, 2, 2, this.zLevel);
            }
        }
    }

    @Override
    public boolean mousePressed(final Minecraft mc, final int mouseX, final int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY, false)) {
            this.setSliderValue((mouseX - (this.x + 4)) / (this.width - 8));
            this.dragging = true;
            return this.checkClickListeners();
        }
        return false;
    }

    public double getSliderValue() {
        return (this.field.get() - this.minValue * 1.0) / (this.maxValue - this.minValue);
    }

    public void setSliderValue(double sliderValue) {
        if (sliderValue < 0.0) {
            sliderValue = 0.0;
        }
        if (sliderValue > 1.0) {
            sliderValue = 1.0;
        }
        final int intVal = (int) Math.round(sliderValue * (this.maxValue - this.minValue) + this.minValue);
        this.setValue(intVal);
    }

    public void updateLabel() {
        if (this.drawString) {
            this.displayString = this.prefix + this.field.get() + this.suffix;
        }
    }

    public void mouseReleased(final int par1, final int par2) {
        if (this.dragging) {
            this.dragging = false;
            this.field.save();
            this.checkClickListeners();
        }
    }

    @Override
    public int getFitWidth(final FontRenderer fr) {
        int max = fr.getStringWidth(this.prefix + this.minValue + this.suffix);
        max = Math.max(max, fr.getStringWidth(this.prefix + this.maxValue + this.suffix));
        return max + this.WIDTH_PAD;
    }

    @Override
    public boolean keyTyped(final char c, final int i) {
        if (this.isEnabled()) {
            if (i == 203 || i == 208 || i == 74) {
                this.setValue(Math.max(this.minValue, this.getValue() - 1));
                return true;
            }
            if (i == 205 || i == 200 || i == 78) {
                this.setValue(Math.min(this.maxValue, this.getValue() + 1));
                return true;
            }
        }
        return false;
    }

    public int getValue() {
        return this.field.get();
    }

    public void setValue(int value) {
        value = Math.min(value, this.maxValue);
        value = Math.max(value, this.minValue);
        if (this.field.get() != value) {
            this.field.set(value);
            if (!this.dragging) {
                this.field.save();
            }
        }
        this.updateLabel();
    }

    @Override
    public IntegerField getConfigField() {
        return this.field;
    }
}
