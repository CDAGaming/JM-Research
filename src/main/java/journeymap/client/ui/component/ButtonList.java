package journeymap.client.ui.component;

import net.minecraft.client.gui.*;
import net.minecraft.client.*;
import java.util.*;

public class ButtonList extends ArrayList<Button>
{
    static EnumSet<Layout> VerticalLayouts;
    static EnumSet<Layout> HorizontalLayouts;
    private Layout layout;
    private Direction direction;
    private String label;
    
    public ButtonList() {
        this.layout = Layout.Horizontal;
        this.direction = Direction.LeftToRight;
    }
    
    public ButtonList(final String label) {
        this.layout = Layout.Horizontal;
        this.direction = Direction.LeftToRight;
        this.label = label;
    }
    
    public ButtonList(final List<GuiButton> buttons) {
        this.layout = Layout.Horizontal;
        this.direction = Direction.LeftToRight;
        for (final GuiButton button : buttons) {
            if (button instanceof Button) {
                this.add((Button)button);
            }
        }
    }
    
    public ButtonList(final Button... buttons) {
        super(Arrays.asList(buttons));
        this.layout = Layout.Horizontal;
        this.direction = Direction.LeftToRight;
    }
    
    public int getWidth(final int hgap) {
        return this.getWidth(-1, hgap);
    }
    
    private int getWidth(final int buttonWidth, final int hgap) {
        if (this.isEmpty()) {
            return 0;
        }
        int total = 0;
        if (ButtonList.HorizontalLayouts.contains(this.layout)) {
            int visible = 0;
            for (final Button button : this) {
                if (button.isVisible()) {
                    if (buttonWidth > 0) {
                        total += buttonWidth;
                    }
                    else {
                        total += button.getWidth();
                    }
                    ++visible;
                }
            }
            if (visible > 1) {
                total += hgap * (visible - 1);
            }
        }
        else {
            if (buttonWidth > 0) {
                total = buttonWidth;
            }
            for (final Button button2 : this) {
                if (button2.isVisible()) {
                    total = Math.max(total, button2.getWidth());
                }
            }
        }
        return total;
    }
    
    public int getHeight() {
        return this.getHeight(0);
    }
    
    public int getHeight(final int vgap) {
        if (this.isEmpty()) {
            return 0;
        }
        int total = 0;
        if (ButtonList.VerticalLayouts.contains(this.layout)) {
            int visible = 0;
            for (final Button button : this) {
                if (button.isVisible()) {
                    total += button.getHeight();
                    ++visible;
                }
            }
            if (visible > 1) {
                total += vgap * (visible - 1);
            }
        }
        else {
            for (final Button button2 : this) {
                if (button2.isVisible()) {
                    total = Math.max(total, button2.getHeight() + vgap);
                }
            }
        }
        return total;
    }
    
    public int getLeftX() {
        int left = Integer.MAX_VALUE;
        for (final Button button : this) {
            if (button.isVisible()) {
                left = Math.min(left, button.getX());
            }
        }
        if (left == Integer.MAX_VALUE) {
            left = 0;
        }
        return left;
    }
    
    public int getTopY() {
        int top = Integer.MAX_VALUE;
        for (final Button button : this) {
            if (button.isVisible()) {
                top = Math.min(top, button.getY());
            }
        }
        if (top == Integer.MAX_VALUE) {
            top = 0;
        }
        return top;
    }
    
    public int getBottomY() {
        int bottom = Integer.MIN_VALUE;
        for (final Button button : this) {
            if (button.isVisible()) {
                bottom = Math.max(bottom, button.getY() + button.getHeight());
            }
        }
        if (bottom == Integer.MIN_VALUE) {
            bottom = 0;
        }
        return bottom;
    }
    
    public int getRightX() {
        int right = 0;
        for (final Button button : this) {
            if (button.isVisible()) {
                right = Math.max(right, button.getX() + button.getWidth());
            }
        }
        return right;
    }
    
    public Button findButton(final int id) {
        for (final Button button : this) {
            if (button.field_146127_k == id) {
                return button;
            }
        }
        return null;
    }
    
    public void setLayout(final Layout layout, final Direction direction) {
        this.layout = layout;
        this.direction = direction;
    }
    
    public ButtonList layoutHorizontal(final int startX, final int y, final boolean leftToRight, final int hgap) {
        this.layout = Layout.Horizontal;
        this.direction = (leftToRight ? Direction.LeftToRight : Direction.RightToLeft);
        Button last = null;
        for (final Button button : this) {
            if (!button.field_146125_m) {
                continue;
            }
            if (last == null) {
                if (leftToRight) {
                    button.rightOf(startX).setY(y);
                }
                else {
                    button.leftOf(startX).setY(y);
                }
            }
            else if (leftToRight) {
                button.rightOf(last, hgap).setY(y);
            }
            else {
                button.leftOf(last, hgap).setY(y);
            }
            last = button;
        }
        this.layout = Layout.Horizontal;
        return this;
    }
    
    public ButtonList layoutVertical(final int x, final int startY, final boolean leftToRight, final int vgap) {
        this.layout = Layout.Vertical;
        this.direction = (leftToRight ? Direction.LeftToRight : Direction.RightToLeft);
        Button last = null;
        for (final Button button : this) {
            if (last == null) {
                if (leftToRight) {
                    button.rightOf(x).setY(startY);
                }
                else {
                    button.leftOf(x).setY(startY);
                }
            }
            else if (leftToRight) {
                button.rightOf(x).below(last, vgap);
            }
            else {
                button.leftOf(x).below(last, vgap);
            }
            last = button;
        }
        this.layout = Layout.Vertical;
        return this;
    }
    
    public ButtonList layoutCenteredVertical(final int x, final int centerY, final boolean leftToRight, final int vgap) {
        this.layout = Layout.CenteredVertical;
        final int height = this.getHeight(vgap);
        this.layoutVertical(x, centerY - height / 2, leftToRight, vgap);
        return this;
    }
    
    public ButtonList layoutCenteredHorizontal(final int centerX, final int y, final boolean leftToRight, final int hgap) {
        this.layout = Layout.CenteredHorizontal;
        final int width = this.getWidth(hgap);
        this.layoutHorizontal(centerX - width / 2, y, leftToRight, hgap);
        return this;
    }
    
    public ButtonList layoutDistributedHorizontal(final int leftX, final int y, final int rightX, final boolean leftToRight) {
        this.layout = Layout.DistributedHorizontal;
        if (this.size() == 0) {
            return this;
        }
        final int width = this.getWidth(0);
        final int filler = rightX - leftX - width;
        final int gaps = this.size() - 1;
        final int hgap = (gaps == 0) ? 0 : ((filler >= gaps) ? (filler / gaps) : 0);
        if (leftToRight) {
            this.layoutHorizontal(leftX, y, true, hgap);
        }
        else {
            this.layoutHorizontal(rightX, y, false, hgap);
        }
        this.layout = Layout.DistributedHorizontal;
        return this;
    }
    
    public ButtonList layoutFilledHorizontal(final FontRenderer fr, final int leftX, final int y, final int rightX, final int hgap, final boolean leftToRight) {
        this.layout = Layout.FilledHorizontal;
        if (this.size() == 0) {
            return this;
        }
        this.equalizeWidths(fr);
        final int width = this.getWidth(hgap);
        final int remaining = rightX - leftX - width;
        if (remaining > this.size()) {
            final int gaps = hgap * this.size();
            final int area = rightX - leftX - gaps;
            final int wider = area / this.size();
            this.setWidths(wider);
            this.layoutDistributedHorizontal(leftX, y, rightX, leftToRight);
        }
        else {
            this.layoutCenteredHorizontal((rightX - leftX) / 2, y, leftToRight, hgap);
        }
        this.layout = Layout.FilledHorizontal;
        return this;
    }
    
    public void setFitWidths(final FontRenderer fr) {
        this.fitWidths(fr);
    }
    
    public boolean isHorizontal() {
        return this.layout != Layout.Vertical && this.layout != Layout.CenteredVertical;
    }
    
    public ButtonList setEnabled(final boolean enabled) {
        for (final Button button : this) {
            button.setEnabled(enabled);
        }
        return this;
    }
    
    public ButtonList setOptions(final boolean enabled, final boolean drawBackground, final boolean drawFrame) {
        for (final Button button : this) {
            button.setEnabled(enabled);
            button.setDrawFrame(drawFrame);
            button.setDrawBackground(drawBackground);
        }
        return this;
    }
    
    public ButtonList setDefaultStyle(final boolean defaultStyle) {
        for (final Button button : this) {
            button.setDefaultStyle(defaultStyle);
        }
        return this;
    }
    
    public ButtonList draw(final Minecraft minecraft, final int mouseX, final int mouseY) {
        for (final Button button : this) {
            button.func_191745_a(minecraft, mouseX, mouseY, 0.0f);
        }
        return this;
    }
    
    public void setHeights(final int height) {
        for (final Button button : this) {
            button.setHeight(height);
        }
    }
    
    public void setWidths(final int width) {
        for (final Button button : this) {
            button.func_175211_a(width);
        }
    }
    
    public void fitWidths(final FontRenderer fr) {
        for (final Button button : this) {
            button.fitWidth(fr);
        }
    }
    
    public void setDrawButtons(final boolean draw) {
        for (final Button button : this) {
            button.setDrawButton(draw);
        }
    }
    
    public void equalizeWidths(final FontRenderer fr) {
        int maxWidth = 0;
        for (final Button button : this) {
            if (button.isVisible()) {
                button.fitWidth(fr);
                maxWidth = Math.max(maxWidth, button.getWidth());
            }
        }
        this.setWidths(maxWidth);
    }
    
    public void equalizeWidths(final FontRenderer fr, final int hgap, final int maxTotalWidth) {
        int maxWidth = 0;
        for (final Button button : this) {
            button.fitWidth(fr);
            maxWidth = Math.max(maxWidth, button.getWidth());
        }
        int totalWidth = this.getWidth(maxWidth, hgap);
        if (totalWidth <= maxTotalWidth) {
            this.setWidths(maxWidth);
        }
        else {
            totalWidth = this.getWidth(hgap);
        }
        if (totalWidth < maxTotalWidth) {
            final int pad = (maxTotalWidth - totalWidth) / this.size();
            if (pad > 0) {
                for (final Button button2 : this) {
                    button2.func_175211_a(button2.getWidth() + pad);
                }
            }
        }
    }
    
    public int getVisibleButtonCount() {
        int count = 0;
        for (final Button button : this) {
            if (button.field_146125_m) {
                ++count;
            }
        }
        return count;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public void setLabel(final String label) {
        this.label = label;
    }
    
    public ButtonList reverse() {
        Collections.reverse(this);
        return this;
    }
    
    static {
        ButtonList.VerticalLayouts = EnumSet.of(Layout.Vertical, Layout.CenteredVertical);
        ButtonList.HorizontalLayouts = EnumSet.of(Layout.Horizontal, Layout.CenteredHorizontal, Layout.DistributedHorizontal, Layout.FilledHorizontal);
    }
    
    public enum Layout
    {
        Horizontal, 
        Vertical, 
        CenteredHorizontal, 
        CenteredVertical, 
        DistributedHorizontal, 
        FilledHorizontal;
    }
    
    public enum Direction
    {
        LeftToRight, 
        RightToLeft;
    }
}
