package journeymap.client.ui.option;

import net.minecraft.client.*;
import journeymap.common.properties.*;
import journeymap.client.ui.component.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.properties.*;
import java.util.*;
import journeymap.client.render.draw.*;

public class CategorySlot implements ScrollListPane.ISlot, Comparable<CategorySlot>
{
    Minecraft mc;
    SlotMetadata metadata;
    Category category;
    int currentSlotIndex;
    Button button;
    int currentListWidth;
    int currentColumns;
    int currentColumnWidth;
    SlotMetadata masterSlot;
    SlotMetadata currentTooltip;
    LinkedList<SlotMetadata> childMetadataList;
    List<ScrollListPane.ISlot> childSlots;
    String glyphClosed;
    String glyphOpen;
    private boolean selected;
    
    public CategorySlot(final Category category) {
        this.mc = FMLClientHandler.instance().getClient();
        this.childMetadataList = new LinkedList<SlotMetadata>();
        this.childSlots = new ArrayList<ScrollListPane.ISlot>();
        this.glyphClosed = "\u25b6";
        this.glyphOpen = "\u25bc";
        this.category = category;
        final boolean advanced = category == ClientCategory.Advanced;
        this.button = new Button(category.getLabel());
        this.metadata = new SlotMetadata(this.button, category.getLabel(), category.getTooltip(), advanced);
        this.updateButtonLabel();
    }
    
    public CategorySlot add(final ScrollListPane.ISlot slot) {
        this.childSlots.add(slot);
        this.childMetadataList.addAll(slot.getMetadata());
        for (final SlotMetadata slotMetadata : slot.getMetadata()) {
            if (slotMetadata.isMasterPropertyForCategory()) {
                this.masterSlot = slotMetadata;
            }
        }
        return this;
    }
    
    public void clear() {
        this.childSlots.clear();
    }
    
    public int size() {
        return this.childSlots.size();
    }
    
    public void sort() {
        Collections.sort(this.childMetadataList);
    }
    
    @Override
    public int getColumnWidth() {
        int columnWidth = 100;
        for (final ScrollListPane.ISlot slot : this.childSlots) {
            columnWidth = Math.max(columnWidth, slot.getColumnWidth());
        }
        return columnWidth;
    }
    
    @Override
    public List<ScrollListPane.ISlot> getChildSlots(final int listWidth, final int columnWidth) {
        if (!this.selected) {
            return (List<ScrollListPane.ISlot>)Collections.EMPTY_LIST;
        }
        final int columns = listWidth / (columnWidth + ButtonListSlot.hgap);
        if (columnWidth == this.currentColumnWidth && columns == this.currentColumns) {
            return this.childSlots;
        }
        this.currentListWidth = listWidth;
        this.currentColumnWidth = columnWidth;
        this.currentColumns = columns;
        this.childSlots.clear();
        this.sort();
        final ArrayList<SlotMetadata> remaining = new ArrayList<SlotMetadata>(this.childMetadataList);
        while (!remaining.isEmpty()) {
            final ButtonListSlot row = new ButtonListSlot(this);
            SlotMetadata.ValueType lastType = null;
            for (int i = 0; i < columns && !remaining.isEmpty(); ++i) {
                final SlotMetadata.ValueType thisType = remaining.get(0).valueType;
                if (lastType == null && thisType == SlotMetadata.ValueType.Toolbar) {
                    row.addAll(remaining);
                    remaining.clear();
                    break;
                }
                if (lastType != null && lastType != thisType) {
                    if (thisType == SlotMetadata.ValueType.Toolbar) {
                        break;
                    }
                    if (lastType == SlotMetadata.ValueType.Boolean && remaining.size() > columns - i) {
                        break;
                    }
                }
                final SlotMetadata column = remaining.remove(0);
                lastType = column.valueType;
                row.add(column);
            }
            row.buttons.setWidths(columnWidth);
            this.childSlots.add(row);
        }
        return this.childSlots;
    }
    
    @Override
    public Collection<SlotMetadata> getMetadata() {
        return (Collection<SlotMetadata>)Arrays.asList(this.metadata);
    }
    
    public List<SlotMetadata> getAllChildMetadata() {
        return this.childMetadataList;
    }
    
    public int getCurrentColumns() {
        return this.currentColumns;
    }
    
    public int getCurrentColumnWidth() {
        return this.currentColumnWidth;
    }
    
    public void func_192633_a(final int p_192633_1_, final int p_192633_2_, final int p_192633_3_, final float p_192633_4_) {
    }
    
    public void func_192634_a(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks) {
        this.currentSlotIndex = slotIndex;
        this.button.func_175211_a(listWidth);
        this.button.setPosition(x, y);
        this.button.setHeight(slotHeight);
        this.button.func_191745_a(this.mc, mouseX, mouseY, 0.0f);
        DrawUtil.drawRectangle(this.button.getX() + 4, this.button.getMiddleY() - 5, 11.0, 10.0, 0, 0.2f);
        DrawUtil.drawLabel(this.selected ? this.glyphOpen : this.glyphClosed, this.button.getX() + 12, this.button.getMiddleY(), DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, 0, 0.0f, this.button.getLabelColor(), 1.0f, 1.0, true);
        if (this.masterSlot != null && this.selected) {
            final boolean enabled = this.masterSlot.button.isActive();
            for (final ScrollListPane.ISlot slot : this.childSlots) {
                slot.setEnabled(enabled);
            }
        }
        if (this.button.mouseOver(mouseX, mouseY)) {
            this.currentTooltip = this.metadata;
        }
        this.currentTooltip = null;
    }
    
    private void updateButtonLabel() {
        this.button.field_146126_j = this.category.getLabel();
    }
    
    public boolean isSelected() {
        return this.selected;
    }
    
    public void setSelected(final boolean selected) {
        this.selected = selected;
    }
    
    public boolean func_148278_a(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
        if (mouseEvent == 0) {
            final boolean pressed = this.button.func_146116_c(this.mc, x, y);
            if (pressed) {
                this.selected = !this.selected;
                this.updateButtonLabel();
            }
            return pressed;
        }
        return false;
    }
    
    @Override
    public String[] mouseHover(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
        if (this.button.mouseOver(x, y)) {
            return this.metadata.getTooltip();
        }
        return new String[0];
    }
    
    public void func_148277_b(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
        this.button.func_146118_a(x, y);
    }
    
    @Override
    public boolean keyTyped(final char c, final int i) {
        return false;
    }
    
    @Override
    public int compareTo(final CategorySlot other) {
        return this.category.compareTo(other.category);
    }
    
    @Override
    public void setEnabled(final boolean enabled) {
    }
    
    @Override
    public SlotMetadata getLastPressed() {
        return null;
    }
    
    @Override
    public SlotMetadata getCurrentTooltip() {
        return this.currentTooltip;
    }
    
    @Override
    public boolean contains(final SlotMetadata slotMetadata) {
        return this.childMetadataList.contains(slotMetadata);
    }
    
    public Category getCategory() {
        return this.category;
    }
}
