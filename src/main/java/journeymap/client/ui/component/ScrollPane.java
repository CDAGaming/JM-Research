package journeymap.client.ui.component;

import java.awt.geom.*;
import net.minecraft.client.*;
import java.awt.*;
import java.util.*;
import org.lwjgl.input.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.*;
import journeymap.client.render.draw.*;

public class ScrollPane extends GuiSlot
{
    public int paneWidth;
    public int paneHeight;
    public Point2D.Double origin;
    protected Scrollable selected;
    private List<? extends Scrollable> items;
    private Minecraft mc;
    private int _mouseX;
    private int _mouseY;
    private boolean showFrame;
    private int firstVisibleIndex;
    private int lastVisibleIndex;
    public int bgColor;
    public float bgAlpha;
    public int frameColor;
    public float frameAlpha;
    
    public ScrollPane(final Minecraft mc, final int width, final int height, final List<? extends Scrollable> items, final int itemHeight, final int itemGap) {
        super(mc, width, height, 16, height, itemHeight + itemGap);
        this.paneWidth = 0;
        this.paneHeight = 0;
        this.origin = new Point2D.Double();
        this.selected = null;
        this.showFrame = true;
        this.bgColor = 0;
        this.bgAlpha = 0.4f;
        this.frameColor = new Color(-6250336).getRGB();
        this.frameAlpha = 1.0f;
        this.items = items;
        this.paneWidth = width;
        this.paneHeight = height;
        this.mc = mc;
    }
    
    public int getX() {
        return (int)this.origin.getX();
    }
    
    public int getY() {
        return (int)this.origin.getY();
    }
    
    public int func_148146_j() {
        return this.field_148149_f;
    }
    
    public void setDimensions(final int width, final int height, final int marginTop, final int marginBottom, final int x, final int y) {
        super.func_148122_a(width, height, marginTop, height - marginBottom);
        this.paneWidth = width;
        this.paneHeight = height;
        this.origin.setLocation(x, y);
    }
    
    public int func_148127_b() {
        return this.items.size();
    }
    
    protected void func_148144_a(final int i, final boolean flag, final int p1, final int p2) {
        this.selected = (Scrollable)this.items.get(i);
    }
    
    protected boolean func_148131_a(final int i) {
        return this.items.get(i) == this.selected;
    }
    
    public boolean isSelected(final Scrollable item) {
        return item == this.selected;
    }
    
    public void select(final Scrollable item) {
        this.selected = item;
    }
    
    protected void func_148123_a() {
    }
    
    public Button mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        if (mouseButton == 0) {
            final ArrayList<Scrollable> itemsCopy = new ArrayList<Scrollable>(this.items);
            for (final Scrollable item : itemsCopy) {
                if (item == null) {
                    continue;
                }
                if (!this.inFullView(item)) {
                    continue;
                }
                if (item instanceof Button) {
                    final Button button = (Button)item;
                    if (button.func_146116_c(this.mc, mouseX, mouseY)) {
                        this.func_148147_a((GuiButton)button);
                        return button;
                    }
                    continue;
                }
                else {
                    if (mouseX >= item.getX() && mouseX <= item.getX() + item.getWidth() && mouseY >= item.getY() && mouseY <= item.getY() + item.getHeight()) {
                        item.clickScrollable(this.mc, mouseX, mouseY);
                        return null;
                    }
                    continue;
                }
            }
        }
        return null;
    }
    
    public void func_148128_a(final int mX, final int mY, final float f) {
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b((float)this.getX(), (float)this.getY(), 0.0f);
        this._mouseX = mX;
        this._mouseY = mY;
        if (this.selected == null || Mouse.isButtonDown(0) || Mouse.getDWheel() != 0 || !Mouse.next() || Mouse.getEventButtonState()) {}
        this.firstVisibleIndex = -1;
        this.lastVisibleIndex = -1;
        super.func_148128_a(mX - this.getX(), mY - this.getY(), f);
        if (Mouse.isButtonDown(0) && this.getY() <= mY && mY <= this.getY() + this.field_148158_l && mX >= this.getX() + this.field_148155_a && mX <= this.getX() + this.field_148155_a + 6) {
            final int slot = (int)((mY - this.getY() * 1.0) / this.paneHeight * this.func_148127_b());
            this.func_148145_f(-(this.func_148127_b() * this.func_148146_j()));
            this.func_148145_f(slot * this.func_148146_j());
        }
        GlStateManager.func_179121_F();
    }
    
    protected void func_192637_a(final int index, final int x, final int yPosition, final int insideSlotHeight, final int mouseXIn, final int mouseYIn, final float partialTicks) {
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b((float)(-this.getX()), (float)(-this.getY()), 0.0f);
        final int margin = 4;
        final int itemX = this.getX() + 2;
        final int itemY = yPosition + this.getY();
        final Scrollable item = (Scrollable)this.items.get(index);
        item.setPosition(itemX, itemY);
        item.setScrollableWidth(this.paneWidth - 4);
        if (this.inFullView(item)) {
            item.drawScrollable(this.mc, this._mouseX, this._mouseY);
            if (this.firstVisibleIndex == -1) {
                this.firstVisibleIndex = index;
            }
            this.lastVisibleIndex = Math.max(this.lastVisibleIndex, index);
        }
        else {
            final int paneBottomY = this.getY() + this.paneHeight;
            final int itemBottomY = itemY + item.getHeight();
            Integer drawY = null;
            int yDiff = 0;
            if (itemY < this.getY() && itemBottomY > this.getY()) {
                drawY = this.getY();
                yDiff = drawY - itemY;
            }
            else if (itemY < paneBottomY && itemBottomY > paneBottomY) {
                drawY = itemY;
                yDiff = itemBottomY - paneBottomY;
            }
            if (drawY != null) {
                item.drawPartialScrollable(this.mc, itemX, drawY, item.getWidth(), item.getHeight() - yDiff);
            }
        }
        GlStateManager.func_179121_F();
    }
    
    public boolean inFullView(final Scrollable item) {
        return item.getY() >= this.getY() && item.getY() + item.getHeight() <= this.getY() + this.paneHeight;
    }
    
    public Scrollable getScrollableUnderMouse(final int mouseX, final int mouseY) {
        for (int i = this.firstVisibleIndex; i <= this.lastVisibleIndex; ++i) {
            if (i >= 0 && i < this.items.size()) {
                final Scrollable item = (Scrollable)this.items.get(i);
                if (mouseX >= item.getX() && mouseX <= item.getX() + item.getWidth() && mouseY >= item.getY() && mouseY <= item.getY() + item.getHeight()) {
                    return item;
                }
            }
        }
        return null;
    }
    
    protected int func_148137_d() {
        return this.paneWidth;
    }
    
    public int getWidth() {
        final boolean scrollVisible = 0 < this.func_148148_g();
        return this.paneWidth + (scrollVisible ? 5 : 0);
    }
    
    public int getFitWidth(final FontRenderer fr) {
        int fit = 0;
        for (final Scrollable item : this.items) {
            fit = Math.max(fit, item.getFitWidth(fr));
        }
        return fit;
    }
    
    public void setShowFrame(final boolean showFrame) {
        this.showFrame = showFrame;
    }
    
    protected void drawContainerBackground(final Tessellator tess) {
        DrawUtil.drawRectangle(this.field_148152_e, this.field_148153_b, this.paneWidth, this.paneHeight, this.bgColor, this.bgAlpha);
        if (this.func_148135_f() == 0) {
            DrawUtil.drawRectangle(this.paneWidth, this.field_148153_b, 6.0, this.paneHeight, 0, this.bgAlpha);
        }
        if (this.showFrame) {
            final int x1 = -2;
            final int y1 = this.field_148153_b - 1;
            final int x2 = this.paneWidth + 8;
            final int y2 = this.paneHeight + 3;
            DrawUtil.drawRectangle(x1, y1, x2, 1.0, this.frameColor, this.frameAlpha);
            DrawUtil.drawRectangle(x1, y2 - 3, x2 + 1, 1.0, this.frameColor, this.frameAlpha);
            DrawUtil.drawRectangle(x1, y1, 1.0, y2 - 2, this.frameColor, this.frameAlpha);
            DrawUtil.drawRectangle(x2 - 2, y1, 1.0, y2 - 2, this.frameColor, this.frameAlpha);
        }
    }
    
    public int getFirstVisibleIndex() {
        return this.firstVisibleIndex;
    }
    
    public int getLastVisibleIndex() {
        return this.lastVisibleIndex;
    }
    
    public interface Scrollable
    {
        void setPosition(final int p0, final int p1);
        
        int getX();
        
        int getY();
        
        int getWidth();
        
        void setScrollableWidth(final int p0);
        
        int getFitWidth(final FontRenderer p0);
        
        int getHeight();
        
        void drawScrollable(final Minecraft p0, final int p1, final int p2);
        
        void drawPartialScrollable(final Minecraft p0, final int p1, final int p2, final int p3, final int p4);
        
        void clickScrollable(final Minecraft p0, final int p1, final int p2);
        
        List<String> getTooltip();
    }
}
