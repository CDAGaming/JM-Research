package journeymap.client.ui.waypoint;

import journeymap.client.ui.component.OnOffButton;
import net.minecraft.client.Minecraft;

class SortButton extends OnOffButton {
    final WaypointManagerItem.Sort sort;
    final String labelInactive;

    public SortButton(final String label, final WaypointManagerItem.Sort sort) {
        super(String.format("%s %s", label, "\u25b2"), String.format("%s %s", label, "\u25bc"), sort.ascending);
        this.labelInactive = label;
        this.sort = sort;
    }

    @Override
    public void toggle() {
        this.sort.ascending = !this.sort.ascending;
        this.setActive(true);
    }

    @Override
    public void drawButton(final Minecraft minecraft, final int mouseX, final int mouseY, final float f) {
        super.drawButton(minecraft, mouseX, mouseY, f);
        super.drawUnderline();
    }

    public void setActive(final boolean active) {
        if (active) {
            this.setToggled(this.sort.ascending);
        } else {
            this.displayString = String.format("%s %s", this.labelInactive, " ");
        }
    }
}
