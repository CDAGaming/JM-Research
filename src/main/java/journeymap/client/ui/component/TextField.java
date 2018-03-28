package journeymap.client.ui.component;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;

public class TextField extends GuiTextField {
    protected final String numericRegex;
    protected final boolean numeric;
    protected final boolean allowNegative;
    protected int minLength;
    protected Integer clampMin;
    protected Integer clampMax;

    public TextField(final Object text, final FontRenderer fontRenderer, final int width, final int height) {
        this(text, fontRenderer, width, height, false, false);
    }

    public TextField(final Object text, final FontRenderer fontRenderer, final int width, final int height, final boolean isNumeric, final boolean negative) {
        super(0, fontRenderer, 0, 0, width, height);
        this.setText(text.toString());
        this.numeric = isNumeric;
        this.allowNegative = negative;
        String regex = null;
        if (this.numeric) {
            if (this.allowNegative) {
                regex = "[^-?\\d]";
            } else {
                regex = "[^\\d]";
            }
        }
        this.numericRegex = regex;
    }

    public void setClamp(final Integer min, final Integer max) {
        this.clampMin = min;
        this.clampMax = max;
    }

    public void setMinLength(final int minLength) {
        this.minLength = minLength;
    }

    public void writeText(final String par1Str) {
        super.writeText(par1Str);
        if (this.numeric) {
            String fixed = this.getText().replaceAll(this.numericRegex, "");
            if (this.allowNegative) {
                final String start = fixed.startsWith("-") ? "-" : "";
                fixed = start + fixed.replaceAll("-", "");
            }
            super.setText(fixed);
        }
    }

    public void setText(final Object object) {
        super.setText(object.toString());
    }

    public boolean isNumeric() {
        return this.numeric;
    }

    public boolean hasMinLength() {
        final String text = this.getText();
        final int textLen = (text == null) ? 0 : text.length();
        return this.minLength <= textLen;
    }

    public boolean textboxKeyTyped(final char par1, final int par2) {
        final boolean res = super.textboxKeyTyped(par1, par2);
        if (this.numeric && this.isFocused()) {
            this.clamp();
        }
        return res;
    }

    public void drawTextBox() {
        super.drawTextBox();
        if (this.getVisible() && !this.hasMinLength()) {
            final int red = Color.red.getRGB();
            final int x1 = this.getX() - 1;
            final int y1 = this.getY() - 1;
            final int x2 = x1 + this.getWidth() + 1;
            final int y2 = y1 + this.getHeight() + 1;
            drawRect(x1, y1, x2, y1 + 1, red);
            drawRect(x1, y2, x2, y2 + 1, red);
            drawRect(x1, y1, x1 + 1, y2, red);
            drawRect(x2, y1, x2 + 1, y2, red);
        }
    }

    public Integer clamp() {
        if (!this.numeric) {
            return null;
        }
        final String text = this.getText();
        if (this.clampMin != null) {
            if (text == null || text.length() == 0 || text.equals("-")) {
                return null;
            }
            try {
                this.setText(Math.max(this.clampMin, Integer.parseInt(text)));
            } catch (Exception e) {
                this.setText(this.clampMin);
            }
            if (this.clampMax != null) {
                try {
                    this.setText(Math.min(this.clampMax, Integer.parseInt(text)));
                } catch (Exception e) {
                    this.setText(this.clampMax);
                }
            }
        }
        try {
            return Integer.parseInt(text);
        } catch (Exception e) {
            return null;
        }
    }

    public int getX() {
        return this.x;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(final int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(final int w) {
        this.width = w;
    }

    public int getHeight() {
        return this.height;
    }

    public int getCenterX() {
        return this.getX() + this.getWidth() / 2;
    }

    public int getMiddleY() {
        return this.getY() + this.getHeight() / 2;
    }

    public int getBottomY() {
        return this.getY() + this.getHeight();
    }

    public int getRightX() {
        return this.getX() + this.getWidth();
    }
}
