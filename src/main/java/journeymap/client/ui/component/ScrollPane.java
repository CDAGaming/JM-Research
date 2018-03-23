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
    private Integer frameColor;
    private List<? extends Scrollable> items;
    private Minecraft mc;
    private int _mouseX;
    private int _mouseY;
    private boolean showFrame;
    private int firstVisibleIndex;
    private int lastVisibleIndex;
    
    public ScrollPane(final Minecraft mc, final int width, final int height, final List<? extends Scrollable> items, final int itemHeight, final int itemGap) {
        super(mc, width, height, 16, height, itemHeight + itemGap);
        this.paneWidth = 0;
        this.paneHeight = 0;
        this.origin = new Point2D.Double();
        this.selected = null;
        this.frameColor = new Color(-6250336).getRGB();
        this.showFrame = true;
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
    
    protected int func_148127_b() {
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
                    item.clickScrollable(this.mc, mouseX, mouseY);
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
        GlStateManager.func_179121_F();
    }
    
    protected void func_192637_a(final int index, final int xPosition, final int y, final int l, final int var6, final int var7, final float f) {
        if (this.firstVisibleIndex == -1) {
            this.firstVisibleIndex = index;
        }
        this.lastVisibleIndex = Math.max(this.lastVisibleIndex, index);
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b((float)(-this.getX()), (float)(-this.getY()), 0.0f);
        final int margin = 4;
        final int itemX = this.getX() + 2;
        final int itemY = y + this.getY();
        final Scrollable item = (Scrollable)this.items.get(index);
        item.setPosition(itemX, itemY);
        item.setScrollableWidth(this.paneWidth - 4);
        if (this.inFullView(item)) {
            item.drawScrollable(this.mc, this._mouseX, this._mouseY);
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
        final int width = this.getWidth();
        float alpha = 0.4f;
        DrawUtil.drawRectangle(0.0, this.field_148153_b, width, this.paneHeight, Color.BLACK.getRGB(), alpha);
        DrawUtil.drawRectangle(width - 6, this.field_148153_b, 5.0, this.paneHeight, Color.BLACK.getRGB(), alpha);
        if (this.showFrame) {
            alpha = 1.0f;
            DrawUtil.drawRectangle(-1.0, -1.0, width + 2, 1.0, this.frameColor, alpha);
            DrawUtil.drawRectangle(-1.0, this.paneHeight, width + 2, 1.0, this.frameColor, alpha);
            DrawUtil.drawRectangle(-1.0, -1.0, 1.0, this.paneHeight + 1, this.frameColor, alpha);
            DrawUtil.drawRectangle(width + 1, -1.0, 1.0, this.paneHeight + 2, this.frameColor, alpha);
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
    }
}
