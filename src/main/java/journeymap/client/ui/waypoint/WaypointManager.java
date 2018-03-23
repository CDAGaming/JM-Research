package journeymap.client.ui.waypoint;

import journeymap.client.ui.component.*;
import journeymap.client.model.*;
import journeymap.client.*;
import journeymap.client.command.*;
import net.minecraftforge.fml.client.*;
import net.minecraft.entity.player.*;
import journeymap.client.log.*;
import java.io.*;
import journeymap.client.ui.option.*;
import net.minecraft.client.gui.*;
import journeymap.client.ui.*;
import journeymap.common.properties.*;
import journeymap.client.properties.*;
import journeymap.client.waypoint.*;
import journeymap.client.ui.fullscreen.*;
import java.util.*;

public class WaypointManager extends JmUI
{
    static final String ASCEND = "\u25b2";
    static final String DESCEND = "\u25bc";
    static final int COLWAYPOINT = 0;
    static final int COLLOCATION = 20;
    static final int COLNAME = 60;
    static final int DEFAULT_ITEMWIDTH = 460;
    private static WaypointManagerItem.Sort currentSort;
    private final String on;
    private final String off;
    protected int colWaypoint;
    protected int colLocation;
    protected int colName;
    protected int itemWidth;
    protected ScrollListPane itemScrollPane;
    protected int rowHeight;
    Boolean canUserTeleport;
    private SortButton buttonSortName;
    private SortButton buttonSortDistance;
    private DimensionsButton buttonDimensions;
    private Button buttonClose;
    private Button buttonAdd;
    private Button buttonOptions;
    private OnOffButton buttonToggleAll;
    private ButtonList bottomButtons;
    private Waypoint focusWaypoint;
    private ArrayList<WaypointManagerItem> items;
    
    public WaypointManager() {
        this(null, null);
    }
    
    public WaypointManager(final JmUI returnDisplay) {
        this(null, returnDisplay);
    }
    
    public WaypointManager(final Waypoint focusWaypoint, final JmUI returnDisplay) {
        super(Constants.getString("jm.waypoint.manage_title"), returnDisplay);
        this.on = Constants.getString("jm.common.on");
        this.off = Constants.getString("jm.common.off");
        this.colWaypoint = 0;
        this.colLocation = 20;
        this.colName = 60;
        this.itemWidth = 460;
        this.rowHeight = 16;
        this.items = new ArrayList<WaypointManagerItem>();
        this.focusWaypoint = focusWaypoint;
    }
    
    @Override
    public void func_73866_w_() {
        try {
            this.field_146292_n.clear();
            this.canUserTeleport = CmdTeleportWaypoint.isPermitted(this.field_146297_k);
            final FontRenderer fr = this.getFontRenderer();
            if (this.buttonSortDistance == null) {
                final WaypointManagerItem.Sort distanceSort = new WaypointManagerItem.DistanceComparator((EntityPlayer)FMLClientHandler.instance().getClient().field_71439_g, true);
                final String distanceLabel = Constants.getString("jm.waypoint.distance");
                (this.buttonSortDistance = new SortButton(distanceLabel, distanceSort)).setTextOnly(fr);
            }
            this.field_146292_n.add(this.buttonSortDistance);
            if (this.buttonSortName == null) {
                final WaypointManagerItem.Sort nameSort = new WaypointManagerItem.NameComparator(true);
                (this.buttonSortName = new SortButton(Constants.getString("jm.waypoint.name"), nameSort)).setTextOnly(fr);
            }
            this.field_146292_n.add(this.buttonSortName);
            if (this.buttonToggleAll == null) {
                final String enableOn = Constants.getString("jm.waypoint.enable_all", "", this.on);
                final String enableOff = Constants.getString("jm.waypoint.enable_all", "", this.off);
                (this.buttonToggleAll = new OnOffButton(enableOff, enableOn, true)).setTextOnly(this.getFontRenderer());
            }
            this.field_146292_n.add(this.buttonToggleAll);
            if (this.buttonDimensions == null) {
                this.buttonDimensions = new DimensionsButton();
            }
            if (this.buttonAdd == null) {
                (this.buttonAdd = new Button(Constants.getString("jm.waypoint.new"))).fitWidth(this.getFontRenderer());
                this.buttonAdd.func_175211_a(this.buttonAdd.getWidth() * 2);
            }
            if (this.buttonOptions == null) {
                (this.buttonOptions = new Button(Constants.getString("jm.common.options_button"))).fitWidth(this.getFontRenderer());
            }
            this.buttonClose = new Button(Constants.getString("jm.common.close"));
            this.bottomButtons = new ButtonList(new Button[] { this.buttonOptions, this.buttonAdd, this.buttonDimensions, this.buttonClose });
            this.field_146292_n.addAll(this.bottomButtons);
            if (this.items.isEmpty()) {
                this.updateItems();
                if (WaypointManager.currentSort == null) {
                    this.updateSort(this.buttonSortDistance);
                }
                else {
                    if (this.buttonSortDistance.sort.equals(WaypointManager.currentSort)) {
                        this.buttonSortDistance.sort.ascending = WaypointManager.currentSort.ascending;
                        this.buttonSortDistance.setActive(true);
                        this.buttonSortName.setActive(false);
                    }
                    if (this.buttonSortName.sort.equals(WaypointManager.currentSort)) {
                        this.buttonSortName.sort.ascending = WaypointManager.currentSort.ascending;
                        this.buttonSortName.setActive(true);
                        this.buttonSortDistance.setActive(false);
                    }
                }
            }
            if (this.itemScrollPane == null) {
                this.itemScrollPane = new ScrollListPane(this, this.field_146297_k, this.field_146294_l, this.field_146295_m, 35, this.field_146295_m - 30, 20);
            }
            else {
                this.itemScrollPane.func_148122_a(this.field_146294_l, this.field_146295_m, 35, this.field_146295_m - 30);
                this.itemScrollPane.updateSlots();
            }
            this.itemScrollPane.setSlots(this.items);
            if (!this.items.isEmpty()) {
                this.itemScrollPane.scrollTo(this.items.get(0));
            }
        }
        catch (Throwable t) {
            JMLogger.logOnce("Error in OptionsManager.initGui(): " + t, t);
        }
    }
    
    @Override
    protected void layoutButtons() {
        if (this.field_146292_n.isEmpty() || this.itemScrollPane == null) {
            this.func_73866_w_();
        }
        this.buttonToggleAll.setDrawButton(!this.items.isEmpty());
        this.buttonSortDistance.setDrawButton(!this.items.isEmpty());
        this.buttonSortName.setDrawButton(!this.items.isEmpty());
        this.bottomButtons.equalizeWidths(this.getFontRenderer());
        final int bottomButtonWidth = Math.min(this.bottomButtons.getWidth(4) + 25, this.itemScrollPane.func_148139_c());
        this.bottomButtons.equalizeWidths(this.getFontRenderer(), 4, bottomButtonWidth);
        this.bottomButtons.layoutCenteredHorizontal(this.field_146294_l / 2, this.field_146295_m - 25, true, 4);
    }
    
    @Override
    public void func_73863_a(final int x, final int y, final float par3) {
        if (this.field_146297_k == null) {
            return;
        }
        if (this.field_146292_n.isEmpty() || this.itemScrollPane == null) {
            this.func_73866_w_();
        }
        try {
            this.itemScrollPane.func_148122_a(this.field_146294_l, this.field_146295_m, 35, this.field_146295_m - 30);
            final String[] lastTooltip = this.itemScrollPane.lastTooltip;
            final long lastTooltipTime = this.itemScrollPane.lastTooltipTime;
            this.itemScrollPane.lastTooltip = null;
            this.itemScrollPane.func_148128_a(x, y, par3);
            super.func_73863_a(x, y, par3);
            if (!this.items.isEmpty()) {
                int headerY = 35 - this.getFontRenderer().field_78288_b;
                final WaypointManagerItem firstRow = this.items.get(0);
                if (firstRow.y > headerY + 16) {
                    headerY = firstRow.y - 16;
                }
                this.buttonToggleAll.centerHorizontalOn(firstRow.getButtonEnableCenterX()).setY(headerY);
                this.buttonSortDistance.centerHorizontalOn(firstRow.getLocationLeftX()).setY(headerY);
                this.colName = this.buttonSortDistance.getRightX() + 10;
                this.buttonSortName.setPosition(this.colName - 5, headerY);
            }
            this.buttonToggleAll.drawUnderline();
            for (final List<SlotMetadata> toolbar : this.getToolbars().values()) {
                for (final SlotMetadata slotMetadata : toolbar) {
                    slotMetadata.getButton().secondaryDrawButton();
                }
            }
            if (this.itemScrollPane.lastTooltip != null && Arrays.equals(this.itemScrollPane.lastTooltip, lastTooltip)) {
                this.itemScrollPane.lastTooltipTime = lastTooltipTime;
                if (System.currentTimeMillis() - this.itemScrollPane.lastTooltipTime > this.itemScrollPane.hoverDelay) {
                    final Button button = this.itemScrollPane.lastTooltipMetadata.getButton();
                    this.drawHoveringText(this.itemScrollPane.lastTooltip, x, button.getBottomY() + 15);
                }
            }
        }
        catch (Throwable t) {
            JMLogger.logOnce("Error in OptionsManager.drawScreen(): " + t, t);
        }
    }
    
    @Override
    public void func_146278_c(final int layer) {
    }
    
    protected void func_73864_a(final int mouseX, final int mouseY, final int mouseEvent) throws IOException {
        super.func_73864_a(mouseX, mouseY, mouseEvent);
        if (mouseEvent == 0) {
            final boolean pressed = this.itemScrollPane.func_148179_a(mouseX, mouseY, mouseEvent);
            if (pressed) {
                this.checkPressedButton();
            }
        }
    }
    
    @Override
    protected void func_146286_b(final int mouseX, final int mouseY, final int state) {
        super.func_146286_b(mouseX, mouseY, state);
        this.itemScrollPane.func_148181_b(mouseX, mouseY, state);
    }
    
    protected void func_146273_a(final int mouseX, final int mouseY, final int lastButtonClicked, final long timeSinceMouseClick) {
        super.func_146273_a(mouseX, mouseY, lastButtonClicked, timeSinceMouseClick);
        this.checkPressedButton();
    }
    
    public void func_146274_d() throws IOException {
        super.func_146274_d();
        this.itemScrollPane.func_178039_p();
    }
    
    protected void checkPressedButton() {
        final SlotMetadata slotMetadata = this.itemScrollPane.getLastPressed();
        if (slotMetadata != null) {}
        final ScrollListPane.ISlot parentSlot = this.itemScrollPane.getLastPressedParentSlot();
        if (parentSlot != null) {}
    }
    
    protected void func_146284_a(final GuiButton guibutton) {
        if (guibutton == this.buttonClose) {
            this.refreshAndClose();
            return;
        }
        if (guibutton == this.buttonSortName) {
            this.updateSort(this.buttonSortName);
            return;
        }
        if (guibutton == this.buttonSortDistance) {
            this.updateSort(this.buttonSortDistance);
            return;
        }
        if (guibutton == this.buttonDimensions) {
            this.buttonDimensions.nextValue();
            this.updateItems();
            this.field_146292_n.clear();
            return;
        }
        if (guibutton == this.buttonAdd) {
            final Waypoint waypoint = Waypoint.of((EntityPlayer)this.field_146297_k.field_71439_g);
            UIManager.INSTANCE.openWaypointEditor(waypoint, true, this);
            return;
        }
        if (guibutton == this.buttonToggleAll) {
            boolean state = this.buttonToggleAll.getToggled();
            state = this.toggleItems(state);
            this.buttonToggleAll.setToggled(state);
            this.field_146292_n.clear();
            return;
        }
        if (guibutton == this.buttonOptions) {
            UIManager.INSTANCE.openOptionsManager(this, ClientCategory.Waypoint, ClientCategory.WaypointBeacon);
        }
    }
    
    @Override
    protected void func_73869_a(final char c, final int i) {
        switch (i) {
            case 1: {
                this.closeAndReturn();
                break;
            }
        }
        final boolean keyUsed = this.itemScrollPane.keyTyped(c, i);
        if (keyUsed) {
            return;
        }
        if (i == 200) {
            this.itemScrollPane.func_148145_f(-this.rowHeight);
        }
        if (i == 208) {
            this.itemScrollPane.func_148145_f(this.rowHeight);
        }
        if (i == 201) {
            this.itemScrollPane.func_148145_f(-this.itemScrollPane.field_148158_l);
        }
        if (i == 209) {
            this.itemScrollPane.func_148145_f(this.itemScrollPane.field_148158_l);
        }
        if (i == 199) {
            this.itemScrollPane.func_148145_f(-this.itemScrollPane.func_148148_g());
        }
        if (i == 207) {
            this.itemScrollPane.func_148145_f(this.itemScrollPane.func_148148_g());
        }
    }
    
    protected boolean toggleItems(boolean enable) {
        for (final WaypointManagerItem item : this.items) {
            if (enable == item.waypoint.isEnable()) {
                enable = !enable;
                break;
            }
        }
        for (final WaypointManagerItem item : this.items) {
            if (item.waypoint.isEnable() != enable) {
                item.enableWaypoint(enable);
            }
        }
        return !enable;
    }
    
    protected void updateItems() {
        this.items.clear();
        final Integer currentDim = (DimensionsButton.currentWorldProvider == null) ? null : DimensionsButton.currentWorldProvider.getDimension();
        final FontRenderer fr = this.getFontRenderer();
        this.itemWidth = 0;
        final Collection<Waypoint> waypoints = WaypointStore.INSTANCE.getAll();
        boolean allOn = true;
        for (final Waypoint waypoint : waypoints) {
            final WaypointManagerItem item = new WaypointManagerItem(waypoint, fr, this);
            item.getDistanceTo((EntityPlayer)this.field_146297_k.field_71439_g);
            if (currentDim == null || item.waypoint.getDimensions().contains(currentDim)) {
                this.items.add(item);
                if (!allOn) {
                    continue;
                }
                allOn = waypoint.isEnable();
            }
        }
        if (this.items.isEmpty()) {
            this.itemWidth = 460;
        }
        else {
            this.itemWidth = this.items.get(0).internalWidth;
        }
        this.buttonToggleAll.setToggled(!allOn);
        this.updateCount();
        if (WaypointManager.currentSort != null) {
            Collections.sort(this.items, WaypointManager.currentSort);
        }
    }
    
    protected void updateSort(final SortButton sortButton) {
        for (final GuiButton button : this.field_146292_n) {
            if (button instanceof SortButton) {
                if (button == sortButton) {
                    if (sortButton.sort.equals(WaypointManager.currentSort)) {
                        sortButton.toggle();
                    }
                    else {
                        sortButton.setActive(true);
                    }
                    WaypointManager.currentSort = sortButton.sort;
                }
                else {
                    ((SortButton)button).setActive(false);
                }
            }
        }
        if (WaypointManager.currentSort != null) {
            Collections.sort(this.items, WaypointManager.currentSort);
        }
        if (this.itemScrollPane != null) {
            this.itemScrollPane.setSlots(this.items);
        }
    }
    
    protected void updateCount() {
        final String itemCount = this.items.isEmpty() ? "" : Integer.toString(this.items.size());
        final String enableOn = Constants.getString("jm.waypoint.enable_all", itemCount, this.on);
        final String enableOff = Constants.getString("jm.waypoint.enable_all", itemCount, this.off);
        this.buttonToggleAll.setLabels(enableOff, enableOn);
    }
    
    protected boolean isSelected(final WaypointManagerItem item) {
        return this.itemScrollPane.func_148131_a(item.getSlotIndex());
    }
    
    protected int getMargin() {
        return (this.field_146294_l > this.itemWidth + 2) ? ((this.field_146294_l - this.itemWidth) / 2) : 0;
    }
    
    public void removeWaypoint(final WaypointManagerItem item) {
        WaypointStore.INSTANCE.remove(item.waypoint);
        this.items.remove(item);
    }
    
    protected void refreshAndClose() {
        this.closeAndReturn();
    }
    
    @Override
    protected void closeAndReturn() {
        this.bottomButtons.setEnabled(false);
        WaypointStore.INSTANCE.bulkSave();
        Fullscreen.state().requireRefresh();
        this.bottomButtons.setEnabled(true);
        if (this.returnDisplay == null) {
            UIManager.INSTANCE.closeAll();
        }
        else {
            UIManager.INSTANCE.open(this.returnDisplay);
        }
    }
    
    Map<Category, List<SlotMetadata>> getToolbars() {
        return (Map<Category, List<SlotMetadata>>)Collections.EMPTY_MAP;
    }
}
