package journeymap.client.ui.waypoint;

import journeymap.client.api.display.*;
import journeymap.client.ui.option.*;
import journeymap.common.*;
import journeymap.client.command.*;
import journeymap.client.*;
import net.minecraft.client.entity.*;
import net.minecraft.client.*;
import journeymap.client.render.draw.*;
import journeymap.client.ui.component.*;
import net.minecraft.util.text.*;
import journeymap.client.waypoint.*;
import journeymap.client.render.texture.*;
import net.minecraftforge.fml.client.*;
import net.minecraft.client.gui.*;
import journeymap.client.ui.*;
import journeymap.client.ui.fullscreen.*;
import net.minecraft.entity.player.*;
import java.awt.*;
import java.util.*;

public class WaypointManagerItem implements ScrollListPane.ISlot
{
    static Integer background;
    static Integer backgroundHover;
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
    final boolean canUserTeleport;
    
    public WaypointManagerItem(final Waypoint waypoint, final FontRenderer fontRenderer, final WaypointManager manager) {
        this.hgap = 4;
        final int id = 0;
        this.waypoint = waypoint;
        this.fontRenderer = fontRenderer;
        this.manager = manager;
        boolean tpAllowed = false;
        final Integer currentDimension = this.getCurrentDimension();
        if (manager.canUserTeleport) {
            final EntityPlayerSP player = Journeymap.clientPlayer();
            tpAllowed = (player != null && currentDimension != null && CmdTeleportWaypoint.isPermitted(player.field_71093_bK, currentDimension));
        }
        this.canUserTeleport = tpAllowed;
        final String on = Constants.getString("jm.common.on");
        final String off = Constants.getString("jm.common.off");
        this.buttonEnable = new OnOffButton(on, off, true);
        if (currentDimension != null) {
            this.buttonEnable.setToggled(waypoint.isDisplayed(currentDimension));
            this.buttonEnable.setEnabled(true);
        }
        else {
            this.buttonEnable.setEnabled(false);
        }
        (this.buttonFind = new Button(Constants.getString("jm.waypoint.find"))).setLabelColors(14737632, 16777120, 6710886);
        (this.buttonTeleport = new Button(Constants.getString("jm.waypoint.teleport"))).setDrawButton(manager.canUserTeleport);
        this.buttonTeleport.setLabelColors(14737632, 16777120, 6710886);
        if (manager.canUserTeleport && !this.canUserTeleport) {
            this.buttonTeleport.setTooltip(Constants.getString("jm.waypoint.teleport.dim_error"));
        }
        (this.buttonListLeft = new ButtonList(new Button[] { this.buttonEnable, this.buttonFind, this.buttonTeleport })).setHeights(manager.rowHeight);
        this.buttonListLeft.fitWidths(fontRenderer);
        this.buttonEdit = new Button(Constants.getString("jm.waypoint.edit"));
        this.buttonRemove = new Button(Constants.getString("jm.waypoint.remove"));
        (this.buttonChat = new Button(Constants.getString("jm.waypoint.chat"))).setTooltip(Constants.getString("jm.waypoint.chat.tooltip"));
        (this.buttonListRight = new ButtonList(new Button[] { this.buttonChat, this.buttonEdit, this.buttonRemove })).setHeights(manager.rowHeight);
        this.buttonListRight.fitWidths(fontRenderer);
        this.internalWidth = fontRenderer.func_78263_a('X') * 32;
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
    
    protected void drawLabels(final int x, final int y) {
        if (this.waypoint == null) {
            return;
        }
        boolean waypointValid = true;
        final Integer currentDimension = this.getCurrentDimension();
        if (currentDimension != null) {
            waypointValid = this.waypoint.isDisplayed(currentDimension);
        }
        final int color = waypointValid ? this.waypoint.getOrDefaultLabelColor(16777215) : 8421504;
        final FontRenderer fr = JmUI.fontRenderer();
        final int yOffset = 1 + (this.manager.rowHeight - fr.field_78288_b) / 2;
        fr.func_175063_a(String.format("%sm", this.getDistance()), (float)(x + this.manager.colLocation), (float)(y + yOffset), color);
        final String name = waypointValid ? this.waypoint.getName() : (TextFormatting.STRIKETHROUGH + this.waypoint.getName());
        fr.func_175063_a(name, (float)this.manager.colName, (float)(y + yOffset), color);
    }
    
    protected void drawWaypoint(final int x, final int y) {
        final TextureImpl wpTexture = TextureCache.getTexture(WaypointStore.getWaypointIcon(this.waypoint).getImageLocation());
        DrawUtil.drawColoredImage(wpTexture, this.waypoint.getOrDefaultIconColor(16777215), 1.0f, x, y - wpTexture.getHeight() / 2, 0.0);
    }
    
    protected void enableWaypoint(final int dim, final boolean enable) {
        this.buttonEnable.setToggled(enable);
        this.waypoint.setDisplayed(dim, enable);
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
            FMLClientHandler.instance().getClient().func_147108_a((GuiScreen)new WaypointChat(this.waypoint));
            mouseOver = true;
        }
        else if (this.buttonRemove.mouseOver(mouseX, mouseY)) {
            this.manager.removeWaypoint(this);
            this.waypoint = null;
            mouseOver = true;
        }
        else if (this.buttonEnable.mouseOver(mouseX, mouseY)) {
            this.buttonEnable.toggle();
            this.setEnabled(this.buttonEnable.getToggled());
            mouseOver = true;
        }
        else if (this.buttonEdit.mouseOver(mouseX, mouseY)) {
            UIManager.INSTANCE.openWaypointEditor(this.waypoint, false, this.manager);
            mouseOver = true;
        }
        else if (this.buttonFind.isEnabled() && this.buttonFind.mouseOver(mouseX, mouseY)) {
            UIManager.INSTANCE.openFullscreenMap(this.waypoint);
            mouseOver = true;
        }
        else if (this.canUserTeleport && this.buttonTeleport.mouseOver(mouseX, mouseY)) {
            Integer targetDim = this.getCurrentDimension();
            if (targetDim == null) {
                targetDim = this.waypoint.getDimension();
            }
            new CmdTeleportWaypoint(this.waypoint, targetDim).run();
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
            this.distance = (int)player.func_174791_d().func_72438_d(this.waypoint.getVec(player.field_71093_bK));
        }
        return this.distance;
    }
    
    @Override
    public Collection<SlotMetadata> getMetadata() {
        return null;
    }
    
    public void func_192634_a(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks) {
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
        this.drawLabels(this.x + margin, this.y);
        this.buttonTeleport.setEnabled(this.canUserTeleport);
        this.buttonFind.setEnabled(this.waypoint.isDisplayed(Fullscreen.state().getDimension()));
        this.buttonListRight.layoutHorizontal(x + this.width - margin, y, false, this.hgap).draw(mc, mouseX, mouseY);
        this.buttonListLeft.layoutHorizontal(this.buttonListRight.getLeftX() - this.hgap * 2, y, false, this.hgap).draw(mc, mouseX, mouseY);
    }
    
    public void setSelected(final int p_178011_1_, final int p_178011_2_, final int p_178011_3_, final float partialTicks) {
    }
    
    public void func_192633_a(final int p_192633_1_, final int p_192633_2_, final int p_192633_3_, final float p_192633_4_) {
    }
    
    public boolean func_148278_a(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
        return this.clickScrollable(x, y);
    }
    
    @Override
    public String[] mouseHover(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
        for (final Button button : this.buttonListLeft) {
            if (button.func_146115_a()) {
                this.manager.drawHoveringText(button.getTooltip(), x, y, JmUI.fontRenderer());
            }
        }
        return new String[0];
    }
    
    public void func_148277_b(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
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
        this.buttonEnable.setToggled(enabled);
        final Integer currentDimension = this.getCurrentDimension();
        if (currentDimension != null) {
            this.waypoint.setDisplayed(currentDimension, this.buttonEnable.getToggled());
            if (this.waypoint.isDirty()) {
                WaypointStore.INSTANCE.save(this.waypoint);
            }
        }
    }
    
    @Override
    public int getColumnWidth() {
        return this.width;
    }
    
    @Override
    public boolean contains(final SlotMetadata slotMetadata) {
        return false;
    }
    
    private Integer getCurrentDimension() {
        if (DimensionsButton.currentWorldProvider != null) {
            return DimensionsButton.currentWorldProvider.getDimension();
        }
        return null;
    }
    
    static {
        WaypointManagerItem.background = new Color(20, 20, 20).getRGB();
        WaypointManagerItem.backgroundHover = new Color(40, 40, 40).getRGB();
    }
    
    abstract static class Sort implements Comparator<WaypointManagerItem>
    {
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
    
    static class NameComparator extends Sort
    {
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
    
    static class DistanceComparator extends Sort
    {
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
