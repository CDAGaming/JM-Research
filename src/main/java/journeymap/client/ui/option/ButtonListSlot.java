package journeymap.client.ui.option;

import journeymap.client.ui.component.Button;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import journeymap.client.ui.component.*;
import net.minecraftforge.fml.client.*;
import java.awt.*;
import journeymap.client.render.draw.*;
import com.google.common.base.*;
import java.util.*;
import java.util.List;

public class ButtonListSlot implements ScrollListPane.ISlot, Comparable<ButtonListSlot>
{
    static int hgap;
    Minecraft mc;
    FontRenderer fontRenderer;
    ButtonList buttons;
    HashMap<Button, SlotMetadata> buttonOptionMetadata;
    CategorySlot parent;
    SlotMetadata lastPressed;
    SlotMetadata currentToolTip;
    Integer colorToolbarBgStart;
    Integer colorToolbarBgEnd;
    
    public ButtonListSlot(final CategorySlot parent) {
        this.mc = FMLClientHandler.instance().getClient();
        this.fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
        this.buttons = new ButtonList();
        this.buttonOptionMetadata = new HashMap<Button, SlotMetadata>();
        this.lastPressed = null;
        this.currentToolTip = null;
        this.colorToolbarBgStart = new Color(0, 0, 100).getRGB();
        this.colorToolbarBgEnd = new Color(0, 0, 100).getRGB();
        this.parent = parent;
    }
    
    public ButtonListSlot add(final SlotMetadata slotMetadata) {
        this.buttons.add(slotMetadata.getButton());
        this.buttonOptionMetadata.put(slotMetadata.getButton(), slotMetadata);
        return this;
    }
    
    public ButtonListSlot addAll(final Collection<SlotMetadata> slotMetadataCollection) {
        for (final SlotMetadata slotMetadata : slotMetadataCollection) {
            this.add(slotMetadata);
        }
        return this;
    }
    
    public ButtonListSlot merge(final ButtonListSlot other) {
        for (final SlotMetadata otherSlot : other.buttonOptionMetadata.values()) {
            this.add(otherSlot);
        }
        return this;
    }
    
    public void clear() {
        this.buttons.clear();
        this.buttonOptionMetadata.clear();
    }
    
    @Override
    public Collection<SlotMetadata> getMetadata() {
        return (Collection<SlotMetadata>)this.buttonOptionMetadata.values();
    }
    
    public void updatePosition(final int slotIndex, final int x, final int y, final float partialTicks) {
    }
    
    public void drawEntry(final int slotIndex, int x, final int y, int listWidth, final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks) {
        int margin = 0;
        if (this.parent.getCurrentColumnWidth() > 0) {
            final int cols = listWidth / this.parent.currentColumnWidth;
            margin = (listWidth - (ButtonListSlot.hgap * cols - 1 + cols * this.parent.getCurrentColumnWidth())) / 2;
            x += margin;
            listWidth -= margin * 2;
        }
        SlotMetadata tooltipMetadata = null;
        if (this.buttons.size() > 0) {
            this.buttons.setHeights(slotHeight);
            if (this.buttonOptionMetadata.get((this.buttons).get(0)).isToolbar()) {
                this.buttons.fitWidths(this.fontRenderer);
                this.buttons.layoutHorizontal(x + listWidth - ButtonListSlot.hgap, y, false, ButtonListSlot.hgap);
                DrawUtil.drawGradientRect(x, y, listWidth, slotHeight, this.colorToolbarBgStart, 0.15f, this.colorToolbarBgEnd, 0.6f);
            }
            else {
                this.buttons.setWidths(this.parent.currentColumnWidth);
                this.buttons.layoutHorizontal(x, y, true, ButtonListSlot.hgap);
            }
            for (final Button button : this.buttons) {
                button.drawButton(this.mc, mouseX, mouseY, 0.0f);
                if (tooltipMetadata == null && button.mouseOver(mouseX, mouseY)) {
                    tooltipMetadata = this.buttonOptionMetadata.get(button);
                }
            }
        }
        this.currentToolTip = tooltipMetadata;
    }
    
    public boolean mousePressed(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
        if (mouseEvent == 0) {
            for (final Button button : this.buttons) {
                if (button.mousePressed(this.mc, x, y)) {
                    this.lastPressed = this.buttonOptionMetadata.get(button);
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public String[] mouseHover(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
        for (final Button button : this.buttons) {
            if (button.mouseOver(x, y)) {
                return this.buttonOptionMetadata.get(button).getTooltip();
            }
        }
        return new String[0];
    }
    
    public void mouseReleased(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
        for (final Button button : this.buttons) {
            button.mouseReleased(x, y);
        }
    }
    
    @Override
    public boolean keyTyped(final char c, final int i) {
        for (final SlotMetadata slot : this.buttonOptionMetadata.values()) {
            if (slot.button.isMouseOver() && slot.button.keyTyped(c, i)) {
                this.lastPressed = slot;
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void setEnabled(final boolean enabled) {
        for (final SlotMetadata slot : this.buttonOptionMetadata.values()) {
            if (!slot.isMasterPropertyForCategory()) {
                slot.button.setEnabled(enabled);
            }
        }
    }
    
    @Override
    public List<ScrollListPane.ISlot> getChildSlots(final int listWidth, final int columnWidth) {
        return (List<ScrollListPane.ISlot>)Collections.EMPTY_LIST;
    }
    
    @Override
    public SlotMetadata getLastPressed() {
        return this.lastPressed;
    }
    
    @Override
    public SlotMetadata getCurrentTooltip() {
        return this.currentToolTip;
    }
    
    @Override
    public int getColumnWidth() {
        this.buttons.equalizeWidths(this.fontRenderer);
        return this.buttons.get(0).getWidth();
    }
    
    @Override
    public boolean contains(final SlotMetadata slotMetadata) {
        return this.buttonOptionMetadata.values().contains(slotMetadata);
    }
    
    protected String getFirstButtonString() {
        if (this.buttons.size() > 0) {
            return this.buttons.get(0).displayString;
        }
        return null;
    }
    
    @Override
    public int compareTo(final ButtonListSlot o) {
        final String buttonString = this.getFirstButtonString();
        final String otherButtonString = o.getFirstButtonString();
        if (!Strings.isNullOrEmpty(buttonString)) {
            return buttonString.compareTo(otherButtonString);
        }
        if (!Strings.isNullOrEmpty(otherButtonString)) {
            return 1;
        }
        return 0;
    }
    
    static {
        ButtonListSlot.hgap = 8;
    }
}
