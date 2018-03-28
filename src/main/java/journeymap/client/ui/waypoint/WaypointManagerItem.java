package journeymap.client.ui.waypoint;

import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.command.CmdTeleportWaypoint;
import journeymap.client.model.Waypoint;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.OnOffButton;
import journeymap.client.ui.component.ScrollListPane;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.option.SlotMetadata;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.awt.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class WaypointManagerItem implements ScrollListPane.ISlot {
    static Integer background;
    static Integer backgroundHover;

    static {
        WaypointManagerItem.background = new Color(20, 20, 20).getRGB();
        WaypointManagerItem.backgroundHover = new Color(40, 40, 40).getRGB();
    }

    final FontRenderer fontRenderer;
    final WaypointManager manager;
    int x;
    int y;
    int width;
    int internalWidth;
    Integer distance;
    Waypoint waypoint;
    OnOffButton buttonEnable;
    Button buttonRemove;
    Button buttonEdit;
    Button buttonFind;
    Button buttonTeleport;
    Button buttonChat;
    int hgap;
    ButtonList buttonListLeft;
    ButtonList buttonListRight;
    int slotIndex;
    SlotMetadata<Waypoint> slotMetadata;

    public WaypointManagerItem(final Waypoint waypoint, final FontRenderer fontRenderer, final WaypointManager manager) {
        this.hgap = 4;
        final int id = 0;
        this.waypoint = waypoint;
        this.fontRenderer = fontRenderer;
        this.manager = manager;
        final SlotMetadata<Waypoint> slotMetadata = new SlotMetadata<Waypoint>(null, null, null, false);
        final String on = Constants.getString("jm.common.on");
        final String off = Constants.getString("jm.common.off");
        (this.buttonEnable = new OnOffButton(on, off, true)).setToggled(waypoint.isEnable());
        this.buttonFind = new Button(Constants.getString("jm.waypoint.find"));
        this.buttonTeleport = new Button(Constants.getString("jm.waypoint.teleport"));
        final JourneymapClient jm = Journeymap.getClient();
        if (jm.isServerEnabled()) {
            this.buttonTeleport.setDrawButton(jm.isServerTeleportEnabled());
            this.buttonTeleport.setEnabled(jm.isServerTeleportEnabled());
        } else {
            this.buttonTeleport.setDrawButton(manager.canUserTeleport);
            this.buttonTeleport.setEnabled(manager.canUserTeleport);
        }
        (this.buttonListLeft = new ButtonList(new Button[]{this.buttonEnable, this.buttonFind, this.buttonTeleport})).setHeights(manager.rowHeight);
        this.buttonListLeft.fitWidths(fontRenderer);
        this.buttonEdit = new Button(Constants.getString("jm.waypoint.edit"));
        this.buttonRemove = new Button(Constants.getString("jm.waypoint.remove"));
        (this.buttonChat = new Button(Constants.getString("jm.waypoint.chat"))).setTooltip(Constants.getString("jm.waypoint.chat.tooltip"));
        (this.buttonListRight = new ButtonList(new Button[]{this.buttonChat, this.buttonEdit, this.buttonRemove})).setHeights(manager.rowHeight);
        this.buttonListRight.fitWidths(fontRenderer);
        this.internalWidth = fontRenderer.getCharWidth('X') * 32;
        this.internalWidth += Math.max(manager.colLocation, manager.colName);
        this.internalWidth += this.buttonListLeft.getWidth(this.hgap);
        this.internalWidth += this.buttonListRight.getWidth(this.hgap);
        this.internalWidth += 10;
    }

    public int getSlotIndex() {
        return this.slotIndex;
    }

    public void setSlotIndex(final int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public void setPosition(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public int getFitWidth(final FontRenderer fr) {
        return this.width;
    }

    public int getHeight() {
        return this.manager.rowHeight;
    }

    public void drawPartialScrollable(final Minecraft mc, final int x, final int y, final int width, final int height) {
        DrawUtil.drawRectangle(this.x, this.y, this.width, this.manager.rowHeight, WaypointManagerItem.background, 0.4f);
    }

    protected void drawLabels(final Minecraft mc, final int x, final int y, Integer color) {
        if (this.waypoint == null) {
            return;
        }
        final boolean waypointValid = this.waypoint.isEnable() && this.waypoint.isInPlayerDimension();
        if (color == null) {
            color = (waypointValid ? this.waypoint.getSafeColor() : 8421504);
        }
        final FontRenderer fr = FMLClientHandler.instance().getClient().fontRenderer;
        final int yOffset = 1 + (this.manager.rowHeight - fr.FONT_HEIGHT) / 2;
        fr.drawStringWithShadow(String.format("%sm", this.getDistance()), (float) (x + this.manager.colLocation), (float) (y + yOffset), (int) color);
        final String name = waypointValid ? this.waypoint.getName() : (TextFormatting.STRIKETHROUGH + this.waypoint.getName());
        fr.drawStringWithShadow(name, (float) this.manager.colName, (float) (y + yOffset), (int) color);
    }

    protected void drawWaypoint(final int x, final int y) {
        final TextureImpl wpTexture = this.waypoint.getTexture();
        DrawUtil.drawColoredImage(wpTexture, this.waypoint.getColor(), 1.0f, x, y - wpTexture.getHeight() / 2, 0.0);
    }

    protected void enableWaypoint(final boolean enable) {
        this.buttonEnable.setToggled(enable);
        this.waypoint.setEnable(enable);
    }

    protected int getButtonEnableCenterX() {
        return this.buttonEnable.getCenterX();
    }

    protected int getNameLeftX() {
        return this.x + this.manager.getMargin() + this.manager.colName;
    }

    protected int getLocationLeftX() {
        return this.x + this.manager.getMargin() + this.manager.colLocation;
    }

    public boolean clickScrollable(final int mouseX, final int mouseY) {
        boolean mouseOver = false;
        if (this.waypoint == null) {
            return false;
        }
        if (this.buttonChat.mouseOver(mouseX, mouseY)) {
            FMLClientHandler.instance().getClient().displayGuiScreen((GuiScreen) new WaypointChat(this.waypoint));
            mouseOver = true;
        } else if (this.buttonRemove.mouseOver(mouseX, mouseY)) {
            this.manager.removeWaypoint(this);
            this.waypoint = null;
            mouseOver = true;
        } else if (this.buttonEnable.mouseOver(mouseX, mouseY)) {
            this.buttonEnable.toggle();
            this.waypoint.setEnable(this.buttonEnable.getToggled());
            if (this.waypoint.isDirty()) {
                WaypointStore.INSTANCE.save(this.waypoint);
            }
            mouseOver = true;
        } else if (this.buttonEdit.mouseOver(mouseX, mouseY)) {
            UIManager.INSTANCE.openWaypointEditor(this.waypoint, false, this.manager);
            mouseOver = true;
        } else if (this.buttonFind.isEnabled() && this.buttonFind.mouseOver(mouseX, mouseY)) {
            UIManager.INSTANCE.openFullscreenMap(this.waypoint);
            mouseOver = true;
        } else if (this.manager.canUserTeleport && this.buttonTeleport.mouseOver(mouseX, mouseY)) {
            new CmdTeleportWaypoint(this.waypoint).run();
            Fullscreen.state().follow.set(true);
            UIManager.INSTANCE.closeAll();
            mouseOver = true;
        }
        return mouseOver;
    }

    public int getDistance() {
        return (this.distance == null) ? 0 : this.distance;
    }

    public int getDistanceTo(final EntityPlayer player) {
        if (this.distance == null) {
            this.distance = (int) player.getPositionVector().distanceTo(this.waypoint.getPosition());
        }
        return this.distance;
    }

    @Override
    public Collection<SlotMetadata> getMetadata() {
        return null;
    }

    public void updatePosition(final int slotIndex, final int x, final int y, final float partialTicks) {
    }

    public void drawEntry(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks) {
        final Minecraft mc = this.manager.getMinecraft();
        this.width = listWidth;
        this.setPosition(x, y);
        if (this.waypoint == null) {
            return;
        }
        final boolean hover = this.manager.isSelected(this) || (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.manager.rowHeight);
        this.buttonListLeft.setOptions(true, hover, true);
        this.buttonListRight.setOptions(true, hover, true);
        final Integer color = hover ? WaypointManagerItem.backgroundHover : WaypointManagerItem.background;
        final float alpha = hover ? 1.0f : 0.4f;
        DrawUtil.drawRectangle(this.x, this.y, this.width, this.manager.rowHeight, color, alpha);
        final int margin = this.manager.getMargin();
        this.drawWaypoint(this.x + margin + this.manager.colWaypoint, this.y + this.manager.rowHeight / 2);
        this.drawLabels(mc, this.x + margin, this.y, null);
        this.buttonFind.setEnabled(this.waypoint.isInPlayerDimension());
        this.buttonTeleport.setEnabled(this.waypoint.isTeleportReady());
        this.buttonListRight.layoutHorizontal(x + this.width - margin, y, false, this.hgap).draw(mc, mouseX, mouseY);
        this.buttonListLeft.layoutHorizontal(this.buttonListRight.getLeftX() - this.hgap * 2, y, false, this.hgap).draw(mc, mouseX, mouseY);
    }

    public boolean mousePressed(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
        return this.clickScrollable(x, y);
    }

    @Override
    public String[] mouseHover(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
        for (final Button button : this.buttonListLeft) {
            if (button.isMouseOver()) {
                this.manager.drawHoveringText(button.getTooltip(), x, y, FMLClientHandler.instance().getClient().fontRenderer);
            }
        }
        return new String[0];
    }

    public void mouseReleased(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
    }

    @Override
    public boolean keyTyped(final char c, final int i) {
        return false;
    }

    @Override
    public List<ScrollListPane.ISlot> getChildSlots(final int listWidth, final int columnWidth) {
        return null;
    }

    @Override
    public SlotMetadata getLastPressed() {
        return null;
    }

    @Override
    public SlotMetadata getCurrentTooltip() {
        return null;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.buttonEnable.setToggled(this.waypoint.isEnable());
    }

    @Override
    public int getColumnWidth() {
        return this.width;
    }

    @Override
    public boolean contains(final SlotMetadata slotMetadata) {
        return false;
    }

    abstract static class Sort implements Comparator<WaypointManagerItem> {
        boolean ascending;

        Sort(final boolean ascending) {
            this.ascending = ascending;
        }

        @Override
        public boolean equals(final Object o) {
            return this == o || (o != null && this.getClass() == o.getClass());
        }

        @Override
        public int hashCode() {
            return this.ascending ? 1 : 0;
        }
    }

    static class NameComparator extends Sort {
        public NameComparator(final boolean ascending) {
            super(ascending);
        }

        @Override
        public int compare(final WaypointManagerItem o1, final WaypointManagerItem o2) {
            if (this.ascending) {
                return o1.waypoint.getName().compareToIgnoreCase(o2.waypoint.getName());
            }
            return o2.waypoint.getName().compareToIgnoreCase(o1.waypoint.getName());
        }
    }

    static class DistanceComparator extends Sort {
        EntityPlayer player;

        public DistanceComparator(final EntityPlayer player, final boolean ascending) {
            super(ascending);
            this.player = player;
        }

        @Override
        public int compare(final WaypointManagerItem o1, final WaypointManagerItem o2) {
            final double dist1 = o1.getDistanceTo(this.player);
            final double dist2 = o2.getDistanceTo(this.player);
            if (this.ascending) {
                return Double.compare(dist1, dist2);
            }
            return Double.compare(dist2, dist1);
        }
    }
}
