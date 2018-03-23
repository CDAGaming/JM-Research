package journeymap.client.ui.component;

import net.minecraft.client.gui.*;
import journeymap.client.ui.option.*;
import net.minecraft.client.*;
import java.util.*;
import net.minecraft.client.renderer.*;

public class ScrollListPane<T extends ISlot> extends GuiListExtended
{
    final JmUI parent;
    public SlotMetadata lastTooltipMetadata;
    public String[] lastTooltip;
    public long lastTooltipTime;
    public long hoverDelay;
    int hpad;
    List<T> rootSlots;
    List<ISlot> currentSlots;
    SlotMetadata lastPressed;
    int lastClickedIndex;
    int scrollbarX;
    int listWidth;
    boolean alignTop;
    
    public ScrollListPane(final JmUI parent, final Minecraft mc, final int width, final int height, final int top, final int bottom, final int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
        this.hoverDelay = 800L;
        this.hpad = 12;
        this.currentSlots = new ArrayList<ISlot>(0);
        this.parent = parent;
        this.func_148122_a(width, height, top, bottom);
    }
    
    public void func_148122_a(final int width, final int height, final int top, final int bottom) {
        super.func_148122_a(width, height, top, bottom);
        this.scrollbarX = this.field_148155_a - this.hpad;
        this.listWidth = this.field_148155_a - this.hpad * 4;
    }
    
    protected int func_148127_b() {
        return (this.currentSlots == null) ? 0 : this.currentSlots.size();
    }
    
    public void setSlots(final List<T> slots) {
        this.rootSlots = slots;
        this.updateSlots();
    }
    
    public List<T> getRootSlots() {
        return this.rootSlots;
    }
    
    public void updateSlots() {
        final int sizeBefore = this.currentSlots.size();
        this.currentSlots.clear();
        int columnWidth = 0;
        for (final ISlot slot : this.rootSlots) {
            columnWidth = Math.max(columnWidth, slot.getColumnWidth());
        }
        for (final ISlot slot : this.rootSlots) {
            this.currentSlots.add(slot);
            final List<? extends ISlot> children = slot.getChildSlots(this.listWidth, columnWidth);
            if (children != null && !children.isEmpty()) {
                this.currentSlots.addAll(children);
            }
        }
        final int sizeAfter = this.currentSlots.size();
        if (sizeBefore < sizeAfter) {
            this.func_148145_f(-(sizeAfter * this.field_148149_f));
            this.func_148145_f(this.lastClickedIndex * this.field_148149_f);
        }
    }
    
    public void scrollTo(final ISlot slot) {
        this.func_148145_f(-(this.currentSlots.size() * this.field_148149_f));
        this.func_148145_f(this.currentSlots.indexOf(slot) * this.field_148149_f);
    }
    
    public void func_178039_p() {
        super.func_178039_p();
    }
    
    protected void func_148144_a(final int index, final boolean doubleClick, final int mouseX, final int mouseY) {
    }
    
    public boolean func_148131_a(final int p_148131_1_) {
        return false;
    }
    
    protected void func_148123_a() {
    }
    
    protected void func_192637_a(final int slotIndex, final int x, final int y, final int slotHeight, final int mouseX, final int mouseY, final float partialTicks) {
        final boolean selected = this.func_148124_c(mouseX, mouseY) == slotIndex;
        final ISlot slot = this.getSlot(slotIndex);
        slot.func_192634_a(slotIndex, x, y, this.func_148139_c(), slotHeight, mouseX, mouseY, selected, 0.0f);
        final SlotMetadata tooltipMetadata = slot.getCurrentTooltip();
        if (tooltipMetadata != null && !Arrays.equals(tooltipMetadata.getTooltip(), this.lastTooltip)) {
            this.lastTooltipMetadata = tooltipMetadata;
            this.lastTooltip = tooltipMetadata.getTooltip();
            this.lastTooltipTime = System.currentTimeMillis();
        }
    }
    
    public int func_148139_c() {
        return this.listWidth;
    }
    
    public boolean func_148179_a(final int mouseX, final int mouseY, final int mouseEvent) {
        if (this.func_148141_e(mouseY)) {
            final int slotIndex = this.func_148124_c(mouseX, mouseY);
            if (slotIndex >= 0) {
                final int i1 = this.field_148152_e + this.hpad + this.field_148155_a / 2 - this.func_148139_c() / 2 + 2;
                final int j1 = this.field_148153_b + 4 - this.func_148148_g() + slotIndex * this.field_148149_f + this.field_148160_j;
                final int relativeX = mouseX - i1;
                final int relativeY = mouseY - j1;
                this.lastClickedIndex = -1;
                if (this.getSlot(slotIndex).func_148278_a(slotIndex, mouseX, mouseY, mouseEvent, relativeX, relativeY)) {
                    this.func_148143_b(false);
                    this.lastClickedIndex = slotIndex;
                    this.lastPressed = this.getSlot(slotIndex).getLastPressed();
                    this.updateSlots();
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean func_148181_b(final int x, final int y, final int mouseEvent) {
        final boolean result = super.func_148181_b(x, y, mouseEvent);
        this.lastPressed = null;
        return result;
    }
    
    public GuiListExtended.IGuiListEntry func_148180_b(final int index) {
        return (GuiListExtended.IGuiListEntry)this.getSlot(index);
    }
    
    public ISlot getSlot(final int index) {
        return this.currentSlots.get(index);
    }
    
    public SlotMetadata getLastPressed() {
        return this.lastPressed;
    }
    
    public void resetLastPressed() {
        this.lastPressed = null;
    }
    
    public ISlot getLastPressedParentSlot() {
        if (this.lastPressed != null) {
            for (final ISlot slot : this.rootSlots) {
                if (slot.contains(this.lastPressed)) {
                    return slot;
                }
            }
        }
        return null;
    }
    
    public boolean keyTyped(final char c, final int i) {
        for (int slotIndex = 0; slotIndex < this.func_148127_b(); ++slotIndex) {
            if (this.getSlot(slotIndex).keyTyped(c, i)) {
                this.lastClickedIndex = slotIndex;
                this.lastPressed = this.getSlot(slotIndex).getLastPressed();
                this.updateSlots();
                return true;
            }
        }
        return false;
    }
    
    protected int func_148137_d() {
        return this.scrollbarX;
    }
    
    protected void drawContainerBackground(final Tessellator tessellator) {
        this.parent.func_73733_a(0, this.field_148153_b, this.field_148155_a, this.field_148153_b + this.field_148158_l, -1072689136, -804253680);
    }
    
    protected int func_148138_e() {
        int contentHeight = super.func_148138_e();
        if (this.alignTop) {
            contentHeight = Math.max(this.field_148154_c - this.field_148153_b - 4, contentHeight);
        }
        return contentHeight;
    }
    
    public void setAlignTop(final boolean alignTop) {
        this.alignTop = alignTop;
    }
    
    public interface ISlot extends GuiListExtended.IGuiListEntry
    {
        Collection<SlotMetadata> getMetadata();
        
        String[] mouseHover(final int p0, final int p1, final int p2, final int p3, final int p4, final int p5);
        
        boolean keyTyped(final char p0, final int p1);
        
        List<? extends ISlot> getChildSlots(final int p0, final int p1);
        
        SlotMetadata getLastPressed();
        
        SlotMetadata getCurrentTooltip();
        
        void setEnabled(final boolean p0);
        
        int getColumnWidth();
        
        boolean contains(final SlotMetadata p0);
    }
}
