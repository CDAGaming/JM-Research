package journeymap.client.ui;

import journeymap.client.data.WaypointsData;
import journeymap.client.log.ChatLog;
import journeymap.client.model.Waypoint;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.dialog.AboutDialog;
import journeymap.client.ui.dialog.GridEditor;
import journeymap.client.ui.dialog.OptionsManager;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.client.ui.waypoint.WaypointEditor;
import journeymap.client.ui.waypoint.WaypointManager;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public enum UIManager {
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
        } catch (Throwable e) {
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
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void closeAll() {
        try {
            this.closeCurrent();
        } catch (LinkageError e) {
            handleLinkageError(e);
        } catch (Throwable e2) {
            this.logger.error("Unexpected error: " + LogFormatter.toString(e2));
        }
        this.minecraft.displayGuiScreen((GuiScreen) null);
        this.minecraft.setIngameFocus();
    }

    public void closeCurrent() {
        try {
            if (this.minecraft.currentScreen != null && this.minecraft.currentScreen instanceof JmUI) {
                this.logger.debug("Closing " + this.minecraft.currentScreen.getClass());
                ((JmUI) this.minecraft.currentScreen).close();
            }
        } catch (LinkageError e) {
            handleLinkageError(e);
        } catch (Throwable e2) {
            this.logger.error("Unexpected error: " + LogFormatter.toString(e2));
        }
    }

    public void openInventory() {
        this.logger.debug("Opening inventory");
        this.closeAll();
        this.minecraft.displayGuiScreen((GuiScreen) new GuiInventory((EntityPlayer) this.minecraft.player));
    }

    public <T extends JmUI> T open(final Class<T> uiClass, final JmUI returnDisplay) {
        try {
            return this.open(uiClass.getConstructor(JmUI.class).newInstance(returnDisplay));
        } catch (LinkageError e) {
            handleLinkageError(e);
            return null;
        } catch (Throwable e2) {
            try {
                return this.open(uiClass.getConstructor((Class<?>[]) new Class[0]).newInstance(new Object[0]));
            } catch (Throwable e3) {
                this.logger.log(Level.ERROR, "1st unexpected exception creating UI: " + LogFormatter.toString(e2));
                this.logger.log(Level.ERROR, "2nd unexpected exception creating UI: " + LogFormatter.toString(e3));
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
        } catch (LinkageError e) {
            handleLinkageError(e);
            return null;
        } catch (Throwable e2) {
            this.logger.log(Level.ERROR, "Unexpected exception creating UI: " + LogFormatter.toString(e2));
            this.closeCurrent();
            return null;
        }
    }

    public <T extends GuiScreen> T open(final T ui) {
        this.closeCurrent();
        this.logger.debug("Opening UI " + ui.getClass().getSimpleName());
        try {
            this.minecraft.displayGuiScreen((GuiScreen) ui);
            KeyBinding.unPressAllKeys();
        } catch (LinkageError e) {
            handleLinkageError(e);
            return null;
        } catch (Throwable t) {
            this.logger.error(String.format("Unexpected exception opening UI %s: %s", ui.getClass(), LogFormatter.toString(t)));
        }
        return ui;
    }

    public void toggleMinimap() {
        try {
            this.setMiniMapEnabled(!this.isMiniMapEnabled());
        } catch (LinkageError e) {
            handleLinkageError(e);
        } catch (Throwable t) {
            this.logger.error(String.format("Unexpected exception in toggleMinimap: %s", LogFormatter.toString(t)));
        }
    }

    public boolean isMiniMapEnabled() {
        try {
            return this.miniMap.getCurrentMinimapProperties().enabled.get();
        } catch (LinkageError e) {
            handleLinkageError(e);
        } catch (Throwable t) {
            this.logger.error(String.format("Unexpected exception in isMiniMapEnabled: %s", LogFormatter.toString(t)));
        }
        return false;
    }

    public void setMiniMapEnabled(final boolean enable) {
        try {
            this.miniMap.getCurrentMinimapProperties().enabled.set(Boolean.valueOf(enable));
            this.miniMap.getCurrentMinimapProperties().save();
        } catch (LinkageError e) {
            handleLinkageError(e);
        } catch (Throwable t) {
            this.logger.error(String.format("Unexpected exception in setMiniMapEnabled: %s", LogFormatter.toString(t)));
        }
    }

    public void drawMiniMap() {
        this.minecraft.mcProfiler.startSection("journeymap");
        try {
            boolean doDraw = false;
            if (this.miniMap.getCurrentMinimapProperties().enabled.get()) {
                final GuiScreen currentScreen = this.minecraft.currentScreen;
                doDraw = (currentScreen == null || currentScreen instanceof GuiChat);
                if (doDraw) {
                    if (!MiniMap.uiState().active) {
                        if (MiniMap.state().getLastMapTypeChange() == 0L) {
                            this.miniMap.reset();
                        } else {
                            MiniMap.state().requireRefresh();
                        }
                    }
                    this.miniMap.drawMap();
                }
            }
            if (doDraw && !MiniMap.uiState().active) {
                MiniMap.updateUIState(true);
            }
        } catch (LinkageError e) {
            handleLinkageError(e);
        } catch (Throwable e2) {
            Journeymap.getLogger().error("Error drawing minimap: " + LogFormatter.toString(e2));
        } finally {
            this.minecraft.mcProfiler.endSection();
        }
    }

    public MiniMap getMiniMap() {
        return this.miniMap;
    }

    public Fullscreen openFullscreenMap() {
        if (this.minecraft.currentScreen instanceof Fullscreen) {
            return (Fullscreen) this.minecraft.currentScreen;
        }
        KeyBinding.unPressAllKeys();
        return this.open(Fullscreen.class);
    }

    public void openFullscreenMap(final Waypoint waypoint) {
        try {
            if (waypoint.isInPlayerDimension()) {
                final Fullscreen map = this.open(Fullscreen.class);
                map.centerOn(waypoint);
            }
        } catch (LinkageError e) {
            handleLinkageError(e);
        } catch (Throwable e2) {
            Journeymap.getLogger().error("Error opening map on waypoint: " + LogFormatter.toString(e2));
        }
    }

    public void openOptionsManager() {
        this.open(OptionsManager.class);
    }

    public void openOptionsManager(final JmUI returnDisplay, final Category... initialCategories) {
        try {
            this.open(new OptionsManager(returnDisplay, initialCategories));
        } catch (LinkageError e) {
            handleLinkageError(e);
        } catch (Throwable e2) {
            this.logger.log(Level.ERROR, "Unexpected exception creating MasterOptions with return class: " + LogFormatter.toString(e2));
        }
    }

    public void openSplash(final JmUI returnDisplay) {
        this.open(AboutDialog.class, returnDisplay);
    }

    public void openWaypointManager(final Waypoint waypoint, final JmUI returnDisplay) {
        if (WaypointsData.isManagerEnabled()) {
            try {
                final WaypointManager manager = new WaypointManager(waypoint, returnDisplay);
                this.open(manager);
            } catch (LinkageError e) {
                handleLinkageError(e);
            } catch (Throwable e2) {
                Journeymap.getLogger().error("Error opening waypoint manager: " + LogFormatter.toString(e2));
            }
        }
    }

    public void openWaypointEditor(final Waypoint waypoint, final boolean isNew, final JmUI returnDisplay) {
        if (WaypointsData.isManagerEnabled()) {
            try {
                final WaypointEditor editor = new WaypointEditor(waypoint, isNew, returnDisplay);
                this.open(editor);
            } catch (LinkageError e) {
                handleLinkageError(e);
            } catch (Throwable e2) {
                Journeymap.getLogger().error("Error opening waypoint editor: " + LogFormatter.toString(e2));
            }
        }
    }

    public void openGridEditor(final JmUI returnDisplay) {
        try {
            final GridEditor editor = new GridEditor(returnDisplay);
            this.open(editor);
        } catch (LinkageError e) {
            handleLinkageError(e);
        } catch (Throwable e2) {
            Journeymap.getLogger().error("Error opening grid editor: " + LogFormatter.toString(e2));
        }
    }

    public void reset() {
        try {
            Fullscreen.state().requireRefresh();
            this.miniMap.reset();
        } catch (LinkageError e) {
            handleLinkageError(e);
        } catch (Throwable e2) {
            Journeymap.getLogger().error("Error during reset: " + LogFormatter.toString(e2));
        }
    }

    public void switchMiniMapPreset() {
        try {
            final int currentPreset = this.miniMap.getCurrentMinimapProperties().getId();
            this.switchMiniMapPreset((currentPreset == 1) ? 2 : 1);
        } catch (LinkageError e) {
            handleLinkageError(e);
        } catch (Throwable e2) {
            Journeymap.getLogger().error("Error during switchMiniMapPreset: " + LogFormatter.toString(e2));
        }
    }

    public void switchMiniMapPreset(final int which) {
        try {
            this.miniMap.setMiniMapProperties(Journeymap.getClient().getMiniMapProperties(which));
            MiniMap.state().requireRefresh();
        } catch (LinkageError e) {
            handleLinkageError(e);
        } catch (Throwable e2) {
            Journeymap.getLogger().error("Error during switchMiniMapPreset: " + LogFormatter.toString(e2));
        }
    }
}
