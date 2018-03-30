package journeymap.client.ui;

import org.apache.logging.log4j.*;
import journeymap.client.ui.minimap.*;
import net.minecraft.client.*;
import journeymap.common.*;
import net.minecraftforge.fml.client.*;
import journeymap.common.log.*;
import journeymap.client.log.*;
import journeymap.client.properties.*;
import journeymap.client.ui.component.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.entity.player.*;
import net.minecraft.client.entity.*;
import net.minecraft.client.settings.*;
import net.minecraft.client.gui.*;
import journeymap.client.ui.fullscreen.*;
import journeymap.client.api.display.*;
import journeymap.common.properties.*;
import journeymap.client.ui.waypoint.*;
import journeymap.client.ui.dialog.*;

public enum UIManager
{
    INSTANCE;
    
    private final Logger logger;
    private final MiniMap miniMap;
    Minecraft minecraft;
    
    private UIManager() {
        this.logger = Journeymap.getLogger();
        this.minecraft = FMLClientHandler.instance().getClient();
        MiniMap tmp;
        try {
            final int preset = Journeymap.getClient().getMiniMapProperties1().isActive() ? 1 : 2;
            tmp = new MiniMap(Journeymap.getClient().getMiniMapProperties(preset));
        }
        catch (Throwable e) {
            this.logger.error("Unexpected error: " + LogFormatter.toString(e));
            if (e instanceof LinkageError) {
                ChatLog.announceError(e.getMessage() + " : JourneyMap is not compatible with this build of Forge!");
            }
            tmp = new MiniMap(new MiniMapProperties(1));
        }
        this.miniMap = tmp;
    }
    
    public static void handleLinkageError(final LinkageError error) {
        Journeymap.getLogger().error(LogFormatter.toString(error));
        try {
            ChatLog.announceError(error.getMessage() + " : JourneyMap is not compatible with this build of Forge!");
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public void closeAll() {
        try {
            this.closeCurrent();
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable e2) {
            this.logger.error("Unexpected error: " + LogFormatter.toString(e2));
        }
        this.minecraft.func_147108_a((GuiScreen)null);
        this.minecraft.func_71381_h();
    }
    
    public void closeCurrent() {
        try {
            if (this.minecraft.field_71462_r != null && this.minecraft.field_71462_r instanceof JmUI) {
                this.logger.debug("Closing " + this.minecraft.field_71462_r.getClass());
                ((JmUI)this.minecraft.field_71462_r).close();
            }
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable e2) {
            this.logger.error("Unexpected error: " + LogFormatter.toString(e2));
        }
    }
    
    public void openInventory() {
        this.logger.debug("Opening inventory");
        this.closeAll();
        final EntityPlayerSP player = Journeymap.clientPlayer();
        if (player == null) {
            return;
        }
        this.minecraft.func_147108_a((GuiScreen)new GuiInventory((EntityPlayer)player));
    }
    
    public <T extends JmUI> T open(final Class<T> uiClass, final JmUI returnDisplay) {
        try {
            return this.open(uiClass.getConstructor(JmUI.class).newInstance(returnDisplay));
        }
        catch (LinkageError e) {
            handleLinkageError(e);
            return null;
        }
        catch (Throwable e2) {
            try {
                return this.open(uiClass.getConstructor((Class<?>[])new Class[0]).newInstance(new Object[0]));
            }
            catch (Throwable e3) {
                this.logger.error("1st unexpected exception creating UI: " + LogFormatter.toString(e2));
                this.logger.error("2nd unexpected exception creating UI: " + LogFormatter.toString(e3));
                this.closeCurrent();
                return null;
            }
        }
    }
    
    public <T extends JmUI> T open(final Class<T> uiClass) {
        try {
            if (MiniMap.uiState().active) {
                MiniMap.updateUIState(false);
            }
            final T ui = uiClass.newInstance();
            return this.open(ui);
        }
        catch (LinkageError e) {
            handleLinkageError(e);
            return null;
        }
        catch (Throwable e2) {
            this.logger.error("Unexpected exception creating UI: " + LogFormatter.toString(e2));
            this.closeCurrent();
            return null;
        }
    }
    
    public <T extends GuiScreen> T open(final T ui) {
        this.closeCurrent();
        this.logger.debug("Opening UI " + ui.getClass().getSimpleName());
        try {
            this.minecraft.func_147108_a((GuiScreen)ui);
            KeyBinding.func_74506_a();
        }
        catch (LinkageError e) {
            handleLinkageError(e);
            return null;
        }
        catch (Throwable t) {
            this.logger.error(String.format("Unexpected exception opening UI %s: %s", ui.getClass(), LogFormatter.toString(t)));
        }
        return ui;
    }
    
    public void toggleMinimap() {
        try {
            this.setMiniMapEnabled(!this.isMiniMapEnabled());
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable t) {
            this.logger.error(String.format("Unexpected exception in toggleMinimap: %s", LogFormatter.toString(t)));
        }
    }
    
    public boolean isMiniMapEnabled() {
        try {
            return this.miniMap.getCurrentMinimapProperties().enabled.get();
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable t) {
            this.logger.error(String.format("Unexpected exception in isMiniMapEnabled: %s", LogFormatter.toString(t)));
        }
        return false;
    }
    
    public void setMiniMapEnabled(final boolean enable) {
        try {
            this.miniMap.getCurrentMinimapProperties().enabled.set(Boolean.valueOf(enable));
            this.miniMap.getCurrentMinimapProperties().save();
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable t) {
            this.logger.error(String.format("Unexpected exception in setMiniMapEnabled: %s", LogFormatter.toString(t)));
        }
    }
    
    public void drawMiniMap() {
        this.minecraft.field_71424_I.func_76320_a("journeymap");
        try {
            boolean doDraw = false;
            if (this.miniMap.getCurrentMinimapProperties().enabled.get()) {
                final GuiScreen currentScreen = this.minecraft.field_71462_r;
                doDraw = (currentScreen == null || currentScreen instanceof GuiChat);
                if (doDraw) {
                    if (!MiniMap.uiState().active) {
                        if (MiniMap.state().getLastMapTypeChange() == 0L) {
                            this.miniMap.reset();
                        }
                        else {
                            MiniMap.state().requireRefresh();
                        }
                    }
                    this.miniMap.drawMap();
                }
            }
            if (doDraw && !MiniMap.uiState().active) {
                MiniMap.updateUIState(true);
            }
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable e2) {
            Journeymap.getLogger().error("Error drawing minimap: " + LogFormatter.toString(e2));
        }
        finally {
            this.minecraft.field_71424_I.func_76319_b();
        }
    }
    
    public MiniMap getMiniMap() {
        return this.miniMap;
    }
    
    public Fullscreen openFullscreenMap() {
        if (this.minecraft.field_71462_r instanceof Fullscreen) {
            return (Fullscreen)this.minecraft.field_71462_r;
        }
        KeyBinding.func_74506_a();
        return this.open(Fullscreen.class);
    }
    
    public void openFullscreenMap(final Waypoint waypoint) {
        try {
            final EntityPlayerSP player = Journeymap.clientPlayer();
            if (player != null && waypoint.isDisplayed(player.field_71093_bK)) {
                final Fullscreen map = this.open(Fullscreen.class);
                map.centerOn(waypoint);
            }
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable e2) {
            Journeymap.getLogger().error("Error opening map on waypoint: " + LogFormatter.toString(e2));
        }
    }
    
    public void openOptionsManager() {
        this.open(OptionsManager.class);
    }
    
    public void openOptionsManager(final JmUI returnDisplay, final Category... initialCategories) {
        try {
            this.open(new OptionsManager(returnDisplay, initialCategories));
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable e2) {
            this.logger.error("Unexpected exception creating MasterOptions with return class: " + LogFormatter.toString(e2));
        }
    }
    
    public void openSplash(final JmUI returnDisplay) {
        this.open(AboutDialog.class, returnDisplay);
    }
    
    public void openWaypointManager(final Waypoint waypoint, final JmUI returnDisplay) {
        try {
            final WaypointManager manager = new WaypointManager(waypoint, returnDisplay);
            this.open(manager);
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable e2) {
            Journeymap.getLogger().error("Error opening waypoint manager: " + LogFormatter.toString(e2));
        }
    }
    
    public void openWaypointEditor(final Waypoint waypoint, final boolean isNew, final JmUI returnDisplay) {
        try {
            final WaypointEditor editor = new WaypointEditor(waypoint, isNew, returnDisplay);
            this.open(editor);
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable e2) {
            Journeymap.getLogger().error("Error opening waypoint editor: " + LogFormatter.toString(e2));
        }
    }
    
    public void openGridEditor(final JmUI returnDisplay) {
        try {
            final GridEditor editor = new GridEditor(returnDisplay);
            this.open(editor);
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable e2) {
            Journeymap.getLogger().error("Error opening grid editor: " + LogFormatter.toString(e2));
        }
    }
    
    public void reset() {
        try {
            Fullscreen.state().requireRefresh();
            this.miniMap.reset();
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable e2) {
            Journeymap.getLogger().error("Error during reset: " + LogFormatter.toString(e2));
        }
    }
    
    public void switchMiniMapPreset() {
        try {
            final int currentPreset = this.miniMap.getCurrentMinimapProperties().getId();
            this.switchMiniMapPreset((currentPreset == 1) ? 2 : 1);
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable e2) {
            Journeymap.getLogger().error("Error during switchMiniMapPreset: " + LogFormatter.toString(e2));
        }
    }
    
    public void switchMiniMapPreset(final int which) {
        try {
            this.miniMap.setMiniMapProperties(Journeymap.getClient().getMiniMapProperties(which));
            MiniMap.state().requireRefresh();
        }
        catch (LinkageError e) {
            handleLinkageError(e);
        }
        catch (Throwable e2) {
            Journeymap.getLogger().error("Error during switchMiniMapPreset: " + LogFormatter.toString(e2));
        }
    }
}
