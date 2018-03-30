package journeymap.client.ui.component;

import journeymap.client.ui.option.SlotMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ScrollListPane<T extends ScrollListPane.ISlot> extends GuiListExtended {
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
        this.currentSlots = new ArrayList<>(0);
        this.parent = parent;
        this.setDimensions(width, height, top, bottom);
    }

    public void setDimensions(final int width, final int height, final int top, final int bottom) {
        super.setDimensions(width, height, top, bottom);
        this.scrollbarX = this.width - this.hpad;
        this.listWidth = this.width - this.hpad * 4;
    }

    protected int getSize() {
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
            this.scrollBy(-(sizeAfter * this.slotHeight));
            this.scrollBy(this.lastClickedIndex * this.slotHeight);
        }
    }

    public void scrollTo(final ISlot slot) {
        this.scrollBy(-(this.currentSlots.size() * this.slotHeight));
        this.scrollBy(this.currentSlots.indexOf(slot) * this.slotHeight);
    }

    public void handleMouseInput() {
        super.handleMouseInput();
    }

    protected void elementClicked(final int index, final boolean doubleClick, final int mouseX, final int mouseY) {
    }

    public boolean isSelected(final int slotIndex) {
        return false;
    }

    protected void drawBackground() {
    }

    protected void drawSlot(final int slotIndex, final int x, final int y, final int slotHeight, final int mouseX, final int mouseY, final float partialTicks) {
        final boolean selected = this.getSlotIndexFromScreenCoords(mouseX, mouseY) == slotIndex;
        final ISlot slot = this.getSlot(slotIndex);
        slot.drawEntry(slotIndex, x, y, this.getListWidth(), slotHeight, mouseX, mouseY, selected, 0.0f);
        final SlotMetadata tooltipMetadata = slot.getCurrentTooltip();
        if (tooltipMetadata != null && !Arrays.equals(tooltipMetadata.getTooltip(), this.lastTooltip)) {
            this.lastTooltipMetadata = tooltipMetadata;
            this.lastTooltip = tooltipMetadata.getTooltip();
            this.lastTooltipTime = System.currentTimeMillis();
        }
    }

    public int getListWidth() {
        return this.listWidth;
    }

    public boolean mouseClicked(final int mouseX, final int mouseY, final int mouseEvent) {
        if (this.isMouseYWithinSlotBounds(mouseY)) {
            final int slotIndex = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
            if (slotIndex >= 0) {
                final int i1 = this.left + this.hpad + this.width / 2 - this.getListWidth() / 2 + 2;
                final int j1 = this.top + 4 - this.getAmountScrolled() + slotIndex * this.slotHeight + this.headerPadding;
                final int relativeX = mouseX - i1;
                final int relativeY = mouseY - j1;
                this.lastClickedIndex = -1;
                if (this.getSlot(slotIndex).mousePressed(slotIndex, mouseX, mouseY, mouseEvent, relativeX, relativeY)) {
                    this.setEnabled(false);
                    this.lastClickedIndex = slotIndex;
                    this.lastPressed = this.getSlot(slotIndex).getLastPressed();
                    this.updateSlots();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseReleased(final int x, final int y, final int mouseEvent) {
        final boolean result = super.mouseReleased(x, y, mouseEvent);
        this.lastPressed = null;
        return result;
    }

    public GuiListExtended.IGuiListEntry getListEntry(final int index) {
        return this.getSlot(index);
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
        for (int slotIndex = 0; slotIndex < this.getSize(); ++slotIndex) {
            if (this.getSlot(slotIndex).keyTyped(c, i)) {
                this.lastClickedIndex = slotIndex;
                this.lastPressed = this.getSlot(slotIndex).getLastPressed();
                this.updateSlots();
                return true;
            }
        }
        return false;
    }

    protected int getScrollBarX() {
        return this.scrollbarX;
    }

    protected void drawContainerBackground(final Tessellator tessellator) {
        this.parent.drawGradientRect(0, this.top, this.width, this.top + this.height, -1072689136, -804253680);
    }

    protected int getContentHeight() {
        int contentHeight = super.getContentHeight();
        if (this.alignTop) {
            contentHeight = Math.max(this.bottom - this.top - 4, contentHeight);
        }
        return contentHeight;
    }

    public void setAlignTop(final boolean alignTop) {
        this.alignTop = alignTop;
    }

    public interface ISlot extends GuiListExtended.IGuiListEntry {
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
